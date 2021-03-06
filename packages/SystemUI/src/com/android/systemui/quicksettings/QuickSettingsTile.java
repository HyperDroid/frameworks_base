/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.systemui.quicksettings;

import android.app.ActivityManagerNative;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.RemoteException;
import android.os.UserHandle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.systemui.R;
import com.android.systemui.statusbar.phone.PhoneStatusBar;
import com.android.systemui.statusbar.phone.QuickSettingsController;
import com.android.systemui.statusbar.phone.QuickSettingsContainerView;
import com.android.systemui.statusbar.phone.QuickSettingsTileView;

public class QuickSettingsTile implements OnClickListener {

    protected final Context mContext;
    protected final ViewGroup mContainerView;
    protected final LayoutInflater mInflater;

    protected QuickSettingsTileView mTile;
    protected OnClickListener mOnClick;
    protected OnLongClickListener mOnLongClick;

    protected int mTileLayout;
    protected int mDrawable;
    protected int mTileTextSize;
    protected int mTileTextColor;
    public static int mTileSize = 141;

    protected String mLabel;
    protected String name = "";
    protected String tileID = "0";

    protected PhoneStatusBar mStatusbarService;
    protected QuickSettingsController mQsc;

    public QuickSettingsTile(Context context, LayoutInflater inflater,
            QuickSettingsContainerView container, QuickSettingsController qsc) {
        mContext = context;
        mContainerView = container;
        mInflater = inflater;
        mDrawable = R.drawable.ic_notifications;
        mLabel = mContext.getString(R.string.quick_settings_label_enabled);
        mStatusbarService = qsc.mStatusBarService;
        mQsc = qsc;
        mTileLayout = R.layout.quick_settings_tile_generic;
        mTileTextSize = ((QuickSettingsContainerView) mContainerView).updateTileTextSize();
        mTileTextColor = ((QuickSettingsContainerView) mContainerView).updateTileTextColor();
        mTileSize = mContext.getResources().getDimensionPixelSize(R.dimen.quick_settings_cell_height);
    }

    public void setupQuickSettingsTile() {
            createQuickSettings();
            onPostCreate();
            updateQuickSettings();
            mTile.setOnClickListener(this);
            mTile.setOnLongClickListener(mOnLongClick);
    }

    void createQuickSettings() {
        mTile = (QuickSettingsTileView) mInflater.inflate(R.layout.quick_settings_tile, mContainerView, false);
        mTile.setContent(mTileLayout, mInflater);
        mContainerView.addView(mTile);
        mTile.addOnLayoutChangeListener(new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                    int oldLeft, int oldTop, int oldRight, int oldBottom)
            {
                if (left == 0 && top == 0 && right == 0 && bottom == 0)
                    return;
                int newSize = v.getHeight();
                if (newSize > 0 && newSize != mTileSize) {
                    mTileSize = newSize;
                }
            }
        });
    }

    void onPostCreate() {}

    public void onReceive(Context context, Intent intent) {}

    public void onChangeUri(ContentResolver resolver, Uri uri) {}

    void updateQuickSettings() {
        TextView tv = (TextView) mTile.findViewById(R.id.tile_textview);
        tv.setCompoundDrawablesWithIntrinsicBounds(0, mDrawable, 0, 0);
        tv.setText(mLabel);
        tv.setTextSize(1, mTileTextSize);
        if (mTileTextColor != -2) {
            tv.setTextColor(mTileTextColor);
        }
    }

    void startSettingsActivity(String action) {
        Intent intent = new Intent(action);
        startSettingsActivity(intent);
    }

    void startSettingsActivity(Intent intent) {
        startSettingsActivity(intent, true);
    }

    private void startSettingsActivity(Intent intent, boolean onlyProvisioned) {
        if (onlyProvisioned && !mStatusbarService.isDeviceProvisioned()) return;
        try {
            // Dismiss the lock screen when Settings starts.
            ActivityManagerNative.getDefault().dismissKeyguardOnNextActivity();
        } catch (RemoteException e) {
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mContext.startActivityAsUser(intent, new UserHandle(UserHandle.USER_CURRENT));
        mStatusbarService.animateCollapsePanels();
    }

    @Override
    public final void onClick(View v) {
        mOnClick.onClick(v);
        ContentResolver resolver = mContext.getContentResolver();
        boolean shouldCollapse = Settings.System.getInt(resolver, Settings.System.QS_COLLAPSE_PANEL, 0) == 1;
        if (shouldCollapse) {
            mQsc.mBar.collapseAllPanels(true);
        }
    }

    public String getTileContent() {
        return name;
    }
}
