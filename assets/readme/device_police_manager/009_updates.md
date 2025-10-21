# Documentación DevicePolicyManager - Categoría 9: Actualización del Sistema

## 📋 Visión General

La categoría de **Actualización del Sistema** agrupa aproximadamente **10 métodos** que permiten controlar cómo y cuándo los dispositivos administrados reciben e instalan actualizaciones del sistema operativo Android. Es fundamental para mantener la seguridad y estabilidad de la flota de dispositivos.

## 🎯 Propósito

Estos métodos permiten:
- **Configurar políticas** de actualización automática
- **Instalar actualizaciones** del sistema programáticamente
- **Consultar actualizaciones** pendientes
- **Programar reinicios** para aplicar actualizaciones
- **Controlar ventanas** de mantenimiento
- **Gestionar el ciclo** de actualización completo

---

## 📋 Conceptos Fundamentales

### Tipos de Actualizaciones

**OTA (Over-The-Air)**:
- Actualizaciones completas del sistema
- Parches de seguridad mensuales
- Actualizaciones de versión (Android 13 → 14)
- Distribuidas por fabricante/operador

**System Updates**:
- Actualizaciones del sistema base
- Google Play System Updates (Project Mainline)
- Actualizaciones de componentes modulares

### Modos de Instalación

| Modo | Descripción | Cuándo se Aplica |
|------|-------------|------------------|
| **Automatic** | Descarga e instala automáticamente | Inmediatamente disponible |
| **Windowed** | Instala en ventana de tiempo específica | Horario programado |
| **Postpone** | Pospone instalación por X días | Después del período |

---

## 📱 Métodos de Política de Actualización

### `setSystemUpdatePolicy(ComponentName admin, SystemUpdatePolicy policy)`

**Propósito**: Establece la política que controla cómo el dispositivo maneja las actualizaciones del sistema. Es el método central de esta categoría.

**Tipos de Políticas**:

**1. TYPE_INSTALL_AUTOMATIC (Automática)**
```kotlin
// Instalar actualizaciones inmediatamente al estar disponibles
val policy = SystemUpdatePolicy.createAutomaticInstallPolicy()
dpm.setSystemUpdatePolicy(admin, policy)

Log.i("Updates", "Política: Instalación AUTOMÁTICA")
// - Descarga en background
// - Instala sin intervención
// - Reinicia cuando sea seguro
// ⚠️ Puede interrumpir uso del dispositivo
```

**Casos de Uso**:
- Kioscos que se pueden reiniciar en cualquier momento
- Dispositivos de señalización digital
- Seguridad crítica: parches deben aplicarse inmediatamente
- Dispositivos que operan 24/7 pero con uso intermitente

---

**2. TYPE_INSTALL_WINDOWED (Ventana de Tiempo)**
```kotlin
// Instalar solo durante horario específico (ej: madrugada)
val policy = SystemUpdatePolicy.createWindowedInstallPolicy(
    120,  // startMinute: 2:00 AM (120 minutos desde medianoche)
    180   // endMinute: 3:00 AM (180 minutos desde medianoche)
)
dpm.setSystemUpdatePolicy(admin, policy)

Log.i("Updates", "Política: Instalación VENTANA 2:00-3:00 AM")
// - Descarga en cualquier momento
// - Instala SOLO en ventana 2-3 AM
// - Reinicia dentro de la ventana
// ✅ No interrumpe horario laboral
```

**Casos de Uso**:
- Dispositivos de oficina (actualizar de noche)
- POS que cierran en cierto horario
- Tablets educativas (actualizar fuera de clase)
- Dispositivos de trabajo con horario definido

**Ejemplo Avanzado - Múltiples Ventanas**:
```kotlin
// Actualizar en madrugada (2-4 AM) o tarde (9-11 PM)
val ventana1 = SystemUpdatePolicy.createWindowedInstallPolicy(
    120,  // 2:00 AM
    240   // 4:00 AM
)

// Nota: Solo se puede configurar UNA ventana con el método estándar
// Para ventanas múltiples, usar lógica de negocio externa
```

---

**3. TYPE_POSTPONE (Posponer)**
```kotlin
// Posponer actualizaciones por 30 días desde que estén disponibles
val policy = SystemUpdatePolicy.createPostponeInstallPolicy()
dpm.setSystemUpdatePolicy(admin, policy)

Log.i("Updates", "Política: POSPONER hasta acción manual")
// - Descarga pero NO instala automáticamente
// - Admin debe instalar manualmente
// - Usuario ve notificación de actualización disponible
// ⚠️ Puede dejar dispositivos sin parches de seguridad
```

**Casos de Uso**:
- Testing: Validar actualización antes de desplegar
- Dispositivos críticos donde estabilidad es prioritaria
- Control total sobre timing de actualizaciones
- Evitar actualizaciones problemáticas conocidas

**⚠️ ADVERTENCIA**: Posponer actualizaciones puede dejar dispositivos vulnerables. Solo usar si hay proceso de testing y aprobación.

---

**4. Remover Política (null)**
```kotlin
// Volver al comportamiento por defecto de Android
dpm.setSystemUpdatePolicy(admin, null)

Log.i("Updates", "Política: Por DEFECTO del sistema")
// - Comportamiento estándar de Android
// - Usuario controla cuándo instalar
// - Descarga automática, instalación manual
```

---

### `getSystemUpdatePolicy()`

**Propósito**: Obtiene la política de actualización actualmente configurada.

**Retorna**:
- `SystemUpdatePolicy`: Política configurada
- `null`: Sin política (comportamiento por defecto)

**Ejemplo de Uso**:
```kotlin
fun auditarPoliticaActualizaciones(): String {
    val policy = dpm.getSystemUpdatePolicy()
    
    return buildString {
        append("🔄 POLÍTICA DE ACTUALIZACIONES\n")
        append("═══════════════════════════════\n\n")
        
        if (policy == null) {
            append("📱 Política: POR DEFECTO\n")
            append("   Usuario controla instalación\n")
        } else {
            when (policy.policyType) {
                SystemUpdatePolicy.TYPE_INSTALL_AUTOMATIC -> {
                    append("⚡ Política: AUTOMÁTICA\n")
                    append("   Instala inmediatamente\n")
                    append("   ⚠️ Puede interrumpir uso\n")
                }
                
                SystemUpdatePolicy.TYPE_INSTALL_WINDOWED -> {
                    val start = policy.installWindowStart
                    val end = policy.installWindowEnd
                    val horaInicio = formatearMinutos(start)
                    val horaFin = formatearMinutos(end)
                    
                    append("🕐 Política: VENTANA DE TIEMPO\n")
                    append("   Ventana: $horaInicio - $horaFin\n")
                    append("   ✅ No interrumpe horario laboral\n")
                }
                
                SystemUpdatePolicy.TYPE_POSTPONE -> {
                    append("⏸️ Política: POSPONER\n")
                    append("   Instalación manual requerida\n")
                    append("   ⚠️ Parches de seguridad retrasados\n")
                }
            }
        }
    }
}

fun formatearMinutos(minutos: Int): String {
    val horas = minutos / 60
    val mins = minutos % 60
    return String.format("%02d:%02d", horas, mins)
}
```

---

## 🔄 Métodos de Instalación de Actualizaciones

### `installSystemUpdate(ComponentName admin, Uri updateFileUri, Executor executor, InstallSystemUpdateCallback callback)`

**Propósito**: Instala una actualización del sistema desde un archivo OTA local. Permite instalación programática sin esperar a la actualización OTA automática.

**Casos de Uso**:
- **Testing interno**: Instalar builds antes del lanzamiento público
- **Actualizaciones corporativas**: Distribuir versiones personalizadas
- **Hotfixes urgentes**: Instalar parches críticos inmediatamente
- **Control de versiones**: Mantener versión específica en toda la flota

**Flujo de Trabajo**:
```
1. Descargar archivo OTA (.zip) al dispositivo
2. Obtener Uri del archivo (content:// o file://)
3. Llamar installSystemUpdate()
4. Callbacks informan progreso
5. Dispositivo reinicia para aplicar
```

**Ejemplo de Uso**:
```kotlin
fun instalarActualizacionOTA(archivoOTA: File) {
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        archivoOTA
    )
    
    val executor = Executors.newSingleThreadExecutor()
    
    dpm.installSystemUpdate(
        admin,
        uri,
        executor,
        object : DevicePolicyManager.InstallSystemUpdateCallback() {
            
            override fun onInstallUpdateError(
                errorCode: Int,
                errorMessage: String
            ) {
                Log.e("OTA", "❌ Error al instalar: $errorCode - $errorMessage")
                
                when (errorCode) {
                    ERROR_FILE_NOT_FOUND -> {
                        Log.e("OTA", "Archivo OTA no encontrado")
                    }
                    ERROR_INCORRECT_OS_VERSION -> {
                        Log.e("OTA", "Versión de OS incompatible")
                    }
                    ERROR_UPDATE_FILE_INVALID -> {
                        Log.e("OTA", "Archivo OTA inválido o corrupto")
                    }
                    ERROR_UNKNOWN -> {
                        Log.e("OTA", "Error desconocido")
                    }
                }
                
                notificarErrorInstalacion(errorCode, errorMessage)
            }
        }
    )
    
    Log.i("OTA", "📦 Instalación de actualización iniciada")
    Log.i("OTA", "   Archivo: ${archivoOTA.name}")
    Log.i("OTA", "   Tamaño: ${archivoOTA.length() / 1024 / 1024} MB")
}
```

**Estados de la Instalación**:
- `onInstallUpdateError()`: Error durante instalación
    - `ERROR_FILE_NOT_FOUND` (1): Archivo no existe
    - `ERROR_INCORRECT_OS_VERSION` (2): Versión incompatible
    - `ERROR_UPDATE_FILE_INVALID` (3): Archivo corrupto/inválido
    - `ERROR_UNKNOWN` (5): Error desconocido

**⚠️ Consideraciones**:
- Archivo OTA debe estar firmado por el fabricante
- Versión debe ser compatible con hardware
- Proceso puede tardar 10-30 minutos
- Dispositivo reinicia automáticamente
- No se puede cancelar una vez iniciado

---

## 📊 Métodos de Consulta de Actualizaciones

### `getPendingSystemUpdate(ComponentName admin)`

**Propósito**: Obtiene información sobre actualizaciones del sistema pendientes de instalación.

**Retorna**:
- `SystemUpdateInfo`: Información de la actualización pendiente
- `null`: No hay actualizaciones pendientes

**Información Disponible**:
- Timestamp de recepción de la actualización
- Si es una actualización de seguridad

**Ejemplo de Uso**:
```kotlin
fun verificarActualizacionesPendientes(): String {
    val updateInfo = dpm.getPendingSystemUpdate(admin)
    
    return buildString {
        append("📋 ACTUALIZACIONES PENDIENTES\n")
        append("═══════════════════════════════\n\n")
        
        if (updateInfo == null) {
            append("✅ Sistema actualizado\n")
            append("   No hay actualizaciones pendientes\n")
        } else {
            val timestamp = Date(updateInfo.receivedTime)
            
            append("⏳ Actualización PENDIENTE\n")
            append("   Recibida: $timestamp\n")
            
            if (updateInfo.isSecurityPatch) {
                append("   Tipo: 🔒 PARCHE DE SEGURIDAD\n")
                append("   ⚠️ Instalación prioritaria recomendada\n")
            } else {
                append("   Tipo: 📦 Actualización del sistema\n")
            }
            
            // Calcular tiempo desde recepción
            val diasPendiente = (System.currentTimeMillis() - updateInfo.receivedTime) / 
                                (1000 * 60 * 60 * 24)
            
            append("   Pendiente: $diasPendiente día(s)\n")
            
            if (diasPendiente > 7) {
                append("   ⚠️ ADVERTENCIA: Actualización retrasada más de 7 días\n")
            }
        }
    }
}

// Verificar periódicamente y alertar
fun monitorearActualizacionesPendientes() {
    val updateInfo = dpm.getPendingSystemUpdate(admin)
    
    if (updateInfo != null) {
        val diasPendiente = (System.currentTimeMillis() - updateInfo.receivedTime) / 
                            (1000 * 60 * 60 * 24)
        
        when {
            diasPendiente > 30 -> {
                alertarCritico("Actualización pendiente más de 30 días")
            }
            diasPendiente > 7 && updateInfo.isSecurityPatch -> {
                alertarAlto("Parche de seguridad sin instalar más de 7 días")
            }
            diasPendiente > 3 -> {
                alertarMedio("Actualización pendiente $diasPendiente días")
            }
        }
    }
}
```

**Uso en Políticas de Cumplimiento**:
```kotlin
fun validarCumplimientoActualizaciones(): Boolean {
    val updateInfo = dpm.getPendingSystemUpdate(admin)
    
    // Regla: Parches de seguridad deben instalarse en 7 días
    if (updateInfo?.isSecurityPatch == true) {
        val diasPendiente = (System.currentTimeMillis() - updateInfo.receivedTime) / 
                            (1000 * 60 * 60 * 24)
        
        if (diasPendiente > 7) {
            Log.e("Compliance", "❌ Incumplimiento: Parche pendiente $diasPendiente días")
            return false
        }
    }
    
    return true
}
```

---

## 🔄 Métodos de Reinicio

### `reboot(ComponentName admin)`

**Propósito**: Reinicia el dispositivo inmediatamente. Se usa típicamente después de instalar actualizaciones o cambios de configuración que requieren reinicio.

**Casos de Uso**:
- **Aplicar actualizaciones**: Finalizar instalación de OTA
- **Cambios de configuración**: Políticas que requieren reinicio
- **Mantenimiento programado**: Reinicio preventivo periódico
- **Recuperación**: Reiniciar dispositivo que no responde

**Ejemplo de Uso**:
```kotlin
fun reiniciarDispositivo(razon: String) {
    try {
        Log.i("Reboot", "⚠️ Reiniciando dispositivo")
        Log.i("Reboot", "   Razón: $razon")
        
        // Notificar al usuario (si hay UI activa)
        mostrarNotificacion(
            "Reiniciando dispositivo",
            "El dispositivo se reiniciará en 5 segundos"
        )
        
        // Dar tiempo para guardar estados
        Thread.sleep(5000)
        
        // Reiniciar
        dpm.reboot(admin)
        
        // Este código nunca se ejecutará (dispositivo reinicia)
        
    } catch (e: SecurityException) {
        Log.e("Reboot", "❌ Sin permisos para reiniciar: ${e.message}")
    }
}

// Reiniciar en horario específico
fun programarReinicio(hora: Int, minuto: Int) {
    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hora)
        set(Calendar.MINUTE, minuto)
        set(Calendar.SECOND, 0)
        
        // Si la hora ya pasó hoy, programar para mañana
        if (timeInMillis < System.currentTimeMillis()) {
            add(Calendar.DAY_OF_YEAR, 1)
        }
    }
    
    val intent = Intent(context, RebootReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
        context, 0, intent, PendingIntent.FLAG_IMMUTABLE
    )
    
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    alarmManager.setExactAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP,
        calendar.timeInMillis,
        pendingIntent
    )
    
    Log.i("Reboot", "Reinicio programado para: ${calendar.time}")
}

class RebootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val admin = ComponentName(context, MyDeviceAdminReceiver::class.java)
        
        Log.i("Reboot", "🔄 Ejecutando reinicio programado")
        dpm.reboot(admin)
    }
}
```

**⚠️ ADVERTENCIAS**:
- **Acción inmediata e irreversible**
- Usuario pierde trabajo no guardado
- Apps en foreground se cierran abruptamente
- No hay confirmación ni cancelación
- Debe usarse con extrema precaución

**Mejores Prácticas**:
```kotlin
// ✅ BUENO: Notificar antes de reiniciar
fun reiniciarConAdvertencia() {
    AlertDialog.Builder(context)
        .setTitle("Reinicio Requerido")
        .setMessage("El dispositivo se reiniciará para aplicar actualizaciones")
        .setPositiveButton("Reiniciar ahora") { _, _ ->
            dpm.reboot(admin)
        }
        .setNegativeButton("Más tarde", null)
        .show()
}

// ❌ MALO: Reiniciar sin advertencia
fun reiniciarSinAdvertencia() {
    dpm.reboot(admin)  // Muy brusco para el usuario
}

// ✅ BUENO: Reiniciar en horario de bajo uso
fun reiniciarEnMadrugada() {
    programarReinicio(hora = 3, minuto = 0)  // 3:00 AM
}
```

---

## 🎯 Casos de Uso por Escenario

### 🏢 Corporativo Estándar - Oficinas
```kotlin
// Actualizar en ventana nocturna
val policy = SystemUpdatePolicy.createWindowedInstallPolicy(
    120,  // 2:00 AM
    240   // 4:00 AM
)
dpm.setSystemUpdatePolicy(admin, policy)

Log.i("Updates", """
    Política aplicada:
    - Descarga: Cualquier momento
    - Instalación: 2:00-4:00 AM
    - Reinicio: Dentro de ventana
    - Impacto: Ninguno en horario laboral
""".trimIndent())

// Monitoreo diario
verificarActualizacionesPendientes()
```

---

### 🏪 Kiosco/POS - Horario de Cierre
```kotlin
// Actualizar cuando tienda está cerrada
val policy = SystemUpdatePolicy.createWindowedInstallPolicy(
    1320,  // 10:00 PM (22:00)
    360    // 6:00 AM (siguiente día)
)
dpm.setSystemUpdatePolicy(admin, policy)

// Reinicio manual adicional semanal
programarReinicio(hora = 23, minuto = 0)  // Domingos a las 11 PM
```

---

### 🏭 Industrial/Manufactura - 24/7
```kotlin
// Posponer actualizaciones para testing
val policy = SystemUpdatePolicy.createPostponeInstallPolicy()
dpm.setSystemUpdatePolicy(admin, policy)

Log.w("Updates", """
    ⚠️ MODO MANUAL:
    - Actualizaciones NO se instalan automáticamente
    - Proceso de validación requerido
    - Instalación manual tras aprobación
    - Ventana de mantenimiento programada
""".trimIndent())

// Verificar actualizaciones cada semana
fun procesarActualizacionesManuales() {
    val updateInfo = dpm.getPendingSystemUpdate(admin)
    
    if (updateInfo != null) {
        // 1. Notificar al equipo de operaciones
        notificarEquipo("Actualización disponible para testing")
        
        // 2. Instalar en dispositivo de prueba
        instalarEnDispositivoPrueba()
        
        // 3. Tras validación exitosa (48-72h)
        if (testingExitoso()) {
            // 4. Programar instalación en ventana de mantenimiento
            programarInstalacionFlota(proximaVentanaMantenimiento())
        }
    }
}
```

---

### 🎓 Educativo - Período Escolar
```kotlin
// Actualizar solo en vacaciones o fines de semana
val policy = SystemUpdatePolicy.createWindowedInstallPolicy(
    1380,  // Sábado 11:00 PM
    420    // Domingo 7:00 AM
)
dpm.setSystemUpdatePolicy(admin, policy)

// Bloquear actualizaciones durante exámenes
fun bloquearActualizacionesDuranteExamenes(periodoExamen: Boolean) {
    if (periodoExamen) {
        // Cambiar a manual durante exámenes
        dpm.setSystemUpdatePolicy(admin, SystemUpdatePolicy.createPostponeInstallPolicy())
        Log.i("Updates", "🎓 Actualizaciones pausadas - Período de exámenes")
    } else {
        // Volver a política normal
        dpm.setSystemUpdatePolicy(admin, policyNormal)
        Log.i("Updates", "🎓 Actualizaciones resumidas - Período regular")
    }
}
```

---

### 🚑 Crítico/Emergencia - Máxima Disponibilidad
```kotlin
// Posponer TODO hasta ventana de mantenimiento planificada
val policy = SystemUpdatePolicy.createPostponeInstallPolicy()
dpm.setSystemUpdatePolicy(admin, policy)

// Alertar sobre actualizaciones de seguridad críticas
fun monitorearActualizacionesCriticas() {
    val updateInfo = dpm.getPendingSystemUpdate(admin)
    
    if (updateInfo?.isSecurityPatch == true) {
        val diasPendiente = (System.currentTimeMillis() - updateInfo.receivedTime) / 
                            (1000 * 60 * 60 * 24)
        
        when {
            diasPendiente > 14 -> {
                alertarCritico(
                    "URGENTE: Parche de seguridad pendiente $diasPendiente días",
                    "Programar ventana de mantenimiento INMEDIATAMENTE"
                )
            }
            diasPendiente > 7 -> {
                alertarAlto(
                    "Parche de seguridad pendiente $diasPendiente días",
                    "Considerar ventana de mantenimiento"
                )
            }
        }
    }
}

// Instalación controlada con mínima interrupción
fun instalarEnVentanaMantenimiento() {
    // 1. Notificar con 48h de anticipación
    notificarUsuarios("Mantenimiento programado para [fecha]")
    
    // 2. Confirmar disponibilidad del sistema de respaldo
    verificarSistemaRespaldo()
    
    // 3. En ventana de mantenimiento
    iniciarModoMantenimiento()
    instalarActualizaciones()
    verificarFuncionalidad()
    finalizarModoMantenimiento()
}
```

---

## ⚠️ Consideraciones Importantes

### Políticas de Actualización

**Automática**:
- ✅ Máxima seguridad (siempre actualizado)
- ✅ Cero esfuerzo administrativo
- ❌ Puede interrumpir uso
- ❌ Sin control sobre timing

**Ventana**:
- ✅ Balance seguridad/productividad
- ✅ No interrumpe horario laboral
- ✅ Predecible
- ⚠️ Requiere dispositivo encendido en ventana

**Posponer**:
- ✅ Control total
- ✅ Testing antes de despliegue
- ❌ Requiere proceso manual
- ❌ Riesgo de retrasos excesivos

### Parches de Seguridad

**Google recomienda**:
- Instalar parches dentro de 90 días (crítico)
- Idealmente dentro de 30 días
- Parches críticos dentro de 7 días

**Cumplimiento**:
- PCI-DSS: Parches críticos en 30 días
- HIPAA: "Razonable y apropiado"
- GDPR: Medidas técnicas actualizadas

### Reinicio del Dispositivo

**Cuándo es Aceptable**:
- Horario de cierre (POS, oficinas)
- Madrugada (kioscos, señalización)
- Ventanas de mantenimiento (industrial)
- Con advertencia previa (corporativo)

**Cuándo NO Hacerlo**:
- Durante uso activo
- Sin advertencia
- Dispositivos críticos 24/7
- Sin plan de contingencia

---

## 📚 Próxima Categoría

**10. Gestión de Usuarios y Perfiles** (~25 métodos)

Esta categoría cubrirá:
- Crear y gestionar usuarios
- Work Profile management
- Afiliación de usuarios
- User session management
- User icons y personalización
- Logout y switch user

---

*Documentación generada para DevicePolicyManager API de Android Enterprise - Categoría 9 de 22*