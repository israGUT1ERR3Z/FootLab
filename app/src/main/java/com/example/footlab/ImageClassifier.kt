import kotlin.math.pow
import kotlin.math.sqrt

class ImageClassifier(private val clusters: Array<FloatArray>) {
    fun classifyImage(pixelValues: Array<FloatArray>): Array<Int> {
        return pixelValues.map { pixel ->
            clusters.indices.minByOrNull { clusterIndex ->
                euclideanDistance(pixel, clusters[clusterIndex])
            } ?: -1
        }.toTypedArray()
    }

    private fun euclideanDistance(a: FloatArray, b: FloatArray): Float {
        return sqrt((a[0] - b[0]).toDouble().pow(2.0) +
                (a[1] - b[1]).toDouble().pow(2.0) +
                (a[2] - b[2]).toDouble().pow(2.0)).toFloat()
    }
}
