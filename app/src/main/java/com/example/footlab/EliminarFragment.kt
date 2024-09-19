package com.example.footlab

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.footlab.utils.FirebaseUtils
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.channels.FileChannel

class EliminarFragment : Fragment() {

    private lateinit var recyclerViewFotos: RecyclerView
    private lateinit var fotosAdapter: FotosAdapter
    private lateinit var interpreter: Interpreter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.item_foto, container, false)

        recyclerViewFotos = rootView.findViewById(R.id.recyclerViewFotos)
        recyclerViewFotos.layoutManager = LinearLayoutManager(context)

        val sharedPreferences = activity?.getSharedPreferences("UserData", Context.MODE_PRIVATE)
        val username = sharedPreferences?.getString("Username", null)

        // Initialize Interpreter here with ByteBuffer
        interpreter = Interpreter(loadModelFile())

        username?.let {
            FirebaseUtils.cargarFotos(requireContext(), it) { fotos ->
                // Convert List<String> to MutableList<String>
                val mutableFotosUrls = fotos.toMutableList()
                if (mutableFotosUrls.isNotEmpty()) {
                    fotosAdapter = FotosAdapter(requireContext(), mutableFotosUrls, interpreter)
                    recyclerViewFotos.adapter = fotosAdapter
                } else {
                    Toast.makeText(context, "No hay fotos disponibles", Toast.LENGTH_SHORT).show()
                }
            }
        }

        return rootView
    }

    private fun loadModelFile(): ByteBuffer {
        val assetManager = requireContext().assets
        val fileDescriptor = assetManager.openFd("model.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }
}
