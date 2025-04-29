package com.winlator.container;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

// import com.winlator.R;
import com.winlator.contents.ContentProfile;
import com.winlator.contents.ContentsManager;
import com.utkarshdalal.PluviaGoldberg.R;
import com.winlator.box86_64.Box86_64Preset;
import com.winlator.core.Callback;
import com.winlator.core.FileUtils;
import com.winlator.core.OnExtractFileListener;
import com.winlator.core.TarCompressorUtils;
import com.winlator.core.WineInfo;
import com.winlator.core.WineThemeManager;
import com.winlator.xenvironment.ImageFs;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ContainerManager {
    private final ArrayList<Container> containers = new ArrayList<>();
    private int maxContainerId = 0;
    private final File homeDir;
    private final Context context;

    private boolean isInitialized = false; // New flag to track initialization

    public ContainerManager(Context context) {
        this.context = context;
        File rootDir = ImageFs.find(context).getRootDir();
        homeDir = new File(rootDir, "home");
        loadContainers();
        isInitialized = true;
    }

    // Check if the ContainerManager is fully initialized
    public boolean isInitialized() {
        return isInitialized;
    }

    public ArrayList<Container> getContainers() {
        return containers;
    }

    // Load containers from the home directory
    private void loadContainers() {
        containers.clear();
        maxContainerId = 0;

        try {
            File[] files = homeDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        if (file.getName().startsWith(ImageFs.USER+"-")) {
                            Container container = new Container(Integer.parseInt(file.getName().replace(ImageFs.USER+"-", "")));
                            container.setRootDir(new File(homeDir, ImageFs.USER+"-"+container.id));
                            JSONObject data = new JSONObject(FileUtils.readString(container.getConfigFile()));
                            container.loadData(data);
                            containers.add(container);
                            maxContainerId = Math.max(maxContainerId, container.id);
                        }
                    }
                }
            }
        }
        catch (JSONException e) {
            Log.e("ContainerManager", "Failed to load containers: " + e);
        }
    }

    public Context getContext() {
        return context;
    }


    public void activateContainer(Container container) {
        container.setRootDir(new File(homeDir, ImageFs.USER+"-"+container.id));
        File file = new File(homeDir, ImageFs.USER);
        file.delete();
        FileUtils.symlink("./"+ImageFs.USER+"-"+container.id, file.getPath());
    }

    public void createContainerAsync(final JSONObject data, Callback<Container> callback) {
        int id = maxContainerId + 1;
        final Handler handler = new Handler();
        Executors.newSingleThreadExecutor().execute(() -> {
            final Container container = createContainer(id, data);
            handler.post(() -> callback.call(container));
        });
    }
    public Future<Container> createContainerFuture(final JSONObject data) {
        int id = maxContainerId + 1;
        return Executors.newSingleThreadExecutor().submit(() -> createContainer(id, data));
    }
    public Future<Container> createContainerFuture(int id, final JSONObject data) {
        return Executors.newSingleThreadExecutor().submit(() -> createContainer(id, data));
    }
    public Future<Container> createDefaultContainerFuture(WineInfo wineInfo) {
        return createDefaultContainerFuture(wineInfo, getNextContainerId());
    }
    public Future<Container> createDefaultContainerFuture(WineInfo wineInfo, int containerId) {
        String name = "container_" + containerId;
        Log.d("XServerScreen", "Creating container $name");
        String screenSize = Container.DEFAULT_SCREEN_SIZE;
        String envVars = Container.DEFAULT_ENV_VARS;
        String graphicsDriver = Container.DEFAULT_GRAPHICS_DRIVER;
        String dxwrapper = Container.DEFAULT_DXWRAPPER;
        String dxwrapperConfig = "";
        String audioDriver = Container.DEFAULT_AUDIO_DRIVER;
        String wincomponents = Container.DEFAULT_WINCOMPONENTS;
        String drives = "";
        Boolean showFPS = false;
        String cpuList = Container.getFallbackCPUList();
        String cpuListWoW64 = Container.getFallbackCPUListWoW64();
        Boolean wow64Mode = WineInfo.isMainWineVersion(wineInfo.identifier());
        // Boolean wow64Mode = false;
        Byte startupSelection = Container.STARTUP_SELECTION_ESSENTIAL;
        String box86Preset = Box86_64Preset.COMPATIBILITY;
        String box64Preset = Box86_64Preset.COMPATIBILITY;
        String desktopTheme = WineThemeManager.DEFAULT_DESKTOP_THEME;

        JSONObject data = new JSONObject();
        try {
            data.put("name", name);
            data.put("screenSize", screenSize);
            data.put("envVars", envVars);
            data.put("cpuList", cpuList);
            data.put("cpuListWoW64", cpuListWoW64);
            data.put("graphicsDriver", graphicsDriver);
            data.put("dxwrapper", dxwrapper);
            data.put("dxwrapperConfig", dxwrapperConfig);
            data.put("audioDriver", audioDriver);
            data.put("wincomponents", wincomponents);
            data.put("drives", drives);
            data.put("showFPS", showFPS);
            data.put("wow64Mode", wow64Mode);
            data.put("startupSelection", startupSelection);
            data.put("box86Preset", box86Preset);
            data.put("box64Preset", box64Preset);
            data.put("desktopTheme", desktopTheme);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return createContainerFuture(containerId, data);
    }

    public void duplicateContainerAsync(Container container, Runnable callback) {
        final Handler handler = new Handler();
        Executors.newSingleThreadExecutor().execute(() -> {
            duplicateContainer(container);
            handler.post(callback);
        });
    }

    public void removeContainerAsync(Container container, Runnable callback) {
        final Handler handler = new Handler();
        Executors.newSingleThreadExecutor().execute(() -> {
            removeContainer(container);
            handler.post(callback);
        });
    }

    public Container createContainer(int containerId, JSONObject data) {
        try {
            data.put("id", containerId);

            File containerDir = new File(homeDir, ImageFs.USER+"-"+containerId);
            if (!containerDir.mkdirs()) return null;

            Container container = new Container(containerId);
            container.setRootDir(containerDir);
            container.loadData(data);

            boolean isMainWineVersion = !data.has("wineVersion") || WineInfo.isMainWineVersion(data.getString("wineVersion"));
            if (!isMainWineVersion) container.setWineVersion(data.getString("wineVersion"));

            if (!extractContainerPatternFile(container.getWineVersion(), containerDir, null)) {
                FileUtils.delete(containerDir);
                return null;
            }

            container.saveData();
            maxContainerId++;
            containers.add(container);
            return container;
        }
        catch (JSONException e) {
            Log.e("ContainerManager", "Failed to create container: " + e);
        }
        return null;
    }

    private void duplicateContainer(Container srcContainer) {
        int id = maxContainerId + 1;

        File dstDir = new File(homeDir, ImageFs.USER+"-"+id);
        if (!dstDir.mkdirs()) return;

        if (!FileUtils.copy(srcContainer.getRootDir(), dstDir, (file) -> FileUtils.chmod(file, 0771))) {
            FileUtils.delete(dstDir);
            return;
        }

        Container dstContainer = new Container(id);
        dstContainer.setRootDir(dstDir);
        dstContainer.setName(srcContainer.getName()+" ("+context.getString(R.string.copy)+")");
        dstContainer.setScreenSize(srcContainer.getScreenSize());
        dstContainer.setEnvVars(srcContainer.getEnvVars());
        dstContainer.setCPUList(srcContainer.getCPUList());
        dstContainer.setCPUListWoW64(srcContainer.getCPUListWoW64());
        dstContainer.setGraphicsDriver(srcContainer.getGraphicsDriver());
        dstContainer.setDXWrapper(srcContainer.getDXWrapper());
        dstContainer.setDXWrapperConfig(srcContainer.getDXWrapperConfig());
        dstContainer.setAudioDriver(srcContainer.getAudioDriver());
        dstContainer.setWinComponents(srcContainer.getWinComponents());
        dstContainer.setDrives(srcContainer.getDrives());
        dstContainer.setShowFPS(srcContainer.isShowFPS());
        dstContainer.setWoW64Mode(srcContainer.isWoW64Mode());
        dstContainer.setStartupSelection(srcContainer.getStartupSelection());
        dstContainer.setBox86Preset(srcContainer.getBox86Preset());
        dstContainer.setBox64Preset(srcContainer.getBox64Preset());
        dstContainer.setDesktopTheme(srcContainer.getDesktopTheme());
        dstContainer.setRcfileId(srcContainer.getRCFileId());
        dstContainer.setWineVersion(srcContainer.getWineVersion());
        dstContainer.saveData();

        maxContainerId++;
        containers.add(dstContainer);
    }


    private void removeContainer(Container container) {
        if (FileUtils.delete(container.getRootDir())) containers.remove(container);
    }

    public ArrayList<Shortcut> loadShortcuts() {
        ArrayList<Shortcut> shortcuts = new ArrayList<>();
        for (Container container : containers) {
            File desktopDir = container.getDesktopDir();
            File[] files = desktopDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getName().endsWith(".desktop")) shortcuts.add(new Shortcut(container, file));
                }
            }
        }

        shortcuts.sort(Comparator.comparing(a -> a.name));
        return shortcuts;
    }

    public int getNextContainerId() {
        return maxContainerId + 1;
    }

    public boolean hasContainer(int id) {
        for (Container container : containers) if (container.id == id) return true;
        return false;
    }
    public Container getContainerById(int id) {
        for (Container container : containers) if (container.id == id) return container;
        return null;
    }

    private void extractCommonDlls(String srcName, String dstName, JSONObject commonDlls, File containerDir, OnExtractFileListener onExtractFileListener) throws JSONException {
        File srcDir = new File(ImageFs.find(context).getRootDir(), "/opt/wine/lib/wine/"+srcName);
        JSONArray dlnames = commonDlls.getJSONArray(dstName);

        for (int i = 0; i < dlnames.length(); i++) {
            String dlname = dlnames.getString(i);
            File dstFile = new File(containerDir, ".wine/drive_c/windows/"+dstName+"/"+dlname);
            if (onExtractFileListener != null) {
                dstFile = onExtractFileListener.onExtractFile(dstFile, 0);
                if (dstFile == null) continue;
            }
            FileUtils.copy(new File(srcDir, dlname), dstFile);
        }
    }

    public boolean extractContainerPatternFile(String wineVersion, File containerDir, OnExtractFileListener onExtractFileListener) {
        if (WineInfo.isMainWineVersion(wineVersion)) {
            boolean result = TarCompressorUtils.extract(TarCompressorUtils.Type.ZSTD, context.getAssets(), "container_pattern.tzst", containerDir, onExtractFileListener);

            if (result) {
                try {
                    JSONObject commonDlls = new JSONObject(FileUtils.readString(context, "common_dlls.json"));
                    extractCommonDlls("x86_64-windows", "system32", commonDlls, containerDir, onExtractFileListener);
                    extractCommonDlls("i386-windows", "syswow64", commonDlls, containerDir, onExtractFileListener);
                }
                catch (JSONException e) {
                    return false;
                }
            }

            return result;
        }
        else {
//            File installedWineDir = ImageFs.find(context).getInstalledWineDir();
//            WineInfo wineInfo = WineInfo.fromIdentifier(context, wineVersion);
//            String suffix = wineInfo.fullVersion()+"-"+wineInfo.getArch();
//            File file = new File(installedWineDir, "container-pattern-"+suffix+".tzst");
//            return TarCompressorUtils.extract(TarCompressorUtils.Type.ZSTD, file, containerDir, onExtractFileListener);
            ContentsManager contentsManager = new ContentsManager(context);
            contentsManager.syncContents();
            ContentProfile profile = contentsManager.getProfileByEntryName(wineVersion);
            if (profile == null)
                return false;
            File file = ContentsManager.getSourceFile(context, profile, profile.winePrefixPack);
            String suffix = FileUtils.getFileSuffix(file);
            if (suffix.equals("xz") || suffix.equals("txz"))
                return TarCompressorUtils.extract(TarCompressorUtils.Type.XZ, file, containerDir, onExtractFileListener);
            else if (suffix.equals("zst") || suffix.equals("tzst"))
                return TarCompressorUtils.extract(TarCompressorUtils.Type.ZSTD, file, containerDir, onExtractFileListener);
            return false;
        }
    }


//    public boolean extractGraphicsDriverFiles(String driverVersion, File containerDir, OnExtractFileListener onExtractFileListener) {
//        // Log the start of the extraction process
//        Log.d("GraphicsDriverExtraction", "Starting extraction for driver version: " + driverVersion);
//
//        // Access the AssetManager to get the graphics driver from assets
//        AssetManager assetManager = context.getAssets();
//        String fileName = "graphics_driver/turnip-" + driverVersion + ".tzst";
//
//        try (InputStream inputStream = assetManager.open(fileName)) {
//            Log.d("GraphicsDriverExtraction", "Driver file found in assets: " + fileName);
//
//            // Define the destination file path in the container directory
//            File destinationFile = new File(containerDir, "turnip-" + driverVersion + ".tzst");
//
//            // Copy the asset file to the destination
//            try (OutputStream outputStream = new FileOutputStream(destinationFile)) {
//                byte[] buffer = new byte[1024];
//                int length;
//                while ((length = inputStream.read(buffer)) > 0) {
//                    outputStream.write(buffer, 0, length);
//                }
//            }
//
//            // Log the extraction result
//            boolean result = TarCompressorUtils.extract(TarCompressorUtils.Type.ZSTD, destinationFile, containerDir, onExtractFileListener);
//
//            if (result) {
//                Log.d("GraphicsDriverExtraction", "Extraction successful for driver version: " + driverVersion);
//            } else {
//                Log.e("GraphicsDriverExtraction", "Extraction failed for driver version: " + driverVersion);
//            }
//
//            return result;
//        } catch (IOException e) {
//            Log.e("GraphicsDriverExtraction", "Driver file not found in assets: " + fileName, e);
//            return false;
//        }
//    }

    public boolean extractGraphicsDriverFiles(String driverVersion, File imageFsRootDir, OnExtractFileListener onExtractFileListener) {
        // Access the AssetManager to get the graphics driver from assets
        AssetManager assetManager = context.getAssets();
        String assetFileName = "graphics_driver/turnip-" + driverVersion + ".tzst";

        // Log the intended paths for extraction
        Log.d("ContainerManager", "Extracting graphics driver from assets: " + assetFileName);

        try (InputStream inputStream = assetManager.open(assetFileName)) {
            // Define the temporary file path within the imagefs root directory
            File tempFile = new File(imageFsRootDir, "turnip-" + driverVersion + ".tzst");

            // Log the destination path for extraction
            Log.d("ContainerManager", "Copying asset file to temporary location: " + tempFile.getAbsolutePath());

            // Copy the asset file to the temporary location
            try (OutputStream outputStream = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
            }

            // Perform the extraction to the imageFs root directory
            boolean result = TarCompressorUtils.extract(TarCompressorUtils.Type.ZSTD, tempFile, imageFsRootDir, onExtractFileListener);

            // Log the result of the extraction
            if (result) {
                Log.d("ContainerManager", "Extraction succeeded for version: " + driverVersion);
            } else {
                Log.e("ContainerManager", "Extraction failed for version: " + driverVersion);
            }

            // Clean up the copied asset file after extraction
            if (tempFile.exists()) {
                boolean deleted = tempFile.delete();
                if (!deleted) {
                    Log.w("ContainerManager", "Failed to delete temporary asset file: " + tempFile.getAbsolutePath());
                }
            }

            // Return the result of the extraction
            return result;

        } catch (IOException e) {
            // Log the exception if the asset file cannot be opened or copied
            Log.e("ContainerManager", "Failed to extract graphics driver from assets: " + assetFileName, e);
            return false;
        }
    }




    private void logDirectoryContents(File dir) {
        Log.d("ContainerManager", "Directory: " + dir.getAbsolutePath());
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    Log.d("ContainerManager", "File/Dir: " + file.getAbsolutePath() + " (" + (file.isDirectory() ? "Dir" : "File") + ")");
                }
            }
        }
    }

    public void importContainer(File importDir, Runnable callback) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                if (!importDir.exists() || !importDir.isDirectory()) {
                    Log.e("ContainerManager", "Invalid container directory for import: " + importDir.getPath());
                    return;
                }

                // Get the next container ID and set the new container name
                int newContainerId = getNextContainerId();
                String newContainerName = ImageFs.USER + "-" + newContainerId;
                File newContainerDir = new File(homeDir, newContainerName);

                if (newContainerDir.exists()) {
                    Log.e("ContainerManager", "Container directory already exists: " + newContainerDir.getPath());
                    return;
                }

                if (!newContainerDir.mkdirs()) {
                    Log.e("ContainerManager", "Failed to create directory: " + newContainerDir.getPath());
                    return;
                }

                // Copy the files from the import directory to the new container directory
                if (!FileUtils.copy(importDir, newContainerDir, file -> FileUtils.chmod(file, 0771))) {
                    FileUtils.delete(newContainerDir);
                    Log.e("ContainerManager", "Failed to copy container files to: " + newContainerDir.getPath());
                    return;
                }

                // Create the new container object and save its data
                Container newContainer = new Container(newContainerId, this);
                newContainer.setRootDir(newContainerDir);
                newContainer.setName(importDir.getName());
                newContainer.saveData();
                containers.add(newContainer);
                maxContainerId++;

                Log.d("ContainerManager", "Container imported successfully to: " + newContainerDir.getPath());
                // Make sure to run the callback after successful import
                if (callback != null) {
                    callback.run();
                }
            } catch (Exception e) {
                Log.e("ContainerManager", "Failed to import container from: " + importDir.getPath(), e);
            }
        });
    }



    public void exportContainer(Container container, Runnable callback) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // Create the export directory path
                File exportDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Winlator/Backups/Containers");

                if (!exportDir.exists() && !exportDir.mkdirs()) {
                    Log.e("ContainerManager", "Failed to create export directory: " + exportDir.getPath());
                    runOnUiThread(() -> callback.run()); // Close the preloader dialog
                    return;
                }

                File containerDir = container.getRootDir();
                File destinationDir = new File(exportDir, containerDir.getName());

                if (destinationDir.exists()) {
                    Log.e("ContainerManager", "Export directory already exists: " + destinationDir.getPath());
                    runOnUiThread(() -> callback.run()); // Close the preloader dialog
                    return;
        }

                if (!destinationDir.mkdirs()) {
                    Log.e("ContainerManager", "Failed to create directory: " + destinationDir.getPath());
                    runOnUiThread(() -> callback.run()); // Close the preloader dialog
                    return;
                }

                if (!FileUtils.copy(containerDir, destinationDir, file -> FileUtils.chmod(file, 0771))) {
                    Log.e("ContainerManager", "Failed to export some container files to: " + destinationDir.getPath());
                    FileUtils.delete(destinationDir); // Optional: Delete partially copied directory
                }

                Log.d("ContainerManager", "Container exported successfully to: " + destinationDir.getPath());
            } catch (Exception e) {
                Log.e("ContainerManager", "Failed to export container: " + container.getName(), e);
            } finally {
                runOnUiThread(callback); // Ensure the callback runs and preloader dialog closes
    }
        });
    }

    // Utility method to run on UI thread
    private void runOnUiThread(Runnable action) {
        new Handler(Looper.getMainLooper()).post(action);
    }



}
