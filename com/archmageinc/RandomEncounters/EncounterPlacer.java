package com.archmageinc.RandomEncounters;

/**
 *
 * @author ArchmageInc
 */
public interface EncounterPlacer {
    public void addPlacedEncounter(PlacedEncounter newEncounter);
    public Encounter getEncounter();
}
