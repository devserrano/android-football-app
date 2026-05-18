package mx.unam

data class RespuestaTablaApi(
    val standings: List<Standing>
)

data class Standing(
    val type: String,
    val table: List<TableItem>
)

data class TableItem(
    val position: Int,
    val team: Team,
    val playedGames: Int,
    val won: Int,
    val draw: Int,
    val lost: Int,
    val points: Int,
    val goalDifference: Int
)

data class Team(
    val name: String,
    val crest: String
)

data class RespuestaPartidosApi(
    val matches: List<MatchApi>
)

data class MatchApi(
    val utcDate: String,
    val competition: CompetitionApi,
    val homeTeam: TeamApi,
    val awayTeam: TeamApi,
    val status: String?,
    val score: ScoreApi?,
    val goals: List<GoalApi>?
)

data class ScoreApi(val fullTime: FullTimeApi?)
data class FullTimeApi(val home: Int?, val away: Int?)
data class GoalApi(val minute: Int?, val scorer: ScorerApi?, val team: TeamApi?)
data class ScorerApi(val name: String?)

data class CompetitionApi(val name: String)
data class TeamApi(val id: Int, val name: String, val crest: String)

data class RespuestaEquipoApi(
    val squad: List<JugadorSquadApi>?
)

data class JugadorSquadApi(
    val name: String?,
    val position: String?,
    val nationality: String?,
    val shirtNumber: Int?,
    val dateOfBirth: String?
)

data class JugadorGitHub(
    val nombre: String,
    val dorsal: Int,
    val posicion: String,
    val nacionalidad: String,
    val edad: Int,
    val imagenUrl: String
)