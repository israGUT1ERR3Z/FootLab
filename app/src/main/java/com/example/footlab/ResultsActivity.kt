package com.example.footlab

import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.Timestamp
import java.util.Date

class ResultsActivity : AppCompatActivity() {

    // Inicializa FirebaseStorage
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results)

        // Inicializa FirebaseStorage
        firebaseStorage = FirebaseStorage.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val maskImageView = findViewById<ImageView>(R.id.maskImageView)
        val segmentedImageView = findViewById<ImageView>(R.id.segmentedImageView)

        val maskImageUrl = intent.getStringExtra("MASK_IMAGE_URL")
        val segmentedImageUrl = intent.getStringExtra("SEGMENTED_IMAGE_URL")

        // Load images asynchronously
        maskImageUrl?.let { url ->
            loadImageAsync(url) { bitmap ->
                bitmap?.let { maskBitmap ->
                    maskImageView.setImageBitmap(maskBitmap)
                }
            }
        }

        segmentedImageUrl?.let { url ->
            loadImageAsync(url) { bitmap ->
                bitmap?.let { segmentedBitmap ->
                    segmentedImageView.setImageBitmap(segmentedBitmap)
                }
            }
        }

        if (maskImageUrl != null && segmentedImageUrl != null) {
            loadImageAsync(maskImageUrl) { maskBitmap ->
                if (maskBitmap != null) {
                    loadImageAsync(segmentedImageUrl) { segmentedBitmap ->
                        if (segmentedBitmap != null) {
                            val combinedBitmap = combineImages(maskBitmap, segmentedBitmap)
                            uploadImageToFirebase(combinedBitmap) // Subir la imagen combinada
                        }
                    }
                }
            }
        }
    }

    private fun loadImageAsync(url: String, callback: (Bitmap?) -> Unit) {
        thread {
            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                val inputStream: InputStream = connection.inputStream
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()  // Close InputStream after use
                runOnUiThread { callback(bitmap) }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread { callback(null) }
            }
        }
    }

    private fun combineImages(maskBitmap: Bitmap, segmentedBitmap: Bitmap): Bitmap {
        val width = maskBitmap.width.coerceAtLeast(segmentedBitmap.width)
        val height = maskBitmap.height + segmentedBitmap.height

        val combinedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(combinedBitmap)

        canvas.drawBitmap(maskBitmap, 0f, 0f, null)
        canvas.drawBitmap(segmentedBitmap, 0f, maskBitmap.height.toFloat(), null)

        return combinedBitmap
    }

    private fun uploadImageToFirebase(imageBitmap: Bitmap) {
        val sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE)
        val username = sharedPreferences.getString("Username", null)

        if (username != null) {
            // Utiliza firebaseStorage en lugar de storage
            val storageRef = firebaseStorage.reference.child("${System.currentTimeMillis()}.jpg")
            val baos = ByteArrayOutputStream()
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()

            storageRef.putBytes(data)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        saveImageInfoToFirestore(uri.toString())
                    }
                }
                .addOnFailureListener {
                    showAlert("Error al cargar la imagen")
                }
        } else {
            showAlert("Usuario no encontrado")
        }
    }

    private fun saveImageInfoToFirestore(imageUrl: String) {
        val sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE)
        val username = sharedPreferences.getString("Username", null)

        if (username != null) {
            val campo = if (username.contains("@")) "Email" else "UserName"

            firestore.collection("Pacientes").whereEqualTo(campo, username)
                .get()
                .addOnSuccessListener { documentos ->
                    if (!documentos.isEmpty) {
                        val batch = firestore.batch()

                        for (paciente in documentos.documents) {
                            val docRef = paciente.reference
                            val fotos = paciente.get("Diagnosticos") as? ArrayList<HashMap<String, Any>> ?: arrayListOf()
                            val currentDate = Timestamp(Date())
                            val newPhoto = hashMapOf("Fecha" to currentDate, "URL" to imageUrl)
                            batch.update(docRef, "Resultados", FieldValue.arrayUnion(newPhoto))
                        }
                        batch.commit()
                            .addOnSuccessListener {
                                showAlert("Resultados guardados exitosamente")
                            }
                            .addOnFailureListener {
                                showAlert("Error al guardar el resultado")
                            }
                    } else {
                        showAlert("Usuario no encontrado")
                    }
                }
                .addOnFailureListener {
                    showAlert("Error al recuperar el documento")
                }
        } else {
            showAlert("Usuario no encontrado")
        }
    }

    private fun showAlert(message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Mensaje")
        builder.setMessage(message)
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }
}
