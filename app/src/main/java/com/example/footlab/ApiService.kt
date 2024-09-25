import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

// Data class para el cuerpo de la solicitud
data class ImageRequest(val imageUrl: String)

// Interfaz para la API
interface ApiService {
    @POST("/clasificar") // Cambia el endpoint según tu API
    fun classifyImage(@Body request: ImageRequest): Call<String> // Aquí puedes cambiar String por una clase si tu respuesta es más compleja
}
