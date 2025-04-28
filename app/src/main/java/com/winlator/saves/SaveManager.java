package com.winlator.saves;

import android.content.Context;
import android.util.Log;

import com.winlator.container.Container;
import com.winlator.container.ContainerManager;
import com.winlator.core.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class SaveManager {
    private final File savesDir;
    private final ContainerManager containerManager;  // Add this to handle containers

    public SaveManager(Context context) {
        this.savesDir = new File(context.getFilesDir(), "saves");
        this.containerManager = new ContainerManager(context);  // Initialize ContainerManager
        if (!savesDir.exists() && !savesDir.mkdirs()) {
            throw new RuntimeException("Failed to create saves directory: " + savesDir.getAbsolutePath());
        }
    }

    public File getSaveJsonFile(Save save) {
        return new File(savesDir, save.getTitle() + ".json");
    }

    public ArrayList<Save> getSaves() {
        ArrayList<Save> saves = new ArrayList<>();
        File[] saveFiles = savesDir.listFiles((dir, name) -> name.endsWith(".json"));
        if (saveFiles != null) {
            for (File file : saveFiles) {
                Save save = loadSave(file);  // Use a method to load the save with its container
                if (save != null) {
                    saves.add(save);
                    Log.d("SaveManager", "Loaded Save: " + save.path);
                }
            }
        }
        return saves;
    }

    private Save loadSave(File file) {
        // Read the JSON data from the file
        String content = FileUtils.readString(file);
        try {
            JSONObject saveData = new JSONObject(content);
            int containerId = saveData.optInt("ContainerID", -1);
            Container container = null;
            if (containerId != -1) {
                container = containerManager.getContainerById(containerId);  // Retrieve the associated container
            }
            return new Save(containerManager, container, file);  // Pass the ContainerManager when creating the Save object
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Save getSaveById(int id) {
        ArrayList<Save> saves = getSaves();
        for (Save save : saves) {
            if (save.id == id) {
                Log.d("SaveManager", "Save found: " + save.path);
                return save;
            }
        }
        return null; // Save with the given ID not found
    }

    public void addSave(String title, String path, Container container) throws IOException {
        int id = generateNewSaveId(); // Generate a unique ID for the new save
        File saveFile = new File(savesDir, title + ".json");
        if (saveFile.exists()) {
            throw new IOException("Save with this name already exists");
        }

        try {
            JSONObject saveData = new JSONObject();
            saveData.put("ID", id);
            saveData.put("Title", title);
            saveData.put("Path", path);

            if (container != null) {
                saveData.put("ContainerID", container.id);
            }

            FileUtils.writeString(saveFile, saveData.toString());
        } catch (JSONException e) {
            throw new IOException("Failed to create save JSON", e);
        }
    }

    public void updateSave(Save save, String newTitle, String newPath, Container newContainer) throws IOException {
        Log.d("SaveManager", "Updating Save: Title=" + newTitle + ", Path=" + newPath); // Debug log
        save.update(newTitle, newPath, newContainer);
        save.saveData();  // Save the updated data

        // Rename the file if the title changes
        File newSaveFile = new File(savesDir, newTitle + ".json");
        if (!save.file.getName().equals(newSaveFile.getName())) {
            if (newSaveFile.exists()) {
                throw new IOException("Save with this name already exists");
            }
            if (!save.file.renameTo(newSaveFile)) {
                throw new IOException("Failed to rename save file to " + newSaveFile.getAbsolutePath());
            }
        }
    }

    public void transferSave(Save save, Container newContainer) throws IOException {
        if (save.container != null && !save.container.equals(newContainer)) {
            // Get the source path (current save path)
            File srcPath = new File(save.path);

            // Construct the destination path within the new container
            File destRootDir = new File(newContainer.getRootDir(), ".wine/drive_c");

            // Calculate the relative path from the drive_c directory in the source container
            String driveCRoot = new File(save.container.getRootDir(), ".wine/drive_c").getAbsolutePath();
            String relativePath = srcPath.getAbsolutePath().substring(driveCRoot.length());

            // Construct the final destination path
            File destPath = new File(destRootDir, relativePath);

            // Ensure that the destination parent directories are created
            if (!destPath.getParentFile().exists() && !destPath.getParentFile().mkdirs()) {
                throw new IOException("Failed to create directories for " + destPath.getAbsolutePath());
            }

            // Log the paths for debugging
            Log.d("SaveManager", "Cloning files from " + srcPath.getAbsolutePath() + " to " + destPath.getAbsolutePath());

            // Copy the files from the source path to the destination path
            if (FileUtils.copy(srcPath, destPath)) {
                Log.d("SaveManager", "Files successfully cloned.");
            } else {
                throw new IOException("Failed to clone files from " + srcPath.getAbsolutePath() + " to " + destPath.getAbsolutePath());
            }

            // Generate a unique name for the cloned save
            String newTitle = generateUniqueTitle(save.getTitle());

            // Register the cloned save as a new save
            addSave(newTitle, destPath.getAbsolutePath(), newContainer);
        } else if (save.container == null) {
            throw new IOException("Current container is null.");
        }
    }

    private String generateUniqueTitle(String baseTitle) {
        ArrayList<Save> saves = getSaves();
        int count = 1;
        String newTitle = baseTitle;

        // Find a unique title by appending an integer
        while (saveExists(newTitle, saves)) {
            newTitle = baseTitle + " (" + count + ")";
            count++;
        }

        return newTitle;
    }

    private boolean saveExists(String title, ArrayList<Save> saves) {
        for (Save save : saves) {
            if (save.getTitle().equalsIgnoreCase(title)) {
                return true;
            }
        }
        return false;
    }




    public void removeSave(Save save) {
        if (save.file.exists() && !save.file.delete()) {
            throw new RuntimeException("Failed to delete save file: " + save.file.getAbsolutePath());
        }
    }

    private int generateNewSaveId() {
        // Generate a unique ID by finding the highest existing ID and adding 1
        int maxId = 0;
        ArrayList<Save> saves = getSaves();
        for (Save save : saves) {
            if (save.id > maxId) {
                maxId = save.id;
            }
        }
        return maxId + 1;
    }
}
