# Documentación DevicePolicyManager - Categoría 10: Gestión de Usuarios y Perfiles

## 📋 Visión General

La categoría de **Gestión de Usuarios y Perfiles** agrupa aproximadamente **25 métodos** que permiten crear, administrar y controlar usuarios y perfiles en dispositivos Android. Es fundamental para dispositivos compartidos, Work Profiles y gestión de identidades corporativas.

## 🎯 Propósito

Estos métodos permiten:
- **Crear usuarios** y perfiles de trabajo programáticamente
- **Gestionar Work Profiles** (perfiles de trabajo)
- **Configurar afiliación** entre usuarios y dispositivos
- **Controlar sesiones** de usuario
- **Personalizar perfiles** con iconos y nombres
- **Gestionar logout** y cambio de usuario
- **Administrar usuarios efímeros**

---

## 👤 Conceptos Fundamentales

### Tipos de Usuarios en Android

**Usuario Principal (Primary User)**:
- Primer usuario creado en el dispositivo
- Usuario ID 0
- Tiene permisos especiales
- No se puede eliminar

**Usuarios Secundarios (Secondary Users)**:
- Usuarios adicionales con espacios separados
- IDs 10, 11, 12...
- Datos completamente aislados
- Pueden ser eliminados

**Usuarios Efímeros (Ephemeral Users)**:
- Usuarios temporales
- Se eliminan automáticamente al logout
- Útiles para dispositivos compartidos

**Perfiles (Profiles)**:
- Espacios aislados dentro de un usuario
- Work Profile: Separación trabajo/personal
- Clone Profile: Duplicar apps (Android 13+)
- Private Profile: Apps ocultas (Android 15+)

### Modelo de Perfiles

```
Dispositivo
├── Usuario Principal (ID: 0)
│   ├── Perfil Personal
│   └── Work Profile (Perfil de Trabajo)
│       └── Apps corporativas
├── Usuario Secundario (ID: 10)
│   └── Espacio aislado
└── Usuario Invitado (ID: 11)
    └── Sesión temporal
```

---

## 👥 Métodos de Creación de Usuarios

### `createAndManageUser(ComponentName admin, String name, ComponentName profileOwner, PersistableBundle adminExtras, int flags)`

**Propósito**: Crea un nuevo usuario secundario en el dispositivo y opcionalmente lo configura con un Profile Owner. Solo disponible para Device Owner.

**Parámetros**:
- `name`: Nombre del usuario
- `profileOwner`: ComponentName del admin que gestionará el usuario (puede ser null)
- `adminExtras`: Datos adicionales para el Profile Owner
- `flags`: Banderas de configuración del usuario

**Flags Disponibles**:

| Flag | Descripción | Uso |
|------|-------------|-----|
| `SKIP_SETUP_WIZARD` | Omitir wizard de configuración inicial | Provisioning automatizado |
| `MAKE_USER_EPHEMERAL` | Usuario temporal (se borra al logout) | Dispositivos compartidos |
| `LEAVE_ALL_SYSTEM_APPS_ENABLED` | Mantener todas las apps del sistema | Usuario necesita funcionalidad completa |

**Casos de Uso**:
- **Dispositivos compartidos**: Múltiples usuarios en tablet familiar/corporativa
- **Kioscos multi-usuario**: Diferentes perfiles por turno de trabajo
- **Educación**: Usuario por estudiante en tablets compartidas
- **Retail**: Usuario por empleado en dispositivos de tienda

**Ejemplo de Uso**:
```kotlin
fun crearUsuarioSecundario(nombre: String, esEfimero: Boolean = false): UserHandle? {
    try {
        val flags = if (esEfimero) {
            DevicePolicyManager.SKIP_SETUP_WIZARD or 
            DevicePolicyManager.MAKE_USER_EPHEMERAL
        } else {
            DevicePolicyManager.SKIP_SETUP_WIZARD
        }
        
        val userHandle = dpm.createAndManageUser(
            admin,
            nombre,
            null,  // Sin Profile Owner por ahora
            null,  // Sin extras
            flags
        )
        
        if (userHandle != null) {
            Log.i("Users", "✅ Usuario creado: $nombre (ID: ${userHandle.identifier})")
            Log.i("Users", "   Efímero: $esEfimero")
            
            // El usuario está creado pero no iniciado
            // Opcionalmente iniciarlo
            startUserInBackground(userHandle)
            
            return userHandle
        } else {
            Log.e("Users", "❌ No se pudo crear usuario: $nombre")
            return null
        }
        
    } catch (e: SecurityException) {
        Log.e("Users", "❌ Sin permisos: ${e.message}")
        return null
    }
}

// Crear usuario con Profile Owner
fun crearUsuarioConProfileOwner(
    nombre: String,
    profileOwnerComponent: ComponentName
): UserHandle? {
    
    val adminExtras = PersistableBundle().apply {
        putString("user_type", "managed")
        putString("department", "ventas")
    }
    
    val userHandle = dpm.createAndManageUser(
        admin,
        nombre,
        profileOwnerComponent,
        adminExtras,
        DevicePolicyManager.SKIP_SETUP_WIZARD
    )
    
    return userHandle
}
```

**Limitaciones**:
- Máximo de usuarios depende del dispositivo (típicamente 4-10)
- Solo Device Owner puede crear usuarios
- Usuario creado está detenido inicialmente
- Requiere permisos MANAGE_USERS

---

### `removeUser(ComponentName admin, UserHandle userHandle)`

**Propósito**: Elimina un usuario secundario del dispositivo. No se puede eliminar el usuario principal.

**Casos de Uso**:
- Eliminar usuarios efímeros al finalizar sesión
- Limpiar usuarios inactivos
- Liberar espacio de almacenamiento
- Revocar acceso de usuario

**Retorna**: `boolean`
- `true`: Usuario eliminado exitosamente
- `false`: No se pudo eliminar (usuario no existe, es el principal, etc.)

**Ejemplo de Uso**:
```kotlin
fun eliminarUsuario(userHandle: UserHandle): Boolean {
    try {
        val eliminado = dpm.removeUser(admin, userHandle)
        
        if (eliminado) {
            Log.i("Users", "✅ Usuario eliminado: ID ${userHandle.identifier}")
        } else {
            Log.w("Users", "⚠️ No se pudo eliminar usuario: ID ${userHandle.identifier}")
        }
        
        return eliminado
        
    } catch (e: SecurityException) {
        Log.e("Users", "❌ Sin permisos para eliminar usuario: ${e.message}")
        return false
    }
}

// Eliminar usuarios inactivos
fun limpiarUsuariosInactivos(diasInactividad: Int) {
    val userManager = context.getSystemService(Context.USER_SERVICE) as UserManager
    val usuarios = userManager.users
    
    usuarios.forEach { userInfo ->
        // No eliminar usuario principal
        if (userInfo.id == 0) return@forEach
        
        val ultimaActividad = obtenerUltimaActividad(userInfo.id)
        val diasInactivo = (System.currentTimeMillis() - ultimaActividad) / (1000 * 60 * 60 * 24)
        
        if (diasInactivo > diasInactividad) {
            Log.i("Users", "🗑️ Eliminando usuario inactivo: ${userInfo.name} ($diasInactivo días)")
            eliminarUsuario(userInfo.userHandle)
        }
    }
}
```

**⚠️ ADVERTENCIAS**:
- Acción irreversible - todos los datos del usuario se pierden
- Apps en ejecución del usuario se terminan
- Proceso puede tardar varios minutos
- No se puede eliminar el usuario actual activo

---

## 📱 Métodos de Gestión de Work Profile

### `setProfileEnabled(ComponentName admin, ComponentName profileOwner)`

**Propósito**: Habilita un perfil de trabajo (Work Profile) previamente creado. El perfil debe existir y estar configurado.

**Contexto**:
Cuando se crea un Work Profile mediante provisioning, inicialmente puede estar deshabilitado. Este método lo activa.

**Ejemplo de Uso**:
```kotlin
fun habilitarWorkProfile() {
    val profileOwner = ComponentName(context, MyDeviceAdminReceiver::class.java)
    
    try {
        dpm.setProfileEnabled(admin, profileOwner)
        Log.i("WorkProfile", "✅ Work Profile habilitado")
        
        // El perfil ahora es visible en el launcher
        // Las apps de trabajo pueden ejecutarse
        
    } catch (e: SecurityException) {
        Log.e("WorkProfile", "❌ Error al habilitar profile: ${e.message}")
    }
}
```

---

### `setProfileName(ComponentName admin, String profileName)`

**Propósito**: Establece el nombre visible del Work Profile. Este nombre aparece en el launcher y en la barra de estado cuando se usan apps de trabajo.

**Casos de Uso**:
- Personalizar con nombre de la empresa
- Distinguir múltiples perfiles de trabajo
- Branding corporativo

**Ejemplo de Uso**:
```kotlin
fun personalizarWorkProfile(nombreEmpresa: String) {
    // Establecer nombre del perfil
    dpm.setProfileName(admin, nombreEmpresa)
    
    Log.i("WorkProfile", "Perfil renombrado: $nombreEmpresa")
    
    // El usuario verá:
    // "Apps de [nombreEmpresa]" en el launcher
}

// Ejemplos de nombres
personalizarWorkProfile("Empresa Corp")
personalizarWorkProfile("Trabajo")
personalizarWorkProfile("Corporativo")
```

---

## 🔗 Métodos de Afiliación

### `setAffiliationIds(ComponentName admin, Set<String> ids)`

**Propósito**: Establece IDs de afiliación que indican que el Device Owner y Profile Owner(s) pertenecen a la misma organización. Esto habilita funcionalidades adicionales entre perfiles.

**Concepto de Afiliación**:
Cuando un dispositivo tiene Device Owner Y Work Profile con Profile Owner, por defecto están "desafiliados" (no relacionados). La afiliación indica que pertenecen a la misma empresa, habilitando:

- Security logging compartido
- Network logging compartido
- Bug reports accesibles por ambos
- Mejor integración entre perfiles

**Ejemplo de Uso**:
```kotlin
// En el Device Owner
fun establecerAfiliacionDeviceOwner() {
    val affiliationIds = setOf(
        "empresa-corp-2024",
        "org.empresa.com"
    )
    
    dpm.setAffiliationIds(admin, affiliationIds)
    Log.i("Affiliation", "✅ Device Owner afiliado con IDs: $affiliationIds")
}

// En el Profile Owner (mismo conjunto de IDs)
fun establecerAfiliacionProfileOwner() {
    val affiliationIds = setOf(
        "empresa-corp-2024",
        "org.empresa.com"
    )
    
    dpm.setAffiliationIds(admin, affiliationIds)
    Log.i("Affiliation", "✅ Profile Owner afiliado con IDs: $affiliationIds")
}

// IMPORTANTE: Los IDs deben coincidir exactamente
```

**⚠️ Importante**:
- Los IDs deben ser EXACTAMENTE iguales en Device Owner y Profile Owner
- Al menos un ID debe coincidir para estar afiliados
- Los IDs son strings arbitrarios (recomendado usar identificadores únicos de la organización)

---

### `getAffiliationIds(ComponentName admin)`

**Propósito**: Obtiene los IDs de afiliación configurados.

**Retorna**: `Set<String>` con los IDs de afiliación.

---

### `isAffiliatedUser()`

**Propósito**: Verifica si el usuario actual está afiliado (el Device Owner y Profile Owner comparten IDs de afiliación).

**Retorna**: `boolean`

**Ejemplo de Uso**:
```kotlin
fun verificarAfiliacion(): String {
    val afiliado = dpm.isAffiliatedUser()
    val idsLocales = dpm.getAffiliationIds(admin)
    
    return buildString {
        append("🔗 ESTADO DE AFILIACIÓN\n")
        append("═══════════════════════════\n\n")
        
        if (afiliado) {
            append("✅ Usuario AFILIADO\n")
            append("   Device Owner y Profile Owner de misma organización\n")
            append("   Funcionalidades compartidas habilitadas\n")
        } else {
            append("❌ Usuario NO AFILIADO\n")
            append("   Device Owner y Profile Owner independientes\n")
            append("   Funcionalidades limitadas\n")
        }
        
        append("\nIDs de afiliación configurados:\n")
        if (idsLocales.isEmpty()) {
            append("   (ninguno)\n")
        } else {
            idsLocales.forEach { id ->
                append("   • $id\n")
            }
        }
    }
}
```

---

## 🎨 Métodos de Personalización de Usuario

### `setUserIcon(ComponentName admin, Bitmap icon)`

**Propósito**: Establece el icono (avatar) del usuario actual. Aparece en la pantalla de bloqueo, selector de usuarios y configuración.

**Casos de Uso**:
- Branding corporativo con logo de empresa
- Foto del empleado
- Identificación visual rápida en dispositivos compartidos
- Personalización de Work Profile

**Ejemplo de Uso**:
```kotlin
fun establecerIconoUsuario(iconoResourceId: Int) {
    val bitmap = BitmapFactory.decodeResource(context.resources, iconoResourceId)
    
    // Redimensionar si es necesario (recomendado 256x256)
    val iconoRedimensionado = Bitmap.createScaledBitmap(bitmap, 256, 256, true)
    
    dpm.setUserIcon(admin, iconoRedimensionado)
    Log.i("UserIcon", "✅ Icono de usuario establecido")
}

// Establecer logo corporativo
fun establecerLogoCorporativo() {
    val logo = cargarLogoCorporativo()
    dpm.setUserIcon(admin, logo)
}

// Establecer foto del empleado
fun establecerFotoEmpleado(empleadoId: String) {
    val foto = descargarFotoEmpleado(empleadoId)
    if (foto != null) {
        dpm.setUserIcon(admin, foto)
    }
}

// Icono por departamento
fun establecerIconoPorDepartamento(departamento: String) {
    val iconoId = when (departamento) {
        "ventas" -> R.drawable.icon_sales
        "it" -> R.drawable.icon_tech
        "rrhh" -> R.drawable.icon_hr
        else -> R.drawable.icon_default
    }
    establecerIconoUsuario(iconoId)
}
```

**Recomendaciones**:
- Resolución: 256x256 píxeles (óptimo)
- Formato: PNG con transparencia
- Tamaño archivo: < 100 KB
- Imagen cuadrada (se recorta circular automáticamente)

---

## 🚪 Métodos de Control de Sesión

### `logoutUser(ComponentName admin)`

**Propósito**: Cierra la sesión del usuario actual y regresa a la pantalla de selección de usuarios.

**Casos de Uso**:
- **Dispositivos compartidos**: Usuario termina su turno
- **Kioscos**: Finalizar sesión tras inactividad
- **Seguridad**: Logout forzado tras evento sospechoso
- **Usuarios efímeros**: Terminar y eliminar sesión temporal

**Ejemplo de Uso**:
```kotlin
fun cerrarSesionUsuario() {
    try {
        val resultado = dpm.logoutUser(admin)
        
        if (resultado == DevicePolicyManager.USER_OPERATION_SUCCESS) {
            Log.i("Session", "✅ Sesión cerrada exitosamente")
            // El usuario verá la pantalla de selección de usuarios
        } else {
            Log.e("Session", "❌ Error al cerrar sesión: código $resultado")
        }
        
    } catch (e: SecurityException) {
        Log.e("Session", "❌ Sin permisos para logout: ${e.message}")
    }
}

// Logout automático por inactividad
fun configurarLogoutPorInactividad(minutosInactividad: Int) {
    var ultimaActividad = System.currentTimeMillis()
    
    // Actualizar en cada interacción del usuario
    fun onUserInteraction() {
        ultimaActividad = System.currentTimeMillis()
    }
    
    // Verificador periódico
    val verificador = Runnable {
        val tiempoInactivo = System.currentTimeMillis() - ultimaActividad
        val minutosInactivo = tiempoInactivo / (1000 * 60)
        
        if (minutosInactivo >= minutosInactividad) {
            Log.i("Session", "⏱️ Inactividad detectada: $minutosInactivo minutos")
            
            // Advertir al usuario
            mostrarAdvertencia("Cerrando sesión por inactividad en 30 segundos")
            
            // Esperar y cerrar sesión
            Handler(Looper.getMainLooper()).postDelayed({
                cerrarSesionUsuario()
            }, 30000)
        }
    }
}

// Logout programado
fun programarLogoutAutomatico(hora: Int, minuto: Int) {
    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hora)
        set(Calendar.MINUTE, minuto)
        
        if (timeInMillis < System.currentTimeMillis()) {
            add(Calendar.DAY_OF_YEAR, 1)
        }
    }
    
    val intent = Intent(context, LogoutReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    alarmManager.setExactAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP,
        calendar.timeInMillis,
        pendingIntent
    )
    
    Log.i("Session", "Logout automático programado: ${calendar.time}")
}
```

**Códigos de Resultado**:
- `USER_OPERATION_SUCCESS` (0): Éxito
- `USER_OPERATION_ERROR_UNKNOWN` (1): Error desconocido
- `USER_OPERATION_ERROR_MANAGED_PROFILE` (2): No puede hacer logout de Work Profile
- `USER_OPERATION_ERROR_CURRENT_USER` (3): Usuario actual no puede hacer logout

---

### `startUserInBackground(ComponentName admin, UserHandle userHandle)`

**Propósito**: Inicia un usuario secundario en segundo plano sin cambiar al usuario visible. Útil para pre-cargar usuarios.

**Casos de Uso**:
- Pre-iniciar usuario antes de cambiar
- Mantener servicios de usuario corriendo
- Optimizar tiempo de cambio de usuario

**Retorna**: `int` con código de resultado.

---

### `stopUser(ComponentName admin, UserHandle userHandle)`

**Propósito**: Detiene un usuario en segundo plano. Termina todos sus procesos y libera recursos.

**Casos de Uso**:
- Liberar memoria cuando usuario no está activo
- Prevenir procesos en background de usuarios inactivos
- Gestión eficiente de recursos

**Retorna**: `int` con código de resultado.

---

### `switchUser(ComponentName admin, UserHandle userHandle)`

**Propósito**: Cambia al usuario especificado como usuario activo del dispositivo.

**Casos de Uso**:
- Dispositivos compartidos con cambio rápido
- Kioscos multi-usuario
- Cambio programático entre usuarios

**Retorna**: `boolean`

**Ejemplo de Uso**:
```kotlin
fun cambiarUsuario(userHandle: UserHandle): Boolean {
    try {
        // Opcional: Iniciar usuario en background primero
        dpm.startUserInBackground(admin, userHandle)
        
        // Esperar a que inicie
        Thread.sleep(2000)
        
        // Cambiar al usuario
        val cambiado = dpm.switchUser(admin, userHandle)
        
        if (cambiado) {
            Log.i("Users", "✅ Cambiado a usuario: ${userHandle.identifier}")
        } else {
            Log.e("Users", "❌ No se pudo cambiar de usuario")
        }
        
        return cambiado
        
    } catch (e: SecurityException) {
        Log.e("Users", "❌ Sin permisos: ${e.message}")
        return false
    }
}

// Selector de usuarios con UI
fun mostrarSelectorUsuarios() {
    val userManager = context.getSystemService(Context.USER_SERVICE) as UserManager
    val usuarios = userManager.users
    
    val opciones = usuarios.map { it.name }.toTypedArray()
    
    AlertDialog.Builder(context)
        .setTitle("Seleccionar Usuario")
        .setItems(opciones) { _, which ->
            val usuario = usuarios[which]
            cambiarUsuario(usuario.userHandle)
        }
        .show()
}
```

---

## 📝 Métodos de Mensajes de Usuario

### `setUserSessionMessage(ComponentName admin, CharSequence startMessage, CharSequence endMessage)`

**Propósito**: Establece mensajes que se muestran al inicio y fin de sesión de un usuario efímero.

**Casos de Uso**:
- Bienvenida personalizada en dispositivos compartidos
- Instrucciones para usuarios temporales
- Recordatorios al cerrar sesión

**Ejemplo de Uso**:
```kotlin
fun configurarMensajesSesion() {
    val mensajeInicio = """
        Bienvenido a Empresa Corp
        
        Este dispositivo es para uso corporativo.
        Sus datos se eliminarán al cerrar sesión.
        
        ¿Necesita ayuda? Contacte IT: ext. 2500
    """.trimIndent()
    
    val mensajeFin = """
        Gracias por usar este dispositivo
        
        Sus datos han sido eliminados.
        Recuerde cerrar todas las aplicaciones.
        
        Hasta pronto!
    """.trimIndent()
    
    dpm.setUserSessionMessage(admin, mensajeInicio, mensajeFin)
    
    Log.i("Session", "✅ Mensajes de sesión configurados")
}

// Mensajes por tipo de usuario
fun configurarMensajesPorRol(rol: String) {
    val (inicio, fin) = when (rol) {
        "invitado" -> Pair(
            "Bienvenido, invitado. Esta es una sesión temporal.",
            "Sesión de invitado finalizada. Datos eliminados."
        )
        "empleado" -> Pair(
            "Bienvenido, empleado. Recuerde seguir las políticas corporativas.",
            "Sesión finalizada. Gracias por su trabajo."
        )
        "estudiante" -> Pair(
            "Bienvenido, estudiante. Usa este dispositivo responsablemente.",
            "Sesión terminada. No olvides tus pertenencias."
        )
        else -> Pair("Bienvenido", "Hasta luego")
    }
    
    dpm.setUserSessionMessage(admin, inicio, fin)
}
```

---

## 🎯 Casos de Uso por Escenario

### 🏢 Dispositivo Compartido Corporativo
```kotlin
// Configurar dispositivo compartido con usuarios efímeros
fun configurarDispositivoCompartido() {
    // 1. Crear usuarios efímeros para empleados
    val turnos = listOf("Turno Mañana", "Turno Tarde", "Turno Noche")
    
    turnos.forEach { turno ->
        val usuario = crearUsuarioSecundario(
            nombre = turno,
            esEfimero = true
        )
        
        if (usuario != null) {
            // 2. Personalizar con icono
            establecerIconoPorTurno(turno)
            
            // 3. Configurar mensajes
            configurarMensajesPorTurno(turno)
        }
    }
    
    // 4. Logout automático tras inactividad
    configurarLogoutPorInactividad(minutosInactividad = 15)
    
    Log.i("SharedDevice", "✅ Dispositivo compartido configurado")
}
```

---

### 🎓 Tablet Educativa Compartida
```kotlin
// Tablet compartida por múltiples estudiantes
fun configurarTabletEducativa() {
    // 1. Crear usuarios efímeros por clase
    val clases = listOf("Clase A", "Clase B", "Clase C")
    
    clases.forEach { clase ->
        val usuario = crearUsuarioSecundario(
            nombre = clase,
            esEfimero = true
        )
        
        if (usuario != null) {
            // 2. Configurar mensajes educativos
            dpm.setUserSessionMessage(
                admin,
                "Bienvenido a la $clase\nUsa la tablet responsablemente",
                "Recuerda guardar tu trabajo antes de salir"
            )
        }
    }
    
    // 3. Logout automático al fin de clase (45 minutos)
    programarLogoutAutomatico(hora = 15, minuto = 30)
}
```

---

### 🏪 POS Multi-Cajero
```kotlin
// Punto de venta con múltiples cajeros
fun configurarPOSMultiCajero() {
    val cajeros = listOf(
        "Caja 1" to "cajero001",
        "Caja 2" to "cajero002",
        "Caja 3" to "cajero003"
    )
    
    cajeros.forEach { (nombre, id) ->
        val usuario = crearUsuarioSecundario(
            nombre = nombre,
            esEfimero = false  // Persistente para mantener configuración
        )
        
        if (usuario != null) {
            // Foto del empleado como icono
            val foto = descargarFotoEmpleado(id)
            if (foto != null) {
                dpm.setUserIcon(admin, foto)
            }
            
            // Configurar Work Profile para separación de datos
            configurarWorkProfileParaCajero(usuario, id)
        }
    }
}
```

---

### 🏥 Dispositivo Médico Compartido
```kotlin
// Dispositivo médico con usuarios por turno
fun configurarDispositivoMedico() {
    // Usuarios persistentes (no efímeros) para auditoría
    val turnos = listOf(
        "Turno 07:00-15:00",
        "Turno 15:00-23:00", 
        "Turno 23:00-07:00"
    )
    
    turnos.forEach { turno ->
        val usuario = crearUsuarioSecundario(
            nombre = turno,
            esEfimero = false  // IMPORTANTE: Mantener logs de auditoría
        )
        
        if (usuario != null) {
            // Establecer afiliación para compartir security logs
            dpm.setAffiliationIds(admin, setOf("hospital-central-2024"))
            
            // Sin mensajes (privacidad de pacientes)
            // Sin icono personalizado (neutralidad)
        }
    }
    
    // Cambio automático de usuario al inicio de turno
    programarCambioUsuarioAutomatico()
}
```

---

## ⚠️ Consideraciones Importantes

### Usuarios Efímeros

**Ventajas**:
- Privacidad: Datos se eliminan al logout
- Sin acumulación de basura
- Ideal para dispositivos compartidos públicos

**Desventajas**:
- No se mantienen preferencias
- Usuario debe reconfigurar cada vez
- No apto si necesitas persistencia

### Límites del Sistema

**Máximo de Usuarios**:
- Depende del fabricante y hardware
- Típicamente 4-8 usuarios adicionales
- Cada usuario consume ~2-4 GB de almacenamiento

**Rendimiento**:
- Cambiar usuario puede tardar 10-30 segundos
- Múltiples usuarios activos consumen RAM
- Monitorear uso de almacenamiento

### Work Profile vs Usuario Secundario

**Work Profile**:
- ✅ Separación trabajo/personal en mismo usuario
- ✅ Cambio rápido (sin logout)
- ✅ Menor consumo de almacenamiento
- ❌ Solo un Work Profile por usuario

**Usuario Secundario**:
- ✅ Aislamiento completo
- ✅ Múltiples usuarios posibles
- ✅ Mejor para dispositivos compartidos
- ❌ Mayor consumo de recursos
- ❌ Cambio más lento

### Afiliación

**Cuándo Afiliar**:
- Device Owner + Work Profile de misma organización
- Necesitas compartir security/network logs
- Gestión unificada entre perfiles

**Cuándo NO Afiliar**:
- BYOD (Bring Your Own Device)
- Usuario personal con perfil corporativo separado
- Máxima separación requerida

### Mejores Prácticas

1. **Usuarios Efímeros** para dispositivos públicos/compartidos

2. **Logout automático** por inactividad siempre

3. **Mensajes claros** explicando que datos se eliminan

4. **Iconos distintivos** para identificación rápida

5. **Límite de usuarios** según capacidad del dispositivo

6. **Monitoreo** de almacenamiento y rendimiento

7. **Auditoría** de sesiones en entornos sensibles

---

## 📚 Próxima Categoría

**11. Cámara y Captura de Pantalla** (~5 métodos)

Esta categoría cubrirá:
- Deshabilitar cámara
- Bloquear captura de pantalla
- Políticas de Nearby streaming
- Control de medios visuales

---

*Documentación generada para DevicePolicyManager API de Android Enterprise - Categoría 10 de 22*