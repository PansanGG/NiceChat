package pansangg.nicechat;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.model.user.User;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Date;

public class ChatMessage implements Message {
    private String id;
    private Date created_date;
    private OfflinePlayer author;

    private String text;
    private String original_text;
    private Date edited_date;

    public ChatMessage(String id, OfflinePlayer author, String text, String original_text) {
        this.id = id;

        this.author = author;
        this.text = text;
        this.original_text = original_text;

        this.created_date = new Date();
        this.edited_date = new Date();
    }

    public ChatMessage(String id, OfflinePlayer author, String text, String original_text, Date created_date, Date edited_date) {
        this.id = id;

        this.author = author;
        this.text = text;
        this.original_text = original_text;

        this.created_date = created_date;
        this.edited_date = edited_date;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setOriginalText(String original_text) {
        this.original_text = original_text;
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

    @Override
    public Message clone() {
        return new ChatMessage(id, author, text, original_text, created_date, edited_date);
    }

    public String getOriginalText() {
        return original_text;
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

            message_text = (TextComponent) message_text.replaceText((b) ->
                b.matchLiteral("{MESSAGE}").replacement(
                    LegacyComponentSerializer.legacySection().deserialize(
                            p.hasPermission("nicechat.chat.color") ?
                                Main.translateHexCodes(Main.me.filterMessage(p, text)) :
                                Main.me.filterMessage(p, text))
                            .hoverEvent(Main.conf.MSG_SPOILER_HINT)
                            .clickEvent(ClickEvent.runCommand("/nicechat spoiler "+getId())))
            );

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
