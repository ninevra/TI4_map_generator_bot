package ti4.draft.items;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ti4.draft.DraftItem;
import ti4.generator.Mapper;
import ti4.helpers.Emojis;
import ti4.model.DraftErrataModel;
import ti4.model.FactionModel;
import ti4.model.TechnologyModel;

import java.util.ArrayList;
import java.util.List;

public class TechDraftItem extends DraftItem {
    public TechDraftItem(String itemId) {
        super(Category.TECH, itemId);
    }

    @JsonIgnore
    @Override
    public String getShortDescription() {
        return getTech().getName();
    }

    private TechnologyModel getTech() {
        return Mapper.getTech(ItemId);
    }

    @JsonIgnore
    @Override
    public String getLongDescriptionImpl() {
        return getTech().getText() + " " + getTech().getRequirementsEmoji();
    }

    @JsonIgnore
    @Override
    public String getItemEmoji() {
        TechnologyModel model = getTech();
        return Emojis.getEmojiFromDiscord(model.getType().toString().toLowerCase() + "tech");
    }
    public static List<DraftItem> buildAllDraftableItems(List<FactionModel> factions) {
        List<DraftItem> allItems = new ArrayList<>();
        for (FactionModel faction : factions) {
            for (var tech : faction.getFactionTech()) {
                allItems.add(DraftItem.Generate(DraftItem.Category.TECH, tech));
            }
        }
        DraftErrataModel.filterUndraftablesAndShuffle(allItems, DraftItem.Category.TECH);
        return allItems;
    }
}
