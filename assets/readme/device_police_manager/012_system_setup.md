# Documentación DevicePolicyManager - Categoría 12: Configuraciones del Sistema

## 📋 Visión General

La categoría de **Configuraciones del Sistema** agrupa aproximadamente **40 métodos** que permiten controlar configuraciones globales del dispositivo, personalizar la experiencia de usuario con branding corporativo, y gestionar ajustes del sistema que normalmente solo serían accesibles a través de Settings.

## 🎯 Propósito

Estos métodos permiten:
- **Modificar Settings** del sistema (Global, Secure, System)
- **Configurar fecha y hora** automática o manualmente
- **Controlar volumen maestro** del dispositivo
- **Personalizar branding** corporativo (nombre, color, logo)
- **Gestionar status bar** y elementos de UI
- **Establecer mensajes** de soporte para usuarios
- **Configurar locales** del sistema

---

## ⚙️ Métodos de Configuración de Settings

**Nota**: Los métodos `setGlobalSetting()`, `setSecureSetting()` y `setSystemSetting()` ya fueron cubiertos en profundidad en la documentación del archivo **device_policy_manager_settings_key.md**. Aquí proporcionaremos un resumen ejecutivo.

### `setGlobalSetting(ComponentName admin, String setting, String value)`

**Propósito**: Modifica configuraciones globales que afectan a TODO el dispositivo y TODOS los usuarios.

**Alcance**: Settings.Global

**Casos de Uso Principales**:
- Configuración de conectividad (WiFi, Bluetooth, datos)
- Debugging y desarrollo (ADB, opciones de desarrollador)
- Comportamiento del sistema (tiempo automático, proxy)
- Animaciones y rendimiento

**Ejemplo de Uso**:
```kotlin
fun configurarGlobalSettings() {
    // Deshabilitar ADB para seguridad
    dpm.setGlobalSetting(admin, Settings.Global.ADB_ENABLED, "0")
    
    // Forzar tiempo automático
    dpm.setGlobalSetting(admin, Settings.Global.AUTO_TIME, "1")
    dpm.setGlobalSetting(admin, Settings.Global.AUTO_TIME_ZONE, "1")
    
    // Pantalla siempre encendida cuando está conectado (kiosko)
    dpm.setGlobalSetting(admin, Settings.Global.STAY_ON_WHILE_PLUGGED_IN, "7")
    
    // Deshabilitar animaciones (mejor rendimiento)
    dpm.setGlobalSetting(admin, Settings.Global.ANIMATOR_DURATION_SCALE, "0")
    dpm.setGlobalSetting(admin, Settings.Global.TRANSITION_ANIMATION_SCALE, "0")
    dpm.setGlobalSetting(admin, Settings.Global.WINDOW_ANIMATION_SCALE, "0")
    
    Log.i("Settings", "✅ Configuraciones globales aplicadas")
}
```

---

### `setSecureSetting(ComponentName admin, String setting, String value)`

**Propósito**: Modifica configuraciones seguras por usuario, más sensibles que las de System.

**Alcance**: Settings.Secure (por usuario)

**Casos de Uso Principales**:
- Localización y ubicación
- Seguridad e instalación de apps
- Accesibilidad
- Métodos de entrada (teclados)
- Configuración de pantalla

**Ejemplo de Uso**:
```kotlin
fun configurarSecureSettings() {
    // Bloquear instalación de apps desconocidas
    dpm.setSecureSetting(admin, Settings.Secure.INSTALL_NON_MARKET_APPS, "0")
    
    // Ubicación en alta precisión
    dpm.setSecureSetting(admin, Settings.Secure.LOCATION_MODE, "3")
    
    // Deshabilitar rotación automática
    dpm.setSecureSetting(admin, Settings.Secure.ACCELEROMETER_ROTATION, "0")
    
    // Forzar orientación landscape
    dpm.setSecureSetting(admin, Settings.Secure.USER_ROTATION, "1")
    
    // Timeout de pantalla (30 segundos)
    dpm.setSecureSetting(admin, Settings.Secure.SCREEN_OFF_TIMEOUT, "30000")
    
    Log.i("Settings", "✅ Configuraciones seguras aplicadas")
}
```

---

### `setSystemSetting(ComponentName admin, String setting, String value)`

**Propósito**: Modifica configuraciones del sistema (legacy). Muchas han sido movidas a Secure en versiones modernas.

**Alcance**: Settings.System

**Casos de Uso Principales**:
- Brillo de pantalla
- Volúmenes de audio
- Formato de fecha/hora
- Configuración de fuente
- Sonidos del sistema

**Ejemplo de Uso**:
```kotlin
fun configurarSystemSettings() {
    // Brillo fijo al 80%
    dpm.setSystemSetting(admin, Settings.System.SCREEN_BRIGHTNESS_MODE, "0")
    dpm.setSystemSetting(admin, Settings.System.SCREEN_BRIGHTNESS, "204")
    
    // Formato 24 horas
    dpm.setSystemSetting(admin, Settings.System.TIME_12_24, "24")
    
    // Volumen de timbre fijo
    dpm.setSystemSetting(admin, Settings.System.VOLUME_RING, "5")
    
    // Deshabilitar sonidos del sistema
    dpm.setSystemSetting(admin, Settings.System.SOUND_EFFECTS_ENABLED, "0")
    dpm.setSystemSetting(admin, Settings.System.HAPTIC_FEEDBACK_ENABLED, "0")
    
    Log.i("Settings", "✅ Configuraciones del sistema aplicadas")
}
```

---

## 🕐 Métodos de Configuración de Fecha y Hora

### `setTime(ComponentName admin, long millis)`

**Propósito**: Establece la fecha y hora del sistema manualmente a un timestamp específico.

**Casos de Uso**:
- Dispositivos sin acceso a red (no pueden sincronizar)
- Testing con fechas específicas
- Dispositivos en ubicaciones sin NTP
- Corrección manual de hora incorrecta

**Ejemplo de Uso**:
```kotlin
fun establecerHoraManual(fecha: Date) {
    val timestamp = fecha.time
    
    // Primero deshabilitar tiempo automático
    dpm.setGlobalSetting(admin, Settings.Global.AUTO_TIME, "0")
    
    // Establecer hora manual
    val exito = dpm.setTime(admin, timestamp)
    
    if (exito) {
        Log.i("Time", "✅ Hora establecida: $fecha")
    } else {
        Log.e("Time", "❌ No se pudo establecer la hora")
    }
}

// Establecer hora actual del servidor
fun sincronizarConServidorCorporativo() {
    val horaServidor = obtenerHoraServidorCorporativo()
    establecerHoraManual(horaServidor)
}

// Establecer fecha específica para testing
fun establecerFechaTesting() {
    val calendar = Calendar.getInstance().apply {
        set(2024, Calendar.DECEMBER, 25, 10, 30, 0)
    }
    establecerHoraManual(calendar.time)
}
```

**Retorna**: `boolean`
- `true`: Hora establecida exitosamente
- `false`: Fallo (típicamente si AUTO_TIME está habilitado)

**⚠️ Importante**:
- Debe deshabilitar `AUTO_TIME` primero
- Cambios manuales pueden causar problemas con certificados SSL/TLS
- Apps que dependen de hora precisa pueden fallar
- No recomendado en producción (usar sincronización NTP)

---

### `setTimeZone(ComponentName admin, String timeZone)`

**Propósito**: Establece la zona horaria del dispositivo manualmente.

**Formato**: Identificadores de zona horaria IANA (ej: "America/New_York", "Europe/London")

**Casos de Uso**:
- Dispositivos que viajan entre zonas horarias
- Forzar zona horaria corporativa
- Dispositivos sin GPS/red para detectar zona automáticamente

**Ejemplo de Uso**:
```kotlin
fun establecerZonaHoraria(zonaHoraria: String) {
    // Deshabilitar zona horaria automática primero
    dpm.setGlobalSetting(admin, Settings.Global.AUTO_TIME_ZONE, "0")
    
    // Establecer zona horaria manual
    dpm.setTimeZone(admin, zonaHoraria)
    
    Log.i("TimeZone", "✅ Zona horaria establecida: $zonaHoraria")
}

// Ejemplos de zonas horarias
fun establecerZonasComunes() {
    establecerZonaHoraria("America/New_York")   // EST/EDT
    establecerZonaHoraria("America/Los_Angeles") // PST/PDT
    establecerZonaHoraria("Europe/London")       // GMT/BST
    establecerZonaHoraria("Asia/Tokyo")          // JST
    establecerZonaHoraria("America/Lima")        // PET
}

// Zona horaria por ubicación de oficina
fun establecerZonaPorOficina(oficinaId: String) {
    val zona = when (oficinaId) {
        "HQ_NY" -> "America/New_York"
        "OFFICE_LA" -> "America/Los_Angeles"
        "OFFICE_LONDON" -> "Europe/London"
        "OFFICE_LIMA" -> "America/Lima"
        else -> TimeZone.getDefault().id
    }
    establecerZonaHoraria(zona)
}
```

**Lista de Zonas Comunes**:
```kotlin
val zonasHorariasComunes = mapOf(
    // América
    "America/New_York" to "Eastern Time (US & Canada)",
    "America/Chicago" to "Central Time (US & Canada)",
    "America/Denver" to "Mountain Time (US & Canada)",
    "America/Los_Angeles" to "Pacific Time (US & Canada)",
    "America/Mexico_City" to "Ciudad de México",
    "America/Lima" to "Lima, Perú",
    "America/Sao_Paulo" to "São Paulo, Brasil",
    "America/Buenos_Aires" to "Buenos Aires, Argentina",
    
    // Europa
    "Europe/London" to "Londres, Reino Unido",
    "Europe/Paris" to "París, Francia",
    "Europe/Berlin" to "Berlín, Alemania",
    "Europe/Madrid" to "Madrid, España",
    "Europe/Moscow" to "Moscú, Rusia",
    
    // Asia
    "Asia/Tokyo" to "Tokio, Japón",
    "Asia/Shanghai" to "Shanghai, China",
    "Asia/Singapore" to "Singapur",
    "Asia/Dubai" to "Dubái, EAU",
    "Asia/Kolkata" to "India",
    
    // Oceanía
    "Australia/Sydney" to "Sídney, Australia",
    "Pacific/Auckland" to "Auckland, Nueva Zelanda"
)
```

---

### `setAutoTimeEnabled(ComponentName admin, boolean enabled)`

**Propósito**: Habilita o deshabilita la sincronización automática de fecha/hora desde la red.

**Ejemplo de Uso**:
```kotlin
// Habilitar sincronización automática (recomendado)
dpm.setAutoTimeEnabled(admin, true)
Log.i("Time", "✅ Sincronización automática habilitada")

// Deshabilitar para control manual
dpm.setAutoTimeEnabled(admin, false)
Log.i("Time", "⚠️ Sincronización automática deshabilitada - control manual")
```

---

### `getAutoTimeEnabled(ComponentName admin)`

**Propósito**: Verifica si la sincronización automática de tiempo está habilitada.

**Retorna**: `boolean`

---

### `setAutoTimeZoneEnabled(ComponentName admin, boolean enabled)`

**Propósito**: Habilita o deshabilita la detección automática de zona horaria.

**Ejemplo de Uso**:
```kotlin
// Habilitar detección automática (usa red/GPS)
dpm.setAutoTimeZoneEnabled(admin, true)

// Deshabilitar para zona horaria manual
dpm.setAutoTimeZoneEnabled(admin, false)
```

---

### `getAutoTimeZoneEnabled(ComponentName admin)`

**Propósito**: Verifica si la detección automática de zona horaria está habilitada.

**Retorna**: `boolean`

```kotlin
fun verificarConfiguracionTiempo(): String {
    val autoTime = dpm.getAutoTimeEnabled(admin)
    val autoTimeZone = dpm.getAutoTimeZoneEnabled(admin)
    
    return buildString {
        append("🕐 CONFIGURACIÓN DE FECHA/HORA\n")
        append("═══════════════════════════════════\n\n")
        
        append("Sincronización automática: ${if (autoTime) "✅ HABILITADA" else "❌ DESHABILITADA"}\n")
        if (autoTime) {
            append("   Hora se sincroniza desde red (NTP)\n")
        } else {
            append("   Hora configurada manualmente\n")
            append("   ⚠️ Puede derivar con el tiempo\n")
        }
        
        append("\nZona horaria automática: ${if (autoTimeZone) "✅ HABILITADA" else "❌ DESHABILITADA"}\n")
        if (autoTimeZone) {
            append("   Zona se detecta por ubicación/red\n")
        } else {
            append("   Zona configurada manualmente\n")
        }
        
        append("\nZona horaria actual: ${TimeZone.getDefault().id}\n")
        append("Hora actual: ${Date()}\n")
    }
}
```

---

## 🔊 Métodos de Control de Volumen

### `setMasterVolumeMuted(ComponentName admin, boolean on)`

**Propósito**: Silencia o activa el volumen maestro del dispositivo. Cuando está silenciado, NINGÚN sonido se reproduce (llamadas, medios, notificaciones, sistema).

**Casos de Uso**:
- **Ambientes silenciosos**: Hospitales, bibliotecas, salas de conferencia
- **Horarios específicos**: Silenciar automáticamente de noche
- **Kioscos**: Dispositivos de información sin sonido
- **Seguridad**: Prevenir sonidos que puedan alertar

**Ejemplo de Uso**:
```kotlin
// Silenciar dispositivo completamente
fun silenciarDispositivo() {
    dpm.setMasterVolumeMuted(admin, true)
    Log.i("Volume", "🔇 Dispositivo SILENCIADO completamente")
    
    // TODO el audio bloqueado:
    // - Llamadas (incluso ring)
    // - Notificaciones
    // - Medios (música, videos)
    // - Alarmas
    // - Sonidos del sistema
}

// Activar sonido nuevamente
fun activarSonido() {
    dpm.setMasterVolumeMuted(admin, false)
    Log.i("Volume", "🔊 Sonido ACTIVADO")
}

// Control por horario
fun controlarVolumenPorHorario() {
    val hora = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    
    val debeEstarSilenciado = hora in 22..7 // 10 PM a 7 AM
    
    dpm.setMasterVolumeMuted(admin, debeEstarSilenciado)
    
    if (debeEstarSilenciado) {
        Log.i("Volume", "🌙 Modo nocturno - Dispositivo silenciado")
    } else {
        Log.i("Volume", "☀️ Modo diurno - Sonido activo")
    }
}

// Silenciar en ubicaciones específicas
fun controlarVolumenPorUbicacion(enHospital: Boolean, enBiblioteca: Boolean) {
    val debeEstarSilenciado = enHospital || enBiblioteca
    
    dpm.setMasterVolumeMuted(admin, debeEstarSilenciado)
    
    if (debeEstarSilenciado) {
        mostrarNotificacion("Dispositivo silenciado", "Está en un área que requiere silencio")
    }
}
```

**⚠️ Advertencia**:
- Silencia COMPLETAMENTE el dispositivo
- Incluso llamadas de emergencia pueden no escucharse
- Alarmas no sonarán
- Usar con precaución en dispositivos críticos

---

### `isMasterVolumeMuted(ComponentName admin)`

**Propósito**: Verifica si el volumen maestro está silenciado.

**Retorna**: `boolean`

```kotlin
fun verificarEstadoVolumen(): String {
    val silenciado = dpm.isMasterVolumeMuted(admin)
    
    return if (silenciado) {
        "🔇 Dispositivo SILENCIADO\n" +
        "   Sin sonido de ningún tipo\n" +
        "   Llamadas, medios, alarmas: Todo mudo"
    } else {
        "🔊 Dispositivo CON SONIDO\n" +
        "   Audio funcionando normalmente"
    }
}
```

---

## 📊 Métodos de Control de UI del Sistema

### `setStatusBarDisabled(ComponentName admin, boolean disabled)`

**Propósito**: Oculta o muestra la barra de estado (status bar) del sistema Android. Cuando está deshabilitada, la barra superior desaparece completamente.

**Casos de Uso**:
- **Kioscos inmersivos**: Experiencia de pantalla completa
- **Señalización digital**: Sin distracciones de UI del sistema
- **Dispositivos dedicados**: Ocultar información del sistema
- **Aplicaciones fullscreen**: Máxima área de contenido

**Ejemplo de Uso**:
```kotlin
// Ocultar status bar
fun ocultarStatusBar() {
    dpm.setStatusBarDisabled(admin, true)
    Log.i("UI", "📊❌ Status bar OCULTA")
    
    // Usuario no verá:
    // - Hora
    // - Nivel de batería
    // - Señal de red
    // - Notificaciones
    // - Iconos del sistema
}

// Mostrar status bar
fun mostrarStatusBar() {
    dpm.setStatusBarDisabled(admin, false)
    Log.i("UI", "📊✅ Status bar VISIBLE")
}

// Configuración de kiosko inmersivo completo
fun configurarKioskoInmersivo(activity: Activity) {
    // 1. Ocultar status bar
    dpm.setStatusBarDisabled(admin, true)
    
    // 2. Ocultar navigation bar con flags
    activity.window.decorView.systemUiVisibility = (
        View.SYSTEM_UI_FLAG_FULLSCREEN or
        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
    )
    
    Log.i("Kiosko", "Modo inmersivo completo activado")
}
```

**Comportamiento**:
- Status bar desaparece completamente
- No se puede mostrar deslizando desde arriba
- Quick Settings inaccesible
- Notificaciones no visibles (siguen funcionando en background)

**⚠️ Limitaciones**:
- Solo Device Owner puede deshabilitar status bar
- En algunos fabricantes (Samsung) puede tener comportamiento diferente
- Usuario puede sentirse desorientado sin información del sistema

---

## 🏢 Métodos de Branding Corporativo

### `setOrganizationName(ComponentName admin, CharSequence title)`

**Propósito**: Establece el nombre de la organización que aparece en varias partes del sistema Android, especialmente en el perfil de trabajo.

**Dónde Aparece**:
- Pantalla de bloqueo
- Settings del dispositivo
- Separador de Work Profile en launcher
- Notificaciones del sistema sobre gestión

**Casos de Uso**:
- Branding corporativo
- Identificar propiedad del dispositivo
- Claridad para usuarios sobre gestión corporativa

**Ejemplo de Uso**:
```kotlin
fun establecerNombreOrganizacion(nombre: String) {
    dpm.setOrganizationName(admin, nombre)
    Log.i("Branding", "🏢 Organización: $nombre")
    
    // El usuario verá:
    // "Este dispositivo es administrado por [nombre]"
    // En lockscreen, settings, notificaciones
}

// Ejemplos
establecerNombreOrganizacion("Empresa Corp S.A.")
establecerNombreOrganizacion("Hospital Central")
establecerNombreOrganizacion("Universidad Nacional")
establecerNombreOrganizacion("Gobierno Municipal")
```

---

### `getOrganizationName(ComponentName admin)`

**Propósito**: Obtiene el nombre de la organización configurado.

**Retorna**: `CharSequence` con el nombre, o `null` si no está configurado.

---

### `setOrganizationColor(ComponentName admin, int color)`

**Propósito**: Establece el color corporativo que se usa en elementos de UI relacionados con la gestión del dispositivo.

**Dónde Aplica**:
- Badge del Work Profile
- Iconos de apps de trabajo
- Elementos de UI de gestión
- Temas de notificaciones corporativas

**Ejemplo de Uso**:
```kotlin
fun establecerColorCorporativo(color: Int) {
    dpm.setOrganizationColor(admin, color)
    Log.i("Branding", "🎨 Color corporativo establecido")
}

// Colores corporativos comunes
fun aplicarBrandingCompleto() {
    // Azul corporativo
    establecerColorCorporativo(Color.parseColor("#0066CC"))
    
    // Verde corporativo
    // establecerColorCorporativo(Color.parseColor("#00A86B"))
    
    // Rojo corporativo
    // establecerColorCorporativo(Color.parseColor("#DC143C"))
}

// Branding por código de color hexadecimal
fun establecerColorHex(colorHex: String) {
    try {
        val color = Color.parseColor(colorHex)
        dpm.setOrganizationColor(admin, color)
    } catch (e: IllegalArgumentException) {
        Log.e("Branding", "Color inválido: $colorHex")
    }
}
```

---

### `getOrganizationColor(ComponentName admin)`

**Propósito**: Obtiene el color corporativo configurado.

**Retorna**: `int` con el color (valor ARGB).

```kotlin
fun aplicarBrandingCompleto(nombreEmpresa: String, colorHex: String) {
    // 1. Establecer nombre
    dpm.setOrganizationName(admin, nombreEmpresa)
    
    // 2. Establecer color
    val color = Color.parseColor(colorHex)
    dpm.setOrganizationColor(admin, color)
    
    // 3. Verificar
    val nombreConfigurado = dpm.getOrganizationName(admin)
    val colorConfigurado = dpm.getOrganizationColor(admin)
    
    Log.i("Branding", """
        🏢 BRANDING CORPORATIVO APLICADO
        ════════════════════════════════
        Organización: $nombreConfigurado
        Color: #${Integer.toHexString(colorConfigurado)}
    """.trimIndent())
}

// Ejemplo
aplicarBrandingCompleto("Empresa Corp", "#0066CC")
```

---

### `setDeviceOwnerLockScreenInfo(ComponentName admin, CharSequence info)`

**Propósito**: Establece un mensaje que aparece en la pantalla de bloqueo del dispositivo. Visible incluso cuando el dispositivo está bloqueado.

**Casos de Uso**:
- Información de contacto para devolución
- Identificación de propiedad corporativa
- Instrucciones de emergencia
- Información de soporte técnico

**Ejemplo de Uso**:
```kotlin
fun establecerMensajeLockscreen(mensaje: String) {
    dpm.setDeviceOwnerLockScreenInfo(admin, mensaje)
    Log.i("Lockscreen", "🔒 Mensaje de lockscreen establecido")
}

// Información de devolución
fun configurarInfoDevolucion() {
    val mensaje = """
        Propiedad de Empresa Corp
        
        Si encuentra este dispositivo, por favor contacte:
        IT Support: +1 (555) 123-4567
        Email: it-support@empresa.com
        
        RECOMPENSA por devolución
    """.trimIndent()
    
    establecerMensajeLockscreen(mensaje)
}

// Información corporativa simple
fun configurarInfoCorporativa() {
    val mensaje = "Dispositivo corporativo de Empresa Corp\nID: ${obtenerDeviceId()}"
    establecerMensajeLockscreen(mensaje)
}

// Información de emergencia
fun configurarInfoEmergencia() {
    val mensaje = """
        🏥 DISPOSITIVO MÉDICO
        En caso de emergencia llamar: 911
        Hospital Central - Departamento IT
    """.trimIndent()
    
    establecerMensajeLockscreen(mensaje)
}
```

**⚠️ Consideraciones**:
- Mensaje visible para CUALQUIERA sin desbloquear
- No incluir información sensible o privada
- Mantener mensaje conciso (máximo 3-4 líneas)
- Considerar privacidad vs utilidad

---

### `getDeviceOwnerLockScreenInfo()`

**Propósito**: Obtiene el mensaje de lockscreen configurado.

**Retorna**: `CharSequence` con el mensaje, o `null`.

---

## 💬 Métodos de Mensajes de Soporte

### `setLongSupportMessage(ComponentName admin, CharSequence message)`

**Propósito**: Establece un mensaje largo de soporte que aparece en Settings cuando el usuario busca ayuda sobre gestión del dispositivo.

**Cuándo se Muestra**:
- Settings > Security > Device admin apps
- Al tocar en el Device Owner/Profile Owner
- Pantalla de información del administrador

**Casos de Uso**:
- Instrucciones detalladas de soporte
- Procedimientos corporativos
- Información de contacto extendida
- Políticas y guías del dispositivo

**Ejemplo de Uso**:
```kotlin
fun establecerMensajeSoporteLargo() {
    val mensaje = """
        📱 SOPORTE TÉCNICO - EMPRESA CORP
        ════════════════════════════════════
        
        Este dispositivo es administrado por el Departamento de TI de Empresa Corp.
        
        🆘 SOPORTE
        • Email: it-support@empresa.com
        • Teléfono: +1 (555) 123-4567
        • Chat: https://support.empresa.com/chat
        • Horario: Lunes a Viernes, 8:00 AM - 6:00 PM
        
        📋 PROBLEMAS COMUNES
        
        1. No puedo instalar una aplicación
           → Solo apps autorizadas pueden instalarse. Solicita aprobación a IT.
        
        2. El dispositivo está bloqueado
           → Contacta a IT con tu ID de empleado para desbloqueo remoto.
        
        3. Olvidé mi contraseña
           → IT puede resetear tu contraseña remotamente en menos de 15 minutos.
        
        4. Necesito una app específica
           → Envía solicitud a it-support@empresa.com con justificación.
        
        📖 POLÍTICAS
        • No instalar apps de fuentes desconocidas
        • Reportar dispositivos perdidos inmediatamente
        • No compartir contraseña corporativa
        • Mantener el dispositivo actualizado
        
        🔒 SEGURIDAD
        Si sospechas que tu dispositivo está comprometido, contacta a:
        security@empresa.com o llama al (555) 999-SECURITY
        
        ⚖️ CUMPLIMIENTO
        Este dispositivo está sujeto a políticas corporativas. El uso indebido puede resultar en acción disciplinaria.
        
        Última actualización: ${Date()}
    """.trimIndent()
    
    dpm.setLongSupportMessage(admin, mensaje)
    Log.i("Support", "✅ Mensaje largo de soporte establecido")
}
```

---

### `getLongSupportMessage(ComponentName admin)`

**Propósito**: Obtiene el mensaje largo de soporte configurado.

**Retorna**: `CharSequence` con el mensaje, o `null`.

---

### `setShortSupportMessage(ComponentName admin, CharSequence message)`

**Propósito**: Establece un mensaje corto de soporte que aparece en notificaciones del sistema sobre gestión del dispositivo.

**Cuándo se Muestra**:
- Notificaciones persistentes de gestión
- Alertas del sistema relacionadas con políticas
- Mensajes breves de administración

**Límite**: Recomendado máximo 200 caracteres.

**Ejemplo de Uso**:
```kotlin
fun establecerMensajeSoporteCorto() {
    val mensaje = "Soporte IT: it-support@empresa.com | Tel: (555) 123-4567"
    
    dpm.setShortSupportMessage(admin, mensaje)
    Log.i("Support", "✅ Mensaje corto de soporte establecido")
}

// Diferentes estilos de mensajes cortos
fun ejemplosMensajesCortos() {
    // Estilo 1: Contacto directo
    dpm.setShortSupportMessage(admin, 
        "¿Necesitas ayuda? IT: ext. 2500 o it@empresa.com")
    
    // Estilo 2: Autoservicio
    dpm.setShortSupportMessage(admin,
        "Ayuda en: support.empresa.com | Chat 24/7 disponible")
    
    // Estilo 3: Emergencia
    dpm.setShortSupportMessage(admin,
        "🆘 Soporte urgente: (555) 999-9999")
}
```

---

### `getShortSupportMessage(ComponentName admin)`

**Propósito**: Obtiene el mensaje corto de soporte configurado.

**Retorna**: `CharSequence` con el mensaje, o `null`.

---

## 🌐 Métodos de Configuración de Locales

### `setSystemLocales(ComponentName admin, LocaleList locales)`

**Propósito**: Establece los idiomas/locales del sistema. Permite configurar múltiples idiomas en orden de preferencia.

**Casos de Uso**:
- Forzar idioma corporativo
- Dispositivos multilingües
- Estandarización de interfaz
- Cumplimiento con requisitos regionales

**Ejemplo de Uso**:

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

