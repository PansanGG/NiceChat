package pansangg.nicechat;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class Config {
    public String DEFAULT_MESSAGE;

    public boolean UNIQUE_MESSAGES_ENABLED;
    public Map<String, String> UNIQUE_MESSAGES;

    public boolean PF_ENABLED;
    public String PF_REPLACING_CHAR;
    public LinkedList<Pattern> PF_REGEX;
    public List<String> PF_BYPASS_PLAYERS;

    public boolean PF_PUNISHMENT_ENABLED;
    public int PF_PUNISHMENT_MAX_COUNT;
    public String PF_PUNISHMENT_COMMAND;

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
                    patterns.add(Pattern.compile(line, Pattern.CASE_INSENSITIVE));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return patterns;
    }

    public void reload() {
        conf.reload();

        DEFAULT_MESSAGE = (String) conf.get("default-message");

        UNIQUE_MESSAGES_ENABLED = (boolean) conf.getDot("unique-messages.enabled");
        UNIQUE_MESSAGES = (Map<String, String>) conf.get("unique-messages");
        UNIQUE_MESSAGES.remove("enabled");

        PF_ENABLED = (boolean) conf.getDot("profanity-filter.enabled");
        PF_REPLACING_CHAR = (String) conf.getDot("profanity-filter.replacing-char");
        PF_BYPASS_PLAYERS = (List<String>) conf.getDot("profanity-filter.bypass-players");
        PF_REGEX = readRegex();

        PF_PUNISHMENT_ENABLED = (boolean) conf.getDot("profanity-filter.punishment.enabled");
        PF_PUNISHMENT_MAX_COUNT = ((Number) conf.getDot("profanity-filter.punishment.max-count")).intValue();
        PF_PUNISHMENT_COMMAND = (String) conf.getDot("profanity-filter.punishment.cmd");
    }
}