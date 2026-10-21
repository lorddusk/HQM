package hardcorequesting.fabric;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Either;
import dev.architectury.fluid.FluidStack;
import hardcorequesting.common.HardcoreQuestingCore;
import hardcorequesting.common.client.sounds.Sounds;
import hardcorequesting.common.io.adapter.MinecraftAdapter;
import hardcorequesting.common.io.adapter.QuestTaskAdapter;
import hardcorequesting.common.items.ModItems;
import hardcorequesting.common.items.crafting.BookCatalystRecipe;
import hardcorequesting.common.items.crafting.ModRecipes;
import hardcorequesting.common.quests.QuestLine;
import hardcorequesting.common.quests.task.TaskType;
import hardcorequesting.common.quests.task.icon.KillMobsTask;
import hardcorequesting.common.quests.task.icon.TameMobsTask;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import hardcorequesting.common.bag.GroupTier;
import hardcorequesting.common.death.DeathType;
import hardcorequesting.common.event.EventTrigger;
import hardcorequesting.common.quests.QuestingDataManager;
import hardcorequesting.common.quests.task.QuestTask;
import hardcorequesting.common.team.Team;
import hardcorequesting.common.util.WrappedText;
import hardcorequesting.common.bag.LootGroup;
import hardcorequesting.common.bag.TierColor;
import hardcorequesting.common.items.BagItem;
import hardcorequesting.common.quests.Quest;
import hardcorequesting.common.tileentity.AbstractBarrelBlockEntity;
import hardcorequesting.common.tileentity.TrackerBlockEntity;
import hardcorequesting.common.tileentity.TrackerType;
import hardcorequesting.fabric.capabilities.ModCapabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.entity.BannerPatterns;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.storage.loot.LootTable;
import hardcorequesting.common.blocks.ModBlocks;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.material.Fluids;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class HQMTest {

    private static ResourceLocation hqm(String path) {
        return ResourceLocation.fromNamespaceAndPath(HardcoreQuestingCore.ID, path);
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void questLineExists(GameTestHelper helper) {

        if (QuestLine.getActiveQuestLine() == null)
            helper.fail("Quest line has not been initialized.");
        else helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void itemsAreRegistered(GameTestHelper helper) {
        for (String id : List.of("quest_book", "enabled_quest_book", "basic_bag", "quarterheart", "halfheart",
                "threequartsheart", "heart", "rottenheart", "item_barrel")) {
            if (!BuiltInRegistries.ITEM.containsKey(hqm(id))) {
                helper.fail("Item hardcorequesting:" + id + " is not registered.");
                return;
            }
        }
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void soundsAreRegistered(GameTestHelper helper) {
        for (Sounds sound : Sounds.values()) {
            if (!BuiltInRegistries.SOUND_EVENT.containsKey(sound.getSoundId())) {
                helper.fail("Sound " + sound.getSoundId() + " is not registered.");
                return;
            }
            if (!sound.getSoundId().equals(sound.getSound().getLocation())) {
                helper.fail("Sound " + sound + " resolves to the wrong sound event.");
                return;
            }
        }
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void taskTypeRegistry(GameTestHelper helper) {
        if (!TaskType.REGISTRY_KEY.location().equals(hqm("task_type"))) {
            helper.fail("Unexpected task type registry key: " + TaskType.REGISTRY_KEY.location());
            return;
        }
        if (TaskType.values().size() < 15) {
            helper.fail("Expected at least 15 task types, found " + TaskType.values().size());
            return;
        }
        if (!hqm("checkbox").toString().equals(TaskType.CHECKBOX.get().toDataName()) || !hqm("kill").toString().equals(TaskType.KILL.get().toDataName())) {
            helper.fail("Task types are registered under the wrong ids.");
            return;
        }
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void recipesAreLoaded(GameTestHelper helper) {
        for (String id : List.of("barrel", "barrel_2", "book", "full_from_half_half_hearts",
                "full_from_quarter_quarter_half_hearts", "full_from_quarter_quarter_quarter_quarter_hearts",
                "full_from_quarter_threequarts_hearts", "half_from_quarter_quarter_hearts",
                "threequarts_from_quarter_half_hearts", "threequarts_from_quarter_quarter_quarter_hearts")) {
            if (helper.getLevel().getRecipeManager().byKey(hqm(id)).isEmpty()) {
                helper.fail("Recipe hardcorequesting:" + id + " was not loaded.");
                return;
            }
        }
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void bookCatalystRecipeKeepsBook(GameTestHelper helper) {
        Optional<RecipeHolder<?>> holder = helper.getLevel().getRecipeManager().byKey(hqm("barrel"));
        if (holder.isEmpty() || !(holder.get().value() instanceof BookCatalystRecipe recipe)) {
            helper.fail("Barrel recipe is missing or is not a book catalyst recipe.");
            return;
        }
        if (recipe.getSerializer() != ModRecipes.bookCatalystSerializer.get()) {
            helper.fail("Barrel recipe reports the wrong serializer.");
            return;
        }

        ItemStack planks = new ItemStack(Items.OAK_PLANKS), glass = new ItemStack(Items.GLASS);
        ItemStack book = new ItemStack(ModItems.book.get());
        CraftingInput input = CraftingInput.of(3, 3, List.of(
                planks, glass, planks,
                glass, book, glass,
                planks, glass, planks));

        if (!recipe.matches(input, helper.getLevel())) {
            helper.fail("Barrel recipe does not match its own pattern.");
            return;
        }
        ItemStack result = recipe.assemble(input, helper.getLevel().registryAccess());
        if (!result.is(BuiltInRegistries.ITEM.get(hqm("item_barrel")))) {
            helper.fail("Barrel recipe produced " + result + " instead of an item barrel.");
            return;
        }

        NonNullList<ItemStack> remaining = recipe.getRemainingItems(input);
        if (remaining.size() != 9 || !remaining.get(4).is(ModItems.book.get())) {
            helper.fail("The quest book was not left behind in the crafting grid: " + remaining);
            return;
        }
        if (!remaining.get(0).isEmpty() || !remaining.get(1).isEmpty()) {
            helper.fail("Only the quest book should remain after crafting: " + remaining);
            return;
        }
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void itemStackAdapterRoundTrip(GameTestHelper helper) {
        ItemStack stack = new ItemStack(Items.DIAMOND, 5);
        stack.set(DataComponents.CUSTOM_NAME, net.minecraft.network.chat.Component.literal("Test"));

        JsonElement json = MinecraftAdapter.ITEM_STACK.serialize(stack);
        ItemStack read = MinecraftAdapter.ITEM_STACK.deserialize(json);
        if (!ItemStack.matches(stack, read)) {
            helper.fail("Item stack changed in a json round trip: " + stack + " -> " + read);
            return;
        }
        ItemStack icon = MinecraftAdapter.ICON_ITEM_STACK.deserialize(MinecraftAdapter.ICON_ITEM_STACK.serialize(stack));
        if (icon.getCount() != 1 || !icon.is(Items.DIAMOND)) {
            helper.fail("Icon item stack was not capped at one item: " + icon);
            return;
        }
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void fluidAdapterRoundTrip(GameTestHelper helper) {
        FluidStack fluid = FluidStack.create(Fluids.WATER, 1000);
        FluidStack read = MinecraftAdapter.FLUID.deserialize(MinecraftAdapter.FLUID.serialize(fluid));
        if (read.getFluid() != Fluids.WATER) {
            helper.fail("Fluid changed in a json round trip: " + read.getFluid());
            return;
        }
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void killMobAdapterRoundTrip(GameTestHelper helper) {
        KillMobsTask.Part part = new KillMobsTask.Part();
        part.setMob(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.ZOMBIE));
        part.setCount(7);
        part.setIconStack(Either.left(new ItemStack(Items.ZOMBIE_SPAWN_EGG)));

        KillMobsTask.Part read = QuestTaskAdapter.MOB_ADAPTER.deserialize(QuestTaskAdapter.MOB_ADAPTER.serialize(part));
        if (read == null || !read.getMob().equals(ResourceLocation.withDefaultNamespace("zombie")) || read.getCount() != 7) {
            helper.fail("Kill mob part changed in a json round trip.");
            return;
        }
        helper.succeed();
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void resourceLocationParsing(GameTestHelper helper) {
        if (!TameMobsTask.ABSTRACT_HORSE.equals(ResourceLocation.fromNamespaceAndPath("minecraft", "abstracthorse"))) {
            helper.fail("Unprefixed resource locations should default to the minecraft namespace.");
            return;
        }
        helper.succeed();
    }
    
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void barrelDropsAndTags(GameTestHelper helper) {
        var barrel = ModBlocks.blockBarrel.get();
        if (!barrel.defaultBlockState().is(BlockTags.MINEABLE_WITH_AXE)) {
            helper.fail("The item barrel is not tagged as mineable with an axe.");
            return;
        }
        ResourceKey<LootTable> key = ResourceKey.create(Registries.LOOT_TABLE, hqm("blocks/item_barrel"));
        LootTable table = helper.getLevel().getServer().reloadableRegistries().getLootTable(key);
        if (table == LootTable.EMPTY) {
            helper.fail("The item barrel loot table was not loaded.");
            return;
        }
        helper.succeed();
    }
    
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void registryBackedComponentsRoundTrip(GameTestHelper helper) {
        var registries = helper.getLevel().registryAccess();
        
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.enchant(registries.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SHARPNESS), 5);
        ItemStack banner = new ItemStack(Items.WHITE_BANNER);
        banner.set(DataComponents.BANNER_PATTERNS, new BannerPatternLayers.Builder()
                .addIfRegistered(registries.lookupOrThrow(Registries.BANNER_PATTERN), BannerPatterns.CREEPER, DyeColor.LIME)
                .build());
        
        for (ItemStack stack : List.of(sword, banner)) {
            JsonElement json = MinecraftAdapter.ITEM_STACK.serialize(stack);
            if (!json.getAsJsonObject().has("components")) {
                helper.fail("Registry backed components were not serialized: " + json);
                return;
            }
            ItemStack read = MinecraftAdapter.ITEM_STACK.deserialize(json);
            if (!ItemStack.matches(stack, read)) {
                helper.fail("Item stack changed in a json round trip: " + json + " -> " + read.getComponentsPatch());
                return;
            }
        }
        
        // The json format is the plain component format: registry entries are referred to by their id
        JsonElement enchantments = MinecraftAdapter.ITEM_STACK.serialize(sword).getAsJsonObject()
                .getAsJsonObject("components").get("minecraft:enchantments");
        if (!enchantments.toString().contains("\"minecraft:sharpness\":5")) {
            helper.fail("Unexpected enchantment json: " + enchantments);
            return;
        }
        helper.succeed();
    }
    
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void trackerSurvivesSaveLoadWithoutQuest(GameTestHelper helper) {
        BlockPos pos = new BlockPos(0, 1, 0);
        helper.setBlock(pos, ModBlocks.blockTracker.get());
        TrackerBlockEntity tracker = (TrackerBlockEntity) helper.getBlockEntity(pos);
        tracker.setRadius(7);
        tracker.setTrackerType(TrackerType.PLAYER);
        
        var registries = helper.getLevel().registryAccess();
        CompoundTag saved = tracker.saveWithFullMetadata(registries);
        tracker.setRadius(0);
        tracker.setTrackerType(TrackerType.TEAM);
        try {
            tracker.loadWithComponents(saved, registries);
        } catch (RuntimeException e) {
            helper.fail("Loading a tracker without a quest failed: " + e);
            return;
        }
        if (tracker.getRadius() != 7 || tracker.getTrackerType() != TrackerType.PLAYER) {
            helper.fail("Tracker settings were not restored: radius " + tracker.getRadius() + ", type " + tracker.getTrackerType());
            return;
        }
        helper.succeed();
    }
    
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void trackerWithUnknownTypeKeepsItsType(GameTestHelper helper) {
        BlockPos pos = new BlockPos(0, 1, 0);
        helper.setBlock(pos, ModBlocks.blockTracker.get());
        TrackerBlockEntity tracker = (TrackerBlockEntity) helper.getBlockEntity(pos);
        tracker.setTrackerType(TrackerType.PLAYER);
        var registries = helper.getLevel().registryAccess();
        
        for (byte unknown : new byte[]{(byte) TrackerType.values().length, 99, -1, Byte.MIN_VALUE}) {
            CompoundTag loaded = new CompoundTag();
            loaded.putInt("Radius", 4);
            loaded.putByte("TrackerType", unknown);
            try {
                tracker.loadWithComponents(loaded, registries);
            } catch (RuntimeException e) {
                helper.fail("Loading a tracker with the type " + unknown + " failed: " + e);
                return;
            }
            if (tracker.getTrackerType() != TrackerType.PLAYER || tracker.getRadius() != 4) {
                helper.fail("A tracker with the type " + unknown + " should keep its type and load the rest, but has " + tracker.getTrackerType() + " and radius " + tracker.getRadius());
                return;
            }
        }
        helper.succeed();
    }
    
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void trackerKeepsUnresolvedQuestWhenSavedAgain(GameTestHelper helper) {
        BlockPos pos = new BlockPos(0, 1, 0);
        helper.setBlock(pos, ModBlocks.blockTracker.get());
        TrackerBlockEntity tracker = (TrackerBlockEntity) helper.getBlockEntity(pos);
        
        // A freshly loaded tracker only knows its quest id, the quest itself is resolved on the first tick
        UUID quest = UUID.randomUUID();
        CompoundTag loaded = new CompoundTag();
        loaded.putUUID("Quest", quest);
        var registries = helper.getLevel().registryAccess();
        tracker.loadWithComponents(loaded, registries);
        
        CompoundTag saved = tracker.saveWithFullMetadata(registries);
        if (!saved.hasUUID("Quest") || !quest.equals(saved.getUUID("Quest")))
            helper.fail("The quest id of a tracker was lost when it was saved before its first tick.");
        else helper.succeed();
    }
    
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void trackerConfigIsMarkedForSaving(GameTestHelper helper) {
        BlockPos pos = new BlockPos(0, 1, 0);
        helper.setBlock(pos, ModBlocks.blockTracker.get());
        BlockPos absolute = helper.absolutePos(pos);
        TrackerBlockEntity tracker = (TrackerBlockEntity) helper.getBlockEntity(pos);
        LevelChunk chunk = helper.getLevel().getChunkAt(absolute);
        chunk.setUnsaved(false);
        
        boolean wasEditing = Quest.isEditing;
        Quest.setEditMode(true);
        try {
            TrackerBlockEntity.saveToServer(helper.makeMockServerPlayerInLevel(), absolute, 5, TrackerType.PLAYER);
        } finally {
            Quest.setEditMode(wasEditing);
        }
        
        if (tracker.getRadius() != 5 || tracker.getTrackerType() != TrackerType.PLAYER) {
            helper.fail("The tracker settings were not applied.");
        } else if (!chunk.isUnsaved()) {
            helper.fail("The tracker was changed, but its chunk was not marked for saving.");
        } else {
            helper.succeed();
        }
    }
    
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void barrelBindingSurvivesSaveLoad(GameTestHelper helper) {
        BlockPos pos = new BlockPos(0, 1, 0);
        helper.setBlock(pos, ModBlocks.blockBarrel.get());
        AbstractBarrelBlockEntity barrel = (AbstractBarrelBlockEntity) helper.getBlockEntity(pos);
        UUID player = UUID.randomUUID(), quest = UUID.randomUUID();
        barrel.setPlayerUUID(player);
        barrel.setQuestUUID(quest);
        barrel.selectedTask = 3;
        
        var registries = helper.getLevel().registryAccess();
        CompoundTag saved = barrel.saveWithFullMetadata(registries);
        barrel.setPlayerUUID(null);
        barrel.setQuestUUID(null);
        barrel.selectedTask = 0;
        barrel.loadWithComponents(saved, registries);
        
        if (!player.equals(barrel.getPlayerUUID()) || !quest.equals(barrel.getQuestUUID()) || barrel.selectedTask != 3) {
            helper.fail("The barrel binding was lost: player " + barrel.getPlayerUUID() + ", quest " + barrel.getQuestUUID() + ", task " + barrel.selectedTask);
            return;
        }
        helper.succeed();
    }
    
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void bagIsKeptWhenNothingCanBeOpened(GameTestHelper helper) {
        Map<UUID, LootGroup> groups = new HashMap<>(LootGroup.getGroups());
        LootGroup.getGroups().clear();
        try {
            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            BagItem bag = ModItems.basicBag.get();
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(bag, 3));
            bag.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
            
            int left = player.getItemInHand(InteractionHand.MAIN_HAND).getCount();
            if (left != 3)
                helper.fail("A bag without any loot group to open was consumed, " + left + " of 3 left.");
            else helper.succeed();
        } finally {
            LootGroup.getGroups().clear();
            LootGroup.getGroups().putAll(groups);
        }
    }
    
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void bagIsConsumedWhenOpened(GameTestHelper helper) {
        Map<UUID, LootGroup> groups = new HashMap<>(LootGroup.getGroups());
        LootGroup.getGroups().clear();
        try {
            LootGroup group = new LootGroup(UUID.randomUUID());
            group.setTier(new GroupTier("Test", TierColor.GRAY, 1, 1, 1, 1, 1));
            group.getItems().add(new ItemStack(Items.DIAMOND));
            LootGroup.add(group);
            
            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            BagItem bag = ModItems.basicBag.get();
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(bag, 3));
            bag.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
            
            int left = player.getItemInHand(InteractionHand.MAIN_HAND).getCount();
            if (left != 2)
                helper.fail("Opening a bag should consume exactly one, " + left + " of 3 left.");
            else if (!player.getInventory().contains(new ItemStack(Items.DIAMOND)))
                helper.fail("The loot of the opened bag was not given to the player.");
            else helper.succeed();
        } finally {
            LootGroup.getGroups().clear();
            LootGroup.getGroups().putAll(groups);
        }
    }
    
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void playerExtraDataIsPersisted(GameTestHelper helper) {
        var registries = helper.getLevel().registryAccess();
        ModCapabilities.CompoundTagComponent component = new ModCapabilities.CompoundTagComponent();
        component.tag.putBoolean("received_book", true);
        
        CompoundTag saved = new CompoundTag();
        component.writeToNbt(saved, registries);
        
        ModCapabilities.CompoundTagComponent restored = new ModCapabilities.CompoundTagComponent();
        restored.readFromNbt(saved, registries);
        if (!restored.tag.getBoolean("received_book"))
            helper.fail("The player extra data was not written to the player's data.");
        else helper.succeed();
    }
    
    private static Quest createQuest(List<Quest> created, String name) {
        Quest quest = new Quest(WrappedText.create(name), WrappedText.create("Test quest"), 0, 0, false);
        created.add(quest);
        return quest;
    }
    
    private static void removeQuests(List<Quest> created) {
        // Goes through removeQuest, so the tasks of the quests are unregistered from the events too
        created.forEach(Quest::removeQuest);
    }
    
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void clearingOptionLinksKeepsRequirementLinks(GameTestHelper helper) {
        List<Quest> created = new ArrayList<>();
        try {
            Quest a = createQuest(created, "a"), b = createQuest(created, "b"), c = createQuest(created, "c"), d = createQuest(created, "d");
            c.addRequirement(a.getQuestId());   // a is a requirement of c
            a.addOptionLink(b.getQuestId());    // a links to b
            d.addOptionLink(a.getQuestId());    // d links to a
            
            a.clearOptionLinks();
            
            if (!a.getReversedRequirement().contains(c))
                helper.fail("Clearing the option links removed a quest that requires this quest.");
            else if (!a.getOptionLinks().isEmpty() || !b.getReversedOptionLinks().isEmpty())
                helper.fail("The option links of the quest were not cleared.");
            else if (!d.getOptionLinks().isEmpty() || !a.getReversedOptionLinks().isEmpty())
                helper.fail("The option links pointing to the quest were not cleared.");
            else helper.succeed();
        } finally {
            removeQuests(created);
        }
    }
    
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void removingQuestIgnoresDanglingLinks(GameTestHelper helper) {
        List<Quest> created = new ArrayList<>();
        try {
            Quest a = createQuest(created, "a"), b = createQuest(created, "b"), c = createQuest(created, "c");
            a.addRequirement(b.getQuestId());
            a.addOptionLink(c.getQuestId());
            // b and c disappear without a being updated
            Quest.getQuests().remove(b.getQuestId());
            Quest.getQuests().remove(c.getQuestId());
            
            try {
                Quest.removeQuest(a);
            } catch (RuntimeException e) {
                helper.fail("Removing a quest with links to missing quests failed: " + e);
                return;
            }
            helper.succeed();
        } finally {
            removeQuests(created);
        }
    }
    
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void progressIsAlwaysANumber(GameTestHelper helper) {
        List<Quest> created = new ArrayList<>();
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        try {
            Team team = QuestingDataManager.getInstance().getQuestingData(player).getTeam();
            team.clearProgress();
            if (!Float.isFinite(team.getProgress())) {
                helper.fail("The progress of a team without quests is not a number.");
                return;
            }
            
            Quest quest = createQuest(created, "p");
            if (!Float.isFinite(quest.getProgress(team))) {
                helper.fail("The progress of a quest without tasks is not a number.");
                return;
            }
            
            // A task of every type, as it is when freshly created (no parts, nothing required)
            for (TaskType<?> type : TaskType.values()) {
                float ratio = type.addTask(quest).getCompletedRatio(team);
                if (!Float.isFinite(ratio)) {
                    helper.fail("The completed ratio of a new " + type.toDataName() + " task is " + ratio);
                    return;
                }
            }
            if (!Float.isFinite(quest.getProgress(team)))
                helper.fail("The progress of a quest with a task of every type is not a number.");
            else helper.succeed();
        } finally {
            removeQuests(created);
            QuestingDataManager.getInstance().getQuestingData(player).getTeam().clearProgress();
        }
    }
    
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void equalLootGroupsHaveEqualHashCodes(GameTestHelper helper) {
        LootGroup a = new LootGroup(UUID.randomUUID()), b = new LootGroup(UUID.randomUUID());
        a.getItems().add(new ItemStack(Items.DIAMOND));
        b.getItems().add(new ItemStack(Items.DIAMOND));
        
        if (!a.equals(b))
            helper.fail("Expected the two loot groups to be equal.");
        else if (a.hashCode() != b.hashCode())
            helper.fail("Equal loot groups have different hash codes.");
        else helper.succeed();
    }
    
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void deathTypeIndexIsClamped(GameTestHelper helper) {
        DeathType[] types = DeathType.values();
        try {
            if (DeathType.getClamped(Integer.MAX_VALUE) != types[types.length - 1] || DeathType.getClamped(-3) != types[0]) {
                helper.fail("Indices outside of the death types were not clamped to the first and last type.");
                return;
            }
        } catch (RuntimeException e) {
            helper.fail("Clamping a death type index failed: " + e);
            return;
        }
        helper.succeed();
    }
    
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void tasksOfRemovedQuestsAreNotNotified(GameTestHelper helper) {
        List<Quest> created = new ArrayList<>();
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        try {
            Quest quest = createQuest(created, "removed");
            for (TaskType<?> type : TaskType.values())
                type.addTask(quest);
            // The quest disappears from the book without going through removeQuest, as when a whole set is dropped while loading
            Quest.getQuests().remove(quest.getQuestId());
            
            for (QuestTask<?> task : quest.getTasks()) {
                if (task.isValid()) {
                    helper.fail("The " + task.getType().toDataName() + " task of a removed quest is still valid.");
                    return;
                }
            }
            try {
                EventTrigger.instance().onPlayerTick(player);
            } catch (RuntimeException e) {
                helper.fail("A task of a removed quest was still notified of an event: " + e);
                return;
            }
            helper.succeed();
        } finally {
            created.forEach(quest -> quest.getTasks().forEach(QuestTask::onDelete));
            QuestingDataManager.getInstance().getQuestingData(player).getTeam().clearProgress();
        }
    }
    
    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void teamDataOfUnknownQuestIsEmpty(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        Team team = QuestingDataManager.getInstance().getQuestingData(player).getTeam();
        UUID unknown = UUID.randomUUID();
        try {
            if (team.getQuestData(unknown) == null || team.getQuestData(unknown).completed)
                helper.fail("Expected empty quest data for a quest that does not exist.");
            else helper.succeed();
        } catch (RuntimeException e) {
            helper.fail("Getting the quest data of an unknown quest failed: " + e);
        }
    }
}
