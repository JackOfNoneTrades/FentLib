package org.fentanylsolutions.fentlib.mixins.early.minecraft;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.List;

import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;

import org.fentanylsolutions.fentlib.FentLib;
import org.fentanylsolutions.fentlib.mixins.DummyTarget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

@Mixin(DummyTarget.class)
public class FeatureSaveServerDataAsJson {

    @Mixin(ServerList.class)
    public static class MixinSaveAsJson {

        @Shadow
        private List<ServerData> servers;

        @Shadow
        private net.minecraft.client.Minecraft mc;

        /**
         * @author jack
         * @reason Completely replace serialization and deserialization of ServerData, to allow objects of arbitrary
         *         size to be dumped
         */
        @Overwrite
        public void saveServerList() {
            try {
                File file = new File(this.mc.mcDataDir, "servers.json");

                Gson gson = new GsonBuilder().setPrettyPrinting()
                    .create();
                FileWriter writer = new FileWriter(file);

                gson.toJson(this.servers, writer);
                writer.close();
            } catch (Exception e) {
                FentLib.LOG.error("Couldn't save server list (JSON)", e);
            }
        }

        /**
         * @author jack
         * @reason Completely replace serialization and deserialization of ServerData, to allow objects of arbitrary
         *         size to be dumped
         */
        @Overwrite
        public void loadServerList() {
            try {
                this.servers.clear();
                File file = new File(this.mc.mcDataDir, "servers.json");

                if (!file.exists()) return;

                Gson gson = new Gson();
                FileReader reader = new FileReader(file);

                Type listType = new TypeToken<List<ServerData>>() {}.getType();
                List<ServerData> loaded = gson.fromJson(reader, listType);
                reader.close();

                if (loaded != null) {
                    this.servers.addAll(loaded);
                }
                for (ServerData serverData : this.servers) {
                    serverData.pingToServer = -1L;
                    serverData.serverMOTD = "";
                    serverData.populationInfo = "";
                    serverData.gameVersion = "";
                    // serverData. = "";
                    serverData.field_78841_f = false;
                    // serverData.field_78843_h = false;
                }
            } catch (Exception e) {
                FentLib.LOG.error("Couldn't load server list (JSON)", e);
            }
        }
    }
}
