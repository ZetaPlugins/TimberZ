![Banner](https://cdn.modrinth.com/data/hjNMOOnF/images/e8fb857eda4377a96bcc794f13835ea85c83c126.png)

---

![paper](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/compact/supported/paper_vector.svg)
![purpur](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/compact/supported/purpur_vector.svg)
[![github](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/compact/available/github_vector.svg)](https://github.com/ZetaPlugins/TimberZ)
[![modrinth](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/compact/available/modrinth_vector.svg)](https://modrinth.com/project/timberz)
[![discord-plural](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/compact/social/discord-plural_vector.svg)](https://strassburger.org/discord)
[![gitbook](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/compact/documentation/gitbook_vector.svg)](https://docs.zetaplugins.com/timberz)
[![generic-plural](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/compact/translate/generic-plural_vector.svg)](https://gitlocalize.com/repo/10274)

TimberZ is a Minecraft plugin that let's you cut down trees in an instant. You can either have it always active, toggleable, make special lumberjack axes or have a custom enchantment.

## Features
- ✅ Advanced Tree Detection
- ✅ Custom Enchantment
- ✅ Customizable Tree Types
- ✅ Cutting down animation
- ✅ Instant Leaf Decay
- ✅ Toggleable Timber mode
- ✅ Custom item support
- ✅ HEX Colors
- ✅ Highly customizable
- ✅ Customizable messages
- ✅ Easy setup
- ✅ Multiple languages

## Permissions

- `timberz.usetimber` - Allows the player to use TimberZ. (true by default)
- `timberz.admin` - Allows the player to use admin commands. (Only for OPs by default)

## Compatability

### ✅ PlaceholderAPI

TimberZ supports PlaceholderAPI, allowing you to use placeholders in your messages and configurations:

- `%timberz_can_timber%` - Returns true if the player can use TimberZ. This takes permissions, regions and other restrictions into account.

### ✅ WorldGuard

TimberZ supports WorldGuard, allowing you to restrict TimberZ usage in certain regions. You can use the `timber` flag to allow or disallow TimberZ usage in a region.

### ✅ AuraSkills

Using TimberZ with AuraSkills, gives players the correct amount of experience when cutting down trees. You can also add a custom XP multiplier to the TimberZ configuration file.

## Configuration

You can find the configuration files in the `plugins/TimberZ` folder. The main configuration file is `config.yml`, and you can also find a `blocks.yml` file that contains the block mappings for logs, leaves, and saplings.

<details>
<summary>config.yml</summary>

```yml
#   _______ _           _                 ______
#  |__   __(_)         | |               |___  /
#     | |   _ _ __ ___ | |__   ___ _ __     / /
#     | |  | | '_ ` _ \| '_ \ / _ \ '__|   / /
#     | |  | | | | | | | |_) |  __/ |     / /__
#     |_|  |_|_| |_| |_|_.__/ \___|_|    /_____|

# === COLOR CODES ===
# This plugin supports old color codes like: &c, &l, &o, etc.
# It also supports MiniMessage, a more advanced way to format messages:
# https://docs.advntr.dev/minimessage/format.html
# With MiniMessage, you can add HEX colors, gradients, hover and click events, etc.


# === GENERAL SETTINGS ===

# If set to true, TimberZ will check for updates and let you know if there's a newer version
checkForUpdates: true

# Set the language to any code found in the "lang" folder (don't add the .yml extension)
# You can add your own language files. Use https://github.com/ZetaPlugins/TimberZ/tree/main/src/main/resources/lang/en-US.yml as a template
# If you want to help translating the plugin, please refer to this article: https://docs.zetaplugins.com/#contributing
#  | en-US | de-DE | pl-PL | ru-RU |
lang: "en-US"

# The accent color of the plugin. This color replaces %ac% in the lang files.
accentColor: "<#00D26A>"


# === TIMBER SETTINGS ===

# Regardless of the settings below, you can disallow TimberZ for a specific player by disallowing them the "timberz.usetimber" permission.

# If set to true, players can enable or disable timber mode by pressing F with a qualifying axe in hand
toggleTimber: true

# If set to true, you can only use axes that have the "Timber" enchantment which can be applied like a normal Enchantment.
requireCustomEnchant: true

# If set to true, you can only use axes with a customModelData value in the list below
restrictAxeModelData: false

# If restrictAxeModelData is set to true, you can only use axes with a customModelData value in this list
allowedModelData:
  - 100

# If set to true, Leaves will decay instantly when cutting down a tree
instantLeafDecay: true

# If set to true, TimberZ will replant the tree after cutting it down
replant: true

# Modify how much durability is removed from the axe when cutting down a tree
# Formula: logCount * durabilityMultiplier
durabilityMultiplier: 1.0

# The minimum durability the axe must have after cutting down a tree
minDurability: 10

# If set to true, Players will need to use an axe (defined in the blocks.yml file) to use TimberZ
# When this is set to false, you can no longer toggle TimberZ by pressing F
requireAxeMaterial: true

# If set to true, you can insta mine leaves when the TimberZ conditions are met
instaBreakLeavesWithTimber: false

# Send a message to the player when they try to use timber mode in a WorldGuard region that does not allow it
messageOnRegionViolation: false

# If using AuraSkills, you can have a custom XP multiplier for the Foraging skill
# Example: If you set this to 2.0, players will receive double the XP for using TimberZ
# Example2: If you set this to 0.5, players will receive half the XP for using TimberZ
auraSkillsXPMultiplier: 1.0


# === TREE DETECTION ===

# Only change these settings if you know what you are doing!
# These are used to detect trees and their leaves and are already fine tuned.

leavesSearchRadius: 4
maxTreeSize: 500
maxSearchRadius: 1
diagonalSearchRange: 2
minLeavesRequired: 5
minLogsRequired: 3
```

</details>

<details>
<summary>blocks.yml</summary>

```yml
logToLeafMap:
  - "OAK_LOG:OAK_LEAVES"
  - "BIRCH_LOG:BIRCH_LEAVES"
  - "SPRUCE_LOG:SPRUCE_LEAVES"
  - "JUNGLE_LOG:JUNGLE_LEAVES"
  - "ACACIA_LOG:ACACIA_LEAVES"
  - "DARK_OAK_LOG:DARK_OAK_LEAVES"
  - "MANGROVE_LOG:MANGROVE_LEAVES"
  - "CHERRY_LOG:CHERRY_LEAVES"
  - "STRIPPED_OAK_LOG:OAK_LEAVES"
  - "STRIPPED_BIRCH_LOG:BIRCH_LEAVES"
  - "STRIPPED_SPRUCE_LOG:SPRUCE_LEAVES"
  - "STRIPPED_JUNGLE_LOG:JUNGLE_LEAVES"
  - "STRIPPED_ACACIA_LOG:ACACIA_LEAVES"
  - "STRIPPED_DARK_OAK_LOG:DARK_OAK_LEAVES"
  - "STRIPPED_MANGROVE_LOG:MANGROVE_LEAVES"
  - "STRIPPED_CHERRY_LOG:CHERRY_LEAVES"
  - "PALE_OAK_LOG:PALE_OAK_LEAVES"

logToSaplingMap:
  - "OAK_LOG:OAK_SAPLING"
  - "BIRCH_LOG:BIRCH_SAPLING"
  - "SPRUCE_LOG:SPRUCE_SAPLING"
  - "JUNGLE_LOG:JUNGLE_SAPLING"
  - "ACACIA_LOG:ACACIA_SAPLING"
  - "DARK_OAK_LOG:DARK_OAK_SAPLING"
  - "MANGROVE_LOG:MANGROVE_PROPAGULE"
  - "CHERRY_LOG:CHERRY_SAPLING"
  - "STRIPPED_OAK_LOG:OAK_SAPLING"
  - "STRIPPED_BIRCH_LOG:BIRCH_SAPLING"
  - "STRIPPED_SPRUCE_LOG:SPRUCE_SAPLING"
  - "STRIPPED_JUNGLE_LOG:JUNGLE_SAPLING"
  - "STRIPPED_ACACIA_LOG:ACACIA_SAPLING"
  - "STRIPPED_DARK_OAK_LOG:DARK_OAK_SAPLING"
  - "STRIPPED_MANGROVE_LOG:MANGROVE_PROPAGULE"
  - "STRIPPED_CHERRY_LOG:CHERRY_SAPLING"
  - "PALE_OAK_LOG:PALE_OAK_SAPLING"

axes:
  - "WOODEN_AXE"
  - "STONE_AXE"
  - "GOLDEN_AXE"
  - "IRON_AXE"
  - "DIAMOND_AXE"
  - "NETHERITE_AXE"
```

</details>

## Support

If you need help with the setup of the plugin, or found a bug, you can join our discord [here](https://strassburger.org/discord).

[![discord-plural](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/compact/social/discord-plural_vector.svg)](https://strassburger.org/discord)
[![gitbook](https://cdn.jsdelivr.net/npm/@intergrav/devins-badges@3/assets/compact/documentation/gitbook_vector.svg)](https://docs.zetaplugins.com/timberz)

---

[![Usage](https://bstats.org/signatures/bukkit/TimberZ.svg)](https://bstats.org/plugin/bukkit/TimberZ/25743)
