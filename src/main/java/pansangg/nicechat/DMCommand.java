package pansangg.nicechat;

import net.kyori.adventure.text.TextComponent;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

import static org.bukkit.Bukkit.getOnlinePlayers;
import static org.bukkit.Bukkit.getPlayer;


public class DMCommand implements CommandExecutor, TabCompleter {
    private final String name;
    private PluginCommand command;

    public DMCommand(String name) {
        this.name = name;
    }

    public void register(JavaPlugin plugin) { // метод для регистрации команды LOL kek 4eburek
        command = plugin.getCommand(name);
        command.setTabCompleter(this);
        command.setExecutor(this);
    }

    public List<String> onTabComplete(CommandSender sender,
                                      Command command,
                                      String alias,
                                      String[] args) {
        List<String> completes = new ArrayList<>();

        if (args.length == 1) {
            for (Player p : getOnlinePlayers()) {
                completes.add(p.getName());
            }
        }

        return completes;
    }

    public boolean onCommand(CommandSender sender,
                             Command command,
                             String alias,
                             String[] args) {
        if (args.length < 2) return true;
        Player author = (Player) sender;
        Player receiver = getPlayer(args[0]);
        StringJoiner joiner = new StringJoiner(" ");
        for (String arg : args) {
            joiner.add(arg);
        }
        String message = joiner.toString();
        author.sendMessage(Main.conf.DM_AUTHOR_FORMAT.replaceText((b) -> {
                                b.matchLiteral("{RECEIVER}").replacement(receiver.getName());
                                b.matchLiteral("{MESSAGE}").replacement(message);
                            }).toString());
        receiver.sendMessage(Main.conf.DM_AUTHOR_FORMAT.replaceText((b) -> {
                                b.matchLiteral("{AUTHOR}").replacement(author.getName());
                                b.matchLiteral("{MESSAGE}").replacement(message);
                            }).toString());
        return true;
    }
}