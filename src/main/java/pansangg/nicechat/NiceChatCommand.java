package pansangg.nicechat;

import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

import static org.bukkit.Bukkit.getPluginManager;


public class NiceChatCommand implements CommandExecutor, TabCompleter, Listener {
    public static String NAME = "nicechat";
    public JavaPlugin plugin;

    public NiceChatCommand(JavaPlugin plugin) {
        this.plugin = plugin;
        PluginCommand cmd = plugin.getCommand(NAME);
        cmd.setTabCompleter(this);
        cmd.setExecutor(this);
        getPluginManager().registerEvents(this, plugin);
    }

    public List<String> onTabComplete(CommandSender sender,
                                      Command command,
                                      String alias,
                                      String[] args) {
        if (args.length == 1) {
            return List.of("reload");
        }
        return new ArrayList<>();
    }

    public boolean onCommand(CommandSender sender,
                             Command command,
                             String alias,
                             String[] args) {
        Player p = (Player) sender;
        switch (args[0]) {
            case ("reload"):
                if (!p.hasPermission("nicechat.reload")) {
                    Main.me.conf.get("messages.missing-permissions");
                    return true;
                }
                p.sendMessage("Config is reloading...");
                plugin.reloadConfig();
                Main.me.conf = plugin.getConfig();
                p.sendMessage("Reloaded!");
                break;
        }
        return true;
    }
}
