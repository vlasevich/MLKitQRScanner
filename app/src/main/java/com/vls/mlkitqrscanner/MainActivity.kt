package com.vls.mlkitqrscanner

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.CompoundButton
import android.widget.Toast
import android.widget.ToggleButton


import java.io.IOException
import java.util.ArrayList

class MainActivity : AppCompatActivity(), OnRequestPermissionsResultCallback, CompoundButton.OnCheckedChangeListener {

    private var cameraSource: CameraSource? = null
    private var preview: CameraSourcePreview? = null
    private val selectedModel = BARCODE_DETECTION

    private val requiredPermissions: Array<String?>
        get() {
            return try {
                val info = this.packageManager
                        .getPackageInfo(this.packageName, PackageManager.GET_PERMISSIONS)
                val ps = info.requestedPermissions
                if (ps != null && ps.isNotEmpty()) {
                    ps
                } else {
                    arrayOfNulls(0)
                }
            } catch (e: Exception) {
                arrayOfNulls(0)
            }

        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!allPermissionsGranted()) {
            getRuntimePermissions()
        }
        setContentView(R.layout.main_activity_layout)
        preview = findViewById<View>(R.id.firePreview) as CameraSourcePreview?

        val facingSwitch = findViewById<View>(R.id.facingswitch) as ToggleButton
        facingSwitch.setOnCheckedChangeListener(this)

        if (allPermissionsGranted()) {
            createCameraSource(selectedModel)
        } else {
            getRuntimePermissions()
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        if (cameraSource != null) {
            if (isChecked) {
                cameraSource!!.setFacing(CameraSource.CAMERA_FACING_FRONT)
            } else {
                cameraSource!!.setFacing(CameraSource.CAMERA_FACING_BACK)
            }
        }
        preview!!.stop()
        startCameraSource()
    }

    private fun createCameraSource(model: String) {
        if (cameraSource == null) {
            cameraSource = CameraSource(this)
        }
        cameraSource!!.setMachineLearningFrameProcessor(BarcodeScanningProcessor(object : BarcodeScanningProcessor.OnScanningListener {
            override fun getRawValue(rawValue: String?) {
                Log.e("VLS", rawValue)
                Toast.makeText(applicationContext, rawValue, Toast.LENGTH_SHORT).show()
                //cameraSource.release();
                try {
                    preview!!.start(cameraSource)
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        }))
    }

    private fun startCameraSource() {
        if (cameraSource != null) {
            try {
                preview!!.start(cameraSource)
            } catch (e: IOException) {
                cameraSource!!.release()
                cameraSource = null
            }

        }
    }

    public override fun onResume() {
        super.onResume()
        startCameraSource()
    }

    override fun onPause() {
        super.onPause()
        preview!!.stop()
    }

    public override fun onDestroy() {
        super.onDestroy()
        if (cameraSource != null) {
            cameraSource!!.release()
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        Log.i(TAG, "Permission granted!")
        if (allPermissionsGranted()) {
            createCameraSource(selectedModel)
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun allPermissionsGranted(): Boolean {
        for (permission in requiredPermissions) {
            if (!isPermissionGranted(this, permission!!)) {
                return false
            }
        }
        return true
    }

    private fun getRuntimePermissions() {
        val allNeededPermissions = ArrayList<String>()
        for (permission in requiredPermissions) {
            if (!isPermissionGranted(this, permission!!)) {
                allNeededPermissions.add(permission)
            }
        }

        if (!allNeededPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this, allNeededPermissions.toTypedArray(), PERMISSION_REQUESTS)
        }
    }

    companion object {

        private const val BARCODE_DETECTION = "Barcode Detection"
        private const val TAG = "vls"
        private const val PERMISSION_REQUESTS = 1

        private fun isPermissionGranted(context: Context, permission: String): Boolean {
            if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "Permission granted: $permission")
                return true
            }
            Log.i(TAG, "Permission NOT granted: $permission")
            return false
        }
    }
}
