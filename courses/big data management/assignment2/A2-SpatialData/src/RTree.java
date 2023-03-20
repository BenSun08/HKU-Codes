import java.util.ArrayList;
import java.io.IOException;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;

public class RTree {
  private int maxCapacity;
  private double minRate;
  private TreeNode root;
  private ArrayList<ArrayList<TreeNode>> levels;

  public RTree(int maxCapacity, double minRate) {
    this.maxCapacity = maxCapacity;
    this.minRate = minRate;
    this.levels = new ArrayList<ArrayList<TreeNode>>();
  }

  private ArrayList<TreeNode> bulkLoadingHelper(ArrayList<Entry> entries, int isNonLeaf) {
    ArrayList<TreeNode> nodes = new ArrayList<TreeNode>();
    if(entries.size() <= maxCapacity) {
      this.root = new TreeNode(1, entries);
      nodes.add(this.root);
    } else {
      // create nodes from entries
      for(int i = 0; i < entries.size();) {
        TreeNode node = new TreeNode(isNonLeaf);
        for(int j = 0; i < entries.size() && j < maxCapacity; j++) {
          node.addEntry(entries.get(i));
          i++;
        }
        nodes.add(node);
      }
      // if number of entries in last node is less than minRate * maxCapacity,
      // then borrow entries from the previous node
      TreeNode lastNode = nodes.get(nodes.size() - 1);
      if(lastNode.size() < minRate * maxCapacity) {
        TreeNode prevNode = nodes.get(nodes.size() - 2);
        int num = (int) (minRate * maxCapacity - lastNode.size());
        for(int i = 0; i < num; i++) {
          Entry e = prevNode.pop();
          lastNode.prepend(e);
        }
      }
      // assign ids to nodes
      if(isNonLeaf == 0) {
        for(int i = 0; i < nodes.size(); i++) {
          nodes.get(i).setId(i);
        }
      } else {
        int startId = entries.get(entries.size() - 1).getId() + 1;
        for(int i = 0; i < nodes.size(); i++) {
          nodes.get(i).setId(startId + i);
        }
      }
    }
    return nodes;
  }

  public void bulkLoading(HeapFile disk) {
    ArrayList<Polygon> polygons = disk.getPolygons();
    ArrayList<Entry> entries = new ArrayList<Entry>();
    for (int i = 0; i < polygons.size(); i++) {
      entries.add(new Entry(polygons.get(i)));
    }
    ArrayList<TreeNode> nodes = this.bulkLoadingHelper(entries, 0);
    while(nodes.size() > 1) {
      this.levels.add(nodes);
      entries.clear();
      for(int i = 0; i < nodes.size(); i++) {
        entries.add(new Entry(nodes.get(i)));
      }
      nodes = this.bulkLoadingHelper(entries, 1);
    }
    this.levels.add(nodes);

    // print the tree
    for(int i = 0; i < levels.size(); i++) {
      System.out.println(String.format("%d nodes at level %d", levels.get(i).size(), i));
    }
  }

  public void writeTree(String fileName) throws IOException {
    BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
    // write the tree to the file
    for(int i = 0; i < levels.size(); i++) {
      ArrayList<TreeNode> nodes = levels.get(i);
      for(int j = 0; j < nodes.size(); j++) {
        TreeNode node = nodes.get(j);
        String line = node.toString();
        writer.write(line + "\n");
      }
    }

    writer.close();
  }
}
