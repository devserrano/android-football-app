package mx.unam

import android.content.Context
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import coil.load

class MaterialAdapter(
    private val context: Context,
    private val listaTarjetas: ArrayList<Card>
) : RecyclerView.Adapter<MaterialAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var nombreTarjeta: TextView = view.findViewById(R.id.name_tarjeta)
        var piernaTarjeta: TextView = view.findViewById(R.id.pierna_tarjeta)
        var imagenView: ImageView = view.findViewById(R.id.image_view)
        var card: CardView = view.findViewById(R.id.card_layout)

        var tvNumeroDorsal: TextView = view.findViewById(R.id.tv_numero_dorsal)

        var layoutCurioso: View = view.findViewById(R.id.layout_curioso)
        var textoCurioso: TextView = view.findViewById(R.id.texto_curioso)
        var btnCerrar: ImageView = view.findViewById(R.id.btn_cerrar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_view_holder, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val jugador = listaTarjetas[position]

        holder.nombreTarjeta.text = jugador.nombre
        holder.piernaTarjeta.text = "Edad: ${jugador.debut} años"
        holder.textoCurioso.text = jugador.datoCurioso

        holder.imagenView.load(jugador.imagenURL) {
            placeholder(R.drawable.escudo)
            error(R.drawable.escudo)
        }

        holder.tvNumeroDorsal.text = jugador.dorsal.toString()

        holder.layoutCurioso.visibility = View.GONE

        holder.card.setOnClickListener {
            val transition = AutoTransition()
            transition.duration = 400
            TransitionManager.beginDelayedTransition(holder.card, transition)
            holder.layoutCurioso.visibility = View.VISIBLE
        }

        holder.btnCerrar.setOnClickListener {
            val transition = AutoTransition()
            transition.duration = 400
            TransitionManager.beginDelayedTransition(holder.card, transition)
            holder.layoutCurioso.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = listaTarjetas.size
}