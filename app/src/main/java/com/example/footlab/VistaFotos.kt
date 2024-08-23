package com.example.footlab

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class VistaFotos(private val fotos: List<String>, private val onAnalizarClick: (String) -> Unit) :
    RecyclerView.Adapter<VistaFotos.VistaFoto>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VistaFoto {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_foto, parent, false)
        return VistaFoto(view)
    }

    override fun onBindViewHolder(holder: VistaFoto, position: Int) {
        val urlFoto = fotos[position]
        Glide.with(holder.itemView.context)
            .load(urlFoto)
            .into(holder.imagenFoto)

        holder.botonAnalizar.setOnClickListener {
            onAnalizarClick(urlFoto)
        }
    }

    override fun getItemCount(): Int {
        return fotos.size
    }

    class VistaFoto(view: View) : RecyclerView.ViewHolder(view) {
        val imagenFoto: ImageView = view.findViewById(R.id.imagenFotoItem)
        val botonAnalizar: Button = view.findViewById(R.id.botonAnalizarItem)
    }
}