/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.nononsenseapps.filepicker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.FileObserver;
import androidx.annotation.NonNull;
import androidx.loader.content.AsyncTaskLoader;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.SortedList;
import androidx.recyclerview.widget.SortedListAdapterCallback;

import android.provider.Settings;
import android.widget.Toast;

import java.io.File;

/**
 * An implementation of the picker which allows you to select a file from the internal/external
 * storage (SD-card) on a device.
 */
public class FilePickerFragment extends AbstractFilePickerFragment<File> {

    public static final int REQUEST_PERMISSION_EXTERNAL_STORAGE = 3;

    protected boolean showHiddenItems = false;
    private File mRequestedPath = null;

    public FilePickerFragment() {
    }

    /**
     * This method is used to dictate whether hidden files and folders should be shown or not
     *
     * @param showHiddenItems whether hidden items should be shown or not
     */
    public void showHiddenItems(boolean showHiddenItems){
        this.showHiddenItems = showHiddenItems;
    }

    /**
     * Returns if hidden items are shown or not
     *
     * @return true if hidden items are shown, otherwise false
     */

    public boolean areHiddenItemsShown(){
        return showHiddenItems;
    }

    /**
     * @return true if app has been granted permission to write to the SD-card.
     */
    @Override
    protected boolean hasPermission(@NonNull File path) {
        return ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager()) ||
                (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)  == PackageManager.PERMISSION_GRANTED &&
                 ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED));
    }

    /**
     * Request permission to write to the SD-card.
     */
    @Override
    protected void handlePermission(@NonNull File path) {
//         Should we show an explanation?
//        if (shouldShowRequestPermissionRationale(
//                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
//             Explain to the user why we need permission
//        }

        mRequestedPath = path;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                try {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                            Uri.parse("package:" + BuildConfig.LIBRARY_PACKAGE_NAME));
                    startActivityForResult(intent, REQUEST_PERMISSION_EXTERNAL_STORAGE);
                } catch (Exception e) {
                    e.printStackTrace();
                    Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    startActivityForResult(intent, REQUEST_PERMISSION_EXTERNAL_STORAGE);
                }
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[] { Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE }, REQUEST_PERMISSION_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (!hasPermission(mRequestedPath)) {
            return;
        }

        switch (requestCode) {
            case REQUEST_PERMISSION_EXTERNAL_STORAGE: {
                break;
            }
        }
    }

    /**
     * Return true if the path is a directory and not a file.
     *
     * @param path either a file or directory
     * @return true if path is a directory, false if file
     */
    @Override
    public boolean isDir(@NonNull final File path) {
        return path.isDirectory();
    }

    /**
     * @param path either a file or directory
     * @return filename of path
     */
    @NonNull
    @Override
    public String getName(@NonNull File path) {
        return path.getName();
    }

    /**
     * Return the path to the parent directory. Should return the root if
     * from is root.
     *
     * @param from either a file or directory
     * @return the parent directory
     */
    @NonNull
    @Override
    public File getParent(@NonNull final File from) {
        if (from.getPath().equals(getRoot().getPath())) {
            // Already at root, we can't go higher
            return from;
        } else if (from.getParentFile() != null) {
            return from.getParentFile();
        } else {
            return from;
        }
    }

    /**
     * Convert the path to the type used.
     *
     * @param path either a file or directory
     * @return File representation of the string path
     */
    @NonNull
    @Override
    public File getPath(@NonNull final String path) {
        return new File(path);
    }

    /**
     * @param path either a file or directory
     * @return the full path to the file
     */
    @NonNull
    @Override
    public String getFullPath(@NonNull final File path) {
        return path.getPath();
    }

    /**
     * Get the root path.
     *
     * @return the highest allowed path, which is "/" by default
     */
    @NonNull
    @Override
    public File getRoot() {
        return new File("/");
    }

    /**
     * Convert the path to a URI for the return intent
     *
     * @param file either a file or directory
     * @return a Uri
     */
    @NonNull
    @Override
    public Uri toUri(@NonNull final File file) {
        return Utils.parseContentProviderUriFromFile(file);
        /*return FileProvider
                .getUriForFile(requireContext(),
                        requireContext().getApplicationContext().getPackageName() + ".provider",
                        file);*/
    }

    /**
     * Get a loader that lists the Files in the current path,
     * and monitors changes.
     */
    @SuppressLint("StaticFieldLeak")
    @NonNull
    @Override
    public Loader<SortedList<File>> getLoader() {
        return new AsyncTaskLoader<SortedList<File>>(requireActivity()) {

            FileObserver fileObserver;

            @Override
            public SortedList<File> loadInBackground() {
                File[] listFiles = mCurrentPath.listFiles();
                final int initCap = listFiles == null ? 0 : listFiles.length;

                SortedList<File> files = new SortedList<>(File.class, new SortedListAdapterCallback<File>(getDummyAdapter()) {
                    @Override
                    public int compare(File lhs, File rhs) {
                        return compareFiles(lhs, rhs);
                    }

                    @Override
                    public boolean areContentsTheSame(File file, File file2) {
                        return file.getAbsolutePath().equals(file2.getAbsolutePath()) && (file.isFile() == file2.isFile());
                    }

                    @Override
                    public boolean areItemsTheSame(File file, File file2) {
                        return areContentsTheSame(file, file2);
                    }
                }, initCap);


                files.beginBatchedUpdates();
                if (listFiles != null) {
                    for (java.io.File f : listFiles) {
                        if (isItemVisible(f)) {
                            files.add(f);
                        }
                    }
                }
                files.endBatchedUpdates();

                return files;
            }

            /**
             * Handles a request to start the Loader.
             */
            @Override
            protected void onStartLoading() {
                super.onStartLoading();

                // handle if directory does not exist. Fall back to root.
                if (mCurrentPath == null || !mCurrentPath.isDirectory()) {
                    mCurrentPath = getRoot();
                }

                // Start watching for changes
                fileObserver = new FileObserver(mCurrentPath.getPath(),
                        FileObserver.CREATE |
                                FileObserver.DELETE
                                | FileObserver.MOVED_FROM | FileObserver.MOVED_TO
                ) {

                    @Override
                    public void onEvent(int event, String path) {
                        // Reload
                        onContentChanged();
                    }
                };
                fileObserver.startWatching();

                forceLoad();
            }

            /**
             * Handles a request to completely reset the Loader.
             */
            @Override
            protected void onReset() {
                super.onReset();

                // Stop watching
                if (fileObserver != null) {
                    fileObserver.stopWatching();
                    fileObserver = null;
                }
            }
        };
    }

    /**
     * Name is validated to be non-null, non-empty and not containing any
     * slashes.
     *
     * @param name The name of the folder the user wishes to create.
     */
    @Override
    public void onNewFolder(@NonNull final String name) {
        File folder = new File(mCurrentPath, name);

        if (folder.mkdir()) {
            refresh(folder);
        } else {
            Toast.makeText(requireActivity(), R.string.nnf_create_folder_error,
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Used by the list to determine whether a file should be displayed or not.
     * Default behavior is to always display folders. If files can be selected,
     * then files are also displayed. Set the showHiddenFiles property to show
     * hidden file. Default behaviour is to hide hidden files. Override this method to enable other
     * filtering behaviour, like only displaying files with specific extensions (.zip, .txt, etc).
     *
     * @param file to maybe add. Can be either a directory or file.
     * @return True if item should be added to the list, false otherwise
     */
    protected boolean isItemVisible(final File file) {
        if (!showHiddenItems && file.isHidden()) {
            return false;
        }
        return super.isItemVisible(file);
    }

    /**
     * Compare two files to determine their relative sort order. This follows the usual
     * comparison interface. Override to determine your own custom sort order.
     * <p/>
     * Default behaviour is to place directories before files, but sort them alphabetically
     * otherwise.
     *
     * @param lhs File on the "left-hand side"
     * @param rhs File on the "right-hand side"
     * @return -1 if if lhs should be placed before rhs, 0 if they are equal,
     * and 1 if rhs should be placed before lhs
     */
    protected int compareFiles(@NonNull File lhs, @NonNull File rhs) {
        if (lhs.isDirectory() && !rhs.isDirectory()) {
            return -1;
        } else if (rhs.isDirectory() && !lhs.isDirectory()) {
            return 1;
        } else {
            return lhs.getName().compareToIgnoreCase(rhs.getName());
        }
    }
}
