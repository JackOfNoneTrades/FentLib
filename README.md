# FentLib
A shared code library and tweak/fix mod.

![logo](images/logo_small.png)

## Features
* Support for animated GIF server icons. Just drop a `server-icon.gif` file in the server root directory. Size limits are configurable. HodgePodge is a soft dependency, required if you want to use larger GIFs (because of the packet size limit).
![animated_server_icons](images/animated_server_icons.gif)
Use the `/reload_icon` command to reload the icon. Also works for `server-icon.png`.
* Removal of EnderCore / HodgePodge Info Button in the mod list screen.
* API to modify the `S00PacketServerInfo` packet. Example:
```java
public class ClientProxy extends CommonProxy {
    @Override
    public void preInit(FMLPreInitializationEvent event) {
      S00PacketServerInfoModifyService.registerDeserializeHandler((response, fentlibData, serverData) -> {
        if (fentlibData.has("server_to_client_payload")) {
          // Yoohoo we got something back!!!!
          // serverData contains stuff like server IP
        }
      });
    }
}

public class CommonProxy {
  public void preInit(FMLPreInitializationEvent event) {
    if (MiscUtil.isServer()) {
      S00PacketServerInfoModifyService.registerHandler((response, fentLibPresent) -> {
        // fentLibPresent is just a boolean indicating whether fentlib is loaded
        return "server_to_client_payload";
        // You can return a String, a S00PacketServerInfoModifyService.KeyValue, or a JsonElement. If you return
        // a non-null value, it will be passed back to the client
      });
    }
  }
}
```
* `/dump_thaumonomicon <Optional Comment>` command. Run it from the client, and all Thaumcraft research will be dumped as a static website. The comment will be visible under the page title, and you can indicate the pack or mods for with which the dump was done.
![Dump example](images/dumper_example.png)
* `/warpdim` [dimension ID] command. Painlessly warp to a dimension (meant for debugging).

## Downloads
<!--* [CurseForge ![curse](images/icons/curse.png)](https://www.curseforge.com/minecraft/mc-mods/fentlib)
* [Modrinth ![modrinth](images/icons/modrinth.png)](https://modrinth.com/mod/fentlib)-->
* [Git ![git](images/icons/git.png)](https://github.com/JackOfNoneTrades/Fentlib/releases)

## Dependencies
* [UniMixins](https://modrinth.com/mod/unimixins) ([![curse](images/icons/curse.png)](https://www.curseforge.com/minecraft/mc-mods/unimixins), [![modrinth](images/icons/modrinth.png)](https://modrinth.com/mod/unimixins/versions), [![git](images/icons/git.png)](https://github.com/LegacyModdingMC/UniMixins/releases)) is a required dependency.

## Building

`./gradlew build`.

## Dev Authlib Note

`SessionAccessTokenOverrideMixin` allows overriding the session access token in dev launches via `-Dfentlib.accessTokenOverride=<token>`.

## Credits
* [GT:NH buildscript](https://github.com/GTNewHorizons/ExampleMod1.7.10)

## License

`LgplV3 + SNEED`.

## Buy me a coffee

* [ko-fi.com](ko-fi.com/jackisasubtlejoke)
* Monero: `893tQ56jWt7czBsqAGPq8J5BDnYVCg2tvKpvwTcMY1LS79iDabopdxoUzNLEZtRTH4ewAcKLJ4DM4V41fvrJGHgeKArxwmJ`

<br>

![license](images/lgplsneed_small.png)

TODO:
* Still generate the vanilla server dat for multimc compat
