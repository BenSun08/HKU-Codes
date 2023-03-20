import java.io.IOException;
import java.util.ArrayList;

public class QueryOperation {
  private String inputFile1;
  private String inputFile2;
  private String outputFile;
  private int blockSize;
  private FileReadWrite reader1;
  private FileReadWrite reader2;
  private FileReadWrite writer;

  private ArrayList<String[]> block1;
  private ArrayList<String[]> block2;
  private ArrayList<String[]> outputBlock = new ArrayList<String[]>();

  private String[] record1;
  private String[] record2;
  private int[] pts = { 0, 0}; // points to the current record in each block

  public QueryOperation(String file1, String file2, String outputFile, int blockSize) throws IOException {
    this.blockSize = blockSize;
    this.inputFile1 = file1;
    this.inputFile2 = file2;
    this.outputFile = outputFile;
    this.reader1 = new FileReadWrite(file1, blockSize, "read");
    this.reader2 = new FileReadWrite(file2, blockSize, "read");
    this.writer = new FileReadWrite(outputFile, blockSize, "write");
  }

  private void init() throws IOException { // initialize the reader and writer
    block1 = reader1.read();
    block2 = reader2.read();
    outputBlock = new ArrayList<String[]>();
    pts[0] = 0;
    pts[1] = 0;
    record1 = block1.get(pts[0]);
    record2 = block2.get(pts[1]);
  }

  private Boolean advanceOuter(Boolean changeRecord) throws IOException {
    pts[0]++;
    if(pts[0] >= block1.size()) {
      block1.clear();
      block1 = reader1.read();
      pts[0] = 0;
      if(block1.size() == 0) {
        return false; // end of file
      }
    }
    if(changeRecord) {
      record1 = block1.get(pts[0]);
    }
    return true;
  }

  private Boolean advanceInner() throws IOException {
    pts[1]++;
    if(pts[1] >= block2.size()) {
      block2.clear();
      block2 = reader2.read();
      pts[1] = 0;
      if(block2.size() == 0) {
        return false; // end of file
      }
    }
    record2 = block2.get(pts[1]);
    return true;
  }

  private void writeOutputBlock(String[] rec) throws IOException {
    outputBlock.add(rec);
    if(outputBlock.size() == blockSize) {
      writer.write(outputBlock);
      outputBlock.clear();
    }
  }

  private void close() throws IOException {
    writer.close();
    reader1.close();
    reader2.close();
  }

  public void reset() throws IOException { // reset the reader and writer
    reader1 = new FileReadWrite(inputFile1, blockSize, "read");
    reader2 = new FileReadWrite(inputFile2, blockSize, "read");
    writer = new FileReadWrite(outputFile, blockSize, "write");
  }

  public void setOutputFile(String fileName) { // set the output file
    outputFile = fileName;
  }

  public void setInnerFile(String fileName) { // set the inner file
    inputFile2 = fileName;
  }

  public void mergeJoin() throws IOException {
    init();
    Boolean endOfFile = false;

    while(block1.size() > 0 && block2.size() > 0) {
      // while the first column of the outer block is smaller than the first column of the inner block
      // advance the outer block
      while(record1[0].compareTo(record2[0]) < 0) {
        if(endOfFile = !advanceOuter(true)) break;
      } 
      if(endOfFile) break;

      // while the first column of the outer block is larger than the first column of the inner block
      // advance the inner block
      while(record1[0].compareTo(record2[0]) > 0) {
        if(endOfFile = !advanceInner()) break;
      } 
      if(endOfFile) break;

      // while the first column of the outer block is equal to the first column of the inner block
      // write the join block to the output buffer and advance the inner block
      while(record1[0].equals(record2[0])) {
        String[] line = new String[]{record1[0], record1[1], record2[1]};
        writeOutputBlock(line);
        if(endOfFile = !advanceInner()) break;
      } 
    }
    if(outputBlock.size() > 0) {
      writer.write(outputBlock);
    }
    close();
  }

  public void union() throws IOException {
    init();
    Boolean endOfFile = false;

    while(block1.size() > 0 && block2.size() > 0) {
      // while the first column of the outer block is smaller than the first column of the inner block
      // or the first column of the outer block is equal to the first column of the inner block
      // and the second column of the outer block is smaller than the second column of the inner block
      // write the outer block to the output buffer and advance the outer block
      while(record1[0].compareTo(record2[0]) < 0 ||
            (record1[0].equals(record2[0]) && record1[1].compareTo(record2[1]) < 0)
      ) {
        writeOutputBlock(record1);
        if(endOfFile = !advanceOuter(true)) break;
      } 
      if(endOfFile) break;

      // while the first column of the outer block is larger than the first column of the inner block
      // or the first column of the outer block is equal to the first column of the inner block
      // and the second column of the outer block is larger than the second column of the inner block
      // write the inner block to the output buffer and advance the inner block
      while(record1[0].compareTo(record2[0]) > 0 ||
          (record1[0].equals(record2[0]) && record1[1].compareTo(record2[1]) > 0)
        ) {
          writeOutputBlock(record2);
          if(endOfFile = !advanceInner()) break;
        } 
      if(endOfFile) break;

      // if the first column of the outer block is equal to the first column of the inner block
      // and the second column of the outer block is equal to the second column of the inner block
      // write the outer block to the output buffer
      if(record1[0].equals(record2[0]) && record1[1].equals(record2[1])) {
        writeOutputBlock(record1);
        if(endOfFile = !advanceOuter(false)) break;
      }
      while(record1[0].equals(record2[0]) && record1[1].equals(record2[1])) {
        if(endOfFile = !advanceInner()) break;
      }
      //  after scanning all the duplicated records, need to advance the outer block
      if(block1.get(pts[0]) != record1) {
        record1 = block1.get(pts[0]);
      }
    }
    // write the remaining input block and input file to the output file
    if(block1.size() == 0) {
      while(block2.size() > 0) {
        writeOutputBlock(record2);
        if(endOfFile = !advanceInner()) break;
      }
    } else {
      while(block1.size() > 0) {
        writeOutputBlock(record1);
        if(endOfFile = !advanceOuter(true)) break;
      }
    }
    // write the remaining output buffer to the output file
    if(outputBlock.size() > 0) {
      writer.write(outputBlock);
    }
    close();
  }

  public void intersection() throws IOException {
    init();
    Boolean endOfFile = false;
    
    while(block1.size() > 0 && block2.size() > 0) {
      while(record1[0].compareTo(record2[0]) < 0 ||
            (record1[0].equals(record2[0]) && record1[1].compareTo(record2[1]) < 0)
      ) {
        // advance the outer block
        if(endOfFile = !advanceOuter(true)) break;
      } 
      if(endOfFile) break;

      while(record1[0].compareTo(record2[0]) > 0 ||
          (record1[0].equals(record2[0]) && record1[1].compareTo(record2[1]) > 0)
        ) {
          // advance the inner block
          if(endOfFile = !advanceInner()) break;
        } 
      if(endOfFile) break;

      // if the first column of the outer block is equal to the first column of the inner block
      // and the second column of the outer block is equal to the second column of the inner block
      if(record1[0].equals(record2[0]) && record1[1].equals(record2[1])) { 
        // write the outer block to the output buffer
        writeOutputBlock(record1);
      }
      // advance the inner block until the first column of the outer block is no longer equal to the first column of the inner block
      while(record1[0].equals(record2[0]) && record1[1].equals(record2[1])) {
        if(endOfFile = !advanceInner()) break;
      }
    }
    // write the output buffer to the file
    if(outputBlock.size() > 0) {
      writer.write(outputBlock);
    }
    close();
  }

  public void setDifference() throws IOException {
    init();
    Boolean endOfFile = false;

    while(block1.size() > 0 && block2.size() > 0) {
      while(record1[0].compareTo(record2[0]) < 0 ||
            (record1[0].equals(record2[0]) && record1[1].compareTo(record2[1]) < 0)
      ) {
        // write the outer block to the output buffer and advance the outer block
        writeOutputBlock(record1);
        if(endOfFile = !advanceOuter(true)) break;
      } 
      if(endOfFile) break;

      while(record1[0].compareTo(record2[0]) > 0 ||
          (record1[0].equals(record2[0]) && record1[1].compareTo(record2[1]) > 0)
        ) {
          if(endOfFile = !advanceInner()) break;
        } 
      if(endOfFile) break;

      if(record1[0].equals(record2[0]) && record1[1].equals(record2[1])) {
        // advance the outer block
        if(endOfFile = !advanceOuter(false)) break;
      }
      // advance inner block until the first column of the outer block is no longer equal to the first column of the inner block
      //  and the second column of the outer block is no longer equal to the second column of the inner block
      while(record1[0].equals(record2[0]) && record1[1].equals(record2[1])) {
        if(endOfFile = !advanceInner()) break;
      }
      if(block1.get(pts[0]) != record1) {
        record1 = block1.get(pts[0]);
      }
    }
    // write the remaining input block and input file to the output buffer
    if(block1.size() == 0) {
      while(block2.size() > 0) {
        writeOutputBlock(record2);
        if(endOfFile = !advanceInner()) break;
      }
    } else {
      while(block1.size() > 0) {
        writeOutputBlock(record1);
        if(endOfFile = !advanceOuter(true)) break;
      }
    }
    // write the output buffer to the output file
    if(outputBlock.size() > 0) {
      writer.write(outputBlock);
    }
    close();
  }

  static public void printBlock(ArrayList<String[]> block) {
    for(int i = 0; i<block.size(); i++) {
      String[] line = block.get(i);
      String lineStr = "";
      for(int j = 0; j < line.length; j++) {
        lineStr += line[j] + "\t";
      }
      System.out.println(lineStr.trim());
    }
  }
}
