package hqm.quest;

import net.minecraft.util.ResourceLocation;

import java.util.*;

/**
 * @author canitzp
 */
public class Questbook {

    private final String name;
    private final UUID id;
    private final ResourceLocation image;
    private final List<String> description;
    private final List<QuestLine> questLines = new ArrayList<>();
    private final List<Integer> dimensions;

    public Questbook(String name, UUID id, List<String> description, ResourceLocation image, List<Integer> dims) {
        this.name = name;
        this.id = id;
        this.description = description;
        this.image = image;
        this.dimensions = dims;
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
        return description;
    }

    public List<QuestLine> getQuestLines() {
        return questLines;
    }

    public List<Integer> getDimensions() {
        return dimensions;
    }

    public Questbook addQuestLine(QuestLine questLine){
        this.questLines.add(questLine);
        return this;
    }

}
