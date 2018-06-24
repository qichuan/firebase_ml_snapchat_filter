package com.zqc.ml.snapchat

import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import com.otaliastudios.cameraview.CameraView

/**
 *
 * FaceProcessor takes the camera frames from CameraView and uses FirebaseVisionFaceDetector
 * to detect the face, and then pass the detected face info to OverlayView so it can draw bitmaps on the face
 *
 * Created by Qichuan on 21/6/18.
 */
class FaceProcessor(private val cameraView: CameraView, private val overlayView: OverlayView) {

    // Initialize the face detection option, and we need all the face landmarks
    private val options = FirebaseVisionFaceDetectorOptions.Builder()
            .setLandmarkType(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
            .build()

    // Obtain the FirebaseVisionFaceDetector instance
    private val detector = FirebaseVision.getInstance().getVisionFaceDetector(options)

    fun startProcessing() {

        // Getting frames from camera view
        cameraView.addFrameProcessor { frame ->

            if (frame.size != null) {
                val rotation = frame.rotation / 90
                if (rotation / 2 == 0) {
                    overlayView.previewWidth = cameraView.previewSize?.width
                    overlayView.previewHeight = cameraView.previewSize?.height
                } else {
                    overlayView.previewWidth = cameraView.previewSize?.height
                    overlayView.previewHeight = cameraView.previewSize?.width
                }
                // Build a image meta data object
                val metadata = FirebaseVisionImageMetadata.Builder()
                        .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
                        .setWidth(frame.size.width)
                        .setHeight(frame.size.height)
                        .setRotation(rotation)
                        .build()
                // Create vision image object, and it will be consumed by FirebaseVisionFaceDetector
                // for face detection
                val firebaseVisionImage = FirebaseVisionImage.fromByteArray(frame.data, metadata)

                // Perform face detection
                detector.detectInImage(firebaseVisionImage).addOnSuccessListener { faceList ->
                    if (faceList.size > 0) {
                        // We just need the first face
                        val face = faceList[0]

                        // Pass the face to OverlayView
                        overlayView.face = face
                    }
                }
            }
        }
    }
}