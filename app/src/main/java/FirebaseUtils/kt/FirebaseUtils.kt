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
                    // Get the "Fotos" field
                    val fotosField = querySnapshot.documents.first().get("Fotos")

                    // Process based on actual data type
                    val fotosUrls = when (fotosField) {
                        is ArrayList<*> -> {
                            // Handle ArrayList case
                            fotosField.filterIsInstance<HashMap<*, *>>()
                                .mapNotNull { (it["URL"] as? String) }
                        }
                        is List<*> -> {
                            // Handle List case
                            fotosField.filterIsInstance<String>()
                        }
                        else -> {
                            // Handle unexpected cases
                            Toast.makeText(context, "Formato de fotos no compatible", Toast.LENGTH_SHORT).show()
                            emptyList()
                        }
                    }
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

