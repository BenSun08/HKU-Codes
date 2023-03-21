import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.IOException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;

public class RTree {
  private int maxCapacity;
  private double minRate;
  private TreeNode root;
  private ArrayList<ArrayList<TreeNode>> levels;
  HashMap<Integer, TreeNode> idToNode = new HashMap<Integer, TreeNode>();

  public RTree(int maxCapacity, double minRate) {
    this.maxCapacity = maxCapacity;
    this.minRate = minRate;
    this.levels = new ArrayList<ArrayList<TreeNode>>();
  }

  private ArrayList<TreeNode> bulkLoadingHelper(ArrayList<Entry> entries, int isNonLeaf) {
    ArrayList<TreeNode> nodes = new ArrayList<TreeNode>();
    if(entries.size() <= maxCapacity) {
      this.root = new TreeNode(1, entries);
      nodes.add(this.root);
    } else {
      // create nodes from entries
      for(int i = 0; i < entries.size();) {
        TreeNode node = new TreeNode(isNonLeaf);
        for(int j = 0; i < entries.size() && j < maxCapacity; j++) {
          node.addEntry(entries.get(i));
          i++;
        }
        nodes.add(node);
      }
      // if number of entries in last node is less than minRate * maxCapacity,
      // then borrow entries from the previous node
      TreeNode lastNode = nodes.get(nodes.size() - 1);
      if(lastNode.size() < minRate * maxCapacity) {
        TreeNode prevNode = nodes.get(nodes.size() - 2);
        int num = (int) (minRate * maxCapacity - lastNode.size());
        for(int i = 0; i < num; i++) {
          Entry e = prevNode.pop();
          lastNode.prepend(e);
        }
      }
      // assign ids to nodes
      if(isNonLeaf == 0) {
        for(int i = 0; i < nodes.size(); i++) {
          nodes.get(i).setId(i);
        }
      } else {
        int startId = entries.get(entries.size() - 1).getId() + 1;
        for(int i = 0; i < nodes.size(); i++) {
          nodes.get(i).setId(startId + i);
        }
      }
    }
    return nodes;
  }

  public void bulkLoading(HeapFile disk) {
    if(levels.size() > 0) {
      System.out.println("The tree has already been built.");
      return;
    }
    ArrayList<Polygon> polygons = disk.getPolygons();
    ArrayList<Entry> entries = new ArrayList<Entry>();
    for (int i = 0; i < polygons.size(); i++) {
      entries.add(new Entry(polygons.get(i)));
    }
    ArrayList<TreeNode> nodes = this.bulkLoadingHelper(entries, 0);
    while(nodes.size() > 1) {
      this.levels.add(nodes);
      entries.clear();
      for(int i = 0; i < nodes.size(); i++) {
        entries.add(new Entry(nodes.get(i)));
      }
      nodes = this.bulkLoadingHelper(entries, 1);
    }
    this.levels.add(nodes);

    // print the tree
    for(int i = 0; i < levels.size(); i++) {
      System.out.println(String.format("%d nodes at level %d", levels.get(i).size(), i));
    }
  }

  public void writeTree(String fileName) throws IOException {
    BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
    // write the tree to the file
    for(int i = 0; i < levels.size(); i++) {
      ArrayList<TreeNode> nodes = levels.get(i);
      for(int j = 0; j < nodes.size(); j++) {
        TreeNode node = nodes.get(j);
        String line = node.toString();
        writer.write(line + "\n");
      }
    }

    writer.close();
  }

  private TreeNode parseLine(String line) {
    Matcher m = Pattern.compile("\\[(.*?)\\]").matcher(line);
    TreeNode node = null;
    int isNonLeaf = 0;
    if(m.find()) {
      String[] rootLineSplit = m.group(1).split(", ");
      isNonLeaf = Integer.parseInt(rootLineSplit[0]);
      node = new TreeNode(isNonLeaf);
      node.setId(Integer.parseInt(rootLineSplit[1]));
      int entryId = Integer.parseInt(rootLineSplit[2].substring(2));
      if (isNonLeaf == 0) {
        ArrayList<Double> mbr = new ArrayList<Double>();
        mbr.add(Double.parseDouble(rootLineSplit[3].substring(1)));
        for(int i= 0; i<3; i++) {
          mbr.add(Double.parseDouble(rootLineSplit[4+i]));
        }

        node.addEntry(new Entry(entryId, mbr, isNonLeaf));
      } else {
        node.addEntry(new Entry(idToNode.get(entryId)));
      }
    }
    if (isNonLeaf == 0) {
      while (m.find()) {
        String[] entryLineSplit = m.group(1).split(", ");
        int entryId = Integer.parseInt(entryLineSplit[0]);
        ArrayList<Double> mbr = new ArrayList<Double>();
        mbr.add(Double.parseDouble(entryLineSplit[1].substring(1)));
        for(int i= 0; i<3; i++) {
          mbr.add(Double.parseDouble(entryLineSplit[2+i]));
        }
        node.addEntry(new Entry(entryId, mbr, isNonLeaf));
      }
    } else {
      while (m.find()) {
        String[] entryLineSplit = m.group(1).split(", ");
        int entryId = Integer.parseInt(entryLineSplit[0]);
        node.addEntry(new Entry(idToNode.get(entryId)));
      }
    }
    return node;
  }

  public void readTreeFromFile(String fileName) throws IOException {
    if(this.levels.size() > 0) {
      System.out.println("The tree has already been built.");
      return;
    }
    // read the tree from the file
    BufferedReader reader = new BufferedReader(new FileReader(fileName));
    String line;
    ArrayList<TreeNode> nodes = new ArrayList<TreeNode>();
    TreeNode node = new TreeNode(0);
    while((line = reader.readLine()) != null) {
      node = this.parseLine(line);
      idToNode.put(node.getId(), node);
      if(node.getIsNonLeaf() == 0) {
        nodes.add(node);
      } else {
        this.levels.add(nodes);
        break;
      }
    }
    ArrayList<TreeNode> nonLeafNodes = new ArrayList<TreeNode>();
    nonLeafNodes.add(node);
    while((line = reader.readLine()) != null) {
      node = this.parseLine(line);
      idToNode.put(node.getId(), node);
      nonLeafNodes.add(node);
    }
    this.root = node;
    this.levels.add(nonLeafNodes);

    reader.close();
  }

  // type ("r": recursive, "i": iterative)
  public void rangeQuery(String fileName, String type) throws IOException {
    if(type != "i" && type != "r") {
      System.out.println("Invalid type. Please choose from 'r' and 'i'.");
      return;
    }
    BufferedReader reader = new BufferedReader(new FileReader(fileName));
    // BufferedWriter writer = new BufferedWriter(new FileWriter("output/RqueriesResults.txt"));

    String line;
    int lineIdx = 0;
    while((line = reader.readLine()) != null) {
      String[] querySplit = line.split(" ");
      double xLow = Double.parseDouble(querySplit[0]);
      double yLow = Double.parseDouble(querySplit[1]);
      double xHigh = Double.parseDouble(querySplit[2]);
      double yHigh = Double.parseDouble(querySplit[3]);

      ArrayList<Double> window = new ArrayList<Double>();
      window.add(xLow);
      window.add(yLow);
      window.add(xHigh);
      window.add(yHigh);

      ArrayList<Integer> results = new ArrayList<Integer>();

      if(type == "i") {
        Stack<TreeNode> stack = new Stack<TreeNode>();
        stack.push(this.root);
        while(!stack.empty()) {
          TreeNode node = stack.pop();
          if(node.getIsNonLeaf() == 0) {
            for(int i = 0; i < node.getEntries().size(); i++) {
              Entry entry = node.getEntries().get(i);
              if(entry.isOverlap(window)) {
                results.add(entry.getId());
              }
            }
          } else {
            for(int i = node.getEntries().size() - 1; i >= 0; i--) {
              Entry entry = node.getEntries().get(i);
              if(entry.isOverlap(window)) {
                stack.push(entry.getNode());
              }
            }
          }
        }
      } else {
        this.rangeQueryRecursive(this.root.getId(), window, results);
      }
      
      String resultStr = String.format("%d (%d): ", lineIdx, results.size());
      for(int i = 0; i < results.size(); i++) {
        if(i == results.size() - 1) {
          resultStr += results.get(i);
        } else {
          resultStr += results.get(i) + ",";
        }
      }
      // writer.write(resultStr + "\n");
      System.out.println(resultStr);

      lineIdx++;
    }

    reader.close();
    // writer.close();
  }

  public int getRootId() {
    return this.root.getId();
  }

  private void rangeQueryRecursive(int nodeId, ArrayList<Double> window, ArrayList<Integer> results) {
    TreeNode node = this.idToNode.get(nodeId);
    if(node.getIsNonLeaf() == 0) {
      for(int i = 0; i < node.getEntries().size(); i++) {
        Entry entry = node.getEntries().get(i);
        if(entry.isOverlap(window)) {
          results.add(entry.getId());
        }
      }
    } else {
      for(int i = 0; i < node.getEntries().size(); i++) {
        Entry entry = node.getEntries().get(i);
        if(entry.isOverlap(window)) {
          rangeQueryRecursive(entry.getNode().getId(), window, results);
        }
      }
    }
  }

  public void kNNQueries(String fileName, int k) throws IOException {
    BufferedReader reader = new BufferedReader(new FileReader(fileName));
    // BufferedWriter writer = new BufferedWriter(new FileWriter("output/kNNqueriesResults.txt"));
    String line;
    int lineIdx = 0;

    while((line = reader.readLine()) != null) {
      String[] lineSplit = line.split(" ");
      double x = Double.parseDouble(lineSplit[0]);
      double y = Double.parseDouble(lineSplit[1]);

      ArrayList<Integer> kNNs = new ArrayList<Integer>();
      PriorityQueue<Entry> pQueue = new PriorityQueue<Entry>(Comparator.comparingDouble(e -> e.distance(x, y)));
      
      for(int i = 0; i < this.root.getEntries().size(); i++) {
        pQueue.add(this.root.getEntries().get(i));
      }

      while(pQueue.size() > 0 && kNNs.size() < k) {
        Entry entry = pQueue.poll();

        if(entry.getIsNonLeaf() == 0) {
          kNNs.add(entry.getId());
        } else {
          for(int i = 0; i < entry.getNode().getEntries().size(); i++) {
            Entry childEntry = entry.getNode().getEntries().get(i);
            pQueue.add(childEntry);
          }
        }
      }

      String kNNsStr = String.format("%d (%d): ", lineIdx, kNNs.size());
      for(int i = 0; i < kNNs.size(); i++) {
        if(i == kNNs.size() - 1) {
          kNNsStr += kNNs.get(i);
        } else {
          kNNsStr += kNNs.get(i) + ",";
        }
      }
      // writer.write(kNNsStr + "\n");
      System.out.println(kNNsStr);
      lineIdx++;
    }

    reader.close();
    // writer.close();
  }
}
