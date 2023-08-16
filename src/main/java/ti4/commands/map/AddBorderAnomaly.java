package ti4.commands.map;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import ti4.MapGenerator;
import ti4.commands.Command;
import ti4.generator.GenerateMap;
import ti4.helpers.Constants;
import ti4.map.Map;
import ti4.map.MapManager;
import ti4.map.MapSaveLoadManager;
import ti4.message.MessageHelper;
import ti4.model.BorderAnomalyModel;

import java.io.File;
import java.util.List;

public class AddBorderAnomaly implements Command {
    @Override
    public String getActionID() {
        return Constants.ADD_BORDER_ANOMALY;
    }

    @Override
    public boolean accept(SlashCommandInteractionEvent event) {
        if (!event.getName().equals(getActionID())) {
            return false;
        }
        User user = event.getUser();
        Map activeMap = MapManager.getInstance().getUserActiveMap(user.getId());

        if (!activeMap.getTileMap().containsKey(event.getOption(Constants.PRIMARY_TILE, null, OptionMapping::getAsString))) {
            MessageHelper.replyToMessage(event, "Map does not contain that tile");
            return false;
        }
        return true;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        String tile = event.getOption(Constants.PRIMARY_TILE, null, OptionMapping::getAsString);
        String direction = event.getOption(Constants.PRIMARY_TILE_DIRECTION, null, OptionMapping::getAsString);
        String anomalyTypeString = event.getOption(Constants.BORDER_TYPE, null, OptionMapping::getAsString);
        BorderAnomalyModel model = new BorderAnomalyModel();

        BorderAnomalyModel.BorderAnomalyType anomalyType = model.getBorderAnomalyTypeFromString(anomalyTypeString);

        int directionVal = -1;
        switch (direction.toLowerCase()) {
            case "north" -> directionVal = 0;
            case "northeast" -> directionVal = 1;
            case "southeast" -> directionVal = 2;
            case "south" -> directionVal = 3;
            case "southwest" -> directionVal = 4;
            case "northwest" -> directionVal = 5;
        }

        if(directionVal == -1) {
            MessageHelper.sendMessageToChannel(event.getChannel(), "Invalid direction");
            return;
        }

        User user = event.getUser();
        Map activeMap = MapManager.getInstance().getUserActiveMap(user.getId());

        if(activeMap.hasBorderAnomalyOn(tile, directionVal)) {
            MessageHelper.sendMessageToChannel(event.getChannel(), "Tile already has an anomaly there!");
            return;
        }

        activeMap.addBorderAnomaly(tile, directionVal, anomalyType);
        MapSaveLoadManager.saveMap(activeMap, event);
        File file = GenerateMap.getInstance().saveImage(activeMap, event);
        MessageHelper.replyToMessage(event, file);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void registerCommands(CommandListUpdateAction commands) {
        commands.addCommands(Commands.slash(getActionID(), "Add a border anomaly to a tile")
                .addOptions(
                        new OptionData(OptionType.STRING, Constants.PRIMARY_TILE, "Tile the border will be linked to").setRequired(true).setAutoComplete(true),
                        new OptionData(OptionType.STRING, Constants.PRIMARY_TILE_DIRECTION, "Side of the tile the anomaly will be on").setRequired(true).setAutoComplete(true),
                        new OptionData(OptionType.STRING, Constants.BORDER_TYPE, "Type of anomaly").setRequired(true).setAutoComplete(true)
                )
        );
    }
}
