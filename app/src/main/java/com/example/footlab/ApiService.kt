import com.example.footlab.PredictionModels
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("/predict")
    fun predict(@Body requestBody: PredictionModels.PredictionRequest): Call<PredictionModels.PredictionResponse>
} 