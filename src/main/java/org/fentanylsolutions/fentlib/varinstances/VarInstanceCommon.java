package org.fentanylsolutions.fentlib.varinstances;

import java.util.ArrayList;
import java.util.Hashtable;

import net.minecraft.world.WorldProvider;

import org.fentanylsolutions.fentlib.Config;
import org.fentanylsolutions.fentlib.FentLib;
import org.fentanylsolutions.fentlib.mixins.early.minecraftforge.AccessorDimensionManager;
import org.fentanylsolutions.fentlib.util.BiomeUtil;
import org.fentanylsolutions.fentlib.util.DimensionUtil;
import org.fentanylsolutions.fentlib.util.MobUtil;
import org.fentanylsolutions.fentlib.util.PotionUtil;
import org.fentanylsolutions.fentlib.util.XSTR;

public class VarInstanceCommon {

    public XSTR rand = new XSTR();
    public ArrayList<Class> passiveMobsWhichCanInflictDamage;
    public Hashtable<Integer, Class<? extends WorldProvider>> providers;

    public void postInitHook() {
        providers = AccessorDimensionManager.getProviders();
        buildPassiveMobList();

        if (Config.printPotions) {
            PotionUtil.printPotionNames();
        }
        if (Config.printMobs) {
            MobUtil.printMobNames();
        }
        if (Config.printBiomes) {
            BiomeUtil.printBiomeNames();
        }
        // Needs to happen after the post init hook
        if (Config.printDimensions) {
            DimensionUtil.printDimensionNames();
        }
    }

    public void buildPassiveMobList() {
        passiveMobsWhichCanInflictDamage = new ArrayList<>();
        for (String s : Config.passiveMobsWhichCanInflictDamage) {
            String class_ = MobUtil.getClassByName(s);
            if (class_ == null) {
                FentLib.LOG.error("Failed to get mob class for name {}", s);
            } else {
                try {
                    Class c = Class.forName(class_);
                    passiveMobsWhichCanInflictDamage.add(c);
                } catch (ClassNotFoundException e) {
                    FentLib.LOG.error("Failed to get class for classname {}", class_);
                }
            }
        }
    }
}
