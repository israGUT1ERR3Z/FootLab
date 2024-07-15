package com.example.footlab

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.*
import android.widget.TextView

class HomeFragment : Fragment() {

    private lateinit var textBienvenido: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        textBienvenido = view.findViewById(R.id.bienvenida2)
        val sharedPreferences = requireContext().getSharedPreferences("UserData",
            Context.MODE_PRIVATE
        )
        val nombre = sharedPreferences.getString("Nombre", "Usuario")

        // Actualizar el TextView con el mensaje de bienvenida
        textBienvenido.text = "\nBIENVENID@\n$nombre"

        return view

    }
}
