# Documentación DevicePolicyManager - Categoría 15: Input Methods (Teclados)

## 📋 Visión General

La categoría de **Input Methods (Teclados)** agrupa aproximadamente **5 métodos** que permiten controlar qué métodos de entrada (teclados) y servicios de accesibilidad pueden usar los usuarios en dispositivos administrados. Es fundamental para seguridad, ya que teclados maliciosos pueden capturar información sensible.

## 🎯 Propósito

Estos métodos permiten:
- **Controlar teclados** permitidos en el dispositivo
- **Gestionar servicios** de accesibilidad
- **Prevenir keyloggers** y teclados maliciosos
- **Whitelist/blacklist** de IMEs específicos
- **Proteger entrada** de información sensible

---

## ⌨️ Conceptos Fundamentales

### Input Method Editors (IME)

**Qué son**:
- Teclados virtuales del sistema Android
- Capturan TODA la entrada del usuario
- Tienen acceso a contraseñas, datos personales, comunicaciones
- Pueden enviar datos a servidores externos

**Riesgo de Seguridad**:
```
Teclado Malicioso Puede:
├── Capturar contraseñas (keylogging)
├── Leer mensajes privados
├── Interceptar datos financieros
├── Enviar información a servidores remotos
├── Inyectar texto automáticamente
└── Modificar entrada del usuario
```

**Teclados Comunes**:
- Gboard (Google)
- SwiftKey (Microsoft)
- Samsung Keyboard
- Teclados de terceros (pueden ser riesgosos)

---

## 🔐 Métodos de Control de Teclados

### `setPermittedInputMethods(ComponentName admin, List<String> packageNames)`

**Propósito**: Define una **whitelist** de teclados (IMEs) que pueden ser usados en el dispositivo. Si se configura, solo los teclados en la lista pueden ser habilitados por el usuario. Cualquier otro teclado queda bloqueado.

**Parámetros**:
- `packageNames`: Lista de package names permitidos
- `null`: Sin restricciones (todos los teclados permitidos)

**Casos de Uso**:

**Whitelist Estricta**:
- 🏦 **Banca/Finanzas**: Solo teclados de confianza verificados
- 🏥 **Salud/HIPAA**: Prevenir captura de información médica
- 🏛️ **Gobierno**: Solo teclados aprobados por seguridad
- 🏢 **Corporativo**: Control de entrada de datos sensibles

**Sin Restricciones**:
- Dispositivos personales con Work Profile
- Ambientes de bajo riesgo
- Preferencia del usuario prioritaria

**Ejemplo de Uso**:
```kotlin
// Permitir solo teclados confiables
fun configurarTecladosPermitidos() {
    val tecladosPermitidos = listOf(
        "com.google.android.inputmethod.latin",  // Gboard (Google)
        "com.samsung.android.honeyboard",        // Samsung Keyboard
        "com.sec.android.inputmethod"            // Samsung IME alternativo
    )
    
    dpm.setPermittedInputMethods(admin, tecladosPermitidos)
    
    Log.i("IME", "⌨️ Whitelist de teclados configurada")
    Log.i("IME", "   Teclados permitidos: ${tecladosPermitidos.size}")
    tecladosPermitidos.forEach { pkg ->
        Log.i("IME", "   ✅ $pkg")
    }
    
    // Comportamiento:
    // - Solo estos teclados pueden ser habilitados
    // - Otros teclados instalados quedan deshabilitados
    // - Usuario no puede habilitar teclados no permitidos
    // - Intento de habilitar otro teclado es bloqueado silenciosamente
}

// Configuración de máxima seguridad (solo Gboard)
fun configurarMaximaSeguridad() {
    val soloGboard = listOf("com.google.android.inputmethod.latin")
    dpm.setPermittedInputMethods(admin, soloGboard)
    
    Log.i("IME", "🔒 Solo Gboard permitido (máxima seguridad)")
}

// Remover restricciones
fun permitirTodosTeclados() {
    dpm.setPermittedInputMethods(admin, null)
    
    Log.i("IME", "✅ Todos los teclados permitidos")
}

// Configuración por industria
fun configurarPorIndustria(industria: Industria) {
    val teclados = when (industria) {
        Industria.FINANCIERA -> listOf(
            "com.google.android.inputmethod.latin"  // Solo Gboard
        )
        
        Industria.MEDICA -> listOf(
            "com.google.android.inputmethod.latin",
            "com.empresa.medical_keyboard"  // Teclado médico especializado
        )
        
        Industria.GOBIERNO -> listOf(
            "gov.approved.keyboard"  // Solo teclado aprobado gubernamental
        )
        
        Industria.CORPORATIVA -> listOf(
            "com.google.android.inputmethod.latin",
            "com.microsoft.swiftkey.keyboard"
        )
        
        Industria.EDUCACION -> null  // Sin restricciones
    }
    
    dpm.setPermittedInputMethods(admin, teclados)
}

enum class Industria {
    FINANCIERA, MEDICA, GOBIERNO, CORPORATIVA, EDUCACION
}
```

**Comportamiento del Sistema**:
```
Con Whitelist Configurada:
✅ Teclado en lista → Usuario puede habilitarlo
❌ Teclado fuera de lista → No se puede habilitar
❌ Teclado ya habilitado → Se deshabilita automáticamente
⚠️ Sin teclado habilitado → Sistema usa teclado por defecto de la lista

Proceso:
1. Admin configura whitelist
2. Sistema verifica teclados habilitados del usuario
3. Teclados no permitidos se deshabilitan automáticamente
4. Usuario intenta habilitar teclado no permitido → Bloqueado
5. Solo teclados de la lista aparecen como opciones
```

**⚠️ CRÍTICO - Validar Teclado por Defecto**:
```kotlin
fun validarTecladoPorDefecto() {
    val tecladosPermitidos = listOf("com.google.android.inputmethod.latin")
    
    // IMPORTANTE: Verificar que al menos un teclado de la lista está instalado
    val algunoInstalado = tecladosPermitidos.any { pkg ->
        estaInstalado(pkg)
    }
    
    if (!algunoInstalado) {
        Log.e("IME", "❌ PELIGRO: Ningún teclado permitido está instalado!")
        Log.e("IME", "   Usuario no podrá escribir texto")
        Log.e("IME", "   Instalar teclado antes de aplicar restricción")
        return
    }
    
    dpm.setPermittedInputMethods(admin, tecladosPermitidos)
}

fun estaInstalado(packageName: String): Boolean {
    return try {
        context.packageManager.getPackageInfo(packageName, 0)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }
}
```

---

### `getPermittedInputMethods(ComponentName admin)`

**Propósito**: Obtiene la lista de teclados permitidos actualmente configurada.

**Retorna**:
- `List<String>`: Package names de teclados permitidos
- `null`: Sin restricciones (todos permitidos)

**Ejemplo de Uso**:
```kotlin
fun auditarTecladosPermitidos(): String {
    val permitidos = dpm.getPermittedInputMethods(admin)
    
    return buildString {
        append("⌨️ TECLADOS PERMITIDOS\n")
        append("═══════════════════════════════\n\n")
        
        if (permitidos == null) {
            append("✅ Sin restricciones\n")
            append("   Todos los teclados permitidos\n")
            append("   Usuario tiene libertad de elección\n")
        } else {
            append("🔒 Whitelist activa\n")
            append("   Teclados permitidos: ${permitidos.size}\n\n")
            
            if (permitidos.isEmpty()) {
                append("⚠️ ADVERTENCIA: Lista vacía\n")
                append("   Ningún teclado permitido\n")
                append("   Usuario no puede escribir!\n")
            } else {
                permitidos.forEach { pkg ->
                    val instalado = estaInstalado(pkg)
                    val nombre = obtenerNombreTeclado(pkg)
                    
                    append("${if (instalado) "✅" else "❌"} $nombre\n")
                    append("   Package: $pkg\n")
                    append("   Instalado: ${if (instalado) "Sí" else "NO"}\n\n")
                }
            }
        }
        
        // Teclados instalados NO permitidos
        append("\n📦 TECLADOS INSTALADOS NO PERMITIDOS\n")
        append("─────────────────────────────────────\n")
        val bloqueados = obtenerTecladosBloqueados(permitidos)
        
        if (bloqueados.isEmpty()) {
            append("✅ Ninguno\n")
        } else {
            bloqueados.forEach { pkg ->
                append("❌ ${obtenerNombreTeclado(pkg)}\n")
                append("   $pkg\n")
            }
        }
    }
}

fun obtenerNombreTeclado(packageName: String): String {
    return try {
        val appInfo = context.packageManager.getApplicationInfo(packageName, 0)
        context.packageManager.getApplicationLabel(appInfo).toString()
    } catch (e: Exception) {
        packageName
    }
}

fun obtenerTecladosBloqueados(permitidos: List<String>?): List<String> {
    val todosLosTeclados = obtenerTodosLosTeclados()
    
    return if (permitidos == null) {
        emptyList()  // Sin restricciones, ninguno bloqueado
    } else {
        todosLosTeclados.filter { it !in permitidos }
    }
}

fun obtenerTodosLosTeclados(): List<String> {
    val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) 
        as InputMethodManager
    
    return inputMethodManager.inputMethodList.map { it.packageName }
}
```

---

### `setPermittedInputMethodsForCurrentUser(List<String> packageNames)`

**Propósito**: Similar a `setPermittedInputMethods()` pero aplica específicamente al usuario actual sin requerir ComponentName.

**Disponibilidad**: Android 9+ (API 28)

**Diferencia**:
- No requiere especificar `admin` (ComponentName)
- Más simple para casos de uso básicos
- Mismo comportamiento de whitelist

**Ejemplo de Uso**:
```kotlin
@RequiresApi(Build.VERSION_CODES.P)
fun configurarTecladosUsuarioActual() {
    val tecladosSeguras = listOf(
        "com.google.android.inputmethod.latin"
    )
    
    dpm.setPermittedInputMethodsForCurrentUser(tecladosSeguras)
    
    Log.i("IME", "⌨️ Teclados configurados para usuario actual")
}
```

---

## ♿ Métodos de Servicios de Accesibilidad

### `setPermittedAccessibilityServices(ComponentName admin, List<String> packageNames)`

**Propósito**: Define una **whitelist** de servicios de accesibilidad permitidos. Servicios de accesibilidad tienen permisos muy amplios y pueden ser vectores de ataque similares a teclados.

**Riesgo de Seguridad**:
```
Servicio de Accesibilidad Malicioso Puede:
├── Leer TODO el contenido en pantalla
├── Interceptar interacciones del usuario
├── Simular toques y gestos
├── Capturar contraseñas (incluso enmascaradas)
├── Leer notificaciones completas
├── Grabar toda la actividad del usuario
└── Realizar acciones en nombre del usuario
```

**Casos de Uso**:

**Whitelist Estricta**:
- 🏦 Banca: Solo servicios aprobados (TalkBack, etc.)
- 🏥 Salud: Prevenir lectura de información médica
- 🏛️ Gobierno: Control estricto de servicios
- 🏢 Corporativo: Solo servicios corporativos autorizados

**Ejemplo de Uso**:
```kotlin
fun configurarServiciosAccesibilidadPermitidos() {
    val serviciosPermitidos = listOf(
        "com.google.android.marvin.talkback",     // TalkBack (Google)
        "com.google.android.accessibility.suite", // Android Accessibility Suite
        "com.samsung.android.accessibility"       // Samsung Accessibility
    )
    
    dpm.setPermittedAccessibilityServices(admin, serviciosPermitidos)
    
    Log.i("Accessibility", "♿ Servicios de accesibilidad configurados")
    Log.i("Accessibility", "   Permitidos: ${serviciosPermitidos.size}")
}

// Máxima seguridad: Solo TalkBack (esencial para accesibilidad)
fun configurarAccesibilidadMinima() {
    val soloTalkBack = listOf("com.google.android.marvin.talkback")
    dpm.setPermittedAccessibilityServices(admin, soloTalkBack)
    
    Log.i("Accessibility", "🔒 Solo TalkBack permitido")
}

// Bloquear todos los servicios de accesibilidad (uso extremo)
fun bloquearTodosServiciosAccesibilidad() {
    dpm.setPermittedAccessibilityServices(admin, emptyList())
    
    Log.w("Accessibility", "⚠️ TODOS los servicios bloqueados")
    Log.w("Accessibility", "   Usuarios con discapacidad no podrán usar el dispositivo")
}

// Remover restricciones
fun permitirTodosServiciosAccesibilidad() {
    dpm.setPermittedAccessibilityServices(admin, null)
    
    Log.i("Accessibility", "✅ Todos los servicios permitidos")
}
```

**⚠️ Consideración Ética y Legal**:
```kotlin
// IMPORTANTE: Bloquear servicios de accesibilidad puede violar:
// - Americans with Disabilities Act (ADA) - USA
// - European Accessibility Act - EU
// - Regulaciones de accesibilidad locales

fun validarAccesibilidadLegal(lista: List<String>?) {
    if (lista != null && lista.isEmpty()) {
        Log.e("Legal", "⚠️ ADVERTENCIA LEGAL:")
        Log.e("Legal", "   Bloquear TODOS los servicios puede violar leyes de accesibilidad")
        Log.e("Legal", "   Asegurar que TalkBack esté en whitelist")
    }
    
    // Verificar que TalkBack está incluido
    val talkbackIncluido = lista?.contains("com.google.android.marvin.talkback") ?: true
    
    if (!talkbackIncluido && lista != null) {
        Log.w("Legal", "⚠️ TalkBack NO incluido en whitelist")
        Log.w("Legal", "   Considerar agregar para cumplimiento de accesibilidad")
    }
}
```

---

### `getPermittedAccessibilityServices(ComponentName admin)`

**Propósito**: Obtiene la lista de servicios de accesibilidad permitidos actualmente configurada.

**Retorna**:
- `List<String>`: Package names de servicios permitidos
- `null`: Sin restricciones (todos permitidos)

**Ejemplo de Uso**:
```kotlin
fun auditarServiciosAccesibilidad(): String {
    val permitidos = dpm.getPermittedAccessibilityServices(admin)
    
    return buildString {
        append("♿ SERVICIOS DE ACCESIBILIDAD\n")
        append("═══════════════════════════════════\n\n")
        
        if (permitidos == null) {
            append("✅ Sin restricciones\n")
            append("   Todos los servicios permitidos\n")
        } else {
            append("🔒 Whitelist activa\n")
            append("   Servicios permitidos: ${permitidos.size}\n\n")
            
            if (permitidos.isEmpty()) {
                append("⚠️ ADVERTENCIA CRÍTICA\n")
                append("   Ningún servicio permitido\n")
                append("   Usuarios con discapacidad no pueden usar dispositivo\n")
                append("   Posible violación de leyes de accesibilidad\n")
            } else {
                permitidos.forEach { pkg ->
                    val instalado = estaInstalado(pkg)
                    val nombre = obtenerNombreApp(pkg)
                    
                    append("${if (instalado) "✅" else "❌"} $nombre\n")
                    append("   Package: $pkg\n\n")
                }
                
                // Verificar TalkBack
                val talkbackIncluido = permitidos.contains("com.google.android.marvin.talkback")
                if (!talkbackIncluido) {
                    append("\n⚠️ TalkBack NO incluido\n")
                    append("   Considerar agregar para accesibilidad\n")
                }
            }
        }
    }
}

fun obtenerNombreApp(packageName: String): String {
    return try {
        val appInfo = context.packageManager.getApplicationInfo(packageName, 0)
        context.packageManager.getApplicationLabel(appInfo).toString()
    } catch (e: Exception) {
        packageName
    }
}
```

---

## 🎯 Casos de Uso Completos por Escenario

### 🏦 Financiero/Bancario - Máxima Seguridad
```kotlin
fun configurarSeguridadFinanciera() {
    // 1. Solo Gboard permitido (verificado por Google)
    val soloGboard = listOf("com.google.android.inputmethod.latin")
    dpm.setPermittedInputMethods(admin, soloGboard)
    
    // 2. Solo servicios de accesibilidad esenciales
    val serviciosEsenciales = listOf(
        "com.google.android.marvin.talkback",
        "com.google.android.accessibility.suite"
    )
    dpm.setPermittedAccessibilityServices(admin, serviciosEsenciales)
    
    // 3. Validar instalación
    if (!estaInstalado("com.google.android.inputmethod.latin")) {
        Log.e("Security", "❌ Gboard no instalado - instalar antes de aplicar política")
        return
    }
    
    Log.i("Security", """
        🔒 SEGURIDAD FINANCIERA APLICADA
        ═══════════════════════════════════
        ⌨️ Teclados: Solo Gboard
        ♿ Accesibilidad: Solo esenciales de Google
        
        Protección contra:
        - Keyloggers maliciosos
        - Captura de contraseñas
        - Servicios de accesibilidad espía
        
        Cumplimiento: PCI-DSS, SOX
    """.trimIndent())
}
```

---

### 🏥 Médico/HIPAA - Protección PHI
```kotlin
fun configurarSeguridadMedica() {
    // 1. Teclados confiables permitidos
    val tecladosPermitidos = listOf(
        "com.google.android.inputmethod.latin",  // Gboard
        "com.empresa.medical_ime"                 // Teclado médico especializado
    )
    dpm.setPermittedInputMethods(admin, tecladosPermitidos)
    
    // 2. Servicios de accesibilidad - incluir TalkBack por ley
    val serviciosPermitidos = listOf(
        "com.google.android.marvin.talkback",
        "com.google.android.accessibility.suite",
        "com.empresa.medical_accessibility"  // Servicio médico aprobado
    )
    dpm.setPermittedAccessibilityServices(admin, serviciosPermitidos)
    
    Log.i("Security", """
        🏥 SEGURIDAD MÉDICA/HIPAA APLICADA
        ═══════════════════════════════════
        ⌨️ Teclados médicos aprobados
        ♿ Accesibilidad con TalkBack incluido
        
        Protección:
        - PHI (Protected Health Information)
        - Datos de pacientes
        - Información médica sensible
        
        Cumplimiento: HIPAA, accesibilidad ADA
    """.trimIndent())
}
```

---

### 🏢 Corporativo Estándar - Balance
```kotlin
fun configurarCorporativoEstandar() {
    // 1. Teclados populares y confiables
    val tecladosPermitidos = listOf(
        "com.google.android.inputmethod.latin",  // Gboard
        "com.microsoft.swiftkey.keyboard",       // SwiftKey
        "com.samsung.android.honeyboard"         // Samsung (si es dispositivo Samsung)
    )
    dpm.setPermittedInputMethods(admin, tecladosPermitidos)
    
    // 2. Servicios de accesibilidad estándar
    val serviciosPermitidos = listOf(
        "com.google.android.marvin.talkback",
        "com.google.android.accessibility.suite",
        "com.samsung.android.accessibility"
    )
    dpm.setPermittedAccessibilityServices(admin, serviciosPermitidos)
    
    Log.i("Security", """
        🏢 CONFIGURACIÓN CORPORATIVA ESTÁNDAR
        ══════════════════════════════════════
        ⌨️ Teclados: Gboard, SwiftKey, Samsung
        ♿ Accesibilidad: Servicios estándar
        
        Balance: Seguridad + Productividad + Elección
    """.trimIndent())
}
```

---

## ⚠️ Consideraciones Importantes

### Validación Pre-Aplicación

**CRÍTICO - Verificar Instalación**:
```kotlin
fun aplicarPoliticasIMESeguras(teclados: List<String>, servicios: List<String>) {
    // 1. Validar que al menos un teclado está instalado
    val tecladoInstalado = teclados.any { estaInstalado(it) }
    if (!tecladoInstalado) {
        Log.e("IME", "❌ ERROR: Ningún teclado permitido está instalado")
        Log.e("IME", "   Instalar al menos uno antes de aplicar política")
        throw IllegalStateException("No hay teclados disponibles")
    }
    
    // 2. Validar TalkBack para accesibilidad
    val talkbackIncluido = servicios.contains("com.google.android.marvin.talkback")
    if (!talkbackIncluido) {
        Log.w("IME", "⚠️ ADVERTENCIA: TalkBack no incluido")
        Log.w("IME", "   Considerar agregar para cumplimiento de accesibilidad")
    }
    
    // 3. Aplicar políticas
    dpm.setPermittedInputMethods(admin, teclados)
    dpm.setPermittedAccessibilityServices(admin, servicios)
    
    Log.i("IME", "✅ Políticas de IME aplicadas exitosamente")
}
```

### Accesibilidad Legal

**Requisitos Legales**:
```
USA:
├── Americans with Disabilities Act (ADA)
├── Section 508 (tecnología federal)
└── State accessibility laws

Europa:
├── European Accessibility Act
├── EN 301 549 standard
└── WCAG 2.1 guidelines

Otros:
├── UK Equality Act
├── Australian DDA
└── Canadian Accessibility Act
```

**Mínimo Recomendado**:
```kotlin
val minimoCumplimientoLegal = listOf(
    "com.google.android.marvin.talkback",     // Lector de pantalla
    "com.google.android.accessibility.suite"  // Suite de accesibilidad
)
```

### Teclados por Fabricante

**Package Names Comunes**:
```kotlin
val teclados Comunes = mapOf(
    // Google
    "Gboard" to "com.google.android.inputmethod.latin",
    
    // Samsung
    "Samsung Keyboard" to "com.samsung.android.honeyboard",
    "Samsung IME" to "com.sec.android.inputmethod",
    
    // Microsoft
    "SwiftKey" to "com.microsoft.swiftkey.keyboard",
    
    // Terceros populares
    "Swype" to "com.nuance.swype.dtc",
    "Fleksy" to "com.syntellia.fleksy.keyboard",
    "GO Keyboard" to "com.jb.gokeyboard",  // ⚠️ Controversias de privacidad
    
    // Especializados
    "Hacker's Keyboard" to "org.pocketworkstation.pckeyboard",
    "AnySoftKeyboard" to "com.menny.android.anysoftkeyboard"
)
```

### Servicios de Accesibilidad Comunes

```kotlin
val serviciosAccesibilidadComunes = mapOf(
    // Google
    "TalkBack" to "com.google.android.marvin.talkback",
    "Accessibility Suite" to "com.google.android.accessibility.suite",
    "Voice Access" to "com.google.android.apps.accessibility.voiceaccess",
    
    // Samsung
    "Samsung Accessibility" to "com.samsung.android.accessibility",
    "Voice Assistant" to "com.samsung.android.app.talkback",
    
    // Terceros
    "JAWS" to "com.freedomscientific.jawsandroid",
    "BrailleBack" to "com.googlecode.eyesfree.brailleback"
)
```

### Mejores Prácticas

**1. Whitelist Progresiva**:
```kotlin
// ❌ MALO: Bloquear todo inmediatamente
fun bloqueadoEstricto() {
    dpm.setPermittedInputMethods(admin, emptyList())  // Usuario no puede escribir!
}

// ✅ BUENO: Whitelist gradual
fun whitlistProgresiva() {
    // Fase 1: Incluir teclados populares
    val fase1 = listOf("com.google.android.inputmethod.latin", "com.microsoft.swiftkey.keyboard")
    dpm.setPermittedInputMethods(admin, fase1)
    
    // Monitorear uso y problemas
    // ...
    
    // Fase 2: Reducir si no hay problemas
    val fase2 = listOf("com.google.android.inputmethod.latin")
    // dpm.setPermittedInputMethods(admin, fase2)
}
```

**2. Testing Exhaustivo**:
```kotlin
fun testearPoliticasIME() {
    val tecladosTest = listOf("com.google.android.inputmethod.latin")
    
    // Test 1: ¿Teclado instalado?
    assert(estaInstalado(tecladosTest[0])) { "Teclado no instalado" }
    
    // Test 2: Aplicar en dispositivo de prueba
    dpm.setPermittedInputMethods(admin, tecladosTest)
    
    // Test 3: Verificar que usuario puede escribir
    // ...
    
    // Test 4: Verificar que otros teclados están bloqueados
    // ...
    
    Log.i("Test", "✅ Tests de IME pasados")
}
```

**3. Comunicación con Usuarios**:
```kotlin
fun aplicarConNotificacion(teclados: List<String>) {
    // Notificar ANTES de aplicar
    mostrarNotificacion(
        "Cambio de Política de Seguridad",
        "Se restringirán los teclados disponibles por seguridad. " +
        "Solo Gboard será permitido."
    )
    
    // Esperar confirmación o tiempo
    Handler(Looper.getMainLooper()).postDelayed({
        dpm.setPermittedInputMethods(admin, teclados)
    }, 5000)  // 5 segundos para leer
}
```

---
