# Documentación DevicePolicyManager - Categoría 11: Cámara y Captura de Pantalla

## 📋 Visión General

La categoría de **Cámara y Captura de Pantalla** agrupa aproximadamente **5 métodos** que permiten controlar el acceso a la cámara del dispositivo, bloquear la captura de pantalla y gestionar políticas de streaming cercano (Nearby). Son controles críticos para prevenir fugas de información visual en ambientes seguros.

## 🎯 Propósito

Estos métodos permiten:
- **Deshabilitar cámaras** del dispositivo completamente
- **Bloquear capturas de pantalla** y grabación de pantalla
- **Controlar Nearby Streaming** de notificaciones y apps
- **Prevenir exfiltración** de información visual sensible

---

## 📷 Métodos de Control de Cámara

### `setCameraDisabled(ComponentName admin, boolean disabled)`

**Propósito**: Deshabilita o habilita todas las cámaras del dispositivo (frontales y traseras). Cuando está deshabilitada, ninguna aplicación puede acceder a la cámara.

**Casos de Uso**:

**Deshabilitar Cámara**:
- **Ambientes seguros**: Instalaciones gubernamentales, militares
- **Espacios confidenciales**: Salas de juntas, centros de datos
- **Cumplimiento normativo**: SCIF (Sensitive Compartmented Information Facility)
- **Privacidad**: Prevenir grabaciones no autorizadas
- **Seguridad industrial**: Proteger propiedad intelectual
- **Exámenes**: Prevenir fraude académico

**Habilitar Cámara**:
- Dispositivos que requieren funcionalidad de cámara
- Apps de escaneo de códigos de barras/QR
- Videoconferencias corporativas

**Ejemplo de Uso**:
```kotlin
// Deshabilitar todas las cámaras
fun deshabilitarCamara() {
    dpm.setCameraDisabled(admin, true)
    Log.i("Camera", "📷❌ Cámara DESHABILITADA en todo el dispositivo")
    
    // Comportamiento:
    // - Todas las apps ven que no hay cámara disponible
    // - Intent de cámara falla
    // - Apps de videoconferencia no pueden usar cámara
    // - Escaneo de QR/códigos de barras no funciona
}

// Habilitar cámara nuevamente
fun habilitarCamara() {
    dpm.setCameraDisabled(admin, false)
    Log.i("Camera", "📷✅ Cámara HABILITADA")
}

// Control contextual basado en ubicación
fun controlarCamaraPorUbicacion(enAreaSegura: Boolean) {
    if (enAreaSegura) {
        dpm.setCameraDisabled(admin, true)
        mostrarNotificacion(
            "Cámara deshabilitada",
            "Ha entrado a un área donde la cámara no está permitida"
        )
    } else {
        dpm.setCameraDisabled(admin, false)
        mostrarNotificacion(
            "Cámara habilitada",
            "Puede usar la cámara en esta área"
        )
    }
}
```

**Comportamiento del Sistema**:
```
Cuando la cámara está deshabilitada:
❌ Apps de cámara no se abren
❌ Videoconferencias sin video
❌ Escaneo de QR/códigos no funciona
❌ Apps de AR/realidad aumentada fallan
❌ Instagram/Snapchat no pueden tomar fotos
✅ El dispositivo sigue funcionando normalmente en todo lo demás
```

**⚠️ Limitaciones**:
- No se puede deshabilitar selectivamente (frontal vs trasera)
- Todas las cámaras se deshabilitan
- Afecta a TODAS las aplicaciones sin excepción
- No hay whitelist de apps permitidas

**Alternativas Granulares**:
```kotlin
// Si necesitas control más granular, usar restricciones de usuario
fun deshabilitarCamaraConRestriccion() {
    // Método alternativo usando UserManager
    dpm.addUserRestriction(admin, UserManager.DISALLOW_CAMERA)
    
    // Ventaja: Puede combinarse con otras restricciones
    // Desventaja: Mismo resultado (todas las cámaras bloqueadas)
}
```

---

### `getCameraDisabled(ComponentName admin)`

**Propósito**: Verifica si la cámara está actualmente deshabilitada por este administrador específico.

**Retorna**: `boolean`
- `true`: Cámara deshabilitada por este admin
- `false`: Cámara habilitada (o deshabilitada por otro admin)

**Nota**: Si hay múltiples admins, la cámara está deshabilitada si AL MENOS UNO la deshabilita.

```kotlin
fun verificarEstadoCamara(): String {
    val deshabilitada = dpm.getCameraDisabled(admin)
    
    return buildString {
        append("📷 ESTADO DE CÁMARA\n")
        append("═══════════════════════\n\n")
        
        if (deshabilitada) {
            append("❌ Cámara: DESHABILITADA\n")
            append("   Ninguna app puede usar la cámara\n")
            append("   Videoconferencias sin video\n")
            append("   Escaneo de códigos bloqueado\n")
        } else {
            append("✅ Cámara: HABILITADA\n")
            append("   Apps pueden solicitar acceso a cámara\n")
            append("   Videoconferencias disponibles\n")
            append("   Funcionalidad completa\n")
        }
    }
}
```

---

## 🖼️ Métodos de Control de Captura de Pantalla

### `setScreenCaptureDisabled(ComponentName admin, boolean disabled)`

**Propósito**: Bloquea la captura de pantalla (screenshots) y grabación de pantalla en el dispositivo. Cuando está habilitado, los usuarios no pueden tomar capturas ni grabar la pantalla.

**Alcance del Bloqueo**:
- Screenshots (botones físicos Power + Volume Down)
- Grabación de pantalla (Screen Recorder)
- Google Assistant screenshots
- Apps de terceros de captura
- Reflejo de pantalla (casting) puede estar limitado

**Casos de Uso**:

**Bloquear Captura**:
- **DLP (Data Loss Prevention)**: Prevenir exfiltración de datos sensibles
- **Información confidencial**: Datos financieros, médicos, legales
- **Cumplimiento**: HIPAA, GDPR, PCI-DSS
- **Propiedad intelectual**: Proteger diseños, documentos corporativos
- **Exámenes**: Prevenir capturas de preguntas de evaluación
- **Apps de streaming**: DRM (Digital Rights Management)

**Ejemplo de Uso**:
```kotlin
// Bloquear captura de pantalla
fun bloquearCapturasPantalla() {
    dpm.setScreenCaptureDisabled(admin, true)
    Log.i("ScreenCapture", "🖼️❌ Capturas de pantalla BLOQUEADAS")
    
    // Comportamiento:
    // - Botones de screenshot no funcionan
    // - Apps de captura fallan
    // - Screen recording bloqueado
    // - Usuario ve mensaje "No se puede capturar contenido seguro"
}

// Permitir capturas nuevamente
fun permitirCapturasPantalla() {
    dpm.setScreenCaptureDisabled(admin, false)
    Log.i("ScreenCapture", "🖼️✅ Capturas de pantalla PERMITIDAS")
}

// Control dinámico según contenido
fun controlarCapturaPorContexto(mostrandoDatosSensibles: Boolean) {
    dpm.setScreenCaptureDisabled(admin, mostrandoDatosSensibles)
    
    if (mostrandoDatosSensibles) {
        Log.i("ScreenCapture", "Captura bloqueada - Datos sensibles en pantalla")
    }
}
```

**Comportamiento para el Usuario**:
```
Cuando captura está bloqueada:
❌ Botón screenshot muestra "No se puede capturar"
❌ Apps de captura reciben error de seguridad
❌ Screen recording no funciona
❌ Google Assistant no puede tomar screenshot
⚠️ Algunas apps pueden mostrar pantalla negra al compartir
```

**⚠️ Importante**:
- **Scope**: Afecta TODO el dispositivo o perfil
- **Profile Owner**: Solo bloquea en Work Profile
- **Device Owner**: Bloquea en todo el dispositivo
- **No hay whitelist**: No se pueden permitir apps específicas
- **Puede afectar productividad**: Usuarios no pueden compartir pantalla

**Limitaciones Técnicas**:
```kotlin
// ⚠️ NO PREVIENE:
// - Fotografiar la pantalla con otro dispositivo
// - Grabación externa con cámara
// - Malware con acceso root (dispositivos rooteados)
// - Acceso físico a través de HDMI/USB con hardware especializado

// ✅ SÍ PREVIENE:
// - Capturas normales por usuario
// - Compartir contenido accidentalmente
// - Exfiltración casual de datos
// - Screenshots de apps de terceros
```

---

### `getScreenCaptureDisabled(ComponentName admin)`

**Propósito**: Verifica si la captura de pantalla está bloqueada por este administrador.

**Retorna**: `boolean`

```kotlin
fun auditarPoliticasVisuales(): String {
    val camaraDeshabilitada = dpm.getCameraDisabled(admin)
    val capturaDeshabilitada = dpm.getScreenCaptureDisabled(admin)
    
    return buildString {
        append("🔒 POLÍTICAS DE SEGURIDAD VISUAL\n")
        append("═══════════════════════════════════\n\n")
        
        append("📷 Cámara: ${if (camaraDeshabilitada) "❌ BLOQUEADA" else "✅ Permitida"}\n")
        append("🖼️ Captura: ${if (capturaDeshabilitada) "❌ BLOQUEADA" else "✅ Permitida"}\n\n")
        
        if (camaraDeshabilitada || capturaDeshabilitada) {
            append("Nivel de seguridad: ")
            when {
                camaraDeshabilitada && capturaDeshabilitada -> {
                    append("🔒 MÁXIMO\n")
                    append("   Sin cámara ni capturas - Ambiente altamente seguro\n")
                }
                camaraDeshabilitada -> {
                    append("🔒 ALTO\n")
                    append("   Sin cámara - Grabación externa prevenida\n")
                }
                capturaDeshabilitada -> {
                    append("🔒 MEDIO\n")
                    append("   Sin capturas - Exfiltración digital prevenida\n")
                }
            }
        } else {
            append("Nivel de seguridad: ⚪ ESTÁNDAR\n")
            append("   Funcionalidad completa habilitada\n")
        }
    }
}
```

---

## 📡 Métodos de Nearby Streaming

### `setNearbyNotificationStreamingPolicy(int policy)`

**Propósito**: Controla si las notificaciones pueden transmitirse (streaming) a dispositivos cercanos (Nearby devices) como smartwatches, pantallas inteligentes, etc.

**Políticas Disponibles**:
- `NEARBY_STREAMING_ENABLED`: Streaming habilitado (por defecto)
- `NEARBY_STREAMING_DISABLED`: Streaming completamente bloqueado
- `NEARBY_STREAMING_SAME_MANAGED_ACCOUNT_ONLY`: Solo a dispositivos de la misma cuenta gestionada

**Casos de Uso**:
- **Privacidad**: Prevenir que notificaciones sensibles aparezcan en smartwatches
- **Seguridad**: Evitar exfiltración de información vía dispositivos wearables
- **Cumplimiento**: Regulaciones que requieren control de datos
- **DLP**: Prevenir fuga de notificaciones corporativas

**Ejemplo de Uso**:
```kotlin
// Bloquear streaming de notificaciones completamente
fun bloquearStreamingNotificaciones() {
    dpm.setNearbyNotificationStreamingPolicy(
        DevicePolicyManager.NEARBY_STREAMING_DISABLED
    )
    Log.i("Nearby", "📡❌ Streaming de notificaciones BLOQUEADO")
    
    // Las notificaciones NO aparecerán en:
    // - Smartwatches
    // - Pantallas inteligentes
    // - Otros dispositivos Nearby
}

// Permitir solo en dispositivos corporativos
fun permitirStreamingSoloGestionado() {
    dpm.setNearbyNotificationStreamingPolicy(
        DevicePolicyManager.NEARBY_STREAMING_SAME_MANAGED_ACCOUNT_ONLY
    )
    Log.i("Nearby", "📡⚠️ Streaming solo a dispositivos gestionados")
}

// Habilitar streaming completo
fun habilitarStreamingCompleto() {
    dpm.setNearbyNotificationStreamingPolicy(
        DevicePolicyManager.NEARBY_STREAMING_ENABLED
    )
    Log.i("Nearby", "📡✅ Streaming de notificaciones HABILITADO")
}
```

---

### `getNearbyNotificationStreamingPolicy()`

**Propósito**: Obtiene la política actual de streaming de notificaciones.

**Retorna**: `int` con la política configurada.

---

### `setNearbyAppStreamingPolicy(int policy)`

**Propósito**: Controla si el contenido de las aplicaciones puede transmitirse a dispositivos cercanos.

**Diferencia con Notificaciones**:
- **Notification Streaming**: Solo notificaciones
- **App Streaming**: Contenido completo de apps (pantalla, audio, etc.)

**Ejemplo de Uso**:
```kotlin
// Bloquear streaming de apps
fun bloquearStreamingApps() {
    dpm.setNearbyAppStreamingPolicy(
        DevicePolicyManager.NEARBY_STREAMING_DISABLED
    )
    Log.i("Nearby", "📱❌ Streaming de apps BLOQUEADO")
    
    // Previene:
    // - Casting de pantalla a dispositivos cercanos
    // - Compartir contenido de apps
    // - Proyección inalámbrica
}
```

---

### `getNearbyAppStreamingPolicy()`

**Propósito**: Obtiene la política actual de streaming de apps.

**Retorna**: `int` con la política configurada.

---

## 🎯 Casos de Uso por Escenario

### 🏦 Financiero/Bancario - Máxima Seguridad
```kotlin
fun configurarSeguridadFinanciera() {
    // Bloquear TODO: cámara, capturas, streaming
    dpm.setCameraDisabled(admin, true)
    dpm.setScreenCaptureDisabled(admin, true)
    dpm.setNearbyNotificationStreamingPolicy(
        DevicePolicyManager.NEARBY_STREAMING_DISABLED
    )
    dpm.setNearbyAppStreamingPolicy(
        DevicePolicyManager.NEARBY_STREAMING_DISABLED
    )
    
    Log.i("Security", """
        🔒 CONFIGURACIÓN DE MÁXIMA SEGURIDAD
        ════════════════════════════════════
        ❌ Cámara bloqueada
        ❌ Capturas bloqueadas
        ❌ Streaming notificaciones bloqueado
        ❌ Streaming apps bloqueado
        
        Uso: Datos financieros altamente sensibles
        Cumplimiento: PCI-DSS, SOX, regulaciones bancarias
    """.trimIndent())
}
```

---

### 🏥 Médico/HIPAA - Protección de PHI
```kotlin
fun configurarSeguridadMedica() {
    // Bloquear capturas y streaming (cámara puede necesitarse para telemedicina)
    dpm.setCameraDisabled(admin, false)  // Permitir para videoconferencias médicas
    dpm.setScreenCaptureDisabled(admin, true)  // Proteger PHI en pantalla
    dpm.setNearbyNotificationStreamingPolicy(
        DevicePolicyManager.NEARBY_STREAMING_DISABLED
    )
    dpm.setNearbyAppStreamingPolicy(
        DevicePolicyManager.NEARBY_STREAMING_DISABLED
    )
    
    Log.i("Security", """
        🏥 CONFIGURACIÓN HIPAA
        ═══════════════════════
        ✅ Cámara permitida (telemedicina)
        ❌ Capturas bloqueadas (proteger PHI)
        ❌ Streaming bloqueado (privacidad)
        
        Cumplimiento: HIPAA, privacidad de pacientes
    """.trimIndent())
}
```

---

## ⚠️ Consideraciones Importantes

### Cámara

**Impacto en Funcionalidad**:
- ❌ Videoconferencias (Teams, Zoom, Meet)
- ❌ Escaneo de QR/códigos de barras
- ❌ Apps de realidad aumentada
- ❌ Reconocimiento facial
- ❌ Fotocopia/escaneo de documentos

**Alternativas**:
- Usar dispositivos externos sin cámara integrada
- Tapa física de cámara (hardware)
- Restricciones a nivel de app en lugar de dispositivo

### Captura de Pantalla

**Impacto en Productividad**:
- ❌ No compartir pantalla en presentaciones
- ❌ No documentar errores/bugs con screenshots
- ❌ No compartir información legítima
- ❌ Frustración de usuarios

**Balance Necesario**:
- Solo en áreas con datos realmente sensibles
- Considerar habilitar/deshabilitar dinámicamente
- Educar usuarios sobre razones de seguridad

### Nearby Streaming

**Dispositivos Afectados**:
- Smartwatches (Wear OS, Apple Watch en BYOD)
- Google Home / Nest Hub
- Chromecast
- Android Auto
- Dispositivos IoT cercanos

**Impacto en UX**:
- Usuario no verá notificaciones en smartwatch
- Casting de presentaciones bloqueado
- Funciones de continuidad deshabilitadas

### Cumplimiento vs Usabilidad

**Matriz de Decisión**:

| Escenario | Cámara | Captura | Streaming | Justificación |
|-----------|--------|---------|-----------|---------------|
| **Gobierno/Defensa** | ❌ | ❌ | ❌ | Máxima seguridad nacional |
| **Financiero** | ❌ | ❌ | ❌ | Proteger datos financieros |
| **Médico** | ✅ | ❌ | ❌ | Telemedicina necesaria, PHI protegido |
| **Legal** | ❌ | ❌ | ⚠️ | Privilegio abogado-cliente |
| **Corporativo Estándar** | ✅ | ⚠️ | ✅ | Balance productividad/seguridad |
| **Educación (examen)** | ❌ | ❌ | ❌ | Prevenir fraude |
| **Educación (normal)** | ✅ | ✅ | ✅ | Funcionalidad completa |

**Leyenda**: ❌ Bloqueado | ⚠️ Selectivo | ✅ Permitido

### Mejores Prácticas

1. **Contextual**: Habilitar/deshabilitar según contexto (ubicación, app activa)

2. **Notificar**: Informar claramente por qué está bloqueado

3. **Documentar**: Mantener políticas documentadas y justificadas

4. **Revisar**: Auditar regularmente si restricciones siguen siendo necesarias

5. **Gradual**: Implementar incrementalmente, no todo de golpe

6. **Excepciones**: Considerar proceso para excepciones justificadas

---

## 📚 Próxima Categoría

**12. Configuraciones del Sistema** (~40 métodos)

Esta categoría cubrirá:
- Global, Secure y System settings
- Configuración de tiempo y zona horaria
- Master volume
- Status bar
- Nombre y color de organización
- Mensajes de soporte
- Configuración de locales

---

*Documentación generada para DevicePolicyManager API de Android Enterprise - Categoría 11 de 22*