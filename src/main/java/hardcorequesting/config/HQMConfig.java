package hardcorequesting.config;

import com.google.common.collect.Lists;
import hardcorequesting.HardcoreQuesting;
import hardcorequesting.client.KeyboardHandler;
import hardcorequesting.items.BagItem;
import hardcorequesting.quests.Quest;
import hardcorequesting.quests.QuestLine;
import hardcorequesting.quests.QuestingData;
import hardcorequesting.team.RewardSetting;
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.ConfigData;
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config;
import me.sargunvohra.mcmods.autoconfig1u.annotation.ConfigEntry;
import me.sargunvohra.mcmods.autoconfig1u.serializer.JanksonConfigSerializer;
import me.sargunvohra.mcmods.autoconfig1u.shadowed.blue.endless.jankson.Comment;

import java.util.List;

@Config(name = "hqm/config")
public class HQMConfig implements ConfigData {
    
    static {
        AutoConfig.register(HQMConfig.class, JanksonConfigSerializer::new);
    }
    
    @Comment("Settings related to hardcore mode")
    //@Name("Hardcore settings")
    public Hardcore Hardcore = new Hardcore();
    @Comment("Settings related to server start & modes")
    //@Name("Server start settings")
    public Starting Starting = new Starting();
    @Comment("Settings related to loot bags and loot tiers")
    //@Name("Loot settings")
    public Loot Loot = new Loot();
    @Comment("Settings related to the interface")
    //@Name("Interface settings")
    public Interface Interface = new Interface();
    
    //@Name("Enable spawn with book")
    @Comment("Enable this to cause the player to be granted a gift on spawning into the world")
    public boolean SPAWN_BOOK = false;
    
    //@Name("Lose quest book upon death")
    @Comment("Loose the quest book when you die, if set to false it will stay in your inventory")
    public boolean LOSE_QUEST_BOOK = true;
    
    //@Name("All in party get rewards")
    @Comment("Allow every single player in a party to claim the reward for a quest. Setting this to false will give the party one set of rewards to share.")
    public boolean MULTI_REWARD = true;
    
    //@Name("NBT Subset Tags to Filter")
    @Comment("Use this to specify NBT tags that should be ignored when comparing items with NBT subset")
    public String[] NBT_SUBSET_FILTER = new String[]{"RepairCost"};
    
    @Comment("Settings related to messages sent from the server")
    //@Name("Message settings")
    public Message Message = new Message();
    @Comment("Settings related to edit mode")
    //@Name("Editing settings")
    public Editing Editing = new Editing();
    
    public static int OVERLAY_XPOS;
    
    public static int OVERLAY_YPOS;
    
    public static int OVERLAY_XPOSDEFAULT = 2;
    
    public static int OVERLAY_YPOSDEFAULT = 2;
    
    public static int CURRENTLY_MODIFYING_QUEST_SET = 0x4040dd;
    
    public static int COMPLETED_SELECTED_IN_BOUNDS_SET = 0x40bb40;
    
    public static int COMPLETED_SELECTED_OUT_OF_BOUNDS_SET = 0x40a040;
    
    public static int COMPLETED_UNSELECTED_IN_BOUNDS_SET = 0x10a010;
    
    public static int COMPLETED_UNSELECTED_OUT_OF_BOUNDS_SET = 0x107010;
    
    public static int UNCOMPLETED_SELECTED_IN_BOUNDS_SET = 0xaaaaaa;
    
    public static int UNCOMPLETED_SELECTED_OUT_OF_BOUNDS_SET = 0x888888;
    
    public static int UNCOMPLETED_UNSELECTED_IN_BOUNDS_SET = 0x666666;
    
    public static int UNCOMPLETED_UNSELECTED_OUT_OF_BOUNDS_SET = 0x404040;
    
    public static int DISABLED_SET = 0xdddddd;
    
    public static int QUEST_INVISIBLE = 0x55FFFFFF;
    
    public static int QUEST_DISABLED = 0xFF888888;
    
    public static int QUEST_COMPLETE = 0xFFFFFFFF;
    
    public static int QUEST_COMPLETE_REPEATABLE = 0xFFFFFFCC;
    
    public static int QUEST_AVAILABLE = 0x554286f4;
    
    public static HQMConfig getInstance() {
        return AutoConfig.getConfigHolder(HQMConfig.class).getConfig();
    }
    
    public static void parseSetColours() {
        try {
            COMPLETED_SELECTED_IN_BOUNDS_SET = Long.decode(getInstance().Interface.QuestSets.COMPLETED_SELECTED_IN_BOUNDS_SET.toLowerCase()).intValue();
            COMPLETED_UNSELECTED_IN_BOUNDS_SET = Long.decode(getInstance().Interface.QuestSets.COMPLETED_UNSELECTED_IN_BOUNDS_SET.toLowerCase()).intValue();
            UNCOMPLETED_SELECTED_IN_BOUNDS_SET = Long.decode(getInstance().Interface.QuestSets.UNCOMPLETED_SELECTED_IN_BOUNDS_SET.toLowerCase()).intValue();
            UNCOMPLETED_UNSELECTED_IN_BOUNDS_SET = Long.decode(getInstance().Interface.QuestSets.UNCOMPLETED_UNSELECTED_IN_BOUNDS_SET.toLowerCase()).intValue();
            DISABLED_SET = Long.decode(getInstance().Interface.QuestSets.DISABLED_SET.toLowerCase()).intValue();
        } catch (NumberFormatException e) {
            HardcoreQuesting.LOG.error("Unable to parse set colours", e);
        }
    }
    
    public static void parseQuestColours() {
        try {
            QUEST_INVISIBLE = Long.decode(getInstance().Interface.Quests.QUEST_INVISIBLE.toLowerCase()).intValue();
            QUEST_DISABLED = Long.decode(getInstance().Interface.Quests.QUEST_DISABLED.toLowerCase()).intValue();
            QUEST_COMPLETE = Long.decode(getInstance().Interface.Quests.QUEST_COMPLETE.toLowerCase()).intValue();
            QUEST_COMPLETE_REPEATABLE = Long.decode(getInstance().Interface.Quests.QUEST_COMPLETE_REPEATABLE.toLowerCase()).intValue();
            QUEST_AVAILABLE = Long.decode(getInstance().Interface.Quests.QUEST_AVAILABLE.toLowerCase()).intValue();
        } catch (NumberFormatException e) {
            HardcoreQuesting.LOG.error("Unable to parse quest colours", e);
        }
    }
    
    @SuppressWarnings("deprecation")
    public static void loadConfig() {
        parseSetColours();
        parseQuestColours();
        
        int maxlives = getInstance().Hardcore.MAX_LIVES;
        int lives = getInstance().Hardcore.DEFAULT_LIVES;
        
        if (lives > maxlives) lives = maxlives;
        
        QuestingData.defaultLives = lives;
        QuestingData.autoHardcoreActivate = getInstance().Starting.AUTO_HARDCORE;
        QuestingData.autoQuestActivate = getInstance().Starting.AUTO_QUESTING;
        RewardSetting.isAllModeEnabled = getInstance().MULTI_REWARD;
        BagItem.displayGui = getInstance().Loot.REWARD_INTERFACE;
        QuestLine.doServerSync = getInstance().Starting.SERVER_SYNC;
        
        Quest.isEditing = getInstance().Editing.USE_EDITOR;
        Quest.saveDefault = getInstance().Editing.SAVE_DEFAULT;
        if (HardcoreQuesting.proxy.isClient()) {
            KeyboardHandler.fromConfig(getInstance().Editing.HOTKEYS.toArray(new String[0]));
        }
    }
    
    public static class Hardcore {
        //@Name("Default lives")
        @Comment("How many lives players should start with.")
        @ConfigEntry.BoundedDiscrete(min = 1, max = 256)
        public int DEFAULT_LIVES = 3;
        
        //@Name("Heart Rot Timer in Seconds")
        @Comment("Define in seconds how long the rot timer is.")
        //@RangeInt(min = 1)
        public int HEART_ROT_TIME = 120;
        
        //@Name("Enable rot timer")
        @Comment("Set to true to enable the heart rot timer")
        public boolean HEART_ROT_ENABLE = false;
        
        //@Name("Maximum lives obtainable")
        @Comment("Use this to set the maximum lives obtainable")
        @ConfigEntry.BoundedDiscrete(min = 0, max = 256)
        public int MAX_LIVES = 20;
    }
    
    public static class Starting {
        //@Name("Auto-start hardcore mode")
        @Comment("If set to true, new worlds will automatically activate Hardcore mode")
        public boolean AUTO_HARDCORE = false;
        //@Name("Auto-start questing mode")
        @Comment("If set to true, new worlds will automatically activate Questing mode")
        public boolean AUTO_QUESTING = true;
        //@Name("Enable server sync")
        @Comment("Set to true to enable the sending of quests from the server to all clients that connect to it")
        public boolean SERVER_SYNC = false;
    }
    
    public static class Loot {
        //@Name("Always use tier name for reward titles")
        @Comment("Always display the tier name, instead of the individual bag's name, when opening a reward bag.")
        public boolean ALWAYS_USE_TIER = false;
        //@Name("Display reward interface")
        @Comment("Set to true to display an interface with the contents of the reward bag when you open it.")
        public boolean REWARD_INTERFACE = true;
    }
    
    public static class Message {
        //@Name("Enable hardcore mode status message")
        @Comment("Set to true to enable sending a status message if Hardcore Questing mode is off")
        public boolean NO_HARDCORE_MESSAGE = false;
        
        //@Name("Enable OP command reminder")
        @Comment("Set to false to prevent the 'use /hqm op instead' message when operators use '/hqm edit' instead.")
        public boolean OP_REMINDER = true;
    }
    
    public static class Editing {
        //@Name("Save quests in default folder")
        @Comment("Set to true to save work-in-progress quests in the default folder of the configuration")
        public boolean SAVE_DEFAULT = true;
        
        //@Name("Enable edit mode by default")
        @Comment("Set to true to automatically enable edit mode when entering worlds in single-player. Has no effect in multiplayer.")
        public boolean USE_EDITOR = false;
        
        //@Name("Hotkeys")
        @Comment("Hotkeys used in the book, one entry per line(Format: [key]:[mode]")
        public List<String> HOTKEYS = Lists.newArrayList("68:delete", "78:create", "77:move", "260:create", "82:rename", "261:delete", "32:normal", "83:swap", "83:swap_select");
    }
    
    public static class Interface {
        //@Name("Quest Set Colours")
        @Comment("Colour settings for quest set rendering")
        public QuestSets QuestSets = new QuestSets();
        //@Name("Quest Colours")
        @Comment("Colour settings for quests")
        public Quests Quests = new Quests();
        
        public static class QuestSets {
            //@Name("Set is completed, selected")
            @Comment("Use the HTML format, e.g.: #ffffff")
            public String COMPLETED_SELECTED_IN_BOUNDS_SET = "#40bb40";
            //@Name("Set is complete, unselected")
            @Comment("Use the HTML format, e.g.: #ffffff")
            public String COMPLETED_UNSELECTED_IN_BOUNDS_SET = "#10a010";
            //@Name("Set is incomplete, selected")
            @Comment("Use the HTML format, e.g.: #ffffff")
            public String UNCOMPLETED_SELECTED_IN_BOUNDS_SET = "#aaaaaa";
            //@Name("Set is incomplete, unselected")
            @Comment("Use the HTML format, e.g.: #ffffff")
            public String UNCOMPLETED_UNSELECTED_IN_BOUNDS_SET = "#666666";
            //@Name("Set is currently disabled")
            @Comment("Use the HTML format, e.g.: #ffffff")
            public String DISABLED_SET = "#dddddd";
        }
        
        public static class Quests {
            
            public String QUEST_INVISIBLE = "#55FFFFFF";
            
            //@Name("Quest is not accessible")
            @Comment("Use the HTML format with alpha, e.g.: #55FFFFFF")
            public String QUEST_DISABLED = "#FF888888";
            
            //@Name("Quest is completed and not repeatable")
            @Comment("Use the HTML format with alpha, e.g.: #55FFFFFF")
            public String QUEST_COMPLETE = "#FFFFFFFF";
            
            //@Name("Quest is completed but repeatable")
            @Comment("Use the HTML format with alpha, e.g.: #55FFFFFF")
            public String QUEST_COMPLETE_REPEATABLE = "#FFFFFFCC";
            
            //@Name("Quest is available")
            @Comment("Use the HTML format with alpha, e.g: #55FFFFFF")
            public String QUEST_AVAILABLE = "#554286f4";
            
            //@Name("Use single colour for available quest")
            @Comment("Set to true to disable the default colour pulse and use the colour specified above")
            public boolean SINGLE_COLOUR = false;
        }
    }
}
