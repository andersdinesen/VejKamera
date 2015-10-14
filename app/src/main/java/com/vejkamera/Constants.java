package com.vejkamera;

import java.util.HashMap;

/**
 * Created by ad on 28-05-2015.
 */
public class Constants {
    public final static int[] AREA_IDS = new int[]{
            R.string.all_areas,
            R.string.copenhagen,
            R.string.zealand,
            R.string.aalborg,
            R.string.northjutland,
            R.string.aarhus,
            R.string.middlejutland,
            R.string.vejle,
            R.string.fredercia,
            R.string.kolding,
            R.string.esbjerg,
            R.string.southjutland,
            R.string.odense,
            R.string.fyn,
            R.string.bornholm};
    public static HashMap<Integer, Integer> AREA_COORDINATES = null;
    public static HashMap<Integer, Integer[]> AREA_CUTOUTS = null;

    static {
        AREA_COORDINATES = new HashMap<>();
        AREA_COORDINATES.put(R.string.all_areas, null);
        AREA_COORDINATES.put(R.string.copenhagen, R.array.copenhagen_coordinates);
        AREA_COORDINATES.put(R.string.zealand, R.array.zealand_coordinates);
        AREA_COORDINATES.put(R.string.aalborg, R.array.aahus_coordinates);
        AREA_COORDINATES.put(R.string.northjutland, R.array.northjutland_coordindates);
        AREA_COORDINATES.put(R.string.aarhus, R.array.aahus_coordinates);
        AREA_COORDINATES.put(R.string.middlejutland, R.array.middlejutland_coordindates);
        AREA_COORDINATES.put(R.string.vejle, R.array.vejle_coordinates);
        AREA_COORDINATES.put(R.string.fredercia, R.array.fredercia_coordinates);
        AREA_COORDINATES.put(R.string.kolding, R.array.kolding_coordinates);
        AREA_COORDINATES.put(R.string.esbjerg, R.array.esbjerg_coordinates);
        AREA_COORDINATES.put(R.string.southjutland, R.array.southjutland_coordindates);
        AREA_COORDINATES.put(R.string.odense, R.array.odense_coordinates);
        AREA_COORDINATES.put(R.string.fyn, R.array.fyn_coordinates);
        AREA_COORDINATES.put(R.string.bornholm, R.array.bornholm_coordinates);

        AREA_CUTOUTS = new HashMap<>();
        Integer[] zealandCutout = {R.string.copenhagen};
        AREA_CUTOUTS.put(R.string.zealand, zealandCutout);

        Integer[] northJutlandCutout = {R.string.aalborg};
        AREA_CUTOUTS.put(R.string.northjutland, northJutlandCutout);

        Integer[] middleJutlandCutout = {R.string.aarhus};
        AREA_CUTOUTS.put(R.string.middlejutland, middleJutlandCutout);

        Integer[] southJutlandCutout = {R.string.vejle, R.string.fredercia, R.string.kolding, R.string.esbjerg};
        AREA_CUTOUTS.put(R.string.southjutland, southJutlandCutout);

        Integer[] fynCutout = {R.string.odense};
        AREA_CUTOUTS.put(R.string.fyn, fynCutout);

    }
}
