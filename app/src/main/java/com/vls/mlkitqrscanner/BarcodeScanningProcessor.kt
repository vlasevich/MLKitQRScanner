package com.vls.mlkitqrscanner

import com.google.android.gms.tasks.Task
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import android.util.Log


import java.io.IOException

class BarcodeScanningProcessor(scanningListener: OnScanningListener) : VisionProcessorBase<List<FirebaseVisionBarcode>>() {

    private val detector: FirebaseVisionBarcodeDetector = FirebaseVision.getInstance().visionBarcodeDetector
    private var scanningListener: OnScanningListener? = scanningListener

    // Note that if you know which format of barcode your app is dealing with, detection will be
    // faster to specify the supported barcode formats one by one, e.g.
    // new FirebaseVisionBarcodeDetectorOptions.Builder()
    //     .setBarcodeFormats(irebaseVisionBarcode.FORMAT_QR_CODE)
    //     .build();

    override fun stop() {
        try {
            detector.close()
        } catch (e: IOException) {
            Log.e(TAG, "Exception thrown while trying to close Barcode Detector: $e")
        }

    }

    override fun detectInImage(image: FirebaseVisionImage): Task<List<FirebaseVisionBarcode>> {
        return detector.detectInImage(image)
    }

    override fun onSuccess(
            barcodes: List<FirebaseVisionBarcode>,
            frameMetadata: FrameMetadata) {

        if (scanningListener != null && barcodes.isNotEmpty()) {
            scanningListener!!.getRawValue(barcodes[0].rawValue)
            scanningListener = null
        }

        /*for (int i = 0; i < barcodes.size(); ++i) {
            FirebaseVisionBarcode barcode = barcodes.get(i);
            BarcodeGraphic barcodeGraphic = new BarcodeGraphic(graphicOverlay, barcode);
            Log.e("VLS", barcode.getRawValue());
            graphicOverlay.add(barcodeGraphic);
        }*/
    }

    override fun onFailure(e: Exception) {
        Log.e(TAG, "Barcode detection failed $e")
    }

    interface OnScanningListener {
        fun getRawValue(rawValue: String?)
    }

    companion object {

        private const val TAG = "BarcodeScanningProcessor"
    }
}
