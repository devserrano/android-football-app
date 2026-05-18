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
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class TablaTabFragment : Fragment() {

    private lateinit var rvTabla: RecyclerView
    private val MI_API_KEY = BuildConfig.API_KEY

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tabla_tab, container, false)

        rvTabla = view.findViewById(R.id.rv_tabla_posiciones)
        rvTabla.layoutManager = LinearLayoutManager(requireContext())
        rvTabla.adapter = TablaAdapter(emptyList())

        descargarTablaReal()

        return view
    }

    private fun descargarTablaReal() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.football-data.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(FootballApiService::class.java)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val respuesta = api.obtenerTablaLaLiga(MI_API_KEY)

                val tablaGeneral = respuesta.standings
                    .find { it.type == "TOTAL" }
                    ?.table ?: emptyList()

                val listaEquipos = tablaGeneral.map { item ->
                    EquipoTabla(
                        posicion = item.position,
                        nombre = item.team.name,
                        escudoUrl = item.team.crest,
                        escudoLocal = R.drawable.escudo,
                        pj = item.playedGames,
                        g = item.won,
                        e = item.draw,
                        p = item.lost,
                        dg = item.goalDifference,
                        pts = item.points
                    )
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "Equipos cargados: ${listaEquipos.size}",
                        Toast.LENGTH_LONG
                    ).show()

                    rvTabla.adapter = TablaAdapter(listaEquipos)
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("API_ERROR", "Error al descargar: ${e.message}")

                    Toast.makeText(
                        requireContext(),
                        "Modo offline (API falló)",
                        Toast.LENGTH_SHORT
                    ).show()

                    // 🔥 DATOS FAKE (fallback)
                    val respaldo = listOf(
                        EquipoTabla(1, "FC Barcelona", "", R.drawable.escudo, 30, 22, 5, 3, 40, 71),
                        EquipoTabla(2, "Real Madrid", "", R.drawable.escudo, 30, 20, 6, 4, 35, 66),
                        EquipoTabla(3, "Atlético Madrid", "", R.drawable.escudo, 30, 18, 7, 5, 25, 61),
                        EquipoTabla(4, "Girona", "", R.drawable.escudo, 30, 17, 6, 7, 20, 57)
                    )

                    rvTabla.adapter = TablaAdapter(respaldo)
                }
            }
        }
    }
}