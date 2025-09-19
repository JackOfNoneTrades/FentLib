# FentLib
A shared code library and tweak/fix mod.

![logo](images/logo_small.png)

## Features
* Support for animated GIF server icons. Just drop a `server-icon.gif` file in the server root directory. Size limits are configurable. HodgePodge is a soft dependency, required if you want to use larger GIFs (because of the packet size limit).
![animated_server_icons](images/animated_server_icons.gif)
Use the `/reload_icon` command to reload the icon. Also works for `server-icon.png`.
* Removal of EnderCore / HodgePodge Info Button in the mod list screen.
* API to pass additional data in the `C00PacketServerQuery` packet (e.g. list your client capabilities), and modify the `S00PacketServerInfo` packet accordingly. Example:
```java
public class ClientProxy extends CommonProxy {
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        S00PacketServerInfoModifyService.put("i_support_animated_gifs", "1");
        // You can also put more comples JsonElement objects
    }
}

public class CommonProxy {
  public void preInit(FMLPreInitializationEvent event) {
    if (MiscUtil.isServer()) {
      S00PacketServerInfoModifyService.registerHandler((response, data) -> {
        if (data.has("i_support_animated_gifs")) {
          FentLib.debug("Client has support for animated gifs!");
          // send animated gif
        } else {
          FentLib.debug("Client has no support for animated gifs");
          // send normal image
        }
      });
    }
  }
}
```
* More to come!

## Downloads
<!--* [CurseForge ![curse](images/icons/curse.png)](https://www.curseforge.com/minecraft/mc-mods/fentlib)
* [Modrinth ![modrinth](images/icons/modrinth.png)](https://modrinth.com/mod/fentlib)-->
* [Git ![git](images/icons/git.png)](https://github.com/JackOfNoneTrades/Fentlib/releases)

## Dependencies
* [UniMixins](https://modrinth.com/mod/unimixins) ([![curse](images/icons/curse.png)](https://www.curseforge.com/minecraft/mc-mods/unimixins), [![modrinth](images/icons/modrinth.png)](https://modrinth.com/mod/unimixins/versions), [![git](images/icons/git.png)](https://github.com/LegacyModdingMC/UniMixins/releases)) is a required dependency.

## Building

`./gradlew build`.

## Credits
* [GT:NH buildscript](https://github.com/GTNewHorizons/ExampleMod1.7.10)

## License

`LgplV3 + SNEED`.

## Buy me a coffee

* [ko-fi.com](ko-fi.com/jackisasubtlejoke)
* Monero: `893tQ56jWt7czBsqAGPq8J5BDnYVCg2tvKpvwTcMY1LS79iDabopdxoUzNLEZtRTH4ewAcKLJ4DM4V41fvrJGHgeKArxwmJ`

<br>

![license](images/lgplsneed_small.png)
