# Product Requirement Document (PRD)
## LitigAndo en eso — Plataforma Integral de Gestión Jurídica e Inteligencia Procesal

---

| **Atributo** | **Detalle** |
| :--- | :--- |
| **Nombre del Producto** | LitigAndo en eso |
| **Versión** | 1.0.0 (Producción Android) |
| **Estado** | Activo / Desplegado |
| **Audiencia Objetivo** | Abogados independientes, bufetes jurídicos, pasantes y despachos corporativos |
| **Plataforma** | Android Nativo (Kotlin + Jetpack Compose + Material Design 3) |
| **Desarrollado con** | AI Studio Applet Builder (Google DeepMind) |

---

## 1. Visión General y Objetivos del Producto

### 1.1 Declaración de Visión
**LitigAndo en eso** es una solución tecnológica integral diseñada para revolucionar la práctica legal en México y Latinoamérica. Combina un gestor de expedientes judiciales con asistencia de Inteligencia Artificial ("Mi Chalán AI"), integración profunda con **Google Workspace (Gmail y Google Drive)**, certificación digital mediante firma electrónica Hash SHA-256 y herramientas automatizadas para el cálculo de términos procesales, liquidaciones y pensiones.

### 1.2 Problema a Resolver
* **Pérdida de términos procesales:** La gestión manual de plazos jurídicos en juzgados genera riesgos críticos de preclusión.
* **Redacción repetitiva de escritos:** Redactar amparos, contestaciones, promociones y recursos consume hasta el 60% de la jornada del abogado.
* **Desorganización documental:** Falta de respaldos automáticos en la nube y dificultad para relacionar correos y acuerdos con carpetas de clientes específicas.
* **Complejidad en cálculos jurídicos:** Errores en la estimación de finiquitos, indemnizaciones laborales, pensiones alimenticias e intereses moratorios.

### 1.3 Objetivos Estratégicos (KPIs)
1. **Reducción del 70%** en el tiempo de redacción de borradores procesales.
2. **Cero términos vencidos** gracias a la agenda procesal y alertas sincronizadas.
3. **Respaldo del 100%** de expedientes y acuerdos en Google Drive en carpetas estructuradas.

---

## 2. Personas de Usuario (User Personas)

### 👤 Persona A: Lic. Carlos Gómez (Socio Titular / Abogado Principal)
* **Perfil:** Abogado con más de 15 años de experiencia en litigio civil, mercantil y laboral.
* **Necesidad:** Control total de expedientes, supervisión de avances de pasantes, validez legal de documentos respaldados y comunicación directa con autoridades e hilos de Gmail.
* **Uso de la App:** Revisa el Dashboard de Hitos, autoriza escritos generados por IA, certifique acuerdos y envía correos corporativos desde la app.

### 👤 Persona B: Sofia Ramírez (Pasante / Auxiliar Jurídico "Mi Chalán")
* **Perfil:** Estudiante de Derecho o recién egresada a cargo de la gestión en juzgados y consulta del boletín judicial.
* **Necesidad:** Herramienta rápida para generar borradores de escritos, consultar tesis jurisprudenciales y subir acuerdos a la nube del despacho.
* **Uso de la App:** Genera borradores en "Mi Chalán AI", guarda promociones directamente en Google Drive y actualiza el estatus del expediente.

---

## 3. Arquitectura del Sistema y Módulos Principales

```
 ┌────────────────────────────────────────────────────────────────────────┐
 │                         LitigAndo en eso - UI                         │
 └──────┬─────────────────┬──────────────────┬───────────────────┬────────┘
        │                 │                  │                   │
 ┌──────▼──────┐   ┌──────▼──────┐    ┌──────▼──────┐     ┌──────▼──────┐
 │ Expedientes │   │ Mi Chalán   │    │ Google      │     │ Herramientas│
 │ & Hitos     │   │ AI Assistant│    │ Workspace   │     │ & Cálculos  │
 └──────┬──────┘   └──────┬──────┘    └──────┬──────┘     └──────┬──────┘
        │                 │                  │                   │
 ┌──────▼─────────────────▼──────────────────▼───────────────────▼────────┐
 │                         Core ViewModel & State Flow                    │
 └──────┬────────────────────────────────────┬────────────────────────────┘
        │                                    │
 ┌──────▼──────┐                      ┌──────▼──────┐
 │ Google Drive│                      │ Gmail API / │
 │ Cloud Sync  │                      │ SMTP Sync   │
 └─────────────┘                      └─────────────┘
```

---

## 4. Requisitos Funcionales Detallados

### 4.1 Módulo 1: Gestor de Expedientes y Casos Procesales
* **RF-1.1:** Registro de Expediente con número de caso (`EXP-2023-0045`), cliente, demandado, juzgado de adscripción y materia jurídica (Civil, Penal, Mercantil, Laboral, Amparo).
* **RF-1.2:** Creación automática de estructura de carpetas en **Google Drive** al dar de alta un caso:
  `LitigAndo / {Nombre Cliente} / {Número de Caso} /`
* **RF-1.3:** Árbol de pruebas y documentos anexos con validación de peso, fecha de subida e indicador de certificación Hash.

### 4.2 Módulo 2: "Mi Chalán AI" — Asistente Legal Inteligente
* **RF-2.1:** Chatbot conversacional especializado en derecho mexicano con capacidad para redactar:
  * Promociones de mero trámite y desahogo de prevenciones.
  * Contestaciones de demanda, amparos e incidentes.
  * Contratos de arrendamiento, convenios y cartas poder.
* **RF-2.2:** Integración directa **"Guardar en Drive"** desde las respuestas generadas por Mi Chalán AI.
* **RF-2.3:** Botón para copiar, compartir o enviar la respuesta directamente como borrador a la bandeja de **Gmail**.

### 4.3 Módulo 3: Google Workspace (Gmail + Google Drive)
* **RF-3.1:** Pantalla dedicada de vinculación de cuenta de Google (`GoogleLoginScreen`) con selección de cuenta corporativa (`enazulyrojo@gmail.com`).
* **RF-3.2:** Cliente de **Gmail integrado**:
  * Pestañas de Recibidos, Borradores, Enviados y Redactor.
  * Contador de mensajes no leídos y distintivo visual de estado en línea.
  * Formulario de composición con opciones para "Guardar Borrador" o "Enviar Correo".
* **RF-3.3:** **Nube Virtual Google Drive**:
  * Visualizador en tiempo real del estado de respaldos por cliente/expediente.
  * Carga automática de documentos certificados y archivos de la app.

### 4.4 Módulo 4: Certificación Digital FIEL / Hash Criptográfico
* **RF-4.1:** Generación de firma digital Hash SHA-256 para acreditar la inmutabilidad de actuaciones procesales.
* **RF-4.2:** Generación de sellos con fecha, hora e id único de autenticidad en documentos PDF.

### 4.5 Módulo 5: Calculadora Judicial y Finiquitos
* **RF-5.1:** **Calculadora Laboral (LFT):**
  * Estimación de indemnización constitucional (90 días), prima de antigüedad, aguinaldo proporcional, vacaciones y prima vacacional.
* **RF-5.2:** **Calculadora de Alimentaria e Intereses:**
  * Cálculo de pensión alimenticia en UMA / Salarios Mínimos e intereses moratorios de pagarés/títulos de crédito.

### 4.6 Módulo 6: Directorio Judicial y Mapas
* **RF-6.1:** Listado interactivo de Juzgados de Distrito, Tribunales Colegiados y Juzgados Locales con teléfonos, dirección y enlaces a la ubicación.

---

## 5. Requisitos No Funcionales (NFR)

### 5.1 Rendimiento
* **Tiempo de Respuesta:** La IA debe ofrecer respuestas iniciales en menos de 3 segundos.
* **Consumo de Memoria:** Operación eficiente mantenida por debajo de 150 MB de RAM.
* **Carga de Pantallas:** Las transiciones entre pestañas no deben superar los 200 ms.

### 5.2 Seguridad y Cifrado
* **Comunicaciones:** Todas las llamadas a servicios externos utilizan cifrado **HTTPS / SSL TLS 1.3**.
* **Credenciales:** Manejo de OAuth 2.0 para autenticación segura en Google APIs sin almacenar contraseñas en texto plano.

### 5.3 UX / UI Design System
* **Estilo:** Material Design 3 con paleta de colores corporativa (Azul Marino Profundo `#0D1B2A`, Azul Real `#1E3A8A`, Dorado `#EAB308` y Blanco Off-White).
* **Accesibilidad:** Cumplimiento de áreas táctiles mínimas de 48dp y alto contraste para legibilidad en juzgados y exteriores.

---

## 6. Stack Tecnológico

| Componente | Tecnología |
| :--- | :--- |
| **Lenguaje de Programación** | Kotlin 1.9+ |
| **Framework de UI** | Jetpack Compose (Material 3) |
| **Arquitectura UI** | MVVM (Model-View-ViewModel) con StateFlow |
| **Integraciones Cloud** | Google OAuth 2.0, Gmail REST API, Google Drive API |
| **Motor Criptográfico** | MessageDigest SHA-256 (Java Security) |
| **Construcción y Gradle** | AGP, KSP, Coroutines |

---

## 7. Roadmap de Lanzamiento y Futuras Versiones

* **Fase 1 (Completada):** Gestor de Expedientes, Mi Chalán AI, Calculadora Laboral, Certificación Hash y Google Drive Sync.
* **Fase 2 (Completada):** Consola de Gmail integrada, pantalla de autenticación de Google y sincronización interactiva de expedientes.
* **Fase 3 (Siguiente paso):** Conexión con la API del Boletín Judicial Oficial para notificaciones en tiempo real en la barra de estado de Android.

---

*Documento generado y mantenido para el proyecto **LitigAndo en eso**.*
