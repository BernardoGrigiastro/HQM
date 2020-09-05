package hardcorequesting.quests.task;

import com.mojang.blaze3d.vertex.PoseStack;
import hardcorequesting.client.EditMode;
import hardcorequesting.client.interfaces.GuiColor;
import hardcorequesting.client.interfaces.GuiQuestBook;
import hardcorequesting.client.interfaces.edit.GuiEditMenuItem;
import hardcorequesting.client.interfaces.edit.GuiEditMenuLocation;
import hardcorequesting.client.interfaces.edit.GuiEditMenuTextEditor;
import hardcorequesting.event.EventTrigger;
import hardcorequesting.quests.ItemPrecision;
import hardcorequesting.quests.Quest;
import hardcorequesting.quests.data.QuestDataTask;
import hardcorequesting.quests.data.QuestDataTaskLocation;
import hardcorequesting.util.SaveHelper;
import hardcorequesting.util.Translator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.UUID;

public class QuestTaskLocation extends QuestTask {
    
    
    private static final int CHECK_DELAY = 20;
    private static final int Y_OFFSET = 30;
    private static final int X_TEXT_OFFSET = 23;
    private static final int X_TEXT_INDENT = 0;
    private static final int Y_TEXT_OFFSET = 0;
    private static final int ITEM_SIZE = 18;
    public Location[] locations = new Location[0];
    private int delay = 1;
    
    public QuestTaskLocation(Quest parent, String description, String longDescription) {
        super(parent, description, longDescription);
        
        register(EventTrigger.Type.SERVER, EventTrigger.Type.PLAYER);
    }
    
    private void tick(Player player, boolean isPlayerEvent) {
        if (!isPlayerEvent) {
            delay++;
        } else if (this.delay >= 0) {
            delay = 0;
            
            Level world = player.getCommandSenderWorld();
            if (!world.isClientSide) {
                boolean[] visited = ((QuestDataTaskLocation) this.getData(player)).visited;
                boolean all = true;
                boolean updated = false;
                
                for (int i = 0; i < locations.length; ++i) {
                    Location location = this.locations[i];
                    if (visited.length < i) { // Fix to make sure than the visited array is as long as the location array (#400)
                        boolean[] oldVisited = ArrayUtils.addAll(visited, (boolean[]) null);
                        visited = new boolean[i];
                        System.arraycopy(oldVisited, 0, visited, 0, oldVisited.length);
                        ((QuestDataTaskLocation) this.getData(player)).visited = visited;
                    }
                    if (!visited[i] && player.getCommandSenderWorld().dimension() == location.dimension) {
                        int current = (int) player.distanceToSqr((double) location.x + 0.5D, (double) location.y + 0.5D, (double) location.z + 0.5D);
                        int target = location.radius * location.radius;
                        if (location.radius >= 0 && current > target) {
                            all = false;
                        } else {
                            if (!this.isCompleted(player) && this.isVisible(player) && this.parent.isEnabled(player) && this.parent.isAvailable(player)) {
                                updated = true;
                                visited[i] = true;
                            }
                        }
                    }
                }
                
                if (updated) {
                    if (all) {
                        completeTask(player.getUUID());
                    }
                    parent.sendUpdatedDataToTeam(player);
                }
            }
        }
    }
    
    @Environment(EnvType.CLIENT)
    private Location[] getEditFriendlyLocations(Location[] locations) {
        if (Quest.canQuestsBeEdited()) {
            locations = Arrays.copyOf(locations, locations.length + 1);
            locations[locations.length - 1] = new Location();
            return locations;
        } else {
            return locations;
        }
    }
    
    private boolean visited(int id, Player player) {
        return id < locations.length && ((QuestDataTaskLocation) getData(player)).visited[id];
    }
    
    public void setLocation(int id, Location location, Player player) {
        if (id >= locations.length) {
            locations = Arrays.copyOf(locations, locations.length + 1);
            QuestDataTaskLocation data = (QuestDataTaskLocation) getData(player);
            data.visited = Arrays.copyOf(data.visited, data.visited.length + 1);
            SaveHelper.add(SaveHelper.EditType.LOCATION_CREATE);
        } else {
            SaveHelper.add(SaveHelper.EditType.LOCATION_CHANGE);
        }
        
        locations[id] = location;
    }
    
    public void setIcon(int id, ItemStack stack, Player player) {
        setLocation(id, id >= locations.length ? new Location() : locations[id], player);
        
        locations[id].iconStack = stack;
    }
    
    public void setName(int id, String str, Player player) {
        setLocation(id, id >= locations.length ? new Location() : locations[id], player);
        
        locations[id].name = str;
    }
    
    @Override
    public Class<? extends QuestDataTask> getDataType() {
        return QuestDataTaskLocation.class;
    }
    
    @Environment(EnvType.CLIENT)
    @Override
    public void draw(PoseStack matrices, GuiQuestBook gui, Player player, int mX, int mY) {
        Location[] locations = getEditFriendlyLocations(this.locations);
        for (int i = 0; i < locations.length; i++) {
            Location location = locations[i];
            
            int x = START_X;
            int y = START_Y + i * Y_OFFSET;
            gui.drawItemStack(location.iconStack, x, y, mX, mY, false);
            gui.drawString(matrices, Translator.plain(location.name), x + X_TEXT_OFFSET, y + Y_TEXT_OFFSET, 0x404040);
            
            if (visited(i, player)) {
                gui.drawString(matrices, Translator.translatable("hqm.locationMenu.visited", GuiColor.GREEN), x + X_TEXT_OFFSET + X_TEXT_INDENT, y + Y_TEXT_OFFSET + 9, 0.7F, 0x404040);
            } else if (location.visible.doShowCoordinate()) {
                if (location.radius >= 0) {
                    gui.drawString(matrices, Translator.plain("(" + location.x + ", " + location.y + ", " + location.z + ")"), x + X_TEXT_OFFSET + X_TEXT_INDENT, y + Y_TEXT_OFFSET + 9, 0.7F, 0x404040);
                }
                
                if (player.getCommandSenderWorld().dimension() == location.dimension) {
                    if (location.radius >= 0) {
                        FormattedText str;
                        int distance = (int) player.distanceToSqr(location.x + 0.5, location.y + 0.5, location.z + 0.5);
                        str = Translator.translatable("hqm.locationMenu.mAway", distance);
                        if (location.visible.doShowRadius()) {
                            str = FormattedText.composite(str, Translator.plain(" ["), Translator.translatable("hqm.locationMenu.mRadius", location.radius), Translator.plain("]"));
                        }
                        gui.drawString(matrices, str, x + X_TEXT_OFFSET + X_TEXT_INDENT, y + Y_TEXT_OFFSET + 15, 0.7F, 0x404040);
                    }
                    
                } else {
                    gui.drawString(matrices, Translator.translatable("hqm.locationMenu.wrongDim"), x + X_TEXT_OFFSET + X_TEXT_INDENT, y + Y_TEXT_OFFSET + (location.radius >= 0 ? 15 : 9), 0.7F, 0x404040);
                }
                
            }
            
        }
    }
    
    @Environment(EnvType.CLIENT)
    @Override
    public void onClick(GuiQuestBook gui, Player player, int mX, int mY, int b) {
        if (Quest.canQuestsBeEdited() && gui.getCurrentMode() != EditMode.NORMAL) {
            Location[] locations = getEditFriendlyLocations(this.locations);
            for (int i = 0; i < locations.length; i++) {
                Location location = locations[i];
                
                int x = START_X;
                int y = START_Y + i * Y_OFFSET;
                
                if (gui.inBounds(x, y, ITEM_SIZE, ITEM_SIZE, mX, mY)) {
                    switch (gui.getCurrentMode()) {
                        case LOCATION:
                            gui.setEditMenu(new GuiEditMenuLocation(gui, this, location.copy(), i, player));
                            break;
                        case ITEM:
                            gui.setEditMenu(new GuiEditMenuItem(gui, player, location.iconStack, i, GuiEditMenuItem.Type.LOCATION, 1, ItemPrecision.PRECISE));
                            break;
                        case RENAME:
                            gui.setEditMenu(new GuiEditMenuTextEditor(gui, player, this, i, location));
                            break;
                        case DELETE:
                            if (i < this.locations.length) {
                                Location[] newLocations = new Location[this.locations.length - 1];
                                int id = 0;
                                for (int j = 0; j < this.locations.length; j++) {
                                    if (j != i) {
                                        newLocations[id] = this.locations[j];
                                        id++;
                                    }
                                }
                                this.locations = newLocations;
                                SaveHelper.add(SaveHelper.EditType.LOCATION_REMOVE);
                            }
                            break;
                        default:
                    }
                    
                    break;
                }
            }
        }
    }
    
    @Override
    public void onUpdate(Player player) {
        
    }
    
    @Override
    public float getCompletedRatio(UUID playerID) {
        int visited = 0;
        for (boolean b : ((QuestDataTaskLocation) getData(playerID)).visited) {
            if (b) {
                visited++;
            }
        }
        
        return (float) visited / locations.length;
    }
    
    @Override
    public void mergeProgress(UUID playerID, QuestDataTask own, QuestDataTask other) {
        boolean[] visited = ((QuestDataTaskLocation) own).visited;
        boolean[] otherVisited = ((QuestDataTaskLocation) other).visited;
        
        boolean all = true;
        for (int i = 0; i < visited.length; i++) {
            if (otherVisited[i]) {
                visited[i] = true;
            } else if (!visited[i]) {
                all = false;
            }
        }
        
        if (all) {
            completeTask(playerID);
        }
    }
    
    @Override
    public void autoComplete(UUID playerID, boolean status) {
        boolean[] visited = ((QuestDataTaskLocation) getData(playerID)).visited;
        for (int i = 0; i < visited.length; i++) {
            visited[i] = status;
        }
    }
    
    @Override
    public void copyProgress(QuestDataTask own, QuestDataTask other) {
        super.copyProgress(own, other);
        boolean[] visited = ((QuestDataTaskLocation) own).visited;
        System.arraycopy(((QuestDataTaskLocation) other).visited, 0, visited, 0, visited.length);
    }
    
    @Override
    public void onServerTick(MinecraftServer server) {
        tick(null, false);
    }
    
    @Override
    public void onPlayerTick(ServerPlayer playerEntity) {
        if (!playerEntity.level.isClientSide) {
            tick(playerEntity, true);
        }
    }
    
    public enum Visibility {
        FULL("Full", true, true),
        LOCATION("Location", true, false),
        NONE("None", false, false);
        
        private boolean showCoordinate;
        private boolean showRadius;
        private String id;
        
        Visibility(String id, boolean showCoordinate, boolean showRadius) {
            this.id = id;
            this.showCoordinate = showCoordinate;
            this.showRadius = showRadius;
        }
        
        public boolean doShowCoordinate() {
            return showCoordinate;
        }
        
        public boolean doShowRadius() {
            return showRadius;
        }
        
        public String getName() {
            return Translator.get("hqm.locationMenu.vis" + id + ".title");
        }
        
        public String getDescription() {
            return Translator.get("hqm.locationMenu.vis" + id + ".desc");
        }
    }
    
    public static class Location {
        
        private ItemStack iconStack = ItemStack.EMPTY;
        private String name = "New";
        private int x;
        private int y;
        private int z;
        private int radius = 3;
        private Visibility visible = Visibility.LOCATION;
        private ResourceKey<Level> dimension;
        
        private Location copy() {
            Location location = new Location();
            location.iconStack = iconStack.isEmpty() ? ItemStack.EMPTY : iconStack.copy();
            location.name = name;
            location.x = x;
            location.y = y;
            location.z = z;
            location.radius = radius;
            location.visible = visible;
            location.dimension = dimension;
            
            return location;
        }
        
        public ItemStack getIconStack() {
            return iconStack;
        }
        
        public void setIconStack(@NotNull ItemStack iconStack) {
            this.iconStack = iconStack;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public int getX() {
            return x;
        }
        
        public void setX(int x) {
            this.x = x;
        }
        
        public int getY() {
            return y;
        }
        
        public void setY(int y) {
            this.y = y;
        }
        
        public int getZ() {
            return z;
        }
        
        public void setZ(int z) {
            this.z = z;
        }
        
        public int getRadius() {
            return radius;
        }
        
        public void setRadius(int radius) {
            this.radius = radius;
        }
        
        public Visibility getVisible() {
            return visible;
        }
        
        public void setVisible(Visibility visible) {
            this.visible = visible;
        }
        
        public ResourceKey<Level> getDimension() {
            return dimension;
        }
        
        public void setDimension(ResourceKey<Level> dimension) {
            this.dimension = dimension;
        }
    }
}
