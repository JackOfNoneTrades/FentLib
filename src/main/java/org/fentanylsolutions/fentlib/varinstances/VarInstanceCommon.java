package org.fentanylsolutions.fentlib.varinstances;

import java.util.HashSet;

public class VarInstanceCommon {

    public HashSet<String> affectedMobs = new HashSet<>();

    public void updateAffectedMobs() {
        /*
         * for (String mobName : Config.mobList) {
         * affectedMobs.add(Util.getClassByName(mobName));
         * }
         */
    }
}
