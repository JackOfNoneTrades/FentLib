package org.fentanylsolutions.fentlib.mixins.late.catalogue;

import java.util.Comparator;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableObject;
import org.fentanylsolutions.fentlib.Config;
import org.fentanylsolutions.fentlib.Config.CatalogueSortOrder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.cleanroommc.catalogue.client.screen.CatalogueModListScreen;

@Mixin(CatalogueModListScreen.class)
public abstract class MixinCatalogueModListScreenPreferences {

    @Shadow(remap = false)
    @Final
    private static Comparator<?> SORT_ALPHABETICALLY;

    @Shadow(remap = false)
    @Final
    private static Comparator<?> SORT_ALPHABETICALLY_REVERSED;

    @Shadow(remap = false)
    @Final
    private static Comparator<?> SORT_FAVOURITES_FIRST;

    @Shadow(remap = false)
    @Final
    private static MutableBoolean OPTION_CONFIGS_ONLY;

    @Shadow(remap = false)
    @Final
    private static MutableBoolean OPTION_FAVOURITES_ONLY;

    @Shadow(remap = false)
    @Final
    private static MutableBoolean OPTION_HIDE_LIBRARIES;

    @Shadow(remap = false)
    @Final
    private static MutableBoolean OPTION_HIDE_CHILD_MODS;

    @Shadow(remap = false)
    @Final
    private static MutableObject<Object> OPTION_SORT;

    @Inject(method = "initGui", at = @At("HEAD"), remap = true)
    private void fentlib$restorePreferences(CallbackInfo ci) {
        Config.refreshCataloguePreferences();
        if (!Config.persistCataloguePreferences) {
            return;
        }

        OPTION_SORT.setValue(fentlib$getComparator(Config.catalogueSortOrder));
        OPTION_CONFIGS_ONLY.setValue(Config.catalogueConfigsOnly);
        OPTION_FAVOURITES_ONLY.setValue(Config.catalogueFavouritesOnly);
        OPTION_HIDE_LIBRARIES.setValue(Config.catalogueHideLibraries);
        OPTION_HIDE_CHILD_MODS.setValue(Config.catalogueHideChildMods);
    }

    @Inject(method = "onGuiClosed", at = @At("HEAD"), remap = true)
    private void fentlib$savePreferences(CallbackInfo ci) {
        fentlib$savePreferencesToConfig();
    }

    @Inject(method = "mouseClicked", at = @At("TAIL"), remap = true)
    private void fentlib$savePreferencesAfterClick(int mouseX, int mouseY, int mouseButton, CallbackInfo ci) {
        fentlib$savePreferencesToConfig();
    }

    private static void fentlib$savePreferencesToConfig() {
        if (!Config.persistCataloguePreferences) {
            return;
        }

        Config.saveCataloguePreferences(
            fentlib$getSortOrder(),
            OPTION_CONFIGS_ONLY.booleanValue(),
            OPTION_FAVOURITES_ONLY.booleanValue(),
            OPTION_HIDE_LIBRARIES.booleanValue(),
            OPTION_HIDE_CHILD_MODS.booleanValue());
    }

    private static Comparator<?> fentlib$getComparator(CatalogueSortOrder sortOrder) {
        switch (sortOrder) {
            case ALPHABETICALLY_REVERSED:
                return SORT_ALPHABETICALLY_REVERSED;
            case FAVOURITES_FIRST:
                return SORT_FAVOURITES_FIRST;
            case ALPHABETICALLY:
            default:
                return SORT_ALPHABETICALLY;
        }
    }

    private static CatalogueSortOrder fentlib$getSortOrder() {
        Object selectedComparator = OPTION_SORT.getValue();
        if (selectedComparator == SORT_ALPHABETICALLY_REVERSED) {
            return CatalogueSortOrder.ALPHABETICALLY_REVERSED;
        }
        if (selectedComparator == SORT_FAVOURITES_FIRST) {
            return CatalogueSortOrder.FAVOURITES_FIRST;
        }
        if (selectedComparator == SORT_ALPHABETICALLY) {
            return CatalogueSortOrder.ALPHABETICALLY;
        }
        return Config.catalogueSortOrder;
    }
}
