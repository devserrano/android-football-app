package mx.unam

import java.time.LocalDate

data class Partido(
    val fecha: LocalDate,
    val rivalNombre: String,
    val rivalIconoUrl: String,
    val esLocal: Boolean,
    val competicion: String
)

data class DiaCalendario(
    val fecha: LocalDate,
    val esMesActual: Boolean,
    val partido: Partido? = null
)

data class MesCalendario(
    val titulo: String,
    val dias: List<DiaCalendario>
)