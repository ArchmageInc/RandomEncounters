package com.archmageinc.RandomEncounters.Encounters;

import java.util.Map;

/**
 *
 * @author ArchmageInc
 */
public interface EncounterPlacer {
    public void addPlacedEncounter(PlacedEncounter newEncounter);
    public Encounter getEncounter();
    public double getInitialAngle();
    public long getPattern();
    public Map<String,Long> getProximities();
}
