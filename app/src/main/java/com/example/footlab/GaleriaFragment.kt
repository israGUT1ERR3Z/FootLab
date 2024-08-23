package com.example.footlab

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.footlab.utils.FirebaseUtils
import org.tensorflow.lite.Interpreter

class GaleriaFragment : Fragment() {

    private lateinit var interpreter: Interpreter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_galeria, container, false)

        // Inicializar el modelo TensorFlow Lite
        val modeloTFLite = ModeloTFLite(requireContext())
        interpreter = Interpreter(modeloTFLite.getModel())

        val sharedPreferences = activity?.getSharedPreferences("UserData", Context.MODE_PRIVATE)
        val username = sharedPreferences?.getString("Username", null)

        username?.let {
            FirebaseUtils.cargarFotos(requireContext(), it) { fotos ->
                if (fotos.isNotEmpty()) {
                    // Aquí podrías manejar las fotos de otra manera si es necesario.
                    Toast.makeText(context, "Fotos cargadas exitosamente", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "No hay fotos disponibles", Toast.LENGTH_SHORT).show()
                }
            }
        }

        return rootView
    }
}
