package com.example.footlab

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class GaleriaFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: VistaFotos
    private val fotos: MutableList<String> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_galeria, container, false)

        recyclerView = view.findViewById(R.id.recycler_view_fotos)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = VistaFotos(fotos) { url ->
            // Aquí puedes manejar la acción cuando se presiona el botón "Analizar"
        }
        recyclerView.adapter = adapter

        cargarFotos()

        return view
    }

    private fun cargarFotos() {
        val sharedPreferences = activity?.getSharedPreferences("UserData", AppCompatActivity.MODE_PRIVATE)
        val username = sharedPreferences?.getString("Username", null)

        username?.let {
            val campo = if (it.contains("@")) "Email" else "UserName"
            val db = FirebaseFirestore.getInstance()
            db.collection("Pacientes").whereEqualTo(campo, it).get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty()) {
                        for (document in documents) {
                            // Obtener la lista de fotos del documento
                            val fotosList = document.get("Fotos") as? ArrayList<HashMap<String, String>> ?: arrayListOf()

                            // Limpiar la lista de fotos actual y añadir las nuevas fotos
                            fotos.clear()
                            fotos.addAll(fotosList.map { it["URL"].toString() })

                            // Notificar al adaptador que los datos han cambiado
                            adapter.notifyDataSetChanged()
                        }
                    } else {
                        Toast.makeText(context, "No hay fotos disponibles", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {

                }
        }
    }
}

