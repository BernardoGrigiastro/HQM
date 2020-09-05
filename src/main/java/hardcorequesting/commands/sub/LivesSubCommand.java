package hardcorequesting.commands.sub;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import hardcorequesting.HardcoreQuesting;
import hardcorequesting.commands.CommandHandler;
import hardcorequesting.config.HQMConfig;
import hardcorequesting.quests.QuestingData;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import static net.minecraft.commands.Commands.literal;

public class LivesSubCommand implements CommandHandler.SubCommand {
    @Override
    public ArgumentBuilder<CommandSourceStack, ?> build(LiteralArgumentBuilder<CommandSourceStack> builder) {
        return builder
                .requires(source -> source.hasPermission(4))
                .then(literal("add")
                        .then(Commands.argument("targets", EntityArgument.players())
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(context -> {
                                            if (!QuestingData.isHardcoreActive()) {
                                                context.getSource().sendFailure(new TranslatableComponent("hqm.message.noHardcoreYet"));
                                                return 1;
                                            }
                                            for (ServerPlayer player : EntityArgument.getPlayers(context, "targets")) {
                                                addLivesTo(context.getSource(), player, IntegerArgumentType.getInteger(context, "amount"));
                                            }
                                            return 1;
                                        }))
                                .executes(context -> {
                                    if (!QuestingData.isHardcoreActive()) {
                                        context.getSource().sendFailure(new TranslatableComponent("hqm.message.noHardcoreYet"));
                                        return 1;
                                    }
                                    for (ServerPlayer player : EntityArgument.getPlayers(context, "targets")) {
                                        addLivesTo(context.getSource(), player, 1);
                                    }
                                    return 1;
                                }))
                        .executes(context -> {
                            if (!QuestingData.isHardcoreActive()) {
                                context.getSource().sendFailure(new TranslatableComponent("hqm.message.noHardcoreYet"));
                                return 1;
                            }
                            if (context.getSource().getEntity() instanceof Player)
                                addLivesTo(context.getSource(), (Player) context.getSource().getEntity(), 1);
                            return 1;
                        })
                )
                .then(literal("remove")
                        .then(Commands.argument("targets", EntityArgument.players())
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(context -> {
                                            if (!QuestingData.isHardcoreActive()) {
                                                context.getSource().sendFailure(new TranslatableComponent("hqm.message.noHardcoreYet"));
                                                return 1;
                                            }
                                            for (ServerPlayer player : EntityArgument.getPlayers(context, "targets")) {
                                                removeLivesFrom(context.getSource(), player, IntegerArgumentType.getInteger(context, "amount"));
                                            }
                                            return 1;
                                        }))
                                .executes(context -> {
                                    if (!QuestingData.isHardcoreActive()) {
                                        context.getSource().sendFailure(new TranslatableComponent("hqm.message.noHardcoreYet"));
                                        return 1;
                                    }
                                    for (ServerPlayer player : EntityArgument.getPlayers(context, "targets")) {
                                        removeLivesFrom(context.getSource(), player, 1);
                                    }
                                    return 1;
                                }))
                        .executes(context -> {
                            if (!QuestingData.isHardcoreActive()) {
                                context.getSource().sendFailure(new TranslatableComponent("hqm.message.noHardcoreYet"));
                                return 1;
                            }
                            if (context.getSource().getEntity() instanceof Player)
                                removeLivesFrom(context.getSource(), (Player) context.getSource().getEntity(), 1);
                            return 1;
                        })
                )
                .then(Commands.argument("targets", EntityArgument.players())
                        .executes(context -> {
                            if (!QuestingData.isHardcoreActive()) {
                                context.getSource().sendFailure(new TranslatableComponent("hqm.message.noHardcoreYet"));
                                return 1;
                            }
                            currentLives(context.getSource(), EntityArgument.getPlayer(context, "targets"));
                            return 1;
                        }))
                .executes(context -> {
                    if (!QuestingData.isHardcoreActive()) {
                        context.getSource().sendFailure(new TranslatableComponent("hqm.message.noHardcoreYet"));
                        return 1;
                    }
                    if (context.getSource().getEntity() instanceof Player)
                        currentLives((Player) context.getSource().getEntity());
                    return 1;
                });
    }
    
    @Override
    public int[] getSyntaxOptions(CommandContext<CommandSourceStack> context) {
        return new int[]{0, 1, 2, 3};
    }
    
    private void removeLivesFrom(CommandSourceStack source, Player player, int amount) {
        QuestingData.getQuestingData(player).removeLives(player, amount);
        sendTranslatableChat(source, amount != 1, "hqm.message.removeLivesFrom", amount, player.getScoreboardName());
        if (source.getEntity() != player)
            sendTranslatableChat(player.createCommandSourceStack(), amount != 1, "hqm.message.removeLivesBy", amount, source.getTextName());
        currentLives(player);
    }
    
    private void addLivesTo(CommandSourceStack source, Player player, int amount) {
        if (QuestingData.getQuestingData(player).getRawLives() + amount <= HQMConfig.getInstance().Hardcore.MAX_LIVES) {
            QuestingData.getQuestingData(player).addLives(player, amount);
            sendTranslatableChat(source, amount != 1, "hqm.message.addLivesTo", amount, player.getScoreboardName());
            if (source.getEntity() != player)
                sendTranslatableChat(player.createCommandSourceStack(), amount != 1, "hqm.message.addLivesBy", amount, source.getTextName());
            currentLives(player);
        } else {
            QuestingData.getQuestingData(player).addLives(player, amount);
            sendTranslatableChat(source, "hqm.message.cantGiveMoreLives", player.getScoreboardName(), HQMConfig.getInstance().Hardcore.MAX_LIVES);
            sendTranslatableChat(source, "hqm.massage.setLivesInstead", player.getScoreboardName(), HQMConfig.getInstance().Hardcore.MAX_LIVES);
            if (source.getEntity() != player)
                sendTranslatableChat(player.createCommandSourceStack(), "hqm.massage.setLivesBy", HQMConfig.getInstance().Hardcore.MAX_LIVES, source.getTextName());
            currentLives(player);
        }
    }
    
    private void getPlayerLives(CommandSourceStack source, String playerName) throws CommandRuntimeException {
        Player player = HardcoreQuesting.getServer().getPlayerList().getPlayerByName(playerName);
        if (player != null) {
            int lives = QuestingData.getQuestingData(player).getLives();
            sendTranslatableChat(source, lives != 1, "hqm.message.hasLivesRemaining", playerName, lives);
        } else {
            throw new CommandRuntimeException(new TranslatableComponent("hqm.message.noPlayer"));
        }
    }
}
