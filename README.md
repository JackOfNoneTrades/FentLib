# FentLib
A shared code library and fix mod.

![logo](images/logo_small.png)

## Features
* Currently fixes `1.7.10` `Carbon Config` array serialization, addressing [this issue](https://github.com/Carbon-Config-Project/CarbonConfigLib/issues/5).
* Java annotation system for `Carbon Config`. Example usage:

```java
@FentConfig(name = "testconf")
public class TestConf {

    @ConfigInt(
        name = "test_value",
        comment = "This is a test integer value",
        defaultValue = 5,
        min = -10,
        max = 10,
        category = "general")
    public static int testValue;
}
```

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
