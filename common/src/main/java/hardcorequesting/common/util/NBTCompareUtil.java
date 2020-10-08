/*
    This file is originally written by Darkhax with contributions from from artdude543.
    It is licensed under the terms of the LGPL, and is thus compatible with HQM's LGPL license.
    Original file: https://github.com/Darkhax-Minecraft/ItemStages/blob/master/src/main/java/net/darkhax/itemstages/StageCompare.java
 */

package hardcorequesting.common.util;

public class NBTCompareUtil {}
/*
    public static boolean isTagEmpty(ItemStack stack) {
        return (stack != null && stack.getTagCompound() != null) && stack.getTagCompound().getKeySet().isEmpty();
    }
    
    /**
     * Checks if two NBT types are partially similar. For two tags to be similar, tag one must
     * have every value that tag two has. Tag two is not required to have any of tag one's
     * values.
     *
     * @param one The first tag to check.
     * @param two The second tag to check, this is the tag containing required NBT data.
     * @return Whether or not the tags are partially similar.
     *//*
    public static boolean arePartiallySimilar(NBTBase one, NBTBase two) {
        
        // First tag can not be null.
        if (one == null) {
            
            return false;
        }
        
        // If the comparison is null or empty, default to true.
        if (two == null || two.isEmpty()) {
            
            return true;
        }
        
        // If tag is a compound, check each key on the second tag.
        else if (one instanceof CompoundTag && two instanceof CompoundTag) {
            
            final CompoundTag tagOne = (CompoundTag) one;
            final CompoundTag tagTwo = (CompoundTag) two;
            
            for (String key : tagTwo.getKeySet()) {
                
                // Recursively check all the tags on two for partial similarity.
                if (!arePartiallySimilar(tagOne.getTag(key), tagTwo.getTag(key))) {
                    
                    // Fail if any tag on two is not partially similar to the counterpart on
                    // one.
                    return false;
                }
            }
            
            // If all tags on two are partially similar with one, return true.
            return true;
        }
        
        // If tag is a list, check if one has all entries of two.
        else if (one instanceof ListTag && two instanceof ListTag) {
            
            final ListTag listOne = (ListTag) one;
            final ListTag listTwo = (ListTag) two;
            
            // Iterate the entries of list two
            for (int i = 0; i < listTwo.tagCount(); i++) {
                
                boolean similar = false;
                
                final NBTBase tagTwo = listTwo.get(i);
                
                // Iterate the entries of list one to check if any match.
                for (int j = 0; j < listOne.tagCount(); j++) {
                    
                    // If a similar tag is found, set to true and break.
                    if (arePartiallySimilar(listOne.get(j), tagTwo)) {
                        
                        similar = true;
                        break;
                    }
                }
                
                // Fail if no similar matches were found.
                if (!similar) {
                    
                    return false;
                }
            }
            
            return true;
        }
        
        // If not a special case, check if values are equal.
        return two.equals(one);
    }
}
*/