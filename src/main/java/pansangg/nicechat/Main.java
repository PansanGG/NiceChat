package pansangg.nicechat;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import net.luckperms.api.LuckPerms;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Main extends JavaPlugin implements Listener {
    public static Main me;
    public static LuckPerms luckperms;
    public static Config conf;
    public static ProtocolManager proto;

    public static boolean has_placeholder_api;
    public static boolean has_luckperms;

    public static Random rand = new Random();

    public NiceChatCommand command;

    public LinkedList<Message> chat_messages;

    public List<String> my_secrets;

    @Override
    public void onEnable() {
        me = this;

        has_placeholder_api = checkPlaceholderAPI();
        has_luckperms = checkLuckPerms();

        command = new NiceChatCommand("nicechat");
        command.register(this);

        conf = new Config(this);

        chat_messages = new LinkedList<>();

        my_secrets = new ArrayList<>();

        proto = ProtocolLibrary.getProtocolManager();
        proto.addPacketListener(new PacketAdapter(
                this,
                ListenerPriority.NORMAL,
                PacketType.Play.Server.SYSTEM_CHAT
        ) {
            @Override
            public void onPacketSending(PacketEvent event) {
                if (!event.getPacketType().equals(PacketType.Play.Server.SYSTEM_CHAT)) return;

                if (conf.AB_ENABLED) {
                    Player player = event.getPlayer();

                    String content = (String) event.getPacket().getModifier().readSafely(0);
                    boolean overlay = (boolean) event.getPacket().getModifier().readSafely(1);

                    if (!overlay) {
                        if (content.length() > 16) {
                            String secret = content.substring(0, 16);
                            String json = content.substring(16);

                            if (my_secrets.contains(secret)) {
                                event.getPacket().getModifier().write(0, json);
                                my_secrets.remove(secret);
                                return;
                            }
                        }

                        chat_messages.add(new SystemMessage(JSONComponentSerializer.json().deserialize(content), player));
                    }
                }
            }
        });

        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Pep soso etity kolot .,/
    }

    public SystemMessage sendSystemMessage(Player player, TextComponent text, int delay) {
        SystemMessage message = new SystemMessage(text, player);
        chat_messages.add(message);

        sendMessage(player, text);

        new BukkitRunnable() {
            public void run() {
                removeMessage(message);
                updateMessages(player);
            }
        }.runTaskLater(this, delay);

        return message;
    }

    public SystemMessage sendSystemMessage(Player player, TextComponent text) {
        SystemMessage message = new SystemMessage(text, player);
        chat_messages.add(message);

        sendMessage(player, text);

        return message;
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
                luckperms = provider.getProvider();
                getLogger().info("Successfully connected to LuckPerms!");
            } else {
                getLogger().info("Can't connect to LuckPerms!");
                return false;
            }
        }
        return true;
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

    private static TextComponent clearChat = Component.text("\n".repeat(1024));

    private void broadcastMessage(TextComponent text) {
        for (Player player : getServer().getOnlinePlayers()) {
            sendMessage(player, text);
        }
    }

    public void broadcastMessage(ChatMessage message) {
        for (Player player : getServer().getOnlinePlayers()) {
            sendMessage(player, message.sendMessage(player));
        }
    }

    private void sendMessage(Player player, Component text) {
        String secret = randomString(16, "1234567890QWERTYUIOPASDFGHJKLZXCVBNM".toCharArray());
        my_secrets.add(secret);

        PacketContainer packet = new PacketContainer(PacketType.Play.Server.SYSTEM_CHAT);
        packet.getModifier().write(0, secret + JSONComponentSerializer.json().serialize(text));
        packet.getModifier().write(1, false);
        proto.sendServerPacket(player, packet);
    }

    private ChatMessage addMessage(Player player, String text) {
        ChatMessage message = new ChatMessage(getId(), player, text);

        if (conf.AB_ENABLED) {
            chat_messages.add(message);

            if (chat_messages.size() > conf.AB_MESSAGES_CACHE) {// TODO: config changing this num
                chat_messages = (LinkedList<Message>) chat_messages.subList(chat_messages.size() - conf.AB_MESSAGES_CACHE, chat_messages.size());
                chat_messages.sort(Comparator.comparing(Message::getCreatedDate));
            }
        }

        return message;
    }

    public TextComponent replaceButtons(Player viewer, ChatMessage message, TextComponent text) {
        return (TextComponent) text
                .replaceText((b) -> b.matchLiteral("{DELETE}").replacement(
                        viewer.hasPermission("nicechat.chat.delete_all") ||
                        (viewer.hasPermission("nicechat.chat.delete") && message.getAuthor().equals(viewer)) ? conf.AB_DELETE_BUTTON
                            .hoverEvent(HoverEvent.showText(conf.MSG_DELETE_HINT))
                            .clickEvent(ClickEvent.runCommand("/nicechat delete "+message.getId())) : Component.empty()
                ))
                .replaceText((b) -> b.matchLiteral("{EDIT}").replacement(
                        viewer.hasPermission("nicechat.chat.edit_all") ||
                        (viewer.hasPermission("nicechat.chat.edit") && message.getAuthor().equals(viewer)) ? conf.AB_EDIT_BUTTON
                            .hoverEvent(HoverEvent.showText(conf.MSG_EDIT_HINT))
                            .clickEvent(ClickEvent.suggestCommand("/nicechat edit "+message.getId()+" ")) : Component.empty()
                ));
    }

    public void clearMessages() {
        chat_messages.clear();
        broadcastMessage(clearChat);
    }

    public void updateMessages() {
        chat_messages.sort(Comparator.comparing(Message::getCreatedDate));

        for (Player player : getServer().getOnlinePlayers()) {
            updateMessages(player);
        }
    }

    public ChatMessage getMessage(String id) {
        for (Message msg : chat_messages)
            if (msg instanceof ChatMessage chat && chat.getId().equals(id))
                return chat;
        return null;
    }

    public void updateMessages(Player player) {
        TextComponent text = clearChat;

        for (Message msg : chat_messages) {
            if (msg instanceof ChatMessage || (
                    msg instanceof SystemMessage sys &&
                    sys.getReceiver().equals(player))) {
                text = text.append(Component.newline().append(msg.sendMessage(player)));
            }
        }

        sendMessage(player, text);
    }

    public void removeMessage(Message msg) {
        chat_messages.removeIf((o) -> o.equals(msg));
    }

    public String getId() {
        String id;

        do {
            id = randomString(5, "qwertyuiopasdfghjklzxcvbnm".toCharArray());
        } while (getMessage(id) != null);

        return id;
    }

    public String randomString(int length, char[] chars) {
        int[] ints = rand.ints(length, 0, chars.length).toArray();
        StringBuilder builder = new StringBuilder();
        for (int a : ints) builder.append(chars[a]);
        return builder.toString();
    }

    public static String setPlaceholders(Player player, String text) {
        return has_placeholder_api ? PlaceholderAPI.setPlaceholders(player, text) : text;
    }

    public static TextComponent setPlaceholders(Player player, TextComponent text) {
        return has_placeholder_api ? (TextComponent) JSONComponentSerializer.json().deserialize(PlaceholderAPI.setPlaceholders(player, JSONComponentSerializer.json().serialize(text))) : text;
    }

    public String filterMessage(Player p, String content) {
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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMessage(AsyncPlayerChatEvent e) {
        broadcastMessage(addMessage(e.getPlayer(), e.getMessage()));
        e.setCancelled(true);
    }
}