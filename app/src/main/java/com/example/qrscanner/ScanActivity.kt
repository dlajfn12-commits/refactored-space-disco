package com.example.qrscanner

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.qrscanner.camera.QRCodeAnalyzer
import com.example.qrscanner.databinding.ActivityScanBinding
import com.example.qrscanner.ui.ScanViewModel
import com.example.qrscanner.ui.adapter.ScanAdapter
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ScanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScanBinding
    private lateinit var viewModel: ScanViewModel
    private lateinit var adapter: ScanAdapter
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var vibrator: Vibrator

    private companion object {
        private const val PERMISSION_REQUEST_CODE = 10
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(ScanViewModel::class.java)
        vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        cameraExecutor = Executors.newSingleThreadExecutor()

        setupUI()
        setupObservers()
        checkCameraPermission()
    }

    private fun setupUI() {
        adapter = ScanAdapter()
        binding.rvRecentScans.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rvRecentScans.adapter = adapter

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnComplete.setOnClickListener {
            // TODO: CSV export functionality
            Toast.makeText(this, "완료 버튼", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupObservers() {
        viewModel.scanCount.observe(this) { count ->
            binding.tvScanCountTop.text = "오늘 스캔: $count EA"
        }

        viewModel.scanRecords.observe(this) { records ->
            adapter.updateItems(records)
        }

        viewModel.messageEvent.observe(this) { event ->
            event?.let {
                when (it.type) {
                    ScanViewModel.MessageType.SUCCESS -> {
                        binding.tvMessage.text = it.message
                        binding.tvMessage.setTextColor(getColor(android.R.color.holo_green_light))
                        vibrateSuccess()
                    }
                    ScanViewModel.MessageType.DUPLICATE -> {
                        binding.tvMessage.text = it.message
                        binding.tvMessage.setTextColor(getColor(android.R.color.holo_red_light))
                    }
                    else -> {}
                }
            }
        }
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun startCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener(
            {
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(binding.previewView.surfaceProvider)
                }

                // QR Code Analyzer
                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor, QRCodeAnalyzer { qrCode ->
                            if (viewModel.addScan(qrCode)) {
                                // 스캔 성공
                            }
                        })
                    }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            ContextCompat.getMainExecutor(this)
        )
    }

    private fun vibrateSuccess() {
        val effect = VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE)
        vibrator.vibrate(effect)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera()
            } else {
                Toast.makeText(this, "카메라 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
