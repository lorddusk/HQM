### 5.20.0 for 1.21.1 (2026-10-?? by canitzp):
- Port to 1.21.1 and dependency updates
- The permission level of all `/hqm` subcommands that required one was lowered to 2 (gamemaster), except for `/hqm edit`, which stays at 4 (owner)
- Removed execution of `/hqm` without a subcommand
- `/hqm lives <players>` is now executed correctly when the selector matches multiple players
- Removed some unused translation keys
- Fixed fluid rendering (#689), enchantments and banner patterns in books
- Fixed delivery system and quest tracker losing settings on reload
- Fixed bags used up without loot, NaN progress, option link and location task bugs
- Fixed team disband crash with offline members, extra player data on Fabric
- Fixed edit mode: mob picker, reputation editor, reputation bars, unsaved count
- Fixed rotten heart growing instead of being used up
- Fixed horse taming not counting for quests on Fabric
- Consume tasks take fluids from buckets and other fluid items on manual hand-in
- Fixed location task distance showing a squared value
- Long quest and task names are cut off instead of running over the page
- Command rewards no longer announce commands to admins (#690)
- Broken files and quests no longer stop loading (#452, #457)
- Server ignores malformed client packets
- Added gametests

### 5.19.0 for 1.20.6 (06.10.2024):
- Update to 1.20.6
- Remove broken heart rot timer feature
- Data component for enabled quest book data
- Fix player name in enabled quest book hover text

### 5.18.0 for 1.20.4 (30.08.2024):
- Update to 1.20.4

### 5.17.0 for 1.20.2 (22.03.2024):
- Update to 1.20.2

### 5.16.0 for 1.20.1 (30.10.2023):
- Added `/hqm reset` command
- Fix world load crash when the world has quest progress data with a missing quest

### 5.15.0 for 1.20.1 (23.08.2023):
- Update to 1.20.1

### 5.14.2 for 1.19.4 (30.10.2023):
- Fix world load crash when the world has quest progress data with a missing quest

### 5.14.1 for 1.19.4 (15.08.2023):
- Fix multiplayer data sync issue introduced in 5.13.0

### 5.14.0 for 1.19.4 (26.05.2023):
- Update to 1.19.4

### 5.13.2 for 1.19.2 (30.10.2023):
- Fix world load crash when the world has quest progress data with a missing quest
- Fix multiplayer data sync issue

### 5.13.1 for 1.19.2 (24.05.2023):
- Fix line wrapping for item reward tooltips in the quest book

### 5.13.0 for 1.19.2 (22.01.2023):
- Support for addons to add new quest task types
- When `SPAWN_BOOK` is turned on, spawn a quest book regardless of whether edit mode is on or not
- Potentially fix an obscure crash while editing quests
- Fix loot tier deletion crash

### 5.12.2 for 1.19.2 (18.10.2022):
- Fix reputation and reputation marker names
- Fix bag loot group renaming crash

### 5.12.1 for 1.19.2 (25.08.2022):
- Updated block and item textures

### 5.12.0 for 1.19.2 (25.08.2022):
- Update to 1.19.2
- No longer includes LibBlockAttributes
- Now uses the Fabric API for fluid transfer

### 5.11.3 for 1.18.2 (24.05.2023):
- Fix line wrapping for item reward tooltips in the quest book

### 5.11.2 for 1.18.2 (18.10.2022):
- Fix reputation and reputation marker names
- Fix bag loot group renaming crash

### 5.11.1 for 1.18.2 (11.08.2022):
- Add loot tables to blocks
- Restore functionality of the tracker block
- Fix issue with the delivery system block entity
- Fluid insertion into the delivery system is now handled more correctly

### 5.11.0 for 1.18.2 (10.06.2022):
- Editable text can now optionally be translated

### 5.10.1 for 1.18.2 (15.04.2022):
- Fix issue with certain item tag data in quests

### 5.10.0 for 1.18.2 (20.03.2022):
- Update to 1.18.2

### 5.9.3 for 1.18.1 (17.02.2022):
- Fix singleplayer crash related to the party menu

### 5.9.2 for 1.18.1 (03.02.2022):
- Fix HQM data only being saved when shutting down normally

### 5.9.1 for 1.18.1 (25.01.2022):
- Fix a crash from deserializing an item stack special case

### 5.9.0 for 1.18.1 (18.12.2021):
- Update to 1.18.1
- Fix buttons for item quest tasks

### 5.8.2 for 1.17.1 (03.02.2022):
- Fix HQM data only being saved when shutting down normally
- Fix a crash from deserializing an item stack special case

### 5.8.1 for 1.17.1 (19.12.2021):
- Fix buttons for item quest tasks

### 5.8.0 for 1.17.1 (01.12.2021):
- Checkbox quest task
- Text boxes are now more feature-complete
- Tweak how some lang entries work
- Removed language mixin that caused some mod incompatibility

### 5.7.2 for 1.17.1 (07.10.2021):
- Architectury API is now a required dependency, Cloth API is no longer required
- Fix crash on dedicated server startup
- Fix issues with the `LOSE_QUEST_BOOK` config option
- Taming tasks now work with more mob types
- Fix block break tasks
- Removed the `SERVER_SYNC` config option
- Fix loading reputation tasks across multiple quest sets
- Fix numerical hotkeys for edit modes during quest editing
- Fix missing scroll bar when picking reputation types on quest maps
- Fix mouse wheel scrolling issues with HQM's scroll bars
- Fix incorrect item reward limits

### 5.7.0 for 1.17.1 (23.09.2021):
- Properly declare all mod dependencies
- Fluid support for detection tasks
- Fluid support for quest and task icons
- When setting an item or fluid to a quest task, it will also be used as the quest icon (if the quest has no icon yet)
- Added recipe type `hardcorequesting:book_catalyst_shaped` to handle recipes where the quest book shouldn't be consumed
- Quest book items are no longer considered a container of themselves for recipes. Modpacks relying on this need to update their recipes
- Fix reward screen issue
- Tweak to mod loading
- Fluid rendering fixes
- Minor tweak to how item icons are serialized/deserialized

### 5.6.2 for 1.17.1 (02.09.2021):
- Includes changes meant for 5.6.1 that were not actually in 5.6.1
- Fix an extra rendering issue
- Presence of reputation rewards no longer prevents command rewards on the same quest from executing

### 5.6.1 for 1.17.1 (31.08.2021):
- Fix crash from old line draw code
- Fix some rendering issues

### 5.6.0 for 1.17.1 (30.08.2021):
- Update to 1.17.1

### 5.5.17 for 1.16.5 (15.04.2022):
- Fix singleplayer crash related to the party menu
- Fix issue with certain item tag data in quests

### 5.5.16 for 1.16.5 (03.02.2022):
- Fix HQM data only being saved when shutting down normally
- Fix a crash from deserializing an item stack special case

### 5.5.15 for 1.16.5 (08.10.2021):
- Architectury API is now a required dependency, Cloth API is no longer required
- Fix issues with the `LOSE_QUEST_BOOK` config option
- Fix block break tasks
- Removed the `SERVER_SYNC` config option
- Fix loading reputation tasks across multiple quest sets
- Fix numerical hotkeys for edit modes during quest editing
- Fix missing scroll bar when picking reputation types on quest maps
- Fix mouse wheel scrolling issues with HQM's scroll bars
- Fix incorrect item reward limits

### 5.5.13 for 1.16.5 (30.09.2021):
- Properly declare all mod dependencies
- Includes fixes meant for 5.5.12 that were missing from the last version
- Some minor backported changes

### 5.5.12 for 1.16.5 (08.09.2021):
- Fix place block task consuming items instead of observing items
- Tweak how certain lines are scaled with the display window
- Fix an issue with combining reputation and command rewards on the same quest
- Small logging message about loaded quests

### 5.5.11 for 1.16.5 (27.08.2021, Fabric only):
- Same changes as 5.5.10
- Fix crash when editing a consume task's item

### 5.5.10 for 1.16.5 (27.08.2021, Forge only):
- Tame tasks now default icons to mob eggs again
- Added soft limits on the number of parts in a task based on graphical space
- Fix quest task description issues by dropping translation key support for task descriptions (translation key support will be added back more properly at a later date)
- Fix missing data packets when connecting to a LAN world
- Fix reward bag issue on dedicated servers
- Fix issues with command rewards
- Fix performance issue with setting a quest parent

### 5.5.9 for 1.16.5 (17.08.2021, Fabric only):
- Fix startup crash on dedicated servers

### 5.5.8 for 1.16.5 (15.08.2021):
- Fix crash with 0.34.0+ versions of the Fabric API
- Fix various problems with repeatable quests
- Fix crash during multiplayer with certain quest tasks
- Fix issue with deserialization of reputation quest tasks from json
- Fix certain tasks getting triggered extra times under certain circumstances
- Fix problem with task requirements disappearing on load when setting item amounts higher than 127 through the gui
- Fix bag group text bar
- Fix crash from deleting an empty reward under certain circumstances
- Fix reputation not getting unselected on deletion
- Fluids in the player inventory now show up again when selecting items for consumable tasks

### 5.5.6 for 1.16.5 (31.01.2021):
- Added the `ENABLE_TEAMS` config option. When disabled, the party section of the quest book is hidden (teams are currently extremely buggy)

### 5.5.5 for 1.16.5 (29.01.2021):
- Fix reading and writing of reputations and reputation markers. Thanks to CodeWolf219

### 5.5.4 for 1.16.5 (08.11.2020):
- Fix #521 (sound events are now registered properly through the mod loader's registries)

### 5.5.3 for 1.16.x (28.10.2020):
- Fix #516, fix #520
- Blocks, items and block entities are now registered through the mod loader's registries, which fixes issues on Forge
- Death stats are now synced to the client in a simplified format
- Better JSON exception handling when reading death stats and bag groups (`BagAdapter` was renamed to `GroupAdapter`)

### 5.5.2 for 1.16.x (13.10.2020):
- Fix #515 (listed as #551 on CurseForge): fix handling of the modified parent requirement count of quests

### 5.5.1 for 1.16.x (10.10.2020):
- Fix #512 (Forge: client-only @Environment annotations are now transformed to @OnlyIn)
- Fix #514 (quest task entries that fail to load are now skipped when reading quest tasks)
- Fix party not working
- Implemented the Forge fluid handler for the delivery system
- Reverted texture changes

### 5.5.0 for 1.16.x (09.10.2020):
This is the first release since 5.4.0 and ports HQM to 1.16 (by shedaniel). It is the first version released for both Fabric and Forge, built from a shared common module.
- Port to 1.16.2
- Support for Fabric and Forge from the same codebase
- Fluid rendering support
- Rewrote the file system logic, there is no more `remotes` folder as an intermediary that sometimes got lost
- Teams now only sync the team the player is in
- Rewrote the gson adapters
- Improved keybindings
- Fix #9 (using a reward bag now updates the held item stack properly)
- Fix quest book not dropping on death
- Fix player inventories not dropping on death

### 5.4.0-hotfix1 for 1.12.x (13.05.2019):
- Fix a bug with EnderIO conduits causing a Null Pointer Error. This may make the detection of breaking these less precise or prevent them from functioning as a quest objective, but they didn't previously function as a quest objective without crashing, so it's not a huge loss.

### 5.4.0 for 1.12.x (22.02.2019 by canitzp, duely):
To accomplish this release of HQM we used a special made modpack with a special server to "field test" the mod completely. Most bugs are fixed through that!

- A lot of internal changes, especially changing string uuid instances to proper UUID instances
- **Fixing server bug, that quests are missing after a rejoin or server restart! This was a very often heard, but difficult to reproduce bug**
- Quest sync from a server does now work properly, even if the server quests are updated
- The QDS is now functioning properly again
- Click on a requirement item in the quest books opens the jei recipes for it. thanks duely
- Added TUMAT and TheOneProbe integration (same behaviour as Waila/Hwyla)

### 5.3.5 for 1.12.x (19.12.2018 by canitzp, duely, codewarrior0, TataTawa):
- Some code cleanup and therefor fixing a team creation bug #407 (and several similar issues)
- Added command to toggle edit mode, without game restart! Thanks duely
- Fixing grey tooltip in reward gui. Thanks to codewarrior0 and duely
- Performance increase in large quest books up to 5000%! Thanks TataTawa
- Fixed some null ItemStacks. Thanks duely

### 5.3.4 for 1.12.x (04.10.2018 by canitzp & duely):
- [Support larger quest data sizes by splitting information. thanks to duely](https://github.com/lorddusk/HQM/commit/c2ba8e2c1be5dfea8c25a98223b4696468bead8d)
- Fixed some QDS issues (#396)
- Blind fixed Location Task issue (#400)

### 5.3.3 for 1.12.x (?):
- no record of changes since this version is completely removed from curseforge

### 5.3.2 for 1.12.x (15.08.2018 by canitzp):
- Blind fix #392 and prevented potential bugs
- Fix for reward bag lags and crashes #318
- Fixed Bags are in all Creative Tabs

BETA version since 20.08.2018, cause I got no new issues at GitHub. pls provide every error found, at the issue tracker

### 5.3.1 for 1.12.x (14.08.2018 by canitzp):
- Fixed startup crash due to not obfuscation of methods and fields. I don't know why this sometimes happens

### 5.3.0 for 1.12.x (14.08.2018 by canitzp):
_This version has some serious compiler issues and is not usable. -> 5.3.1_
- General fore and dependences update
- Update Russian language (thx to yaroslav4167)
- Fix #284 (Fluid NPE)
- Blind Fix #275 (Reward Label NPE)
- Blind Fix #377 (Crash on new world start cause ConcurrentModificationException)
- Blind Fix for QuestData syncing error, when too much quests


### 5.2.0 Alpha1 for 1.12.x (24.10.2017 by canitzp):
- Update to all 1.12 versions