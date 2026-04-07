package org.fentanylsolutions.fentlib.mixins.early.minecraft;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.FishingHooks;

import org.fentanylsolutions.fentlib.Config;
import org.fentanylsolutions.fentlib.services.fishing.FishingLootConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public class FeatureFishingLoot {

    @Mixin(EntityFishHook.class)
    public abstract static class MixinEntityFishHook extends Entity {

        protected MixinEntityFishHook(World world) {
            super(world);
        }

        @Shadow
        public EntityPlayer field_146042_b;

        @Inject(method = "func_146033_f", at = @At("HEAD"), cancellable = true, require = 1)
        private void fentlib$overrideFishingLoot(CallbackInfoReturnable<ItemStack> cir) {
            if (!Config.enableFishingLootTable) {
                return;
            }
            if (this.field_146042_b == null || this.worldObj == null) {
                return;
            }

            float chance = this.worldObj.rand.nextFloat();
            int luck = EnchantmentHelper.func_151386_g(this.field_146042_b);
            int speed = EnchantmentHelper.func_151387_h(this.field_146042_b);
            FishingHooks.FishableCategory category = FishingHooks.getFishableCategory(chance, luck, speed);
            BiomeGenBase biome = this.worldObj
                .getBiomeGenForCoords(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posZ));

            ItemStack customLoot = FishingLootConfig.getRandomLoot(biome, category, this.rand);
            this.field_146042_b.addStat(category.stat, 1);

            if (customLoot == null) {
                customLoot = new ItemStack(Items.stick);
            }

            cir.setReturnValue(customLoot);
        }
    }
}
