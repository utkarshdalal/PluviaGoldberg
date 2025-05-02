package com.winlator.xenvironment.components;

import android.content.Context;
import android.content.SharedPreferences;
import android.icu.util.TimeZone;
import android.os.Process;
import android.util.Log;

import com.winlator.PrefManager;
import com.winlator.box86_64.Box86_64Preset;
import com.winlator.box86_64.Box86_64PresetManager;
import com.winlator.contents.ContentProfile;
import com.winlator.contents.ContentsManager;
import com.winlator.core.Callback;
import com.winlator.core.DefaultVersion;
import com.winlator.core.FileUtils;
import com.winlator.core.envvars.EnvVars;
import com.winlator.core.ProcessHelper;
import com.winlator.core.TarCompressorUtils;
import com.winlator.xconnector.UnixSocketConfig;
import com.winlator.xenvironment.EnvironmentComponent;
import com.winlator.xenvironment.ImageFs;
import com.winlator.xenvironment.XEnvironment;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class GlibcProgramLauncherComponent extends GuestProgramLauncherComponent {
    private String guestExecutable;
    private static int pid = -1;
    private String[] bindingPaths;
    private EnvVars envVars;
    private String box86Version = DefaultVersion.BOX86;
    private String box64Version = DefaultVersion.BOX64;
    private String box86Preset = Box86_64Preset.COMPATIBILITY;
    private String box64Preset = Box86_64Preset.COMPATIBILITY;
    private Callback<Integer> terminationCallback;
    private static final Object lock = new Object();
    private boolean wow64Mode = true;
    private final ContentsManager contentsManager;
    private final ContentProfile wineProfile;

    public GlibcProgramLauncherComponent(ContentsManager contentsManager, ContentProfile wineProfile) {
        this.contentsManager = contentsManager;
        this.wineProfile = wineProfile;
    }

    @Override
    public void start() {
        Log.d("GlibcProgramLauncherComponent", "Starting...");

        // Log targetSdk version and related info
        Context context = environment.getContext();
        try {
            int targetSdk = context.getApplicationInfo().targetSdkVersion;
            int compileSdk = android.os.Build.VERSION_CODES.CUR_DEVELOPMENT;
            int deviceSdk = android.os.Build.VERSION.SDK_INT;

            Log.d("GlibcProgramLauncherComponent", "App targetSdk: " + targetSdk);
            Log.d("GlibcProgramLauncherComponent", "Device SDK: " + deviceSdk + " (" + android.os.Build.VERSION.RELEASE + ")");
            Log.d("GlibcProgramLauncherComponent", "Package name: " + context.getPackageName());

        } catch (Exception e) {
            Log.e("GlibcProgramLauncherComponent", "Error getting SDK info: " + e.getMessage());
        }

        synchronized (lock) {
            stop();
            extractBox86_64Files();
            pid = execGuestProgram();
            Log.d("GlibcProgramLauncherComponent", "Process " + pid + " started");
        }
    }

    @Override
    public void stop() {
        Log.d("GlibcProgramLauncherComponent", "Stopping...");
        synchronized (lock) {
            if (pid != -1) {
                Process.killProcess(pid);
                Log.d("GlibcProgramLauncherComponent", "Stopped process " + pid);
                pid = -1;
                List<ProcessHelper.ProcessInfo> subProcesses = ProcessHelper.listSubProcesses();
                for (ProcessHelper.ProcessInfo subProcess : subProcesses) {
                    Log.d("GlibcProgramLauncherComponent",
                            "Sub-process still running: "
                                    + subProcess.name + " | "
                                    + subProcess.pid + " | "
                                    + subProcess.ppid + ", stopping..."
                    );
                    Process.killProcess(subProcess.pid);
                }
            }
        }
    }

    public Callback<Integer> getTerminationCallback() {
        return terminationCallback;
    }

    public void setTerminationCallback(Callback<Integer> terminationCallback) {
        this.terminationCallback = terminationCallback;
    }

    public String getGuestExecutable() {
        return guestExecutable;
    }

    public void setGuestExecutable(String guestExecutable) {
        this.guestExecutable = guestExecutable;
    }

    public boolean isWoW64Mode() {
        return wow64Mode;
    }

    public void setWoW64Mode(boolean wow64Mode) {
        this.wow64Mode = wow64Mode;
    }

    public String[] getBindingPaths() {
        return bindingPaths;
    }

    public void setBindingPaths(String[] bindingPaths) {
        this.bindingPaths = bindingPaths;
    }

    public EnvVars getEnvVars() {
        return envVars;
    }

    public void setEnvVars(EnvVars envVars) {
        this.envVars = envVars;
    }

    public String getBox86Version() { return box86Version; }

    public void setBox86Version(String box86Version) { this.box86Version = box86Version; }

    public String getBox64Version() { return box64Version; }

    public void setBox64Version(String box64Version) { this.box64Version = box64Version; }

    public String getBox86Preset() {
        return box86Preset;
    }

    public void setBox86Preset(String box86Preset) {
        this.box86Preset = box86Preset;
    }

    public String getBox64Preset() {
        return box64Preset;
    }

    public void setBox64Preset(String box64Preset) {
        this.box64Preset = box64Preset;
    }

    private int execGuestProgram() {
        Context context = environment.getContext();
        ImageFs imageFs = ImageFs.find(context);
        File rootDir = imageFs.getRootDir();

        PrefManager.init(context);
        boolean enableBox86_64Logs = PrefManager.getBoolean("enable_box86_64_logs", true);

        EnvVars envVars = new EnvVars();
        if (!wow64Mode) addBox86EnvVars(envVars, enableBox86_64Logs);
        addBox64EnvVars(envVars, enableBox86_64Logs);
        envVars.put("HOME", imageFs.home_path);
        envVars.put("USER", ImageFs.USER);
        envVars.put("TMPDIR", imageFs.getRootDir().getPath() + "/tmp");
        envVars.put("DISPLAY", ":0");

        String winePath = wineProfile == null ? imageFs.getWinePath() + "/bin"
                : ContentsManager.getSourceFile(context, wineProfile, wineProfile.wineBinPath).getAbsolutePath();
        envVars.put("PATH", winePath + ":" +
                imageFs.getRootDir().getPath() + "/usr/bin:" +
                imageFs.getRootDir().getPath() + "/usr/local/bin");

        envVars.put("LD_LIBRARY_PATH", imageFs.getRootDir().getPath() + "/usr/lib");
        envVars.put("BOX64_LD_LIBRARY_PATH", imageFs.getRootDir().getPath() + "/usr/lib/x86_64-linux-gnu");
        envVars.put("ANDROID_SYSVSHM_SERVER", imageFs.getRootDir().getPath() + UnixSocketConfig.SYSVSHM_SERVER_PATH);
        envVars.put("FONTCONFIG_PATH", imageFs.getRootDir().getPath() + "/usr/etc/fonts");

        if ((new File(imageFs.getGlibc64Dir(), "libandroid-sysvshm.so")).exists() ||
                (new File(imageFs.getGlibc32Dir(), "libandroid-sysvshm.so")).exists())
            envVars.put("LD_PRELOAD", "libandroid-sysvshm.so");
        if (this.envVars != null) envVars.putAll(this.envVars);

        String box64Path = rootDir.getPath() + "/usr/local/bin/box64";
        
        // Check if box64 exists and log its details before executing
        File box64File = new File(box64Path);
        Log.d("GlibcProgramLauncherComponent", "About to execute box64 from: " + box64Path);
        
        // Ensure box64 is executable
        FileUtils.chmod(box64File, 0755);
        
        // Check architecture of the binary
        Log.d("GlibcProgramLauncherComponent", "Checking box64 binary format...");
        checkBinaryFormat(box64File);
        
        // Log library dependencies
        Log.d("GlibcProgramLauncherComponent", "Checking box64 library dependencies...");
        checkLibraryDependencies(box64File, rootDir);
        
        // Log all parent directories and their permissions
        Log.d("GlibcProgramLauncherComponent", "Checking parent directory permissions...");
        checkParentDirectories(box64File);
        
        // Log all environment variables
        Log.d("GlibcProgramLauncherComponent", "Environment variables:");
        String[] envArray = envVars.toStringArray();
        for (String env : envArray) {
            Log.d("GlibcProgramLauncherComponent", "  " + env);
        }
        
        // Check if dynamic linker exists
        File linker64 = new File("/system/bin/linker64");
        Log.d("GlibcProgramLauncherComponent", "System linker exists: " + linker64.exists());
        if (linker64.exists()) {
            Log.d("GlibcProgramLauncherComponent", "System linker permissions: " + 
                  "readable=" + linker64.canRead() + 
                  ", executable=" + linker64.canExecute());
        }
        
        // Standard file checks
        logFileDetails(box64File);
        
        String command = box64Path + " " + guestExecutable;
        Log.d("GlibcProgramLauncherComponent", "Final command: " + command);

        return ProcessHelper.exec(command, envVars.toStringArray(), rootDir, (status) -> {
            Log.d("GlibcProgramLauncherComponent", "Process terminated " + pid + " with status " + status);
            synchronized (lock) {
                pid = -1;
            }
            if (terminationCallback != null) terminationCallback.call(status);
        });
    }

    private void extractBox86_64Files() {
        ImageFs imageFs = environment.getImageFs();
        Context context = environment.getContext();
        PrefManager.init(context);
        String currentBox86Version = PrefManager.getString("current_box86_version", "");
        String currentBox64Version = PrefManager.getString("current_box64_version", "");
        File rootDir = imageFs.getRootDir();

        Log.d("GlibcProgramLauncherComponent", "Extracting box86/64 files to rootDir: " + rootDir.getAbsolutePath());

        if (wow64Mode) {
            Log.d("GlibcProgramLauncherComponent", "wow64mode");
            File box86File = new File(rootDir, "/usr/local/bin/box86");
            if (box86File.isFile()) {
                box86File.delete();
                PrefManager.putString("current_box86_version", "");
            }
        } else if (!box86Version.equals(currentBox86Version)) {
            Log.d("GlibcProgramLauncherComponent", "Extracting box86 version " + box86Version + " (current version: " + currentBox86Version + ")");
            TarCompressorUtils.extract(TarCompressorUtils.Type.ZSTD, context.getAssets(), "box86_64/box86-" + box86Version + ".tzst", rootDir);
            PrefManager.putString("current_box86_version", box86Version);

            // Log box86 file details
            File box86File = new File(rootDir, "/usr/local/bin/box86");
            logFileDetails(box86File);
        }

        Log.d("GlibcProgramLauncherComponent", "box64Version " + box64Version);
        Log.d("GlibcProgramLauncherComponent", "currentBox64Version " + currentBox64Version);

        if (!box64Version.equals(currentBox64Version)) {
            ContentProfile profile = contentsManager.getProfileByEntryName("box64-" + box64Version);
            if (profile != null) {
                Log.d("GlibcProgramLauncherComponent", "Profile is not null - applying content for box64 version " + box64Version);
                contentsManager.applyContent(profile);

                // Get destination path for box64 from profile
                File box64File = new File(rootDir, "/usr/local/bin/box64");
                Log.d("GlibcProgramLauncherComponent", "Expected box64 path after profile application: " + box64File.getAbsolutePath());
                logFileDetails(box64File);
            }
            else {
                Log.d("GlibcProgramLauncherComponent", "Profile is null - extracting box64 version " + box64Version + " directly");
                TarCompressorUtils.extract(TarCompressorUtils.Type.ZSTD, context.getAssets(), "box86_64/box64-" + box64Version + ".tzst", rootDir);

                // Log box64 file details after extraction
                File box64File = new File(rootDir, "/usr/local/bin/box64");
                if (box64File.exists()) {
                    FileUtils.chmod(box64File, 0755);
                }
                Log.d("GlibcProgramLauncherComponent", "Expected box64 path after direct extraction: " + box64File.getAbsolutePath());
                logFileDetails(box64File);
            }
            PrefManager.putString("current_box64_version", box64Version);
        } else {
            // Log current box64 file details even if not extracting
            File box64File = new File(rootDir, "/usr/local/bin/box64");
            Log.d("GlibcProgramLauncherComponent", "Using existing box64 at: " + box64File.getAbsolutePath());
            logFileDetails(box64File);
        }
    }

    // Helper method to log file details including permissions and SELinux context
    private void logFileDetails(File file) {
        if (file.exists()) {
            Log.d("GlibcProgramLauncherComponent", "File exists: " + file.getAbsolutePath());
            Log.d("GlibcProgramLauncherComponent", "File permissions: " +
                  "readable=" + file.canRead() +
                  ", writable=" + file.canWrite() +
                  ", executable=" + file.canExecute());
            Log.d("GlibcProgramLauncherComponent", "File size: " + file.length() + " bytes");

            // Get file permissions as octal string
            try {
                String permissionsCmd = "ls -la " + file.getAbsolutePath();
                ProcessBuilder pb = new ProcessBuilder("sh", "-c", permissionsCmd);
                java.lang.Process proc = pb.start();
                java.util.Scanner scanner = new java.util.Scanner(proc.getInputStream()).useDelimiter("\\A");
                String lsOutput = scanner.hasNext() ? scanner.next() : "";
                Log.d("GlibcProgramLauncherComponent", "File ls output: " + lsOutput);
                proc.waitFor();
            } catch (Exception e) {
                Log.e("GlibcProgramLauncherComponent", "Error getting file permissions: " + e.getMessage());
            }

            // Try to get SELinux context if available
            try {
                String selinuxCmd = "ls -Z " + file.getAbsolutePath();
                ProcessBuilder pb = new ProcessBuilder("sh", "-c", selinuxCmd);
                java.lang.Process proc = pb.start();
                java.util.Scanner scanner = new java.util.Scanner(proc.getInputStream()).useDelimiter("\\A");
                String selinuxOutput = scanner.hasNext() ? scanner.next() : "";
                Log.d("GlibcProgramLauncherComponent", "SELinux context: " + selinuxOutput);
                proc.waitFor();
            } catch (Exception e) {
                Log.e("GlibcProgramLauncherComponent", "Error getting SELinux context: " + e.getMessage());
            }
        } else {
            Log.e("GlibcProgramLauncherComponent", "File does NOT exist: " + file.getAbsolutePath());
        }
    }

    private void addBox86EnvVars(EnvVars envVars, boolean enableLogs) {
        envVars.put("BOX86_NOBANNER", ProcessHelper.PRINT_DEBUG && enableLogs ? "0" : "1");
        envVars.put("BOX86_DYNAREC", "1");

        if (enableLogs) {
            envVars.put("BOX86_LOG", "1");
            envVars.put("BOX86_DYNAREC_MISSING", "1");
        }

        envVars.putAll(Box86_64PresetManager.getEnvVars("box86", environment.getContext(), box86Preset));
        envVars.put("BOX86_X11GLX", "1");
    }

    private void addBox64EnvVars(EnvVars envVars, boolean enableLogs) {
        envVars.put("BOX64_NOBANNER", ProcessHelper.PRINT_DEBUG && enableLogs ? "0" : "1");
        envVars.put("BOX64_DYNAREC", "1");
        if (wow64Mode) envVars.put("BOX64_MMAP32", "1");

        if (enableLogs) {
            envVars.put("BOX64_LOG", "1");
            envVars.put("BOX64_DYNAREC_MISSING", "1");
        }

        envVars.putAll(Box86_64PresetManager.getEnvVars("box64", environment.getContext(), box64Preset));
        envVars.put("BOX64_X11GLX", "1");
        envVars.put("BOX64_NORCFILES", "1");
    }

    public void suspendProcess() {
        synchronized (lock) {
            if (pid != -1) ProcessHelper.suspendProcess(pid);
        }
    }

    public void resumeProcess() {
        synchronized (lock) {
            if (pid != -1) ProcessHelper.resumeProcess(pid);
        }
    }

    // Helper method to check binary format using 'file' command
    private void checkBinaryFormat(File binaryFile) {
        try {
            ProcessBuilder pb = new ProcessBuilder("file", binaryFile.getAbsolutePath());
            java.lang.Process proc = pb.start();
            java.util.Scanner scanner = new java.util.Scanner(proc.getInputStream()).useDelimiter("\\A");
            String output = scanner.hasNext() ? scanner.next() : "";
            Log.d("GlibcProgramLauncherComponent", "Binary format: " + output);
            proc.waitFor();
        } catch (Exception e) {
            Log.e("GlibcProgramLauncherComponent", "Error checking binary format: " + e.getMessage());
            
            // Try alternative method
            try {
                ProcessBuilder pb = new ProcessBuilder("sh", "-c", "head -c 20 " + binaryFile.getAbsolutePath() + " | xxd -p");
                java.lang.Process proc = pb.start();
                java.util.Scanner scanner = new java.util.Scanner(proc.getInputStream()).useDelimiter("\\A");
                String output = scanner.hasNext() ? scanner.next() : "";
                Log.d("GlibcProgramLauncherComponent", "Binary header hex: " + output);
                proc.waitFor();
            } catch (Exception ex) {
                Log.e("GlibcProgramLauncherComponent", "Error checking binary header: " + ex.getMessage());
            }
        }
    }

    // Helper method to check library dependencies using 'ldd' or similar
    private void checkLibraryDependencies(File binaryFile, File rootDir) {
        // Try using readelf
        try {
            // First try with readelf (may be available on some devices)
            ProcessBuilder pb = new ProcessBuilder(rootDir.getPath() + "/usr/bin/readelf", "-d", binaryFile.getAbsolutePath());
            java.lang.Process proc = pb.start();
            java.util.Scanner scanner = new java.util.Scanner(proc.getInputStream()).useDelimiter("\\A");
            String output = scanner.hasNext() ? scanner.next() : "";
            Log.d("GlibcProgramLauncherComponent", "Library dependencies (readelf): " + output);
            proc.waitFor();
        } catch (Exception e) {
            Log.e("GlibcProgramLauncherComponent", "Error checking dependencies with readelf: " + e.getMessage());
            
            // Try alternate approach with objdump if available
            try {
                ProcessBuilder pb = new ProcessBuilder(rootDir.getPath() + "/usr/bin/objdump", "-p", binaryFile.getAbsolutePath());
                java.lang.Process proc = pb.start();
                java.util.Scanner scanner = new java.util.Scanner(proc.getInputStream()).useDelimiter("\\A");
                String output = scanner.hasNext() ? scanner.next() : "";
                Log.d("GlibcProgramLauncherComponent", "Library dependencies (objdump): " + output);
                proc.waitFor();
            } catch (Exception ex) {
                Log.e("GlibcProgramLauncherComponent", "Error checking dependencies with objdump: " + ex.getMessage());
            }
        }
        
        // Check existence of some common libraries
        String[] commonLibs = {
            "/usr/lib/libstdc++.so.6",
            "/usr/lib/libc.so.6",
            "/usr/lib/libm.so.6",
            "/usr/lib/ld-linux-aarch64.so.1"
        };
        
        for (String lib : commonLibs) {
            File libFile = new File(rootDir, lib);
            Log.d("GlibcProgramLauncherComponent", "Library " + lib + " exists: " + libFile.exists());
            if (libFile.exists()) {
                Log.d("GlibcProgramLauncherComponent", "Library permissions: " + 
                      "readable=" + libFile.canRead() + 
                      ", executable=" + libFile.canExecute());
            }
        }
    }

    // Helper method to check parent directory permissions
    private void checkParentDirectories(File file) {
        File current = file.getParentFile();
        while (current != null) {
            Log.d("GlibcProgramLauncherComponent", "Directory: " + current.getAbsolutePath());
            Log.d("GlibcProgramLauncherComponent", "  exists=" + current.exists() + 
                  ", readable=" + current.canRead() + 
                  ", writable=" + current.canWrite() + 
                  ", executable=" + current.canExecute());
            
            // Ensure directory has execute permission
            if (current.exists()) {
                FileUtils.chmod(current, 0755);
            }
            
            current = current.getParentFile();
        }
    }
}
