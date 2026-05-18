package mx.unam

import retrofit2.http.GET
import retrofit2.http.Header

interface FootballApiService {

    @GET("v4/competitions/2073/standings")
    suspend fun obtenerTablaLaLiga(
        @Header("X-Auth-Token") apiKey: String
    ): RespuestaTablaApi

    @GET("v4/teams/81/matches")
    suspend fun obtenerPartidosBarcelona(
        @Header("X-Auth-Token") apiKey: String
    ): RespuestaPartidosApi

    @GET("pagannnnn/app-barca-data/main/plantilla.json")
    suspend fun obtenerMiPlantillaPropia(): List<JugadorGitHub>
}