# Documentación DevicePolicyManager - Categoría 17: Device Identifiers

## 📋 Visión General

La categoría de **Device Identifiers** agrupa aproximadamente **5 métodos** que permiten obtener identificadores únicos del dispositivo para tracking, inventario y gestión de flotas. Son esenciales para sistemas MDM que necesitan identificar y rastrear dispositivos corporativos.

## 🎯 Propósito

Estos métodos permiten:
- **Identificar dispositivos** únicamente en sistemas MDM
- **Rastrear inventario** de activos corporativos
- **Vincular dispositivos** a usuarios/departamentos
- **Auditoría y compliance** por dispositivo
- **Gestión de flotas** empresariales

---

## 🆔 Conceptos Fundamentales

### Tipos de Identificadores en Android

**Identificadores Disponibles**:
```
Identificadores Permanentes (Hardware):
├── IMEI/MEID → Identificador del módem
├── Serial Number → Número de serie del dispositivo
├── MAC Address (WiFi) → Dirección física WiFi
└── MAC Address (Bluetooth) → Dirección física BT

Identificadores de Software:
├── Android ID → Cambia por usuario/app (no único)
├── SSAID → Android ID legacy (deprecado)
└── Advertising ID → Para publicidad (usuario puede resetear)

Identificadores MDM/EMM:
├── Enrollment ID → Único por enrollment, cambia tras reset
└── Device ID → Deprecated en Android 14
```

**Privacidad y Restricciones**:
```
Android 10+ (API 29):
├── IMEI/Serial → Requieren permisos especiales
├── MAC Address → Randomizado por defecto
└── Identificadores permanentes → Restringidos

Solución Corporativa:
└── Enrollment-specific ID → Provisto por DPM
```

---

## 📱 Método Principal

### `getEnrollmentSpecificId()`

**Propósito**: Obtiene un identificador único específico para este enrollment del dispositivo. Es el método recomendado para identificar dispositivos en sistemas MDM modernos.

**Disponibilidad**: Android 12+ (API 31)

**Características**:
- ✅ Único por dispositivo Y enrollment
- ✅ No expone identificadores de hardware
- ✅ Respeta privacidad del usuario
- ✅ Persistente durante el enrollment
- ❌ Cambia tras factory reset
- ❌ Cambia si se re-enrola el dispositivo
- ❌ Cambia si cambia el Device Owner

**Casos de Uso**:
- Tracking de flota corporativa
- Inventario de activos
- Auditoría por dispositivo
- Reportes de compliance
- Vinculación dispositivo-usuario en MDM

**Ejemplo de Uso**:
```kotlin
fun obtenerEnrollmentId(): String? {
    val enrollmentId = dpm.getEnrollmentSpecificId()
    
    if (enrollmentId != null) {
        Log.i("DeviceID", "📱 Enrollment ID: $enrollmentId")
        Log.i("DeviceID", "   Formato: String único")
        Log.i("DeviceID", "   Persistencia: Durante enrollment actual")
        Log.i("DeviceID", "   Cambia: Tras factory reset o re-enrollment")
    } else {
        Log.w("DeviceID", "❌ No disponible (Android < 12)")
    }
    
    return enrollmentId
}

// Registrar en sistema MDM
fun registrarEnMDM() {
    val enrollmentId = dpm.getEnrollmentSpecificId()
    
    if (enrollmentId != null) {
        val datosDispositivo = DispositivoInfo(
            enrollmentId = enrollmentId,
            modelo = Build.MODEL,
            fabricante = Build.MANUFACTURER,
            androidVersion = Build.VERSION.RELEASE,
            timestamp = System.currentTimeMillis()
        )
        
        enviarAServidorMDM(datosDispositivo)
        Log.i("MDM", "✅ Dispositivo registrado: $enrollmentId")
    }
}

data class DispositivoInfo(
    val enrollmentId: String,
    val modelo: String,
    val fabricante: String,
    val androidVersion: String,
    val timestamp: Long
)
```

**Comportamiento**:
```
Escenario: Dispositivo Factory Reset

Estado Inicial:
├── Enrollment ID: "abc123xyz789"
├── Registrado en MDM
└── Políticas aplicadas

Tras Factory Reset:
├── Enrollment ID: "def456uvw012" (NUEVO)
├── MDM lo ve como dispositivo diferente
└── Debe re-enrollarse

Ventaja:
└── Privacidad: No rastrea hardware físico
```

---

## 📶 Método de MAC Address

### `getWifiMacAddress(ComponentName admin)`

**Propósito**: Obtiene la dirección MAC del adaptador WiFi del dispositivo.

**Disponibilidad**: API 24+

**Restricciones**:
- Solo Device Owner puede llamar este método
- Android 10+ requiere permisos adicionales
- MAC puede estar randomizado en algunas implementaciones

**Casos de Uso**:
- Control de acceso a red corporativa (whitelist MAC)
- Identificación en sistemas de red
- Auditoría de conexiones WiFi
- Binding dispositivo a red específica

**Ejemplo de Uso**:
```kotlin
fun obtenerMACWiFi(): String? {
    try {
        val macAddress = dpm.getWifiMacAddress(admin)
        
        if (macAddress != null) {
            Log.i("Network", "📶 MAC WiFi: $macAddress")
            Log.i("Network", "   Formato: XX:XX:XX:XX:XX:XX")
        } else {
            Log.w("Network", "⚠️ MAC no disponible")
        }
        
        return macAddress
    } catch (e: SecurityException) {
        Log.e("Network", "❌ Sin permisos (requiere Device Owner)")
        return null
    }
}

// Registrar MAC en sistema de red corporativa
fun registrarEnSistemaRed() {
    val mac = dpm.getWifiMacAddress(admin)
    
    if (mac != null) {
        // Agregar a whitelist de red
        agregarAWhitelistWiFi(mac)
        
        Log.i("Network", "✅ MAC registrada en red corporativa")
    }
}
```

**⚠️ Limitaciones**:
```
Android 10+ (API 29):
├── MAC randomizado por defecto (privacidad)
├── MAC real solo disponible para Device Owner
└── Apps normales ven MAC aleatorio

Android 11+ (API 30):
├── Randomización más agresiva
└── getWifiMacAddress() puede devolver null

Recomendación:
└── Usar Enrollment ID en lugar de MAC para tracking
```

---

## 🚫 Método Deprecated

### `getDeviceId()` ⚠️ **DEPRECATED - API 34**

**Propósito**: Obtiene un identificador del dispositivo (IMEI en telefónos).

**Estado**: **Deprecated en Android 14 (API 34)**

**Reemplazo**: Usar `getEnrollmentSpecificId()` en su lugar

**Razón de Deprecación**:
- Exponía identificadores de hardware (privacidad)
- IMEI es información sensible
- No cumple con estándares modernos de privacidad

**Ejemplo (Solo Referencia)**:
```kotlin
@Deprecated("Usar getEnrollmentSpecificId() en su lugar")
fun obtenerDeviceIdDeprecated(): String? {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        @Suppress("DEPRECATION")
        return dpm.getDeviceId()
    } else {
        Log.w("DeviceID", "⚠️ getDeviceId() deprecated en Android 14+")
        return dpm.getEnrollmentSpecificId()
    }
}
```

---

## 🔗 Método de Organization ID

### `setOrganizationId(String organizationId)`

**Propósito**: Establece un identificador de organización asociado al dispositivo. No es un identificador único del dispositivo, sino de la organización propietaria.

**Uso**: Multi-tenancy en sistemas MDM

**Ejemplo de Uso**:
```kotlin
fun establecerOrganizacionId() {
    val orgId = "empresa-corp-2024-${UUID.randomUUID()}"
    
    dpm.setOrganizationId(orgId)
    
    Log.i("OrgID", "🏢 Organization ID: $orgId")
}

// Usar junto con affiliation IDs
fun configurarOrganizacion(nombreEmpresa: String) {
    // 1. Organization ID único
    val orgId = generarOrgId(nombreEmpresa)
    dpm.setOrganizationId(orgId)
    
    // 2. Affiliation IDs
    val affiliationIds = setOf(orgId, nombreEmpresa.lowercase())
    dpm.setAffiliationIds(admin, affiliationIds)
    
    Log.i("OrgID", "Organización configurada: $nombreEmpresa")
}

fun generarOrgId(nombre: String): String {
    return "${nombre.lowercase()}-${UUID.randomUUID()}"
}
```

---

## 📊 Comparación de Identificadores

| Identificador | Único | Persistente | Privacidad | Disponibilidad | Uso Recomendado |
|---------------|-------|-------------|------------|----------------|-----------------|
| **Enrollment ID** | ✅ Por enrollment | Durante enrollment | ✅ Alta | Android 12+ | **✅ MDM Moderno** |
| **WiFi MAC** | ✅ Por hardware | ✅ Permanente | ⚠️ Media | Device Owner | Whitelist red |
| **Device ID** | ✅ Por hardware | ✅ Permanente | ❌ Baja | **Deprecated** | ❌ No usar |
| **Android ID** | ❌ Por app/usuario | Hasta uninstall | ✅ Alta | Siempre | Apps normales |
| **Organization ID** | ❌ Manual | Manual | ✅ Alta | Siempre | Multi-tenancy |

---

## 🎯 Caso de Uso: Sistema MDM Completo

```kotlin
class MDMDeviceManager(
    private val context: Context,
    private val dpm: DevicePolicyManager,
    private val admin: ComponentName
) {
    
    fun registrarDispositivoCompleto(): DispositivoRegistro {
        // 1. Obtener Enrollment ID (principal)
        val enrollmentId = dpm.getEnrollmentSpecificId()
            ?: throw IllegalStateException("Enrollment ID no disponible")
        
        // 2. Obtener MAC WiFi (secundario)
        val macWiFi = dpm.getWifiMacAddress(admin)
        
        // 3. Información del dispositivo
        val info = DispositivoRegistro(
            enrollmentId = enrollmentId,
            macWiFi = macWiFi,
            modelo = Build.MODEL,
            fabricante = Build.MANUFACTURER,
            androidVersion = Build.VERSION.RELEASE,
            sdkVersion = Build.VERSION.SDK_INT,
            serialNumber = obtenerSerialSafe(),
            organizacionId = obtenerOrganizacionId()
        )
        
        // 4. Enviar a servidor MDM
        enviarAServidorMDM(info)
        
        // 5. Guardar localmente
        guardarLocalmente(info)
        
        return info
    }
    
    private fun obtenerSerialSafe(): String? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Build.getSerial()
            } else {
                @Suppress("DEPRECATION")
                Build.SERIAL
            }
        } catch (e: SecurityException) {
            null
        }
    }
    
    private fun obtenerOrganizacionId(): String? {
        // Obtener del storage local si fue configurado
        return context.getSharedPreferences("mdm", Context.MODE_PRIVATE)
            .getString("organization_id", null)
    }
}

data class DispositivoRegistro(
    val enrollmentId: String,
    val macWiFi: String?,
    val modelo: String,
    val fabricante: String,
    val androidVersion: String,
    val sdkVersion: Int,
    val serialNumber: String?,
    val organizacionId: String?
)
```

---

## ⚠️ Consideraciones Importantes

### Privacidad y Cumplimiento

**GDPR Compliance**:
- Enrollment ID: ✅ Cumple (no identifica persona física)
- MAC Address: ⚠️ Puede considerarse dato personal
- IMEI/Serial: ❌ Identificador de hardware (dato personal)

**Recomendaciones**:
- Usar Enrollment ID como identificador principal
- Evitar almacenar IMEI/Serial a menos que sea necesario
- Documentar en política de privacidad
- Permitir que usuario vea qué datos se recopilan

### Migración desde Device ID

**Para apps que usaban getDeviceId()**:
```
Antes (Android < 14):
└── getDeviceId() → IMEI/Serial

Ahora (Android 14+):
└── getEnrollmentSpecificId() → ID único por enrollment

Consideraciones:
├── IDs existentes en BD deben migrarse
├── Dispositivos re-enrollados tendrán nuevo ID
├── Mantener tabla de mapeo si es necesario
└── Actualizar lógica de tracking
```

### Persistencia de Datos

**Enrollment ID cambia cuando**:
- Factory reset
- Cambio de Device Owner
- Re-enrollment del dispositivo
- Migración entre sistemas MDM

**Solución**:
```
Estrategia de Re-identificación:
├── Guardar metadata adicional (MAC, Serial, usuario)
├── Permitir re-asociación manual en servidor MDM
├── Usar certificados para continuidad
└── Implementar proceso de re-registro
```

### Limitaciones por Versión Android

```
Android < 12 (API < 31):
├── getEnrollmentSpecificId() NO disponible
├── Usar getDeviceId() (deprecated)
└── Considerar identificadores alternativos

Android 12-13:
├── getEnrollmentSpecificId() disponible ✅
└── getDeviceId() funcional pero deprecated

Android 14+:
├── getEnrollmentSpecificId() recomendado ✅
└── getDeviceId() deprecated oficialmente ⚠️
```

---

## 💡 Recomendaciones

### Identificación Primaria
- **Usar siempre** `getEnrollmentSpecificId()` como identificador principal en Android 12+
- **Fallback** a identificadores secundarios solo si es absolutamente necesario
- **No usar** IMEI/Serial directamente si enrollment ID está disponible

### Registro en MDM
- **Registrar inmediatamente** tras enrollment
- **Incluir metadata** (modelo, versión Android, usuario)
- **Timestamp** de registro para auditoría
- **Actualizar** periódicamente para detectar cambios

### Manejo de Cambios
- **Detectar** cuando enrollment ID cambia (indica re-enrollment)
- **Proceso de re-registro** automatizado
- **Notificar** a servidor MDM del cambio
- **Mantener historial** de enrollments previos si es requerido

### Privacidad
- **Minimizar** recopilación de identificadores de hardware
- **Documentar** qué identificadores se usan y por qué
- **Permitir** que usuario vea datos recopilados
- **Eliminar** datos tras de-enrollment

### Multi-Tenancy
- **Organization ID** para distinguir entre clientes en MDM
- **Combinar** con affiliation IDs para correlación
- **Namespace** claro para evitar colisiones

---

## 📊 Auditoría de Identificadores

```kotlin
fun auditarIdentificadores(): String {
    return buildString {
        append("🆔 IDENTIFICADORES DEL DISPOSITIVO\n")
        append("═══════════════════════════════════════\n\n")
        
        // Enrollment ID
        append("📱 ENROLLMENT ID\n")
        val enrollmentId = dpm.getEnrollmentSpecificId()
        if (enrollmentId != null) {
            append("   ID: $enrollmentId\n")
            append("   Estado: ✅ Disponible\n")
            append("   Persistencia: Durante enrollment actual\n")
        } else {
            append("   Estado: ❌ No disponible (Android < 12)\n")
        }
        append("\n")
        
        // WiFi MAC
        append("📶 WIFI MAC ADDRESS\n")
        val macWiFi = try { dpm.getWifiMacAddress(admin) } catch (e: Exception) { null }
        if (macWiFi != null) {
            append("   MAC: $macWiFi\n")
            append("   Estado: ✅ Disponible\n")
        } else {
            append("   Estado: ❌ No disponible\n")
        }
        append("\n")
        
        // Device Info
        append("📋 INFORMACIÓN DEL DISPOSITIVO\n")
        append("   Fabricante: ${Build.MANUFACTURER}\n")
        append("   Modelo: ${Build.MODEL}\n")
        append("   Android: ${Build.VERSION.RELEASE}\n")
        append("   SDK: ${Build.VERSION.SDK_INT}\n")
        
        append("\n")
        append("Generado: ${Date()}\n")
    }
}
```

---

## 📚 Próxima Categoría

**18. Factory Reset Protection** (~3 métodos)

Esta categoría cubrirá:
- Factory Reset Protection Policy
- Configuración de FRP
- Prevención de theft/pérdida
- Recuperación de dispositivos

---

*Documentación generada para DevicePolicyManager API de Android Enterprise - Categoría 17 de 22*