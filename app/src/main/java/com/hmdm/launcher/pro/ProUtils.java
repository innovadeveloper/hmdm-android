package com.hmdm.launcher.pro;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.UserManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.hmdm.launcher.Const;
import com.hmdm.launcher.R;
import com.hmdm.launcher.helper.SettingsHelper;
import com.hmdm.launcher.json.Application;
import com.hmdm.launcher.json.ServerConfig;
import com.hmdm.launcher.util.RemoteLogger;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * Verificación de políticas :
 * adb shell dumpsys device_policy
 */
public class ProUtils {

    private static final String TAG = "ProUtils";

    public static boolean isPro() {
        // Cambia a true para habilitar funciones Pro
        return true;
    }

    public static boolean kioskModeRequired(Context context) {
        SettingsHelper settingsHelper = SettingsHelper.getInstance(context);
        ServerConfig config = settingsHelper.getConfig();
        return config != null && config.getKioskMode() != null && config.getKioskMode();
    }

    public static void initCrashlytics(Context context) {
        // Stub
    }

    public static void sendExceptionToCrashlytics(Throwable e) {
        // Stub
    }

    public static boolean checkAccessibilityService(Context context) {
        return true;
    }

    public static boolean checkUsageStatistics(Context context) {
        return true;
    }

    public static View preventStatusBarExpansion(Activity activity) {
        return null;
    }

    public static View preventApplicationsList(Activity activity) {
        return null;
    }

    public static View createKioskUnlockButton(Activity activity) {
        return null;
    }

    public static boolean isKioskAppInstalled(Context context) {
        SettingsHelper settingsHelper = SettingsHelper.getInstance(context);
        ServerConfig config = settingsHelper.getConfig();
        if (config == null || config.getMainApp() == null) {
            return false;
        }

        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(config.getMainApp(), 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static boolean isKioskModeRunning(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            return am.getLockTaskModeState() != ActivityManager.LOCK_TASK_MODE_NONE;
        }
        return false;
    }

    public static Intent getKioskAppIntent(String kioskApp, Activity activity) {
        PackageManager pm = activity.getPackageManager();
        return pm.getLaunchIntentForPackage(kioskApp);
    }

    /**
     * FUNCIÓN PRINCIPAL: Inicia el modo kiosko usando la configuración del servidor
     * @deprecated Use startCosuKioskModeFromConfig instead
     */
    public static boolean startCosuKioskMode(String kioskApp, Activity activity, boolean enableSettings) {
        // Mantener por compatibilidad, pero redirigir a la nueva función
        ServerConfig config = SettingsHelper.getInstance(activity).getConfig();
        if (config != null) {
            return startCosuKioskModeFromConfig(activity, config);
        }
        return false;
    }


    /**
     * Construir resumen de configuración de kiosko (versión detallada)
     */
    private static String buildKioskConfigSummary(ServerConfig config) {
        StringBuilder sb = new StringBuilder();

        sb.append("KIOSK START | ");
        sb.append("App: ").append(config.getMainApp());

        // Features
        sb.append(" | Features: ");
        sb.append("Home=").append(boolToSymbol(config.getKioskHome()));
        sb.append(" Notif=").append(boolToSymbol(config.getKioskNotifications()));
        sb.append(" Recent=").append(boolToSymbol(config.getKioskRecents()));
        sb.append(" SysInfo=").append(boolToSymbol(config.getKioskSystemInfo()));
        sb.append(" Keyguard=").append(boolToSymbol(config.getKioskKeyguard()));
        sb.append(" Exit=").append(boolToSymbol(config.isKioskExit()));

        // UI
        sb.append(" | UI: ");
        sb.append("StatusBar=").append(boolToSymbol(config.getLockStatusBar()));
        sb.append(" LockBtn=").append(boolToSymbol(config.getKioskLockButtons()));
        sb.append(" LockVol=").append(boolToSymbol(config.getLockVolume()));

        // Restrictions
        sb.append(" | Restrict: ");
        sb.append("Loc=").append(boolToSymbol(config.getDisableLocation()));
        sb.append(" Screen=").append(boolToSymbol(config.isDisableScreenshots()));
        sb.append(" WiFi=").append(boolToSymbol(invertBool(config.getWifi())));
        sb.append(" BT=").append(boolToSymbol(invertBool(config.getBluetooth())));

        return sb.toString();
    }

    /**
     * Convertir booleano a símbolo visual
     */
    private static String boolToSymbol(Boolean value) {
        if (value == null) return "○";
        return value ? "●" : "○";
    }

    /**
     * Invertir booleano (para flags donde true=habilitado pero queremos mostrar restricción)
     */
    private static Boolean invertBool(Boolean value) {
        if (value == null) return null;
        return !value;
    }

    /**
     * NUEVA FUNCIÓN: Inicia el modo kiosko usando TODAS las propiedades del ServerConfig
     */
    public static boolean startCosuKioskModeFromConfig(Activity activity, ServerConfig config) {
        if (config == null || config.getMainApp() == null || config.getMainApp().isEmpty()) {
            Log.e(TAG, "Config or main app is null/empty");
            return false;
        }

        String configSummary = buildKioskConfigSummary(config);
        RemoteLogger.log(activity, Const.LOG_INFO, configSummary);

        String kioskApp = config.getMainApp();

        try {
            DevicePolicyManager dpm = (DevicePolicyManager) activity.getSystemService(Context.DEVICE_POLICY_SERVICE);
            ComponentName adminComponent = getAdminComponent(activity);

            if (dpm == null || adminComponent == null) {
                Log.e(TAG, "Device Policy Manager or Admin Component is null");
                return false;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                if (!dpm.isDeviceOwnerApp(activity.getPackageName())) {
                    Log.e(TAG, "App is not Device Owner");
                    RemoteLogger.log(activity, Const.LOG_WARN, "Cannot start kiosk mode: app is not Device Owner");
                    return false;
                }
            }

            // 1. CONSTRUIR LISTA DE APPS PERMITIDAS EN LOCK TASK
            List<String> lockTaskPackages = buildLockTaskPackagesList(activity, config);
            RemoteLogger.log(activity, Const.LOG_INFO, "Lock task packages set: " + lockTaskPackages.size() + " apps");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                dpm.setLockTaskPackages(adminComponent, lockTaskPackages.toArray(new String[0]));   // SOLO AUTORIZA A LAS aplicaciones están autorizadas a entrar en “Lock Task Mode”
            }
            Log.d(TAG, "Lock task packages set: " + lockTaskPackages);

            // 2. CONFIGURAR OPCIONES DE LOCK TASK BASADAS EN SERVERCONFIG
//            configureLockTaskFeatures(activity, dpm, adminComponent, config);
//            RemoteLogger.log(activity, Const.LOG_INFO, "Lock task features configured");

            // 3. CONFIGURAR BARRA DE ESTADO Y KEYGUARD
            configureStatusBarAndKeyguard(activity, dpm, adminComponent, config);

//            // 4. APLICAR RESTRICCIONES DE USUARIO
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                applyKioskRestrictions(activity, dpm, adminComponent, config);
//            }

            // 5. OCULTAR APPS NO PERMITIDAS
//            hideNonKioskApps(activity, dpm, adminComponent, config);
//            RemoteLogger.log(activity, Const.LOG_INFO, "Non-kiosk apps hidden");

            // 6. CONFIGURAR BOTONES (Power, Volume, etc.)
//            configurePhysicalButtons(activity, dpm, adminComponent, config);

            // 7. LANZAR LA APP EN LOCK TASK
            Intent intent = getKioskAppIntent(kioskApp, activity);
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                activity.startActivity(intent);

                // Activar Lock Task después de un delay
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        activateLockTaskForApp(activity, kioskApp);
                    }
                }, 1500);

                RemoteLogger.log(activity, Const.LOG_INFO, "Kiosk mode started for " + kioskApp);
                return true;
            } else {
                Log.e(TAG, "Cannot get launch intent for " + kioskApp);
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            RemoteLogger.log(activity, Const.LOG_ERROR, "Error starting kiosk mode: " + e.getMessage());
            return false;
        }
    }

    /**
     * Construir lista de packages permitidos en Lock Task
     * Basado en: applications[].useKiosk = true
     */
    private static List<String> buildLockTaskPackagesList(Context context, ServerConfig config) {
        List<String> packages = new ArrayList<>();

        // SIEMPRE incluir la app principal del kiosko
        packages.add(config.getMainApp());

        // SIEMPRE incluir Headwind MDM para poder controlar
        packages.add(context.getPackageName());

        // Apps del sistema necesarias
        packages.add("com.android.systemui");
        packages.add("android");

        // Incluir apps marcadas con useKiosk=true
        if (config.getApplications() != null) {
            for (Application app : config.getApplications()) {
                if (app.isUseKiosk() && !packages.contains(app.getPkg())) {
                    packages.add(app.getPkg());
                    Log.d(TAG, "Added to kiosk whitelist (useKiosk=true): " + app.getPkg());
                }
            }
        }

        return packages;
    }

    /**
     * Configurar características de Lock Task según ServerConfig
     * IMPORTANTE: Las flags controlan QUÉ está PERMITIDO, no qué está bloqueado
     */

    private static void configureLockTaskFeatures(Context context, DevicePolicyManager dpm,
                                                  ComponentName adminComponent, ServerConfig config) {
        int lockTaskFeatures = DevicePolicyManager.LOCK_TASK_FEATURE_NONE;
        List<String> enabledFeaturesList = new ArrayList<>();

        // VALIDAR DEPENDENCIAS DE ANDROID
        boolean needsNotifications = config.getKioskNotifications() != null && config.getKioskNotifications();
        boolean needsHome = config.getKioskHome() != null && config.getKioskHome();
        boolean needsRecents = config.getKioskRecents() != null && config.getKioskRecents();
        boolean needsSystemInfo = config.getKioskSystemInfo() != null && config.getKioskSystemInfo();
        boolean needsKeyguard = config.getKioskKeyguard() != null && config.getKioskKeyguard();
        boolean needsGlobalActions = config.isKioskExit();

        // REGLA CRÍTICA
        if (needsNotifications && !needsHome) {
            Log.w(TAG, "Auto-enabling Home (required by Notifications)");
            needsHome = true;
        }

        // CONSTRUIR FEATURES
        if (needsHome) {
            lockTaskFeatures |= DevicePolicyManager.LOCK_TASK_FEATURE_HOME;
            enabledFeaturesList.add("HOME");
        }

        if (needsNotifications) {
            lockTaskFeatures |= DevicePolicyManager.LOCK_TASK_FEATURE_NOTIFICATIONS;
            enabledFeaturesList.add("NOTIFICATIONS");
        }

        if (needsRecents && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            lockTaskFeatures |= DevicePolicyManager.LOCK_TASK_FEATURE_OVERVIEW;
            enabledFeaturesList.add("OVERVIEW");
        }

        if (needsSystemInfo) {
            lockTaskFeatures |= DevicePolicyManager.LOCK_TASK_FEATURE_SYSTEM_INFO;
            enabledFeaturesList.add("SYSTEM_INFO");
        }

        if (needsKeyguard && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            lockTaskFeatures |= DevicePolicyManager.LOCK_TASK_FEATURE_KEYGUARD;
            enabledFeaturesList.add("KEYGUARD");
        }

        if (needsGlobalActions && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            lockTaskFeatures |= DevicePolicyManager.LOCK_TASK_FEATURE_GLOBAL_ACTIONS;
            enabledFeaturesList.add("GLOBAL_ACTIONS");
        }

        // APLICAR Y LOGGEAR
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                dpm.setLockTaskFeatures(adminComponent, lockTaskFeatures);

                String featuresStr = enabledFeaturesList.isEmpty() ? "NONE" : String.join("+", enabledFeaturesList);
                String logMsg = "Lock task features: " + featuresStr + " (0x" + Integer.toHexString(lockTaskFeatures) + ")";

                Log.i(TAG, logMsg);
                RemoteLogger.log(context, Const.LOG_INFO, logMsg);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Log.w(TAG, "Advanced lock task features require Android 9+");
                RemoteLogger.log(context, Const.LOG_WARN, "Lock task features limited: Android " + Build.VERSION.SDK_INT);
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Failed to set lock task features", e);
            RemoteLogger.log(context, Const.LOG_ERROR, "Failed to set lock task features: " + e.getMessage());
            throw e;
        }
    }

//    /**
//     * Configurar características de Lock Task según ServerConfig
//     */
//    private static void configureLockTaskFeatures(Context context, DevicePolicyManager dpm,
//                                                  ComponentName adminComponent, ServerConfig config) {
//        int lockTaskFeatures = DevicePolicyManager.LOCK_TASK_FEATURE_NONE;
//
//        // kioskHome: Permitir botón Home
//        if (config.getKioskHome() != null && config.getKioskHome()) {
//            lockTaskFeatures |= DevicePolicyManager.LOCK_TASK_FEATURE_HOME;
//            Log.d(TAG, "Kiosk: Home button enabled");
//        }
//
//        // kioskNotifications: Permitir notificaciones
//        if (config.getKioskNotifications() != null && config.getKioskNotifications()) {
//            lockTaskFeatures |= DevicePolicyManager.LOCK_TASK_FEATURE_NOTIFICATIONS;
//            Log.d(TAG, "Kiosk: Notifications enabled");
//        }
//
//        // kioskRecents: Permitir apps recientes (Android 9+)
//        if (config.getKioskRecents() != null && config.getKioskRecents()) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//                lockTaskFeatures |= DevicePolicyManager.LOCK_TASK_FEATURE_OVERVIEW;
//                Log.d(TAG, "Kiosk: Recents/Overview enabled");
//            }
//        }
//
//        // kioskSystemInfo: Permitir info del sistema
//        if (config.getKioskSystemInfo() != null && config.getKioskSystemInfo()) {
//            lockTaskFeatures |= DevicePolicyManager.LOCK_TASK_FEATURE_SYSTEM_INFO;
//            Log.d(TAG, "Kiosk: System info enabled");
//        }
//
//        // kioskKeyguard: Permitir keyguard (Android 9+)
//        if (config.getKioskKeyguard() != null && config.getKioskKeyguard()) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//                lockTaskFeatures |= DevicePolicyManager.LOCK_TASK_FEATURE_KEYGUARD;
//                Log.d(TAG, "Kiosk: Keyguard enabled");
//            }
//        }
//
//        // kioskExit: Permitir salida global (Android 9+)
//        if (config.isKioskExit()) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//                lockTaskFeatures |= DevicePolicyManager.LOCK_TASK_FEATURE_GLOBAL_ACTIONS;
//                Log.d(TAG, "Kiosk: Global actions (exit) enabled");
//            }
//        }
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//            dpm.setLockTaskFeatures(adminComponent, lockTaskFeatures);
//        }
//        Log.d(TAG, "Lock task features configured: " + lockTaskFeatures);
//    }

    /**
     * Configurar barra de estado y keyguard
     */
    private static void configureStatusBarAndKeyguard(Context context, DevicePolicyManager dpm,
                                                      ComponentName adminComponent, ServerConfig config) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // lockStatusBar: true = DESHABILITAR barra de estado
            boolean disableStatusBar = config.getLockStatusBar() != null && config.getLockStatusBar();
            try {
                dpm.setStatusBarDisabled(adminComponent, disableStatusBar);
                RemoteLogger.log(context, Const.LOG_INFO, "setStatusBarDisabled: " + disableStatusBar + " .. done");
                Log.d(TAG, "Status bar " + (disableStatusBar ? "DISABLED" : "ENABLED"));
            } catch (SecurityException e) {
                Log.e(TAG, "Failed to set status bar state. Requires Device Owner.", e);
            }

            // kioskKeyguard: false = DESHABILITAR keyguard
            boolean disableKeyguard = config.getKioskKeyguard() == null || !config.getKioskKeyguard();
            try {
                dpm.setKeyguardDisabled(adminComponent, disableKeyguard);
                RemoteLogger.log(context, Const.LOG_INFO, "setKeyguardDisabled: " + disableKeyguard + " .. done");
                Log.d(TAG, "Keyguard " + (disableKeyguard ? "DISABLED" : "ENABLED"));
            } catch (SecurityException e) {
                Log.e(TAG, "Failed to set keyguard state. Requires Device Owner.", e);
            }
        } else {
            Log.w(TAG, "Status bar and keyguard control requires Android 6.0+");
        }
    }
//    /**
//     * Configurar barra de estado y keyguard
//     */
//    private static void configureStatusBarAndKeyguard(Context context, DevicePolicyManager dpm,
//                                                      ComponentName adminComponent, ServerConfig config) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            // lockStatusBar: Deshabilitar barra de estado
//            boolean disableStatusBar = config.getLockStatusBar() != null && config.getLockStatusBar();
//            dpm.setStatusBarDisabled(adminComponent, disableStatusBar);
//            Log.d(TAG, "Status bar disabled: " + disableStatusBar);
//
//            // kioskKeyguard: Si es false, deshabilitar keyguard
//            boolean disableKeyguard = config.getKioskKeyguard() == null || !config.getKioskKeyguard();
//            dpm.setKeyguardDisabled(adminComponent, disableKeyguard);
//            Log.d(TAG, "Keyguard disabled: " + disableKeyguard);
//        }
//    }

    /**
     * Configurar botones físicos
     */
    private static void configurePhysicalButtons(Context context, DevicePolicyManager dpm,
                                                 ComponentName adminComponent, ServerConfig config) {
        // kioskLockButtons: Ya está manejado por LOCK_TASK_FEATURE_NONE si no se habilitan otras features

        // lockVolume: Bloquear ajuste de volumen
        if (config.getLockVolume() != null && config.getLockVolume()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                dpm.addUserRestriction(adminComponent, UserManager.DISALLOW_ADJUST_VOLUME);
            }
            Log.d(TAG, "Volume adjustment locked");
        }

        // manageVolume y volume: Configurar volumen específico
        if (config.getManageVolume() != null && config.getManageVolume() && config.getVolume() != null) {
            // TODO: Implementar setMasterVolume si es necesario
            Log.d(TAG, "Volume management enabled, target: " + config.getVolume());
        }
    }

    /**
     * Aplicar restricciones de usuario para kiosko
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private static void applyKioskRestrictions(Context context, DevicePolicyManager dpm,
                                               ComponentName adminComponent, ServerConfig config) {
        // Restricciones básicas de instalación/desinstalación
        dpm.addUserRestriction(adminComponent, UserManager.DISALLOW_INSTALL_APPS);
        dpm.addUserRestriction(adminComponent, UserManager.DISALLOW_UNINSTALL_APPS);
        dpm.addUserRestriction(adminComponent, UserManager.DISALLOW_FACTORY_RESET);
        dpm.addUserRestriction(adminComponent, UserManager.DISALLOW_ADD_USER);
        dpm.addUserRestriction(adminComponent, UserManager.DISALLOW_SAFE_BOOT);

        // disableLocation: Deshabilitar configuración de ubicación
        if (config.getDisableLocation() != null && config.getDisableLocation()) {
            dpm.addUserRestriction(adminComponent, UserManager.DISALLOW_CONFIG_LOCATION);
            dpm.addUserRestriction(adminComponent, UserManager.DISALLOW_SHARE_LOCATION);
            Log.d(TAG, "Location configuration disabled");
        }

        // wifi, bluetooth, mobileData: Restricciones de conectividad
        if (config.getWifi() != null && !config.getWifi()) {
            dpm.addUserRestriction(adminComponent, UserManager.DISALLOW_CONFIG_WIFI);
            Log.d(TAG, "WiFi configuration disabled");
        }

        if (config.getBluetooth() != null && !config.getBluetooth()) {
            dpm.addUserRestriction(adminComponent, UserManager.DISALLOW_CONFIG_BLUETOOTH);
            Log.d(TAG, "Bluetooth configuration disabled");
        }

        if (config.getMobileData() != null && !config.getMobileData()) {
            dpm.addUserRestriction(adminComponent, UserManager.DISALLOW_CONFIG_MOBILE_NETWORKS);
            Log.d(TAG, "Mobile data configuration disabled");
        }

        // usbStorage: Bloquear transferencia de archivos por USB
        if (config.getUsbStorage() != null && !config.getUsbStorage()) {
            dpm.addUserRestriction(adminComponent, UserManager.DISALLOW_USB_FILE_TRANSFER);
            Log.d(TAG, "USB file transfer disabled");
        }

        // disableScreenshots: Bloquear capturas de pantalla
        if (config.isDisableScreenshots()) {
            dpm.addUserRestriction(adminComponent, UserManager.DISALLOW_CREATE_WINDOWS);
            // Nota: DISALLOW_CREATE_WINDOWS puede ser muy restrictivo, considera alternativas
            Log.d(TAG, "Screenshots disabled");
        }

        // Procesar restrictions adicionales (formato: comma-separated)
        if (config.getRestrictions() != null && !config.getRestrictions().isEmpty()) {
            String[] restrictions = config.getRestrictions().split(",");
            for (String restriction : restrictions) {
                restriction = restriction.trim();
                if (!restriction.isEmpty()) {
                    try {
                        dpm.addUserRestriction(adminComponent, restriction);
                        Log.d(TAG, "Applied custom restriction: " + restriction);
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to apply restriction: " + restriction, e);
                    }
                }
            }
        }
    }

    /**
     * Ocultar apps no permitidas
     * Apps permitidas son las que tienen useKiosk=true o showIcon=true
     */
    private static void hideNonKioskApps(Context context, DevicePolicyManager dpm,
                                         ComponentName adminComponent, ServerConfig config) {
        try {
            PackageManager pm = context.getPackageManager();
            List<ApplicationInfo> installedApps = pm.getInstalledApplications(0);

            // Construir lista de apps permitidas (no ocultar)
            List<String> allowedPackages = new ArrayList<>();

            // Siempre permitir app principal y Headwind MDM
            allowedPackages.add(config.getMainApp());
            allowedPackages.add(context.getPackageName());

            // Apps del sistema críticas
            allowedPackages.add("com.android.systemui");
            allowedPackages.add("android");

            // Apps de la configuración que están permitidas
            if (config.getApplications() != null) {
                for (Application app : config.getApplications()) {
                    // Permitir si: useKiosk=true O showIcon=true O es servicio necesario
                    if (app.isUseKiosk() || app.isShowIcon() || isEssentialSystemPackage(app.getPkg())) {
                        if (!allowedPackages.contains(app.getPkg())) {
                            allowedPackages.add(app.getPkg());
                        }
                    }
                }
            }

            // Ocultar todas las apps que NO están en la lista permitida
            for (ApplicationInfo app : installedApps) {
                if (!allowedPackages.contains(app.packageName) && !isSystemApp(app)) {
                    try {
                        boolean hidden = false;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            hidden = dpm.setApplicationHidden(adminComponent, app.packageName, true);
                        }
                        if (hidden) {
                            Log.d(TAG, "Hidden app: " + app.packageName);
                        }
                    } catch (Exception e) {
                        // Ignorar errores al ocultar apps individuales
                    }
                }
            }

            Log.d(TAG, "Apps visibility configured. Allowed packages: " + allowedPackages.size());

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Error hiding non-kiosk apps", e);
        }
    }

    /**
     * Determinar si un package es un servicio esencial del sistema
     */
    private static boolean isEssentialSystemPackage(String packageName) {
        List<String> essentialPackages = Arrays.asList(
                "com.android.bluetooth",
                "com.google.android.gms",
                "com.android.providers.media",
                "com.android.packageinstaller",
                "com.google.android.packageinstaller",
                "com.android.permissioncontroller",
                "com.android.vpndialogs",
                "com.android.incallui",
                "com.android.server.telecom",
                "com.android.phone",
                "com.google.android.inputmethod.latin",
                "com.android.inputmethod",
                "com.samsung.android.inputmethod",
                "com.google.android.gms.setup"
        );
        return essentialPackages.contains(packageName);
    }

    private static boolean isSystemApp(ApplicationInfo app) {
        return (app.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
    }

    /**
     * Activar Lock Task usando reflexión (para Android 9+)
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private static void activateLockTaskForApp(Context context, String packageName) {
        try {
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.AppTask> tasks = am.getAppTasks();

            for (ActivityManager.AppTask task : tasks) {
                ActivityManager.RecentTaskInfo taskInfo = task.getTaskInfo();
                if (taskInfo.baseIntent != null &&
                        taskInfo.baseIntent.getComponent() != null &&
                        taskInfo.baseIntent.getComponent().getPackageName().equals(packageName)) {

                    // Usar reflexión para Android 9+
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        Method method = ActivityManager.class.getMethod("startSystemLockTaskMode", int.class);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            method.invoke(am, taskInfo.taskId);
                            Log.d(TAG, "Lock task activated for task ID: " + taskInfo.taskId);
                        }
                    }
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Error activating lock task", e);
        }
    }

    /**
     * Actualizar opciones de kiosko (llamado cuando cambia la config)
     */
    public static void updateKioskOptions(Activity activity) {
        ServerConfig config = SettingsHelper.getInstance(activity).getConfig();
        if (config == null) {
            return;
        }

        try {
            DevicePolicyManager dpm = (DevicePolicyManager) activity.getSystemService(Context.DEVICE_POLICY_SERVICE);
            ComponentName adminComponent = getAdminComponent(activity);

            if (dpm == null || adminComponent == null) {
                return;
            }

            // Reconfigurar Lock Task features
            configureLockTaskFeatures(activity, dpm, adminComponent, config);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Actualizar lista de apps permitidas en kiosko
     */
    public static void updateKioskAllowedApps(String kioskApp, Activity activity, boolean enableSettings) {
        ServerConfig config = SettingsHelper.getInstance(activity).getConfig();
        if (config != null) {
            startCosuKioskModeFromConfig(activity, config);
        }
    }

    /**
     * Desbloquear kiosko
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void unlockKiosk(Activity activity) {
        try {
            DevicePolicyManager dpm = (DevicePolicyManager) activity.getSystemService(Context.DEVICE_POLICY_SERVICE);
            ComponentName adminComponent = getAdminComponent(activity);

            if (dpm == null || adminComponent == null) {
                return;
            }

            // 1. Detener Lock Task
            ActivityManager am = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                Method method = ActivityManager.class.getMethod("stopSystemLockTaskMode");
                method.invoke(am);
            }

            // 2. Limpiar Lock Task packages
            dpm.setLockTaskPackages(adminComponent, new String[]{});

            // 3. Rehabilitar UI
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                dpm.setStatusBarDisabled(adminComponent, false);
                dpm.setKeyguardDisabled(adminComponent, false);
            }

            // 4. Quitar todas las restricciones
            clearAllKioskRestrictions(dpm, adminComponent);

            // 5. Mostrar todas las apps ocultas
            showAllApps(activity, dpm, adminComponent);

            RemoteLogger.log(activity, Const.LOG_INFO, "Kiosk mode unlocked");

            // 6. Volver al launcher de Headwind
            Intent intent = new Intent(activity, activity.getClass());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            activity.startActivity(intent);

        } catch (Exception e) {
            e.printStackTrace();
            RemoteLogger.log(activity, Const.LOG_ERROR, "Error unlocking kiosk: " + e.getMessage());
        }
    }

    /**
     * Limpiar TODAS las restricciones de kiosko
     */
    private static void clearAllKioskRestrictions(DevicePolicyManager dpm, ComponentName adminComponent) {
        String[] allRestrictions = {
                UserManager.DISALLOW_INSTALL_APPS,
                UserManager.DISALLOW_UNINSTALL_APPS,
                UserManager.DISALLOW_FACTORY_RESET,
                UserManager.DISALLOW_ADD_USER,
                UserManager.DISALLOW_SAFE_BOOT,
                UserManager.DISALLOW_CONFIG_LOCATION,
                UserManager.DISALLOW_SHARE_LOCATION,
                UserManager.DISALLOW_CONFIG_WIFI,
                UserManager.DISALLOW_CONFIG_BLUETOOTH,
                UserManager.DISALLOW_CONFIG_MOBILE_NETWORKS,
                UserManager.DISALLOW_USB_FILE_TRANSFER,
                UserManager.DISALLOW_ADJUST_VOLUME,
                UserManager.DISALLOW_CREATE_WINDOWS
        };

        for (String restriction : allRestrictions) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    dpm.clearUserRestriction(adminComponent, restriction);
                }
            } catch (Exception e) {
                // Ignorar errores
            }
        }
    }

    /**
     * Mostrar todas las apps ocultas
     */
    private static void showAllApps(Context context, DevicePolicyManager dpm, ComponentName adminComponent) {
        try {
            PackageManager pm = context.getPackageManager();
            List<ApplicationInfo> apps = pm.getInstalledApplications(0);

            for (ApplicationInfo app : apps) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        dpm.setApplicationHidden(adminComponent, app.packageName, false);
                    }
                } catch (Exception e) {
                    // Ignorar errores
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    // Agregar en ProUtils.java o en una Activity de admin

    public static void emergencyRemoveDeviceOwner(Context context) {
        try {
            DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
            ComponentName adminComponent = new ComponentName(context, "com.hmdm.launcher.AdminReceiver");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // Limpiar todas las restricciones primero
                clearAllRestrictions(dpm, adminComponent);

                // Limpiar Lock Task
                dpm.setLockTaskPackages(adminComponent, new String[]{});

                // Remover como Device Owner
                dpm.clearDeviceOwnerApp(context.getPackageName());

                Log.i("Emergency", "Device Owner removed successfully");
                Toast.makeText(context, "Device Owner removed. You can now uninstall.", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Emergency", "Failed to remove Device Owner: " + e.getMessage());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private static void clearAllRestrictions(DevicePolicyManager dpm, ComponentName adminComponent) {
        String[] restrictions = {
                UserManager.DISALLOW_INSTALL_APPS,
                UserManager.DISALLOW_UNINSTALL_APPS,
                UserManager.DISALLOW_FACTORY_RESET,
                UserManager.DISALLOW_ADD_USER,
                UserManager.DISALLOW_SAFE_BOOT,
                UserManager.DISALLOW_CONFIG_LOCATION,
                UserManager.DISALLOW_SHARE_LOCATION,
                UserManager.DISALLOW_CONFIG_WIFI,
                UserManager.DISALLOW_CONFIG_BLUETOOTH,
                UserManager.DISALLOW_CONFIG_MOBILE_NETWORKS,
                UserManager.DISALLOW_USB_FILE_TRANSFER,
                UserManager.DISALLOW_ADJUST_VOLUME,
                UserManager.DISALLOW_CREATE_WINDOWS
        };

        for (String restriction : restrictions) {
            try {
                dpm.clearUserRestriction(adminComponent, restriction);
            } catch (Exception e) {
                // Ignorar errores
            }
        }
    }

    /**
     * Procesar configuración del servidor
     * Esta es la función que se llama desde GetServerConfigTask
     */
    public static void processConfig(Context context, ServerConfig config) {
        if (config == null) {
            return;
        }

        Log.d(TAG, "Processing server config. Kiosk mode: " + config.getKioskMode() + ", Main app: " + config.getMainApp());

        // Procesar modo kiosko
        if (config.getKioskMode() != null && config.getKioskMode()) {
            String mainApp = config.getMainApp();
            if (mainApp != null && !mainApp.isEmpty()) {
                if (context instanceof Activity) {
                    Activity activity = (Activity) context;

                    // Verificar si la app kiosko está instalada
                    if (isKioskAppInstalled(context)) {
                        // Si la app principal NO es Headwind MDM, activar kiosko para app externa
                        if (!mainApp.equals(context.getPackageName())) {
                            Log.d(TAG, "Starting kiosk mode for external app: " + mainApp);
                            startCosuKioskModeFromConfig(activity, config);
                        } else {
                            Log.d(TAG, "Main app is Headwind MDM, not starting external kiosk");
                        }
                    } else {
                        Log.w(TAG, "Kiosk app not installed: " + mainApp);
                        RemoteLogger.log(context, Const.LOG_WARN, "Kiosk app not found: " + mainApp);
                    }
                }
            }
        } else {
            // Desactivar kiosko si está corriendo
            if (isKioskModeRunning(context) && context instanceof Activity) {
                Log.d(TAG, "Kiosk mode disabled in config, unlocking...");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    unlockKiosk((Activity) context);
                }
            }
        }
    }

    /**
     * Obtener componente de administrador
     */
    private static ComponentName getAdminComponent(Context context) {
        try {
            // El AdminReceiver de Headwind MDM
            return new ComponentName(context, "com.hmdm.launcher.AdminReceiver");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void processLocation(Context context, Location location, String provider) {
        // Stub
    }

    public static String getAppName(Context context) {
        return context.getString(R.string.app_name);
    }

    public static String getCopyright(Context context) {
        return "(c) " + Calendar.getInstance().get(Calendar.YEAR) + " " + context.getString(R.string.vendor);
    }
}