import java.util.ArrayList;

public class Utils {
  static public ArrayList<Double> _DIVISORS = new ArrayList<Double>();
  static {
    for(int i = 0; i < 32; i++) {
      Utils._DIVISORS.add(180.0 / Math.pow(2, i));
    }
  }
}
