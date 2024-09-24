package com.example.footlab

class PredictionModels {
    data class PredictionRequest(val features: List<List<Double>>)
    data class PredictionResponse(val predictions: List<Int>)

}