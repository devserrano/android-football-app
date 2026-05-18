package mx.unam

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MesesAdapter(
    private val listaMeses: List<MesCalendario>
) : RecyclerView.Adapter<MesesAdapter.MesViewHolder>() {

    class MesViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombreMes: TextView = view.findViewById(R.id.tv_nombre_mes)
        val rvDiasDelMes: RecyclerView = view.findViewById(R.id.rv_dias_del_mes)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MesViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mes_calendario, parent, false)
        return MesViewHolder(view)
    }

    override fun onBindViewHolder(holder: MesViewHolder, position: Int) {
        val mesActual = listaMeses[position]

        holder.tvNombreMes.text = mesActual.titulo

        holder.rvDiasDelMes.layoutManager =
            GridLayoutManager(holder.itemView.context, 7)

        holder.rvDiasDelMes.adapter = DiasAdapter(mesActual.dias)
    }

    override fun getItemCount(): Int = listaMeses.size
}