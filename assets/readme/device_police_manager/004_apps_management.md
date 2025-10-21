# Documentación DevicePolicyManager - Categoría 4: Gestión de Aplicaciones

## 📋 Visión General

La categoría de **Gestión de Aplicaciones** es una de las más extensas de DevicePolicyManager, con aproximadamente **45 métodos** que proporcionan control completo sobre el ciclo de vida, permisos, visibilidad y comportamiento de las aplicaciones en dispositivos administrados.

## 🎯 Propósito

Estos métodos permiten:
- **Ocultar/mostrar** aplicaciones sin desinstalarlas
- **Suspender** aplicaciones temporalmente
- **Bloquear desinstalación** de apps críticas
- **Habilitar apps del sistema** deshabilitadas por defecto
- **Gestionar permisos** de forma programática
- **Configurar apps predeterminadas** persistentes
- **Establecer restricciones** específicas por aplicación
- **Delegar capacidades** a aplicaciones específicas
- **Controlar instalación** y actualización de paquetes
- **Gestionar datos móviles** por aplicación

---

## 👁️ Métodos de Visibilidad de Aplicaciones

### `setApplicationHidden(ComponentName admin, String packageName, boolean hidden)`

**Propósito**: Oculta o muestra una aplicación sin desinstalarla. La app permanece instalada pero invisible para el usuario en el launcher y en Settings.

**Casos de Uso**:
- **Apps pre-instaladas no deseadas**: Ocultar bloatware del fabricante sin root
- **Apps corporativas sensibles**: Ocultar apps de administración para usuarios finales
- **Control parental**: Ocultar apps inapropiadas en dispositivos infantiles
- **Kioscos**: Ocultar todas las apps excepto las autorizadas
- **Transición gradual**: Ocultar apps antiguas mientras se migra a nuevas versiones

**Características**:
- La app sigue instalada y consumiendo espacio
- Los datos de la app se mantienen intactos
- Los servicios en background pueden continuar ejecutándose (depende de la implementación)
- No aparece en launcher, drawer de apps, ni en Settings > Apps
- Se puede revertir fácilmente sin pérdida de datos

**Ejemplo de Uso**:
```kotlin
// Ocultar apps de Google pre-instaladas no necesarias
dpm.setApplicationHidden(admin, "com.google.android.music", true)
dpm.setApplicationHidden(admin, "com.google.android.videos", true)
dpm.setApplicationHidden(admin, "com.android.chrome", true)

// Ocultar apps de fabricante (bloatware)
dpm.setApplicationHidden(admin, "com.samsung.android.game.gamehome", true)
dpm.setApplicationHidden(admin, "com.facebook.katana", true) // Facebook pre-instalado

// Mostrar app corporativa solo cuando sea necesario
fun mostrarAppTemporalmente(packageName: String) {
    dpm.setApplicationHidden(admin, packageName, false)
    // Usuario usa la app
    // Después de X tiempo o evento, ocultar nuevamente
}
```

**Ventajas sobre desinstalar**:
- No se pierden datos
- Reversible instantáneamente
- No requiere reinstalación
- Útil para apps del sistema que no se pueden desinstalar

**⚠️ Limitaciones**:
- Apps del sistema crítico (Settings, SystemUI) no se pueden ocultar
- Algunos servicios pueden seguir ejecutándose en background
- No libera espacio de almacenamiento

---

### `isApplicationHidden(ComponentName admin, String packageName)`

**Propósito**: Verifica si una aplicación específica está actualmente oculta.

**Retorna**:
- `true`: La app está oculta
- `false`: La app es visible

**Casos de Uso**:
- **Auditoría**: Verificar qué apps están ocultas actualmente
- **Validación**: Confirmar que el ocultamiento se aplicó correctamente
- **Sincronización**: Mantener estado consistente con servidor MDM
- **UI dinámica**: Mostrar/ocultar botones según estado de apps

```kotlin
fun verificarEstadoVisibilidad(packageName: String): String {
    val oculta = dpm.isApplicationHidden(admin, packageName)
    return if (oculta) {
        "📦 $packageName: OCULTA"
    } else {
        "👁️ $packageName: VISIBLE"
    }
}

// Generar reporte de apps ocultas
fun reportarAppsOcultas(listaApps: List<String>): List<String> {
    return listaApps.filter { dpm.isApplicationHidden(admin, it) }
}
```

---

## ⏸️ Métodos de Suspensión de Aplicaciones

### `setPackagesSuspended(ComponentName admin, String[] packageNames, boolean suspended)`

**Propósito**: Suspende o reactiva aplicaciones. Una app suspendida no puede ejecutarse, sus notificaciones se bloquean, y su icono aparece atenuado en el launcher.

**Diferencia con `setApplicationHidden()`**:

| Característica | `setApplicationHidden()` | `setPackagesSuspended()` |
|----------------|-------------------------|-------------------------|
| Visibilidad en launcher | ❌ Invisible | ✅ Visible pero atenuada |
| Usuario puede intentar abrir | ❌ No (no la ve) | ⚠️ Sí (pero ve mensaje) |
| Servicios en background | Pueden ejecutarse | ❌ Completamente detenidos |
| Notificaciones | Pueden aparecer | ❌ Bloqueadas |
| Mensaje al usuario | Ninguno | ✅ Personalizable |
| Intención | Ocultar existencia | Bloquear temporalmente |

**Casos de Uso**:
- **Horarios de trabajo**: Suspender apps de entretenimiento en horas laborales
- **Control de datos**: Suspender apps que consumen muchos datos cuando el límite está cerca
- **Respuesta a incidentes**: Suspender apps sospechosas mientras se investiga
- **Cumplimiento de políticas**: Suspender apps no autorizadas hasta revisión
- **Gestión de distracciones**: Suspender redes sociales durante periodos de concentración

**Ejemplo de Uso - Horarios de Trabajo**:
```kotlin
// Suspender apps de entretenimiento durante horario laboral (9 AM - 6 PM)
fun gestionarAppsPorHorario() {
    val cal = Calendar.getInstance()
    val hora = cal.get(Calendar.HOUR_OF_DAY)
    
    val appsEntretenimiento = arrayOf(
        "com.facebook.katana",
        "com.instagram.android",
        "com.twitter.android",
        "com.netflix.mediaclient",
        "com.spotify.music",
        "com.whatsapp"
    )
    
    val suspender = hora in 9..17 // Entre 9 AM y 6 PM
    
    val resultado = dpm.setPackagesSuspended(admin, appsEntretenimiento, suspender)
    
    resultado.forEachIndexed { index, packageName ->
        if (packageName != null) {
            Log.e("AppManager", "Fallo al suspender: $packageName")
        }
    }
}
```

**Ejemplo de Uso - Control de Datos**:
```kotlin
// Suspender apps que consumen muchos datos cuando se alcanza 80% del límite
fun suspenderAppsAltoConsumo(porcentajeUsoDatos: Int) {
    if (porcentajeUsoDatos >= 80) {
        val appsAltoConsumo = arrayOf(
            "com.netflix.mediaclient",
            "com.google.android.youtube",
            "com.spotify.music",
            "com.instagram.android"
        )
        
        dpm.setPackagesSuspended(admin, appsAltoConsumo, true)
        
        // Notificar al usuario
        mostrarNotificacion(
            "Apps suspendidas",
            "Has alcanzado el 80% de tu límite de datos. Apps de streaming suspendidas."
        )
    }
}
```

**Retorna**: Array de `String` con:
- `null` para cada app suspendida/reactivada exitosamente
- El `packageName` para apps que fallaron

**Mensaje al Usuario**:
Cuando el usuario intenta abrir una app suspendida, Android muestra un diálogo con el mensaje configurado mediante `setPackagesSuspendedAsUser()` (API 29+) o un mensaje genérico.

---

### `isPackageSuspended(ComponentName admin, String packageName)`

**Propósito**: Verifica si un paquete específico está actualmente suspendido.

**Retorna**:
- `true`: La app está suspendida
- `false`: La app está activa

**Casos de Uso**:
- Verificar estado antes de intentar suspender
- Generar reportes de apps suspendidas
- Validar políticas aplicadas
- UI condicional según estado de apps

```kotlin
fun verificarEstadoApp(packageName: String): EstadoApp {
    val suspendida = dpm.isPackageSuspended(admin, packageName)
    val oculta = dpm.isApplicationHidden(admin, packageName)
    
    return when {
        oculta -> EstadoApp.OCULTA
        suspendida -> EstadoApp.SUSPENDIDA
        else -> EstadoApp.ACTIVA
    }
}

enum class EstadoApp {
    ACTIVA,
    SUSPENDIDA,
    OCULTA
}
```

---

## 🚫 Métodos de Bloqueo de Desinstalación

### `setUninstallBlocked(ComponentName admin, String packageName, boolean uninstallBlocked)`

**Propósito**: Bloquea o desbloquea la desinstalación de una aplicación específica. El usuario no puede desinstalar apps bloqueadas ni desde Settings ni desde el launcher.

**Casos de Uso**:
- **Apps corporativas críticas**: Proteger apps de trabajo esenciales
- **Apps MDM**: Prevenir desinstalación del agente de gestión
- **Apps de seguridad**: Mantener antivirus y apps de cumplimiento
- **Kioscos**: Asegurar que apps necesarias permanezcan instaladas
- **Control parental**: Prevenir que niños desinstalen apps de monitoreo

**Ejemplo de Uso**:
```kotlin
// Bloquear desinstalación de apps críticas corporativas
val appsCriticas = listOf(
    "com.empresa.mdm",              // Agente MDM
    "com.empresa.vpn",              // Cliente VPN corporativa
    "com.microsoft.office.outlook", // Email corporativo
    "com.empresa.autenticador",     // Autenticación 2FA
    "com.empresa.intranet"          // Portal corporativo
)

appsCriticas.forEach { packageName ->
    dpm.setUninstallBlocked(admin, packageName, true)
    Log.i("AppProtection", "Desinstalación bloqueada: $packageName")
}

// Bloquear apps del sistema importantes
dpm.setUninstallBlocked(admin, "com.android.vending", true) // Play Store
dpm.setUninstallBlocked(admin, "com.google.android.gms", true) // Google Play Services
```

**Comportamiento**:
- El botón "Desinstalar" aparece deshabilitado en Settings
- Al intentar desinstalar desde launcher, muestra mensaje de error
- Las actualizaciones de la app SÍ están permitidas
- Se puede desbloquear y desinstalar programáticamente

**⚠️ Importante**:
- No confundir con `DISALLOW_UNINSTALL_APPS` (UserManager restriction que bloquea TODAS las desinstalaciones)
- `setUninstallBlocked()` es granular por aplicación
- Útil cuando quieres permitir desinstalar apps en general, pero proteger algunas específicas

---

### `isUninstallBlocked(ComponentName admin, String packageName)`

**Propósito**: Verifica si la desinstalación de un paquete está bloqueada.

**Retorna**:
- `true`: Desinstalación bloqueada
- `false`: Se puede desinstalar normalmente

```kotlin
fun auditarProteccionApps(listaApps: List<String>): Map<String, Boolean> {
    return listaApps.associateWith { packageName ->
        dpm.isUninstallBlocked(admin, packageName)
    }
}

// Generar reporte
fun reporteAppsProtegidas(): String {
    val appsCorporativas = listOf(
        "com.empresa.mdm",
        "com.empresa.vpn",
        "com.microsoft.office.outlook"
    )
    
    val protecciones = auditarProteccionApps(appsCorporativas)
    
    return buildString {
        append("📊 REPORTE DE PROTECCIÓN DE APPS\n")
        append("═══════════════════════════════\n\n")
        protecciones.forEach { (pkg, bloqueada) ->
            val estado = if (bloqueada) "🔒 PROTEGIDA" else "⚠️ VULNERABLE"
            append("$estado - $pkg\n")
        }
    }
}
```

---

## ✅ Métodos de Habilitación de Apps del Sistema

### `enableSystemApp(ComponentName admin, String packageName)`

**Propósito**: Habilita una aplicación del sistema que está deshabilitada por defecto o que fue deshabilitada previamente.

**Contexto**:
Muchos dispositivos Android vienen con apps del sistema deshabilitadas para ahorrar recursos o simplificar la experiencia. Como Device Owner, puedes habilitarlas programáticamente.

**Casos de Uso**:
- **Habilitar apps de comunicación**: Email, Calendar, Contacts del sistema
- **Provisioning selectivo**: Activar solo las apps del sistema que el negocio necesita
- **Restaurar funcionalidad**: Re-habilitar apps deshabilitadas por error
- **Configuración por roles**: Activar apps específicas según el rol del usuario

**Ejemplo de Uso**:
```kotlin
// Habilitar apps de productividad del sistema
dpm.enableSystemApp(admin, "com.android.email")      // Email
dpm.enableSystemApp(admin, "com.android.calendar")   // Calendario
dpm.enableSystemApp(admin, "com.android.contacts")   // Contactos
dpm.enableSystemApp(admin, "com.android.calculator2") // Calculadora

// Habilitar apps de Google
dpm.enableSystemApp(admin, "com.google.android.apps.docs")  // Google Drive
dpm.enableSystemApp(admin, "com.google.android.gm")         // Gmail
dpm.enableSystemApp(admin, "com.google.android.calendar")   // Google Calendar
```

**Diferencia con `installExistingPackage()`**:
- `enableSystemApp()`: Para apps del sistema YA instaladas pero deshabilitadas
- `installExistingPackage()`: Para apps que fueron desinstaladas pero están en imagen del sistema

---

### `enableSystemApp(ComponentName admin, Intent intent)`

**Propósito**: Habilita aplicaciones del sistema que pueden manejar un Intent específico.

**Casos de Uso**:
- **Habilitar por funcionalidad**: Activar todas las apps que pueden abrir PDFs
- **Provisioning basado en capacidades**: Habilitar apps según lo que necesitan hacer
- **Configuración dinámica**: Activar apps según categorías de uso

**Ejemplo de Uso**:
```kotlin
// Habilitar todas las apps del sistema que pueden manejar PDFs
val pdfIntent = Intent(Intent.ACTION_VIEW).apply {
    setDataAndType(Uri.parse("file:///dummy.pdf"), "application/pdf")
}
val pdfAppsCount = dpm.enableSystemApp(admin, pdfIntent)
Log.i("SystemApps", "Habilitadas $pdfAppsCount apps para PDFs")

// Habilitar navegadores del sistema
val browserIntent = Intent(Intent.ACTION_VIEW).apply {
    addCategory(Intent.CATEGORY_BROWSABLE)
    data = Uri.parse("https://www.ejemplo.com")
}
dpm.enableSystemApp(admin, browserIntent)

// Habilitar apps de cámara
val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
dpm.enableSystemApp(admin, cameraIntent)
```

**Retorna**: Número de aplicaciones habilitadas.

---

### `installExistingPackage(ComponentName admin, String packageName)`

**Propósito**: Instala (o re-instala) un paquete que ya existe en la imagen del sistema pero fue desinstalado por el usuario.

**Casos de Uso**:
- **Restaurar apps del sistema**: Reinstalar apps que el usuario desinstalo
- **Provisioning**: Asegurar que apps necesarias estén instaladas
- **Recuperación**: Restaurar apps después de limpieza accidental

**Diferencia con instalación normal**:
- No descarga nada desde internet
- Solo funciona con apps que están en la partición /system
- Muy rápido (no hay descarga ni instalación real)
- Útil para apps pre-instaladas por el fabricante

```kotlin
// Reinstalar apps de Google que el usuario pudo haber desinstalado
try {
    dpm.installExistingPackage(admin, "com.google.android.youtube")
    Log.i("AppRestore", "YouTube reinstalado exitosamente")
} catch (e: PackageManager.NameNotFoundException) {
    Log.e("AppRestore", "YouTube no está en la imagen del sistema")
}

// Asegurar que apps críticas estén instaladas
val appsCriticasDelSistema = listOf(
    "com.android.vending",      // Play Store
    "com.google.android.gms",   // Play Services
    "com.android.chrome"        // Chrome
)

appsCriticasDelSistema.forEach { packageName ->
    try {
        dpm.installExistingPackage(admin, packageName)
    } catch (e: Exception) {
        Log.w("AppRestore", "No se pudo reinstalar $packageName: ${e.message}")
    }
}
```

**⚠️ Limitación**: Solo funciona si el APK está en la partición del sistema. No puede instalar apps arbitrarias.

---

## 🔐 Métodos de Gestión de Permisos

### `setPermissionPolicy(ComponentName admin, int policy)`

**Propósito**: Define la política global de cómo se manejan los permisos de aplicaciones en el dispositivo.

**Políticas Disponibles**:

| Política | Valor | Comportamiento |
|----------|-------|----------------|
| `PERMISSION_POLICY_PROMPT` | 0 | Usuario decide (comportamiento estándar de Android) |
| `PERMISSION_POLICY_AUTO_GRANT` | 1 | Conceder automáticamente todos los permisos |
| `PERMISSION_POLICY_AUTO_DENY` | 2 | Denegar automáticamente todos los permisos |

**Casos de Uso**:

**AUTO_GRANT**:
- Kioscos donde las apps necesitan permisos sin intervención
- Provisioning masivo sin interacción del usuario
- Apps corporativas que requieren permisos específicos

**AUTO_DENY**:
- Máxima seguridad: negar todo por defecto
- Control granular: usar `setPermissionGrantState()` para permitir solo permisos específicos
- Prevenir que apps soliciten permisos sin autorización

**PROMPT** (Default):
- Balance entre seguridad y usabilidad
- Usuario tiene control final
- Experiencia estándar de Android

```kotlin
// Configuración de alta seguridad: Denegar todo por defecto
dpm.setPermissionPolicy(admin, DevicePolicyManager.PERMISSION_POLICY_AUTO_DENY)
// Luego otorgar permisos específicos con setPermissionGrantState()

// Configuración de kiosko: Conceder todo automáticamente
dpm.setPermissionPolicy(admin, DevicePolicyManager.PERMISSION_POLICY_AUTO_GRANT)

// Volver a comportamiento estándar
dpm.setPermissionPolicy(admin, DevicePolicyManager.PERMISSION_POLICY_PROMPT)
```

**⚠️ Recomendación**:
La mejor práctica es usar `PERMISSION_POLICY_PROMPT` o `AUTO_DENY` y luego gestionar permisos individualmente con `setPermissionGrantState()` para tener control preciso.

---

### `getPermissionPolicy(ComponentName admin)`

**Propósito**: Obtiene la política actual de permisos.

**Retorna**:
- `PERMISSION_POLICY_PROMPT` (0)
- `PERMISSION_POLICY_AUTO_GRANT` (1)
- `PERMISSION_POLICY_AUTO_DENY` (2)

---

### `setPermissionGrantState(ComponentName admin, String packageName, String permission, int grantState)`

**Propósito**: Controla el estado de un permiso específico para una aplicación específica, sin interacción del usuario.

**Estados de Permiso**:

| Estado | Valor | Descripción |
|--------|-------|-------------|
| `PERMISSION_GRANT_STATE_DEFAULT` | 0 | Usuario decide (comportamiento normal) |
| `PERMISSION_GRANT_STATE_GRANTED` | 1 | Permiso concedido por admin |
| `PERMISSION_GRANT_STATE_DENIED` | 2 | Permiso denegado por admin |

**Casos de Uso**:
- **Apps corporativas**: Otorgar permisos necesarios automáticamente
- **Seguridad**: Denegar permisos peligrosos a apps no confiables
- **Provisioning**: Configurar permisos durante setup inicial
- **Compliance**: Asegurar que apps tengan solo permisos autorizados

**Ejemplo de Uso - Gestión de Permisos Corporativos**:
```kotlin
// Otorgar permisos a app corporativa de tracking
val appTracking = "com.empresa.gps_tracking"

dpm.setPermissionGrantState(admin, appTracking,
    android.Manifest.permission.ACCESS_FINE_LOCATION,
    DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED)

dpm.setPermissionGrantState(admin, appTracking,
    android.Manifest.permission.ACCESS_BACKGROUND_LOCATION,
    DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED)

// Otorgar permisos a app de escáner
val appEscaner = "com.empresa.barcode_scanner"

dpm.setPermissionGrantState(admin, appEscaner,
    android.Manifest.permission.CAMERA,
    DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED)

// Denegar permisos peligrosos a app de terceros
val appTerceros = "com.tercero.app"

dpm.setPermissionGrantState(admin, appTerceros,
    android.Manifest.permission.READ_CONTACTS,
    DevicePolicyManager.PERMISSION_GRANT_STATE_DENIED)

dpm.setPermissionGrantState(admin, appTerceros,
    android.Manifest.permission.ACCESS_FINE_LOCATION,
    DevicePolicyManager.PERMISSION_GRANT_STATE_DENIED)
```

**Ejemplo de Uso - Control de Privacidad**:
```kotlin
// Denegar acceso a micrófono para todas las apps de redes sociales
val redesSociales = listOf(
    "com.facebook.katana",
    "com.instagram.android",
    "com.twitter.android",
    "com.snapchat.android"
)

redesSociales.forEach { packageName ->
    dpm.setPermissionGrantState(admin, packageName,
        android.Manifest.permission.RECORD_AUDIO,
        DevicePolicyManager.PERMISSION_GRANT_STATE_DENIED)
    
    dpm.setPermissionGrantState(admin, packageName,
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        DevicePolicyManager.PERMISSION_GRANT_STATE_DENIED)
}
```

**Retorna**: `boolean`
- `true`: Estado establecido exitosamente
- `false`: Falló (app no instalada, permiso no existe, etc.)

---

### `getPermissionGrantState(ComponentName admin, String packageName, String permission)`

**Propósito**: Obtiene el estado actual de un permiso específico para una aplicación.

**Retorna**:
- `PERMISSION_GRANT_STATE_DEFAULT` (0): Usuario controla
- `PERMISSION_GRANT_STATE_GRANTED` (1): Concedido por admin
- `PERMISSION_GRANT_STATE_DENIED` (2): Denegado por admin

```kotlin
fun auditarPermisosApp(packageName: String): Map<String, String> {
    val permisosImportantes = listOf(
        android.Manifest.permission.CAMERA,
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.RECORD_AUDIO,
        android.Manifest.permission.READ_CONTACTS,
        android.Manifest.permission.READ_SMS
    )
    
    return permisosImportantes.associateWith { permiso ->
        when (dpm.getPermissionGrantState(admin, packageName, permiso)) {
            DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED -> "✅ CONCEDIDO"
            DevicePolicyManager.PERMISSION_GRANT_STATE_DENIED -> "❌ DENEGADO"
            else -> "⚪ USUARIO DECIDE"
        }
    }
}
```

---

## 🎯 Métodos de Apps Predeterminadas

### `addPersistentPreferredActivity(ComponentName admin, IntentFilter filter, ComponentName activity)`

**Propósito**: Establece una aplicación como handler predeterminado **persistente** para ciertos Intents. A diferencia de las preferencias normales del usuario, estas NO pueden ser cambiadas por el usuario.

**Casos de Uso**:
- **Navegador corporativo**: Forzar que todos los links se abran en navegador específico
- **Cliente de email obligatorio**: Todos los mailto: se abren en Outlook corporativo
- **Visor de documentos**: PDFs siempre se abren en app corporativa aprobada
- **Kioscos**: Asegurar que ciertos tipos de contenido usen apps específicas

**Ejemplo de Uso - Navegador Corporativo**:
```kotlin
// Forzar Chrome como navegador predeterminado para TODOS los links
val filter = IntentFilter().apply {
    addAction(Intent.ACTION_VIEW)
    addCategory(Intent.CATEGORY_DEFAULT)
    addCategory(Intent.CATEGORY_BROWSABLE)
    addDataScheme("http")
    addDataScheme("https")
}

val chromeComponent = ComponentName(
    "com.android.chrome",
    "com.google.android.apps.chrome.Main"
)

dpm.addPersistentPreferredActivity(admin, filter, chromeComponent)
```

**Ejemplo de Uso - Email Corporativo**:
```kotlin
// Forzar Outlook para todos los enlaces mailto:
val mailtoFilter = IntentFilter().apply {
    addAction(Intent.ACTION_VIEW)
    addAction(Intent.ACTION_SENDTO)
    addCategory(Intent.CATEGORY_DEFAULT)
    addDataScheme("mailto")
}

val outlookComponent = ComponentName(
    "com.microsoft.office.outlook",
    "com.microsoft.office.outlook.MainActivity"
)

dpm.addPersistentPreferredActivity(admin, mailtoFilter, outlookComponent)
```

**Ejemplo de Uso - Visor de PDFs**:
```kotlin
// Forzar Adobe Acrobat para PDFs
val pdfFilter = IntentFilter().apply {
    addAction(Intent.ACTION_VIEW)
    addCategory(Intent.CATEGORY_DEFAULT)
    addDataScheme("file")
    addDataScheme("content")
    addDataMimeType("application/pdf")
}

val adobeComponent = ComponentName(
    "com.adobe.reader",
    "com.adobe.reader.AdobeReader"
)

dpm.addPersistentPreferredActivity(admin, pdfFilter, adobeComponent)
```

**⚠️ Importante**:
- Usuario NO puede cambiar esta preferencia desde Settings
- Sobrescribe cualquier preferencia del usuario
- Se mantiene incluso después de reiniciar
- Solo se puede quitar con `clearPackagePersistentPreferredActivities()`

---

### `clearPackagePersistentPreferredActivities(ComponentName admin, String packageName)`

**Propósito**: Elimina todas las actividades predeterminadas persistentes asociadas a un paquete específico.

**Casos de Uso**:
- Cambiar app predeterminada a otra
- Remover forzado de apps predeterminadas
- Restaurar comportamiento normal donde usuario elige
- Limpiar configuración anterior

```kotlin
// Remover Chrome como navegador forzado
dpm.clearPackagePersistentPreferredActivities(admin, "com.android.chrome")

// Ahora el usuario puede elegir su navegador preferido
```

---

## 📦 Métodos de Restricciones por Aplicación

### `setApplicationRestrictionsManagingPackage(ComponentName admin, String packageName)`

**Propósito**: Designa una aplicación específica que puede gestionar las restricciones (configuraciones) de otras aplicaciones.

**Concepto**:
Las "Application Restrictions" son configuraciones específicas de apps que el administrador puede establecer. Por ejemplo, configurar el servidor de una app corporativa, deshabilitar ciertas features, etc.

**Casos de Uso**:
- **Delegar gestión**: Permitir que una app de configuración gestione otras apps
- **Self-service**: App corporativa puede configurarse a sí misma
- **Gestión distribuida**: Diferentes administradores gestionan diferentes subconjuntos de apps

```kotlin
// Designar app de configuración corporativa
dpm.setApplicationRestrictionsManagingPackage(admin, "com.empresa.config_manager")

// Remover delegación (null)
dpm.setApplicationRestrictionsManagingPackage(admin, null)
```

---

### `getApplicationRestrictionsManagingPackage(ComponentName admin)`

**Propósito**: Obtiene el paquete actualmente designado para gestionar restricciones de aplicaciones.

**Retorna**: Package name del gestor, o `null` si no hay ninguno designado.

---

### `setApplicationRestrictions(ComponentName admin, String packageName, Bundle settings)`

**Propósito**: Establece restricciones (configuraciones) específicas para una aplicación. Estas configuraciones son definidas por cada app y pueden incluir URLs de servidor, features habilitadas, límites, etc.

**Casos de Uso**:
- **Configuración remota**: Configurar apps sin intervención del usuario
- **Provisioning**: Establecer configuración inicial durante setup
- **Políticas corporativas**: Aplicar configuraciones específicas del negocio
- **Personalización por rol**: Diferentes configuraciones según tipo de usuario

**Ejemplo de Uso - Configurar Chrome Managed**:

```kotlin
val chromeRestrictions = Bundle().apply {
    // URL de página de inicio
    putString("HomepageLocation", "https://intranet.empresa.com")
    
    // Bloquear modo incógnito
    putBoolean("IncognitoModeAvailability", false)
    
    // URLs bloqueadas
    putStringArray("URLBlocklist", arrayOf(
        "facebook.com",
        "twitter.com",
        "youtube.com"
    ))
    
    // URLs permitidas (whitelist)
    putStringArray("URLAllowlist", arrayOf(
        "empresa.com",
        "*.empresa.com",
        "docs.google.com"
    ))
    
    // Deshabilitar sincronización
    putBoolean("SyncDisabled", true)
}

dpm.setApplicationRestrictions(admin, "com.android.chrome", chromeRestrictions)
```

**Ejemplo de Uso - Configurar App Corporativa**:
```kotlin
// Configurar app de CRM corporativa
val crmRestrictions = Bundle().apply {
    putString("server_url", "https://crm.empresa.com/api")
    putString("api_key", "ENCRYPTED_API_KEY_HERE")
    putBoolean("offline_mode_enabled", true)
    putInt("sync_interval_minutes", 15)
    putBoolean("debug_logging", false)
}

dpm.setApplicationRestrictions(admin, "com.empresa.crm", crmRestrictions)
```

**Ejemplo de Uso - Configurar Gmail Managed**:
```kotlin
val gmailRestrictions = Bundle().apply {
    // Deshabilitar agregar cuentas personales
    putBoolean("DisallowNonManagedAccounts", true)
    
    // Forzar firma corporativa
    putString("Signature", """
        --
        ${nombreEmpleado}
        ${cargo}
        Empresa S.A.
        Tel: ${telefono}
    """.trimIndent())
}

dpm.setApplicationRestrictions(admin, "com.google.android.gm", gmailRestrictions)
```

**⚠️ Importante**:
- Las keys y tipos de datos son específicos de cada aplicación
- Consultar documentación de cada app para saber qué restricciones soporta
- Apps que no soporten managed configurations ignorarán el Bundle

---

### `getApplicationRestrictions(ComponentName admin, String packageName)`

**Propósito**: Obtiene las restricciones actualmente configuradas para una aplicación específica.

**Retorna**: `Bundle` con las restricciones, o Bundle vacío si no hay ninguna.

**Casos de Uso**:
- Auditar configuración actual
- Verificar que restricciones se aplicaron correctamente
- Sincronizar estado con servidor MDM
- Debugging de problemas de configuración

```kotlin
fun auditarRestriccionesApp(packageName: String): String {
    val restrictions = dpm.getApplicationRestrictions(admin, packageName)
    
    if (restrictions.isEmpty) {
        return "📦 $packageName: Sin restricciones configuradas"
    }
    
    val sb = StringBuilder("📦 RESTRICCIONES DE $packageName\n")
    sb.append("═══════════════════════════════════\n")
    
    restrictions.keySet().forEach { key ->
        val value = when (val obj = restrictions.get(key)) {
            is String -> obj
            is Boolean -> obj.toString()
            is Int -> obj.toString()
            is Array<*> -> obj.joinToString(", ")
            else -> obj?.toString() ?: "null"
        }
        sb.append("$key: $value\n")
    }
    
    return sb.toString()
}
```

---

## 🔄 Métodos de Instalación y Mantenimiento de Paquetes

### `setKeepUninstalledPackages(ComponentName admin, List<String> packageNames)`

**Propósito**: Especifica una lista de paquetes que deben reinstalarse automáticamente si el usuario los desinstala. El sistema descarga y reinstala la app automáticamente.

**Casos de Uso**:
- **Apps corporativas críticas**: Asegurar que apps importantes siempre estén instaladas
- **Compliance**: Mantener apps de cumplimiento normativo
- **Apps de seguridad**: Garantizar que antivirus o apps de gestión no se eliminen permanentemente
- **Experiencia consistente**: Mantener set de apps requerido

```kotlin
// Apps que siempre deben estar instaladas
val appsObligatorias = listOf(
    "com.empresa.mdm",
    "com.empresa.vpn",
    "com.microsoft.office.outlook",
    "com.empresa.autenticador"
)

dpm.setKeepUninstalledPackages(admin, appsObligatorias)
```

**Comportamiento**:
```
1. Usuario desinstala "com.empresa.mdm"
2. Sistema detecta la desinstalación
3. Play Store descarga automáticamente el APK
4. Sistema reinstala la app sin intervención
5. App vuelve a estar disponible
```

**⚠️ Requisitos**:
- La app debe estar disponible en Play Store (o store configurado)
- Dispositivo debe tener conexión a internet
- Play Store debe estar habilitado y funcional

---

### `getKeepUninstalledPackages(ComponentName admin)`

**Propósito**: Obtiene la lista de paquetes configurados para reinstalación automática.

**Retorna**: `List<String>` con los package names.

```kotlin
fun verificarAppsProtegidas(): List<String> {
    val appsProtegidas = dpm.getKeepUninstalledPackages(admin)
    
    Log.i("AppProtection", "Apps configuradas para reinstalación automática:")
    appsProtegidas.forEach { pkg ->
        Log.i("AppProtection", "  - $pkg")
    }
    
    return appsProtegidas
}
```

---

## 🎮 Métodos de Control de Usuario sobre Apps

### `setUserControlDisabledPackages(ComponentName admin, List<String> packages)`

**Propósito**: Impide que el usuario detenga o administre ciertas aplicaciones desde Settings. Los botones "Force Stop" y opciones de gestión aparecen deshabilitados.

**Casos de Uso**:
- **Apps de sistema críticas**: Prevenir que usuario detenga servicios vitales
- **Monitoreo continuo**: Asegurar que apps de tracking sigan ejecutándose
- **Kioscos**: Evitar manipulación de apps en modo kiosko
- **Seguridad**: Proteger apps de seguridad contra detención

```kotlin
// Proteger apps críticas de ser detenidas
val appsProtegidas = listOf(
    "com.empresa.mdm",
    "com.empresa.tracking",
    "com.empresa.seguridad"
)

dpm.setUserControlDisabledPackages(admin, appsProtegidas)
```

**Efecto en Settings**:
- Botón "Force Stop" aparece deshabilitado
- Botón "Disable" no disponible
- Opciones de notificaciones limitadas
- Usuario no puede detener la app manualmente

**Diferencia con otras protecciones**:
- `setUninstallBlocked()`: Protege contra desinstalación
- `setUserControlDisabledPackages()`: Protege contra detención/gestión
- Pueden usarse juntos para protección completa

---

### `getUserControlDisabledPackages(ComponentName admin)`

**Propósito**: Obtiene la lista de paquetes protegidos contra control del usuario.

**Retorna**: `List<String>` con los package names.

---

## 📊 Métodos de Control de Datos Móviles

### `setMeteredDataDisabledPackages(ComponentName admin, List<String> packageNames)`

**Propósito**: Bloquea el uso de datos móviles (metered data) para aplicaciones específicas. Las apps solo pueden usar WiFi.

**Casos de Uso**:
- **Control de costos**: Prevenir consumo excesivo de datos móviles
- **Apps de alto consumo**: Bloquear streaming en datos móviles
- **Políticas de uso**: Permitir apps críticas, bloquear entretenimiento en móvil
- **Optimización**: Forzar actualizaciones y descargas solo en WiFi

**Ejemplo de Uso**:
```kotlin
// Bloquear datos móviles para apps de entretenimiento
val appsEntretenimiento = listOf(
    "com.netflix.mediaclient",
    "com.spotify.music",
    "com.google.android.youtube",
    "com.instagram.android",
    "com.facebook.katana",
    "com.twitter.android"
)

val resultado = dpm.setMeteredDataDisabledPackages(admin, appsEntretenimiento)

// Verificar resultados
resultado.forEach { packageName ->
    Log.w("DataControl", "No se pudo bloquear datos móviles para: $packageName")
}
```

**Ejemplo de Uso - Solo Apps Corporativas en Móvil**:
```kotlin
// Estrategia: Bloquear TODAS las apps excepto las corporativas
val todasLasApps = obtenerTodasLasAppsInstaladas()
val appsCorporativasPermitidas = listOf(
    "com.empresa.crm",
    "com.microsoft.office.outlook",
    "com.empresa.vpn",
    "com.whatsapp.w4b" // WhatsApp Business
)

val appsABloquear = todasLasApps.filter { it !in appsCorporativasPermitidas }
dpm.setMeteredDataDisabledPackages(admin, appsABloquear)
```

**Retorna**: `List<String>` con paquetes donde falló el bloqueo (lista vacía si todo exitoso).

**Comportamiento**:
- Apps bloqueadas no pueden usar datos móviles
- Funcionan normalmente en WiFi
- Sincronización y notificaciones push pueden retrasarse
- Usuario ve mensaje "Data saver active" o similar

---

### `getMeteredDataDisabledPackages(ComponentName admin)`

**Propósito**: Obtiene la lista de paquetes con datos móviles bloqueados.

**Retorna**: `List<String>` con los package names.

```kotlin
fun reporteUsoDatos(): String {
    val appsBloqueadas = dpm.getMeteredDataDisabledPackages(admin)
    
    return buildString {
        append("📊 REPORTE DE CONTROL DE DATOS\n")
        append("═══════════════════════════════\n")
        append("Apps con datos móviles BLOQUEADOS: ${appsBloqueadas.size}\n\n")
        
        if (appsBloqueadas.isEmpty()) {
            append("✅ Todas las apps pueden usar datos móviles\n")
        } else {
            append("Apps bloqueadas:\n")
            appsBloqueadas.forEach { pkg ->
                append("  📵 $pkg\n")
            }
        }
    }
}
```

---

## 🔑 Métodos de Delegación de Capacidades

### `setDelegatedScopes(ComponentName admin, String delegatePackage, List<String> scopes)`

**Propósito**: Delega capacidades administrativas específicas a una aplicación que NO es Device Admin. Permite que apps normales realicen ciertas operaciones administrativas.

**Scopes Disponibles**:

| Scope | Capacidad Delegada |
|-------|-------------------|
| `DELEGATION_CERT_INSTALL` | Instalar/remover certificados CA |
| `DELEGATION_APP_RESTRICTIONS` | Gestionar restricciones de aplicaciones |
| `DELEGATION_BLOCK_UNINSTALL` | Bloquear/desbloquear desinstalación de apps |
| `DELEGATION_PERMISSION_GRANT` | Otorgar/denegar permisos de runtime |
| `DELEGATION_PACKAGE_ACCESS` | Acceder a información de paquetes |
| `DELEGATION_ENABLE_SYSTEM_APP` | Habilitar apps del sistema |
| `DELEGATION_NETWORK_LOGGING` | Acceder a logs de red |
| `DELEGATION_CERT_SELECTION` | Solicitar selección de certificados |
| `DELEGATION_SECURITY_LOGGING` | Acceder a logs de seguridad |

**Casos de Uso**:
- **Apps especializadas**: Delegar gestión de certificados a app de seguridad
- **Self-service**: Permitir que app corporativa se configure a sí misma
- **Arquitectura modular**: Separar responsabilidades entre apps
- **Gestión distribuida**: Diferentes apps manejan diferentes aspectos

**Ejemplo de Uso - Delegar Gestión de Certificados**:
```kotlin
// Permitir que app de seguridad instale certificados
dpm.setDelegatedScopes(
    admin,
    "com.empresa.cert_manager",
    listOf(DevicePolicyManager.DELEGATION_CERT_INSTALL)
)

// Ahora "com.empresa.cert_manager" puede:
// - Instalar certificados CA
// - Remover certificados
// - Sin necesidad de ser Device Admin
```

**Ejemplo de Uso - App Self-Service**:
```kotlin
// Permitir que app corporativa se gestione a sí misma
dpm.setDelegatedScopes(
    admin,
    "com.empresa.portal",
    listOf(
        DevicePolicyManager.DELEGATION_APP_RESTRICTIONS,
        DevicePolicyManager.DELEGATION_PERMISSION_GRANT
    )
)

// "com.empresa.portal" ahora puede:
// - Configurar sus propias restricciones
// - Otorgarse permisos que necesita
```

**Ejemplo de Uso - Gestión Modular**:
```kotlin
// App especializada en gestión de aplicaciones
dpm.setDelegatedScopes(
    admin,
    "com.empresa.app_manager",
    listOf(
        DevicePolicyManager.DELEGATION_BLOCK_UNINSTALL,
        DevicePolicyManager.DELEGATION_ENABLE_SYSTEM_APP,
        DevicePolicyManager.DELEGATION_PACKAGE_ACCESS
    )
)

// App especializada en seguridad
dpm.setDelegatedScopes(
    admin,
    "com.empresa.security",
    listOf(
        DevicePolicyManager.DELEGATION_CERT_INSTALL,
        DevicePolicyManager.DELEGATION_SECURITY_LOGGING,
        DevicePolicyManager.DELEGATION_NETWORK_LOGGING
    )
)
```

**Remover Delegación**:
```kotlin
// Revocar todas las delegaciones
dpm.setDelegatedScopes(admin, "com.empresa.app", emptyList())

// O asignar null
dpm.setDelegatedScopes(admin, "com.empresa.app", null)
```

---

### `getDelegatedScopes(ComponentName admin, String delegatePackage)`

**Propósito**: Obtiene los scopes actualmente delegados a una aplicación específica.

**Retorna**: `List<String>` con los scopes delegados.

```kotlin
fun auditarDelegaciones(packageName: String): String {
    val scopes = dpm.getDelegatedScopes(admin, packageName)
    
    if (scopes.isEmpty()) {
        return "📦 $packageName: Sin delegaciones"
    }
    
    return buildString {
        append("📦 DELEGACIONES DE $packageName\n")
        append("═══════════════════════════════\n")
        scopes.forEach { scope ->
            append("  ✅ ${traducirScope(scope)}\n")
        }
    }
}

fun traducirScope(scope: String): String = when(scope) {
    DevicePolicyManager.DELEGATION_CERT_INSTALL -> "Gestión de certificados"
    DevicePolicyManager.DELEGATION_APP_RESTRICTIONS -> "Restricciones de apps"
    DevicePolicyManager.DELEGATION_BLOCK_UNINSTALL -> "Bloqueo de desinstalación"
    DevicePolicyManager.DELEGATION_PERMISSION_GRANT -> "Otorgamiento de permisos"
    DevicePolicyManager.DELEGATION_PACKAGE_ACCESS -> "Acceso a información de paquetes"
    DevicePolicyManager.DELEGATION_ENABLE_SYSTEM_APP -> "Habilitación de apps del sistema"
    else -> scope
}
```

---

### `getDelegatePackages(ComponentName admin, String delegationScope)`

**Propósito**: Obtiene todas las aplicaciones que tienen un scope específico delegado.

**Retorna**: `List<String>` con package names que tienen ese scope.

```kotlin
// Encontrar quién puede instalar certificados
val gestoresCertificados = dpm.getDelegatePackages(
    admin,
    DevicePolicyManager.DELEGATION_CERT_INSTALL
)

Log.i("Delegation", "Apps con permiso para gestionar certificados:")
gestoresCertificados.forEach { pkg ->
    Log.i("Delegation", "  - $pkg")
}
```

---

## 🎯 Casos de Uso por Escenario

### 🏢 Corporativo Estándar
```kotlin
// Ocultar apps de consumo pre-instaladas
dpm.setApplicationHidden(admin, "com.facebook.katana", true)
dpm.setApplicationHidden(admin, "com.android.chrome", true)

// Proteger apps corporativas críticas
dpm.setUninstallBlocked(admin, "com.empresa.mdm", true)
dpm.setUninstallBlocked(admin, "com.microsoft.office.outlook", true)

// Forzar navegador corporativo
val browserFilter = IntentFilter().apply {
    addAction(Intent.ACTION_VIEW)
    addCategory(Intent.CATEGORY_BROWSABLE)
    addDataScheme("https")
}
dpm.addPersistentPreferredActivity(admin, browserFilter, 
    ComponentName("com.microsoft.emmx", "..."))

// Otorgar permisos automáticamente a apps corporativas
dpm.setPermissionGrantState(admin, "com.empresa.crm",
    android.Manifest.permission.ACCESS_FINE_LOCATION,
    DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED)

// Bloquear datos móviles para entretenimiento
dpm.setMeteredDataDisabledPackages(admin, listOf(
    "com.netflix.mediaclient",
    "com.spotify.music"
))
```

---

### 🏭 Kiosko/Punto de Venta
```kotlin
// Ocultar TODAS las apps excepto la del kiosko
val todasLasApps = obtenerTodasLasAppsInstaladas()
val appKiosko = "com.empresa.pos"

todasLasApps.filter { it != appKiosko }.forEach { pkg ->
    dpm.setApplicationHidden(admin, pkg, true)
}

// Forzar app de kiosko para todos los intents relevantes
val homeFilter = IntentFilter(Intent.ACTION_MAIN).apply {
    addCategory(Intent.CATEGORY_HOME)
    addCategory(Intent.CATEGORY_DEFAULT)
}
dpm.addPersistentPreferredActivity(admin, homeFilter,
    ComponentName(appKiosko, "..."))

// Proteger app de kiosko
dpm.setUninstallBlocked(admin, appKiosko, true)
dpm.setUserControlDisabledPackages(admin, listOf(appKiosko))

// Auto-conceder todos los permisos
dpm.setPermissionPolicy(admin, DevicePolicyManager.PERMISSION_POLICY_AUTO_GRANT)
```

---

### 🏥 Médico/HIPAA
```kotlin
// Bloquear apps de entretenimiento con datos sensibles
val appsBloquear = listOf(
    "com.facebook.katana",
    "com.instagram.android",
    "com.snapchat.android"
)

appsBloquear.forEach { pkg ->
    dpm.setApplicationHidden(admin, pkg, true)
}

// Proteger apps médicas críticas
dpm.setUninstallBlocked(admin, "com.hospital.ehr", true)
dpm.setUserControlDisabledPackages(admin, listOf("com.hospital.ehr"))

// Denegar permisos peligrosos por defecto
dpm.setPermissionPolicy(admin, DevicePolicyManager.PERMISSION_POLICY_AUTO_DENY)

// Otorgar permisos solo a apps médicas autorizadas
dpm.setPermissionGrantState(admin, "com.hospital.ehr",
    android.Manifest.permission.CAMERA,
    DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED)

// Sin datos móviles para apps no médicas
val appsNoMedicas = obtenerAppsNoMedicas()
dpm.setMeteredDataDisabledPackages(admin, appsNoMedicas)
```

---

## ⚠️ Consideraciones Importantes

### Compatibilidad
- Muchos métodos requieren API levels específicos
- `setPackagesSuspended()` requiere API 24+
- `setDelegatedScopes()` requiere API 26+
- Verificar siempre `Build.VERSION.SDK_INT`

### Seguridad
- **Bloqueo de desinstalación**: No abuses, puede frustrar usuarios
- **Apps ocultas**: Usuario puede sospechar si falta funcionalidad esperada
- **Permisos automáticos**: Solo para apps de confianza absoluta
- **Delegación**: Auditar regularmente qué apps tienen qué capacidades

### Mejores Prácticas

1. **Gestión de apps ocultas**: Mantener lista de apps ocultas en configuración remota para fácil actualización

2. **Permisos granulares**: Preferir `setPermissionGrantState()` individual sobre `PERMISSION_POLICY_AUTO_GRANT` global

3. **Documentación**: Documentar por qué cada app tiene bloqueo de desinstalación o permisos especiales

4. **Testing**: Probar impacto en experiencia de usuario antes de desplegar

5. **Reversibilidad**: Mantener capacidad de revertir cambios remotamente

---

## 📚 Próxima Categoría

**5. Lock Task Mode (Modo Kiosko)** (~10 métodos)

Esta categoría cubrirá:
- Configuración de modo kiosko
- Paquetes permitidos en Lock Task
- Features habilitadas en modo kiosko
- Iniciar/detener Lock Task Mode
- Verificación de permisos

---

*Documentación generada para DevicePolicyManager API de Android Enterprise - Categoría 4 de 22*