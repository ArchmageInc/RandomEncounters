package com.archmageinc.RandomEncounters.Encounters;

/**
 *
 * @author ArchmageInc
 */
public interface EncounterPlacer {
    public void addPlacedEncounter(PlacedEncounter newEncounter);
    public Encounter getEncounter();
    public double getInitialAngle();
}
