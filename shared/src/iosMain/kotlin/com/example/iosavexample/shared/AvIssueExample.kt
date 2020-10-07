package com.example.iosavexample.shared

import platform.AVFoundation.*
import platform.CoreImage.CIImage
import platform.CoreMedia.CMSampleBufferGetImageBuffer
import platform.CoreMedia.CMSampleBufferRef
import platform.darwin.NSObject
import platform.darwin.dispatch_get_main_queue
import kotlin.native.concurrent.AtomicReference
import kotlin.native.concurrent.freeze
import kotlin.native.internal.GC

class AvIssueExample {

    val session = AtomicReference<AVCaptureSession?>(null)
    val delegate = AtomicReference<AVCaptureVideoDataOutputSampleBufferDelegateProtocol?>(null)

    fun start() {
        AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo, { granted: Boolean ->
            if (granted) {
                startSession()
            } else {
                TODO()
            }
        }.freeze())
    }

    private fun startSession() {
        val session = AVCaptureSession().also { session.value = it.freeze() }

        @Suppress("CONFLICTING_OVERLOADS")
        val delegate = object : NSObject(), AVCaptureVideoDataOutputSampleBufferDelegateProtocol {
            override fun captureOutput(
                output: AVCaptureOutput,
                didDropSampleBuffer: CMSampleBufferRef?,
                fromConnection: AVCaptureConnection
            ) {
                println("captureOutput didDropSampleBuffer")
            }

            override fun captureOutput(
                output: AVCaptureOutput,
                didOutputSampleBuffer: CMSampleBufferRef?,
                fromConnection: AVCaptureConnection
            ) {
                println("captureOutput didOutputSampleBuffer")

                val pixelBuffer = CMSampleBufferGetImageBuffer(didOutputSampleBuffer)
                val ciImage = CIImage(cVPixelBuffer = pixelBuffer)

                // TODO: Uncommenting the following line fixes the issue.
                //GC.collect()
            }
        }.also { delegate.value = it.freeze() }

        session.addInput(
            AVCaptureDeviceInput(
                AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo)!!, null
            )
        )

        val videoDataOutput = AVCaptureVideoDataOutput().apply {
            setSampleBufferDelegate(delegate.freeze(), dispatch_get_main_queue())
        }

        session.addOutput(videoDataOutput)

        session.startRunning()
    }
}
