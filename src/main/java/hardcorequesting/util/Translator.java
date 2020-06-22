package hardcorequesting.util;


import com.google.common.collect.Lists;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import hardcorequesting.client.interfaces.GuiColor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import java.util.*;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Translator {
    
    private static final Map<String, String> MAP = new HashMap<>();
    
    static {
        String s = "{\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"_comment\": \"TRANSLATE GUIDELINES\",\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"_comment\": \"%s is where something will be filled in at runtime.\",\n" +
                   "  \"_comment\": \"Don't remove it since it will make things weird.\",\n" +
                   "  \"_comment\": \"Just put it on the correct place in the sentence.\",\n" +
                   "  \"_comment\": \"To use the % use %% to escape the formatter.\",\n" +
                   "  \"_comment\": \"Use [[singular||plural]] to support singular and plural\",\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"_comment\": \"GENERAL\",\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"itemGroup.hqm\": \"Hardcore Questing Mode\",\n" +
                   "  \"hqm.message.editMode\": \"To enter or leave HQM edit mode, run /hqm edit in creative. This message can be disabled in configuration.\",\n" +
                   "  \"hqm.message.noHardcore\": \"This server doesn't have Hardcore Questing Mode enabled.\",\n" +
                   "  \"hqm.message.hardcore\": \"This server currently has Hardcore Questing Mode enabled!\",\n" +
                   "  \"hqm.message.livesLeft\": \"You currently have %s [[live||lives]] left.\",\n" +
                   "  \"hqm.message.lostLife\": \"You just lost a life. You have %s [[live||lives]] left\",\n" +
                   "  \"hqm.message.lostTeamLife\": \"%s just lost a life %s. You have %s [[live||lives]] left\",\n" +
                   "  \"hqm.message.andBanned\": \"and got banned\",\n" +
                   "  \"hqm.message.gameOver\": \"You're out of lives. Game over, man, it's game over!\",\n" +
                   "  \"hqm.message.singlePlayerHardcore\": \"BAM BAM!!! You're dead! Game over! (Hardcore doesn't really work in Single Player. Try deleting your world and starting over!)\",\n" +
                   "  \"hqm.message.vanillaHardcore\": \"Hardcore mode don't work together with vanilla hardcore mode. Will try to disable it...\",\n" +
                   "  \"hqm.message.vanillaHardcoreOn\": \"Vanilla hardcore mode is already enabled. Can't enable Hardcore Mode.\",\n" +
                   "  \"hqm.message.vanillaHardcoreOverride\": \"Vanilla hardcore mode has now been disabled. Please reopen your world for the change to take full effect.\",\n" +
                   "  \"hqm.message.noPlayer\": \"That player does not exist.\",\n" +
                   "  \"hqm.message.questActivated\": \"Questing mode has been activated. Enjoy!\",\n" +
                   "  \"hqm.message.questHardcore\": \"Hardcore Mode has been activated. Enjoy!\",\n" +
                   "  \"hqm.message.questAlreadyActivated\": \"Questing mode is already activated.\",\n" +
                   "  \"hqm.message.hardcoreAlreadyActivated\": \"Hardcore Mode is already activated.\",\n" +
                   "  \"hqm.message.hardcoreDisabled\": \"Hardcore Mode disabled.\",\n" +
                   "  \"hqm.message.noQuestYet\": \"Questing Mode isn't enabled yet. use '/hqm quest' to enable it.\",\n" +
                   "  \"hqm.message.noHardcoreYet\": \"Hardcore Mode isn't enabled yet. use '/hqm hardcore' to enable it.\",\n" +
                   "  \"hqm.message.positiveNumbers\": \"Please use only positive numbers.\",\n" +
                   "  \"hqm.message.posNumberAndPlayer\": \"Please use only positive numbers and / or a correct Playername.\",\n" +
                   "  \"hqm.message.cantRemoveLives\": \"You currently have %s [[live||lives]] remaining, you can't remove that much lives.\",\n" +
                   "  \"hqm.message.removeLives\": \"You have removed %s [[live||lives]] from your lifepool.\",\n" +
                   "  \"hqm.message.removeLivesFrom\": \"You have removed %s [[live||lives]] from %s.\",\n" +
                   "  \"hqm.message.removeLivesBy\": \"You had %s [[live||lives]] removed by %s\",\n" +
                   "  \"hqm.message.cantAddLives\": \"You can't have more than %s [[live||lives]].\",\n" +
                   "  \"hqm.message.addLives\": \"You have added %s [[live||lives]] to your lifepool.\",\n" +
                   "  \"hqm.message.addLivesTo\": \"You have added %s [[live||lives]] to %s.\",\n" +
                   "  \"hqm.message.addLivesBy\": \"You had %s [[live||lives]] added by %s\",\n" +
                   "  \"hqm.message.cantGiveMoreLives\": \"You can't give %s more than %s lives.\",\n" +
                   "  \"hqm.message.haveMaxLives\": \"You already have maximum lives.\",\n" +
                   "  \"hqm.massage.setLivesInstead\": \"Setting %s to %s lives instead.\",\n" +
                   "  \"hqm.massage.setLivesBy\": \"You have got your lives set to %s by %s\",\n" +
                   "  \"hqm.message.haveRemaining\": \"You have %s remaining\",\n" +
                   "  \"hqm.message.hasLivesRemaining\": \"%s has %s [[live||lives]] remaining\",\n" +
                   "  \"hqm.message.version\": \"Hardcore Questing Mode - Version : %s\",\n" +
                   "  \"hqm.message.addOne\": \"You have added 1 to your total life.\",\n" +
                   "  \"hqm.message.eatRottenHearth\": \"Why did you eat a rotten heart?\",\n" +
                   "  \"hqm.message.hearthDecay\": \"One or more of your hearts has just decade into a Rotten Heart.\",\n" +
                   "  \"hqm.message.alreadyEditing\": \"Another OP is already accessing this player's quests.\",\n" +
                   "  \"hqm.message.bookNoPermission\": \"You don't have permission to use this book.\",\n" +
                   "  \"hqm.message.bookNoData\": \"Still loading quest data.\",\n" +
                   "  \"hqm.message.bookNoEntry\": \"No entry was found for that player.\",\n" +
                   "  \"hqm.message.bookNoPlayer\": \"No player entry could be loaded for you. Please try again soon or report to your server administrator. This shouldn't happen.\",\n" +
                   "  \"hqm.message.noTaskSelected\": \"You currently have not selected any tasks\",\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"_comment\": \"BLOCKS\",\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"block.hardcorequesting.item_barrel\": \"Quest Delivery System\",\n" +
                   "  \"tile.hqm:item_barrel.nonBound\": \"You currently have not bound a quest to the QDS\",\n" +
                   "  \"tile.hqm:item_barrel.boundTo\": \"The QDS is currently bound to '%s'\",\n" +
                   "  \"tile.hqm:item_barrel.bindTo\": \"You bound '%s' to the QDS\",\n" +
                   "  \"tile.hqm:item_barrel.selectedTask\": \"The task '%s' has been selected and can now be applied to a QDS by right-clicking with the quest book.\",\n" +
                   "  \"block.hardcorequesting.quest_tracker\": \"Quest Tracking System\",\n" +
                   "  \"tile.hqm:quest_tracker.offLimit\": \"You're not in edit mode. This block is off limit.\",\n" +
                   "  \"tile.hqm:quest_tracker.bindTo\": \"You bound '%s' to the Quest Tracker.\",\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"_comment\": \"ITEMS\",\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"itemGroup.hardcorequesting.hardcorequesting\": \"Hardcore Questing\",\n" +
                   "  \"item.hardcorequesting.quest_book\": \"Quest Book\",\n" +
                   "  \"item.hardcorequesting.enabled_quest_book\": \"Quest Book - OP Edition\",\n" +
                   "  \"item.hqm:quest_book_1.useAs\": \"Use book as: %s\",\n" +
                   "  \"item.hqm:quest_book_1.invalid\": \"Invalid book!\",\n" +
                   "  \"item.hardcorequesting.quarterheart\": \"Quarter of a heart\",\n" +
                   "  \"item.hardcorequesting.halfheart\": \"Half a heart\",\n" +
                   "  \"item.hardcorequesting.threequartsheart\": \"Three quarters of a heart\",\n" +
                   "  \"item.hardcorequesting.heart\": \"Full heart\",\n" +
                   "  \"item.hqm:hearts_heart.tooltip\": \"Consume to get an extra life\",\n" +
                   "  \"item.hardcorequesting.hearts_heart.freshness\": \"Current freshness : %s %%\",\n" +
                   "  \"item.hardcorequesting.rottenheart\": \"Rotten heart\",\n" +
                   "  \"item.hqm:hearts_rottenheart.tooltip\": \"Rotten Heart. Do Not Eat\",\n" +
                   "  \"item.hardcorequesting.basic_bag\": \"Reward Bag\",\n" +
                   "  \"item.hardcorequesting.good_bag\": \"Reward Bag\",\n" +
                   "  \"item.hardcorequesting.greater_bag\": \"Reward Bag\",\n" +
                   "  \"item.hardcorequesting.epic_bag\": \"Reward Bag\",\n" +
                   "  \"item.hardcorequesting.legendary_bag\": \"Reward Bag\",\n" +
                   "  \"item.hardcorequesting.hqm_invalid_item\": \"Invalid Item\",\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"_comment\": \"QUESTS\",\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"hqm.quest.claim\": \"Claim reward\",\n" +
                   "  \"hqm.quest.manualSubmit\": \"Manual submit\",\n" +
                   "  \"hqm.quest.manualDetect\": \"Manual detect\",\n" +
                   "  \"hqm.quest.requirement\": \"Requirement\",\n" +
                   "  \"hqm.quest.selectTask\": \"Select task\",\n" +
                   "  \"hqm.quest.rewards\": \"Rewards\",\n" +
                   "  \"hqm.quest.pickOne\": \"Pick one\",\n" +
                   "  \"hqm.quest.pickOneReward\": \"Pick one reward\",\n" +
                   "  \"hqm.quest.itemTaskChangeTo\": \"Click on the item task type you want to change to.\",\n" +
                   "  \"hqm.quest.itemTaskTypeOnly\": \"You can only change the type of item tasks.\",\n" +
                   "  \"hqm.quest.itemTaskTypeChange\": \"Select an item task you want to change the type of.\",\n" +
                   "  \"hqm.quest.createTasks\": \"Create tasks of different types by using the buttons below\",\n" +
                   "  \"hqm.quest.partyRepReward\": \"The party will receive this reputation when the first member claims their reward.\",\n" +
                   "  \"hqm.quest.partyRepRewardClaimed\": \"The party has already received this reputation.\",\n" +
                   "  \"hqm.quest.crtlNonEditor\": \"Hold Ctrl to see this as a non-editor.\",\n" +
                   "  \"hqm.quest.selected\": \"Selected\",\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"_comment\": \"BAGS\",\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"hqm.bag.basic\": \"Basic\",\n" +
                   "  \"hqm.bag.good\": \"Good\",\n" +
                   "  \"hqm.bag.greater\": \"Greater\",\n" +
                   "  \"hqm.bag.epic\": \"Epic\",\n" +
                   "  \"hqm.bag.legendary\": \"Legendary\",\n" +
                   "  \"hqm.bag.group\": \"%s Group\",\n" +
                   "  \"hqm.bag.unknown\": \"Unknown\",\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"_comment\": \"TEAM SETTINGS\",\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"hqm.team.sharedLives.title\": \"Shared lives\",\n" +
                   "  \"hqm.team.sharedLives.desc\": \"Everyone puts their lives into a shared pool. If anyone dies a life is removed from there. Everyone needs at least one life to be kept in the game, so if your death results in the party getting too few lives, you get banned. The others can keep playing.\",\n" +
                   "  \"hqm.team.individualLives.title\": \"Individual lives\",\n" +
                   "  \"hqm.team.individualLives.desc\": \"Everyone keeps their lives separated. If you run out of lives, you're out of the game. The other players in the party can continue playing with their lives.\",\n" +
                   "  \"hqm.team.allReward.title\": \"Multiple rewards\",\n" +
                   "  \"hqm.team.allReward.desc\": \"Everyone in the party can claim their rewards from a quest. This option gives more rewards than the others but can be disabled in the config.\",\n" +
                   "  \"hqm.team.anyReward.title\": \"Shared rewards\",\n" +
                   "  \"hqm.team.anyReward.desc\": \"The party receives one set of rewards when completing a quest. Anyone can claim the reward but as soon as it is claimed no body else can claim it.\",\n" +
                   "  \"hqm.team.randomReward.title\": \"Random rewards\",\n" +
                   "  \"hqm.team.randomReward.desc\": \"Each time the party completes a quest that set of rewards is assigned to a player. This player is the only one that can claim the rewards.\",\n" +
                   "  \"hqm.team.invalidPlayer.title\": \"Invite Error\",\n" +
                   "  \"hqm.team.invalidPlayer.desc\": \"The username does not match a player on this server. The players you invite must have logged on at least once before.\",\n" +
                   "  \"hqm.team.playerInParty.title\": \"Invite Error\",\n" +
                   "  \"hqm.team.playerInParty.desc\": \"That player is already in a party, you can't invite players from other parties.\",\n" +
                   "  \"hqm.team.usedTeamName.title\": \"Team Error\",\n" +
                   "  \"hqm.team.usedTeamName.desc\": \"That party name is already used by someone else.\",\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"_comment\": \"TASK TYPES\",\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"hqm.taskType.consume.title\": \"Consume task\",\n" +
                   "  \"hqm.taskType.consume.desc\": \"A task where the player can hand in items or fluids. One can also use the Quest Delivery System to submit items and fluids.\",\n" +
                   "  \"hqm.taskType.craft.title\": \"Crafting task\",\n" +
                   "  \"hqm.taskType.craft.desc\": \"A task where the player has to craft specific items.\",\n" +
                   "  \"hqm.taskType.location.title\": \"Location task\",\n" +
                   "  \"hqm.taskType.location.desc\": \"A task where the player has to reach one or more locations.\",\n" +
                   "  \"hqm.taskType.consumeQDS.title\": \"QDS task\",\n" +
                   "  \"hqm.taskType.consumeQDS.desc\": \"A task where the player can hand in items or fluids. This is a normal consume task where manual submit has been disabled to teach the player about the QDS\",\n" +
                   "  \"hqm.taskType.detect.title\": \"Detection task\",\n" +
                   "  \"hqm.taskType.detect.desc\": \"A task where the player needs specific items. These do not have to be handed in, having them in one's inventory is enough.\",\n" +
                   "  \"hqm.taskType.kill.title\": \"Killing task\",\n" +
                   "  \"hqm.taskType.kill.desc\": \"A task where the player has to kill certain monsters.\",\n" +
                   "  \"hqm.taskType.death.title\": \"Death task\",\n" +
                   "  \"hqm.taskType.death.desc\": \"A task where the player has to die a certain amount of times.\",\n" +
                   "  \"hqm.taskType.reputation.title\": \"Reputation task\",\n" +
                   "  \"hqm.taskType.reputation.desc\": \"A task where the player has to reach a certain reputation.\",\n" +
                   "  \"hqm.taskType.reputationKill.title\": \"Rep kill task\",\n" +
                   "  \"hqm.taskType.reputationKill.desc\": \"A task where the player has to kill other players with certain reputations.\",\n" +
                   "  \"hqm.taskType.tame.title\": \"Taming task\",\n" +
                   "  \"hqm.taskType.tame.desc\": \"A task where the player has to tame specific creatures.\",\n" +
                   "  \"hqm.taskType.advancement.title\": \"Advancement\",\n" +
                   "  \"hqm.taskType.advancement.desc\": \"A task where the player has to complete a specific advancement.\",\n" +
                   "  \"hqm.taskType.completion.title\": \"Quest Complete\",\n" +
                   "  \"hqm.taskType.completion.desc\": \"A task where the player must have already completed another quest.\",\n" +
                   "  \"hqm.taskType.break.title\": \"Break Block\",\n" +
                   "  \"hqm.taskType.break.desc\": \"A task where the player must break one or more blocks.\",\n" +
                   "  \"hqm.taskType.place.title\": \"Place Block\",\n" +
                   "  \"hqm.taskType.place.desc\": \"A task where the player must place one or more blocks.\",\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"_comment\": \"DEATH TYPES\",\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"hqm.deathType.lava\": \"Lava\",\n" +
                   "  \"hqm.deathType.fire\": \"Fire\",\n" +
                   "  \"hqm.deathType.suffocation\": \"Suffocation\",\n" +
                   "  \"hqm.deathType.thorns\": \"Thorns\",\n" +
                   "  \"hqm.deathType.drowning\": \"Drowning\",\n" +
                   "  \"hqm.deathType.starvation\": \"Starvation\",\n" +
                   "  \"hqm.deathType.fall\": \"Fall\",\n" +
                   "  \"hqm.deathType.void\": \"Void\",\n" +
                   "  \"hqm.deathType.crushed\": \"Crushed\",\n" +
                   "  \"hqm.deathType.explosions\": \"Explosions\",\n" +
                   "  \"hqm.deathType.monsters\": \"Monsters\",\n" +
                   "  \"hqm.deathType.otherPlayers\": \"Other Players\",\n" +
                   "  \"hqm.deathType.magic\": \"Magic\",\n" +
                   "  \"hqm.deathType.rottenHearts\": \"Rotten Hearts\",\n" +
                   "  \"hqm.deathType.other\": \"Other / Unknown\",\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"_comment\": \"DEATH STATS\",\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"hqm.deathStat.first\": \"1st\",\n" +
                   "  \"hqm.deathStat.second\": \"2nd\",\n" +
                   "  \"hqm.deathStat.third\": \"3rd\",\n" +
                   "  \"hqm.deathStat.worstPlayers\": \"Worst Players\",\n" +
                   "  \"hqm.deathStat.everyone\": \"Everyone\",\n" +
                   "  \"hqm.deathStat.noOneDied\": \"No one has died this way, yet.\",\n" +
                   "  \"hqm.deathStat.payer\": \"player\",\n" +
                   "  \"hqm.deathStat.payers\": \"players\",\n" +
                   "  \"hqm.deathStat.diedThisWay\": \"have died this way.\",\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"_comment\": \"TRACKER TYPES\",\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"hqm.tracker.team.title\": \"Team Tracker\",\n" +
                   "  \"hqm.tracker.team.desc\": \"Emits a redstone signal depending on how many teams that have completed the selected quest. Players without teams count as separate one person teams.\",\n" +
                   "  \"hqm.tracker.player.title\": \"Player Tracker\",\n" +
                   "  \"hqm.tracker.player.desc\": \"Emits a redstone signal depending on how many players that have completed this quest.\",\n" +
                   "  \"hqm.tracker.progressMax.title\": \"Progress Tracker (Max)\",\n" +
                   "  \"hqm.tracker.progressMax.desc\": \"Emits a redstone signal depending on the progress of this quest for the player/team that has the highest progress. This will only emit at full strength if someone has completed the quest.\",\n" +
                   "  \"hqm.tracker.progressClose.title\": \"Progress Tracker (Close)\",\n" +
                   "  \"hqm.tracker.progressClose.desc\": \"Emits a redstone signal depending on the progress of the nearest player. This will only emit at full strength if that player has completed the quest. This mode requires the players to be online, no matter the radius setting.\",\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"_comment\": \"PORTAL TYPES\",\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"hqm.portal.tech.title\": \"Tech Theme\",\n" +
                   "  \"hqm.portal.tech.desc\": \"This set works perfectly for your technical themed map.\",\n" +
                   "  \"hqm.portal.magic.title\": \"Magic Theme\",\n" +
                   "  \"hqm.portal.magic.desc\": \"This set works perfectly for your magical themed map.\",\n" +
                   "  \"hqm.portal.custom.title\": \"Custom Theme\",\n" +
                   "  \"hqm.portal.custom.desc\": \"Customize the theme by specify what block this block should look like.\",\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"_comment\": \"ITEM PRECISION\",\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"hqm.precision.precise\": \"Precise detection\",\n" +
                   "  \"hqm.precision.nbtFuzzy\": \"NBT independent detection\",\n" +
                   "  \"hqm.precision.fuzzy\": \"Fuzzy detection\",\n" +
                   "  \"hqm.precision.tagNbtFuzzy\": \"Tag & NBT independent detection\",\n" +
                   "  \"hqm.precision.tagFuzzy\": \"Tag Fuzzy detection\",\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"_comment\": \"REPEAT TYPES\",\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"hqm.repeat.none.title\": \"Not repeatable\",\n" +
                   "  \"hqm.repeat.none.desc\": \"This quest is not repeatable and can therefore only be completed once.\",\n" +
                   "  \"hqm.repeat.instant.title\": \"Instant repeat\",\n" +
                   "  \"hqm.repeat.instant.desc\": \"As soon as this quest is completed it can be completed again for another set of rewards\",\n" +
                   "  \"hqm.repeat.instant.message\": \"Instant Cooldown\",\n" +
                   "  \"hqm.repeat.interval.title\": \"Interval repeat\",\n" +
                   "  \"hqm.repeat.interval.desc\": \"At a specific interval this quest will be reset and available for completion again. The quest is only reset if it has already been completed.\",\n" +
                   "  \"hqm.repeat.interval.message\": \"Refreshes on interval\",\n" +
                   "  \"hqm.repeat.time.title\": \"Cooldown repeat\",\n" +
                   "  \"hqm.repeat.time.desc\": \"After completing this quest it goes on a cooldown, when this cooldown ends you can complete the quest again.\",\n" +
                   "  \"hqm.repeat.time.message\": \"Cooldown on completion\",\n" +
                   "  \"hqm.repeat.invalid\": \"Invalid Time\",\n" +
                   "  \"hqm.repeat.resetIn\": \"Resets in %s\",\n" +
                   "  \"hqm.repeat.nextReset\": \"Next reset: %s\",\n" +
                   "  \"hqm.repeat.day\": \"day\",\n" +
                   "  \"hqm.repeat.days\": \"days\",\n" +
                   "  \"hqm.repeat.and\": \"and\",\n" +
                   "  \"hqm.repeat.hour\": \"hour\",\n" +
                   "  \"hqm.repeat.hours\": \"hours\",\n" +
                   "  \"hqm.repeat.repeatable\": \"Repeatable Quest\",\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"_comment\": \"EDIT MODES\",\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"hqm.editMode.normal.title\": \"Cursor\",\n" +
                   "  \"hqm.editMode.normal.desc\": \"Use the book as if you would be in play mode.\",\n" +
                   "  \"hqm.editMode.move.title\": \"Move\",\n" +
                   "  \"hqm.editMode.move.desc\": \"Click and drag to move quests\",\n" +
                   "  \"hqm.editMode.create.title\": \"Create\",\n" +
                   "  \"hqm.editMode.create.desc\": \"Click to create quests, quest sets, and reward groups, among other things.\",\n" +
                   "  \"hqm.editMode.req.title\": \"Quest Requirements\",\n" +
                   "  \"hqm.editMode.req.desc\": \"Click on a quest to select it and then click on the quests you want as requirements for the selected quest.\\\\n\\\\nHold shift and click on the selected quest to remove its requirements.\",\n" +
                   "  \"hqm.editMode.size.title\": \"Change Size\",\n" +
                   "  \"hqm.editMode.size.desc\": \"Click on a quest to change its size, this is purely visual and has no impact on how the quest works.\",\n" +
                   "  \"hqm.editMode.rename.title\": \"Edit Text\",\n" +
                   "  \"hqm.editMode.rename.desc\": \"Click on the text you want to edit. Works with names and descriptions for quest sets, quests, tasks, main lore, reward groups and reward tiers.\",\n" +
                   "  \"hqm.editMode.rename.invalid_set\": \"Invalid name for set. Set names must be unique. Sets cannot be named \\\"sets\\\", \\\"reputations\\\", \\\"bags\\\", any of the Windows reserved filenames, or contain the characters < > : \\\" \\\\ / | ? or *.\",\n" +
                   "  \"hqm.editMode.item.title\": \"Change Item\",\n" +
                   "  \"hqm.editMode.item.desc\": \"Click on an item or item slot to open the item selector. This works for quest rewards and task items to give some examples.\",\n" +
                   "  \"hqm.editMode.task.title\": \"Create Task\",\n" +
                   "  \"hqm.editMode.task.desc\": \"Opens up the task creation menu.\",\n" +
                   "  \"hqm.editMode.delete.title\": \"Delete\",\n" +
                   "  \"hqm.editMode.delete.desc\": \"Be careful with this one, things you click on will be deleted. Works with quest sets, quests, tasks, rewards, task items and just about everything.\",\n" +
                   "  \"hqm.editMode.swap.title\": \"Change Set\",\n" +
                   "  \"hqm.editMode.swap.desc\": \"Click on a quest to move it to another set. Before using this you will have to use the \\\"Target Set\\\" command to select a target set.\",\n" +
                   "  \"hqm.editMode.swapSelect.title\": \"Target Set\",\n" +
                   "  \"hqm.editMode.swapSelect.desc\": \"Mark a set as the target for quest movement. The \\\"Change Set\\\" command can then be used to move quests to this set\",\n" +
                   "  \"hqm.editMode.tier.title\": \"Set group tier\",\n" +
                   "  \"hqm.editMode.tier.desc\": \"Selected a group and then click on a tier to set the group's tier.\",\n" +
                   "  \"hqm.editMode.bag.title\": \"Reward Bags\",\n" +
                   "  \"hqm.editMode.bag.desc\": \"Open up the reward bag menu. Here you will be able to modify the group tiers and add groups of items for the reward bags.\",\n" +
                   "  \"hqm.editMode.location.title\": \"Edit Information\",\n" +
                   "  \"hqm.editMode.location.desc\": \"Edit the target location for location tasks, or edit the specified advancement for advancement tasks.\",\n" +
                   "  \"hqm.editMode.repeatable.title\": \"Set Repeatability\",\n" +
                   "  \"hqm.editMode.repeatable.desc\": \"Change if a quest should be repeatable or not, and if so, the properties of the repeatability.\",\n" +
                   "  \"hqm.editMode.trigger.title\": \"Trigger Quests\",\n" +
                   "  \"hqm.editMode.trigger.desc\": \"Specify any properties for trigger quests.\",\n" +
                   "  \"hqm.editMode.mob.title\": \"Edit Creature\",\n" +
                   "  \"hqm.editMode.mob.desc\": \"Edit the creature target for killing and taming tasks.\",\n" +
                   "  \"hqm.editMode.questSelection.title\": \"Select Quest\",\n" +
                   "  \"hqm.editMode.questSelection.desc\": \"Mark a quest as the selected quest. When a quest is selected you can bind it to a Quest Tracker System or a Quest Gate System by right clicking it with a book. You can also bind it to a Quest Completed task by using the Name or Change Item tool on the task.\",\n" +
                   "  \"hqm.editMode.questOption.title\": \"Quest Option\",\n" +
                   "  \"hqm.editMode.questOption.desc\": \"Click on a quest to select it and then click on the quests you want to link it to. If an option linked quest is completed all quests it's linked to becomes invisible and uncompletable.\\\\n\\\\nHold shift and click on the selected quest to remove all its links.\",\n" +
                   "  \"hqm.editMode.changeTask.title\": \"Change Task\",\n" +
                   "  \"hqm.editMode.changeTask.desc\": \"Change the task type of item tasks.\",\n" +
                   "  \"hqm.editMode.reqParents.title\": \"Required parents\",\n" +
                   "  \"hqm.editMode.reqParents.desc\": \"Change how many of the parent quests that have to be completed before this one unlocks.\",\n" +
                   "  \"hqm.editMode.rep.title\": \"Reputation\",\n" +
                   "  \"hqm.editMode.rep.desc\": \"Open the reputation menu where you can create reputations and their tiers.\",\n" +
                   "  \"hqm.editMode.repValue.title\": \"Change value\",\n" +
                   "  \"hqm.editMode.repValue.desc\": \"Change the value of the different reputation tiers\",\n" +
                   "  \"hqm.editMode.repTask.title\": \"Edit reputation target\",\n" +
                   "  \"hqm.editMode.repTask.desc\": \"Change the configurations for a reputation target for the selected reputation task\",\n" +
                   "  \"hqm.editMode.repReward.title\": \"Edit reputation reward\",\n" +
                   "  \"hqm.editMode.repReward.desc\": \"Click on the reputation reward icon to bring up the reputation reward menu.\",\n" +
                   "  \"hqm.editMode.repBarCreate.title\": \"Add Reputation Bar\",\n" +
                   "  \"hqm.editMode.repBarCreate.desc\": \"Click to add a new reputation bar to display on the set page.\",\n" +
                   "  \"hqm.editMode.repBarChange.title\": \"Edit Reputation Bar\",\n" +
                   "  \"hqm.editMode.repBarChange.desc\": \"Click on the reputation bar you want to edit.\",\n" +
                   "  \"hqm.editMode.commandCreate.title\": \"Add a new Command\",\n" +
                   "  \"hqm.editMode.commandCreate.desc\": \"Adds a new Command to be executed on quest claim.\\\\n@p will be replaced by the player's name.\",\n" +
                   "  \"hqm.editMode.commandChange.title\": \"Change Commands\",\n" +
                   "  \"hqm.editMode.commandChange.desc\": \"Change existing Commands.\\\\n@p will be replaced by the player's name.\",\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"_comment\": \"COMMAND\",\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"hqm.command.notFound\": \"Command not found\",\n" +
                   "  \"hqm.command.noPermission\": \"You not have permission to use this command\",\n" +
                   "  \"hqm.command.loadFailed\": \"Loading failed\",\n" +
                   "  \"hqm.command.loadSuccess\": \"Loaded: %s\",\n" +
                   "  \"hqm.command.fileNotFound\": \"File not found\",\n" +
                   "  \"hqm.command.saveFailed\": \"Saving %s failed\",\n" +
                   "  \"hqm.command.savedTo\": \"Quests saved to: %s\",\n" +
                   "  \"hqm.command.questNotFound\": \"Quest page %s not found\",\n" +
                   "  \"hqm.command.editMode.enabled\": \"Editing mode is now enabled.\",\n" +
                   "  \"hqm.command.editMode.disabled\": \"Editing mode is now disabled.\",\n" +
                   "  \"hqm.command.editMode.server\": \"Editing mode does not function properly on servers and has been disabled.\",\n" +
                   "  \"hqm.command.editMode.useOP\": \"This command now toggles edit mode in single-player. To receive an operator quest book, use /hqm op instead. This message can be disabled in the configuration.\",\n" +
                   "  \"hqm.command.editMode.disableSync\": \"You must disable Server Sync in your configuration in order to properly and safely edit your quests without losing data.\",\n" +
                   "  \"hqm.command.info.help.start\": \"Available commands are:\",\n" +
                   "  \"hqm.command.help.syntax0\": \"/hqm help <command>\",\n" +
                   "  \"hqm.command.help.info0\": \"For help with all available commands.\",\n" +
                   "  \"hqm.command.version.syntax0\": \"/hqm version\",\n" +
                   "  \"hqm.command.version.info0\": \"Get mod version.\",\n" +
                   "  \"hqm.command.hardcore.syntax0\": \"/hqm hardcore\",\n" +
                   "  \"hqm.command.hardcore.info0\": \"Enable Hardcore mode.\",\n" +
                   "  \"hqm.command.quest.syntax0\": \"/hqm quest\",\n" +
                   "  \"hqm.command.quest.info0\": \"Enable Questing mode.\",\n" +
                   "  \"hqm.command.enable.syntax0\": \"/hqm enable\",\n" +
                   "  \"hqm.command.enable.info0\": \"Enable Hardcore mode and Questing mode.\",\n" +
                   "  \"hqm.command.edit.syntax0\": \"/hqm edit <player>\",\n" +
                   "  \"hqm.command.edit.info0\": \"Give yourself a book in edit mode, defaults: user.\",\n" +
                   "  \"hqm.command.lives.syntax0\": \"/hqm lives\",\n" +
                   "  \"hqm.command.lives.info0\": \"Check your current lives remaining.\",\n" +
                   "  \"hqm.command.lives.syntax1\": \"/hqm lives [player]\",\n" +
                   "  \"hqm.command.lives.info1\": \"Check a player's current lives remaining.\",\n" +
                   "  \"hqm.command.lives.syntax2\": \"/hqm lives add <player> <amount>\",\n" +
                   "  \"hqm.command.lives.info2\": \"Add lives to a player, defaults: user, 1\",\n" +
                   "  \"hqm.command.lives.syntax3\": \"/hqm lives remove <player> <amount>\",\n" +
                   "  \"hqm.command.lives.info3\": \"Remove lives from a player, defaults: user, 1\",\n" +
                   "  \"hqm.command.load.syntax0\": \"/hqm load [filename]\",\n" +
                   "  \"hqm.command.load.info0\": \"Load the given quest line into HQM.\",\n" +
                   "  \"hqm.command.load.syntax1\": \"/hqm load all\",\n" +
                   "  \"hqm.command.load.info1\": \"Load all quest pages into HQM.\",\n" +
                   "  \"hqm.command.save.syntax2\": \"/hqm load bags\",\n" +
                   "  \"hqm.command.save.info2\": \"Loads quest bags and tiers from JSON - already existing reward groups will not be loaded.\",\n" +
                   "  \"hqm.command.save.syntax0\": \"/hqm save [Quest Page] <filename>\",\n" +
                   "  \"hqm.command.save.info0\": \"Saves the given quest page to a JSON file, defaults: page name\",\n" +
                   "  \"hqm.command.save.syntax1\": \"/hqm save all\",\n" +
                   "  \"hqm.command.save.info1\": \"Saves all quest pages by their name\",\n" +
                   "  \"hqm.command.save.syntax2\": \"/hqm save bags\",\n" +
                   "  \"hqm.command.save.info2\": \"Saves all quest bag rewards to a JSON.\",\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"_comment\": \"REWARD GUI\",\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"hqm.rewardGui.chance\": \"%s%% chance to get this reward\",\n" +
                   "  \"hqm.rewardGui.tierReward\": \"%s Reward\",\n" +
                   "  \"hqm.rewardGui.close\": \"Click to close\",\n" +
                   "  \"hqm.rewardGui.shiftInfo\": \"Hold shift for more info\",\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"_comment\": \"EDIT MENU\",\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"hqm.edit.ok\": \"Ok\",\n" +
                   "  \"hqm.edit.cancel\": \"Cancel\",\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"_comment\": \"DEATH MENU\",\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"hqm.deathMenu.showWorst\": \"Show Worst\",\n" +
                   "  \"hqm.deathMenu.showTotal\": \"Show Total\",\n" +
                   "  \"hqm.deathMenu.total\": \"Total Deaths: %s\",\n" +
                   "  \"hqm.deathMenu.lots\": \"lots\",\n" +
                   "  \"hqm.deathMenu.deaths\": \"You've died %s [[time||times]].\",\n" +
                   "  \"hqm.deathMenu.deathsOutOf\": \"You've died %s of %s [[time||times]].\",\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"_comment\": \"DEATH TASK\",\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"hqm.deathTask.reqDeathCount\": \"Required death count\",\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"_comment\": \"MOB TASK\",\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"hqm.mobTask.reqKills\": \"Required kill count\",\n" +
                   "  \"hqm.mobTask.search\": \"Search\",\n" +
                   "  \"hqm.mobTask.nothingSelected\": \"Nothing Selected\",\n" +
                   "  \"hqm.mobTask.currentlySelected\": \"Currently Selected\",\n" +
                   "  \"hqm.mobTask.exactMatch.title\": \"Exact Match\",\n" +
                   "  \"hqm.mobTask.typeMatch.title\": \"Type Match\",\n" +
                   "  \"hqm.mobTask.exactMatch.desc\": \"Only matching monsters with the selected type, subtypes are not counted towards the killing count.\",\n" +
                   "  \"hqm.mobTask.typeMatch.desc\": \"Matching every monster that has the selected type or a subtype to the selected type.\",\n" +
                   "  \"hqm.mobTask.allKilled\": \"All killed\",\n" +
                   "  \"hqm.mobTask.partKills\": \"%s[%s%%] killed\",\n" +
                   "  \"hqm.mobTask.totalKills\": \"Kill a total of %s\",\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"_comment\": \"TAMING TASK\",\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"hqm.tameTask.reqKills\": \"Required tame count\",\n" +
                   "  \"hqm.tameTask.search\": \"Search\",\n" +
                   "  \"hqm.tameTask.nothingSelected\": \"Nothing Selected\",\n" +
                   "  \"hqm.tameTask.currentlySelected\": \"Currently Selected\",\n" +
                   "  \"hqm.tameTask.exactMatch.title\": \"Exact Match\",\n" +
                   "  \"hqm.tameTask.typeMatch.title\": \"Type Match\",\n" +
                   "  \"hqm.tameTask.exactMatch.desc\": \"Only matching creatures with the selected type, subtypes are not counted towards the taming count\",\n" +
                   "  \"hqm.tameTask.typeMatch.desc\": \"Matching every creature that has the selected type or a subtype to the selected type.\",\n" +
                   "  \"hqm.tameTask.allTamed\": \"All tamed\",\n" +
                   "  \"hqm.tameTask.partTames\": \"%s[%s%%] tamed\",\n" +
                   "  \"hqm.tameTask.totalTames\": \"Tame a total of %s\",\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"_comment\": \"REPUTATION REWARD\",\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"hqm.repReward.value\": \"Reward value\",\n" +
                   "  \"hqm.repReward.create\": \"Create new\",\n" +
                   "  \"hqm.repReward.delete\": \"Delete\",\n" +
                   "  \"hqm.repReward.noValidReps\": \"There are no valid reputations to choose from. Please go to the reputation menu (click on the reputation button on the left hand side of the book when browsing the main menu) and create at least one first.\",\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"_comment\": \"REPUTATION SETTING\",\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"hqm.repSetting.invRange\": \"Inverted range?\",\n" +
                   "  \"hqm.repSetting.lower\": \"Lower bounds\",\n" +
                   "  \"hqm.repSetting.upper\": \"Upper bounds\",\n" +
                   "  \"hqm.repSetting.preview\": \"Preview\",\n" +
                   "  \"hqm.repSetting.invalid\": \"Invalid\",\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"_comment\": \"REPUTATION\",\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"hqm.rep.noValueOf\": \"No value of\",\n" +
                   "  \"hqm.rep.anyValueOf\": \"Any value of\",\n" +
                   "  \"hqm.rep.not\": \"Not\",\n" +
                   "  \"hqm.rep.atLeastTwo\": \"You need at least two tiers\",\n" +
                   "  \"hqm.rep.notZero\": \"A tier can't have value 0\",\n" +
                   "  \"hqm.rep.unique\": \"Tiers must have unique values\",\n" +
                   "  \"hqm.rep.neutral\": \"Neutral: %s\",\n" +
                   "  \"hqm.rep.select\": \"Select a reputation to display\",\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"_comment\": \"REPUTATION KILL\",\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"hqm.repKil.kills\": \"Kills: %s/%s\",\n" +
                   "  \"hqm.repKil.killCount\": \"You've killed %s [[player||players]].\",\n" +
                   "  \"hqm.repKil.killCountOutOf\": \"You've killed %s of %s [[player||players]].\",\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"_comment\": \"REPUTATION VALUE\",\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"hqm.repValue.tierValue\": \"Tier value\",\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"_comment\": \"MENU TIER\",\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"hqm.menuTier.weights\": \"Weights\",\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"_comment\": \"MENU TRACKER\",\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"hqm.menuTracker.radius.title\": \"Player Radius\",\n" +
                   "  \"hqm.menuTracker.radius.desc\": \"Only includes players within the given distance. Leaving this at zero makes it detect any player, even offline ones.\",\n" +
                   "  \"hqm.menuTracker.noQuest\": \"No quest selected\",\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"_comment\": \"MENU TRIGGER\",\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"hqm.menuTrigger.taskCount\": \"Trigger tasks count\",\n" +
                   "  \"hqm.trigger.none.title\": \"Normal Quest\",\n" +
                   "  \"hqm.trigger.none.desc\": \"Just a normal quest.\",\n" +
                   "  \"hqm.trigger.quest.title\": \"Trigger Quest\",\n" +
                   "  \"hqm.trigger.quest.desc\": \"A trigger quest is an invisible quest. The quest can still be completed as usual but you can't claim any rewards for it or see it in any lists. It can be used to trigger other quests, hence its name.\",\n" +
                   "  \"hqm.trigger.task.title\": \"Trigger Tasks\",\n" +
                   "  \"hqm.trigger.task.desc\": \"Trigger tasks are the first few tasks of a quest that have to be completed before the quest shows up. The quest will be invisible until the correct amount of tasks have been completed. When the quest becomes visible the player can see the tasks that have already been completed.\",\n" +
                   "  \"hqm.trigger.anti.title\": \"Reversed Trigger\",\n" +
                   "  \"hqm.trigger.anti.desc\": \"This quest will be invisible until it is enabled (all its parent quests are completed). This way you can make a secret quest line appear all of a sudden when a known quest is completed.\",\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"_comment\": \"QUEST BOOK\",\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"hqm.questBook.saveAll\": \"Save all\",\n" +
                   "  \"hqm.questBook.open\": \"Open\",\n" +
                   "  \"hqm.questBook.createSet\": \"Create Set\",\n" +
                   "  \"hqm.questBook.createGroup\": \"Create Group\",\n" +
                   "  \"hqm.questBook.createTier\": \"Create Tier\",\n" +
                   "  \"hqm.questBook.reset\": \"Reset\",\n" +
                   "  \"hqm.questBook.create\": \"Create New\",\n" +
                   "  \"hqm.questBook.start\": \"Click here to start\",\n" +
                   "  \"hqm.questBook.playAgain\": \"Click to play again\",\n" +
                   "  \"hqm.questBook.lives\": \"Lives\",\n" +
                   "  \"hqm.questBook.party\": \"Party\",\n" +
                   "  \"hqm.questBook.quests\": \"Quests\",\n" +
                   "  \"hqm.questBook.reputation\": \"Reputation\",\n" +
                   "  \"hqm.questBook.showQuests\": \"Click here to show quests\",\n" +
                   "  \"hqm.questBook.deadOut\": \"If you die, you're out.\",\n" +
                   "  \"hqm.questBook.infiniteLives\": \"Hardcore mode is not active, you have an infinite amount of lives.\",\n" +
                   "  \"hqm.questBook.deaths\": \"You've currently died %s [[time||times]]\",\n" +
                   "  \"hqm.questBook.invites\": \"You have %s party [[invite||invites]]\",\n" +
                   "  \"hqm.questBook.notInParty\": \"You're currently not in a party.\",\n" +
                   "  \"hqm.questBook.inParty\": \"You're in a party with %s [[player||players]]\",\n" +
                   "  \"hqm.questBook.openParty\": \"Click here to open party window\",\n" +
                   "  \"hqm.questBook.resetParty\": \"Reset the quest progress for the entire party.\",\n" +
                   "  \"hqm.questBook.shiftCtrlConfirm\": \"Hold shift+ctrl while clicking to confirm.\",\n" +
                   "  \"hqm.questBook.maxRetrieval\": \"Maximum retrieval count\",\n" +
                   "  \"hqm.questBook.noRestriction\": \"Leave at 0 for no restriction\",\n" +
                   "  \"hqm.questBook.items\": \"%s items\",\n" +
                   "  \"hqm.questBook.allQuests\": \"All Quests Completed\",\n" +
                   "  \"hqm.questBook.percentageQuests\": \"%s%% Completed\",\n" +
                   "  \"hqm.questBook.locked\": \"Locked\",\n" +
                   "  \"hqm.questBook.lockedQuest\": \"Locked Quest\",\n" +
                   "  \"hqm.questBook.unclaimedRewards\": \"%s [[quest||quests]] with unclaimed rewards\",\n" +
                   "  \"hqm.questBook.createNewSet\": \"Click the button below to create a new empty quest set.\",\n" +
                   "  \"hqm.questBook.shiftSetReset\": \"Hold shift and click on quests to automatically complete them or reset their progress.\",\n" +
                   "  \"hqm.questBook.parentCount\": \"Requires %s/%s [[quest||quests]] to be completed.\",\n" +
                   "  \"hqm.questBook.parentCountElsewhere\": \"Requires %s/%s [[quest||quests]] to be completed elsewhere.\",\n" +
                   "  \"hqm.questBook.and\": \"and\",\n" +
                   "  \"hqm.questBook.hold\": \"Hold %s\",\n" +
                   "  \"hqm.questBook.holding\": \"Holding %s\",\n" +
                   "  \"hqm.questBook.completed\": \"Completed\",\n" +
                   "  \"hqm.questBook.unclaimedReward\": \"Unclaimed reward\",\n" +
                   "  \"hqm.questBook.reqOnly\": \"Only requires %s [[quest||quests]] to be completed.\",\n" +
                   "  \"hqm.questBook.reqMore\": \"Requires %s [[quest||quests]] to be completed. This is more than there are, weird.\",\n" +
                   "  \"hqm.questBook.reqAll\": \"Requires all %s [[quest||quests]] to be completed.\",\n" +
                   "  \"hqm.questBook.noTasks\": \"This quest has no tasks!\",\n" +
                   "  \"hqm.questBook.completedTasks\": \"%s/%s completed tasks.\",\n" +
                   "  \"hqm.questBook.invisLocked\": \"Invisible while locked\",\n" +
                   "  \"hqm.questBook.invisPerm\": \"Permanently invisible\",\n" +
                   "  \"hqm.questBook.invisCount\": \"Invisible until %s [[task has||tasks have]] been completed.\",\n" +
                   "  \"hqm.questBook.invisInherit\": \"Inherited invisibility\",\n" +
                   "  \"hqm.questBook.invisOption\": \"Invisible through quest option.\",\n" +
                   "  \"hqm.questBook.optionLinks\": \"Connected to %s [[quest||quests]] through option links.\",\n" +
                   "  \"hqm.questBook.childUnlocks\": \"Unlocks %s [[quest||quests]] elsewhere.\",\n" +
                   "  \"hqm.questBook.ctrlNonEditor\": \"Hold Ctrl to see this as a non-editor.\",\n" +
                   "  \"hqm.questBook.resetQuest\": \"Click to reset quest\",\n" +
                   "  \"hqm.questBook.completeQuest\": \"Click to complete quest\",\n" +
                   "  \"hqm.questBook.resetTask\": \"Click to reset task\",\n" +
                   "  \"hqm.questBook.completeTask\": \"Click to complete task\",\n" +
                   "  \"hqm.questBook.warning\": \"WARNING!\",\n" +
                   "  \"hqm.questBook.deleteOnClick\": \"You're now deleting everything you click on!\",\n" +
                   "  \"hqm.questBook.goBack\": \"Go back\",\n" +
                   "  \"hqm.questBook.backToMenu\": \"Back to the menu\",\n" +
                   "  \"hqm.questBook.rightClick\": \"You can also right click anywhere\",\n" +
                   "  \"hqm.questBook.totalQuests\": \"%s [[quest||quests]] in total\",\n" +
                   "  \"hqm.questBook.unlockedQuests\": \"%s unlocked [[quest||quests]]\",\n" +
                   "  \"hqm.questBook.completedQuests\": \"%s completed [[quest||quests]]\",\n" +
                   "  \"hqm.questBook.availableQuests\": \"%s [[quest||quests]] available for completion\",\n" +
                   "  \"hqm.questBook.unclaimedQuests\": \"%s [[quest||quests]] with unclaimed rewards\",\n" +
                   "  \"hqm.questBook.inclInvisiQuests\": \"%s [[quest||quests]] including invisible ones\",\n" +
                   "  \"hqm.questBook.moreInfo\": \"Click here for more info\",\n" +
                   "  \"hqm.questBook.itemRequirementProgress\": \"Progress\",\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"_comment\": \"TEAM LIST\",\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"hqm.teamList.done\": \"%s%% done\",\n" +
                   "  \"hqm.teamList.players\": \"Players: %s\",\n" +
                   "  \"hqm.teamList.lives\": \"Lives: %s\",\n" +
                   "  \"hqm.teamList.page\": \"Page %s\",\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"_comment\": \"LOCATION MENU\",\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"hqm.locationMenu.xTarget\": \"X target\",\n" +
                   "  \"hqm.locationMenu.yTarget\": \"Y target\",\n" +
                   "  \"hqm.locationMenu.zTarget\": \"Z target\",\n" +
                   "  \"hqm.locationMenu.dim\": \"Dimension\",\n" +
                   "  \"hqm.locationMenu.radius\": \"Radius\",\n" +
                   "  \"hqm.locationMenu.negRadius\": \"A negative radius ignores the player's location\",\n" +
                   "  \"hqm.locationMenu.location\": \"Your location\",\n" +
                   "  \"hqm.locationMenu.visFull.title\": \"Show All\",\n" +
                   "  \"hqm.locationMenu.visFull.desc\": \"Will display the location of the target to the player and therefore the distance to it as well. The maximum distance the player can be from the target (the radius) is also displayed.\",\n" +
                   "  \"hqm.locationMenu.visLocation.title\": \"Show Location\",\n" +
                   "  \"hqm.locationMenu.visLocation.desc\": \"The radius required to trigger the location is hidden from the user. The location is however still visible, and therefore even the distance to it.\",\n" +
                   "  \"hqm.locationMenu.visNone.title\": \"Hide info\",\n" +
                   "  \"hqm.locationMenu.visNone.desc\": \"The location, distance and radius will be hidden from the user. It's up to you to guide them through the map or through text.\",\n" +
                   "  \"hqm.locationMenu.visited\": \"Visited\",\n" +
                   "  \"hqm.locationMenu.mAway\": \"%sm away\",\n" +
                   "  \"hqm.locationMenu.mRadius\": \"%sm radius\",\n" +
                   "  \"hqm.locationMenu.wrongDim\": \"Wrong dimension\",\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"_comment\": \"ADVANCEMENT MENU\",\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"hqm.advancementMenu.visited\": \"Completed\",\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"_comment\": \"COMPLETION MENU\",\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"hqm.completedMenu.visited\": \"Completed\",\n" +
                   "  \"hqm.completionTask.firstline\": \"Use \\\"Quest Select\\\" tool\",\n" +
                   "  \"hqm.completionTask.secondline\": \"to pick a quest then\",\n" +
                   "  \"hqm.completionTask.thirdline\": \"click icon to set.\",\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"_comment\": \"REPEAT MENU\",\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"hqm.repeatMenu.days\": \"Days\",\n" +
                   "  \"hqm.repeatMenu.hours\": \"Hours\",\n" +
                   "  \"hqm.repeatMenu.mcDaysHours\": \"These are minecraft days and hours.\",\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"_comment\": \"PORTAL MENU\",\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"hqm.portalMenu.edit\": \"Edit item\",\n" +
                   "  \"hqm.portalMenu.collisionOnComplete\": \"Use collision when completed\",\n" +
                   "  \"hqm.portalMenu.texOnComplete\": \"Use textures when completed\",\n" +
                   "  \"hqm.portalMenu.collisionNonComplete\": \"Use collision when not completed\",\n" +
                   "  \"hqm.portalMenu.texNonComplete\": \"Use textures when not completed\",\n" +
                   "  \"hqm.portalMenu.noQuest\": \"No quest selected\",\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"_comment\": \"PARENT COUNT MENU\",\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"hqm.parentCount.count\": \"Parent count\",\n" +
                   "  \"hqm.parentCount.reqAll.title\": \"Requires all\",\n" +
                   "  \"hqm.parentCount.reqCount.title\": \"Requires specified amount\",\n" +
                   "  \"hqm.parentCount.reqAll.desc\": \"All parent quests have to be completed before this quest unlocks.\",\n" +
                   "  \"hqm.parentCount.reqCount.desc\": \"For this quest to unlock the player will have to complete a certain amount of parent quests. The required amount can be specified below.\",\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"_comment\": \"TEXT EDITOR\",\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"hqm.textEditor.copyAll\": \"Copy all\",\n" +
                   "  \"hqm.textEditor.paste\": \"Paste\",\n" +
                   "  \"hqm.textEditor.clear\": \"Clear\",\n" +
                   "  \"hqm.textEditor.clearPaste\": \"Clear & Paste\",\n" +
                   "  \"hqm.textEditor.unnamed\": \"Unnamed\",\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"_comment\": \"PARTY MENU\",\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"hqm.party.create\": \"Create party\",\n" +
                   "  \"hqm.party.invitePlayer\": \"Invite player\",\n" +
                   "  \"hqm.party.accept\": \"Accept\",\n" +
                   "  \"hqm.party.decline\": \"Decline\",\n" +
                   "  \"hqm.party.decideLater\": \"Decide later\",\n" +
                   "  \"hqm.party.kickPlayer\": \"Kick player\",\n" +
                   "  \"hqm.party.removeInvite\": \"Remove invite\",\n" +
                   "  \"hqm.party.leave\": \"Leave Party\",\n" +
                   "  \"hqm.party.disband\": \"Disband Party\",\n" +
                   "  \"hqm.party.list\": \"Party List\",\n" +
                   "  \"hqm.party.invites\": \"Your invites\",\n" +
                   "  \"hqm.party.noInvites\": \"No party invites\",\n" +
                   "  \"hqm.party.name\": \"Party name\",\n" +
                   "  \"hqm.party.owner\": \"Owner\",\n" +
                   "  \"hqm.party.invite\": \"Invited\",\n" +
                   "  \"hqm.party.playerName\": \"Player name\",\n" +
                   "  \"hqm.party.shiftConfirm\": \"Hold shift while clicking to confirm.\",\n" +
                   "  \"hqm.party.shiftCtrlConfirm\": \"Hold shift+ctrl while clicking to confirm.\",\n" +
                   "  \"hqm.party.currentSelection\": \"You have currently selected: %s\",\n" +
                   "  \"hqm.party.stats\": \"Stats about all existing parties.\",\n" +
                   "  \"hqm.party.lifeSetting\": \"Life setting: %s\",\n" +
                   "  \"hqm.party.rewardSetting\": \"Reward setting: %s\",\n" +
                   "  \"hqm.party.change\": \"Click here to change\",\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"_comment\": \"EDIT TYPES\",\n" +
                   "  \"_comment\": \"======================================\",\n" +
                   "  \"hqm.editType.allSaved\": \"Everything is saved!\",\n" +
                   "  \"hqm.editType.neverSaved\": \"Never saved this session\",\n" +
                   "  \"hqm.editType.unsaved\": \"Unsaved changes: %s\",\n" +
                   "  \"hqm.editType.other\": \"Other changes: %s\",\n" +
                   "  \"hqm.editType.savedRecent\": \"Saved recently\",\n" +
                   "  \"hqm.editType.savedMinutes\": \"Saved %s [[minute||minutes]] ago\",\n" +
                   "  \"hqm.editType.savedHours\": \"Saved %s [[hour||hours]] ago\",\n" +
                   "  \"hqm.editType.added\": \"Created\",\n" +
                   "  \"hqm.editType.changed\": \"Changed\",\n" +
                   "  \"hqm.editType.moved\": \"Moved\",\n" +
                   "  \"hqm.editType.removed\": \"Removed\",\n" +
                   "  \"hqm.editType.quest\": \"quests\",\n" +
                   "  \"hqm.editType.task\": \"tasks\",\n" +
                   "  \"hqm.editType.taskType\": \"task type\",\n" +
                   "  \"hqm.editType.req\": \"requirements\",\n" +
                   "  \"hqm.editType.repeat\": \"repeatability\",\n" +
                   "  \"hqm.editType.vis\": \"triggers\",\n" +
                   "  \"hqm.editType.parent\": \"parent count\",\n" +
                   "  \"hqm.editType.option\": \"quest options\",\n" +
                   "  \"hqm.editType\": \"names\",\n" +
                   "  \"hqm.editType.desc\": \"descriptions\",\n" +
                   "  \"hqm.editType.icon\": \"quest icons\",\n" +
                   "  \"hqm.editType.questSize\": \"quest sizes\",\n" +
                   "  \"hqm.editType.set\": \"quest sets\",\n" +
                   "  \"hqm.editType.reward\": \"rewards\",\n" +
                   "  \"hqm.editType.monster\": \"mobs\",\n" +
                   "  \"hqm.editType.location\": \"locations\",\n" +
                   "  \"hqm.editType.tier\": \"tiers\",\n" +
                   "  \"hqm.editType.group\": \"groups\",\n" +
                   "  \"hqm.editType.groupItem\": \"group items\",\n" +
                   "  \"hqm.editType.death\": \"deaths\",\n" +
                   "  \"hqm.editType.taskItem\": \"task items\",\n" +
                   "  \"hqm.editType.rep\": \"reputations\",\n" +
                   "  \"hqm.editType.repMark\": \"rep tiers\",\n" +
                   "  \"hqm.editType.repTask\": \"targets\",\n" +
                   "  \"hqm.editType.repReward\": \"rep rewards\",\n" +
                   "  \"hqm.editType.kills\": \"kills\",\n" +
                   "  \"hqm.editType.repBar\": \"rep bar\",\n" +
                   "  \"hqm.editType.betweenSets\": \"between sets\",\n" +
                   "  \"hqm.editType.command\": \"commands\",\n" +
                   "  \"hqm.editType.advancement\": \"advancements\",\n" +
                   "  \"hqm.editType.questCompletion\": \"quest completions\"\n" +
                   "}";
        JsonObject object = new GsonBuilder().create().fromJson(s, JsonObject.class);
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            MAP.put(entry.getKey(), entry.getValue().getAsString());
        }
    }
    
    private static Pattern pluralPattern = Pattern.compile("\\[\\[(.*)\\|\\|(.*)\\]\\]");
    
    public static LiteralText translateComponent(String id) {
        return new LiteralText(translate(id));
    }
    
    @SuppressWarnings("Convert2MethodRef")
    private static final BiFunction<String, Object[], String> storageTranslator = Executor.call(() -> () -> (s, args) -> I18n.translate(s, args), () -> () -> (s, args) -> {
        String s1 = MAP.get(s);
        if (s1 == null) s1 = s;
        
        try {
            return String.format(s1, args);
        } catch (IllegalFormatException var5) {
            return "Format error: " + s1;
        }
    });
    
    private static String storageTranslate(String id, Object... args) {
        return storageTranslator.apply(id, args);
    }
    
    public static String commonTranslate(String id) {
        return storageTranslate(id).replace("\\n", "\n");
    }
    
    public static StringRenderable translated(String translationKey) {
        return Translator.plain(translate(translationKey));
    }
    
    public static StringRenderable translated(String translationKey, Object... args) {
        return Translator.plain(translate(translationKey, args));
    }
    
    @Environment(EnvType.CLIENT)
    public static StringRenderable translated(String translationKey, TextColor color) {
        return colored(translate(translationKey), color);
    }
    
    @Environment(EnvType.CLIENT)
    public static StringRenderable translated(String translationKey, TextColor color, Object... args) {
        return colored(translate(translationKey, args), color);
    }
    
    @Environment(EnvType.CLIENT)
    public static StringRenderable translated(String translationKey, Formatting formatting) {
        return translated(translationKey, TextColor.fromFormatting(formatting));
    }
    
    @Environment(EnvType.CLIENT)
    public static StringRenderable translated(String translationKey, Formatting formatting, Object... args) {
        return translated(translationKey, TextColor.fromFormatting(formatting), args);
    }
    
    @Environment(EnvType.CLIENT)
    public static StringRenderable translated(String translationKey, GuiColor color) {
        return translated(translationKey, TextColor.fromRgb(color.getHexColor() & 0xFFFFFF));
    }
    
    @Environment(EnvType.CLIENT)
    public static StringRenderable translated(String translationKey, GuiColor color, Object... args) {
        return translated(translationKey, TextColor.fromRgb(color.getHexColor() & 0xFFFFFF), args);
    }
    
    private static String translate(String id, Object... args) {
        return storageTranslate(id, args).replace("\\n", "\n");
    }
    
    public static StringRenderable pluralTranslated(boolean plural, String id, Object... args) {
        return format(translated(id, args), plural);
    }
    
    @Environment(EnvType.CLIENT)
    public static StringRenderable pluralTranslated(boolean plural, String id, GuiColor color, Object... args) {
        return format(translated(id, args), color, plural);
    }
    
    public static MutableText translatable(String id, Object... args) {
        return translatable(Formatting.WHITE, id, args);
    }
    
    public static MutableText translatable(Formatting formatting, String id, Object... args) {
        return new TranslatableText(id, args).formatted(formatting);
    }
    
    public static MutableText translatable(boolean plural, String id, Object... args) {
        return translatable(Formatting.RESET, plural, id, args);
    }
    
    public static MutableText translatable(Formatting formatting, boolean plural, String id, Object... args) {
        return new LiteralText(rawString(Translator.pluralTranslated(plural, id, args))).formatted(formatting);
    }
    
    public static StringRenderable format(StringRenderable text, boolean plural) {
        if (text == null) return StringRenderable.EMPTY;
        try {
            TextCollector collector = new TextCollector();
            text.visit(asString -> {
                Matcher matcher = pluralPattern.matcher(asString);
                while (matcher.find()) {
                    asString = matcher.replaceFirst(matcher.group(plural ? 2 : 1));
                    matcher = pluralPattern.matcher(asString);
                }
                collector.add(Translator.plain(asString));
                return Optional.empty();
            });
            return collector.getCombined();
        } catch (IllegalFormatException e) {
            return concat(Translator.plain("Format Exception: "), text);
        }
    }
    
    @Environment(EnvType.CLIENT)
    public static StringRenderable format(StringRenderable text, GuiColor color, boolean plural) {
        if (text == null) return StringRenderable.EMPTY;
        try {
            TextCollector collector = new TextCollector();
            text.visit(asString -> {
                Matcher matcher = pluralPattern.matcher(asString);
                while (matcher.find()) {
                    asString = matcher.replaceFirst(matcher.group(plural ? 2 : 1));
                    matcher = pluralPattern.matcher(asString);
                }
                collector.add(Translator.colored(asString, color));
                return Optional.empty();
            });
            return collector.getCombined();
        } catch (IllegalFormatException e) {
            return concat(Translator.plain("Format Exception: "), text);
        }
    }
    
    public static class TextCollector {
        private final List<StringRenderable> texts = Lists.newArrayList();
        
        public void add(StringRenderable stringRenderable) {
            this.texts.add(stringRenderable);
        }
        
        public StringRenderable getRawCombined() {
            if (this.texts.isEmpty()) {
                return null;
            } else {
                return this.texts.size() == 1 ? this.texts.get(0) : concat(this.texts);
            }
        }
        
        public StringRenderable getCombined() {
            StringRenderable stringRenderable = this.getRawCombined();
            return stringRenderable != null ? stringRenderable : StringRenderable.EMPTY;
        }
    }
    
    static StringRenderable concat(final StringRenderable... visitables) {
        return concat(Arrays.asList(visitables));
    }
    
    static StringRenderable concat(final List<StringRenderable> visitables) {
        return new StringRenderable() {
            @Override
            public <T> Optional<T> visit(StringRenderable.Visitor<T> visitor) {
                Iterator var2 = visitables.iterator();
                
                Optional optional;
                do {
                    if (!var2.hasNext()) {
                        return Optional.empty();
                    }
                    
                    StringRenderable stringRenderable = (StringRenderable) var2.next();
                    optional = stringRenderable.visit(visitor);
                } while (!optional.isPresent());
                
                return optional;
            }
            
            @Environment(EnvType.CLIENT)
            @Override
            public <T> Optional<T> visit(StyledVisitor<T> styledVisitor, Style style) {
                return Optional.empty();
            }
        };
    }
    
    @Environment(EnvType.CLIENT)
    public static StringRenderable colored(String s, TextColor color) {
        return StringRenderable.styled(s, Style.EMPTY.withColor(color));
    }
    
    @Environment(EnvType.CLIENT)
    public static StringRenderable colored(String s, GuiColor color) {
        return colored(s, TextColor.fromRgb(color.getHexColor() & 0xFFFFFF));
    }
    
    public static String rawString(StringRenderable text) {
        StringBuilder builder = new StringBuilder();
        text.visit((asString) -> {
            builder.append(asString);
            return Optional.empty();
        });
        return builder.toString();
    }
    
    public static StringRenderable plain(String s) {
        if (s == null) return StringRenderable.EMPTY;
        return StringRenderable.plain(s);
    }
}
