# Documentación DevicePolicyManager - Categoría 7: Configuración de Red

## 📋 Visión General

La categoría de **Configuración de Red** agrupa aproximadamente **30 métodos** que permiten controlar y gestionar todos los aspectos de conectividad de red en dispositivos administrados. Es una de las categorías más amplias y críticas para la seguridad corporativa.

## 🎯 Propósito

Estos métodos permiten:
- **Configurar VPN Always-On** obligatoria
- **Establecer proxy global** para todo el tráfico
- **Gestionar APNs** (Access Point Names) personalizados
- **Configurar Private DNS** para privacidad
- **Controlar políticas WiFi** por SSID
- **Establecer seguridad mínima** de red
- **Gestionar preferencias** de red

---

## 🔐 Métodos de VPN Always-On

### `setAlwaysOnVpnPackage(ComponentName admin, String vpnPackage, boolean lockdownEnabled)`

**Propósito**: Configura una aplicación VPN que debe estar SIEMPRE activa. Si la VPN se desconecta, el dispositivo bloquea todo el tráfico de red hasta que la VPN se reconecte.

**Parámetros**:
- `vpnPackage`: Package name de la app VPN (ej: "com.empresa.vpn")
- `lockdownEnabled`:
   - `true`: Modo lockdown - sin VPN = sin internet
   - `false`: Modo best-effort - intenta mantener VPN pero permite tráfico si falla

**Casos de Uso**:

**Modo Lockdown (lockdownEnabled = true)**:
- **Máxima seguridad**: Todo tráfico DEBE pasar por VPN corporativa
- **Prevención de fugas**: Evitar que datos salgan fuera del túnel VPN
- **Cumplimiento estricto**: Regulaciones que requieren tráfico encriptado
- **Zero Trust**: Ningún acceso sin VPN activa

**Modo Best-Effort (lockdownEnabled = false)**:
- **Productividad primero**: Mantener conectividad si VPN falla temporalmente
- **Redes confiables**: Permitir tráfico directo en ubicaciones seguras
- **Experiencia de usuario**: Menos frustrante si hay problemas de VPN

**Ejemplo de Uso**:
```kotlin
// Configurar VPN Always-On con lockdown
fun configurarVPNAlwaysOn() {
    val vpnPackage = "com.empresa.securevpn"
    
    try {
        // Modo lockdown: Sin VPN = Sin internet
        dpm.setAlwaysOnVpnPackage(admin, vpnPackage, true)
        
        Log.i("VPN", "✅ VPN Always-On configurada en modo LOCKDOWN")
        Log.i("VPN", "   Paquete: $vpnPackage")
        Log.i("VPN", "   Sin VPN activa = Sin acceso a internet")
        
    } catch (e: PackageManager.NameNotFoundException) {
        Log.e("VPN", "❌ App VPN no instalada: $vpnPackage")
        // Instalar app VPN primero
    } catch (e: UnsupportedOperationException) {
        Log.e("VPN", "❌ App VPN no soporta Always-On")
        // La app debe declarar SERVICE_META_DATA_SUPPORTS_ALWAYS_ON
    }
}

// Desactivar VPN Always-On
fun desactivarVPNAlwaysOn() {
    dpm.setAlwaysOnVpnPackage(admin, null, false)
    Log.i("VPN", "VPN Always-On desactivada")
}
```

**Requisitos de la App VPN**:
```xml
<!-- En AndroidManifest.xml de la app VPN -->
<service android:name=".VpnService"
         android:permission="android.permission.BIND_VPN_SERVICE">
    <intent-filter>
        <action android:name="android.net.VpnService"/>
    </intent-filter>
    <meta-data 
        android:name="android.net.VpnService.SUPPORTS_ALWAYS_ON"
        android:value="true"/>
</service>
```

**Comportamiento del Modo Lockdown**:
```
1. Usuario enciende el dispositivo
2. Sistema inicia app VPN automáticamente
3. Mientras VPN no esté conectada:
   ❌ Sin acceso a internet
   ❌ Sin DNS
   ❌ Apps no pueden hacer requests
4. VPN se conecta
5. ✅ Tráfico fluye a través del túnel VPN
6. Si VPN se desconecta:
   → Volver al paso 3
```

---

### `setAlwaysOnVpnPackage(ComponentName admin, String vpnPackage, boolean lockdownEnabled, Set<String> lockdownWhitelist)`

**Propósito**: Versión extendida que permite especificar apps que pueden acceder a internet SIN pasar por la VPN, incluso en modo lockdown.

**Parámetro Adicional**:
- `lockdownWhitelist`: Set de package names que pueden bypassear la VPN

**Casos de Uso**:
- **Servicios del sistema**: Permitir actualizaciones del sistema sin VPN
- **Apps de emergencia**: Acceso a servicios críticos si VPN falla
- **Troubleshooting**: Apps de diagnóstico que necesitan acceso directo

**Ejemplo de Uso**:
```kotlin
fun configurarVPNConWhitelist() {
    val vpnPackage = "com.empresa.securevpn"
    
    // Apps que pueden bypassear VPN
    val whitelist = setOf(
        "com.android.vending",           // Play Store (actualizaciones)
        "com.google.android.gms",        // Play Services
        "com.empresa.emergency_access"   // App de emergencia
    )
    
    dpm.setAlwaysOnVpnPackage(admin, vpnPackage, true, whitelist)
    
    Log.i("VPN", "VPN Always-On con whitelist de ${whitelist.size} apps")
}
```

**⚠️ Consideraciones de Seguridad**:
- Cada app en whitelist es un posible punto de fuga de datos
- Solo incluir apps absolutamente necesarias
- Auditar whitelist regularmente
- Documentar justificación de cada app incluida

---

### `getAlwaysOnVpnPackage(ComponentName admin)`

**Propósito**: Obtiene el package name de la VPN configurada como Always-On.

**Retorna**:
- `String`: Package name de la VPN Always-On
- `null`: No hay VPN Always-On configurada

---

### `isAlwaysOnVpnLockdownEnabled(ComponentName admin)`

**Propósito**: Verifica si el modo lockdown está habilitado para la VPN Always-On.

**Retorna**:
- `true`: Modo lockdown activo (sin VPN = sin internet)
- `false`: Modo best-effort o VPN Always-On no configurada

---

### `getAlwaysOnVpnLockdownWhitelist(ComponentName admin)`

**Propósito**: Obtiene la lista de apps que pueden bypassear la VPN Always-On en modo lockdown.

**Retorna**: `Set<String>` con package names en whitelist.

```kotlin
fun auditarConfiguracionVPN(): String {
    val vpnPackage = dpm.getAlwaysOnVpnPackage(admin)
    
    return buildString {
        append("🔐 CONFIGURACIÓN VPN ALWAYS-ON\n")
        append("═══════════════════════════════════\n\n")
        
        if (vpnPackage == null) {
            append("❌ VPN Always-On: NO CONFIGURADA\n")
        } else {
            append("✅ VPN Always-On: ACTIVA\n")
            append("   Paquete: $vpnPackage\n")
            
            val lockdown = dpm.isAlwaysOnVpnLockdownEnabled(admin)
            append("   Modo: ${if (lockdown) "🔒 LOCKDOWN" else "⚡ BEST-EFFORT"}\n")
            
            if (lockdown) {
                val whitelist = dpm.getAlwaysOnVpnLockdownWhitelist(admin)
                if (whitelist.isEmpty()) {
                    append("   Whitelist: Ninguna app puede bypassear\n")
                } else {
                    append("   Whitelist: ${whitelist.size} app(s)\n")
                    whitelist.forEach { pkg ->
                        append("      - $pkg\n")
                    }
                }
            }
        }
    }
}
```

---

## 🌐 Métodos de Proxy Global

### `setRecommendedGlobalProxy(ComponentName admin, ProxyInfo proxyInfo)`

**Propósito**: Configura un proxy HTTP/HTTPS global "recomendado" para todo el tráfico de red del dispositivo. A diferencia de métodos legacy, este respeta configuraciones de apps individuales.

**Casos de Uso**:
- **Filtrado de contenido**: Todo tráfico pasa por proxy corporativo
- **Monitoreo de red**: Inspeccionar/registrar tráfico HTTP/HTTPS
- **Cache corporativo**: Optimizar ancho de banda con proxy cache
- **Cumplimiento**: Bloquear acceso a sitios no autorizados

**Tipos de ProxyInfo**:

**Proxy Manual**:
```kotlin
// Proxy HTTP/HTTPS estándar
val proxyInfo = ProxyInfo.buildDirectProxy(
    "proxy.empresa.com",  // Host
    8080,                 // Puerto
    listOf(               // Exclusiones (bypass)
        "localhost",
        "127.0.0.1",
        "*.empresa.com"   // Dominios internos
    )
)

dpm.setRecommendedGlobalProxy(admin, proxyInfo)
```

**Proxy PAC (Proxy Auto-Config)**:
```kotlin
// Archivo PAC para configuración dinámica
val proxyInfo = ProxyInfo.buildPacProxy(
    Uri.parse("http://proxy.empresa.com/proxy.pac")
)

dpm.setRecommendedGlobalProxy(admin, proxyInfo)
```

**Sin Proxy**:
```kotlin
// Remover proxy configurado
dpm.setRecommendedGlobalProxy(admin, null)
```

**Ejemplo de Archivo PAC**:
```javascript
// proxy.pac
function FindProxyForURL(url, host) {
    // Dominios internos - sin proxy
    if (shExpMatch(host, "*.empresa.com") ||
        shExpMatch(host, "intranet.*")) {
        return "DIRECT";
    }
    
    // Sitios bloqueados
    if (shExpMatch(host, "*.facebook.com") ||
        shExpMatch(host, "*.youtube.com")) {
        return "PROXY proxy-bloqueado.empresa.com:8080";
    }
    
    // Resto del tráfico por proxy normal
    return "PROXY proxy.empresa.com:8080; DIRECT";
}
```

**⚠️ Comportamiento "Recomendado"**:
- Apps pueden ignorar este proxy si tienen configuración propia
- No es un proxy "forzado" absoluto
- Para tráfico no HTTP/HTTPS (TCP directo), el proxy no aplica
- VPN Always-On tiene prioridad sobre proxy

---

### `getGlobalProxyAdmin()`

**Propósito**: Obtiene el ComponentName del Device Admin que configuró el proxy global.

**Retorna**:
- `ComponentName`: Admin que configuró el proxy
- `null`: No hay proxy global configurado

**Uso**: Verificar quién tiene control del proxy (útil en dispositivos con múltiples admins).

---

## 📡 Métodos de Gestión de APNs

Los APNs (Access Point Names) configuran cómo el dispositivo se conecta a la red de datos móviles del operador.

### `addOverrideApn(ComponentName admin, ApnSetting apnSetting)`

**Propósito**: Agrega una configuración APN personalizada que sobrescribe las configuraciones del operador.

**Casos de Uso**:
- **APN corporativo**: Usar APN privado para tráfico empresarial
- **IoT/M2M**: Dispositivos con SIM corporativo y APN dedicado
- **Optimización de costos**: Rutear tráfico por APN más económico
- **Separación de tráfico**: Diferentes APNs para diferentes tipos de datos

**Ejemplo de Uso**:
```kotlin
fun configurarAPNCorporativo() {
    // Construir configuración APN
    val apnSetting = ApnSetting.Builder()
        .setEntryName("Empresa Corporativo")
        .setApnName("empresa.apn")
        .setProxyAddress("proxy.empresa.com")
        .setProxyPort(8080)
        .setMmsc(Uri.parse("http://mms.empresa.com"))
        .setMmsProxyAddress("mmsproxy.empresa.com")
        .setMmsProxyPort(80)
        .setUser("usuario@empresa.com")
        .setPassword("password")
        .setAuthType(ApnSetting.AUTH_TYPE_PAP)
        .setApnTypeBitmask(
            ApnSetting.TYPE_DEFAULT or 
            ApnSetting.TYPE_SUPL or
            ApnSetting.TYPE_MMS
        )
        .setProtocol(ApnSetting.PROTOCOL_IPV4V6)
        .setRoamingProtocol(ApnSetting.PROTOCOL_IPV4V6)
        .setCarrierEnabled(true)
        .setNetworkTypeBitmask(
            TelephonyManager.NETWORK_TYPE_BITMASK_LTE or
            TelephonyManager.NETWORK_TYPE_BITMASK_NR
        )
        .build()
    
    // Agregar APN
    val apnId = dpm.addOverrideApn(admin, apnSetting)
    
    if (apnId != -1) {
        Log.i("APN", "✅ APN corporativo agregado con ID: $apnId")
        
        // Habilitar el APN
        dpm.setOverrideApnsEnabled(admin, true)
    } else {
        Log.e("APN", "❌ Error al agregar APN")
    }
}
```

**Retorna**:
- `int`: ID del APN agregado (>= 0)
- `-1`: Error al agregar

---

### `updateOverrideApn(ComponentName admin, int apnId, ApnSetting apnSetting)`

**Propósito**: Actualiza una configuración APN existente.

**Casos de Uso**:
- Cambiar credenciales del APN
- Actualizar direcciones de proxy
- Modificar tipos de red soportados

**Retorna**: `boolean` (true si se actualizó exitosamente)

---

### `removeOverrideApn(ComponentName admin, int apnId)`

**Propósito**: Remueve un APN personalizado previamente agregado.

**Retorna**: `boolean` (true si se removió exitosamente)

---

### `getOverrideApns(ComponentName admin)`

**Propósito**: Obtiene la lista de todos los APNs personalizados configurados.

**Retorna**: `List<ApnSetting>` con las configuraciones.

---

### `setOverrideApnsEnabled(ComponentName admin, boolean enabled)`

**Propósito**: Habilita o deshabilita el uso de APNs personalizados.

**Comportamiento**:
- `true`: Dispositivo usa APNs configurados por admin
- `false`: Dispositivo vuelve a usar APNs del operador

**Uso**: Activar/desactivar APNs corporativos sin eliminar la configuración.

---

### `isOverrideApnEnabled(ComponentName admin)`

**Propósito**: Verifica si los APNs personalizados están habilitados.

**Retorna**: `boolean`

```kotlin
fun auditarConfiguracionAPNs(): String {
    val apns = dpm.getOverrideApns(admin)
    val habilitado = dpm.isOverrideApnEnabled(admin)
    
    return buildString {
        append("📡 CONFIGURACIÓN APNs\n")
        append("═══════════════════════\n\n")
        
        append("Estado: ${if (habilitado) "✅ HABILITADO" else "❌ DESHABILITADO"}\n")
        append("APNs configurados: ${apns.size}\n\n")
        
        if (apns.isNotEmpty()) {
            apns.forEach { apn ->
                append("• ${apn.entryName}\n")
                append("  APN: ${apn.apnName}\n")
                append("  Proxy: ${apn.proxyAddress}:${apn.proxyPort}\n")
                append("  Tipos: ${apn.apnTypeBitmask}\n\n")
            }
        }
    }
}
```

---

## 🔒 Métodos de Private DNS

### `setGlobalPrivateDnsModeSpecifiedHost(ComponentName admin, String privateDnsHost)`

**Propósito**: Configura Private DNS (DNS over TLS) con un servidor específico para todo el dispositivo. Encripta consultas DNS para privacidad y seguridad.

**Casos de Uso**:
- **Privacidad**: Evitar que ISP vea consultas DNS
- **Seguridad**: Prevenir DNS spoofing/hijacking
- **Filtrado corporativo**: DNS corporativo con filtrado de contenido
- **Cumplimiento**: Requisitos de privacidad como GDPR

**Ejemplo de Uso**:
```kotlin
// Configurar DNS corporativo con TLS
dpm.setGlobalPrivateDnsModeSpecifiedHost(admin, "dns.empresa.com")

// Usar DNS públicos seguros
dpm.setGlobalPrivateDnsModeSpecifiedHost(admin, "dns.google")        // 8.8.8.8
dpm.setGlobalPrivateDnsModeSpecifiedHost(admin, "cloudflare-dns.com") // 1.1.1.1
dpm.setGlobalPrivateDnsModeSpecifiedHost(admin, "dns.quad9.net")      // 9.9.9.9

// Servidor DNS corporativo con filtrado
dpm.setGlobalPrivateDnsModeSpecifiedHost(admin, "filtered-dns.empresa.com")
```

**Requisitos del Servidor**:
- Debe soportar DNS over TLS (DoT) en puerto 853
- Debe tener certificado TLS válido
- El hostname debe coincidir con el certificado

---

### `setGlobalPrivateDnsModeOpportunistic(ComponentName admin)`

**Propósito**: Habilita Private DNS en modo "oportunista" - intenta usar DNS over TLS si está disponible, pero fallback a DNS normal si no.

**Casos de Uso**:
- Balance entre privacidad y compatibilidad
- Redes donde no todos los DNS soportan TLS
- Transición gradual a Private DNS

```kotlin
// Habilitar Private DNS oportunista
dpm.setGlobalPrivateDnsModeOpportunistic(admin)
```

---

### `getGlobalPrivateDnsMode(ComponentName admin)`

**Propósito**: Obtiene el modo actual de Private DNS configurado.

**Retorna**:
- `PRIVATE_DNS_MODE_OFF` (1): Private DNS deshabilitado
- `PRIVATE_DNS_MODE_OPPORTUNISTIC` (2): Modo oportunista
- `PRIVATE_DNS_MODE_PROVIDER_HOSTNAME` (3): Servidor específico configurado

---

### `getGlobalPrivateDnsHost(ComponentName admin)`

**Propósito**: Obtiene el hostname del servidor Private DNS configurado.

**Retorna**:
- `String`: Hostname del servidor (si está en modo PROVIDER_HOSTNAME)
- `null`: Si no hay servidor específico configurado

```kotlin
fun auditarPrivateDNS(): String {
    val mode = dpm.getGlobalPrivateDnsMode(admin)
    val host = dpm.getGlobalPrivateDnsHost(admin)
    
    return buildString {
        append("🔒 CONFIGURACIÓN PRIVATE DNS\n")
        append("═══════════════════════════════\n\n")
        
        when (mode) {
            DevicePolicyManager.PRIVATE_DNS_MODE_OFF -> {
                append("❌ Private DNS: DESHABILITADO\n")
                append("   ⚠️ Consultas DNS sin encriptar\n")
            }
            DevicePolicyManager.PRIVATE_DNS_MODE_OPPORTUNISTIC -> {
                append("⚡ Private DNS: OPORTUNISTA\n")
                append("   Usa TLS si está disponible\n")
            }
            DevicePolicyManager.PRIVATE_DNS_MODE_PROVIDER_HOSTNAME -> {
                append("✅ Private DNS: SERVIDOR ESPECÍFICO\n")
                append("   Servidor: $host\n")
                append("   Todas las consultas DNS por TLS\n")
            }
        }
    }
}
```

---

## 📶 Métodos de Políticas WiFi

### `setWifiSsidPolicy(ComponentName admin, WifiSsidPolicy policy)`

**Propósito**: Configura una política de SSID WiFi que controla a qué redes puede conectarse el dispositivo. Permite crear whitelist o blacklist de redes.

**Tipos de Política**:

**WIFI_SSID_POLICY_TYPE_ALLOWLIST** (Whitelist):
```kotlin
// Solo permitir redes corporativas específicas
val ssidsPermitidos = setOf(
    WifiSsid.fromBytes("Empresa-Corp".toByteArray()),
    WifiSsid.fromBytes("Empresa-Guest".toByteArray()),
    WifiSsid.fromBytes("Empresa-Secure".toByteArray())
)

val policy = WifiSsidPolicy(
    WifiSsidPolicy.WIFI_SSID_POLICY_TYPE_ALLOWLIST,
    ssidsPermitidos
)

dpm.setWifiSsidPolicy(admin, policy)
// Dispositivo solo puede conectarse a estas redes
```

**WIFI_SSID_POLICY_TYPE_DENYLIST** (Blacklist):
```kotlin
// Bloquear redes públicas conocidas/inseguras
val ssidsBloqueados = setOf(
    WifiSsid.fromBytes("Free-WiFi".toByteArray()),
    WifiSsid.fromBytes("Public-Hotspot".toByteArray()),
    WifiSsid.fromBytes("Airport-WiFi".toByteArray())
)

val policy = WifiSsidPolicy(
    WifiSsidPolicy.WIFI_SSID_POLICY_TYPE_DENYLIST,
    ssidsBloqueados
)

dpm.setWifiSsidPolicy(admin, policy)
// Dispositivo puede conectarse a cualquier red EXCEPTO estas
```

**Casos de Uso**:
- **Whitelist**: Dispositivos que solo deben usar WiFi corporativo
- **Blacklist**: Prevenir conexión a redes públicas inseguras
- **Cumplimiento**: Forzar uso de redes aprobadas
- **Seguridad**: Evitar hotspots maliciosos (evil twin attacks)

---

### `getWifiSsidPolicy(ComponentName admin)`

**Propósito**: Obtiene la política de SSID WiFi actualmente configurada.

**Retorna**: `WifiSsidPolicy` con tipo y lista de SSIDs.

---

## 🛡️ Métodos de Seguridad de Red Mínima

### `setMinimumRequiredWifiSecurityLevel(ComponentName admin, int level)`

**Propósito**: Establece el nivel mínimo de seguridad que debe tener una red WiFi para que el dispositivo pueda conectarse.

**Niveles de Seguridad**:

| Nivel | Constante | Descripción | Protocolos |
|-------|-----------|-------------|------------|
| 0 | `WIFI_SECURITY_OPEN` | Sin seguridad | Open networks |
| 1 | `WIFI_SECURITY_PERSONAL` | Seguridad básica | WPA2-PSK, WPA3-SAE |
| 2 | `WIFI_SECURITY_ENTERPRISE_EAP` | Seguridad empresarial | 802.1X EAP |
| 3 | `WIFI_SECURITY_ENTERPRISE_192` | Máxima seguridad | WPA3-Enterprise 192-bit |

**Casos de Uso**:

**Corporativo Estándar**:
```kotlin
// Permitir solo WPA2/WPA3 personal o superior
dpm.setMinimumRequiredWifiSecurityLevel(
    admin,
    DevicePolicyManager.WIFI_SECURITY_PERSONAL
)
// Bloquea: Redes abiertas
// Permite: WPA2/WPA3-PSK, 802.1X
```

**Alta Seguridad**:
```kotlin
// Solo WiFi Enterprise con 802.1X
dpm.setMinimumRequiredWifiSecurityLevel(
    admin,
    DevicePolicyManager.WIFI_SECURITY_ENTERPRISE_EAP
)
// Bloquea: Redes abiertas, WPA2-PSK
// Permite: Solo 802.1X, WPA3-Enterprise
```

**Gobierno/Defensa**:
```kotlin
// Solo WPA3-Enterprise con 192-bit
dpm.setMinimumRequiredWifiSecurityLevel(
    admin,
    DevicePolicyManager.WIFI_SECURITY_ENTERPRISE_192
)
// Bloquea: Todo excepto WPA3-192
// Permite: Solo máxima seguridad
```

---

### `getMinimumRequiredWifiSecurityLevel()`

**Propósito**: Obtiene el nivel mínimo de seguridad WiFi configurado.

**Retorna**: `int` con el nivel de seguridad.

---

## 🎯 Casos de Uso por Escenario

### 🏢 Corporativo Estándar
```kotlin
// VPN Always-On con whitelist
dpm.setAlwaysOnVpnPackage(admin, "com.empresa.vpn", true, setOf(
    "com.android.vending"  // Play Store solo
))

// Proxy corporativo con PAC
val proxyPac = ProxyInfo.buildPacProxy(
    Uri.parse("http://proxy.empresa.com/proxy.pac")
)
dpm.setRecommendedGlobalProxy(admin, proxyPac)

// Private DNS corporativo
dpm.setGlobalPrivateDnsModeSpecifiedHost(admin, "dns.empresa.com")

// Solo WiFi corporativo permitido
val ssidsPermitidos = setOf(
    WifiSsid.fromBytes("Empresa-Corp".toByteArray())
)
dpm.setWifiSsidPolicy(admin, WifiSsidPolicy(
    WifiSsidPolicy.WIFI_SSID_POLICY_TYPE_ALLOWLIST,
    ssidsPermitidos
))

// Seguridad WiFi mínima
dpm.setMinimumRequiredWifiSecurityLevel(
    admin,
    DevicePolicyManager.WIFI_SECURITY_PERSONAL
)
```

---

### 🏦 Financiero/Alta Seguridad
```kotlin
// VPN Always-On estricta sin whitelist
dpm.setAlwaysOnVpnPackage(admin, "com.empresa.securevpn", true)

// Sin proxy (tráfico directo a VPN)
dpm.setRecommendedGlobalProxy(admin, null)

// Private DNS con validación estricta
dpm.setGlobalPrivateDnsModeSpecifiedHost(admin, "secure-dns.empresa.com")

// Solo WiFi Enterprise 802.1X
dpm.setWifiSsidPolicy(admin, WifiSsidPolicy(
    WifiSsidPolicy.WIFI_SSID_POLICY_TYPE_ALLOWLIST,
    setOf(WifiSsid.fromBytes("Empresa-802.1X".toByteArray()))
))

dpm.setMinimumRequiredWifiSecurityLevel(
    admin,
    DevicePolicyManager.WIFI_SECURITY_ENTERPRISE_EAP
)
```

---

### 📱 Dispositivo IoT/M2M
```kotlin
// APN corporativo dedicado
val apnIoT = ApnSetting.Builder()
    .setEntryName("IoT Corporate")
    .setApnName("iot.empresa.apn")
    .setApnTypeBitmask(ApnSetting.TYPE_DEFAULT)
    .setProtocol(ApnSetting.PROTOCOL_IPV4V6)
    .build()

val apnId = dpm.addOverrideApn(admin, apnIoT)
dpm.setOverrideApnsEnabled(admin, true)

// Sin VPN (tráfico directo)
// Sin proxy
// DNS corporativo
dpm.setGlobalPrivateDnsModeSpecifiedHost(admin, "iot-dns.empresa.com")
```

---

## ⚠️ Consideraciones Importantes

### VPN Always-On

**Ventajas**:
- Máxima seguridad - todo tráfico encriptado
- Previene fugas de datos
- Cumplimiento normativo

**Desventajas**:
- Depende de conectividad VPN
- Puede afectar rendimiento
- Problemas de VPN = sin internet
- Consumo adicional de batería

### Proxy Global

**Limitaciones**:
- Solo HTTP/HTTPS
- Apps pueden ignorarlo
- No afecta tráfico UDP/TCP directo
- VPN tiene prioridad

### Private DNS

**Requisitos**:
- Android 9+ (API 28)
- Servidor debe soportar DoT
- Certificado TLS válido
- Puerto 853 abierto

### APNs

**Consideraciones**:
- Específico del operador
- Requiere SIM corporativo
- Configuración compleja
- Puede afectar MMS

---

## 📚 Próxima Categoría

**8. Seguridad y Cifrado** (~20 métodos)

Esta categoría cubrirá:
- Cifrado de almacenamiento
- Timeouts de autenticación fuerte
- Security logging
- Network logging
- Bug reports
- Backup service

---

*Documentación generada para DevicePolicyManager API de Android Enterprise - Categoría 7 de 22*