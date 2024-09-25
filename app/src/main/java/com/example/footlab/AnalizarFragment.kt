
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.footlab.FotosAdapter
import com.example.footlab.ModeloTFLite
import com.example.footlab.R
import com.example.footlab.RetrofitClient
import com.example.footlab.utils.FirebaseUtils
import org.tensorflow.lite.Interpreter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AnalizarFragment : Fragment() {

    private lateinit var recyclerViewFotos: RecyclerView
    private lateinit var fotosAdapter: FotosAdapter
    private lateinit var interpreter: Interpreter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_galeria, container, false)

        // Inicializar el modelo TensorFlow Lite
        val modeloTFLite = ModeloTFLite(requireContext())
        interpreter = Interpreter(modeloTFLite.getModel())

        recyclerViewFotos = rootView.findViewById(R.id.recycler_view_fotos)
        recyclerViewFotos.layoutManager = LinearLayoutManager(context)

        val sharedPreferences = activity?.getSharedPreferences("UserData", Context.MODE_PRIVATE)
        val username = sharedPreferences?.getString("Username", null)

        username?.let {
            FirebaseUtils.cargarFotos(requireContext(), it) { fotos ->
                if (fotos.isNotEmpty()) {
                    // Aquí se pasa la implementación de onClasificarClick
                    fotosAdapter = FotosAdapter(requireContext(), fotos, interpreter) { imageUrl ->
                        callYourApi(imageUrl)  // Llama a tu API con la URL de la imagen
                    }
                    recyclerViewFotos.adapter = fotosAdapter
                } else {
                    Toast.makeText(context, "No hay fotos disponibles", Toast.LENGTH_SHORT).show()
                }
            }
        }

        return rootView
    }

    // Implementa esta función para llamar a tu API usando Retrofit
    private fun callYourApi(imageUrl: String) {
        // Crea el cuerpo de la solicitud (JSON)
        val imageRequest = ImageRequest(imageUrl)

        // Llama al servicio API usando Retrofit
        val call = RetrofitClient.apiService.classifyImage(imageRequest)

        // Ejecuta la llamada de forma asíncrona
        call.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    // Manejar la respuesta exitosa de la API
                    val result = response.body()
                    Toast.makeText(context, "Clasificación exitosa: $result", Toast.LENGTH_SHORT).show()
                } else {
                    // Manejar el error de la API
                    Toast.makeText(context, "Error en la clasificación: ${response.errorBody()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                // Manejar la falla en la solicitud
                Toast.makeText(context, "Error al conectar con la API: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
