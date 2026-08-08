package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

@Entity(tableName = "colaboradores")
data class ColaboradorEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val correo: String,
    val especialidad: String,
    val monitoreo: Boolean
)

@Entity(tableName = "actividades_colaborador")
data class ActividadColaboradorEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val colaboradorId: Long,
    val descripcion: String,
    val fechaInicio: String,
    val fechaFin: String,
    val verificacionRequerida: Boolean
)

@Entity(tableName = "actividades_programadas")
data class ActividadProgramadaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val titulo: String,
    val expediente: String,
    val horaInicio: String,
    val horaFin: String,
    val tipo: String, // "Audiencia", "Reunión", "Revisión", "Otro"
    val ubicacionOEnlace: String,
    val notas: String,
    val estado: String // "Pendiente", "Completado"
)

@Entity(tableName = "expedientes")
data class ExpedienteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val numeroCaso: String,
    val nombreCliente: String,
    val descripcion: String,
    val tieneAntecedentes: Boolean,
    val documentoTexto: String = "",
    val escanearPdf: String = "",
    val fotoRuta: String = ""
)

@Dao
interface LegalDao {
    @Query("SELECT * FROM colaboradores ORDER BY id DESC")
    fun getAllColaboradores(): Flow<List<ColaboradorEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertColaborador(colaborador: ColaboradorEntity): Long

    @Delete
    suspend fun deleteColaborador(colaborador: ColaboradorEntity)

    @Query("SELECT * FROM actividades_colaborador WHERE colaboradorId = :colaboradorId")
    fun getActividadesForColaborador(colaboradorId: Long): Flow<List<ActividadColaboradorEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActividadColaborador(actividad: ActividadColaboradorEntity)

    @Query("DELETE FROM actividades_colaborador WHERE colaboradorId = :colaboradorId")
    suspend fun deleteActividadesForColaborador(colaboradorId: Long)

    @Query("SELECT * FROM actividades_programadas ORDER BY id DESC")
    fun getAllActividadesProgramadas(): Flow<List<ActividadProgramadaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActividadProgramada(actividad: ActividadProgramadaEntity)

    @Delete
    suspend fun deleteActividadProgramada(actividad: ActividadProgramadaEntity)

    @Query("SELECT * FROM expedientes ORDER BY id DESC")
    fun getAllExpedientes(): Flow<List<ExpedienteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpediente(expediente: ExpedienteEntity): Long

    @Delete
    suspend fun deleteExpediente(expediente: ExpedienteEntity)
}

@Database(
    entities = [
        ColaboradorEntity::class,
        ActividadColaboradorEntity::class,
        ActividadProgramadaEntity::class,
        ExpedienteEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun legalDao(): LegalDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "legal_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class LegalRepository(private val legalDao: LegalDao) {
    val colaboradores: Flow<List<ColaboradorEntity>> = legalDao.getAllColaboradores()
    val actividadesProgramadas: Flow<List<ActividadProgramadaEntity>> = legalDao.getAllActividadesProgramadas()
    val expedientes: Flow<List<ExpedienteEntity>> = legalDao.getAllExpedientes()

    fun getActividadesForColaborador(colaboradorId: Long): Flow<List<ActividadColaboradorEntity>> {
        return legalDao.getActividadesForColaborador(colaboradorId)
    }

    suspend fun insertColaborador(colaborador: ColaboradorEntity, actividades: List<ActividadColaboradorEntity>) {
        val id = legalDao.insertColaborador(colaborador)
        actividades.forEach {
            legalDao.insertActividadColaborador(it.copy(colaboradorId = id))
        }
    }

    suspend fun insertActividadProgramada(actividad: ActividadProgramadaEntity) {
        legalDao.insertActividadProgramada(actividad)
    }

    suspend fun deleteColaborador(colaborador: ColaboradorEntity) {
        legalDao.deleteColaborador(colaborador)
        legalDao.deleteActividadesForColaborador(colaborador.id)
    }

    suspend fun deleteActividadProgramada(actividad: ActividadProgramadaEntity) {
        legalDao.deleteActividadProgramada(actividad)
    }

    suspend fun insertExpediente(expediente: ExpedienteEntity): Long {
        return legalDao.insertExpediente(expediente)
    }

    suspend fun deleteExpediente(expediente: ExpedienteEntity) {
        legalDao.deleteExpediente(expediente)
    }

    suspend fun prepopulateIfEmpty() {
        val existing = legalDao.getAllActividadesProgramadas().first()
        if (existing.isEmpty()) {
            legalDao.insertActividadProgramada(
                ActividadProgramadaEntity(
                    titulo = "Audiencia de Pruebas",
                    expediente = "Caso #9821 - Ramirez v. Estado",
                    horaInicio = "10:00",
                    horaFin = "11:30",
                    tipo = "Audiencia",
                    ubicacionOEnlace = "Sala B, Edificio Central",
                    notas = "Preparar alegatos iniciales",
                    estado = "Pendiente"
                )
            )
            legalDao.insertActividadProgramada(
                ActividadProgramadaEntity(
                    titulo = "Reunión con Cliente",
                    expediente = "Smith v. Global",
                    horaInicio = "14:00",
                    horaFin = "15:00",
                    tipo = "Reunión",
                    ubicacionOEnlace = "Enlace de Zoom",
                    notas = "Revisar cláusulas adicionales",
                    estado = "Completado"
                )
            )
        }

        val existingExpedientes = legalDao.getAllExpedientes().first()
        if (existingExpedientes.isEmpty()) {
            legalDao.insertExpediente(
                ExpedienteEntity(
                    numeroCaso = "Caso #9821",
                    nombreCliente = "Ramirez v. Estado",
                    descripcion = "Audiencia pendiente • Juzgado 4to de Distrito",
                    tieneAntecedentes = true
                )
            )
            legalDao.insertExpediente(
                ExpedienteEntity(
                    numeroCaso = "Smith v. Global",
                    nombreCliente = "Global Inc.",
                    descripcion = "Acuerdo de Servicios • Pendiente de firma de auxiliar",
                    tieneAntecedentes = false
                )
            )
            legalDao.insertExpediente(
                ExpedienteEntity(
                    numeroCaso = "Exp. 2024/045",
                    nombreCliente = "Corporativo Alfa",
                    descripcion = "Revisión tributaria • Activa",
                    tieneAntecedentes = true
                )
            )
            legalDao.insertExpediente(
                ExpedienteEntity(
                    numeroCaso = "Juicio Mercantil #312",
                    nombreCliente = "Constructora Sol",
                    descripcion = "Instancia final • Resuelta",
                    tieneAntecedentes = false
                )
            )
        }
    }
}
