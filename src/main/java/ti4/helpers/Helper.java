package ti4.helpers;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import org.jetbrains.annotations.Nullable;

import ti4.MapGenerator;
import ti4.ResourceHelper;
import ti4.commands.bothelper.ArchiveOldThreads;
import ti4.commands.bothelper.ListOldChannels;
import ti4.commands.tokens.AddCC;
import ti4.generator.Mapper;
import ti4.map.*;
import ti4.message.BotLogger;
import ti4.message.MessageHelper;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Helper {

    @Nullable
    public static Player getGamePlayer(Map map, Player initialPlayer, SlashCommandInteractionEvent event, String userID) {
        return getGamePlayer(map, initialPlayer, event.getMember(), userID);
    }

    @Nullable
    public static Player getGamePlayer(Map map, Player initialPlayer, Member member, String userID) {
        Collection<Player> players = map.getPlayers().values();
        if (!map.isCommunityMode()) {
            Player player = map.getPlayer(userID);
            if (player != null) return player;
            return initialPlayer;
        }
        if (member == null) {
            Player player = map.getPlayer(userID);
            if (player != null) return player;
            return initialPlayer;
        }
        java.util.List<Role> roles = member.getRoles();
        for (Player player : players) {
            if (roles.contains(player.getRoleForCommunity())) {
                return player;
            }
        }
        return initialPlayer != null ? initialPlayer : map.getPlayer(userID);
    }

    @Nullable
    public static Player getPlayer(Map activeMap, Player player, SlashCommandInteractionEvent event) {
        OptionMapping playerOption = event.getOption(Constants.PLAYER);
        OptionMapping factionColorOption = event.getOption(Constants.FACTION_COLOR);
        if (playerOption != null) {
            String playerID = playerOption.getAsUser().getId();
            if (activeMap.getPlayer(playerID) != null) {
                player = activeMap.getPlayers().get(playerID);
            } else {
                player = null;
            }
        } else if (factionColorOption != null) {
            String factionColor = AliasHandler.resolveColor(factionColorOption.getAsString().toLowerCase());
            factionColor = AliasHandler.resolveFaction(factionColor);
            for (Player player_ : activeMap.getPlayers().values()) {
                if (Objects.equals(factionColor, player_.getFaction()) ||
                        Objects.equals(factionColor, player_.getColor())) {
                    player = player_;
                    break;
                }
            }
        }
        return player;
    }

    @Nullable
    public static String getColor(Map activeMap, SlashCommandInteractionEvent event) {
        OptionMapping factionColorOption = event.getOption(Constants.FACTION_COLOR);
        if (factionColorOption != null) {
            String colorFromString = getColorFromString(activeMap, factionColorOption.getAsString());
            if (Mapper.isColorValid(colorFromString)) {
                return colorFromString;
            }
        } else {
            String userID = event.getUser().getId();
            Player foundPlayer = activeMap.getPlayers().values().stream().filter(player -> player.getUserID().equals(userID)).findFirst().orElse(null);
            foundPlayer = Helper.getGamePlayer(activeMap, foundPlayer, event, null);
            if (foundPlayer != null) {
                return foundPlayer.getColor();
            }
        }
        return null;
    }


    public static String getColorFromString(Map activeMap, String factionColor) {
        factionColor = AliasHandler.resolveColor(factionColor);
        factionColor = AliasHandler.resolveFaction(factionColor);
        for (Player player_ : activeMap.getPlayers().values()) {
            if (Objects.equals(factionColor, player_.getFaction()) ||
                    Objects.equals(factionColor, player_.getColor())) {
                return player_.getColor();
            }
        }
        return factionColor;
    }

    @Nullable
    public static String getDamagePath() {
        String tokenPath = ResourceHelper.getInstance().getResourceFromFolder("extra/", "marker_damage.png", "Could not find damage token file");
        if (tokenPath == null) {
            BotLogger.log("Could not find token: marker_damage");
            return null;
        }
        return tokenPath;
    }

    public static void addMirageToTile(Tile tile) {
        HashMap<String, UnitHolder> unitHolders = tile.getUnitHolders();
        if (unitHolders.get(Constants.MIRAGE) == null) {
            Point mirageCenter = new Point(Constants.MIRAGE_POSITION.x + Constants.MIRAGE_CENTER_POSITION.x, Constants.MIRAGE_POSITION.y + Constants.MIRAGE_CENTER_POSITION.y);
            Planet planetObject = new Planet(Constants.MIRAGE, mirageCenter);
            unitHolders.put(Constants.MIRAGE, planetObject);
        }
    }

    public static String getDateRepresentation(long dateInfo) {
        Date date = new Date(dateInfo);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd");
        return simpleDateFormat.format(date);
    }

    public static String getRoleMentionByName(Guild guild,  String roleName) {
        List<Role> roles = guild.getRolesByName(roleName, true);
        if (!roles.isEmpty()){
            return roles.get(0).getAsMention();
        } 
        return "[@" + roleName + "]";
    }

    public static String getColourAsMention(String colour) {
        return switch (colour) {
            case "gray" -> "<@&1061551360870453259>";
            case "red" -> "<@&1061551006019764274>";
            case "purple" -> "<@&1061551337344614462>";
            case "pink" -> "<@&1061551925218267166>";
            case "yellow" -> "<@&1061551266213408788>";
            case "orange" -> "<@&1061551323616657468>";
            case "green" -> "<@&1061551347561926716>";
            case "blue" -> "<@&1061551166397366292>";
            case "black" -> "<@&1061551792128806962>";
            default -> "(" + colour + ")";
        };
    }

    public static String getColourAsMention(Guild guild, String colour) {
        return getRoleMentionByName(guild, colour);
    }

    public static String getSCAsMention(int sc) {
        return switch (sc) {
            case 1 -> "<@&947965021168762890>";
            case 2 -> "<@&947965277633650699>";
            case 3 -> "<@&947965381488807956>";
            case 4 -> "<@&947965493376061441>";
            case 5 -> "<@&947965546660495381>";
            case 6 -> "<@&947965592013525022>";
            case 7 -> "<@&947965632933146634>";
            case 8 -> "<@&947965671394906172>";
            default -> "**" + sc + "**";
        };
    }

    public static String getSCAsMention(Guild guild, int sc) {
        return getRoleMentionByName(guild, getSCName(sc));
    }

    public static String getSCAsMention(Guild guild, String scname) {
        return getRoleMentionByName(guild, scname);
    }

    public static String getSCName(int sc) {
        return switch (sc) {
            case 1 -> "leadership";
            case 2 -> "diplomacy";
            case 3 -> "politics";
            case 4 -> "construction";
            case 5 -> "trade";
            case 6 -> "warfare";
            case 7 -> "technology";
            case 8 -> "imperial";
            default -> "" + sc;
        };
    }

    public static Integer getSCNumber(String sc) {
        return switch (sc.toLowerCase()) {
            case "leadership" -> 1;
            case "diplomacy" -> 2;
            case "politics" -> 3;
            case "construction" -> 4;
            case "trade" -> 5;
            case "warfare" -> 6;
            case "technology" -> 7;
            case "imperial" -> 8;
            default -> 0;
        };
    }

    public static String getFactionIconFromDiscord(String faction) {
        return switch (faction) {
            case "arborec" -> Emojis.Arborec;
            case "argent" -> Emojis.Argent;
            case "cabal" -> Emojis.Cabal;
            case "empyrean" -> Emojis.Empyrean;
            case "ghost" -> Emojis.Ghost;
            case "hacan" -> Emojis.Hacan;
            case "jolnar" -> Emojis.Jolnar;
            case "l1z1x" -> Emojis.L1Z1X;
            case "letnev" -> Emojis.Letnev;
            case "yssaril" -> Emojis.Yssaril;
            case "mahact" -> Emojis.Mahact;
            case "mentak" -> Emojis.Mentak;
            case "muaat" -> Emojis.Muaat;
            case "naalu" -> Emojis.Naalu;
            case "naaz" -> Emojis.Naaz;
            case "nekro" -> Emojis.Nekro;
            case "nomad" -> Emojis.Nomad;
            case "saar" -> Emojis.Saar;
            case "sardakk" -> Emojis.Sardakk;
            case "sol" -> Emojis.Sol;
            case "titans" -> Emojis.Titans;
            case "winnu" -> Emojis.Winnu;
            case "xxcha" -> Emojis.Xxcha;
            case "yin" -> Emojis.Yin;
            case "lazax" -> Emojis.Lazax;
            case "keleres" -> Emojis.Keleres;
            default -> "";
        };
    }

    public static String getPlanetEmoji(String planet) {
        return switch (planet.toLowerCase()) {
            case "mr" -> Emojis.MecatolRex;
            case "hopesend" -> Emojis.HopesEnd;
            case "primor" -> Emojis.Primor;
            case "meharxull" -> Emojis.PlanetMeharXull;
            case "perimeter" -> Emojis.PlanetPerimeter;
            case "archonvail" -> Emojis.PlanetArchonVail;
            case "semlore" -> Emojis.SemLord;
            default -> Emojis.SemLor;
        };
    }

    public static String getPlanetRepresentationPlusEmojis (String planet) {
        String planetProper = Mapper.getPlanetRepresentations().get(planet);
        return Helper.getPlanetEmoji(planet) + " " + (Objects.isNull(planetProper) ? planet : planetProper);
    }

    /**
     * Takes an emoji's name string and returns its full name including ID.
     * @emojiName the name of the emoji as entered on the Emoji section of the server
     * @return the name of the emoji including ID
     */
    public static String getEmojiFromDiscord(String emojiName) {
        return switch (emojiName.toLowerCase()) {
            //EXPLORATION
            case "hfrag" -> Emojis.HFrag;
            case "cfrag" -> Emojis.CFrag;
            case "ifrag" -> Emojis.IFrag;
            case "ufrag" -> Emojis.UFrag;
            case "relic" -> Emojis.Relic;
            case "cultural" -> Emojis.Cultural;
            case "industrial" -> Emojis.Industrial;
            case "hazardous" -> Emojis.Hazardous;
            case "frontier" -> Emojis.Frontier;

            //CARDS
            case "sc1" -> Emojis.SC1;
            case "sc2" -> Emojis.SC2;
            case "sc3" -> Emojis.SC3;
            case "sc4" -> Emojis.SC4;
            case "sc5" -> Emojis.SC5;
            case "sc6" -> Emojis.SC6;
            case "sc7" -> Emojis.SC7;
            case "sc8" -> Emojis.SC8;
            case "sc1back" -> Emojis.SC1Back;
            case "sc2back" -> Emojis.SC2Back;
            case "sc3back" -> Emojis.SC3Back;
            case "sc4back" -> Emojis.SC4Back;
            case "sc5back" -> Emojis.SC5Back;
            case "sc6back" -> Emojis.SC6Back;
            case "sc7back" -> Emojis.SC7Back;
            case "sc8back" -> Emojis.SC8Back;
            case "actioncard" -> Emojis.ActionCard;
            case "agenda" -> Emojis.Agenda;
            case "pn" -> Emojis.PN;
            
            //OBJECTIVES
            case "secretobjective" -> Emojis.SecretObjective;
            case "public1" -> Emojis.Public1;
            case "public2" -> Emojis.Public2;
            case "public1alt" -> Emojis.Public1alt;
            case "public2alt" -> Emojis.Public2alt;
            case "secretobjectivealt" -> Emojis.SecretObjectiveAlt;

            //COMPONENTS
            case "tg" -> Emojis.tg;
            case "comm" -> Emojis.comm;
            case "sleeper" -> Emojis.Sleeper;
            case "sleeperb" -> Emojis.SleeperB;
            
            //UNITS
            case "warsun" -> Emojis.warsun;
            case "spacedock" -> Emojis.spacedock;
            case "pds" -> Emojis.pds;
            case "mech" -> Emojis.mech;
            case "infantry" -> Emojis.infantry;
            case "flagship" -> Emojis.flagship;
            case "fighter" -> Emojis.fighter;
            case "dreadnought" -> Emojis.dreadnought;
            case "destroyer" -> Emojis.destroyer;
            case "carrier" -> Emojis.carrier;
            case "cruiser" -> Emojis.cruiser;

            //LEADERS - AGENTS
            case "arborecagent" -> Emojis.ArborecAgent;
            case "argentagent" -> Emojis.ArgentAgent;
            case "cabalagent" -> Emojis.CabalAgent;
            case "ghostagent" -> Emojis.GhostAgent;
            case "empyreanagent" -> Emojis.EmpyreanAgent;
            case "hacanagent" -> Emojis.HacanAgent;
            case "jolnaragent" -> Emojis.JolnarAgent;
            case "keleresagent" -> Emojis.KeleresAgent;
            case "l1z1xagent" -> Emojis.L1z1xAgent;
            case "letnevagent" -> Emojis.LetnevAgent;
            case "mahactagent" -> Emojis.MahactAgent;
            case "mentakagent" -> Emojis.MentakAgent;
            case "muaatagent" -> Emojis.MuaatAgent;
            case "naaluagent" -> Emojis.NaaluAgent;
            case "naazagent" -> Emojis.NaazAgent;
            case "nekroagent" -> Emojis.NekroAgent;
            case "nomadagentartuno" -> Emojis.NomadAgentArtuno;
            case "nomadagentmercer" -> Emojis.NomadAgentMercer;
            case "nomadagentthundarian" -> Emojis.NomadAgentThundarian;
            case "sardakkagent" -> Emojis.SardakkAgent;
            case "saaragent" -> Emojis.SaarAgent;
            case "solagent" -> Emojis.SolAgent;
            case "titansagent" -> Emojis.TitansAgent;
            case "winnuagent" -> Emojis.WinnuAgent;
            case "xxchaagent" -> Emojis.XxchaAgent;
            case "yinagent" -> Emojis.YinAgent;
            case "yssarilagent" -> Emojis.YssarilAgent;
            
            //LEADERS - COMMANDERS
            case "arboreccommander" -> Emojis.ArborecCommander;
            case "argentcommander" -> Emojis.ArgentCommander;
            case "cabalcommander" -> Emojis.CabalCommander;
            case "ghostcommander" -> Emojis.GhostCommander;
            case "empyreancommander" -> Emojis.EmpyreanCommander;
            case "hacancommander" -> Emojis.HacanCommander;
            case "jolnarcommander" -> Emojis.JolnarCommander;
            case "kelerescommander" -> Emojis.KeleresCommander;
            case "l1z1xcommander" -> Emojis.L1z1xCommander;
            case "letnevcommander" -> Emojis.LetnevCommander;
            case "mahactcommander" -> Emojis.MahactCommander;
            case "mentakcommander" -> Emojis.MentakCommander;
            case "muaatcommander" -> Emojis.MuaatCommander;
            case "naalucommander" -> Emojis.NaaluCommander;
            case "naazcommander" -> Emojis.NaazCommander;
            case "nekrocommander" -> Emojis.NekroCommander;
            case "nomadcommander" -> Emojis.NomadCommander;
            case "sardakkcommander" -> Emojis.SardakkCommander;
            case "saarcommander" -> Emojis.SaarCommander;
            case "solcommander" -> Emojis.SolCommander;
            case "titanscommander" -> Emojis.TitansCommander;
            case "winnucommander" -> Emojis.WinnuCommander;
            case "xxchacommander" -> Emojis.XxchaCommander;
            case "yincommander" -> Emojis.YinCommander;
            case "yssarilcommander" -> Emojis.YssarilCommander;

            //LEADERS - HEROES
            case "arborechero" -> Emojis.ArborecHero;
            case "argenthero" -> Emojis.ArgentHero;
            case "cabalhero" -> Emojis.CabalHero;
            case "ghosthero" -> Emojis.GhostHero;
            case "empyreanhero" -> Emojis.EmpyreanHero;
            case "hacanhero" -> Emojis.HacanHero;
            case "jolnarhero" -> Emojis.JolnarHero;
            case "keleresherokuuasi" -> Emojis.KeleresHeroKuuasi;
            case "keleresheroodlynn" -> Emojis.KeleresHeroOdlynn;
            case "keleresheroharka" -> Emojis.KeleresHeroHarka;
            case "l1z1xhero" -> Emojis.L1z1xHero;
            case "letnevhero" -> Emojis.LetnevHero;
            case "mahacthero" -> Emojis.MahactHero;
            case "mentakhero" -> Emojis.MentakHero;
            case "muaathero" -> Emojis.MuaatHero;
            case "naaluhero" -> Emojis.NaaluHero;
            case "naazhero" -> Emojis.NaazHero;
            case "nekrohero" -> Emojis.NekroHero;
            case "nomadhero" -> Emojis.NomadHero;
            case "sardakkhero" -> Emojis.SardakkHero;
            case "saarhero" -> Emojis.SaarHero;
            case "solhero" -> Emojis.SolHero;
            case "titanshero" -> Emojis.TitansHero;
            case "winnuhero" -> Emojis.WinnuHero;
            case "xxchahero" -> Emojis.XxchaHero;
            case "yinhero" -> Emojis.YinHero;
            case "yssarilhero" -> Emojis.YssarilHero;

            //OTHER
            case "whalpha" -> Emojis.WHalpha;
            case "whbeta" -> Emojis.WHbeta;
            case "whgamma" -> Emojis.WHgamma;
            case "influence" -> Emojis.influence;
            case "resources" -> Emojis.resources;
            case "legendaryplanet" -> Emojis.LegendaryPlanet;
            case "cybernetictech" -> Emojis.CyberneticTech;
            case "propulsiontech" -> Emojis.PropulsionTech;
            case "biotictech" -> Emojis.BioticTech;
            case "warfaretech" -> Emojis.WarfareTech;

            default -> "";
        };
    }

    public static String getGamePing(SlashCommandInteractionEvent event, Map activeMap) {
        return getGamePing(event.getGuild(), activeMap);
    }

    public static String getGamePing(Guild guild, Map activeMap) {
        String categoryForPlayers = "";
        if (guild != null) {
            for (Role role : guild.getRoles()) {
                if (activeMap.getName().equals(role.getName().toLowerCase())) {
                    categoryForPlayers = role.getAsMention();
                }
            }
        }
        return categoryForPlayers;
    }

    public static String getPlayerPing(Player player) {
        User userById = MapGenerator.jda.getUserById(player.getUserID());
        if (userById == null) {
            return "";
        }
        String mention = userById.getAsMention();
        if (player.getUserID() == "154000388121559040") {
            mention += " " + Emoji.fromFormatted(Emojis.BortWindow);
        }
        return mention;
    }

    public static String getPlayerRepresentation(Player player) {
        StringBuilder sb = new StringBuilder(Helper.getFactionIconFromDiscord(player.getFaction()));
        sb.append(" ").append(Helper.getPlayerPing(player));
        if (player.getColor() != null) {
            sb.append(" _").append(getColourAsMention(player.getColor())).append("_");
        }
        return sb.toString();
    }

    public static String getPlayerRepresentation(Guild guild, Player player) {
        StringBuilder sb = new StringBuilder(Helper.getFactionIconFromDiscord(player.getFaction()));
        sb.append(" ").append(Helper.getPlayerPing(player));
        if (player.getColor() != null) {
            sb.append(" _").append(getColourAsMention(guild, player.getColor())).append("_");
        }
        return sb.toString();
    }

    @Nullable
    public static String getPlayerRepresentation(GenericCommandInteractionEvent event, Player player) {
        Boolean privateGame = FoWHelper.isPrivateGame(event);
        if (privateGame != null && privateGame){
            return getColourAsMention(event.getGuild(), player.getColor());
        }
        if (event == null) {
            return getPlayerRepresentation(player);
        }
        if (MapManager.getInstance().getUserActiveMap(event.getUser().getId()).isCommunityMode()) {
            return getColourAsMention(event.getGuild(), player.getColor());
        }
        return getPlayerRepresentation(event.getGuild(), player);
    }

    @Nullable
    public static String getPlayerRepresentation(SlashCommandInteractionEvent event, Player player) {
        Boolean privateGame = FoWHelper.isPrivateGame(event);
        if (privateGame != null && privateGame){
            return getColourAsMention(event.getGuild(), player.getColor());
        }
        if (event == null) {
            return getPlayerRepresentation(player);
        }
        if (MapManager.getInstance().getUserActiveMap(event.getUser().getId()).isCommunityMode()) {
            return getColourAsMention(event.getGuild(), player.getColor());
        }
        return getPlayerRepresentation(event.getGuild(), player);
    }

    @Nullable
    public static String getPlayerRepresentation(ButtonInteractionEvent event, Player player) {
        Boolean privateGame = FoWHelper.isPrivateGame(event);
        if (privateGame != null && privateGame){
            return getColourAsMention(event.getGuild(), player.getColor());
        }
        if (event == null) {
            return getPlayerRepresentation(player);
        }
        if (MapManager.getInstance().getUserActiveMap(event.getUser().getId()).isCommunityMode()) {
            return getColourAsMention(event.getGuild(), player.getColor());
        }
        return getPlayerRepresentation(event.getGuild(), player);
    }
    
    public static String getFactionLeaderEmoji(String faction, Leader leader) {
        return getEmojiFromDiscord(faction + leader.getId() + leader.getName());
    }

    public static String getFactionLeaderEmoji(Player player, Leader leader) {
        return getFactionLeaderEmoji(player.getFaction(), leader);
    }
    
    public static String getLeaderRepresentation(String faction, Leader leader, boolean includeTitle, boolean includeAbility, boolean includeUnlockCondition) {
        String leaderID = faction + leader.getId() + leader.getName();

        String leaderRep =  Mapper.getLeaderRepresentations().get(leaderID.toString());
        if (leaderRep == null) {
            BotLogger.log("Invalid `leaderID=" + leaderID.toString() + "` caught within `Helper.getLeaderRepresentation`");
            return leader.getId();
        }

        //leaderID = 0:LeaderName ; 1:LeaderTitle ; 2:BacksideTitle/HeroAbility ; 3:AbilityWindow ; 4:AbilityText 
        String[] leaderRepSplit = leaderRep.split(";");
        String leaderName = leaderRepSplit[0];
        String leaderTitle = leaderRepSplit[1];
        String heroAbilityName = leaderRepSplit[2];
        String leaderAbilityWindow = leaderRepSplit[3];
        String leaderAbilityText = leaderRepSplit[4];
        String leaderUnlockCondition = leaderRepSplit[5];
        
        StringBuilder representation = new StringBuilder();
        representation.append(getFactionLeaderEmoji(faction, leader)).append(" **").append(leaderName).append("**");
        if (includeTitle) representation.append(": ").append(leaderTitle); //add title
        if (includeAbility && leader.getId().equals(Constants.HERO)) representation.append(" - ").append("__**").append(heroAbilityName).append("**__"); //add hero ability name
        if (includeAbility) representation.append(" - *").append(leaderAbilityWindow).append("* ").append(leaderAbilityText); //add ability
        if (includeUnlockCondition) representation.append(" *Unlock:* ").append(leaderUnlockCondition);

        return representation.toString();
    }
    
    public static String getLeaderRepresentation(Player player, String leader, boolean includeTitle, boolean includeAbility) {
        return getLeaderRepresentation(player.getFaction(), player.getLeader(leader), includeTitle, includeAbility, false);
    }

    public static String getLeaderRepresentation(Player player, Leader leader, boolean includeTitle, boolean includeAbility) {
        return getLeaderRepresentation(player.getFaction(), leader, includeTitle, includeAbility, false);
    }
    
    public static String getLeaderShortRepresentation(Player player, Leader leader) {
        return getLeaderRepresentation(player.getFaction(), leader, false, false, false);
    }

    public static String getLeaderMediumRepresentation(Player player, Leader leader) {
        return getLeaderRepresentation(player.getFaction(), leader, true, false, false);
    }

    public static String getLeaderFullRepresentation(Player player, Leader leader) {
        return getLeaderRepresentation(player.getFaction(), leader, true, true, false);
    }

    public static String getLeaderLockedRepresentation(Player player, Leader leader) {
        return getLeaderRepresentation(player.getFaction(), leader, false, false, true);
    }

    public static String getSCEmojiFromInteger(Integer strategy_card) {
        String scEmojiName = "SC" + String.valueOf(strategy_card);
        return Helper.getEmojiFromDiscord(scEmojiName);
    }
    
    public static String getSCBackEmojiFromInteger(Integer strategy_card) {
        String scEmojiName = "SC" + String.valueOf(strategy_card) + "Back";
        return Helper.getEmojiFromDiscord(scEmojiName);
    }

    public static void isCCCountCorrect(SlashCommandInteractionEvent event, Map map, String color) {
        int ccCount = getCCCount(map, color);
        informUserCCOverLimit(event, map, color, ccCount);
    }

    public static int getCCCount(Map map, String color) {
        int ccCount = 0;
        if (color == null){
            return 0;
        }
        HashMap<String, Tile> tileMap = map.getTileMap();
        for (java.util.Map.Entry<String, Tile> tileEntry : tileMap.entrySet()) {
            Tile tile = tileEntry.getValue();
            boolean hasCC = AddCC.hasCC(null, color, tile);
            if (hasCC) {
                ccCount++;
            }
        }
        String factionColor = AliasHandler.resolveColor(color.toLowerCase());
        factionColor = AliasHandler.resolveFaction(factionColor);
        for (Player player_ : map.getPlayers().values()) {
            if (Objects.equals(factionColor, player_.getFaction()) ||
                    Objects.equals(factionColor, player_.getColor())) {
                ccCount += player_.getStrategicCC();
                ccCount += player_.getTacticalCC();
                ccCount += player_.getFleetCC();
                break;
            } else if ("mahact".equals(player_.getFaction())){
                for (String color_ : player_.getMahactCC()) {
                    if (factionColor.equals(color_)){
                        ccCount++;
                    }
                }
            }
        }
        return ccCount;
    }

    private static void informUserCCOverLimit(SlashCommandInteractionEvent event, Map map, String color, int ccCount) {
        boolean ccCountIsOver = ccCount > 16;
        if (ccCountIsOver) {
            Player player = null;
            String factionColor = AliasHandler.resolveColor(color.toLowerCase());
            factionColor = AliasHandler.resolveFaction(factionColor);
            for (Player player_ : map.getPlayers().values()) {
                if (Objects.equals(factionColor, player_.getFaction()) ||
                        Objects.equals(factionColor, player_.getColor())) {
                    player = player_;
                }
            }
            String msg = getGamePing(event, map) + " ";
            if (player != null) {
                msg += getFactionIconFromDiscord(player.getFaction()) + " " + player.getFaction() + " ";
                msg += getPlayerPing(player) + " ";
            }
            msg += "(" + color + ") is over CC limit. CC used: " + ccCount;
            MessageHelper.replyToMessage(event, msg);
        }
    }

    /**
     * @param map : ti4.map.Map object
     * @return String : TTS/TTPG Map String
     */
    public static String getMapString(Map map) {
        List<String> tilePositions = new ArrayList<String>();
        tilePositions.add("0a");
        if (map.getPlayerCountForMap() == 6) {
            tilePositions.addAll(MapStringMapper.mapFor6Player);
        } else if (map.getPlayerCountForMap() == 8) {
            tilePositions.addAll(MapStringMapper.mapFor8Player);
        } else {
            return new String("at the current time, `/game get_map_string` is only supported for 6 and 8 player games");
        }
        List<String> sortedTilePositions = tilePositions.stream().sorted().collect(Collectors.toList());
        
        HashMap<String, Tile> tileMap = new HashMap<>(map.getTileMap());
        StringBuilder sb = new StringBuilder();
        for (String position : sortedTilePositions) {
            Boolean missingTile = true;
            for (Tile tile : tileMap.values()){
                if (tile.getPosition().equals(position)){
                    String tileID = AliasHandler.resolveStandardTile(tile.getTileID()).toUpperCase();
                    if (position.equalsIgnoreCase("0a") && tileID.equalsIgnoreCase("18")) { //Mecatol Rex in Centre Position
                        //do nothing!
                    } else if (position.equalsIgnoreCase("0a") && !tileID.equalsIgnoreCase("18")) { //Something else is in the Centre Position
                        sb.append("{").append(tileID).append("}");
                    } else {
                        sb.append(tileID);
                    }
                    missingTile = false;
                    break;
                } 
            }
            if (missingTile) {
                sb.append("-1");
            }
            sb.append(" ");
        }     
        return sb.toString().trim();
    }

    public static HashMap<String, Integer> getLastEntryInHashMap(LinkedHashMap<String, Integer> linkedHashMap) {
        int count = 1;
        for (java.util.Map.Entry<String, Integer> it : linkedHashMap.entrySet()) {
            if (count == linkedHashMap.size()) {
                HashMap<String, Integer> lastEntry = new HashMap<String, Integer>();
                lastEntry.put(it.getKey(), it.getValue());
                return lastEntry;
            }
            count++;
        }
        return null;
    }

    /**
     * @param text string to add spaces on the left
     * @param length minimum length of string
     * @return left padded string
     */
    public static String leftpad(String text, int length) {
        return String.format("%" + length + "." + length + "s", text);
    }
    
    /**
     * @param text string to add spaces on the right
     * @param length minimum length of string
     * @return right padded string
     */
    public static String rightpad(String text, int length) {
        return String.format("%-" + length + "." + length + "s", text);
    }

    public static void checkThreadLimitAndArchive(Guild guild) {
        int threadCount = guild.getThreadChannels().size();
        int closeCount = 5;
        if (threadCount > 995) {
            BotLogger.log("`Helper.checkThreadLimitAndArchive:` Thread count is too high ( " + threadCount + " ) - auto-archiving  " + closeCount + " threads:");
            BotLogger.log(ListOldChannels.getOldThreadsMessage(guild, closeCount));
            ArchiveOldThreads.archiveOldThreads(guild, closeCount);
        }
    }
}
