import java.io.*;
import java.util.ArrayList;

public class FileReadWrite {
  private File file;
  private int blockSize;
  BufferedReader reader;
  BufferedWriter writer;
  String type;
  public FileReadWrite(String fileName, int blockSize, String type) throws IOException {
    this.file = new File(fileName);
    this.blockSize = blockSize;
    this.type = type;
    if(type == "read") {
      reader = new BufferedReader(new FileReader(file));
    } else if(type == "write") {
      writer = new BufferedWriter(new FileWriter(file));
    }
  }
  
  public ArrayList<String[]> read() throws IOException {
    ArrayList<String[]> lines = new ArrayList<String[]>();
    int i = 0;
    String st;
    while (i < blockSize && (st = reader.readLine()) != null) {
      String[] line = st.split("\t");
      lines.add(line);
      i++;
    } 
    return lines;
  }

  public void write(ArrayList<String[]> lines) throws IOException {
    for(int i = 0; i<lines.size(); i++) {
      String[] line = lines.get(i);
      for(int j = 0; j<line.length; j++) {
        writer.write(line[j]);
        if(j != line.length - 1) {
          writer.write("\t");
        }
      }
      writer.write("\n");
    }
  }

  public void close() throws IOException {
    if(type == "read") {
      reader.close();
    } else if(type == "write") {
      writer.close();
    }
  }
}


