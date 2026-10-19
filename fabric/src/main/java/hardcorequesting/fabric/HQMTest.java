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

import java.util.List;
import java.util.Optional;

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
}
