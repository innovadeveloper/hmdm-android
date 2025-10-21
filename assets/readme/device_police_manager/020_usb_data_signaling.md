# Documentación DevicePolicyManager - Categoría 20: USB Data Signaling

## 📋 Visión General

La categoría de **USB Data Signaling** agrupa aproximadamente **3 métodos** que permiten controlar la señalización de datos a través del puerto USB del dispositivo. Esta funcionalidad previene la exfiltración de datos y ataques mediante conexiones USB no autorizadas.

## 🎯 Propósito

Estos métodos permiten:
- **Bloquear transferencia** de datos por USB
- **Prevenir exfiltración** de información sensible
- **Proteger contra ataques** USB (BadUSB, Juice Jacking)
- **Permitir solo carga** sin datos
- **Control granular** de puerto físico

---

## 🔌 Conceptos Fundamentales

### USB Data Signaling

**Qué es**:
```
USB Data Signaling:
├── Comunicación de datos a través del puerto USB
├── Incluye: MTP, PTP, ADB, Tethering, transferencia de archivos
├── Separado de: Carga de batería (puede seguir funcionando)
└── Control: Habilitar/deshabilitar señalización de datos

Modos USB comunes:
├── MTP (Media Transfer Protocol) → Transferir archivos
├── PTP (Picture Transfer Protocol) → Transferir fotos
├── ADB (Android Debug Bridge) → Debugging/desarrollo
├── Tethering → Compartir internet
└── MIDI → Instrumentos musicales
```

**Vectores de Ataque USB**:
```
Riesgos de Seguridad:
├── Exfiltración de datos → Copiar archivos sensibles
├── BadUSB → USB malicioso que se hace pasar por teclado/ratón
├── Juice Jacking → Robo de datos mientras carga en público
├── ADB sin autorización → Acceso debug no autorizado
└── Malware injection → Instalación de software malicioso

Protección:
└── Deshabilitar USB Data Signaling → Solo carga
```

---

## 🔐 Métodos de Control USB

### `setUsbDataSignalingEnabled(boolean enabled)`

**Propósito**: Habilita o deshabilita la señalización de datos USB en el dispositivo. Cuando está deshabilitada, el puerto USB solo puede usarse para carga de batería.

**Disponibilidad**: Android 14+ (API 34)

**Requisitos**: Device Owner

**Casos de Uso**:
- **Seguridad máxima**: Dispositivos con información clasificada
- **Prevención DLP**: Evitar copia de datos corporativos
- **Ambientes seguros**: Hospitales, bancos, gobierno
- **Anti-exfiltración**: Proteger propiedad intelectual
- **Kioscos públicos**: Prevenir conexiones no autorizadas

**Ejemplo de Uso**:
```kotlin
@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
fun deshabilitarDatosUSB() {
    dpm.setUsbDataSignalingEnabled(false)
    
    Log.i("USB", "🔌❌ USB Data Signaling DESHABILITADO")
    Log.i("USB", "   Puerto USB: Solo carga")
    Log.i("USB", "   Bloqueado:")
    Log.i("USB", "   - Transferencia de archivos (MTP/PTP)")
    Log.i("USB", "   - ADB (Android Debug Bridge)")
    Log.i("USB", "   - USB Tethering")
    Log.i("USB", "   - MIDI")
    Log.i("USB", "   ✅ Carga de batería: Funciona normalmente")
}

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
fun habilitarDatosUSB() {
    dpm.setUsbDataSignalingEnabled(true)
    
    Log.i("USB", "🔌✅ USB Data Signaling HABILITADO")
    Log.i("USB", "   Funcionalidad completa USB disponible")
}

// Control contextual basado en ubicación
@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
fun controlarUSBPorUbicacion(enAreaSegura: Boolean) {
    if (enAreaSegura) {
        // En área segura: bloquear datos USB
        dpm.setUsbDataSignalingEnabled(false)
        
        mostrarNotificacion(
            "USB Restringido",
            "Datos USB deshabilitados en área segura. Solo carga disponible."
        )
    } else {
        // Fuera de área segura: permitir datos USB
        dpm.setUsbDataSignalingEnabled(true)
        
        mostrarNotificacion(
            "USB Completo",
            "Funcionalidad USB completa disponible"
        )
    }
}

// Control por horario (seguridad nocturna)
@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
fun controlarUSBPorHorario() {
    val hora = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    
    // Horario laboral: 8 AM - 6 PM
    val esHorarioLaboral = hora in 8..18
    
    dpm.setUsbDataSignalingEnabled(esHorarioLaboral)
    
    if (esHorarioLaboral) {
        Log.i("USB", "☀️ Horario laboral - USB habilitado")
    } else {
        Log.i("USB", "🌙 Fuera de horario - USB solo carga")
    }
}
```

**Comportamiento del Sistema**:
```
USB Data Signaling DESHABILITADO:

Usuario conecta cable USB a PC:
├── Carga de batería → ✅ Funciona
├── Transferencia de archivos → ❌ No disponible
├── PC no detecta dispositivo como almacenamiento
├── ADB → ❌ Bloqueado
├── USB Tethering → ❌ Bloqueado
├── MIDI → ❌ Bloqueado
└── Usuario ve mensaje: "Carga únicamente"

Seguridad:
├── Previene BadUSB
├── Previene Juice Jacking
├── Previene exfiltración de datos
└── Carga segura en lugares públicos

USB Data Signaling HABILITADO:
├── Todas las funciones USB disponibles
├── Usuario puede elegir modo (MTP, PTP, etc.)
└── Funcionalidad estándar
```

**⚠️ Impacto en Desarrollo**:
```
Con USB Data deshabilitado:
❌ ADB no funciona
❌ Android Studio no puede conectar
❌ No se puede instalar apps vía USB
❌ No se puede hacer debugging
❌ logcat no accesible por USB

Alternativas para desarrollo:
✅ ADB over WiFi (si está habilitado)
✅ ADB over TCP/IP
✅ Wireless debugging (Android 11+)
✅ Habilitar USB temporalmente para desarrollo
```

---

### `isUsbDataSignalingEnabled()`

**Propósito**: Verifica si la señalización de datos USB está actualmente habilitada.

**Disponibilidad**: Android 14+ (API 34)

**Retorna**: `boolean`
- `true`: Datos USB habilitados (funcionalidad completa)
- `false`: Datos USB deshabilitados (solo carga)

**Ejemplo de Uso**:
```kotlin
@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
fun verificarEstadoUSB(): String {
    val habilitado = dpm.isUsbDataSignalingEnabled()
    
    return buildString {
        append("🔌 ESTADO USB DATA SIGNALING\n")
        append("═══════════════════════════════════\n\n")
        
        if (habilitado) {
            append("Estado: ✅ HABILITADO\n")
            append("Puerto USB: Funcionalidad completa\n\n")
            append("Disponible:\n")
            append("  ✅ Transferencia de archivos (MTP/PTP)\n")
            append("  ✅ ADB (Android Debug Bridge)\n")
            append("  ✅ USB Tethering\n")
            append("  ✅ MIDI\n")
            append("  ✅ Carga de batería\n\n")
            append("Seguridad: ⚠️ ESTÁNDAR\n")
            append("  Vulnerable a BadUSB, Juice Jacking\n")
        } else {
            append("Estado: 🔒 DESHABILITADO\n")
            append("Puerto USB: Solo carga\n\n")
            append("Bloqueado:\n")
            append("  ❌ Transferencia de archivos\n")
            append("  ❌ ADB\n")
            append("  ❌ USB Tethering\n")
            append("  ❌ MIDI\n")
            append("  ✅ Carga: Funciona normalmente\n\n")
            append("Seguridad: 🔒 ALTA\n")
            append("  Protegido contra ataques USB\n")
            append("  Previene exfiltración de datos\n")
        }
    }
}

// Monitoreo continuo del estado
@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
fun monitorearEstadoUSB() {
    val handler = Handler(Looper.getMainLooper())
    val runnable = object : Runnable {
        override fun run() {
            val habilitado = dpm.isUsbDataSignalingEnabled()
            Log.d("USB", "Estado USB Data: ${if (habilitado) "Habilitado" else "Deshabilitado"}")
            
            // Verificar nuevamente en 60 segundos
            handler.postDelayed(this, 60000)
        }
    }
    handler.post(runnable)
}
```

---

### `isUsbDataSignalingEnabledForUser()` / `canUsbDataSignalingBeDisabled()`

**Propósito**: Métodos complementarios para verificar estado y capacidad de deshabilitar USB data signaling.

**Disponibilidad**: Android 14+ (API 34)

**`isUsbDataSignalingEnabledForUser()`**:
- Verifica estado para usuario específico
- Útil en dispositivos multi-usuario

**`canUsbDataSignalingBeDisabled()`**:
- Verifica si el dispositivo soporta deshabilitar USB data signaling
- Algunos dispositivos/fabricantes pueden no soportar esta función

**Ejemplo de Uso**:
```kotlin
@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
fun verificarCapacidadUSB(): String {
    return buildString {
        append("🔍 CAPACIDADES USB\n")
        append("═══════════════════════════════════\n\n")
        
        // Verificar si se puede deshabilitar
        val puedeDeshabilitar = dpm.canUsbDataSignalingBeDisabled()
        
        append("Soporte: ")
        if (puedeDeshabilitar) {
            append("✅ SOPORTADO\n")
            append("Este dispositivo puede deshabilitar USB data\n\n")
            
            // Verificar estado actual
            val habilitado = dpm.isUsbDataSignalingEnabled()
            append("Estado actual: ${if (habilitado) "Habilitado" else "Deshabilitado"}\n")
        } else {
            append("❌ NO SOPORTADO\n")
            append("Este dispositivo/fabricante no soporta\n")
            append("deshabilitar USB data signaling\n\n")
            append("Razones posibles:\n")
            append("  • Limitación del fabricante\n")
            append("  • Hardware específico\n")
            append("  • ROM custom sin soporte\n")
        }
    }
}

// Validar antes de aplicar política
@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
fun aplicarPoliticaUSBSafe() {
    if (!dpm.canUsbDataSignalingBeDisabled()) {
        Log.w("USB", "⚠️ Dispositivo no soporta deshabilitar USB data")
        Log.w("USB", "   Usar restricciones alternativas:")
        Log.w("USB", "   - DISALLOW_USB_FILE_TRANSFER")
        Log.w("USB", "   - DISALLOW_DEBUGGING_FEATURES")
        return
    }
    
    // Dispositivo soporta la función
    dpm.setUsbDataSignalingEnabled(false)
    Log.i("USB", "✅ USB data signaling deshabilitado exitosamente")
}
```

---

## 🎯 Caso de Uso: Sistema de Seguridad Completo

```kotlin
@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
class USBSecurityManager(
    private val context: Context,
    private val dpm: DevicePolicyManager,
    private val admin: ComponentName
) {
    
    fun aplicarSeguridadMaximaUSB() {
        // 1. Verificar soporte
        if (!dpm.canUsbDataSignalingBeDisabled()) {
            Log.w("USB", "Dispositivo no soporta control USB data")
            aplicarFallbackUSB()
            return
        }
        
        // 2. Deshabilitar USB data signaling
        dpm.setUsbDataSignalingEnabled(false)
        
        // 3. Restricciones adicionales por si acaso
        dpm.addUserRestriction(admin, UserManager.DISALLOW_USB_FILE_TRANSFER)
        dpm.addUserRestriction(admin, UserManager.DISALLOW_DEBUGGING_FEATURES)
        
        // 4. Configurar monitoreo
        iniciarMonitoreoUSB()
        
        Log.i("Security", """
            🔒 SEGURIDAD USB MÁXIMA APLICADA
            ═══════════════════════════════════
            ✅ USB Data Signaling: DESHABILITADO
            ✅ File Transfer: BLOQUEADO
            ✅ ADB: BLOQUEADO
            
            Puerto USB: Solo carga
            Protección: Anti-exfiltración activa
        """.trimIndent())
    }
    
    private fun aplicarFallbackUSB() {
        // Si dispositivo no soporta USB data signaling,
        // usar restricciones tradicionales
        dpm.addUserRestriction(admin, UserManager.DISALLOW_USB_FILE_TRANSFER)
        dpm.addUserRestriction(admin, UserManager.DISALLOW_DEBUGGING_FEATURES)
        
        Log.i("Security", "Aplicadas restricciones USB fallback")
    }
    
    private fun iniciarMonitoreoUSB() {
        // Monitorear intentos de conexión USB
        val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        
        // Registrar broadcast receiver para eventos USB
        val filter = IntentFilter().apply {
            addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
            addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        }
        
        context.registerReceiver(usbReceiver, filter)
    }
    
    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                    Log.i("USB", "📱 Dispositivo USB conectado")
                    
                    if (!dpm.isUsbDataSignalingEnabled()) {
                        Log.i("USB", "   Solo carga - datos bloqueados")
                        notificarUsuario(
                            "USB Conectado",
                            "Solo carga disponible. Datos bloqueados por seguridad."
                        )
                    }
                }
                
                UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                    Log.i("USB", "📱 Dispositivo USB desconectado")
                }
            }
        }
    }
    
    fun habilitarTemporalmenteParaDesarrollo(duracionMinutos: Int) {
        if (!esAmbienteDesarrollo()) {
            Log.e("USB", "❌ No permitido en producción")
            return
        }
        
        // Habilitar temporalmente
        dpm.setUsbDataSignalingEnabled(true)
        Log.i("USB", "🔧 USB habilitado temporalmente para desarrollo")
        
        // Programar deshabilitación automática
        Handler(Looper.getMainLooper()).postDelayed({
            dpm.setUsbDataSignalingEnabled(false)
            Log.i("USB", "⏰ USB deshabilitado tras timeout")
        }, TimeUnit.MINUTES.toMillis(duracionMinutos.toLong()))
    }
    
    private fun esAmbienteDesarrollo(): Boolean {
        return BuildConfig.DEBUG && Build.FINGERPRINT.contains("test-keys")
    }
}
```

---

## ⚠️ Consideraciones Importantes

### Compatibilidad de Dispositivos

**Soporte Variable**:
```
Android 14+ (API 34):
├── API disponible en AOSP
├── Fabricantes pueden no implementar
├── Algunos dispositivos no soportan control hardware
└── Verificar siempre con canUsbDataSignalingBeDisabled()

Fabricantes con buen soporte:
├── Google Pixel
├── Samsung (modelos recientes)
└── Dispositivos Android One/Enterprise

Fabricantes con soporte limitado:
├── Algunos OEM chinos
├── Dispositivos económicos
└── ROMs custom sin parches completos
```

### Impacto en Usuarios

**Experiencia de Usuario**:
```
Con USB Data deshabilitado:
❌ No pueden transferir fotos/archivos por USB
❌ No pueden hacer backup por cable
❌ Conexión con PC más limitada
✅ Carga funciona normalmente
⚠️ Usuario puede frustrarse si no entiende por qué

Mitigación:
├── Comunicar claramente la política
├── Ofrecer alternativas (WiFi, cloud)
├── Explicar razones de seguridad
└── Habilitar temporalmente si es justificado
```

### Alternativas para Transferencia

**Con USB Data deshabilitado**:
```
Alternativas para usuarios:
├── Cloud storage (Drive, OneDrive, Dropbox)
├── WiFi Direct
├── Bluetooth (lento pero funcional)
├── Apps de transferencia inalámbrica
├── Email (archivos pequeños)
└── Portal web corporativo

Para IT/Desarrollo:
├── ADB over WiFi
├── Wireless debugging (Android 11+)
├── MDM push de apps
└── Habilitar USB temporalmente cuando necesario
```

### Seguridad en Capas

**USB Data como parte de estrategia DLP**:
```
Capa 1: Prevenir conexión física
└── setUsbDataSignalingEnabled(false)

Capa 2: Restricciones adicionales
├── DISALLOW_USB_FILE_TRANSFER
├── DISALLOW_DEBUGGING_FEATURES
└── DISALLOW_MOUNT_PHYSICAL_MEDIA

Capa 3: Monitoreo y alertas
├── Logs de intentos de conexión
├── Alertas a equipo de seguridad
└── Auditoría de eventos USB

Capa 4: Educación
├── Capacitar usuarios sobre riesgos
├── Políticas claras y comunicadas
└── Proceso para excepciones justificadas
```

---

## 💡 Recomendaciones

### Implementación
- **Verificar soporte** con `canUsbDataSignalingBeDisabled()` antes de aplicar
- **Combinar con restricciones** tradicionales como fallback
- **Comunicar claramente** a usuarios sobre limitaciones
- **Ofrecer alternativas** para transferir archivos (cloud, WiFi)

### Políticas por Industria
- **Financiero/Gobierno**: Deshabilitar siempre (seguridad máxima)
- **Salud (HIPAA)**: Deshabilitar para proteger PHI
- **Corporativo general**: Deshabilitar o controlar por contexto
- **Educación**: Generalmente permitir (menor riesgo)

### Excepciones
- **Desarrollo**: Habilitar temporalmente con timeout automático
- **IT Support**: Proceso claro para habilitar temporalmente
- **Testing**: Solo en dispositivos de desarrollo/QA
- **Usuarios especiales**: Con aprobación de seguridad

### Monitoreo
- **Registrar eventos** de conexión/desconexión USB
- **Alertar intentos** frecuentes de conexión
- **Auditoría regular** de excepciones concedidas
- **Dashboard** para visualizar estado de flota

### Educación de Usuarios
- **Explicar razones** de seguridad claramente
- **Documentar alternativas** para tareas comunes
- **Capacitación** sobre riesgos USB (BadUSB, Juice Jacking)
- **Proceso claro** para solicitar excepciones

### Balance Seguridad/Productividad
- **Evaluar necesidad real** de acceso USB por rol
- **Control contextual** (ubicación, horario) si es posible
- **Revisión periódica** de políticas
- **Feedback de usuarios** para ajustar balance

---

## 📊 Auditoría USB

```kotlin
@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
fun generarReporteUSB(): String {
    return buildString {
        append("═══════════════════════════════════════════\n")
        append("        REPORTE USB DATA SIGNALING\n")
        append("═══════════════════════════════════════════\n\n")
        
        // Soporte del dispositivo
        append("📱 SOPORTE DEL DISPOSITIVO\n")
        append("───────────────────────────────────────────\n")
        
        val soportado = dpm.canUsbDataSignalingBeDisabled()
        append("Control USB Data: ${if (soportado) "✅ SOPORTADO" else "❌ NO SOPORTADO"}\n")
        
        if (!soportado) {
            append("\n⚠️ Este dispositivo no puede controlar USB data\n")
            append("Usar restricciones alternativas:\n")
            append("  • DISALLOW_USB_FILE_TRANSFER\n")
            append("  • DISALLOW_DEBUGGING_FEATURES\n")
            return@buildString
        }
        
        append("\n")
        
        // Estado actual
        append("🔌 ESTADO ACTUAL\n")
        append("───────────────────────────────────────────\n")
        
        val habilitado = dpm.isUsbDataSignalingEnabled()
        append("USB Data: ${if (habilitado) "✅ HABILITADO" else "🔒 DESHABILITADO"}\n")
        
        if (habilitado) {
            append("\nFuncionalidad disponible:\n")
            append("  ✅ Transferencia de archivos (MTP/PTP)\n")
            append("  ✅ ADB (Android Debug Bridge)\n")
            append("  ✅ USB Tethering\n")
            append("  ✅ MIDI\n")
            append("  ✅ Carga de batería\n\n")
            append("Nivel de seguridad: ⚠️ ESTÁNDAR\n")
            append("Riesgo: Vulnerable a ataques USB\n")
        } else {
            append("\nPuerto USB:\n")
            append("  ✅ Carga de batería: Disponible\n")
            append("  ❌ Transferencia de archivos: Bloqueada\n")
            append("  ❌ ADB: Bloqueado\n")
            append("  ❌ USB Tethering: Bloqueado\n")
            append("  ❌ MIDI: Bloqueado\n\n")
            append("Nivel de seguridad: 🔒 ALTO\n")
            append("Protección: Anti-exfiltración activa\n")
        }
        
        append("\n")
        
        // Restricciones adicionales
        append("🚫 RESTRICCIONES ADICIONALES\n")
        append("───────────────────────────────────────────\n")
        
        val restrictions = dpm.getUserRestrictions(admin)
        val usbFileTransfer = restrictions.getBoolean(UserManager.DISALLOW_USB_FILE_TRANSFER, false)
        val debugging = restrictions.getBoolean(UserManager.DISALLOW_DEBUGGING_FEATURES, false)
        
        append("USB File Transfer: ${if (usbFileTransfer) "🔒 BLOQUEADO" else "✅ Permitido"}\n")
        append("Debugging Features: ${if (debugging) "🔒 BLOQUEADO" else "✅ Permitido"}\n")
        
        append("\n")
        
        // Recomendaciones
        append("💡 RECOMENDACIONES\n")
        append("───────────────────────────────────────────\n")
        
        if (habilitado && !usbFileTransfer && !debugging) {
            append("⚠️ ADVERTENCIA DE SEGURIDAD\n")
            append("USB completamente abierto sin restricciones\n")
            append("Recomendación: Deshabilitar USB data o agregar restricciones\n")
        } else if (!habilitado && (usbFileTransfer || debugging)) {
            append("✅ SEGURIDAD ÓPTIMA\n")
            append("Múltiples capas de protección USB activas\n")
        }
        
        append("\n")
        append("Generado: ${Date()}\n")
    }
}
```

---

## 📚 Próxima Categoría

**21. Content Protection Policy** (~3 métodos)

Esta categoría cubrirá:
- Políticas de protección de contenido
- Control de captura de pantalla en apps específicas
- DRM y contenido protegido
- Prevención de grabación de contenido sensible

---

*Documentación generada para DevicePolicyManager API de Android Enterprise - Categoría 20 de 22*