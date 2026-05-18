package mx.unam

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import coil.imageLoader
import coil.load
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mx.unam.databinding.FragmentHomeBinding
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var adapter: MaterialAdapter? = null
    private var listaTarjetas = ArrayList<Card>()

    private val MI_API_KEY = BuildConfig.API_KEY
    private var temporizadorJob: Job? = null
    private val CANAL_ID = "partidos_barca_canal"

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d("PERMISOS", "Notificaciones permitidas")
        } else {
            Log.w("PERMISOS", "Notificaciones denegadas")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.alpha = 0f
        binding.cardProximoPartido.alpha = 0f

        adapter = MaterialAdapter(requireContext(), listaTarjetas)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )

        binding.swipeRefreshLayout.setColorSchemeColors(
            android.graphics.Color.parseColor("#004D98"),
            android.graphics.Color.parseColor("#A50044")
        )

        binding.swipeRefreshLayout.setOnRefreshListener {
            buscarProximoPartido()
            descargarPlantillaReal()
        }

        binding.tvLocalNombre.text = "Cargando..."
        binding.tvVisitanteNombre.text = "Cargando..."
        binding.ivLocalCrest.setImageResource(R.drawable.escudo)
        binding.ivVisitanteCrest.setImageResource(R.drawable.escudo)

        solicitarPermisoNotificaciones()
        crearCanalNotificaciones()

        configurarBotonesWallpapers()
        descargarPlantillaReal()
        buscarProximoPartido()
    }

    private fun buscarProximoPartido() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.football-data.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(FootballApiService::class.java)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val respuesta = api.obtenerPartidosBarcelona(MI_API_KEY)
                val ahora = Instant.now()

                withContext(Dispatchers.Main) {
                    val partidos = respuesta.matches

                    val partidoEnVivo = partidos.find {
                        it.status == "IN_PLAY" || it.status == "PAUSED"
                    }

                    if (partidoEnVivo != null) {
                        mostrarMarcador(partidoEnVivo, true)
                        return@withContext
                    }

                    val partidoTerminado = partidos.filter { it.status == "FINISHED" }
                        .maxByOrNull { Instant.parse(it.utcDate) }

                    if (partidoTerminado != null) {
                        val inicioPartido = Instant.parse(partidoTerminado.utcDate)
                        val horasDesdeInicio = Duration.between(inicioPartido, ahora).toHours()

                        if (horasDesdeInicio <= 26) {
                            mostrarMarcador(partidoTerminado, false)
                            return@withContext
                        }
                    }

                    val proximoPartido = partidos.find {
                        (it.status == "TIMED" || it.status == "SCHEDULED") &&
                                Instant.parse(it.utcDate).isAfter(ahora)
                    }

                    if (proximoPartido != null) {
                        mostrarContadorProximo(proximoPartido)
                    }

                    binding.swipeRefreshLayout.isRefreshing = false
                }
            } catch (e: Exception) {
                Log.e("HOME_API", "Error: ${e.message}")
                withContext(Dispatchers.Main) {
                    binding.swipeRefreshLayout.isRefreshing = false
                }
            }
        }
    }

    private fun mostrarMarcador(match: MatchApi, esEnVivo: Boolean) {
        binding.swipeRefreshLayout.isRefreshing = false
        temporizadorJob?.cancel()

        binding.layoutContador.visibility = View.GONE
        binding.layoutMarcador.visibility = View.VISIBLE
        binding.cardProximoPartido.visibility = View.VISIBLE

        binding.tvLocalNombre.text = match.homeTeam.name
        binding.tvVisitanteNombre.text = match.awayTeam.name

        binding.ivLocalCrest.load(match.homeTeam.crest) {
            placeholder(R.drawable.escudo)
            error(R.drawable.escudo)
        }

        binding.ivVisitanteCrest.load(match.awayTeam.crest) {
            placeholder(R.drawable.escudo)
            error(R.drawable.escudo)
        }

        if (esEnVivo) {
            binding.tvEstadoPartido.text = "🔴 EN VIVO"
            binding.tvEstadoPartido.setTextColor(android.graphics.Color.parseColor("#FFFF00"))
        } else {
            binding.tvEstadoPartido.text = "FINALIZADO"
            binding.tvEstadoPartido.setTextColor(android.graphics.Color.WHITE)
        }

        val golesLocal = match.score?.fullTime?.home ?: 0
        val golesVisitante = match.score?.fullTime?.away ?: 0
        binding.tvResultado.text = "$golesLocal - $golesVisitante"

        var textoGoleadores = ""
        match.goals?.forEach { gol ->
            val nombreJugador = gol.scorer?.name ?: "Jugador"
            val minuto = gol.minute ?: 0
            textoGoleadores += "⚽ $nombreJugador ($minuto')\n"
        }

        binding.tvGoleadores.text = textoGoleadores.trim()

        actualizarFooterFecha(Instant.parse(match.utcDate))
        binding.cardProximoPartido.animate().alpha(1f).setDuration(600).start()
    }

    private fun mostrarContadorProximo(match: MatchApi) {
        binding.swipeRefreshLayout.isRefreshing = false

        binding.layoutMarcador.visibility = View.GONE
        binding.layoutContador.visibility = View.VISIBLE
        binding.cardProximoPartido.visibility = View.VISIBLE

        binding.tvLocalNombre.text = match.homeTeam.name
        binding.tvVisitanteNombre.text = match.awayTeam.name

        binding.ivLocalCrest.load(match.homeTeam.crest) {
            placeholder(R.drawable.escudo)
            error(R.drawable.escudo)
        }

        binding.ivVisitanteCrest.load(match.awayTeam.crest) {
            placeholder(R.drawable.escudo)
            error(R.drawable.escudo)
        }

        val instantePartido = Instant.parse(match.utcDate)

        actualizarFooterFecha(instantePartido)
        iniciarContador(instantePartido, match.homeTeam.name, match.awayTeam.name)

        binding.cardProximoPartido.animate().alpha(1f).setDuration(600).start()
    }

    private fun actualizarFooterFecha(instante: Instant) {
        val fechaLocal = instante.atZone(ZoneId.systemDefault())
        val formatoDia = DateTimeFormatter.ofPattern("dd")
        val formatoMes = DateTimeFormatter.ofPattern("MMM", Locale("es", "ES"))
        val formatoHora = DateTimeFormatter.ofPattern("HH:mm")

        binding.tvDiaNumero.text = fechaLocal.format(formatoDia)
        binding.tvMesTexto.text = fechaLocal.format(formatoMes).uppercase()
        binding.tvHoraNumero.text = fechaLocal.format(formatoHora)
    }

    private fun iniciarContador(matchTime: Instant, local: String?, visitante: String?) {
        temporizadorJob?.cancel()

        temporizadorJob = lifecycleScope.launch {
            while (isActive) {
                val ahora = Instant.now()
                val duracion = Duration.between(ahora, matchTime)
                val totalSegundos = duracion.seconds

                if (totalSegundos <= 0) {
                    binding.layoutContador.visibility = View.GONE
                    break
                }

                val dias = duracion.toDays()
                val horas = duracion.toHours() % 24
                val minutos = duracion.toMinutes() % 60
                val segundos = totalSegundos % 60

                binding.tvDias.text = String.format("%02d", dias)
                binding.tvHoras.text = String.format("%02d", horas)
                binding.tvMinutos.text = String.format("%02d", minutos)
                binding.tvSegundos.text = String.format("%02d", segundos)

                if (totalSegundos == 300L) {
                    enviarNotificacion(
                        "¡El partido está por empezar!",
                        "$local vs $visitante comienza en 5 minutos."
                    )
                }

                delay(1000)
            }
        }
    }

    private fun descargarPlantillaReal() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://raw.githubusercontent.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(FootballApiService::class.java)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val jugadoresPropios = api.obtenerMiPlantillaPropia()
                val nuevasTarjetas = ArrayList<Card>()

                for ((index, jugadorGit) in jugadoresPropios.withIndex()) {
                    val tarjeta = Card()
                    tarjeta.id = index.toLong()
                    tarjeta.nombre = jugadorGit.nombre
                    tarjeta.dorsal = jugadorGit.dorsal
                    tarjeta.debut = jugadorGit.edad
                    tarjeta.datoCurioso =
                        "País: ${jugadorGit.nacionalidad}\nPosición: ${jugadorGit.posicion}"
                    tarjeta.imagenURL = jugadorGit.imagenUrl

                    nuevasTarjetas.add(tarjeta)

                    val request = ImageRequest.Builder(requireContext())
                        .data(jugadorGit.imagenUrl)
                        .build()
                    requireContext().imageLoader.enqueue(request)
                }

                withContext(Dispatchers.Main) {
                    listaTarjetas.clear()
                    listaTarjetas.addAll(nuevasTarjetas)
                    adapter?.notifyDataSetChanged()
                    binding.recyclerView.animate().alpha(1f).setDuration(600).start()
                    binding.swipeRefreshLayout.isRefreshing = false
                }
            } catch (e: Exception) {
                Log.e("PLANTILLA_API", "Error: ${e.message}")
                withContext(Dispatchers.Main) {
                    binding.swipeRefreshLayout.isRefreshing = false
                }
            }
        }
    }

    private fun configurarBotonesWallpapers() {

        binding.cardFutbolistas.setOnClickListener {
            abrirHistoria(intArrayOf(
                R.drawable.futbolistas
            ))
        }

        binding.cardEstadio.setOnClickListener {
            abrirHistoria(intArrayOf(
                R.drawable.estadio
            ))
        }

        binding.cardCamisetas.setOnClickListener {
            abrirHistoria(intArrayOf(
                R.drawable.camiseta
            ))
        }

        binding.cardLeyendas.setOnClickListener {
            abrirHistoria(intArrayOf(
                R.drawable.leyendas
            ))
        }
    }
    private fun abrirHistoria(imagenes: IntArray) {
        val intent = android.content.Intent(requireContext(), StoryActivity::class.java)
        intent.putExtra("STORY_IMAGES", imagenes)
        startActivity(intent)
    }

    private fun crearCanalNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CANAL_ID,
                "Partidos",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Avisos de inicio de partido"
            }

            val notificationManager =
                requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun enviarNotificacion(titulo: String, mensaje: String) {
        val builder = NotificationCompat.Builder(requireContext(), CANAL_ID)
            .setSmallIcon(R.drawable.escudo)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        val notificationManager =
            requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(101, builder.build())
    }

    private fun solicitarPermisoNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        temporizadorJob?.cancel()
        _binding = null
    }
}