public class App {
    public static void main(String[] args) throws Exception {
        HeapFile heapFile = new HeapFile();
        heapFile.readFromFiles("input/coords.txt", "input/offsets.txt");

        RTree rTree = new RTree(20, 0.4);
        rTree.bulkLoading(heapFile);

        rTree.writeTree("output/Rtree.txt");
    }
}
