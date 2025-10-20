# Restricciones UserManager

## 🔒 SEGURIDAD Y AUTENTICACIÓN

| Key | Qué Hace | Uso |
|-----|----------|-----|
| `DISALLOW_BIOMETRIC` | Deshabilita autenticación biométrica (huella dactilar, reconocimiento facial) | Ambientes que requieren solo contraseña/PIN por políticas de seguridad estrictas |
| `DISALLOW_UNIFIED_PASSWORD` | Previene usar la misma contraseña para perfil de trabajo y personal | Separación estricta en dispositivos con Work Profile, forzar contraseñas independientes |
| `DISALLOW_GRANT_ADMIN` | Impide otorgar permisos de administrador a otras aplicaciones | **CRÍTICO**: Evita que otras apps se vuelvan Device Admin y protejan tu posición como Device Owner |

---

## 🔐 DEBUGGING Y DESARROLLO

| Key | Qué Hace | Uso |
|-----|----------|-----|
| `DISALLOW_DEBUGGING_FEATURES` | Bloquea ADB, opciones de desarrollador, depuración USB | Producción, quioscos, dispositivos corporativos. **IMPORTANTE**: También te bloqueará a ti del ADB |
| `DISALLOW_OEM_UNLOCK` | Impide desbloquear el bootloader | Prevenir instalación de ROMs custom y bypass completo del sistema de seguridad |
| `DISALLOW_SAFE_BOOT` | Deshabilita arranque en modo seguro | **CRÍTICO PARA KIOSK**: Evitar que usuario arranque sin ejecutar tu app |

---

## 📱 GESTIÓN DE APLICACIONES

| Key | Qué Hace | Uso |
|-----|----------|-----|
| `DISALLOW_INSTALL_APPS` | Bloquea instalación de cualquier aplicación | Dispositivos con apps predefinidas únicamente, Play Store se vuelve inútil |
| `DISALLOW_UNINSTALL_APPS` | Previene desinstalación de aplicaciones | Proteger apps críticas del sistema, usar con `setUninstallBlocked()` para apps específicas |
| `DISALLOW_INSTALL_UNKNOWN_SOURCES` | Bloquea instalación de APKs fuera de Play Store | Seguridad estándar corporativa, solo apps de fuentes confiables |
| `DISALLOW_INSTALL_UNKNOWN_SOURCES_GLOBALLY` | Bloqueo global de fuentes desconocidas sin excepciones | Android 9+, más estricto que el anterior, máxima seguridad |
| `DISALLOW_APPS_CONTROL` | Oculta "Forzar detención" y "Desinstalar" en Configuración de Apps | Prevenir manipulación de apps desde Settings, usuario técnico aún puede usar ADB |
| `DISALLOW_RUN_IN_BACKGROUND` | Las aplicaciones no pueden ejecutarse en segundo plano | Ahorro batería extremo. **CUIDADO**: Puede romper notificaciones y servicios |

---

## 🌐 CONECTIVIDAD - WIFI Y REDES

| Key | Qué Hace | Uso |
|-----|----------|-----|
| `DISALLOW_CONFIG_WIFI` | Usuario no puede añadir/modificar redes WiFi | Control total de conectividad, solo admin configura WiFi programáticamente |
| `DISALLOW_CHANGE_WIFI_STATE` | WiFi siempre encendido/apagado según configuración del admin | Control ON/OFF del WiFi. Combinar con anterior para control total |
| `DISALLOW_WIFI_DIRECT` | Bloquea WiFi Direct (conexión peer-to-peer) | Prevenir transferencias de archivos no autorizadas y posible fuga de datos |
| `DISALLOW_WIFI_TETHERING` | Deshabilita uso del dispositivo como hotspot WiFi | Controlar consumo de datos, evitar compartir conexión corporativa |
| `DISALLOW_ADD_WIFI_CONFIG` | No añadir nuevas redes, pero puede usar existentes | Menos restrictivo que DISALLOW_CONFIG_WIFI, permite conexión a redes preconfiguradas |
| `DISALLOW_SHARING_ADMIN_CONFIGURED_WIFI` | Redes WiFi configuradas por admin no se pueden compartir | Android 10+, evitar leak de credenciales WiFi corporativo |

---

## 📶 CONECTIVIDAD - CELULAR Y DATOS

| Key | Qué Hace | Uso |
|-----|----------|-----|
| `DISALLOW_CONFIG_MOBILE_NETWORKS` | Bloquea configuración de APN, operador, datos móviles | Operadores o corporativos con SIM gestionado, prevenir cambios en configuración celular |
| `DISALLOW_DATA_ROAMING` | Deshabilita datos en roaming internacional | Control de costos, prevenir cargos internacionales en dispositivos corporativos |
| `DISALLOW_CELLULAR_2G` | Bloquea conexiones 2G completamente | Android 12+, seguridad contra ataques SS7 y Stingray (interceptación de comunicaciones) |
| `DISALLOW_SIM_GLOBALLY` | Deshabilita completamente funcionalidad de SIM | Android 13+, uso extremo para dispositivos WiFi-only |
| `DISALLOW_SMS` | Bloquea envío/recepción de SMS | Prevenir phishing vía SMS, costos no autorizados. No necesariamente afecta MMS |
| `DISALLOW_OUTGOING_CALLS` | Usuario no puede hacer llamadas salientes | Dispositivos de solo recepción o datos. Llamadas de emergencia pueden seguir funcionando |

---

## 🔌 CONECTIVIDAD - OTRAS RADIOS

| Key | Qué Hace | Uso |
|-----|----------|-----|
| `DISALLOW_BLUETOOTH` | Deshabilita Bluetooth completamente | Prevenir exfiltración de datos. **PROBLEMA**: También deshabilita periféricos Bluetooth |
| `DISALLOW_CONFIG_BLUETOOTH` | Usuario no puede emparejar nuevos dispositivos Bluetooth | Más flexible, BT funciona pero solo con dispositivos pre-emparejados por admin |
| `DISALLOW_BLUETOOTH_SHARING` | Bloquea transferencia de archivos por Bluetooth | Prevenir fuga de datos, pero permite otros usos de BT (audio, teclados) |
| `DISALLOW_NEAR_FIELD_COMMUNICATION_RADIO` | Deshabilita radio NFC completamente | Prevenir pagos/lecturas NFC no autorizadas en ambientes seguros |
| `DISALLOW_CHANGE_NEAR_FIELD_COMMUNICATION_RADIO` | Usuario no puede activar/desactivar NFC | Android 14+, admin decide estado de NFC |
| `DISALLOW_OUTGOING_BEAM` | Bloquea Android Beam (compartir por NFC) | Android Beam deprecado en Android 10+, más relevante en versiones antiguas |
| `DISALLOW_ULTRA_WIDEBAND_RADIO` | Deshabilita radio UWB (Ultra Wideband) | Android 12+, prevenir tracking de ubicación de alta precisión |
| `DISALLOW_THREAD_NETWORK` | Bloquea redes Thread (protocolo IoT) | Android 14+, evitar integración con dispositivos smart home Matter/Thread |
| `DISALLOW_AIRPLANE_MODE` | Usuario no puede activar modo avión | Mantener dispositivo siempre conectado, útil para tracking y localización constante |

---

## 🔊 AUDIO Y MULTIMEDIA

| Key | Qué Hace | Uso |
|-----|----------|-----|
| `DISALLOW_ADJUST_VOLUME` | Botones de volumen no funcionan | Kioscos con volumen fijo. Puede ser frustrante para usuarios |
| `DISALLOW_UNMUTE_MICROPHONE` | Micrófono siempre silenciado | Prevenir grabaciones. No todas las apps lo respetan consistentemente |
| `DISALLOW_RECORD_AUDIO` | Aplicaciones no pueden grabar audio | Más estricto que UNMUTE_MICROPHONE, bloqueo a nivel de sistema para ambientes sensibles |
| `DISALLOW_UNMUTE_DEVICE` | Dispositivo siempre en modo silencio | Android 9+, ambientes donde no se permite ningún sonido |
| `DISALLOW_CAMERA` | Deshabilita todas las cámaras del dispositivo | Alternativa a `setCameraDisabled()`, ninguna app puede usar cámara |

---

## 🖼️ PERSONALIZACIÓN

| Key | Qué Hace | Uso |
|-----|----------|-----|
| `DISALLOW_SET_WALLPAPER` | Usuario no puede cambiar fondo de pantalla | Mantener branding corporativo, control visual del dispositivo |
| `DISALLOW_WALLPAPER` | Fuerza fondo negro/predeterminado sin wallpaper | Más restrictivo que anterior, elimina wallpaper completamente |
| `DISALLOW_SET_USER_ICON` | Usuario no puede cambiar su foto de perfil | Dispositivos compartidos o corporativos, menor impacto funcional |
| `DISALLOW_AMBIENT_DISPLAY` | Deshabilita "Always On Display" (pantalla ambiental) | Ahorro de batería, no mostrar notificaciones en pantalla bloqueada por seguridad |

---

## ⚙️ CONFIGURACIÓN DEL SISTEMA

| Key | Qué Hace | Uso |
|-----|----------|-----|
| `DISALLOW_CONFIG_CREDENTIALS` | Usuario no puede instalar certificados de seguridad | **SEGURIDAD CRÍTICA**: Prevenir ataques MITM con certificados falsos, solo admin instala certificados |
| `DISALLOW_CONFIG_DATE_TIME` | Fecha/hora automáticas, usuario no puede cambiar manualmente | Evitar manipulación de logs basados en tiempo, prevenir bypass de restricciones temporales |
| `DISALLOW_CONFIG_LOCALE` | Usuario no puede cambiar idioma/región del sistema | Dispositivos de uso específico, mantener idioma corporativo consistente |
| `DISALLOW_CONFIG_LOCATION` | Configuración de ubicación bloqueada | Mantener GPS siempre encendido para tracking, dispositivos de flota |
| `DISALLOW_CONFIG_SCREEN_TIMEOUT` | Tiempo de espera de pantalla fijo | Kiosko con pantalla siempre encendida o timeout corto específico |
| `DISALLOW_CONFIG_BRIGHTNESS` | Brillo de pantalla fijo, no ajustable | Kioscos, señalización digital. **PROBLEMA**: Puede ser incómodo en diferentes condiciones de luz |
| `DISALLOW_CONFIG_DEFAULT_APPS` | Usuario no puede cambiar apps predeterminadas | Forzar navegador, launcher específico para experiencia consistente |
| `DISALLOW_CONFIG_PRIVATE_DNS` | Bloquea configuración DNS over TLS | Android 9+, prevenir bypass de filtros DNS corporativos |
| `DISALLOW_CONFIG_TETHERING` | Usuario no puede configurar tethering (compartir internet) | Incluye USB, WiFi y Bluetooth tethering |
| `DISALLOW_CONFIG_VPN` | Usuario no puede añadir/modificar configuraciones VPN | Solo VPN corporativo permitido, admin gestiona VPN programáticamente |
| `DISALLOW_CONFIG_CELL_BROADCASTS` | Bloquea configuración de alertas de emergencia celular | Raro, pocas razones legítimas. **CUIDADO**: Puede afectar recepción de alertas de emergencia |

---

## 🔄 GESTIÓN DE USUARIOS Y PERFILES

| Key | Qué Hace | Uso |
|-----|----------|-----|
| `DISALLOW_ADD_USER` | No permite crear usuarios adicionales en el dispositivo | Dispositivo de usuario único, solo cuenta corporativa |
| `DISALLOW_REMOVE_USER` | No permite eliminar usuarios existentes | Complemento de ADD_USER, mantener estructura de usuarios protegida |
| `DISALLOW_USER_SWITCH` | Usuario no puede cambiar entre cuentas/usuarios | Kiosko bloqueado en sesión específica, dispositivo dedicado |
| `DISALLOW_ADD_MANAGED_PROFILE` | No permite crear perfil de trabajo (Work Profile) | Device Owner puro sin Work Profile, prevenir separación trabajo/personal |
| `DISALLOW_ADD_CLONE_PROFILE` | No permite crear perfil clonado de apps | Android 13+, algunos fabricantes permiten clonar apps, prevenir duplicación |
| `DISALLOW_ADD_PRIVATE_PROFILE` | No permite crear perfil privado | Android 15+, nuevo tipo de perfil con apps ocultas |
| `DISALLOW_REMOVE_MANAGED_PROFILE` | Work Profile no se puede eliminar | Solo aplicable a Work Profile, no para Device Owner, proteger perfil corporativo |
| `DISALLOW_MODIFY_ACCOUNTS` | No permite añadir/eliminar cuentas (Google, etc.) | Cuentas preconfiguradas únicamente, solo cuenta corporativa activa |

---

## 💾 ALMACENAMIENTO Y TRANSFERENCIAS

| Key | Qué Hace | Uso |
|-----|----------|-----|
| `DISALLOW_USB_FILE_TRANSFER` | Bloquea MTP/PTP por USB (transferencia de archivos) | Prevenir exfiltración de datos. Permite ADB y carga si están habilitados |
| `DISALLOW_MOUNT_PHYSICAL_MEDIA` | SD cards y USB drives no se montan automáticamente | Prevenir malware desde medios externos, dispositivos seguros |
| `DISALLOW_PRINTING` | Deshabilita servicios de impresión del sistema | Android 9+, DLP (Data Loss Prevention), prevenir impresión de documentos sensibles |

---

## 🔄 COMPARTIR Y COPIAR

| Key | Qué Hace | Uso |
|-----|----------|-----|
| `DISALLOW_CROSS_PROFILE_COPY_PASTE` | No permite copiar/pegar entre perfil personal y trabajo | Solo Work Profile, DLP para prevenir fuga de datos entre perfiles |
| `DISALLOW_SHARE_INTO_MANAGED_PROFILE` | No permite compartir archivos hacia perfil de trabajo | Solo Work Profile, DLP para controlar entrada de datos al perfil corporativo |
| `ALLOW_PARENT_PROFILE_APP_LINKING` | **[ALLOW, no DISALLOW]** Permite que apps del perfil hijo abran enlaces en perfil padre | Work Profile, abrir links corporativos en navegador personal |

---

## 🛡️ SEGURIDAD DEL SISTEMA

| Key | Qué Hace | Uso |
|-----|----------|-----|
| `DISALLOW_FACTORY_RESET` | **CRÍTICO**: Usuario no puede hacer factory reset desde Settings | Tu Device Owner persiste. **NOTA**: Reset desde bootloader puede aún funcionar en algunos dispositivos |
| `DISALLOW_NETWORK_RESET` | Bloquea "Reset Wi-Fi, mobile & Bluetooth" en Settings | Mantener configuración de red, evitar que usuario borre VPN/WiFi corporativo |
| `DISALLOW_SYSTEM_ERROR_DIALOGS` | Suprime diálogos de error del sistema (crashs de apps) | Kiosko: usuario no ve "App has stopped", UX más limpia pero menos feedback |
| `DISALLOW_FUN` | Bloquea easter eggs de Android (juegos ocultos) | Impide acceder al juego oculto en "About phone", más simbólico que funcional |

---

## 🎯 PRIVACIDAD Y DATOS

| Key | Qué Hace | Uso |
|-----|----------|-----|
| `DISALLOW_SHARE_LOCATION` | Aplicaciones no pueden acceder a ubicación GPS | Bloqueo total de ubicación. **CUIDADO**: Rompe apps de mapas, delivery, navegación |
| `DISALLOW_AUTOFILL` | Deshabilita autocompletar (contraseñas, formularios) | Android 9+, prevenir gestores de contraseñas, mayor seguridad |
| `DISALLOW_CONTENT_CAPTURE` | Deshabilita Content Capture (screenshots automáticos del sistema) | Android 10+, prevenir que sistema analice contenido sensible para sugerencias |
| `DISALLOW_CONTENT_SUGGESTIONS` | Deshabilita sugerencias basadas en contenido de pantalla | Android 10+, no análisis de contenido para recomendaciones |
| `DISALLOW_ASSIST_CONTENT` | Google Assistant no puede ver contenido en pantalla | Bloquea contexto para asistente, datos confidenciales no compartidos con Assistant |

---

## 📷 TOGGLES DE HARDWARE (ANDROID 12+)

| Key | Qué Hace | Uso |
|-----|----------|-----|
| `DISALLOW_CAMERA_TOGGLE` | Usuario no puede activar/desactivar cámara desde quick settings | Android 12+, admin decide si cámara está disponible mediante toggles de privacidad |
| `DISALLOW_MICROPHONE_TOGGLE` | Usuario no puede toggle micrófono desde quick settings | Android 12+, asegurar que micrófono esté siempre available para llamadas corporativas |

---

## 🪟 INTERFAZ Y VENTANAS

| Key | Qué Hace | Uso |
|-----|----------|-----|
| `DISALLOW_CREATE_WINDOWS` | Apps no pueden crear ventanas overlay sobre otras apps | Prevenir overlays maliciosos, evitar phishing con ventanas falsas tipo toast |

---

## ✅ VERIFICACIÓN DE APPS

| Key | Qué Hace | Uso |
|-----|----------|-----|
| `ENSURE_VERIFY_APPS` | **[ENSURE, no DISALLOW]** Fuerza Play Protect siempre activo | Verificación de apps habilitada obligatoriamente, seguridad adicional |

---

## ⏳ ESTADO DEL SISTEMA

| Key | Qué Hace | Uso |
|-----|----------|-----|
| `KEY_RESTRICTIONS_PENDING` | **[SOLO LECTURA]** Indica que hay restricciones pendientes de aplicar | Sistema interno, no establecer manualmente. Consultar con `getUserRestrictions()` |

---

## 📋 EJEMPLOS DE CONFIGURACIÓN POR ESCENARIO

### 🏢 CORPORATIVO ESTÁNDAR
```kotlin
dpm.addUserRestriction(admin, UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES)
dpm.addUserRestriction(admin, UserManager.DISALLOW_FACTORY_RESET)
dpm.addUserRestriction(admin, UserManager.DISALLOW_DEBUGGING_FEATURES)
dpm.addUserRestriction(admin, UserManager.DISALLOW_USB_FILE_TRANSFER)
dpm.addUserRestriction(admin, UserManager.DISALLOW_CONFIG_VPN)
dpm.addUserRestriction(admin, UserManager.ENSURE_VERIFY_APPS)
```

### 🖥️ KIOSKO EXTREMO
```kotlin
dpm.addUserRestriction(admin, UserManager.DISALLOW_FACTORY_RESET)
dpm.addUserRestriction(admin, UserManager.DISALLOW_SAFE_BOOT)
dpm.addUserRestriction(admin, UserManager.DISALLOW_ADD_USER)
dpm.addUserRestriction(admin, UserManager.DISALLOW_INSTALL_APPS)
dpm.addUserRestriction(admin, UserManager.DISALLOW_CONFIG_WIFI)
dpm.addUserRestriction(admin, UserManager.DISALLOW_ADJUST_VOLUME)
dpm.addUserRestriction(admin, UserManager.DISALLOW_SYSTEM_ERROR_DIALOGS)
dpm.addUserRestriction(admin, UserManager.DISALLOW_CREATE_WINDOWS)
```

### 🔐 MÁXIMA SEGURIDAD
```kotlin
dpm.addUserRestriction(admin, UserManager.DISALLOW_DEBUGGING_FEATURES)
dpm.addUserRestriction(admin, UserManager.DISALLOW_OEM_UNLOCK)
dpm.addUserRestriction(admin, UserManager.DISALLOW_SAFE_BOOT)
dpm.addUserRestriction(admin, UserManager.DISALLOW_FACTORY_RESET)
dpm.addUserRestriction(admin, UserManager.DISALLOW_USB_FILE_TRANSFER)
dpm.addUserRestriction(admin, UserManager.DISALLOW_MOUNT_PHYSICAL_MEDIA)
dpm.addUserRestriction(admin, UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES_GLOBALLY)
dpm.addUserRestriction(admin, UserManager.DISALLOW_CONFIG_CREDENTIALS)
dpm.addUserRestriction(admin, UserManager.DISALLOW_BLUETOOTH_SHARING)
dpm.addUserRestriction(admin, UserManager.DISALLOW_PRINTING)
```

### 🎮 DISPOSITIVO INFANTIL
```kotlin
dpm.addUserRestriction(admin, UserManager.DISALLOW_MODIFY_ACCOUNTS)
dpm.addUserRestriction(admin, UserManager.DISALLOW_INSTALL_APPS)
dpm.addUserRestriction(admin, UserManager.DISALLOW_OUTGOING_CALLS)
dpm.addUserRestriction(admin, UserManager.DISALLOW_SMS)
dpm.addUserRestriction(admin, UserManager.DISALLOW_SHARE_LOCATION)
dpm.addUserRestriction(admin, UserManager.DISALLOW_FACTORY_RESET)
```

### 📦 DISPOSITIVO DE ALMACÉN/LOGÍSTICA
```kotlin
dpm.addUserRestriction(admin, UserManager.DISALLOW_ADD_USER)
dpm.addUserRestriction(admin, UserManager.DISALLOW_FACTORY_RESET)
dpm.addUserRestriction(admin, UserManager.DISALLOW_CONFIG_WIFI)
dpm.addUserRestriction(admin, UserManager.DISALLOW_BLUETOOTH)
dpm.addUserRestriction(admin, UserManager.DISALLOW_ADJUST_VOLUME)
dpm.addUserRestriction(admin, UserManager.DISALLOW_CAMERA)
```

---

## ⚠️ NOTAS IMPORTANTES

1. **Compatibilidad**: Muchas restricciones dependen de la versión de Android. Siempre verificar `Build.VERSION.SDK_INT`

2. **Testing**: NUNCA pruebes en tu dispositivo personal principal. Usa dispositivos de desarrollo

3. **Reversibilidad**: Para remover Device Owner necesitas factory reset o comando ADB:
   ```bash
   adb shell dpm remove-active-admin com.tu.paquete/.TuDeviceAdminReceiver
   ```

4. **Combinaciones**: Algunas restricciones se complementan, otras se solapan. Planifica tu estrategia

5. **Fabricantes**: Samsung, Huawei y otros OEM pueden tener comportamientos diferentes o APIs propias

6. **Verificación**: Usa `dpm.getUserRestrictions()` para confirmar restricciones aplicadas

7. **Claves especiales**:
    - `ALLOW_*` son permisos, no restricciones
    - `ENSURE_*` fuerzan activación de features
    - `KEY_RESTRICTIONS_PENDING` es solo lectura