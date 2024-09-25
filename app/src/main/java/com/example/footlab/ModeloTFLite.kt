package com.example.footlab

import android.content.Context
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class ModeloTFLite(context: Context) {

    private val model: MappedByteBuffer = loadModelFile(context, "unet_model_gpu.tflite")

    private fun loadModelFile(context: Context, modelName: String): MappedByteBuffer {
        val assetManager = context.assets
        val fileDescriptor = assetManager.openFd(modelName)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel // Corrige aquí
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    fun getModel(): MappedByteBuffer {
        return model
    }
}
