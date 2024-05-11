package pansangg.nicechat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.luckperms.api.model.user.User;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.w3c.dom.Text;

import java.util.Date;

public class SystemMessage implements Message {
    private Date created_date;
    private Component text;

    public SystemMessage(Component text) {
        this.text = text;
        this.created_date = new Date();
    }

    public SystemMessage(Component text, Date created_dater) {
        this.text = text;
        this.created_date = created_dater;
    }

    public Component getText() {
        return text;
    }

    public Date getCreatedDate() {
        return created_date;
    }

    @Override
    public Message clone() {
        return new SystemMessage(text, created_date);
    }

    public Component sendMessage(Player receiver) {
        return text;
    }
}
