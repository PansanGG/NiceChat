package pansangg.nicechat;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class Config {
    public static String                DEFAULT_MESSAGE;

    public static boolean               UNIQUE_MESSAGES_ENABLED;
    public static Map<String, String>   UNIQUE_MESSAGES;

    public static boolean               PF_ENABLED;
    public static String                PF_REPLACING_CHAR;
    public static Pattern               PF_REGEX;
    public static List<String>          PF_BYPASS_PLAYERS;

    public static boolean               PF_PUNISHMENT_ENABLED;
    public static int                   PF_PUNISHMENT_MAX_COUNT;
    public static String                PF_PUNISHMENT_COMMAND;

    public static void load(UnrealConfig conf) {
        DEFAULT_MESSAGE = (String) conf.get("default-message");

        UNIQUE_MESSAGES_ENABLED = (boolean) conf.getDot("unique-messages.enabled");
        UNIQUE_MESSAGES = (Map<String,String>) conf.get("unique-messages");
        UNIQUE_MESSAGES.remove("enabled");

        PF_ENABLED = (boolean) conf.getDot("profanity-filter.enabled");
        PF_REPLACING_CHAR = (String) conf.getDot("profanity-filter.replacing-char");
        PF_BYPASS_PLAYERS = (List<String>) conf.getDot("profanity-filter.bypass-players");
        PF_REGEX = Pattern.compile((String) conf.getDot("profanity-filter.regex"), Pattern.CASE_INSENSITIVE);

        PF_PUNISHMENT_ENABLED = (boolean) conf.getDot("profanity-filter.punishment.enabled");
        PF_PUNISHMENT_MAX_COUNT = ((Number) conf.getDot("profanity-filter.punishment.max-count")).intValue();
        PF_PUNISHMENT_COMMAND = (String) conf.getDot("profanity-filter.punishment.cmd");
    }
}