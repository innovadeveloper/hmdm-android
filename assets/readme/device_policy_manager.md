# Análisis Completo de DevicePolicyManager

Excelente pregunta. La clase `DevicePolicyManager` es **masiva** y tiene muchísima más funcionalidad que solo `addUserRestriction()`. Déjame darte un análisis completo.

## 📊 Cantidad Total de Métodos

La clase `DevicePolicyManager` tiene aproximadamente **350-400 métodos públicos** dependiendo de la versión de Android (el número ha ido creciendo con cada versión). En Android 14/15, son más de **380 métodos públicos**.

## 📑 Categorías de Métodos

Voy a organizarlos por categorías funcionales para que puedas navegar mejor:

### 1. **Gestión de Políticas de Contraseña** (~25 métodos)

```kotlin
// Establecer requisitos
setPasswordQuality(admin, quality)
setPasswordMinimumLength(admin, length)
setPasswordMinimumLetters(admin, length)
setPasswordMinimumLowerCase(admin, length)
setPasswordMinimumUpperCase(admin, length)
setPasswordMinimumNumeric(admin, length)
setPasswordMinimumSymbols(admin, length)
setPasswordMinimumNonLetter(admin, length)
setPasswordHistoryLength(admin, length)
setPasswordExpirationTimeout(admin, timeout)
setMaximumFailedPasswordsForWipe(admin, num)
setMaximumTimeToLock(admin, timeMs)
setRequiredPasswordComplexity(complexity)

// Consultar estado
getPasswordQuality(admin)
getPasswordMinimumLength(admin)
isActivePasswordSufficient()
isActivePasswordSufficientForDeviceRequirement()
getCurrentFailedPasswordAttempts()
getPasswordExpiration(admin)
getPasswordComplexity()
getRequiredPasswordComplexity()

// Acciones
resetPassword(password, flags) // Deprecated API 26
resetPasswordWithToken(admin, password, token, flags)
```

### 2. **Bloqueo y Seguridad del Dispositivo** (~20 métodos)

```kotlin
// Bloquear dispositivo
lockNow()
lockNow(flags)

// Borrado
wipeData(flags)
wipeData(flags, reason)

// Keyguard (pantalla de bloqueo)
setKeyguardDisabled(admin, disabled)
setKeyguardDisabledFeatures(admin, which)
getKeyguardDisabledFeatures(admin)

// Tokens de bloqueo
setResetPasswordToken(admin, token)
clearResetPasswordToken(admin)
isResetPasswordTokenActive(admin)

// Timeout
setRequiredStrongAuthTimeout(admin, timeoutMs)
getRequiredStrongAuthTimeout(admin)

// Trust agents
setTrustAgentConfiguration(admin, target, configuration)
getTrustAgentConfiguration(admin, target)
```

### 3. **Restricciones de Usuario** (~15 métodos)

```kotlin
// Agregar/quitar restricciones
addUserRestriction(admin, key)
clearUserRestriction(admin, key)

// Consultar restricciones
getUserRestrictions(admin)
Bundle restrictions = dpm.getUserRestrictions(admin)

// Cross-profile
addCrossProfileIntentFilter(admin, filter, flags)
clearCrossProfileIntentFilters(admin)
setCrossProfileCallerIdDisabled(admin, disabled)
getCrossProfileCallerIdDisabled(admin)
setCrossProfileContactsSearchDisabled(admin, disabled)
getCrossProfileContactsSearchDisabled(admin)

// Widgets
addCrossProfileWidgetProvider(admin, packageName)
removeCrossProfileWidgetProvider(admin, packageName)
getCrossProfileWidgetProviders(admin)
```

### 4. **Gestión de Aplicaciones** (~45 métodos)

```kotlin
// Ocultar/mostrar apps
setApplicationHidden(admin, packageName, hidden)
isApplicationHidden(admin, packageName)

// Suspender apps
setPackagesSuspended(admin, packageNames, suspended)
isPackageSuspended(admin, packageName)

// Bloquear desinstalación
setUninstallBlocked(admin, packageName, uninstallBlocked)
isUninstallBlocked(admin, packageName)

// Apps del sistema
enableSystemApp(admin, packageName)
enableSystemApp(admin, intent)
installExistingPackage(admin, packageName)

// Permisos
setPermissionPolicy(admin, policy)
getPermissionPolicy(admin)
setPermissionGrantState(admin, packageName, permission, grantState)
getPermissionGrantState(admin, packageName, permission)

// Apps predeterminadas
addPersistentPreferredActivity(admin, filter, activity)
clearPackagePersistentPreferredActivities(admin, packageName)

// Launcher persistente
setApplicationRestrictionsManagingPackage(admin, packageName)
getApplicationRestrictionsManagingPackage(admin)

// Restricciones de apps
setApplicationRestrictions(admin, packageName, settings)
getApplicationRestrictions(admin, packageName)

// Instalación
setKeepUninstalledPackages(admin, packageNames)
getKeepUninstalledPackages(admin)

// App ops
setUserControlDisabledPackages(admin, packages)
getUserControlDisabledPackages(admin)

// Metered data
setMeteredDataDisabledPackages(admin, packageNames)
getMeteredDataDisabledPackages(admin)

// Delegación
setDelegatedScopes(admin, delegatePackage, scopes)
getDelegatedScopes(admin, delegatePackage)
```

### 5. **Lock Task Mode (Modo Kiosko)** (~10 métodos)

```kotlin
// Configurar kiosko
setLockTaskPackages(admin, packages)
getLockTaskPackages(admin)
isLockTaskPermitted(packageName)

// Features en lock task
setLockTaskFeatures(admin, flags)
getLockTaskFeatures(admin)

// Iniciar/detener (en Activity)
startLockTask()
stopLockTask()
```

### 6. **Gestión de Certificados** (~15 métodos)

```kotlin
// Instalar certificados
installCaCert(admin, certBuffer)
uninstallCaCert(admin, certBuffer)
getInstalledCaCerts(admin)
hasCaCertInstalled(admin, certBuffer)

// Limpiar
uninstallAllUserCaCerts(admin)

// Certificados de cliente
installKeyPair(admin, privKey, cert, alias)
installKeyPair(admin, privKey, certs, alias, flags)
removeKeyPair(admin, alias)
generateKeyPair(admin, algorithm, keySpec, idAttestationFlags)

// Delegación de certificados
setDelegatedScopes(admin, delegatePackage, List.of(DELEGATION_CERT_INSTALL))

// Selección de certificados
setKeyPairCertificate(admin, alias, certs, isUserSelectable)
```

### 7. **Configuración de Red** (~30 métodos)

```kotlin
// VPN
setAlwaysOnVpnPackage(admin, vpnPackage, lockdownEnabled)
setAlwaysOnVpnPackage(admin, vpnPackage, lockdownEnabled, lockdownWhitelist)
getAlwaysOnVpnPackage(admin)
getAlwaysOnVpnLockdownWhitelist(admin)
isAlwaysOnVpnLockdownEnabled(admin)

// Proxy global
setRecommendedGlobalProxy(admin, proxyInfo)
getGlobalProxyAdmin()

// WiFi directo desde admin
addOverrideApn(admin, apnSetting)
updateOverrideApn(admin, apnId, apnSetting)
removeOverrideApn(admin, apnId)
getOverrideApns(admin)
setOverrideApnsEnabled(admin, enabled)
isOverrideApnEnabled(admin)

// Private DNS
setGlobalPrivateDnsModeSpecifiedHost(admin, privateDnsHost)
setGlobalPrivateDnsModeOpportunistic(admin)
getGlobalPrivateDnsMode(admin)
getGlobalPrivateDnsHost(admin)

// WiFi config desde admin
setWifiSsidPolicy(admin, policy)
getWifiSsidPolicy(admin)

// Minimum WiFi security
setMinimumRequiredWifiSecurityLevel(admin, level)
getMinimumRequiredWifiSecurityLevel()
```

### 8. **Seguridad y Cifrado** (~20 métodos)

```kotlin
// Cifrado
setStorageEncryption(admin, encrypt) // Deprecated
getStorageEncryption(admin)
getStorageEncryptionStatus()

// Requerir cifrado
setRequiredStrongAuthTimeout(admin, timeoutMs)

// Verificación de apps
setApplicationsListWithPackageDetailsDisabled(admin, packageNames)

// Security logging
setSecurityLoggingEnabled(admin, enabled)
isSecurityLoggingEnabled(admin)
retrieveSecurityLogs(admin)
retrievePreRebootSecurityLogs(admin)

// Network logging
setNetworkLoggingEnabled(admin, enabled)
isNetworkLoggingEnabled(admin)
retrieveNetworkLogs(admin, batchToken)

// Bug reports
requestBugreport(admin)

// Backup
setBackupServiceEnabled(admin, enabled)
isBackupServiceEnabled(admin)
```

### 9. **Actualización del Sistema** (~10 métodos)

```kotlin
// Política de actualización
setSystemUpdatePolicy(admin, policy)
getSystemUpdatePolicy()

// Instalación de actualizaciones
installSystemUpdate(admin, uri, executor, callback)

// Pending updates
getPendingSystemUpdate(admin)

// Reboot
reboot(admin)
```

### 10. **Gestión de Usuarios y Perfiles** (~25 métodos)

```kotlin
// Crear perfiles
createAndManageUser(admin, name, componentName, adminExtras, flags)
removeUser(admin, userHandle)

// Work profile
setProfileEnabled(admin, componentName)
setProfileName(admin, profileName)

// Afiliación
setAffiliationIds(admin, ids)
getAffiliationIds(admin)
isAffiliatedUser()

// User icon
setUserIcon(admin, icon)

// Logout
logoutUser(admin)

// Start user
startUserInBackground(admin, userHandle)
stopUser(admin, userHandle)

// Switch user
switchUser(admin, userHandle)

// Ephemeral users
setUserSessionMessage(admin, startMessage, endMessage)

// User restrictions (ya cubierto arriba)
```

### 11. **Cámara y Captura de Pantalla** (~5 métodos)

```kotlin
// Cámara
setCameraDisabled(admin, disabled)
getCameraDisabled(admin)

// Screenshot
setScreenCaptureDisabled(admin, disabled)
getScreenCaptureDisabled(admin)

// Nearby streaming
setNearbyNotificationStreamingPolicy(policy)
getNearbyNotificationStreamingPolicy()
setNearbyAppStreamingPolicy(policy)
getNearbyAppStreamingPolicy()
```

### 12. **Configuraciones del Sistema** (~40 métodos)

```kotlin
// Settings globales
setGlobalSetting(admin, setting, value)

// Settings secure  
setSecureSetting(admin, setting, value)

// Settings system
setSystemSetting(admin, setting, value)

// Configuración de tiempo
setTime(admin, millis)
setTimeZone(admin, timeZone)
setAutoTimeEnabled(admin, enabled)
setAutoTimeZoneEnabled(admin, enabled)
getAutoTimeEnabled(admin)
getAutoTimeZoneEnabled(admin)

// Master volume
setMasterVolumeMuted(admin, on)
isMasterVolumeMuted(admin)

// Status bar
setStatusBarDisabled(admin, disabled)

// Configurar organización
setOrganizationName(admin, title)
getOrganizationName(admin)
setOrganizationColor(admin, color)
getOrganizationColor(admin)

// Device owner strings
setDeviceOwnerLockScreenInfo(admin, info)
getDeviceOwnerLockScreenInfo()

// Long support message
setLongSupportMessage(admin, message)
getLongSupportMessage(admin)
setShortSupportMessage(admin, message)
getShortSupportMessage(admin)

// Locale
setSystemLocales(admin, locales)
getSystemLocales(admin)
```

### 13. **Device Owner y Profile Owner** (~20 métodos)

```kotlin
// Verificación
isDeviceOwnerApp(packageName)
isProfileOwnerApp(packageName)
getDeviceOwner()
getDeviceOwnerNameOnAnyUser()
getProfileOwner()
getProfileOwnerAsUser(userHandle)
getProfileOwnerName()
getDeviceOwnerComponentOnCallingUser()
getDeviceOwnerComponentOnAnyUser()

// Transfer ownership
transferOwnership(admin, target, bundle)

// Provisioning
setDeviceOwnerLockScreenInfo(admin, info)

// Device IDs
getEnrollmentSpecificId()
getDeviceId()

// Organización
setOrganizationId(organizationId)
```

### 14. **Bluetooth y NFC** (~8 métodos)

```kotlin
// Bluetooth contacto sharing
setBluetoothContactSharingDisabled(admin, disabled)
getBluetoothContactSharingDisabled(admin)

// Configuración BT
setBluetoothContactSharingEnabledForKnownContacts(admin, enabled)

// Preferencias de pairing BT (Android 14+)
setPreferentialNetworkServiceConfigs(configs)
getPreferentialNetworkServiceConfigs()
```

### 15. **Input Methods (Teclados)** (~5 métodos)

```kotlin
// Métodos de entrada permitidos
setPermittedInputMethods(admin, packageNames)
getPermittedInputMethods(admin)

// Para managed profile
setPermittedInputMethodsForCurrentUser(packageNames)

// Accessibility services
setPermittedAccessibilityServices(admin, packageNames)
getPermittedAccessibilityServices(admin)
```

### 16. **Account Management** (~8 métodos)

```kotlin
// Tipos de cuenta
setAccountManagementDisabled(admin, accountType, disabled)
getAccountTypesWithManagementDisabled()

// Agregar cuenta
addAccount(admin, accountType, callback)
```

### 17. **Device Identifiers** (~5 métodos)

```kotlin
// IDs
getWifiMacAddress(admin)
getEnrollmentSpecificId()
```

### 18. **Factory Reset Protection** (~3 métodos)

```kotlin
// FRP
setFactoryResetProtectionPolicy(admin, policy)
getFactoryResetProtectionPolicy(admin)
```

### 19. **Compliance y DeviceCompliance** (~15 métodos)

```kotlin
// Compliance
setDeviceCompliant(compliant)
setComplianceAcknowledgementRequired(admin, complianceAcknowledgementRequired)
isComplianceAcknowledgementRequired()

// Owned device compliance (Android 14+)
setManagedProfileMaximumTimeOff(admin, timeoutMs)
getManagedProfileMaximumTimeOff(admin)
```

### 20. **USB Data Signaling** (~3 métodos)

```kotlin
// Android 14+
setUsbDataSignalingEnabled(enabled)
isUsbDataSignalingEnabled()
isUsbDataSignalingEnabledForUser()
canUsbDataSignalingBeDisabled()
```

### 21. **Content Protection Policy** (~3 métodos)

```kotlin
// Android 15+
setContentProtectionPolicy(admin, policy)
getContentProtectionPolicy(admin)
```

### 22. **Otros Métodos Importantes** (~30 métodos)

```kotlin
// Notificaciones
setNotificationPolicy(admin, policy)
getNotificationPolicy(admin)

// Do not disturb
setAutoTimeRequired(admin, required)
getAutoTimeRequired()

// Localización
setLocationEnabled(admin, locationEnabled)

// Trabajo y vida personal
setPersonalAppsSuspended(admin, suspended)
getPersonalAppsSuspendedReasons(admin)

// End of life (EOL)
setDeviceOwnerType(admin, deviceOwnerType)
getDeviceOwnerType(admin)

// Common criteria mode
setCommonCriteriaModeEnabled(admin, enabled)
isCommonCriteriaModeEnabled(admin)

// Organizacional
setOrganizationIdForUser(admin, enterpriseId, userId)

// preferencias
setPreferentialNetworkServiceEnabled(enabled)

// Biometric Auth
setBiometricAuthenticationEnabled(admin, enabled)

// Delegated scopes
getDelegatePackages(admin, delegationScope)

// Nearby streaming
setNearbyNotificationStreamingPolicy(policy)
setNearbyAppStreamingPolicy(policy)

// Cross profile packages  
setCrossProfilePackages(admin, packages)
getAllCrossProfilePackages()

// Financed device
isDeviceFinanced()

// Managed subscriptions
setManagedSubscriptionsPolicy(policy)
getManagedSubscriptionsPolicy()
```

## Resumen por Versión de Android

| Versión Android | Métodos Aproximados |
|-----------------|---------------------|
| Android 5.0 (API 21) | ~120 métodos |
| Android 6.0 (API 23) | ~150 métodos |
| Android 7.0 (API 24) | ~180 métodos |
| Android 8.0 (API 26) | ~220 métodos |
| Android 9.0 (API 28) | ~250 métodos |
| Android 10 (API 29) | ~280 métodos |
| Android 11 (API 30) | ~310 métodos |
| Android 12 (API 31) | ~340 métodos |
| Android 13 (API 33) | ~360 métodos |
| Android 14 (API 34) | ~380 métodos |
| Android 15 (API 35) | **~400+ métodos** |


**Categorías principales:**
- 📱 Gestión de Apps: ~45 métodos
- ⚙️ Configuración Sistema: ~40 métodos
- 🌐 Redes: ~30 métodos
- 👥 Usuarios/Perfiles: ~25 métodos
- 🔒 Contraseñas: ~25 métodos
- 🔐 Seguridad: ~20 métodos
- 🔒 Bloqueo: ~20 métodos
- 📜 Certificados: ~15 métodos
- 🚫 Restricciones: ~15 métodos
- 🖥️ Lock Task: ~10 métodos
- 🔄 Updates: ~10 métodos
- Y muchas más categorías pequeñas...
