package pansangg.nicechat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.entity.Player;

import java.util.Date;

public interface Message {
    Date getCreatedDate();
    Component sendMessage(Player receiver);
}
