package pansangg.nicechat;

import net.kyori.adventure.text.Component;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;


public class NiceChatCommand implements CommandExecutor, TabCompleter {
    private final String name;
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
        List<String> completes = new ArrayList<>();

        if (args.length == 1) {
            if (sender.hasPermission("nicechat.command.reload")) {
                completes.add("reload");
            }

            if (sender.hasPermission("nicechat.chat.delete") ||
                    sender.hasPermission("nicechat.chat.delete_all")) {
                completes.add("delete");
            }

            if (sender.hasPermission("nicechat.chat.edit") ||
                    sender.hasPermission("nicechat.chat.edit_all")) {
                completes.add("edit");
            }
        }

        return completes;
    }

    public boolean onCommand(CommandSender sender,
                             Command command,
                             String alias,
                             String[] args) {
        Player p = (Player) sender;

        if (args.length >= 1) {
            if (args[0].equals("reload")) {
                Main.me.sendSystemMessage(p, Main.conf.MSG_RELOAD);
                Main.conf.reload();
                Main.me.sendSystemMessage(p, Main.conf.MSG_RELOAD_FINISHED);
            } else if (args[0].equals("delete")) {
                if (args.length >= 2) {
                    String id = args[1];
                    ChatMessage message = Main.me.getMessage(id);

                    if (message != null &&
                            sender.hasPermission("nicechat.chat.delete_all") ||
                            (sender.hasPermission("nicechat.chat.delete") && message.getAuthor().equals(p))) {
                        Main.me.removeMessage(message);
                        Main.me.updateMessages();
                        Main.me.sendSystemMessage(p, Main.conf.MSG_DELETE_FINISHED, 60);
                    }
                }
            } else if (args[0].equals("edit")) {
                if (args.length >= 3) {
                    String id = args[1];
                    String text = String.join(" ", List.of(args).subList(2, args.length));
                    ChatMessage message = Main.me.getMessage(id);

                    if (message != null &&
                            sender.hasPermission("nicechat.chat.edit_all") ||
                            (sender.hasPermission("nicechat.chat.edit") && message.getAuthor().equals(p))) {
                        message.setText(text);
                        Main.me.updateMessages();
                        Main.me.sendSystemMessage(p, Main.conf.MSG_EDIT_FINISHED, 60);
                    }
                }
            }
        }

        return true;
    }
}