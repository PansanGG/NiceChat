package pansangg.nicechat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.entity.Player;

import java.util.Date;

public interface Message extends Cloneable {
    Date getCreatedDate();
    Message clone();
    Component sendMessage(Player receiver);
}
