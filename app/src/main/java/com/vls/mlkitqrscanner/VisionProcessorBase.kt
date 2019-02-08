package com.vls.mlkitqrscanner


import com.google.android.gms.tasks.Task
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata

import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean

abstract class VisionProcessorBase<T> : VisionImageProcessor {

    // Whether we should ignore process(). This is usually caused by feeding input data faster than
    // the model can handle.
    private val shouldThrottle = AtomicBoolean(false)

    override fun process(
            data: ByteBuffer, frameMetadata: FrameMetadata) {
        if (shouldThrottle.get()) {
            return
        }
        val metadata = FirebaseVisionImageMetadata.Builder()
                .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
                .setWidth(frameMetadata.width)
                .setHeight(frameMetadata.height)
                .setRotation(frameMetadata.rotation)
                .build()

        detectInVisionImage(
                FirebaseVisionImage.fromByteBuffer(data, metadata), frameMetadata)
    }

    private fun detectInVisionImage(
            image: FirebaseVisionImage,
            metadata: FrameMetadata
    ) {
        detectInImage(image)
                .addOnSuccessListener { results ->
                    shouldThrottle.set(false)
                    this@VisionProcessorBase.onSuccess(results, metadata)
                }
                .addOnFailureListener { e ->
                    shouldThrottle.set(false)
                    this@VisionProcessorBase.onFailure(e)
                }
        // Begin throttling until this frame of input has been processed, either in onSuccess or
        // onFailure.
        shouldThrottle.set(true)
    }

    override fun stop() {}

    protected abstract fun detectInImage(image: FirebaseVisionImage): Task<T>

    protected abstract fun onSuccess(
            results: T,
            frameMetadata: FrameMetadata)

    protected abstract fun onFailure(e: Exception)
}
