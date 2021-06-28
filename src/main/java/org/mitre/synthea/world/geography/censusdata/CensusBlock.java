package org.mitre.synthea.world.geography.censusdata;

import java.io.IOException;
import java.awt.geom.Point2D;
import java.util.SortedMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Comparator;

import org.mitre.synthea.helpers.Config;
import org.mitre.synthea.helpers.SimpleCSV;
import org.mitre.synthea.helpers.Utilities;

/**
 * CensusBlock class 
 */
public class CensusBlock {
    public String geoid;
    public String tract_id;
    public CensusTract tract;
    public Point2D.Double coordinate;

    
    /**
     * Store Blocks sorted by their coordinates in order to find
     * the nearest Census Block to a given point reasonably quickly.
     * Perhaps could eventually be replaced with a k-d tree
     */
    static TreeMap<Point2D.Double, CensusBlock> coordinateToBlock = new TreeMap<Point2D.Double, CensusBlock>(new Comparator<Point2D>(){
        public int compare(Point2D p1, Point2D p2) {
            int x_comp = Double.compare(p1.getX(), p2.getX());
            if (x_comp != 0){
                return x_comp;
            }
            else{
                return Double.compare(p1.getY(), p2.getY());
            }
        }
    });

    /**
     * Determine which CensusBlock stored in the coordinateToBlock map
     * has a coordinate point closest to the given point
     */
    public static CensusBlock findNearestBlockTo(Point2D.Double point){
        if (coordinateToBlock.isEmpty()){
            return null;
        }

        SortedMap<Point2D.Double, CensusBlock> lowerPoints = coordinateToBlock.headMap(point);
        SortedMap<Point2D.Double, CensusBlock> upperPoints = coordinateToBlock.tailMap(point);

        if (lowerPoints.isEmpty()){
            return upperPoints.get(upperPoints.lastKey());
        }
        else if (upperPoints.isEmpty()){
            return lowerPoints.get(lowerPoints.firstKey());
        }

        Point2D.Double upperKey = upperPoints.lastKey();
        Point2D.Double lowerKey = upperPoints.firstKey();

        double upperDistance = Point2D.distanceSq(
            point.x, point.y,
            upperKey.x, upperKey.y
        );

        double lowerDistance = Point2D.distanceSq(
            point.x, point.y,
            lowerKey.x, lowerKey.y
        );

        if (lowerDistance < upperDistance){
            return coordinateToBlock.get(lowerKey);
        }
        else{
            return coordinateToBlock.get(upperKey);
        }

    }

    public static HashMap<String, CensusBlock> load(String state) throws IOException {
        String filename = Config.get("generate.census.block_file");
        String csv = Utilities.readResource(filename);

        List<? extends Map<String,String>> blocksCsv = SimpleCSV.parse(csv);

        HashMap<String, CensusBlock> map = new HashMap<String, CensusBlock>();

        for (Map<String, String> blocksLine : blocksCsv) {
            String geoid = blocksLine.get("GEOID20");
            String state_name = blocksLine.get("state_name");

            if (state == "" || state.equalsIgnoreCase(state_name)) {
                CensusBlock parsed = csvLineToCensusBlock(blocksLine);

                if (parsed.tract != null){
                    map.put(geoid, parsed);
                    coordinateToBlock.put(parsed.coordinate, parsed);
                }
            }
        }
        
        return map;
    }

    static CensusBlock csvLineToCensusBlock(Map<String, String> csvline) throws IOException {

        CensusBlock b = new CensusBlock();

        b.geoid = csvline.get("GEOID20");
        b.tract_id = b.geoid.substring(0, b.geoid.length() - 4);

        b.tract = CensusTract.getTractByGeoid(b.tract_id);

        if (b.tract == null){
            throw new IOException("Attempting to create CensusBlock without valid tract.");
        }

        b.coordinate = new Point2D.Double(
            Double.parseDouble(csvline.get("INTPTLON20")),
            Double.parseDouble(csvline.get("INTPTLAT20"))
        );

        return b;
    }

    public CensusTract getTract(){
        return tract;
    }

}
