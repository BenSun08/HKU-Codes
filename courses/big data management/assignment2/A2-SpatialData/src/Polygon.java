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
    if(centerX > 180) {
      centerX = (centerX % 180) + 180.0;
    } else if (centerX < -180){
      centerX = (-((-centerX) % 180)) + 180.0;
    } else {
      centerX = centerX + 180.0;
    }
    if(centerY > 90) {
      centerY = (centerY % 90) + 90.0;
    } else if (centerY < -90){
      centerY = (-((-centerY) % 90)) + 90.0;
    } else {
      centerY = centerY + 90.0;
    }

    this.zOrderCode = "";
    int z = 0;
    for (int i = 0; i < 32; i++) {
      double dx = Utils._DIVISORS.get(i);
      int digit = 0;
      if (centerY >= dx) {
        digit |= 2;
        centerY -=dx;
      }
      if (centerX >= dx) {
        digit |= 1;
        centerX -=dx;
      }
      this.zOrderCode += Integer.toString(digit);
    }
  }

  public String getZOrderCode() {
    return this.zOrderCode;
  }
}

