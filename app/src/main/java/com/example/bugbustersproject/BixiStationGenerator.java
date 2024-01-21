package com.example.bugbustersproject;

import java.util.ArrayList;
import java.util.List;

public class BixiStationGenerator {
    public BixiStationGenerator() {

    }

    public List<BixiSation> getBixiStations() {
        List<BixiSation> bixiSationList = new ArrayList<>();
        bixiSationList.add(new BixiSation("Alexis-Nihon/St-Louis", 45.497149549895525f, -73.57106410408002f));
        bixiSationList.add(new BixiSation("Lucien l'Allier/Argyle", 45.495047390348795f, -73.571164012052f));
        bixiSationList.add(new BixiSation("St-Antoine / de la Montagne)", 45.49551366573489f, -73.56872856636197f));
        return bixiSationList;
    }
}
