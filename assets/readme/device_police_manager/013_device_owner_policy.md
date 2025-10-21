# Documentación DevicePolicyManager - Categoría 13: Device Owner y Profile Owner

## 📋 Visión General

La categoría de **Device Owner y Profile Owner** agrupa aproximadamente **20 métodos** que permiten verificar, gestionar y transferir la propiedad administrativa de dispositivos y perfiles. Son fundamentales para entender el estado de gestión del dispositivo y realizar operaciones de provisioning y ownership.

## 🎯 Propósito

Estos métodos permiten:
- **Verificar** si una app es Device Owner o Profile Owner
- **Obtener información** del administrador actual
- **Transferir ownership** entre aplicaciones
- **Gestionar provisioning** del dispositivo
- **Obtener identificadores** únicos del dispositivo
- **Configurar organization IDs**

---

## 🔍 Conceptos Fundamentales

### Device Owner vs Profile Owner

**Device Owner (DO)**:
- Control total del dispositivo completo
- Solo uno por dispositivo
- Se configura durante provisioning inicial (OOBE)
- Puede crear y gestionar usuarios
- Acceso a todas las funcionalidades de DPM
- Típico: Dispositivos corporativos completamente gestionados

**Profile Owner (PO)**:
- Control de un perfil específico (Work Profile)
- Múltiples posibles (uno por perfil)
- Se puede configurar después del setup inicial
- Control limitado al perfil
- No puede gestionar usuarios del dispositivo
- Típico: BYOD con separación trabajo/personal

**Comparación Rápida**:
```
┌─────────────────────────────────────────────────┐
│              DEVICE OWNER                       │
│  ┌─────────────────────────────────────────┐   │
│  │         Usuario Principal               │   │
│  │  ┌──────────────────────────────────┐   │   │
│  │  │      Profile Owner (opcional)    │   │   │
│  │  │        Work Profile              │   │   │
│  │  └──────────────────────────────────┘   │   │
│  └─────────────────────────────────────────┘   │
│                                                 │
│  ┌─────────────────────────────────────────┐   │
│  │      Usuario Secundario                 │   │
│  └─────────────────────────────────────────┘   │
└─────────────────────────────────────────────────┘
```

---

## ✅ Métodos de Verificación

### `isDeviceOwnerApp(String packageName)`

**Propósito**: Verifica si un paquete específico es el Device Owner del dispositivo.

**Casos de Uso**:
- Validar permisos antes de operaciones críticas
- Verificar estado de gestión del dispositivo
- Debugging de problemas de ownership
- Validación en runtime de capacidades

**Ejemplo de Uso**:
```kotlin
fun verificarEsDeviceOwner(): Boolean {
    val miPackage = context.packageName
    val esDeviceOwner = dpm.isDeviceOwnerApp(miPackage)

    if (esDeviceOwner) {
        Log.i("Ownership", "✅ Esta app ES Device Owner")
        Log.i("Ownership", "   Control total del dispositivo")
        Log.i("Ownership", "   Acceso a todas las APIs de DPM")
    } else {
        Log.w("Ownership", "❌ Esta app NO es Device Owner")
        Log.w("Ownership", "   Funcionalidades limitadas")
    }

    return esDeviceOwner
}

// Validar antes de operación que requiere Device Owner
fun operacionRequiereDeviceOwner() {
    if (!dpm.isDeviceOwnerApp(context.packageName)) {
        throw SecurityException("Esta operación requiere Device Owner")
    }

    // Proceder con operación
    dpm.reboot(admin)
}

// Verificar otro paquete
fun verificarOtraApp(packageName: String): Boolean {
    return dpm.isDeviceOwnerApp(packageName)
}
```

**Retorna**: `boolean`
- `true`: El paquete ES Device Owner
- `false`: El paquete NO es Device Owner

---

### `isProfileOwnerApp(String packageName)`

**Propósito**: Verifica si un paquete específico es Profile Owner del perfil actual.

**Casos de Uso**:
- Validar capacidades en Work Profile
- Verificar gestión de BYOD
- Debugging de configuración de perfiles

**Ejemplo de Uso**:
```kotlin
fun verificarEsProfileOwner(): Boolean {
    val miPackage = context.packageName
    val esProfileOwner = dpm.isProfileOwnerApp(miPackage)

    if (esProfileOwner) {
        Log.i("Ownership", "✅ Esta app ES Profile Owner")
        Log.i("Ownership", "   Control del Work Profile")
    } else {
        Log.w("Ownership", "❌ Esta app NO es Profile Owner")
    }

    return esProfileOwner
}

// Determinar tipo de administrador
fun determinarTipoAdmin(): TipoAdmin {
    val miPackage = context.packageName

    return when {
        dpm.isDeviceOwnerApp(miPackage) -> TipoAdmin.DEVICE_OWNER
        dpm.isProfileOwnerApp(miPackage) -> TipoAdmin.PROFILE_OWNER
        else -> TipoAdmin.NINGUNO
    }
}

enum class TipoAdmin {
    DEVICE_OWNER,
    PROFILE_OWNER,
    NINGUNO
}
```

**Retorna**: `boolean`

---

## 📋 Métodos de Obtención de Información

### `getDeviceOwner()`

**Propósito**: Obtiene el package name de la aplicación que es Device Owner del dispositivo.

**Retorna**:
- `String`: Package name del Device Owner
- `null`: No hay Device Owner configurado

**Ejemplo de Uso**:
```kotlin
fun obtenerDeviceOwner(): String? {
    val deviceOwner = dpm.getDeviceOwner()

    if (deviceOwner != null) {
        Log.i("Ownership", "📱 Device Owner: $deviceOwner")

        // Obtener información adicional de la app
        try {
            val appInfo = context.packageManager.getApplicationInfo(deviceOwner, 0)
            val appName = context.packageManager.getApplicationLabel(appInfo)
            Log.i("Ownership", "   Nombre: $appName")
        } catch (e: PackageManager.NameNotFoundException) {
            Log.w("Ownership", "   App no encontrada")
        }
    } else {
        Log.i("Ownership", "📱 Sin Device Owner configurado")
        Log.i("Ownership", "   Dispositivo no gestionado")
    }

    return deviceOwner
}

// Verificar si yo soy el Device Owner
fun soyYoElDeviceOwner(): Boolean {
    val deviceOwner = dpm.getDeviceOwner()
    return deviceOwner == context.packageName
}
```

---

### `getDeviceOwnerNameOnAnyUser()`

**Propósito**: Obtiene el nombre de la aplicación Device Owner, funciona desde cualquier usuario del dispositivo.

**Diferencia con `getDeviceOwner()`**:
- `getDeviceOwner()`: Solo funciona en el usuario donde está el DO
- `getDeviceOwnerNameOnAnyUser()`: Funciona desde cualquier usuario

**Retorna**: `String` con el nombre de la app o `null`

---

### `getProfileOwner()`

**Propósito**: Obtiene el ComponentName del Profile Owner del perfil actual.

**Retorna**:
- `ComponentName`: Admin que es Profile Owner
- `null`: No hay Profile Owner en este perfil

**Ejemplo de Uso**:
```kotlin
fun obtenerProfileOwner(): ComponentName? {
    val profileOwner = dpm.getProfileOwner()

    if (profileOwner != null) {
        Log.i("Ownership", "👔 Profile Owner: ${profileOwner.packageName}")
        Log.i("Ownership", "   Clase: ${profileOwner.className}")
    } else {
        Log.i("Ownership", "👔 Sin Profile Owner en este perfil")
    }

    return profileOwner
}
```

---

### `getProfileOwnerAsUser(UserHandle userHandle)`

**Propósito**: Obtiene el ComponentName del Profile Owner de un usuario específico.

**Casos de Uso**:
- Verificar Profile Owner de otros usuarios (requiere permisos)
- Auditoría de configuración multi-usuario

**Retorna**: `ComponentName` o `null`

---

### `getProfileOwnerName()`

**Propósito**: Obtiene el nombre legible del Profile Owner del perfil actual.

**Retorna**: `String` con el nombre de la app o `null`

---

### `getDeviceOwnerComponentOnCallingUser()`

**Propósito**: Obtiene el ComponentName del Device Owner desde el contexto del usuario que llama.

**Retorna**: `ComponentName` o `null`

---

### `getDeviceOwnerComponentOnAnyUser()`

**Propósito**: Obtiene el ComponentName del Device Owner independientemente del usuario actual.

**Retorna**: `ComponentName` o `null`

**Ejemplo Completo - Auditoría de Ownership**:
```kotlin
fun auditarOwnership(): String {
    return buildString {
        append("🔐 AUDITORÍA DE OWNERSHIP\n")
        append("═══════════════════════════════════\n\n")

        // Device Owner
        append("📱 DEVICE OWNER\n")
        append("─────────────────\n")
        val deviceOwner = dpm.getDeviceOwner()
        if (deviceOwner != null) {
            append("✅ Configurado\n")
            append("   Package: $deviceOwner\n")

            val deviceOwnerComponent = dpm.getDeviceOwnerComponentOnAnyUser()
            if (deviceOwnerComponent != null) {
                append("   Component: ${deviceOwnerComponent.className}\n")
            }

            val deviceOwnerName = dpm.getDeviceOwnerNameOnAnyUser()
            append("   Nombre: ${deviceOwnerName ?: "N/A"}\n")

            // ¿Soy yo?
            if (deviceOwner == context.packageName) {
                append("   👑 ESTA APP es el Device Owner\n")
            }
        } else {
            append("❌ No configurado\n")
            append("   Dispositivo no tiene gestión completa\n")
        }

        append("\n")

        // Profile Owner
        append("👔 PROFILE OWNER\n")
        append("─────────────────\n")
        val profileOwner = dpm.getProfileOwner()
        if (profileOwner != null) {
            append("✅ Configurado\n")
            append("   Package: ${profileOwner.packageName}\n")
            append("   Component: ${profileOwner.className}\n")

            val profileOwnerName = dpm.getProfileOwnerName()
            append("   Nombre: ${profileOwnerName ?: "N/A"}\n")

            // ¿Soy yo?
            if (profileOwner.packageName == context.packageName) {
                append("   👑 ESTA APP es el Profile Owner\n")
            }
        } else {
            append("❌ No configurado en este perfil\n")
        }

        append("\n")

        // Mi estado
        append("📊 MI ESTADO\n")
        append("─────────────────\n")
        val tipoAdmin = determinarTipoAdmin()
        when (tipoAdmin) {
            TipoAdmin.DEVICE_OWNER -> {
                append("👑 SOY DEVICE OWNER\n")
                append("   Control total del dispositivo\n")
                append("   Acceso a todas las APIs\n")
            }
            TipoAdmin.PROFILE_OWNER -> {
                append("👔 SOY PROFILE OWNER\n")
                append("   Control del Work Profile\n")
                append("   APIs limitadas al perfil\n")
            }
            TipoAdmin.NINGUNO -> {
                append("⚠️ NO SOY ADMINISTRADOR\n")
                append("   Sin privilegios de gestión\n")
            }
        }
    }
}
```

---

## 🔄 Métodos de Transferencia de Ownership

### `transferOwnership(ComponentName admin, ComponentName target, PersistableBundle bundle)`

**Propósito**: Transfiere el ownership (Device Owner o Profile Owner) de la aplicación actual a otra aplicación. Útil para migración entre sistemas de gestión.

**Parámetros**:
- `admin`: ComponentName del admin actual (origen)
- `target`: ComponentName del nuevo admin (destino)
- `bundle`: Datos adicionales para transferir a la nueva app

**Casos de Uso**:
- **Migración de MDM**: Cambiar de proveedor de gestión
- **Actualización de sistema**: Nueva versión con package diferente
- **Consolidación**: Unificar gestión bajo una sola app
- **Testing**: Transferir entre versiones de desarrollo

**Requisitos Previos**:
1. App destino debe estar instalada
2. App destino debe declarar capacidad de recibir ownership
3. App destino debe tener DeviceAdminReceiver configurado
4. Usuario/admin debe confirmar transferencia

**Ejemplo de Uso**:
```kotlin
fun transferirOwnershipANuevaApp() {
    val targetPackage = "com.empresa.nuevo_mdm"
    val targetReceiver = "$targetPackage.NuevoDeviceAdminReceiver"
    val target = ComponentName(targetPackage, targetReceiver)

    // Preparar datos para transferir
    val bundle = PersistableBundle().apply {
        putString("migration_version", "2.0")
        putString("previous_mdm", context.packageName)
        putLong("transfer_timestamp", System.currentTimeMillis())
        putString("device_id", obtenerDeviceId())

        // Configuraciones a migrar
        putString("org_name", "Empresa Corp")
        putString("org_color", "#0066CC")
        putBoolean("camera_disabled", true)
        putBoolean("screen_capture_disabled", true)
    }

    try {
        // Mostrar confirmación al usuario
        AlertDialog.Builder(context)
            .setTitle("Transferir Gestión")
            .setMessage("¿Transferir control del dispositivo a la nueva app de gestión?")
            .setPositiveButton("Transferir") { _, _ ->
                ejecutarTransferencia(target, bundle)
            }
            .setNegativeButton("Cancelar", null)
            .show()

    } catch (e: Exception) {
        Log.e("Transfer", "❌ Error al preparar transferencia: ${e.message}")
    }
}

private fun ejecutarTransferencia(target: ComponentName, bundle: PersistableBundle) {
    try {
        dpm.transferOwnership(admin, target, bundle)

        Log.i("Transfer", "✅ Ownership transferido exitosamente")
        Log.i("Transfer", "   Nuevo admin: ${target.packageName}")

        // Esta app ya no es admin después de la transferencia
        mostrarNotificacion(
            "Transferencia Completa",
            "La gestión del dispositivo ha sido transferida"
        )

        // Opcionalmente, esta app puede desinstalarse
        // ya que ya no tiene rol de administrador

    } catch (e: IllegalArgumentException) {
        Log.e("Transfer", "❌ App destino no válida: ${e.message}")
        mostrarError("App destino no puede recibir ownership")
    } catch (e: SecurityException) {
        Log.e("Transfer", "❌ Sin permisos: ${e.message}")
        mostrarError("No se pudo completar la transferencia")
    }
}
```

**Configuración en App Destino**:
```xml
<!-- AndroidManifest.xml de la app destino -->
<receiver android:name=".NuevoDeviceAdminReceiver"
        android:permission="android.permission.BIND_DEVICE_ADMIN"
        android:exported="true">
    <meta-data android:name="android.app.device_admin"
            android:resource="@xml/device_admin_receiver" />

    <!-- CRÍTICO: Declarar soporte para transferencia -->
    <meta-data android:name="android.app.device_admin.SUPPORT_TRANSFER_OWNERSHIP"
            android:value="true" />

    <intent-filter>
        <action android:name="android.app.action.DEVICE_ADMIN_ENABLED" />
        <action android:name="android.app.action.PROFILE_OWNER_CHANGED" />
        <action android:name="android.app.action.DEVICE_OWNER_CHANGED" />
    </intent-filter>
</receiver>
```

**Recibir Transferencia en App Destino**:
```kotlin
class NuevoDeviceAdminReceiver : DeviceAdminReceiver() {

    override fun onTransferOwnershipComplete(context: Context, bundle: PersistableBundle?) {
        super.onTransferOwnershipComplete(context, bundle)

        Log.i("Transfer", "🎉 Ownership recibido exitosamente")

        if (bundle != null) {
            // Recuperar datos transferidos
            val previousMdm = bundle.getString("previous_mdm")
            val orgName = bundle.getString("org_name")
            val deviceId = bundle.getString("device_id")

            Log.i("Transfer", "Migrado desde: $previousMdm")
            Log.i("Transfer", "Organización: $orgName")
            Log.i("Transfer", "Device ID: $deviceId")

            // Aplicar configuraciones migradas
            aplicarConfiguracionesMigradas(bundle)
        }

        // Inicializar nuevo MDM
        inicializarNuevoMDM(context)
    }

    private fun aplicarConfiguracionesMigradas(bundle: PersistableBundle) {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val admin = ComponentName(context, NuevoDeviceAdminReceiver::class.java)

        // Aplicar configuraciones del MDM anterior
        val orgName = bundle.getString("org_name")
        if (orgName != null) {
            dpm.setOrganizationName(admin, orgName)
        }

        val cameraDisabled = bundle.getBoolean("camera_disabled", false)
        dpm.setCameraDisabled(admin, cameraDisabled)

        val screenCaptureDisabled = bundle.getBoolean("screen_capture_disabled", false)
        dpm.setScreenCaptureDisabled(admin, screenCaptureDisabled)

        Log.i("Transfer", "✅ Configuraciones migradas aplicadas")
    }
}
```

**⚠️ Importante**:
- Transferencia es **irreversible** sin factory reset
- App origen pierde todos los privilegios de admin
- App destino debe estar preparada para recibir ownership
- Usuario puede ver notificación del sistema
- Proceso puede tardar varios segundos

---

## 🆔 Métodos de Identificación del Dispositivo

### `getEnrollmentSpecificId()`

**Propósito**: Obtiene un identificador único del dispositivo que es específico para este enrollment (inscripción). Este ID cambia si el dispositivo es factory reset o re-enrolado.

**Características**:
- Único por dispositivo Y enrollment
- Cambia tras factory reset
- Cambia si se cambia de Device Owner
- No es el IMEI, MAC, o Android ID
- Útil para tracking sin exponer identificadores permanentes

**Casos de Uso**:
- **Tracking de flota**: Identificar dispositivos en sistema MDM
- **Auditoría**: Vincular eventos a dispositivos específicos
- **Inventario**: Gestión de activos corporativos
- **Compliance**: Reportes de cumplimiento por dispositivo
- **Privacidad**: ID que no compromete identidad física

**Ejemplo de Uso**:
```kotlin
fun obtenerIdDispositivo(): String? {
    val enrollmentId = dpm.getEnrollmentSpecificId()

    if (enrollmentId != null) {
        Log.i("DeviceID", "📱 Enrollment ID: $enrollmentId")
        Log.i("DeviceID", "   Único para este enrollment")
        Log.i("DeviceID", "   Cambia tras factory reset")
    } else {
        Log.w("DeviceID", "❌ No disponible (Android < 12)")
    }

    return enrollmentId
}

// Registrar dispositivo en servidor MDM
fun registrarDispositivoEnMDM() {
    val enrollmentId = dpm.getEnrollmentSpecificId()

    if (enrollmentId != null) {
        val infoDispositivo = mapOf(
            "enrollment_id" to enrollmentId,
            "model" to Build.MODEL,
            "manufacturer" to Build.MANUFACTURER,
            "android_version" to Build.VERSION.RELEASE,
            "timestamp" to System.currentTimeMillis()
        )

        enviarAServidorMDM(infoDispositivo)
    }
}

// Verificar si dispositivo ya está registrado
fun verificarRegistro(): Boolean {
    val enrollmentId = dpm.getEnrollmentSpecificId() ?: return false
    return consultarServidorMDM(enrollmentId)
}
```

**Disponibilidad**: Android 12+ (API 31)

**Retorna**: `String` con el ID o `null` si no disponible

---

### `getDeviceId()` ⚠️ **DEPRECATED - API 34**

**Propósito**: Obtiene un identificador del dispositivo. Deprecated en Android 14 en favor de `getEnrollmentSpecificId()`.

**Estado**: Usar `getEnrollmentSpecificId()` en su lugar.

---

## 🏢 Métodos de Organization ID

### `setOrganizationId(String organizationId)`

**Propósito**: Establece un identificador de organización que puede ser compartido entre Device Owner y Profile Owner para indicar que pertenecen a la misma empresa (afiliación).

**Relación con Afiliación**:
- Complementa `setAffiliationIds()`
- Ayuda a identificar la organización propietaria
- Útil para multi-tenancy en MDM

**Casos de Uso**:
- **MDM multi-empresa**: Distinguir entre clientes
- **Subsidiarias**: Identificar divisiones de empresa
- **Reporting**: Agrupar dispositivos por organización
- **Compliance**: Políticas específicas por organización

**Ejemplo de Uso**:
```kotlin
fun configurarOrganizationId() {
    // ID único de la organización (puede ser UUID, código interno, etc.)
    val orgId = "empresa-corp-2024-uuid-12345"

    dpm.setOrganizationId(orgId)

    Log.i("OrgID", "🏢 Organization ID establecido: $orgId")

    // También establecer affiliation IDs para Device Owner y Profile Owner
    val affiliationIds = setOf(orgId, "empresa-corp")
    dpm.setAffiliationIds(admin, affiliationIds)
}

// Por tipo de cliente en MDM
fun configurarPorCliente(cliente: Cliente) {
    val orgId = when (cliente.tipo) {
        TipoCliente.ENTERPRISE -> "ent-${cliente.id}"
        TipoCliente.SMB -> "smb-${cliente.id}"
        TipoCliente.EDU -> "edu-${cliente.id}"
        TipoCliente.GOV -> "gov-${cliente.id}"
    }

    dpm.setOrganizationId(orgId)
    Log.i("OrgID", "Cliente ${cliente.nombre}: $orgId")
}
```

---

## 🎯 Métodos de Tipo de Device Owner

### `setDeviceOwnerType(ComponentName admin, int deviceOwnerType)`

**Propósito**: Establece el tipo de Device Owner, indicando el propósito o modelo de gestión del dispositivo.

**Tipos Disponibles**:
- `DEVICE_OWNER_TYPE_DEFAULT` (0): Tipo por defecto
- `DEVICE_OWNER_TYPE_FINANCED` (1): Dispositivo financiado/arrendado

**Casos de Uso**:
- **Dispositivos financiados**: Indicar que dispositivo está bajo financiamiento
- **Leasing**: Dispositivos arrendados a empleados
- **Programa BYOD con subsidio**: Dispositivos con ayuda financiera corporativa

**Ejemplo de Uso**:
```kotlin
fun configurarDispositivoFinanciado() {
    dpm.setDeviceOwnerType(
        admin,
        DevicePolicyManager.DEVICE_OWNER_TYPE_FINANCED
    )

    Log.i("OwnerType", "💳 Dispositivo marcado como FINANCIADO")
    Log.i("OwnerType", "   Políticas de devolución aplicables")
}

fun configurarDispositivoEstandar() {
    dpm.setDeviceOwnerType(
        admin,
        DevicePolicyManager.DEVICE_OWNER_TYPE_DEFAULT
    )

    Log.i("OwnerType", "📱 Dispositivo tipo ESTÁNDAR")
}
```

---

### `getDeviceOwnerType(ComponentName admin)`

**Propósito**: Obtiene el tipo de Device Owner configurado.

**Retorna**: `int` con el tipo.

---

### `isDeviceFinanced()`

**Propósito**: Verifica rápidamente si el dispositivo está marcado como financiado.

**Retorna**: `boolean`

**Ejemplo de Uso**:
```kotlin
fun verificarEstadoFinanciamiento(): String {
    val esFinanciado = dpm.isDeviceFinanced()
    val tipo = dpm.getDeviceOwnerType(admin)

    return buildString {
        append("💳 ESTADO DE FINANCIAMIENTO\n")
        append("═══════════════════════════════\n\n")

        if (esFinanciado) {
            append("✅ Dispositivo FINANCIADO\n")
            append("   - Políticas de pago activas\n")
            append("   - Restricciones de devolución\n")
            append("   - Bloqueo por falta de pago posible\n")
        } else {
            append("📱 Dispositivo PROPIEDAD COMPLETA\n")
            append("   - Sin financiamiento activo\n")
            append("   - Sin restricciones de pago\n")
        }

        append("\nTipo: ")
        when (tipo) {
            DevicePolicyManager.DEVICE_OWNER_TYPE_FINANCED -> {
                append("Financiado/Arrendado")
            }
            DevicePolicyManager.DEVICE_OWNER_TYPE_DEFAULT -> {
                append("Estándar")
            }
            else -> append("Desconocido")
        }
    }
}

// Aplicar políticas según financiamiento
fun aplicarPoliticasSegunFinanciamiento() {
    if (dpm.isDeviceFinanced()) {
        // Políticas más estrictas para dispositivos financiados
        dpm.setUserRestriction(admin, UserManager.DISALLOW_FACTORY_RESET)
        dpm.setUserRestriction(admin, UserManager.DISALLOW_SAFE_BOOT)
        dpm.setUninstallBlocked(admin, context.packageName, true)

        Log.i("Policy", "🔒 Políticas de dispositivo financiado aplicadas")
    } else {
        // Políticas estándar
        Log.i("Policy", "📱 Políticas estándar aplicadas")
    }
}
```

---

## 🎯 Caso de Uso Completo

### Sistema de Ownership y Gestión
```kotlin
class OwnershipManager(
    private val context: Context,
    private val dpm: DevicePolicyManager,
    private val admin: ComponentName
) {
    
    fun inicializarYConfigura() {
        // 1. Verificar tipo de admin
        val tipoAdmin = determinarTipoAdmin()
        
        when (tipoAdmin) {
            TipoAdmin.DEVICE_OWNER -> {
                configurarComoDeviceOwner()
            }
            TipoAdmin.PROFILE_OWNER -> {
                configurarComoProfileOwner()
            }
            TipoAdmin.NINGUNO -> {
                Log.e("Ownership", "❌ No somos administrador")
                solicitarActivacion()
            }
        }
    }
    
    private fun configurarComoDeviceOwner() {
        Log.i("Ownership", "👑 Configurando como Device Owner")
        
        // Establecer organization ID
        dpm.setOrganizationId("empresa-corp-2024-${UUID.randomUUID()}")
        
        // Establecer affiliation IDs
        dpm.setAffiliationIds(admin, setOf("empresa-corp", "division-ti"))
        
        // Obtener y registrar enrollment ID
        val enrollmentId = dpm.getEnrollmentSpecificId()
        if (enrollmentId != null) {
            registrarEnServidorMDM(enrollmentId)
        }
        
        // Configurar tipo de dispositivo
        val esFinanciado = verificarSiEsDispositivoFinanciado()
        if (esFinanciado) {
            dpm.setDeviceOwnerType(admin, DevicePolicyManager.DEVICE_OWNER_TYPE_FINANCED)
            aplicarPoliticasFinanciamiento()
        } else {
            dpm.setDeviceOwnerType(admin, DevicePolicyManager.DEVICE_OWNER_TYPE_DEFAULT)
        }
        
        // Aplicar configuración completa
        aplicarConfiguracionCompleta()
    }
    
    private fun configurarComoProfileOwner() {
        Log.i("Ownership", "👔 Configurando como Profile Owner")
        
        // Affiliation con Device Owner (si existe)
        dpm.setAffiliationIds(admin, setOf("empresa-corp", "division-ti"))
        
        // Configuración de Work Profile
        dpm.setProfileName(admin, "Empresa Corp")
        dpm.setProfileEnabled(admin, admin)
        
        aplicarConfiguracionWorkProfile()
    }
    
    fun generarReporteCompleto(): String {
        return buildString {
            append("═══════════════════════════════════════\n")
            append("    REPORTE DE OWNERSHIP Y GESTIÓN\n")
            append("═══════════════════════════════════════\n\n")
            
            // Tipo de administrador
            append("👑 TIPO DE ADMINISTRADOR\n")
            append("─────────────────────────────────\n")
            val tipo = determinarTipoAdmin()
            append("Estado: ${tipo.name}\n\n")
            
            // Device Owner Info
            append("📱 DEVICE OWNER\n")
            append("─────────────────────────────────\n")
            val deviceOwner = dpm.getDeviceOwner()
            if (deviceOwner != null) {
                append("Package: $deviceOwner\n")
                append("Nombre: ${dpm.getDeviceOwnerNameOnAnyUser() ?: "N/A"}\n")
                append("Soy yo: ${deviceOwner == context.packageName}\n")
            } else {
                append("No configurado\n")
            }
            append("\n")
            
            // Profile Owner Info
            append("👔 PROFILE OWNER\n")
            append("─────────────────────────────────\n")
            val profileOwner = dpm.getProfileOwner()
            if (profileOwner != null) {
                append("Package: ${profileOwner.packageName}\n")
                append("Nombre: ${dpm.getProfileOwnerName() ?: "N/A"}\n")
                append("Soy yo: ${profileOwner.packageName == context.packageName}\n")
            } else {
                append("No configurado\n")
            }
            append("\n")
            
            // IDs y Afiliación
            append("🔗 IDENTIFICACIÓN\n")
            append("─────────────────────────────────\n")
            val enrollmentId = dpm.getEnrollmentSpecificId()
            append("Enrollment ID: ${enrollmentId ?: "N/A (Android < 12)"}\n")
            
            val afiliado = dpm.isAffiliatedUser()
            append("Afiliado: ${if (afiliado) "✅ Sí" else "❌ No"}\n")
            
            val affiliationIds = dpm.getAffiliationIds(admin)
            if (affiliationIds.isNotEmpty()) {
                append("Affiliation IDs:\n")
                affiliationIds.forEach { id ->
                    append("  • $id\n")
                }
            }
            append("\n")
            
            // Tipo de dispositivo
            append("💳 TIPO DE DISPOSITIVO\n")
            append("─────────────────────────────────\n")
            val esFinanciado = dpm.isDeviceFinanced()
            append("Financiado: ${if (esFinanciado) "✅ Sí" else "❌ No"}\n")
            
            val tipoOwner = dpm.getDeviceOwnerType(admin)
            append("Tipo Owner: ")
            when (tipoOwner) {
                DevicePolicyManager.DEVICE_OWNER_TYPE_FINANCED -> append("Financiado/Arrendado\n")
                DevicePolicyManager.DEVICE_OWNER_TYPE_DEFAULT -> append("Estándar\n")
                else -> append("Desconocido\n")
            }
            append("\n")
            
            // Capacidades
            append("🔓 CAPACIDADES\n")
            append("─────────────────────────────────\n")
            when (tipo) {
                TipoAdmin.DEVICE_OWNER -> {
                    append("✅ Control total del dispositivo\n")
                    append("✅ Gestión de usuarios\n")
                    append("✅ Todas las APIs de DPM\n")
                    append("✅ Global/Secure/System settings\n")
                    append("✅ Factory reset protection\n")
                }
                TipoAdmin.PROFILE_OWNER -> {
                    append("✅ Control del Work Profile\n")
                    append("⚠️ APIs limitadas al perfil\n")
                    append("❌ No gestión de usuarios\n")
                    append("❌ No Global settings\n")
                }
                TipoAdmin.NINGUNO -> {
                    append("❌ Sin privilegios administrativos\n")
                }
            }
            
            append("\n")
            append("Generado: ${Date()}\n")
            append("═══════════════════════════════════════\n")
        }
    }
    
    private fun determinarTipoAdmin(): TipoAdmin {
        val miPackage = context.packageName
        return when {
            dpm.isDeviceOwnerApp(miPackage) -> TipoAdmin.DEVICE_OWNER
            dpm.isProfileOwnerApp(miPackage) -> TipoAdmin.PROFILE_OWNER
            else -> TipoAdmin.NINGUNO
        }
    }
    
    private fun registrarEnServidorMDM(enrollmentId: String) {
        // Implementación de registro en servidor MDM
        Log.i("MDM", "Registrando dispositivo: $enrollmentId")
    }
    
    private fun verificarSiEsDispositivoFinanciado(): Boolean {
        // Lógica para determinar si es financiado
        // Puede consultar servidor, verificar inventario, etc.
        return false // Placeholder
    }
    
    private fun aplicarPoliticasFinanciamiento() {
        Log.i("Policy", "Aplicando políticas de dispositivo financiado")
        dpm.setUserRestriction(admin, UserManager.DISALLOW_FACTORY_RESET)
        dpm.setUserRestriction(admin, UserManager.DISALLOW_SAFE_BOOT)
        dpm.setUninstallBlocked(admin, context.packageName, true)
    }
    
    private fun aplicarConfiguracionCompleta() {
        // Implementar configuración completa del dispositivo
        Log.i("Config", "Aplicando configuración completa")
    }
    
    private fun aplicarConfiguracionWorkProfile() {
        // Implementar configuración de Work Profile
        Log.i("Config", "Aplicando configuración de Work Profile")
    }
    
    private fun solicitarActivacion() {
        // Mostrar UI para que usuario active admin
        Log.w("Ownership", "Solicitar activación de administrador")
    }
}

enum class TipoAdmin {
    DEVICE_OWNER,
    PROFILE_OWNER,
    NINGUNO
}
```

---

## ⚠️ Consideraciones Importantes

### Device Owner vs Profile Owner

**Cuándo Usar Device Owner**:
- ✅ Dispositivos completamente corporativos (COBO)
- ✅ Control total requerido
- ✅ Kioscos, dispositivos dedicados
- ✅ Gestión de múltiples usuarios
- ❌ BYOD (invasivo para usuario)

**Cuándo Usar Profile Owner**:
- ✅ BYOD (Bring Your Own Device)
- ✅ Separación trabajo/personal
- ✅ Menos invasivo para usuario
- ❌ Control limitado
- ❌ No gestión del dispositivo completo

### Provisioning

**Device Owner se configura**:
- Durante OOBE (Out Of Box Experience)
- Con NFC bump
- Con QR code
- Con DPC identifier
- Con adb (solo para desarrollo)

**Profile Owner se configura**:
- En cualquier momento después de setup
- Sin factory reset
- Desde Google Play Managed
- Programáticamente con APIs

**No se puede**:
- ❌ Convertir Device Owner en Profile Owner
- ❌ Tener múltiples Device Owners
- ❌ Configurar Device Owner en dispositivo usado (sin factory reset)

### Transferencia de Ownership

**Requisitos**:
1. App destino instalada
2. App destino declara soporte (`SUPPORT_TRANSFER_OWNERSHIP`)
3. App destino tiene DeviceAdminReceiver válido
4. No hay conflictos de políticas

**Limitaciones**:
- Proceso irreversible sin factory reset
- Puede tardar varios segundos
- Usuario puede ver notificaciones
- Algunas configuraciones pueden perderse

**Mejores Prácticas**:
```kotlin
// ✅ BUENO: Validar antes de transferir
fun validarAntesDeTransferir(target: ComponentName): Boolean {
    // 1. Verificar que app destino existe
    try {
        context.packageManager.getPackageInfo(target.packageName, 0)
    } catch (e: PackageManager.NameNotFoundException) {
        Log.e("Transfer", "❌ App destino no instalada")
        return false
    }
    
    // 2. Verificar que declara soporte
    try {
        val info = context.packageManager.getReceiverInfo(
            target,
            PackageManager.GET_META_DATA
        )
        val soportaTransfer = info.metaData?.getBoolean(
            "android.app.device_admin.SUPPORT_TRANSFER_OWNERSHIP",
            false
        ) ?: false
        
        if (!soportaTransfer) {
            Log.e("Transfer", "❌ App destino no soporta transferencia")
            return false
        }
    } catch (e: PackageManager.NameNotFoundException) {
        Log.e("Transfer", "❌ Receiver no encontrado")
        return false
    }
    
    // 3. Preparar bundle con configuraciones
    // 4. Mostrar confirmación al usuario
    
    return true
}

// ❌ MALO: Transferir sin validación
fun transferirSinValidar(target: ComponentName) {
    dpm.transferOwnership(admin, target, null)  // Puede fallar
}
```

### Identificadores del Dispositivo

**Enrollment ID**:
- ✅ Cambia tras factory reset (privacidad)
- ✅ Único por enrollment
- ✅ Disponible Android 12+
- ❌ No persistente tras re-enrollment

**Organization ID**:
- Identificador arbitrario de la empresa
- Útil para multi-tenancy
- Se usa con affiliation IDs

**Device ID (deprecated)**:
- ⚠️ No usar en Android 14+
- Reemplazado por Enrollment ID

### Financiamiento de Dispositivos

**Cuando marcar como financiado**:
- Dispositivos en leasing
- Programas de financiamiento corporativo
- Subsidios con obligaciones de devolución

**Políticas recomendadas**:
```kotlin
fun aplicarPoliticasDispositvoFinanciado() {
    // Prevenir factory reset
    dpm.setUserRestriction(admin, UserManager.DISALLOW_FACTORY_RESET)
    
    // Prevenir safe boot
    dpm.setUserRestriction(admin, UserManager.DISALLOW_SAFE_BOOT)
    
    // Bloquear desinstalación del MDM
    dpm.setUninstallBlocked(admin, context.packageName, true)
    
    // Mensaje en lockscreen
    dpm.setDeviceOwnerLockScreenInfo(admin, """
        Dispositivo financiado por Empresa Corp
        Obligaciones de pago pendientes
        No desinstalar apps de gestión
    """.trimIndent())
    
    // Información clara
    dpm.setShortSupportMessage(admin, 
        "Dispositivo financiado - Contacto: finance@empresa.com")
}
```

### Seguridad

**Validar siempre ownership**:
```kotlin
fun operacionCritica() {
    // Validar que somos admin antes de operaciones críticas
    if (!dpm.isDeviceOwnerApp(context.packageName) &&
        !dpm.isProfileOwnerApp(context.packageName)) {
        throw SecurityException("Operación requiere privilegios de administrador")
    }
    
    // Proceder con operación
}
```

**Proteger contra remoción**:
```kotlin
fun protegerAdmin() {
    if (dpm.isDeviceOwnerApp(context.packageName)) {
        // Bloquear factory reset
        dpm.setUserRestriction(admin, UserManager.DISALLOW_FACTORY_RESET)
        
        // Bloquear desinstalación
        dpm.setUninstallBlocked(admin, context.packageName, true)
        
        // Prevenir remoción por usuario
        dpm.setUserRestriction(admin, UserManager.DISALLOW_GRANT_ADMIN)
    }
}
```

---

## 📊 Matriz de Capacidades

| Capacidad | Device Owner | Profile Owner |
|-----------|--------------|---------------|
| Factory reset | ✅ | ❌ |
| Crear usuarios | ✅ | ❌ |
| Global settings | ✅ | ❌ |
| Secure settings | ✅ | ✅ (perfil) |
| System settings | ✅ | ✅ (perfil) |
| Security logging | ✅ | ✅ (si afiliado) |
| Network logging | ✅ | ✅ (si afiliado) |
| Lockdown mode | ✅ | ❌ |
| Status bar | ✅ | ❌ |
| Reboot | ✅ | ❌ |
| Transfer ownership | ✅ | ✅ |
| Affiliation | ✅ | ✅ |

---

## 📚 Próxima Categoría

**14. Bluetooth y NFC** (~8 métodos)

Esta categoría cubrirá:
- Control de Bluetooth
- Bluetooth contact sharing
- Preferencias de red Bluetooth
- Control de NFC
- Pagos NFC predeterminados

---

*Documentación generada para DevicePolicyManager API de Android Enterprise - Categoría 13 de 22*

