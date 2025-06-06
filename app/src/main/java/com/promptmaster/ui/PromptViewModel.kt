package com.promptmaster.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.promptmaster.data.Prompt
import com.promptmaster.data.PromptRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import android.content.Context
import androidx.preference.PreferenceManager
import com.promptmaster.utils.getStringList
import com.promptmaster.utils.putStringList
import android.app.Application // Import Application
import androidx.lifecycle.AndroidViewModel // Import AndroidViewModel
import android.content.SharedPreferences // Import SharedPreferences


class PromptViewModel(
    private val repository: PromptRepository,
    application: Application // Accept Application in constructor
) : AndroidViewModel(application) { // Extend AndroidViewModel to get application context

    // State for search query and filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // State for search history
    private val _searchHistory = MutableStateFlow<List<String>>(emptyList())
    val searchHistory: StateFlow<List<String>> = _searchHistory.asStateFlow()

    private val sharedPreferences: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(application)
    }

    init {
        loadSearchHistory()
    }

    private fun loadSearchHistory() {
        _searchHistory.value = sharedPreferences.getStringList("search_history")
    }

    private fun saveSearchHistory(history: List<String>) {
        sharedPreferences.edit().apply {
            putStringList("search_history", history)
        }

    }

    val favoritePrompts: StateFlow<List<Prompt>> = repository.favoritePrompts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // TODO: Add MutableStateFlows for filter selections (category, model, tags)
    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _selectedSubcategory = MutableStateFlow<String?>(null) // Add subcategory state
    val selectedSubcategory: StateFlow<String?> = _selectedSubcategory.asStateFlow()

    private val _selectedModel = MutableStateFlow<String?>(null)
    val selectedModel: StateFlow<String?> = _selectedModel.asStateFlow()

    private val _selectedTag = MutableStateFlow<String?>(null)
    val selectedTag: StateFlow<String?> = _selectedTag.asStateFlow()

    private val _showFavoritesOnly = MutableStateFlow(false)
    val showFavoritesOnly: StateFlow<Boolean> = _showFavoritesOnly.asStateFlow()

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setShowFavoritesOnly(show: Boolean) {
        _showFavoritesOnly.value = show
    }

    fun setSelectedCategory(category: String?) {
        _selectedCategory.value = category
        // Reset subcategory when category changes
        _selectedSubcategory.value = null
    }

    fun setSelectedSubcategory(subcategory: String?) { // Add setSelectedSubcategory function
        _selectedSubcategory.value = subcategory
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val filteredPrompts: StateFlow<List<Prompt>> = combine(
        listOf(
            _searchQuery,
            _selectedCategory,
            _selectedSubcategory,
            _selectedModel,
            _selectedTag,
            _showFavoritesOnly
        )
    ) { values: Array<Any?> ->
        val query = values[0] as String
        val category = values[1] as String?
        val subcategory = values[2] as String?
        val model = values[3] as String?
        val tag = values[4] as String?
        val showFavorites = values[5] as Boolean

        android.util.Log.d("PromptViewModel", "Combining filters: query=$query, category=$category, subcategory=$subcategory, model=$model, tag=$tag, showFavorites=$showFavorites")
        repository.getFilteredAndSortedPrompts(query, category, subcategory, showFavorites)
    }
        .flatMapLatest { it }
        .onEach { prompts ->
            android.util.Log.d("PromptViewModel", "Filtered prompts emitted: ${prompts.size}")
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Function to mark a prompt as used
    fun markPromptAsUsed(id: Int) {
        viewModelScope.launch {
            repository.updateLastUsed(id, System.currentTimeMillis())
        }
    }

    // Expose getAllCategories from the repository
    fun getAllCategories(): Flow<List<String>> = repository.getAllCategories()
        .onEach { categories ->
            android.util.Log.d("PromptViewModel", "Categories emitted: ${categories.size}")
            categories.forEach { category ->
                android.util.Log.d("PromptViewModel", "Category: $category")
            }
        }

    // Expose getAllSubcategories from the repository
    fun getAllSubcategories(category: String?): Flow<List<String>> = repository.getAllSubcategories(category)
        .onEach { subcategories ->
            android.util.Log.d("PromptViewModel", "Subcategories emitted: ${subcategories.size}")
            subcategories.forEach { subcategory ->
                android.util.Log.d("PromptViewModel", "Subcategory: $subcategory")
            }
        }

    fun insert(prompt: Prompt) = viewModelScope.launch {
        val newPromptId = repository.insert(prompt)
        repository.updateLastUsed(Integer.parseInt(newPromptId.toString()), System.currentTimeMillis()) // Explicitly cast Long to Int
    }

    fun update(prompt: Prompt) = viewModelScope.launch {
        repository.update(prompt)
        repository.updateLastUsed(prompt.id, System.currentTimeMillis())
    }

    fun delete(prompt: Prompt) = viewModelScope.launch {
        repository.delete(prompt)
    }

    fun getPrompt(id: Int) = repository.getPrompt(id)

    fun duplicatePrompt(prompt: Prompt) = viewModelScope.launch {
        val newPrompt = prompt.copy(id = 0) // Create a copy with default ID
        val newPromptId = repository.insert(newPrompt) // Insert the new prompt
        repository.updateLastUsed(Integer.parseInt(newPromptId.toString()), System.currentTimeMillis()) // Explicitly cast Long to Int
    }

    // Function to copy text to clipboard
    fun copyTextToClipboard(text: String) {
        val clipboardManager = getApplication<Application>().getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clipData = android.content.ClipData.newPlainText("Prompt Description", text)
        clipboardManager.setPrimaryClip(clipData)
        // TODO: Find the prompt with this description and update its lastUsed timestamp
    }

    // Function to reset application data to initial state
    suspend fun resetApplicationData() {
        repository.deleteAllPrompts()

        val prompts = listOf(
            Prompt(
                title = "Verificador de Fiabilidad y Fuentes Oficiales",
                description = "Evalúa la veracidad de este contenido verificando hechos clave, sesgos, y proporciona enlaces a fuentes oficiales.",
                category = "Prompts Básicos",
                tags = listOf("fiabilidad", "verificación", "fuentes oficiales", "fact-checking"),
                recommendedModel = "gpt-4",
                customizableFields = mapOf(
                    "idioma" to "es",
                    "tipo_fuente_requerida" to "[\"gobierno\", \"OMS\", \"universidades\"]",
                    "formato_salida" to "JSON con campos: {veracidad, evidencia, enlaces, fiabilidad_general}"
                ),
                imagePath = null,
                videoPath = null,
                isFavorite = false,
                subcategory = ""
            ),


        Prompt(
            title = "Rol personalizado para IA: estructurar personalidad",
            description = "Define un personaje o asistente virtual estableciendo su rol, tono, conocimientos, formato de respuesta y restricciones. Esto permite tener respuestas coherentes con el personaje definido.",
            category = "Prompts Básicos",
            tags = listOf("rol", "agente", "personaje", "tono", "formato"),
            recommendedModel = "gpt-4",
            customizableFields = mapOf(
                "rol" to "asesor fiscal experto en autónomos de España",
                "tono" to "formal y claro",
                "formato_respuesta" to "lista + resumen final",
                "restricciones" to "sin opiniones personales, solo datos oficiales",
                "contexto" to "consultas tributarias para 2025"
            ),
            imagePath = null,
            videoPath = null,
            isFavorite = false,
            subcategory = ""
        ),
        Prompt(
            title = "Ayuda para tomar decisiones complejas paso a paso",
            description = "Analiza una decisión complicada desglosándola en criterios, alternativas, beneficios, riesgos y una conclusión justificada. Ideal para decidir entre varias opciones relevantes.",
            category = "Prompts Básicos",
            tags = listOf("decisiones", "análisis", "estrategia"),
            recommendedModel = "gpt-4",
            customizableFields = mapOf(
                "decisión" to "cambiar de proveedor tecnológico",
                "criterios" to "[\"coste\", \"fiabilidad\", \"soporte\"]", // Store as JSON string
                "formato_salida" to "tabla comparativa + conclusión"
            ),
            imagePath = null,
            videoPath = null,
                isFavorite = false,
                subcategory = ""
            ),
        Prompt(
            title = "Reformula un texto según estilo",
            description = "Convierte el siguiente texto a un tono, nivel de formalidad y estilo específico. Mantén el significado pero adapta la forma según el contexto deseado.",
            category = "Prompts Básicos",
            tags = listOf("estilo", "reformulación", "redacción"),
            recommendedModel = "gpt-3.5-turbo",
            customizableFields = mapOf(
                "tono_deseado" to "profesional",
                "nivel_formalidad" to "alto",
                "adaptar_a" to "presentación corporativa"
            ),
            imagePath = null,
            videoPath = null,
            isFavorite = false,
            subcategory = ""
        ),
        Prompt(
            title = "Estructurar contenido educativo",
            description = "Crea una lección o explicación sobre un tema para un público con cierto nivel. Incluye resumen, desarrollo, ejemplos y actividades sugeridas.",
            category = "Prompts Básicos",
            tags = listOf("educación", "enseñanza", "clases", "estructura"),
            recommendedModel = "gpt-4",
            customizableFields = mapOf(
                "tema" to "álgebra básica",
                "nivel" to "secundaria",
                "formato_salida" to "lección estructurada con preguntas al final"
            ),
            imagePath = null,
            videoPath = null,
                isFavorite = false,
                subcategory = ""
            ),
        Prompt(
            title = "Extraer ideas clave",
            description = "Identifica ideas clave, argumentos principales y detalles secundarios. Devuelve una estructura jerárquica de la información.",
            category = "Prompts Básicos",
            tags = listOf("resumen", "ideas clave", "comprensión"),
            recommendedModel = "gpt-3.5-turbo",
            customizableFields = mapOf(
                "idioma" to "es",
                "nivel_detalle" to "alto",
                "formato_salida" to "bullet points con sangría"
            ),
            imagePath = null,
            videoPath = null,
            isFavorite = false,
            subcategory = ""
        ),
        Prompt(
            title = "Actuar como moderador de debates",
            description = "Responde como un moderador que organiza turnos, mantiene el respeto y sintetiza los argumentos sin inclinarse por ningún lado.",
            category = "Prompts Básicos",
            tags = listOf("debate", "neutralidad", "moderación"),
            recommendedModel = "gpt-4",
            customizableFields = mapOf(
                "tema_del_debate" to "inteligencia artificial en educación",
                "cantidad_participantes" to "2", // Store as string
                "formato" to "pregunta inicial + turnos + conclusión objetiva"
            ),
            imagePath = null,
            videoPath = null,
            isFavorite = false,
            subcategory = ""
        ),
            Prompt(
                title = "Generar contrato laboral personalizado",
                description = "Crea un contrato laboral completo con los datos proporcionados. Incluye cláusulas legales estándar, condiciones laborales, y campos editables para personalizar según el rol y la legislación local.",
                category = "Automatización",
                subcategory = "Documentos",
                tags = listOf("contrato", "laboral", "legal", "documento"),
                recommendedModel = "gpt-4",
                customizableFields = mapOf(
                    "nombre_empleado" to "Juan Pérez",
                    "puesto" to "Desarrollador Backend",
                    "salario" to "32.000 € brutos anuales",
                    "duración" to "indefinido",
                    "jornada" to "completa",
                    "ubicación" to "Madrid, España"
                ),
                imagePath = null,
                videoPath = null,
                isFavorite = false
            ),
            Prompt(
            title = "Redactar correo formal automáticamente",
            description = "Genera un correo profesional según el objetivo, destinatario y contexto. El texto debe ser claro, directo y con un cierre adecuado.",
            category = "Comunicación",
            subcategory = "Correo",
            tags = listOf("correo", "email", "oficina", "profesional"),
                recommendedModel = "gpt-3.5-turbo",
                customizableFields = mapOf(
                    "destinatario" to "cliente",
                    "objetivo" to "informar sobre retraso en entrega",
                    "tono" to "respetuoso y resolutivo",
                    "idioma" to "es",
                    "firma" to "Laura Gómez\nGestión de cuentas"
                ),
                imagePath = null,
                videoPath = null,
                isFavorite = false
            ),
            Prompt(
                title = "Contrato de prestación de servicios personalizado",
                description = "Genera un contrato entre proveedor y cliente que detalle alcance, condiciones, duración, cláusulas legales, y métodos de pago.",
                category = "Automatización",
                subcategory = "Documentos",
                tags = listOf("servicios", "contrato", "proveedor", "legal"),
                recommendedModel = "gpt-4",
                customizableFields = mapOf(
                    "nombre_cliente" to "TechNova S.L.",
                    "nombre_proveedor" to "Freelancer AI Consulting",
                    "servicio" to "Desarrollo de módulo IA personalizado",
                    "duración" to "3 meses",
                    "precio" to "6.000 € + IVA",
                    "jurisdicción" to "España"
                ),
                imagePath = null,
                videoPath = null,
                isFavorite = false
            ),
            Prompt(
                title = "Resumen automático de contrato",
                description = "Resume un contrato largo destacando cláusulas clave, obligaciones, penalizaciones, y duración. Útil para revisión rápida.",
                category = "Análisis",
                subcategory = "Documentos",
                tags = listOf("resumen", "contrato", "legal", "síntesis"),
                recommendedModel = "gpt-3.5-turbo",
                customizableFields = mapOf(
                    "tipo_contrato" to "servicio de mantenimiento web",
                    "idioma" to "es",
                    "formato" to "puntos clave + tabla con partes y fechas importantes"
                ),
                imagePath = null,
                videoPath = null,
                isFavorite = false
            ),
            Prompt(
                title = "Correo de seguimiento para clientes",
                description = "Redacta un correo amable y profesional para hacer seguimiento a un cliente tras una reunión, incluyendo resumen, próximos pasos y agradecimiento.",
                category = "Comunicación",
                subcategory = "Seguimiento",
                tags = listOf("seguimiento", "email", "cliente", "negocios"),
                recommendedModel = "gpt-3.5-turbo",
                customizableFields = mapOf(
                    "cliente" to "María Rodríguez",
                    "fecha_reunión" to "10 de mayo de 2025",
                    "temas_tratados" to "campaña de lanzamiento y presupuesto",
                    "próximo_paso" to "enviar propuesta antes del viernes"
                ),
                imagePath = null,
                videoPath = null,
                isFavorite = false
            ),
            Prompt(
                title = "Contrato de confidencialidad (NDA) personalizado",
                description = "Genera un contrato de confidencialidad entre dos partes, detallando las definiciones de información confidencial, duración, limitaciones y consecuencias legales.",
                category = "Automatización",
                subcategory = "Documentos",
                tags = listOf("NDA", "confidencialidad", "contrato", "empresa"),
                recommendedModel = "gpt-4",
                customizableFields = mapOf(
                    "parte_receptora" to "Desarrollador Freelance",
                    "parte_divulgadora" to "TechGlobal Solutions",
                    "alcance" to "documentación técnica y estratégica",
                    "duración" to "2 años",
                    "jurisdicción" to "España"
                ),
                imagePath = null,
                videoPath = null,
                isFavorite = false
            ),
            Prompt(
                title = "Responder queja de cliente de forma profesional y empática",
                description = "Redacta una respuesta profesional y empática a una queja de cliente, asumiendo responsabilidad y ofreciendo una solución concreta.",
                category = "Atención al Cliente",
                subcategory = "Queja",
                tags = listOf("respuesta", "queja", "cliente", "correo"),
                recommendedModel = "gpt-3.5-turbo",
                customizableFields = mapOf(
                    "nombre_cliente" to "Laura Gómez",
                    "motivo_queja" to "entrega retrasada de pedido",
                    "acción_tomada" to "envío urgente sin costes",
                    "firma" to "Soporte TechNova"
                ),
                imagePath = null,
                videoPath = null,
                isFavorite = false
            ),
            Prompt(
                title = "Propuesta comercial adaptada a cliente",
                description = "Genera una propuesta comercial estructurada con introducción, objetivos, servicios ofrecidos, cronograma, presupuesto y cierre profesional.",
                category = "Documentación",
                subcategory = "Comercial",
                tags = listOf("propuesta", "oferta", "cliente", "negocio"),
                recommendedModel = "gpt-4",
                customizableFields = mapOf(
                    "nombre_cliente" to "Grupo Solvia",
                    "servicio" to "automatización de procesos con IA",
                    "duración_proyecto" to "2 meses",
                    "presupuesto" to "9.500 € + IVA"
                ),
                imagePath = null,
                videoPath = null,
                isFavorite = false
            ),
            Prompt(
                title = "Redactar acta de reunión estructurada",
                description = "Crea un acta de reunión clara y formal incluyendo fecha, asistentes, temas tratados, acuerdos y próximos pasos.",
                category = "Documentación",
                subcategory = "Interna",
                tags = listOf("acta", "reunión", "interno", "resumen"),
                recommendedModel = "gpt-3.5-turbo",
                customizableFields = mapOf(
                    "fecha" to "5 de junio de 2025",
                    "asistentes" to "Equipo de Marketing + Dirección",
                    "temas" to "plan de contenidos Q3",
                    "decisiones" to "incrementar presupuesto de redes"
                ),
                imagePath = null,
                videoPath = null,
                isFavorite = false
            ),
            Prompt(
                title = "Correo de bienvenida a nuevo empleado",
                description = "Redacta un correo amigable y formal para dar la bienvenida a un nuevo miembro del equipo. Incluye datos útiles y próximos pasos.",
                category = "Comunicación",
                subcategory = "Interna",
                tags = listOf("bienvenida", "empleado", "onboarding", "correo"),
                recommendedModel = "gpt-3.5-turbo",
                customizableFields = mapOf(
                    "nombre_empleado" to "Miguel Ortega",
                    "equipo" to "Departamento de Producto",
                    "fecha_inicio" to "12 de junio de 2025",
                    "contacto_soporte" to "rrhh@empresa.com"
                ),
                imagePath = null,
                videoPath = null,
                isFavorite = false
            ),
            Prompt(
                title = "Generar aviso legal personalizado",
                description = "Crea un aviso legal adaptado a las leyes de protección de datos, propiedad intelectual y uso del sitio o app según el país.",
                category = "Documentación",
                subcategory = "Legal",
                tags = listOf("aviso legal", "protección de datos", "web", "app", "RGPD"),
                recommendedModel = "gpt-4",
                customizableFields = mapOf(
                    "nombre_empresa" to "PromptMaster Inc.",
                    "tipo_servicio" to "plataforma de generación de prompts IA",
                    "país_jurisdicción" to "España",
                    "email_contacto" to "legal@promptmaster.com"
                ),
                imagePath = null,
                videoPath = null,
                isFavorite = false
            ),
            Prompt(
                title = "Redactar cláusula adicional para contrato",
                description = "Crea una cláusula adicional específica para un contrato existente, detallando condiciones y efectos jurídicos claros.",
                category = "Documentación",
                subcategory = "Legal",
                tags = listOf("cláusula", "anexo", "modificación", "contrato"),
                recommendedModel = "gpt-4",
                customizableFields = mapOf(
                    "tipo_contrato" to "contrato de prestación de servicios",
                    "motivo" to "extensión de plazos y condiciones de entrega",
                    "parte_interesada" to "proveedor externo",
                    "fecha_efectiva" to "15 de junio de 2025"
                ),
                imagePath = null,
                videoPath = null,
                isFavorite = false
            ),
            Prompt(
                title = "Generar anexo técnico explicativo",
                description = "Redacta un anexo que detalle especificaciones técnicas, condiciones de ejecución o requerimientos vinculados a un proyecto o contrato.",
                category = "Documentación",
                subcategory = "Técnica",
                tags = listOf("anexo", "documentación técnica", "contrato", "proyecto"),
                recommendedModel = "gpt-3.5-turbo",
                customizableFields = mapOf(
                    "proyecto" to "automatización de facturación con IA",
                    "elementos_especificados" to "modelos utilizados, métricas de precisión, APIs de terceros",
                    "vinculado_a" to "cláusula 5.3 del contrato",
                    "formato_salida" to "apartados con títulos + tabla de requisitos"
                ),
                imagePath = null,
                videoPath = null,
                isFavorite = false
            ),
            Prompt(
                title = "Generar memorando interno a empleados",
                description = "Redacta un memorando breve y profesional para comunicar información interna relevante a empleados o departamentos.",
                category = "Comunicación",
                subcategory = "Interna",
                tags = listOf("memorando", "interno", "información", "empresa"),
                recommendedModel = "gpt-3.5-turbo",
                customizableFields = mapOf(
                    "asunto" to "Nueva política de trabajo remoto",
                    "destinatarios" to "todo el personal de operaciones",
                    "fecha" to "10 de junio de 2025",
                    "resumen_contenido" to "normas, horarios flexibles, control de asistencia y uso de herramientas online"
                ),
                imagePath = null,
                videoPath = null,
                isFavorite = false
            ),
            Prompt(
                title = "Crear acuerdo de colaboración entre entidades",
                description = "Genera un acuerdo formal entre dos partes donde se establecen los objetivos, duración, responsabilidades y beneficios mutuos.",
                category = "Negociaciones",
                subcategory = "Acuerdos",
                tags = listOf("colaboración", "contrato", "empresa", "partes"),
                recommendedModel = "gpt-4",
                customizableFields = mapOf(
                    "empresa1" to "StartUp Vision",
                    "empresa2" to "BigTech Labs",
                    "objetivo" to "desarrollar solución conjunta de análisis predictivo",
                    "duración" to "12 meses",
                    "compromisos" to "entregables trimestrales y propiedad compartida del código"
                ),
                imagePath = null,
                videoPath = null,
                isFavorite = false
            ),
            Prompt(
                title = "Negociar precio con proveedor",
                description = "Redacta un correo cortés y estratégico para negociar condiciones económicas con un proveedor o socio comercial.",
                category = "Comercial",
                subcategory = "Compras",
                tags = listOf("negociación", "correo", "proveedor", "precio"),
                recommendedModel = "gpt-3.5-turbo",
                customizableFields = mapOf(
                    "nombre_proveedor" to "TechParts Ltd.",
                    "producto_servicio" to "componentes electrónicos para IoT",
                    "precio_actual" to "4.500 €/mes",
                    "motivo_negociación" to "reducción por volumen de pedidos y fidelidad"
                ),
                imagePath = null,
                videoPath = null,
                isFavorite = false
            ),
            Prompt(
                title = "Renegociar condiciones de contrato actual",
                description = "Genera un correo profesional para plantear la revisión o ajuste de cláusulas de un contrato vigente por cambios en las circunstancias.",
                category = "Negociaciones",
                subcategory = "Renegociaciones",
                tags = listOf("renegociación", "contrato", "condiciones", "correo"),
                recommendedModel = "gpt-4",
                customizableFields = mapOf(
                    "nombre_cliente_o_proveedor" to "MundoAI Solutions",
                    "motivo" to "modificación del alcance del proyecto por nuevas regulaciones",
                    "cláusulas_a_revisar" to "cronograma de entregas y coste adicional",
                    "tono_deseado" to "profesional, conciliador y claro"
                ),
                imagePath = null,
                videoPath = null,
                isFavorite = false
            )
        )

        prompts.forEach { repository.insert(it) }
    }
}

class PromptViewModelFactory(
    private val repository: PromptRepository,
    private val application: Application // Accept Application in constructor
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PromptViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PromptViewModel(repository, application) as T // Pass application to ViewModel
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
