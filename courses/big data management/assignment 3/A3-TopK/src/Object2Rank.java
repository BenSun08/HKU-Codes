import java.time.Instant;

public class Object2Rank implements Comparable<Object2Rank> { 
    private int[] bitmap = { 0, 0 }; // indecates the sources of the object
    private boolean inTopK = false;
    private Instant timestamp; // the timestamp of the last update

    public int id;
    public float score = 0.0f;
    public float raScore = 0.0f;

    public Object2Rank(int id, float score, int source, float raScore) {
        this.id = id;
        this.score = score;
        this.bitmap[source] = 1;
        this.raScore = raScore;
        this.timestamp = Instant.now();
    }

    @Override
    public int compareTo(Object2Rank o) {
        int comp = Float.compare(this.score, o.score);
        if (comp != 0) {
            return comp;
        } else {
            // if the scores are equal, compare the timestamps
            return -this.timestamp.compareTo(o.timestamp);
        }
    }

    public String getBitmap() {
      return this.bitmap[0] + "" + this.bitmap[1];
    }

    public void setBitmap(int pos, int val) {
        this.bitmap[pos] = val;
    }

    public boolean isInTopK() {
        return this.inTopK;
    }

    public void setInTopK(boolean inTopK) {
        this.inTopK = inTopK;
        this.timestamp = Instant.now();
    }
}
