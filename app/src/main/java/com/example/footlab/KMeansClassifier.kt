import kotlin.math.pow
import kotlin.math.sqrt

class KMeansClassifier(private val clusters: Array<FloatArray>) {

    fun classify(pixels: FloatArray): IntArray {
        val classifications = IntArray(pixels.size / 3)

        for (i in classifications.indices) {
            val pixel = FloatArray(3)
            pixel[0] = pixels[i * 3]   // Red
            pixel[1] = pixels[i * 3 + 1] // Green
            pixel[2] = pixels[i * 3 + 2] // Blue

            // Find closest cluster
            var closestCluster = 0
            var minDistance = Float.MAX_VALUE

            for (j in clusters.indices) {
                val cluster = clusters[j]
                val distance = euclideanDistance(pixel, cluster)

                if (distance < minDistance) {
                    minDistance = distance
                    closestCluster = j
                }
            }
            classifications[i] = closestCluster
        }

        return classifications
    }

    private fun euclideanDistance(a: FloatArray, b: FloatArray): Float {
        return sqrt((a[0] - b[0]).toDouble().pow(2.0) +
                (a[1] - b[1]).toDouble().pow(2.0) +
                (a[2] - b[2]).toDouble().pow(2.0)).toFloat()
    }
}
