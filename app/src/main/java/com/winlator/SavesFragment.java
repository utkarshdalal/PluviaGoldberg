package com.winlator;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.winlator.core.AppUtils;
import com.winlator.core.Callback;
import com.winlator.core.FileUtils;
import com.winlator.container.Container;
import com.winlator.container.ContainerManager;
import com.winlator.core.PreloaderDialog;
import com.winlator.core.TarCompressorUtils;
import com.winlator.saves.Save;
import com.winlator.saves.SaveManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SavesFragment extends Fragment {
    private RecyclerView recyclerView;
    private TextView emptyTextView;
    private SaveManager saveManager;
    private List<Save> savesList = new ArrayList<>();
    private ContainerManager containerManager;

    private static final int REQUEST_CODE_IMPORT_ARCHIVE = 1001;

    private boolean isDarkMode;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        containerManager = new ContainerManager(getContext());

        // Initialize isDarkMode based on shared preferences or theme
        isDarkMode = PreferenceManager.getDefaultSharedPreferences(getContext())
                .getBoolean("dark_mode", false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.saves);
        saveManager = new SaveManager(getContext());
        loadSavesList();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FrameLayout frameLayout = (FrameLayout) inflater.inflate(R.layout.containers_fragment, container, false);
        recyclerView = frameLayout.findViewById(R.id.RecyclerView);
        emptyTextView = frameLayout.findViewById(R.id.TVEmptyText);
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
        return frameLayout;
    }

    private void loadSavesList() {
        savesList = saveManager.getSaves();
        recyclerView.setAdapter(new SavesAdapter(savesList));
        if (savesList.isEmpty()) {
            emptyTextView.setVisibility(View.VISIBLE);
        } else {
            emptyTextView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.saves_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.saves_menu_add) {
            // SaveSettingsDialog or similar logic for adding a new save
            return true;
        } else if (menuItem.getItemId() == R.id.saves_menu_import) {
            selectArchiveForImport();
            return true;
        } else {
            return super.onOptionsItemSelected(menuItem);
        }
    }

    private void selectArchiveForImport() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, REQUEST_CODE_IMPORT_ARCHIVE);
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_IMPORT_ARCHIVE && resultCode == Activity.RESULT_OK && data != null) {
            Uri archiveUri = data.getData();
            if (archiveUri != null) {
                importSave(archiveUri);
            }
        }
    }

    private void showContainerSelectionDialog(Callback<Container> onContainerSelected, Runnable onCancel) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.container_selection_dialog, null);
        Spinner spinner = dialogView.findViewById(R.id.spinner_container_selection);

        List<Container> containers = containerManager.getContainers();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        for (Container container : containers) {
            adapter.add(container.getName());
        }
        spinner.setAdapter(adapter);

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle(R.string.import_save)
                .setView(dialogView)
                .setPositiveButton(android.R.string.ok, (dialogInterface, which) -> {
                    int selectedPosition = spinner.getSelectedItemPosition();
                    Container selectedContainer = containers.get(selectedPosition);
                    onContainerSelected.call(selectedContainer);
                })
                .setNegativeButton(android.R.string.cancel, (dialogInterface, which) -> {
                    onCancel.run(); // Run the cancel callback
                })
                .create();

        // Apply background based on isDarkMode
        if (isDarkMode) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.content_dialog_background_dark);
        } else {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.content_dialog_background);
        }

        dialog.show();
    }

    private void importSave(Uri archiveUri) {
        PreloaderDialog preloaderDialog = new PreloaderDialog(getActivity());
        preloaderDialog.showOnUiThread(R.string.importing_save);

        new Thread(() -> {
            try {
                File tempDir = new File(getContext().getCacheDir(), "import_temp");
                if (tempDir.exists()) {
                    FileUtils.delete(tempDir);
                }
                if (!tempDir.mkdirs()) {
                    AppUtils.showToast(getContext(), "Failed to create temporary directory.");
                    preloaderDialog.closeOnUiThread();
                    return;
                }

                // Use TarCompressorUtils to extract the archive
                boolean success = TarCompressorUtils.extract(TarCompressorUtils.Type.XZ, getContext(), archiveUri, tempDir);
                if (!success) {
                    AppUtils.showToast(getContext(), "Failed to decompress archive.");
                    preloaderDialog.closeOnUiThread();
                    return;
                }

                // Navigate to the extracted directory, assuming there's only one directory inside tempDir
                File[] extractedFiles = tempDir.listFiles();
                if (extractedFiles == null || extractedFiles.length != 1 || !extractedFiles[0].isDirectory()) {
                    AppUtils.showToast(getContext(), "Unexpected archive structure.");
                    preloaderDialog.closeOnUiThread();
                    return;
                }

                File extractedDir = extractedFiles[0]; // This is the "temp_<savename>_<timestamp>" directory

                // Find the JSON file within the extracted directory
                File[] jsonFiles = extractedDir.listFiles((dir, name) -> name.endsWith(".json"));
                if (jsonFiles == null || jsonFiles.length != 1) {
                    AppUtils.showToast(getContext(), "JSON file not found in the archive.");
                    preloaderDialog.closeOnUiThread();
                    return;
                }

                File jsonFile = jsonFiles[0]; // Use the found JSON file

                String jsonString = FileUtils.readString(jsonFile);
                JSONObject saveData = new JSONObject(jsonString);
                String title = saveData.getString("Title");
                String savePath = saveData.getString("Path");

                getActivity().runOnUiThread(() -> showContainerSelectionDialog((selectedContainer) -> {
                    try {
                        // Adjust the save path based on the selected container
                        File destRootDir = new File(selectedContainer.getRootDir(), ".wine/drive_c");

                        // Ensure savePath starts relative to drive_c
                        String relativeSavePath;
                        int driveCIndex = savePath.indexOf("drive_c");
                        if (driveCIndex != -1) {
                            relativeSavePath = savePath.substring(driveCIndex + "drive_c/".length());
                        } else {
                            relativeSavePath = savePath;  // If drive_c is not found, use the path as-is (fallback)
                        }

                        File destSaveDir = new File(destRootDir, relativeSavePath);

                        // Ensure the full directory structure exists
                        if (!destSaveDir.getParentFile().exists() && !destSaveDir.getParentFile().mkdirs()) {
                            AppUtils.showToast(getContext(), "Failed to create directories for save path.");
                            preloaderDialog.closeOnUiThread();
                            return;
                        }

                        // Copy the contents of the extracted directory to the correct location
                        File saveDirectoryToCopy = new File(extractedDir, new File(savePath).getName());
                        if (!FileUtils.copy(saveDirectoryToCopy, destSaveDir)) {
                            AppUtils.showToast(getContext(), "Failed to copy save files.");
                            preloaderDialog.closeOnUiThread();
                            return;
                        }

                        // Create the new save with the adjusted path
                        saveManager.addSave(title, destSaveDir.getAbsolutePath(), selectedContainer);

                        AppUtils.showToast(getContext(), "Save imported successfully.");
                        loadSavesList();
                    } catch (IOException e) {
                        AppUtils.showToast(getContext(), "Failed to import save: " + e.getMessage());
                    } finally {
                        FileUtils.delete(tempDir);
                        preloaderDialog.closeOnUiThread();
                    }
                }, () -> {
                    // Handle user canceling the container selection
                    preloaderDialog.closeOnUiThread();
                }));
            } catch (Exception e) {
                AppUtils.showToast(getContext(), "Import failed: " + e.getMessage());
                e.printStackTrace(); // Log the error to console
                preloaderDialog.closeOnUiThread();
            }
        }).start();
    }




    public void refreshSavesList() {
        loadSavesList();
    }

    private class SavesAdapter extends RecyclerView.Adapter<SavesAdapter.ViewHolder> {
        private final List<Save> data;

        private class ViewHolder extends RecyclerView.ViewHolder {
            private final ImageButton menuButton;
            private final ImageView imageView;
            private final TextView title;
            private final TextView containerName; // New TextView for container name

            private ViewHolder(View view) {
                super(view);
                this.imageView = view.findViewById(R.id.ImageView);
                this.title = view.findViewById(R.id.TVTitle);
                this.containerName = view.findViewById(R.id.TVContainerName); // Initialize container name TextView
                this.menuButton = view.findViewById(R.id.BTMenu);
            }
        }

        public SavesAdapter(List<Save> data) {
            this.data = data;
        }

        @Override
        public final ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.saves_list_item, parent, false));
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            final Save item = data.get(position);
            holder.imageView.setImageResource(R.drawable.icon_save);
            holder.title.setText(item.getTitle());
            holder.containerName.setText(item.container != null ? item.container.getName() : ""); // Set container name
            holder.menuButton.setOnClickListener((view) -> showListItemMenu(view, item));
        }

        @Override
        public final int getItemCount() {
            return data.size();
        }

        private void showListItemMenu(View anchorView, Save save) {
            final Context context = getContext();
            PopupMenu listItemMenu = new PopupMenu(context, anchorView);
            listItemMenu.inflate(R.menu.save_popup_menu);

            listItemMenu.setOnMenuItemClickListener((menuItem) -> {
                switch (menuItem.getItemId()) {
                    case R.id.save_edit:
                        // Delegate to MainActivity to handle showing SaveEditDialog
                        ((MainActivity) getActivity()).showSaveEditDialog(save);
                        return true;
                    case R.id.save_transfer:
                        showContainerSelectionDialog(save);
                        return true;
                    case R.id.save_export:
                        exportSave(save, false);
                        return true;
                    case R.id.save_share:
                        exportSave(save, true);
                        return true;
                    case R.id.save_unregister:
                        saveManager.removeSave(save);
                        loadSavesList();
                        return true;
                }
                return false;
            });
            listItemMenu.show();
        }

        private File getExportDirectory() {
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File winlatorSavesDir = new File(downloadsDir, "Winlator/Saves");

            if (!winlatorSavesDir.exists()) {
                winlatorSavesDir.mkdirs();
            }

            return winlatorSavesDir;
        }

        private void exportSave(Save save, boolean shareAfterExport) {
            PreloaderDialog preloaderDialog = new PreloaderDialog(getActivity());
            preloaderDialog.showOnUiThread(R.string.exporting_save);

            new Thread(() -> {
                try {
                    File saveDirectory = new File(save.path);
                    if (!saveDirectory.exists() || !saveDirectory.isDirectory()) {
                        AppUtils.showToast(getContext(), "Save directory is invalid.");
                        return;
                    }

                    File saveJsonFile = saveManager.getSaveJsonFile(save);
                    if (!saveJsonFile.exists()) {
                        AppUtils.showToast(getContext(), "Save .json file is missing.");
                        return;
                    }

                    File exportDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Winlator/Saves");
                    if (!exportDirectory.exists() && !exportDirectory.mkdirs()) {
                        AppUtils.showToast(getContext(), "Failed to create export directory.");
                        return;
                    }

                    String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                    String archiveName = save.getTitle() + "_" + timestamp + ".tar.xz"; // Use XZ compression as example
                    File exportFile = new File(exportDirectory, archiveName);

                    File tempExportDir = new File(exportDirectory, "temp_" + save.getTitle() + "_" + timestamp);
                    if (!tempExportDir.exists() && !tempExportDir.mkdirs()) {
                        AppUtils.showToast(getContext(), "Failed to create temporary directory.");
                        return;
                    }

                    // Copy the .json file and the save directory to the temp directory
                    File copiedJsonFile = new File(tempExportDir, saveJsonFile.getName());
                    if (!FileUtils.copy(saveJsonFile, copiedJsonFile)) {
                        AppUtils.showToast(getContext(), "Failed to copy .json file.");
                        return;
                    }

                    File copiedSaveDirectory = new File(tempExportDir, saveDirectory.getName());
                    if (!FileUtils.copy(saveDirectory, copiedSaveDirectory)) {
                        AppUtils.showToast(getContext(), "Failed to copy save directory.");
                        return;
                    }

                    // Compress the temp directory itself, which now contains the JSON file and save directory
                    TarCompressorUtils.compress(TarCompressorUtils.Type.XZ, tempExportDir, exportFile, 3, null);


                    // Clean up the temporary directory
                    FileUtils.delete(tempExportDir);

                    AppUtils.showToast(getContext(), "Save exported to " + exportFile.getAbsolutePath());

                    makeFileVisible(exportFile);

                    if (shareAfterExport) {
                        shareExportedFile(exportFile);
                    }
                } catch (Exception e) {
                    AppUtils.showToast(getContext(), "Failed to export save.");
                } finally {
                    preloaderDialog.closeOnUiThread();
                }
            }).start();
        }



        private void makeFileVisible(File file) {
            // Force a media scan so the file becomes visible to other apps
            MediaScannerConnection.scanFile(getContext(),
                    new String[]{file.getAbsolutePath()},
                    null, // MIME type can be null
                    (path, uri) -> {
                        // Scan complete, file should be visible now
                    });

            // Set file permissions for other apps
            file.setReadable(true, false);
            file.setWritable(true, false);
        }

        private void shareExportedFile(File exportFile) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/octet-stream"); // More general MIME type
            Uri fileUri = FileProvider.getUriForFile(getContext(), "com.winlator.fileprovider", exportFile);
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(shareIntent, "Share Save Archive"));
        }


        private void showContainerSelectionDialog(Save save) {
            View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.container_selection_dialog, null);
            Spinner spinner = dialogView.findViewById(R.id.spinner_container_selection);

            List<Container> containers = containerManager.getContainers();
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            for (Container container : containers) {
                adapter.add(container.getName());
            }
            spinner.setAdapter(adapter);

            new AlertDialog.Builder(getContext())
                    .setTitle(R.string.save_transfer)
                    .setView(dialogView)
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        int selectedPosition = spinner.getSelectedItemPosition();
                        Container selectedContainer = containers.get(selectedPosition);

                        try {
                            saveManager.transferSave(save, selectedContainer);
                            loadSavesList();
                            AppUtils.showToast(getContext(), "Transfer complete");
                        } catch (IOException e) {
                            AppUtils.showToast(getContext(), "Transfer failed: " + e.getMessage());
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        }
    }




        private void showTransferDialog(Save save) {
            // Create a simple dialog with a Spinner for selecting a container
            View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.container_selection_dialog, null);
            Spinner spinner = dialogView.findViewById(R.id.spinner_container_selection);

            List<Container> containers = containerManager.getContainers();
            ArrayAdapter<Container> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, containers);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);

            new androidx.appcompat.app.AlertDialog.Builder(getContext())
                    .setTitle(R.string.select_container)
                    .setView(dialogView)
                    .setPositiveButton(R.string.transfer, (dialog, which) -> {
                        Container selectedContainer = (Container) spinner.getSelectedItem();
                        transferSaveFiles(save, selectedContainer);
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        }

        private void transferSaveFiles(Save save, Container container) {
            // Clone the save directory to the container's directory
            File sourceDir = new File(save.path);
            File targetDir = new File(container.getRootDir(), "xuser-" + container.id);

            boolean success = FileUtils.copy(sourceDir, targetDir);
            if (success) {
                // Optionally, notify the user of the successful transfer
                AppUtils.showToast(getContext(), R.string.transfer_complete);
            } else {
                AppUtils.showToast(getContext(), R.string.transfer_failed);
            }
        }
    }

