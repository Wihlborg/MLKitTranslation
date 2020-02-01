package com.example.mlkittranslation

import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata

class MyImageAnalyzer(_viewModel: TextDataViewModel): ImageAnalysis.Analyzer {
    private val viewModel = _viewModel

    private fun degreesToFirebaseRotation(degrees: Int): Int = when(degrees){
        0 -> FirebaseVisionImageMetadata.ROTATION_0
        90 -> FirebaseVisionImageMetadata.ROTATION_90
        180 -> FirebaseVisionImageMetadata.ROTATION_180
        270 -> FirebaseVisionImageMetadata.ROTATION_270
        else -> throw Exception("Rotation must be 0, 90, 180, or 270.")

    }


    override fun analyze(image: ImageProxy?, rotationDegrees: Int){
        val mediaImage = image?.image

        if (mediaImage != null){
            val firebaseVisionImage = FirebaseVisionImage.fromMediaImage(mediaImage, degreesToFirebaseRotation(rotationDegrees))

            val detector = FirebaseVision.getInstance().onDeviceTextRecognizer

            detector.processImage(firebaseVisionImage)
                .addOnSuccessListener{firebaseVisionText ->
                    val text = firebaseVisionText.text.replace(System.lineSeparator(), " ", true) //Make it single line
                    this.viewModel.updateText(text)}
                .addOnFailureListener { exception -> Log.e("Exception", "${exception.printStackTrace()}") }
        }

    }
}