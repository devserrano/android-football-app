package mx.unam

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import coil.load
import java.time.LocalDate

class DiasAdapter(
    private val dias: List<DiaCalendario>
) : RecyclerView.Adapter<DiasAdapter.DiaViewHolder>() {

    class DiaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val bgHoy: View = view.findViewById(R.id.bg_hoy)
        val tvDiaNumero: TextView = view.findViewById(R.id.tv_dia_numero)
        val ivEscudoRival: ImageView = view.findViewById(R.id.iv_escudo_rival)
        val ivCasaLocal: ImageView = view.findViewById(R.id.iv_casa_local)
        val layoutPrincipal: View = view
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendario, parent, false)
        return DiaViewHolder(view)
    }

    override fun onBindViewHolder(holder: DiaViewHolder, position: Int) {
        val dia = dias[position]
        val hoy = LocalDate.now()

        holder.tvDiaNumero.visibility = View.VISIBLE
        holder.ivEscudoRival.visibility = View.GONE
        holder.ivCasaLocal.visibility = View.GONE
        holder.bgHoy.visibility = View.GONE
        holder.layoutPrincipal.setOnClickListener(null)

        if (dia.fecha == hoy) {
            holder.bgHoy.visibility = View.VISIBLE
        }

        holder.tvDiaNumero.text = dia.fecha.dayOfMonth.toString()

        if (dia.esMesActual) {
            holder.tvDiaNumero.setTextColor(Color.BLACK)
        } else {
            holder.tvDiaNumero.setTextColor(Color.parseColor("#E0E0E0"))
        }

        dia.partido?.let { partido ->
            holder.tvDiaNumero.visibility = View.GONE
            holder.ivEscudoRival.visibility = View.VISIBLE

            holder.ivEscudoRival.load(partido.rivalIconoUrl) {
                crossfade(true)
                placeholder(R.drawable.escudo)
                error(R.drawable.escudo)
            }

            if (partido.esLocal) {
                holder.ivCasaLocal.visibility = View.VISIBLE
            }

            holder.layoutPrincipal.setOnClickListener {
                Toast.makeText(
                    holder.itemView.context,
                    "Partido: Barça vs ${partido.rivalNombre} (${partido.competicion})",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun getItemCount(): Int = dias.size
}