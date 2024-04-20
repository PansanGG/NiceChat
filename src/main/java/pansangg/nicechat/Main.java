package pansangg.nicechat;

import me.clip.placeholderapi.PlaceholderAPI;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class Main extends JavaPlugin implements Listener {
    public static Main me;
    public FileConfiguration conf;

    private String filterMessage(Player p, String content) {
        List<String> immunity_players = (List<String>) conf.get("options.profanity-filter.immunity-players");
        boolean immunity = immunity_players.contains(p.getName());
        if (immunity || !(boolean) conf.get("options.profanity-filter.enabled")) return content;

        List<String> profanity = List.of("сука", "пидор", "еблан", "долб", "хуй", "еба", "уёб", "уеб", "пизд", "муд", "хуё",
                "хуя", "манд", "бля", "ебё", "еби", "ебо", "пенис", "письк", "сись", "fuck", "dick", "penis",
                "ass", "suck", "shit", "bitch", "arse", "bollocks", "bugger", "cunt", "damn", "SuperSausages");

        int count = 0;
        String message = PlaceholderAPI.setPlaceholders(p, content);
        getLogger().info("message: " + message);
        String character = (String) conf.get("options.profanity-filter.replacing-char");
        getLogger().info("character: " + character);
        message = ProfanityFilter.filter(message, character);
        getLogger().info("final_message: " + message);

        if (!(boolean) conf.get("options.profanity-filter.overdoing.enabled")) return message;
        String cmd = (String) conf.get("options.profanity-filter.overdoing.cmd");
        if (count > (int) conf.get("options.profanity-filter.overdoing.max-count")) {
            getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
                getServer().dispatchCommand(getServer().getConsoleSender(), cmd.replace("{PLAYER}", p.getName()));
            });
        }
        return message;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        me = this;
        conf = getConfig();

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") == null) {
            getLogger().info("PlaceholderAPI not found! Disabling plugin...");
            getServer().getPluginManager().disablePlugin(me);
        }

        if (getServer().getPluginManager().getPlugin("LuckPerms") == null) {
            getLogger().info("LuckPerms not found!");
            getLogger().info("Disabling plugin...");
            getServer().getPluginManager().disablePlugin(me);
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
            }
        }

        new NiceChatCommand(me);

        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // The most empty place...
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
            new_message = (String) conf.get()
        }
        getServer().broadcastMessage(ColorUtils.color(new_message));
    }
}
