package pansangg.nicechat;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.model.user.User;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Date;

public class ChatMessage implements Message {
    private final String id;
    private final Date created_date;
    private final OfflinePlayer author;

    private String text;
    private Date edited_date;

    public ChatMessage(String id, OfflinePlayer author, String text) {
        this.id = id;

        this.author = author;
        this.text = text;

        this.created_date = new Date();
        this.edited_date = new Date();
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setEditedDate(Date edited_date) {
        this.edited_date = edited_date;
    }

    public String getId() {
        return id;
    }

    public OfflinePlayer getAuthor() {
        return author;
    }

    public String getText() {
        return text;
    }

    public Date getEditedDate() {
        return edited_date;
    }

    public Date getCreatedDate() {
        return created_date;
    }

    public Component sendMessage(Player receiver) {
        if (author.getPlayer() != null) {
            Player p = author.getPlayer();

            TextComponent message_text;

            if (Main.conf.UNIQUE_MESSAGES_ENABLED && Main.has_luckperms) {
                User user = Main.luckperms.getPlayerAdapter(Player.class).getUser(p);
                String group = user.getPrimaryGroup();
                String prefix = user.getCachedData().getMetaData().getPrefix() == null ? "" : user.getCachedData().getMetaData().getPrefix() + " ";
                String suffix = user.getCachedData().getMetaData().getSuffix() == null ? "" : " " + user.getCachedData().getMetaData().getSuffix();

                message_text = Main.setPlaceholders(p, Main.conf.UNIQUE_MESSAGES.getOrDefault(group, Main.conf.DEFAULT_MESSAGE));
                message_text = (TextComponent) message_text.replaceText((b) -> {
                    b.matchLiteral("{PLAYER}").replacement(prefix + p.getName() + suffix);
                });
            } else {
                message_text = Main.setPlaceholders(p, Main.conf.DEFAULT_MESSAGE);
                message_text = (TextComponent) message_text.replaceText((b) -> {
                    b.matchLiteral("{PLAYER}").replacement(p.getName());
                });
            }

            message_text = Main.me.replaceButtons(receiver, this, message_text);

            message_text = (TextComponent) message_text.replaceText((b) -> {
                b.matchLiteral("{MESSAGE}").replacement(
                    p.hasPermission("nicechat.chat.color") ?
                        Main.translateHexCodes(Main.me.filterMessage(p, text)) :
                        Main.me.filterMessage(p, text));
            });

            return message_text;
        }

        return Component.empty();
    }
    
    @Override
    public boolean equals(Object other) {
        if (other instanceof ChatMessage msg) {
            if (msg.getId().equals(getId())) {
                return true;
            }
        }
        return false;
    }
}
