package com.example.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class DraftActivity(
    val id: Long = System.nanoTime(),
    var descripcion: String = "",
    var fechaInicio: String = "",
    var fechaFin: String = "",
    var verificacionRequerida: Boolean = true
)

data class ChatMessage(
    val id: Long = System.nanoTime(),
    val text: String,
    val isUser: Boolean,
    val timestamp: String
)

data class TimelineHito(
    val id: Long = System.nanoTime(),
    var fecha: String = "dd/mm/aaaa",
    var descripcion: String = "Evento principal..."
)

data class DocumentoPrueba(
    val id: Long = System.nanoTime(),
    val nombre: String,
    val info: String,
    val certificado: Boolean
)

data class GoogleDriveFile(
    val id: String = "drive_" + System.nanoTime(),
    val nombreColumna: String,       // Name of the file
    val cliente: String,             // Folder level 1: Cliente
    val caseNumero: String,          // Folder level 2: Case Numero o Expediente
    val size: String = "1.5 MB",     // Size
    val mimeType: String = "application/pdf",
    val fechaSubida: String = "07/06/2026",
    val esDeChalan: Boolean = false  // If created by Mi Chalán
)

data class GmailEmail(
    val id: String = "mail_" + System.nanoTime(),
    val remitente: String,
    val destinatario: String,
    val asunto: String,
    val cuerpo: String,
    val fecha: String,
    val estado: String = "Recibidos", // "Recibidos", "Borradores", "Enviados", "Papelera"
    val leido: Boolean = false
)

class LegalViewModel(application: Application) : AndroidViewModel(application) {
    val repository: LegalRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = LegalRepository(database.legalDao())
        viewModelScope.launch {
            repository.prepopulateIfEmpty()
        }
    }

    // Reactive Flow Streams
    val colaboradores: StateFlow<List<ColaboradorEntity>> = repository.colaboradores
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val actividadesProgramadas: StateFlow<List<ActividadProgramadaEntity>> = repository.actividadesProgramadas
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val expedientes: StateFlow<List<ExpedienteEntity>> = repository.expedientes
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Current navigation state: "inicio", "expedientes", "agenda", "chalan", "ajustes"
    var currentTab by mutableStateOf("agenda")

    // --- GOOGLE INTEGRATION STATE ---
    var isUserLoggedIn by mutableStateOf(false)
    var loggedInEmail by mutableStateOf("enazulyrojo@gmail.com")
    var loggedInName by mutableStateOf("Abogado Principal")
    var loggedInPhotoUrl by mutableStateOf("https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&q=80&w=250")

    val driveFiles = mutableStateListOf<GoogleDriveFile>().apply {
        add(GoogleDriveFile(nombreColumna = "Contrato_Servicios_v2.pdf", cliente = "Thomas Anderson", caseNumero = "EXP-2023-0045", size = "2.4 MB", fechaSubida = "12/10/2023"))
        add(GoogleDriveFile(nombreColumna = "Evidencia_Fisica_01.jpg", cliente = "Thomas Anderson", caseNumero = "EXP-2023-0045", size = "1.1 MB", fechaSubida = "12/10/2023"))
    }

    val gmailEmails = mutableStateListOf<GmailEmail>().apply {
        add(GmailEmail(
            remitente = "notificaciones@tribunalsupremo.gob.mx",
            destinatario = "enazulyrojo@gmail.com",
            asunto = "ACUERDO PUBLICADO - Exp. 456/2025 - Juzgado 3° Distrito",
            cuerpo = "Estimado Abogado, se le notifica que se ha dictado un nuevo acuerdo de prevención en el expediente de referencia. Cuenta con un término de 3 días hábiles para su desahogo legal correspondiente.",
            fecha = "Hoy, 10:24 AM",
            estado = "Recibidos",
            leido = false
        ))
        add(GmailEmail(
            remitente = "soporte@google.com",
            destinatario = "enazulyrojo@gmail.com",
            asunto = "Activación Exitosa - Google Workspace en LitigAndo",
            cuerpo = "Estimado usuario, su cuenta de Gmail y Google Drive se han vinculado satisfactoriamente a LitigAndo en eso. Ahora puede almacenar documentos procesales y redactar borradores automáticos.",
            fecha = "Hoy, 09:15 AM",
            estado = "Recibidos",
            leido = true
        ))
        add(GmailEmail(
            remitente = "cliente@correo.com",
            destinatario = "enazulyrojo@gmail.com",
            asunto = "Borrador de Convenio de Confidencialidad",
            cuerpo = "Estimado de Licenciado, adjunto contrato para su revisión y aprobación final antes de subirlo a Drive.",
            fecha = "Ayer, 04:30 PM",
            estado = "Borradores"
        ))
    }

    // --- MI CHALÁN AI ASSISTANT STATE ---
    var chalanTabMode by mutableStateOf("asistente") // "asistente" or "colaboradores"
    var chalanInputMessage by mutableStateOf("")
    var chalanSelectedCase by mutableStateOf("Caso #9821 - Ramirez v. Estado")
    var chalanIsLoading by mutableStateOf(false)
    val chalanMessages = mutableStateListOf<ChatMessage>().apply {
        add(ChatMessage(
            text = "¡Hola Lic.! Soy su Chalán digital, su auxiliar jurídico de cabecera. ⚖️🤖\n\nEstoy coordinado con sus expedientes y su agenda de **'LitigAndo en eso'**.\n" +
                   "Puedo ayudarle con:\n" +
                   "• Redactar demandas, amparos, y contestaciones.\n" +
                   "• Resumir sus expedientes y prepararle para audiencias.\n" +
                   "• Revisar leyes, códigos, analizar riesgos y organizar actividades.\n\n¿En qué caso o tarea legal desea trabajar el día de hoy?",
            isUser = false,
            timestamp = "Ahora"
        ))
    }

    fun sendChalanMessage(userMessageText: String) {
        if (userMessageText.isBlank() || chalanIsLoading) return
        
        val timestamp = "Hoy"
        chalanMessages.add(ChatMessage(text = userMessageText, isUser = true, timestamp = timestamp))
        chalanInputMessage = ""
        chalanIsLoading = true
        
        viewModelScope.launch {
            try {
                // Prepare okhttp client with high timeouts
                val client = OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .build()
                
                // Get general info of activities to pass in prompt
                val agendaInfo = actividadesProgramadas.value.joinToString("\n") { act -> 
                    "- ${act.titulo} en '${act.expediente}' (${act.horaInicio} - ${act.horaFin}, tipo: ${act.tipo}), Notas: ${act.notas}, Estado: ${act.estado}"
                }
                
                // Build prompt context
                val promptBuilder = StringBuilder()
                promptBuilder.append("Expediente asociado seleccionado: $chalanSelectedCase\n")
                promptBuilder.append("Contexto de la Agenda actual del Abogado:\n$agendaInfo\n\n")
                promptBuilder.append("Mensaje o Petición actual del Abogado: $userMessageText\n")
                
                // Build history context
                val historyContext = StringBuilder()
                val recentMessages = chalanMessages.takeLast(10)
                for (msg in recentMessages) {
                    val role = if (msg.isUser) "Usuario (Abogado)" else "Tú (Mi Chalán)"
                    historyContext.append("$role: ${msg.text}\n")
                }
                
                val finalPrompt = """
                    Historial Reciente (debes conservarlo para dar seguimiento a la conversación):
                    $historyContext
                    
                    Pregunta o petición final con contexto:
                    ${promptBuilder.toString()}
                """.trimIndent()
                
                // JSON structure using standard org.json API
                val jsonRequest = JSONObject()
                
                // Contents array
                val contentsArray = JSONArray()
                val contentObject = JSONObject()
                val partsArray = JSONArray()
                val partObject = JSONObject()
                partObject.put("text", finalPrompt)
                partsArray.put(partObject)
                contentObject.put("parts", partsArray)
                contentsArray.put(contentObject)
                jsonRequest.put("contents", contentsArray)
                
                // System instruction
                val systemInstructionObject = JSONObject()
                val siPartsArray = JSONArray()
                val siPartObject = JSONObject()
                siPartObject.put("text", """
                    Eres "Mi Chalán", el asistente de inteligencia artificial y auxiliar jurídico del abogado de la aplicación "LitigAndo en eso".
                    Hablas con un tono sumamente servicial, rápido, perspicaz, profesional pero amigable e inmersivo ("un chaval trabajador y muy leal"). Puedes usar expresiones como "Lic.", "con todo gusto", "a la orden", etc.
                    Asistes en todo momento al abogado con la redacción de escritos, demandas, amparos, análisis de expedientes, preparación de audiencias y organización de su agenda.
                    Proteges la confidencialidad, y siempre presentas de forma impecable y formateada los borradores de documentos que te pida.
                    
                    Tu misión es hacer de la labor del Licenciado un ejercicio impecable y simplificado. Siempre estructurado y con fundamentación legal lógica.
                """.trimIndent())
                siPartsArray.put(siPartObject)
                systemInstructionObject.put("parts", siPartsArray)
                jsonRequest.put("systemInstruction", systemInstructionObject)
                
                // Retrieve API key from build config
                val apiKey = com.example.BuildConfig.GEMINI_API_KEY
                val mediaType = "application/json; charset=utf-8".toMediaType()
                val requestBody = RequestBody.create(mediaType, jsonRequest.toString())
                
                val request = Request.Builder()
                    .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
                    .post(requestBody)
                    .build()
                
                val response = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    client.newCall(request).execute()
                }
                
                if (response.isSuccessful) {
                    val bodyString = response.body?.string() ?: ""
                    val jsonResponse = JSONObject(bodyString)
                    val candidates = jsonResponse.getJSONArray("candidates")
                    val firstCandidate = candidates.getJSONObject(0)
                    val candidateContent = firstCandidate.getJSONObject("content")
                    val parts = candidateContent.getJSONArray("parts")
                    val textReply = parts.getJSONObject(0).getString("text")
                    
                    chalanMessages.add(ChatMessage(text = textReply, isUser = false, timestamp = timestamp))
                } else {
                    val errorCode = response.code
                    chalanMessages.add(ChatMessage(
                        text = "Lic., disculpe, tuve un inconveniente de red al contactar al tribunal digital. (Código de error: $errorCode). ¿Intentamos de nuevo por favor?", 
                        isUser = false, 
                        timestamp = timestamp
                    ))
                }
            } catch (e: Exception) {
                chalanMessages.add(ChatMessage(
                    text = "Lic., por el momento no pude conectarme al servidor del Chalán (Error: ${e.localizedMessage ?: "Conexión interrumpida"}). Por favor, verifique su red o intente nuevamente.",
                    isUser = false,
                    timestamp = "Soporte"
                ))
            } finally {
                chalanIsLoading = false
            }
        }
    }

    // --- Colaborador Form State (Screen 1) ---
    var colNombre by mutableStateOf("")
    var colCorreo by mutableStateOf("")
    var colEspecialidad by mutableStateOf("")
    var colMonitoreo by mutableStateOf(true)
    val colDraftActivities = mutableStateListOf<DraftActivity>().apply {
        add(DraftActivity())
    }

    // --- Agenda Form State (Screen 2) ---
    var agendaTitulo by mutableStateOf("")
    var agendaExpediente by mutableStateOf("Seleccionar Expediente...")
    var agendaHoraInicio by mutableStateOf("")
    var agendaHoraFin by mutableStateOf("")
    var agendaTipo by mutableStateOf("Reunión") // Default tag value
    var agendaUbicacion by mutableStateOf("")
    var agendaNotas by mutableStateOf("")

    // --- Ajustes State (Screen 3) ---
    var biometricAccess by mutableStateOf(true)
    var isDarkMode by mutableStateOf(false)
    var allowNotifications by mutableStateOf(true)
    var userName by mutableStateOf("Lic. Carlos Gómez")
    var userRole by mutableStateOf("Socio Fundador / Director de Litigios")
    var userEmail by mutableStateOf("carlos.gomez@litigando.com")
    var userCedula by mutableStateOf("9812401-A")
    var userAvatarUrl by mutableStateOf("https://images.unsplash.com/photo-1560250097-0b93528c311a?auto=format&fit=crop&q=80&w=250")
    var userPhone by mutableStateOf("+52 (55) 5432-1098")
    var userBufete by mutableStateOf("Gómez & Asociados Abogados Litigantes S.C.")
    var userDireccion by mutableStateOf("Paseo de la Reforma 250, Piso 12, Ciudad de México, CP 06600")
    var userJurisdiccion by mutableStateOf("Fuero Federal, Amparo y Penal Especializado")
    var userColegio by mutableStateOf("Barra Mexicana, Colegio de Abogados A.C.")

    // --- Alta de Nuevo Caso Form State ---
    var caseNumero by mutableStateOf("")
    var caseCliente by mutableStateOf("")
    var caseDescripcion by mutableStateOf("")
    var caseAntecedentes by mutableStateOf(false)
    var caseDocumentoTexto by mutableStateOf("")
    var caseEscanearPdf by mutableStateOf("")
    var caseFotoRuta by mutableStateOf("")

    // --- Cuestionario de Antecedentes Form State ---
    var antecedentesHistorial by mutableStateOf("")
    var antecedentesExpedienteInicial by mutableStateOf("EXP-2023-0045")
    var antecedentesJurisdiccion by mutableStateOf("Civil y Comercial")

    val antecedentesHitos = mutableStateListOf<TimelineHito>().apply {
        add(TimelineHito(fecha = "12/10/2023", descripcion = "Radicación del expediente principal"))
        add(TimelineHito(fecha = "", descripcion = "Haga clic en 'Añadir Hito' para expandir la línea de tiempo."))
    }

    val antecedentesDocumentos = mutableStateListOf<DocumentoPrueba>().apply {
        add(DocumentoPrueba(nombre = "Contrato_Servicios_v2.pdf", info = "Cargado el 12/10/2023 • 2.4 MB", certificado = true))
        add(DocumentoPrueba(nombre = "Evidencia_Fisica_01.jpg", info = "Pendiente de certificación", certificado = false))
    }

    var antecedentesDemandante by mutableStateOf("Thomas Anderson")
    var antecedentesRepresentacion by mutableStateOf("DR. SMITH")
    var antecedentesContraparte by mutableStateOf("")

    fun saveExpediente(onSuccess: () -> Unit) {
        if (caseNumero.isBlank() || caseCliente.isBlank()) return
        viewModelScope.launch {
            repository.insertExpediente(
                ExpedienteEntity(
                    numeroCaso = caseNumero,
                    nombreCliente = caseCliente,
                    descripcion = if (caseDescripcion.isBlank()) "Sin descripción detallada." else caseDescripcion,
                    tieneAntecedentes = caseAntecedentes,
                    documentoTexto = caseDocumentoTexto,
                    escanearPdf = caseEscanearPdf,
                    fotoRuta = caseFotoRuta
                )
            )
            // Reset
            caseNumero = ""
            caseCliente = ""
            caseDescripcion = ""
            caseAntecedentes = false
            caseDocumentoTexto = ""
            caseEscanearPdf = ""
            caseFotoRuta = ""
            onSuccess()
        }
    }

    fun addDraftActivity() {
        colDraftActivities.add(DraftActivity())
    }

    fun removeDraftActivity(id: Long) {
        if (colDraftActivities.size > 1) {
            colDraftActivities.removeAll { it.id == id }
        }
    }

    fun updateDraftActivityDesc(id: Long, value: String) {
        val index = colDraftActivities.indexOfFirst { it.id == id }
        if (index != -1) {
            val item = colDraftActivities[index]
            colDraftActivities[index] = DraftActivity(
                id = item.id,
                descripcion = value,
                fechaInicio = item.fechaInicio,
                fechaFin = item.fechaFin,
                verificacionRequerida = item.verificacionRequerida
            )
        }
    }

    fun updateDraftActivityInicio(id: Long, value: String) {
        val index = colDraftActivities.indexOfFirst { it.id == id }
        if (index != -1) {
            val item = colDraftActivities[index]
            colDraftActivities[index] = DraftActivity(
                id = item.id,
                descripcion = item.descripcion,
                fechaInicio = value,
                fechaFin = item.fechaFin,
                verificacionRequerida = item.verificacionRequerida
            )
        }
    }

    fun updateDraftActivityFin(id: Long, value: String) {
        val index = colDraftActivities.indexOfFirst { it.id == id }
        if (index != -1) {
            val item = colDraftActivities[index]
            colDraftActivities[index] = DraftActivity(
                id = item.id,
                descripcion = item.descripcion,
                fechaInicio = item.fechaInicio,
                fechaFin = value,
                verificacionRequerida = item.verificacionRequerida
            )
        }
    }

    fun updateDraftActivityVerif(id: Long, value: Boolean) {
        val index = colDraftActivities.indexOfFirst { it.id == id }
        if (index != -1) {
            val item = colDraftActivities[index]
            colDraftActivities[index] = DraftActivity(
                id = item.id,
                descripcion = item.descripcion,
                fechaInicio = item.fechaInicio,
                fechaFin = item.fechaFin,
                verificacionRequerida = value
            )
        }
    }

    fun registerColaborador(onSuccess: () -> Unit) {
        if (colNombre.isBlank() || colCorreo.isBlank()) return
        viewModelScope.launch {
            val list = colDraftActivities.map {
                ActividadColaboradorEntity(
                    colaboradorId = 0,
                    descripcion = it.descripcion,
                    fechaInicio = it.fechaInicio,
                    fechaFin = it.fechaFin,
                    verificacionRequerida = it.verificacionRequerida
                )
            }
            repository.insertColaborador(
                ColaboradorEntity(
                    nombre = colNombre,
                    correo = colCorreo,
                    especialidad = colEspecialidad,
                    monitoreo = colMonitoreo
                ),
                list
            )
            // Reset state
            colNombre = ""
            colCorreo = ""
            colEspecialidad = ""
            colDraftActivities.clear()
            colDraftActivities.add(DraftActivity())
            onSuccess()
        }
    }

    fun saveActividadProgramada(onSuccess: () -> Unit) {
        if (agendaTitulo.isBlank()) return
        viewModelScope.launch {
            repository.insertActividadProgramada(
                ActividadProgramadaEntity(
                    titulo = agendaTitulo,
                    expediente = if (agendaExpediente == "Seleccionar Expediente...") "Expediente General" else agendaExpediente,
                    horaInicio = agendaHoraInicio.ifBlank { "12:00" },
                    horaFin = agendaHoraFin.ifBlank { "13:00" },
                    tipo = agendaTipo,
                    ubicacionOEnlace = agendaUbicacion,
                    notas = agendaNotas,
                    estado = "Pendiente"
                )
            )
            // Reset state
            agendaTitulo = ""
            agendaExpediente = "Seleccionar Expediente..."
            agendaHoraInicio = ""
            agendaHoraFin = ""
            agendaTipo = "Reunión"
            agendaUbicacion = ""
            agendaNotas = ""
            onSuccess()
        }
    }

    fun deleteActivity(activity: ActividadProgramadaEntity) {
        viewModelScope.launch {
            repository.deleteActividadProgramada(activity)
        }
    }
}
