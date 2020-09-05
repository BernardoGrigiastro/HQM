package hardcorequesting.util;

import hardcorequesting.HardcoreQuesting;
import net.minecraft.server.MinecraftServer;

/**
 * @author canitzp
 * @since 5.4.0
 */
public class HQMUtil {
    
    /**
     * A true and save way to determine if a player is actually in a single player only game. No Server nor Integrated Server (LAN World)
     *
     * @return true if the player plays in a real single player world
     */
    public static boolean isGameSingleplayer() {
        MinecraftServer server = HardcoreQuesting.getServer();
        return server != null && server.isSingleplayer();
    }
}
    
    /*
    @Nullable
    public static <T> T tryToCreateClassOfType(@NotNull String className, @NotNull Class<T> hasToBeAssignableFrom) {
        try {
            Class c = Class.forName(className);
            if (hasToBeAssignableFrom.isAssignableFrom(c)) {
                return (T) c.newInstance();
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static List<Integer> getIntListFromNBT(CompoundTag nbt, String key) {
        List<Integer> list = new ArrayList<>();
        for (Tag base : nbt.getList(key, 3)) {
            if (base instanceof IntTag) {
                list.add(((IntTag) base).getInt());
            }
        }
        return list;
    }
    
    public static void setIntListToNBT(CompoundTag nbt, String key, List<Integer> ints) {
        ListTag nbtTagList = new ListTag();
        for (int i : ints) {
            nbtTagList.add(new IntTag(i));
        }
        nbt.put(key, nbtTagList);
    }
    
    @Nullable
    public static <T extends ISimpleParsable> T getInstanceFromNBT(CompoundTag nbt, String key, Class<T> classToGet) {
        CompoundTag rawData = nbt.getCompoundTag(key);
        if (!rawData.isEmpty() && rawData.hasKey("Class", Constants.NBT.TAG_STRING) && rawData.hasKey("Data", Constants.NBT.TAG_COMPOUND)) {
            try {
                Class c = Class.forName(rawData.getString("Class"));
                if (classToGet.isAssignableFrom(c)) {
                    Object o = c.newInstance();
                    if (o instanceof ISimpleParsable) {
                        ((ISimpleParsable) o).onLoad(rawData.getCompoundTag("Data"));
                        return (T) o;
                    }
                }
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    
    public static void setInstanceToNBT(@NotNull CompoundTag nbt, @NotNull String key, @NotNull ISimpleParsable object) {
        CompoundTag rawData = new CompoundTag();
        rawData.putString("Class", object.getClassName());
        rawData.put("Data", object.getData());
        nbt.put(key, rawData);
    }
    
}

     */
