package org.mitre.synthea.world.geography.censusdata;

import java.awt.geom.Point2D;
import java.util.SortedMap;

/**
 * CensusBlock class 
 */
public class CensusBlock {
    public String id;
    public String tract_id;
    public CensusTract tract;
    public Point2D.Double coordinate;

    /// Store Blocks sorted by their coordinates in order to quickly find
    /// the nearest Census Block to a given point
    static SortedMap<Point2D.Double, CensusBlock> coordinateToBlock;

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

    CensusTract getTract(){
        return tract;
    }

}
