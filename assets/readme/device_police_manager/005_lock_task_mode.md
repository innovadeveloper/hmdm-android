# Documentación DevicePolicyManager - Categoría 5: Lock Task Mode (Modo Kiosko)

## 📋 Visión General

La categoría de **Lock Task Mode** agrupa aproximadamente **10 métodos** que permiten configurar y gestionar el modo kiosko de Android. Este modo bloquea el dispositivo a una o más aplicaciones específicas, impidiendo que el usuario salga o acceda a otras funcionalidades del sistema.

## 🎯 Propósito

Estos métodos permiten:
- **Bloquear el dispositivo** a aplicaciones específicas
- **Configurar qué apps** pueden ejecutarse en modo kiosko
- **Controlar features del sistema** disponibles en modo kiosko
- **Verificar permisos** de Lock Task para aplicaciones
- **Iniciar/detener** el modo kiosko programáticamente

---

## 🔐 Concepto de Lock Task Mode

**Lock Task Mode** (también llamado "Kiosk Mode" o "Single-App Mode") es una característica de Android Enterprise que convierte un dispositivo en un quiosco dedicado.

### Estados del Dispositivo

| Estado | Descripción | Puede Salir |
|--------|-------------|-------------|
| **Normal** | Funcionamiento estándar de Android | N/A |
| **Lock Task (Pinned)** | Usuario puede salir manteniendo botones Back+Recent | ✅ Sí |
| **Lock Task (Locked)** | Usuario NO puede salir, requiere contraseña o código | ❌ No |

### Características del Lock Task Mode

**Cuando está activo**:
- ✅ Solo las apps permitidas pueden ejecutarse
- ❌ Botón Home deshabilitado (o limitado)
- ❌ Botón Recent Apps deshabilitado (o limitado)
- ❌ Notificaciones bloqueadas (opcional)
- ❌ No se puede acceder a Settings
- ❌ Barra de estado limitada o oculta
- ❌ Quick Settings deshabilitado (opcional)

**Casos de Uso Comunes**:
- 🏪 Punto de Venta (POS)
- 📊 Quioscos informativos
- 🏥 Check-in de pacientes
- ✈️ Check-in de aeropuerto
- 🎓 Tablets educativas para exámenes
- 🏭 Control industrial
- 🎨 Señalización digital

---

## 📱 Métodos de Configuración de Paquetes

### `setLockTaskPackages(ComponentName admin, String[] packages)`

**Propósito**: Define la lista de aplicaciones que están autorizadas para ejecutarse en modo Lock Task. Solo estas apps pueden iniciar Lock Task Mode.

**Casos de Uso**:
- **POS dedicado**: Solo app de punto de venta
- **Quiosco multi-app**: Varias apps relacionadas (catálogo + checkout + ayuda)
- **Dispositivos educativos**: Apps de examen aprobadas
- **Check-in**: App de registro + cámara + escáner

**Comportamiento**:
- Apps en la lista pueden iniciar Lock Task llamando a `startLockTask()`
- Apps fuera de la lista no pueden iniciar Lock Task
- Si estás en Lock Task, solo puedes cambiar a otras apps de la lista
- Usuario no puede salir a apps no autorizadas

**Ejemplo de Uso - POS Simple**:
```kotlin
// Configurar modo kiosko para punto de venta
val appsPermitidas = arrayOf(
    "com.empresa.pos",           // App principal de POS
    "com.android.camera2",       // Cámara para escanear códigos
    "com.empresa.inventario"     // Consultar inventario
)

dpm.setLockTaskPackages(admin, appsPermitidas)
Log.i("LockTask", "Modo kiosko configurado con ${appsPermitidas.size} apps")
```

**Ejemplo de Uso - Quiosco Informativo**:
```kotlin
// Quiosco de información con navegador restringido
val appsQuiosco = arrayOf(
    "com.empresa.quiosco",       // App principal
    "com.android.chrome",        // Navegador para contenido web
    "com.adobe.reader"           // Ver PDFs informativos
)

dpm.setLockTaskPackages(admin, appsQuiosco)
```

**Ejemplo de Uso - Dispositivo Educativo**:
```kotlin
// Tablet para exámenes - Solo apps de evaluación
val appsExamen = arrayOf(
    "com.escuela.examen",        // App de examen
    "com.wolfram.android.alpha", // Calculadora científica
    "com.android.calculator2"    // Calculadora básica
)

dpm.setLockTaskPackages(admin, appsExamen)
```

**Ejemplo de Uso - Desactivar Lock Task**:
```kotlin
// Remover todas las apps autorizadas
dpm.setLockTaskPackages(admin, emptyArray())
// Ahora ninguna app puede iniciar Lock Task
```

**⚠️ Importante**:
- El array NO puede contener apps que no estén instaladas (se ignoran)
- Si incluyes el launcher, el usuario puede acceder al home en Lock Task
- Las apps incluidas deben llamar explícitamente a `startLockTask()` para activarlo
- Solo Device Owner o Profile Owner pueden configurar esto

---

### `getLockTaskPackages(ComponentName admin)`

**Propósito**: Obtiene la lista actual de aplicaciones autorizadas para Lock Task Mode.

**Retorna**: `String[]` con los package names autorizados.

**Casos de Uso**:
- Auditoría de configuración actual
- Verificar que la configuración se aplicó correctamente
- Sincronizar con sistema MDM
- Debugging de problemas de Lock Task

```kotlin
fun auditarLockTaskConfig(): String {
    val appsAutorizadas = dpm.getLockTaskPackages(admin)
    
    return buildString {
        append("🔒 CONFIGURACIÓN LOCK TASK MODE\n")
        append("═══════════════════════════════════\n")
        
        if (appsAutorizadas.isEmpty()) {
            append("❌ Lock Task Mode: DESHABILITADO\n")
            append("   Ninguna app autorizada\n")
        } else {
            append("✅ Lock Task Mode: CONFIGURADO\n")
            append("   Apps autorizadas: ${appsAutorizadas.size}\n\n")
            
            appsAutorizadas.forEachIndexed { index, packageName ->
                append("${index + 1}. $packageName\n")
                
                // Verificar si la app está instalada
                val instalada = estaInstalada(packageName)
                if (!instalada) {
                    append("   ⚠️ APP NO INSTALADA\n")
                }
            }
        }
    }
}

fun estaInstalada(packageName: String): Boolean {
    return try {
        context.packageManager.getPackageInfo(packageName, 0)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }
}
```

---

### `isLockTaskPermitted(String packageName)`

**Propósito**: Verifica si una aplicación específica tiene permiso para ejecutarse en Lock Task Mode.

**Retorna**:
- `true`: La app puede iniciar Lock Task
- `false`: La app no está autorizada

**Casos de Uso**:
- Validar antes de llamar a `startLockTask()`
- Debugging de problemas de permisos
- UI condicional según capacidades
- Verificación en tiempo de ejecución

```kotlin
fun verificarPermisoLockTask(packageName: String): Boolean {
    val permitido = dpm.isLockTaskPermitted(packageName)
    
    if (permitido) {
        Log.i("LockTask", "✅ $packageName puede iniciar Lock Task")
    } else {
        Log.w("LockTask", "❌ $packageName NO puede iniciar Lock Task")
    }
    
    return permitido
}

// Uso antes de iniciar Lock Task
fun iniciarModoKiosko() {
    val miPackage = context.packageName
    
    if (dpm.isLockTaskPermitted(miPackage)) {
        (context as Activity).startLockTask()
        Log.i("Kiosko", "Modo kiosko iniciado")
    } else {
        Log.e("Kiosko", "App no autorizada para Lock Task")
        mostrarError("Dispositivo no configurado para modo kiosko")
    }
}
```

---

## ⚙️ Métodos de Configuración de Features

### `setLockTaskFeatures(ComponentName admin, int flags)`

**Propósito**: Controla qué características del sistema están disponibles mientras el dispositivo está en Lock Task Mode. Permite personalizar el nivel de restricción del modo kiosko.

**Features/Flags Disponibles**:

| Flag | Valor | Qué Permite | Uso Típico |
|------|-------|-------------|------------|
| `LOCK_TASK_FEATURE_NONE` | 0 | Nada (máxima restricción) | Kioscos ultra-restrictivos |
| `LOCK_TASK_FEATURE_SYSTEM_INFO` | 1 | Ver hora, batería, señal en status bar | Mayoría de kioscos |
| `LOCK_TASK_FEATURE_NOTIFICATIONS` | 2 | Ver notificaciones | Dispositivos que necesitan notificaciones |
| `LOCK_TASK_FEATURE_HOME` | 4 | Botón Home funcional | Kioscos multi-app con launcher |
| `LOCK_TASK_FEATURE_OVERVIEW` | 8 | Botón Recent Apps funcional | Kioscos multi-app |
| `LOCK_TASK_FEATURE_GLOBAL_ACTIONS` | 16 | Power menu (apagar, reiniciar) | Administradores necesitan apagar |
| `LOCK_TASK_FEATURE_KEYGUARD` | 32 | Pantalla de bloqueo funciona | Dispositivos con múltiples usuarios |

**Casos de Uso**:

**Kiosko Ultra-Restrictivo** (Señalización Digital):
```kotlin
// Sin ninguna feature - máxima restricción
dpm.setLockTaskFeatures(admin, DevicePolicyManager.LOCK_TASK_FEATURE_NONE)

// Resultado:
// ❌ Sin status bar
// ❌ Sin notificaciones
// ❌ Sin botón home
// ❌ Sin botón recent
// ❌ No se puede apagar
// ❌ Sin pantalla de bloqueo
```

**Kiosko Estándar** (POS, Check-in):
```kotlin
// Mostrar información del sistema solamente
dpm.setLockTaskFeatures(admin, DevicePolicyManager.LOCK_TASK_FEATURE_SYSTEM_INFO)

// Resultado:
// ✅ Status bar con hora, batería, señal
// ❌ Sin notificaciones
// ❌ Sin botón home
// ❌ Sin botón recent
// ❌ No se puede apagar
```

**Kiosko Multi-App** (Educativo, Corporativo):
```kotlin
// Permitir navegación entre apps autorizadas
val features = DevicePolicyManager.LOCK_TASK_FEATURE_SYSTEM_INFO or
               DevicePolicyManager.LOCK_TASK_FEATURE_HOME or
               DevicePolicyManager.LOCK_TASK_FEATURE_OVERVIEW

dpm.setLockTaskFeatures(admin, features)

// Resultado:
// ✅ Status bar con información
// ✅ Botón Home funcional (a launcher incluido en setLockTaskPackages)
// ✅ Botón Recent Apps funcional
// ❌ Sin notificaciones
// ❌ No se puede apagar
```

**Kiosko con Notificaciones** (Dispositivos de Trabajo):
```kotlin
// Permitir ver notificaciones importantes
val features = DevicePolicyManager.LOCK_TASK_FEATURE_SYSTEM_INFO or
               DevicePolicyManager.LOCK_TASK_FEATURE_NOTIFICATIONS or
               DevicePolicyManager.LOCK_TASK_FEATURE_GLOBAL_ACTIONS

dpm.setLockTaskFeatures(admin, features)

// Resultado:
// ✅ Status bar completa
// ✅ Notificaciones visibles
// ✅ Power menu disponible (apagar/reiniciar)
// ❌ Sin botón home
// ❌ Sin botón recent
```

**Kiosko Completo** (Dispositivos Compartidos):
```kotlin
// Todas las features habilitadas
val features = DevicePolicyManager.LOCK_TASK_FEATURE_SYSTEM_INFO or
               DevicePolicyManager.LOCK_TASK_FEATURE_NOTIFICATIONS or
               DevicePolicyManager.LOCK_TASK_FEATURE_HOME or
               DevicePolicyManager.LOCK_TASK_FEATURE_OVERVIEW or
               DevicePolicyManager.LOCK_TASK_FEATURE_GLOBAL_ACTIONS or
               DevicePolicyManager.LOCK_TASK_FEATURE_KEYGUARD

dpm.setLockTaskFeatures(admin, features)

// Resultado:
// ✅ Experiencia casi normal de Android
// ✅ Pero limitado a apps autorizadas
// ✅ Usuario puede navegar y ver todo
// ✅ Útil para dispositivos compartidos con múltiples perfiles
```

**Configuración por Escenario**:
```kotlin
fun configurarPorTipoKiosko(tipo: TipoKiosko) {
    val features = when (tipo) {
        TipoKiosko.SENALIZACION_DIGITAL -> {
            DevicePolicyManager.LOCK_TASK_FEATURE_NONE
        }
        
        TipoKiosko.PUNTO_VENTA -> {
            DevicePolicyManager.LOCK_TASK_FEATURE_SYSTEM_INFO
        }
        
        TipoKiosko.CHECK_IN_AEROPUERTO -> {
            DevicePolicyManager.LOCK_TASK_FEATURE_SYSTEM_INFO or
            DevicePolicyManager.LOCK_TASK_FEATURE_HOME
        }
        
        TipoKiosko.TABLET_EDUCATIVA -> {
            DevicePolicyManager.LOCK_TASK_FEATURE_SYSTEM_INFO or
            DevicePolicyManager.LOCK_TASK_FEATURE_HOME or
            DevicePolicyManager.LOCK_TASK_FEATURE_OVERVIEW or
            DevicePolicyManager.LOCK_TASK_FEATURE_GLOBAL_ACTIONS
        }
        
        TipoKiosko.DISPOSITIVO_TRABAJO -> {
            DevicePolicyManager.LOCK_TASK_FEATURE_SYSTEM_INFO or
            DevicePolicyManager.LOCK_TASK_FEATURE_NOTIFICATIONS or
            DevicePolicyManager.LOCK_TASK_FEATURE_GLOBAL_ACTIONS
        }
    }
    
    dpm.setLockTaskFeatures(admin, features)
    Log.i("LockTask", "Features configuradas para: $tipo")
}

enum class TipoKiosko {
    SENALIZACION_DIGITAL,
    PUNTO_VENTA,
    CHECK_IN_AEROPUERTO,
    TABLET_EDUCATIVA,
    DISPOSITIVO_TRABAJO
}
```

---

### `getLockTaskFeatures(ComponentName admin)`

**Propósito**: Obtiene las features actualmente habilitadas para Lock Task Mode.

**Retorna**: `int` (bitmask) con las features habilitadas.

**Casos de Uso**:
- Verificar configuración actual
- Auditoría de políticas
- Debugging de comportamiento
- Sincronización con MDM

```kotlin
fun auditarLockTaskFeatures(): String {
    val features = dpm.getLockTaskFeatures(admin)
    
    return buildString {
        append("⚙️ FEATURES DE LOCK TASK MODE\n")
        append("═══════════════════════════════\n\n")
        
        if (features == DevicePolicyManager.LOCK_TASK_FEATURE_NONE) {
            append("🔒 MODO ULTRA-RESTRICTIVO\n")
            append("   Sin ninguna feature habilitada\n")
        } else {
            append("Features habilitadas:\n\n")
            
            if (tieneFeature(features, DevicePolicyManager.LOCK_TASK_FEATURE_SYSTEM_INFO)) {
                append("✅ SYSTEM_INFO - Status bar con información del sistema\n")
            }
            if (tieneFeature(features, DevicePolicyManager.LOCK_TASK_FEATURE_NOTIFICATIONS)) {
                append("✅ NOTIFICATIONS - Notificaciones visibles\n")
            }
            if (tieneFeature(features, DevicePolicyManager.LOCK_TASK_FEATURE_HOME)) {
                append("✅ HOME - Botón Home funcional\n")
            }
            if (tieneFeature(features, DevicePolicyManager.LOCK_TASK_FEATURE_OVERVIEW)) {
                append("✅ OVERVIEW - Botón Recent Apps funcional\n")
            }
            if (tieneFeature(features, DevicePolicyManager.LOCK_TASK_FEATURE_GLOBAL_ACTIONS)) {
                append("✅ GLOBAL_ACTIONS - Power menu disponible\n")
            }
            if (tieneFeature(features, DevicePolicyManager.LOCK_TASK_FEATURE_KEYGUARD)) {
                append("✅ KEYGUARD - Pantalla de bloqueo funcional\n")
            }
        }
    }
}

fun tieneFeature(features: Int, feature: Int): Boolean {
    return (features and feature) != 0
}
```

---

## 🚀 Métodos de Inicio y Detención (Nivel de Actividad)

### `startLockTask()` (Activity)

**Propósito**: Inicia Lock Task Mode desde una Activity. La Activity que llama a este método queda bloqueada en pantalla.

**⚠️ Importante**: Este método se llama desde una `Activity`, NO desde `DevicePolicyManager`.

**Requisitos**:
- La app debe estar en la lista de `setLockTaskPackages()`
- Debe llamarse desde el thread principal (UI thread)
- Solo funciona en Activities, no en Services o BroadcastReceivers

**Casos de Uso**:
- Iniciar modo kiosko al abrir la app
- Activar kiosko tras autenticación de administrador
- Bloquear dispositivo a app específica temporalmente

**Ejemplo de Uso - Iniciar al Abrir App**:
```kotlin
class KioskoActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kiosko)
        
        // Verificar si podemos iniciar Lock Task
        val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        
        if (dpm.isLockTaskPermitted(packageName)) {
            // Iniciar modo kiosko
            startLockTask()
            Log.i("Kiosko", "Modo kiosko iniciado")
            
            // Opcional: Ocultar action bar y navigation bar
            ocultarBarras()
        } else {
            Log.w("Kiosko", "Dispositivo no configurado como kiosko")
            // Mostrar mensaje o solicitar configuración
        }
    }
    
    private fun ocultarBarras() {
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        )
    }
}
```

**Ejemplo de Uso - Iniciar tras Autenticación**:
```kotlin
class MainActivity : AppCompatActivity() {
    
    fun onAdminAutenticado() {
        AlertDialog.Builder(this)
            .setTitle("Activar Modo Kiosko")
            .setMessage("¿Desea activar el modo kiosko?")
            .setPositiveButton("Activar") { _, _ ->
                activarModoKiosko()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    private fun activarModoKiosko() {
        val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        
        if (dpm.isLockTaskPermitted(packageName)) {
            startLockTask()
            
            // UI feedback
            Toast.makeText(this, "Modo kiosko activado", Toast.LENGTH_SHORT).show()
            
            // Cambiar UI para modo kiosko
            mostrarUIKiosko()
        }
    }
}
```

**Ejemplo de Uso - Kiosko Temporal**:
```kotlin
class TransaccionActivity : AppCompatActivity() {
    
    override fun onResume() {
        super.onResume()
        
        // Activar kiosko durante transacción
        if (hayTransaccionEnCurso()) {
            startLockTask()
        }
    }
    
    fun onTransaccionCompletada() {
        // Salir de kiosko al finalizar
        stopLockTask()
        finish()
    }
}
```

**Comportamiento**:
- La Activity queda fijada en pantalla
- Botones Home/Recent dejan de funcionar (según `setLockTaskFeatures()`)
- Solo se puede salir llamando a `stopLockTask()` o con código de salida

---

### `stopLockTask()` (Activity)

**Propósito**: Detiene Lock Task Mode y permite al usuario salir de la app bloqueada.

**⚠️ Importante**: También se llama desde una `Activity`, NO desde `DevicePolicyManager`.

**Casos de Uso**:
- Salir de modo kiosko tras autenticación de administrador
- Finalizar sesión de kiosko
- Desactivar kiosko tras completar tarea

**Ejemplo de Uso - Salir con Código**:
```kotlin
class KioskoActivity : AppCompatActivity() {
    
    fun onBotonSalirPresionado() {
        // Solicitar código de administrador
        solicitarCodigoAdmin { codigoValido ->
            if (codigoValido) {
                salirDeModoKiosko()
            } else {
                Toast.makeText(this, "Código incorrecto", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun salirDeModoKiosko() {
        try {
            stopLockTask()
            Log.i("Kiosko", "Modo kiosko desactivado")
            
            // Opcional: Cerrar la app
            finish()
        } catch (e: IllegalStateException) {
            Log.e("Kiosko", "Error al salir de Lock Task: ${e.message}")
        }
    }
    
    private fun solicitarCodigoAdmin(callback: (Boolean) -> Unit) {
        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
        
        AlertDialog.Builder(this)
            .setTitle("Código de Administrador")
            .setMessage("Ingrese código para salir del modo kiosko")
            .setView(input)
            .setPositiveButton("OK") { _, _ ->
                val codigo = input.text.toString()
                val valido = verificarCodigoAdmin(codigo)
                callback(valido)
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.cancel()
                callback(false)
            }
            .show()
    }
}
```

**Ejemplo de Uso - Salir tras Inactividad**:
```kotlin
class KioskoActivity : AppCompatActivity() {
    
    private var ultimaActividad: Long = System.currentTimeMillis()
    private val handler = Handler(Looper.getMainLooper())
    
    override fun onUserInteraction() {
        super.onUserInteraction()
        ultimaActividad = System.currentTimeMillis()
    }
    
    private val verificadorInactividad = object : Runnable {
        override fun run() {
            val tiempoInactivo = System.currentTimeMillis() - ultimaActividad
            val TIMEOUT = 5 * 60 * 1000L // 5 minutos
            
            if (tiempoInactivo >= TIMEOUT) {
                // Salir de kiosko por inactividad
                Log.i("Kiosko", "Saliendo por inactividad")
                stopLockTask()
                finish()
            } else {
                // Verificar nuevamente en 10 segundos
                handler.postDelayed(this, 10000)
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        handler.post(verificadorInactividad)
    }
    
    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(verificadorInactividad)
    }
}
```

---

## 🎯 Casos de Uso por Escenario

### 🏪 Punto de Venta (POS)
```kotlin
// Configuración de Lock Task
val appsPermitidas = arrayOf(
    "com.empresa.pos",
    "com.android.camera2"  // Para escanear códigos
)
dpm.setLockTaskPackages(admin, appsPermitidas)

// Features: Solo información del sistema
dpm.setLockTaskFeatures(admin, 
    DevicePolicyManager.LOCK_TASK_FEATURE_SYSTEM_INFO)

// En la Activity de POS
class POSActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pos)
        
        // Iniciar kiosko automáticamente
        startLockTask()
    }
}
```

---

### 📊 Quiosco Informativo
```kotlin
// Solo una app - display estático
val appsPermitidas = arrayOf("com.empresa.quiosco_info")
dpm.setLockTaskPackages(admin, appsPermitidas)

// Sin ninguna feature - ultra-restrictivo
dpm.setLockTaskFeatures(admin, 
    DevicePolicyManager.LOCK_TASK_FEATURE_NONE)

// Activity a pantalla completa
class QuioscoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Pantalla completa inmersiva
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        )
        
        setContentView(R.layout.activity_quiosco)
        startLockTask()
    }
}
```

---

### 🎓 Tablet Educativa para Exámenes
```kotlin
// Múltiples apps educativas permitidas
val appsPermitidas = arrayOf(
    "com.escuela.examen",
    "com.escuela.launcher",  // Launcher personalizado
    "com.android.calculator2",
    "com.wolfram.android.alpha"
)
dpm.setLockTaskPackages(admin, appsPermitidas)

// Permitir navegación entre apps
val features = DevicePolicyManager.LOCK_TASK_FEATURE_SYSTEM_INFO or
               DevicePolicyManager.LOCK_TASK_FEATURE_HOME or
               DevicePolicyManager.LOCK_TASK_FEATURE_OVERVIEW

dpm.setLockTaskFeatures(admin, features)

// El launcher inicia Lock Task
class ExamenLauncherActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)
        
        // Iniciar kiosko al comenzar examen
        if (esHoraDeExamen()) {
            startLockTask()
        }
    }
}
```

---

## ⚠️ Consideraciones Importantes

### Requisitos Técnicos

1. **Device Owner obligatorio**: Lock Task Mode requiere que tu app sea Device Owner

2. **Configuración previa**: Debes llamar a `setLockTaskPackages()` ANTES de intentar `startLockTask()`

3. **Context correcto**: `startLockTask()` y `stopLockTask()` solo funcionan en Activities

### Limitaciones

1. **Una Activity a la vez**: Solo una Activity puede estar en Lock Task simultáneamente

2. **No todos los fabricantes**: Algunos OEMs (Samsung, Huawei) tienen implementaciones propias

3. **Android 5.0+**: Lock Task Mode requiere API 21 o superior

4. **Permisos del sistema**: Algunas features pueden requerir permisos adicionales
