package org.mitre.synthea.world.geography.censusdata;

import java.io.IOException;
import java.util.Random;
import java.util.Map;
import java.util.List;
import java.util.HashMap;

import org.mitre.synthea.helpers.Config;
import org.mitre.synthea.helpers.SimpleCSV;
import org.mitre.synthea.helpers.Utilities;

/**
 * CensusTract class
 */
public class CensusTract {
    public String id;

    Double percentOfPopInFishingAndForestry;
    Double percentOfPopUnemployed;
    Double percentOfPopUninsured;

    static HashMap<String, CensusTract> tracts = new HashMap<String, CensusTract>();

    public String toString(){
        return id;
    }

    public static CensusTract getTractByGeoid(String geoid) {
        return tracts.get(geoid);
    }

    public static HashMap<String, CensusTract> load(String state) throws IOException {
        String filename = Config.get("generate.census.tract_file");

        String csv = Utilities.readResource(filename);

        List<? extends Map<String,String>> tractsCsv = SimpleCSV.parse(csv);

        for (Map<String, String> tractsLine : tractsCsv) {
            String geoid = tractsLine.get("tract_code");
            String state_name = tractsLine.get("state_name");

            if (state != null && state.equalsIgnoreCase(state_name)) {
                CensusTract parsed = csvLineToCensusTract(tractsLine);

                tracts.put(geoid, parsed);
            }
            else {
                throw new IOException("something is wrong with the state from the file and stuff: " + state + " state_name: " + state_name);
            }
        }
        
        return tracts;

    }

    static CensusTract csvLineToCensusTract(Map<String, String> csvline){
        CensusTract t = new CensusTract();

        t.id = csvline.get("tract_code");

        t.percentOfPopInFishingAndForestry = Double.parseDouble(csvline.get("percent_population_fishing_forestry"));
        t.percentOfPopUnemployed = Double.parseDouble(csvline.get("percent_population_unemployed"));
        t.percentOfPopUninsured = Double.parseDouble(csvline.get("percent_population_uninsured"));

        return t;
    }

    public String getRandomOccupation(double random_roll) {

        if (random_roll < percentOfPopInFishingAndForestry) {
            return "fishing_and_forestry";
        }
        else if (random_roll < (percentOfPopInFishingAndForestry + percentOfPopUnemployed)) {
            return "unemployed";
        }
        else{
            return "other";
        }
       
    }

    public boolean getInsuranceStatus(double random_roll) {
        if (random_roll < percentOfPopUninsured) {
            return false;
        }
        else {
            return true;
        }
    }
}
