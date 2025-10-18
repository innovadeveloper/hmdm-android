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

import android.annotation.SuppressLint;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.UserManager;
import android.util.Log;
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
        buttons.add(new DynamicButton("Bloquear Status Bar", ACTION_LOCK_STATUS_BAR));
        buttons.add(new DynamicButton("Desbloquear Status Bar", ACTION_UNLOCK_STATUS_BAR));
        
//        // Botones adicionales dinámicos
//        buttons.add(new DynamicButton("Logs del Sistema", ACTION_SYSTEM_LOGS));
//        buttons.add(new DynamicButton("Información de Red", ACTION_NETWORK_INFO));
//        buttons.add(new DynamicButton("Estado de Batería", ACTION_BATTERY_STATUS));
//        buttons.add(new DynamicButton("Aplicaciones Instaladas", ACTION_INSTALLED_APPS));
//        buttons.add(new DynamicButton("Limpiar Cache", ACTION_CLEAR_CACHE));
//        buttons.add(new DynamicButton("Exportar Configuración", ACTION_EXPORT_CONFIG));
        
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
            case ACTION_LOCK_STATUS_BAR:
//                Toast.makeText(this, "Mostrando logs del sistema...", Toast.LENGTH_SHORT).show();
                lockStatusBar();
                break;
            case ACTION_UNLOCK_STATUS_BAR:
                unlockStatusBar();
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

    private void lockStatusBar() {
        try {
            ComponentName deviceAdmin = LegacyUtils.getAdminComponentName(this);
            DevicePolicyManager devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (devicePolicyManager != null && devicePolicyManager.isDeviceOwnerApp(getPackageName())) {
                    devicePolicyManager.addUserRestriction(deviceAdmin, "disallow_status_bar_expansion");
//                    devicePolicyManager.addUserRestriction(deviceAdmin, );
                    Toast.makeText(this, "Status Bar bloqueado", Toast.LENGTH_SHORT).show();
                    RemoteLogger.log(this, Const.LOG_INFO, "Status bar expansion disabled");
                } else {
                    Toast.makeText(this, "No se puede bloquear: No es Device Owner", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "Función no disponible en Android < 6.0", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error al bloquear Status Bar: " + e.getMessage(), Toast.LENGTH_LONG).show();
            RemoteLogger.log(this, Const.LOG_ERROR, "Failed to lock status bar: " + e.getMessage());
        }
    }

    @SuppressLint("WrongConstant")
    private void unlockStatusBar() {
        try {
            ComponentName deviceAdmin = LegacyUtils.getAdminComponentName(this);
            DevicePolicyManager devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (devicePolicyManager != null && devicePolicyManager.isDeviceOwnerApp(getPackageName())) {
                    devicePolicyManager.clearUserRestriction(deviceAdmin, "disallow_status_bar_expansion");
                    Toast.makeText(this, "Status Bar desbloqueado", Toast.LENGTH_SHORT).show();
                    RemoteLogger.log(this, Const.LOG_INFO, "Status bar expansion enabled");
                } else {
                    Toast.makeText(this, "No se puede desbloquear: No es Device Owner", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "Función no disponible en Android < 6.0", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error al desbloquear Status Bar: " + e.getMessage(), Toast.LENGTH_LONG).show();
            RemoteLogger.log(this, Const.LOG_ERROR, "Failed to unlock status bar: " + e.getMessage());
        }
    }
}
