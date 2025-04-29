package com.winlator.container;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.winlator.core.FileUtils;
import com.winlator.core.StringUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
    import java.util.ArrayList;
import java.util.Iterator;
    import java.util.List;
    import java.util.UUID;

public class Shortcut {
    public final Container container;
    public final String name;
    public final String path;
    public final Bitmap icon;
    public final File file;
    public final File iconFile;
    public final String wmClass;
    private final JSONObject extraData = new JSONObject();
    private Bitmap coverArt; // Changed to private to use getter method
    private String customCoverArtPath; // Path to custom cover art

    private static final String COVER_ART_DIR = "app_data/cover_arts/"; // Removed leading "/" to keep it relative

    public Shortcut(Container container, File file) {
        this.container = container;
        this.file = file;

        String execArgs = "";
        Bitmap icon = null;
        File iconFile = null;
        String wmClass = "";

        File[] iconDirs = {container.getIconsDir(64), container.getIconsDir(48), container.getIconsDir(32), container.getIconsDir(16)};
        String section = "";

        int index;
        for (String line : FileUtils.readLines(file)) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue; // Skip empty lines and comments
            if (line.startsWith("[")) {
                section = line.substring(1, line.indexOf("]"));
            }
            else {
                index = line.indexOf("=");
                if (index == -1) continue;
                String key = line.substring(0, index);
                String value = line.substring(index+1);

                if (section.equals("Desktop Entry")) {
                    if (key.equals("Exec")) execArgs = value;
                    if (key.equals("Icon")) {
                        for (File iconDir : iconDirs) {
                            iconFile = new File(iconDir, value+".png");
                            if (iconFile.isFile()){
                                icon = BitmapFactory.decodeFile(iconFile.getPath());
                                break;
                            }
                        }
                    }
                    if (key.equals("StartupWMClass")) wmClass = value;
                }
                else if (section.equals("Extra Data")) {
                    try {
                        extraData.put(key, value);
                    }
                    catch (JSONException e) {
                        Log.e("Shortcut", "Failed to put extra data: " + e);
                    }
                }
            }
        }

        this.name = FileUtils.getBasename(file.getPath());
        this.icon = icon;
        this.iconFile = iconFile;
        this.path = StringUtils.unescape(execArgs.substring(execArgs.lastIndexOf("wine ") + 4));
        this.wmClass = wmClass;

        this.customCoverArtPath = getExtra("customCoverArtPath");

        // Load cover art if available
        loadCoverArt();

        Container.checkObsoleteOrMissingProperties(extraData);
    }

    private void loadCoverArt() {
        // Check for custom cover art first
        if (customCoverArtPath != null && !customCoverArtPath.isEmpty()) {
            File customCoverArtFile = new File(customCoverArtPath);
            if (customCoverArtFile.isFile()) {
                this.coverArt = BitmapFactory.decodeFile(customCoverArtFile.getPath());
                return; // Exit if custom cover art is loaded
            }
        }

        // Fallback to standard cover art location
        File defaultCoverArtFile = new File(COVER_ART_DIR, this.name + ".png");
        if (defaultCoverArtFile.isFile()) {
            this.coverArt = BitmapFactory.decodeFile(defaultCoverArtFile.getPath());
        }
    }

    // Getters and setters for coverArt and customCoverArtPath
    public Bitmap getCoverArt() {
        return coverArt;
    }

    public void setCoverArt(Bitmap coverArt) {
        this.coverArt = coverArt;
    }

    public String getCustomCoverArtPath() {
        return customCoverArtPath;
    }

    public void setCustomCoverArtPath(String customCoverArtPath) {
        this.customCoverArtPath = customCoverArtPath;
        putExtra("customCoverArtPath", customCoverArtPath); // Save the custom cover art path to extra data
        saveData(); // Save immediately to ensure persistence
        Log.d("Shortcut", "Set and saved custom cover art path: " + customCoverArtPath); // Add a log for debugging
    }

    public String getExtra(String name) {
        return getExtra(name, "");
    }

    public String getExtra(String name, String fallback) {
        try {
            return extraData.has(name) ? extraData.getString(name) : fallback;
        }
        catch (JSONException e) {
            return fallback;
        }
    }

    public void putExtra(String name, String value) {
        try {
            if (value != null) {
                extraData.put(name, value);
            }
            else extraData.remove(name);
        }
        catch (JSONException e) {
            Log.e("Shortcut", "Failed to put extra: " + e);
        }
    }

    public void saveData() {
        String content = "[Desktop Entry]\n";
        for (String line : FileUtils.readLines(file)) {
            if (line.contains("[Extra Data]")) break;
            if (!line.contains("[Desktop Entry]") && !line.isEmpty()) content += line+"\n";
        }

        if (extraData.length() > 0) {
            content += "\n[Extra Data]\n";
            Iterator<String> keys = extraData.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                try {
                    content += key + "=" + extraData.getString(key) + "\n";
                }
                catch (JSONException e) {
                    Log.e("Shortcut", "Failed to save extra data: " + e);
                }
            }
        }

        FileUtils.writeString(file, content);
    }
}
