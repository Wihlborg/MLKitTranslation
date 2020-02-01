package com.example.mlkittranslation

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Matrix
import android.os.Bundle
import android.util.Size
import android.view.Surface
import android.view.TextureView
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.mlkittranslation.databinding.ActivityMainBinding
import java.util.concurrent.Executors

private const val REQUEST_CODE_PERMISSIONS = 10

private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

class MainActivity : AppCompatActivity() {
    private lateinit var analyzer: MyImageAnalyzer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding:ActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        val textDataViewModel = ViewModelProvider(this).get(TextDataViewModel::class.java)

        binding.textData =  textDataViewModel
        binding.lifecycleOwner = this
        this.analyzer = MyImageAnalyzer(textDataViewModel)

        viewFinder = findViewById(R.id.view_finder)

        if (allPermissionsGranted()){
            viewFinder.post { startCamera() }
        } else{
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        viewFinder.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            updateTransform()
        }
    }
    private val executor = Executors.newSingleThreadExecutor()
    private lateinit var viewFinder: TextureView

    private fun startCamera(){
        val previewConfig = PreviewConfig.Builder().apply {
            setTargetResolution(Size(640,480))
        }.build()

        val preview = Preview(previewConfig)

        preview.setOnPreviewOutputUpdateListener {
            val parent = viewFinder.parent as ViewGroup
            parent.removeView(viewFinder)
            parent.addView(viewFinder, 0)

            viewFinder.surfaceTexture = it.surfaceTexture

            updateTransform()
        }

        val imageCaptureConfig = ImageCaptureConfig.Builder().apply {
            setCaptureMode(ImageCapture.CaptureMode.MAX_QUALITY)
        }.build()

        val imageCapture = ImageCapture(imageCaptureConfig)

        findViewById<ImageButton>(R.id.capture_button).setOnClickListener {
            imageCapture.takePicture(executor, object : ImageCapture.OnImageCapturedListener() {
                override fun onCaptureSuccess(image: ImageProxy?, rotationDegrees: Int) {
                    analyzer.analyze(image, rotationDegrees)
                    image?.close()
                }
            })
        }

        CameraX.bindToLifecycle(this, preview, imageCapture)
    }

    private fun updateTransform(){
        val matrix = Matrix()

        val centerX = viewFinder.width/ 2f
        val centerY = viewFinder.height / 2f

        val rotationDegrees = when(viewFinder.display.rotation){
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> return
        }

        matrix.postRotate(-rotationDegrees.toFloat(), centerX, centerY)

        viewFinder.setTransform(matrix)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray) {
            if (requestCode == REQUEST_CODE_PERMISSIONS){
                if (allPermissionsGranted()){
                    viewFinder.post{startCamera()}
                } else{
                    Toast.makeText(this, "Permissions not granted by user", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }
}



