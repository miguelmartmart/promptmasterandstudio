package com.promptmaster.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Prompt::class], version = 3, exportSchema = false)
@TypeConverters(Converters::class)
abstract class PromptRoomDatabase : RoomDatabase() {

    abstract fun promptDao(): PromptDao

    companion object {
        @Volatile
        private var INSTANCE: PromptRoomDatabase? = null

        fun getDatabase(context: Context): PromptRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PromptRoomDatabase::class.java,
                    "prompt_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(PromptDatabaseCallback(context))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class PromptDatabaseCallback(
        private val context: Context
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            android.util.Log.d("PromptDatabaseCallback", "Database onCreate called")

            // NO PUEDES acceder a INSTANCE directamente aquí, así que:
            CoroutineScope(Dispatchers.IO).launch {
                val dao = getDatabase(context).promptDao()

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
                        isFavorite = false
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
                    isFavorite = false
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
                    isFavorite = false
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
                    isFavorite = false
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
                    isFavorite = false
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
                    isFavorite = false
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
                    isFavorite = false
                ),
                    Prompt(
                        title = "Generar contrato laboral personalizado",
                        description = "Crea un contrato laboral completo con los datos proporcionados. Incluye cláusulas legales estándar, condiciones laborales, y campos editables para personalizar según el rol y la legislación local.",
                        category = "Automatización de Documentos",
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
                        category = "Automatización de Documentos",
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
                        category = "Análisis de Documentos",
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
                        category = "Automatización de Documentos",
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
                        category = "Documentación Comercial",
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
                        category = "Documentación Interna",
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
                        category = "Comunicación Interna",
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
                        category = "Documentación Legal",
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
                        category = "Documentación Legal",
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
                        category = "Documentación Técnica",
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
                        category = "Comunicación Interna",
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
                        category = "Negociaciones y Acuerdos",
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
                        category = "Comercial / Compras",
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

                prompts.forEach { dao.insert(it) }
                android.util.Log.d("PromptDatabaseCallback", "Initial prompts inserted")
            }
        }
    }
}
