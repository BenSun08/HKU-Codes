public class App {
    static private int BLOCK_SIZE = 4;
    public static void main(String[] args) throws Exception {
        QueryOperation queryOperation = new QueryOperation("input/R.tsv", "input/T.tsv", "output/RjoinT.tsv", BLOCK_SIZE);
        // 1. Merge-join
        queryOperation.mergeJoin();
        // 2. Union
        queryOperation.setInnerFile("input/S.tsv");
        queryOperation.setOutputFile("output/RunionS.tsv");
        queryOperation.reset();
        queryOperation.union();
        // 3. Intersection
        queryOperation.setOutputFile("output/RintersectionS.tsv");
        queryOperation.reset();
        queryOperation.intersection();
        // 4. Set Difference
        queryOperation.setOutputFile("output/RdifferenceS.tsv");
        queryOperation.reset();
        queryOperation.setDifference();
    }
}
