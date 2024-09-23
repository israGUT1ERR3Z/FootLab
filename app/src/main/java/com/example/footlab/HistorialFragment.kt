package com.example.footlab

import android.os.Bundle
import android.view.*
import android.widget.Button
import androidx.fragment.app.*

class HistorialFragment : Fragment() {

    private lateinit var botonHistClinica: Button
    private lateinit var botonResultados: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_historial, container, false)

        botonHistClinica = view.findViewById(R.id.botonVerHistorialClinico)
        botonResultados = view.findViewById(R.id.botonVerResultados)


        botonHistClinica.setOnClickListener {
            openFragment(HistorialClinicoFragment())
        }


        botonResultados.setOnClickListener {

        }

        return view
    }

    private fun openFragment(fragment: Fragment, tag: String? = null) {
        val fragmentTransaction: FragmentTransaction = parentFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, fragment, tag)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }
}

