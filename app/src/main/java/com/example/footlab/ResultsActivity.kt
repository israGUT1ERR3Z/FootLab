package com.example.footlab

import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
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

        // Load images asynchronously using Glide
        maskImageUrl?.let { url ->
            loadImage(url, maskImageView)
        }

        segmentedImageUrl?.let { url ->
            loadImage(url, segmentedImageView)
        }

        // Combine images and upload if both URLs are provided
        if (maskImageUrl != null && segmentedImageUrl != null) {
            loadImage(maskImageUrl) { maskBitmap ->
                loadImage(segmentedImageUrl) { segmentedBitmap ->
                    if (maskBitmap != null && segmentedBitmap != null) {
                        val combinedBitmap = combineImages(maskBitmap, segmentedBitmap)
                        uploadImageToFirebase(combinedBitmap) // Subir la imagen combinada
                    } else {
                        showAlert("Error al cargar las imágenes")
                    }
                }
            }
        }
    }

    private fun loadImage(url: String, imageView: ImageView? = null, callback: ((Bitmap?) -> Unit)? = null) {
        Glide.with(this)
            .asBitmap()
            .load(url)
            .into(object : com.bumptech.glide.request.target.CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?) {
                    imageView?.setImageBitmap(resource)
                    callback?.invoke(resource)
                }

                override fun onLoadCleared(placeholder: android.graphics.drawable.Drawable?) {
                    // Handle cleanup if needed
                }

                override fun onLoadFailed(errorDrawable: android.graphics.drawable.Drawable?) {
                    super.onLoadFailed(errorDrawable)
                    showAlert("Error al cargar la imagen desde $url")
                }
            })
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
                            val resultados = paciente.get("Resultados") as? ArrayList<HashMap<String, Any>> ?: arrayListOf()
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
