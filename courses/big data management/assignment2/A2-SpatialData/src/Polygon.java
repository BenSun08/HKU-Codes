import java.util.ArrayList;
import java.awt.geom.Point2D;

public class Polygon {
  private ArrayList<Point2D.Double> points;
  private int id;
  private String zOrderCode;
  private Double minX = Double.POSITIVE_INFINITY, minY = Double.POSITIVE_INFINITY, maxX = Double.NEGATIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;


  public Polygon(int id) {
    this.points = new ArrayList<Point2D.Double>();
    this.id = id;
  }

  public int getId() {
    return this.id;
  }

  public void addPoint(Double x, Double y) {
    this.points.add(new Point2D.Double(x, y));
    if (x < this.minX) {
      this.minX = x;
    }
    if (x > this.maxX) {
      this.maxX = x;
    }
    if (y < this.minY) {
      this.minY = y;
    }
    if (y > this.maxY) {
      this.maxY = y;
    }
  }

  public ArrayList<Double> getMBR() {
    ArrayList<Double> mbr = new ArrayList<Double>();
    mbr.add(this.minX);
    mbr.add(this.maxX);
    mbr.add(this.minY);
    mbr.add(this.maxY);
    return mbr;
  }

  public void calZOrderCode() {
    double centerX = (this.maxX + this.minX) / 2;
    double centerY = (this.maxY + this.minY) / 2;
    this.zOrderCode = "";
    
  }

  public String getZOrderCode() {
    return this.zOrderCode;
  }
}

