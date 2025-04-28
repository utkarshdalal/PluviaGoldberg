package com.winlator.restore;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.winlator.R;
import com.winlator.core.AppUtils;
import com.winlator.core.FileUtils;
import com.winlator.core.PreloaderDialog;
import com.winlator.core.TarCompressorUtils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;

public class RestoreActivity extends Activity {

    private PreloaderDialog preloaderDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preloaderDialog = new PreloaderDialog(this);
        preloaderDialog.showOnUiThread(R.string.restoring_data);

        Uri backupUri = getIntent().getData();
        if (backupUri != null) {
            startRestoreOperation(backupUri);
        } else {
            finish(); // Close the activity if no URI is passed
        }
    }

    private void startRestoreOperation(Uri backupUri) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                File dataDir = getFilesDir().getParentFile(); // App's data directory
                File backupFile = FileUtils.getFileFromUri(this, backupUri);

                if (backupFile != null) {
                    boolean success = TarCompressorUtils.extractTar(backupFile, dataDir, (file, size) -> {
                        // Skip symbolic links pointing to critical directories
                        if (isCriticalSymbolicLink(file)) {
                            Log.d("RestoreOp", "Skipping symbolic link: " + file.getAbsolutePath());
                            return null; // Return null to skip extraction
                        }
                        Log.d("RestoreOp", "Extracting file: " + file.getAbsolutePath());
                        return file;
                    });

                    backupFile.delete(); // Clean up the temp file if it was created

                    if (success) {
                        onRestoreSuccess();
                    } else {
                        throw new IOException("Extraction failed");
                    }
                } else {
                    throw new IOException("Failed to obtain file from URI");
                }
            } catch (Exception e) {
                Log.e("RestoreOp", "Restore failed", e);
                onRestoreFailed();
            }
        });
    }

    private boolean isCriticalSymbolicLink(File file) {
        if (FileUtils.isSymlink(file)) {
            String targetPath = FileUtils.readSymlink(file);
            // Check if the symlink points to the Downloads or Storage directories
            if (targetPath.contains("Download") || targetPath.contains("storage")) {
                return true;
            }
        }
        return false;
    }


    private void onRestoreSuccess() {
        runOnUiThread(() -> {
            preloaderDialog.closeOnUiThread();
            AppUtils.showToast(this, "Data restored successfully.");
            finish();
            // Restart the main application after restore
            Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
            if (intent != null) {
                startActivity(intent);
            }
        });
    }

    private void onRestoreFailed() {
        runOnUiThread(() -> {
            preloaderDialog.closeOnUiThread();
            AppUtils.showToast(this, "Data restore failed.");
            finish();
        });
    }
}
