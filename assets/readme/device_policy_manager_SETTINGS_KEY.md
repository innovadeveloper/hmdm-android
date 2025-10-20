# Reporte Completo: Settings en DevicePolicyManager

## 📋 Introducción

Los tres métodos de settings en `DevicePolicyManager` te permiten modificar configuraciones del sistema Android de manera programática como Device Owner. Cada uno controla un nivel diferente de configuración.

## 🔍 Diferencias Entre los Tres Métodos

| Método | Nivel | Scope | Requiere |
|--------|-------|-------|----------|
| `setGlobalSetting()` | Global (todo el dispositivo) | Afecta todos los usuarios | Device Owner |
| `setSecureSetting()` | Por usuario | Específico del usuario actual | Device Owner o Profile Owner |
| `setSystemSetting()` | Sistema (legacy) | Configuraciones del sistema | Device Owner o Profile Owner |

---

## 1️⃣ setGlobalSetting(ComponentName admin, String setting, String value)

**Descripción**: Configuraciones globales que afectan a **todo el dispositivo** y **todos los usuarios**.

### 📱 Constantes de Settings.Global

#### **CONECTIVIDAD Y RED**

| Constante | Qué Hace | Valores Posibles | Ejemplo de Uso |
|-----------|----------|------------------|----------------|
| `AIRPLANE_MODE_ON` | Activa/desactiva modo avión | "0" = Off, "1" = On | Forzar modo avión en horarios específicos |
| `WIFI_ON` | Estado del WiFi | "0" = Off, "1" = On | Mantener WiFi siempre encendido |
| `WIFI_SLEEP_POLICY` | Política de WiFi en suspensión | "0" = Never, "1" = Only when plugged, "2" = Always | Optimizar batería o conectividad |
| `BLUETOOTH_ON` | Estado del Bluetooth | "0" = Off, "1" = On | Forzar Bluetooth off por seguridad |
| `DATA_ROAMING` | Roaming de datos | "0" = Disabled, "1" = Enabled | Prevenir cargos de roaming |
| `MOBILE_DATA` | Datos móviles | "0" = Off, "1" = On | Forzar solo WiFi |
| `NETWORK_PREFERENCE` | Preferencia de red | "1" = WiFi preferred | Priorizar WiFi sobre datos |
| `USB_MASS_STORAGE_ENABLED` | Almacenamiento masivo USB | "0" = Disabled, "1" = Enabled | Seguridad: deshabilitar MTP |
| `WIFI_NETWORKS_AVAILABLE_NOTIFICATION_ON` | Notificar redes WiFi | "0" = Off, "1" = On | Reducir notificaciones |
| `WIFI_WATCHDOG_ON` | WiFi watchdog | "0" = Off, "1" = On | Auto-reconectar WiFi |
| `WIFI_MAX_DHCP_RETRY_COUNT` | Reintentos DHCP | Número (default: 9) | Configurar reintentos de red |
| `PREFERRED_NETWORK_MODE` | Modo de red preferido | "0" = WCDMA preferred, "1" = GSM only, etc | Forzar 4G/5G only |

#### **OPCIONES DE DESARROLLADOR Y DEBUG**

| Constante | Qué Hace | Valores Posibles | Ejemplo de Uso |
|-----------|----------|------------------|----------------|
| `ADB_ENABLED` | ⚠️ **CRÍTICO**: Debug por ADB | "0" = Disabled, "1" = Enabled | Bloquear ADB en producción |
| `DEVELOPMENT_SETTINGS_ENABLED` | Opciones de desarrollador | "0" = Disabled, "1" = Enabled | Ocultar menú de desarrollador |
| `USB_DEBUGGING_ENABLED` | Depuración USB | "0" = Disabled, "1" = Enabled | Prevenir debugging USB |
| `STAY_ON_WHILE_PLUGGED_IN` | Pantalla encendida al cargar | "0" = Never, "7" = Always | Kioscos: pantalla siempre on |
| `WAIT_FOR_DEBUGGER` | Esperar debugger al iniciar | "0" = No, "1" = Yes | Debug de apps al boot |
| `DEBUG_APP` | App a debuggear | Package name | Desarrollo controlado |

#### **COMPORTAMIENTO DEL SISTEMA**

| Constante | Qué Hace | Valores Posibles | Ejemplo de Uso |
|-----------|----------|------------------|----------------|
| `AUTO_TIME` | Fecha/hora automática | "0" = Manual, "1" = Automatic | Forzar sincronización horaria |
| `AUTO_TIME_ZONE` | Zona horaria automática | "0" = Manual, "1" = Automatic | Sincronizar zona horaria |
| `BOOT_COUNT` | ⚠️ Solo lectura: Contador de boots | Número | Monitorear reinicios |
| `DEVICE_PROVISIONED` | Dispositivo provisionado | "0" = No, "1" = Yes | Estado de setup inicial |
| `HTTP_PROXY` | Proxy HTTP global | "host:port" | Forzar proxy corporativo |
| `INSTALL_NON_MARKET_APPS` | ⚠️ Deprecated: Apps de fuentes desconocidas | "0" = No, "1" = Yes | Usar UserManager.DISALLOW_* |
| `MODE_RINGER` | Modo de timbre | "0" = Silent, "1" = Vibrate, "2" = Normal | Forzar silencio en kioscos |
| `PACKAGE_VERIFIER_ENABLE` | Play Protect | "0" = Disabled, "1" = Enabled | Forzar verificación de apps |
| `ASSISTED_GPS_ENABLED` | A-GPS (GPS asistido) | "0" = Disabled, "1" = Enabled | Mejorar precisión GPS |
| `WIFI_DEVICE_OWNER_CONFIGS_LOCKDOWN` | Bloquear configs WiFi del DO | "0" = No, "1" = Yes | Proteger redes corporativas |

#### **ANIMACIONES Y RENDIMIENTO**

| Constante | Qué Hace | Valores Posibles | Ejemplo de Uso |
|-----------|----------|------------------|----------------|
| `ANIMATOR_DURATION_SCALE` | Escala de animaciones | "0" = Off, "1" = 1x, "0.5" = 0.5x | Acelerar UI o ahorrar batería |
| `TRANSITION_ANIMATION_SCALE` | Animaciones de transición | "0" = Off, "1" = 1x | Mejorar rendimiento |
| `WINDOW_ANIMATION_SCALE` | Animaciones de ventanas | "0" = Off, "1" = 1x | Deshabilitar animaciones |

#### **ACCESIBILIDAD**

| Constante | Qué Hace | Valores Posibles | Ejemplo de Uso |
|-----------|----------|------------------|----------------|
| `ACCESSIBILITY_ENABLED` | Servicios de accesibilidad | "0" = Disabled, "1" = Enabled | Forzar accesibilidad |
| `ACCESSIBILITY_DISPLAY_MAGNIFICATION_ENABLED` | Lupa de pantalla | "0" = Off, "1" = On | Ayuda visual |
| `ACCESSIBILITY_DISPLAY_INVERSION_ENABLED` | Inversión de colores | "0" = Off, "1" = On | Alto contraste |

#### **CAPTIVE PORTAL Y CONECTIVIDAD**

| Constante | Qué Hace | Valores Posibles | Ejemplo de Uso |
|-----------|----------|------------------|----------------|
| `CAPTIVE_PORTAL_MODE` | Detección de portal cautivo | "0" = Disabled, "1" = Enabled | Deshabilitar check de conectividad |
| `CAPTIVE_PORTAL_SERVER` | Servidor de verificación | URL | Cambiar servidor de check |

#### **NTP (SINCRONIZACIÓN DE TIEMPO)**

| Constante | Qué Hace | Valores Posibles | Ejemplo de Uso |
|-----------|----------|------------------|----------------|
| `NTP_SERVER` | Servidor NTP | Hostname/IP | Configurar servidor NTP corporativo |
| `NTP_TIMEOUT` | Timeout NTP | Milisegundos | Ajustar timeout de sincronización |

#### **OTROS GLOBALES IMPORTANTES**

| Constante | Qué Hace | Valores Posibles | Ejemplo de Uso |
|-----------|----------|------------------|----------------|
| `MULTI_SIM_DATA_CALL_SUBSCRIPTION` | SIM para datos | ID de SIM | Dispositivos dual SIM |
| `NETWORK_RECOMMENDATIONS_ENABLED` | Recomendaciones de red | "0" = Off, "1" = On | Deshabilitar sugerencias WiFi |
| `TETHER_OFFLOAD_DISABLED` | Deshabilitar offload tethering | "0" = Enabled, "1" = Disabled | Control de tethering |
| `WIFI_SCAN_ALWAYS_AVAILABLE` | Escaneo WiFi siempre activo | "0" = No, "1" = Yes | Mejorar localización |
| `DOCK_AUDIO_MEDIA_ENABLED` | Audio en dock | "0" = Disabled, "1" = Enabled | Control de audio |

### 💻 Ejemplo de Uso - setGlobalSetting

```kotlin
val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
val admin = ComponentName(this, MyDeviceAdminReceiver::class.java)

// Deshabilitar ADB completamente
dpm.setGlobalSetting(admin, Settings.Global.ADB_ENABLED, "0")

// Forzar fecha/hora automática
dpm.setGlobalSetting(admin, Settings.Global.AUTO_TIME, "1")
dpm.setGlobalSetting(admin, Settings.Global.AUTO_TIME_ZONE, "1")

// Mantener pantalla encendida cuando está conectado (kiosko)
dpm.setGlobalSetting(admin, Settings.Global.STAY_ON_WHILE_PLUGGED_IN, "7")

// Deshabilitar opciones de desarrollador
dpm.setGlobalSetting(admin, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, "0")

// Forzar WiFi siempre encendido
dpm.setGlobalSetting(admin, Settings.Global.WIFI_ON, "1")

// Deshabilitar animaciones para mejor rendimiento
dpm.setGlobalSetting(admin, Settings.Global.ANIMATOR_DURATION_SCALE, "0")
dpm.setGlobalSetting(admin, Settings.Global.TRANSITION_ANIMATION_SCALE, "0")
dpm.setGlobalSetting(admin, Settings.Global.WINDOW_ANIMATION_SCALE, "0")
```

---

## 2️⃣ setSecureSetting(ComponentName admin, String setting, String value)

**Descripción**: Configuraciones **por usuario**, más seguras que System settings. Cada usuario tiene sus propios valores.

### 🔐 Constantes de Settings.Secure

#### **LOCALIZACIÓN Y UBICACIÓN**

| Constante | Qué Hace | Valores Posibles | Ejemplo de Uso |
|-----------|----------|------------------|----------------|
| `LOCATION_MODE` | Modo de ubicación | "0" = Off, "1" = Sensors only, "2" = Battery saving, "3" = High accuracy | Forzar GPS siempre activo |
| `LOCATION_PROVIDERS_ALLOWED` | Proveedores permitidos | "gps,network,passive" | Habilitar solo GPS |
| `ASSISTED_GPS_ENABLED` | A-GPS | "0" = Off, "1" = On | Mejorar precisión GPS |

#### **SEGURIDAD Y PRIVACIDAD**

| Constante | Qué Hace | Valores Posibles | Ejemplo de Uso |
|-----------|----------|------------------|----------------|
| `INSTALL_NON_MARKET_APPS` | Apps de fuentes desconocidas | "0" = No permitir, "1" = Permitir | Bloquear instalación externa |
| `UNKNOWN_SOURCES_DEFAULT_REVERSED` | Invertir comportamiento | "0" = Default, "1" = Reversed | Cambiar default de seguridad |
| `LOCK_SCREEN_ALLOW_PRIVATE_NOTIFICATIONS` | Notificaciones privadas en lockscreen | "0" = No, "1" = Yes | Privacidad en pantalla bloqueada |
| `LOCK_SCREEN_SHOW_NOTIFICATIONS` | Mostrar notificaciones | "0" = No, "1" = Yes | Ocultar notificaciones sensibles |
| `LOCK_TO_APP_EXIT_LOCKED` | Salir de lock task requiere unlock | "0" = No, "1" = Yes | Seguridad en modo kiosko |

#### **ACCESIBILIDAD**

| Constante | Qué Hace | Valores Posibles | Ejemplo de Uso |
|-----------|----------|------------------|----------------|
| `ACCESSIBILITY_ENABLED` | Servicios de accesibilidad habilitados | "0" = Off, "1" = On | Forzar TalkBack |
| `ENABLED_ACCESSIBILITY_SERVICES` | Lista de servicios activos | "pkg/service:pkg/service" | Activar servicios específicos |
| `TOUCH_EXPLORATION_ENABLED` | Exploración táctil | "0" = Off, "1" = On | Para usuarios con discapacidad visual |
| `ACCESSIBILITY_DISPLAY_MAGNIFICATION_ENABLED` | Lupa | "0" = Off, "1" = On | Magnificación de pantalla |
| `ACCESSIBILITY_DISPLAY_INVERSION_ENABLED` | Inversión de colores | "0" = Off, "1" = On | Alto contraste |
| `ACCESSIBILITY_SPEAK_PASSWORD` | Hablar contraseñas | "0" = No, "1" = Yes | Accesibilidad vs seguridad |
| `ACCESSIBILITY_CAPTIONING_ENABLED` | Subtítulos | "0" = Off, "1" = On | Subtítulos para sordos |

#### **ENTRADA Y TECLADO**

| Constante | Qué Hace | Valores Posibles | Ejemplo de Uso |
|-----------|----------|------------------|----------------|
| `DEFAULT_INPUT_METHOD` | Teclado predeterminado | Package/Service | Forzar teclado corporativo |
| `ENABLED_INPUT_METHODS` | Métodos de entrada habilitados | "pkg:pkg:pkg" | Limitar teclados disponibles |
| `SELECTED_INPUT_METHOD_SUBTYPE` | Subtipo de método de entrada | ID numérico | Forzar idioma de teclado |
| `SHOW_IME_WITH_HARD_KEYBOARD` | Mostrar teclado con teclado físico | "0" = No, "1" = Yes | Comportamiento de teclado |
| `INPUT_METHODS_SUBTYPE_HISTORY` | Historial de subtipos | String codificado | Gestionar historial |

#### **PANTALLA Y VISUALIZACIÓN**

| Constante | Qué Hace | Valores Posibles | Ejemplo de Uso |
|-----------|----------|------------------|----------------|
| `SCREEN_BRIGHTNESS_MODE` | Modo de brillo | "0" = Manual, "1" = Automatic | Forzar brillo manual |
| `SCREEN_OFF_TIMEOUT` | Tiempo para apagar pantalla | Milisegundos | Configurar timeout |
| `ADAPTIVE_SLEEP` | Mantener pantalla encendida si miras | "0" = Off, "1" = On | Kioscos interactivos |
| `FONT_SCALE` | Escala de fuente | "1.0" = Normal, "1.15" = Large | Accesibilidad de texto |
| `DISPLAY_DENSITY_FORCED` | Densidad de pantalla forzada | DPI numérico | Cambiar DPI del sistema |

#### **SONIDO Y NOTIFICACIONES**

| Constante | Qué Hace | Valores Posibles | Ejemplo de Uso |
|-----------|----------|------------------|----------------|
| `NOTIFICATION_BUBBLES` | Burbujas de notificación | "0" = Off, "1" = On | Controlar burbujas |
| `LOCK_SCREEN_ALLOW_REMOTE_INPUT` | Input remoto en lockscreen | "0" = No, "1" = Yes | Responder notificaciones bloqueado |

#### **CONECTIVIDAD NFC Y BLUETOOTH**

| Constante | Qué Hace | Valores Posibles | Ejemplo de Uso |
|-----------|----------|------------------|----------------|
| `NFC_PAYMENT_DEFAULT_COMPONENT` | App de pago NFC predeterminada | ComponentName | Forzar app de pago corporativa |
| `BLUETOOTH_ON` | Estado Bluetooth | "0" = Off, "1" = On | Control de Bluetooth |

#### **ASISTENTE Y BÚSQUEDA**

| Constante | Qué Hace | Valores Posibles | Ejemplo de Uso |
|-----------|----------|------------------|----------------|
| `ASSISTANT` | Asistente predeterminado | ComponentName | Cambiar de Google Assistant |
| `VOICE_INTERACTION_SERVICE` | Servicio de interacción por voz | ComponentName | Configurar asistente de voz |
| `VOICE_RECOGNITION_SERVICE` | Servicio de reconocimiento | ComponentName | Motor de voz |

#### **ROTAR PANTALLA Y ORIENTACIÓN**

| Constante | Qué Hace | Valores Posibles | Ejemplo de Uso |
|-----------|----------|------------------|----------------|
| `ACCELEROMETER_ROTATION` | Rotación automática | "0" = Off, "1" = On | Bloquear orientación |
| `USER_ROTATION` | Rotación forzada del usuario | "0" = 0°, "1" = 90°, "2" = 180°, "3" = 270° | Forzar landscape |

#### **PRIVACIDAD Y PERMISOS**

| Constante | Qué Hace | Valores Posibles | Ejemplo de Uso |
|-----------|----------|------------------|----------------|
| `LOCATION_CHANGER` | Quién cambió ubicación | Package name | Auditoría |
| `LOCK_SCREEN_LOCK_AFTER_TIMEOUT` | Bloqueo tras timeout | Milisegundos | Seguridad automática |

#### **DOZE Y BATERÍA**

| Constante | Qué Hace | Valores Posibles | Ejemplo de Uso |
|-----------|----------|------------------|----------------|
| `DOZE_ENABLED` | Modo Doze habilitado | "0" = Disabled, "1" = Enabled | Optimizar batería |

#### **DESARROLLO Y DEBUG**

| Constante | Qué Hace | Valores Posibles | Ejemplo de Uso |
|-----------|----------|------------------|----------------|
| `ADB_ENABLED` | ADB habilitado para este usuario | "0" = Off, "1" = On | Control de ADB por usuario |
| `ANDROID_ID` | ⚠️ Solo lectura: ID único de Android | String hex | Identificador del dispositivo |

#### **OTROS SECURE IMPORTANTES**

| Constante | Qué Hace | Valores Posibles | Ejemplo de Uso |
|-----------|----------|----------|----------------|
| `BACKUP_AUTO_RESTORE` | Restauración automática | "0" = No, "1" = Yes | Control de backups |
| `BACKUP_ENABLED` | Backup habilitado | "0" = No, "1" = Yes | Política de respaldo |
| `SKIP_FIRST_USE_HINTS` | Saltar hints de primer uso | "0" = No, "1" = Yes | Setup automatizado |
| `SCREENSAVER_ENABLED` | Salvapantallas habilitado | "0" = No, "1" = Yes | Kioscos con screensaver |
| `SCREENSAVER_COMPONENTS` | Componentes de screensaver | ComponentName | Configurar screensaver |

### 💻 Ejemplo de Uso - setSecureSetting

```kotlin
val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
val admin = ComponentName(this, MyDeviceAdminReceiver::class.java)

// Forzar GPS siempre en modo alta precisión
dpm.setSecureSetting(admin, Settings.Secure.LOCATION_MODE, "3")

// Bloquear instalación de fuentes desconocidas
dpm.setSecureSetting(admin, Settings.Secure.INSTALL_NON_MARKET_APPS, "0")

// Ocultar notificaciones en pantalla bloqueada
dpm.setSecureSetting(admin, Settings.Secure.LOCK_SCREEN_SHOW_NOTIFICATIONS, "0")

// Deshabilitar rotación automática
dpm.setSecureSetting(admin, Settings.Secure.ACCELEROMETER_ROTATION, "0")

// Forzar orientación landscape
dpm.setSecureSetting(admin, Settings.Secure.USER_ROTATION, "1") // 90 grados

// Configurar timeout de pantalla (30 segundos)
dpm.setSecureSetting(admin, Settings.Secure.SCREEN_OFF_TIMEOUT, "30000")

// Forzar teclado específico
dpm.setSecureSetting(admin, Settings.Secure.DEFAULT_INPUT_METHOD, 
    "com.tu.teclado/.TuTecladoService")

// Deshabilitar ADB para este usuario
dpm.setSecureSetting(admin, Settings.Secure.ADB_ENABLED, "0")
```

---

## 3️⃣ setSystemSetting(ComponentName admin, String setting, String value)

**Descripción**: Configuraciones del sistema (legacy). Muchas han sido **movidas a Secure** en versiones modernas de Android.

### ⚙️ Constantes de Settings.System

#### **PANTALLA Y BRILLO**

| Constante | Qué Hace | Valores Posibles | Ejemplo de Uso |
|-----------|----------|------------------|----------------|
| `SCREEN_BRIGHTNESS` | Brillo de pantalla | 0-255 | Establecer brillo fijo |
| `SCREEN_BRIGHTNESS_MODE` | Modo de brillo | "0" = Manual, "1" = Automatic | Deshabilitar brillo automático |
| `SCREEN_OFF_TIMEOUT` | Timeout de pantalla | Milisegundos | Configurar apagado automático |

#### **SONIDO Y VOLUMEN**

| Constante | Qué Hace | Valores Posibles | Ejemplo de Uso |
|-----------|----------|------------------|----------------|
| `VOLUME_RING` | Volumen de timbre | 0-7 (depende del dispositivo) | Establecer volumen fijo |
| `VOLUME_SYSTEM` | Volumen del sistema | 0-7 | Sonidos del sistema |
| `VOLUME_MUSIC` | Volumen multimedia | 0-15 | Controlar volumen multimedia |
| `VOLUME_ALARM` | Volumen de alarmas | 0-7 | Alarmas |
| `VOLUME_NOTIFICATION` | Volumen de notificaciones | 0-7 | Notificaciones |
| `VOLUME_VOICE` | Volumen de voz (llamadas) | 0-7 | Llamadas telefónicas |
| `MODE_RINGER` | Modo de timbre | "0" = Silent, "1" = Vibrate, "2" = Normal | Forzar silencio |
| `VIBRATE_ON` | Vibración habilitada | "0" = Off, "1" = On | Control de vibración |
| `VIBRATE_WHEN_RINGING` | Vibrar al timbrar | "0" = No, "1" = Yes | Comportamiento de llamadas |
| `NOTIFICATION_SOUND` | Sonido de notificación | URI | Cambiar sonido |
| `RINGTONE` | Tono de llamada | URI | Cambiar ringtone |
| `ALARM_ALERT` | Sonido de alarma | URI | Cambiar alarma |

#### **FECHA Y HORA**

| Constante | Qué Hace | Valores Posibles | Ejemplo de Uso |
|-----------|----------|------------------|----------------|
| `TIME_12_24` | Formato de hora | "12" o "24" | Forzar formato 24h |
| `DATE_FORMAT` | Formato de fecha | Pattern string | Cambiar formato de fecha |

#### **TEXTO Y FUENTE**

| Constante | Qué Hace | Valores Posibles | Ejemplo de Uso |
|-----------|----------|------------------|----------------|
| `FONT_SCALE` | Escala de fuente | Float "1.0" = Normal, "1.15" = Large | Accesibilidad |
| `TEXT_AUTO_REPLACE` | Autocorrección | "0" = Off, "1" = On | Control de teclado |
| `TEXT_AUTO_CAPS` | Mayúsculas automáticas | "0" = Off, "1" = On | Comportamiento de teclado |
| `TEXT_AUTO_PUNCTUATE` | Puntuación automática | "0" = Off, "1" = On | Puntuación inteligente |
| `TEXT_SHOW_PASSWORD` | Mostrar contraseña | "0" = No, "1" = Yes | Seguridad vs UX |

#### **ROTACIÓN Y ORIENTACIÓN**

| Constante | Qué Hace | Valores Posibles | Ejemplo de Uso |
|-----------|----------|------------------|----------------|
| `ACCELEROMETER_ROTATION` | Rotación automática | "0" = Off, "1" = On | Bloquear rotación |
| `USER_ROTATION` | Rotación forzada | "0" = 0°, "1" = 90°, "2" = 180°, "3" = 270° | Forzar orientación |

#### **CONFIGURACIÓN DE PANTALLA**

| Constante | Qué Hace | Valores Posibles | Ejemplo de Uso |
|-----------|----------|------------------|----------------|
| `DIM_SCREEN` | Atenuar pantalla antes de apagar | "0" = No, "1" = Yes | Comportamiento de pantalla |
| `SHOW_TOUCHES` | Mostrar toques en pantalla | "0" = No, "1" = Yes | Debug visual |
| `POINTER_LOCATION` | Mostrar ubicación del puntero | "0" = No, "1" = Yes | Debug táctil |

#### **SONIDOS DEL SISTEMA**

| Constante | Qué Hace | Valores Posibles | Ejemplo de Uso |
|-----------|----------|------------------|----------------|
| `DTMF_TONE_WHEN_DIALING` | Tonos al marcar | "0" = Off, "1" = On | Silenciar marcado |
| `SOUND_EFFECTS_ENABLED` | Efectos de sonido | "0" = Off, "1" = On | Deshabilitar clicks |
| `HAPTIC_FEEDBACK_ENABLED` | Feedback háptico | "0" = Off, "1" = On | Deshabilitar vibración táctil |
| `LOCKSCREEN_SOUNDS_ENABLED` | Sonidos de bloqueo | "0" = Off, "1" = On | Silenciar lockscreen |

#### **ANIMACIONES (Legacy - ahora en Global)**

| Constante | Qué Hace | Valores Posibles | Ejemplo de Uso |
|-----------|----------|------------------|----------------|
| `WINDOW_ANIMATION_SCALE` | Escala animaciones ventana | Float "0.0" = Off, "1.0" = Normal | Deshabilitar animaciones |
| `TRANSITION_ANIMATION_SCALE` | Escala animaciones transición | Float "0.0" = Off | Mejorar rendimiento |

#### **OTROS SYSTEM IMPORTANTES**

| Constante | Qué Hace | Valores Posibles | Ejemplo de Uso |
|-----------|----------|------------------|----------------|
| `END_BUTTON_BEHAVIOR` | Comportamiento botón power | "1" = Go home, "2" = Sleep, "3" = Nothing | Cambiar botón power |
| `ADVANCED_SETTINGS` | Configuraciones avanzadas | "0" = Hide, "1" = Show | Ocultar opciones |
| `SETUP_WIZARD_HAS_RUN` | Setup wizard completado | "0" = No, "1" = Yes | Marcar como configurado |

### 💻 Ejemplo de Uso - setSystemSetting

```kotlin
val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
val admin = ComponentName(this, MyDeviceAdminReceiver::class.java)

// Establecer brillo fijo al 70%
dpm.setSystemSetting(admin, Settings.System.SCREEN_BRIGHTNESS_MODE, "0") // Manual
dpm.setSystemSetting(admin, Settings.System.SCREEN_BRIGHTNESS, "178") // ~70% de 255

// Timeout de pantalla: 5 minutos
dpm.setSystemSetting(admin, Settings.System.SCREEN_OFF_TIMEOUT, "300000")

// Forzar formato 24 horas
dpm.setSystemSetting(admin, Settings.System.TIME_12_24, "24")

// Establecer volúmenes fijos
dpm.setSystemSetting(admin, Settings.System.VOLUME_RING, "5")
dpm.setSystemSetting(admin, Settings.System.VOLUME_MUSIC, "10")
dpm.setSystemSetting(admin, Settings.System.VOLUME_ALARM, "6")

// Deshabilitar todos los sonidos del sistema
dpm.setSystemSetting(admin, Settings.System.SOUND_EFFECTS_ENABLED, "0")
dpm.setSystemSetting(admin, Settings.System.HAPTIC_FEEDBACK_ENABLED, "0")
dpm.setSystemSetting(admin, Settings.System.LOCKSCREEN_SOUNDS_ENABLED, "0")

// Deshabilitar rotación automática
dpm.setSystemSetting(admin, Settings.System.ACCELEROMETER_ROTATION, "0")

// Forzar orientación landscape (90°)
dpm.setSystemSetting(admin, Settings.System.USER_ROTATION, "1")

// Deshabilitar animaciones completamente
dpm.setSystemSetting(admin, Settings.System.WINDOW_ANIMATION_SCALE, "0")
dpm.setSystemSetting(admin, Settings.System.TRANSITION_ANIMATION_SCALE, "0")

// Escala de fuente grande (accesibilidad)
dpm.setSystemSetting(admin, Settings.System.FONT_SCALE, "1.3")
```

---

## 🛡️ RESTRICCIONES Y CONSIDERACIONES IMPORTANTES

### ⚠️ Permisos Requeridos

```xml
<!-- En tu AndroidManifest.xml -->
<uses-permission android:name="android.permission.WRITE_SETTINGS" />
<uses-permission android:name="android.permission.WRITE_SECURE_SETTINGS" />
```

**NOTA**: Como Device Owner, estos permisos son otorgados automáticamente. No necesitas solicitarlos explícitamente en runtime.

### 🚫 Settings que NO Debes Modificar

Algunos settings son **solo lectura** o causarán problemas si los modificas:

- `Settings.Secure.ANDROID_ID` - Solo lectura, ID único del dispositivo
- `Settings.Global.BOOT_COUNT` - Solo lectura, contador de reinicios
- `Settings.Global.DEVICE_PROVISIONED` - Solo modificar en provisioning inicial
- Cualquier setting con `_HISTORY` en el nombre - Gestionado por el sistema

### 📱 Compatibilidad por Versión de Android

| Setting | Introducido en | Deprecado en | Notas |
|---------|----------------|--------------|-------|
| `INSTALL_NON_MARKET_APPS` | API 1 | API 26 (Android 8.0) | Usar UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES |
| `HTTP_PROXY` | API 1 | - | Funciona pero preferir `setRecommendedGlobalProxy()` |
| `AIRPLANE_MODE_ON` | API 1 | - | Algunos OEM pueden ignorarlo |
| `LOCATION_MODE` | API 19 | - | Cambió significado en API 28+ |
| `ADB_ENABLED` | API 1 | - | Crítico para seguridad |

### ⚙️ Verificar Settings Actuales

```kotlin
// Leer un Global setting
val adbEnabled = Settings.Global.getInt(
    contentResolver, 
    Settings.Global.ADB_ENABLED, 
    0 // default value
)

// Leer un Secure setting
val locationMode = Settings.Secure.getInt(
    contentResolver,
    Settings.Secure.LOCATION_MODE,
    0
)

// Leer un System setting
val brightness = Settings.System.getInt(
    contentResolver,
    Settings.System.SCREEN_BRIGHTNESS,
    128 // default
)

// Verificar si un setting existe
try {
    val value = Settings.Global.getString(contentResolver, "SETTING_NAME")
    if (value != null) {
        Log.d("Settings", "Setting existe con valor: $value")
    }
} catch (e: Settings.SettingNotFoundException) {
    Log.e("Settings", "Setting no encontrado")
}
```

---

## 🎯 CASOS DE USO PRÁCTICOS COMPLETOS

### 📱 Caso 1: Kiosko de Punto de Venta (POS)

```kotlin
fun configurarPOS(dpm: DevicePolicyManager, admin: ComponentName) {
    // Pantalla siempre encendida cuando está conectado
    dpm.setGlobalSetting(admin, Settings.Global.STAY_ON_WHILE_PLUGGED_IN, "7")
    
    // Brillo fijo alto para visibilidad
    dpm.setSystemSetting(admin, Settings.System.SCREEN_BRIGHTNESS_MODE, "0")
    dpm.setSystemSetting(admin, Settings.System.SCREEN_BRIGHTNESS, "230") // ~90%
    
    // Timeout largo (10 minutos)
    dpm.setSystemSetting(admin, Settings.System.SCREEN_OFF_TIMEOUT, "600000")
    
    // Sin animaciones para mejor rendimiento
    dpm.setGlobalSetting(admin, Settings.Global.WINDOW_ANIMATION_SCALE, "0")
    dpm.setGlobalSetting(admin, Settings.Global.TRANSITION_ANIMATION_SCALE, "0")
    dpm.setGlobalSetting(admin, Settings.Global.ANIMATOR_DURATION_SCALE, "0")
    
    // WiFi siempre encendido
    dpm.setGlobalSetting(admin, Settings.Global.WIFI_ON, "1")
    dpm.setGlobalSetting(admin, Settings.Global.WIFI_SLEEP_POLICY, "2") // Never sleep
    
    // Deshabilitar todos los sonidos
    dpm.setSystemSetting(admin, Settings.System.SOUND_EFFECTS_ENABLED, "0")
    dpm.setSystemSetting(admin, Settings.System.HAPTIC_FEEDBACK_ENABLED, "0")
    dpm.setSystemSetting(admin, Settings.System.VOLUME_RING, "0")
    dpm.setSystemSetting(admin, Settings.System.VOLUME_NOTIFICATION, "0")
    
    // Orientación fija landscape
    dpm.setSystemSetting(admin, Settings.System.ACCELEROMETER_ROTATION, "0")
    dpm.setSystemSetting(admin, Settings.System.USER_ROTATION, "1") // 90°
    
    // Deshabilitar rotación automática de pantalla
    dpm.setSecureSetting(admin, Settings.Secure.ACCELEROMETER_ROTATION, "0")
    
    // Ubicación en alta precisión (si necesitas geolocalización de transacciones)
    dpm.setSecureSetting(admin, Settings.Secure.LOCATION_MODE, "3")
}
```

### 🏢 Caso 2: Dispositivo Corporativo Seguro

```kotlin
fun configurarCorporativoSeguro(dpm: DevicePolicyManager, admin: ComponentName) {
    // SEGURIDAD MÁXIMA
    
    // Deshabilitar completamente ADB
    dpm.setGlobalSetting(admin, Settings.Global.ADB_ENABLED, "0")
    dpm.setSecureSetting(admin, Settings.Secure.ADB_ENABLED, "0")
    
    // Deshabilitar opciones de desarrollador
    dpm.setGlobalSetting(admin, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, "0")
    
    // Bloquear instalación de fuentes desconocidas
    dpm.setSecureSetting(admin, Settings.Secure.INSTALL_NON_MARKET_APPS, "0")
    
    // Forzar fecha/hora automática (para certificados y logs)
    dpm.setGlobalSetting(admin, Settings.Global.AUTO_TIME, "1")
    dpm.setGlobalSetting(admin, Settings.Global.AUTO_TIME_ZONE, "1")
    
    // Configurar servidor NTP corporativo
    dpm.setGlobalSetting(admin, Settings.Global.NTP_SERVER, "time.empresa.com")
    
    // Ocultar notificaciones sensibles en lockscreen
    dpm.setSecureSetting(admin, Settings.Secure.LOCK_SCREEN_SHOW_NOTIFICATIONS, "0")
    dpm.setSecureSetting(admin, Settings.Secure.LOCK_SCREEN_ALLOW_PRIVATE_NOTIFICATIONS, "0")
    
    // Timeout de pantalla corto (2 minutos)
    dpm.setSystemSetting(admin, Settings.System.SCREEN_OFF_TIMEOUT, "120000")
    
    // GPS en alta precisión para tracking de empleados
    dpm.setSecureSetting(admin, Settings.Secure.LOCATION_MODE, "3")
    
    // Forzar verificación de apps (Play Protect)
    dpm.setGlobalSetting(admin, Settings.Global.PACKAGE_VERIFIER_ENABLE, "1")
    
    // Deshabilitar Bluetooth por seguridad
    dpm.setGlobalSetting(admin, Settings.Global.BLUETOOTH_ON, "0")
    
    // WiFi corporativo siempre activo
    dpm.setGlobalSetting(admin, Settings.Global.WIFI_ON, "1")
    
    // Deshabilitar captive portal detection (puede filtrar datos)
    dpm.setGlobalSetting(admin, Settings.Global.CAPTIVE_PORTAL_MODE, "0")
}
```

### 🎮 Caso 3: Tablet de Entretenimiento Infantil

```kotlin
fun configurarTabletInfantil(dpm: DevicePolicyManager, admin: ComponentName) {
    // Brillo moderado automático para proteger vista
    dpm.setSystemSetting(admin, Settings.System.SCREEN_BRIGHTNESS_MODE, "1") // Auto
    
    // Timeout de 5 minutos
    dpm.setSystemSetting(admin, Settings.System.SCREEN_OFF_TIMEOUT, "300000")
    
    // Volumen limitado para proteger audición
    dpm.setSystemSetting(admin, Settings.System.VOLUME_MUSIC, "8") // 50% aprox
    dpm.setSystemSetting(admin, Settings.System.VOLUME_RING, "4")
    
    // Rotación automática habilitada
    dpm.setSystemSetting(admin, Settings.System.ACCELEROMETER_ROTATION, "1")
    
    // Fuente más grande para facilitar lectura
    dpm.setSystemSetting(admin, Settings.System.FONT_SCALE, "1.15")
    
    // Deshabilitar instalación de apps
    dpm.setSecureSetting(admin, Settings.Secure.INSTALL_NON_MARKET_APPS, "0")
    
    // Ubicación deshabilitada
    dpm.setSecureSetting(admin, Settings.Secure.LOCATION_MODE, "0")
    
    // Habilitar modo Doze para mejor batería
    dpm.setSecureSetting(admin, Settings.Secure.DOZE_ENABLED, "1")
    
    // Feedback háptico habilitado (más interactivo)
    dpm.setSystemSetting(admin, Settings.System.HAPTIC_FEEDBACK_ENABLED, "1")
}
```

### 📊 Caso 4: Dispositivo de Monitoreo/Dashboard

```kotlin
fun configurarDashboard(dpm: DevicePolicyManager, admin: ComponentName) {
    // Pantalla SIEMPRE encendida
    dpm.setGlobalSetting(admin, Settings.Global.STAY_ON_WHILE_PLUGGED_IN, "7")
    dpm.setSystemSetting(admin, Settings.System.SCREEN_OFF_TIMEOUT, "-1") // Never
    
    // Brillo alto fijo
    dpm.setSystemSetting(admin, Settings.System.SCREEN_BRIGHTNESS_MODE, "0")
    dpm.setSystemSetting(admin, Settings.System.SCREEN_BRIGHTNESS, "255") // Max
    
    // Orientación fija landscape
    dpm.setSystemSetting(admin, Settings.System.ACCELEROMETER_ROTATION, "0")
    dpm.setSystemSetting(admin, Settings.System.USER_ROTATION, "1") // Landscape
    
    // Sin animaciones (dashboard estático)
    dpm.setGlobalSetting(admin, Settings.Global.WINDOW_ANIMATION_SCALE, "0")
    dpm.setGlobalSetting(admin, Settings.Global.TRANSITION_ANIMATION_SCALE, "0")
    
    // Silencioso completamente
    dpm.setSystemSetting(admin, Settings.System.MODE_RINGER, "0") // Silent
    dpm.setSystemSetting(admin, Settings.System.VOLUME_RING, "0")
    dpm.setSystemSetting(admin, Settings.System.VOLUME_NOTIFICATION, "0")
    dpm.setSystemSetting(admin, Settings.System.SOUND_EFFECTS_ENABLED, "0")
    
    // WiFi siempre encendido para recibir datos
    dpm.setGlobalSetting(admin, Settings.Global.WIFI_ON, "1")
    dpm.setGlobalSetting(admin, Settings.Global.WIFI_SLEEP_POLICY, "2")
    
    // Deshabilitar Bluetooth (no necesario)
    dpm.setGlobalSetting(admin, Settings.Global.BLUETOOTH_ON, "0")
    
    // No mostrar notificaciones (puede molestar al visualizar)
    dpm.setSecureSetting(admin, Settings.Secure.LOCK_SCREEN_SHOW_NOTIFICATIONS, "0")
}
```

### 🚗 Caso 5: Dispositivo de Flota/Vehículo

```kotlin
fun configurarFlota(dpm: DevicePolicyManager, admin: ComponentName) {
    // GPS en máxima precisión SIEMPRE
    dpm.setSecureSetting(admin, Settings.Secure.LOCATION_MODE, "3") // High accuracy
    
    // WiFi escaneo siempre activo (mejor localización)
    dpm.setGlobalSetting(admin, Settings.Global.WIFI_SCAN_ALWAYS_AVAILABLE, "1")
    
    // Datos móviles siempre encendidos
    dpm.setGlobalSetting(admin, Settings.Global.MOBILE_DATA, "1")
    
    // Deshabilitar modo avión (nunca debe desconectarse)
    dpm.setGlobalSetting(admin, Settings.Global.AIRPLANE_MODE_ON, "0")
    
    // Sincronización automática de hora
    dpm.setGlobalSetting(admin, Settings.Global.AUTO_TIME, "1")
    dpm.setGlobalSetting(admin, Settings.Global.AUTO_TIME_ZONE, "1")
    
    // Formato 24 horas para logs
    dpm.setSystemSetting(admin, Settings.System.TIME_12_24, "24")
    
    // Brillo automático (condiciones de luz variables)
    dpm.setSystemSetting(admin, Settings.System.SCREEN_BRIGHTNESS_MODE, "1")
    
    // Timeout más largo (5 minutos)
    dpm.setSystemSetting(admin, Settings.System.SCREEN_OFF_TIMEOUT, "300000")
    
    // Rotación automática para adaptarse al soporte
    dpm.setSystemSetting(admin, Settings.System.ACCELEROMETER_ROTATION, "1")
    
    // Bluetooth ON para periféricos (escáner, impresora)
    dpm.setGlobalSetting(admin, Settings.Global.BLUETOOTH_ON, "1")
    
    // Deshabilitar ADB por seguridad
    dpm.setGlobalSetting(admin, Settings.Global.ADB_ENABLED, "0")
    
    // Volumen moderado para notificaciones
    dpm.setSystemSetting(admin, Settings.System.VOLUME_NOTIFICATION, "5")
}
```

### 🏥 Caso 6: Dispositivo Médico/Hospital

```kotlin
fun configurarMedico(dpm: DevicePolicyManager, admin: ComponentName) {
    // MÁXIMA SEGURIDAD Y PRIVACIDAD (HIPAA Compliance)
    
    // ADB completamente deshabilitado
    dpm.setGlobalSetting(admin, Settings.Global.ADB_ENABLED, "0")
    dpm.setSecureSetting(admin, Settings.Secure.ADB_ENABLED, "0")
    dpm.setGlobalSetting(admin, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, "0")
    
    // Sin notificaciones visibles en lockscreen (privacidad de pacientes)
    dpm.setSecureSetting(admin, Settings.Secure.LOCK_SCREEN_SHOW_NOTIFICATIONS, "0")
    dpm.setSecureSetting(admin, Settings.Secure.LOCK_SCREEN_ALLOW_PRIVATE_NOTIFICATIONS, "0")
    
    // Timeout de pantalla muy corto (30 segundos)
    dpm.setSystemSetting(admin, Settings.System.SCREEN_OFF_TIMEOUT, "30000")
    
    // Sin captura de pantalla (contenido sensible)
    // Esto se hace con DPM.setScreenCaptureDisabled(), no con settings
    
    // Deshabilitar Bluetooth (seguridad)
    dpm.setGlobalSetting(admin, Settings.Global.BLUETOOTH_ON, "0")
    
    // WiFi en red hospitalaria segura solamente
    dpm.setGlobalSetting(admin, Settings.Global.WIFI_ON, "1")
    
    // Ubicación deshabilitada (privacidad)
    dpm.setSecureSetting(admin, Settings.Secure.LOCATION_MODE, "0")
    
    // Hora sincronizada (crítico para registros médicos)
    dpm.setGlobalSetting(admin, Settings.Global.AUTO_TIME, "1")
    dpm.setGlobalSetting(admin, Settings.Global.AUTO_TIME_ZONE, "1")
    
    // Formato 24 horas para registros médicos
    dpm.setSystemSetting(admin, Settings.System.TIME_12_24, "24")
    
    // Brillo adaptativo para diferentes áreas del hospital
    dpm.setSystemSetting(admin, Settings.System.SCREEN_BRIGHTNESS_MODE, "1")
    
    // Sonido mínimo (ambiente hospitalario silencioso)
    dpm.setSystemSetting(admin, Settings.System.VOLUME_RING, "1")
    dpm.setSystemSetting(admin, Settings.System.VOLUME_NOTIFICATION, "2")
    dpm.setSystemSetting(admin, Settings.System.MODE_RINGER, "1") // Vibrate
    
    // Deshabilitar instalación de apps no autorizadas
    dpm.setSecureSetting(admin, Settings.Secure.INSTALL_NON_MARKET_APPS, "0")
    
    // Teclado médico especializado como predeterminado
    dpm.setSecureSetting(admin, Settings.Secure.DEFAULT_INPUT_METHOD, 
        "com.hospital.keyboard/.MedicalKeyboardService")
}
```

---
