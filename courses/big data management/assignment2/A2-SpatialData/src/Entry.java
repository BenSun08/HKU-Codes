import java.util.ArrayList;

public class Entry {
  private int id;
  private Polygon obj;
  private TreeNode ptr;
  private ArrayList<Double> mbr;


  public Entry(Polygon obj) {
    this.obj = obj;
    this.id = obj.getId();
    this.mbr = obj.getMBR();
  }

  public Entry(TreeNode ptr) {
    this.ptr = ptr;
    this.id = ptr.getId();
    this.mbr = ptr.getMBR();
  }

  public Entry(int id, ArrayList<Double> mbr) {
    this.id = id;
    this.mbr = mbr;
  }

  public int getId() {
    return this.id;
  }

  public ArrayList<Double> getMBR() {
    return this.mbr;
  }

  public String toString() {
    return String.format("[%d, [%s, %s, %s, %s]]", this.id, this.mbr.get(0), this.mbr.get(1), this.mbr.get(2), this.mbr.get(3));
  }
}
