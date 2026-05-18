package mx.unam

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load

class TablaAdapter(
    private val listaEquipos: List<EquipoTabla>
) : RecyclerView.Adapter<TablaAdapter.TablaViewHolder>() {

    class TablaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val filaFondo: View = view.findViewById(R.id.fila_equipo)
        val tvPos: TextView = view.findViewById(R.id.tv_pos)
        val ivEscudo: ImageView = view.findViewById(R.id.iv_escudo_tabla)
        val tvNombre: TextView = view.findViewById(R.id.tv_nombre_equipo)
        val tvPj: TextView = view.findViewById(R.id.tv_pj)
        val tvG: TextView = view.findViewById(R.id.tv_g)
        val tvE: TextView = view.findViewById(R.id.tv_e)
        val tvP: TextView = view.findViewById(R.id.tv_p)
        val tvDg: TextView = view.findViewById(R.id.tv_dg)
        val tvPts: TextView = view.findViewById(R.id.tv_pts)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TablaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_equipo_tabla, parent, false)
        return TablaViewHolder(view)
    }

    override fun onBindViewHolder(holder: TablaViewHolder, position: Int) {
        val equipo = listaEquipos[position]

        holder.tvPos.text = equipo.posicion.toString()
        holder.tvNombre.text = equipo.nombre
        holder.tvPj.text = equipo.pj.toString()
        holder.tvG.text = equipo.g.toString()
        holder.tvE.text = equipo.e.toString()
        holder.tvP.text = equipo.p.toString()
        holder.tvDg.text = equipo.dg.toString()
        holder.tvPts.text = equipo.pts.toString()

        holder.ivEscudo.load(equipo.escudoUrl) {
            crossfade(true)
            placeholder(R.drawable.escudo)
            error(R.drawable.escudo)
        }

        if (equipo.nombre == "FC Barcelona") {
            holder.filaFondo.setBackgroundColor(Color.parseColor("#E3F2FD"))
        } else {
            holder.filaFondo.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    override fun getItemCount(): Int = listaEquipos.size
}