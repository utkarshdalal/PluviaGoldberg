package com.winlator.contentdialog;

import android.content.SharedPreferences;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import androidx.preference.PreferenceManager;

import com.winlator.R;
import com.winlator.box86_64.rc.RCFile;
import com.winlator.box86_64.rc.RCGroup;
import com.winlator.box86_64.rc.RCManager;
import com.winlator.core.Callback;

import java.util.ArrayList;
import java.util.List;

public class ImportGroupDialog extends ContentDialog {
    public ImportGroupDialog(View anchor, RCManager manager, Callback<RCGroup> callback) {
        super(anchor.getContext(), R.layout.box86_64_rc_groups_dialog);
        setIcon(R.drawable.icon_settings);
        setTitle(anchor.getContext().getString(R.string.import_group));

        final Spinner sProfile = findViewById(R.id.SProfile);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(anchor.getContext());
        boolean isDarkMode = prefs.getBoolean("dark_mode", false);
        sProfile.setPopupBackgroundResource(isDarkMode ? R.drawable.content_dialog_background_dark : R.drawable.content_dialog_background);

        final ListView lvGroup = findViewById(R.id.LVGroup);

        findViewById(R.id.BTConfirm).setVisibility(View.GONE);

        List<RCFile> rcfiles = manager.getRCFiles();
        List<String> rcfilesName = new ArrayList<>();
        for (RCFile rcfile : rcfiles)
            rcfilesName.add(rcfile.getName());
        sProfile.setAdapter(new ArrayAdapter<>(anchor.getContext(), android.R.layout.simple_spinner_dropdown_item, rcfilesName));
        sProfile.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                List<RCGroup> groups = rcfiles.get(position).getGroups();
                List<String> groupsName = new ArrayList<>();
                for (RCGroup group : groups)
                    groupsName.add(group.getGroupName());
                lvGroup.setAdapter(new ArrayAdapter<>(anchor.getContext(), android.R.layout.simple_list_item_1, groupsName));
                lvGroup.setOnItemClickListener((parent1, view1, position1, id1) -> {
                    RCGroup group = RCGroup.copy(groups.get(position1));
                    callback.call(group);
                    dismiss();
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        sProfile.setSelection(0, false);
    }


}
