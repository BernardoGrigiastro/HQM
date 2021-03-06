package hardcorequesting.commands.sub;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import hardcorequesting.commands.CommandHandler;
import hardcorequesting.quests.Quest;
import hardcorequesting.quests.QuestLine;
import hardcorequesting.util.HQMUtil;
import hardcorequesting.util.Translator;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;

public class EditSubCommand implements CommandHandler.SubCommand {
    @Override
    public ArgumentBuilder<ServerCommandSource, ?> build(LiteralArgumentBuilder<ServerCommandSource> builder) {
        return builder
                .requires(source -> source.hasPermissionLevel(4))
                .executes(context -> {
                    if (HQMUtil.isGameSingleplayer() && QuestLine.doServerSync) {
                        context.getSource().sendFeedback(Translator.translatable("hqm.command.editMode.disableSync").setStyle(Style.EMPTY.setColor(Formatting.RED).setBold(true)), false);
                        Quest.setEditMode(false);
                    } else if (HQMUtil.isGameSingleplayer()) {
                        boolean newEditModeState = !Quest.canQuestsBeEdited();
                        Quest.setEditMode(newEditModeState);
                        if (newEditModeState) {
                            context.getSource().sendFeedback(Translator.translatable("hqm.command.editMode.enabled"), false);
                        } else {
                            context.getSource().sendFeedback(Translator.translatable("hqm.command.editMode.disabled"), false);
                        }
                    } else {
                        context.getSource().sendFeedback(Translator.translatable("hqm.command.editMode.server"), false);
                        Quest.setEditMode(false);
                    }
                    return 1;
                });
    }
}
