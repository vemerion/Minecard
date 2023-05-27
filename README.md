# Minecard

This is the source code to my mod **Minecard**, which adds a trading card game to Minecraft.

Special thanks to Sxuuz for providing textures for the mod! (These textures are also under MIT license)

## Data-driven cards

Cards can be added/changed by using a data pack. Here follows a brief description of how to get started with this. First of all, you need to create a data pack. The Minecraft wiki does a really good job of explaining how this is done ([link](https://minecraft.fandom.com/wiki/Data_pack)).

The cards always need to be located in a folder called 'minecard_cards', and the json file needs to be named the same as the entity you are creating it for. For example, if you want to add a card for an entity 'bob' from a mod called 'abcde', then the path should look like this:

```
[ROOT FOLDER OF DATA PACK]/data/abcde/minecard_cards/bob.json
```

Similar, if you want to change the vanilla zombie card, the path should be:

```
[ROOT FOLDER OF DATA PACK]/data/minecraft/minecard_cards/zombie.json
```

This is how a very simple card could look in json:

```
{
	"entity": "minecraft:zombie",
	"cost": 10,
	"health": 1,
	"damage": 20
}
```

To gain inspiration, you could take a look at the cards already defined by the mod, located [here](https://github.com/vemerion/Minecard/tree/1.18/src/generated/resources/data/minecraft/minecard_cards).