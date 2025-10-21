# Documentación DevicePolicyManager - Categoría 21: Content Protection Policy

## 📋 Visión General

La categoría de **Content Protection Policy** agrupa aproximadamente **3 métodos** que permiten establecer políticas de protección de contenido para prevenir la captura de pantalla y grabación de contenido sensible en aplicaciones específicas.

## 🎯 Propósito

Estos métodos permiten:
- **Prevenir captura** de pantalla en apps sensibles
- **Proteger contenido** corporativo/confidencial
- **Control granular** por aplicación
- **DLP a nivel de app** (Data Loss Prevention)
- **Complementar políticas** de seguridad existentes

---

## 🎬 Conceptos Fundamentales

### Content Protection

**Qué es**:
```
Content Protection Policy:
├── Políticas para prevenir captura de contenido en apps específicas
├── Más granular que setScreenCaptureDisabled() (todo el dispositivo)
├── Control por paquete/aplicación
└── Protege contra screenshots y grabación de pantalla

Diferencias con métodos previos:
├── setScreenCaptureDisabled() → TODO el dispositivo
└── Content Protection Policy → Apps específicas (granular)
```

**Niveles de Protección**:
```
Sin Protección:
└── Usuario puede capturar pantalla libremente

Protección Básica (App implementa FLAG_SECURE):
├── App individual previene captura
├── Requiere modificación de código de app
└── Developer debe implementar

Protección Corporativa (Content Protection Policy):
├── Admin puede forzar protección sin modificar app
├── Aplicable a apps de terceros
└── Control centralizado por MDM
```

**Vectores de Fuga de Información**:
```
Captura de Contenido:
├── Screenshot (botones físicos)
├── Screen recording (grabación de pantalla)
├── Google Assistant screenshot
├── Apps de captura de terceros
├── Casting/mirroring a pantallas externas
└── Acceso de apps de accesibilidad

Content Protection previene:
✅ Screenshots
✅ Screen recording
✅ Casting (parcialmente)
⚠️ Fotografía de pantalla con otro dispositivo (no previene)
```

---

## 🔒 Métodos de Content Protection

### `setContentProtectionPolicy(ComponentName admin, int policy)`

**Propósito**: Establece la política de protección de contenido que determina qué aplicaciones deben tener su contenido protegido contra capturas de pantalla y grabación.

**Disponibilidad**: Android 15+ (API 35)

**Políticas Disponibles**:
```kotlin
DevicePolicyManager.CONTENT_PROTECTION_DISABLED (0)
└── Sin protección - captura permitida en todas las apps

DevicePolicyManager.CONTENT_PROTECTION_ENABLED (1)
└── Protección habilitada - apps sensibles protegidas
```

**Casos de Uso**:
- **Apps financieras**: Proteger información de cuentas y transacciones
- **Apps médicas**: Prevenir captura de PHI (Protected Health Information)
- **Apps corporativas**: Proteger documentos y comunicaciones sensibles
- **Apps legales**: Prevenir captura de información privilegiada
- **Apps de RH**: Proteger información personal de empleados

**Ejemplo de Uso**:
```kotlin
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
fun habilitarContentProtection() {
    dpm.setContentProtectionPolicy(
        admin,
        DevicePolicyManager.CONTENT_PROTECTION_ENABLED
    )
    
    Log.i("ContentProtection", "🔒 Content Protection HABILITADA")
    Log.i("ContentProtection", "   Apps sensibles protegidas contra captura")
    Log.i("ContentProtection", "   Screenshots y grabación bloqueados en apps corporativas")
}

@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
fun deshabilitarContentProtection() {
    dpm.setContentProtectionPolicy(
        admin,
        DevicePolicyManager.CONTENT_PROTECTION_DISABLED
    )
    
    Log.i("ContentProtection", "✅ Content Protection DESHABILITADA")
    Log.i("ContentProtection", "   Captura de pantalla permitida en todas las apps")
}

// Habilitar protección con notificación al usuario
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
fun habilitarConNotificacion() {
    dpm.setContentProtectionPolicy(
        admin,
        DevicePolicyManager.CONTENT_PROTECTION_ENABLED
    )
    
    mostrarNotificacion(
        "Protección de Contenido Activa",
        "Algunas apps corporativas no permitirán capturas de pantalla por seguridad."
    )
    
    Log.i("ContentProtection", "Protección habilitada - usuario notificado")
}
```

**Comportamiento del Sistema**:
```
Content Protection HABILITADA:

Apps Corporativas/Sensibles:
├── Screenshot (Power + Vol Down) → Bloqueado
├── Screen recording → Bloqueado
├── Assistant screenshot → Bloqueado
├── Casting → Pantalla negra en app sensible
└── Usuario ve mensaje: "No se puede capturar contenido seguro"

Apps No Sensibles:
└── Funcionan normalmente, captura permitida

Determinación de apps sensibles:
├── Apps que manejan datos corporativos
├── Apps con FLAG_SECURE en código
├── Apps definidas por MDM como sensibles
└── Criterio del sistema/fabricante
```

**⚠️ Limitaciones**:
```
NO previene:
❌ Fotografiar pantalla con otro dispositivo
❌ Grabación con cámara externa
❌ Shoulder surfing (mirar por encima del hombro)
❌ Acceso físico al dispositivo

SÍ previene:
✅ Screenshots digitales
✅ Screen recording del sistema
✅ Captura por apps de terceros
✅ Casting de contenido sensible
```

---

### `getContentProtectionPolicy(ComponentName admin)`

**Propósito**: Obtiene la política de protección de contenido actualmente configurada.

**Disponibilidad**: Android 15+ (API 35)

**Retorna**: `int`
- `CONTENT_PROTECTION_DISABLED` (0): Protección deshabilitada
- `CONTENT_PROTECTION_ENABLED` (1): Protección habilitada

**Ejemplo de Uso**:
```kotlin
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
fun verificarContentProtection(): String {
    val policy = dpm.getContentProtectionPolicy(admin)
    
    return buildString {
        append("🔒 CONTENT PROTECTION POLICY\n")
        append("═══════════════════════════════════\n\n")
        
        when (policy) {
            DevicePolicyManager.CONTENT_PROTECTION_ENABLED -> {
                append("Estado: ✅ HABILITADA\n")
                append("Protección: ACTIVA\n\n")
                append("Apps corporativas/sensibles:\n")
                append("  ❌ Screenshot bloqueado\n")
                append("  ❌ Screen recording bloqueado\n")
                append("  ❌ Casting bloqueado\n")
                append("  ❌ Captura por accesibilidad bloqueada\n\n")
                append("Nivel de seguridad: 🔒 ALTO\n")
                append("DLP: Protección activa contra fuga visual\n")
            }
            
            DevicePolicyManager.CONTENT_PROTECTION_DISABLED -> {
                append("Estado: ⚠️ DESHABILITADA\n")
                append("Protección: INACTIVA\n\n")
                append("Todas las apps:\n")
                append("  ✅ Screenshot permitido\n")
                append("  ✅ Screen recording permitido\n")
                append("  ✅ Casting permitido\n\n")
                append("Nivel de seguridad: ⚠️ ESTÁNDAR\n")
                append("Riesgo: Contenido sensible puede ser capturado\n")
            }
            
            else -> {
                append("Estado: ❓ DESCONOCIDO\n")
                append("Valor: $policy\n")
            }
        }
    }
}

// Auditar y alertar si no está habilitada
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
fun auditarContentProtection() {
    val policy = dpm.getContentProtectionPolicy(admin)
    
    if (policy == DevicePolicyManager.CONTENT_PROTECTION_DISABLED) {
        Log.w("Security", "⚠️ ALERTA: Content Protection deshabilitada")
        Log.w("Security", "   Apps sensibles vulnerables a captura")
        Log.w("Security", "   Recomendación: Habilitar inmediatamente")
        
        notificarAdminSeguridad("Content Protection no configurada")
    } else {
        Log.i("Security", "✅ Content Protection activa")
    }
}
```

---

## 🛡️ Integración con Políticas Existentes

### Combinación con setScreenCaptureDisabled()

**Diferencias y Complementariedad**:
```kotlin
// Comparación de métodos

// 1. setScreenCaptureDisabled() - Global
fun proteccionGlobal() {
    // Bloquea TODO el dispositivo
    dpm.setScreenCaptureDisabled(admin, true)
    
    // Resultado:
    // ❌ NINGUNA app puede capturar pantalla
    // ❌ Screenshot bloqueado en Settings, launcher, todas las apps
    // ⚠️ Muy restrictivo, afecta productividad
}

// 2. Content Protection Policy - Granular
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
fun proteccionGranular() {
    // Bloquea solo apps sensibles
    dpm.setContentProtectionPolicy(
        admin,
        DevicePolicyManager.CONTENT_PROTECTION_ENABLED
    )
    
    // Resultado:
    // ❌ Apps corporativas/sensibles: captura bloqueada
    // ✅ Apps normales: captura permitida
    // ✅ Balance seguridad/productividad
}

// 3. Combinación recomendada
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
fun proteccionOptima(nivelSeguridad: NivelSeguridad) {
    when (nivelSeguridad) {
        NivelSeguridad.MAXIMA -> {
            // Seguridad máxima: bloquear todo
            dpm.setScreenCaptureDisabled(admin, true)
            dpm.setContentProtectionPolicy(admin, 
                DevicePolicyManager.CONTENT_PROTECTION_ENABLED)
            Log.i("Security", "🔒 Protección máxima: TODO bloqueado")
        }
        
        NivelSeguridad.ALTA -> {
            // Alta seguridad: solo apps sensibles
            dpm.setScreenCaptureDisabled(admin, false)
            dpm.setContentProtectionPolicy(admin, 
                DevicePolicyManager.CONTENT_PROTECTION_ENABLED)
            Log.i("Security", "🔒 Protección alta: Apps sensibles bloqueadas")
        }
        
        NivelSeguridad.MEDIA -> {
            // Seguridad media: sin protección forzada
            dpm.setScreenCaptureDisabled(admin, false)
            dpm.setContentProtectionPolicy(admin, 
                DevicePolicyManager.CONTENT_PROTECTION_DISABLED)
            Log.i("Security", "⚠️ Protección media: Sin bloqueos")
        }
    }
}

enum class NivelSeguridad {
    MAXIMA, ALTA, MEDIA
}
```

---

## 🎯 Caso de Uso: Sistema DLP Completo

```kotlin
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
class DLPManager(
    private val context: Context,
    private val dpm: DevicePolicyManager,
    private val admin: ComponentName
) {
    
    fun aplicarPoliticasDLP(industria: Industria) {
        when (industria) {
            Industria.FINANCIERA -> configurarFinanciera()
            Industria.MEDICA -> configurarMedica()
            Industria.LEGAL -> configurarLegal()
            Industria.CORPORATIVA -> configurarCorporativa()
            Industria.EDUCACION -> configurarEducacion()
        }
    }
    
    private fun configurarFinanciera() {
        // Máxima protección: bloquear todo
        dpm.setScreenCaptureDisabled(admin, true)
        dpm.setContentProtectionPolicy(admin, 
            DevicePolicyManager.CONTENT_PROTECTION_ENABLED)
        
        // Restricciones adicionales
        dpm.addUserRestriction(admin, UserManager.DISALLOW_PRINTING)
        dpm.setUsbDataSignalingEnabled(false)
        
        Log.i("DLP", """
            🏦 DLP FINANCIERO APLICADO
            ═══════════════════════════
            🔒 Screenshot: Bloqueado globalmente
            🔒 Content Protection: Habilitada
            🔒 Printing: Bloqueado
            🔒 USB Data: Bloqueado
            
            Cumplimiento: PCI-DSS, SOX, GLBA
        """.trimIndent())
    }
    
    private fun configurarMedica() {
        // Protección alta: apps sensibles
        dpm.setScreenCaptureDisabled(admin, false)
        dpm.setContentProtectionPolicy(admin, 
            DevicePolicyManager.CONTENT_PROTECTION_ENABLED)
        
        // Restricciones específicas
        dpm.addUserRestriction(admin, UserManager.DISALLOW_PRINTING)
        dpm.setBluetoothContactSharingDisabled(admin, true)
        
        Log.i("DLP", """
            🏥 DLP MÉDICO APLICADO
            ═══════════════════════
            ✅ Screenshot: Permitido en apps generales
            🔒 Content Protection: Apps médicas protegidas
            🔒 Printing: Bloqueado
            🔒 Bluetooth Contacts: Bloqueado
            
            Cumplimiento: HIPAA
        """.trimIndent())
    }
    
    private fun configurarLegal() {
        // Protección similar a médico
        dpm.setScreenCaptureDisabled(admin, false)
        dpm.setContentProtectionPolicy(admin, 
            DevicePolicyManager.CONTENT_PROTECTION_ENABLED)
        
        Log.i("DLP", "⚖️ DLP Legal aplicado - privilegio abogado-cliente protegido")
    }
    
    private fun configurarCorporativa() {
        // Balance: protección moderada
        dpm.setScreenCaptureDisabled(admin, false)
        dpm.setContentProtectionPolicy(admin, 
            DevicePolicyManager.CONTENT_PROTECTION_ENABLED)
        
        Log.i("DLP", "🏢 DLP Corporativo - protección balanceada")
    }
    
    private fun configurarEducacion() {
        // Mínima protección: permitir capturas para aprendizaje
        dpm.setScreenCaptureDisabled(admin, false)
        dpm.setContentProtectionPolicy(admin, 
            DevicePolicyManager.CONTENT_PROTECTION_DISABLED)
        
        Log.i("DLP", "🎓 DLP Educación - capturas permitidas")
    }
    
    enum class Industria {
        FINANCIERA, MEDICA, LEGAL, CORPORATIVA, EDUCACION
    }
}
```

---

## ⚠️ Consideraciones Importantes

### Compatibilidad

**Disponibilidad Limitada**:
```
Android 15+ (API 35):
├── API muy nueva
├── Pocos dispositivos actualmente
├── Requiere tiempo de adopción
└── Verificar soporte antes de usar

Alternativas para versiones anteriores:
├── setScreenCaptureDisabled() - Global (API 21+)
├── FLAG_SECURE en apps propias
└── Educar usuarios sobre no capturar
```

### Determinación de Apps Sensibles

**Cómo se determina qué apps proteger**:
```
Criterios del Sistema:
├── Apps con FLAG_SECURE en código (developer marca como sensible)
├── Apps que manejan contenido DRM
├── Apps corporativas instaladas por MDM
├── Apps en Work Profile (contexto corporativo)
└── Criterio del fabricante/OEM

Limitación:
└── Admin no puede especificar lista exacta de apps
    (al menos en implementación inicial de API 35)
```

### Impacto en Usuario

**Experiencia de Usuario**:
```
Usuario intenta captura en app protegida:
├── Presiona botones de screenshot
├── Animación de captura NO aparece
├── Mensaje: "No se puede capturar contenido seguro"
├── No se guarda imagen
└── Usuario puede frustrarse si no entiende por qué

Mitigación:
├── Comunicar política claramente
├── Explicar razones (seguridad, cumplimiento)
├── Documentar qué apps están protegidas
└── Alternativas si usuario necesita documentar algo
```

### Limitaciones Técnicas

**Qué NO protege**:
```
Ataques Físicos:
❌ Fotografiar pantalla con cámara
❌ Grabación con cámara externa
❌ Shoulder surfing (mirar pantalla)

Ataques Avanzados:
❌ Malware con root access
❌ Custom ROM sin protecciones
❌ Hardware modificado (HDMI capture)

Protección Limitada:
⚠️ Casting puede mostrar pantalla negra pero no siempre
⚠️ Servicios de accesibilidad pueden tener acceso
⚠️ Apps de screenshot con permisos especiales
```

---

## 💡 Recomendaciones

### Estrategia de Implementación
- **Preferir Content Protection** sobre bloqueo global cuando sea posible (mejor UX)
- **Combinar con setScreenCaptureDisabled()** solo para seguridad máxima
- **Comunicar política** claramente a usuarios antes de aplicar
- **Monitorear feedback** de usuarios sobre impacto en productividad

### Por Industria
- **Financiero/Gobierno**: Usar ambos métodos (global + granular)
- **Salud (HIPAA)**: Content Protection suficiente, combinado con otras DLP
- **Legal**: Content Protection para privilegio abogado-cliente
- **Corporativo general**: Content Protection balanceado
- **Educación**: Generalmente no bloquear (capturas útiles para aprendizaje)

### Testing
- **Validar en dispositivos** con Android 15+ antes de despliegue
- **Verificar qué apps** son consideradas sensibles por el sistema
- **Probar experiencia** de usuario al intentar captura
- **Documentar comportamiento** por fabricante (pueden variar)

### Alternativas para Versiones Anteriores
- **FLAG_SECURE en apps propias** (developer implementa)
- **setScreenCaptureDisabled()** global (Android 5+)
- **Educación y políticas** de usuario
- **Watermarking** de contenido sensible

### Balance Seguridad/Productividad
- **Evaluar impacto real** en workflows diarios
- **Excepciones justificadas** con proceso aprobación
- **Revisión periódica** de política
- **Feedback continuo** de usuarios

### Auditoría y Compliance
- **Documentar política** de protección de contenido
- **Registrar configuración** en sistema MDM
- **Auditorías regulares** de estado de protección
- **Reportes** para compliance regulatorio

---

## 📊 Auditoría Content Protection

```kotlin
@RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
fun generarReporteContentProtection(): String {
    return buildString {
        append("═══════════════════════════════════════════\n")
        append("      REPORTE CONTENT PROTECTION POLICY\n")
        append("═══════════════════════════════════════════\n\n")
        
        // Estado Content Protection
        append("🔒 CONTENT PROTECTION\n")
        append("───────────────────────────────────────────\n")
        
        val policy = dpm.getContentProtectionPolicy(admin)
        
        when (policy) {
            DevicePolicyManager.CONTENT_PROTECTION_ENABLED -> {
                append("Estado: ✅ HABILITADA\n")
                append("Apps sensibles: Protegidas\n")
                append("Nivel de seguridad: ALTO\n")
            }
            DevicePolicyManager.CONTENT_PROTECTION_DISABLED -> {
                append("Estado: ⚠️ DESHABILITADA\n")
                append("Apps sensibles: Sin protección\n")
                append("Nivel de seguridad: BAJO\n")
            }
            else -> {
                append("Estado: ❓ DESCONOCIDO ($policy)\n")
            }
        }
        
        append("\n")
        
        // Screen Capture Global
        append("📸 SCREEN CAPTURE GLOBAL\n")
        append("───────────────────────────────────────────\n")
        
        val screenCaptureDisabled = dpm.getScreenCaptureDisabled(admin)
        append("Estado: ${if (screenCaptureDisabled) "🔒 BLOQUEADO" else "✅ Permitido"}\n")
        
        if (screenCaptureDisabled) {
            append("Alcance: TODO el dispositivo\n")
        }
        
        append("\n")
        
        // Análisis combinado
        append("🛡️ ANÁLISIS DE PROTECCIÓN\n")
        append("───────────────────────────────────────────\n")
        
        when {
            screenCaptureDisabled && policy == DevicePolicyManager.CONTENT_PROTECTION_ENABLED -> {
                append("Configuración: 🔒 MÁXIMA SEGURIDAD\n")
                append("  • Bloqueo global activo\n")
                append("  • Content Protection habilitada\n")
                append("  • Redundancia de protección\n")
                append("Uso recomendado: Financiero, Gobierno\n")
            }
            !screenCaptureDisabled && policy == DevicePolicyManager.CONTENT_PROTECTION_ENABLED -> {
                append("Configuración: ✅ BALANCEADA\n")
                append("  • Solo apps sensibles protegidas\n")
                append("  • Apps normales permiten captura\n")
                append("  • Balance seguridad/productividad\n")
                append("Uso recomendado: Corporativo, Salud, Legal\n")
            }
            !screenCaptureDisabled && policy == DevicePolicyManager.CONTENT_PROTECTION_DISABLED -> {
                append("Configuración: ⚠️ SIN PROTECCIÓN\n")
                append("  • Sin bloqueos de captura\n")
                append("  • Máxima productividad\n")
                append("  • Riesgo de fuga de información\n")
                append("Uso recomendado: Educación, bajo riesgo\n")
            }
            else -> {
                append("Configuración: ❓ MIXTA\n")
            }
        }
        
        append("\n")
        
        // Recomendaciones
        append("💡 RECOMENDACIONES\n")
        append("───────────────────────────────────────────\n")
        
        if (policy == DevicePolicyManager.CONTENT_PROTECTION_DISABLED && !screenCaptureDisabled) {
            append("⚠️ Sin protección contra captura de contenido\n")
            append("Acción: Habilitar Content Protection mínimo\n")
        } else if (screenCaptureDisabled && policy == DevicePolicyManager.CONTENT_PROTECTION_ENABLED) {
            append("✅ Protección óptima configurada\n")
            append("Mantener configuración actual\n")
        } else {
            append("✅ Configuración adecuada para el contexto\n")
        }
        
        append("\n")
        append("Generado: ${Date()}\n")
    }
}
```

---

## 📚 Próxima Categoría

**22. Otros Métodos Importantes** (~30 métodos)

Esta categoría cubrirá:
- Notificaciones y Do Not Disturb
- Localización
- Personal apps suspended
- End of Life (EOL)
- Common Criteria Mode
- Biometric authentication
- Preferential network services
- Cross-profile packages
- Managed subscriptions
- Métodos misceláneos

---

*Documentación generada para DevicePolicyManager API de Android Enterprise - Categoría 21 de 22*