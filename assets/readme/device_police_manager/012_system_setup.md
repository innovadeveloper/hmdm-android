```kotlin
fun establecerIdiomasSistema(idiomasPrioritarios: List<Locale>) {
    val localeList = LocaleList(*idiomasPrioritarios.toTypedArray())
    
    dpm.setSystemLocales(admin, localeList)
    
    Log.i("Locales", "🌐 Idiomas del sistema configurados:")
    idiomasPrioritarios.forEachIndexed { index, locale ->
        Log.i("Locales", "  ${index + 1}. ${locale.displayName}")
    }
}

// Ejemplos de configuraciones comunes

// Solo español
fun configurarSoloEspanol() {
    establecerIdiomasSistema(listOf(Locale("es", "ES")))
}

// Inglés con fallback a español
fun configurarInglesEspanol() {
    establecerIdiomasSistema(listOf(
        Locale("en", "US"),
        Locale("es", "ES")
    ))
}

// Configuración multilingüe corporativa
fun configurarIdiomasCorporativos() {
    establecerIdiomasSistema(listOf(
        Locale("en", "US"),  // Inglés US (principal)
        Locale("es", "MX"),  // Español México
        Locale("pt", "BR"),  // Portugués Brasil
        Locale("fr", "FR")   // Francés
    ))
}

// Por ubicación de oficina
fun configurarPorOficina(pais: String) {
    val idiomas = when (pais) {
        "US" -> listOf(Locale.US)
        "ES" -> listOf(Locale("es", "ES"), Locale.UK)
        "MX" -> listOf(Locale("es", "MX"), Locale.US)
        "BR" -> listOf(Locale("pt", "BR"), Locale.US)
        "PE" -> listOf(Locale("es", "PE"), Locale.US)
        "JP" -> listOf(Locale.JAPAN, Locale.US)
        else -> listOf(Locale.US)
    }
    establecerIdiomasSistema(idiomas)
}
```

**Códigos de Idioma Comunes**:
```kotlin
val idiomasCorporativosComunes = mapOf(
    // Inglés
    "en-US" to Locale("en", "US"),
    "en-GB" to Locale("en", "GB"),
    "en-AU" to Locale("en", "AU"),
    
    // Español
    "es-ES" to Locale("es", "ES"),
    "es-MX" to Locale("es", "MX"),
    "es-AR" to Locale("es", "AR"),
    "es-PE" to Locale("es", "PE"),
    
    // Portugués
    "pt-BR" to Locale("pt", "BR"),
    "pt-PT" to Locale("pt", "PT"),
    
    // Francés
    "fr-FR" to Locale.FRANCE,
    "fr-CA" to Locale.CANADA_FRENCH,
    
    // Alemán
    "de-DE" to Locale.GERMANY,
    
    // Italiano
    "it-IT" to Locale.ITALY,
    
    // Chino
    "zh-CN" to Locale.SIMPLIFIED_CHINESE,
    "zh-TW" to Locale.TRADITIONAL_CHINESE,
    
    // Japonés
    "ja-JP" to Locale.JAPAN,
    
    // Coreano
    "ko-KR" to Locale.KOREA
)
```

---

### `getSystemLocales(ComponentName admin)`

**Propósito**: Obtiene la lista de locales del sistema actualmente configurados.

**Retorna**: `LocaleList` con los idiomas en orden de prioridad.

```kotlin
fun verificarIdiomasSistema(): String {
    val locales = dpm.getSystemLocales(admin)
    
    return buildString {
        append("🌐 IDIOMAS DEL SISTEMA\n")
        append("═══════════════════════════\n\n")
        
        if (locales.isEmpty) {
            append("⚙️ Usando configuración por defecto del sistema\n")
        } else {
            append("Idiomas configurados (en orden de prioridad):\n\n")
            
            for (i in 0 until locales.size()) {
                val locale = locales[i]
                append("${i + 1}. ${locale.displayName}\n")
                append("   Código: ${locale.language}-${locale.country}\n")
                append("   Idioma: ${locale.displayLanguage}\n")
                append("   País: ${locale.displayCountry}\n\n")
            }
        }
    }
}
```

---

## 🎯 Casos de Uso Completos por Escenario

### 🏢 Corporativo Estándar - Configuración Completa
```kotlin
fun configurarDispositivoCorporativo() {
    // 1. Settings del Sistema
    dpm.setGlobalSetting(admin, Settings.Global.ADB_ENABLED, "0")
    dpm.setGlobalSetting(admin, Settings.Global.AUTO_TIME, "1")
    dpm.setGlobalSetting(admin, Settings.Global.AUTO_TIME_ZONE, "1")
    
    // 2. Tiempo y Zona Horaria
    dpm.setAutoTimeEnabled(admin, true)
    dpm.setAutoTimeZoneEnabled(admin, true)
    
    // 3. Volumen normal (no silenciar)
    dpm.setMasterVolumeMuted(admin, false)
    
    // 4. Status bar visible
    dpm.setStatusBarDisabled(admin, false)
    
    // 5. Branding Corporativo
    dpm.setOrganizationName(admin, "Empresa Corp S.A.")
    dpm.setOrganizationColor(admin, Color.parseColor("#0066CC"))
    
    // 6. Lockscreen Info
    dpm.setDeviceOwnerLockScreenInfo(admin, """
        Propiedad de Empresa Corp
        Si encuentra este dispositivo: +1 (555) 123-4567
    """.trimIndent())
    
    // 7. Mensajes de Soporte
    dpm.setShortSupportMessage(admin, 
        "IT Support: it@empresa.com | Tel: (555) 123-4567")
    
    dpm.setLongSupportMessage(admin, """
        Soporte Técnico 24/7
        Email: it@empresa.com
        Teléfono: (555) 123-4567
        Portal: https://support.empresa.com
    """.trimIndent())
    
    // 8. Idioma Corporativo
    establecerIdiomasSistema(listOf(Locale.US, Locale("es", "ES")))
    
    Log.i("Setup", "✅ Configuración corporativa completa aplicada")
}
```

---

### 🏪 Kiosko - Modo Inmersivo
```kotlin
fun configurarKioskoInmersivo() {
    // 1. Pantalla siempre encendida
    dpm.setGlobalSetting(admin, Settings.Global.STAY_ON_WHILE_PLUGGED_IN, "7")
    
    // 2. Sin animaciones (mejor rendimiento)
    dpm.setGlobalSetting(admin, Settings.Global.ANIMATOR_DURATION_SCALE, "0")
    dpm.setGlobalSetting(admin, Settings.Global.TRANSITION_ANIMATION_SCALE, "0")
    dpm.setGlobalSetting(admin, Settings.Global.WINDOW_ANIMATION_SCALE, "0")
    
    // 3. Brillo fijo alto
    dpm.setSystemSetting(admin, Settings.System.SCREEN_BRIGHTNESS_MODE, "0")
    dpm.setSystemSetting(admin, Settings.System.SCREEN_BRIGHTNESS, "230")
    
    // 4. Silenciar completamente
    dpm.setMasterVolumeMuted(admin, true)
    
    // 5. Ocultar status bar
    dpm.setStatusBarDisabled(admin, true)
    
    // 6. Tiempo automático
    dpm.setAutoTimeEnabled(admin, true)
    dpm.setAutoTimeZoneEnabled(admin, true)
    
    // 7. Formato 24 horas
    dpm.setSystemSetting(admin, Settings.System.TIME_12_24, "24")
    
    // 8. Branding mínimo
    dpm.setOrganizationName(admin, "Kiosko Información")
    dpm.setOrganizationColor(admin, Color.parseColor("#FF6600"))
    
    // 9. Sin mensaje de lockscreen (kiosko no se bloquea)
    dpm.setDeviceOwnerLockScreenInfo(admin, null)
    
    Log.i("Kiosko", "✅ Kiosko inmersivo configurado")
}
```

---

## ⚠️ Consideraciones Importantes

### Settings del Sistema

**Precauciones**:
- **Valores incorrectos** pueden hacer el dispositivo inestable
- **Incompatibilidad de versiones**: Settings varían entre versiones de Android
- **OEM differences**: Samsung, Huawei, etc. pueden tener settings propios
- **Permisos**: Solo Device Owner puede modificar Global/Secure settings

**Validación**:
```kotlin
fun establecerSettingSafe(setting: String, value: String): Boolean {
    return try {
        dpm.setGlobalSetting(admin, setting, value)
        
        // Verificar que se aplicó
        val valorActual = Settings.Global.getString(context.contentResolver, setting)
        if (valorActual == value) {
            Log.i("Settings", "✅ $setting = $value")
            true
        } else {
            Log.w("Settings", "⚠️ $setting no se aplicó correctamente")
            false
        }
    } catch (e: Exception) {
        Log.e("Settings", "❌ Error al establecer $setting: ${e.message}")
        false
    }
}
```

### Fecha y Hora

**Mejores Prácticas**:
- ✅ **Usar sincronización automática** siempre que sea posible
- ✅ Servidor NTP corporativo para consistencia
- ❌ Evitar configuración manual (puede derivar)
- ⚠️ Hora incorrecta rompe certificados SSL/TLS

**Problemas Comunes**:
```kotlin
// ❌ MALO: Establecer hora sin deshabilitar AUTO_TIME
dpm.setTime(admin, System.currentTimeMillis())  // Se ignora si AUTO_TIME=1

// ✅ BUENO: Deshabilitar AUTO_TIME primero
dpm.setAutoTimeEnabled(admin, false)
dpm.setTime(admin, obtenerHoraServidorCorporativo())
```

### Volumen Maestro

**Impacto**:
- 🔇 Silenciar: NO se escucha nada (ni emergencias)
- ⚠️ Considerar contexto (hospitales vs oficinas)
- 📱 Usuario puede frustrarse si no sabe por qué no hay sonido

**Alternativas**:
```kotlin
// Alternativa: Usar DND (Do Not Disturb) en lugar de silenciar
val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) 
    as NotificationManager

// DND permite excepciones (llamadas prioritarias, alarmas)
notificationManager.setInterruptionFilter(
    NotificationManager.INTERRUPTION_FILTER_PRIORITY
)
```

### Branding Corporativo

**Consistencia**:
- Usar mismo nombre/color en todos los dispositivos
- Logo corporativo para reconocimiento visual
- Mensajes claros y profesionales

**Evitar**:
- ❌ Mensajes demasiado largos en lockscreen
- ❌ Colores que no cumplen accesibilidad
- ❌ Información sensible en lockscreen
- ❌ Contactos que ya no existen

### Idiomas/Locales

**Consideraciones Culturales**:
- 🌐 Respetar preferencias regionales
- 📅 Formatos de fecha/hora varían por país
- 💱 Moneda y números según región
- ⚙️ Algunas apps pueden no soportar todos los idiomas

**Testing**:
```kotlin
fun validarIdiomasFuncionales() {
    val locales = dpm.getSystemLocales(admin)
    
    // Verificar que apps críticas soportan idiomas configurados
    val appsCriticas = listOf("com.empresa.crm", "com.empresa.pos")
    
    appsCriticas.forEach { packageName ->
        verificarSoporteIdiomas(packageName, locales)
    }
}
```

### Status Bar

**Cuándo Deshabilitar**:
- ✅ Kioscos de información
- ✅ Señalización digital
- ✅ Dispositivos dedicados

**Cuándo NO Deshabilitar**:
- ❌ Dispositivos de trabajo general
- ❌ Si usuarios necesitan ver hora/batería
- ❌ Dispositivos personales con Work Profile

---

## 📊 Auditoría Completa de Configuración

```kotlin
fun generarReporteConfiguracionCompleto(): String {
    return buildString {
        append("📋 REPORTE DE CONFIGURACIÓN DEL SISTEMA\n")
        append("═══════════════════════════════════════════\n\n")
        
        // Tiempo
        append("🕐 FECHA Y HORA\n")
        append("─────────────────\n")
        append("Auto Time: ${if (dpm.getAutoTimeEnabled(admin)) "✅" else "❌"}\n")
        append("Auto TimeZone: ${if (dpm.getAutoTimeZoneEnabled(admin)) "✅" else "❌"}\n")
        append("Zona Horaria: ${TimeZone.getDefault().id}\n")
        append("Hora Actual: ${Date()}\n\n")
        
        // Volumen
        append("🔊 AUDIO\n")
        append("─────────────────\n")
        append("Master Volume: ${if (dpm.isMasterVolumeMuted(admin)) "🔇 MUDO" else "🔊 ACTIVO"}\n\n")
        
        // UI
        append("📊 INTERFAZ\n")
        append("─────────────────\n")
        append("Status Bar: ${if (esStatusBarVisible()) "👁️ VISIBLE" else "🚫 OCULTA"}\n\n")
        
        // Branding
        append("🏢 BRANDING\n")
        append("─────────────────\n")
        val orgName = dpm.getOrganizationName(admin)
        append("Organización: ${orgName ?: "No configurada"}\n")
        
        val orgColor = dpm.getOrganizationColor(admin)
        append("Color: #${Integer.toHexString(orgColor)}\n\n")
        
        // Lockscreen
        append("🔒 LOCKSCREEN\n")
        append("─────────────────\n")
        val lockscreenInfo = dpm.getDeviceOwnerLockScreenInfo()
        append("Mensaje: ${lockscreenInfo ?: "No configurado"}\n\n")
        
        // Soporte
        append("💬 SOPORTE\n")
        append("─────────────────\n")
        val shortMsg = dpm.getShortSupportMessage(admin)
        append("Mensaje Corto: ${shortMsg ?: "No configurado"}\n")
        
        val longMsg = dpm.getLongSupportMessage(admin)
        append("Mensaje Largo: ${if (longMsg != null) "Configurado" else "No configurado"}\n\n")
        
        // Idiomas
        append("🌐 IDIOMAS\n")
        append("─────────────────\n")
        val locales = dpm.getSystemLocales(admin)
        if (locales.isEmpty) {
            append("Sistema por defecto\n")
        } else {
            for (i in 0 until locales.size()) {
                append("${i + 1}. ${locales[i].displayName}\n")
            }
        }
        
        append("\n")
        append("Generado: ${Date()}\n")
    }
}

fun esStatusBarVisible(): Boolean {
    // Método para verificar si status bar está visible
    // Implementación depende de la versión de Android
    return true  // Placeholder
}
```

---

## 📚 Próxima Categoría

**13. Device Owner y Profile Owner** (~20 métodos)

Esta categoría cubrirá:
- Verificación de Device Owner/Profile Owner
- Obtener información del owner
- Transfer ownership
- Provisioning
- Device IDs
- Organization IDs

---

*Documentación generada para DevicePolicyManager API de Android Enterprise - Categoría 12 de 22*