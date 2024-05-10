package pansangg.nicechat;

import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class UnrealConfig extends HashMap<String, Object> {
    private static final Yaml yaml = new Yaml();

    private final File file;

    public UnrealConfig(JavaPlugin plugin, String filename) {
        file = Paths.get(plugin.getDataFolder().getPath(), filename).toFile();
        if (!file.exists()) plugin.saveResource(filename, false);
        reload();
    }

    public void reload() {
        try {
            clear();
            putAll(yaml.load(new FileInputStream(file)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void save() {
        try {
            yaml.dump(this, new FileWriter(file, StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<String, Object> clone() {
        return new HashMap<>(this);
    }

    public Object getDot(String dot_key) {
        Map<String, Object> data = clone();

        String[] dotted = dot_key.split("\\.");

        int m = dotted.length;
        int i = 0;
        for (String s : dotted) {
            if (data.containsKey(s)) {
                if (i >= m - 1) return data.get(s);
                data = (Map<String, Object>) data.get(s);
            } else {
                throw new RuntimeException("key not found");
            }
            i++;
        }

        return null;
    }

    public void setDot(String dot_key, Object value) {
        Map<String, Object> data = clone();

        String[] dotted = dot_key.split("\\.");

        int m = dotted.length;
        int i = 0;
        for (String s : dotted) {
            if (data.containsKey(s)) {
                if (i >= m - 1) {
                    data.put(s, value);
                    return;
                }
                data = (Map<String, Object>) data.get(s);
            } else {
                throw new RuntimeException("key not found");
            }
            i++;
        }
    }
}
