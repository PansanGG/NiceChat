package pansangg.nicechat;

import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

import static org.bukkit.Bukkit.getPluginManager;


public class NiceChatCommand implements CommandExecutor, TabCompleter {
    private String name;
    private PluginCommand command;

    public NiceChatCommand(String name) {
        this.name = name;
    }

    public void register(JavaPlugin plugin) { // метод для регистрации команды LOL
        command = plugin.getCommand(name);
        command.setTabCompleter(this);
        command.setExecutor(this);
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

        if (args.length > 0) {
            if (args[0].equals("reload")) {
                if (!p.hasPermission("nicechat.reload")) {
                    Main.me.conf.getDot("messages.missing-permissions");
                    return true;
                }

                p.sendMessage("Config is reloading...");

                Main.me.conf.reload();
                Config.load(Main.me.conf);

                p.sendMessage("Reloaded!");
            }
        }

        return true;
    }
}
