package com.example.comfortnoise.record

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.core.app.ActivityCompat
import com.example.comfortnoise.CanvasSpectogram
import com.example.comfortnoise.OVERLAP_FACTOR
import com.example.comfortnoise.SignalService
import com.example.comfortnoise.WINDOW_SIZE

class RecordService(spectogramView: CanvasSpectogram/*, mReceiver: ScreenReceiver*/) {

    private var _spectogramView: CanvasSpectogram = spectogramView
    var doUpdateView: Boolean = true
    val Fs: Int = 20000

    // fft
    val WS = WINDOW_SIZE //  WS = window size
    private var signalServiceObj: SignalService = SignalService(WS, OVERLAP_FACTOR)

    private var ar: AudioRecord? = null
    private var minSize = 0

    private fun startRecording(context: Context) {
        minSize = AudioRecord.getMinBufferSize(
            Fs,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            /*ActivityCompat.requestPermissions(context.act,
                arrayOf(Manifest.permission.RECORD_AUDIO)
            )*/
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        ar = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            Fs,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            minSize
        )
        ar!!.startRecording()
        isRecording = true
    }

    private fun startBleAudioRecording(context: Context, channelMask: Int) {
        minSize = AudioRecord.getMinBufferSize(
            Fs,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            /*ActivityCompat.requestPermissions(context.act,
                arrayOf(Manifest.permission.RECORD_AUDIO)
            )*/
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        val ar =
        AudioRecord.Builder()
            .setAudioSource(MediaRecorder.AudioSource.MIC)
            .setAudioFormat(AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(32000)
                .setChannelMask(channelMask)
                .build())
            .setBufferSizeInBytes(2 * minSize)
            .build()
        ar!!.startRecording()
    }
    private fun analyzeMicrophone()
    {
        val windowSize = 4096
        /*var signal = DoubleArray(minSize*OVERLAP_FACTOR)
        val buffer = ShortArray(minSize)*/
        val signal = DoubleArray(windowSize* OVERLAP_FACTOR)
        val buffer = ShortArray(windowSize)

        while (isRecording) {
            // recorder!!.read(buffer, 0, windowSize)
            ar!!.read(buffer, 0, windowSize)
            signal.copyInto(signal,0, windowSize,windowSize* OVERLAP_FACTOR -1)
            for (i in 0 until windowSize/*minSize*/) {
                signal[i+windowSize] = buffer[i].toDouble()
            }
            val plotData = signalServiceObj.getSpectrogram(signal, 120.0)
            for (i in 0 until OVERLAP_FACTOR) {
                _spectogramView.drawSpectogram(plotData[i])
            }
        }
    }


    private var isRecording = false
    fun startMicrophoneThread(context: Context)
    {
        Thread {
            startRecording(context)
            analyzeMicrophone()
        }.start()
    }
    fun startBleAudioMicrophoneThread(context: Context, channelMask: Int)
    {
        Thread {
            startBleAudioRecording(context, channelMask)
            analyzeMicrophone()
        }.start()
    }
    fun stopRecording() {
        isRecording = false
    }

    fun onPause() {
        doUpdateView = false
    }
    fun onResume() {
        doUpdateView = true
    }
}