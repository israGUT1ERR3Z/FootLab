package com.example.footlab

import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.*
import android.widget.TextView

class PerfilFragment : Fragment() {

    private lateinit var textBienvenido: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?):
            View? {
        // Inflar el layout para este fragmento
        val view = inflater.inflate(R.layout.fragment_perfil, container, false)

        // Inicialización de los elementos del layout
        textBienvenido = view.findViewById(R.id.bienvenida)

        // Recuperar el nombre del usuario de SharedPreferences
        val sharedPreferences = requireContext().getSharedPreferences("UserData", MODE_PRIVATE)
        val nombre = sharedPreferences.getString("Nombre", "Usuario")

        // Actualizar el TextView con el mensaje de bienvenida
        textBienvenido.text = "BIENVENID@\n$nombre"

        return view
    }
}
