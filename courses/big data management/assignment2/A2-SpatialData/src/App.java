public class App {
    static int MAX_CAPACITY = 20;
    static double MIN_FILL = 0.4;
    public static void main(String[] args) throws Exception {
        System.out.println("Please select the part of the codes you want to run (available options: 1, 2, 3): ");
        if(Integer.parseInt(args[0]) == 1) {
            // part 1
            HeapFile heapFile = new HeapFile();
            System.out.println("Please input the file path of the coordinates of points: ");
            String coordsFileName = args[1];
            System.out.println("Please input the file path of the offsets of polygon objects: ");
            String offsetFileName = args[2];
            heapFile.readFromFiles(coordsFileName, offsetFileName);

            RTree rTree = new RTree(MAX_CAPACITY, MIN_FILL);
            rTree.bulkLoading(heapFile);
            rTree.writeTree("output/Rtree.txt");
        } else if(Integer.parseInt(args[0]) == 2) {
            // part 2
            RTree rTree = new RTree(MAX_CAPACITY, MIN_FILL);
            System.out.println("Please input the file path of the Rtree: ");
            String rTreeFileName = args[1];
            rTree.readTreeFromFile(rTreeFileName);
             rTree.writeTree("output/Rtree2.txt");

            System.out.println("Please input the file path of the range queries: ");
            String rQueriesFileName = args[2];
            rTree.rangeQuery(rQueriesFileName, "r");
        } else if(Integer.parseInt(args[0]) == 3) {
            // part 3
            RTree rTree = new RTree(MAX_CAPACITY, MIN_FILL);
            System.out.println("Please input the file path of the Rtree: ");
            String rTreeFileName = args[1];
            rTree.readTreeFromFile(rTreeFileName);

            System.out.println("Please input the file path of the nearest neighbors queries: ");
            String rnnQueriesFileName = args[2];

            System.out.println("Please input the number of nearest neighbors: ");
            int k = Integer.parseInt(args[3]);

            rTree.kNNQueries(rnnQueriesFileName, k);
        } else {
            System.out.println("Invalid argument.");
        }
        
    }
}
