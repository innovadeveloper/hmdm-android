# Documentación DevicePolicyManager - Categoría 6: Gestión de Certificados

## 📋 Visión General

La categoría de **Gestión de Certificados** agrupa aproximadamente **15 métodos** que permiten instalar, gestionar y controlar certificados de seguridad en dispositivos administrados. Estos certificados son fundamentales para establecer conexiones seguras, autenticar usuarios y validar la identidad de servidores.

## 🎯 Propósito

Estos métodos permiten:
- **Instalar certificados CA** (Certificate Authority) de confianza
- **Remover certificados** instalados
- **Gestionar pares de claves** (clave privada + certificado)
- **Generar pares de claves** directamente en el dispositivo
- **Delegar capacidades** de gestión de certificados
- **Configurar selección** de certificados por aplicación

---

## 🔐 Conceptos Fundamentales

### Tipos de Certificados

**Certificados CA (Certificate Authority)**:
- Certificados raíz que permiten validar otros certificados
- Se instalan en el almacén de confianza del sistema
- Todas las apps pueden usarlos para validar conexiones SSL/TLS
- Ejemplo: Certificado CA corporativo para intranet

**Certificados de Cliente (Key Pairs)**:
- Par de clave privada + certificado público
- Se usan para autenticación del cliente
- Pueden ser específicos por aplicación
- Ejemplo: Certificado para autenticación VPN o WiFi Enterprise

### Almacenes de Certificados

| Almacén | Ubicación | Acceso | Uso |
|---------|-----------|--------|-----|
| **Sistema** | Partición /system | Solo lectura (excepto Device Owner) | CAs pre-instaladas por fabricante |
| **Usuario** | Almacén de usuario | Lectura/escritura por Device Owner | CAs instaladas por admin |
| **WiFi/VPN** | Almacén de credenciales | Gestión específica | Autenticación de redes |

---

## 📥 Métodos de Instalación de Certificados CA

### `installCaCert(ComponentName admin, byte[] certBuffer)`

**Propósito**: Instala un certificado de Certificate Authority (CA) en el almacén de confianza del dispositivo. Este certificado permite validar conexiones SSL/TLS a servidores que usen certificados firmados por esta CA.

**Casos de Uso**:
- **Intranet corporativa**: Instalar CA corporativa para acceder a servidores internos
- **Proxy HTTPS**: Instalar certificado del proxy corporativo para inspección SSL
- **Servidores de desarrollo**: Instalar CAs de staging/testing
- **APIs privadas**: Validar conexiones a APIs internas

**Formato del Certificado**:
- Debe ser formato X.509 en codificación DER (binario) o PEM (Base64)
- El `certBuffer` es un `byte[]` con los datos del certificado
- Puede contener un solo certificado o una cadena de certificados

**Ejemplo de Uso**:
```kotlin
fun instalarCertificadoCorporativo() {
    try {
        // Opción 1: Cargar desde assets
        val certBytes = context.assets.open("empresa_ca.crt").readBytes()
        
        // Opción 2: Descargar desde servidor
        // val certBytes = descargarCertificado("https://pki.empresa.com/ca.crt")
        
        // Instalar el certificado
        val instalado = dpm.installCaCert(admin, certBytes)
        
        if (instalado) {
            Log.i("Certificados", "✅ CA corporativa instalada correctamente")
            
            // Verificar que se instaló
            if (dpm.hasCaCertInstalled(admin, certBytes)) {
                Log.i("Certificados", "✅ Verificación exitosa")
            }
        } else {
            Log.e("Certificados", "❌ Fallo al instalar CA")
        }
        
    } catch (e: SecurityException) {
        Log.e("Certificados", "❌ Sin permisos: ${e.message}")
    } catch (e: Exception) {
        Log.e("Certificados", "❌ Error: ${e.message}")
    }
}

// Función auxiliar para convertir PEM a DER si es necesario
fun pemToDer(pemString: String): ByteArray {
    val pemContent = pemString
        .replace("-----BEGIN CERTIFICATE-----", "")
        .replace("-----END CERTIFICATE-----", "")
        .replace("\n", "")
        .replace("\r", "")
    
    return Base64.decode(pemContent, Base64.DEFAULT)
}
```

**Retorna**:
- `true`: Certificado instalado exitosamente
- `false`: Falló la instalación (certificado inválido, ya existe, etc.)

**⚠️ Importante**:
- El certificado queda visible para TODAS las aplicaciones del dispositivo
- Aparece en Settings > Security > Trusted Credentials (User tab)
- Usuario puede ver pero no puede eliminar (solo admin puede)
- Válido para conexiones HTTPS en navegadores y apps

---

### `uninstallCaCert(ComponentName admin, byte[] certBuffer)`

**Propósito**: Remueve un certificado CA previamente instalado del almacén de confianza.

**Casos de Uso**:
- **Rotación de certificados**: Remover CA antigua antes de instalar nueva
- **Fin de validez**: Eliminar certificados expirados o comprometidos
- **Cambio de políticas**: Remover acceso a recursos que ya no deben ser accesibles
- **Limpieza**: Eliminar certificados de prueba

**Comportamiento**:
- Solo puede remover certificados que fueron instalados por el mismo Device Owner
- No puede remover certificados del sistema pre-instalados
- Si el certificado no existe, el método no hace nada (no falla)

---

### `getInstalledCaCerts(ComponentName admin)`

**Propósito**: Obtiene una lista de todos los certificados CA instalados por el Device Owner.

**Retorna**: `List<byte[]>` donde cada elemento es un certificado en formato DER.

**Casos de Uso**:
- Auditoría de certificados instalados
- Verificar que las políticas se aplicaron
- Sincronizar estado con servidor MDM
- Generar reportes de cumplimiento

```kotlin
fun auditarCertificadosCA(): String {
    val certificados = dpm.getInstalledCaCerts(admin)
    
    return buildString {
        append("🔐 CERTIFICADOS CA INSTALADOS\n")
        append("═══════════════════════════════\n\n")
        
        if (certificados.isEmpty()) {
            append("❌ No hay certificados CA instalados\n")
        } else {
            append("Total: ${certificados.size} certificado(s)\n\n")
            
            certificados.forEachIndexed { index, certBytes ->
                try {
                    val cert = parseCertificado(certBytes)
                    append("${index + 1}. ${cert.subjectDN}\n")
                    append("   Emisor: ${cert.issuerDN}\n")
                    append("   Válido hasta: ${cert.notAfter}\n")
                    append("   Huella SHA-256: ${calcularHuella(certBytes)}\n\n")
                } catch (e: Exception) {
                    append("${index + 1}. ⚠️ Error al parsear certificado\n\n")
                }
            }
        }
    }
}

fun parseCertificado(certBytes: ByteArray): X509Certificate {
    val factory = CertificateFactory.getInstance("X.509")
    return factory.generateCertificate(ByteArrayInputStream(certBytes)) as X509Certificate
}

fun calcularHuella(certBytes: ByteArray): String {
    val digest = MessageDigest.getInstance("SHA-256")
    val hash = digest.digest(certBytes)
    return hash.joinToString(":") { "%02x".format(it) }
}
```

---

### `hasCaCertInstalled(ComponentName admin, byte[] certBuffer)`

**Propósito**: Verifica si un certificado CA específico está instalado.

**Retorna**:
- `true`: El certificado está instalado
- `false`: El certificado no está instalado

**Casos de Uso**:
- Verificar instalación antes de intentar usar
- Validar estado de configuración
- Debugging de problemas de conexión SSL

---

### `uninstallAllUserCaCerts(ComponentName admin)`

**Propósito**: Remueve TODOS los certificados CA instalados por usuarios o Device Owners (no los del sistema).

**Casos de Uso**:
- **Limpieza masiva**: Resetear configuración de certificados
- **Incident response**: Eliminar todos los certificados ante compromiso de seguridad
- **Reconfiguración**: Limpiar antes de aplicar nueva configuración
- **Preparación para devolución**: Limpiar dispositivo antes de reasignar

**⚠️ ADVERTENCIA**:
- Acción destructiva que afecta todos los certificados de usuario
- Puede romper conexiones SSL/TLS a servicios corporativos
- No se puede deshacer
- Usar con precaución

---

## 🔑 Métodos de Gestión de Pares de Claves

### `installKeyPair(ComponentName admin, PrivateKey privKey, Certificate cert, String alias)`

**Propósito**: Instala un par de clave privada + certificado público en el almacén de credenciales del dispositivo. Se usa para autenticación de cliente en conexiones WiFi Enterprise, VPN, o aplicaciones que requieren certificados de cliente.

**Parámetros**:
- `privKey`: Clave privada (PrivateKey)
- `cert`: Certificado público (Certificate) o cadena de certificados
- `alias`: Nombre único para identificar este par de claves

**Casos de Uso**:
- **WiFi Enterprise (802.1X)**: Certificados para autenticación EAP-TLS
- **VPN corporativa**: Certificados de cliente para IPSec o OpenVPN
- **Autenticación mutua TLS**: Apps que requieren mTLS
- **Firma digital**: Certificados para firmar documentos

**Formato**:
- Clave privada: Típicamente RSA o EC
- Certificado: X.509 en formato DER
- Alias: String único (ej: "vpn-client-cert", "wifi-auth")

**Ejemplo de Uso - Instalar Certificado de Cliente**:
```kotlin
fun instalarCertificadoCliente() {
    try {
        // 1. Cargar el certificado y clave privada
        val certBytes = context.assets.open("client_cert.crt").readBytes()
        val keyBytes = context.assets.open("client_key.pk8").readBytes()
        
        // 2. Parsear certificado
        val certFactory = CertificateFactory.getInstance("X.509")
        val certificate = certFactory.generateCertificate(
            ByteArrayInputStream(certBytes)
        ) as X509Certificate
        
        // 3. Parsear clave privada (formato PKCS#8)
        val keySpec = PKCS8EncodedKeySpec(keyBytes)
        val keyFactory = KeyFactory.getInstance("RSA")
        val privateKey = keyFactory.generatePrivate(keySpec)
        
        // 4. Instalar el par de claves
        val alias = "empresa-vpn-client"
        val instalado = dpm.installKeyPair(admin, privateKey, certificate, alias)
        
        if (instalado) {
            Log.i("Certificados", "✅ Certificado de cliente instalado: $alias")
        } else {
            Log.e("Certificados", "❌ Fallo al instalar certificado de cliente")
        }
        
    } catch (e: Exception) {
        Log.e("Certificados", "❌ Error al instalar certificado: ${e.message}", e)
    }
}
```

**Retorna**:
- `true`: Par de claves instalado exitosamente
- `false`: Falló la instalación

---

### `installKeyPair(ComponentName admin, PrivateKey privKey, Certificate[] certs, String alias, int flags)`

**Propósito**: Versión extendida que permite instalar una **cadena de certificados** (certificado + intermedios + raíz) y especificar flags adicionales.

**Flags Disponibles**:
- `INSTALLKEY_REQUEST_CREDENTIALS_ACCESS`: Permitir que apps soliciten acceso a esta clave
- `INSTALLKEY_SET_USER_SELECTABLE`: Usuario puede seleccionar esta clave en diálogos del sistema

**Casos de Uso**:
- Cadenas de certificados completas con intermedios
- Certificados que apps específicas deben poder usar
- Configuraciones avanzadas de acceso a claves

---

### `removeKeyPair(ComponentName admin, String alias)`

**Propósito**: Remueve un par de claves previamente instalado.

**Casos de Uso**:
- Rotación de certificados de cliente
- Revocar acceso tras fin de contrato
- Limpieza de certificados expirados
- Cambio de políticas de seguridad

**Retorna**:
- `true`: Par de claves removido exitosamente
- `false`: Alias no existe o error al remover

---

### `generateKeyPair(ComponentName admin, String algorithm, KeyGenParameterSpec keySpec, int idAttestationFlags)`

**Propósito**: Genera un par de claves (pública/privada) directamente en el dispositivo usando el hardware de seguridad (TEE/StrongBox si está disponible).

**Ventajas de Generación Local**:
- La clave privada NUNCA sale del dispositivo
- Más seguro que importar claves generadas externamente
- Puede usar hardware security module (HSM)
- Soporta key attestation (prueba criptográfica de dónde se generó)

**Algoritmos Soportados**:
- `"RSA"`: RSA con tamaños 2048, 3072, 4096 bits
- `"EC"`: Curvas elípticas (P-256, P-384, P-521)

**Casos de Uso**:
- Generar certificados para WiFi/VPN sin exponer claves
- PKI corporativo donde claves nunca deben exportarse
- Máxima seguridad para autenticación de dispositivos
- Key attestation para verificar integridad del dispositivo

**Retorna**: `AttestedKeyPair` que contiene:
- `keyPair`: El par de claves generado
- `attestationRecord`: Certificados de attestation (prueba de generación en hardware)

---

## 🔧 Métodos de Delegación y Configuración

### `setDelegatedScopes(ComponentName admin, String delegatePackage, List<String> scopes)`

**Propósito**: Permite delegar la capacidad de gestionar certificados a una aplicación que NO es Device Admin.

**Scope Relevante**: `DELEGATION_CERT_INSTALL`

**Casos de Uso**:
- App especializada en gestión de PKI
- Self-service: App corporativa instala sus propios certificados
- Arquitectura modular: Separar gestión de certificados

**Cuando delegar**:
```kotlin
// Permitir que app de seguridad gestione certificados
dpm.setDelegatedScopes(
    admin,
    "com.empresa.security_manager",
    listOf(DevicePolicyManager.DELEGATION_CERT_INSTALL)
)

// Ahora "com.empresa.security_manager" puede:
// - installCaCert()
// - uninstallCaCert()
// - installKeyPair()
// - removeKeyPair()
// Sin ser Device Admin
```

---

### `setKeyPairCertificate(ComponentName admin, String alias, List<Certificate> certs, boolean isUserSelectable)`

**Propósito**: Actualiza la cadena de certificados de un par de claves existente sin cambiar la clave privada.

**Casos de Uso**:
- **Renovación de certificados**: Actualizar certificado expirado manteniendo la misma clave
- **Actualizar cadena**: Agregar o cambiar certificados intermedios
- **Cambio de políticas**: Modificar si usuario puede seleccionar el certificado

**Ventajas**:
- No necesita regenerar la clave privada
- Útil para renovación automática de certificados
- Mantiene compatibilidad con configuraciones existentes (WiFi, VPN)

**Retorna**: `boolean`
- `true`: Certificados actualizados exitosamente
- `false`: Alias no existe o error

---

## 🎯 Casos de Uso por Escenario

### 🏢 Corporativo Estándar - Intranet con HTTPS
```kotlin
// Instalar CA corporativa para acceder a intranet
val caCert = cargarCertificadoDesdeAssets("empresa_root_ca.crt")
dpm.installCaCert(admin, caCert)

// Ahora los navegadores y apps pueden acceder a:
// - https://intranet.empresa.com
// - https://crm.empresa.com
// - https://docs.empresa.com
// Todos firmados por la CA corporativa
```

---

### 🔐 WiFi Enterprise con Certificados
```kotlin
// 1. Instalar CA del servidor RADIUS
val radiusCa = cargarCertificado("radius_ca.crt")
dpm.installCaCert(admin, radiusCa)

// 2. Instalar certificado de cliente para autenticación
val clientKey = cargarClavePrivada("client.key")
val clientCert = cargarCertificado("client.crt")
dpm.installKeyPair(admin, clientKey, clientCert, "wifi-empresa")

// 3. Configurar perfil WiFi Enterprise (EAP-TLS)
val wifiConfig = WifiEnterpriseConfig().apply {
    eapMethod = WifiEnterpriseConfig.Eap.TLS
    clientCertificateAlias = "wifi-empresa"
    caCertificateAlias = null // Usa el CA instalado del sistema
}
```

---

### 🌐 VPN Corporativa con mTLS
```kotlin
// Instalar certificado de cliente para VPN
val vpnKey = cargarClavePrivada("vpn_client.key")
val vpnCertChain = cargarCadenaCertificados("vpn_client_chain.crt")

dpm.installKeyPair(
    admin,
    vpnKey,
    vpnCertChain,
    "vpn-corporativa",
    DevicePolicyManager.INSTALLKEY_REQUEST_CREDENTIALS_ACCESS
)

// Configurar VPN que usa el certificado
// (código de configuración VPN específico del proveedor)
```

---

## ⚠️ Consideraciones Importantes

### Seguridad

1. **Protección de claves privadas**:
    - NUNCA almacenar claves privadas en código fuente
    - NUNCA transmitir claves privadas sin cifrado
    - Preferir generación local con `generateKeyPair()`
    - Usar hardware security cuando esté disponible

2. **Validación de certificados**:
    - Verificar firma antes de instalar
    - Validar fechas de validez
    - Comprobar que el certificado es de una CA confiable
    - Verificar revocación (CRL/OCSP) si es crítico

3. **Rotación de certificados**:
    - Implementar proceso de renovación antes de expiración
    - Mantener periodo de solapamiento durante rotación
    - Auditar certificados expirados regularmente

### Formatos de Certificados

**PEM (Privacy Enhanced Mail)**:
```
-----BEGIN CERTIFICATE-----
MIIDXTCCAkWgAwIBAgIJAKL0UG+mRKSzMA0GCSqGSIb3DQEBCwUAMEUxCzAJBgNV
...
-----END CERTIFICATE-----
```
- Formato Base64 con headers
- Legible en texto plano
- Común en Linux/Apache

**DER (Distinguished Encoding Rules)**:
- Formato binario
- Más compacto
- Común en Java/Android
- Convertir: `openssl x509 -in cert.pem -outform DER -out cert.der`

**PKCS#12 (.p12, .pfx)**:
- Contenedor que incluye clave privada + certificado
- Protegido con contraseña
- Necesita parsing especial en Android

### Mejores Prácticas

1. **Alias descriptivos**: Usar nombres claros que indiquen propósito
```kotlin
"wifi-empresa-802-1x"
"vpn-ssl-client-2024"
"intranet-ca-root"
```

2. **Auditoría regular**: Verificar certificados instalados mensualmente

3. **Limpieza proactiva**: Remover certificados expirados o no usados

4. **Testing exhaustivo**: Probar renovación en ambiente de desarrollo

5. **Documentación**: Mantener registro de qué certificados están instalados y por qué

### Troubleshooting Común

**"Certificate already exists"**:
- Usar alias único o remover certificado existente primero

**"Invalid certificate format"**:
- Verificar que el formato es DER o PEM válido
- Convertir con OpenSSL si es necesario

**"SSL handshake failed" después de instalar CA**:
- Verificar que el certificado del servidor está firmado por la CA instalada
- Comprobar cadena de certificados completa
- Validar fechas de validez

**Apps no confían en CA instalada**:
- Algunas apps usan su propio almacén de confianza
- Network Security Config puede anular certificados del sistema
- Verificar que la app no tiene certificate pinning

---

## 📚 Próxima Categoría

**7. Configuración de Red** (~30 métodos)

Esta categoría cubrirá:
- VPN Always-On
- Configuración de proxy global
- Gestión de APNs
- Private DNS
- WiFi SSID policies
- Seguridad de red mínima

---

*Documentación generada para DevicePolicyManager API de Android Enterprise - Categoría 6 de 22*