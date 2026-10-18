package com.cappucare.app

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cappucare.app.model.Movimiento
import com.cappucare.app.model.Producto
import com.cappucare.app.ui.theme.MyApplicationTheme
import com.cappucare.app.viewmodel.InventarioViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel = ViewModelProvider(this)[InventarioViewModel::class.java]
        crearCanalNotificaciones()
        setContent {
            MyApplicationTheme {
                // Define el color de texto predeterminado para todas las pantallas.
                // Evita que los textos hereden negro sobre el fondo oscuro.
                Surface(
                    modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.onBackground
                ) {
                    CappucareApp(viewModel)
                }
            }
        }
    }
}

private const val CANAL_STOCK_BAJO = "cappucare_stock_bajo"

private fun MainActivity.crearCanalNotificaciones() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val canal = NotificationChannel(
            CANAL_STOCK_BAJO,
            "Alertas de inventario",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Avisos cuando hay productos bajo su nivel mínimo"
        }
        getSystemService(NotificationManager::class.java)
            .createNotificationChannel(canal)
    }
}

private fun enviarNotificacionStockBajo(context: Context, cantidad: Int) {
    if (Build.VERSION.SDK_INT >= 33 &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
    ) return

    val notificacion = NotificationCompat.Builder(context, CANAL_STOCK_BAJO)
        .setSmallIcon(android.R.drawable.ic_dialog_alert)
        .setContentTitle("Cappucare Stock: revisar inventario")
        .setContentText("Hay $cantidad producto(s) bajo su nivel mínimo.")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)
        .build()

    context.getSystemService(NotificationManager::class.java).notify(1001, notificacion)
}

private sealed class Screen {
    object Login : Screen()
    object Inicio : Screen()
    object Productos : Screen()
    object Movimientos : Screen()
    object RegistrarIngreso : Screen()
    object RegistrarSalida : Screen()
    object Perfil : Screen()
}

@Composable
fun CappucareApp(viewModel: InventarioViewModel) {
    var pantalla by remember { mutableStateOf<Screen>(Screen.Login) }
    var usuarioActivo by remember { mutableStateOf("") }
    val productos by viewModel.productos.collectAsStateWithLifecycle()
    val movimientos by viewModel.movimientos.collectAsStateWithLifecycle()
    val mensaje by viewModel.mensaje.collectAsStateWithLifecycle()
    val stockBajo by viewModel.cantidadStockBajo.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current
    val solicitarPermiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) solicitarPermiso.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    LaunchedEffect(mensaje, stockBajo) {
        if (mensaje == "Salida registrada correctamente" && stockBajo > 0) {
            enviarNotificacionStockBajo(context, stockBajo)
        }
    }

    when (pantalla) {
        Screen.Login -> LoginScreen { usuario ->
            usuarioActivo = usuario
            pantalla = Screen.Inicio
        }
        Screen.Inicio -> InicioScreen(
            stockBajo = stockBajo,
            perfil = if (usuarioActivo == "encargado@cappucare.cl") "Personal encargado" else "Otros usuarios autorizados",
            totalProductos = productos.size,
            totalMovimientos = movimientos.size,
            onProductos = { pantalla = Screen.Productos },
            onMovimientos = { pantalla = Screen.Movimientos },
            onPerfil = { pantalla = Screen.Perfil },
            onCerrarSesion = {
                viewModel.limpiarMensaje()
                usuarioActivo = ""
                pantalla = Screen.Login
            }
        )
        Screen.Productos -> ProductosScreen(
            productos = productos,
            onVolver = { pantalla = Screen.Inicio },
            onInicio = { pantalla = Screen.Inicio },
            onMovimientos = { pantalla = Screen.Movimientos },
            onPerfil = { pantalla = Screen.Perfil }
        )
        Screen.Perfil -> PerfilScreen(
            correo = usuarioActivo,
            perfil = if (usuarioActivo == "encargado@cappucare.cl") "Personal encargado" else "Usuario autorizado",
            onInicio = { pantalla = Screen.Inicio },
            onProductos = { pantalla = Screen.Productos },
            onMovimientos = { pantalla = Screen.Movimientos },
            onCerrarSesion = {
                viewModel.limpiarMensaje()
                usuarioActivo = ""
                pantalla = Screen.Login
            }
        )
        Screen.Movimientos -> MovimientosScreen(
            movimientos = movimientos,
            onVolver = { pantalla = Screen.Inicio },
            onInicio = { pantalla = Screen.Inicio },
            onProductos = { pantalla = Screen.Productos },
            onPerfil = { pantalla = Screen.Perfil },
            onRegistrarIngreso = {
                viewModel.limpiarMensaje()
                pantalla = Screen.RegistrarIngreso
            },
            onRegistrarSalida = {
                viewModel.limpiarMensaje()
                pantalla = Screen.RegistrarSalida
            }
        )
        Screen.RegistrarIngreso -> RegistrarIngresoScreen(
            mensaje = mensaje,
            responsableActivo = usuarioActivo,
            onVolver = {
                viewModel.limpiarMensaje()
                pantalla = Screen.Movimientos
            },
            onGuardar = { codigo, cantidad, _ ->
                viewModel.registrarIngreso(codigo, cantidad, usuarioActivo)
            }
        )
        Screen.RegistrarSalida -> RegistrarSalidaScreen(
            mensaje = mensaje,
            responsableActivo = usuarioActivo,
            onVolver = {
                viewModel.limpiarMensaje()
                pantalla = Screen.Movimientos
            },
            onGuardar = { codigo, cantidad, motivo, _ ->
                viewModel.registrarSalida(codigo, cantidad, motivo, usuarioActivo)
            }
        )
    }
}

@Composable
private fun BannerMedico(titulo: String, acento: String, subtitulo: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(
            Modifier.fillMaxWidth()
                .background(androidx.compose.ui.graphics.Brush.linearGradient(listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.background)))
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.size(56.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = .16f), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                    Text("✚", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                }
                Text(titulo, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(acento, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(subtitulo, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            }
            Text("✚", modifier = Modifier.align(Alignment.TopEnd).padding(4.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = .35f), style = MaterialTheme.typography.displayMedium)
        }
    }
}

@Composable
private fun BannerPanel(titulo: String, subtitulo: String) {
    Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(52.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = .14f), RoundedCornerShape(15.dp)), contentAlignment = Alignment.Center) {
                Text("✚", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(titulo, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(subtitulo, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun NavegacionCappucare(
    seleccion: String,
    onInicio: () -> Unit,
    onProductos: () -> Unit,
    onMovimientos: () -> Unit,
    onPerfil: () -> Unit
) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        NavigationBarItem(selected = seleccion == "Inicio", onClick = onInicio, icon = { Text("⌂", style = MaterialTheme.typography.titleLarge) }, label = { Text("Inicio") })
        NavigationBarItem(selected = seleccion == "Productos", onClick = onProductos, icon = { Text("▦", style = MaterialTheme.typography.titleLarge) }, label = { Text("Productos") })
        NavigationBarItem(selected = seleccion == "Movimientos", onClick = onMovimientos, icon = { Text("⇄", style = MaterialTheme.typography.titleLarge) }, label = { Text("Movimientos") })
        NavigationBarItem(selected = seleccion == "Perfil", onClick = onPerfil, icon = { Text("♙", style = MaterialTheme.typography.titleLarge) }, label = { Text("Perfil") })
    }
}

@Composable
private fun PerfilScreen(
    correo: String,
    perfil: String,
    onInicio: () -> Unit,
    onProductos: () -> Unit,
    onMovimientos: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { NavegacionCappucare("Perfil", onInicio, onProductos, onMovimientos, {}) }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            MarcaCappucare()
            BannerPanel("Mi perfil", "Información de usuario y acceso.")
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Box(Modifier.size(58.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = .15f), CircleShape), contentAlignment = Alignment.Center) {
                        Text("♙", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.headlineLarge)
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(perfil, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text(correo, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                        EtiquetaEstado("Acceso autorizado", false)
                    }
                }
            }
            Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Preferencias", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("Notificaciones de inventario activadas", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = .4f))
                    Text("Cappucare Stock · Gestión de inventario médico", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
            }
            OutlinedButton(onClick = onCerrarSesion, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(14.dp)) { Text("Cerrar sesión", color = MaterialTheme.colorScheme.error) }
        }
    }
}

@Composable
private fun HeroMedico(
    titulo: String,
    descripcion: String,
    simbolo: String,
    compacto: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(if (compacto) 18.dp else 24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(if (compacto) 14.dp else 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier.size(if (compacto) 48.dp else 58.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    simbolo,
                    color = MaterialTheme.colorScheme.primary,
                    style = if (compacto) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    titulo,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = if (compacto) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(descripcion, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
            if (!compacto) Text("C+", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun MarcaCappucare() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier.size(44.dp)
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("C+", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
        }
        Column {
            Text("Cappucare", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("STOCK MANAGEMENT", style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun LoginScreen(onLogin: (String) -> Unit) {
    var usuario by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var mostrarPassword by remember { mutableStateOf(false) }

    val fondo = Color(0xFF061626)
    val panel = Color(0xE60A1B2D)
    val texto = Color(0xFFF4F8FC)
    val secundario = Color(0xFFB7C7D8)
    val verde = Color(0xFF39D6AD)

    Box(modifier = Modifier.fillMaxSize().background(fondo)) {
        // La imagen ocupa la zona superior completa para que se vea el rostro del médico.
        Image(
            painter = painterResource(id = R.drawable.cappucare_login_hero),
            contentDescription = "Médico utilizando una tablet en un entorno clínico",
            modifier = Modifier.fillMaxWidth().height(500.dp).align(Alignment.TopCenter).offset(y = 18.dp),
            contentScale = ContentScale.Crop,
            alignment = Alignment.TopCenter
        )
        Box(
            modifier = Modifier.fillMaxWidth().height(530.dp).align(Alignment.TopCenter).offset(y = 18.dp).background(
                Brush.verticalGradient(
                    0.0f to Color(0x22051626),
                    0.40f to Color(0x55051626),
                    0.72f to Color(0xCC061626),
                    1.0f to fondo
                )
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                // Mantiene logo, títulos y accesos dentro del área segura del teléfono.
                // La imagen de fondo puede extenderse detrás de las barras del sistema.
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 20.dp)
                .padding(top = 63.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.Top
        ) {
            // Marca dentro del área segura: nunca queda debajo de la barra de estado.
            Row(
                modifier = Modifier.fillMaxWidth().heightIn(min = 70.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    Modifier.size(62.dp).background(verde, RoundedCornerShape(19.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("C+", color = Color(0xFF062033), fontSize = 29.sp, fontWeight = FontWeight.Black)
                }
                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text("Cappucare", color = texto, fontSize = 25.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
                    Text("STOCK MANAGEMENT", color = secundario, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.15.sp, maxLines = 1)
                }
            }

            // Desplaza hacia abajo el bloque principal desde el título, sin mover el logo.
            Spacer(Modifier.height(72.dp))
            Text("Control de", color = texto, fontSize = 30.sp, lineHeight = 35.sp, fontWeight = FontWeight.ExtraBold)
            Text("inventario médico", color = verde, fontSize = 30.sp, lineHeight = 36.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(8.dp))
            Text(
                "Accede para administrar productos y movimientos de stock.",
                color = secundario,
                fontSize = 16.sp,
                lineHeight = 23.sp
            )
            // Más aire antes del formulario: el formulario y los accesos inferiores
            // bajan ligeramente sin mover el encabezado ni los títulos.
            Spacer(Modifier.height(48.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = panel),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x222B536A))
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 17.dp, vertical = 17.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Iniciar sesión", color = texto, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = usuario,
                        onValueChange = { usuario = it; error = "" },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 60.dp),
                        singleLine = true,
                        placeholder = { Text("Correo electrónico", color = secundario) },
                        leadingIcon = { Text("✉", color = secundario, fontSize = 22.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(22.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF3B6075), unfocusedBorderColor = Color(0xFF263D52),
                            focusedTextColor = texto, unfocusedTextColor = texto, cursorColor = verde,
                            focusedContainerColor = Color(0x22051424), unfocusedContainerColor = Color(0x22051424)
                        )
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; error = "" },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 60.dp),
                        singleLine = true,
                        placeholder = { Text("Contraseña", color = secundario) },
                        leadingIcon = { Text("♙", color = secundario, fontSize = 23.sp) },
                        trailingIcon = {
                            Text(
                                if (mostrarPassword) "◉" else "◉̸",
                                color = secundario,
                                modifier = Modifier.clickable { mostrarPassword = !mostrarPassword }.padding(horizontal = 12.dp),
                                fontSize = 20.sp
                            )
                        },
                        visualTransformation = if (mostrarPassword) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(22.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF3B6075), unfocusedBorderColor = Color(0xFF263D52),
                            focusedTextColor = texto, unfocusedTextColor = texto, cursorColor = verde,
                            focusedContainerColor = Color(0x22051424), unfocusedContainerColor = Color(0x22051424)
                        )
                    )
                    AnimatedVisibility(visible = error.isNotEmpty(), enter = fadeIn(), exit = fadeOut()) {
                        Text(error, color = Color(0xFFFF7A88), style = MaterialTheme.typography.bodyMedium)
                    }
                    Button(
                        onClick = {
                            when {
                                usuario.isBlank() -> error = "Ingrese su correo electrónico."
                                password.isBlank() -> error = "Ingrese su contraseña."
                                usuario.equals("encargado@cappucare.cl", true) && password == "encargado123" -> { error = ""; onLogin("encargado@cappucare.cl") }
                                usuario.equals("ventas@cappucare.cl", true) && password == "ventas123" -> { error = ""; onLogin("ventas@cappucare.cl") }
                                else -> error = "Correo o contraseña incorrectos."
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(62.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = verde, contentColor = Color(0xFF062033))
                    ) {
                        Text(
                            text = "Ingresar",
                            fontSize = 21.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(Modifier.height(28.dp))
            Row(
                Modifier.fillMaxWidth().padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.weight(1f)) {
                    Text("♢", color = verde, fontSize = 22.sp)
                    Text("Acceso\nseguro", color = secundario, textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontSize = 15.sp, lineHeight = 21.sp)
                }
                Box(Modifier.width(1.dp).height(55.dp).background(Color(0xFF294055)))
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.weight(1f)) {
                    Text("⬡", color = verde, fontSize = 22.sp)
                    Text("Gestión de\ninventario", color = secundario, textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontSize = 15.sp, lineHeight = 21.sp)
                }
                Box(Modifier.width(1.dp).height(55.dp).background(Color(0xFF294055)))
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.weight(1f)) {
                    Text("♧", color = verde, fontSize = 22.sp)
                    Text("Control de\nmovimientos", color = secundario, textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontSize = 15.sp, lineHeight = 21.sp)
                }
            }
        }
    }
}
@Composable
fun InicioScreen(
    stockBajo: Int,
    perfil: String,
    totalProductos: Int,
    totalMovimientos: Int,
    onProductos: () -> Unit,
    onMovimientos: () -> Unit,
    onPerfil: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { NavegacionCappucare("Inicio", onInicio = {}, onProductos = onProductos, onMovimientos = onMovimientos, onPerfil = onPerfil) }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                MarcaCappucare()
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surface, modifier = Modifier.size(42.dp)) {
                    Box(contentAlignment = Alignment.Center) { Text("♙", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleLarge) }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("Panel", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
                Text("principal", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text("Gestión de inventario médico", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                IndicadorCard("Productos", totalProductos.toString(), "en inventario", Modifier.weight(1f))
                IndicadorCard("Movimientos", totalMovimientos.toString(), "registrados", Modifier.weight(1f))
            }
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = if (stockBajo > 0) MaterialTheme.colorScheme.errorContainer.copy(alpha = .28f) else MaterialTheme.colorScheme.surface)) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.size(44.dp).background(MaterialTheme.colorScheme.error.copy(alpha = .14f), RoundedCornerShape(13.dp)), contentAlignment = Alignment.Center) {
                        Text("⚠", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.headlineSmall)
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text("Stock bajo", fontWeight = FontWeight.SemiBold)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(stockBajo.toString(), color = if (stockBajo > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                            Text(if (stockBajo == 1) "producto por revisar" else "productos por revisar", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Text("›", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.headlineLarge)
                }
            }
            Text("Accesos rápidos", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                AccionCard("Productos", "Ver y gestionar", "▦", onProductos, Modifier.weight(1f))
                AccionCard("Movimientos", "Historial y registros", "⇄", onMovimientos, Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                AccionCard("Mi perfil", "Datos de acceso", "♙", onPerfil, Modifier.weight(1f))
                AccionCard("Cerrar sesión", "Salir de la cuenta", "↪", onCerrarSesion, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun IndicadorCard(titulo: String, valor: String, detalle: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(titulo, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            Text(valor, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(detalle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun AccionCard(titulo: String, detalle: String, numero: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(onClick = onClick, modifier = modifier.heightIn(min = 104.dp), shape = RoundedCornerShape(17.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.fillMaxWidth().padding(13.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.size(40.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = .14f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                Text(numero, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            Text(titulo, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(detalle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductosScreen(
    productos: List<Producto>,
    onVolver: () -> Unit,
    onInicio: () -> Unit,
    onMovimientos: () -> Unit,
    onPerfil: () -> Unit
) {
    var busqueda by remember { mutableStateOf("") }
    var categoriaActiva by remember { mutableStateOf("Todos") }
    val categorias = listOf("Todos", "Medicamentos", "Insumos", "Otro")
    val filtrados = productos.filter {
        (it.nombre.contains(busqueda, true) || it.codigo.contains(busqueda, true)) &&
            (categoriaActiva == "Todos" || (categoriaActiva == "Medicamentos" && it.categoria.contains("med", true)) || (categoriaActiva == "Insumos" && it.categoria.contains("insum", true)) || (categoriaActiva == "Otro" && !it.categoria.contains("med", true) && !it.categoria.contains("insum", true)))
    }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { BarraSuperior("Productos y stock", onVolver) },
        bottomBar = { NavegacionCappucare("Productos", onInicio, {}, onMovimientos, onPerfil) }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp, vertical = 10.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(bottom = 14.dp)) {
                Text("Productos y stock", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                Text("Administra los productos del inventario", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            CampoCappucare(busqueda, { busqueda = it }, "⌕  Buscar productos...", singleLine = true)
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                categorias.forEach { categoria ->
                    val activa = categoriaActiva == categoria
                    Box(
                        Modifier.weight(1f).background(if (activa) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
                            .clickable { categoriaActiva = categoria }.padding(horizontal = 4.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(categoria, color = if (activa) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = if (activa) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Text("${filtrados.size} productos", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(8.dp))
            if (filtrados.isEmpty()) {
                EstadoVacio("No hay productos para mostrar", "Prueba con otro nombre o código, o cambia la categoría.")
            } else {
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(filtrados, key = { it.codigo }) { producto -> ProductoCard(producto) }
                }
            }
        }
    }
}

@Composable
fun ProductoCard(producto: Producto) {
    val bajo = producto.cantidad <= producto.nivelMinimo
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(17.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(68.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = .12f), RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                Text(if (producto.categoria.contains("med", true)) "✚" else "▦", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(producto.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("Cód: ${producto.codigo}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                Text(producto.categoria, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Stock", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                Text(producto.cantidad.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold, color = if (bajo) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
                EtiquetaEstado(if (bajo) "Stock bajo" else "Stock normal", bajo)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovimientosScreen(
    movimientos: List<Movimiento>,
    onVolver: () -> Unit,
    onInicio: () -> Unit,
    onProductos: () -> Unit,
    onPerfil: () -> Unit,
    onRegistrarIngreso: () -> Unit,
    onRegistrarSalida: () -> Unit
) {
    var filtroProducto by remember { mutableStateOf("") }
    var tipoActivo by remember { mutableStateOf("Todos") }
    val filtrados = movimientos.filter {
        it.codigoProducto.contains(filtroProducto, true) &&
            (tipoActivo == "Todos" || (tipoActivo == "Ingresos" && it.tipo.contains("ingreso", true)) || (tipoActivo == "Salidas" && it.tipo.contains("salida", true)))
    }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { BarraSuperior("Movimientos", onVolver) },
        bottomBar = { NavegacionCappucare("Movimientos", onInicio, onProductos, {}, onPerfil) }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp, vertical = 10.dp)) {
            Text("Movimientos", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
            Text("Historial de ingresos y salidas de stock", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onRegistrarIngreso, modifier = Modifier.weight(1f).height(46.dp), shape = RoundedCornerShape(13.dp)) { Text("＋ Ingreso") }
                OutlinedButton(onClick = onRegistrarSalida, modifier = Modifier.weight(1f).height(46.dp), shape = RoundedCornerShape(13.dp)) { Text("− Salida") }
            }
            Spacer(Modifier.height(12.dp))
            CampoCappucare(filtroProducto, { filtroProducto = it }, "Buscar por código...", singleLine = true)
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Todos", "Ingresos", "Salidas").forEach { filtro ->
                    val activa = tipoActivo == filtro
                    Box(Modifier.background(if (activa) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp)).clickable { tipoActivo = filtro }.padding(horizontal = 18.dp, vertical = 10.dp)) {
                        Text(filtro, color = if (activa) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.labelMedium, fontWeight = if (activa) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            if (filtrados.isEmpty()) {
                EstadoVacio("Sin movimientos registrados", "Las operaciones guardadas aparecerán aquí.")
            } else {
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    items(filtrados, key = { it.id }) { movimiento -> MovimientoCard(movimiento) }
                }
            }
        }
    }
}

@Composable
fun MovimientoCard(movimiento: Movimiento) {
    val esIngreso = movimiento.tipo.contains("ingreso", ignoreCase = true)
    val colorEstado = if (esIngreso) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(11.dp)) {
            Box(Modifier.size(44.dp).background(colorEstado.copy(alpha = .16f), CircleShape), contentAlignment = Alignment.Center) {
                Text(if (esIngreso) "+" else "−", color = colorEstado, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(if (esIngreso) "Ingreso de stock" else "Salida de stock", color = colorEstado, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text("Código: ${movimiento.codigoProducto}", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                if (movimiento.motivo.isNotBlank()) Text(movimiento.motivo, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(movimiento.fecha, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(if (esIngreso) "+ ${movimiento.cantidad}" else "− ${movimiento.cantidad}", color = colorEstado, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                Text(movimiento.responsable, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrarIngresoScreen(
    mensaje: String?,
    responsableActivo: String,
    onVolver: () -> Unit,
    onGuardar: (String, Int, String) -> Unit
) {
    var codigo by remember { mutableStateOf("") }
    var cantidad by remember { mutableStateOf("") }
    var responsable by remember(responsableActivo) { mutableStateOf(responsableActivo) }
    var mensajeValidacion by remember { mutableStateOf("") }

    Scaffold(containerColor = MaterialTheme.colorScheme.background, topBar = { BarraSuperior("Registrar ingreso", onVolver) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Registrar ingreso", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
            Text("Agrega stock al inventario", color = MaterialTheme.colorScheme.onSurfaceVariant)
            HeroMedico(
                titulo = "Ingreso de insumos",
                descripcion = "Actualiza el inventario con nuevas unidades.",
                simbolo = "＋", compacto = true
            )
            EncabezadoFormulario("Entrada de productos", "Registra las unidades que se incorporan al inventario.", "＋")
            CampoCappucare(codigo, { codigo = it; mensajeValidacion = "" }, "Código del producto", singleLine = true)
            CampoCappucare(cantidad, { cantidad = it; mensajeValidacion = "" }, "Cantidad", singleLine = true, keyboardType = KeyboardType.Number)
            CampoCappucare(responsable, {}, "Responsable (usuario activo)", readOnly = true, singleLine = true)
            MensajeFormulario(mensajeValidacion, mensaje)
            Spacer(Modifier.height(4.dp))
            Button(
                onClick = {
                    val numero = cantidad.toIntOrNull()
                    when {
                        codigo.isBlank() -> mensajeValidacion = "Debe ingresar el código del producto."
                        numero == null -> mensajeValidacion = "Debe ingresar una cantidad válida."
                        numero <= 0 -> mensajeValidacion = "La cantidad debe ser mayor que 0."
                        responsable.isBlank() -> mensajeValidacion = "Debe ingresar el responsable."
                        else -> { mensajeValidacion = ""; onGuardar(codigo.trim(), numero, responsable.trim()) }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(14.dp)
            ) { Text("Guardar ingreso", fontWeight = FontWeight.SemiBold) }
            OutlinedButton(onClick = onVolver, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(14.dp)) { Text("Cancelar y volver") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrarSalidaScreen(
    mensaje: String?,
    responsableActivo: String,
    onVolver: () -> Unit,
    onGuardar: (String, Int, String, String) -> Unit
) {
    var codigo by remember { mutableStateOf("") }
    var cantidad by remember { mutableStateOf("") }
    var motivo by remember { mutableStateOf("") }
    var responsable by remember(responsableActivo) { mutableStateOf(responsableActivo) }
    var mensajeValidacion by remember { mutableStateOf("") }

    Scaffold(containerColor = MaterialTheme.colorScheme.background, topBar = { BarraSuperior("Registrar salida", onVolver) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Registrar salida", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
            Text("Reduce el stock disponible", color = MaterialTheme.colorScheme.onSurfaceVariant)
            HeroMedico(
                titulo = "Salida de insumos",
                descripcion = "Registra cada retiro con su motivo.",
                simbolo = "−", compacto = true
            )
            EncabezadoFormulario("Salida de productos", "Registra una venta, consumo o retiro de stock.", "−")
            CampoCappucare(codigo, { codigo = it; mensajeValidacion = "" }, "Código del producto", singleLine = true)
            CampoCappucare(cantidad, { cantidad = it; mensajeValidacion = "" }, "Cantidad", singleLine = true, keyboardType = KeyboardType.Number)
            CampoCappucare(motivo, { motivo = it; mensajeValidacion = "" }, "Motivo de la salida", singleLine = true)
            CampoCappucare(responsable, {}, "Responsable (usuario activo)", readOnly = true, singleLine = true)
            MensajeFormulario(mensajeValidacion, mensaje)
            Spacer(Modifier.height(4.dp))
            Button(
                onClick = {
                    val numero = cantidad.toIntOrNull()
                    when {
                        codigo.isBlank() -> mensajeValidacion = "Debe ingresar el código del producto."
                        numero == null -> mensajeValidacion = "Debe ingresar una cantidad válida."
                        numero <= 0 -> mensajeValidacion = "La cantidad debe ser mayor que 0."
                        motivo.isBlank() -> mensajeValidacion = "Debe ingresar el motivo de la salida."
                        responsable.isBlank() -> mensajeValidacion = "Debe ingresar el responsable."
                        else -> { mensajeValidacion = ""; onGuardar(codigo.trim(), numero, motivo.trim(), responsable.trim()) }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) { Text("Guardar salida", fontWeight = FontWeight.SemiBold) }
            OutlinedButton(onClick = onVolver, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(14.dp)) { Text("Cancelar y volver") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BarraSuperior(titulo: String, onVolver: () -> Unit) {
    TopAppBar(
        title = { Text(titulo, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold) },
        navigationIcon = {
            androidx.compose.material3.TextButton(onClick = onVolver) { Text("‹", style = MaterialTheme.typography.headlineMedium) }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
            navigationIconContentColor = MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
private fun CampoCappucare(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = false,
    readOnly: Boolean = false,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation = androidx.compose.ui.text.input.VisualTransformation.None,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        singleLine = singleLine,
        readOnly = readOnly,
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            cursorColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
private fun EtiquetaEstado(texto: String, alerta: Boolean) {
    val color = if (alerta) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    Surface(color = color.copy(alpha = 0.13f), shape = RoundedCornerShape(8.dp)) {
        Text(texto, color = color, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp))
    }
}

@Composable
private fun EncabezadoFormulario(titulo: String, descripcion: String, simbolo: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.size(48.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.13f), RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
            Text(simbolo, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(titulo, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(descripcion, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun MensajeFormulario(validacion: String, mensaje: String?) {
    if (validacion.isNotBlank()) {
        Text(validacion, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
    } else if (!mensaje.isNullOrBlank()) {
        Text(mensaje, color = if (mensaje.contains("correctamente", true)) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun EstadoVacio(titulo: String, descripcion: String) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(titulo, fontWeight = FontWeight.SemiBold)
            Text(descripcion, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
    }
}

