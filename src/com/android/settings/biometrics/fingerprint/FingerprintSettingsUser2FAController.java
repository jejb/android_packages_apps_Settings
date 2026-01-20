/*
 * Copyright (C) 2026 James.Bottomley@HansenPartnership.com
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

package com.android.settings.biometrics.fingerprint;

import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.provider.Settings;

import androidx.preference.Preference;

import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.widget.LockPatternUtils;
import com.android.settings.Utils;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

/**
 * Preference controller that controls whether the user can enable 2FA
 * fingerprint authentication to unlock the device.
 */
@SearchIndexable
public class FingerprintSettingsUser2FAController
        extends FingerprintSettingsPreferenceController {
    private static final String TAG =
            "FingerprintSettingsUser2FAController";

    @VisibleForTesting
    protected final LockPatternUtils mLockPatternUtils;
    @VisibleForTesting
    protected FingerprintManager mFingerprintManager;

    public FingerprintSettingsUser2FAController(
            Context context, String prefKey) {
        super(context, prefKey);
        mLockPatternUtils = new LockPatternUtils(context);
        mFingerprintManager = Utils.getFingerprintManagerOrNull(context);
   }

    @Override
    public boolean isChecked() {
        if (!FingerprintSettings.isFingerprintHardwareDetected(mContext)) {
            mLockPatternUtils.setUser2FA(getUserId(), false);
        } else if (getRestrictingAdmin() != null) {
            mLockPatternUtils.setUser2FA(getUserId(), false);
        }
        return mLockPatternUtils.isUser2FA(getUserId());
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        mLockPatternUtils.setUser2FA(getUserId(), isChecked);
        return true;
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        if (!FingerprintSettings.isFingerprintHardwareDetected(mContext)) {
            preference.setEnabled(false);
        } else if (!mFingerprintManager.hasEnrolledTemplates(getUserId())) {
            preference.setEnabled(false);
        } else if (getRestrictingAdmin() != null) {
            preference.setEnabled(false);
        } else {
            preference.setEnabled(true);
        }
    }

    @Override
    public int getAvailabilityStatus() {
        /*
         * This is the place we update the secure setting if all
         * enrolled fingerprints are removed.  It relies on the fact
         * that after deletion, the view will be refreshed which will
         * call the availability status of all the fragments.
         */
        if (mFingerprintManager != null
                && mFingerprintManager.isHardwareDetected()) {
            boolean isAvailable = mFingerprintManager.hasEnrolledTemplates(getUserId());
            if (!isAvailable) {
                setChecked(false);
            }
            return isAvailable ? AVAILABLE : CONDITIONALLY_UNAVAILABLE;
        } else {
            setChecked(false);
            return UNSUPPORTED_ON_DEVICE;
        }
    }

    /**
     * This feature is not directly searchable.
     */
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {};

}
