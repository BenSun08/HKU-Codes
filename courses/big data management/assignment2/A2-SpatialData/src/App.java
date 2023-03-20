public class App {
    static int MAX_CAPACITY = 20;
    static double MIN_FILL = 0.4;
    public static void main(String[] args) throws Exception {
        // part 1
        // HeapFile heapFile = new HeapFile();
        // heapFile.readFromFiles("input/coords.txt", "input/offsets.txt");

        // RTree rTree = new RTree(MAX_CAPACITY, MIN_FILL);
        // rTree.bulkLoading(heapFile);
        // rTree.writeTree("output/Rtree.txt");
        
        // part 2
        RTree rTree2 = new RTree(MAX_CAPACITY, MIN_FILL);
        rTree2.readTreeFromFile("output/Rtree.txt");
        rTree2.writeTree("output/Rtree2.txt");
        
    }
}
