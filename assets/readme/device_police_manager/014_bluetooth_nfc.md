# Documentación DevicePolicyManager - Categoría 14: Bluetooth y NFC

## 📋 Visión General

La categoría de **Bluetooth y NFC** agrupa aproximadamente **8 métodos** que permiten controlar la funcionalidad de Bluetooth y NFC (Near Field Communication) en dispositivos administrados. Estos controles son esenciales para seguridad, prevención de exfiltración de datos y gestión de conectividad inalámbrica.

## 🎯 Propósito

Estos métodos permiten:
- **Controlar Bluetooth contact sharing** entre perfiles
- **Configurar preferencias** de servicios de red
- **Gestionar pagos NFC** predeterminados
- **Prevenir exfiltración** de datos vía Bluetooth/NFC
- **Cumplir políticas** de seguridad corporativa

---

## 📱 Métodos de Control de Bluetooth

### `setBluetoothContactSharingDisabled(ComponentName admin, boolean disabled)`

**Propósito**: Controla si los contactos del perfil de trabajo pueden compartirse con dispositivos Bluetooth (por ejemplo, sistemas de manos libres de vehículos, auriculares con función de contactos).

**Alcance**: Solo Profile Owner (Work Profile)

**Casos de Uso**:

**Deshabilitar Compartir (disabled = true)**:
- **Privacidad corporativa**: Prevenir que contactos de trabajo aparezcan en dispositivos personales
- **DLP (Data Loss Prevention)**: Evitar fuga de información de contactos
- **Cumplimiento**: GDPR, HIPAA - proteger información de contacto sensible
- **Seguridad**: Prevenir acceso no autorizado a directorio corporativo

**Habilitar Compartir (disabled = false)**:
- **Productividad**: Permitir llamadas manos libres con acceso a contactos
- **Experiencia de usuario**: Funcionalidad completa en vehículos
- **Conveniencia**: Ver quién llama en dispositivos Bluetooth

**Ejemplo de Uso**:
```kotlin
// Bloquear compartir contactos de trabajo vía Bluetooth
fun bloquearCompartirContactosBluetooth() {
    dpm.setBluetoothContactSharingDisabled(admin, true)
    
    Log.i("Bluetooth", "📵 Compartir contactos vía Bluetooth BLOQUEADO")
    Log.i("Bluetooth", "   Contactos de trabajo no visibles en:")
    Log.i("Bluetooth", "   - Sistema manos libres del vehículo")
    Log.i("Bluetooth", "   - Auriculares con función de contactos")
    Log.i("Bluetooth", "   - Otros dispositivos Bluetooth")
}

// Permitir compartir contactos
fun permitirCompartirContactosBluetooth() {
    dpm.setBluetoothContactSharingDisabled(admin, false)
    
    Log.i("Bluetooth", "✅ Compartir contactos vía Bluetooth PERMITIDO")
}

// Control dinámico según ubicación
fun controlarSegunContexto(enAreaSegura: Boolean) {
    // En áreas seguras, bloquear
    // Fuera de áreas seguras, permitir para productividad
    dpm.setBluetoothContactSharingDisabled(admin, enAreaSegura)
    
    if (enAreaSegura) {
        mostrarNotificacion(
            "Modo Seguro",
            "Compartir contactos Bluetooth deshabilitado en esta área"
        )
    }
}
```

**Comportamiento**:
```
Cuando está BLOQUEADO:
❌ Contactos de trabajo no se sincronizan con sistema de auto
❌ Auriculares no pueden leer lista de contactos
❌ Smartwatch no recibe contactos del Work Profile
✅ Llamadas siguen funcionando (sin información de contacto)
✅ Contactos personales no afectados

Cuando está PERMITIDO:
✅ Contactos de trabajo disponibles en dispositivos Bluetooth
✅ Sistema de auto muestra nombre del llamante
✅ Auriculares pueden acceder a directorio
⚠️ Posible fuga de información corporativa
```

**⚠️ Importante**:
- Solo aplica a **Work Profile** (Profile Owner)
- No afecta a contactos personales del usuario
- Device Owner no puede usar este método (no tiene sentido sin Work Profile)
- No bloquea Bluetooth completamente, solo compartir contactos

---

### `getBluetoothContactSharingDisabled(ComponentName admin)`

**Propósito**: Verifica si compartir contactos vía Bluetooth está bloqueado.

**Retorna**: `boolean`
- `true`: Compartir contactos bloqueado
- `false`: Compartir contactos permitido

```kotlin
fun verificarEstadoBluetoothSharing(): String {
    val bloqueado = dpm.getBluetoothContactSharingDisabled(admin)
    
    return buildString {
        append("📱 BLUETOOTH CONTACT SHARING\n")
        append("═══════════════════════════════════\n\n")
        
        if (bloqueado) {
            append("🔒 Estado: BLOQUEADO\n")
            append("   Contactos de trabajo NO se comparten\n")
            append("   Protección de privacidad activa\n\n")
            append("Impacto:\n")
            append("   ❌ No visible en sistema de auto\n")
            append("   ❌ No accesible desde auriculares\n")
            append("   ✅ Previene fuga de información\n")
        } else {
            append("✅ Estado: PERMITIDO\n")
            append("   Contactos de trabajo se pueden compartir\n")
            append("   Funcionalidad completa\n\n")
            append("Disponible en:\n")
            append("   ✅ Sistema manos libres de vehículo\n")
            append("   ✅ Auriculares Bluetooth\n")
            append("   ✅ Dispositivos emparejados\n")
        }
    }
}
```

---

## 🌐 Métodos de Preferencias de Red

### `setPreferentialNetworkServiceConfigs(List<PreferentialNetworkServiceConfig> configs)`

**Propósito**: Configura preferencias de servicios de red para diferentes tipos de tráfico, incluyendo Bluetooth. Permite priorizar o rutear tráfico específico a través de redes específicas.

**Disponibilidad**: Android 14+ (API 34)

**Casos de Uso**:
- **Priorización de tráfico**: VoIP sobre Bluetooth de alta calidad
- **Separación de tráfico**: Datos corporativos vs personales en diferentes rutas
- **Optimización**: Bluetooth A2DP para audio, otro protocolo para datos
- **QoS**: Garantizar calidad de servicio para aplicaciones críticas

**Ejemplo de Uso**:
```kotlin
@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
fun configurarPreferenciasRed() {
    // Configurar preferencias para diferentes tipos de servicios
    val configs = listOf(
        PreferentialNetworkServiceConfig.Builder()
            .setNetworkId(1)
            .setIncludedUids(intArrayOf(obtenerUidAppVoIP()))
            .setFallbackToDefaultConnectionAllowed(true)
            .build()
    )
    
    try {
        dpm.setPreferentialNetworkServiceConfigs(configs)
        Log.i("NetworkPrefs", "✅ Preferencias de red configuradas")
    } catch (e: Exception) {
        Log.e("NetworkPrefs", "❌ Error: ${e.message}")
    }
}
```

**Nota**: API muy específica y avanzada, principalmente para casos de uso enterprise especializados.

---

### `getPreferentialNetworkServiceConfigs()`

**Propósito**: Obtiene las configuraciones actuales de preferencias de servicios de red.

**Retorna**: `List<PreferentialNetworkServiceConfig>`

---

## 💳 Métodos de Control de NFC

### Control Indirecto de NFC

**Nota Importante**: DevicePolicyManager **no tiene métodos directos** específicos para controlar NFC como `setNfcEnabled()` o similar. El control de NFC se realiza principalmente a través de:

1. **UserManager Restrictions**:
```kotlin
// Deshabilitar NFC completamente
dpm.addUserRestriction(admin, UserManager.DISALLOW_NEAR_FIELD_COMMUNICATION_RADIO)

// Prevenir cambios en estado de NFC
dpm.addUserRestriction(admin, UserManager.DISALLOW_CHANGE_NEAR_FIELD_COMMUNICATION_RADIO)

// Bloquear Android Beam (deprecated en Android 10+)
dpm.addUserRestriction(admin, UserManager.DISALLOW_OUTGOING_BEAM)
```

2. **Settings.Secure** (Control indirecto):
```kotlin
// Nota: Settings de NFC son de solo lectura para DPM
// No se pueden modificar directamente, solo a través de restricciones
```

**Ejemplo de Control de NFC**:
```kotlin
fun gestionarNFC(habilitar: Boolean) {
    if (habilitar) {
        // Permitir NFC
        dpm.clearUserRestriction(admin, UserManager.DISALLOW_NEAR_FIELD_COMMUNICATION_RADIO)
        Log.i("NFC", "📡 NFC habilitado")
    } else {
        // Deshabilitar NFC
        dpm.addUserRestriction(admin, UserManager.DISALLOW_NEAR_FIELD_COMMUNICATION_RADIO)
        Log.i("NFC", "📡❌ NFC deshabilitado")
    }
}

// Bloquear cambios de usuario en NFC
fun bloquearCambiosNFC() {
    dpm.addUserRestriction(admin, UserManager.DISALLOW_CHANGE_NEAR_FIELD_COMMUNICATION_RADIO)
    Log.i("NFC", "🔒 Usuario no puede modificar estado de NFC")
}

// Control completo de NFC para seguridad
fun aplicarPoliticasNFCSeguras() {
    // Deshabilitar NFC completamente
    dpm.addUserRestriction(admin, UserManager.DISALLOW_NEAR_FIELD_COMMUNICATION_RADIO)
    
    // Prevenir que usuario lo active
    dpm.addUserRestriction(admin, UserManager.DISALLOW_CHANGE_NEAR_FIELD_COMMUNICATION_RADIO)
    
    // Bloquear Android Beam (si aplica)
    dpm.addUserRestriction(admin, UserManager.DISALLOW_OUTGOING_BEAM)
    
    Log.i("NFC", "🔒 Políticas de seguridad NFC aplicadas")
}
```

---

## 💳 Método de Pagos NFC

### Configuración en Settings.Secure

Aunque DevicePolicyManager no tiene un método específico `setNfcPaymentDefault()`, se puede configurar el componente de pago NFC predeterminado usando:

**Settings.Secure.NFC_PAYMENT_DEFAULT_COMPONENT**:
```kotlin
fun configurarPagoNFCPredeterminado(componentName: ComponentName) {
    try {
        // Configurar app de pago predeterminada
        dpm.setSecureSetting(
            admin,
            Settings.Secure.NFC_PAYMENT_DEFAULT_COMPONENT,
            componentName.flattenToString()
        )
        
        Log.i("NFCPayment", "💳 App de pago NFC configurada: ${componentName.packageName}")
        
    } catch (e: SecurityException) {
        Log.e("NFCPayment", "❌ Sin permisos: ${e.message}")
    }
}

// Configurar app corporativa de pagos
fun configurarPagoCorporativo() {
    val appPagoCorp = ComponentName(
        "com.empresa.wallet",
        "com.empresa.wallet.PaymentService"
    )
    
    configurarPagoNFCPredeterminado(appPagoCorp)
}

// Forzar Google Pay
fun configurarGooglePay() {
    val googlePay = ComponentName(
        "com.google.android.apps.walletnfcrel",
        "com.google.android.apps.walletnfcrel.service.HceService"
    )
    
    configurarPagoNFCPredeterminado(googlePay)
}

// Obtener app de pago actual
fun obtenerPagoNFCActual(): String? {
    return Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.NFC_PAYMENT_DEFAULT_COMPONENT
    )
}
```

---

## 🎯 Casos de Uso Completos por Escenario

### 🏦 Financiero/Bancario - Máxima Seguridad
```kotlin
fun configurarSeguridadFinanciera() {
    // 1. Bloquear compartir contactos vía Bluetooth
    dpm.setBluetoothContactSharingDisabled(admin, true)
    
    // 2. Deshabilitar Bluetooth completamente (vía restricciones)
    dpm.addUserRestriction(admin, UserManager.DISALLOW_BLUETOOTH)
    
    // 3. Deshabilitar NFC
    dpm.addUserRestriction(admin, UserManager.DISALLOW_NEAR_FIELD_COMMUNICATION_RADIO)
    dpm.addUserRestriction(admin, UserManager.DISALLOW_CHANGE_NEAR_FIELD_COMMUNICATION_RADIO)
    
    // 4. Bloquear compartir por Bluetooth
    dpm.addUserRestriction(admin, UserManager.DISALLOW_BLUETOOTH_SHARING)
    
    Log.i("Security", """
        🔒 SEGURIDAD FINANCIERA APLICADA
        ═══════════════════════════════════
        ❌ Bluetooth deshabilitado
        ❌ NFC deshabilitado
        ❌ Compartir contactos bloqueado
        ❌ Transferencias inalámbricas bloqueadas
        
        Justificación: Prevenir exfiltración de datos
        Cumplimiento: PCI-DSS, SOX
    """.trimIndent())
}
```

---

### 🏥 Médico/HIPAA - Protección PHI
```kotlin
fun configurarSeguridadMedica() {
    // 1. Bloquear compartir contactos (pueden contener info de pacientes)
    dpm.setBluetoothContactSharingDisabled(admin, true)
    
    // 2. Permitir Bluetooth pero bloquear transferencias
    dpm.addUserRestriction(admin, UserManager.DISALLOW_BLUETOOTH_SHARING)
    
    // 3. Bloquear NFC (prevenir transferencias accidentales)
    dpm.addUserRestriction(admin, UserManager.DISALLOW_NEAR_FIELD_COMMUNICATION_RADIO)
    
    // 4. Bloquear Android Beam
    dpm.addUserRestriction(admin, UserManager.DISALLOW_OUTGOING_BEAM)
    
    Log.i("Security", """
        🏥 SEGURIDAD MÉDICA/HIPAA APLICADA
        ═══════════════════════════════════
        ✅ Bluetooth permitido (para dispositivos médicos)
        ❌ Compartir contactos bloqueado (PHI)
        ❌ Transferencias Bluetooth bloqueadas
        ❌ NFC deshabilitado
        
        Cumplimiento: HIPAA, privacidad de pacientes
    """.trimIndent())
}
```

---

### 🏢 Corporativo Estándar - Balance Productividad/Seguridad
```kotlin
fun configurarCorporativoEstandar() {
    // 1. Permitir Bluetooth (productividad)
    dpm.clearUserRestriction(admin, UserManager.DISALLOW_BLUETOOTH)
    
    // 2. Bloquear compartir contactos corporativos
    dpm.setBluetoothContactSharingDisabled(admin, true)
    
    // 3. Permitir NFC pero configurar app de pago corporativa
    dpm.clearUserRestriction(admin, UserManager.DISALLOW_NEAR_FIELD_COMMUNICATION_RADIO)
    
    val appPagoCorp = ComponentName("com.empresa.wallet", "com.empresa.wallet.PaymentService")
    configurarPagoNFCPredeterminado(appPagoCorp)
    
    // 4. Bloquear compartir archivos por Bluetooth
    dpm.addUserRestriction(admin, UserManager.DISALLOW_BLUETOOTH_SHARING)
    
    Log.i("Security", """
        🏢 CONFIGURACIÓN CORPORATIVA ESTÁNDAR
        ═══════════════════════════════════════
        ✅ Bluetooth habilitado (productividad)
        ❌ Compartir contactos bloqueado
        ✅ NFC habilitado (pagos corporativos)
        ❌ Compartir archivos Bluetooth bloqueado
        
        Balance: Productividad + Seguridad
    """.trimIndent())
}
```

---

## ⚠️ Consideraciones Importantes

### Bluetooth Contact Sharing

**Limitaciones**:
- Solo aplica a **Profile Owner** (Work Profile)
- No funciona en Device Owner (no hay separación de perfiles)
- No bloquea Bluetooth completamente
- Solo afecta contactos del Work Profile

**Impacto en Usuario**:
```
Bloqueado:
❌ "Juan Pérez (Trabajo)" no aparece en sistema de auto
❌ Llamada entrante muestra solo número
❌ Auriculares no pueden buscar en directorio
⚠️ Usuario puede frustrarse por funcionalidad limitada

Permitido:
✅ Contactos visibles en todos los dispositivos Bluetooth
✅ Experiencia completa en vehículo
⚠️ Posible fuga de directorio corporativo
```

**Mejores Prácticas**:
- Evaluar riesgo vs beneficio por organización
- Considerar tipo de contactos (clientes vs colegas)
- Implementar políticas claras y comunicarlas
- Monitorear compliance

### Control de Bluetooth

**Métodos Disponibles**:

| Método | Granularidad | Uso |
|--------|--------------|-----|
| `DISALLOW_BLUETOOTH` | Bloqueo total | Seguridad máxima |
| `DISALLOW_CONFIG_BLUETOOTH` | Bloquea emparejar nuevos | Control moderado |
| `DISALLOW_BLUETOOTH_SHARING` | Bloquea transferencias | Solo archivos |
| `setBluetoothContactSharingDisabled()` | Bloquea contactos | Solo Work Profile |

**Estrategia Recomendada**:
```kotlin
fun aplicarPoliticasBluetoothGranulares(nivelSeguridad: NivelSeguridad) {
    when (nivelSeguridad) {
        NivelSeguridad.MAXIMA -> {
            // Deshabilitar completamente
            dpm.addUserRestriction(admin, UserManager.DISALLOW_BLUETOOTH)
        }
        
        NivelSeguridad.ALTA -> {
            // Permitir Bluetooth pero bloquear todo lo demás
            dpm.clearUserRestriction(admin, UserManager.DISALLOW_BLUETOOTH)
            dpm.addUserRestriction(admin, UserManager.DISALLOW_BLUETOOTH_SHARING)
            dpm.addUserRestriction(admin, UserManager.DISALLOW_CONFIG_BLUETOOTH)
            dpm.setBluetoothContactSharingDisabled(admin, true)
        }
        
        NivelSeguridad.MEDIA -> {
            // Permitir Bluetooth, bloquear solo contactos y archivos
            dpm.clearUserRestriction(admin, UserManager.DISALLOW_BLUETOOTH)
            dpm.addUserRestriction(admin, UserManager.DISALLOW_BLUETOOTH_SHARING)
            dpm.setBluetoothContactSharingDisabled(admin, true)
        }
        
        NivelSeguridad.BAJA -> {
            // Permitir todo
            dpm.clearUserRestriction(admin, UserManager.DISALLOW_BLUETOOTH)
            dpm.clearUserRestriction(admin, UserManager.DISALLOW_BLUETOOTH_SHARING)
            dpm.setBluetoothContactSharingDisabled(admin, false)
        }
    }
}

enum class NivelSeguridad {
    MAXIMA, ALTA, MEDIA, BAJA
}
```

### Control de NFC

**Limitaciones de API**:
- No hay método directo `setNfcEnabled()` en DPM
- Control principalmente vía UserManager restrictions
- Configuración de pagos vía Settings.Secure
- No se puede controlar NFC por app (todo o nada)

**Casos de Uso por Industria**:

**Deshabilitar NFC**:
- 🏦 Finanzas: Prevenir skimming
- 🏛️ Gobierno: Seguridad clasificada
- 🏥 Salud: Proteger RFID médico
- 🏭 Industrial: Evitar interferencias

**Habilitar NFC**:
- 🏪 Retail: Pagos sin contacto
- 🚇 Transporte: Pases de transporte
- 🏢 Oficinas: Control de acceso
- 📦 Logística: Escaneo de etiquetas

### Pagos NFC

**Apps de Pago Comunes**:
```kotlin
val appsNFCComunes = mapOf(
    "Google Pay" to ComponentName(
        "com.google.android.apps.walletnfcrel",
        "com.google.android.apps.walletnfcrel.service.HceService"
    ),
    "Samsung Pay" to ComponentName(
        "com.samsung.android.spay",
        "com.samsung.android.spay.service.HCEService"
    ),
    "Apple Pay" to null, // No disponible en Android
)
```

**Validación**:
```kotlin
fun validarAppPagoInstalada(component: ComponentName): Boolean {
    return try {
        context.packageManager.getServiceInfo(component, 0)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        Log.e("NFCPayment", "App de pago no instalada: ${component.packageName}")
        false
    }
}
```

### Exfiltración de Datos

**Vectores de Ataque**:
```
Bluetooth:
├── Compartir contactos → Directorio corporativo
├── Transferencia de archivos → Documentos sensibles
├── Emparejamiento malicioso → Man-in-the-middle
└── Audio → Grabaciones de conversaciones

NFC:
├── Android Beam → Transferencia de archivos
├── Pagos → Información financiera
├── Etiquetas → Datos almacenados
└── Card emulation → Credenciales
```

**Matriz de Mitigación**:

| Vector | Restricción | Efectividad |
|--------|-------------|-------------|
| Contactos BT | `setBluetoothContactSharingDisabled()` | 🟢 Alta |
| Archivos BT | `DISALLOW_BLUETOOTH_SHARING` | 🟢 Alta |
| Emparejamiento | `DISALLOW_CONFIG_BLUETOOTH` | 🟡 Media |
| NFC general | `DISALLOW_NEAR_FIELD_COMMUNICATION_RADIO` | 🟢 Alta |
| Android Beam | `DISALLOW_OUTGOING_BEAM` | 🟢 Alta |

---

## 📊 Auditoría de Configuración

```kotlin
fun auditarConfiguracionBluetoothNFC(): String {
    return buildString {
        append("📡 AUDITORÍA BLUETOOTH Y NFC\n")
        append("═══════════════════════════════════════\n\n")
        
        // Bluetooth
        append("📶 BLUETOOTH\n")
        append("─────────────────────────────────────\n")
        
        val btContactSharing = try {
            dpm.getBluetoothContactSharingDisabled(admin)
        } catch (e: Exception) {
            null // No es Profile Owner
        }
        
        if (btContactSharing != null) {
            append("Compartir Contactos: ${if (btContactSharing) "🔒 BLOQUEADO" else "✅ Permitido"}\n")
        } else {
            append("Compartir Contactos: N/A (No es Profile Owner)\n")
        }
        
        val btRestrictions = listOf(
            UserManager.DISALLOW_BLUETOOTH to "Bluetooth completo",
            UserManager.DISALLOW_CONFIG_BLUETOOTH to "Configuración BT",
            UserManager.DISALLOW_BLUETOOTH_SHARING to "Compartir archivos"
        )
        
        btRestrictions.forEach { (key, nombre) ->
            val restringido = esRestriccionActiva(key)
            append("$nombre: ${if (restringido) "🔒 BLOQUEADO" else "✅ Permitido"}\n")
        }
        
        append("\n")
        
        // NFC
        append("📱 NFC\n")
        append("─────────────────────────────────────\n")
        
        val nfcRestrictions = listOf(
            UserManager.DISALLOW_NEAR_FIELD_COMMUNICATION_RADIO to "NFC Radio",
            UserManager.DISALLOW_CHANGE_NEAR_FIELD_COMMUNICATION_RADIO to "Cambiar NFC",
            UserManager.DISALLOW_OUTGOING_BEAM to "Android Beam"
        )
        
        nfcRestrictions.forEach { (key, nombre) ->
            val restringido = esRestriccionActiva(key)
            append("$nombre: ${if (restringido) "🔒 BLOQUEADO" else "✅ Permitido"}\n")
        }
        
        // App de pago NFC
        append("\nApp de Pago NFC: ")
        val pagoNFC = obtenerPagoNFCActual()
        if (pagoNFC != null) {
            append("$pagoNFC\n")
        } else {
            append("No configurada\n")
        }
        
        append("\n")
        append("Generado: ${Date()}\n")
    }
}

fun esRestriccionActiva(key: String): Boolean {
    val restrictions = dpm.getUserRestrictions(admin)
    return restrictions.getBoolean(key, false)
}
```

---

## 📚 Próxima Categoría

**15. Input Methods (Teclados)** (~5 métodos)

Esta categoría cubrirá:
- Métodos de entrada permitidos
- Control de teclados
- Servicios de accesibilidad
- IME restrictions

---

*Documentación generada para DevicePolicyManager API de Android Enterprise - Categoría 14 de 22*