package com.example.mlkittranslation

import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata

class MyImageAnalyzer: ImageAnalysis.Analyzer {

    private fun degreesToFirebaseRotation(degrees: Int): Int = when(degrees){
        0 -> FirebaseVisionImageMetadata.ROTATION_0
        90 -> FirebaseVisionImageMetadata.ROTATION_90
        180 -> FirebaseVisionImageMetadata.ROTATION_180
        270 -> FirebaseVisionImageMetadata.ROTATION_270
        else -> throw Exception("Rotation must be 0, 90, 180, or 270.")

    }

    override fun analyze(image: ImageProxy?, rotationDegrees: Int) {
        val mediaImage = image?.image
        val imageRotation = degreesToFirebaseRotation(rotationDegrees)

        if (mediaImage != null){
            val image = FirebaseVisionImage.fromMediaImage(mediaImage, imageRotation)

            val detector = FirebaseVision.getInstance().onDeviceTextRecognizer

            detector.processImage(image)
                .addOnSuccessListener{firebaseVisionText ->
                    println(firebaseVisionText.text)} //TODO Data binding
                .addOnFailureListener { exception -> Log.e("Exception", "${exception.printStackTrace()}") }
        }

    }
}