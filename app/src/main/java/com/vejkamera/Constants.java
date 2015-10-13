package com.vejkamera;

import java.util.HashMap;

/**
 * Created by ad on 28-05-2015.
 */
public class Constants {
    public final static int[] AREA_IDS = new int[]{R.string.all_areas, R.string.copenhagen, R.string.zealand, R.string.aarhus, R.string.aalborg, R.string.esbjerg, R.string.jutland, R.string.odense, R.string.fyn, R.string.bornholm};
    public static HashMap<Integer, Integer> AREA_COORDINATES = null;
    public static HashMap<Integer, Integer[]> AREA_CUTOUTS = null;

    static {
        AREA_COORDINATES = new HashMap<>();
        AREA_COORDINATES.put(R.string.all_areas, null);
        AREA_COORDINATES.put(R.string.copenhagen, R.array.copenhagen_coordinates);
        AREA_COORDINATES.put(R.string.zealand, R.array.zealand_coordinates);

        AREA_CUTOUTS = new HashMap<>();
        Integer[] zealandCutout = {R.array.copenhagen_coordinates};
        AREA_CUTOUTS.put(R.string.zealand, zealandCutout);
    }
}
