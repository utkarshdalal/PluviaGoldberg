package com.winlator;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.winlator.box86_64.rc.RCField;
import com.winlator.box86_64.rc.RCGroup;
import com.winlator.box86_64.rc.RCItem;
import com.winlator.box86_64.rc.RCManager;
import com.winlator.box86_64.rc.RCFile;
import com.winlator.contentdialog.ContentDialog;
import com.winlator.contentdialog.ImportGroupDialog;
import com.winlator.core.AppUtils;
import com.winlator.core.Callback;
import com.winlator.core.FileUtils;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Box86_64RCFragment extends Fragment {
    private static final int FILTER_ALL = 0;
    private static final int FILTER_ONLY_ENABLED = 1;
    private static final int FILTER_ONLY_DISABLED = 2;
    private RecyclerView recyclerView;
    private RCManager manager;
    private RCFile currentRCFile;
    private String filterStr = "";
    private int filterMode = FILTER_ALL;
    private boolean resumedFromGroupFragment = false;
    private Spinner sRCFile;
    private Callback<RCFile> importRCFileCallback;

    private boolean isDarkMode;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
        manager = new RCManager(getContext());

        // Initialize isDarkMode based on shared preferences or theme
        isDarkMode = PreferenceManager.getDefaultSharedPreferences(getContext())
                .getBoolean("dark_mode", false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.box64_rc_file);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.box86_64_rc_fragment, container, false);

        sRCFile = layout.findViewById(R.id.SRCFile);
        loadRCFileSpinner(sRCFile);

        View btAddRCFile = layout.findViewById(R.id.BTAddRCFile);
        View btEditRCFile = layout.findViewById(R.id.BTEditRCFile);
        View btDuplicateRCFile = layout.findViewById(R.id.BTDuplicateRCFile);
        View btExportRCFile = layout.findViewById(R.id.BTExportRCFile);
        View btRemoveRCFile = layout.findViewById(R.id.BTRemoveRCFile);
        View btNewGroup = layout.findViewById(R.id.BTNewGroup);
        View btImportGroup = layout.findViewById(R.id.BTImportGroup);

        View[] buttons = {btAddRCFile, btEditRCFile, btDuplicateRCFile, btExportRCFile, btRemoveRCFile, btNewGroup, btImportGroup};
        for (View button : buttons)
            button.setOnClickListener(clickListener);

        RadioGroup rbgFilter = layout.findViewById(R.id.RBGFilter);
        rbgFilter.setOnCheckedChangeListener(checkedChangeListener);

        EditText etFilter = layout.findViewById(R.id.ETFilter);

        // Set the background resource based on isDarkMode
        etFilter.setBackgroundResource(isDarkMode ? R.drawable.edit_text_dark : R.drawable.edit_text);

        etFilter.addTextChangedListener(textWatcher);

        recyclerView = layout.findViewById(R.id.RecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
        return layout;
    }

    private void loadRCFileSpinner(Spinner spinner) {
        final List<RCFile> rcfiles = manager.getRCFiles();
        List<String> values = new ArrayList<>();
        values.add("-- " + getString(R.string.select_profile) + " --");

        for (int i = 0; i < rcfiles.size(); i++) {
            RCFile rcfile = rcfiles.get(i);
            values.add(rcfile.getName());
        }

        spinner.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, values));

        // Set popup background based on theme
        spinner.setPopupBackgroundResource(isDarkMode ? R.drawable.content_dialog_background_dark : R.drawable.content_dialog_background);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentRCFile = position > 0 ? rcfiles.get(position - 1) : null;
                loadRCGroupList();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        if (currentRCFile != null)
            spinner.setSelection(manager.getRCFiles().indexOf(currentRCFile) + 1);
        else
            spinner.setSelection(0);
    }

    private final View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.BTAddRCFile:
                    PopupMenu popupMenu = getPopupMenu(v);
                    popupMenu.show();
                    break;
                case R.id.BTEditRCFile:
                    if (currentRCFile != null) {
                        if (currentRCFile.id == 1) {
                            ContentDialog.confirm(getContext(), R.string.do_you_want_to_restore_default_profile, () -> {
                                manager.removeRCFile(currentRCFile);
                                FileUtils.copy(getContext(), "box86_64/rcfiles", RCManager.getRCFilesDir(getContext()));
                                manager.loadRCFiles();
                                currentRCFile = manager.getRcfile(1);
                                loadRCFileSpinner(sRCFile);
                                loadRCGroupList();
                            });
                            break;
                        }
                        ContentDialog.prompt(getContext(), R.string.profile_name, currentRCFile.getName(), (name) -> {
                            currentRCFile.setName(name);
                            currentRCFile.save();
                            loadRCFileSpinner(sRCFile);
                        });
                    } else AppUtils.showToast(getContext(), R.string.no_profile_selected);
                    break;
                case R.id.BTDuplicateRCFile:
                    if (currentRCFile != null) {
                        ContentDialog.confirm(getContext(), R.string.do_you_want_to_duplicate_this_profile, () -> {
                            manager.duplicateRCFile(currentRCFile);
                            loadRCFileSpinner(sRCFile);
                        });
                    } else AppUtils.showToast(getContext(), R.string.no_profile_selected);
                    break;
                case R.id.BTExportRCFile:
                    if (currentRCFile != null) {
                        File exportedFile = manager.exportRCFile(currentRCFile);
                        if (exportedFile != null) {
                            String path = exportedFile.getPath().substring(exportedFile.getPath().indexOf(Environment.DIRECTORY_DOWNLOADS));
                            AppUtils.showToast(getContext(), getContext().getString(R.string.profile_exported_to) + " " + path);
                        }
                    } else AppUtils.showToast(getContext(), R.string.no_profile_selected);
                    break;
                case R.id.BTRemoveRCFile:
                    if (currentRCFile != null) {
                        if (currentRCFile.id == 1) {
                            AppUtils.showToast(getContext(), R.string.cannot_remove_default_profile);
                            break;
                        }
                        ContentDialog.confirm(getContext(), R.string.do_you_want_to_remove_this_profile, () -> {
                            manager.removeRCFile(currentRCFile);
                            currentRCFile = null;
                            loadRCFileSpinner(sRCFile);
                            loadRCGroupList();
                        });
                    } else AppUtils.showToast(getContext(), R.string.no_profile_selected);
                    break;
                case R.id.BTNewGroup:
                    if (currentRCFile != null) {
                        ContentDialog.prompt(getContext(), R.string.group_name, null, (name) -> {
                            currentRCFile.getGroups().add(new RCGroup(name, "", true, null));
                            loadRCGroupList();
                        });
                    } else AppUtils.showToast(getContext(), R.string.no_profile_selected);
                    break;
                case R.id.BTImportGroup:
                    if (currentRCFile != null) {
                        ImportGroupDialog dialog = new ImportGroupDialog(v, manager, new Callback<RCGroup>() {
                            @Override
                            public void call(RCGroup group) {
                                currentRCFile.getGroups().add(group);
                                loadRCGroupList();
                            }
                        });
                        dialog.show();
                    } else AppUtils.showToast(getContext(), R.string.no_profile_selected);
                    break;
            }
        }
    };

    @NonNull
    private PopupMenu getPopupMenu(View v) {
        PopupMenu popupMenu = new PopupMenu(getContext(), v);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) popupMenu.setForceShowIcon(true);
        popupMenu.inflate(R.menu.rc_new_menu);
        popupMenu.setOnMenuItemClickListener((menuItem) -> {
            int itemId = menuItem.getItemId();
            if (itemId == R.id.create_file) {
                ContentDialog.prompt(getContext(), R.string.profile_name, null, (name) -> {
                    currentRCFile = manager.createRCFile(name);
                    loadRCFileSpinner(sRCFile);
                    loadRCGroupList();
                });
            } else if (itemId == R.id.open_file) {
                openRCFile();
            }
            return true;
        });
        return popupMenu;
    }

    private final RadioGroup.OnCheckedChangeListener checkedChangeListener = (group, checkedId) -> {
        if (checkedId == R.id.RBAll) filterMode = FILTER_ALL;
        else if (checkedId == R.id.RBEnabled) filterMode = FILTER_ONLY_ENABLED;
        else if (checkedId == R.id.RBDisabled) filterMode = FILTER_ONLY_DISABLED;
        loadRCGroupList();
    };

    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            filterStr = s.toString();
            loadRCGroupList();
        }
    };

    private void openRCFile() {
        importRCFileCallback = (importedRCFile) -> {
            currentRCFile = importedRCFile;
            loadRCFileSpinner(sRCFile);
            loadRCGroupList();
        };
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        getActivity().startActivityFromFragment(this, intent, MainActivity.OPEN_FILE_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == MainActivity.OPEN_FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            try {
                RCFile importedProfile = RCManager.loadRCFile(getContext(), new JSONObject(FileUtils.readString(getContext(), data.getData())));
                if (importedProfile != null) {
                    importedProfile = manager.duplicateRCFile(importedProfile);
                    importRCFileCallback.call(importedProfile);
                }
            } catch (Exception e) {
                AppUtils.showToast(getContext(), R.string.unable_to_import_profile);
            }
            importRCFileCallback = null;
        }
    }

    private boolean passFilter(RCGroup group) {
        if (!group.getGroupName().contains(filterStr)) return false;
        if (filterMode == FILTER_ALL) return true;
        else if (filterMode == FILTER_ONLY_ENABLED && group.isEnabled()) return true;
        else return filterMode == FILTER_ONLY_DISABLED && !group.isEnabled();
    }

    public void loadRCGroupList() {
        List<RCGroup> groups = new LinkedList<>();
        if (currentRCFile != null) {
            for (RCGroup group : currentRCFile.getGroups())
                if (passFilter(group)) groups.add(group);
        }
        recyclerView.setAdapter(new RCGroupAdapter(groups));
    }

    private class RCGroupAdapter extends RecyclerView.Adapter<RCGroupAdapter.ViewHolder> {
        private final List<RCGroup> data;

        private static class ViewHolder extends RecyclerView.ViewHolder {
            private final TextView tvTitle;
            private final ToggleButton tbEnabled;
            private final ImageButton btMenu;

            private ViewHolder(View view) {
                super(view);
                tvTitle = view.findViewById(R.id.TVTitle);
                tbEnabled = view.findViewById(R.id.TBEnabled);
                btMenu = view.findViewById(R.id.BTMenu);
            }
        }

        public RCGroupAdapter(List<RCGroup> data) {
            this.data = data;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.box86_64_rc_list_item, parent, false));
        }

        @Override
        public void onViewRecycled(@NonNull ViewHolder holder) {
            holder.tvTitle.setOnClickListener(null);
            holder.tbEnabled.setOnCheckedChangeListener(null);
            holder.btMenu.setOnClickListener(null);
            super.onViewRecycled(holder);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            final RCGroup group = data.get(position);
            holder.tvTitle.setText(group.getGroupName());
            holder.tvTitle.setOnClickListener(v -> {
                resumedFromGroupFragment = true;
                getParentFragmentManager().beginTransaction().replace(R.id.FLFragmentContainer, new RCGroupFragment(group)).addToBackStack(null).commit();
            });

            holder.tbEnabled.setChecked(group.isEnabled());
            holder.tbEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> group.setEnabled(isChecked));

            holder.btMenu.setOnClickListener(v -> showListItemMenu(v, group));
        }

        @Override
        public final int getItemCount() {
            return data.size();
        }

        private void showListItemMenu(View anchorView, final RCGroup group) {
            final Context context = getContext();
            PopupMenu listItemMenu = new PopupMenu(context, anchorView);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) listItemMenu.setForceShowIcon(true);

            listItemMenu.inflate(R.menu.rc_group_menu);
            listItemMenu.setOnMenuItemClickListener((menuItem) -> {
                int itemId = menuItem.getItemId();
                if (itemId == R.id.remove_group) {
                    ContentDialog.confirm(getContext(), R.string.do_you_want_to_remove_this_group, () -> {
                        currentRCFile.getGroups().remove(group);
                        loadRCGroupList();
                    });
                } else if (itemId == R.id.duplicate_group) {
                    ContentDialog.confirm(getContext(), R.string.do_you_want_to_duplicate_this_group, () -> {
                        RCGroup groupNew = RCGroup.copy(group);
                        String nameNew;
                        for (int i = 1; ; i++) {
                            boolean ok = true;
                            nameNew = groupNew.getGroupName() + " (" + i + ")";
                            for (RCGroup g : currentRCFile.getGroups()) {
                                if (g.getGroupName().equals(nameNew)) {
                                    ok = false;
                                    break;
                                }
                            }
                            if (ok) break;
                        }
                        groupNew.setGroupName(nameNew);
                        currentRCFile.getGroups().add(groupNew);
                        loadRCGroupList();
                    });
                }
                return true;
            });
            listItemMenu.show();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        manager.saveAllRCFiles();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (resumedFromGroupFragment) {
            loadRCGroupList();
            resumedFromGroupFragment = false;
        }
    }

    public static class RCGroupFragment extends Fragment {
        private final RCGroup group;
        private RecyclerView recyclerView;
        private EditText etGroupName;

        public RCGroupFragment(RCGroup group) {
            this.group = group;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(false);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.group_edit);
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.box86_64_rc_group_fragment, container, false);

            etGroupName = layout.findViewById(R.id.ETGroupName);
            etGroupName.setText(group.getGroupName());

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            boolean isDarkMode = prefs.getBoolean("dark_mode", false);
            applyDarkThemeToEditText(etGroupName, isDarkMode);

            final View btNewItem = layout.findViewById(R.id.BTNewItem);
            btNewItem.setOnClickListener(v -> ContentDialog.prompt(getContext(), R.string.process_name, null, (name) -> {
                group.getItems().add(new RCItem(name, "", null));
                loadRCItemList();
            }));

            recyclerView = layout.findViewById(R.id.RecyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
            recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));

            loadRCItemList();
            return layout;
        }

        @Override
        public void onStop() {
            super.onStop();
            String nameNew = etGroupName.getText().toString();
            group.setGroupName(nameNew.isEmpty() ? group.getGroupName() : nameNew);
        }

        public void loadRCItemList() {
            List<RCItem> items = group.getItems();
            Collections.sort(items);
            recyclerView.setAdapter(new RCItemAdapter(items));
        }

        private class RCItemAdapter extends RecyclerView.Adapter<RCItemAdapter.ViewHolder> {
            private final List<RCItem> data;

            private static class ViewHolder extends RecyclerView.ViewHolder {
                private final LinearLayout llItem;
                private final TextView tvTitle;
                private final ToggleButton tbExpand;
                private final LinearLayout llItemTools;
                private final View[] buttons;
                private final LinearLayout llItemVars;
                private final ArrayList<Vars> varList = new ArrayList<>();

                private ViewHolder(View view) {
                    super(view);
                    llItem = view.findViewById(R.id.LLItem);
                    tvTitle = view.findViewById(R.id.TVTitle);
                    tbExpand = view.findViewById(R.id.TBExpand);
                    llItemTools = view.findViewById(R.id.LLItemTools);
                    ImageButton bAddVar = view.findViewById(R.id.BTAddVar);
                    ImageButton bEditItem = view.findViewById(R.id.BTEditItem);
                    ImageButton bDuplicateItem = view.findViewById(R.id.BTDuplicateItem);
                    ImageButton bRemoveItem = view.findViewById(R.id.BTRemoveItem);
                    buttons = new View[]{bAddVar, bEditItem, bDuplicateItem, bRemoveItem};
                    llItemVars = view.findViewById(R.id.LLItemVars);
                }
            }

            public RCItemAdapter(List<RCItem> data) {
                this.data = data;
            }

            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.box86_64_rc_group_list_item, parent, false));
            }

            @Override
            public void onViewRecycled(@NonNull ViewHolder holder) {
                holder.llItem.setOnClickListener(null);
                holder.tbExpand.setOnCheckedChangeListener(null);
                for (View v : holder.buttons)
                    v.setOnClickListener(null);
                super.onViewRecycled(holder);
            }

            @Override
            public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
                final RCItem item = data.get(position);
                holder.llItem.setOnClickListener(v -> holder.tbExpand.performClick());

                holder.tvTitle.setText(item.getProcessName());

                holder.tbExpand.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    int visibility = isChecked ? View.VISIBLE : View.GONE;
                    holder.llItemTools.setVisibility(visibility);
                    holder.llItemVars.setVisibility(visibility);
                });

                View.OnClickListener clickListener = v -> {
                    int id = v.getId();

                    if (id == R.id.BTAddVar) {
                        Vars newVar = new Vars();
                        newVar.key = "";
                        newVar.value = "";
                        newVar.buildView(getContext());
                        addListener(newVar, holder.varList, item);
                        holder.varList.add(newVar);
                        holder.llItemVars.addView(newVar.view);
                    } else if (id == R.id.BTEditItem) {
                        ContentDialog.prompt(getContext(), R.string.process_name, item.getProcessName(), (name) -> {
                            item.setProcessName(name);
                            holder.tvTitle.setText(name);
                        });
                    } else if (id == R.id.BTDuplicateItem) {
                        ContentDialog.confirm(getContext(), R.string.do_you_want_to_duplicate_this_process, () -> {
                            String processNameNew;
                            for (int i = 1; ; i++) {
                                processNameNew = item.getProcessName() + " (" + i + ")";
                                boolean ok = true;
                                for (RCItem rcItem : data) {
                                    if (rcItem.getProcessName().equals(processNameNew)) {
                                        ok = false;
                                        break;
                                    }
                                }
                                if (ok) break;
                            }
                            RCItem itemNew = RCItem.copy(item);
                            itemNew.setProcessName(processNameNew);
                            data.add(itemNew);
                            loadRCItemList();
                        });
                    } else if (id == R.id.BTRemoveItem) {
                        ContentDialog.confirm(getContext(), R.string.do_you_want_to_remove_this_process, () -> {
                            data.remove(item);
                            loadRCItemList();
                        });
                    }
                };
                for (View v : holder.buttons)
                    v.setOnClickListener(clickListener);

                for (String s : item.getVarMap().keySet()) {
                    Vars var = new Vars();
                    var.key = s;
                    var.value = item.getVarMap().get(var.key);
                    holder.varList.add(var);
                    var.buildView(getContext());
                    addListener(var, holder.varList, item);
                    holder.llItemVars.addView(var.view);
                }
                holder.llItemVars.setVisibility(View.GONE);
            }

            @Override
            public final int getItemCount() {
                return data.size();
            }

            private void addListener(Vars var, List<Vars> vars, RCItem item) {
                var.etKey.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        String text = s.toString();
                        if (text.isEmpty()) return;

                        if (text.contains(" ")) {
                            s.replace(0, s.length(), text.replace(" ", ""));
                            return;
                        }

                        if (item.getVarMap().containsKey(s.toString())) {
                            item.getVarMap().remove(var.key);
                        } else {
                            item.getVarMap().remove(var.key);
                            var.key = text;
                            item.getVarMap().put(var.key, var.value);
                        }
                    }
                });

                var.etValue.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if (item.getVarMap().containsKey(var.key)) {
                            var.value = s.toString();
                            item.getVarMap().put(var.key, var.value);
                        }
                    }
                });

                var.btRemove.setOnClickListener(v -> ContentDialog.confirm(getContext(), R.string.do_you_want_to_remove_this_variable, () -> {
                    item.getVarMap().remove(var.key);
                    vars.remove(var);
                    ((ViewGroup) (var.view.getParent())).removeView(var.view);
                }));

                var.btVarMenu.setOnClickListener(v -> {
                    String[] fields = RCField.getEnabledField();
                    ContentDialog.showSingleChoiceList(getContext(), R.string.variable_name, fields, object -> {
                        String name = fields[object];
                        if (item.getVarMap().containsKey(name))
                            AppUtils.showToast(getContext(), R.string.variable_already_exists);
                        else var.etKey.setText(name);
                    });
                });

                var.btValueMenu.setOnClickListener(v -> {
                    RCField field;
                    try {
                        field = RCField.valueOf(var.etKey.getText().toString());
                    } catch (Exception e) {
                        return;
                    }

                    if (!field.isEnabled())
                        return;

                    PopupMenu selectionMenu = new PopupMenu(getContext(), var.btValueMenu);
                    String[] selection = field.getSelections();
                    for (int i = 0; i < selection.length; i++)
                        selectionMenu.getMenu().add(Menu.NONE, i + 1, Menu.NONE, selection[i]);
                    selectionMenu.setOnMenuItemClickListener(item1 -> {
                        var.etValue.setText(selection[item1.getItemId() - 1]);
                        return true;
                    });
                    selectionMenu.show();

                });
            }

            private static class Vars {
                public View view;
                public String key;
                public String value;
                public EditText etKey;
                public EditText etValue;
                public View btRemove;
                public View btVarMenu;
                public View btValueMenu;

                public void buildView(Context context) {
                    LinearLayout layout = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.box86_64_rc_var, null);
                    etKey = layout.findViewById(R.id.ETKey);

                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                    boolean isDarkMode = prefs.getBoolean("dark_mode", false);
                    applyDarkThemeToEditText(etKey, isDarkMode);


                    etValue = layout.findViewById(R.id.ETValue);

                    applyDarkThemeToEditText(etValue, isDarkMode);

                    btRemove = layout.findViewById(R.id.BTRemoveVar);
                    btVarMenu = layout.findViewById(R.id.BTVarMenu);
                    btValueMenu = layout.findViewById(R.id.BTValueMenu);

                    etKey.setText(key);
                    etValue.setText(value);

                    view = layout;
                }
            }
        }
    }

    private static void applyDarkThemeToEditText(EditText editText, boolean isDarkMode) {
        if (isDarkMode) {
            editText.setTextColor(Color.WHITE); // Set text color to white for dark theme
            editText.setHintTextColor(Color.GRAY); // Set hint color to gray
            editText.setBackgroundResource(R.drawable.edit_text_dark); // Custom dark background drawable
        } else {
            editText.setTextColor(Color.BLACK); // Default text color
            editText.setHintTextColor(Color.GRAY); // Default hint color
            editText.setBackgroundResource(R.drawable.edit_text); // Custom light background drawable
        }
    }
}
