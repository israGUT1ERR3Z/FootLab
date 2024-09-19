
import android.content.Context
import org.json.JSONArray
import java.io.InputStream

class ClusterUtils {
    fun loadClustersFromAssets(context: Context): JSONArray {
        val inputStream: InputStream = context.assets.open("clusters.json")
        val json = inputStream.bufferedReader().use { it.readText() }
        return JSONArray(json)
    }

    fun processClusters(jsonArray: JSONArray): Array<FloatArray> {
        val clusters = Array(jsonArray.length()) { FloatArray(3) }
        for (i in 0 until jsonArray.length()) {
            val jsonCluster = jsonArray.getJSONArray(i)
            for (j in 0 until jsonCluster.length()) {
                clusters[i][j] = jsonCluster.getDouble(j).toFloat()
            }
        }
        return clusters
    }
}
