# Documentación DevicePolicyManager - Categoría 2: Bloqueo y Seguridad del Dispositivo

## 📋 Visión General

La categoría de **Bloqueo y Seguridad del Dispositivo** agrupa aproximadamente **20 métodos** fundamentales que permiten controlar el estado de bloqueo, gestionar el borrado de datos, configurar la pantalla de bloqueo (keyguard) y administrar mecanismos de autenticación seguros mediante tokens.

## 🎯 Propósito

Estos métodos permiten:
- **Bloquear** el dispositivo inmediatamente de forma remota o local
- **Borrar** todos los datos del dispositivo (factory reset)
- **Deshabilitar** completamente la pantalla de bloqueo
- **Configurar** características específicas del keyguard
- **Gestionar tokens** para reseteo seguro de contraseñas
- **Controlar timeouts** de autenticación fuerte
- **Configurar Trust Agents** para escenarios de confianza

---

## 🔒 Métodos de Bloqueo

### `lockNow()`

**Propósito**: Bloquea el dispositivo inmediatamente, forzando la pantalla de bloqueo sin importar el timeout configurado.

**Casos de Uso**:
- **Bloqueo remoto de emergencia**: Proteger dispositivo perdido o robado
- **Fin de sesión de trabajo**: Bloquear al finalizar turno laboral
- **Botón de pánico en app**: Usuario bloquea manualmente desde la app
- **Políticas de seguridad**: Bloqueo automático al detectar comportamiento sospechoso
- **Kioscos**: Bloquear al finalizar transacción

**Ejemplo de Escenario**:
```
Usuario reporta dispositivo perdido
→ Administrador envía comando remoto
→ App recibe comando y llama lockNow()
→ Dispositivo se bloquea inmediatamente
→ Datos protegidos hasta recuperación
```

**Características**:
- Acción instantánea, no requiere parámetros
- Respeta la configuración de contraseña establecida
- Usuario debe autenticarse para desbloquear
- No afecta servicios en background

---

### `lockNow(int flags)`

**Propósito**: Versión extendida de `lockNow()` que permite especificar flags para controlar el comportamiento del bloqueo.

**Casos de Uso**:
- **Bloqueo con borrado de notificaciones**: Limpiar información sensible visible
- **Bloqueo con flags específicos del fabricante**: Comportamientos personalizados en Samsung Knox, etc.
- **Control granular**: Diferentes tipos de bloqueo según contexto

**Flags Comunes**:
- `0`: Bloqueo estándar (equivalente a `lockNow()`)
- `FLAG_EVICT_CREDENTIAL_ENCRYPTION_KEY` (Android 11+): Invalida claves de cifrado basadas en credenciales

**Ejemplo de Uso**:
```
Bloqueo de emergencia que además invalida claves de cifrado temporales
para mayor seguridad en caso de robo
```

**Nota**: La mayoría de casos usan `lockNow()` sin flags. Este método es para casos avanzados.

---

## 🗑️ Métodos de Borrado de Datos

### `wipeData(int flags)`

**Propósito**: Realiza un **factory reset** completo del dispositivo, borrando TODOS los datos del usuario de forma permanente e irrecuperable.

**⚠️ CRÍTICO**: Este es uno de los métodos más destructivos de la API. Su uso debe estar extremadamente controlado.

**Casos de Uso**:
- **Dispositivo robado confirmado**: Proteger datos corporativos sensibles
- **Fin de ciclo de vida del dispositivo**: Limpieza antes de devolución/reciclaje
- **Violación de políticas críticas**: Borrado automático tras intentos de hackeo
- **Desvinculación de empleado**: Limpieza tras despido/renuncia
- **Cumplimiento normativo**: Destrucción certificada de datos (GDPR, HIPAA)

**Flags Disponibles**:

| Flag | Valor | Descripción | Uso |
|------|-------|-------------|-----|
| `WIPE_EXTERNAL_STORAGE` | 0x0001 | Borra también SD card externa | Borrado completo incluyendo medios extraíbles |
| `WIPE_RESET_PROTECTION_DATA` | 0x0002 | Borra datos de FRP (Factory Reset Protection) | Permitir reactivación inmediata tras borrado |
| `WIPE_EUICC` | 0x0004 | Borra perfiles eSIM | Dispositivos con eSIM corporativa |
| `WIPE_SILENTLY` | 0x0008 | Borra sin mostrar notificación al usuario | Borrado remoto silencioso |

**Ejemplo de Combinación**:
```kotlin
// Borrado total: datos internos + SD + eSIM + sin aviso
val flags = DevicePolicyManager.WIPE_EXTERNAL_STORAGE or 
            DevicePolicyManager.WIPE_EUICC or
            DevicePolicyManager.WIPE_SILENTLY

dpm.wipeData(flags)
```

**⚠️ ADVERTENCIAS**:
- **Irreversible**: No hay forma de recuperar datos después
- **Inmediato**: Se ejecuta sin confirmación del usuario
- **Legal**: Asegúrate de tener autorización legal/contractual
- **Testing**: NUNCA pruebes en dispositivos con datos reales
- **Logs**: Mantén auditoría de cuándo y por qué se ejecutó

---

### `wipeData(int flags, CharSequence reason)`

**Propósito**: Versión extendida que permite especificar una **razón del borrado** que se registra en logs del sistema.

**Casos de Uso**:
- **Auditoría mejorada**: Registrar motivo específico del borrado
- **Cumplimiento legal**: Documentar razón para investigaciones
- **Gestión de flota**: Tracking de por qué se borraron dispositivos
- **Debugging**: Identificar borrados accidentales vs intencionales

**Ejemplos de Razones**:
- `"Dispositivo reportado como robado el 2025-10-20"`
- `"Fin de contrato laboral - empleado ID 12345"`
- `"Violación de política: instalación de apps prohibidas"`
- `"Dispositivo devuelto - fin de ciclo de vida"`
- `"Comando administrativo remoto - ticket #ABC123"`

**Ventajas**:
- Razón queda registrada en logs del sistema
- Útil para investigaciones post-facto
- Mejora compliance y auditoría
- Ayuda en debugging de borrados no planificados

**Ejemplo**:
```kotlin
val razon = "Dispositivo robado - reporte policial #2025-10-20-001"
dpm.wipeData(
    DevicePolicyManager.WIPE_EXTERNAL_STORAGE or DevicePolicyManager.WIPE_SILENTLY,
    razon
)
```

---

## 🛡️ Métodos de Configuración del Keyguard (Pantalla de Bloqueo)

### `setKeyguardDisabled(ComponentName admin, boolean disabled)`

**Propósito**: Deshabilita **completamente** la pantalla de bloqueo (keyguard). El dispositivo nunca muestra lockscreen.

**⚠️ IMPACTO DE SEGURIDAD**: Este método elimina una capa crítica de seguridad.

**Casos de Uso**:
- **Kioscos dedicados**: Dispositivos en modo quiosco sin acceso físico restringido
- **Señalización digital**: Pantallas informativas sin interacción
- **Dispositivos embebidos**: Sistemas integrados en maquinaria
- **Punto de venta**: POS que no requiere bloqueo entre transacciones
- **Displays informativos**: Tableros de información pública

**Escenarios donde NO usar**:
- ❌ Dispositivos con datos sensibles
- ❌ Dispositivos portátiles que pueden perderse
- ❌ Ambientes con acceso público no supervisado
- ❌ Cumplimiento con regulaciones de seguridad (HIPAA, PCI-DSS)

**Ejemplo de Flujo Seguro**:
```
1. Dispositivo montado en pared, físicamente seguro
2. Lock Task Mode activo (modo kiosko)
3. Restricciones de usuario aplicadas
4. Sin datos sensibles almacenados localmente
5. ENTONCES: Deshabilitar keyguard es aceptable
```

**Alternativas más seguras**:
- Usar `setKeyguardDisabledFeatures()` para deshabilitar solo ciertas características
- Configurar timeout muy largo en lugar de deshabilitar
- Usar Smart Lock con ubicaciones/dispositivos confiables

---

### `setKeyguardDisabledFeatures(ComponentName admin, int which)`

**Propósito**: Deshabilita **características específicas** del keyguard sin eliminarlo completamente, permitiendo control granular sobre qué puede hacer el usuario desde la pantalla de bloqueo.

**Casos de Uso**:
- **Seguridad selectiva**: Mantener lockscreen pero limitar funcionalidad
- **Prevenir bypass**: Evitar desactivación de seguridad desde lockscreen
- **Control de acceso**: Limitar qué información es visible cuando está bloqueado
- **Políticas corporativas**: Cumplir requisitos específicos sin eliminar toda la seguridad

**Features/Flags Disponibles**:

| Flag | Valor | Qué Deshabilita | Caso de Uso |
|------|-------|-----------------|-------------|
| `KEYGUARD_DISABLE_FEATURES_NONE` | 0 | Ninguna característica (valor por defecto) | Habilitar todas las features |
| `KEYGUARD_DISABLE_WIDGETS_ALL` | 1 << 0 | Todos los widgets en lockscreen | Prevenir información visible sin desbloquear |
| `KEYGUARD_DISABLE_SECURE_CAMERA` | 1 << 1 | Acceso rápido a cámara desde lockscreen | Prevenir fotos sin autenticación |
| `KEYGUARD_DISABLE_SECURE_NOTIFICATIONS` | 1 << 2 | Notificaciones seguras visibles | Ocultar contenido sensible |
| `KEYGUARD_DISABLE_UNREDACTED_NOTIFICATIONS` | 1 << 3 | Notificaciones sin redactar | Ocultar detalles de notificaciones |
| `KEYGUARD_DISABLE_TRUST_AGENTS` | 1 << 4 | Trust Agents (Smart Lock) | Requerir siempre autenticación manual |
| `KEYGUARD_DISABLE_FINGERPRINT` | 1 << 5 | Huella dactilar en lockscreen | Forzar solo PIN/contraseña |
| `KEYGUARD_DISABLE_REMOTE_INPUT` | 1 << 6 | Responder mensajes desde lockscreen | Prevenir respuestas sin autenticar |
| `KEYGUARD_DISABLE_ALL_FEATURES` | 0xffffffff | TODAS las características | Lockscreen mínimo funcional |
| `KEYGUARD_DISABLE_FACE` | 1 << 8 | Reconocimiento facial | Requerir métodos más seguros |
| `KEYGUARD_DISABLE_IRIS` | 1 << 9 | Reconocimiento de iris | Forzar PIN/contraseña |
| `KEYGUARD_DISABLE_BIOMETRICS` | 1 << 10 | Todos los métodos biométricos | Solo autenticación con conocimiento |

**Ejemplos de Combinaciones**:

```kotlin
// Caso 1: Alta seguridad - Sin biometría, sin cámara, sin notificaciones
val flagsAltaSeguridad = 
    DevicePolicyManager.KEYGUARD_DISABLE_BIOMETRICS or
    DevicePolicyManager.KEYGUARD_DISABLE_SECURE_CAMERA or
    DevicePolicyManager.KEYGUARD_DISABLE_SECURE_NOTIFICATIONS or
    DevicePolicyManager.KEYGUARD_DISABLE_TRUST_AGENTS

dpm.setKeyguardDisabledFeatures(admin, flagsAltaSeguridad)

// Caso 2: Corporativo moderado - Sin widgets, sin cámara, sí biometría
val flagsCorporativo = 
    DevicePolicyManager.KEYGUARD_DISABLE_WIDGETS_ALL or
    DevicePolicyManager.KEYGUARD_DISABLE_SECURE_CAMERA

dpm.setKeyguardDisabledFeatures(admin, flagsCorporativo)

// Caso 3: Médico/HIPAA - Ocultar TODO contenido sensible
val flagsMedico = 
    DevicePolicyManager.KEYGUARD_DISABLE_UNREDACTED_NOTIFICATIONS or
    DevicePolicyManager.KEYGUARD_DISABLE_SECURE_NOTIFICATIONS or
    DevicePolicyManager.KEYGUARD_DISABLE_REMOTE_INPUT or
    DevicePolicyManager.KEYGUARD_DISABLE_SECURE_CAMERA

dpm.setKeyguardDisabledFeatures(admin, flagsMedico)

// Caso 4: Habilitar todas las features nuevamente
dpm.setKeyguardDisabledFeatures(admin, DevicePolicyManager.KEYGUARD_DISABLE_FEATURES_NONE)
```

**Ventaja sobre `setKeyguardDisabled()`**:
- Mantiene la seguridad básica del lockscreen
- Control granular y ajustable según necesidades
- Mejor cumplimiento con estándares de seguridad
- Más aceptable para usuarios finales

---

### `getKeyguardDisabledFeatures(ComponentName admin)`

**Propósito**: Obtiene las características del keyguard que están actualmente deshabilitadas.

**Casos de Uso**:
- **Auditoría de configuración**: Verificar qué features están bloqueadas
- **Validación de políticas**: Confirmar que configuración se aplicó correctamente
- **Reportes de cumplimiento**: Documentar estado de seguridad
- **Debugging**: Investigar problemas de comportamiento del lockscreen
- **Sincronización**: Mantener consistencia con sistemas MDM externos

**Retorna**: Un entero (bitmask) con los flags activos.

**Ejemplo de Verificación**:
```kotlin
val featuresDeshabilitadas = dpm.getKeyguardDisabledFeatures(admin)

// Verificar si biometría está deshabilitada
val biometriaDeshabilitada = (featuresDeshabilitadas and 
    DevicePolicyManager.KEYGUARD_DISABLE_BIOMETRICS) != 0

// Verificar si cámara está deshabilitada
val camaraDeshabilitada = (featuresDeshabilitadas and 
    DevicePolicyManager.KEYGUARD_DISABLE_SECURE_CAMERA) != 0

// Verificar múltiples flags
fun verificarFeature(features: Int, flag: Int): Boolean {
    return (features and flag) != 0
}

val reporte = """
    Biometría: ${if (biometriaDeshabilitada) "DESHABILITADA" else "Habilitada"}
    Cámara: ${if (camaraDeshabilitada) "DESHABILITADA" else "Habilitada"}
    Trust Agents: ${if (verificarFeature(featuresDeshabilitadas, 
        DevicePolicyManager.KEYGUARD_DISABLE_TRUST_AGENTS)) "DESHABILITADO" else "Habilitado"}
""".trimIndent()
```

---

## 🔑 Métodos de Gestión de Tokens de Reseteo

### `setResetPasswordToken(ComponentName admin, byte[] token)`

**Propósito**: Establece un token criptográfico seguro que posteriormente puede usarse para resetear la contraseña del dispositivo sin conocer la contraseña actual.

**Flujo Completo**:
```
1. Admin genera token aleatorio seguro (32+ bytes)
2. Admin llama setResetPasswordToken() durante provisioning
3. Usuario debe desbloquear dispositivo (activa el token)
4. Token se almacena cifrado en el sistema
5. Admin guarda token en servidor MDM seguro
6. Cuando usuario olvida contraseña:
   → Admin recupera token del servidor
   → Admin llama resetPasswordWithToken()
   → Contraseña se resetea exitosamente
```

**Casos de Uso**:
- **Recuperación de contraseña corporativa**: Help desk puede resetear sin conocer contraseña anterior
- **Provisioning masivo**: Preparar dispositivos para recuperación futura
- **Políticas de soporte**: Capacidad de recuperación sin comprometer seguridad
- **Cumplimiento**: Mantener control administrativo sin violar privacidad del usuario

**Requisitos de Seguridad**:
- Token debe ser criptográficamente aleatorio
- Longitud mínima: 32 bytes (recomendado 64 bytes)
- Almacenar token en servidor seguro (HSM, vault, etc.)
- Cifrar token en tránsito y reposo
- Implementar auditoría de uso del token

**Ejemplo de Generación Segura**:
```kotlin
import java.security.SecureRandom

fun generarTokenSeguro(longitud: Int = 64): ByteArray {
    val random = SecureRandom()
    val token = ByteArray(longitud)
    random.nextBytes(token)
    return token
}

// Uso
val token = generarTokenSeguro()
val tokenEstablecido = dpm.setResetPasswordToken(admin, token)

if (tokenEstablecido) {
    // Guardar token de forma segura en servidor MDM
    guardarTokenEnServidorSeguro(dispositivoId, token)
    Log.i("Security", "Token de reseteo establecido correctamente")
} else {
    Log.e("Security", "Fallo al establecer token - verificar requisitos")
}
```

**⚠️ Consideraciones de Seguridad**:
- Token es equivalente a conocer la contraseña
- Proteger token con el mismo nivel de seguridad que contraseñas
- Rotar tokens periódicamente (cada 90-180 días)
- Auditar cada uso del token
- Revocar token cuando ya no sea necesario

---

### `clearResetPasswordToken(ComponentName admin)`

**Propósito**: Elimina permanentemente el token de reseteo de contraseña del dispositivo.

**Casos de Uso**:
- **Rotación de seguridad**: Eliminar token antiguo antes de establecer uno nuevo
- **Fin de gestión**: Remover capacidades de reseteo al desvincular dispositivo
- **Compromiso de seguridad**: Revocar token si se sospecha exposición
- **Cambio de políticas**: Transición a sistema de recuperación diferente
- **Limpieza post-uso**: Eliminar token después de usarlo para resetear

**Ejemplo de Rotación de Token**:
```kotlin
fun rotarTokenReseteo(dpm: DevicePolicyManager, admin: ComponentName) {
    // 1. Limpiar token antiguo
    val limpiado = dpm.clearResetPasswordToken(admin)
    
    if (limpiado) {
        Log.i("Security", "Token anterior eliminado")
        
        // 2. Generar nuevo token
        val nuevoToken = generarTokenSeguro()
        
        // 3. Establecer nuevo token
        val establecido = dpm.setResetPasswordToken(admin, nuevoToken)
        
        if (establecido) {
            // 4. Actualizar en servidor
            actualizarTokenEnServidor(dispositivoId, nuevoToken)
            Log.i("Security", "Rotación de token completada")
        }
    }
}
```

**Escenarios de Revocación Inmediata**:
- Empleado despedido/renunció
- Token potencialmente comprometido
- Migración a nuevo sistema de gestión
- Dispositivo reportado como perdido/robado
- Detección de acceso no autorizado

---

### `isResetPasswordTokenActive(ComponentName admin)`

**Propósito**: Verifica si el token de reseteo está activo y listo para usar. Un token solo se activa después de que el usuario desbloquea el dispositivo al menos una vez tras su configuración.

**Casos de Uso**:
- **Validación post-provisioning**: Confirmar que usuario activó el token
- **Verificación previa a reseteo**: Asegurar capacidad de recuperación antes de necesitarla
- **Monitoreo de estado**: Reportar capacidad de recuperación a sistemas MDM
- **Debugging de problemas**: Identificar por qué no se puede resetear contraseña
- **Compliance**: Verificar que todos los dispositivos tienen recuperación habilitada

**Estados Posibles**:

| Estado | `isResetPasswordTokenActive()` | Significado |
|--------|-------------------------------|-------------|
| Sin token | `false` | Nunca se configuró token |
| Token configurado, sin activar | `false` | Usuario no ha desbloqueado desde configuración |
| Token activo | `true` | Listo para usar en reseteo |
| Token revocado | `false` | Token fue eliminado con `clearResetPasswordToken()` |

**Ejemplo de Verificación Completa**:
```kotlin
fun verificarEstadoRecuperacion(dpm: DevicePolicyManager, admin: ComponentName): EstadoRecuperacion {
    val tokenActivo = dpm.isResetPasswordTokenActive(admin)
    
    return when {
        tokenActivo -> {
            EstadoRecuperacion.LISTO(
                mensaje = "✅ Token activo - recuperación disponible"
            )
        }
        else -> {
            // Token no activo - necesita investigación
            EstadoRecuperacion.NO_DISPONIBLE(
                mensaje = "⚠️ Token no activo - usuario debe desbloquear dispositivo",
                accionRequerida = "Solicitar al usuario que desbloquee el dispositivo"
            )
        }
    }
}

sealed class EstadoRecuperacion {
    data class LISTO(val mensaje: String) : EstadoRecuperacion()
    data class NO_DISPONIBLE(val mensaje: String, val accionRequerida: String) : EstadoRecuperacion()
}
```

**Flujo de Activación**:
```
1. Admin configura token → setResetPasswordToken()
2. Estado: Token configurado pero inactivo
3. Usuario desbloquea dispositivo con su contraseña/PIN
4. Sistema activa token automáticamente
5. Estado: Token activo y utilizable
6. isResetPasswordTokenActive() retorna true
```

---

## ⏱️ Métodos de Timeout de Autenticación

### `setRequiredStrongAuthTimeout(ComponentName admin, long timeoutMs)`

**Propósito**: Define el intervalo máximo de tiempo durante el cual el usuario puede usar métodos de autenticación débiles (biometría) antes de que el sistema requiera autenticación fuerte (PIN/contraseña/patrón).

**Concepto Clave**:
- **Autenticación Fuerte**: PIN, contraseña, patrón (algo que sabes)
- **Autenticación Débil**: Huella dactilar, reconocimiento facial, iris (algo que eres)

**Casos de Uso**:
- **Cumplimiento de seguridad**: Regulaciones que requieren autenticación fuerte periódica
- **Balance seguridad/UX**: Permitir biometría pero con verificaciones regulares
- **Prevenir bypass prolongado**: Evitar dependencia indefinida de biometría
- **Políticas corporativas**: Diferentes timeouts según nivel de seguridad del usuario

**Escenarios por Industria**:

| Industria | Timeout Recomendado | Justificación |
|-----------|---------------------|---------------|
| Financiera/Bancaria | 1-4 horas | Datos financieros sensibles |
| Salud/Médica (HIPAA) | 2-4 horas | Información médica protegida |
| Gobierno/Defensa | 15 min - 1 hora | Seguridad nacional |
| Corporativo estándar | 4-8 horas | Balance seguridad/productividad |
| Retail/POS | 8-24 horas | Conveniencia operacional |
| Educación | 24 horas | Menor sensibilidad de datos |

**Valores Especiales**:
- `0`: Deshabilitar autenticación débil completamente (siempre requiere fuerte)
- Sin configurar: Android usa default del sistema (generalmente 24-72 horas)

**Ejemplo de Configuraciones**:
```kotlin
// Alta seguridad - Contraseña cada hora
dpm.setRequiredStrongAuthTimeout(admin, 3600000L) // 1 hora

// Estándar corporativo - Contraseña cada 4 horas
dpm.setRequiredStrongAuthTimeout(admin, 14400000L) // 4 horas

// Médico/HIPAA - Contraseña cada 2 horas
dpm.setRequiredStrongAuthTimeout(admin, 7200000L) // 2 horas

// Solo autenticación fuerte - Deshabilitar biometría
dpm.setRequiredStrongAuthTimeout(admin, 0L)
```

**Flujo de Usuario**:
```
Hora 00:00 - Usuario desbloquea con contraseña ✅
Hora 00:15 - Usuario desbloquea con huella ✅
Hora 02:30 - Usuario desbloquea con huella ✅
Hora 04:00 - Usuario intenta huella ❌
           - Sistema requiere contraseña
           - Usuario debe ingresar contraseña
           - Ciclo reinicia desde aquí
```

---

### `getRequiredStrongAuthTimeout(ComponentName admin)`

**Propósito**: Obtiene el timeout de autenticación fuerte actualmente configurado.

**Casos de Uso**:
- **Auditoría de políticas**: Verificar configuración actual
- **Reportes de cumplimiento**: Documentar estado de seguridad
- **Validación post-configuración**: Confirmar que el cambio se aplicó
- **Debugging**: Investigar comportamiento de autenticación
- **UI informativa**: Mostrar al usuario cada cuánto necesitará contraseña

**Retorna**: Timeout en milisegundos, o `0` si autenticación débil está deshabilitada.

**Ejemplo de Reporte**:
```kotlin
fun generarReporteAutenticacion(dpm: DevicePolicyManager, admin: ComponentName): String {
    val timeoutMs = dpm.getRequiredStrongAuthTimeout(admin)
    
    return when {
        timeoutMs == 0L -> {
            "🔒 MODO ALTA SEGURIDAD\n" +
            "Solo autenticación fuerte permitida (PIN/Contraseña/Patrón)\n" +
            "Biometría: DESHABILITADA"
        }
        timeoutMs > 0 -> {
            val horas = timeoutMs / 3600000
            val minutos = (timeoutMs % 3600000) / 60000
            
            val tiempoLegible = when {
                horas > 0 && minutos > 0 -> "$horas h $minutos min"
                horas > 0 -> "$horas horas"
                else -> "$minutos minutos"
            }
            
            "🔓 AUTENTICACIÃ"N MIXTA\n" +
            "Requiere contraseña cada: $tiempoLegible\n" +
            "Entre intervalos: Biometría permitida"
        }
        else -> {
            "⚙️ CONFIGURACIÃ"N POR DEFECTO\n" +
            "Usando timeout del sistema Android"
        }
    }
}
```

---

## 🤝 Métodos de Trust Agents

### `setTrustAgentConfiguration(ComponentName admin, ComponentName target, PersistableBundle configuration)`

**Propósito**: Configura Trust Agents específicos, que son componentes del sistema que pueden mantener el dispositivo desbloqueado bajo condiciones de "confianza" predefinidas (ubicación, dispositivos cercanos, etc.).

**Concepto de Trust Agents**:
Los Trust Agents implementan **Smart Lock** de Android:
- **On-body detection**: Mantener desbloqueado mientras se lleva puesto
- **Trusted places**: Desbloquear en ubicaciones específicas (casa, oficina)
- **Trusted devices**: Desbloquear cuando está cerca un dispositivo Bluetooth emparejado
- **Trusted face**: Reconocimiento facial (deprecated en favor de BiometricPrompt)
- **Voice Match**: "Ok Google" mantiene desbloqueado

**Casos de Uso Corporativos**:
- **Oficina sin lockscreen**: Mantener desbloqueado dentro del edificio corporativo
- **Vehículos de flota**: Desbloquear automáticamente cuando conductor está presente
- **Dispositivos wearable corporativos**: Desbloquear con smartwatch de empresa
- **Zonas seguras**: Áreas con control de acceso físico donde lockscreen es innecesario

**Parámetros de Configuración** (en PersistableBundle):

```kotlin
// Ejemplo: Configurar ubicación de confianza (oficina)
val config = PersistableBundle().apply {
    putDouble("latitude", -12.0464)      // Coordenada de oficina
    putDouble("longitude", -77.0428)
    putInt("radius", 100)                 // Radio en metros
    putString("label", "Oficina Central")
}

val trustAgent = ComponentName(
    "com.google.android.gms",
    "com.google.android.gms.auth.trustagent.GoogleTrustAgent"
)

dpm.setTrustAgentConfiguration(admin, trustAgent, config)
```

**Trust Agents Comunes**:

| Trust Agent | Package | Funcionalidad |
|-------------|---------|---------------|
| Google Trust Agent | `com.google.android.gms` | Smart Lock (ubicación, dispositivos, on-body) |
| Samsung Trust Agent | `com.samsung.android.knox.kpecore` | Knox Workspace (Samsung) |
| Custom Corporate | Tu package | Implementación personalizada |

**⚠️ Consideraciones de Seguridad**:
- Trust Agents **reducen** la seguridad
- Solo usar en entornos con seguridad física
- No usar con datos altamente sensibles
- Puede ser deshabilitado con `KEYGUARD_DISABLE_TRUST_AGENTS`
- Monitorear uso y configuración regularmente

---

### `getTrustAgentConfiguration(ComponentName admin, ComponentName target)`

**Propósito**: Obtiene la configuración actual de un Trust Agent específico.

**Casos de Uso**:
- **Auditoría**: Verificar qué Trust Agents están configurados
- **Validación**: Confirmar que configuración se aplicó correctamente
- **Debugging**: Investigar problemas de desbloqueo automático
- **Sincronización**: Mantener consistencia con sistemas MDM
- **Reporting**: Documentar configuraciones de seguridad

**Retorna**: `PersistableBundle` con la configuración, o `null` si no está configurado.

**Ejemplo de Verificación**:
```kotlin
fun verificarTrustAgent(dpm: DevicePolicyManager, admin: ComponentName) {
    val trustAgent = ComponentName(
        "com.google.android.gms",
        "com.google.android.gms.auth.trustagent.GoogleTrustAgent"
    )
    
    val config = dpm.getTrustAgentConfiguration(admin, trustAgent)
    
    if (config != null) {
        val latitude = config.getDouble("latitude", 0.0)
        val longitude = config.getDouble("longitude", 0.0)
        val radius = config.getInt("radius", 0)
        val label = config.getString("label", "Sin nombre")
        
        Log.i("TrustAgent", """
            Trust Agent configurado:
            - Ubicación: $label
            - Coordenadas: ($latitude, $longitude)
            - Radio: $radius metros
        """.trimIndent())
    } else {
        Log.i("TrustAgent", "No hay Trust Agent configurado")
    }
}
```

---

## 📝 Ejemplos de Implementación

### Ejemplo 1: Sistema de Bloqueo Remoto de Emergencia

```kotlin
/**
 * Sistema completo de bloqueo remoto para dispositivos corporativos
 * Incluye bloqueo, verificación de estado y auditoría
 */
class SistemaBloqueoRemoto(
    private val context: Context,
    private val dpm: DevicePolicyManager,
    private val admin: ComponentName
) {
    
    /**
     * Bloquea el dispositivo inmediatamente y registra el evento
     */
    fun bloqueoEmergencia(razon: String, solicitadoPor: String): ResultadoBloqueo {
        try {
            // Registrar intento en logs locales
            registrarEventoSeguridad(
                tipo = "BLOQUEO_REMOTO_INICIADO",
                razon = razon,
                solicitante = solicitadoPor,
                timestamp = System.currentTimeMillis()
            )
            
            // Ejecutar bloqueo con flag de invalidación de claves de cifrado
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                dpm.lockNow(DevicePolicyManager.FLAG_EVICT_CREDENTIAL_ENCRYPTION_KEY)
            } else {
                dpm.lockNow()
            }
            
            // Confirmar bloqueo exitoso
            val bloqueado = verificarEstadoBloqueo()
            
            if (bloqueado) {
                registrarEventoSeguridad(
                    tipo = "BLOQUEO_REMOTO_EXITOSO",
                    razon = razon,
                    solicitante = solicitadoPor,
                    timestamp = System.currentTimeMillis()
                )
                
                // Notificar al servidor MDM
                notificarServidorMDM(
                    evento = "device_locked",
                    razon = razon,
                    timestamp = System.currentTimeMillis()
                )
                
                return ResultadoBloqueo.Exitoso(
                    mensaje = "Dispositivo bloqueado exitosamente",
                    timestamp = System.currentTimeMillis()
                )
            } else {
                return ResultadoBloqueo.Fallo(
                    error = "No se pudo verificar el bloqueo"
                )
            }
            
        } catch (e: SecurityException) {
            registrarEventoSeguridad(
                tipo = "BLOQUEO_REMOTO_FALLIDO",
                razon = "SecurityException: ${e.message}",
                solicitante = solicitadoPor,
                timestamp = System.currentTimeMillis()
            )
            
            return ResultadoBloqueo.Fallo(
                error = "Permisos insuficientes: ${e.message}"
            )
        } catch (e: Exception) {
            return ResultadoBloqueo.Fallo(
                error = "Error inesperado: ${e.message}"
            )
        }
    }
    
    /**
     * Verifica si el dispositivo está actualmente bloqueado
     */
    private fun verificarEstadoBloqueo(): Boolean {
        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        return keyguardManager.isKeyguardLocked
    }
    
    /**
     * Sistema de bloqueo progresivo con múltiples intentos
     */
    fun bloqueoProgresivo(
        intentosFallidos: Int,
        limiteBloqueo: Int = 10,
        limiteBorrado: Int = 20
    ): AccionSeguridad {
        return when {
            intentosFallidos >= limiteBorrado -> {
                AccionSeguridad.BorradoCompleto(
                    razon = "Límite de intentos fallidos alcanzado: $intentosFallidos"
                )
            }
            intentosFallidos >= limiteBloqueo -> {
                bloqueoEmergencia(
                    razon = "Múltiples intentos fallidos: $intentosFallidos",
                    solicitadoPor = "SISTEMA_AUTOMATICO"
                )
                AccionSeguridad.BloqueoAutomatico
            }
            intentosFallidos >= (limiteBloqueo * 0.7).toInt() -> {
                AccionSeguridad.Advertencia(
                    intentosRestantes = limiteBloqueo - intentosFallidos
                )
            }
            else -> {
                AccionSeguridad.Ninguna
            }
        }
    }
    
    private fun registrarEventoSeguridad(
        tipo: String, 
        razon: String, 
        solicitante: String, 
        timestamp: Long
    ) {
        // Implementar logging seguro
        Log.i("SeguridadDispositivo", "[$timestamp] $tipo - $razon (por: $solicitante)")
    }
    
    private fun notificarServidorMDM(evento: String, razon: String, timestamp: Long) {
        // Implementar notificación al servidor MDM
    }
}

sealed class ResultadoBloqueo {
    data class Exitoso(val mensaje: String, val timestamp: Long) : ResultadoBloqueo()
    data class Fallo(val error: String) : ResultadoBloqueo()
}

sealed class AccionSeguridad {
    data class BorradoCompleto(val razon: String) : AccionSeguridad()
    object BloqueoAutomatico : AccionSeguridad()
    data class Advertencia(val intentosRestantes: Int) : AccionSeguridad()
    object Ninguna : AccionSeguridad()
}
```

---

### Ejemplo 2: Configuración y Verificación Completa del Keyguard

```kotlin
/**
 * Gestor completo de configuración del Keyguard
 * Maneja diferentes perfiles de seguridad según el tipo de dispositivo
 */
class GestorKeyguard(
    private val dpm: DevicePolicyManager,
    private val admin: ComponentName
) {
    
    /**
     * Configura el keyguard según perfil de seguridad
     */
    fun configurarPorPerfil(perfil: PerfilSeguridad) {
        when (perfil) {
            is PerfilSeguridad.AltaSeguridad -> configurarAltaSeguridad()
            is PerfilSeguridad.CorporativoEstandar -> configurarCorporativoEstandar()
            is PerfilSeguridad.Kiosko -> configurarKiosko()
            is PerfilSeguridad.MedicoHIPAA -> configurarMedico()
            is PerfilSeguridad.Personalizado -> configurarPersonalizado(perfil.flags)
        }
    }
    
    /**
     * Perfil de Alta Seguridad - Gobierno, Defensa, Finanzas
     */
    private fun configurarAltaSeguridad() {
        val flags = DevicePolicyManager.KEYGUARD_DISABLE_BIOMETRICS or
                    DevicePolicyManager.KEYGUARD_DISABLE_SECURE_CAMERA or
                    DevicePolicyManager.KEYGUARD_DISABLE_SECURE_NOTIFICATIONS or
                    DevicePolicyManager.KEYGUARD_DISABLE_UNREDACTED_NOTIFICATIONS or
                    DevicePolicyManager.KEYGUARD_DISABLE_TRUST_AGENTS or
                    DevicePolicyManager.KEYGUARD_DISABLE_REMOTE_INPUT or
                    DevicePolicyManager.KEYGUARD_DISABLE_WIDGETS_ALL
        
        dpm.setKeyguardDisabledFeatures(admin, flags)
        
        // Solo autenticación fuerte
        dpm.setRequiredStrongAuthTimeout(admin, 0L)
        
        // Auto-bloqueo rápido (1 minuto)
        dpm.setMaximumTimeToLock(admin, 60000L)
        
        Log.i("Keyguard", "Configuración ALTA SEGURIDAD aplicada")
    }
    
    /**
     * Perfil Corporativo Estándar - Empresas generales
     */
    private fun configurarCorporativoEstandar() {
        val flags = DevicePolicyManager.KEYGUARD_DISABLE_WIDGETS_ALL or
                    DevicePolicyManager.KEYGUARD_DISABLE_SECURE_CAMERA
        
        dpm.setKeyguardDisabledFeatures(admin, flags)
        
        // Autenticación fuerte cada 4 horas
        dpm.setRequiredStrongAuthTimeout(admin, 14400000L)
        
        // Auto-bloqueo moderado (5 minutos)
        dpm.setMaximumTimeToLock(admin, 300000L)
        
        Log.i("Keyguard", "Configuración CORPORATIVO ESTÁNDAR aplicada")
    }
    
    /**
     * Perfil Kiosko - Dispositivos dedicados
     */
    private fun configurarKiosko() {
        // Opción 1: Deshabilitar keyguard completamente (solo si físicamente seguro)
        // dpm.setKeyguardDisabled(admin, true)
        
        // Opción 2 (recomendada): Keyguard mínimo con todas las features deshabilitadas
        dpm.setKeyguardDisabledFeatures(admin, DevicePolicyManager.KEYGUARD_DISABLE_ALL_FEATURES)
        
        // Timeout muy largo o sin timeout
        dpm.setMaximumTimeToLock(admin, 3600000L) // 1 hora
        
        Log.i("Keyguard", "Configuración KIOSKO aplicada")
    }
    
    /**
     * Perfil Médico - Cumplimiento HIPAA
     */
    private fun configurarMedico() {
        val flags = DevicePolicyManager.KEYGUARD_DISABLE_UNREDACTED_NOTIFICATIONS or
                    DevicePolicyManager.KEYGUARD_DISABLE_SECURE_NOTIFICATIONS or
                    DevicePolicyManager.KEYGUARD_DISABLE_REMOTE_INPUT or
                    DevicePolicyManager.KEYGUARD_DISABLE_SECURE_CAMERA or
                    DevicePolicyManager.KEYGUARD_DISABLE_TRUST_AGENTS
        
        dpm.setKeyguardDisabledFeatures(admin, flags)
        
        // Autenticación fuerte cada 2 horas
        dpm.setRequiredStrongAuthTimeout(admin, 7200000L)
        
        // Auto-bloqueo muy rápido (30 segundos)
        dpm.setMaximumTimeToLock(admin, 30000L)
        
        Log.i("Keyguard", "Configuración MÉDICO/HIPAA aplicada")
    }
    
    /**
     * Perfil Personalizado - Flags específicos
     */
    private fun configurarPersonalizado(flags: Int) {
        dpm.setKeyguardDisabledFeatures(admin, flags)
        Log.i("Keyguard", "Configuración PERSONALIZADA aplicada")
    }
    
    /**
     * Genera reporte completo del estado del keyguard
     */
    fun generarReporteEstado(): ReporteKeyguard {
        val featuresDeshabilitadas = dpm.getKeyguardDisabledFeatures(admin)
        val strongAuthTimeout = dpm.getRequiredStrongAuthTimeout(admin)
        
        return ReporteKeyguard(
            biometriaDeshabilitada = verificarFlag(
                featuresDeshabilitadas, 
                DevicePolicyManager.KEYGUARD_DISABLE_BIOMETRICS
            ),
            camaraDeshabilitada = verificarFlag(
                featuresDeshabilitadas,
                DevicePolicyManager.KEYGUARD_DISABLE_SECURE_CAMERA
            ),
            notificacionesDeshabilitadas = verificarFlag(
                featuresDeshabilitadas,
                DevicePolicyManager.KEYGUARD_DISABLE_SECURE_NOTIFICATIONS
            ),
            trustAgentsDeshabilitados = verificarFlag(
                featuresDeshabilitadas,
                DevicePolicyManager.KEYGUARD_DISABLE_TRUST_AGENTS
            ),
            widgetsDeshabilitados = verificarFlag(
                featuresDeshabilitadas,
                DevicePolicyManager.KEYGUARD_DISABLE_WIDGETS_ALL
            ),
            remoteInputDeshabilitado = verificarFlag(
                featuresDeshabilitadas,
                DevicePolicyManager.KEYGUARD_DISABLE_REMOTE_INPUT
            ),
            strongAuthTimeoutHoras = strongAuthTimeout / 3600000.0,
            todasFeaturesDeshabilitadas = featuresDeshabilitadas == DevicePolicyManager.KEYGUARD_DISABLE_ALL_FEATURES
        )
    }
    
    private fun verificarFlag(features: Int, flag: Int): Boolean {
        return (features and flag) != 0
    }
    
    /**
     * Validar que la configuración cumple con estándar de seguridad
     */
    fun validarCumplimientoSeguridad(estandar: EstandarSeguridad): ResultadoValidacion {
        val reporte = generarReporteEstado()
        
        val cumple = when (estandar) {
            EstandarSeguridad.HIPAA -> {
                reporte.notificacionesDeshabilitadas &&
                reporte.strongAuthTimeoutHoras <= 4.0 &&
                reporte.camaraDeshabilitada
            }
            EstandarSeguridad.PCI_DSS -> {
                reporte.strongAuthTimeoutHoras <= 8.0 &&
                !reporte.todasFeaturesDeshabilitadas
            }
            EstandarSeguridad.NIST_800_53 -> {
                reporte.strongAuthTimeoutHoras <= 4.0 &&
                reporte.trustAgentsDeshabilitados
            }
            EstandarSeguridad.ISO_27001 -> {
                reporte.strongAuthTimeoutHoras <= 24.0
            }
        }
        
        return if (cumple) {
            ResultadoValidacion.Cumple(
                estandar = estandar.name,
                detalles = reporte
            )
        } else {
            ResultadoValidacion.NoCumple(
                estandar = estandar.name,
                razones = identificarIncumplimientos(estandar, reporte)
            )
        }
    }
    
    private fun identificarIncumplimientos(
        estandar: EstandarSeguridad, 
        reporte: ReporteKeyguard
    ): List<String> {
        val incumplimientos = mutableListOf<String>()
        
        when (estandar) {
            EstandarSeguridad.HIPAA -> {
                if (!reporte.notificacionesDeshabilitadas) {
                    incumplimientos.add("Las notificaciones visibles en lockscreen violan privacidad HIPAA")
                }
                if (reporte.strongAuthTimeoutHoras > 4.0) {
                    incumplimientos.add("Timeout de autenticación fuerte excede límite HIPAA (4 horas)")
                }
                if (!reporte.camaraDeshabilitada) {
                    incumplimientos.add("Cámara accesible desde lockscreen puede capturar información sensible")
                }
            }
            EstandarSeguridad.PCI_DSS -> {
                if (reporte.strongAuthTimeoutHoras > 8.0) {
                    incumplimientos.add("Timeout excede requisito PCI-DSS de autenticación periódica")
                }
            }
            EstandarSeguridad.NIST_800_53 -> {
                if (reporte.strongAuthTimeoutHoras > 4.0) {
                    incumplimientos.add("NIST requiere autenticación fuerte al menos cada 4 horas")
                }
                if (!reporte.trustAgentsDeshabilitados) {
                    incumplimientos.add("Trust Agents reducen nivel de autenticación requerido por NIST")
                }
            }
            EstandarSeguridad.ISO_27001 -> {
                if (reporte.strongAuthTimeoutHoras > 24.0) {
                    incumplimientos.add("ISO 27001 requiere verificación de credenciales al menos diaria")
                }
            }
        }
        
        return incumplimientos
    }
}

// Clases de datos y enums
sealed class PerfilSeguridad {
    object AltaSeguridad : PerfilSeguridad()
    object CorporativoEstandar : PerfilSeguridad()
    object Kiosko : PerfilSeguridad()
    object MedicoHIPAA : PerfilSeguridad()
    data class Personalizado(val flags: Int) : PerfilSeguridad()
}

data class ReporteKeyguard(
    val biometriaDeshabilitada: Boolean,
    val camaraDeshabilitada: Boolean,
    val notificacionesDeshabilitadas: Boolean,
    val trustAgentsDeshabilitados: Boolean,
    val widgetsDeshabilitados: Boolean,
    val remoteInputDeshabilitado: Boolean,
    val strongAuthTimeoutHoras: Double,
    val todasFeaturesDeshabilitadas: Boolean
) {
    fun toStringLegible(): String {
        return """
            📊 REPORTE KEYGUARD
            ══════════════════════════════════════
            🔐 Biometría: ${if (biometriaDeshabilitada) "DESHABILITADA" else "Habilitada"}
            📷 Cámara: ${if (camaraDeshabilitada) "DESHABILITADA" else "Habilitada"}
            🔔 Notificaciones: ${if (notificacionesDeshabilitadas) "OCULTAS" else "Visibles"}
            🤝 Trust Agents: ${if (trustAgentsDeshabilitados) "DESHABILITADOS" else "Habilitados"}
            📱 Widgets: ${if (widgetsDeshabilitados) "DESHABILITADOS" else "Habilitados"}
            ⌨️ Input Remoto: ${if (remoteInputDeshabilitado) "DESHABILITADO" else "Habilitado"}
            ⏱️ Auth Fuerte: Cada ${strongAuthTimeoutHoras} horas
            🛡️ Modo: ${if (todasFeaturesDeshabilitadas) "MÍNIMO SEGURO" else "FUNCIONAL"}
        """.trimIndent()
    }
}

enum class EstandarSeguridad {
    HIPAA,      // Health Insurance Portability and Accountability Act
    PCI_DSS,    // Payment Card Industry Data Security Standard
    NIST_800_53, // National Institute of Standards and Technology
    ISO_27001   // International Organization for Standardization
}

sealed class ResultadoValidacion {
    data class Cumple(val estandar: String, val detalles: ReporteKeyguard) : ResultadoValidacion()
    data class NoCumple(val estandar: String, val razones: List<String>) : ResultadoValidacion()
}
```

---

## 🎯 Casos de Uso por Escenario

### 🏢 Corporativo Estándar
```kotlin
// Configuración balanceada entre seguridad y usabilidad
dpm.setKeyguardDisabledFeatures(admin, 
    DevicePolicyManager.KEYGUARD_DISABLE_WIDGETS_ALL or
    DevicePolicyManager.KEYGUARD_DISABLE_SECURE_CAMERA
)
dpm.setRequiredStrongAuthTimeout(admin, 14400000L) // 4 horas
dpm.setMaximumTimeToLock(admin, 300000L) // 5 minutos
```

### 🏥 Médico/HIPAA
```kotlin
// Máxima privacidad en lockscreen
dpm.setKeyguardDisabledFeatures(admin,
    DevicePolicyManager.KEYGUARD_DISABLE_UNREDACTED_NOTIFICATIONS or
    DevicePolicyManager.KEYGUARD_DISABLE_SECURE_NOTIFICATIONS or
    DevicePolicyManager.KEYGUARD_DISABLE_REMOTE_INPUT or
    DevicePolicyManager.KEYGUARD_DISABLE_SECURE_CAMERA or
    DevicePolicyManager.KEYGUARD_DISABLE_TRUST_AGENTS
)
dpm.setRequiredStrongAuthTimeout(admin, 7200000L) // 2 horas
dpm.setMaximumTimeToLock(admin, 30000L) // 30 segundos
```

### 🏦 Financiero/Bancario
```kotlin
// Sin biometría, solo autenticación fuerte
dpm.setKeyguardDisabledFeatures(admin,
    DevicePolicyManager.KEYGUARD_DISABLE_BIOMETRICS or
    DevicePolicyManager.KEYGUARD_DISABLE_TRUST_AGENTS or
    DevicePolicyManager.KEYGUARD_DISABLE_SECURE_CAMERA
)
dpm.setRequiredStrongAuthTimeout(admin, 0L) // Solo autenticación fuerte
dpm.setMaximumTimeToLock(admin, 120000L) // 2 minutos
```

### 🏭 Kiosko/Punto de Venta
```kotlin
// Keyguard mínimo o deshabilitado
dpm.setKeyguardDisabledFeatures(admin, 
    DevicePolicyManager.KEYGUARD_DISABLE_ALL_FEATURES
)
// O alternativamente (solo si físicamente seguro):
// dpm.setKeyguardDisabled(admin, true)
dpm.setMaximumTimeToLock(admin, 600000L) // 10 minutos
```

### 🛡️ Gobierno/Alta Seguridad
```kotlin
// Restricciones máximas
dpm.setKeyguardDisabledFeatures(admin,
    DevicePolicyManager.KEYGUARD_DISABLE_BIOMETRICS or
    DevicePolicyManager.KEYGUARD_DISABLE_SECURE_CAMERA or
    DevicePolicyManager.KEYGUARD_DISABLE_SECURE_NOTIFICATIONS or
    DevicePolicyManager.KEYGUARD_DISABLE_UNREDACTED_NOTIFICATIONS or
    DevicePolicyManager.KEYGUARD_DISABLE_TRUST_AGENTS or
    DevicePolicyManager.KEYGUARD_DISABLE_REMOTE_INPUT or
    DevicePolicyManager.KEYGUARD_DISABLE_WIDGETS_ALL
)
dpm.setRequiredStrongAuthTimeout(admin, 0L) // Solo autenticación fuerte
dpm.setMaximumTimeToLock(admin, 60000L) // 1 minuto
dpm.setMaximumFailedPasswordsForWipe(admin, 5) // Borrado tras 5 intentos
```

---

## ⚠️ Consideraciones Críticas

### Seguridad

1. **Bloqueo del dispositivo**:
    - `lockNow()` no requiere confirmación del usuario
    - El bloqueo es inmediato e irreversible hasta autenticación
    - Usar con precaución en flujos automáticos

2. **Borrado de datos**:
    - `wipeData()` es **destructivo e irreversible**
    - Siempre implementar confirmaciones múltiples
    - Mantener logs de auditoría detallados
    - Verificar autorización legal antes de ejecutar

3. **Keyguard deshabilitado**:
    - `setKeyguardDisabled(true)` elimina capa crítica de seguridad
    - Solo para dispositivos con seguridad física garantizada
    - Preferir `setKeyguardDisabledFeatures()` para control granular

4. **Trust Agents**:
    - Reducen el nivel de seguridad significativamente
    - Solo usar en ambientes controlados
    - Monitorear y auditar regularmente

### Cumplimiento Normativo

| Regulación | Requisitos Clave | Configuración Recomendada |
|------------|------------------|---------------------------|
| **HIPAA** | Privacidad de notificaciones, timeout corto | Ocultar notificaciones, auth cada 2h, lock 30s |
| **PCI-DSS** | Autenticación periódica, timeout moderado | Auth cada 4-8h, lock 2-5 min |
| **GDPR** | Protección de datos personales | Cifrado, auth periódica, logs de acceso |
| **SOX** | Auditoría de accesos | Logs detallados, auth fuerte |
| **NIST 800-53** | Autenticación multi-factor periódica | Auth cada 4h, sin trust agents |

### Mejores Prácticas

1. **Tokens de reseteo**:
    - Generar tokens criptográficamente seguros (64+ bytes)
    - Almacenar en HSM o vault de servidor
    - Rotar cada 90-180 días
    - Auditar cada uso del token

2. **Timeouts de autenticación**:
    - Balance entre seguridad y usabilidad
    - Ajustar según sensibilidad de datos
    - Notificar al usuario antes de requerir auth fuerte

3. **Configuración de keyguard**:
    - Usar perfiles predefinidos por industria
    - Validar cumplimiento con estándares
    - Documentar razones de configuración
    - Revisar periódicamente

4. **Testing**:
    - NUNCA probar `wipeData()` en dispositivos reales
    - Usar emuladores o dispositivos de desarrollo
    - Implementar confirmaciones de seguridad
    - Mantener backups antes de pruebas

---

## 📚 Próxima Categoría

**3. Restricciones de Usuario** (~15 métodos)

Esta categoría cubrirá:
- Gestión de restricciones con `UserManager`
- Control de funcionalidades del dispositivo
- Restricciones de perfil de trabajo
- Políticas de separación de datos

---

*Documentación generada para DevicePolicyManager API de Android Enterprise - Categoría 2 de 22*
