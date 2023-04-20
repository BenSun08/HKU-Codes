import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.PriorityQueue;

public class LARA {
  private int k;
  private BufferedReader reader1;
  private BufferedReader reader2;
  private ArrayList<Float> randAccessSource = new ArrayList<Float>();
  private HashMap<Integer, Object2Rank> seenObjects = new HashMap<Integer, Object2Rank>();
  private PriorityQueue<Object2Rank> topK; // the top-k list

  private float[] usUpperBound = { 5.0f, 5.0f }; // the last upper bound of the unseen objects
  private float T = 15.0f; // the threshold
  private float maxSeenObjUpperBound = 15.0f; // the upper bound of the seen objects
  private HashMap<String, PriorityQueue<Object2Rank>> lattice = new HashMap<String, PriorityQueue<Object2Rank>>();


  public LARA(int k, String fileName1, String fileName2, String raFileName) throws IOException{
    this.k = k;
    this.reader1 = new BufferedReader(new FileReader(fileName1));
    this.reader2 = new BufferedReader(new FileReader(fileName2));
    this.topK = new PriorityQueue<Object2Rank>(k);
    this.lattice.put("01", new PriorityQueue<Object2Rank>(Collections.reverseOrder()));
    this.lattice.put("10", new PriorityQueue<Object2Rank>(Collections.reverseOrder()));

    BufferedReader raReader = new BufferedReader(new FileReader(raFileName));
    String line = null;
    while ((line = raReader.readLine()) != null) {
      String[] obj = line.split(" ");
      float score = this.roundFloat(Float.parseFloat(obj[1]), 2);
      this.randAccessSource.add(score);
    }
    raReader.close();
  }

  public void printTopK() {
    System.out.println("Top k objects:");
    ArrayList<Object2Rank> topKArr = new ArrayList<Object2Rank>();
    
    while(this.topK.size() > 0) {
      Object2Rank o = this.topK.poll();
      topKArr.add(0, o);
    }
    for (int i = 0; i < topKArr.size(); i++) {
      Object2Rank o = topKArr.get(i);
      System.out.println(String.format("%d: %.2f", o.id, o.score));
      this.topK.add(o);
    }
  }

  private Object2Rank putObj2TopK(Object2Rank o2r) {
    if (this.topK.size() < this.k) {
      if(!o2r.isInTopK()) {
        this.topK.add(o2r);
        o2r.setInTopK(true);
      }
      return null;
    } else {
      if (Float.compare(o2r.score, this.topK.peek().score) > 0) {
        if(o2r.isInTopK()) { 
          // this is the problem in java's priority queue, you need to trigger the reosorting of the heap
          // by removing and adding the object
          this.topK.remove(o2r);
          this.topK.add(o2r);
          return null;
        } else {
          o2r.setInTopK(true);
          Object2Rank last = this.topK.poll();
          last.setInTopK(false);
          this.topK.add(o2r);
          return last;
        }
      }
      return null;
    }
  }
 
  private float roundFloat(float num, int decimalPlaces) {
    String s = String.format("%." + decimalPlaces + "f", num);
    return Float.parseFloat(s);
  }

  public void execute() throws IOException {
    int accessCount = 0;
    boolean growing = true;

    while (true) {
      int source = accessCount % 2; // the source
      String line = source == 0 ? this.reader1.readLine() : this.reader2.readLine();
      if (line == null) {
        break;
      }
      accessCount++;

      String[] obj = line.split(" ");
      int id = Integer.parseInt(obj[0]);
      float score = Float.parseFloat(obj[1]);

      if (growing) {
        Object2Rank o2r;
        if(this.seenObjects.containsKey(id)) { // if the object has been seen before, update the score
          o2r = this.seenObjects.get(id);
          o2r.score = this.roundFloat(o2r.score + score, 2);
          o2r.setBitmap(source, 1);
          this.seenObjects.remove(id, o2r);
        } else { // otherwise, create a new object
          float raScore = this.randAccessSource.get(id);
          o2r = new Object2Rank(id, this.roundFloat(score + raScore, 2), source, raScore);
          this.seenObjects.put(id, o2r);
        }

        this.putObj2TopK(o2r);

        this.usUpperBound[source] = score;
        // update upper bound of unseen objects
        this.T = this.roundFloat(this.usUpperBound[0] + this.usUpperBound[1] + 5.0f, 2);
        // if the lower bound of the top-k list is larger than or equal to the upper bound of unseen objects, stop growing
        if (Float.compare(this.T, this.topK.peek().score) <= 0) {
          growing = false;
          // update the upper bound of the seen objects
          this.seenObjects.forEach((k, v) -> {
            lattice.get(v.getBitmap()).add(v);
          });
        }
      } else { // shringking phase
        if (this.seenObjects.containsKey(id)) {
          Object2Rank o2r = this.seenObjects.get(id);
          this.lattice.get(o2r.getBitmap()).remove(o2r);

          o2r.score = this.roundFloat(o2r.score + score, 2);
          o2r.setBitmap(source, 1);
          this.putObj2TopK(o2r);
        }
        String partialBitmap = source == 0 ? "01" : "10";
        float updatedMaxUb = this.roundFloat(this.lattice.get(partialBitmap).peek().score + score, 2);
        String theOtherBitmap = partialBitmap.equals("01") ? "10" : "01";
        float theOtherMaxUb = this.roundFloat(this.lattice.get(theOtherBitmap).peek().score + usUpperBound[(source + 1) % 2], 2);

        this.maxSeenObjUpperBound = Math.max(updatedMaxUb, theOtherMaxUb);
        if (Float.compare(this.maxSeenObjUpperBound, this.topK.peek().score) <= 0) {
          break;
        }
        usUpperBound[source] = score;
      }
    }
    this.reader1.close();
    this.reader2.close();

    System.out.println("Number of sequential accesses = " + accessCount);
    this.printTopK();
  }
}
