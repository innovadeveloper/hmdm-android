# Documentación DevicePolicyManager - Categoría 16: Account Management

## 📋 Visión General

La categoría de **Account Management** agrupa aproximadamente **8 métodos** que permiten controlar qué tipos de cuentas (Google, Exchange, etc.) pueden ser agregadas o gestionadas en el dispositivo. Es fundamental para prevenir sincronización no autorizada de datos corporativos y controlar el acceso a servicios en la nube.

## 🎯 Propósito

Estos métodos permiten:
- **Bloquear tipos de cuenta** específicos (Gmail, Exchange, etc.)
- **Prevenir sincronización** no autorizada
- **Controlar acceso** a servicios corporativos
- **Agregar cuentas** programáticamente
- **Gestionar DLP** (Data Loss Prevention)

---

## 📧 Conceptos Fundamentales

### Tipos de Cuenta en Android

**Cuentas Comunes**:
```
com.google                    → Cuenta Google (Gmail, Drive, Calendar)
com.google.android.gm.exchange → Exchange (Empresarial)
com.google.android.gm         → Gmail específico
com.facebook.auth.login       → Facebook
com.twitter.android.auth.login → Twitter
com.microsoft.office.outlook  → Outlook
com.whatsapp                  → WhatsApp
```

**Riesgos de Seguridad**:
```
Cuenta Personal en Dispositivo Corporativo:
├── Sincronización de datos corporativos a nube personal
├── Backup automático de información sensible
├── Acceso a servicios no autorizados
├── Exfiltración de contactos corporativos
├── Fuga de correos/calendarios empresariales
└── Violación de políticas de retención de datos

Cuenta Corporativa en Dispositivo Personal (BYOD):
├── Datos corporativos en dispositivo no controlado
├── Apps personales accediendo a datos de trabajo
├── Pérdida de control sobre información sensible
└── Problemas de cumplimiento regulatorio
```

---

## 🚫 Métodos de Bloqueo de Cuentas

### `setAccountManagementDisabled(ComponentName admin, String accountType, boolean disabled)`

**Propósito**: Deshabilita la capacidad de agregar o remover cuentas de un tipo específico. Las cuentas existentes de ese tipo permanecen, pero no se pueden modificar ni agregar nuevas.

**Parámetros**:
- `accountType`: Tipo de cuenta (ej: "com.google")
- `disabled`: `true` para bloquear, `false` para permitir

**Casos de Uso**:

**Bloquear Cuentas Personales**:
- 🏢 **Corporativo**: Solo cuenta Google corporativa, bloquear personales
- 🏦 **Financiero**: Prevenir sincronización con servicios externos
- 🏥 **Salud**: Proteger PHI de sync no autorizado
- 🏛️ **Gobierno**: Seguridad clasificada

**Bloquear Cuentas Sociales**:
- Prevenir distracción
- Evitar compartir contenido corporativo
- Control de productividad

**Ejemplo de Uso**:
```kotlin
// Bloquear cuentas Google personales
fun bloquearCuentasGooglePersonales() {
    dpm.setAccountManagementDisabled(
        admin,
        "com.google",
        true  // disabled = true → bloquear
    )
    
    Log.i("Accounts", "📧❌ Cuentas Google BLOQUEADAS")
    Log.i("Accounts", "   Usuario NO puede:")
    Log.i("Accounts", "   - Agregar nuevas cuentas Google")
    Log.i("Accounts", "   - Remover cuentas Google existentes")
    Log.i("Accounts", "   ✅ Cuentas existentes siguen funcionando")
}

// Permitir cuentas Google nuevamente
fun permitirCuentasGoogle() {
    dpm.setAccountManagementDisabled(
        admin,
        "com.google",
        false  // disabled = false → permitir
    )
    
    Log.i("Accounts", "📧✅ Cuentas Google PERMITIDAS")
}

// Bloquear múltiples tipos de cuenta
fun bloquearCuentasNoAutorizadas() {
    val tiposBloqueados = listOf(
        "com.facebook.auth.login",           // Facebook
        "com.twitter.android.auth.login",    // Twitter
        "com.instagram.android",             // Instagram
        "com.snapchat.android",              // Snapchat
        "com.linkedin.android"               // LinkedIn
    )
    
    tiposBloqueados.forEach { tipo ->
        try {
            dpm.setAccountManagementDisabled(admin, tipo, true)
            Log.i("Accounts", "❌ Bloqueado: $tipo")
        } catch (e: Exception) {
            Log.w("Accounts", "⚠️ No se pudo bloquear $tipo: ${e.message}")
        }
    }
    
    Log.i("Accounts", "🔒 Cuentas sociales bloqueadas")
}

// Configuración por industria
fun configurarPorIndustria(industria: Industria) {
    when (industria) {
        Industria.FINANCIERA -> {
            // Bloquear TODO excepto Exchange corporativo
            bloquearCuentasGoogle()
            bloquearCuentasSociales()
            bloquearCuentasPersonales()
        }
        
        Industria.MEDICA -> {
            // Bloquear cuentas sociales, permitir Google corporativo
            bloquearCuentasSociales()
        }
        
        Industria.CORPORATIVA -> {
            // Solo bloquear sociales
            bloquearCuentasSociales()
        }
        
        Industria.EDUCACION -> {
            // Permitir todo
        }
    }
}

enum class Industria {
    FINANCIERA, MEDICA, CORPORATIVA, EDUCACION
}

fun bloquearCuentasSociales() {
    listOf(
        "com.facebook.auth.login",
        "com.twitter.android.auth.login",
        "com.instagram.android",
        "com.snapchat.android"
    ).forEach { tipo ->
        dpm.setAccountManagementDisabled(admin, tipo, true)
    }
}

fun bloquearCuentasPersonales() {
    // Bloquear cuentas Google personales
    // Nota: Esto puede afectar Play Store
    dpm.setAccountManagementDisabled(admin, "com.google", true)
}
```

**Comportamiento del Sistema**:
```
Cuando un tipo está BLOQUEADO:
❌ Settings > Accounts > Add account → Tipo no aparece en lista
❌ Apps no pueden agregar cuentas de ese tipo programáticamente
❌ Usuario no puede remover cuentas existentes de ese tipo
✅ Cuentas existentes siguen funcionando normalmente
✅ Sincronización de cuentas existentes continúa
✅ Apps pueden usar cuentas existentes

Ejemplo:
1. Usuario tiene cuenta Gmail personal (antes del bloqueo)
2. Admin bloquea "com.google"
3. Gmail personal sigue funcionando
4. Usuario NO puede agregar otra cuenta Gmail
5. Usuario NO puede remover la cuenta Gmail existente
```

**⚠️ Advertencias Importantes**:
```kotlin
// CRÍTICO: Bloquear "com.google" puede afectar funcionalidad
fun advertenciasBloqueoCuentaGoogle() {
    Log.w("Accounts", """
        ⚠️ ADVERTENCIA: Bloquear cuentas Google
        ════════════════════════════════════════
        
        Impactos potenciales:
        ❌ Play Store puede no funcionar (requiere cuenta Google)
        ❌ Google Play Services afectado
        ❌ Backup automático deshabilitado
        ❌ Find My Device no funciona
        ❌ Apps que requieren Google Sign-In fallan
        
        Considerar:
        ✅ Permitir cuenta Google corporativa (G Suite/Workspace)
        ✅ Usar Work Profile en lugar de bloqueo total
        ✅ Solo bloquear en dispositivos COBO (Corporate Owned)
    """.trimIndent())
}
```

---

### `getAccountTypesWithManagementDisabled()`

**Propósito**: Obtiene la lista de tipos de cuenta que están actualmente bloqueados para agregar/remover.

**Retorna**: `String[]` con tipos de cuenta bloqueados

**Ejemplo de Uso**:
```kotlin
fun auditarCuentasBloqueadas(): String {
    val tiposBloqueados = dpm.getAccountTypesWithManagementDisabled()
    
    return buildString {
        append("📧 CUENTAS BLOQUEADAS\n")
        append("═══════════════════════════════════\n\n")
        
        if (tiposBloqueados.isEmpty()) {
            append("✅ Sin restricciones\n")
            append("   Todos los tipos de cuenta permitidos\n")
            append("   Usuario puede agregar cualquier cuenta\n")
        } else {
            append("🔒 Tipos bloqueados: ${tiposBloqueados.size}\n\n")
            
            tiposBloqueados.forEach { tipo ->
                val nombre = obtenerNombreTipoCuenta(tipo)
                val cuentasExistentes = obtenerCuentasExistentes(tipo)
                
                append("❌ $nombre\n")
                append("   Tipo: $tipo\n")
                append("   Cuentas existentes: ${cuentasExistentes.size}\n")
                
                if (cuentasExistentes.isNotEmpty()) {
                    cuentasExistentes.forEach { cuenta ->
                        append("     • ${cuenta.name}\n")
                    }
                }
                append("\n")
            }
            
            append("Impacto:\n")
            append("❌ Usuario NO puede agregar estos tipos\n")
            append("❌ Usuario NO puede remover cuentas existentes\n")
            append("✅ Cuentas existentes siguen funcionando\n")
        }
    }
}

fun obtenerNombreTipoCuenta(tipo: String): String {
    return when (tipo) {
        "com.google" -> "Google Account"
        "com.google.android.gm.exchange" -> "Exchange ActiveSync"
        "com.facebook.auth.login" -> "Facebook"
        "com.twitter.android.auth.login" -> "Twitter"
        "com.microsoft.office.outlook" -> "Outlook"
        else -> tipo
    }
}

fun obtenerCuentasExistentes(tipo: String): List<Account> {
    val accountManager = AccountManager.get(context)
    return accountManager.getAccountsByType(tipo).toList()
}
```

---

## ➕ Método de Agregar Cuentas

### `addAccount(ComponentName admin, String accountType, AccountManagerCallback<Bundle> callback)`

**Propósito**: Agrega una cuenta de un tipo específico programáticamente, sin interacción del usuario. Útil para provisioning automático de cuentas corporativas.

**Disponibilidad**: Android 14+ (API 34)

**Casos de Uso**:
- **Provisioning**: Agregar cuenta corporativa automáticamente
- **Zero-touch**: Configuración sin intervención del usuario
- **MDM**: Despliegue masivo de cuentas empresariales
- **Onboarding**: Simplificar proceso de configuración inicial

**Ejemplo de Uso**:
```kotlin
@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
fun agregarCuentaCorporativa(email: String) {
    val accountType = "com.google.android.gm.exchange"  // Exchange
    
    dpm.addAccount(
        admin,
        accountType,
        object : AccountManagerCallback<Bundle> {
            override fun run(future: AccountManagerFuture<Bundle>) {
                try {
                    val result = future.result
                    val accountName = result.getString(AccountManager.KEY_ACCOUNT_NAME)
                    
                    if (accountName != null) {
                        Log.i("Accounts", "✅ Cuenta agregada: $accountName")
                        
                        // Configurar cuenta adicional (sincronización, etc.)
                        configurarCuentaCorporativa(accountName)
                    } else {
                        Log.e("Accounts", "❌ No se obtuvo nombre de cuenta")
                    }
                } catch (e: Exception) {
                    Log.e("Accounts", "❌ Error al agregar cuenta: ${e.message}")
                }
            }
        }
    )
}

fun configurarCuentaCorporativa(accountName: String) {
    // Habilitar sincronización de calendarios y contactos
    ContentResolver.setSyncAutomatically(
        Account(accountName, "com.google.android.gm.exchange"),
        "com.android.calendar",
        true
    )
    
    ContentResolver.setSyncAutomatically(
        Account(accountName, "com.google.android.gm.exchange"),
        "com.android.contacts",
        true
    )
    
    Log.i("Accounts", "📅 Sincronización configurada para $accountName")
}

// Provisioning completo de cuenta
@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
fun provisioningAutomaticoCuenta(
    email: String,
    servidor: String,
    dominio: String
) {
    // 1. Agregar cuenta Exchange corporativa
    agregarCuentaCorporativa(email)
    
    // 2. Bloquear otras cuentas
    dpm.setAccountManagementDisabled(admin, "com.google", true)
    dpm.setAccountManagementDisabled(admin, "com.facebook.auth.login", true)
    
    // 3. Configurar políticas adicionales
    configurarPoliticasDeCuenta()
    
    Log.i("Provisioning", """
        ✅ PROVISIONING COMPLETO
        ═══════════════════════════
        📧 Cuenta: $email
        🏢 Servidor: $servidor
        🔒 Cuentas personales bloqueadas
    """.trimIndent())
}
```

**⚠️ Importante**:
- Requiere Android 14+
- Solo funciona con tipos de cuenta que soporten agregar sin UI
- Puede requerir credenciales adicionales
- No todos los tipos de cuenta soportan este método

---

## 🎯 Casos de Uso Completos por Escenario

### 🏦 Financiero - Solo Cuentas Corporativas
```kotlin
fun configurarFinanciero() {
    // 1. Bloquear TODAS las cuentas personales
    val tiposPersonales = listOf(
        "com.google",                        // Google personal
        "com.facebook.auth.login",           // Facebook
        "com.twitter.android.auth.login",    // Twitter
        "com.instagram.android",             // Instagram
        "com.snapchat.android",              // Snapchat
        "com.linkedin.android",              // LinkedIn
        "com.microsoft.office.outlook"       // Outlook personal
    )
    
    tiposPersonales.forEach { tipo ->
        dpm.setAccountManagementDisabled(admin, tipo, true)
    }
    
    // 2. Solo permitir Exchange corporativo
    // (No bloqueamos "com.google.android.gm.exchange")
    
    // 3. Si Android 14+, agregar cuenta corporativa automáticamente
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        agregarCuentaCorporativa("empleado@financiera.com")
    }
    
    Log.i("Security", """
        🏦 CONFIGURACIÓN FINANCIERA
        ═══════════════════════════════
        ❌ Cuentas personales bloqueadas
        ✅ Solo Exchange corporativo permitido
        🔒 Prevención de exfiltración de datos
        
        Cumplimiento: PCI-DSS, SOX, GLBA
    """.trimIndent())
}
```

---

### 🏥 Médico/HIPAA - Protección PHI
```kotlin
fun configurarMedico() {
    // 1. Bloquear cuentas sociales (alto riesgo de compartir PHI)
    val tiposSociales = listOf(
        "com.facebook.auth.login",
        "com.twitter.android.auth.login",
        "com.instagram.android",
        "com.snapchat.android"
    )
    
    tiposSociales.forEach { tipo ->
        dpm.setAccountManagementDisabled(admin, tipo, true)
    }
    
    // 2. Permitir Google corporativo (G Suite/Workspace con BAA de HIPAA)
    // No bloqueamos "com.google" si es cuenta corporativa
    
    // 3. Permitir Exchange corporativo
    // No bloqueamos "com.google.android.gm.exchange"
    
    Log.i("Security", """
        🏥 CONFIGURACIÓN MÉDICA/HIPAA
        ══════════════════════════════
        ❌ Redes sociales bloqueadas
        ✅ Google Workspace con BAA permitido
        ✅ Exchange corporativo permitido
        🔒 PHI protegido de sync no autorizado
        
        Cumplimiento: HIPAA
    """.trimIndent())
}
```

---

### 🏢 Corporativo BYOD - Balance
```kotlin
fun configurarBYOD() {
    // En BYOD, más flexible pero con controles
    
    // 1. Solo bloquear cuentas sociales durante horario laboral
    // (Opcional: implementar control por horario)
    
    // 2. Permitir cuenta Google personal (es su dispositivo)
    // No bloqueamos "com.google"
    
    // 3. Agregar cuenta corporativa en Work Profile
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        agregarCuentaCorporativa("empleado@empresa.com")
    }
    
    // 4. Bloquear solo en Work Profile
    configurarWorkProfileBYOD()
    
    Log.i("BYOD", """
        🏢 CONFIGURACIÓN BYOD
        ══════════════════════════
        ✅ Cuenta personal permitida
        ✅ Cuenta corporativa en Work Profile
        ⚖️ Balance privacidad/seguridad
    """.trimIndent())
}

fun configurarWorkProfileBYOD() {
    // En Work Profile, bloquear cuentas personales
    if (esWorkProfile()) {
        dpm.setAccountManagementDisabled(admin, "com.google", true)
        
        Log.i("BYOD", "Work Profile: Solo cuenta corporativa permitida")
    }
}

fun esWorkProfile(): Boolean {
    return dpm.isProfileOwnerApp(context.packageName)
}
```

---

### 🎓 Educación - Control Parental
```kotlin
fun configurarEducacion(edad: Int) {
    if (edad < 13) {
        // Menores de 13: Bloquear redes sociales (COPPA)
        val redesSociales = listOf(
            "com.facebook.auth.login",
            "com.twitter.android.auth.login",
            "com.instagram.android",
            "com.snapchat.android",
            "com.tiktok"
        )
        
        redesSociales.forEach { tipo ->
            dpm.setAccountManagementDisabled(admin, tipo, true)
        }
        
        Log.i("Education", "🔒 Redes sociales bloqueadas (menor de 13)")
        Log.i("Education", "   Cumplimiento: COPPA")
    } else {
        // Mayores de 13: Solo monitoreo, sin bloqueos
        Log.i("Education", "✅ Sin restricciones de cuentas")
    }
}
```

---

## ⚠️ Consideraciones Importantes

### Impacto en Funcionalidad

**Bloquear "com.google"**:
```
Afecta:
❌ Play Store (requiere cuenta Google)
❌ Google Play Services
❌ Backup automático
❌ Find My Device
❌ Chrome Sync
❌ YouTube
❌ Google Maps (algunas funciones)
❌ Google Photos backup

Alternativas:
✅ Usar Work Profile (separación)
✅ Permitir solo Google Workspace/G Suite
✅ Managed Google Play (no requiere cuenta personal)
```

**Solución Recomendada**:
```kotlin
// En lugar de bloquear completamente "com.google"
fun solucionRecomendada() {
    // Opción 1: Work Profile
    // Cuenta personal en perfil personal
    // Cuenta corporativa en Work Profile
    
    // Opción 2: Managed Google Play
    // No requiere cuenta Google del usuario
    // Apps se instalan desde Managed Play
    
    // Opción 3: Dispositivo Completamente Gestionado
    // Sin cuenta Google personal
    // Solo cuenta corporativa G Suite/Workspace
}
```

### Cuentas Existentes

**Comportamiento**:
```kotlin
fun comportamientoCuentasExistentes() {
    // Escenario:
    // 1. Usuario tiene cuenta Gmail personal
    // 2. Admin bloquea "com.google"
    
    Log.i("Behavior", """
        Resultado:
        ✅ Cuenta Gmail personal sigue funcionando
        ✅ Sincronización continúa
        ✅ Apps pueden usar la cuenta
        ❌ Usuario NO puede agregar otra cuenta Google
        ❌ Usuario NO puede remover la cuenta
        
        Para remover cuentas existentes:
        → Usar AccountManager.removeAccount() programáticamente
        → O hacer factory reset
    """.trimIndent())
}
```

**Remover Cuentas Programáticamente**:
```kotlin
fun removerCuentasNoAutorizadas() {
    val accountManager = AccountManager.get(context)
    val cuentasGoogle = accountManager.getAccountsByType("com.google")
    
    cuentasGoogle.forEach { cuenta ->
        if (!esCuentaCorporativa(cuenta.name)) {
            // Remover cuenta personal
            accountManager.removeAccount(
                cuenta,
                null,
                { future ->
                    try {
                        val result = future.result
                        Log.i("Accounts", "✅ Cuenta removida: ${cuenta.name}")
                    } catch (e: Exception) {
                        Log.e("Accounts", "❌ Error: ${e.message}")
                    }
                },
                null
            )
        }
    }
}

fun esCuentaCorporativa(email: String): Boolean {
    // Verificar si es del dominio corporativo
    return email.endsWith("@empresa.com") || 
           email.endsWith("@empresacorp.com")
}
```

### Tipos de Cuenta Comunes

**Referencia Completa**:
```kotlin
val tiposCuentaComunes = mapOf(
    // Google
    "com.google" to "Google Account",
    "com.google.android.gm" to "Gmail",
    "com.google.android.gm.exchange" to "Exchange ActiveSync",
    
    // Microsoft
    "com.microsoft.office.outlook" to "Outlook",
    "com.microsoft.workaccount" to "Microsoft Work Account",
    
    // Redes Sociales
    "com.facebook.auth.login" to "Facebook",
    "com.twitter.android.auth.login" to "Twitter",
    "com.instagram.android" to "Instagram",
    "com.snapchat.android" to "Snapchat",
    "com.linkedin.android" to "LinkedIn",
    "com.tiktok" to "TikTok",
    
    // Mensajería
    "com.whatsapp" to "WhatsApp",
    "org.telegram.messenger" to "Telegram",
    
    // Otros
    "com.dropbox.android" to "Dropbox",
    "com.box.android" to "Box",
    "com.samsung.android.scloud" to "Samsung Cloud"
)
```

### Mejores Prácticas

**1. Política Gradual**:
```kotlin
// ✅ BUENO: Implementar gradualmente
fun politicaGradual() {
    // Semana 1: Notificar cambio
    notificarCambioProximo()
    
    // Semana 2: Bloquear redes sociales
    bloquearRedesSociales()
    
    // Semana 3: Monitorear y ajustar
    monitorearImpacto()
    
    // Semana 4: Si no hay problemas, bloquear más
    if (!hayProblemas()) {
        bloquearCuentasPersonales()
    }
}

// ❌ MALO: Bloquear todo de golpe
fun bloqueadoMasivo() {
    listOf("com.google", "com.facebook.auth.login", /* ... */)
        .forEach { dpm.setAccountManagementDisabled(admin, it, true) }
    // Usuario sorprendido y frustrado
}
```

**2. Comunicación Clara**:
```kotlin
fun aplicarConComunicacion(tipos: List<String>) {
    // 1. Notificar ANTES
    mostrarNotificacion(
        "Cambio de Política de Cuentas",
        "A partir del próximo lunes, solo cuentas corporativas estarán permitidas. " +
        "Cuentas personales existentes seguirán funcionando."
    )
    
    // 2. Documentar razones
    enviarEmail(
        asunto = "Política de Cuentas Actualizada",
        cuerpo = "Por seguridad y cumplimiento, se restringirán los tipos de cuenta..."
    )
    
    // 3. Aplicar después de tiempo razonable
    Handler(Looper.getMainLooper()).postDelayed({
        tipos.forEach { tipo ->
            dpm.setAccountManagementDisabled(admin, tipo, true)
        }
    }, TimeUnit.DAYS.toMillis(7))  // 7 días
}
```

**3. Monitoreo Post-Implementación**:
```kotlin
fun monitorearImpacto() {
    // Monitorear tickets de soporte
    val tickets = consultarTicketsSoporte()
    val relacionadosCuentas = tickets.filter { 
        it.descripcion.contains("cuenta", ignoreCase = true) ||
        it.descripcion.contains("account", ignoreCase = true)
    }
    
    if (relacionadosCuentas.size > umbral) {
        Log.w("Monitoring", "⚠️ Alto número de tickets relacionados con cuentas")
        Log.w("Monitoring", "   Considerar ajustar política")
        
        // Alertar a admin
        notificarAdmin("Revisar política de cuentas")
    }
}
```

---

## 📊 Auditoría Completa

```kotlin
fun generarReporteCompleto(): String {
    return buildString {
        append("═══════════════════════════════════════════\n")
        append("       REPORTE DE GESTIÓN DE CUENTAS\n")
        append("═══════════════════════════════════════════\n\n")
        
        // Tipos bloqueados
        append("🚫 TIPOS DE CUENTA BLOQUEADOS\n")
        append("───────────────────────────────────────────\n")
        
        val tiposBloqueados = dpm.getAccountTypesWithManagementDisabled()
        if (tiposBloqueados.isEmpty()) {
            append("✅ Ninguno - Todos los tipos permitidos\n")
        } else {
            tiposBloqueados.forEach { tipo ->
                append("❌ ${obtenerNombreTipoCuenta(tipo)}\n")
                append("   Tipo: $tipo\n")
            }
        }
        
        append("\n")
        
        // Cuentas existentes
        append("📧 CUENTAS EXISTENTES EN DISPOSITIVO\n")
        append("───────────────────────────────────────────\n")
        
        val accountManager = AccountManager.get(context)
        val todasLasCuentas = accountManager.accounts
        
        if (todasLasCuentas.isEmpty()) {
            append("⚠️ Sin cuentas configuradas\n")
        } else {
            todasLasCuentas.groupBy { it.type }.forEach { (tipo, cuentas) ->
                val bloqueado = tipo in tiposBloqueados
                append("${if (bloqueado) "🔒" else "✅"} ${obtenerNombreTipoCuenta(tipo)}\n")
                cuentas.forEach { cuenta ->
                    append("   • ${cuenta.name}\n")
                }
                append("\n")
            }
        }
        
        append("\n")
        append("Generado: ${Date()}\n")
    }
}
```

---

## 📚 Próxima Categoría

**17. Device Identifiers (~5 métodos)**

Esta categoría cubrirá:
- Wi-Fi MAC address
- Enrollment-specific ID
- Device ID (deprecated)
- Identificadores únicos
- Tracking de dispositivos

---

*Documentación generada para DevicePolicyManager API de Android Enterprise - Categoría 16 de 22*