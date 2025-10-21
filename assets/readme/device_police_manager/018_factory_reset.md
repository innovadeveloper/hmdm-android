# Documentación DevicePolicyManager - Categoría 18: Factory Reset Protection

## 📋 Visión General

La categoría de **Factory Reset Protection** agrupa aproximadamente **3 métodos** que permiten configurar políticas de protección contra factory reset, previniendo que dispositivos corporativos perdidos o robados puedan ser usados por terceros no autorizados.

## 🎯 Propósito

Estos métodos permiten:
- **Proteger dispositivos** contra factory reset no autorizado
- **Prevenir robo** mediante bloqueo post-reset
- **Configurar cuentas** de recuperación autorizadas
- **Asegurar devolución** de dispositivos corporativos
- **Anti-theft** corporativo

---

## 🔒 Conceptos Fundamentales

### Factory Reset Protection (FRP)

**Qué es FRP**:
```
Factory Reset Protection:
├── Función de seguridad de Android (Android 5.1+)
├── Previene uso tras factory reset
├── Requiere cuenta Google para desbloquear
└── Protección contra robo/pérdida

Comportamiento Estándar:
1. Usuario hace factory reset
2. Dispositivo reinicia
3. Setup wizard solicita cuenta Google
4. Solo cuentas previamente en dispositivo pueden desbloquear
5. Si no se proporciona cuenta válida → dispositivo bloqueado
```

**FRP Corporativo**:
```
Con DevicePolicyManager:
├── Admin define cuentas autorizadas
├── Solo cuentas corporativas pueden desbloquear
├── Previene bypass con cuentas personales
└── Control total sobre recuperación
```

---

## 📱 Métodos de FRP

### `setFactoryResetProtectionPolicy(ComponentName admin, FactoryResetProtectionPolicy policy)`

**Propósito**: Establece una política de Factory Reset Protection que define qué cuentas pueden desbloquear el dispositivo tras un factory reset.

**Disponibilidad**: Android 9+ (API 28)

**Requisitos**: Solo Device Owner

**Parámetros**:
- `policy`: Objeto `FactoryResetProtectionPolicy` con cuentas autorizadas
- `null`: Remover política (usar FRP estándar de Android)

**Casos de Uso**:
- **Dispositivos corporativos**: Solo admins IT pueden recuperar
- **Dispositivos financiados**: Prevenir uso hasta pago completo
- **Anti-robo**: Dispositivo inútil para ladrones
- **Control de devolución**: Forzar devolución a empresa

**Ejemplo de Uso**:
```kotlin
@RequiresApi(Build.VERSION_CODES.P)
fun configurarFRPCorporativo() {
    // Cuentas corporativas autorizadas para desbloqueo
    val cuentasAutorizadas = listOf(
        "it-admin@empresa.com",
        "security@empresa.com",
        "mdm-recovery@empresa.com"
    )
    
    // Crear política FRP
    val policy = FactoryResetProtectionPolicy.Builder()
        .setFactoryResetProtectionAccounts(cuentasAutorizadas)
        .setFactoryResetProtectionEnabled(true)
        .build()
    
    // Aplicar política
    dpm.setFactoryResetProtectionPolicy(admin, policy)
    
    Log.i("FRP", "🔒 Factory Reset Protection configurado")
    Log.i("FRP", "   Cuentas autorizadas: ${cuentasAutorizadas.size}")
    Log.i("FRP", "   Solo estas cuentas pueden desbloquear tras reset")
}

// Configuración mínima (solo una cuenta admin)
@RequiresApi(Build.VERSION_CODES.P)
fun configurarFRPMinimo() {
    val policy = FactoryResetProtectionPolicy.Builder()
        .setFactoryResetProtectionAccounts(listOf("admin@empresa.com"))
        .setFactoryResetProtectionEnabled(true)
        .build()
    
    dpm.setFactoryResetProtectionPolicy(admin, policy)
    
    Log.i("FRP", "🔒 FRP con cuenta única de admin")
}

// Remover política FRP (volver a estándar Android)
@RequiresApi(Build.VERSION_CODES.P)
fun removerPoliticaFRP() {
    dpm.setFactoryResetProtectionPolicy(admin, null)
    
    Log.i("FRP", "✅ Política FRP removida - usando FRP estándar")
}
```

**FactoryResetProtectionPolicy.Builder**:
```kotlin
// API del Builder
FactoryResetProtectionPolicy.Builder()
    .setFactoryResetProtectionAccounts(List<String>)  // Cuentas autorizadas
    .setFactoryResetProtectionEnabled(Boolean)        // Habilitar/deshabilitar
    .build()
```

**Comportamiento**:
```
Escenario: Dispositivo con FRP configurado

Usuario/Ladrón hace Factory Reset:
1. Dispositivo resetea completamente
2. Setup wizard inicia
3. Solicita cuenta Google
4. Usuario intenta cuenta personal → ❌ RECHAZADA
5. Usuario intenta cuenta no autorizada → ❌ RECHAZADA
6. Solo admin@empresa.com puede desbloquear → ✅ ACEPTADA
7. Dispositivo desbloquea y permite re-enrollment

Sin cuenta autorizada:
└── Dispositivo permanece bloqueado (brick funcional)
```

**⚠️ CRÍTICO - Validación de Cuentas**:
```kotlin
@RequiresApi(Build.VERSION_CODES.P)
fun validarCuentasAntesFRP(cuentas: List<String>) {
    // 1. Verificar que cuentas existen y son accesibles
    cuentas.forEach { email ->
        if (!validarEmailCorporativo(email)) {
            Log.e("FRP", "⚠️ PELIGRO: $email no válido")
            Log.e("FRP", "   Si esta cuenta no existe, dispositivo puede quedar bloqueado permanentemente!")
        }
    }
    
    // 2. Verificar acceso a cuentas
    if (!verificarAccesoRecuperacion(cuentas)) {
        Log.e("FRP", "❌ ERROR: No se puede acceder a cuentas de recuperación")
        Log.e("FRP", "   NO aplicar FRP sin confirmar acceso")
        return
    }
    
    // 3. Documentar cuentas
    documentarCuentasFRP(cuentas)
    
    // 4. Aplicar política
    val policy = FactoryResetProtectionPolicy.Builder()
        .setFactoryResetProtectionAccounts(cuentas)
        .setFactoryResetProtectionEnabled(true)
        .build()
    
    dpm.setFactoryResetProtectionPolicy(admin, policy)
}

fun validarEmailCorporativo(email: String): Boolean {
    return email.matches(Regex("^[a-zA-Z0-9._%+-]+@empresa\\.com$"))
}
```

---

### `getFactoryResetProtectionPolicy(ComponentName admin)`

**Propósito**: Obtiene la política de Factory Reset Protection actualmente configurada.

**Retorna**:
- `FactoryResetProtectionPolicy`: Política actual
- `null`: Sin política configurada (usando FRP estándar)

**Ejemplo de Uso**:
```kotlin
@RequiresApi(Build.VERSION_CODES.P)
fun verificarPoliticaFRP(): String {
    val policy = dpm.getFactoryResetProtectionPolicy(admin)
    
    return buildString {
        append("🔒 FACTORY RESET PROTECTION\n")
        append("═══════════════════════════════════\n\n")
        
        if (policy == null) {
            append("Estado: ⚠️ SIN POLÍTICA CONFIGURADA\n")
            append("   Usando FRP estándar de Android\n")
            append("   Cualquier cuenta Google previa puede desbloquear\n")
        } else {
            val habilitado = policy.isFactoryResetProtectionEnabled
            val cuentas = policy.factoryResetProtectionAccounts
            
            append("Estado: ${if (habilitado) "✅ HABILITADO" else "❌ DESHABILITADO"}\n")
            append("Cuentas autorizadas: ${cuentas.size}\n\n")
            
            if (cuentas.isEmpty()) {
                append("⚠️ ADVERTENCIA: Lista vacía\n")
                append("   Dispositivo puede quedar bloqueado permanentemente\n")
            } else {
                cuentas.forEach { cuenta ->
                    append("  ✅ $cuenta\n")
                }
            }
            
            append("\nProtección:\n")
            if (habilitado) {
                append("  🔒 Tras factory reset, solo cuentas listadas pueden desbloquear\n")
                append("  🔒 Cuentas personales NO pueden desbloquear\n")
                append("  🔒 Dispositivo protegido contra robo\n")
            } else {
                append("  ⚠️ FRP deshabilitado - sin protección\n")
            }
        }
    }
}

// Auditar y alertar si no hay FRP
@RequiresApi(Build.VERSION_CODES.P)
fun auditarFRP() {
    val policy = dpm.getFactoryResetProtectionPolicy(admin)
    
    if (policy == null || !policy.isFactoryResetProtectionEnabled) {
        Log.w("Security", "⚠️ ALERTA DE SEGURIDAD: FRP no configurado")
        Log.w("Security", "   Dispositivos vulnerables a robo")
        Log.w("Security", "   Recomendación: Configurar FRP inmediatamente")
        
        notificarAdminSeguridad("FRP no configurado en dispositivo")
    }
}
```

---

## 🔐 Interacción con User Restrictions

### `DISALLOW_FACTORY_RESET`

**Propósito**: Aunque no es un método directo de FRP, la restricción `DISALLOW_FACTORY_RESET` complementa FRP al prevenir que usuario inicie factory reset desde Settings.

**Uso Combinado**:
```kotlin
fun proteccionCompletaFactoryReset() {
    // 1. Prevenir factory reset desde Settings
    dpm.addUserRestriction(admin, UserManager.DISALLOW_FACTORY_RESET)
    
    // 2. Configurar FRP para proteger si se hace reset por recovery
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val policy = FactoryResetProtectionPolicy.Builder()
            .setFactoryResetProtectionAccounts(listOf("admin@empresa.com"))
            .setFactoryResetProtectionEnabled(true)
            .build()
        
        dpm.setFactoryResetProtectionPolicy(admin, policy)
    }
    
    Log.i("Security", """
        🔒 PROTECCIÓN COMPLETA FACTORY RESET
        ════════════════════════════════════
        ✅ Factory reset bloqueado en Settings
        ✅ FRP configurado para recovery mode
        
        Capas de protección:
        1. Usuario NO puede resetear desde Settings
        2. Reset desde recovery requiere cuenta admin
        3. Dispositivo robado = inútil sin cuenta admin
    """.trimIndent())
}
```

**Diferencias**:
```
DISALLOW_FACTORY_RESET:
├── Previene reset desde Settings UI
├── Usuario ve opción deshabilitada
├── NO previene reset desde recovery mode
└── Complementa FRP

Factory Reset Protection Policy:
├── NO previene iniciar reset
├── Protege DESPUÉS del reset
├── Funciona en recovery mode
└── Requiere cuenta para desbloquear
```

---

## 🎯 Caso de Uso: Dispositivo Financiado

```kotlin
@RequiresApi(Build.VERSION_CODES.P)
fun configurarDispositivoFinanciado(emailCliente: String, emailAdmin: String) {
    // 1. Marcar como dispositivo financiado
    dpm.setDeviceOwnerType(admin, DevicePolicyManager.DEVICE_OWNER_TYPE_FINANCED)
    
    // 2. Configurar FRP con ambas cuentas
    val cuentasFRP = listOf(
        emailAdmin,      // IT puede recuperar siempre
        emailCliente     // Cliente puede usar mientras pague
    )
    
    val policy = FactoryResetProtectionPolicy.Builder()
        .setFactoryResetProtectionAccounts(cuentasFRP)
        .setFactoryResetProtectionEnabled(true)
        .build()
    
    dpm.setFactoryResetProtectionPolicy(admin, policy)
    
    // 3. Bloquear factory reset desde Settings
    dpm.addUserRestriction(admin, UserManager.DISALLOW_FACTORY_RESET)
    
    // 4. Bloquear desinstalación del MDM
    dpm.setUninstallBlocked(admin, context.packageName, true)
    
    Log.i("Financed", """
        💳 DISPOSITIVO FINANCIADO CONFIGURADO
        ══════════════════════════════════════
        Cliente: $emailCliente
        Admin: $emailAdmin
        
        Protecciones:
        ✅ FRP con cuentas cliente + admin
        ✅ Factory reset bloqueado
        ✅ MDM no desinstalable
        
        Si cliente no paga:
        → Remover su cuenta de FRP
        → Solo admin puede recuperar dispositivo
    """.trimIndent())
}

// Cuando cliente deja de pagar: remover su acceso
@RequiresApi(Build.VERSION_CODES.P)
fun bloquearDispositivoPorNoPago(emailCliente: String) {
    // Solo dejar cuenta admin en FRP
    val policy = FactoryResetProtectionPolicy.Builder()
        .setFactoryResetProtectionAccounts(listOf("admin@empresa.com"))
        .setFactoryResetProtectionEnabled(true)
        .build()
    
    dpm.setFactoryResetProtectionPolicy(admin, policy)
    
    Log.i("Financed", "🔒 Dispositivo bloqueado - cuenta cliente removida de FRP")
}
```

---

## ⚠️ Consideraciones Importantes

### Riesgos Críticos

**Peligro de Bloqueo Permanente**:
```
ESCENARIO CRÍTICO:
1. Configurar FRP con cuenta inexistente
2. Hacer factory reset
3. Intentar desbloquear con cuenta
4. Cuenta no existe o contraseña perdida
5. ❌ DISPOSITIVO BLOQUEADO PERMANENTEMENTE

Prevención:
├── Verificar cuentas EXISTEN
├── Verificar contraseñas FUNCIONAN
├── Documentar credenciales de recuperación
├── Tener múltiples cuentas admin (redundancia)
└── Probar en dispositivo de desarrollo primero
```

**Cuentas de Recuperación**:
```
Mejores Prácticas:
├── Mínimo 2 cuentas admin (redundancia)
├── Cuentas de servicio (no personales)
├── Credenciales en vault seguro (1Password, LastPass)
├── Proceso documentado de recuperación
└── Contact list actualizado de quién tiene acceso
```

### Compatibilidad

**Versiones de Android**:
```
Android 5.1 - 8.1 (API 22-27):
├── FRP estándar de Google
├── No configurable por DPM
└── Basado en cuentas Google del dispositivo

Android 9+ (API 28+):
├── FRP configurable por DPM ✅
├── setFactoryResetProtectionPolicy() disponible
└── Control corporativo completo
```

### Limitaciones

**FRP NO protege contra**:
- Reset desde bootloader desbloqueado (desarrollo)
- Flasheo de ROM custom (requiere bootloader desbloqueado)
- Ataques hardware avanzados
- Ingeniería social para obtener credenciales

**FRP SÍ protege contra**:
- ✅ Usuario/ladrón haciendo factory reset desde Settings
- ✅ Factory reset desde recovery mode estándar
- ✅ Uso casual del dispositivo tras reset
- ✅ Venta del dispositivo robado

### Testing

**Proceso de Validación**:
```
ANTES de Despliegue en Producción:
1. Configurar FRP en dispositivo de prueba
2. Hacer factory reset
3. Intentar desbloquear con cuenta autorizada
4. Verificar que funciona correctamente
5. Documentar proceso de recuperación
6. Entrenar equipo IT

❌ NUNCA aplicar FRP en producción sin testing
```

---

## 💡 Recomendaciones

### Configuración Inicial
- **Siempre validar** que cuentas de recuperación existen y son accesibles
- **Documentar credenciales** en sistema seguro (vault)
- **Configurar múltiples cuentas** admin (mínimo 2) para redundancia
- **Probar proceso** de recuperación en dispositivo de prueba

### Gestión de Cuentas
- **Usar cuentas de servicio** en lugar de cuentas personales
- **Rotar contraseñas** periódicamente pero mantener historial
- **Sincronizar cambios** entre servidor MDM y políticas FRP
- **Auditar acceso** a cuentas de recuperación regularmente

### Dispositivos Financiados
- **Incluir cuenta cliente** inicialmente en FRP
- **Remover cuenta cliente** si no hay pago
- **Mantener cuenta admin** siempre en FRP
- **Documentar proceso** de recuperación y bloqueo

### Seguridad en Capas
- **Combinar FRP** con `DISALLOW_FACTORY_RESET`
- **Bloquear bootloader** si es posible (OEM dependent)
- **Habilitar Find My Device** para tracking
- **Configurar remote wipe** en servidor MDM

### Recuperación
- **Documentar proceso** paso a paso de recuperación
- **Capacitar equipo IT** en recuperación de dispositivos
- **Mantener contacto list** actualizado de quiénes tienen acceso
- **Plan de contingencia** si cuentas principales fallan

### Compliance
- **Informar usuarios** sobre FRP en dispositivos corporativos
- **Documentar políticas** de recuperación y devolución
- **Proceso claro** para ex-empleados
- **Auditar regularmente** que FRP está activo

---

## 📊 Auditoría de FRP

```kotlin
@RequiresApi(Build.VERSION_CODES.P)
fun generarReporteFRP(): String {
    return buildString {
        append("═══════════════════════════════════════════\n")
        append("    REPORTE FACTORY RESET PROTECTION\n")
        append("═══════════════════════════════════════════\n\n")
        
        // Política FRP
        append("🔒 POLÍTICA FRP\n")
        append("───────────────────────────────────────────\n")
        
        val policy = dpm.getFactoryResetProtectionPolicy(admin)
        if (policy == null) {
            append("Estado: ⚠️ NO CONFIGURADA\n")
            append("Nivel de protección: BAJO\n")
            append("Riesgo: Alto - dispositivo vulnerable a robo\n")
        } else {
            val habilitado = policy.isFactoryResetProtectionEnabled
            val cuentas = policy.factoryResetProtectionAccounts
            
            append("Estado: ${if (habilitado) "✅ HABILITADA" else "❌ DESHABILITADA"}\n")
            append("Cuentas autorizadas: ${cuentas.size}\n")
            
            if (cuentas.isEmpty()) {
                append("⚠️ CRÍTICO: Sin cuentas configuradas\n")
            } else {
                append("\nCuentas:\n")
                cuentas.forEachIndexed { index, cuenta ->
                    append("  ${index + 1}. $cuenta\n")
                }
            }
            
            append("\nNivel de protección: ${calcularNivelProteccion(cuentas.size)}\n")
        }
        
        append("\n")
        
        // Restricción factory reset
        append("🚫 RESTRICCIÓN FACTORY RESET\n")
        append("───────────────────────────────────────────\n")
        val restrictions = dpm.getUserRestrictions(admin)
        val resetBloqueado = restrictions.getBoolean(UserManager.DISALLOW_FACTORY_RESET, false)
        
        append("DISALLOW_FACTORY_RESET: ${if (resetBloqueado) "✅ ACTIVO" else "❌ INACTIVO"}\n")
        
        append("\n")
        
        // Recomendaciones
        append("💡 RECOMENDACIONES\n")
        append("───────────────────────────────────────────\n")
        
        if (policy == null || !policy.isFactoryResetProtectionEnabled) {
            append("❗ Configurar FRP inmediatamente\n")
        }
        if (!resetBloqueado) {
            append("❗ Habilitar DISALLOW_FACTORY_RESET\n")
        }
        if (policy != null && policy.factoryResetProtectionAccounts.size < 2) {
            append("❗ Agregar cuenta admin redundante\n")
        }
        
        append("\n")
        append("Generado: ${Date()}\n")
    }
}

fun calcularNivelProteccion(numCuentas: Int): String {
    return when {
        numCuentas == 0 -> "⚠️ CRÍTICO"
        numCuentas == 1 -> "🟡 MEDIO (sin redundancia)"
        numCuentas >= 2 -> "🟢 ALTO"
        else -> "DESCONOCIDO"
    }
}
```

---

## 📚 Próxima Categoría

**19. Compliance y DeviceCompliance** (~15 métodos)

Esta categoría cubrirá:
- Device compliance status
- Compliance acknowledgement
- Managed profile time off
- Security requirements
- Compliance reporting

---

*Documentación generada para DevicePolicyManager API de Android Enterprise - Categoría 18 de 22*