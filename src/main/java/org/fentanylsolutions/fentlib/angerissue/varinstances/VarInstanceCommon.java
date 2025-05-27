package org.fentanylsolutions.fentlib.angerissue.varinstances;

import java.util.HashSet;

public class VarInstanceCommon {

    public HashSet<String> affectedMobs = new HashSet<>();

    public void updateAffectedMobs() {
        /*
         * for (String mobName : Config.mobList) {
         * affectedMobs.add(ClassUtil.getEntityClassByName(mobName));
         * }
         */
    }
}
