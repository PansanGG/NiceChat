package pansangg.nicechat;

import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.plugin.java.JavaPlugin;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class Config {
    public TextComponent DEFAULT_MESSAGE;

    public boolean UNIQUE_MESSAGES_ENABLED;
    public Map<String, TextComponent> UNIQUE_MESSAGES;

    public boolean PF_ENABLED;
    public String PF_REPLACING_CHAR;
    public LinkedList<Pattern> PF_REGEX;
    public List<String> PF_BYPASS_PLAYERS;

    public boolean PF_PUNISHMENT_ENABLED;
    public int PF_PUNISHMENT_MAX_COUNT;
    public String PF_PUNISHMENT_COMMAND;

    public boolean AB_ENABLED;
    public TextComponent AB_DELETE_BUTTON;
    public TextComponent AB_EDIT_BUTTON;
    public int AB_MESSAGES_CACHE;

    public TextComponent MSG_DELETE_HINT;
    public TextComponent MSG_EDIT_HINT;
    public TextComponent MSG_SPOILER_HINT;
    public TextComponent MSG_DELETE_FINISHED;
    public TextComponent MSG_EDIT_FINISHED;
    public TextComponent MSG_RELOAD;
    public TextComponent MSG_RELOAD_FINISHED;

    public boolean SP_ENABLED;
    public String SP_REPLACING_CHAR;
    public Pattern SP_REGEX;

    private UnrealConfig conf;
    private File regex_file;

    public Config(JavaPlugin plugin) {
        conf = new UnrealConfig(plugin, "config.yml");

        regex_file = new File(plugin.getDataFolder(), "regex.txt");
        if (!regex_file.exists()) {
            try {
                Files.copy(plugin.getResource("regex.txt"), regex_file.toPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        reload();
    }

    public LinkedList<Pattern> readRegex() {
        LinkedList<Pattern> patterns = new LinkedList<>();

        try {
            for (String line : Files.readAllLines(regex_file.toPath())) {
                if (!line.trim().isEmpty() && !line.startsWith("#")) {
                    patterns.add(Pattern.compile(line, Pattern.UNICODE_CASE + Pattern.CASE_INSENSITIVE));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return patterns;
    }

    public TextComponent fromLegacy(String text) {
        return LegacyComponentSerializer.legacySection().deserialize(Main.translateHexCodes(text));
    }

    public void reload() {
        conf.reload();

        DEFAULT_MESSAGE = fromLegacy((String) conf.get("default-message"));

        UNIQUE_MESSAGES_ENABLED = (boolean) conf.getDot("unique-messages.enabled");
        UNIQUE_MESSAGES = new HashMap<>();

        for (Map.Entry<String, String> en : ((Map<String, String>) conf.get("unique-messages")).entrySet()) {
            if (en.getKey().equals("enabled")) continue;
            UNIQUE_MESSAGES.put(en.getKey(), fromLegacy(en.getValue()));
        }

        PF_ENABLED = (boolean) conf.getDot("profanity-filter.enabled");
        PF_REPLACING_CHAR = Main.translateHexCodes((String) conf.getDot("profanity-filter.replacing-char"));
        PF_BYPASS_PLAYERS = (List<String>) conf.getDot("profanity-filter.bypass-players");
        PF_REGEX = readRegex();

        PF_PUNISHMENT_ENABLED = (boolean) conf.getDot("profanity-filter.punishment.enabled");
        PF_PUNISHMENT_MAX_COUNT = ((Number) conf.getDot("profanity-filter.punishment.max-count")).intValue();
        PF_PUNISHMENT_COMMAND = (String) conf.getDot("profanity-filter.punishment.cmd");

        AB_ENABLED = (boolean) conf.getDot("action-buttons.enabled");
        AB_DELETE_BUTTON = fromLegacy((String) conf.getDot("action-buttons.delete-button"));
        AB_EDIT_BUTTON = fromLegacy((String) conf.getDot("action-buttons.edit-button"));
        AB_MESSAGES_CACHE = (int) conf.getDot("action-buttons.messages-cache");

        MSG_DELETE_HINT = fromLegacy((String) conf.getDot("messages.delete-hint"));
        MSG_EDIT_HINT = fromLegacy((String) conf.getDot("messages.edit-hint"));
        MSG_SPOILER_HINT = fromLegacy((String) conf.getDot("messages.spoiler-hint"));
        MSG_DELETE_FINISHED = fromLegacy((String) conf.getDot("messages.delete-finished"));
        MSG_EDIT_FINISHED = fromLegacy((String) conf.getDot("messages.edit-finished"));
        MSG_RELOAD = fromLegacy((String) conf.getDot("messages.reload"));
        MSG_RELOAD_FINISHED = fromLegacy((String) conf.getDot("messages.reload-finished"));

        SP_ENABLED = (boolean) conf.getDot("spoilers.enabled");
        SP_REPLACING_CHAR = Main.translateHexCodes((String) conf.getDot("spoilers.replacing-char"));
        SP_REGEX = Pattern.compile(Main.translateHexCodes((String) conf.getDot("spoilers.regex")));
    }
}