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
import android.os.UserManager;
import android.util.Log;
import android.view.View;

import androidx.annotation.RequiresApi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hmdm.launcher.Const;
import com.hmdm.launcher.R;
import com.hmdm.launcher.helper.SettingsHelper;
import com.hmdm.launcher.json.ServerConfig;
import com.hmdm.launcher.util.RemoteLogger;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

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
        // Stub o implementa Crashlytics si lo necesitas
    }

    public static void sendExceptionToCrashlytics(Throwable e) {
        // Stub
    }

    public static boolean checkAccessibilityService(Context context) {
        // Stub - implementa si necesitas verificar servicio de accesibilidad
        return true;
    }

    public static boolean checkUsageStatistics(Context context) {
        // Stub - implementa si necesitas estadísticas de uso
        return true;
    }

    public static View preventStatusBarExpansion(Activity activity) {
        // Stub - implementa vista transparente sobre status bar si lo necesitas
        return null;
    }

    public static View preventApplicationsList(Activity activity) {
        // Stub
        return null;
    }

    public static View createKioskUnlockButton(Activity activity) {
        // Stub - implementa botón de desbloqueo si lo necesitas
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
     * FUNCIÓN PRINCIPAL: Inicia el modo kiosko
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static boolean startCosuKioskMode(String kioskApp, Activity activity, boolean enableSettings) {
        if (kioskApp == null || kioskApp.isEmpty()) {
            Log.e(TAG, "Kiosk app package name is empty");
            return false;
        }

        try {
            // Obtener DevicePolicyManager
            DevicePolicyManager dpm = (DevicePolicyManager) activity.getSystemService(Context.DEVICE_POLICY_SERVICE);
            ComponentName adminComponent = getAdminComponent(activity);

            if (dpm == null || adminComponent == null) {
                Log.e(TAG, "Device Policy Manager or Admin Component is null");
                return false;
            }

            // Verificar que somos Device Owner
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                if (!dpm.isDeviceOwnerApp(activity.getPackageName())) {
                    Log.e(TAG, "App is not Device Owner");
                    RemoteLogger.log(activity, Const.LOG_WARN, "Cannot start kiosk mode: app is not Device Owner");
                    return false;
                }
            }

            // 1. CONFIGURAR LOCK TASK PACKAGES
            String[] lockTaskPackages = {
                    kioskApp,  // App que queremos poner en kiosko
                    activity.getPackageName()  // Headwind MDM (para poder controlar)
            };

            if (enableSettings) {
                // Agregar app de configuración si es necesario
                lockTaskPackages = new String[]{
                        kioskApp,
                        activity.getPackageName(),
                        "com.android.settings"
                };
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                dpm.setLockTaskPackages(adminComponent, lockTaskPackages);
            }
            Log.d(TAG, "Lock task packages set: " + Arrays.toString(lockTaskPackages));

            // 2. CONFIGURAR OPCIONES DE KIOSKO
            updateKioskOptions(activity);

            // 3. DESHABILITAR BARRA DE ESTADO Y KEYGUARD
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                dpm.setStatusBarDisabled(adminComponent, true);
                dpm.setKeyguardDisabled(adminComponent, true);
            }

            // 4. APLICAR RESTRICCIONES
            applyKioskRestrictions(activity, dpm, adminComponent);

            // 5. OCULTAR OTRAS APPS (opcional)
            hideNonKioskApps(activity, dpm, adminComponent, kioskApp, enableSettings);

            // 6. LANZAR LA APP EN LOCK TASK
            Intent intent = getKioskAppIntent(kioskApp, activity);
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                activity.startActivity(intent);

                // Activar Lock Task después de un delay
                activity.runOnUiThread(() -> {
                    try {
                        Thread.sleep(1000);
                        activateLockTaskForApp(activity, kioskApp);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });

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
     * Configurar opciones de Lock Task (qué está permitido en kiosko)
     */
    public static void updateKioskOptions(Activity activity) {
        try {
            DevicePolicyManager dpm = (DevicePolicyManager) activity.getSystemService(Context.DEVICE_POLICY_SERVICE);
            ComponentName adminComponent = getAdminComponent(activity);

            if (dpm == null || adminComponent == null) {
                return;
            }

            SettingsHelper settingsHelper = SettingsHelper.getInstance(activity);
            ServerConfig config = settingsHelper.getConfig();

            if (config == null) {
                return;
            }

            // Configurar características permitidas en Lock Task
            int lockTaskFeatures = DevicePolicyManager.LOCK_TASK_FEATURE_NONE;

            // Leer configuración del servidor
            if (config.getKioskHome() != null && config.getKioskHome()) {
                lockTaskFeatures |= DevicePolicyManager.LOCK_TASK_FEATURE_HOME;
            }

            if (config.getKioskNotifications() != null && config.getKioskNotifications()) {
                lockTaskFeatures |= DevicePolicyManager.LOCK_TASK_FEATURE_NOTIFICATIONS;
            }

            if (config.getKioskRecents() != null && config.getKioskRecents()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    lockTaskFeatures |= DevicePolicyManager.LOCK_TASK_FEATURE_OVERVIEW;
                }
            }

            if (config.getKioskSystemInfo() != null && config.getKioskSystemInfo()) {
                lockTaskFeatures |= DevicePolicyManager.LOCK_TASK_FEATURE_SYSTEM_INFO;
            }

            if (config.getKioskKeyguard() != null && config.getKioskKeyguard()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    lockTaskFeatures |= DevicePolicyManager.LOCK_TASK_FEATURE_KEYGUARD;
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                dpm.setLockTaskFeatures(adminComponent, lockTaskFeatures);
            }
            Log.d(TAG, "Lock task features updated: " + lockTaskFeatures);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Actualizar lista de apps permitidas en kiosko
     */
    public static void updateKioskAllowedApps(String kioskApp, Activity activity, boolean enableSettings) {
        // Esta función se llama cuando cambia la configuración
        // Reutilizamos la lógica de startCosuKioskMode
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            startCosuKioskMode(kioskApp, activity, enableSettings);
        }
    }

    /**
     * Aplicar restricciones de usuario para kiosko
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private static void applyKioskRestrictions(Context context, DevicePolicyManager dpm, ComponentName adminComponent) {
        // Restricciones básicas
        dpm.addUserRestriction(adminComponent, UserManager.DISALLOW_INSTALL_APPS);
        dpm.addUserRestriction(adminComponent, UserManager.DISALLOW_UNINSTALL_APPS);
        dpm.addUserRestriction(adminComponent, UserManager.DISALLOW_FACTORY_RESET);
        dpm.addUserRestriction(adminComponent, UserManager.DISALLOW_ADD_USER);
        dpm.addUserRestriction(adminComponent, UserManager.DISALLOW_SAFE_BOOT);

        SettingsHelper settingsHelper = SettingsHelper.getInstance(context);
        ServerConfig config = settingsHelper.getConfig();

        if (config != null && config.getDisableLocation() != null && config.getDisableLocation()) {
            dpm.addUserRestriction(adminComponent, UserManager.DISALLOW_CONFIG_LOCATION);
        }
    }

    /**
     * Ocultar apps que no son parte del kiosko
     */
    private static void hideNonKioskApps(Context context, DevicePolicyManager dpm,
                                         ComponentName adminComponent, String kioskApp, boolean enableSettings) {
        try {
            PackageManager pm = context.getPackageManager();
            List<ApplicationInfo> apps = pm.getInstalledApplications(0);

            // Apps que NO deben ocultarse
            List<String> whitelist = Arrays.asList(
                    kioskApp,
                    context.getPackageName(),  // Headwind MDM
                    "com.android.systemui",
                    "android"
            );

            if (enableSettings) {
                whitelist.add("com.android.settings");
            }

            for (ApplicationInfo app : apps) {
                if (!whitelist.contains(app.packageName) && !isSystemApp(app)) {
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            dpm.setApplicationHidden(adminComponent, app.packageName, true);
                        }
                    } catch (Exception e) {
                        // Ignorar errores al ocultar apps individuales
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean isSystemApp(ApplicationInfo app) {
        return (app.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
    }

    /**
     * Activar Lock Task usando reflexión (para Android 9+)
     */
    private static void activateLockTaskForApp(Context context, String packageName) {
        try {
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.AppTask> tasks = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                tasks = am.getAppTasks();
            }

            for (ActivityManager.AppTask task : tasks) {
                ActivityManager.RecentTaskInfo taskInfo = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    taskInfo = task.getTaskInfo();
                }
                if (taskInfo.baseIntent != null &&
                        taskInfo.baseIntent.getComponent() != null &&
                        taskInfo.baseIntent.getComponent().getPackageName().equals(packageName)) {

                    // Usar reflexión para Android 9+
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        Method method = ActivityManager.class.getMethod("startSystemLockTaskMode", int.class);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            method.invoke(am, taskInfo.taskId);
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            Log.d(TAG, "Lock task activated for task ID: " + taskInfo.taskId);
                        }
                    }
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Desbloquear kiosko
     */
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                dpm.setLockTaskPackages(adminComponent, new String[]{});
            }

            // 3. Rehabilitar UI
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                dpm.setStatusBarDisabled(adminComponent, false);
                dpm.setKeyguardDisabled(adminComponent, false);
            }

            // 4. Quitar restricciones
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                clearKioskRestrictions(dpm, adminComponent);
            }

            // 5. Mostrar apps ocultas
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

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private static void clearKioskRestrictions(DevicePolicyManager dpm, ComponentName adminComponent) {
        dpm.clearUserRestriction(adminComponent, UserManager.DISALLOW_INSTALL_APPS);
        dpm.clearUserRestriction(adminComponent, UserManager.DISALLOW_UNINSTALL_APPS);
        dpm.clearUserRestriction(adminComponent, UserManager.DISALLOW_FACTORY_RESET);
        dpm.clearUserRestriction(adminComponent, UserManager.DISALLOW_ADD_USER);
        dpm.clearUserRestriction(adminComponent, UserManager.DISALLOW_SAFE_BOOT);
        dpm.clearUserRestriction(adminComponent, UserManager.DISALLOW_CONFIG_LOCATION);
    }

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

    /**
     * Procesar configuración del servidor
     */
    public static void processConfig(Context context, ServerConfig config) {
        String configJson = "-";
        try {
            configJson = new ObjectMapper().writeValueAsString(config);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        Log.i(Const.LOG_TAG, "[processConfig] step 01, configJson -> " + configJson);
        if (config == null) {
            return;
        }

        Log.i(Const.LOG_TAG, "[processConfig] step 02");
        // Procesar modo kiosko
        if (config.getKioskMode() != null && config.getKioskMode()) {
            Log.i(Const.LOG_TAG, "[processConfig] step 03 activate");
            String mainApp = config.getMainApp();
            if (mainApp != null && !mainApp.isEmpty() && !mainApp.equals(context.getPackageName())) {
                Log.i(Const.LOG_TAG, "[processConfig] step 03, mainApp" + mainApp);
                // La app kiosko es externa
                if (context instanceof Activity) {
                    Activity activity = (Activity) context;
                    boolean enableSettings = false; // Cambiar según necesidad
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        Log.i(Const.LOG_TAG, "[processConfig] step 04 ");
                        startCosuKioskMode(mainApp, activity, enableSettings);
                    }
                }
            }
            Log.i(Const.LOG_TAG, "[processConfig] step 05 done");
        } else {
            // Desactivar kiosko si está configurado
            if (isKioskModeRunning(context) && context instanceof Activity) {
                unlockKiosk((Activity) context);
            }
            Log.i(Const.LOG_TAG, "[processConfig] step 03 deactivate");
        }
    }

    /**
     * Obtener componente de administrador
     */
    private static ComponentName getAdminComponent(Context context) {
        try {
            // Buscar el receiver de admin en el manifest
            // Asumiendo que se llama AdminReceiver
            String adminReceiverClass = "com.hmdm.launcher.AdminReceiver";
            return new ComponentName(context, adminReceiverClass);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void processLocation(Context context, Location location, String provider) {
        // Stub - implementa si necesitas procesar ubicación
    }

    public static String getAppName(Context context) {
        return context.getString(R.string.app_name);
    }

    public static String getCopyright(Context context) {
        return "(c) " + Calendar.getInstance().get(Calendar.YEAR) + " " + context.getString(R.string.vendor);
    }
}