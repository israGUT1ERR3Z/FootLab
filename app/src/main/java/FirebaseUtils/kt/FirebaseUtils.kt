package com.example.footlab.utils

import android.content.Context
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseUtils {

    fun cargarFotos(context: Context, username: String, callback: (List<String>) -> Unit) {
        val campo = if (username.contains("@")) "Email" else "UserName"
        val db = FirebaseFirestore.getInstance()
        db.collection("Pacientes").whereEqualTo(campo, username).get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.documents.isNotEmpty()) {
                    val fotosList = querySnapshot.documents.first().get("Fotos") as? ArrayList<HashMap<String, String>> ?: arrayListOf()
                    val fotosUrls = fotosList.map { it["URL"].toString() }
                    callback(fotosUrls)
                } else {
                    Toast.makeText(context, "No hay fotos disponibles", Toast.LENGTH_SHORT).show()
                    callback(emptyList())
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error al cargar las fotos", Toast.LENGTH_SHORT).show()
                callback(emptyList())
            }
    }
}
