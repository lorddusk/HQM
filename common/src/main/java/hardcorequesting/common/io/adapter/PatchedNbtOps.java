package hardcorequesting.common.io.adapter;

import net.minecraft.nbt.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A version of NbtOps that can handle list creation with tags of different numerical types.
 * Fixes a problem with conversion from json to nbt where the json ops guesses the type of a number based on its size,
 * which can mean that numbers in the same json list can get different types when converted to nbt tags.
 * When it is relevant to create an array tag, instead of expecting all tags to be of the exact same type,
 * this op will create an array tag of the smallest possible type.
 * TODO this is still not perfect since this guesswork can still get the list/array type wrong in certain cases,
 *  which may cause the tag to not be found by the code that would normally use it.
 *  A more complete solution would need to add extra information when converting nbt to json,
 *  or simply not try to store nbt through json at all.
 */
public class PatchedNbtOps extends NbtOps {
    public static final PatchedNbtOps INSTANCE = new PatchedNbtOps();
    
    @Override
    public Tag createList(Stream<Tag> stream) {
        List<Tag> elements = stream.collect(Collectors.toList());
        
        if (elements.isEmpty())
            return new ListTag();
        else if (elements.stream().allMatch(this::canBeInByteArray)) {
            return new ByteArrayTag(elements.stream().map(tag -> ((NumericTag) tag).getAsByte()).collect(Collectors.toList()));
        } else if (elements.stream().allMatch(this::canBeInIntArray)) {
            return new IntArrayTag(elements.stream().map(tag -> ((NumericTag) tag).getAsInt()).collect(Collectors.toList()));
        } else if (elements.stream().allMatch(this::canBeInLongArray)) {
            return new LongArrayTag(elements.stream().map(tag -> ((NumericTag) tag).getAsLong()).collect(Collectors.toList()));
        } else {
            ListTag listTag = new ListTag();
            elements.forEach(tag -> {
                if (!(tag instanceof EndTag))
                    listTag.add(tag);
            });
            return listTag;
        }
    }
    
    public boolean canBeInLongArray(Tag tag) {
        return tag instanceof LongTag || canBeInIntArray(tag);
    }
    
    public boolean canBeInIntArray(Tag tag) {
        return tag instanceof IntTag || tag instanceof ShortTag || canBeInByteArray(tag);
    }
    
    public boolean canBeInByteArray(Tag tag) {
        return tag instanceof ByteTag;
    }
}