package ti4.commands.player;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import ti4.generator.Mapper;
import ti4.helpers.Constants;
import ti4.map.Map;
import ti4.map.Player;
import ti4.message.MessageHelper;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public abstract class TechAddRemove extends PlayerSubcommandData{
    public TechAddRemove(String id, String description) {
        super(id, description);
        addOptions(new OptionData(OptionType.STRING, Constants.TECH, "Tech").setRequired(true).setAutoComplete(true));
        addOptions(new OptionData(OptionType.STRING, Constants.TECH2, "2dn Tech").setAutoComplete(true));
        addOptions(new OptionData(OptionType.STRING, Constants.TECH3, "3rd Tech").setAutoComplete(true));
        addOptions(new OptionData(OptionType.STRING, Constants.TECH4, "4th Tech").setAutoComplete(true));
        addOptions(new OptionData(OptionType.USER, Constants.PLAYER, "Player for which you set up faction").setRequired(false));
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Map activeMap = getActiveMap();
        Player player = activeMap.getPlayer(getUser().getId());
        if (player == null) {
            MessageHelper.sendMessageToChannel(event.getChannel(), "Player could not be found");
            return;
        }

        OptionMapping playerOption = event.getOption(Constants.PLAYER);
        if (playerOption != null) {
            String playerID = playerOption.getAsUser().getId();
            if (activeMap.getPlayer(playerID) != null) {
                player = activeMap.getPlayers().get(playerID);
            } else {
                MessageHelper.sendMessageToChannel(event.getChannel(), "Player:" + playerOption.getAsUser().getName() + " could not be found in map:" + activeMap.getName());
                return;
            }
        }

        parseParameter(event, player, event.getOption(Constants.TECH));
        parseParameter(event, player, event.getOption(Constants.TECH2));
        parseParameter(event, player, event.getOption(Constants.TECH3));
        parseParameter(event, player, event.getOption(Constants.TECH4));
    }

    private void parseParameter(SlashCommandInteractionEvent event, Player player, OptionMapping techOption) {
        if (techOption != null) {
            String techID = techOption.getAsString();
            if (Mapper.isValidTech(techID)) {
                doAction(player, techID);
            } else {
                HashMap<String, String> techs = Mapper.getTechs();
                List<String> possibleTechs = techs.entrySet().stream().filter(value -> value.getValue().toLowerCase().contains(techID))
                        .map(java.util.Map.Entry::getKey).toList();
                if (possibleTechs.isEmpty()){
                    MessageHelper.sendMessageToChannel(event.getChannel(), "No matching Tech found");
                    return;
                } else if (possibleTechs.size() > 1){
                    MessageHelper.sendMessageToChannel(event.getChannel(), "More that one matching Tech found");
                    return;
                }
                doAction(player, possibleTechs.get(0));
            }
        } else {
            MessageHelper.sendMessageToChannel(event.getChannel(), "Please select valid tech");
        }
    }

    public abstract void doAction(Player player, String techID);
}
