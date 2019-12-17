package hqm.quest;

import com.google.common.collect.Lists;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author canitzp
 */
public class Questbook {

    private final String name;
    private final UUID id;
    private final ResourceLocation image;
    private final List<String> description, tooltip;
    private final List<QuestLine> questLines = new ArrayList<>();
    private final List<Integer> dimensions;
    private final List<Team> teams = new ArrayList<>();
    private CompoundNBT data;

    public Questbook(String name, UUID id, List<String> description, List<String> tooltip, ResourceLocation image, List<Integer> dims) {
        this.name = name;
        this.id = id;
        this.description = description;
        this.tooltip = tooltip;
        this.image = image;
        this.dimensions = dims;
    }

    public Questbook setData(CompoundNBT data) {
        this.data = data;
        return this;
    }

    public String getName() {
        return name;
    }

    public UUID getId() {
        return id;
    }

    public ResourceLocation getImage() {
        return image;
    }

    public List<String> getDescription() {
        return this.description.isEmpty() ? Collections.singletonList("No Description") : this.description;
    }

    // todo use ITextComponent instead of String
    public List<String> getTooltip() {
        return tooltip;
    }

    public List<QuestLine> getQuestLines() {
        return questLines;
    }

    public List<Integer> getDimensions() {
        return dimensions;
    }

    public List<Team> getTeams() {
        return teams;
    }

    public Questbook addQuestLine(QuestLine questLine){
        this.questLines.add(questLine);
        return this;
    }

    public Questbook addTeam(Team team){
        this.teams.add(team);
        return this;
    }

    public List<String> getQuestLineNames(){
        return questLines.stream().map(QuestLine::getName).collect(Collectors.toList());
    }

    public int countQuestLines(){
        return this.questLines.size();
    }

    public int countQuests(){
        return this.questLines.stream().mapToInt(questLine -> questLine.getQuests().size()).sum();
    }

    public int countUnlockedQuests(Team team){
        return this.questLines.stream().mapToInt(questLines -> questLines.getUnlocked(team).size()).sum();
    }

    public int countCompletedQuests(Team team){
        return this.questLines.stream().mapToInt(questLines -> questLines.getCompleted(team).size()).sum();
    }

    public int countUnlockedUncompleteQuests(Team team){
        return this.questLines.stream().mapToInt(questLines -> questLines.getUnlockedUncompleted(team).size()).sum();
    }

    public Team getTeam(PlayerEntity player){
        for(Team team : this.teams){
            if(team.containsPlayer(player)){
                return team;
            }
        }
        Team team = new Team(player.getDisplayName().getString(), 0x000000, 3, Lists.newArrayList(player.getUniqueID())); // TODO random color
        this.teams.add(team);
        return team;
    }

}
