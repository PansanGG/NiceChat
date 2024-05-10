package pansangg.nicechat;

import me.clip.placeholderapi.PlaceholderAPI;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Main extends JavaPlugin implements Listener {
    public static Main me;
    public static LuckPerms lp;
    public static Config conf;

    public static boolean has_placeholder_api;
    public static boolean has_luckperms;

    public NiceChatCommand command;

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

    @Override
    public void onEnable() {
        me = this;

        has_placeholder_api = checkPlaceholderAPI();
        has_luckperms = checkLuckPerms();

        command = new NiceChatCommand("nicechat");
        command.register(this);

        conf = new Config(this);

        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Pep soso etity kolot .,/
    }

    public boolean checkPlaceholderAPI() {
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") == null) {
            getLogger().info("PlaceholderAPI not found!");
            return false;
        }
        return true;
    }

    public boolean checkLuckPerms() {
        if (getServer().getPluginManager().getPlugin("LuckPerms") == null) {
            getLogger().info("LuckPerms not found!");
            return false;
        } else {
            RegisteredServiceProvider<LuckPerms> provider = getServer().getServicesManager().getRegistration(LuckPerms.class);

            if (provider != null) {
                lp = provider.getProvider();
                getLogger().info("Successfully connected to LuckPerms!");
            } else {
                getLogger().info("Can't connect to LuckPerms!");
                return false;
            }
        }
        return true;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMessage(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        String new_message;

        if (conf.UNIQUE_MESSAGES_ENABLED && has_luckperms) {
            User user = lp.getPlayerAdapter(Player.class).getUser(e.getPlayer());
            String group = user.getPrimaryGroup();
            String prefix = user.getCachedData().getMetaData().getPrefix() == null ? "" : user.getCachedData().getMetaData().getPrefix() + " ";
            String suffix = user.getCachedData().getMetaData().getSuffix() == null ? "" : " " + user.getCachedData().getMetaData().getSuffix();

            new_message = translateHexCodes(conf.UNIQUE_MESSAGES.get(group));
            new_message = new_message.replace("{PLAYER}", translateHexCodes(prefix + p.getName() + suffix));
            new_message = new_message.replace("{MESSAGE}", p.hasPermission("nicechat.chat.color") ? translateHexCodes(filterMessage(e.getPlayer(), e.getMessage())) : filterMessage(e.getPlayer(), e.getMessage()));
        } else {
            new_message = translateHexCodes(conf.DEFAULT_MESSAGE);
            new_message = new_message.replace("{PLAYER}", p.getName());
            new_message = new_message.replace("{MESSAGE}", filterMessage(e.getPlayer(), e.getMessage()));
        }

        e.setCancelled(true);

        getServer().broadcastMessage(new_message);
    }

    public static String setPlaceholders(Player player, String text) {
        return has_placeholder_api ? PlaceholderAPI.setPlaceholders(player, text) : text;
    }

    public String filterMessage(Player p, String content) {
        // profanity filter
        if (conf.PF_BYPASS_PLAYERS.contains(p.getName()) || !conf.PF_ENABLED) return content;

        String output = content;

        int count = 0;

        output = setPlaceholders(p, output);

        for (Pattern pattern : conf.PF_REGEX) {
            Matcher matcher = pattern.matcher(output.toLowerCase().trim());

            while (matcher.find()) {
                output = output.replace(matcher.group(), conf.PF_REPLACING_CHAR.repeat(matcher.end() - matcher.start()));
                count++;
            }
        }

        if (conf.PF_PUNISHMENT_ENABLED) {
            if (count > conf.PF_PUNISHMENT_MAX_COUNT) {
                new BukkitRunnable() {
                    public void run() {
                        getServer().dispatchCommand(
                                getServer().getConsoleSender(),
                                conf.PF_PUNISHMENT_COMMAND.replace("{PLAYER}", p.getName()));
                    }
                }.runTask(this);
            }
        }

//        return output;

//        // spoilers
//        Pattern spoilers = Pattern.compile("||(.*?)||");
//        Matcher spoilers_matcher = spoilers.matcher(output);
//
//        while (spoilers_matcher.find()) {
//            output = output.replace(spoilers_matcher.group(), "#".repeat(spoilers_matcher.end() - spoilers_matcher.start()));
//        }

        return output;
    }
}