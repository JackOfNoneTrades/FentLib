package org.fentanylsolutions.fentlib.varinstances;

import cpw.mods.fml.common.registry.GameData;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityList;
import net.minecraft.item.Item;
import org.fentanylsolutions.fentlib.Config;
import org.fentanylsolutions.fentlib.carbonextension.CarbonNamespacedRegistry;
import org.fentanylsolutions.fentlib.carbonextension.MobWrapper;
import org.fentanylsolutions.fentlib.util.MobUtil;

public class CarbonCompat {
    public CarbonNamespacedRegistry<MobWrapper> entityRegistry;
    public CarbonNamespacedRegistry<Item> itemFMLRegistry;
    public CarbonNamespacedRegistry<Block> blockFMLRegistry;

    public void initHook() {
        itemFMLRegistry = new CarbonNamespacedRegistry<>("", 0, 0, Item.class, '0');
        itemFMLRegistry.set(GameData.getItemRegistry());

        blockFMLRegistry = new CarbonNamespacedRegistry<>("", 0, 0, Block.class, '0');
        blockFMLRegistry.set(GameData.getBlockRegistry());

        entityRegistry = new CarbonNamespacedRegistry<>("minecraft:Pig", 4095, 0, MobWrapper.class, 'M');
        for (String e : EntityList.stringToClassMapping.keySet()) {
            String pseudoResource = MobUtil.nameToPseudoResource(e);
            boolean found = false;
            for (String s : Config.entityBlacklist) {
                if (s.equals(pseudoResource)) {
                    found = true;
                    break;
                }
            }
            if (found) {
                continue;
            }
            MobWrapper wrapper = new MobWrapper(pseudoResource);
            entityRegistry.addObject(
                -1,
                wrapper.resourceName,
                wrapper);
        }
    }
}
