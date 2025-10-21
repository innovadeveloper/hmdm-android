# Documentación DevicePolicyManager - Categoría 3: Restricciones de Usuario

## 📋 Visión General

La categoría de **Restricciones de Usuario** agrupa aproximadamente **15 métodos** que permiten controlar funcionalidades del dispositivo mediante restricciones definidas en `UserManager`. Estas restricciones son la forma más granular de controlar qué puede y qué no puede hacer un usuario en el dispositivo.

## 🎯 Propósito

Estos métodos permiten:
- **Agregar/eliminar** restricciones de usuario específicas
- **Consultar** restricciones activas en el dispositivo
- **Controlar** funcionalidades del sistema (WiFi, Bluetooth, cámara, instalación de apps, etc.)
- **Gestionar** perfiles de trabajo y separación de datos
- **Configurar** filtros de intents entre perfiles
- **Administrar** widgets entre perfiles

---

## 🔐 Métodos Principales de Restricciones

### `addUserRestriction(ComponentName admin, String key)`

**Propósito**: Agrega una restricción específica al usuario, deshabilitando o limitando una funcionalidad del dispositivo.

**Casos de Uso**:
- **Seguridad corporativa**: Bloquear instalación de apps no autorizadas
- **Kioscos**: Deshabilitar configuraciones que el usuario no debe modificar
- **Control parental**: Limitar funcionalidades en dispositivos infantiles
- **Cumplimiento normativo**: Aplicar políticas requeridas por regulaciones
- **Dispositivos de flota**: Mantener configuración consistente y controlada

**Ejemplo de Restricciones Comunes**:
```kotlin
// Bloquear instalación de apps de fuentes desconocidas
dpm.addUserRestriction(admin, UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES)

// Deshabilitar factory reset
dpm.addUserRestriction(admin, UserManager.DISALLOW_FACTORY_RESET)

// Bloquear debugging
dpm.addUserRestriction(admin, UserManager.DISALLOW_DEBUGGING_FEATURES)

// Deshabilitar configuración de WiFi
dpm.addUserRestriction(admin, UserManager.DISALLOW_CONFIG_WIFI)

// Prevenir modificación de cuentas
dpm.addUserRestriction(admin, UserManager.DISALLOW_MODIFY_ACCOUNTS)
```

**Restricciones Críticas para Seguridad**:
```kotlin
// Configuración de seguridad estándar corporativa
dpm.addUserRestriction(admin, UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES)
dpm.addUserRestriction(admin, UserManager.DISALLOW_FACTORY_RESET)
dpm.addUserRestriction(admin, UserManager.DISALLOW_DEBUGGING_FEATURES)
dpm.addUserRestriction(admin, UserManager.DISALLOW_USB_FILE_TRANSFER)
dpm.addUserRestriction(admin, UserManager.DISALLOW_SAFE_BOOT)
```

**⚠️ Advertencia Importante**:
Algunas restricciones son **permanentes** hasta el factory reset o remoción como Device Owner. Por ejemplo, `DISALLOW_FACTORY_RESET` solo se puede eliminar con ADB o desde código antes de aplicarla.

---

### `clearUserRestriction(ComponentName admin, String key)`

**Propósito**: Elimina una restricción previamente establecida, restaurando la funcionalidad al usuario.

**Casos de Uso**:
- **Cambio de políticas**: Adaptar restricciones a nuevos requisitos corporativos
- **Fin de kiosko mode**: Restaurar dispositivo a modo normal
- **Testing**: Remover restricciones de prueba
- **Devolución de dispositivo**: Preparar dispositivo para nuevo usuario
- **Actualización de permisos**: Otorgar funcionalidades temporalmente restringidas

**Ejemplo de Remoción de Restricciones**:
```kotlin
// Permitir nuevamente instalación de apps
dpm.clearUserRestriction(admin, UserManager.DISALLOW_INSTALL_APPS)

// Habilitar configuración de WiFi
dpm.clearUserRestriction(admin, UserManager.DISALLOW_CONFIG_WIFI)

// Restaurar acceso a debugging (para desarrollo)
dpm.clearUserRestriction(admin, UserManager.DISALLOW_DEBUGGING_FEATURES)
```

**Flujo de Transición de Políticas**:
```kotlin
// Cambiar de modo kiosko a modo corporativo estándar
fun transicionKioskoACorporativo(dpm: DevicePolicyManager, admin: ComponentName) {
    // Remover restricciones de kiosko
    dpm.clearUserRestriction(admin, UserManager.DISALLOW_INSTALL_APPS)
    dpm.clearUserRestriction(admin, UserManager.DISALLOW_ADJUST_VOLUME)
    dpm.clearUserRestriction(admin, UserManager.DISALLOW_CREATE_WINDOWS)
    
    // Mantener restricciones corporativas
    // DISALLOW_FACTORY_RESET permanece
    // DISALLOW_DEBUGGING_FEATURES permanece
}
```

**Nota sobre DISALLOW_FACTORY_RESET**:
Esta restricción es especial y difícil de remover una vez aplicada. Solo se puede eliminar:
1. Con comando ADB (requiere debugging habilitado)
2. Factory reset físico del dispositivo
3. Programáticamente ANTES de que el usuario intente acceder a Settings

---

### `getUserRestrictions(ComponentName admin)`

**Propósito**: Obtiene un `Bundle` con todas las restricciones actualmente aplicadas al usuario.

**Casos de Uso**:
- **Auditoría de configuración**: Verificar qué restricciones están activas
- **Reportes de cumplimiento**: Documentar estado de políticas
- **Validación post-configuración**: Confirmar que restricciones se aplicaron
- **Debugging**: Investigar problemas de funcionalidad bloqueada
- **Sincronización con MDM**: Mantener consistencia con servidor

**Retorna**: `Bundle` donde las keys son las restricciones y los valores son `true` si están activas.

**Ejemplo de Verificación Completa**:
```kotlin
fun auditarRestricciones(dpm: DevicePolicyManager, admin: ComponentName): ReporteRestricciones {
    val restricciones = dpm.getUserRestrictions(admin)
    
    // Verificar restricciones críticas
    val factoryResetBloqueado = restricciones.getBoolean(UserManager.DISALLOW_FACTORY_RESET, false)
    val debuggingBloqueado = restricciones.getBoolean(UserManager.DISALLOW_DEBUGGING_FEATURES, false)
    val instalacionBloqueada = restricciones.getBoolean(UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES, false)
    val safeBootBloqueado = restricciones.getBoolean(UserManager.DISALLOW_SAFE_BOOT, false)
    
    // Verificar restricciones de conectividad
    val wifiBloqueado = restricciones.getBoolean(UserManager.DISALLOW_CONFIG_WIFI, false)
    val bluetoothBloqueado = restricciones.getBoolean(UserManager.DISALLOW_BLUETOOTH, false)
    val usbTransferBloqueado = restricciones.getBoolean(UserManager.DISALLOW_USB_FILE_TRANSFER, false)
    
    // Verificar restricciones de configuración
    val modificarCuentas = restricciones.getBoolean(UserManager.DISALLOW_MODIFY_ACCOUNTS, false)
    val agregarUsuarios = restricciones.getBoolean(UserManager.DISALLOW_ADD_USER, false)
    
    // Contar total de restricciones activas
    val totalRestricciones = restricciones.keySet().count { restricciones.getBoolean(it, false) }
    
    return ReporteRestricciones(
        totalActivas = totalRestricciones,
        seguridadCritica = factoryResetBloqueado && debuggingBloqueado && safeBootBloqueado,
        conectividadControlada = wifiBloqueado || bluetoothBloqueado,
        instalacionBloqueada = instalacionBloqueada,
        restriccionesCompletas = restricciones
    )
}

data class ReporteRestricciones(
    val totalActivas: Int,
    val seguridadCritica: Boolean,
    val conectividadControlada: Boolean,
    val instalacionBloqueada: Boolean,
    val restriccionesCompletas: Bundle
)
```

**Generar Reporte Legible**:
```kotlin
fun generarReporteTexto(bundle: Bundle): String {
    val sb = StringBuilder("📊 RESTRICCIONES ACTIVAS\n")
    sb.append("═══════════════════════════\n\n")
    
    val restriccionesActivas = bundle.keySet()
        .filter { bundle.getBoolean(it, false) }
        .sorted()
    
    if (restriccionesActivas.isEmpty()) {
        sb.append("✅ No hay restricciones aplicadas\n")
    } else {
        sb.append("Total: ${restriccionesActivas.size} restricciones\n\n")
        restriccionesActivas.forEach { key ->
            sb.append("🔒 $key\n")
            sb.append("   ${obtenerDescripcionRestriccion(key)}\n\n")
        }
    }
    
    return sb.toString()
}

fun obtenerDescripcionRestriccion(key: String): String = when(key) {
    UserManager.DISALLOW_FACTORY_RESET -> "Usuario no puede hacer factory reset"
    UserManager.DISALLOW_DEBUGGING_FEATURES -> "Debugging y ADB deshabilitados"
    UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES -> "Solo apps de Play Store"
    UserManager.DISALLOW_CONFIG_WIFI -> "Configuración WiFi bloqueada"
    UserManager.DISALLOW_MODIFY_ACCOUNTS -> "No puede agregar/eliminar cuentas"
    UserManager.DISALLOW_SAFE_BOOT -> "Modo seguro deshabilitado"
    UserManager.DISALLOW_USB_FILE_TRANSFER -> "Transferencia USB bloqueada"
    else -> "Restricción aplicada"
}
```

---

## 🌐 Métodos de Cross-Profile (Perfil de Trabajo)

### `addCrossProfileIntentFilter(ComponentName admin, IntentFilter filter, int flags)`

**Propósito**: Permite que ciertos intents se compartan entre el perfil personal y el perfil de trabajo, creando una experiencia integrada pero segura.

**Concepto de Cross-Profile**:
En dispositivos con Work Profile, existen dos "mundos" separados:
- **Perfil Personal**: Apps y datos personales del usuario
- **Perfil de Trabajo**: Apps y datos corporativos

Por defecto, estos perfiles están completamente aislados. Este método permite comunicación controlada entre ellos.

**Casos de Uso**:

1. **Abrir URLs corporativas en navegador de trabajo**:
```kotlin
// URLs de dominios corporativos se abren en navegador de trabajo
val filter = IntentFilter().apply {
    addAction(Intent.ACTION_VIEW)
    addCategory(Intent.CATEGORY_DEFAULT)
    addCategory(Intent.CATEGORY_BROWSABLE)
    addDataScheme("https")
    addDataAuthority("intranet.empresa.com", null)
    addDataAuthority("docs.empresa.com", null)
}

dpm.addCrossProfileIntentFilter(
    admin, 
    filter, 
    DevicePolicyManager.FLAG_MANAGED_CAN_ACCESS_PARENT
)
```

2. **Compartir archivos desde personal a trabajo**:
```kotlin
// Permitir compartir documentos hacia el perfil de trabajo
val filter = IntentFilter().apply {
    addAction(Intent.ACTION_SEND)
    addCategory(Intent.CATEGORY_DEFAULT)
    addDataType("application/pdf")
    addDataType("image/*")
}

dpm.addCrossProfileIntentFilter(
    admin,
    filter,
    DevicePolicyManager.FLAG_PARENT_CAN_ACCESS_MANAGED
)
```

3. **Email corporativo maneja enlaces mailto:**:
```kotlin
// Enlaces mailto: se abren en cliente de email corporativo
val filter = IntentFilter().apply {
    addAction(Intent.ACTION_VIEW)
    addCategory(Intent.CATEGORY_DEFAULT)
    addDataScheme("mailto")
}

dpm.addCrossProfileIntentFilter(
    admin,
    filter,
    DevicePolicyManager.FLAG_MANAGED_CAN_ACCESS_PARENT
)
```

**Flags Disponibles**:

| Flag | Dirección | Descripción |
|------|-----------|-------------|
| `FLAG_MANAGED_CAN_ACCESS_PARENT` | Trabajo → Personal | Perfil de trabajo puede enviar intents al personal |
| `FLAG_PARENT_CAN_ACCESS_MANAGED` | Personal → Trabajo | Perfil personal puede enviar intents al trabajo |

**⚠️ Consideraciones de Seguridad**:
- Solo permitir intents necesarios para productividad
- Evitar filtros demasiado amplios que expongan datos
- Documentar cada filtro y su justificación
- Auditar regularmente qué intents están permitidos
- Considerar DLP (Data Loss Prevention) al permitir compartir

---

### `clearCrossProfileIntentFilters(ComponentName admin)`

**Propósito**: Elimina TODOS los filtros de intents cross-profile previamente configurados.

**Casos de Uso**:
- **Cambio de políticas**: Reconfigurar desde cero los filtros
- **Máxima seguridad**: Eliminar toda comunicación entre perfiles
- **Incident response**: Aislar perfiles ante incidente de seguridad
- **Testing**: Limpiar configuración de pruebas
- **Fin de Work Profile**: Preparar para eliminación del perfil

**Ejemplo de Aislamiento Completo**:
```kotlin
// Ante incidente de seguridad, aislar completamente los perfiles
fun aislarPerfiles(dpm: DevicePolicyManager, admin: ComponentName) {
    // Eliminar todos los filtros de intents
    dpm.clearCrossProfileIntentFilters(admin)
    
    // Deshabilitar compartir contactos
    dpm.setCrossProfileCallerIdDisabled(admin, true)
    dpm.setCrossProfileContactsSearchDisabled(admin, true)
    
    // Deshabilitar widgets cross-profile
    dpm.getCrossProfileWidgetProviders(admin).forEach { provider ->
        dpm.removeCrossProfileWidgetProvider(admin, provider)
    }
    
    Log.w("Security", "Perfiles aislados completamente - modo seguridad máxima")
}
```

---

### `setCrossProfileCallerIdDisabled(ComponentName admin, boolean disabled)`

**Propósito**: Controla si la información del llamante (Caller ID) se muestra en llamadas entre perfiles personal y de trabajo.

**Casos de Uso**:

**Habilitar Caller ID (disabled = false)**:
- Mejorar experiencia de usuario: saber quién llama
- Permitir identificar llamadas de colegas vs. personales
- Facilitar productividad al reconocer contactos

**Deshabilitar Caller ID (disabled = true)**:
- **Privacidad máxima**: No revelar información de contactos personales en perfil de trabajo
- **Cumplimiento GDPR**: Evitar procesamiento no autorizado de datos personales
- **Seguridad**: Prevenir que apps de trabajo accedan a lista de contactos personal
- **Separación estricta**: Mantener perfiles completamente independientes

```kotlin
// Configuración de privacidad alta
dpm.setCrossProfileCallerIdDisabled(admin, true) // No mostrar información del llamante

// Configuración de productividad
dpm.setCrossProfileCallerIdDisabled(admin, false) // Mostrar quién llama
```

**Escenario de Uso**:
```
SIN Caller ID (disabled = true):
  Llamada entrante → Solo muestra número "555-1234"
  
CON Caller ID (disabled = false):
  Llamada entrante → Muestra "Juan Pérez (Trabajo)" con foto
```

---

### `getCrossProfileCallerIdDisabled(ComponentName admin)`

**Propósito**: Consulta el estado actual de la configuración de Caller ID entre perfiles.

**Retorna**:
- `true`: Caller ID está deshabilitado (privacidad máxima)
- `false`: Caller ID está habilitado (información visible)

---

### `setCrossProfileContactsSearchDisabled(ComponentName admin, boolean disabled)`

**Propósito**: Controla si se puede buscar contactos del otro perfil desde el perfil actual.

**Casos de Uso**:

**Habilitar búsqueda (disabled = false)**:
- Productividad: buscar contactos de trabajo desde perfil personal
- Integración: experiencia unificada de contactos
- Conveniencia: evitar duplicar contactos en ambos perfiles

**Deshabilitar búsqueda (disabled = true)**:
- **DLP (Data Loss Prevention)**: Prevenir fuga de contactos corporativos
- **Privacidad**: Mantener contactos personales privados del perfil de trabajo
- **Cumplimiento**: Separación estricta requerida por políticas
- **Seguridad**: Evitar que apps maliciosas accedan a contactos del otro perfil

```kotlin
// Alta seguridad - Separación estricta
dpm.setCrossProfileContactsSearchDisabled(admin, true)

// Productividad - Búsqueda unificada
dpm.setCrossProfileContactsSearchDisabled(admin, false)
```

**Impacto en Experiencia de Usuario**:
```
CON búsqueda habilitada:
  Usuario busca "María" → Encuentra: María (Personal) + María González (Trabajo)
  
SIN búsqueda:
  Usuario busca "María" → Solo encuentra contactos del perfil actual
```

---

### `getCrossProfileContactsSearchDisabled(ComponentName admin)`

**Propósito**: Consulta si la búsqueda de contactos entre perfiles está deshabilitada.

**Retorna**:
- `true`: Búsqueda deshabilitada (perfiles aislados)
- `false`: Búsqueda habilitada (búsqueda unificada)

---

## 🎨 Métodos de Widgets Cross-Profile

### `addCrossProfileWidgetProvider(ComponentName admin, String packageName)`

**Propósito**: Permite que un widget del perfil de trabajo aparezca en el launcher del perfil personal (o viceversa).

**Casos de Uso**:
- **Dashboard corporativo**: Widget con métricas de trabajo en pantalla personal
- **Calendario unificado**: Ver eventos de trabajo en perfil personal
- **Notificaciones importantes**: Alertas corporativas visibles siempre
- **Productividad**: Acceso rápido a apps de trabajo sin cambiar perfil

**Ejemplo**:
```kotlin
// Permitir widget de calendario corporativo en perfil personal
dpm.addCrossProfileWidgetProvider(admin, "com.empresa.calendario")

// Permitir widget de email corporativo
dpm.addCrossProfileWidgetProvider(admin, "com.microsoft.office.outlook")

// Permitir widget de chat corporativo
dpm.addCrossProfileWidgetProvider(admin, "com.slack")
```

**Restricciones**:
- Solo funciona con widgets específicamente diseñados para cross-profile
- Requiere que el paquete esté instalado en el perfil de trabajo
- Usuario final puede elegir si agregar o no el widget
- El widget se ejecuta en contexto del perfil de trabajo (datos seguros)

**⚠️ Consideración de Seguridad**:
Los widgets pueden mostrar información sensible. Solo permitir widgets de apps de confianza que manejen datos corporativos apropiadamente.

---

### `removeCrossProfileWidgetProvider(ComponentName admin, String packageName)`

**Propósito**: Revoca el permiso de un widget para aparecer en el otro perfil.

**Casos de Uso**:
- **Cambio de políticas**: App ya no autorizada para widgets cross-profile
- **Incidente de seguridad**: Revocar acceso inmediatamente
- **Desinstalación de app**: Limpiar permisos al remover aplicación
- **Auditoría**: Remover widgets innecesarios o no usados

```kotlin
// Revocar permiso de widget de calendario
dpm.removeCrossProfileWidgetProvider(admin, "com.empresa.calendario")
```

**Efecto**: El widget desaparece del launcher del otro perfil y no se puede agregar nuevamente hasta que se otorgue permiso otra vez.

---

### `getCrossProfileWidgetProviders(ComponentName admin)`

**Propósito**: Obtiene la lista de paquetes autorizados para mostrar widgets cross-profile.

**Retorna**: `List<String>` con los package names autorizados.

**Casos de Uso**:
- Auditar qué widgets están permitidos
- Generar reportes de configuración
- Validar políticas de seguridad
- Sincronizar con sistema MDM

```kotlin
// Auditoría de widgets autorizados
val widgetsAutorizados = dpm.getCrossProfileWidgetProviders(admin)

if (widgetsAutorizados.isEmpty()) {
    Log.i("Widgets", "No hay widgets cross-profile autorizados")
} else {
    Log.i("Widgets", "Widgets autorizados: ${widgetsAutorizados.joinToString(", ")}")
}

// Verificar si un widget específico está autorizado
val calendarioAutorizado = widgetsAutorizados.contains("com.empresa.calendario")
```

---

## 🎯 Casos de Uso por Escenario

### 🏢 Corporativo Estándar - Balance Seguridad/Productividad
```kotlin
// Restricciones básicas de seguridad
dpm.addUserRestriction(admin, UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES)
dpm.addUserRestriction(admin, UserManager.DISALLOW_FACTORY_RESET)
dpm.addUserRestriction(admin, UserManager.DISALLOW_DEBUGGING_FEATURES)
dpm.addUserRestriction(admin, UserManager.DISALLOW_USB_FILE_TRANSFER)

// Cross-profile moderado para productividad
dpm.setCrossProfileCallerIdDisabled(admin, false) // Mostrar quién llama
dpm.setCrossProfileContactsSearchDisabled(admin, false) // Búsqueda unificada

// Permitir URLs corporativas en navegador de trabajo
val filterWeb = IntentFilter().apply {
    addAction(Intent.ACTION_VIEW)
    addCategory(Intent.CATEGORY_BROWSABLE)
    addDataScheme("https")
    addDataAuthority("intranet.empresa.com", null)
}
dpm.addCrossProfileIntentFilter(admin, filterWeb, 
    DevicePolicyManager.FLAG_MANAGED_CAN_ACCESS_PARENT)

// Widgets corporativos permitidos
dpm.addCrossProfileWidgetProvider(admin, "com.empresa.calendario")
dpm.addCrossProfileWidgetProvider(admin, "com.microsoft.office.outlook")
```

---

### 🏥 Médico/HIPAA - Máxima Privacidad y Separación
```kotlin
// Restricciones de seguridad médica
dpm.addUserRestriction(admin, UserManager.DISALLOW_DEBUGGING_FEATURES)
dpm.addUserRestriction(admin, UserManager.DISALLOW_USB_FILE_TRANSFER)
dpm.addUserRestriction(admin, UserManager.DISALLOW_MOUNT_PHYSICAL_MEDIA)
dpm.addUserRestriction(admin, UserManager.DISALLOW_PRINTING)
dpm.addUserRestriction(admin, UserManager.DISALLOW_BLUETOOTH_SHARING)
dpm.addUserRestriction(admin, UserManager.DISALLOW_CAMERA) // En áreas sensibles

// Aislamiento TOTAL entre perfiles (HIPAA compliance)
dpm.setCrossProfileCallerIdDisabled(admin, true) // Sin información de llamante
dpm.setCrossProfileContactsSearchDisabled(admin, true) // Sin búsqueda cross-profile
dpm.clearCrossProfileIntentFilters(admin) // Sin compartir datos

// Sin widgets cross-profile (datos médicos no deben mezclarse)
// No agregar ningún widget provider
```

---

### 🏦 Financiero/Bancario - Control Estricto
```kotlin
// Restricciones financieras
dpm.addUserRestriction(admin, UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES_GLOBALLY)
dpm.addUserRestriction(admin, UserManager.DISALLOW_DEBUGGING_FEATURES)
dpm.addUserRestriction(admin, UserManager.DISALLOW_USB_FILE_TRANSFER)
dpm.addUserRestriction(admin, UserManager.DISALLOW_BLUETOOTH_SHARING)
dpm.addUserRestriction(admin, UserManager.DISALLOW_CONFIG_VPN) // Solo VPN corporativa
dpm.addUserRestriction(admin, UserManager.DISALLOW_NETWORK_RESET)

// Separación estricta de perfiles
dpm.setCrossProfileCallerIdDisabled(admin, true)
dpm.setCrossProfileContactsSearchDisabled(admin, true)

// Solo permitir intents críticos para negocio
val filterDocumentos = IntentFilter().apply {
    addAction(Intent.ACTION_VIEW)
    addDataType("application/pdf") // Solo PDFs hacia apps bancarias
}
dpm.addCrossProfileIntentFilter(admin, filterDocumentos,
    DevicePolicyManager.FLAG_PARENT_CAN_ACCESS_MANAGED)

// Sin widgets (información financiera sensible)
```

---

### 🏭 Kiosko/Punto de Venta - Restricciones Máximas
```kotlin
// Kiosko ultra-restrictivo
dpm.addUserRestriction(admin, UserManager.DISALLOW_FACTORY_RESET)
dpm.addUserRestriction(admin, UserManager.DISALLOW_SAFE_BOOT)
dpm.addUserRestriction(admin, UserManager.DISALLOW_ADD_USER)
dpm.addUserRestriction(admin, UserManager.DISALLOW_INSTALL_APPS)
dpm.addUserRestriction(admin, UserManager.DISALLOW_UNINSTALL_APPS)
dpm.addUserRestriction(admin, UserManager.DISALLOW_CONFIG_WIFI)
dpm.addUserRestriction(admin, UserManager.DISALLOW_BLUETOOTH)
dpm.addUserRestriction(admin, UserManager.DISALLOW_ADJUST_VOLUME)
dpm.addUserRestriction(admin, UserManager.DISALLOW_CREATE_WINDOWS)
dpm.addUserRestriction(admin, UserManager.DISALLOW_SYSTEM_ERROR_DIALOGS)
dpm.addUserRestriction(admin, UserManager.DISALLOW_MODIFY_ACCOUNTS)
dpm.addUserRestriction(admin, UserManager.DISALLOW_CONFIG_CREDENTIALS)

// Sin cross-profile (dispositivo de perfil único)
// No aplicable en modo kiosko puro
```

---

### 🎮 Dispositivo Infantil - Control Parental
```kotlin
// Control parental
dpm.addUserRestriction(admin, UserManager.DISALLOW_MODIFY_ACCOUNTS)
dpm.addUserRestriction(admin, UserManager.DISALLOW_INSTALL_APPS)
dpm.addUserRestriction(admin, UserManager.DISALLOW_UNINSTALL_APPS)
dpm.addUserRestriction(admin, UserManager.DISALLOW_OUTGOING_CALLS)
dpm.addUserRestriction(admin, UserManager.DISALLOW_SMS)
dpm.addUserRestriction(admin, UserManager.DISALLOW_SHARE_LOCATION)
dpm.addUserRestriction(admin, UserManager.DISALLOW_FACTORY_RESET)
dpm.addUserRestriction(admin, UserManager.DISALLOW_CONFIG_WIFI) // Padres controlan WiFi
dpm.addUserRestriction(admin, UserManager.DISALLOW_CONFIG_DATE_TIME)

// Sin cross-profile (dispositivo de perfil único infantil)
```

---

### 🚗 Dispositivo de Flota/Vehículo - Operacional
```kotlin
// Restricciones de flota
dpm.addUserRestriction(admin, UserManager.DISALLOW_ADD_USER)
dpm.addUserRestriction(admin, UserManager.DISALLOW_FACTORY_RESET)
dpm.addUserRestriction(admin, UserManager.DISALLOW_CONFIG_WIFI) // WiFi pre-configurado
dpm.addUserRestriction(admin, UserManager.DISALLOW_BLUETOOTH) // O pre-emparejado
dpm.addUserRestriction(admin, UserManager.DISALLOW_INSTALL_APPS)
dpm.addUserRestriction(admin, UserManager.DISALLOW_DEBUGGING_FEATURES)

// Permitir solo apps críticas para operación
// Cross-profile no aplicable (perfil único de trabajo)
```

---

### 🔐 Gobierno/Alta Seguridad - Máximo Control
```kotlin
// Seguridad gubernamental
dpm.addUserRestriction(admin, UserManager.DISALLOW_DEBUGGING_FEATURES)
dpm.addUserRestriction(admin, UserManager.DISALLOW_OEM_UNLOCK)
dpm.addUserRestriction(admin, UserManager.DISALLOW_SAFE_BOOT)
dpm.addUserRestriction(admin, UserManager.DISALLOW_FACTORY_RESET)
dpm.addUserRestriction(admin, UserManager.DISALLOW_USB_FILE_TRANSFER)
dpm.addUserRestriction(admin, UserManager.DISALLOW_MOUNT_PHYSICAL_MEDIA)
dpm.addUserRestriction(admin, UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES_GLOBALLY)
dpm.addUserRestriction(admin, UserManager.DISALLOW_CONFIG_CREDENTIALS)
dpm.addUserRestriction(admin, UserManager.DISALLOW_BLUETOOTH_SHARING)
dpm.addUserRestriction(admin, UserManager.DISALLOW_PRINTING)
dpm.addUserRestriction(admin, UserManager.DISALLOW_CONFIG_VPN)
dpm.addUserRestriction(admin, UserManager.DISALLOW_CONFIG_PRIVATE_DNS)
dpm.addUserRestriction(admin, UserManager.DISALLOW_AUTOFILL)
dpm.addUserRestriction(admin, UserManager.DISALLOW_CONTENT_CAPTURE)

// Aislamiento total entre perfiles
dpm.setCrossProfileCallerIdDisabled(admin, true)
dpm.setCrossProfileContactsSearchDisabled(admin, true)
dpm.clearCrossProfileIntentFilters(admin)

// Sin widgets cross-profile
```

---

