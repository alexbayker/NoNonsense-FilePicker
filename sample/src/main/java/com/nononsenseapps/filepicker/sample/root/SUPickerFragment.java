package com.nononsenseapps.filepicker.sample.root;

import androidx.annotation.NonNull;

import com.nononsenseapps.filepicker.FilePickerFragment;
import com.topjohnwu.superuser.Shell;

import java.io.File;
import java.util.List;

/**
 * An example picker which calls out to LibSU to get Root-permissions to view otherwise hidden files.
 */
public class SUPickerFragment extends FilePickerFragment {

    @Override
    protected boolean hasPermission(@NonNull File path) {
        // Return the combination of normal file permissions and SU permissions
        return super.hasPermission(path) & (!needSUPermission(path) | hasSUPermission());
    }

    @Override
    protected void handlePermission(@NonNull File path) {
        // Only call super if we don't have normal file permissions
        if (!super.hasPermission(path)) {
            super.handlePermission(path);
        }
        // Only if we need SU permissions
        if (needSUPermission(path) && !hasSUPermission()) {
            handleSUPermission();
        }
    }

    private boolean haveReadPermission(@NonNull File file) {
        List<String> result =
                Shell.cmd("test -r " + file.getAbsolutePath() + " && echo \"rootsuccess\"")
                        .exec().getOut();
        return !result.isEmpty() && "rootsuccess".equals(result.get(0));
    }

    private boolean needSUPermission(@NonNull File path) {
        return !haveReadPermission(path);
    }

    private boolean isSUAvailable() {
        return Shell.isAppGrantedRoot() == Boolean.TRUE;
    }

    private boolean hasSUPermission() {
        if (isSUAvailable()) {
            List<String> result = Shell.cmd("ls -l /").exec().getOut();
            return !result.isEmpty();
        }
        return false;
    }

    private void handleSUPermission() {
        if (!isSUAvailable()) {
            // Notify that no root access available
            SUErrorFragment.showDialog(getParentFragmentManager());
        }
    }
}
