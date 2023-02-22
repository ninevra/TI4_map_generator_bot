package ti4.commands.bothelper;

import java.util.Objects;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import ti4.helpers.Constants;
import ti4.helpers.Emojis;
import ti4.message.BotLogger;
import ti4.message.MessageHelper;

public class CreateGameChannels extends BothelperSubcommandData {
    public CreateGameChannels(){
        super(Constants.CREATE_GAME_CHANNELS, "Create Role and Game Channels for a New Game");
        addOptions(new OptionData(OptionType.STRING, Constants.GAME_NAME, "Game Name/Role to Create - e.g. pbd###").setRequired(true));
        addOptions(new OptionData(OptionType.STRING, Constants.GAME_FUN_NAME, "Fun Name for the Channel - e.g. pbd###-fun-name-goes-here").setRequired(true));
        addOptions(new OptionData(OptionType.CHANNEL, Constants.CATEGORY, "Category #category-name - only select a category").setRequired(true));
        addOptions(new OptionData(OptionType.USER, Constants.PLAYER1, "Player1 @playerName"));
        addOptions(new OptionData(OptionType.USER, Constants.PLAYER2, "Player2 @playerName"));
        addOptions(new OptionData(OptionType.USER, Constants.PLAYER3, "Player3 @playerName"));
        addOptions(new OptionData(OptionType.USER, Constants.PLAYER4, "Player4 @playerName"));
        addOptions(new OptionData(OptionType.USER, Constants.PLAYER5, "Player5 @playerName"));
        addOptions(new OptionData(OptionType.USER, Constants.PLAYER6, "Player6 @playerName"));
        addOptions(new OptionData(OptionType.USER, Constants.PLAYER7, "Player7 @playerName"));
        addOptions(new OptionData(OptionType.USER, Constants.PLAYER8, "Player8 @playerName"));
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        String gameName = event.getOption(Constants.GAME_NAME).getAsString();
        
        //CHECK ROLE IS VALID
        if (event.getGuild().getRolesByName(gameName, false).size() > 0) {
            MessageHelper.replyToMessage(event, "Role: **" + gameName + "** already exists. Try again with a new name.");
            return;
        }

        //CHECK ROLE COUNT
        if (event.getGuild().getRoles().size() >= 250) {
            MessageHelper.replyToMessage(event, "Server is at the role limit - please contact @Admin to resolve.");
            BotLogger.log(event, "Cannot create a new role. Server is currently has " + event.getGuild().getRoles().size() + " roles.");
            return;
        }

        //CHECK CATEGORY IS VALID
        GuildChannelUnion categoryChannel = event.getOption(Constants.CATEGORY).getAsChannel();
        if (categoryChannel == null || categoryChannel.getType() != ChannelType.CATEGORY) {
            MessageHelper.replyToMessage(event, "Category: **" + categoryChannel.getName() + "** does not exist. Create the category or pick a different category, then try again.");
            return;
        } 
        
        Category category = categoryChannel.asCategory();
        if (category.getChannels().size() > 48) {
            MessageHelper.replyToMessage(event, "Category: **" + category.getName() + "** is full. Create a new category then try again.");
            return;
        }

        StringBuilder message = new StringBuilder("Role and Channels have been set up:\n");

        //CREATE ROLE
        Role role = event.getGuild().createRole()
            .setName(gameName)
            .setMentionable(true)
            .complete();
        message.append("> " + role.getAsMention() + "\n");

        //ADD PLAYERS TO ROLE
        for (int i = 1; i <= 8; i++) {
            if (Objects.nonNull(event.getOption("player" + i))) {
                Member member = event.getOption("player" + i).getAsMember();
                event.getGuild().addRoleToMember(member, role).complete();
            } else {
                break;
            }
        }

        //CREATE CHANNELS
        String gameFunName = event.getOption(Constants.GAME_FUN_NAME).getAsString().replaceAll(" ", "-");
        String newChatChannelName = gameName + "-" + gameFunName;
        String newActionsChannelName = gameName + Constants.ACTIONS_CHANNEL_SUFFIX;
        String newBotThreadName = gameName + Constants.BOT_CHANNEL_SUFFIX;
        long gameRoleID = role.getIdLong();
        long permission = Permission.MESSAGE_MANAGE.getRawValue() | Permission.VIEW_CHANNEL.getRawValue();

        //TABLETALK CHANNEL
        TextChannel chatChannel = event.getGuild().createTextChannel(newChatChannelName, category)
            .syncPermissionOverrides()
            .addRolePermissionOverride(gameRoleID, permission, 0)
            .complete();
        MessageHelper.sendMessageToChannel((MessageChannel) chatChannel, role.getAsMention()+ " - table talk channel");
        message.append("> " + chatChannel.getAsMention()).append("\n");

        //ACTIONS CHANNEL
        TextChannel actionsChannel = event.getGuild().createTextChannel(newActionsChannelName, category)
            .syncPermissionOverrides()
            .addRolePermissionOverride(gameRoleID, permission, 0)
            .complete();
        MessageHelper.sendMessageToChannel((MessageChannel) actionsChannel, role.getAsMention() + " - actions channel");
        message.append("> " + actionsChannel.getAsMention()).append("\n");

        //BOT/MAP THREAD
        ThreadChannel botThread = actionsChannel.createThreadChannel(newBotThreadName)
                        .setAutoArchiveDuration(ThreadChannel.AutoArchiveDuration.TIME_24_HOURS)
                        .complete();

        StringBuilder botGetStartedMessage = new StringBuilder(role.getAsMention()).append(" - bot/map channel\n");
        botGetStartedMessage.append("Use the following commands to get started:\n");
        botGetStartedMessage.append("> `/create_game game_name:" + gameName + "`\n");
        botGetStartedMessage.append("> `/game setup game_custom_name:" + gameName + "-" + gameFunName + "` to set player count and additional options\n");
        botGetStartedMessage.append("> `/add_tile_list {mapString}`, replacing {mapString} with the actual map string\n");
        botGetStartedMessage.append("> `/game add` to add players to the game - do this in speaker order (starting at top of map going clockwise)\n");
        botGetStartedMessage.append("> `/game set_order` to fix the order if incorrect\n");
        botGetStartedMessage.append("> `/player setup` to set player faction and colour\n");
        botGetStartedMessage.append("> `/add_frontier_tokens` to place frontier tokens\n");
        botGetStartedMessage.append("> `/player tech_add` for factions who need to add tech\n");
        botGetStartedMessage.append("> `/so deal_to_all (count:2)` to deal two" + Emojis.SecretObjective + " to all players\n");
        // botGetStartedMessage.append("> `/status po_reveal_stage1` to reveal the first" + Emojis.Public1 + "Stage 1 Public Objective\n");
        MessageHelper.sendMessageToChannel((MessageChannel) botThread, botGetStartedMessage.toString());
        message.append("> " + botThread.getAsMention()).append("\n");
        
        MessageHelper.replyToMessage(event, message.toString());
    }
}
