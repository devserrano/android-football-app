package mx.unam

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class CalendarTabFragment : Fragment() {

    private lateinit var rvMesesVertical: RecyclerView
    private var listaPartidos = listOf<Partido>()

    private val MI_API_KEY = BuildConfig.API_KEY

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_calendar_tab, container, false)

        rvMesesVertical = view.findViewById(R.id.rv_meses_vertical)
        rvMesesVertical.layoutManager = LinearLayoutManager(requireContext())
        rvMesesVertical.adapter = MesesAdapter(generarCalendarioCompleto())

        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(rvMesesVertical)

        rvMesesVertical.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                actualizarOpacidadMeses(recyclerView)
            }
        })

        descargarPartidosReales()

        return view
    }

    private fun descargarPartidosReales() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.football-data.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(FootballApiService::class.java)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val respuesta = api.obtenerPartidosBarcelona(MI_API_KEY)

                val partidosDescargados = respuesta.matches.map { matchApi ->
                    val fechaLimpia = matchApi.utcDate.substring(0, 10)
                    val fecha = LocalDate.parse(fechaLimpia)

                    val esLocal = matchApi.homeTeam.id == 81
                    val rival = if (esLocal) matchApi.awayTeam else matchApi.homeTeam

                    Partido(
                        fecha = fecha,
                        rivalNombre = rival.name,
                        rivalIconoUrl = rival.crest,
                        esLocal = esLocal,
                        competicion = matchApi.competition.name
                    )
                }

                listaPartidos = partidosDescargados

                withContext(Dispatchers.Main) {
                    rvMesesVertical.adapter = MesesAdapter(generarCalendarioCompleto())
                    rvMesesVertical.post {
                        actualizarOpacidadMeses(rvMesesVertical)
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("API_ERROR", "Error al descargar calendario: ${e.message}")
                    Toast.makeText(
                        requireContext(),
                        "Error al cargar partidos",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun actualizarOpacidadMeses(recyclerView: RecyclerView) {
        val centroPantalla = recyclerView.height / 2f

        for (i in 0 until recyclerView.childCount) {
            val vistaMes = recyclerView.getChildAt(i)
            val centroDeEstaVista = vistaMes.top + (vistaMes.height / 2f)
            val distanciaAlCentro = abs(centroPantalla - centroDeEstaVista)

            val zonaSegura = 450f

            val opacidad = if (distanciaAlCentro <= zonaSegura) {
                1f
            } else {
                1f - ((distanciaAlCentro - zonaSegura) / (centroPantalla / 1.5f))
            }

            vistaMes.alpha = max(0.3f, min(1f, opacidad))
        }
    }

    private fun generarCalendarioCompleto(): List<MesCalendario> {
        val meses = mutableListOf<MesCalendario>()

        for (numMes in 3..6) {
            val yearMonth = YearMonth.of(2026, numMes)
            val nombreMes = yearMonth.month
                .getDisplayName(TextStyle.FULL, Locale("es", "ES"))
                .replaceFirstChar { it.uppercase() }

            val titulo = "$nombreMes ${yearMonth.year}"
            meses.add(MesCalendario(titulo, generarDiasDelMes(yearMonth)))
        }

        return meses
    }

    private fun generarDiasDelMes(mes: YearMonth): List<DiaCalendario> {
        val dias = mutableListOf<DiaCalendario>()

        val primerDiaDelMes = mes.atDay(1)
        val diaDeLaSemana = primerDiaDelMes.dayOfWeek.value
        val diasPrevios = diaDeLaSemana - 1

        for (i in 1..diasPrevios) {
            val fechaAnterior = primerDiaDelMes.minusDays((diasPrevios - i + 1).toLong())
            dias.add(DiaCalendario(fechaAnterior, esMesActual = false))
        }

        for (i in 1..mes.lengthOfMonth()) {
            val fecha = mes.atDay(i)
            val partido = listaPartidos.find { it.fecha == fecha }
            dias.add(DiaCalendario(fecha, esMesActual = true, partido = partido))
        }

        return dias
    }
}