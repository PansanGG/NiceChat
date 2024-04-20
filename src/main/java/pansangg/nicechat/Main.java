package pansangg.nicechat;

import me.clip.placeholderapi.PlaceholderAPI;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Main extends JavaPlugin implements Listener {
    public static Main me;

    public UnrealConfig conf;
    public NiceChatCommand command;

    @Override
    public void onEnable() {
        me = this;

        if (!checkPlaceholderAPI()) return;  // проверяем наличие плейсхолдер апи
        if (!checkLuckPerms()) return;       // проверяем наличие лакпермс

        conf = new UnrealConfig(this, "config.yml");

        command = new NiceChatCommand("nicechat");
        command.register(this);

        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // The most empty place...
    }

    public boolean checkPlaceholderAPI() {
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") == null) {
            getLogger().info("PlaceholderAPI not found! Disabling plugin...");
            getServer().getPluginManager().disablePlugin(me);
            return false;
        }
        return true;
    }

    public boolean checkLuckPerms() {
        if (getServer().getPluginManager().getPlugin("LuckPerms") == null) {
            getLogger().info("LuckPerms not found!");
            getLogger().info("Disabling plugin...");
            getServer().getPluginManager().disablePlugin(me);
            return false;
        } else {
            RegisteredServiceProvider<LuckPerms> provider = getServer().getServicesManager().getRegistration(LuckPerms.class);
            LuckPerms api;
            if (provider != null) {
                api = provider.getProvider();
                getServer().getPluginManager().registerEvents(new LuckPermsInit(api), this);
                getLogger().info("Successfully connected to LuckPerms!");
            } else {
                getLogger().info("Can't connect to LuckPerms!");
                getLogger().info("Disabling plugin...");
                return false;
            }
        }
        return true;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMessage(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        String new_message;
        if ((boolean) conf.get("chat.unique-messages.enabled")) {
            String group;
            String prefix;
            String suffix;

            User user = LuckPermsInit.lp.getPlayerAdapter(Player.class).getUser(e.getPlayer());
            group = user.getPrimaryGroup();
            prefix = user.getCachedData().getMetaData().getPrefix() == null ? "" : user.getCachedData().getMetaData().getPrefix() + " ";
            suffix = user.getCachedData().getMetaData().getSuffix() == null ? "" : " " + user.getCachedData().getMetaData().getSuffix();

            new_message = (String) conf.get("chat." + group);
            new_message = new_message.replace("{PLAYER}", prefix + p.getName() + suffix);
            new_message = new_message.replace("{MESSAGE}", filterMessage(e.getPlayer(), e.getMessage()));

            e.setCancelled(true);
        } else {
            new_message = (String) conf.get() // TODO: дописать
        }
        getServer().broadcastMessage(translateHexCodes(new_message));
    }

    public String filterMessage(Player p, String content) {
        List<String> immunity_players = (List<String>) conf.get("options.profanity-filter.immunity-players");
        boolean immunity = immunity_players.contains(p.getName());
        if (immunity || !(boolean) conf.get("options.profanity-filter.enabled")) return content;

//        List<String> profanity = List.of("сука", "пидор", "еблан", "долб", "хуй", "еба", "уёб", "уеб", "пизд", "муд", "хуё",
//                "хуя", "манд", "бля", "ебё", "еби", "ебо", "пенис", "письк", "сись", "fuck", "dick", "penis",
//                "ass", "suck", "shit", "bitch", "arse", "bollocks", "bugger", "cunt", "damn", "SuperSausages");

        int count = 0;
        String message = PlaceholderAPI.setPlaceholders(p, content);
//        getLogger().info("message: " + message);
        String character = (String) conf.get("options.profanity-filter.replacing-char");
//        getLogger().info("character: " + character);
        message = profanityFilter(message, character);
//        getLogger().info("final_message: " + message);

        if (!(boolean) conf.get("options.profanity-filter.overdoing.enabled")) return message;
        String cmd = (String) conf.get("options.profanity-filter.overdoing.cmd");
        if (count > (int) conf.get("options.profanity-filter.overdoing.max-count")) {
            getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
                getServer().dispatchCommand(getServer().getConsoleSender(), cmd.replace("{PLAYER}", p.getName()));
            });
        }
        return message;
    }

    public static String profanityFilter(String content, String character) {
        Pattern pattern = Pattern.compile("(?<![а-яё])(?:(?:(?:у|[нз]а|(?:хитро|не)?вз?[ыьъ]|с[ьъ]|(?:и|ра)[зс]ъ?|(?:о[тб]|п[оа]д)[ьъ]?|(?:\\S(?=[а-яё]))+?[оаеи-])-?)?(?:[её](?:б(?!о[рй]|рач)|п[уа](?:ц|тс))|и[пб][ае][тцд][ьъ]).*?|(?:(?:н[иеа]|ра[зс]|[зд]?[ао](?:т|дн[оа])?|с(?:м[еи])?|а[пб]ч)-?)?ху(?:[яйиеёю]|л+и(?!ган)).*?|бл(?:[эя]|еа?)(?:[дт][ьъ]?)?|\\S*?(?:п(?:[иеё]зд|ид[аое]?р|ед(?:р(?!о)|[аое]р|ик)|охую)|бля(?:[дбц]|тс)|[ое]ху[яйиеё]|хуйн).*?|(?:о[тб]?|про|на|вы)?м(?:анд(?:[ауеыи](?:л(?:и[сзщ])?[ауеиы])?|ой|[ао]в.*?|юк(?:ов|[ауи])?|е[нт]ь|ища)|уд(?:[яаиое].+?|е?н(?:[ьюия]|ей))|[ао]л[ао]ф[ьъ](?:[яиюе]|[еёо]й))|елд[ауые].*?|ля[тд]ь|(?:[нз]а|по)х)(?![а-яё])");
        Matcher matcher = pattern.matcher(content);
        StringBuilder buffer = new StringBuilder();
        while (matcher.find()) {
            System.out.println(matcher.group() + " " + matcher.start() + " " + matcher.end());
            matcher.appendReplacement(buffer, character.repeat(matcher.end() - matcher.start()));
        }
        System.out.println(buffer);
        return buffer.toString();
    }

    public static String translateHexCodes(String from) {
        Pattern pattern = Pattern.compile("&#[a-fA-F0-9]{6}");
        Matcher matcher = pattern.matcher(from);
        while (matcher.find()) {
            String hexCode = from.substring(matcher.start(), matcher.end());
            String replaceSharp = hexCode.replace("&#", "x");
            char[] ch = replaceSharp.toCharArray();
            StringBuilder builder = new StringBuilder();
            for (char c : ch)
                builder.append("&").append(c);
            from = from.replace(hexCode, builder.toString());
            matcher = pattern.matcher(from);
        }
        return ChatColor.translateAlternateColorCodes('&', from);
    }
}
