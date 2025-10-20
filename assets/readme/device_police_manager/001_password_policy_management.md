# Documentación DevicePolicyManager - Categoría 1: Gestión de Políticas de Contraseña

## 📋 Visión General

La gestión de políticas de contraseña es uno de los pilares fundamentales de seguridad en Android Enterprise. Esta categoría agrupa aproximadamente **25 métodos** que permiten a un Device Owner o Profile Owner establecer, consultar y hacer cumplir requisitos estrictos sobre las contraseñas del dispositivo.

## 🎯 Propósito

Estos métodos permiten:
- Definir la **calidad mínima** de contraseñas (numérica, alfanumérica, biométrica, etc.)
- Establecer requisitos de **complejidad** (longitud, mayúsculas, números, símbolos)
- Implementar **políticas de expiración** y historial de contraseñas
- Controlar **bloqueos automáticos** tras intentos fallidos
- Gestionar **tokens de reseteo** de contraseña de forma segura
- Forzar **autenticación fuerte** periódica

---

## 🔐 Métodos de Configuración de Requisitos

### `setPasswordQuality(ComponentName admin, int quality)`

**Propósito**: Define el nivel mínimo de seguridad que debe cumplir la contraseña del dispositivo.

**Casos de Uso**:
- **Corporativo estándar**: Exigir contraseñas alfanuméricas con símbolos
- **Kiosco básico**: Permitir solo PIN numérico simple
- **Alta seguridad**: Requerir contraseñas complejas con combinación de caracteres
- **Dispositivos compartidos**: Forzar al menos un patrón o PIN

**Valores Comunes**:
- `PASSWORD_QUALITY_UNSPECIFIED` (0): Sin restricciones
- `PASSWORD_QUALITY_BIOMETRIC_WEAK` (0x8000): Huella/face débil
- `PASSWORD_QUALITY_SOMETHING` (0x10000): Patrón, PIN o contraseña
- `PASSWORD_QUALITY_NUMERIC` (0x20000): PIN numérico
- `PASSWORD_QUALITY_NUMERIC_COMPLEX` (0x30000): PIN sin secuencias repetitivas
- `PASSWORD_QUALITY_ALPHABETIC` (0x40000): Contraseña alfabética
- `PASSWORD_QUALITY_ALPHANUMERIC` (0x50000): Letras + números
- `PASSWORD_QUALITY_COMPLEX` (0x60000): Letras, números y símbolos

---

### `setPasswordMinimumLength(ComponentName admin, int length)`

**Propósito**: Establece la longitud mínima que debe tener la contraseña.

**Casos de Uso**:
- **Dispositivos corporativos**: Mínimo 8 caracteres (estándar empresarial)
- **Alta seguridad**: 12-16 caracteres para datos sensibles
- **Kioscos de bajo riesgo**: 4-6 dígitos suficientes
- **Cumplimiento normativo**: Cumplir con NIST, ISO27001, PCI-DSS

**Recomendaciones**:
- **Mínimo aceptable**: 6 caracteres
- **Corporativo estándar**: 8 caracteres
- **Alta seguridad**: 12+ caracteres
- Combinar con `setPasswordQuality()` para mayor efectividad

---

### `setPasswordMinimumLetters(ComponentName admin, int length)`
### `setPasswordMinimumLowerCase(ComponentName admin, int length)`
### `setPasswordMinimumUpperCase(ComponentName admin, int length)`
### `setPasswordMinimumNumeric(ComponentName admin, int length)`
### `setPasswordMinimumSymbols(ComponentName admin, int length)`

**Propósito**: Definen requisitos granulares sobre la composición de caracteres en la contraseña.

**Casos de Uso**:
- **Cumplimiento bancario**: Requerir al menos 1 mayúscula, 1 minúscula, 1 número, 1 símbolo
- **Políticas corporativas**: Seguir estándares de seguridad específicos de la industria
- **Prevención de contraseñas débiles**: Forzar diversidad de caracteres
- **Regulaciones específicas**: Cumplir con HIPAA, SOX, GDPR

**Ejemplo de Política Fuerte**:
- Mínimo 10 caracteres totales
- Al menos 2 letras mayúsculas
- Al menos 2 letras minúsculas
- Al menos 2 números
- Al menos 1 símbolo

---

### `setPasswordMinimumNonLetter(ComponentName admin, int length)`

**Propósito**: Requiere un número mínimo de caracteres que NO sean letras (números y símbolos).

**Casos de Uso**:
- Prevenir contraseñas puramente alfabéticas como "password"
- Forzar diversidad con números y símbolos
- Complementar políticas de complejidad sin especificar exactamente cuántos números vs símbolos

---

### `setPasswordHistoryLength(ComponentName admin, int length)`

**Propósito**: Impide que el usuario reutilice las últimas N contraseñas.

**Casos de Uso**:
- **Estándar corporativo**: Recordar últimas 5-12 contraseñas
- **Alta seguridad**: Historial de 24 contraseñas
- **Prevenir rotación cíclica**: Evitar que usuarios alternen entre 2-3 contraseñas
- **Cumplimiento**: Muchas normativas requieren historial de al menos 5

**Valores Comunes**:
- `0`: Sin historial (no recomendado)
- `5-12`: Estándar empresarial
- `24`: Alta seguridad

---

### `setPasswordExpirationTimeout(ComponentName admin, long timeout)`

**Propósito**: Fuerza al usuario a cambiar su contraseña después de un período de tiempo específico.

**Casos de Uso**:
- **Rotación periódica**: Cambiar contraseña cada 90 días (estándar)
- **Cumplimiento normativo**: PCI-DSS requiere cambio cada 90 días
- **Alta seguridad**: Cambio mensual (30 días)
- **Dispositivos compartidos**: Rotación más frecuente

**Valores Comunes**:
- `0`: Sin expiración (no recomendado para seguridad)
- `30 días`: 2592000000 milisegundos (alta seguridad)
- `90 días`: 7776000000 milisegundos (estándar corporativo)
- `180 días`: 15552000000 milisegundos (menos restrictivo)

**Nota**: Debe establecerse en milisegundos desde el momento actual.

---

### `setMaximumFailedPasswordsForWipe(ComponentName admin, int num)`

**Propósito**: Borra completamente el dispositivo tras N intentos fallidos de contraseña.

**Casos de Uso**:
- **Dispositivos con datos sensibles**: 5-10 intentos antes de borrado
- **Alta seguridad gubernamental**: 3 intentos (extremo)
- **Prevención de robo**: Proteger datos corporativos si el dispositivo es robado
- **Cumplimiento de seguridad**: Políticas que requieren borrado automático

**Valores Comunes**:
- `0`: Sin borrado automático (valor por defecto)
- `3`: Muy restrictivo, solo para alta seguridad
- `5-10`: Balance entre seguridad y usabilidad
- `15-20`: Menos restrictivo

**⚠️ ADVERTENCIA**: Este método es **destructivo e irreversible**. El borrado incluye todos los datos del usuario y configuraciones. Usar con extrema precaución.

---

### `setMaximumTimeToLock(ComponentName admin, long timeMs)`

**Propósito**: Establece el tiempo máximo de inactividad antes de que el dispositivo se bloquee automáticamente.

**Casos de Uso**:
- **Alta seguridad**: Bloqueo tras 30 segundos de inactividad
- **Corporativo estándar**: 1-5 minutos
- **Dispositivos médicos**: 30 segundos (HIPAA compliance)
- **Balance usabilidad/seguridad**: 2-3 minutos

**Valores Comunes**:
- `0`: Sin bloqueo automático (no recomendado)
- `30000`: 30 segundos (muy restrictivo)
- `60000`: 1 minuto (restrictivo)
- `300000`: 5 minutos (estándar)
- `600000`: 10 minutos (permisivo)

---

### `setRequiredPasswordComplexity(int complexity)`

**Propósito**: API moderna (Android 11+) que simplifica la configuración de políticas de contraseña mediante niveles predefinidos de complejidad, reemplazando la necesidad de configurar múltiples métodos individuales.

**Casos de Uso**:
- **Simplificación de políticas**: Un único método en lugar de múltiples setters
- **Cumplimiento rápido**: Aplicar estándares de seguridad sin configuración granular
- **Experiencia de usuario**: Permitir que el usuario elija entre varios tipos de autenticación que cumplan el nivel de complejidad
- **Gestión multi-plataforma**: Políticas consistentes a través de diferentes versiones de Android

**Valores (Constantes)**:
- `PASSWORD_COMPLEXITY_NONE` (0): Sin requisitos
- `PASSWORD_COMPLEXITY_LOW` (0x10000): PIN de 4 dígitos o patrón
- `PASSWORD_COMPLEXITY_MEDIUM` (0x30000): PIN de 4+ dígitos sin repeticiones, o contraseña de 4+ caracteres
- `PASSWORD_COMPLEXITY_HIGH` (0x50000): PIN de 8+ dígitos sin repeticiones, o contraseña de 6+ caracteres

**Ventaja**: Este método es más flexible y amigable con el usuario que los métodos legacy, ya que permite diferentes tipos de autenticación que cumplan el nivel de complejidad.

---

## 📊 Métodos de Consulta de Estado

### `getPasswordQuality(ComponentName admin)`

**Propósito**: Obtiene la calidad mínima de contraseña configurada actualmente.

**Casos de Uso**:
- Verificar que las políticas se aplicaron correctamente
- Auditoría de configuración de seguridad
- Reportes de cumplimiento
- Debugging de problemas de configuración

**Retorna**: Un entero representando la calidad (`PASSWORD_QUALITY_*`)

---

### `getPasswordMinimumLength(ComponentName admin)`

**Propósito**: Obtiene la longitud mínima de contraseña configurada.

**Casos de Uso**:
- Validar configuración antes de mostrar UI al usuario
- Generar reportes de auditoría
- Sincronizar políticas con sistemas externos
- Verificar que cambios se aplicaron

---

### `isActivePasswordSufficient()`

**Propósito**: Verifica si la contraseña **actual** del dispositivo cumple con todos los requisitos establecidos por las políticas.

**Casos de Uso**:
- **Validación post-configuración**: Verificar que el usuario configuró una contraseña válida
- **Onboarding de dispositivos**: Comprobar cumplimiento antes de dar acceso
- **Auditoría continua**: Verificar periódicamente el estado de cumplimiento
- **Flujos condicionales**: Permitir/denegar acceso basado en la validez de la contraseña

**Retorna**: `true` si la contraseña actual cumple todas las políticas, `false` en caso contrario.

**Ejemplo de Flujo**:
```
1. Administrador establece políticas de contraseña
2. Usuario intenta configurar nueva contraseña
3. Sistema valida contra políticas
4. App llama isActivePasswordSufficient()
5. Si false → mostrar mensaje de error y requisitos
```

---

### `isActivePasswordSufficientForDeviceRequirement()`

**Propósito**: Similar a `isActivePasswordSufficient()` pero considera **todos** los requisitos del dispositivo, incluyendo los establecidos por el sistema además de los del admin.

**Casos de Uso**:
- Dispositivos con políticas de seguridad del fabricante (Samsung Knox, etc.)
- Verificación más estricta que incluye requisitos de hardware
- Cumplimiento con políticas de seguridad en capas

---

### `getCurrentFailedPasswordAttempts()`

**Propósito**: Retorna el número de intentos fallidos de contraseña desde el último desbloqueo exitoso.

**Casos de Uso**:
- **Monitoreo de seguridad**: Detectar posibles intentos de acceso no autorizado
- **Alertas proactivas**: Notificar al administrador antes de que se alcance el límite de borrado
- **Análisis forense**: Investigar incidentes de seguridad
- **UI personalizada**: Mostrar advertencias al usuario sobre intentos restantes

**Ejemplo**:
```
Intentos actuales: 4
Límite configurado: 10
Mensaje: "Quedan 6 intentos antes del borrado del dispositivo"
```

---

### `getPasswordExpiration(ComponentName admin)`

**Propósito**: Retorna la fecha/hora (en milisegundos) cuando expirará la contraseña actual.

**Casos de Uso**:
- **Notificaciones proactivas**: Avisar al usuario 7-14 días antes de la expiración
- **Gestión de cumplimiento**: Reportar estado de expiración a sistemas MDM
- **Auditoría**: Verificar que las políticas de rotación se están cumpliendo
- **UX mejorada**: Mostrar cuenta regresiva hasta expiración

**Retorna**: `0` si no hay expiración configurada, o timestamp en milisegundos.

---

### `getPasswordComplexity()`

**Propósito**: Obtiene el nivel de complejidad **actual** de la contraseña del dispositivo (no el requerido, sino el real).

**Casos de Uso**:
- Verificar si la contraseña actual excede los requisitos mínimos
- Reportes de auditoría sobre la fortaleza real de contraseñas
- Comparar complejidad requerida vs. real
- Análisis de seguridad del estado actual

**Retorna**: `PASSWORD_COMPLEXITY_NONE`, `LOW`, `MEDIUM`, o `HIGH`

---

### `getRequiredPasswordComplexity()`

**Propósito**: Obtiene el nivel de complejidad **requerido** por las políticas (lo que configuraste con `setRequiredPasswordComplexity()`).

**Casos de Uso**:
- Verificar configuración actual de políticas
- Sincronizar estado con sistemas externos
- Validar que las políticas se aplicaron correctamente
- Generar documentación de cumplimiento

---

## 🔄 Métodos de Acción y Gestión

### `resetPassword(String password, int flags)` ⚠️ **DEPRECATED desde API 26**

**Propósito**: Permite al administrador cambiar la contraseña del dispositivo directamente de forma programática.

**Estado**: **Deprecated** desde Android 8.0 (API 26). Reemplazado por `resetPasswordWithToken()`.

**Razón de Deprecación**: Considerado un riesgo de seguridad que el administrador pueda cambiar contraseñas sin el consentimiento del usuario.

**Casos de Uso Históricos** (solo en Android 7.1 y anteriores):
- Resetear contraseña de dispositivos bloqueados
- Provisioning inicial de dispositivos
- Recuperación de acceso a dispositivos perdidos

**⚠️ NO USAR EN NUEVAS IMPLEMENTACIONES**

---

### `resetPasswordWithToken(ComponentName admin, String password, byte[] token, int flags)`

**Propósito**: API moderna y segura para resetear contraseñas usando un token de autorización pre-establecido.

**Casos de Uso**:
- **Recuperación segura**: Resetear contraseña de usuario que olvidó su PIN/contraseña
- **Soporte remoto**: Help desk puede restablecer acceso sin conocer la contraseña anterior
- **Provisioning desasistido**: Configurar dispositivos nuevos con contraseña inicial
- **Escenarios de pérdida de dispositivo**: Cambiar contraseña remotamente antes de borrado

**Flujo de Trabajo**:
```
1. Admin establece token con setResetPasswordToken()
2. Token se activa con credenciales del usuario
3. Cuando necesitas resetear, llamas resetPasswordWithToken() con el token
4. Sistema verifica token y cambia contraseña
```

**Ventajas sobre el método deprecated**:
- Requiere configuración previa (token)
- Más seguro: no permite cambios arbitrarios
- El usuario debe haber activado el token con sus credenciales primero

---

### `setResetPasswordToken(ComponentName admin, byte[] token)`

**Propósito**: Establece un token criptográfico que posteriormente puede usarse para resetear la contraseña del dispositivo.

**Casos de Uso**:
- **Setup inicial**: Configurar durante el provisioning del dispositivo
- **Políticas de recuperación**: Preparar mecanismo de recuperación antes de que se necesite
- **Gestión proactiva**: Tener plan de contingencia para usuarios que olvidan contraseñas
- **Cumplimiento**: Mantener capacidad de recuperación sin comprometer seguridad

**Proceso**:
```
1. Generar token seguro (32 bytes aleatorios recomendado)
2. Llamar setResetPasswordToken()
3. Usuario debe desbloquear dispositivo para activar token
4. Una vez activo, guardar token de forma segura en servidor MDM
5. Usar token con resetPasswordWithToken() cuando sea necesario
```

**Requisitos**:
- Device Owner o Profile Owner
- Token debe tener longitud mínima (generalmente 32 bytes)
- Usuario debe activar el token desbloqueando el dispositivo

---

### `clearResetPasswordToken(ComponentName admin)`

**Propósito**: Elimina el token de reseteo de contraseña previamente establecido.

**Casos de Uso**:
- **Rotación de seguridad**: Cambiar tokens periódicamente
- **Limpieza tras uso**: Eliminar token después de usarlo para resetear
- **Revocación de acceso**: Remover capacidad de reseteo cuando ya no es necesario
- **Cambio de políticas**: Actualizar a nuevas políticas de recuperación

---

### `isResetPasswordTokenActive(ComponentName admin)`

**Propósito**: Verifica si el token de reseteo está activo y listo para usar.

**Casos de Uso**:
- Validar que el token fue activado por el usuario
- Verificar capacidad de recuperación antes de necesitarla
- Reportar estado de preparación en sistemas de gestión
- Debugging de problemas de recuperación de contraseñas

**Retorna**: 
- `true`: Token activo, se puede usar para resetear
- `false`: Token no configurado o no activado aún

**Nota**: Un token solo se activa cuando el usuario desbloquea el dispositivo después de establecerlo.

---

### `setRequiredStrongAuthTimeout(ComponentName admin, long timeoutMs)`

**Propósito**: Define cada cuánto tiempo el usuario debe usar autenticación **fuerte** (contraseña/PIN/patrón) en lugar de métodos débiles (huella, face unlock).

**Casos de Uso**:
- **Alta seguridad**: Requiere contraseña cada 4 horas, no solo biometría
- **Cumplimiento normativo**: Muchas regulaciones requieren autenticación fuerte periódica
- **Prevenir bypass biométrico**: Evitar que biometría sea el único método indefinidamente
- **Balance seguridad/UX**: Permitir biometría pero con verificaciones periódicas

**Valores Comunes**:
- `0`: Requerir autenticación fuerte siempre (sin biometría)
- `14400000`: 4 horas (estándar corporativo)
- `28800000`: 8 horas (menos restrictivo)
- `86400000`: 24 horas (mínimo aceptable)

**Ejemplo de Flujo**:
```
1. Usuario desbloquea con contraseña (autenticación fuerte)
2. Por las próximas 4 horas, puede usar huella dactilar
3. Después de 4 horas, sistema requiere contraseña nuevamente
4. Ciclo se repite
```

---

### `getRequiredStrongAuthTimeout(ComponentName admin)`

**Propósito**: Obtiene el timeout configurado para autenticación fuerte.

**Casos de Uso**:
- Verificar políticas actuales
- Reportar configuración a sistemas MDM
- Auditoría de seguridad
- Sincronizar con políticas corporativas

---

### `setTrustAgentConfiguration(ComponentName admin, ComponentName target, PersistableBundle configuration)`

**Propósito**: Configura Trust Agents, componentes que pueden mantener el dispositivo desbloqueado bajo ciertas condiciones de confianza (ej: ubicación, dispositivos Bluetooth cercanos).

**Casos de Uso**:
- **Smart Lock corporativo**: Mantener desbloqueado en oficina mediante geofencing
- **Dispositivos confiables**: Desbloqueo automático con smartwatch corporativo emparejado
- **Ubicaciones seguras**: No requerir contraseña en áreas controladas del edificio
- **Políticas contextuales**: Seguridad adaptativa según contexto

**Ejemplo de Configuraciones**:
- Ubicación de confianza: Coordenadas de oficina corporativa
- Dispositivo confiable: MAC address de dispositivos Bluetooth aprobados
- Red confiable: BSSID de red WiFi corporativa

---

### `getTrustAgentConfiguration(ComponentName admin, ComponentName target)`

**Propósito**: Obtiene la configuración actual de un Trust Agent específico.

**Casos de Uso**:
- Verificar configuración de Smart Lock
- Auditar políticas de confianza
- Sincronizar configuraciones
- Debugging de problemas de desbloqueo

---

## 📝 Ejemplos de Implementación

### Ejemplo 1: Configurar Política de Contraseña Corporativa Estándar

```kotlin
fun configurarPoliticaPasswordCorporativa(dpm: DevicePolicyManager, admin: ComponentName) {
    // Establecer calidad alfanumérica compleja
    dpm.setPasswordQuality(admin, DevicePolicyManager.PASSWORD_QUALITY_COMPLEX)
    
    // Requisitos de longitud y composición
    dpm.setPasswordMinimumLength(admin, 8)           // Mínimo 8 caracteres
    dpm.setPasswordMinimumLetters(admin, 1)          // Al menos 1 letra
    dpm.setPasswordMinimumLowerCase(admin, 1)        // Al menos 1 minúscula
    dpm.setPasswordMinimumUpperCase(admin, 1)        // Al menos 1 mayúscula
    dpm.setPasswordMinimumNumeric(admin, 1)          // Al menos 1 número
    dpm.setPasswordMinimumSymbols(admin, 1)          // Al menos 1 símbolo
    
    // Historial y expiración
    dpm.setPasswordHistoryLength(admin, 12)          // Recordar últimas 12 contraseñas
    dpm.setPasswordExpirationTimeout(admin, 7776000000L) // Cambiar cada 90 días
    
    // Seguridad adicional
    dpm.setMaximumFailedPasswordsForWipe(admin, 10)  // Borrar tras 10 intentos fallidos
    dpm.setMaximumTimeToLock(admin, 300000L)         // Auto-bloqueo tras 5 minutos
    
    // Autenticación fuerte periódica
    dpm.setRequiredStrongAuthTimeout(admin, 14400000L) // Contraseña cada 4 horas
    
    Log.d("PasswordPolicy", "Política corporativa configurada exitosamente")
}
```

### Ejemplo 2: Verificar Cumplimiento de Contraseña

```kotlin
fun verificarCumplimientoPassword(dpm: DevicePolicyManager, admin: ComponentName): ReportePassword {
    // Obtener configuración actual
    val calidadRequerida = dpm.getPasswordQuality(admin)
    val longitudRequerida = dpm.getPasswordMinimumLength(admin)
    val complejidadRequerida = dpm.getRequiredPasswordComplexity()
    val complejidadActual = dpm.getPasswordComplexity()
    
    // Verificar si la contraseña actual cumple
    val passwordValida = dpm.isActivePasswordSufficient()
    val passwordValidaCompleta = dpm.isActivePasswordSufficientForDeviceRequirement()
    
    // Obtener estado de intentos fallidos
    val intentosFallidos = dpm.getCurrentFailedPasswordAttempts()
    val limiteIntentos = 10 // Tu límite configurado
    val intentosRestantes = limiteIntentos - intentosFallidos
    
    // Verificar expiración
    val expiracion = dpm.getPasswordExpiration(admin)
    val diasHastaExpiracion = if (expiracion > 0) {
        ((expiracion - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)).toInt()
    } else {
        -1 // Sin expiración
    }
    
    // Verificar token de reseteo
    val tokenActivo = dpm.isResetPasswordTokenActive(admin)
    
    // Crear reporte
    return ReportePassword(
        passwordValida = passwordValida,
        passwordValidaCompleta = passwordValidaCompleta,
        calidadRequerida = obtenerNombreCalidad(calidadRequerida),
        longitudRequerida = longitudRequerida,
        complejidadRequerida = obtenerNombreComplejidad(complejidadRequerida),
        complejidadActual = obtenerNombreComplejidad(complejidadActual),
        intentosFallidos = intentosFallidos,
        intentosRestantes = intentosRestantes,
        diasHastaExpiracion = diasHastaExpiracion,
        tokenReseteoActivo = tokenActivo
    )
}

data class ReportePassword(
    val passwordValida: Boolean,
    val passwordValidaCompleta: Boolean,
    val calidadRequerida: String,
    val longitudRequerida: Int,
    val complejidadRequerida: String,
    val complejidadActual: String,
    val intentosFallidos: Int,
    val intentosRestantes: Int,
    val diasHastaExpiracion: Int,
    val tokenReseteoActivo: Boolean
)

fun obtenerNombreCalidad(quality: Int): String = when(quality) {
    DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED -> "Sin restricciones"
    DevicePolicyManager.PASSWORD_QUALITY_BIOMETRIC_WEAK -> "Biométrica débil"
    DevicePolicyManager.PASSWORD_QUALITY_SOMETHING -> "Patrón/PIN/Contraseña"
    DevicePolicyManager.PASSWORD_QUALITY_NUMERIC -> "PIN numérico"
    DevicePolicyManager.PASSWORD_QUALITY_NUMERIC_COMPLEX -> "PIN complejo"
    DevicePolicyManager.PASSWORD_QUALITY_ALPHABETIC -> "Alfabética"
    DevicePolicyManager.PASSWORD_QUALITY_ALPHANUMERIC -> "Alfanumérica"
    DevicePolicyManager.PASSWORD_QUALITY_COMPLEX -> "Compleja"
    else -> "Desconocida"
}

fun obtenerNombreComplejidad(complexity: Int): String = when(complexity) {
    DevicePolicyManager.PASSWORD_COMPLEXITY_NONE -> "Ninguna"
    DevicePolicyManager.PASSWORD_COMPLEXITY_LOW -> "Baja"
    DevicePolicyManager.PASSWORD_COMPLEXITY_MEDIUM -> "Media"
    DevicePolicyManager.PASSWORD_COMPLEXITY_HIGH -> "Alta"
    else -> "Desconocida"
}
```

---

## 🎯 Casos de Uso por Escenario

### 🏢 Corporativo Estándar
- **Complejidad**: MEDIUM-HIGH
- **Longitud**: 8-12 caracteres
- **Expiración**: 90 días
- **Historial**: 12 contraseñas
- **Intentos**: 10 antes de borrado
- **Auto-lock**: 5 minutos
- **Strong auth**: Cada 4 horas

### 🏥 Médico/HIPAA
- **Complejidad**: HIGH
- **Longitud**: 12+ caracteres
- **Expiración**: 60 días
- **Historial**: 24 contraseñas
- **Intentos**: 5 antes de borrado
- **Auto-lock**: 30 segundos
- **Strong auth**: Cada 2 horas

### 🏦 Financiero/Bancario
- **Complejidad**: HIGH
- **Longitud**: 10+ caracteres
- **Expiración**: 60-90 días
- **Historial**: 24 contraseñas
- **Intentos**: 3-5 antes de borrado
- **Auto-lock**: 1-2 minutos
- **Strong auth**: Cada 1 hora

### 🏭 Industrial/Kiosko
- **Complejidad**: LOW-MEDIUM
- **Longitud**: 4-6 dígitos PIN
- **Expiración**: 180 días o sin expiración
- **Historial**: 5 contraseñas
- **Intentos**: 20 antes de borrado
- **Auto-lock**: 10-15 minutos
- **Strong auth**: Sin biometría o cada 24 horas

---

## ⚠️ Consideraciones Importantes

### Compatibilidad
- `setRequiredPasswordComplexity()` requiere Android 11+ (API 30)
- `resetPasswordWithToken()` requiere Android 8.0+ (API 26)
- `resetPassword()` está deprecated desde API 26

### Seguridad
- Siempre combinar múltiples políticas para seguridad en capas
- No confiar solo en biometría para datos sensibles
- Considerar el balance entre seguridad y usabilidad
- Documentar políticas para auditorías de cumplimiento

### Mejores Prácticas
- Usar `setRequiredPasswordComplexity()` en Android 11+ por simplicidad
- Implementar notificaciones proactivas antes de expiración
- Configurar token de reseteo durante provisioning inicial
- Testear políticas en dispositivos de desarrollo antes de producción
- Considerar el impacto en UX de políticas muy restrictivas

---

## 📚 Próximas Categorías

Esta es la primera de 22 categorías de `DevicePolicyManager`. La siguiente categoría será:

**2. Bloqueo y Seguridad del Dispositivo** (~20 métodos)

---

*Documentación generada para DevicePolicyManager API de Android Enterprise*