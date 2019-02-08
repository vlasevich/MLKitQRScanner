package com.vls.mlkitqrscanner


import com.google.firebase.ml.common.FirebaseMLException
import java.nio.ByteBuffer

interface VisionImageProcessor {

    @Throws(FirebaseMLException::class)
    fun process(data: ByteBuffer, frameMetadata: FrameMetadata)

    fun stop()
}
