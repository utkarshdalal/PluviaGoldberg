package com.winlator.contentdialog;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.preference.PreferenceManager;

import com.winlator.MainActivity;
import com.winlator.R;
import com.winlator.container.Container;
import com.winlator.container.ContainerManager;
import com.winlator.core.AppUtils;
import com.winlator.saves.CustomFilePickerActivity;
import com.winlator.saves.Save;
import com.winlator.saves.SaveManager;

import java.io.File;

public class SaveEditDialog extends ContentDialog {
    public static final int REQUEST_CODE_CUSTOM_FILE_PICKER = 1;  // Match the SaveSettingsDialog constant
    private final SaveManager saveManager;
    private final ContainerManager containerManager;
    private final Activity activity;
    private Container selectedContainer;
    private TextView tvOriginalPath;
    //private TextView tvUpdatedPath;
    private String selectedPath;
    private EditText etTitle;
    private Save saveToEdit;

    private boolean isDarkMode;

    // Constructor for editing an existing save
    public SaveEditDialog(Activity activity, SaveManager saveManager, ContainerManager containerManager, Save saveToEdit) {
        super(activity, R.layout.save_edit_dialog);
        this.activity = activity;
        this.saveManager = saveManager;
        this.containerManager = containerManager;
        this.saveToEdit = saveToEdit; // Set the save object being edited
        setTitle(R.string.edit_save);
        setIcon(R.drawable.icon_save);

        createContentView();
    }

    private void createContentView() {
        final Context context = getContext();


        isDarkMode = PreferenceManager.getDefaultSharedPreferences(getContext())
                .getBoolean("dark_mode", false);

        LinearLayout llContent = findViewById(R.id.LLContent);
        llContent.getLayoutParams().width = AppUtils.getPreferredDialogWidth(context);

        etTitle = findViewById(R.id.ETTitle);

        // Set the background resource based on isDarkMode
        etTitle.setBackgroundResource(isDarkMode ? R.drawable.edit_text_dark : R.drawable.edit_text);

        tvOriginalPath = findViewById(R.id.TVOriginalPath);
        //tvUpdatedPath = findViewById(R.id.TVUpdatedPath);

        // Display original path
        etTitle.setText(saveToEdit.getTitle());
        tvOriginalPath.setText(saveToEdit.path);
        selectedPath = saveToEdit.path;
        //tvUpdatedPath.setText(selectedPath);
        //tvUpdatedPath.setVisibility(View.VISIBLE);

        final Spinner sContainer = findViewById(R.id.SContainer);
        //loadContainerSpinner(sContainer);

        // Set the background resource based on isDarkMode
//        sContainer.setBackgroundResource(isDarkMode ? R.drawable.content_dialog_background_dark : R.drawable.content_dialog_background);
//        sContainer.setPopupBackgroundResource(isDarkMode ? R.drawable.content_dialog_background_dark : R.drawable.content_dialog_background);

        int containerPosition = containerManager.getContainers().indexOf(saveToEdit.container);
        if (containerPosition >= 0) {
            sContainer.setSelection(containerPosition);
        }

        /*
        findViewById(R.id.BTPickPath).setOnClickListener((v) -> {
            if (selectedContainer != null) {
                openFolderPicker();
            } else {
                AppUtils.showToast(context, R.string.select_container_first);
            }
        });

         */

        // Container can't be modified in edit mode
        Container currentContainer = saveToEdit.container;

        setOnConfirmCallback(() -> {
            String title = etTitle.getText().toString().trim();

            if (title.isEmpty()) {
                AppUtils.showToast(getContext(), R.string.name_required);
                return;
            }

            try {
                saveManager.updateSave(saveToEdit, title, selectedPath, currentContainer);
                if (activity instanceof MainActivity) {
                    ((MainActivity) activity).onSaveAdded();
                }
                dismiss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void updateSelectedPath(String path) {
        selectedPath = path;
        //tvUpdatedPath.setText(path);
        //tvUpdatedPath.setVisibility(View.VISIBLE);
        Log.d("SaveEditDialog", "Selected Path Updated in UI: " + path); // Debug log
    }

    /*
    private void loadContainerSpinner(Spinner sContainer) {
        List<Container> containers = containerManager.getContainers();
        List<String> containerNames = new ArrayList<>();
        for (Container container : containers) {
            containerNames.add(container.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, containerNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sContainer.setAdapter(adapter);

        sContainer.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedContainer = containers.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedContainer = null;
            }
        });
    }

     */

    private void openFolderPicker() {
        if (selectedContainer == null || selectedContainer.getRootDir() == null) {
            AppUtils.showToast(getContext(), R.string.invalid_container);
            return;
        }

        File rootDir = selectedContainer.getRootDir();
        String dynamicPath = new File(rootDir, ".wine/drive_c/").getAbsolutePath();

        Intent intent = new Intent(activity, CustomFilePickerActivity.class);
        intent.putExtra("initialDirectory", dynamicPath);
        intent.putExtra("isEditing", true); // Indicate that we are editing an existing save
        intent.putExtra("editingPath", saveToEdit.path); // Pass the current path if editing

        activity.startActivityForResult(intent, REQUEST_CODE_CUSTOM_FILE_PICKER);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_CUSTOM_FILE_PICKER && resultCode == Activity.RESULT_OK) {
            String path = data.getStringExtra("selectedDirectory");
            Log.d("SaveEditDialog", "Returned Path from Picker: " + path); // Debug log
            if (path != null && isPathValidForContainer(path)) {
                activity.runOnUiThread(() -> updateSelectedPath(path));  // Ensure UI updates happen on the main thread
                Log.d("SaveEditDialog", "Path selected: " + path);
            } else {
                AppUtils.showToast(getContext(), R.string.invalid_path);
            }
        }
    }

    private boolean isPathValidForContainer(String path) {
        if (selectedContainer == null) return false;
        File rootDir = selectedContainer.getRootDir();
        return path.startsWith(rootDir.getAbsolutePath());
    }

    public int getSaveId() {
        return saveToEdit.id;
    }
}
