import java.util.ArrayList;
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedReader;

public class HeapFile {
    private ArrayList<Polygon> polygons;

    public HeapFile() {
        this.polygons = new ArrayList<Polygon>();
    }

    public void readFromFiles(String coordsFileName, String offsetFileName) throws IOException {
        BufferedReader coordsReader = new BufferedReader(new FileReader(coordsFileName));
        BufferedReader offsetReader = new BufferedReader(new FileReader(offsetFileName));

        String coordsLine;
        String offsetLine;
        while ((offsetLine = offsetReader.readLine()) != null) {
            String[] offsetLineSplit = offsetLine.split(",");
            int id = Integer.parseInt(offsetLineSplit[0]);
            int start = Integer.parseInt(offsetLineSplit[1]);
            int end = Integer.parseInt(offsetLineSplit[2]);
            int length = end - start + 1;

            Polygon polygon = new Polygon(id);
            for (int i = 0; i < length; i++) {
                coordsLine = coordsReader.readLine();
                String[] coordsLineSplit = coordsLine.split(",");
                Double x = Double.parseDouble(coordsLineSplit[0]);
                Double y = Double.parseDouble(coordsLineSplit[1]);
                polygon.addPoint(x, y);
            }
            polygon.calZOrderCode();
            this.polygons.add(polygon);
        }

        coordsReader.close();
        offsetReader.close();

        this.polygons.sort((Polygon p1, Polygon p2) -> p1.getZOrderCode().compareTo(p2.getZOrderCode()));
    }

    public ArrayList<Polygon> getPolygons() {
        return this.polygons;
    }
}
