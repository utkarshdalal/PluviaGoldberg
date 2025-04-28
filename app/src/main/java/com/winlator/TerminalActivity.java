package com.winlator;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.winlator.R;
import com.winlator.core.FileUtils;
import com.winlator.xenvironment.ImageFs;
import com.winlator.xenvironment.XEnvironment;
import com.winlator.xenvironment.components.GlibcProgramLauncherComponent;
import com.winlator.contents.ContentsManager;

import java.io.File;

public class TerminalActivity extends AppCompatActivity {
    private TextView outputTextView;
    private EditText commandInput;
    private Button executeButton;
    private GlibcProgramLauncherComponent launcher;
    private XEnvironment xEnvironment;
    private ImageFs imageFs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terminal);

        outputTextView = findViewById(R.id.outputTextView);
        commandInput = findViewById(R.id.commandInput);
        executeButton = findViewById(R.id.executeButton);

        // Initialize ImageFs and XEnvironment
        imageFs = ImageFs.find(this);
        if (!imageFs.isValid()) {
            outputTextView.setText("Error: Invalid ImageFs.");
            return;
        }
        xEnvironment = new XEnvironment(this, imageFs);

        // Initialize ContentsManager and GlibcProgramLauncherComponent without Shortcut
        ContentsManager contentsManager = new ContentsManager(this);
        launcher = new GlibcProgramLauncherComponent(contentsManager, null, null);
        xEnvironment.addComponent(launcher);

        // Set execute permissions for all binaries in the bin folder
        setExecutePermissionsForBinaries();

        // Set up executeButton onClickListener
        executeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String command = commandInput.getText().toString();
                if (!command.isEmpty()) {
                    executeCommand(command);
                }
            }
        });
    }

    private void setExecutePermissionsForBinaries() {
        File binDir = new File(imageFs.getRootDir(), "usr/bin");
        if (binDir.isDirectory()) {
            File[] files = binDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    FileUtils.chmod(file, 0755); // Set permissions to make it executable
                }
            }
        }
    }

    // Modify `executeCommand` to handle specific commands first
    private void executeCommand(String command) {
        // Avoid interactive shells initially
        if (command.equals("bash") || command.equals("dash")) {
            outputTextView.append("\n$ " + command + "\nInteractive shells are unsupported.\n");
            return;
        }

        // Add full path for the command to run within imageFs
        String fullCommand = imageFs.getRootDir().getPath() + "/usr/bin/" + command;

        // Execute and capture the command output
        String output = launcher.execShellCommand(fullCommand);
        outputTextView.append("\n$ " + command + "\n" + output);
        commandInput.setText("");
    }

}
