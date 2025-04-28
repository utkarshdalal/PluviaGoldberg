package com.winlator;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.collection.ArrayMap;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;

import com.google.android.material.navigation.NavigationView;
import com.winlator.box86_64.Box86_64EditPresetDialog;
import com.winlator.box86_64.Box86_64Preset;
import com.winlator.box86_64.Box86_64PresetManager;
import com.winlator.container.Container;
import com.winlator.container.ContainerManager;
import com.winlator.contentdialog.ContentDialog;
import com.winlator.contents.ContentProfile;
import com.winlator.contents.ContentsManager;
import com.winlator.core.AppUtils;
import com.winlator.core.ArrayUtils;
import com.winlator.core.Callback;
import com.winlator.core.DefaultVersion;
import com.winlator.core.FileUtils;
import com.winlator.core.PreloaderDialog;
import com.winlator.core.StringUtils;
import com.winlator.core.TarCompressorUtils;
import com.winlator.core.WineInfo;
import com.winlator.core.WineUtils;
import com.winlator.inputcontrols.ControlElement;
import com.winlator.inputcontrols.ControlsProfile;
import com.winlator.inputcontrols.ExternalController;
import com.winlator.midi.MidiManager;
import com.winlator.restore.RestoreActivity;
import com.winlator.widget.InputControlsView;
import com.winlator.xenvironment.ImageFs;
import com.winlator.xenvironment.ImageFsInstaller;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SettingsFragment extends Fragment {
    public static final String DEFAULT_WINE_DEBUG_CHANNELS = "warn,err,fixme";
    private Callback<Uri> selectWineFileCallback;
    private Callback<Uri> installSoundFontCallback;
    private PreloaderDialog preloaderDialog;
    public static final String DEFAULT_EXPORT_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/Winlator/Frontend";
    private SharedPreferences preferences;

	// Disable or enable True Mouse Control
	private CheckBox cbCursorLock;
    // Disable or enable Xinput Processing
    private CheckBox cbXinputToggle;
    // Disable or enable Touchscreen Input Mode
    private CheckBox cbXTouchscreenToggle;

    private CheckBox cbGyroEnabled;
    private SeekBar sbGyroXSensitivity;
    private SeekBar sbGyroYSensitivity;
    private SeekBar sbGyroSmoothing;
    private SeekBar sbGyroDeadzone;
    private CheckBox cbInvertGyroX;
    private CheckBox cbInvertGyroY;

    private boolean isRestoreAction = false;

    private boolean enableLegacyInputMode = false;

    private CheckBox cbEnableBigPictureMode;
    private CheckBox cbEnableCustomApiKey;
    private EditText etCustomApiKey;

    private CheckBox cbDarkMode;
    boolean isDarkMode;

    private static final int REQUEST_CODE_FRONTEND_EXPORT_PATH = 1002;
    private static final int REQUEST_CODE_INSTALL_SOUNDFONT = 1001;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
        preloaderDialog = new PreloaderDialog(getActivity());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Apply dynamic styles to all labels
        applyDynamicStylesRecursively(view);

        Button btnConfigureGyro = view.findViewById(R.id.BTConfigureGyro);
        btnConfigureGyro.setOnClickListener(v -> showGyroConfigDialog());

        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(R.string.settings);
    }



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_fragment, container, false);
        final Context context = getContext();
        preferences = PreferenceManager.getDefaultSharedPreferences(context);

        // Check for Dark Mode preference
        isDarkMode = preferences.getBoolean("dark_mode", false);
        // Apply dynamic styles
        applyDynamicStyles(view, isDarkMode);

        // Initialize the Dark Mode checkbox
        cbDarkMode = view.findViewById(R.id.CBDarkMode);
        cbDarkMode.setChecked(preferences.getBoolean("dark_mode", false));

        cbDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Save dark mode preference
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("dark_mode", isChecked);
            editor.apply();

            // Update the UI or activity theme if necessary
            updateTheme(isChecked);
        });

        // Initialize Big Picture Mode Checkbox
        cbEnableBigPictureMode = view.findViewById(R.id.CBEnableBigPictureMode);
        cbEnableBigPictureMode.setChecked(preferences.getBoolean("enable_big_picture_mode", false));

        initCustomApiKeySettings(view);

        // Initialize the cursor lock checkbox
        cbCursorLock = view.findViewById(R.id.CBCursorLock);
        cbCursorLock.setChecked(preferences.getBoolean("cursor_lock", true));

        // Initialize the xinput toggle checkbox
        cbXinputToggle = view.findViewById(R.id.CBXinputToggle);
        cbXinputToggle.setChecked(preferences.getBoolean("xinput_toggle", false));

        // Initialize the Touchscreen mode toggle
        cbXTouchscreenToggle = view.findViewById(R.id.CBXTouchscreenToggle);
        cbXTouchscreenToggle.setChecked(preferences.getBoolean("touchscreen_toggle", false));

        // Inside onCreateView in SettingsFragment.java
        CheckBox cbLegacyInputMode = view.findViewById(R.id.CBLegacyInputMode);
        enableLegacyInputMode = preferences.getBoolean("legacy_mode_enabled", false);
        cbLegacyInputMode.setChecked(enableLegacyInputMode);

        // Listen to changes and update the temporary state
        cbLegacyInputMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            enableLegacyInputMode = isChecked;
            preferences.edit().putBoolean("legacy_mode_enabled", isChecked).apply();
        });


        // Initialize gyro enable checkbox
        cbGyroEnabled = view.findViewById(R.id.CBGyroEnabled);
        cbGyroEnabled.setChecked(preferences.getBoolean("gyro_enabled", false));

//        CheckBox cbProcessGyroWithLeftTrigger = view.findViewById(R.id.CBProcessGyroWithLeftTrigger);
//        cbProcessGyroWithLeftTrigger.setChecked(preferences.getBoolean("process_gyro_with_left_trigger", false));

        Spinner sbGyroTriggerButton = view.findViewById(R.id.SBGyroTriggerButton);
        RadioGroup rgGyroMode = view.findViewById(R.id.RGyroMode);


        int selectedMode = preferences.getInt("gyro_mode", 0); // 0 for hold, 1 for toggle

        TypedArray keycodeArray = getResources().obtainTypedArray(R.array.button_keycodes);
        int[] keycodes = new int[keycodeArray.length()];

// Log the keycodes for debugging purposes
        Log.d("SettingsFragment", "Populating keycodes array:");

        for (int i = 0; i < keycodeArray.length(); i++) {
            keycodes[i] = keycodeArray.getResourceId(i, -1); // Get the resource ID
            if (keycodes[i] != -1) {
                keycodes[i] = getResources().getInteger(keycodes[i]); // Fetch the actual integer value
                Log.d("SettingsFragment", "Keycode[" + i + "] = " + keycodes[i]); // Log the populated keycode
            } else {
                Log.e("SettingsFragment", "Invalid keycode resource at index " + i);
            }
        }
        keycodeArray.recycle(); // Always recycle TypedArray to free up resources

// Now get the currently selected button from SharedPreferences
        int selectedButton = preferences.getInt("gyro_trigger_button", KeyEvent.KEYCODE_BUTTON_L1);
        Log.d("SettingsFragment", "Selected button keycode: " + selectedButton);

// Find the index of the selectedButton in the keycodes array
        int selectedIndex = -1;
        for (int i = 0; i < keycodes.length; i++) {
            if (keycodes[i] == selectedButton) {
                selectedIndex = i;
                break;
            }
        }

// Ensure a valid index is found, otherwise, handle fallback
        if (selectedIndex != -1) {
            Log.d("SettingsFragment", "Selected button found at index: " + selectedIndex);
            sbGyroTriggerButton.setSelection(selectedIndex);
        } else {
            Log.e("SettingsFragment", "Selected button not found in keycodes array!");
            // If needed, handle the case where the button is not found (you can choose to throw an exception or show an error)
        }


        rgGyroMode.check(selectedMode == 0 ? R.id.RBHoldMode : R.id.RBToggleMode);


        // Configure Frontend Export Path button

        Button btnChooseFrontendExportPath = view.findViewById(R.id.BTChooseFrontendExportPath);
        TextView tvFrontendExportPath = view.findViewById(R.id.TVFrontendExportPath);

        // Get the saved export path from SharedPreferences or use the default
        String savedUriString = preferences.getString("frontend_export_uri", null);
        if (savedUriString == null) {
            // No saved path, set default path
            tvFrontendExportPath.setText(DEFAULT_EXPORT_PATH);
        } else {
            // Parse and display the saved URI path
            Uri savedUri = Uri.parse(savedUriString);
            String displayPath = FileUtils.getFilePathFromUri(getContext(), savedUri);
            tvFrontendExportPath.setText(displayPath != null ? displayPath : savedUriString);
        }

        // Set the click listener for the "Choose Frontend Export Path" button
        btnChooseFrontendExportPath.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE); // Launch File Picker for directory selection
            startActivityForResult(intent, REQUEST_CODE_FRONTEND_EXPORT_PATH);
        });

        final Spinner sBox86Version = view.findViewById(R.id.SBox86Version);
        String box86Version = preferences.getString("box86_version", DefaultVersion.BOX86);
        if (!AppUtils.setSpinnerSelectionFromIdentifier(sBox86Version, box86Version)) {
            AppUtils.setSpinnerSelectionFromIdentifier(sBox86Version, DefaultVersion.BOX86);
        }

        final Spinner sBox64Version = view.findViewById(R.id.SBox64Version);
        String box64Version = preferences.getString("box64_version", DefaultVersion.BOX64);

        ContentsManager contentsManager = new ContentsManager(context);
        contentsManager.syncContents();
        loadBox64VersionSpinner(context, contentsManager, sBox64Version);

        if (!AppUtils.setSpinnerSelectionFromIdentifier(sBox64Version, box64Version)) {
            AppUtils.setSpinnerSelectionFromIdentifier(sBox64Version, DefaultVersion.BOX64);
        }

        final Spinner sBox86Preset = view.findViewById(R.id.SBox86Preset);
        final Spinner sBox64Preset = view.findViewById(R.id.SBox64Preset);
        loadBox86_64PresetSpinners(view, sBox86Preset, sBox64Preset);

        final Spinner sMIDISoundFont = view.findViewById(R.id.SMIDISoundFont);

        sMIDISoundFont.setPopupBackgroundResource(isDarkMode ? R.drawable.content_dialog_background_dark : R.drawable.content_dialog_background);

        final View btInstallSF = view.findViewById(R.id.BTInstallSF);
        final View btRemoveSF = view.findViewById(R.id.BTRemoveSF);

        MidiManager.loadSFSpinnerWithoutDisabled(sMIDISoundFont);
        btInstallSF.setOnClickListener(v -> {
            installSoundFontCallback = uri -> {
                PreloaderDialog dialog = new PreloaderDialog(requireActivity());
                dialog.showOnUiThread(R.string.installing_content);
                MidiManager.installSF2File(context, uri, new MidiManager.OnSoundFontInstalledCallback() {
                    @Override
                    public void onSuccess() {
                        dialog.closeOnUiThread();
                        requireActivity().runOnUiThread(() -> {
                            ContentDialog.alert(context, R.string.sound_font_installed_success, null);
                            MidiManager.loadSFSpinnerWithoutDisabled(sMIDISoundFont);
                        });
                    }

                    @Override
                    public void onFailed(int reason) {
                        dialog.closeOnUiThread();
                        int resId = switch (reason) {
                            case MidiManager.ERROR_BADFORMAT -> R.string.sound_font_bad_format;
                            case MidiManager.ERROR_EXIST -> R.string.sound_font_already_exist;
                            default -> R.string.sound_font_installed_failed;
                        };
                        requireActivity().runOnUiThread(() -> ContentDialog.alert(context, resId, null));
                    }
                });
            };

            // Open the file picker with the request code for SoundFont installation
            openFile(REQUEST_CODE_INSTALL_SOUNDFONT);
        });

        btRemoveSF.setOnClickListener(v -> {
            if (sMIDISoundFont.getSelectedItemPosition() != 0) {
                ContentDialog.confirm(context, R.string.do_you_want_to_remove_this_sound_font, () -> {
                    if (MidiManager.removeSF2File(context, sMIDISoundFont.getSelectedItem().toString())) {
                        AppUtils.showToast(context, R.string.sound_font_removed_success);
                        MidiManager.loadSFSpinnerWithoutDisabled(sMIDISoundFont);
                    } else
                        AppUtils.showToast(context, R.string.sound_font_removed_failed);
                });
            } else
                AppUtils.showToast(context, R.string.cannot_remove_default_sound_font);
        });

        final CheckBox cbUseDRI3 = view.findViewById(R.id.CBUseDRI3);
        cbUseDRI3.setChecked(preferences.getBoolean("use_dri3", true));

        final CheckBox cbEnableWineDebug = view.findViewById(R.id.CBEnableWineDebug);
        cbEnableWineDebug.setChecked(preferences.getBoolean("enable_wine_debug", false));

        final ArrayList<String> wineDebugChannels = new ArrayList<>(Arrays.asList(preferences.getString("wine_debug_channels", DEFAULT_WINE_DEBUG_CHANNELS).split(",")));
        loadWineDebugChannels(view, wineDebugChannels);

        final CheckBox cbEnableBox86_64Logs = view.findViewById(R.id.CBEnableBox86_64Logs);
        cbEnableBox86_64Logs.setChecked(preferences.getBoolean("enable_box86_64_logs", false));

        final TextView tvCursorSpeed = view.findViewById(R.id.TVCursorSpeed);
        final SeekBar sbCursorSpeed = view.findViewById(R.id.SBCursorSpeed);
        sbCursorSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvCursorSpeed.setText(progress+"%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        sbCursorSpeed.setProgress((int)(preferences.getFloat("cursor_speed", 1.0f) * 100));

        final RadioGroup rgTriggerType = view.findViewById(R.id.RGTriggerType);
        final View btHelpTriggerMode = view.findViewById(R.id.BTHelpTriggerMode);
        List<Integer> triggerRbIds = List.of(R.id.RBTriggerIsButton, R.id.RBTriggerIsAxis, R.id.RBTriggerIsMixed);
        int triggerType = preferences.getInt("trigger_type", ExternalController.TRIGGER_IS_AXIS);

        if (triggerType >= 0 && triggerType < triggerRbIds.size()) {
            ((RadioButton) (rgTriggerType.findViewById(triggerRbIds.get(triggerType)))).setChecked(true);
        }
        btHelpTriggerMode.setOnClickListener(v -> AppUtils.showHelpBox(context, v, R.string.help_trigger_mode));

        final CheckBox cbUseGlibc = view.findViewById(R.id.CBUseGlibc);
        cbUseGlibc.setChecked(preferences.getBoolean("use_glibc", true));
        cbUseGlibc.setEnabled(false);

        final CheckBox cbEnableFileProvider = view.findViewById(R.id.CBEnableFileProvider);
        final View btHelpFileProvider = view.findViewById(R.id.BTHelpFileProvider);

        cbEnableFileProvider.setChecked(preferences.getBoolean("enable_file_provider", true));
        cbEnableFileProvider.setOnClickListener(v -> AppUtils.showToast(context, R.string.take_effect_next_startup));
        btHelpFileProvider.setOnClickListener(v -> AppUtils.showHelpBox(context, v, R.string.help_file_provider));

        loadInstalledWineList(view);

        view.findViewById(R.id.BTSelectWineFile).setOnClickListener((v) -> {
            ContentDialog.alert(context, R.string.msg_warning_install_wine, this::selectWineFileForInstall);
        });

        view.findViewById(R.id.BTReInstallImagefs).setOnClickListener(v -> {
            ContentDialog.confirm(context, R.string.do_you_want_to_reinstall_imagefs, () -> ImageFsInstaller.installFromAssets((MainActivity) getActivity()));
        });

        // Add backup button
        Button btnBackupData = view.findViewById(R.id.BTBackupData);
        btnBackupData.setOnClickListener(v -> {
            showBackupConfirmationDialog();
        });

        // Add restore button
        Button btnRestoreData = view.findViewById(R.id.BTRestoreData);
        btnRestoreData.setOnClickListener(v -> {
            selectBackupFileForRestore();
        });

        int finalSelectedIndex = selectedIndex;
        view.findViewById(R.id.BTConfirm).setOnClickListener((v) -> {
            SharedPreferences.Editor editor = preferences.edit();

            // Save Dark Mode setting
            editor.putBoolean("dark_mode", cbDarkMode.isChecked());

            editor.putString("box86_version", StringUtils.parseIdentifier(sBox86Version.getSelectedItem()));
            editor.putString("box64_version", StringUtils.parseIdentifier(sBox64Version.getSelectedItem()));
            editor.putString("box86_preset", Box86_64PresetManager.getSpinnerSelectedId(sBox86Preset));
            editor.putString("box64_preset", Box86_64PresetManager.getSpinnerSelectedId(sBox64Preset));
            editor.putBoolean("use_dri3", cbUseDRI3.isChecked());
            editor.putFloat("cursor_speed", sbCursorSpeed.getProgress() / 100.0f);
            editor.putBoolean("enable_wine_debug", cbEnableWineDebug.isChecked());
            editor.putBoolean("enable_box86_64_logs", cbEnableBox86_64Logs.isChecked());
            editor.putInt("trigger_type", triggerRbIds.indexOf(rgTriggerType.getCheckedRadioButtonId()));
            editor.putBoolean("use_glibc", cbUseGlibc.isChecked());
            editor.putBoolean("cursor_lock", cbCursorLock.isChecked()); // Save cursor lock state
            editor.putBoolean("xinput_toggle", cbXinputToggle.isChecked()); // Save xinput toggle state
            editor.putBoolean("touchscreen_toggle", cbXTouchscreenToggle.isChecked()); // Save touchscreen toggle state
            editor.putBoolean("enable_file_provider", cbEnableFileProvider.isChecked());

            // Save gyro settings
            editor.putBoolean("gyro_enabled", cbGyroEnabled.isChecked());
//            editor.putBoolean("process_gyro_with_left_trigger", cbProcessGyroWithLeftTrigger.isChecked());

            int selectedKeycode = keycodes[sbGyroTriggerButton.getSelectedItemPosition()];

// Save the selected keycode to preferences
            editor.putInt("gyro_trigger_button", selectedKeycode);

            editor.putInt("gyro_mode", rgGyroMode.getCheckedRadioButtonId() == R.id.RBHoldMode ? 0 : 1);



            if (!wineDebugChannels.isEmpty()) {
                editor.putString("wine_debug_channels", String.join(",", wineDebugChannels));
            } else if (preferences.contains("wine_debug_channels")) {
                editor.remove("wine_debug_channels");
            }
            else if (preferences.contains("wine_debug_channels")) editor.remove("wine_debug_channels");

            editor.putBoolean("legacy_mode_enabled", enableLegacyInputMode); // Save the 7.1.2 legacy mode state

            // Save Big Picture Mode setting
            editor.putBoolean("enable_big_picture_mode", ((CheckBox) view.findViewById(R.id.CBEnableBigPictureMode)).isChecked());
            saveCustomApiKeySettings(editor);

            if (editor.commit()) {
                // Now perform the extraction based on the saved state

                extractLegacyInputFiles(enableLegacyInputMode);



                NavigationView navigationView = getActivity().findViewById(R.id.NavigationView);
                navigationView.setCheckedItem(R.id.main_menu_containers);
                FragmentManager fragmentManager = getParentFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.FLFragmentContainer, new ContainersFragment())
                        .commit();
            }
        });



        return view;
    }

    private void updateTheme(boolean isDarkMode) {
        if (isDarkMode) {
            getActivity().setTheme(R.style.AppTheme_Dark);
        } else {
            getActivity().setTheme(R.style.AppTheme);
        }

        // Recreate the activity to apply the new theme
        getActivity().recreate();
    }


    private void applyDynamicStyles(View view, boolean isDarkMode) {

        Spinner sBox86Version = view.findViewById(R.id.SBox86Version);
        sBox86Version.setPopupBackgroundResource(isDarkMode ? R.drawable.content_dialog_background_dark : R.drawable.content_dialog_background);

        Spinner sBox64Version = view.findViewById(R.id.SBox64Version);
        sBox64Version.setPopupBackgroundResource(isDarkMode ? R.drawable.content_dialog_background_dark : R.drawable.content_dialog_background);

        Spinner sBox86Preset = view.findViewById(R.id.SBox86Preset);
        sBox86Preset.setPopupBackgroundResource(isDarkMode ? R.drawable.content_dialog_background_dark : R.drawable.content_dialog_background);

        Spinner sBox64Preset = view.findViewById(R.id.SBox64Preset);
        sBox64Preset.setPopupBackgroundResource(isDarkMode ? R.drawable.content_dialog_background_dark : R.drawable.content_dialog_background);

    }

    private void applyDynamicStylesRecursively(View view) {

        // Find TextViews by ID and apply dynamic styles
        TextView installedWineLabel = view.findViewById(R.id.TVInstalledWine);
        applyFieldSetLabelStyle(installedWineLabel, isDarkMode);

        TextView box86box64Label = view.findViewById(R.id.TVBox86Box64);
        applyFieldSetLabelStyle(box86box64Label, isDarkMode);

        TextView soundLabel = view.findViewById(R.id.TVSound);
        applyFieldSetLabelStyle(soundLabel, isDarkMode);

        TextView themeLabel = view.findViewById(R.id.TVTheme);
        applyFieldSetLabelStyle(themeLabel, isDarkMode);

        TextView shortcutSettingsLabel = view.findViewById(R.id.TVShortcutSettings);
        applyFieldSetLabelStyle(shortcutSettingsLabel, isDarkMode);

        TextView bigPictureModeLabel = view.findViewById(R.id.TVBigPictureMode);
        applyFieldSetLabelStyle(bigPictureModeLabel, isDarkMode);

        TextView tvCustomApiKey = view.findViewById(R.id.TVCustomApiKey);
        applyFieldSetLabelStyle(tvCustomApiKey, isDarkMode);

//        TextView shortcutSettingsLabel = view.findViewById(R.id.TVShortcutSettings);
//        applyFieldSetLabelStyle(shortcutSettingsLabel, isDarkMode);

        // Inputs tab labels
        TextView xServerLabel = view.findViewById(R.id.TVXServer);
        applyFieldSetLabelStyle(xServerLabel, isDarkMode);

        TextView gyroSettingsLabel = view.findViewById(R.id.TVGyroSettings);
        applyFieldSetLabelStyle(gyroSettingsLabel, isDarkMode);

        TextView gameControllerLabel = view.findViewById(R.id.TVGameControllerLabel);
        applyFieldSetLabelStyle(gameControllerLabel, isDarkMode);

        // Advanced tab labels
        TextView logsLabel = view.findViewById(R.id.TVLogs);
        applyFieldSetLabelStyle(logsLabel, isDarkMode);

        TextView experimentalLabel = view.findViewById(R.id.TVExperimental);
        applyFieldSetLabelStyle(experimentalLabel, isDarkMode);

        TextView ImageFsLabel = view.findViewById(R.id.TVImageFs);
        applyFieldSetLabelStyle(ImageFsLabel, isDarkMode);

    }

    private void applyFieldSetLabelStyle(TextView textView, boolean isDarkMode) {
//        Context context = textView.getContext();

        if (isDarkMode) {
            // Apply dark mode-specific attributes
            textView.setTextColor(Color.parseColor("#cccccc")); // Set text color to #cccccc
            textView.setBackgroundResource(R.color.window_background_color_dark); // Set dark background color
        } else {
            // Apply light mode-specific attributes (original FieldSetLabel)
            textView.setTextColor(Color.parseColor("#bdbdbd")); // Set text color to #bdbdbd
            textView.setBackgroundResource(R.color.window_background_color); // Set light background color
        }
    }

    private void initCustomApiKeySettings(View view) {
        cbEnableCustomApiKey = view.findViewById(R.id.CBEnableCustomApiKey);
        etCustomApiKey = view.findViewById(R.id.ETCustomApiKey);

        // Load saved preferences
        boolean isCustomApiKeyEnabled = preferences.getBoolean("enable_custom_api_key", false);
        String customApiKey = preferences.getString("custom_api_key", "");

        cbEnableCustomApiKey.setChecked(isCustomApiKeyEnabled);
        etCustomApiKey.setText(customApiKey);

        // Show/hide the EditText based on checkbox state
        etCustomApiKey.setVisibility(isCustomApiKeyEnabled ? View.VISIBLE : View.GONE);

        cbEnableCustomApiKey.setOnCheckedChangeListener((buttonView, isChecked) -> {
            etCustomApiKey.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        // Help button listener to open API documentation
        view.findViewById(R.id.BTHelpApiKey).setOnClickListener(v -> {
            String url = "https://www.steamgriddb.com/profile/preferences/api";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        });
    }

    private void saveCustomApiKeySettings(SharedPreferences.Editor editor) {
        // Save custom API key preferences
        boolean isCustomApiKeyEnabled = cbEnableCustomApiKey.isChecked();
        editor.putBoolean("enable_custom_api_key", isCustomApiKeyEnabled);

        if (isCustomApiKeyEnabled) {
            String customApiKey = etCustomApiKey.getText().toString().trim();
            editor.putString("custom_api_key", customApiKey);
        } else {
            editor.remove("custom_api_key");
        }
    }

    private void loadBox86_64PresetSpinners(View view, final Spinner sBox86Preset, final Spinner sBox64Preset) {
        final ArrayMap<String, Spinner> spinners = new ArrayMap<String, Spinner>() {{
            put("box86", sBox86Preset);
            put("box64", sBox64Preset);
        }};
        final Context context = getContext();

        Callback<String> updateSpinner = (prefix) -> {
            Box86_64PresetManager.loadSpinner(prefix, spinners.get(prefix), preferences.getString(prefix+"_preset", Box86_64Preset.COMPATIBILITY));
        };

        Callback<String> onAddPreset = (prefix) -> {
            Box86_64EditPresetDialog dialog = new Box86_64EditPresetDialog(context, prefix, null);
            dialog.setOnConfirmCallback(() -> updateSpinner.call(prefix));
            dialog.show();
        };

        Callback<String> onEditPreset = (prefix) -> {
            Box86_64EditPresetDialog dialog = new Box86_64EditPresetDialog(context, prefix, Box86_64PresetManager.getSpinnerSelectedId(spinners.get(prefix)));
            dialog.setOnConfirmCallback(() -> updateSpinner.call(prefix));
            dialog.show();
        };

        Callback<String> onDuplicatePreset = (prefix) -> ContentDialog.confirm(context, R.string.do_you_want_to_duplicate_this_preset, () -> {
            Spinner spinner = spinners.get(prefix);
            Box86_64PresetManager.duplicatePreset(prefix, context, Box86_64PresetManager.getSpinnerSelectedId(spinner));
            updateSpinner.call(prefix);
            spinner.setSelection(spinner.getCount()-1);
        });

        Callback<String> onRemovePreset = (prefix) -> {
            final String presetId = Box86_64PresetManager.getSpinnerSelectedId(spinners.get(prefix));
            if (!presetId.startsWith(Box86_64Preset.CUSTOM)) {
                AppUtils.showToast(context, R.string.you_cannot_remove_this_preset);
                return;
            }
            ContentDialog.confirm(context, R.string.do_you_want_to_remove_this_preset, () -> {
                Box86_64PresetManager.removePreset(prefix, context, presetId);
                updateSpinner.call(prefix);
            });
        };

        updateSpinner.call("box86");
        updateSpinner.call("box64");

        view.findViewById(R.id.BTAddBox86Preset).setOnClickListener((v) -> onAddPreset.call("box86"));
        view.findViewById(R.id.BTEditBox86Preset).setOnClickListener((v) -> onEditPreset.call("box86"));
        view.findViewById(R.id.BTDuplicateBox86Preset).setOnClickListener((v) -> onDuplicatePreset.call("box86"));
        view.findViewById(R.id.BTRemoveBox86Preset).setOnClickListener((v) -> onRemovePreset.call("box86"));

        view.findViewById(R.id.BTAddBox64Preset).setOnClickListener((v) -> onAddPreset.call("box64"));
        view.findViewById(R.id.BTEditBox64Preset).setOnClickListener((v) -> onEditPreset.call("box64"));
        view.findViewById(R.id.BTDuplicateBox64Preset).setOnClickListener((v) -> onDuplicatePreset.call("box64"));
        view.findViewById(R.id.BTRemoveBox64Preset).setOnClickListener((v) -> onRemovePreset.call("box64"));
    }

    private void removeInstalledWine(WineInfo wineInfo, Runnable onSuccess) {
        final Activity activity = getActivity();
        ContainerManager manager = new ContainerManager(activity);

        ArrayList<Container> containers = manager.getContainers();
        for (Container container : containers) {
            if (container.getWineVersion().equals(wineInfo.identifier())) {
                AppUtils.showToast(activity, R.string.unable_to_remove_this_wine_version);
                return;
            }
        }

        String suffix = wineInfo.fullVersion()+"-"+wineInfo.getArch();
        File installedWineDir = ImageFs.find(activity).getInstalledWineDir();
        File wineDir = new File(wineInfo.path);
        File containerPatternFile = new File(installedWineDir, "container-pattern-"+suffix+".tzst");

        if (!wineDir.isDirectory() || !containerPatternFile.isFile()) {
            AppUtils.showToast(activity, R.string.unable_to_remove_this_wine_version);
            return;
        }

        preloaderDialog.show(R.string.removing_wine);
        Executors.newSingleThreadExecutor().execute(() -> {
            FileUtils.delete(wineDir);
            FileUtils.delete(containerPatternFile);
            preloaderDialog.closeOnUiThread();
            if (onSuccess != null) activity.runOnUiThread(onSuccess);
        });
    }

    private void loadInstalledWineList(final View view) {
        Context context = getContext();
        LinearLayout container = view.findViewById(R.id.LLInstalledWineList);
        container.removeAllViews();
        ArrayList<WineInfo> wineInfos = WineUtils.getInstalledWineInfos(context);

        LayoutInflater inflater = LayoutInflater.from(context);
        for (final WineInfo wineInfo : wineInfos) {
            View itemView = inflater.inflate(R.layout.installed_wine_list_item, container, false);
            ((TextView)itemView.findViewById(R.id.TVTitle)).setText(wineInfo.toString());
            if (wineInfo != WineInfo.MAIN_WINE_VERSION) {
                View removeButton = itemView.findViewById(R.id.BTRemove);
                removeButton.setVisibility(View.VISIBLE);
                removeButton.setOnClickListener((v) -> {
                    ContentDialog.confirm(getContext(), R.string.do_you_want_to_remove_this_wine_version, () -> {
                        removeInstalledWine(wineInfo, () -> loadInstalledWineList(view));
                    });
                });
            }
            container.addView(itemView);
        }
    }

    private void selectWineFileForInstall() {
        final Context context = getContext();
        selectWineFileCallback = (uri) -> {
            preloaderDialog.show(R.string.preparing_installation);
            WineUtils.extractWineFileForInstallAsync(context, uri, (wineDir) -> {
                if (wineDir != null) {
                    WineUtils.findWineVersionAsync(context, wineDir, (wineInfo) -> {
                        preloaderDialog.closeOnUiThread();
                        if (wineInfo == null) {
                            AppUtils.showToast(context, R.string.unable_to_install_wine);
                            return;
                        }

                        getActivity().runOnUiThread(() -> showWineInstallOptionsDialog(wineInfo));
                    });
                }
                else {
                    AppUtils.showToast(context, R.string.unable_to_install_wine);
                    preloaderDialog.closeOnUiThread();
                }
            });
        };
        openFile(MainActivity.OPEN_FILE_REQUEST_CODE);
    }


    private void openFile(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");

        // Start activity for result based on the provided request code
        getActivity().startActivityFromFragment(this, intent, requestCode);
    }


    private void installWine(final WineInfo wineInfo) {
        Context context = getContext();
        File installedWineDir = ImageFs.find(context).getInstalledWineDir();

        File wineDir = new File(installedWineDir, wineInfo.identifier());
        if (wineDir.isDirectory()) {
            AppUtils.showToast(context, R.string.unable_to_install_wine);
            return;
        }

        Intent intent = new Intent(context, XServerDisplayActivity.class);
        intent.putExtra("generate_wineprefix", true);
        intent.putExtra("wine_info", wineInfo);
        context.startActivity(intent);
    }

    private void showWineInstallOptionsDialog(final WineInfo wineInfo) {
        Context context = getContext();
        ContentDialog dialog = new ContentDialog(context, R.layout.wine_install_options_dialog);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setTitle(R.string.install_wine);
        dialog.setIcon(R.drawable.icon_wine);

        EditText etVersion = dialog.findViewById(R.id.ETVersion);
        etVersion.setText("Wine "+wineInfo.version+(wineInfo.subversion != null ? " ("+wineInfo.subversion+")" : ""));

        Spinner sArch = dialog.findViewById(R.id.SArch);
        List<String> archList = wineInfo.isWin64() ? Arrays.asList("x86", "x86_64") : Arrays.asList("x86");
        sArch.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, archList));
        sArch.setSelection(archList.size()-1);

        dialog.setOnConfirmCallback(() -> {
            wineInfo.setArch(sArch.getSelectedItem().toString());
            installWine(wineInfo);
        });
        dialog.show();
    }

    private void loadWineDebugChannels(final View view, final ArrayList<String> debugChannels) {
        final Context context = getContext();
        LinearLayout container = view.findViewById(R.id.LLWineDebugChannels);
        container.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.wine_debug_channel_list_item, container, false);
        itemView.findViewById(R.id.TextView).setVisibility(View.GONE);
        itemView.findViewById(R.id.BTRemove).setVisibility(View.GONE);

        View addButton = itemView.findViewById(R.id.BTAdd);
        addButton.setVisibility(View.VISIBLE);
        addButton.setOnClickListener((v) -> {
            JSONArray jsonArray = null;
            try {
                jsonArray = new JSONArray(FileUtils.readString(context, "wine_debug_channels.json"));
            }
            catch (JSONException e) {}

            final String[] items = ArrayUtils.toStringArray(jsonArray);
            ContentDialog.showMultipleChoiceList(context, R.string.wine_debug_channel, items, (selectedPositions) -> {
                for (int selectedPosition : selectedPositions) if (!debugChannels.contains(items[selectedPosition])) debugChannels.add(items[selectedPosition]);
                loadWineDebugChannels(view, debugChannels);
            });
        });

        View resetButton = itemView.findViewById(R.id.BTReset);
        resetButton.setVisibility(View.VISIBLE);
        resetButton.setOnClickListener((v) -> {
            debugChannels.clear();
            debugChannels.addAll(Arrays.asList(DEFAULT_WINE_DEBUG_CHANNELS.split(",")));
            loadWineDebugChannels(view, debugChannels);
        });
        container.addView(itemView);

        for (int i = 0; i < debugChannels.size(); i++) {
            itemView = inflater.inflate(R.layout.wine_debug_channel_list_item, container, false);
            TextView textView = itemView.findViewById(R.id.TextView);
            textView.setText(debugChannels.get(i));
            final int index = i;
            itemView.findViewById(R.id.BTRemove).setOnClickListener((v) -> {
                debugChannels.remove(index);
                loadWineDebugChannels(view, debugChannels);
            });
            container.addView(itemView);
        }
    }

    public static void resetBox86_64Version(AppCompatActivity activity) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("box86_version", DefaultVersion.BOX86);
        editor.putString("box64_version", DefaultVersion.BOX64);
        editor.remove("current_box86_version");
        editor.remove("current_box64_version");
        editor.apply();
    }

    public static void loadBox64VersionSpinner(Context context, ContentsManager manager, Spinner spinner) {
        String[] originalItems = context.getResources().getStringArray(R.array.box64_version_entries);
        List<String> itemList = new ArrayList<>(Arrays.asList(originalItems));
        for (ContentProfile profile : manager.getProfiles(ContentProfile.ContentType.CONTENT_TYPE_BOX64)) {
            String entryName = ContentsManager.getEntryName(profile);
            int firstDashIndex = entryName.indexOf('-');
            itemList.add(entryName.substring(firstDashIndex + 1));
        }
        spinner.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, itemList));
    }

    private void showGyroConfigDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.gyro_config_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(dialogView);
        builder.setTitle("Gyroscope Configuration");

        // Initialize InputControlsView and configure it for displaying the stick
        InputControlsView inputControlsView = new InputControlsView(getContext(), true);
        inputControlsView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        inputControlsView.setEditMode(false);  // Disable edit mode

        // Initialize the stick element and set its type to STICK
        inputControlsView.initializeStickElement(600, 250, 2.0f);
        inputControlsView.getStickElement().setType(ControlElement.Type.STICK); // Set the type to STICK


        // Add the InputControlsView to the placeholder in your dialog layout
        FrameLayout placeholder = dialogView.findViewById(R.id.stick_placeholder);
        placeholder.addView(inputControlsView);

        // Redraw the stick in InputControlsView
        inputControlsView.invalidate();

        // Initialize the "Reset Center" button
        Button btnResetCenter = dialogView.findViewById(R.id.btnResetCenter);
        btnResetCenter.setOnClickListener(v -> {
            // Reset the stick element's position to the center
            inputControlsView.resetStickPosition();
            inputControlsView.invalidate();  // Redraw the stick
        });

        // Initialize the UI elements in the dialog
        SeekBar sbGyroXSensitivity = dialogView.findViewById(R.id.SBGyroXSensitivity);
        SeekBar sbGyroYSensitivity = dialogView.findViewById(R.id.SBGyroYSensitivity);
        SeekBar sbGyroSmoothing = dialogView.findViewById(R.id.SBGyroSmoothing);
        SeekBar sbGyroDeadzone = dialogView.findViewById(R.id.SBGyroDeadzone);
        CheckBox cbInvertGyroX = dialogView.findViewById(R.id.CBInvertGyroX);
        CheckBox cbInvertGyroY = dialogView.findViewById(R.id.CBInvertGyroY);
        TextView tvGyroXSensitivity = dialogView.findViewById(R.id.TVGyroXSensitivity);
        TextView tvGyroYSensitivity = dialogView.findViewById(R.id.TVGyroYSensitivity);
        TextView tvGyroSmoothing = dialogView.findViewById(R.id.TVGyroSmoothing);
        TextView tvGyroDeadzone = dialogView.findViewById(R.id.TVGyroDeadzone);


        // Load current preferences
        sbGyroXSensitivity.setProgress((int) (preferences.getFloat("gyro_x_sensitivity", 1.0f) * 100));
        sbGyroYSensitivity.setProgress((int) (preferences.getFloat("gyro_y_sensitivity", 1.0f) * 100));
        sbGyroSmoothing.setProgress((int) (preferences.getFloat("gyro_smoothing", 0.9f) * 100));
        sbGyroDeadzone.setProgress((int) (preferences.getFloat("gyro_deadzone", 0.05f) * 100));
        cbInvertGyroX.setChecked(preferences.getBoolean("invert_gyro_x", false));
        cbInvertGyroY.setChecked(preferences.getBoolean("invert_gyro_y", false));

        // Update text views for SeekBars
        tvGyroXSensitivity.setText("X Sensitivity: " + sbGyroXSensitivity.getProgress() + "%");
        tvGyroYSensitivity.setText("Y Sensitivity: " + sbGyroYSensitivity.getProgress() + "%");
        tvGyroSmoothing.setText("Smoothing: " + sbGyroSmoothing.getProgress() + "%");
        tvGyroDeadzone.setText("Deadzone: " + sbGyroDeadzone.getProgress() + "%");

        // Listeners for SeekBars
        sbGyroXSensitivity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvGyroXSensitivity.setText("X Sensitivity: " + progress + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        sbGyroYSensitivity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvGyroYSensitivity.setText("Y Sensitivity: " + progress + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        sbGyroSmoothing.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvGyroSmoothing.setText("Smoothing: " + progress + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        sbGyroDeadzone.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvGyroDeadzone.setText("Deadzone: " + progress + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // SensorManager to handle gyroscope input and affect only the thumbstick position within a fixed radius
        SensorManager sensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
        Sensor gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

// Define variables for smoothing and deadzone
        final float[] smoothGyroX = {0};
        final float[] smoothGyroY = {0};
        float smoothingFactor = preferences.getFloat("gyro_smoothing", 0.9f);  // User-defined smoothing factor
        float gyroDeadzone = preferences.getFloat("gyro_deadzone", 0.05f);      // User-defined deadzone
        boolean invertGyroX = preferences.getBoolean("invert_gyro_x", false);   // User-defined inversion for X axis
        boolean invertGyroY = preferences.getBoolean("invert_gyro_y", false);   // User-defined inversion for Y axis
        float gyroSensitivityX = preferences.getFloat("gyro_x_sensitivity", 1.0f); // User-defined sensitivity for X axis
        float gyroSensitivityY = preferences.getFloat("gyro_y_sensitivity", 1.0f); // User-defined sensitivity for Y axis

        SensorEventListener gyroListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                float rawGyroX = event.values[0];  // Gyroscope X axis value
                float rawGyroY = event.values[1];  // Gyroscope Y axis value

                // Apply deadzone
                if (Math.abs(rawGyroX) < gyroDeadzone) rawGyroX = 0;
                if (Math.abs(rawGyroY) < gyroDeadzone) rawGyroY = 0;

                // Apply inversion
                if (invertGyroX) rawGyroX = -rawGyroX;
                if (invertGyroY) rawGyroY = -rawGyroY;

                // Apply sensitivity
                rawGyroX *= gyroSensitivityX;
                rawGyroY *= gyroSensitivityY;

                // Apply smoothing (exponential smoothing)
                smoothGyroX[0] = smoothGyroX[0] * smoothingFactor + rawGyroX * (1 - smoothingFactor);
                smoothGyroY[0] = smoothGyroY[0] * smoothingFactor + rawGyroY * (1 - smoothingFactor);

                // Define the outer stick's center as a fixed point (outer circle center)
                int stickCenterX = inputControlsView.getStickElement().getX(); // Base stick X (center of outer circle)
                int stickCenterY = inputControlsView.getStickElement().getY(); // Base stick Y (center of outer circle)
                int stickRadius = 100;  // Example radius (adjust as needed)

                // Calculate the new thumbstick (inner circle) position based on the smoothed gyro data
                float newX = inputControlsView.getStickElement().getCurrentPosition().x + smoothGyroX[0];
                float newY = inputControlsView.getStickElement().getCurrentPosition().y + smoothGyroY[0];

                // Calculate the distance between the new thumbstick position and the outer circle center
                float deltaX = newX - stickCenterX;
                float deltaY = newY - stickCenterY;
                float distance = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);

                // Constrain the inner circle within the outer circle's radius
                if (distance > stickRadius) {
                    float scaleFactor = stickRadius / distance;
                    newX = stickCenterX + deltaX * scaleFactor;
                    newY = stickCenterY + deltaY * scaleFactor;
                }

                // Update the thumbstick (inner circle) position, but keep the outer circle fixed
                inputControlsView.updateStickPosition(newX, newY);

                // Redraw InputControlsView to reflect the new thumbstick position
                inputControlsView.invalidate();
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {}
        };

        sensorManager.registerListener(gyroListener, gyroscopeSensor, SensorManager.SENSOR_DELAY_GAME);

        // Set up the dialog buttons
        builder.setPositiveButton("OK", (dialog, which) -> {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putFloat("gyro_x_sensitivity", sbGyroXSensitivity.getProgress() / 100.0f);
            editor.putFloat("gyro_y_sensitivity", sbGyroYSensitivity.getProgress() / 100.0f);
            editor.putFloat("gyro_smoothing", sbGyroSmoothing.getProgress() / 100.0f);
            editor.putFloat("gyro_deadzone", sbGyroDeadzone.getProgress() / 100.0f);
            editor.putBoolean("invert_gyro_x", cbInvertGyroX.isChecked());
            editor.putBoolean("invert_gyro_y", cbInvertGyroY.isChecked());
            editor.apply();
        });

        builder.setNegativeButton("Cancel", null);

        // Show the dialog
        builder.create().show();
    }



    private void showBackupConfirmationDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("Backup Data")
                .setMessage("Do you want to create a backup of the app's data directory?")
                .setPositiveButton("Yes", (dialog, which) -> backupAppData())
                .setNegativeButton("No", null)
                .show();
    }

    private void backupAppData() {
        File dataDir = getContext().getFilesDir().getParentFile(); // App's data directory
        File backupFile = new File(Environment.getExternalStorageDirectory(), "app_data_backup.tar");

        preloaderDialog.showOnUiThread(R.string.backing_up_data);

        int availableProcessors = Runtime.getRuntime().availableProcessors();
        ExecutorService compressionExecutor = Executors.newFixedThreadPool(availableProcessors);

        compressionExecutor.execute(() -> {
            try {
                TarCompressorUtils.archive(new File[]{dataDir}, backupFile, file -> {
                    // Exclude the problematic directory
                    String excludePath = "imagefs/tmp/.sysvshm";
                    return !file.getAbsolutePath().contains(excludePath);
                });
                getActivity().runOnUiThread(() -> {
                    preloaderDialog.closeOnUiThread();
                    AppUtils.showToast(getContext(), "Backup completed: " + backupFile.getPath());
                });
            } catch (Exception e) {
                getActivity().runOnUiThread(() -> {
                    preloaderDialog.closeOnUiThread();
                    AppUtils.showToast(getContext(), "Backup failed.");
                });
            }
        });
    }



    private void selectBackupFileForRestore() {
        isRestoreAction = true; // Set the flag to indicate a restore operation
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, MainActivity.OPEN_FILE_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();

            if (uri != null) {
                switch (requestCode) {
                    // Case for File Picker to restore data
                    case MainActivity.OPEN_FILE_REQUEST_CODE:
                        if (isRestoreAction) {
                            restoreAppData(uri);
                            isRestoreAction = false;  // Reset the flag
                        } else if (selectWineFileCallback != null) {
                            try {
                                selectWineFileCallback.call(uri);
                            } catch (Exception e) {
                                AppUtils.showToast(getContext(), R.string.unable_to_import_profile);
                            } finally {
                                selectWineFileCallback = null;
                            }
                        }
                        break;

                    // Case for FilePicker to select frontend export path
                    case REQUEST_CODE_FRONTEND_EXPORT_PATH:
                        // Save the selected URI as a string in SharedPreferences
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("frontend_export_uri", uri.toString());
                        editor.apply();

                        // Take persistable URI permission
                        try {
                            // Take persistable URI permission with explicit flags
                            requireContext().getContentResolver().takePersistableUriPermission(
                                    uri,
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                            );
                        } catch (SecurityException e) {
                            AppUtils.showToast(getContext(), "Unable to take persistable permissions: " + e.getMessage());
                        }

                        // Convert the URI to an absolute path and display it
                        String fullPath = FileUtils.getFilePathFromUri(getContext(), uri);

                        // Update the TextView with the absolute path or URI string if conversion fails
                        TextView tvFrontendExportPath = getView().findViewById(R.id.TVFrontendExportPath);
                        tvFrontendExportPath.setText(fullPath != null ? fullPath : uri.toString());
                        break;

                    // Case for installing a SoundFont
                    case REQUEST_CODE_INSTALL_SOUNDFONT:
                        if (installSoundFontCallback != null) {
                            try {
                                installSoundFontCallback.call(uri);
                            } catch (Exception e) {
                                AppUtils.showToast(getContext(), R.string.unable_to_install_soundfont);
                            } finally {
                                installSoundFontCallback = null;
                            }
                        }
                        break;

                    // Add future cases here for other request codes...
                    default:
                        break;
                }
            }
        }
    }






    private void restoreAppData(Uri backupUri) {
        if (getActivity() != null) {  // Ensure the activity is not null
            Intent intent = new Intent(getActivity(), RestoreActivity.class);
            intent.setData(backupUri);
            startActivity(intent);
            getActivity().finish(); // Close the main activity
        }
    }


    private void moveFiles(File sourceDir, File targetDir) throws IOException {
        File[] files = sourceDir.listFiles();
        if (files != null) {
            for (File file : files) {
                File targetFile = new File(targetDir, file.getName());
                if (file.isDirectory()) {
                    if (!targetFile.exists()) {
                        targetFile.mkdirs();
                    }
                    moveFiles(file, targetFile); // Recursively move directory contents
                } else {
                    if (!file.renameTo(targetFile)) {
                        throw new IOException("Failed to move file: " + file.getAbsolutePath());
                    }
                }
            }
        }
        // Clear the temporary directory after moving
        FileUtils.clear(sourceDir);
    }


    private void onRestoreSuccess() {
        getActivity().runOnUiThread(() -> {
            preloaderDialog.closeOnUiThread();
            AppUtils.showToast(getContext(), "Data restored successfully.");
            AppUtils.restartApplication(getActivity());  // Restart the app to apply changes
        });
    }

    private void onRestoreFailed() {
        getActivity().runOnUiThread(() -> {
            preloaderDialog.closeOnUiThread();
            AppUtils.showToast(getContext(), "Data restore failed.");
        });
    }

    private boolean extractLegacyInputFiles(boolean enableLegacyMode) {
        Context context = getContext();
        ImageFs imageFs = ImageFs.find(context);
        File destinationDir = imageFs.getRootDir(); // Assuming you want to extract into the rootDir

        // Determine the correct asset file name based on the mode
        String assetFileName = enableLegacyMode ? "lj2-7.1.2-xinputdlls.tzst" : "lj2-7.1.3-xinputdlls.tzst";

        // Set the compression type to ZSTD for .tzst files
        TarCompressorUtils.Type compressionType = TarCompressorUtils.Type.ZSTD;

        // Use the correct method to extract the asset file
        boolean extractionSuccess = TarCompressorUtils.extract(compressionType, context, assetFileName, destinationDir);

        // Log the result of the extraction process
        if (extractionSuccess) {
            String message = enableLegacyMode ? "7.1.2 legacy input files extracted successfully." : "7.1.3 input files extracted successfully.";
            Log.i("SettingsFragment", message); // Info log for successful extraction
        } else {
            Log.e("SettingsFragment", "Failed to extract input files."); // Error log for failed extraction
        }

        return extractionSuccess;
    }





}
