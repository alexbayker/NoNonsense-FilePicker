/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.nononsenseapps.filepicker.sample;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;

import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;


public class NoNonsenseFilePickerTest extends NoNonsenseFilePicker {

    public static final int REQUEST_PERMISSION_EXTERNAL_STORAGE = 3;

    @Override
    protected void onResume() {
        super.onResume();

        // Request permission
        if (hasPermission()) {
            try {
                createTestData();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            requestPermission();
        }
    }

    void createTestData() throws IOException {
        File sdRoot = Environment.getExternalStorageDirectory().getAbsoluteFile();

        File testRoot = new File(sdRoot, "000000_nonsense-tests");

        testRoot.mkdir();
        assertTrue("Failed to create directory", testRoot.isDirectory());

        List<File> subdirs = Arrays.asList(new File(testRoot, "A-dir"),
                new File(testRoot, "B-dir"),
                new File(testRoot, "C-dir"));


        for (File subdir : subdirs) {
            subdir.mkdir();
            assertTrue("Failed to create sub directory", subdir.isDirectory());

            for (int sf = 0; sf < 10; sf++) {
                File subfile = new File(subdir, "file-" + sf + ".txt");

                subfile.createNewFile();

                assertTrue("Failed to create file", subfile.isFile());
            }
        }
    }

    protected boolean hasPermission() {
        return ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager()) ||
                (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)  == PackageManager.PERMISSION_GRANTED &&
                 ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED));
    }

    protected void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                try {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                            Uri.parse("package:" + BuildConfig.APPLICATION_ID));
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
}
