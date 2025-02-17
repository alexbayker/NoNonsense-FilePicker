package com.nononsenseapps.filepicker.sample.root;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AlertDialog;

import com.nononsenseapps.filepicker.sample.R;

/**
 * A dialog which tells the user that no SU binary is available
 */
public class SUErrorFragment extends DialogFragment {

    private static final String TAG = "SUErrorFragment";

    public static void showDialog(@NonNull final FragmentManager fm) {
        SUErrorFragment d = new SUErrorFragment();
        d.show(fm, TAG);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setMessage(requireActivity().getString(R.string.root_unavailable_error)).setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss());
        return builder.create();
    }
}
