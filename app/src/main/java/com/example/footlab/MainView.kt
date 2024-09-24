package com.example.footlab

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.footlab.databinding.ActivityMainViewBinding
import com.google.android.material.navigation.NavigationView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.util.Date

class MainView : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var fragmentManager: FragmentManager
    private lateinit var binding: ActivityMainViewBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 1
        const val REQUEST_CAMERA_PERMISSION = 200
        const val HOME_FRAGMENT_TAG = "home_fragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        setSupportActionBar(binding.toolbar)

        val toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.toolbar,
            R.string.nav_open,
            R.string.nav_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.navigationDrawer.setNavigationItemSelectedListener(this)

        binding.bottomNavigation.background = null
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bottom_gallery -> openFragment(AnalizarFragment())
                R.id.bottom_help -> openFragment(AyudaFragment())
            }
            true
        }

        fragmentManager = supportFragmentManager
        openFragment(HomeFragment(), HOME_FRAGMENT_TAG)

        binding.fab.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CAMERA),
                    REQUEST_CAMERA_PERMISSION
                )
            } else {
                openCamera()
            }
        }
    }



    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                Toast.makeText(
                    this,
                    "Camera permission is required to use the camera",
                    Toast.LENGTH_SHORT
                ).show()
            }
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

    private fun openResultsActivity(bitmap: Bitmap, classifications: Any) {
        // Implement this method to display the results
    }

    private fun uploadImageToFirebase(imageBitmap: Bitmap) {
        val sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE)
        val username = sharedPreferences.getString("Username", null)

        if (username != null) {
            val storageRef = storage.reference.child("${System.currentTimeMillis()}.jpg")
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
                            val fotos = paciente.get("Fotos") as? ArrayList<HashMap<String, Any>> ?: arrayListOf()
                            val currentDate = Timestamp(Date())
                            val newPhoto = hashMapOf("Fecha" to currentDate, "URL" to imageUrl)
                            batch.update(docRef, "Fotos", FieldValue.arrayUnion(newPhoto))
                        }
                        batch.commit()
                            .addOnSuccessListener {
                                showAlert("Imagen guardada exitosamente")
                            }
                            .addOnFailureListener {
                                showAlert("Error al guardar la imagen")
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

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> openFragment(HomeFragment(), HOME_FRAGMENT_TAG)
            R.id.nav_profile -> openFragment(PerfilFragment())
            R.id.nav_history -> openFragment(HistorialFragment())
            R.id.nav_logout -> cerrarSesion()
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            val currentFragment = fragmentManager.findFragmentById(R.id.fragment_container)
            if (currentFragment !is HomeFragment) {
                openFragment(HomeFragment(), HOME_FRAGMENT_TAG)
            } else {
                super.onBackPressed()
            }
        }
    }

    private fun cerrarSesion() {
        val sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()

        val intent = Intent(this, LoginView::class.java)
        startActivity(intent)
        finish()
    }

    private fun openFragment(fragment: Fragment, tag: String? = null) {
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, fragment, tag)
        if (tag == HOME_FRAGMENT_TAG) {
            fragmentTransaction.addToBackStack(null)
        }
        fragmentTransaction.commit()
    }


}
