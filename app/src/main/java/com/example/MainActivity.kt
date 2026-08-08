package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.example.data.*
import com.example.ui.ChatMessage
import com.example.ui.DraftActivity
import com.example.ui.LegalViewModel
import com.example.ui.TimelineHito
import com.example.ui.DocumentoPrueba
import com.example.ui.GoogleDriveFile
import com.example.ui.GmailEmail
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MainContainer()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContainer() {
    val viewModel: LegalViewModel = viewModel()
    val context = LocalContext.current

    MyApplicationTheme(darkTheme = viewModel.isDarkMode) {
        if (!viewModel.isUserLoggedIn) {
            GoogleLoginScreen(viewModel)
        } else {
            Scaffold(
                bottomBar = {
                    BottomNavigationBar(
                        currentTab = viewModel.currentTab,
                        onTabSelected = { tab ->
                            viewModel.currentTab = tab
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    Crossfade(targetState = viewModel.currentTab, label = "ScreenTransition") { tab ->
                        when (tab) {
                            "inicio" -> InicioScreen(viewModel)
                            "expedientes" -> ExpedientesScreen(viewModel)
                            "alta_expediente" -> AltaNuevoCasoScreen(viewModel)
                            "cuestionario_antecedentes" -> CuestionarioAntecedentesScreen(viewModel)
                            "agenda" -> AgendaScreen(viewModel)
                            "chalan" -> ChalanScreen(viewModel)
                            "correo" -> GmailScreen(viewModel)
                            "ajustes" -> AjustesScreen(viewModel)
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// NEW COMPONENT: Dynamic Bottom Navigation Bar matching exactly
// -------------------------------------------------------------
@Composable
fun BottomNavigationBar(
    currentTab: String,
    onTabSelected: (String) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        tonalElevation = 8.dp,
        modifier = Modifier
            .height(64.dp)
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .border(
                1.dp,
                Color.Black.copy(alpha = 0.05f),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            )
    ) {
        val items = listOf(
            Triple("inicio", "Inicio", Icons.Default.Home),
            Triple("expedientes", "Expedientes", Icons.Default.Folder),
            Triple("agenda", "Agenda", Icons.Default.CalendarToday),
            Triple("chalan", "Mi Chalán", Icons.Default.ContactSupport),
            Triple("correo", "Gmail", Icons.Default.Email),
            Triple("ajustes", "Ajustes", Icons.Default.Settings)
        )

        items.forEach { (route, label, icon) ->
            val isSelected = currentTab == route || (route == "expedientes" && (currentTab == "alta_expediente" || currentTab == "cuestionario_antecedentes"))
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(route) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline
                    )
                },
                label = {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.15f)
                ),
                modifier = Modifier.testTag("nav_tab_$route")
            )
        }
    }
}

// -------------------------------------------------------------
// SCREEN 1: Inicio / Welcome Dashboard with AI Assistant Welcome
// -------------------------------------------------------------
@Composable
fun InicioScreen(viewModel: LegalViewModel) {
    val dbCases by viewModel.expedientes.collectAsStateWithLifecycle()
    val currentHour = remember {
        java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    }
    val greetingTime = when (currentHour) {
        in 6..12 -> "Buenos días"
        in 13..19 -> "Buenas tardes"
        else -> "Buenas noches"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        // Welcome Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Bienvenido a",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
                Text(
                    text = "LitigAndo en eso",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 26.sp,
                        lineHeight = 32.sp
                    ),
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            // User Lic profile picture - clickable to Settings/Ajustes
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(AccentBlue.copy(alpha = 0.1f))
                    .border(2.dp, AccentBlue, CircleShape)
                    .clickable { viewModel.currentTab = "ajustes" }
            ) {
                AsyncImage(
                    model = viewModel.userAvatarUrl,
                    contentDescription = "Foto de ${viewModel.userName}",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Cute Robot Robot 3D Gavel Gavel representation card - now interactive buttons to open "Mi Chalán"
        Card(
            onClick = { viewModel.currentTab = "chalan" },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Render of the high-fidelity cute robot avatar from user attachment
                Image(
                    painter = painterResource(id = R.drawable.img_chalan_avatar_1780789609069),
                    contentDescription = "Mi Chalán AI Avatar",
                    modifier = Modifier
                        .size(86.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, AccentBlue.copy(alpha = 0.25f), RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "$greetingTime, mi Lic,",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "¿Quiere que revisemos lo pendiente?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    Text(
                        text = "Pregúntale a Mi Chalán AI para coordinar tu equipo legal.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Actions
        Text(
            text = "ACCIONES RÁPIDAS",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Card Button 1 - Agenda
            Card(
                onClick = { viewModel.currentTab = "agenda" },
                modifier = Modifier
                    .weight(1f)
                    .height(110.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(AccentBlue.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = AccentBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "Ver Agenda y Actividades",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = DeepNavy
                    )
                }
            }

            // Card Button 2 - Alta Auxiliar
            Card(
                onClick = { viewModel.currentTab = "chalan" },
                modifier = Modifier
                    .weight(1f)
                    .height(110.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(ActiveGreen.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = null,
                            tint = ActiveGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "Alta de Colaboradores",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = DeepNavy
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Previo de Casos en Ejecución (Upcoming Execution Cases Timeline Preview)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "CASOS EN EJECUCIÓN (FECHA PRÓXIMA)",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Ver todos",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = RoyalBlue,
                modifier = Modifier.clickable { viewModel.currentTab = "expedientes" }
            )
        }

        if (dbCases.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay casos en ejecución activos.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedSlate
                    )
                }
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                dbCases.take(4).forEachIndexed { index, expediente ->
                    val displayTitle = if (expediente.numeroCaso.contains("Caso") || expediente.numeroCaso.contains("Exp")) {
                        "${expediente.numeroCaso} - ${expediente.nombreCliente}"
                    } else {
                        "${expediente.numeroCaso} • ${expediente.nombreCliente}"
                    }

                    val (fechaProxima, tipoActo, colorTag) = when (index % 4) {
                        0 -> Triple("Hoy, 10:00 AM", "Audiencia de Pruebas", UrgentRed)
                        1 -> Triple("Hoy, 02:00 PM", "Reunión con Cliente", GoldenYellow)
                        2 -> Triple("Mañana, 09:30 AM", "Revisión Tributaria", AccentBlue)
                        else -> Triple("Lunes 15 Jun, 11:00 AM", "Entrega de Evidencia", MutedSlate)
                    }

                    Card(
                        onClick = {
                            viewModel.agendaExpediente = displayTitle
                            viewModel.currentTab = "agenda"
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(colorTag.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = if (index % 2 == 0) Icons.Default.Gavel else Icons.Default.Event,
                                        contentDescription = null,
                                        tint = colorTag,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (index == 0) "HOY" else "PRÓX",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colorTag,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 8.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = displayTitle,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepNavy,
                                    maxLines = 1
                                )
                                Text(
                                    text = "Acto: $tipoActo",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedSlate,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AccessTime,
                                        contentDescription = null,
                                        tint = if (index == 0) { UrgentRed } else { MutedSlate },
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = fechaProxima,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (index == 0) { UrgentRed } else { MutedSlate },
                                        fontWeight = if (index == 0) { FontWeight.Bold } else { FontWeight.Normal }
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Ver detalles",
                                tint = OutlineGrey,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// SCREEN 2: Case Files Directory View
// -------------------------------------------------------------
@Composable
fun ExpedientesScreen(viewModel: LegalViewModel) {
    val dbCases by viewModel.expedientes.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Mis Expedientes",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = DeepNavy
            )
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = null,
                tint = RoyalBlue,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Premium CTA Card: Alta de Nuevo Caso
        Card(
            onClick = {
                viewModel.currentTab = "alta_expediente"
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = IceBlue),
            border = BorderStroke(1.dp, AccentBlue.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(AccentBlue.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CreateNewFolder,
                        contentDescription = "Nuevo Expediente",
                        tint = RoyalBlue,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Alta de Nuevo Caso",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = DeepNavy
                    )
                    Text(
                        text = "Registrar expediente judicial con validación criptográfica.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedSlate
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = RoyalBlue
                )
            }
        }

        Text(
            text = "EXPEDIENTES ACTIVOS",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (dbCases.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = null,
                        tint = OutlineGrey,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No hay expedientes registrados.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedSlate
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(dbCases) { expediente ->
                    val displayTitle = if (expediente.numeroCaso.contains("Caso") || expediente.numeroCaso.contains("Exp")) {
                        "${expediente.numeroCaso} - ${expediente.nombreCliente}"
                    } else {
                        "${expediente.numeroCaso} • ${expediente.nombreCliente}"
                    }
                    Card(
                        onClick = {
                            viewModel.agendaExpediente = displayTitle
                            viewModel.chalanSelectedCase = displayTitle
                            viewModel.currentTab = "agenda"
                            Toast.makeText(context, "Expediente Seleccionado: $displayTitle", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(RoyalBlue.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FolderOpen,
                                    contentDescription = null,
                                    tint = RoyalBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = displayTitle,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepNavy
                                )
                                Text(
                                    text = expediente.descripcion,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (expediente.tieneAntecedentes) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.History,
                                            contentDescription = null,
                                            tint = AccentBlue,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Tiene Antecedentes Procesales",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = AccentBlue,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// NEW SCREEN: Alta de Nuevo Caso Screen (High Fidelity Replica)
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AltaNuevoCasoScreen(viewModel: LegalViewModel) {
    val context = LocalContext.current
    var showPdfDialog by remember { mutableStateOf(false) }
    var showTxtDialog by remember { mutableStateOf(false) }
    var showPhotoDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightSlateBackground)
    ) {
        // High fidelity replication of the top bar: "< Alta de Nuevo Caso  🔒  [Profile Avatar]"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { viewModel.currentTab = "expedientes" },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "Volver",
                        tint = DeepNavy,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Alta de Nuevo Caso",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DeepNavy
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Certificado",
                    tint = BioPurple,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                // User avatar
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(AccentBlue.copy(alpha = 0.2f))
                        .clickable { viewModel.currentTab = "ajustes" }
                ) {
                    AsyncImage(
                        model = viewModel.userAvatarUrl,
                        contentDescription = "Perfil",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        // Main form body
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // DETALLES DEL EXPEDIENTE
            Text(
                text = "Detalles del Expediente",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DeepNavy,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // White container card for form inputs
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.03f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    AltaCasoTextField(
                        label = "Número de Caso",
                        value = viewModel.caseNumero,
                        onValueChange = { viewModel.caseNumero = it },
                        placeholder = "Ej. LP-2023-0045"
                    )

                    AltaCasoTextField(
                        label = "Nombre del Cliente",
                        value = viewModel.caseCliente,
                        onValueChange = { viewModel.caseCliente = it },
                        placeholder = "Razón Social o Persona Física"
                    )

                    AltaCasoTextField(
                        label = "Descripción del Caso",
                        value = viewModel.caseDescripcion,
                        onValueChange = { viewModel.caseDescripcion = it },
                        placeholder = "Resumen preliminar de los hechos y pretensiones jurídicas...",
                        isTextArea = true
                    )

                    // Dynamic Google Drive Folder System Creation Preview Card!
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = AccentBlue.copy(alpha = 0.05f)),
                        border = BorderStroke(1.dp, AccentBlue.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CloudQueue,
                                    contentDescription = "Google Drive",
                                    tint = RoyalBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Estructura de Google Drive",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = RoyalBlue
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Al crear el expediente se generará automáticamente:",
                                style = MaterialTheme.typography.labelSmall,
                                color = DeepNavy,
                                fontSize = 10.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 4.dp)) {
                                Icon(
                                    imageVector = Icons.Default.FolderOpen,
                                    contentDescription = null,
                                    tint = GoldenYellow,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "LitigAndo",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = DeepNavy
                                )
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = MutedSlate,
                                    modifier = Modifier.size(14.dp)
                                )
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = null,
                                    tint = RoyalBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (viewModel.caseCliente.isNotBlank()) viewModel.caseCliente else "[Nombre de Cliente]",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = RoyalBlue
                                )
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = MutedSlate,
                                    modifier = Modifier.size(14.dp)
                                )
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = null,
                                    tint = BioPurple,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (viewModel.caseNumero.isNotBlank()) viewModel.caseNumero else "[N° Caso]",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Normal,
                                    color = BioPurple
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Antecedentes selector matching exactly: "No" | "Sí >"
                    Text(
                        text = "ANTECEDENTES DEL CASO",
                        style = MaterialTheme.typography.labelSmall,
                        color = MutedSlate,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp, start = 2.dp)
                    )
                    Text(
                        text = "¿Existen antecedentes procesales o hechos previos relevantes?",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedSlate.copy(alpha = 0.8f),
                        modifier = Modifier.padding(bottom = 12.dp, start = 2.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceLow),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // "No" button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (!viewModel.caseAntecedentes) Color.White else Color.Transparent)
                                .border(
                                    if (!viewModel.caseAntecedentes) BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f)) else BorderStroke(0.dp, Color.Transparent),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { viewModel.caseAntecedentes = false },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (!viewModel.caseAntecedentes) FontWeight.Bold else FontWeight.Normal,
                                color = if (!viewModel.caseAntecedentes) DeepNavy else MutedSlate
                            )
                        }

                        // "Sí >" button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (viewModel.caseAntecedentes) RoyalBlue else Color.Transparent)
                                .clickable { 
                                    viewModel.currentTab = "cuestionario_antecedentes"
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Sí",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (viewModel.caseAntecedentes) FontWeight.Bold else FontWeight.Normal,
                                    color = if (viewModel.caseAntecedentes) Color.White else MutedSlate
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = if (viewModel.caseAntecedentes) Color.White else MutedSlate,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // GESTIÓN DE EVIDENCIA
            Text(
                text = "Gestión de Evidencia",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DeepNavy,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Evidence Cards list
            // Card 1: Documento de Texto
            Card(
                onClick = { showTxtDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.03f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(RoyalBlue.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = RoyalBlue,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Documento de Texto",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = DeepNavy
                        )
                        Text(
                            text = if (viewModel.caseDocumentoTexto.isNotBlank()) "✓ Cargado: ${viewModel.caseDocumentoTexto.take(24)}..." else "Redacción directa certificada",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (viewModel.caseDocumentoTexto.isNotBlank()) ActiveGreen else MutedSlate
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = OutlineGrey
                    )
                }
            }

            // Card 2: Escanear PDF
            Card(
                onClick = { showPdfDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.03f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Badge in top right corner
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 12.dp, end = 12.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(ActiveGreen)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Check, // representation of AI spark or active node
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(10.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "AI ACTIVE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 8.sp
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(BioPurple.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                tint = BioPurple,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f).padding(end = 60.dp)) {
                            Text(
                                text = "Escanear PDF",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = DeepNavy
                            )
                            Text(
                                text = if (viewModel.caseEscanearPdf.isNotBlank()) "✓ Escaneado con Éxito" else "Conversión inteligente de anexos mediante IA estructural",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (viewModel.caseEscanearPdf.isNotBlank()) ActiveGreen else MutedSlate
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = OutlineGrey
                        )
                    }
                }
            }

            // Card 3: Tomar Foto
            Card(
                onClick = { showPhotoDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.03f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(AccentBlue.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = null,
                            tint = AccentBlue,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Tomar Foto",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = DeepNavy
                        )
                        Text(
                            text = if (viewModel.caseFotoRuta.isNotBlank()) "✓ Foto Registrada" else "Extracción de texto legal via LLM Vision",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (viewModel.caseFotoRuta.isNotBlank()) ActiveGreen else MutedSlate
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = OutlineGrey
                    )
                }
            }

            // VALIDACIÓN JUDICIAL WARNING BLOCK
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                colors = CardDefaults.cardColors(containerColor = IceBlue.copy(alpha = 0.4f)),
                border = BorderStroke(1.dp, AccentBlue.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Información",
                        tint = AccentBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Validación Judicial",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = DeepNavy
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Cada documento cargado es sometido a un proceso de hash criptográfico para garantizar su integridad y admisibilidad en sede judicial conforme a la Ley de Litigación Digital.",
                            style = MaterialTheme.typography.bodySmall,
                            color = DeepNavy.copy(alpha = 0.85f),
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // STICKY ACTION BUTTONS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = {
                        Toast.makeText(context, "Borrador guardado localmente de forma segura.", Toast.LENGTH_SHORT).show()
                        viewModel.currentTab = "expedientes"
                    }
                ) {
                    Text(
                        text = "Guardar\nBorrador",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MutedSlate,
                        textAlign = TextAlign.Center
                    )
                }

                Button(
                    onClick = {
                        if (viewModel.caseNumero.isBlank() || viewModel.caseCliente.isBlank()) {
                            Toast.makeText(context, "Favor de ingresar Número de Caso y Nombre del Cliente", Toast.LENGTH_LONG).show()
                        } else {
                            val clientName = viewModel.caseCliente
                            val caseNo = viewModel.caseNumero
                            viewModel.saveExpediente {
                                viewModel.driveFiles.add(
                                    GoogleDriveFile(
                                        nombreColumna = "Acondicionamiento_Legal_Inicial.pdf",
                                        cliente = clientName,
                                        caseNumero = caseNo,
                                        size = "250 KB"
                                    )
                                )
                                Toast.makeText(context, "Carpeta de Google Drive inicializada:\nLitigAndo / $clientName / $caseNo/", Toast.LENGTH_LONG).show()
                                viewModel.currentTab = "expedientes"
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DeepNavy),
                    contentPadding = PaddingValues(horizontal = 32.dp, vertical = 14.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = "Crear\nExpediente",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    // Modal dialogs to prevent dead-end UI and allow actual interaction / editing of states!
    if (showTxtDialog) {
        var inputTxt by remember { mutableStateOf(viewModel.caseDocumentoTexto) }
        AlertDialog(
            onDismissRequest = { showTxtDialog = false },
            title = { Text("Redactar Documento de Texto", color = DeepNavy, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Ingrese los datos o redacción legal certificada:", color = MutedSlate, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = inputTxt,
                        onValueChange = { inputTxt = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4,
                        placeholder = { Text("Escriba aquí los hechos, demandas, etc...") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.caseDocumentoTexto = inputTxt
                        showTxtDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DeepNavy)
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTxtDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showPdfDialog) {
        var simulatedParsing by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { showPdfDialog = false },
            title = { Text("Escanear PDF con IA", color = DeepNavy, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("La IA estructural analizará los anexos y extraerá las tablas, fechas críticas y litigación previa.", color = MutedSlate, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(16.dp))
                    if (simulatedParsing) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = DeepNavy)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Extrayendo metadatos jurídicos...", color = DeepNavy, style = MaterialTheme.typography.bodyMedium)
                        }
                    } else {
                        Button(
                            onClick = {
                                simulatedParsing = true
                                viewModel.caseEscanearPdf = "anexos_procesales_extract.pdf"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                        ) {
                            Text("Iniciar Escaneo Inteligente")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showPdfDialog = false },
                    enabled = simulatedParsing
                ) {
                    Text("Terminar")
                }
            }
        )
    }

    if (showPhotoDialog) {
        AlertDialog(
            onDismissRequest = { showPhotoDialog = false },
            title = { Text("Tomar Foto / Cargar Evidencia", color = DeepNavy, fontWeight = FontWeight.Bold) },
            text = {
                Text("Se solicita acceso a la cámara judicial para capturar evidencia física con geolocalización blindada y hash de tiempo real.", color = MutedSlate)
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.caseFotoRuta = "judicial_evidence_capture_00.jpg"
                        showPhotoDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DeepNavy)
                ) {
                    Text("Capturar Evidencia")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPhotoDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun AltaCasoTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isTextArea: Boolean = false
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MutedSlate,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 6.dp, start = 2.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = OutlineGrey.copy(alpha = 0.5f), fontSize = 14.sp) },
            modifier = Modifier.fillMaxWidth(),
            maxLines = if (isTextArea) 4 else 1,
            minLines = if (isTextArea) 3 else 1,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SurfaceLow,
                unfocusedContainerColor = SurfaceLow,
                focusedBorderColor = AccentBlue.copy(alpha = 0.5f),
                unfocusedBorderColor = Color.Transparent,
                disabledBorderColor = Color.Transparent,
                errorBorderColor = UrgentRed
            )
        )
    }
}

// -------------------------------------------------------------
// SCREEN 3: HIGH FIDELITY REPLICA OF "Martes, 8 de Octubre, 2024" (AGENDA)
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendaScreen(viewModel: LegalViewModel) {
    val activities by viewModel.actividadesProgramadas.collectAsStateWithLifecycle()
    val dbCases by viewModel.expedientes.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // TopAppBar (Fixed/Glass)
        TopAppBar(
            title = {
                Text(
                    text = "Martes, 8 de Octubre, 2024",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = DeepNavy
                )
            },
            navigationIcon = {
                IconButton(onClick = { viewModel.currentTab = "inicio" }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint = DeepNavy
                    )
                }
            },
            actions = {
                IconButton(onClick = { /* Go to calendar view */ }) {
                    Icon(
                        imageVector = Icons.Default.EventNote,
                        contentDescription = "Calendario",
                        tint = DeepNavy
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = LightSlateBackground.copy(alpha = 0.82f),
                scrolledContainerColor = LightSlateBackground
            )
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Section Title: Actividades Programadas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Actividades Programadas",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = DeepNavy
                    )
                    Box(
                        modifier = Modifier
                            .background(SurfaceMedium, shape = CircleShape)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${activities.size} EVENTOS",
                            style = MaterialTheme.typography.labelSmall,
                            color = MutedSlate,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Real DB bound activities matching design
            items(activities, key = { it.id }) { activity ->
                val isUrgent = activity.estado == "Pendiente"
                val stripeColor = if (isUrgent) PendingOrange else ActiveGreen

                SwipeToDismissActivityCard(
                    activity = activity,
                    stripeColor = stripeColor,
                    onDelete = {
                        viewModel.deleteActivity(activity)
                        Toast.makeText(context, "Actividad eliminada", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            // Agendar Nueva Actividad Header
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceLow),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, OutlineVariantGrey.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddCircle,
                                contentDescription = null,
                                tint = DeepNavy,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Agendar Nueva Actividad",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = DeepNavy
                            )
                        }

                        // Form Fields
                        LegalBoxTextField(
                            label = "Título de la Actividad",
                            value = viewModel.agendaTitulo,
                            onValueChange = { viewModel.agendaTitulo = it },
                            placeholder = "Ej. Presentación de Recurso"
                        )

                        LegalBoxDropdown(
                            label = "Expediente Asociado",
                            value = viewModel.agendaExpediente,
                            onValueChange = { viewModel.agendaExpediente = it },
                            options = listOf("Seleccionar Expediente...") + dbCases.map {
                                if (it.numeroCaso.contains("Caso") || it.numeroCaso.contains("Exp")) {
                                    "${it.numeroCaso} - ${it.nombreCliente}"
                                } else {
                                    "${it.numeroCaso} • ${it.nombreCliente}"
                                }
                            },
                            placeholder = "Seleccionar Expediente..."
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            LegalBoxTextField(
                                label = "Inicio",
                                value = viewModel.agendaHoraInicio,
                                onValueChange = { viewModel.agendaHoraInicio = it },
                                placeholder = "hh:mm",
                                modifier = Modifier.weight(1f)
                            )
                            LegalBoxTextField(
                                label = "Fin",
                                value = viewModel.agendaHoraFin,
                                onValueChange = { viewModel.agendaHoraFin = it },
                                placeholder = "hh:mm",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Tipo de Actividad Tags Row
                        Text(
                            text = "TIPO DE ACTIVIDAD",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 12.dp, bottom = 6.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val tags = listOf("Audiencia", "Reunión", "Revisión", "Otro")
                            tags.forEach { tag ->
                                val isActive = viewModel.agendaTipo == tag
                                Button(
                                    onClick = { viewModel.agendaTipo = tag },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isActive) DeepNavy else Color.White,
                                        contentColor = if (isActive) Color.White else DeepNavy
                                    ),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isActive) DeepNavy else OutlineVariantGrey
                                    ),
                                    shape = CircleShape,
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Text(text = tag, style = MaterialTheme.typography.bodySmall, fontSize = 12.sp)
                                }
                            }
                        }

                        // Location
                        LegalBoxTextField(
                            label = "Ubicación o Enlace",
                            value = viewModel.agendaUbicacion,
                            onValueChange = { viewModel.agendaUbicacion = it },
                            placeholder = "Dirección física o link de videollamada",
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.outline
                                )
                            }
                        )

                        // Notes
                        LegalBoxTextField(
                            label = "Notas Adicionales",
                            value = viewModel.agendaNotas,
                            onValueChange = { viewModel.agendaNotas = it },
                            placeholder = "Detalles relevantes para la actividad...",
                            isTextArea = true
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Guardar button
                        Button(
                            onClick = {
                                if (viewModel.agendaTitulo.isBlank()) {
                                    Toast.makeText(context, "Ingrese el título", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.saveActividadProgramada {
                                        Toast.makeText(context, "Actividad registrada con éxito", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("save_activity_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = DeepNavy),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = null,
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Guardar Actividad",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Inspirational legal graphic card at the bottom
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    AsyncImage(
                        model = "https://lh3.googleusercontent.com/aida-public/AB6AXuAV-QYehgFWEie3zg22SAGZJL4tPAwQ9raxYHTpJ6l4e5TjYL9mD5uMK2mqDhOBlqHJ8inYcFK2iaBTE78EcV7EbElWI20irekqvqYLwqlcrp-vHhRJtCk6HluKJEPcLB3EGrhJG-2bBGNHFUd8Z8oEe_yrA7eiv2THj455uWRepTyS9lEV0FKz0uk3lrZT3C4g_Tpc4OphK6jPsAB7xo9FFgHvnr_6d6JnQvW9HWLnCnUV7_ZbqCYRbSdLVvPGlcIuQtge3fVgs5s",
                        contentDescription = "Despacho legal",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Bottom gradient overlay with text
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                                )
                            )
                            .padding(16.dp),
                        contentAlignment = Alignment.BottomStart
                    ) {
                        Text(
                            text = "\"La preparación es la clave del éxito en el litigio.\"",
                            style = MaterialTheme.typography.bodyLarge,
                            fontStyle = FontStyle.Italic,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun SwipeToDismissActivityCard(
    activity: ActividadProgramadaEntity,
    stripeColor: Color,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, color = OutlineVariantGrey.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            // Dynamic color stripe indicator
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(stripeColor)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Time box
                Column(
                    modifier = Modifier.width(56.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = activity.horaInicio,
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                        fontWeight = FontWeight.Bold,
                        color = DeepNavy
                    )
                    Text(
                        text = if (activity.horaInicio < "12:00") "AM" else "PM",
                        style = MaterialTheme.typography.labelSmall,
                        color = MutedSlate
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Mid Content
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = activity.titulo,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = DeepNavy
                        )
                        // Status mark
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (activity.estado == "Pendiente") {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(PendingOrange)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Pendiente",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PendingOrange,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = ActiveGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Completado",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ActiveGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Text(
                        text = activity.expediente,
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedSlate,
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Location detail
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (activity.ubicacionOEnlace.contains(
                                    "Zoom",
                                    ignoreCase = true
                                )
                            ) Icons.Default.Videocam else Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = if (activity.ubicacionOEnlace.contains(
                                    "Zoom",
                                    ignoreCase = true
                                )
                            ) RoyalBlue else DeepNavy,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = activity.ubicacionOEnlace.ifBlank { "Sin ubicación designada" },
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = if (activity.ubicacionOEnlace.contains(
                                    "Zoom",
                                    ignoreCase = true
                                )
                            ) RoyalBlue else DeepNavy
                        )
                    }
                }

                // Delete trash button inside row
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = UrgentRed.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// SCREEN 1 PART 2: HIGH FIDELITY REPLICA OF "Alta de Colaborador" (CHALAN)
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChalanScreen(viewModel: LegalViewModel) {
    val context = LocalContext.current
    val specialties = listOf("Abogado Litigante", "Perito Especializado", "Pasante Jurídico", "Analista de Evidencia")

    Column(modifier = Modifier.fillMaxSize()) {
        // Custom Top Header App Bar representing client headshot and logo
        TopAppBar(
            title = {
                Text(
                    text = if (viewModel.chalanTabMode == "asistente") "Mi Chalán AI" else "Alta de Colaborador",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = DeepNavy
                )
            },
            navigationIcon = {
                IconButton(onClick = { viewModel.currentTab = "inicio" }) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Atrás", tint = DeepNavy)
                }
            },
            actions = {
                // Circular direct image
                Box(
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                        .border(1.dp, OutlineGrey.copy(alpha = 0.2f), CircleShape)
                ) {
                    AsyncImage(
                        model = "https://lh3.googleusercontent.com/aida-public/AB6AXuB-FddN5d5NwLZJwPL12UpQIvjqEN6xZ_mQd_zUmEhxYHUMobDWVEAV_UCba_3vEJ36abLeplRbE5lgsbLXRINb7q4DxNnIdhlVRn_DwLsLHbX52bhGVA7l-iJyW0N4TGQ4q-esVelux986b_m3H0VyY5QmbdCLCkiLHsg1ot6lCuhcg7-ybaMHD9_BnT8aYXo5JAHVhRt48YA0jX-7udXLKdf8kIhFHnv3tJL7kKk5393gR82MnTr8ibPIzbxyZfOkMp1uCZTxuHk",
                        contentDescription = "Partner profile logo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = LightSlateBackground)
        )

        // Tab selection row
        TabRow(
            selectedTabIndex = if (viewModel.chalanTabMode == "asistente") 0 else 1,
            containerColor = LightSlateBackground,
            contentColor = DeepNavy,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[if (viewModel.chalanTabMode == "asistente") 0 else 1]),
                    color = AccentBlue
                )
            }
        ) {
            Tab(
                selected = viewModel.chalanTabMode == "asistente",
                onClick = { viewModel.chalanTabMode = "asistente" },
                text = { Text("Mi Chalán (AI)", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                icon = { Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = viewModel.chalanTabMode == "colaboradores",
                onClick = { viewModel.chalanTabMode = "colaboradores" },
                text = { Text("Alta de Colaboradores", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                icon = { Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
        }

        // Animated render based on mode
        Box(modifier = Modifier.weight(1f)) {
            if (viewModel.chalanTabMode == "asistente") {
                ChalanAiAssistantView(viewModel = viewModel)
            } else {
                ColaboradoresFormView(viewModel = viewModel, specialties = specialties, context = context)
            }
        }
    }
}

@Composable
fun ChalanAiAssistantView(viewModel: LegalViewModel) {
    val cases = listOf(
        "Caso #9821 - Ramirez v. Estado",
        "Smith v. Global",
        "Exp. 2024/045 - Corporativo Alfa",
        "Juicio Mercantil #312 - Constructora Sol"
    )
    val listState = rememberLazyListState()

    LaunchedEffect(viewModel.chalanMessages.size) {
        if (viewModel.chalanMessages.isNotEmpty()) {
            listState.animateScrollToItem(viewModel.chalanMessages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightSlateBackground)
    ) {
        // Associated Case Selector dropdown bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 1.dp,
            color = Color.White
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.FolderOpen,
                    contentDescription = null,
                    tint = RoyalBlue,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Asociar a:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedSlate,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                
                var expanded by remember { mutableStateOf(false) }
                Box(modifier = Modifier.weight(1f)) {
                    TextButton(
                        onClick = { expanded = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, OutlineVariantGrey.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .height(36.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = viewModel.chalanSelectedCase,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                color = DeepNavy,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = DeepNavy,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        cases.forEach { caseTitle ->
                            DropdownMenuItem(
                                text = { Text(caseTitle, color = DeepNavy, fontSize = 12.sp) },
                                onClick = {
                                    viewModel.chalanSelectedCase = caseTitle
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Messages area list
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            items(viewModel.chalanMessages, key = { it.id }) { msg ->
                ChatBubble(msg)
            }

            if (viewModel.chalanIsLoading) {
                item {
                    ChatLoadingBubble()
                }
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        // Fast Prompts suggestion chips
        val prompts = listOf(
            "Análisis de riesgo",
            "Borrador de Amparo",
            "Recomendar Acciones",
            "Resumen ejecutivo"
        )
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(prompts) { text ->
                SuggestionChip(
                    onClick = {
                        val fullPrompt = when (text) {
                            "Análisis de riesgo" -> "Por favor realiza un análisis de riesgos formal sobre el expediente '${viewModel.chalanSelectedCase}' basándote en la información disponible."
                            "Borrador de Amparo" -> "Licenciado, solicito redactar un borrador estructurado de suspensión provisional de Amparo para el expediente '${viewModel.chalanSelectedCase}' con fundamentación en la Ley de Amparo vigente."
                            "Recomendar Acciones" -> "¿Qué acciones me recomiendas programar en la Agenda para el '${viewModel.chalanSelectedCase}'?"
                            "Resumen ejecutivo" -> "Por favor, escribe un breve resumen de carácter urgente con las novedades del expediente '${viewModel.chalanSelectedCase}'."
                            else -> text
                        }
                        viewModel.sendChalanMessage(fullPrompt)
                    },
                    label = { Text(text, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = RoyalBlue) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = RoyalBlue.copy(alpha = 0.08f)
                    ),
                    border = BorderStroke(1.dp, RoyalBlue.copy(alpha = 0.15f))
                )
            }
        }

        // Send bar message input
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 8.dp,
            color = Color.White
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = viewModel.chalanInputMessage,
                    onValueChange = { viewModel.chalanInputMessage = it },
                    placeholder = { Text("Escribe una instrucción o duda legal...", fontSize = 13.sp, color = OutlineGrey) },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(max = 120.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = SurfaceLow,
                        unfocusedContainerColor = SurfaceLow,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = DeepNavy,
                        unfocusedTextColor = DeepNavy
                    ),
                    trailingIcon = {
                        if (viewModel.chalanInputMessage.isNotEmpty()) {
                            IconButton(onClick = { viewModel.chalanInputMessage = "" }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Limpiar text", tint = OutlineGrey)
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        val messageToSend = viewModel.chalanInputMessage
                        if (messageToSend.isNotBlank()) {
                            viewModel.sendChalanMessage(messageToSend)
                        }
                    },
                    enabled = viewModel.chalanInputMessage.isNotBlank() && !viewModel.chalanIsLoading,
                    modifier = Modifier
                        .background(
                            color = if (viewModel.chalanInputMessage.isNotBlank() && !viewModel.chalanIsLoading) AccentBlue else Color.LightGray,
                            shape = CircleShape
                        )
                        .size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Enviar",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubble(msg: ChatMessage) {
    val alignment = if (msg.isUser) Alignment.CenterEnd else Alignment.CenterStart
    val containerColor = if (msg.isUser) AccentBlue else Color(0xFFE9EEF4)
    val textColor = if (msg.isUser) Color.White else DeepNavy
    val cornerShape = if (msg.isUser) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 2.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 2.dp, bottomEnd = 16.dp)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        contentAlignment = alignment
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.fillMaxWidth(0.85f),
            horizontalArrangement = if (msg.isUser) Arrangement.End else Arrangement.Start
        ) {
            if (!msg.isUser) {
                Box(
                    modifier = Modifier
                        .padding(end = 8.dp, top = 2.dp)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(IceBlue)
                        .border(1.dp, AccentBlue, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = RoyalBlue,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Card(
                colors = CardDefaults.cardColors(containerColor = containerColor),
                shape = cornerShape,
                border = if (!msg.isUser) BorderStroke(1.dp, Color.Black.copy(alpha = 0.03f)) else null
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = msg.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = textColor,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    
                    if (!msg.isUser && msg.text.length > 50) {
                        var isSaved by remember { mutableStateOf(false) }
                        var isSaving by remember { mutableStateOf(false) }
                        val localViewModel: LegalViewModel = viewModel()
                        
                        Divider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = DeepNavy.copy(alpha = 0.08f)
                        )
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "☁️ Google Drive Integración",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = RoyalBlue,
                                fontSize = 10.sp
                            )
                            
                            Button(
                                onClick = {
                                    isSaving = true
                                    // Parse case details
                                    val fullCase = localViewModel.chalanSelectedCase
                                    var clientName = "Ramirez"
                                    var caseNo = "Caso #9821"
                                    if (fullCase.contains(" - ")) {
                                        val parts = fullCase.split(" - ")
                                        caseNo = parts.getOrNull(0)?.trim() ?: "Caso Gral"
                                        clientName = parts.getOrNull(1)?.replace(" v. Estado", "")?.replace(" vs. Contraparte", "")?.trim() ?: "Cliente"
                                    } else {
                                        clientName = fullCase
                                    }
                                    
                                    val filename = "Escrito_" + msg.id.toString().takeLast(4) + ".pdf"
                                    
                                    localViewModel.driveFiles.add(
                                        GoogleDriveFile(
                                            nombreColumna = filename,
                                            cliente = clientName,
                                            caseNumero = caseNo,
                                            size = "135 KB",
                                            esDeChalan = true
                                        )
                                    )
                                    isSaved = true
                                    isSaving = false
                                },
                                enabled = !isSaved && !isSaving,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSaved) ActiveGreen else RoyalBlue,
                                    disabledContainerColor = if (isSaved) ActiveGreen.copy(alpha = 0.8f) else RoyalBlue.copy(alpha = 0.5f)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isSaving) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(12.dp),
                                            color = Color.White,
                                            strokeWidth = 1.dp
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Guardando...", fontSize = 10.sp, color = Color.White)
                                    } else if (isSaved) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Guardado", fontSize = 10.sp, color = Color.White)
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.CloudUpload,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Guardar en Drive", fontSize = 10.sp, color = Color.White)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = msg.timestamp,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (msg.isUser) Color.White.copy(alpha = 0.7f) else MutedSlate.copy(alpha = 0.7f),
                        fontSize = 10.sp,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }
    }
}

@Composable
fun ChatLoadingBubble() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            Box(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(IceBlue)
                    .border(1.dp, AccentBlue, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = RoyalBlue,
                    modifier = Modifier.size(16.dp)
                )
            }
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE9EEF4).copy(alpha = 0.8f)),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 2.dp, bottomEnd = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(12.dp),
                        strokeWidth = 2.dp,
                        color = RoyalBlue
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Mi Chalán está redactando...",
                        style = MaterialTheme.typography.bodySmall,
                        color = DeepNavy,
                        fontSize = 11.sp,
                        fontStyle = FontStyle.Italic
                    )
                }
            }
        }
    }
}

@Composable
fun ColaboradoresFormView(viewModel: LegalViewModel, specialties: List<String>, context: android.content.Context) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // SECTION: Datos Generales Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = null,
                    tint = RoyalBlue,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Datos Generales",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = DeepNavy
                )
            }

            // Generales Card Input field containing forms block
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, OutlineVariantGrey.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    LegalUnderlineTextField(
                        label = "Nombre Completo",
                        value = viewModel.colNombre,
                        onValueChange = { viewModel.colNombre = it },
                        placeholder = "Ej. Lic. Roberto Estrada"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LegalUnderlineTextField(
                        label = "Correo Institucional",
                        value = viewModel.colCorreo,
                        onValueChange = { viewModel.colCorreo = it },
                        placeholder = "roberto.e@lexshield.com"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LegalUnderlineDropdown(
                        label = "Especialidad / Cargo",
                        value = viewModel.colEspecialidad,
                        onValueChange = { viewModel.colEspecialidad = it },
                        options = specialties,
                        placeholder = "Seleccionar cargo..."
                    )
                }
            }
        }

        // Information Sync Card banner (Blue background gradient block)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A2B3C)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = null,
                        tint = Color(0xFFD8E2FF),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Sincronización Inteligente",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Las actividades registradas se vincularán automáticamente al Calendario de Actividades y activarán monitoreo preventivo vía notificaciones push.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.padding(top = 4.dp),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // Dynamics Actividades y Responsabilidades Layout
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Assignment,
                        contentDescription = null,
                        tint = RoyalBlue,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Actividades y Responsabilidades",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = DeepNavy
                    )
                }

                // "+ AGREGAR" dynamic row append button
                Button(
                    onClick = { viewModel.addDraftActivity() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentBlue.copy(alpha = 0.1f),
                        contentColor = AccentBlue
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "AGREGAR",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Sub-list of multiple active card drafts
        items(viewModel.colDraftActivities, key = { it.id }) { draft ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, OutlineVariantGrey.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                ) {
                    // Blue vertical stripe left border identifier
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .fillMaxHeight()
                            .background(AccentBlue)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Descripción de la Actividad",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MutedSlate,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                                TextField(
                                    value = draft.descripcion,
                                    onValueChange = { viewModel.updateDraftActivityDesc(draft.id, it) },
                                    placeholder = {
                                        Text(
                                            "Describa la responsabilidad u objetivo...",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = SurfaceLow,
                                        unfocusedContainerColor = SurfaceLow,
                                        disabledContainerColor = SurfaceLow,
                                        focusedTextColor = DeepNavy,
                                        unfocusedTextColor = DeepNavy,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent
                                    ),
                                    minLines = 2,
                                    maxLines = 4
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Trash Delete helper
                            IconButton(
                                onClick = { viewModel.removeDraftActivity(draft.id) },
                                enabled = viewModel.colDraftActivities.size > 1
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Remover",
                                    tint = if (viewModel.colDraftActivities.size > 1) UrgentRed.copy(alpha = 0.7f) else Color.LightGray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Date range selects
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Fecha Inicio",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MutedSlate,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                OutlinedTextField(
                                    value = draft.fechaInicio,
                                    onValueChange = { viewModel.updateDraftActivityInicio(draft.id, it) },
                                    placeholder = { Text("mm/dd/yyyy", color = OutlineGrey.copy(alpha = 0.5f), fontSize = 12.sp) },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                    trailingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = SurfaceLow,
                                        unfocusedContainerColor = SurfaceLow,
                                        focusedBorderColor = AccentBlue,
                                        unfocusedBorderColor = Color.Transparent
                                    )
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Fecha Término",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MutedSlate,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                OutlinedTextField(
                                    value = draft.fechaFin,
                                    onValueChange = { viewModel.updateDraftActivityFin(draft.id, it) },
                                    placeholder = { Text("mm/dd/yyyy", color = OutlineGrey.copy(alpha = 0.5f), fontSize = 12.sp) },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                    trailingIcon = { Icon(Icons.Default.EventBusy, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = SurfaceLow,
                                        unfocusedContainerColor = SurfaceLow,
                                        focusedBorderColor = AccentBlue,
                                        unfocusedBorderColor = Color.Transparent
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Verification gold badge toggle
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.updateDraftActivityVerif(draft.id, !draft.verificacionRequerida) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = GoldenYellow,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "VERIFICACIÓN JUDICIAL REQUERIDA",
                                style = MaterialTheme.typography.labelSmall,
                                color = GoldenYellow,
                                fontWeight = FontWeight.Bold,
                                textDecoration = if (draft.verificacionRequerida) TextDecoration.None else TextDecoration.LineThrough
                            )
                        }
                    }
                }
            }
        }

        // Real Time monitoring push toggle Card block elements
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceLow),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, OutlineVariantGrey.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(AccentBlue.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = AccentBlue
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Monitoreo en Tiempo Real",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = DeepNavy
                            )
                            Text(
                                text = "Alertas push ante vencimientos.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedSlate
                            )
                        }
                    }

                    // Switch toggle
                    Switch(
                        checked = viewModel.colMonitoreo,
                        onCheckedChange = { viewModel.colMonitoreo = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = AccentBlue,
                            uncheckedThumbColor = OutlineGrey,
                            uncheckedTrackColor = OutlineVariantGrey
                        )
                    )
                }
            }
        }

        // Register Submit Button
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    if (viewModel.colNombre.isBlank() || viewModel.colCorreo.isBlank()) {
                        Toast.makeText(context, "Favor de rellenar los datos obligatorios", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.registerColaborador {
                            Toast.makeText(context, "Colaborador registrado con éxito en Base de Datos", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("register_colaborador_button"),
                colors = ButtonDefaults.buttonColors(containerColor = DeepNavy),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Registrar Colaborador",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// -------------------------------------------------------------
// SCREEN 3: HIGH FIDELITY REPLICA OF "Seguridad y Cumplimiento" (AJUSTES)
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjustesScreen(viewModel: LegalViewModel) {
    AjustesScreenNew(viewModel)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjustesScreenLegacy(viewModel: LegalViewModel) {
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        // App header containing picture and dynamic bells
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .border(1.dp, OutlineVariantGrey, CircleShape)
                    ) {
                        AsyncImage(
                            model = viewModel.userAvatarUrl,
                            contentDescription = "User Headshot Profile",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "LitigAndo en eso",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = DeepNavy
                    )
                }
            },
            actions = {
                IconButton(onClick = { Toast.makeText(context, "No hay nuevas alertas judiciales", Toast.LENGTH_SHORT).show() }) {
                    Icon(imageVector = Icons.Default.Notifications, contentDescription = "Alertas", tint = DeepNavy)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = LightSlateBackground)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Big Headers
                Text(
                    text = "Seguridad y Cumplimiento",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = DeepNavy
                )
                Text(
                    text = "Estándares de la Normativa 2025",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedSlate,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // USER PROFILE CARD (NUEVO PANEL DE CONFIGURACIÓN DE DATOS REQUERIDOS)
            item {
                Text(
                    text = "PERFIL DEL ABOGADO (LIC. USUARIO)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedSlate,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, OutlineVariantGrey.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Current profile display row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(AccentBlue.copy(alpha = 0.1f))
                                    .border(2.dp, AccentBlue, CircleShape)
                            ) {
                                AsyncImage(
                                    model = viewModel.userAvatarUrl,
                                    contentDescription = "Foto Actual",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = viewModel.userName.ifBlank { "Sin Nombre" },
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepNavy
                                )
                                Text(
                                    text = viewModel.userRole.ifBlank { "Abogado Titular" },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = RoyalBlue,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Cédula: ${viewModel.userCedula.ifBlank { "No Proporcionada" }}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MutedSlate
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = OutlineVariantGrey.copy(alpha = 0.2f), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(12.dp))

                        // Quick Avatar select presets
                        Text(
                            text = "SELECCIONAR FOTO DE PERFIL (PRESETS)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MutedSlate,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        val avatarPresets = listOf(
                            "https://images.unsplash.com/photo-1560250097-0b93528c311a?auto=format&fit=crop&q=80&w=250" to "Lic. 1",
                            "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?auto=format&fit=crop&q=80&w=250" to "Lic. 2",
                            "https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?auto=format&fit=crop&q=80&w=250" to "Lic. 3",
                            "https://images.unsplash.com/photo-1580489944761-15a19d654956?auto=format&fit=crop&q=80&w=250" to "Lic. 4",
                            "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?auto=format&fit=crop&q=80&w=250" to "Lic. 5"
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(avatarPresets.size) { i ->
                                val (url, desc) = avatarPresets[i]
                                val isSelected = viewModel.userAvatarUrl == url
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) RoyalBlue else OutlineVariantGrey.copy(alpha = 0.2f))
                                        .border(if (isSelected) 3.dp else 1.dp, if (isSelected) RoyalBlue else Color.Transparent, CircleShape)
                                        .clickable { viewModel.userAvatarUrl = url },
                                    contentAlignment = Alignment.Center
                                ) {
                                    AsyncImage(
                                        model = url,
                                        contentDescription = desc,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Custom URL field
                        OutlinedTextField(
                            value = viewModel.userAvatarUrl,
                            onValueChange = { viewModel.userAvatarUrl = it },
                            label = { Text("URL de Foto Personalizada") },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = RoyalBlue,
                                unfocusedBorderColor = OutlineVariantGrey
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Form Fields
                        OutlinedTextField(
                            value = viewModel.userName,
                            onValueChange = { viewModel.userName = it },
                            label = { Text("Nombre Completo (Lic. Usuario)") },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = RoyalBlue,
                                unfocusedBorderColor = OutlineVariantGrey
                            )
                        )

                        OutlinedTextField(
                            value = viewModel.userRole,
                            onValueChange = { viewModel.userRole = it },
                            label = { Text("Cargo o Especialidad Jurídica") },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = RoyalBlue,
                                unfocusedBorderColor = OutlineVariantGrey
                            )
                        )

                        OutlinedTextField(
                            value = viewModel.userEmail,
                            onValueChange = { viewModel.userEmail = it },
                            label = { Text("Correo Electrónico de Contacto") },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = RoyalBlue,
                                unfocusedBorderColor = OutlineVariantGrey
                            )
                        )

                        OutlinedTextField(
                            value = viewModel.userCedula,
                            onValueChange = { viewModel.userCedula = it },
                            label = { Text("Cédula / Credencial Profesional") },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = RoyalBlue,
                                unfocusedBorderColor = OutlineVariantGrey
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                Toast.makeText(context, "Información de Abogado Guardada Exitosamente", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Guardar Cambios de Perfil", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Compliance Status Bento Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Card bento 1
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(115.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, OutlineVariantGrey.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = null,
                                    tint = GoldenYellow,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "CERTIFICADO",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MutedSlate,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "Listo para Juicio",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = DeepNavy
                            )
                            Text(
                                text = "Hash Verificado: 04-2025",
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                color = MutedSlate,
                                fontSize = 10.sp
                            )
                        }
                    }

                    // Card bento 2
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(115.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, OutlineVariantGrey.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Fingerprint,
                                    contentDescription = null,
                                    tint = BioPurple,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "ACTIVO",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MutedSlate,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "Autenticación",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = DeepNavy
                            )
                            Text(
                                text = "Capa Biométrica Activa",
                                style = MaterialTheme.typography.labelSmall,
                                color = MutedSlate,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            // Section 1: IDENTIDAD Y ACCESO group list
            item {
                Text(
                    text = "IDENTIDAD Y ACCESO",
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedSlate,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, OutlineVariantGrey.copy(alpha = 0.3f))
                ) {
                    Column {
                        // Acceso Biometrico Toggle Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(BioPurple.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Face,
                                        contentDescription = null,
                                        tint = BioPurple,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Acceso Biométrico",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = DeepNavy
                                    )
                                    Text(
                                        text = "Inicio con FaceID o TouchID",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MutedSlate
                                    )
                                }
                            }

                            Switch(
                                checked = viewModel.biometricAccess,
                                onCheckedChange = { viewModel.biometricAccess = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = ActiveGreen,
                                    uncheckedThumbColor = OutlineGrey,
                                    uncheckedTrackColor = OutlineVariantGrey
                                )
                            )
                        }

                        Divider(color = OutlineVariantGrey.copy(alpha = 0.2f), thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))

                        // Digital Signature navigation item
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    Toast.makeText(context, "Certificados cargados del Colegio de Abogados", Toast.LENGTH_SHORT).show()
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(RoyalBlue.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Draw,
                                        contentDescription = null,
                                        tint = RoyalBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Gestión de Firma Digital",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = DeepNavy
                                    )
                                    Text(
                                        text = "Administrar certificados eIDAS v2.0",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MutedSlate
                                    )
                                }
                            }
                            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = OutlineGrey)
                        }
                    }
                }
            }

            // Section 2: PRIVACIDAD Y CUMPLIMIENTO group list
            item {
                Text(
                    text = "PRIVACIDAD Y CUMPLIMIENTO",
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedSlate,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, OutlineVariantGrey.copy(alpha = 0.3f))
                ) {
                    Column {
                        // ARCO list item
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    Toast.makeText(context, "Cargando Panel ARCO Seguro...", Toast.LENGTH_SHORT).show()
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(ActiveGreen.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalanceWallet,
                                        contentDescription = null,
                                        tint = ActiveGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Panel de Derechos ARCO",
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = DeepNavy
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .background(UrgentRed.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp))
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "URGENTE",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = UrgentRed,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.sp
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Estado: 20 días restantes para solicitud",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MutedSlate
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "15/20d",
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                    color = PendingOrange,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                                Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = OutlineGrey)
                            }
                        }

                        Divider(color = OutlineVariantGrey.copy(alpha = 0.2f), thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))

                        // GDPR Document item
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    Toast.makeText(context, "Descargando certificado de auditoría...", Toast.LENGTH_SHORT).show()
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(GoldenYellow.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.WorkspacePremium,
                                        contentDescription = null,
                                        tint = GoldenYellow,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Certificado de Privacidad",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = DeepNavy
                                    )
                                    Text(
                                        text = "Ver sello de cumplimiento GDPR / Normativa 2025",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MutedSlate
                                    )
                                }
                            }
                            Icon(imageVector = Icons.Default.Download, contentDescription = null, tint = OutlineGrey)
                        }
                    }
                }
            }

            // Compliance Detail Card (Blockchain retención judicial)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DeepNavy),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        // Background giant decoration gavel
                        Icon(
                            imageVector = Icons.Default.Gavel,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.05f),
                            modifier = Modifier
                                .size(130.dp)
                                .align(Alignment.CenterEnd)
                                .offset(x = 20.dp)
                        )

                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(ActiveGreen)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "ESCUDO REGULATORIO ACTIVADO",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Retención Judicial Automatizada",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Todas las interacciones de la cuenta se registran en blockchain para asegurar la cadena de custodia judicial 2025.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            Button(
                                onClick = {
                                    Toast.makeText(context, "Consultando bloque blockchain #218329...", Toast.LENGTH_LONG).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = DeepNavy),
                                shape = CircleShape
                            ) {
                                Text(
                                    text = "Ver Registro de Auditoría",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Sign Out / Cerrar Sesion Segura BUTTON
            item {
                Button(
                    onClick = {
                        Toast.makeText(context, "Sesión cerrada de forma segura. ¡Hasta luego!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = UrgentRed.copy(alpha = 0.15f),
                        contentColor = UrgentRed
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Logout, contentDescription = null, tint = UrgentRed)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Cerrar Sesión de Forma Segura",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = UrgentRed
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

// -------------------------------------------------------------
// DRAFT HELPER CONTROLS & FIELD WRAPPERS
// -------------------------------------------------------------
@Composable
fun LegalUnderlineTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MutedSlate,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 2.dp)
        )
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = OutlineGrey.copy(alpha = 0.5f), fontSize = 14.sp) },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = royalBlueGradient(),
                unfocusedIndicatorColor = OutlineVariantGrey.copy(alpha = 0.4f),
                focusedTextColor = DeepNavy,
                unfocusedTextColor = DeepNavy
            ),
            singleLine = true
        )
    }
}

@Composable
fun LegalUnderlineDropdown(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    options: List<String>,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MutedSlate,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 2.dp)
        )
        Box(modifier = Modifier.fillMaxWidth()) {
            TextField(
                value = value.ifBlank { placeholder },
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = royalBlueGradient(),
                    unfocusedIndicatorColor = OutlineVariantGrey.copy(alpha = 0.4f),
                    focusedTextColor = if (value.isBlank()) OutlineGrey.copy(alpha = 0.5f) else DeepNavy,
                    unfocusedTextColor = if (value.isBlank()) OutlineGrey.copy(alpha = 0.5f) else DeepNavy
                ),
                trailingIcon = {
                    IconButton(onClick = { expanded = true }) {
                        Icon(
                            imageVector = Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = MutedSlate
                        )
                    }
                }
            )

            // Transparent overlay touch target
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { expanded = true }
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color.White)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option, style = MaterialTheme.typography.bodyMedium, color = DeepNavy) },
                        onClick = {
                            onValueChange(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun LegalBoxTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    isTextArea: Boolean = false,
    leadingIcon: @Composable (() -> Unit)? = null
) {
    Column(modifier = modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MutedSlate,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = OutlineGrey.copy(alpha = 0.5f), fontSize = 14.sp) },
            modifier = Modifier.fillMaxWidth(),
            maxLines = if (isTextArea) 5 else 1,
            minLines = if (isTextArea) 3 else 1,
            leadingIcon = leadingIcon,
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = AccentBlue,
                unfocusedBorderColor = OutlineVariantGrey.copy(alpha = 0.4f)
            )
        )
    }
}

@Composable
fun LegalBoxDropdown(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    options: List<String>,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MutedSlate,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
        )
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = value,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.clickable { expanded = true }
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = AccentBlue,
                    unfocusedBorderColor = OutlineVariantGrey.copy(alpha = 0.4f)
                )
            )

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { expanded = true }
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color.White)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option, color = DeepNavy) },
                        onClick = {
                            onValueChange(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

fun royalBlueGradient() = RoyalBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjustesScreenNew(viewModel: LegalViewModel) {
    val context = LocalContext.current
    var showCameraSimulator by remember { mutableStateOf(false) }
    var showGallerySimulator by remember { mutableStateOf(false) }
    var selectedCameraPresetIndex by remember { mutableStateOf(0) }

    // Premium lawyer avatar presets
    val avatarPresets = listOf(
        "https://images.unsplash.com/photo-1560250097-0b93528c311a?auto=format&fit=crop&q=80&w=350" to "Lic. Carlos (Principal)",
        "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?auto=format&fit=crop&q=80&w=350" to "Lic. Sofía (Corporativo)",
        "https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?auto=format&fit=crop&q=80&w=350" to "Lic. Alejandro (Litigante)",
        "https://images.unsplash.com/photo-1580489944761-15a19d654956?auto=format&fit=crop&q=80&w=350" to "Lic. Elena (Familiar)",
        "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?auto=format&fit=crop&q=80&w=350" to "Lic. Roberto (Civil)",
        "https://images.unsplash.com/photo-1567532939604-b6b5b0db2604?auto=format&fit=crop&q=80&w=350" to "Lic. Gabriela (Penal)"
    )

    Column(modifier = Modifier.fillMaxSize()) {
        // App header containing picture and dynamic bells
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .border(1.dp, OutlineVariantGrey, CircleShape)
                    ) {
                        AsyncImage(
                            model = viewModel.userAvatarUrl,
                            contentDescription = "User Headshot Profile",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "LitigAndo en eso",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            actions = {
                IconButton(onClick = { 
                    val alertMsg = if (viewModel.allowNotifications) "No hay nuevas alertas judiciales" else "Notificaciones inhabilitadas. Actívelas en Ajustes."
                    Toast.makeText(context, alertMsg, Toast.LENGTH_SHORT).show() 
                }) {
                    Icon(
                        imageVector = if (viewModel.allowNotifications) Icons.Default.Notifications else Icons.Default.NotificationsOff, 
                        contentDescription = "Alertas", 
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Big Headers
                Text(
                    text = "Configuración General",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Ajustes de identidad, visualización y herramientas para la abogacía",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedSlate,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // USER PROFILE CARD & AVATAR EDITORS
            item {
                Text(
                    text = "PERFIL DEL ABOGADO (LIC. USUARIO)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedSlate,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, OutlineVariantGrey.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Current profile display row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(AccentBlue.copy(alpha = 0.15f))
                                    .border(2.dp, AccentBlue, CircleShape)
                            ) {
                                AsyncImage(
                                    model = viewModel.userAvatarUrl,
                                    contentDescription = "Foto Actual",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = viewModel.userName.ifBlank { "Lic. Sin Nombre" },
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepNavy
                                )
                                Text(
                                    text = viewModel.userRole.ifBlank { "Especialidad no configurada" },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = RoyalBlue,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Bufete: ${viewModel.userBufete.ifBlank { "Despacho Individual" }}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MutedSlate
                                )
                                Text(
                                    text = "Cédula: ${viewModel.userCedula.ifBlank { "No proporcionada" }}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MutedSlate
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = OutlineVariantGrey.copy(alpha = 0.2f), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(12.dp))

                        // PHOTO CHOOSER BUTTONS (SIMULATED CAMERA & GALLERY UPLOADER)
                        Text(
                            text = "ACTUALIZAR FOTO DE ABOGADO",
                            style = MaterialTheme.typography.labelSmall,
                            color = MutedSlate,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { showCameraSimulator = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                                contentPadding = PaddingValues(vertical = 10.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Camera, contentDescription = null, tint = DeepNavy, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Tomar Foto", color = DeepNavy, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { showGallerySimulator = true },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = LightSlateBackground),
                                contentPadding = PaddingValues(vertical = 10.dp),
                                border = BorderStroke(1.dp, OutlineVariantGrey.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = null, tint = DeepNavy, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Elegir Avatar", color = DeepNavy, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // LAWYER SPECIFIC PRACTICE DATA FORM (DATOS DE LA ABOGACÍA)
            item {
                Text(
                    text = "DATOS CENTRALES DE LA ABOGACÍA",
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedSlate,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, OutlineVariantGrey.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = viewModel.userName,
                            onValueChange = { viewModel.userName = it },
                            label = { Text("Nombre Completo (Lic. o Doctor)") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = RoyalBlue) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = RoyalBlue,
                                unfocusedBorderColor = OutlineVariantGrey
                            )
                        )

                        OutlinedTextField(
                            value = viewModel.userRole,
                            onValueChange = { viewModel.userRole = it },
                            label = { Text("Especialidad / Cargo Jurídico") },
                            leadingIcon = { Icon(Icons.Default.Work, contentDescription = null, tint = RoyalBlue) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = RoyalBlue,
                                unfocusedBorderColor = OutlineVariantGrey
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = viewModel.userCedula,
                                onValueChange = { viewModel.userCedula = it },
                                label = { Text("Cédula Profesional") },
                                modifier = Modifier.weight(1.1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = RoyalBlue,
                                    unfocusedBorderColor = OutlineVariantGrey
                                )
                            )

                            OutlinedTextField(
                                value = viewModel.userPhone,
                                onValueChange = { viewModel.userPhone = it },
                                label = { Text("Teléfono Legal") },
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = RoyalBlue, modifier = Modifier.size(16.dp)) },
                                modifier = Modifier.weight(1.2f),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = RoyalBlue,
                                    unfocusedBorderColor = OutlineVariantGrey
                                )
                            )
                        }

                        OutlinedTextField(
                            value = viewModel.userEmail,
                            onValueChange = { viewModel.userEmail = it },
                            label = { Text("Correo Electrónico Oficial") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = RoyalBlue) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = RoyalBlue,
                                unfocusedBorderColor = OutlineVariantGrey
                            )
                        )

                        OutlinedTextField(
                            value = viewModel.userBufete,
                            onValueChange = { viewModel.userBufete = it },
                            label = { Text("Nombre del Despacho / Bufete") },
                            leadingIcon = { Icon(Icons.Default.Business, contentDescription = null, tint = RoyalBlue) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = RoyalBlue,
                                unfocusedBorderColor = OutlineVariantGrey
                            )
                        )

                        OutlinedTextField(
                            value = viewModel.userDireccion,
                            onValueChange = { viewModel.userDireccion = it },
                            label = { Text("Domicilio Procesal / Oficina") },
                            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = RoyalBlue) },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 2,
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = RoyalBlue,
                                unfocusedBorderColor = OutlineVariantGrey
                            )
                        )

                        OutlinedTextField(
                            value = viewModel.userJurisdiccion,
                            onValueChange = { viewModel.userJurisdiccion = it },
                            label = { Text("Jurisdicción de Práctica") },
                            leadingIcon = { Icon(Icons.Default.Gavel, contentDescription = null, tint = RoyalBlue) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = RoyalBlue,
                                unfocusedBorderColor = OutlineVariantGrey
                            )
                        )

                        OutlinedTextField(
                            value = viewModel.userColegio,
                            onValueChange = { viewModel.userColegio = it },
                            label = { Text("Asociación o Colegio de Abogados") },
                            leadingIcon = { Icon(Icons.Default.AccountBalance, contentDescription = null, tint = RoyalBlue) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = RoyalBlue,
                                unfocusedBorderColor = OutlineVariantGrey
                            )
                        )

                        Button(
                            onClick = {
                                Toast.makeText(context, "Información de Abogacía Guardada Exitosamente", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Save, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Guardar Datos de Práctica", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // APP STATUS & VISUAL SYSTEM PREFERENCES (MODO DÍA/NOCHE, NOTIFICACIONES, ETC)
            item {
                Text(
                    text = "AJUSTES DE LA APLICACIÓN",
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedSlate,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, OutlineVariantGrey.copy(alpha = 0.3f))
                ) {
                    Column {
                        // TEMA SWITCH: MODO NOCHE (DARK MODE)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (viewModel.isDarkMode) {
                                                Color(0xFFFEFCFF).copy(alpha = 0.15f)
                                            } else {
                                                AccentBlue.copy(alpha = 0.1f)
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (viewModel.isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                                        contentDescription = null,
                                        tint = if (viewModel.isDarkMode) Color(0xFFFEAA20) else RoyalBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Modo Noche (Oscuro)",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = DeepNavy
                                    )
                                    Text(
                                        text = "Optimizado para juzgados y lectura nocturna",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MutedSlate
                                    )
                                }
                            }

                            Switch(
                                checked = viewModel.isDarkMode,
                                onCheckedChange = { viewModel.isDarkMode = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = GoldenYellow,
                                    uncheckedThumbColor = OutlineGrey,
                                    uncheckedTrackColor = OutlineVariantGrey
                                )
                            )
                        }

                        Divider(color = OutlineVariantGrey.copy(alpha = 0.15f), thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))

                        // ACCORD / BELL NOTIFICATION SWITCH
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(AccentBlue.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = null,
                                        tint = RoyalBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Notificaciones y Acuerdos",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = DeepNavy
                                    )
                                    Text(
                                        text = "Alertas instantáneas de boletines y federales",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MutedSlate
                                    )
                                }
                            }

                            Switch(
                                checked = viewModel.allowNotifications,
                                onCheckedChange = { viewModel.allowNotifications = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = ActiveGreen,
                                    uncheckedThumbColor = OutlineGrey,
                                    uncheckedTrackColor = OutlineVariantGrey
                                )
                            )
                        }

                        Divider(color = OutlineVariantGrey.copy(alpha = 0.15f), thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))

                        // ACCESO BIOMETRICO SWITCH
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(BioPurple.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Fingerprint,
                                        contentDescription = null,
                                        tint = BioPurple,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Acceso Biométrico Seguro",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = DeepNavy
                                    )
                                    Text(
                                        text = "Inicio con autenticación facial dactilar",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MutedSlate
                                    )
                                }
                            }

                            Switch(
                                checked = viewModel.biometricAccess,
                                onCheckedChange = { viewModel.biometricAccess = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = ActiveGreen,
                                    uncheckedThumbColor = OutlineGrey,
                                    uncheckedTrackColor = OutlineVariantGrey
                                )
                            )
                        }
                    }
                }
            }

            // SEGURIDAD & COMPLIANCE STATS ROW (BENTO BLOCKS)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Card bento 1
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(115.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, OutlineVariantGrey.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = null,
                                    tint = GoldenYellow,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "CONTRATO",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MutedSlate,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "Listo para Firmar",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = DeepNavy
                            )
                            Text(
                                text = "Certificado Blockchain 2025",
                                style = MaterialTheme.typography.labelSmall,
                                color = MutedSlate,
                                fontSize = 9.sp
                            )
                        }
                    }

                    // Card bento 2
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(115.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, OutlineVariantGrey.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = ActiveGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "ESTADO",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MutedSlate,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "Protección Real",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = DeepNavy
                            )
                            Text(
                                text = "Capa Criptográfica Militar",
                                style = MaterialTheme.typography.labelSmall,
                                color = MutedSlate,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }

            // PRIVACIDAD SECCIÓN PANEL EXPLICATIVO
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DeepNavy),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = GoldenYellow,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Aviso De Privacidad y Blindaje Legal",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Todos los datos ingresados en LitigAndo se guardan bajo cifrado local y hashes emitidos de acuerdo con los lineamientos del Colegio de Abogados y la Ley de Datos Personales en Posesión de Particulares. Su información jamás es compartida con terceros.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.padding(top = 4.dp),
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Sign Out / Cerrar Sesión Segura BUTTON
            item {
                Button(
                    onClick = {
                        Toast.makeText(context, "Sesión cerrada de forma segura. ¡Hasta pronto, Lic!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = UrgentRed.copy(alpha = 0.15f),
                        contentColor = UrgentRed
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Logout, contentDescription = null, tint = UrgentRed)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Cerrar Sesión de Forma Segura",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = UrgentRed
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // INTERACTIVE CAMERA VIEWPORT SIMULATOR DIALOG
    if (showCameraSimulator) {
        AlertDialog(
            onDismissRequest = { showCameraSimulator = false },
            confirmButton = {},
            dismissButton = {},
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Cámara de Autoretrato (Simulator)", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { showCameraSimulator = false }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Gesticule para su fotografía profesional. LitigAndo AI detecta el encuadre óptimo.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedSlate,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Finder Simulator Frame
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .clip(CircleShape)
                            .background(Color.Black)
                            .border(4.dp, AccentBlue, CircleShape)
                            .border(10.dp, Color.Black.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        // Display the selected simulated camera preset image
                        AsyncImage(
                            model = avatarPresets[selectedCameraPresetIndex].first,
                            contentDescription = "Cámara Viewfinder Preview",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Camera guidelines overlay
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // Draws center targeting crosshair
                            val r = size.minDimension / 2
                            drawCircle(
                                color = AccentBlue.copy(alpha = 0.4f),
                                radius = r - 20,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .background(Color.Black.copy(alpha = 0.6f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                .clip(RoundedCornerShape(4.dp))
                        ) {
                            Text(
                                text = "ENFOQUE AUTO",
                                color = ActiveGreen,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Buttons to change preview persona simulating "moving the camera or selecting portrait filters"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            selectedCameraPresetIndex = (selectedCameraPresetIndex + avatarPresets.size - 1) % avatarPresets.size
                        }) {
                            Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Prev", tint = RoyalBlue)
                        }
                        Text(
                            text = "Filtro Encuadre #${selectedCameraPresetIndex + 1}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = DeepNavy
                        )
                        IconButton(onClick = {
                            selectedCameraPresetIndex = (selectedCameraPresetIndex + 1) % avatarPresets.size
                        }) {
                            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Next", tint = RoyalBlue)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            viewModel.userAvatarUrl = avatarPresets[selectedCameraPresetIndex].first
                            showCameraSimulator = false
                            Toast.makeText(context, "¡Fotografía capturada e incorporada exitosamente!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = UrgentRed),
                        shape = RoundedCornerShape(25.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Camera, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("DISPARAR / TOMAR FOTO", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White
        )
    }

    // INTERACTIVE GALLERY PIX MASTER DIALOG
    if (showGallerySimulator) {
        AlertDialog(
            onDismissRequest = { showGallerySimulator = false },
            confirmButton = {},
            dismissButton = {},
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Elegir de Galería o Enlace", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { showGallerySimulator = false }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Seleccione un avatar representativo para su cuenta o ingrese una dirección URL directa.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedSlate,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Grid-like list of the options
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        avatarPresets.forEach { (url, label) ->
                            val isSelected = viewModel.userAvatarUrl == url
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) AccentBlue.copy(alpha = 0.2f) else Color.Transparent)
                                    .clickable {
                                        viewModel.userAvatarUrl = url
                                        Toast.makeText(context, "Avatar seleccionado: $label", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color.LightGray)
                                ) {
                                    AsyncImage(
                                        model = url,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = DeepNavy,
                                    modifier = Modifier.weight(1f)
                                )
                                if (isSelected) {
                                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Seleccionado", tint = RoyalBlue, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = OutlineVariantGrey.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = viewModel.userAvatarUrl,
                        onValueChange = { viewModel.userAvatarUrl = it },
                        label = { Text("URL de Foto Personalizada") },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RoyalBlue,
                            unfocusedBorderColor = OutlineVariantGrey
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { showGallerySimulator = false },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Confirmar Selección", color = Color.White)
                    }
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CuestionarioAntecedentesScreen(viewModel: LegalViewModel) {
    val context = LocalContext.current
    var showAddHitoDialog by remember { mutableStateOf(false) }
    var nuevoHitoFecha by remember { mutableStateOf("") }
    var nuevoHitoDesc by remember { mutableStateOf("") }

    var showAddDocDialog by remember { mutableStateOf(false) }
    var nuevoDocNombre by remember { mutableStateOf("") }
    var nuevoDocInfo by remember { mutableStateOf("Cargado el 12/10/2023 • 1.5 MB") }
    var nuevoDocCertificado by remember { mutableStateOf(true) }

    var showJurisdiccionDropdown by remember { mutableStateOf(false) }
    val jurisdicciones = listOf("Civil y Comercial", "Familiar", "Penal", "Laboral", "Federal", "Administrativo")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightSlateBackground)
    ) {
        // TOP BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { viewModel.currentTab = "alta_expediente" },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "Volver",
                        tint = RoyalBlue,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Text(
                    text = "Atrás",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = RoyalBlue,
                    modifier = Modifier.clickable { viewModel.currentTab = "alta_expediente" }
                )
                Spacer(modifier = Modifier.width(16.dp))
                val caseTitle = if (viewModel.caseCliente.isNotBlank()) "${viewModel.caseCliente} vs. Contraparte" else "Anderson vs. Global Lo..."
                Text(
                    text = if (caseTitle.length > 25) caseTitle.take(22) + "..." else caseTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DeepNavy
                )
            }
            IconButton(onClick = { 
                Toast.makeText(context, "Opciones de antecedente procesal", Toast.LENGTH_SHORT).show()
            }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Opciones",
                    tint = MutedSlate
                )
            }
        }

        // MAIN BODY
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Cuestionario de Antecedentes",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = DeepNavy
                )
                Text(
                    text = "Complete el expediente operativo para blindar la estrategia legal.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedSlate,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // 1. Antecedentes Procesales
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(AccentBlue.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Gavel,
                                    contentDescription = null,
                                    tint = RoyalBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "1. Antecedentes Procesales",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = DeepNavy
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))

                        // Historia del litigio
                        Text(
                            text = "HISTORIA DEL LITIGIO",
                            style = MaterialTheme.typography.labelSmall,
                            color = MutedSlate,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        OutlinedTextField(
                            value = viewModel.antecedentesHistorial,
                            onValueChange = { viewModel.antecedentesHistorial = it },
                            placeholder = { Text("Describa brevemente la trayectoria judicial previa...", color = OutlineGrey.copy(alpha = 0.4f), fontSize = 14.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            maxLines = 5,
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = SurfaceLow,
                                unfocusedContainerColor = SurfaceLow,
                                focusedBorderColor = AccentBlue.copy(alpha = 0.5f),
                                unfocusedBorderColor = Color.Transparent
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // N° Expediente Inicial
                        Text(
                            text = "N° EXPEDIENTE INICIAL",
                            style = MaterialTheme.typography.labelSmall,
                            color = MutedSlate,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        OutlinedTextField(
                            value = viewModel.antecedentesExpedienteInicial,
                            onValueChange = { viewModel.antecedentesExpedienteInicial = it },
                            placeholder = { Text("EXP-2023-0045", color = OutlineGrey.copy(alpha = 0.4f), fontSize = 14.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = SurfaceLow,
                                unfocusedContainerColor = SurfaceLow,
                                focusedBorderColor = AccentBlue.copy(alpha = 0.5f),
                                unfocusedBorderColor = Color.Transparent
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Jurisdicción Dropdown
                        Text(
                            text = "JURISDICCIÓN",
                            style = MaterialTheme.typography.labelSmall,
                            color = MutedSlate,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = viewModel.antecedentesJurisdiccion,
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showJurisdiccionDropdown = true },
                                shape = RoundedCornerShape(8.dp),
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Desplegar",
                                        tint = RoyalBlue,
                                        modifier = Modifier.clickable { showJurisdiccionDropdown = true }
                                    )
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = SurfaceLow,
                                    unfocusedContainerColor = SurfaceLow,
                                    focusedBorderColor = AccentBlue.copy(alpha = 0.5f),
                                    unfocusedBorderColor = Color.Transparent,
                                    disabledBorderColor = Color.Transparent,
                                    disabledContainerColor = SurfaceLow,
                                    disabledTextColor = DeepNavy
                                ),
                                enabled = false
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable { showJurisdiccionDropdown = true }
                            )
                            DropdownMenu(
                                expanded = showJurisdiccionDropdown,
                                onDismissRequest = { showJurisdiccionDropdown = false },
                                modifier = Modifier.background(Color.White)
                            ) {
                                jurisdicciones.forEach { jur ->
                                    DropdownMenuItem(
                                        text = { Text(jur, color = DeepNavy) },
                                        onClick = {
                                            viewModel.antecedentesJurisdiccion = jur
                                            showJurisdiccionDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Hechos Relevantes
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(AccentBlue.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    tint = RoyalBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "2. Hechos Relevantes",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = DeepNavy,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CRONOLOGÍA DE SUCESOS",
                                style = MaterialTheme.typography.labelSmall,
                                color = MutedSlate,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                modifier = Modifier.clickable { showAddHitoDialog = true },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddCircle,
                                    contentDescription = null,
                                    tint = RoyalBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "AÑADIR HITO",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = RoyalBlue,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        viewModel.antecedentesHitos.forEachIndexed { index, hito ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.width(24.dp)
                                ) {
                                    val isBlueDot = hito.fecha.isNotBlank() && hito.fecha != "dd/mm/aaaa"
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clip(CircleShape)
                                            .background(if (isBlueDot) AccentBlue else OutlineVariantGrey)
                                            .border(
                                                width = 3.dp,
                                                color = if (isBlueDot) AccentBlue.copy(alpha = 0.2f) else Color.White,
                                                shape = CircleShape
                                            )
                                    )
                                    if (index < viewModel.antecedentesHitos.size - 1) {
                                        Box(
                                            modifier = Modifier
                                                .width(2.dp)
                                                .height(60.dp)
                                                .background(OutlineVariantGrey.copy(alpha = 0.5f))
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))

                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(bottom = 12.dp),
                                    colors = CardDefaults.cardColors(containerColor = SurfaceLow.copy(alpha = 0.6f)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.CalendarToday,
                                                    contentDescription = null,
                                                    tint = MutedSlate,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = hito.fecha.ifBlank { "dd/mm/aaaa" },
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (hito.fecha.isBlank() || hito.fecha == "dd/mm/aaaa") MutedSlate.copy(alpha = 0.7f) else DeepNavy
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = hito.descripcion,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = DeepNavy
                                            )
                                        }
                                        IconButton(onClick = {
                                            viewModel.antecedentesHitos.removeAt(index)
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Eliminar Hito",
                                                tint = UrgentRed.copy(alpha = 0.7f),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. Pruebas Existentes
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(AccentBlue.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    tint = RoyalBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "3. Pruebas Existentes",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = DeepNavy,
                                modifier = Modifier.weight(1f)
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(GoldenYellow.copy(alpha = 0.12f))
                                    .border(1.dp, GoldenYellow.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(GoldenYellow)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "HASH JUDICIAL",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = GoldenYellow,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))

                        viewModel.antecedentesDocumentos.forEachIndexed { index, doc ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 10.dp)
                                    .border(BorderStroke(1.dp, OutlineVariantGrey.copy(alpha = 0.2f)), shape = RoundedCornerShape(12.dp))
                                    .background(Color.White)
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (doc.nombre.endsWith(".pdf")) Color(0xFFFFECEB) else Color(0xFFEBF5FF)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (doc.nombre.endsWith(".pdf")) Icons.Default.PictureAsPdf else Icons.Default.Image,
                                        contentDescription = null,
                                        tint = if (doc.nombre.endsWith(".pdf")) Color(0xFFD32F2F) else RoyalBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = doc.nombre,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = DeepNavy
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = doc.info,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MutedSlate,
                                            fontSize = 11.sp
                                        )
                                        if (doc.certificado) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = "Certificado",
                                                tint = ActiveGreen,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        } else {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Icon(
                                                imageVector = Icons.Default.Info,
                                                contentDescription = "Pendiente",
                                                tint = PendingOrange,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                                IconButton(onClick = {
                                    viewModel.antecedentesDocumentos.removeAt(index)
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Borrar documento",
                                        tint = UrgentRed.copy(alpha = 0.6f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceLow.copy(alpha = 0.5f))
                                .clickable { showAddDocDialog = true }
                                .border(1.dp, OutlineVariantGrey.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CloudUpload,
                                    contentDescription = null,
                                    tint = RoyalBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Subir nuevo documento probatorio",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = RoyalBlue,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Live Google Drive Sync Dashboard inside the Case Details!
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = AccentBlue.copy(alpha = 0.05f)),
                            border = BorderStroke(1.dp, AccentBlue.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.CloudQueue,
                                            contentDescription = "Google Drive",
                                            tint = RoyalBlue,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Google Drive Virtual",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = RoyalBlue
                                        )
                                    }
                                    
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(ActiveGreen.copy(alpha = 0.1f))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Sincronizado",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = ActiveGreen,
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text(
                                    text = "Ruta de Respaldos: LitigAndo / ${viewModel.antecedentesDemandante.ifBlank { "Thomas Anderson" }} / ${viewModel.antecedentesExpedienteInicial.ifBlank { "EXP-2023-0045" }} /",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MutedSlate,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                // List virtual files in this google drive subfolders
                                val caseFiles = viewModel.driveFiles.filter {
                                    it.cliente == viewModel.antecedentesDemandante && it.caseNumero == viewModel.antecedentesExpedienteInicial
                                }
                                
                                if (caseFiles.isEmpty()) {
                                    Text(
                                        text = "Carpeta vacía en Drive. Registre un archivo arriba para iniciar sincronización en la nube.",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MutedSlate,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        caseFiles.forEach { df ->
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(Color.White, RoundedCornerShape(8.dp))
                                                    .border(1.dp, OutlineVariantGrey.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                                    .padding(8.dp)
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.InsertDriveFile,
                                                        contentDescription = null,
                                                        tint = RoyalBlue,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = df.nombreColumna,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = DeepNavy,
                                                        fontSize = 11.sp
                                                    )
                                                }
                                                Text(
                                                    text = df.size,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MutedSlate,
                                                    fontSize = 10.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4. Partes Involucradas
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(AccentBlue.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Group,
                                    contentDescription = null,
                                    tint = RoyalBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "4. Partes Involucradas",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = DeepNavy
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = SurfaceLow.copy(alpha = 0.7f)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(DeepNavy),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("A", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Demandante",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = DeepNavy
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = viewModel.antecedentesDemandante,
                                    onValueChange = { viewModel.antecedentesDemandante = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(6.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White,
                                        focusedBorderColor = AccentBlue.copy(alpha = 0.4f),
                                        unfocusedBorderColor = Color.Transparent
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "REPRESENTACIÓN: ${viewModel.antecedentesRepresentacion.uppercase()}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MutedSlate,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    modifier = Modifier.padding(start = 2.dp, top = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = AccentBlue.copy(alpha = 0.05f)),
                            border = BorderStroke(1.dp, AccentBlue.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(RoyalBlue),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("G", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Contraparte (Demandado)",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = RoyalBlue
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = viewModel.antecedentesContraparte,
                                    onValueChange = { viewModel.antecedentesContraparte = it },
                                    placeholder = { Text("Nombre de la contraparte...", color = OutlineGrey.copy(alpha = 0.4f), fontSize = 14.sp) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(6.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White,
                                        focusedBorderColor = AccentBlue.copy(alpha = 0.4f),
                                        unfocusedBorderColor = Color.Transparent
                                    )
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Fingerprint,
                                        contentDescription = null,
                                        tint = BioPurple,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Sujeto a verificación biométrica",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = BioPurple,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Save elements
            item {
                Button(
                    onClick = {
                        viewModel.caseAntecedentes = true
                        viewModel.currentTab = "alta_expediente"
                        Toast.makeText(context, "Antecedentes Procesales Guardados Exitosamente", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DeepNavy),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = "Guardar",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Guardar Antecedentes",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Al guardar, los datos se encriptarán y se generará un hash de integridad para validez judicial.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedSlate,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // DIALOG ADDS HITO
    if (showAddHitoDialog) {
        AlertDialog(
            onDismissRequest = { showAddHitoDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    if (nuevoHitoDesc.isNotBlank()) {
                        viewModel.antecedentesHitos.add(
                            TimelineHito(
                                fecha = nuevoHitoFecha.ifBlank { "dd/mm/aaaa" },
                                descripcion = nuevoHitoDesc
                            )
                        )
                        nuevoHitoFecha = ""
                        nuevoHitoDesc = ""
                        showAddHitoDialog = false
                        Toast.makeText(context, "Hito agregado a la cronología", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text("Aceptar", color = RoyalBlue, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddHitoDialog = false }) {
                    Text("Cancelar", color = MutedSlate)
                }
            },
            title = { Text("Añadir Hito Cronológico", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = DeepNavy) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = nuevoHitoFecha,
                        onValueChange = { nuevoHitoFecha = it },
                        label = { Text("Fecha (e.g. 12/10/2023)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = nuevoHitoDesc,
                        onValueChange = { nuevoHitoDesc = it },
                        label = { Text("Descripción del Evento") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // DIALOG ADDS DOCUMENT
    if (showAddDocDialog) {
        AlertDialog(
            onDismissRequest = { showAddDocDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    if (nuevoDocNombre.isNotBlank()) {
                        viewModel.antecedentesDocumentos.add(
                            DocumentoPrueba(
                                nombre = nuevoDocNombre,
                                info = nuevoDocInfo,
                                certificado = nuevoDocCertificado
                            )
                        )
                        
                        val clientName = viewModel.antecedentesDemandante.ifBlank { "Thomas Anderson" }
                        val caseNo = viewModel.antecedentesExpedienteInicial.ifBlank { "EXP-2023-0045" }
                        viewModel.driveFiles.add(
                            GoogleDriveFile(
                                nombreColumna = nuevoDocNombre,
                                cliente = clientName,
                                caseNumero = caseNo,
                                size = nuevoDocInfo.ifBlank { "1.5 MB" }
                            )
                        )
                        Toast.makeText(context, "Sincronizado a Google Drive:\nLitigAndo / $clientName / $caseNo / $nuevoDocNombre", Toast.LENGTH_LONG).show()
                        
                        nuevoDocNombre = ""
                        showAddDocDialog = false
                    }
                }) {
                    Text("Guardar", color = RoyalBlue, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDocDialog = false }) {
                    Text("Cancelar", color = MutedSlate)
                }
            },
            title = { Text("Registrar Documento Probatorio", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = DeepNavy) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = nuevoDocNombre,
                        onValueChange = { nuevoDocNombre = it },
                        label = { Text("Nombre del Documento (con extensión)") },
                        placeholder = { Text("ejemplo.pdf") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = nuevoDocInfo,
                        onValueChange = { nuevoDocInfo = it },
                        label = { Text("Información / Tamaño") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("¿Está Certificado Judicialmente?", style = MaterialTheme.typography.bodyMedium, color = DeepNavy)
                        Switch(
                            checked = nuevoDocCertificado,
                            onCheckedChange = { nuevoDocCertificado = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = ActiveGreen,
                                uncheckedThumbColor = OutlineGrey,
                                uncheckedTrackColor = OutlineVariantGrey
                            )
                        )
                    }
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

// -------------------------------------------------------------
// NEW GOOGLE SIGN/UP & ACCOUNT VINCULATION SCREEN (HIGH FIDELITY)
// -------------------------------------------------------------
@Composable
fun GoogleLoginScreen(viewModel: LegalViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showAccountSelection by remember { mutableStateOf(false) }
    var selectedEmail by remember { mutableStateOf("enazulyrojo@gmail.com") }
    var customEmailInput by remember { mutableStateOf("") }
    var selectCustom by remember { mutableStateOf(false) }
    var isVerifying by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(DeepNavy, Color(0xFF0F1E36))
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Immersive Google Partner Icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(2.dp, AccentBlue, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "G",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 42.sp
                    ),
                    color = RoyalBlue
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "LitigAndo en eso",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = AccentBlue,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Activación y Registro Único",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Vincule su cuenta de Google para activar el almacenamiento judicial y desatar el poder del correo electrónico corporativo.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Integration Features Cards
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                LoginFeatureCard(
                    icon = Icons.Default.CloudQueue,
                    iconColor = GoldenYellow,
                    title = "Google Drive Corporativo",
                    desc = "Cree expedientes digitales con subcarpetas automáticas basadas en Cliente y Caso. Suba antecedentes procesales de inmediato."
                )

                LoginFeatureCard(
                    icon = Icons.Default.Email,
                    iconColor = AccentBlue,
                    title = "Consola de Gmail Integrada",
                    desc = "Borradores redactados por Mi Chalán AI listos para revisar, guardar en borradores, o enviar directamente a juzgados y clientes."
                )

                LoginFeatureCard(
                    icon = Icons.Default.Lock,
                    iconColor = ActiveGreen,
                    title = "Validación de Identidad Gmail",
                    desc = "Vincule su correo para asegurar accesibilidad cifrada a su portafolio litigante y evitar accesos no autorizados."
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Beautiful Google OAuth Trigger Button
            Button(
                onClick = { showAccountSelection = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White
                ),
                shape = RoundedCornerShape(27.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Google colored icon simulation
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    ) {
                        Text(
                            text = "G",
                            fontWeight = FontWeight.ExtraBold,
                            color = RoyalBlue,
                            fontSize = 18.sp,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Vincular con Google / Gmail",
                        style = MaterialTheme.typography.bodyMedium,
                        color = DeepNavy,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "LitigAndo utiliza Google APIs con cifrado SSL de extremo a extremo. Al continuar, usted acepta los términos de licencia judicial.",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // Processing / Vinculando overlay
        if (isVerifying) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.75f))
                    .clickable(enabled = false) {},
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = AccentBlue, strokeWidth = 3.dp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Activando cuenta de Google...",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Text(
                        text = "Sincronizando Google Drive y Gmail",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }

    // Google Account Chooser bottom dialog
    if (showAccountSelection) {
        AlertDialog(
            onDismissRequest = { showAccountSelection = false },
            confirmButton = {
                Button(
                    onClick = {
                        val finalEmail = if (selectCustom) customEmailInput else selectedEmail
                        if (finalEmail.isBlank() || !finalEmail.contains("@")) {
                            Toast.makeText(context, "Favor de ingresar un Gmail válido", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        showAccountSelection = false
                        isVerifying = true
                        
                        // Fake verifying for great realistic feedback
                        coroutineScope.launch {
                            delay(1000)
                            viewModel.loggedInEmail = finalEmail
                            viewModel.loggedInName = if (finalEmail.startsWith("enazul")) "Socio Principal" else "Abogado Asociado"
                            viewModel.userEmail = finalEmail
                            viewModel.userName = if (finalEmail.startsWith("enazul")) "Lic. Carlos Gómez" else "Lic. Abogado Asociado"
                            viewModel.isUserLoggedIn = true
                            isVerifying = false
                            Toast.makeText(context, "¡Sesión Vinculada con Éxito!", Toast.LENGTH_LONG).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Confirmar Vinculación", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAccountSelection = false }) {
                    Text("Cancelar", color = MutedSlate)
                }
            },
            title = {
                Column {
                    Text(
                        text = "Seleccionar cuenta de Google",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = DeepNavy
                    )
                    Text(
                        text = "para continuar a LitigAndo en eso",
                        style = MaterialTheme.typography.labelSmall,
                        color = MutedSlate
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Option 1: enazulyrojo@gmail.com (detected email from platform)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (!selectCustom) AccentBlue.copy(alpha = 0.1f) else Color.White)
                            .border(
                                1.dp,
                                if (!selectCustom) AccentBlue else OutlineVariantGrey.copy(alpha = 0.4f),
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { selectCustom = false }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(RoyalBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("E", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Abogado Principal",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = DeepNavy
                            )
                            Text(
                                text = "enazulyrojo@gmail.com",
                                style = MaterialTheme.typography.labelSmall,
                                color = MutedSlate
                            )
                        }
                    }

                    // Option 2: Custom input
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selectCustom) AccentBlue.copy(alpha = 0.1f) else Color.White)
                            .border(
                                1.dp,
                                if (selectCustom) AccentBlue else OutlineVariantGrey.copy(alpha = 0.4f),
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { selectCustom = true }
                            .padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AddCircle,
                                contentDescription = null,
                                tint = if (selectCustom) RoyalBlue else OutlineGrey,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Vincular otra cuenta Gmail",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (selectCustom) RoyalBlue else DeepNavy
                            )
                        }

                        if (selectCustom) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = customEmailInput,
                                onValueChange = { customEmailInput = it },
                                placeholder = { Text("abogado@gmail.com") },
                                label = { Text("Correo Gmail") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Al vincular su cuenta, Google compartirá legalmente su nombre, la dirección de correo y la foto de perfil con LitigAndo en eso.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MutedSlate,
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun LoginFeatureCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    title: String,
    desc: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier
                    .size(28.dp)
                    .padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = desc,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.7f),
                    lineHeight = 15.sp
                )
            }
        }
    }
}

// -------------------------------------------------------------
// NEW GMAIL WRITER & INTEGRATED CLIENT SCREEN (HIGH FIDELITY)
// -------------------------------------------------------------
@Composable
fun GmailScreen(viewModel: LegalViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedFolder by remember { mutableStateOf("Recibidos") }
    var searchQuery by remember { mutableStateOf("") }
    
    // Email compose form state
    var composeTo by remember { mutableStateOf("") }
    var composeSubject by remember { mutableStateOf("") }
    var composeBody by remember { mutableStateOf("") }
    var isSendingMail by remember { mutableStateOf(false) }

    // Read details selected state
    val selectedMailState = remember { mutableStateOf<GmailEmail?>(null) }
    val selectedMailForView = selectedMailState.value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightSlateBackground)
    ) {
        // TOP GMAIL CLOUD BAR MATCHING CORPORATE LUXURY
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .background(DeepNavy)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Text("M", fontWeight = FontWeight.Bold, color = RoyalBlue, fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Gmail Workspace",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = viewModel.loggedInEmail,
                        style = MaterialTheme.typography.labelSmall,
                        color = AccentBlue,
                        fontSize = 11.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(ActiveGreen.copy(alpha = 0.2f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(ActiveGreen)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "En Línea",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = ActiveGreen,
                        fontSize = 10.sp
                    )
                }
            }
        }

        // Folder pills selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(vertical = 10.dp, horizontal = 12.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val folders = listOf("Recibidos", "Borradores", "Enviados", "Redactar")
            folders.forEach { folder ->
                val isSelected = selectedFolder == folder
                val countUnread = if (folder == "Recibidos") viewModel.gmailEmails.count { it.estado == "Recibidos" && !it.leido } else 0

                FilterChip(
                    selected = isSelected,
                    onClick = { selectedFolder = folder },
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(folder, fontWeight = FontWeight.Bold)
                            if (countUnread > 0) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(UrgentRed),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = countUnread.toString(),
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = RoyalBlue,
                        selectedLabelColor = Color.White,
                        containerColor = LightSlateBackground,
                        labelColor = DeepNavy
                    )
                )
            }
        }

        // BODY AREA CONTROLS
        if (selectedFolder == "Redactar") {
            // COMPOSE MAIL INTERFACE
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.04f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Redactar Nuevo Mensaje",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = DeepNavy
                        )

                        Divider(color = OutlineVariantGrey.copy(alpha = 0.4f))

                        OutlinedTextField(
                            value = viewModel.loggedInEmail,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("De") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = composeTo,
                            onValueChange = { composeTo = it },
                            label = { Text("Para (Destinatario)") },
                            placeholder = { Text("notificaciones@tribunal.gob.mx") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = composeSubject,
                            onValueChange = { composeSubject = it },
                            label = { Text("Asunto") },
                            placeholder = { Text("Escrito desahogando prevención / Contrato de Arrendamiento") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = composeBody,
                            onValueChange = { composeBody = it },
                            label = { Text("Mensaje") },
                            placeholder = { Text("Escriba aquí el contenido legal de su correo o borrador...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            maxLines = 10
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = {
                                    if (composeTo.isBlank() && composeSubject.isBlank()) {
                                        Toast.makeText(context, "Ingrese mínimo destinatario o asunto", Toast.LENGTH_SHORT).show()
                                        return@TextButton
                                    }
                                    viewModel.gmailEmails.add(
                                        GmailEmail(
                                            remitente = viewModel.loggedInEmail,
                                            destinatario = composeTo,
                                            asunto = composeSubject.ifBlank { "(Sin Asunto)" },
                                            cuerpo = composeBody,
                                            fecha = "Hoy, " + java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(java.util.Date()),
                                            estado = "Borradores"
                                        )
                                    )
                                    Toast.makeText(context, "✓ Mensaje guardado en Borradores", Toast.LENGTH_SHORT).show()
                                    // Reset & switch
                                    composeTo = ""
                                    composeSubject = ""
                                    composeBody = ""
                                    selectedFolder = "Borradores"
                                },
                                enabled = !isSendingMail
                            ) {
                                Icon(imageVector = Icons.Default.Save, contentDescription = null, tint = RoyalBlue)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Guardar Borrador", color = RoyalBlue)
                            }

                            Button(
                                onClick = {
                                    if (composeTo.isBlank()) {
                                        Toast.makeText(context, "Favor de rellenar el destinatario", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    isSendingMail = true
                                    
                                    coroutineScope.launch {
                                        delay(1000)
                                        viewModel.gmailEmails.add(
                                            GmailEmail(
                                                remitente = viewModel.loggedInEmail,
                                                destinatario = composeTo,
                                                asunto = composeSubject.ifBlank { "Asunto Legal Urgente" },
                                                cuerpo = composeBody,
                                                fecha = "Hoy, " + java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(java.util.Date()),
                                                estado = "Enviados"
                                            )
                                        )
                                        isSendingMail = false
                                        Toast.makeText(context, "✈ Correo enviado con éxito por Gmail", Toast.LENGTH_LONG).show()
                                        composeTo = ""
                                        composeSubject = ""
                                        composeBody = ""
                                        selectedFolder = "Enviados"
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                                shape = RoundedCornerShape(10.dp),
                                enabled = !isSendingMail
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isSendingMail) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Enviando...", color = Color.White)
                                    } else {
                                        Icon(imageVector = Icons.Default.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Enviar Correo", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // LIST VIEW OF SEARCH & POSTS FOR INBOX, DRAFTS, SENT
            Column(modifier = Modifier.weight(1f)) {
                // Internal Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar por remitente, asunto o contenido...") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = OutlineGrey) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                        .background(Color.White, RoundedCornerShape(10.dp)),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RoyalBlue,
                        unfocusedBorderColor = OutlineVariantGrey.copy(alpha = 0.1f)
                    ),
                    singleLine = true
                )

                val filteredList = viewModel.gmailEmails.filter {
                    it.estado == selectedFolder && (
                        it.remitente.contains(searchQuery, ignoreCase = true) ||
                        it.destinatario.contains(searchQuery, ignoreCase = true) ||
                        it.asunto.contains(searchQuery, ignoreCase = true) ||
                        it.cuerpo.contains(searchQuery, ignoreCase = true)
                    )
                }

                if (filteredList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = OutlineGrey.copy(alpha = 0.5f), modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Bandeja de '$selectedFolder' Vacía", style = MaterialTheme.typography.bodyMedium, color = MutedSlate)
                            Text("No se detectó ningún correo electrónico", style = MaterialTheme.typography.labelSmall, color = MutedSlate.copy(alpha = 0.7f))
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredList) { mail ->
                            val isUnreadIncoming = mail.estado == "Recibidos" && !mail.leido
                            
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedMailState.value = mail
                                        // Mark read automatically
                                        if (mail.estado == "Recibidos" && !mail.leido) {
                                            val index = viewModel.gmailEmails.indexOfFirst { it.id == mail.id }
                                            if (index != -1) {
                                                viewModel.gmailEmails[index] = mail.copy(leido = true)
                                            }
                                        }
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isUnreadIncoming) Color(0xFFF1F5FB) else Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(
                                    1.dp,
                                    if (isUnreadIncoming) AccentBlue.copy(alpha = 0.3f) else Color.Black.copy(alpha = 0.03f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Circular Initial badge representing sender
                                    val senderLabel = if (mail.estado == "Recibidos") mail.remitente else mail.destinatario
                                    val badgeInitial = senderLabel.take(2).uppercase()
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isUnreadIncoming) RoyalBlue else OutlineGrey.copy(alpha = 0.15f)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = badgeInitial,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (isUnreadIncoming) Color.White else DeepNavy
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = if (mail.estado == "Recibidos") mail.remitente.substringBefore("@") else "Para: " + mail.destinatario.substringBefore("@"),
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = if (isUnreadIncoming) FontWeight.Bold else FontWeight.SemiBold,
                                                color = DeepNavy,
                                                maxLines = 1
                                            )
                                            Text(
                                                text = mail.fecha,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MutedSlate,
                                                fontSize = 9.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = mail.asunto,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = if (isUnreadIncoming) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isUnreadIncoming) RoyalBlue else DeepNavy,
                                            maxLines = 1
                                        )
                                        Spacer(modifier = Modifier.height(1.dp))
                                        Text(
                                            text = mail.cuerpo,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MutedSlate,
                                            maxLines = 1,
                                            fontSize = 11.sp
                                        )
                                    }

                                    if (isUnreadIncoming) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(RoyalBlue)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // GMAIL EMAIL READER MODAL
    if (selectedMailForView != null) {
        val mail = selectedMailForView
        AlertDialog(
            onDismissRequest = { selectedMailState.value = null },
            confirmButton = {
                // Action Reply
                Button(
                    onClick = {
                        composeTo = if (mail.estado == "Recibidos") mail.remitente else mail.destinatario
                        composeSubject = "RE: " + mail.asunto
                        composeBody = "\n\n-----Mensaje Original-----\nDe: ${mail.remitente}\nAsunto: ${mail.asunto}\n\n" + mail.cuerpo
                        selectedFolder = "Redactar"
                        selectedMailState.value = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Reply, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Responder")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        // Trash function
                        viewModel.gmailEmails.remove(mail)
                        selectedMailState.value = null
                        Toast.makeText(context, "Correo eliminado de Gmail", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = UrgentRed, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Eliminar", color = UrgentRed)
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = RoyalBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Correo de de Gmail",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = DeepNavy
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = LightSlateBackground),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "De: ${mail.remitente}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = DeepNavy
                            )
                            Text(
                                text = "Para: ${mail.destinatario}",
                                style = MaterialTheme.typography.labelSmall,
                                color = DeepNavy
                            )
                            Text(
                                text = "Fecha: ${mail.fecha}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MutedSlate
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Asunto: " + mail.asunto,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = RoyalBlue
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = mail.cuerpo,
                        style = MaterialTheme.typography.bodySmall,
                        color = DeepNavy,
                        lineHeight = 20.sp,
                        textAlign = TextAlign.Justify,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
