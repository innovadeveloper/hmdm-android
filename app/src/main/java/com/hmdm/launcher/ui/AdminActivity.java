/*
 * Headwind MDM: Open Source Android MDM Software
 * https://h-mdm.com
 *
 * Copyright (C) 2019 Headwind Solutions LLC (http://h-sms.com)
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

package com.hmdm.launcher.ui;

import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.UserManager;
import android.provider.Settings;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hmdm.launcher.BuildConfig;
import com.hmdm.launcher.Const;
import com.hmdm.launcher.R;
import com.hmdm.launcher.databinding.ActivityAdminBinding;
import com.hmdm.launcher.helper.SettingsHelper;
import com.hmdm.launcher.json.ServerConfig;
import com.hmdm.launcher.pro.ProUtils;
import com.hmdm.launcher.server.ServerServiceKeeper;
import com.hmdm.launcher.util.AppInfo;
import com.hmdm.launcher.util.LegacyUtils;
import com.hmdm.launcher.util.PushNotificationMqttWrapper;
import com.hmdm.launcher.util.RemoteLogger;
import com.hmdm.launcher.util.Utils;

import java.util.ArrayList;
import java.util.List;

import static com.hmdm.launcher.ui.DynamicButtonActions.*;

public class AdminActivity extends BaseActivity implements DynamicButtonAdapter.OnDynamicButtonClickListener {

    private static final String KEY_APP_INFO = "info";
    private SettingsHelper settingsHelper;

    @Nullable
    public static AppInfo getAppInfo(Intent intent){
        if (intent == null){
            return null;
        }
        return intent.getParcelableExtra(KEY_APP_INFO);
    }

    ActivityAdminBinding binding;
    private RecyclerView dynamicButtonsRecycler;
    private DynamicButtonAdapter dynamicButtonAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_admin);
        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        binding.toolbar.setTitle(ProUtils.getAppName(this));
        binding.toolbar.setSubtitle(ProUtils.getCopyright(this));

        // If QR code doesn't contain "android.app.extra.PROVISIONING_LEAVE_ALL_SYSTEM_APPS_ENABLED":true
        // the system launcher is turned off, so it's not possible to exit and we must hide the exit button
        // Currently the QR code contains this parameter, so the button is always visible
        //binding.systemLauncherButton.setVisibility(Utils.isDeviceOwner(this) ? View.GONE : View.VISIBLE);

//        if ( Build.VERSION.SDK_INT <= Build.VERSION_CODES.M ) {
//            binding.rebootButton.setVisibility(View.GONE);
//        }

        settingsHelper = SettingsHelper.getInstance( this );
        binding.deviceId.setText(settingsHelper.getDeviceId());
        binding.deviceId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAndShowInfoDialog();
            }
        });


        setupDynamicButtonsRecyclerView();
        loadDynamicButtons();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    public void changeDeviceId(View view) {
        dismissDialog(enterDeviceIdDialog);
        createAndShowEnterDeviceIdDialog(false, settingsHelper.getDeviceId());
    }

    public void changeServerUrl(View view) {
        dismissDialog(enterServerDialog);
        createAndShowServerDialog(false, settingsHelper.getBaseUrl(), settingsHelper.getServerProject());
    }

    public void allowSettings(View view) {
        LocalBroadcastManager.getInstance( this ).sendBroadcast( new Intent( Const.ACTION_ENABLE_SETTINGS ) );
        Toast.makeText(this, R.string.settings_allowed, Toast.LENGTH_LONG).show();
        startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
        //finish();
    }

    public void clearRestrictions(View view) {
        String restrictions =
                UserManager.DISALLOW_SAFE_BOOT + "," +
                UserManager.DISALLOW_USB_FILE_TRANSFER + "," +
                UserManager.DISALLOW_MOUNT_PHYSICAL_MEDIA + "," +
                UserManager.DISALLOW_CONFIG_BRIGHTNESS + "," +
                UserManager.DISALLOW_CONFIG_SCREEN_TIMEOUT + "," +
                UserManager.DISALLOW_ADJUST_VOLUME;
        if (settingsHelper.getConfig() != null && settingsHelper.getConfig().getRestrictions() != null) {
            restrictions = "," + settingsHelper.getConfig().getRestrictions();
        }
        Utils.unlockUserRestrictions(this, restrictions);
        Utils.disableScreenshots(false, this);
        LocalBroadcastManager.getInstance( this ).sendBroadcast( new Intent( Const.ACTION_PERMISSIVE_MODE ) );
        LocalBroadcastManager.getInstance( this ).sendBroadcast( new Intent( Const.ACTION_STOP_CONTROL ) );
        Toast.makeText(this, R.string.permissive_mode_enabled, Toast.LENGTH_LONG).show();
        //finish();
    }
    @Override
    protected void updateSettingsFromQr(String qrcode) {
        super.updateSettingsFromQr(qrcode);
        dismissDialog(enterServerDialog);
        dismissDialog(enterDeviceIdDialog);
        binding.deviceId.setText(settingsHelper.getDeviceId());
    }

    public void saveServerUrl(View view ) {
        if (saveServerUrlBase()) {
            ServerServiceKeeper.resetServices();
            String pushOptions = null;
            if (settingsHelper != null && settingsHelper.getConfig() != null) {
                pushOptions = settingsHelper.getConfig().getPushOptions();
            }
            if (BuildConfig.ENABLE_PUSH && pushOptions != null && (pushOptions.equals(ServerConfig.PUSH_OPTIONS_MQTT_WORKER)
                    || pushOptions.equals(ServerConfig.PUSH_OPTIONS_MQTT_ALARM))) {
                PushNotificationMqttWrapper.getInstance().disconnect(this);
            }
            updateConfig(view);
        }
    }

    public void saveDeviceId(View view ) {
        String deviceId = enterDeviceIdDialogBinding.deviceId.getText().toString();
        if ( "".equals( deviceId ) ) {
            return;
        } else {
            settingsHelper.setDeviceId( deviceId );
            enterDeviceIdDialogBinding.setError( false );

            dismissDialog(enterDeviceIdDialog);

            Log.i(Const.LOG_TAG, "saveDeviceId(): calling updateConfig()");
            updateConfig(view);
        }
    }

    public void updateConfig( View view ) {
        LocalBroadcastManager.getInstance( this ).
                sendBroadcast( new Intent( Const.ACTION_UPDATE_CONFIGURATION ) );
        finish();
    }

    public void resetPermissions(View view) {
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(Const.ACTION_ENABLE_SETTINGS));
        SharedPreferences preferences = getSharedPreferences( Const.PREFERENCES, MODE_PRIVATE );
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(Const.PREFERENCES_UNKNOWN_SOURCES);
        editor.remove(Const.PREFERENCES_ADMINISTRATOR);
        editor.remove(Const.PREFERENCES_ACCESSIBILITY_SERVICE);
        editor.remove(Const.PREFERENCES_OVERLAY);
        editor.remove(Const.PREFERENCES_USAGE_STATISTICS);
        editor.remove(Const.PREFERENCES_DEVICE_OWNER);
        editor.remove(Const.PREFERENCES_MIUI_PERMISSIONS);
        editor.remove(Const.PREFERENCES_MIUI_OPTIMIZATION);
        editor.remove(Const.PREFERENCES_DEVICE_OWNER);
        editor.commit();
        RemoteLogger.log(this, Const.LOG_INFO, "Reset saved permissions state, will be refreshed at next start");
        Toast.makeText(this, R.string.permissions_reset_hint, Toast.LENGTH_LONG).show();
    }


    public void resetNetworkPolicy(View view) {
        ServerConfig config = settingsHelper.getConfig();
        if (config != null) {
            config.setWifi(null);
            config.setMobileData(null);
            settingsHelper.updateConfig(config);
        }
        RemoteLogger.log(this, Const.LOG_INFO, "Network policies are cleared");
        Toast.makeText(this, R.string.admin_reset_network_hint, Toast.LENGTH_LONG).show();
    }

    public void reboot(View view) {
        if ( Build.VERSION.SDK_INT > Build.VERSION_CODES.M ) {
            ComponentName deviceAdmin = LegacyUtils.getAdminComponentName(this);
            DevicePolicyManager devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
            try {
                devicePolicyManager.reboot(deviceAdmin);
            } catch (Exception e) {
                Toast.makeText(this, R.string.reboot_failed, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void setupDynamicButtonsRecyclerView() {
        dynamicButtonsRecycler = findViewById(R.id.dynamicButtonsRecycler);
        dynamicButtonAdapter = new DynamicButtonAdapter(this);
        dynamicButtonsRecycler.setLayoutManager(new LinearLayoutManager(this));
        dynamicButtonsRecycler.setAdapter(dynamicButtonAdapter);
    }

    private void loadDynamicButtons() {
        List<DynamicButton> buttons = new ArrayList<>();
        
        // Botones originales de AdminActivity
        buttons.add(new DynamicButton(getString(R.string.admin_allow_settings), ACTION_ALLOW_SETTINGS));
        buttons.add(new DynamicButton(getString(R.string.admin_clear_restrictions), ACTION_CLEAR_RESTRICTIONS));
        buttons.add(new DynamicButton(getString(R.string.admin_change_device_id), ACTION_CHANGE_DEVICE_ID));
        buttons.add(new DynamicButton(getString(R.string.admin_change_server_url), ACTION_CHANGE_SERVER_URL));
        buttons.add(new DynamicButton(getString(R.string.admin_refresh), ACTION_UPDATE_CONFIG));
        buttons.add(new DynamicButton(getString(R.string.admin_exit), ACTION_EXIT_TO_SYSTEM_LAUNCHER));
        buttons.add(new DynamicButton(getString(R.string.admin_reset_permissions), ACTION_RESET_PERMISSIONS));
        buttons.add(new DynamicButton(getString(R.string.admin_reset_network), ACTION_RESET_NETWORK));
        
        // Solo agregar botón de reinicio si la versión lo permite
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            buttons.add(new DynamicButton(getString(R.string.reboot), ACTION_REBOOT));
        }
        
        buttons.add(new DynamicButton("RESET SETTINGS (DEV)", ACTION_RESET_SETTINGS));
        
        // Botones de control del status bar
        buttons.add(new DynamicButton("ACTION_LOCK_ADB", ACTION_LOCK_ADB));
        buttons.add(new DynamicButton("ACTION_UNLOCK_ADB", ACTION_UNLOCK_ADB));
        buttons.add(new DynamicButton("ACTION_LOCK_DISALLOW_SYSTEM_ERROR_DIALOGS", ACTION_LOCK_DISALLOW_SYSTEM_ERROR_DIALOGS));
        buttons.add(new DynamicButton("ACTION_UNLOCK_DISALLOW_SYSTEM_ERROR_DIALOGS", ACTION_UNLOCK_DISALLOW_SYSTEM_ERROR_DIALOGS));

        buttons.add(new DynamicButton("ACTION_LOCK_BRIGHTNESS", ACTION_LOCK_BRIGHTNESS));
        buttons.add(new DynamicButton("ACTION_UNLOCK_BRIGHTNESS", ACTION_UNLOCK_BRIGHTNESS));


        buttons.add(new DynamicButton("ACTION_LOCK_TASK_PACKAGE_KIOSK_MODE", ACTION_LOCK_TASK_PACKAGE_KIOSK_MODE));
        buttons.add(new DynamicButton("ACTION_LOCK_KIOSK_SYSTEM_BUTTONS", ACTION_LOCK_KIOSK_SYSTEM_BUTTONS));
        buttons.add(new DynamicButton("ACTION_LOCK_KIOSK_STRICT", ACTION_LOCK_KIOSK_STRICT));
        buttons.add(new DynamicButton("ACTION_LOCK_KIOSK_SHOW_HOME_ONLY", ACTION_LOCK_KIOSK_SHOW_HOME_ONLY));
        buttons.add(new DynamicButton("ACTION_SETUP_TIME_MANUALLY", ACTION_SETUP_TIME_MANUALLY));
        buttons.add(new DynamicButton("ACTION_SETUP_TIME_AUTOMATICALLY", ACTION_SETUP_TIME_AUTOMATICALLY));
        buttons.add(new DynamicButton("LOCK_STATUS_BAR", LOCK_STATUS_BAR));
        buttons.add(new DynamicButton("UNLOCK_STATUS_BAR", UNLOCK_STATUS_BAR));



        dynamicButtonAdapter.setDynamicButtons(buttons);
    }

    @Override
    public void onDynamicButtonClick(DynamicButton button) {
        String action = button.getAction();
        
        switch (action) {
            // Acciones originales de AdminActivity
            case ACTION_ALLOW_SETTINGS:
                allowSettings(null);
                break;
            case ACTION_CLEAR_RESTRICTIONS:
                clearRestrictions(null);
                break;
            case ACTION_CHANGE_DEVICE_ID:
                changeDeviceId(null);
                break;
            case ACTION_CHANGE_SERVER_URL:
                changeServerUrl(null);
                break;
            case ACTION_UPDATE_CONFIG:
                updateConfig(null);
                break;
            case ACTION_EXIT_TO_SYSTEM_LAUNCHER:
                exitToSystemLauncher(null);
                break;
            case ACTION_RESET_PERMISSIONS:
                resetPermissions(null);
                break;
            case ACTION_RESET_NETWORK:
                resetNetworkPolicy(null);
                break;
            case ACTION_REBOOT:
                reboot(null);
                break;
            case ACTION_RESET_SETTINGS:
                ProUtils.emergencyRemoveDeviceOwner(this);
                break;
                
            // Acciones de control del status bar
            case ACTION_LOCK_BRIGHTNESS:
                lockBrightness();
                break;
            case ACTION_UNLOCK_BRIGHTNESS:
                unlockBrightness();
                break;
            case ACTION_LOCK_ADB:
                lockADB();
                break;
            case ACTION_UNLOCK_ADB:
                unlockADB();
                break;
            case ACTION_LOCK_DISALLOW_SYSTEM_ERROR_DIALOGS:
                lockErrorSystemDialog();
                break;
            case ACTION_UNLOCK_DISALLOW_SYSTEM_ERROR_DIALOGS:
                unlockErrorSystemDialog();
                break;

            case ACTION_LOCK_TASK_PACKAGE_KIOSK_MODE:
                lockTaskPackage();
                break;
            case ACTION_LOCK_KIOSK_SYSTEM_BUTTONS:
                enableKioskHideSystemButtons();
                break;
            case ACTION_LOCK_KIOSK_STRICT:
                enableKioskStrict();
                break;
            case ACTION_LOCK_KIOSK_SHOW_HOME_ONLY:
                enableKioskShowHomeOnly();
                break;
            case ACTION_SETUP_TIME_MANUALLY:
                setupTimeManually();
                break;
            case LOCK_STATUS_BAR:
                lockStatusBar(true);
                break;
            case UNLOCK_STATUS_BAR:
                lockStatusBar(false);
                break;
            default:
                Toast.makeText(this, "Acción: " + button.getTitle(), Toast.LENGTH_SHORT).show();
                break;
        }
    }

    public void addDynamicButton(String title, String action) {
        if (dynamicButtonAdapter != null) {
            dynamicButtonAdapter.addDynamicButton(new DynamicButton(title, action));
        }
    }

    public void clearDynamicButtons() {
        if (dynamicButtonAdapter != null) {
            dynamicButtonAdapter.clearDynamicButtons();
        }
    }

    private void lockBrightness(){
        try {
            Pair<ComponentName, DevicePolicyManager> pair = buildComponents();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    pair.second.addUserRestriction(pair.first, UserManager.DISALLOW_CONFIG_BRIGHTNESS);
                }
            }
            RemoteLogger.log(this, Const.LOG_INFO, "Enable DISALLOW_CONFIG_BRIGHTNESS");
        } catch (Exception e) {
            Toast.makeText(this, "Error al bloquear Status Bar: " + e.getMessage(), Toast.LENGTH_LONG).show();
            RemoteLogger.log(this, Const.LOG_ERROR, "Failed to lock status bar: " + e.getMessage());
        }
    }

    private void unlockBrightness(){
        try {
            Pair<ComponentName, DevicePolicyManager> pair = buildComponents();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    pair.second.clearUserRestriction(pair.first, UserManager.DISALLOW_CONFIG_BRIGHTNESS);
                }
            }
            RemoteLogger.log(this, Const.LOG_INFO, "Disable DISALLOW_CONFIG_BRIGHTNESS");
        } catch (Exception e) {
            Toast.makeText(this, "Error al bloquear Status Bar: " + e.getMessage(), Toast.LENGTH_LONG).show();
            RemoteLogger.log(this, Const.LOG_ERROR, "Failed to lock status bar: " + e.getMessage());
        }
    }

    private void lockADB(){
        try {
            Pair<ComponentName, DevicePolicyManager> pair = buildComponents();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    pair.second.addUserRestriction(pair.first, UserManager.DISALLOW_DEBUGGING_FEATURES);
                }
            }
            RemoteLogger.log(this, Const.LOG_INFO, "Enable DISALLOW_DEBUGGING_FEATURES");
        } catch (Exception e) {
            RemoteLogger.log(this, Const.LOG_ERROR, "Failed to " + e.getMessage());
        }
    }

    private void unlockADB(){
        try {
            Pair<ComponentName, DevicePolicyManager> pair = buildComponents();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    pair.second.clearUserRestriction(pair.first, UserManager.DISALLOW_DEBUGGING_FEATURES);
                }
            }
            RemoteLogger.log(this, Const.LOG_INFO, "Disable DISALLOW_DEBUGGING_FEATURES");
        } catch (Exception e) {
            RemoteLogger.log(this, Const.LOG_ERROR, "Failed to " + e.getMessage());
        }
    }

    private void lockErrorSystemDialog(){
        try {
            Pair<ComponentName, DevicePolicyManager> pair = buildComponents();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    pair.second.addUserRestriction(pair.first, UserManager.DISALLOW_SYSTEM_ERROR_DIALOGS);
                }
            }
            RemoteLogger.log(this, Const.LOG_INFO, "Enable DISALLOW_SYSTEM_ERROR_DIALOGS");
        } catch (Exception e) {
            RemoteLogger.log(this, Const.LOG_ERROR, "Failed to " + e.getMessage());
        }
    }

    private void lockTaskPackage(){
        Pair<ComponentName, DevicePolicyManager> pair = buildComponents();
        List<String> packages = new ArrayList<>();
        packages.add("com.abexa.simple_app");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            pair.second.setLockTaskPackages(pair.first, packages.toArray(new String[0]));
        }
        Toast.makeText(this, "lockTaskPackage com.abexa.simple_app hardcoded", Toast.LENGTH_LONG).show();
    }

    private void unlockErrorSystemDialog(){
        try {
            Pair<ComponentName, DevicePolicyManager> pair = buildComponents();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    pair.second.clearUserRestriction(pair.first, UserManager.DISALLOW_SYSTEM_ERROR_DIALOGS);
                }
            }
            RemoteLogger.log(this, Const.LOG_INFO, "Disable DISALLOW_SYSTEM_ERROR_DIALOGS");
        } catch (Exception e) {
            RemoteLogger.log(this, Const.LOG_ERROR, "Failed to " + e.getMessage());
        }
    }

    /**
     * 🔒 KIOSK MODE (HIDE SYSTEM BUTTONS : POWER AND REBOOT)
     * Permite barra de estado, notificaciones, pero oculta menú de apagado/reinicio.
     */
    public void enableKioskHideSystemButtons() {
        Pair<ComponentName, DevicePolicyManager> pair = buildComponents();

        int flags =
                DevicePolicyManager.LOCK_TASK_FEATURE_HOME |
                        DevicePolicyManager.LOCK_TASK_FEATURE_SYSTEM_INFO |
                        DevicePolicyManager.LOCK_TASK_FEATURE_NOTIFICATIONS;
        // No incluye GLOBAL_ACTIONS => Oculta "Apagar" / "Reiniciar"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            pair.second.setLockTaskFeatures(pair.first, flags);
        }
        Toast.makeText(this, "enableKioskHideSystemButtons done", Toast.LENGTH_LONG).show();
    }


    /**
     * 🚫 KIOSK MODE STRICT (LOCK_TASK_FEATURE_NONE)
     * Bloqueo total: sin barra de estado, sin botones del sistema.
     */
    public void enableKioskStrict() {
        Pair<ComponentName, DevicePolicyManager> pair = buildComponents();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            pair.second.setLockTaskFeatures(pair.first, DevicePolicyManager.LOCK_TASK_FEATURE_NONE);
        }
        Toast.makeText(this, "enableKioskStrict done", Toast.LENGTH_LONG).show();

    }


    /**
     * 🏠 KIOSK MODE (HIDE RECENTS, HIDE RETURN, SHOW HOME)
     * Solo permite el botón HOME (útil para kioskos con navegación controlada).
     */
    public void enableKioskShowHomeOnly() {
        Pair<ComponentName, DevicePolicyManager> pair = buildComponents();
        int flags =
                DevicePolicyManager.LOCK_TASK_FEATURE_HOME |
                        DevicePolicyManager.LOCK_TASK_FEATURE_SYSTEM_INFO;
        // No incluye RECENTS => oculta el botón de apps recientes
        // No incluye GLOBAL_ACTIONS => oculta "Apagar"/"Reiniciar"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            pair.second.setLockTaskFeatures(pair.first, flags);
        }
        Toast.makeText(this, "enableKioskShowHomeOnly done", Toast.LENGTH_LONG).show();
    }

    public void setupTimeManually() {
        Pair<ComponentName, DevicePolicyManager> pair = buildComponents();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            pair.second.setGlobalSetting(pair.first, Settings.Global.AUTO_TIME, "0");
            pair.second.setGlobalSetting(pair.first, Settings.Global.AUTO_TIME_ZONE, "0");
            long oneHourEarlier = System.currentTimeMillis() - 3600000; // 1 hora menos

            pair.second.setTime(pair.first, oneHourEarlier);
        }
        Toast.makeText(this, "setupTimeManually done", Toast.LENGTH_LONG).show();
    }
    public void setupTimeAutomatically() {
        Pair<ComponentName, DevicePolicyManager> pair = buildComponents();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            pair.second.setGlobalSetting(pair.first, Settings.Global.AUTO_TIME, "1");
            pair.second.setGlobalSetting(pair.first, Settings.Global.AUTO_TIME_ZONE, "1");
        }
        Toast.makeText(this, "setupTimeAutomatically done", Toast.LENGTH_LONG).show();
    }

    public void lockStatusBar(boolean isEnabled) {
        Pair<ComponentName, DevicePolicyManager> pair = buildComponents();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            pair.second.setStatusBarDisabled(pair.first, isEnabled);
        }
        Toast.makeText(this, "lockStatusBar -> " + isEnabled, Toast.LENGTH_LONG).show();
    }

    private Pair<ComponentName, DevicePolicyManager> buildComponents(){
        ComponentName deviceAdmin = LegacyUtils.getAdminComponentName(this);
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        return new Pair<>(deviceAdmin, devicePolicyManager);
    }
}
