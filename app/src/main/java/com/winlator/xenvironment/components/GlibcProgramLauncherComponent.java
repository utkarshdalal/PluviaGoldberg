package com.winlator.xenvironment.components;

import android.content.Context;
import android.icu.util.TimeZone;
import android.os.Process;
import android.util.Log;

import com.winlator.PrefManager;
import com.winlator.box86_64.Box86_64Preset;
import com.winlator.box86_64.Box86_64PresetManager;
import com.winlator.container.Shortcut;
import com.winlator.contents.ContentProfile;
import com.winlator.contents.ContentsManager;
import com.winlator.core.Callback;
import com.winlator.core.DefaultVersion;
import com.winlator.core.envvars.EnvVars;
import com.winlator.core.FileUtils;
import com.winlator.core.ProcessHelper;
import com.winlator.core.TarCompressorUtils;
import com.winlator.xconnector.UnixSocketConfig;
import com.winlator.xenvironment.ImageFs;
import com.winlator.xenvironment.XEnvironment;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

public class GlibcProgramLauncherComponent extends GuestProgramLauncherComponent {
    private static String guestExecutable;
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

    public GlibcProgramLauncherComponent() {
    }

    @Override
    public void start() {
        synchronized (lock) {
            stop();

//            // Set execute permissions for vk_instance_debugger
//            File vkInstanceDebugger = new File(environment.getImageFs().getRootDir(), "usr/bin/vk_instance_debugger");
//            if (vkInstanceDebugger.exists()) {
//                FileUtils.chmod(vkInstanceDebugger, 0755); // Set execute permissions for all users
//            }
//
//            // Check dependencies and log
//            String vkDebuggerDeps = checkCurlDependencies();  // Change 'checkCurlDependencies' as needed
//            Log.d("GlibcProgramLauncherComponent", "vk_instance_debugger Dependencies:\n" + vkDebuggerDeps);
//
//            Log.d("PermissionsCheck", "Checking if vk_instance_debugger has execute permission: " +
//                    new File("/data/user/0/com.winlator/files/imagefs/usr/bin/vk_instance_debugger").canExecute());
//
//            // Delay the execution of vk_instance_debugger by 30 seconds
//            new Handler(Looper.getMainLooper()).postDelayed(() -> {
//                runWithoutBox64(environment.getImageFs().getRootDir().getPath() + "/usr/bin/vk_instance_debugger");
//            }, 30000); // 30000 milliseconds = 30 seconds

            // Proceed with other startup tasks
            extractBox86_64Files();
            pid = execGuestProgram();
        }
    }


//    private void executeCurlInBackground() {
//        String curlPath = environment.getImageFs().getRootDir().getPath() + "/usr/bin/vk_instance_debugger";
////        String[] command = {curlPath, "--help"};
//        String[] command = {
//                "/data/user/0/com.winlator/files/imagefs/usr/lib/ld-linux-aarch64.so.1",
//                "--library-path", "/data/user/0/com.winlator/files/imagefs/usr/lib",
//                "/data/user/0/com.winlator/files/imagefs/usr/bin/vk_instance_debugger", "--help"
//        };
//
//
//        EnvVars envVars = new EnvVars();
//        envVars.put("LD_LIBRARY_PATH", environment.getImageFs().getRootDir().getPath() + "/usr/lib");
//
//        new Thread(() -> {
//            StringBuilder output = new StringBuilder("Running curl in the background...\n");
//
//            try {
//                java.lang.Process process = Runtime.getRuntime().exec(command, envVars.toStringArray(), new File(environment.getImageFs().getRootDir().getPath()));
//                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
//                BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
//
//                String line;
//                while ((line = reader.readLine()) != null) {
//                    output.append(line).append("\n");
//                }
//                while ((line = errorReader.readLine()) != null) {
//                    output.append(line).append("\n");
//                }
//
//                int exitCode = process.waitFor();
//                output.append("Curl finished with exit code: ").append(exitCode).append("\n");
//            } catch (Exception e) {
//                output.append("Error running curl: ").append(e.getMessage()).append("\n");
//            }
//
//            Log.d("CurlOutput", output.toString());
//        }).start();
//    }
//
//    public void runWithoutBox64(String programPath) {
//        String[] command = {programPath, "--help"};  // Example to run `curl --help`
//
//        EnvVars envVars = new EnvVars();
//        envVars.put("LD_LIBRARY_PATH", environment.getImageFs().getRootDir().getPath() + "/usr/lib");
//
//        new Thread(() -> {
//            StringBuilder output = new StringBuilder("Running program without Box64...\n");
//
//            try {
//                java.lang.Process process = Runtime.getRuntime().exec(command, envVars.toStringArray(), new File(environment.getImageFs().getRootDir().getPath()));
//                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
//                BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
//
//                String line;
//                while ((line = reader.readLine()) != null) {
//                    output.append(line).append("\n");
//                }
//                while ((line = errorReader.readLine()) != null) {
//                    output.append(line).append("\n");
//                }
//
//                int exitCode = process.waitFor();
//                output.append("Program finished with exit code: ").append(exitCode).append("\n");
//            } catch (Exception e) {
//                output.append("Error running program: ").append(e.getMessage()).append("\n");
//            }
//
//            Log.d("ProgramOutput", output.toString());
//        }).start();
//    }
//
//
//    private String checkCurlDependencies() {
//        String curlPath = environment.getImageFs().getRootDir().getPath() + "/usr/lib/mangohud/libMangoHud.so";
//        String lddCommand = environment.getImageFs().getRootDir().getPath() + "/usr/bin/ldd " + curlPath;
//
//        StringBuilder output = new StringBuilder("Checking Curl dependencies...\n");
//
//        try {
//            java.lang.Process process = Runtime.getRuntime().exec(lddCommand);
//            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
//            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
//
//            String line;
//            while ((line = reader.readLine()) != null) {
//                output.append(line).append("\n");
//            }
//            while ((line = errorReader.readLine()) != null) {
//                output.append(line).append("\n");
//            }
//
//            process.waitFor();
//        } catch (Exception e) {
//            output.append("Error running ldd: ").append(e.getMessage());
//        }
//
//        Log.d("CurlDeps", output.toString()); // Log the full dependency output
//        return output.toString();
//    }


    @Override
    public void stop() {
        synchronized (lock) {
            if (pid != -1) {
                Process.killProcess(pid);
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
        ImageFs imageFs = environment.getImageFs();
        File rootDir = imageFs.getRootDir();

        PrefManager.init(context);
        boolean enableBox86_64Logs = PrefManager.getBoolean("enable_box86_64_logs", false);

        EnvVars envVars = new EnvVars();
        if (!wow64Mode) addBox86EnvVars(envVars, enableBox86_64Logs);
        addBox64EnvVars(envVars, enableBox86_64Logs);
        if (this.envVars != null) envVars.putAll(this.envVars);

        return exec(context, !wow64Mode, bindingPaths, envVars, terminationCallback, "box64 " + guestExecutable);
    }
    public static int exec(Context context, String prootCmd) {
        return exec(context, false, new String[0], null, null, prootCmd);
    }

    public static int exec(Context context, boolean proot32, String[] bindingPaths, EnvVars extraVars, Callback<Integer> terminationCallback, String prootCmd) {
        Log.d("GlibcProgramLauncherComponent", "Executing guest program");
        // Context context = environment.getContext();
        // ImageFs imageFs = environment.getImageFs();
        ImageFs imageFs = ImageFs.find(context);
        File rootDir = imageFs.getRootDir();
        File tmpDir = XEnvironment.getTmpDir(context);
        String nativeLibraryDir = context.getApplicationInfo().nativeLibraryDir;
        File nativeLibs = new File(nativeLibraryDir);
        // Log.d("GuestProgramLauncherComponent", nativeLibraryDir + " exists: " + nativeLibs.exists());
        // Log.d("GuestProgramLauncherComponent", nativeLibraryDir + " is directory: " + nativeLibs.isDirectory());
        Log.d("GuestProgramLauncherComponent", nativeLibraryDir + " contains: " + Arrays.toString(Arrays.stream(nativeLibs.listFiles()).map(File::getName).toArray()));
        // nativeLibraryDir = nativeLibraryDir.replace("arm64", "arm64-v8a");
        // Log.d("GuestProgramLauncherComponent", nativeLibraryDir + " exists: " + (new File(nativeLibraryDir)).exists());
        // Log.d("GuestProgramLauncherComponent", steamApiPath + " exists: " + new File(steamApiPath).exists());
        // ImageFs fs = ImageFs.find(context);
        // Path dllsDir = Paths.get(fs.getRootDir().getAbsolutePath(), "/usr/dlls");
        // Path steamApiTargetPath = Paths.get(dllsDir.toString(), "steam_api.dll.so");
        // Path steamApiTargetPath = Paths.get(fs.getLib64Dir().toString(), "libsteam_api.dll.so");
        // try {
        //     if (Files.exists(steamApiTargetPath)) {
        //         Files.delete(steamApiTargetPath);
        //     }
        //     // Files.createDirectories(dllsDir);
        //     Path steamApiPath = Paths.get(nativeLibraryDir, "libsteam_api.dll.so");
        //     Files.copy(steamApiPath, steamApiTargetPath);
        //     FileUtils.chmod(new File(steamApiTargetPath.toString()), 0771);
        // } catch (IOException e) {
        //     Log.e("GuestProgramLauncherComponent", "Failed to copy steam_api.dll.so to /usr/lib " + e);
        // }

        // PrefManager.init(context);
        // boolean enableBox86_64Logs = PrefManager.getBoolean("enable_box86_64_logs", false);

        EnvVars envVars = new EnvVars();
        // if (!wow64Mode) addBox86EnvVars(envVars, enableBox86_64Logs);
        // addBox64EnvVars(envVars, enableBox86_64Logs);

        TimeZone androidTz = TimeZone.getDefault();
        String tzId = androidTz.getID();
        // Log.d("GuestProgramLauncherComponent", "Android timezone: " + tzId);

        envVars.put("TZ", tzId);
        envVars.put("HOME", ImageFs.HOME_PATH);
        envVars.put("USER", ImageFs.USER);
        envVars.put("TMPDIR", "/tmp");
        envVars.put("LC_ALL", "en_US.utf8");
        envVars.put("DISPLAY", ":0");
        envVars.put("PATH", imageFs.getWinePath() + "/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin");
        envVars.put("LD_LIBRARY_PATH", "/usr/lib/aarch64-linux-gnu:/usr/lib/arm-linux-gnueabihf");
        envVars.put("ANDROID_SYSVSHM_SERVER", UnixSocketConfig.SYSVSHM_SERVER_PATH);
        envVars.put("BOX64_LD_LIBRARY_PATH", imageFs.getRootDir().getPath() + "/usr/lib/x86_64-linux-gnu");
        envVars.put("FONTCONFIG_PATH", imageFs.getRootDir().getPath() + "/usr/etc/fonts");

//        File libMangoHud = new File(imageFs.getRootDir(), "usr/lib/mangohud/libMangoHud.so");
//        if (libMangoHud.exists()) {
//            libMangoHud.setExecutable(true, false); // Set execute permissions for all users
//        }


        if ((new File(imageFs.getGlibc64Dir(), "libandroid-sysvshm.so")).exists() ||
                (new File(imageFs.getGlibc32Dir(), "libandroid-sysvshm.so")).exists())
            envVars.put("LD_PRELOAD", "libandroid-sysvshm.so");
        if (extraVars != null) envVars.putAll(extraVars);

        boolean bindSHM = envVars.get("WINEESYNC").equals("1");

        String command = rootDir.getPath() + "/usr/local/bin/box64 ";

        Log.d("GlibcProgramLauncherComponent", command + "/libproot.so exists: " + (new File(rootDir.getPath() + "/usr/local/bin/box64")).exists());
        command += " --kill-on-exit";
        command += " --rootfs=" + rootDir;
        command += " --cwd=" + ImageFs.HOME_PATH;
        command += " --bind=/dev";

        if (bindSHM) {
            File shmDir = new File(rootDir, "/tmp/shm");
            shmDir.mkdirs();
            command += " --bind=" + shmDir.getAbsolutePath() + ":/dev/shm";
        }

        command += " --bind=/proc";
        command += " --bind=/sys";

        if (bindingPaths != null) {
            for (String path : bindingPaths)
                command += " --bind=\"" + (new File(path)).getAbsolutePath() + "\"";
        }

        // Set execute permissions for box64 just in case
        File box64File = new File(rootDir, "/usr/local/bin/box64");
        if (box64File.exists()) {
            FileUtils.chmod(box64File, 0755);
        }

        command += guestExecutable;

        Log.d("LD_LIBRARY_PATH", envVars.get("LD_LIBRARY_PATH"));

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

        if (wow64Mode) {
            File box86File = new File(rootDir, "/usr/local/bin/box86");
            if (box86File.isFile()) {
                box86File.delete();
                PrefManager.putString("current_box86_version", "");
            }
        } else if (!box86Version.equals(currentBox86Version)) {
            TarCompressorUtils.extract(TarCompressorUtils.Type.ZSTD, context.getAssets(), "box86_64/box86-" + box86Version + ".tzst", rootDir);
            PrefManager.putString("current_box86_version", box86Version);
        }

        if (!box64Version.equals(currentBox64Version)) {
            TarCompressorUtils.extract(TarCompressorUtils.Type.ZSTD, context.getAssets(), "box86_64/box64-" + box64Version + ".tzst", rootDir);
            PrefManager.putString("current_box64_version", box64Version);
        }

        // Set execute permissions for box64 just in case
        File box64File = new File(rootDir, "/usr/local/bin/box64");
        if (box64File.exists()) {
            FileUtils.chmod(box64File, 0755);
        }
        Log.d("GlibcProgramLauncherComponent", "box64File exists: " + box64File.exists());
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
        envVars.put("BOX86_NORCFILES", "1");
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

    public String execShellCommand(String command) {
        StringBuilder output = new StringBuilder();
        EnvVars envVars = new EnvVars();
        ImageFs imageFs = environment.getImageFs();

        envVars.put("PATH", imageFs.getRootDir().getPath() + "/usr/bin:/usr/local/bin:" + imageFs.getWinePath() + "/bin");
        envVars.put("LD_LIBRARY_PATH", imageFs.getRootDir().getPath() + "/usr/lib");

        // Execute the command and capture its output
        try {
            java.lang.Process process = Runtime.getRuntime().exec(command, envVars.toStringArray(), imageFs.getRootDir());
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            while ((line = errorReader.readLine()) != null) {
                output.append(line).append("\n");
            }

            process.waitFor();
        } catch (Exception e) {
            output.append("Error: ").append(e.getMessage());
        }

        return output.toString();
    }



}
