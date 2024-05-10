package pansangg.nicechat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.luckperms.api.model.user.User;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.w3c.dom.Text;

import java.util.Date;

public class SystemMessage implements Message {
    private final Date created_date;
    private final Component text;
    private final OfflinePlayer receiver;

    public SystemMessage(Component text, OfflinePlayer receiver) {
        this.text = text;
        this.created_date = new Date();
        this.receiver = receiver;
    }

    public Component getText() {
        return text;
    }

    public Date getCreatedDate() {
        return created_date;
    }

    public OfflinePlayer getReceiver() {
        return receiver;
    }

    public Component sendMessage(Player receiver) {
        return text;
    }
}
