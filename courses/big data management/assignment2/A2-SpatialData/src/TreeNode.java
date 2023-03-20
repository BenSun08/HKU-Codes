import java.util.ArrayList;

public class TreeNode {
  private int id;
  private int isNonLeaf;
  private ArrayList<Entry> entries;
  private Double minX = Double.POSITIVE_INFINITY, minY = Double.POSITIVE_INFINITY, maxX = Double.NEGATIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;

  public TreeNode(int isNonLeaf) {
    this.isNonLeaf = isNonLeaf;
    this.entries = new ArrayList<Entry>();
  }

  public TreeNode(int isNonLeaf, ArrayList<Entry> entries) {
    this.isNonLeaf = isNonLeaf;
    this.entries = entries;
    if(entries.size() > 0) {
      this.id = entries.get(entries.size() - 1).getId() + 1;
    }
    this.recalMBR();
  }

  private void updateMBR(Entry entry) {
    ArrayList<Double> mbr = entry.getMBR();
    if (mbr.get(0) < this.minX) {
      this.minX = mbr.get(0);
    }
    if (mbr.get(1) > this.maxX) {
      this.maxX = mbr.get(1);
    }
    if (mbr.get(2) < this.minY) {
      this.minY = mbr.get(2);
    }
    if (mbr.get(3) > this.maxY) {
      this.maxY = mbr.get(3);
    }
  }

  private void recalMBR() {
    this.minX = Double.POSITIVE_INFINITY;
    this.minY = Double.POSITIVE_INFINITY;
    this.maxX = Double.NEGATIVE_INFINITY;
    this.maxY = Double.NEGATIVE_INFINITY;
    for (int i = 0; i < this.entries.size(); i++) {
      ArrayList<Double> mbr = this.entries.get(i).getMBR();
      if (mbr.get(0) < this.minX) {
        this.minX = mbr.get(0);
      }
      if (mbr.get(1) > this.maxX) {
        this.maxX = mbr.get(1);
      }
      if (mbr.get(2) < this.minY) {
        this. minY = mbr.get(2);
      }
      if (mbr.get(3) > this.maxY) {
        this.maxY = mbr.get(3);
      }
    }
  }
  
  // push entry to the end of the node
  public void addEntry(Entry entry) {
    this.entries.add(entry);
    this.updateMBR(entry);
  }

  // push entry to the beginning of the node
  public void prepend(Entry entry) {
    this.entries.add(0, entry);
    this.updateMBR(entry);
  }

  // pop entry from the end of the node and return it
  public Entry pop() {
    Entry tail = this.entries.get(this.entries.size() - 1);
    this.recalMBR();
    return tail;
  }

  public int getIsNonLeaf() {
    return this.isNonLeaf;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getId() {
    return this.id;
  }

  public ArrayList<Double> getMBR() {
    ArrayList<Double> mbr = new ArrayList<Double>();
    mbr.add(this.minX);
    mbr.add(this.maxX);
    mbr.add(this.minY);
    mbr.add(this.maxY);
    return mbr;
  }

  public ArrayList<Entry> getEntries() {
    return this.entries;
  }

  public int size() {
    return this.entries.size();
  }

  public String toString() {
    String str = String.format("[%d, $s, [", this.isNonLeaf, this.id);
    for (int i = 0; i < this.entries.size(); i++) {
      str += this.entries.get(i).toString();
      if (i != this.entries.size() - 1) {
        str += ", ";
      }
    }
    str += "]]";
    return str;
  }
}
