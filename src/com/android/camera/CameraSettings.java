/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.camera;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.hardware.Camera.Parameters;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;

import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 *  CameraSettings
 */
public class CameraSettings extends PreferenceActivity implements
        OnSharedPreferenceChangeListener {
    public static final String KEY_VIDEO_QUALITY =
            "pref_camera_videoquality_key";
    public static final String KEY_WHITE_BALANCE =
            "pref_camera_whitebalance_key";
    public static final String KEY_EFFECT = "pref_camera_effect_key";
    public static final String KEY_BRIGHTNESS = "pref_camera_brightness_key";
    public static final String KEY_PICTURE_SIZE = "pref_camera_picturesize_key";
    public static final String KEY_JPEG_QUALITY = "pref_camera_jpegquality_key";
    public static final String KEY_FOCUS_MODE = "pref_camera_focusmode_key";
    public static final String KEY_ISO = "pref_camera_iso_key";
    public static final boolean DEFAULT_VIDEO_QUALITY_VALUE = true;

    private ListPreference mVideoQuality;
    private ListPreference mWhiteBalance;
    private ListPreference mEffect;
    private ListPreference mBrightness;
    private ListPreference mPictureSize;
    private ListPreference mJpegQuality;
    private ListPreference mFocusMode;
    private ListPreference mIso;
    private Parameters mParameters;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.camera_preferences);

        initUI();
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateVideoQualitySummary();
        updateWhiteBalanceSummary();
        updateEffectSummary();
        updateBrightnessSummary();
        updatePictureSizeSummary();
        updateJpegQualitySummary();
        updateFocusModeSummary();
        updateIsoSummary();
    }

    private void initUI() {
        mVideoQuality = (ListPreference) findPreference(KEY_VIDEO_QUALITY);
        mWhiteBalance = (ListPreference) findPreference(KEY_WHITE_BALANCE);
        mEffect = (ListPreference) findPreference(KEY_EFFECT);
        mBrightness = (ListPreference) findPreference(KEY_BRIGHTNESS);

        mPictureSize = (ListPreference) findPreference(KEY_PICTURE_SIZE);
        mJpegQuality = (ListPreference) findPreference(KEY_JPEG_QUALITY);
        mFocusMode = (ListPreference) findPreference(KEY_FOCUS_MODE);
        mIso = (ListPreference) findPreference(KEY_ISO);
        getPreferenceScreen().getSharedPreferences().
                registerOnSharedPreferenceChangeListener(this);

        // Get parameters.
        android.hardware.Camera device = android.hardware.Camera.open();
        mParameters = device.getParameters();
        device.release();

        // Create white balance settings.
        createSettings(mWhiteBalance, Camera.SUPPORTED_WHITE_BALANCE,
                       R.array.pref_camera_whitebalance_entries,
                       R.array.pref_camera_whitebalance_entryvalues);

        // Create effect settings.
        createSettings(mEffect, Camera.SUPPORTED_EFFECT,
                       R.array.pref_camera_effect_entries,
                       R.array.pref_camera_effect_entryvalues);

        // Create brightness settings.
        createSettings(mBrightness, Camera.SUPPORTED_BRIGHTNESS,
                       R.array.pref_camera_brightness_entries,
                       R.array.pref_camera_brightness_entryvalues);

        // Create picture size settings.
        createSettings(mPictureSize, Camera.SUPPORTED_PICTURE_SIZE,
                       R.array.pref_camera_picturesize_entries,
                       R.array.pref_camera_picturesize_entryvalues);

        // Create iso settings.
        createSettings(mIso, Camera.SUPPORTED_ISO,
                       R.array.pref_camera_iso_entries,
                       R.array.pref_camera_iso_entryvalues);

        // Set default JPEG quality value if it is empty.
        if (mJpegQuality.getValue() == null) {
            mJpegQuality.setValue(getString(
                R.string.pref_camera_jpegquality_default));
        }

        // Set default focus mode value if it is empty.
        if (mFocusMode.getValue() == null) {
            mFocusMode.setValue(getString(
                R.string.pref_camera_focusmode_default));
        }
    }

    private void createSettings(
            ListPreference pref, String paramName, int prefEntriesResId,
            int prefEntryValuesResId) {
        // Disable the preference if the parameter is not supported.
        String supportedParamStr = mParameters.get(paramName);
        if (supportedParamStr == null) {
            pref.setEnabled(false);
            return;
        }

        // Get the supported parameter settings.
        StringTokenizer tokenizer = new StringTokenizer(supportedParamStr, ",");
        ArrayList<CharSequence> supportedParam = new ArrayList<CharSequence>();
        while (tokenizer.hasMoreElements()) {
            supportedParam.add(tokenizer.nextToken());
        }

        // Prepare setting entries and entry values.
        String[] allEntries = getResources().getStringArray(prefEntriesResId);
        String[] allEntryValues = getResources().getStringArray(
                prefEntryValuesResId);
        ArrayList<CharSequence> entries = new ArrayList<CharSequence>();
        ArrayList<CharSequence> entryValues = new ArrayList<CharSequence>();
        for (int i = 0, len = allEntryValues.length; i < len; i++) {
            int found = supportedParam.indexOf(allEntryValues[i]);
            if (found != -1) {
                entries.add(allEntries[i]);
                entryValues.add(allEntryValues[i]);
            }
        }

        // Set entries and entry values to list preference.
        pref.setEntries(entries.toArray(new CharSequence[entries.size()]));
        pref.setEntryValues(entryValues.toArray(
                new CharSequence[entryValues.size()]));

        // Set the value to the first entry if it is invalid.
        String value = pref.getValue();
        int index = pref.findIndexOfValue(value);
        if (index == -1) {
            pref.setValueIndex(0);
        }
    }

    private void updateVideoQualitySummary() {
        mVideoQuality.setSummary(mVideoQuality.getEntry());
    }

    private void updateWhiteBalanceSummary() {
        mWhiteBalance.setSummary(mWhiteBalance.getEntry());
    }

    private void updateEffectSummary() {
        mEffect.setSummary(mEffect.getEntry());
    }

    private void updateBrightnessSummary() {
        mBrightness.setSummary(mBrightness.getEntry());
    }

    private void updatePictureSizeSummary() {
        mPictureSize.setSummary(mPictureSize.getEntry());
    }

    private void updateJpegQualitySummary() {
        mJpegQuality.setSummary(mJpegQuality.getEntry());
    }

    private void updateFocusModeSummary() {
        mFocusMode.setSummary(mFocusMode.getEntry());
    }

    private void updateIsoSummary() {
        mIso.setSummary(mIso.getEntry());
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
            String key) {
        if (key.equals(KEY_VIDEO_QUALITY)) {
            updateVideoQualitySummary();
        } else if (key.equals(KEY_WHITE_BALANCE)) {
            updateWhiteBalanceSummary();
        } else if (key.equals(KEY_EFFECT)) {
            updateEffectSummary();
        } else if (key.equals(KEY_PICTURE_SIZE)) {
            updatePictureSizeSummary();
        } else if (key.equals(KEY_JPEG_QUALITY)) {
            updateJpegQualitySummary();
        } else if (key.equals(KEY_FOCUS_MODE)) {
            updateFocusModeSummary();
        } else if (key.equals(KEY_BRIGHTNESS)) {
            updateBrightnessSummary();
        } else if (key.equals(KEY_ISO)) {
            updateIsoSummary();
        }
    }
}
