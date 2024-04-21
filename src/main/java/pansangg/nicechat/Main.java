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
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Main extends JavaPlugin implements Listener {
    public static Main me;
    public static LuckPerms lp;

    public UnrealConfig conf;
    public NiceChatCommand command;

    @Override
    public void onEnable() {
        me = this;

        if (!checkPlaceholderAPI()) return;
        if (!checkLuckPerms()) return;

        conf = new UnrealConfig(this, "config.yml");

        Config.load(conf);

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

            if (provider != null) {
                lp = provider.getProvider();
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

        if (Config.UNIQUE_MESSAGES_ENABLED) {
            User user = lp.getPlayerAdapter(Player.class).getUser(e.getPlayer());
            String group = user.getPrimaryGroup();
            String prefix = user.getCachedData().getMetaData().getPrefix() == null ? "" : user.getCachedData().getMetaData().getPrefix() + " ";
            String suffix = user.getCachedData().getMetaData().getSuffix() == null ? "" : " " + user.getCachedData().getMetaData().getSuffix();

            new_message = translateHexCodes(Config.UNIQUE_MESSAGES.get(group));
            new_message = new_message.replace("{PLAYER}", prefix + p.getName() + suffix);
            new_message = new_message.replace("{MESSAGE}", filterMessage(e.getPlayer(), e.getMessage()));
        } else {
            new_message = translateHexCodes(Config.DEFAULT_MESSAGE);
            new_message = new_message.replace("{PLAYER}", p.getName());
            new_message = new_message.replace("{MESSAGE}", filterMessage(e.getPlayer(), e.getMessage()));
        }

        e.setCancelled(true);

        getServer().broadcastMessage(new_message);
    }

    public String filterMessage(Player p, String content) {
        if (Config.PF_BYPASS_PLAYERS.contains(p.getName()) || !Config.PF_ENABLED) return content;

        String output = content;

        int count = 0;

        output = PlaceholderAPI.setPlaceholders(p, output);

        Matcher matcher = Config.PF_REGEX.matcher(output);
        StringBuilder buffer = new StringBuilder();

        while (matcher.find()) {
            matcher.appendReplacement(buffer, Config.PF_REPLACING_CHAR.repeat(matcher.end() - matcher.start()));
            count++;
        }

        if (count > 0) output = buffer.toString();

        if (Config.PF_PUNISHMENT_ENABLED) {
            if (count > Config.PF_PUNISHMENT_MAX_COUNT) {
                new BukkitRunnable() {
                    public void run() {
                        getServer().dispatchCommand(
                                getServer().getConsoleSender(),
                                Config.PF_PUNISHMENT_COMMAND.replace("{PLAYER}", p.getName()));
                    }
                }.runTask(this);
            }
        }

        return output;
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