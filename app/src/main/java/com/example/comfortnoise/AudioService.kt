package com.example.comfortnoise

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import androidx.core.app.ActivityCompat
import java.io.InputStream
import java.util.Random


//import be.tarsos.dsp.filters.BandPass


class AudioService(spectogramView: CanvasSpectogram/*, mReceiver: ScreenReceiver*/) {

    /* currently there is no need to check for screen off as onPause is called anyhow before the screen is disabled.
    * Anyhow also the disabled screen event was received */
    /*
     val screenReceiver: ScreenReceiver = mReceiver
     class ScreenReceiver : BroadcastReceiver() {
        var wasScreenOn:Boolean = true

        override fun onReceive(context: Context?, intent: Intent) {
            if (intent.action == Intent.ACTION_SCREEN_OFF) {
                // do whatever you need to do here
                wasScreenOn = false
            } else if (intent.action == Intent.ACTION_SCREEN_ON) {
                // and do whatever you need to do here
                wasScreenOn = true
            }
        }
    }*/

    private var _spectogramView: CanvasSpectogram = spectogramView
    // synthesize sound
    lateinit var Track: AudioTrack
    var isPlaying: Boolean = false
    var isPause: Boolean = false
    var doUpdateView: Boolean = true
    val Fs: Int = SAMPLING_FREQUENCY
    //val buffLength: Int = AudioTrack.getMinBufferSize(Fs, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)
    var noiseLength: Int = Fs*1 // 1s
    var microphoneSignalLength: Int = Fs*1 // 1s
    val buffLength: Int = 4096//AudioTrack.getMinBufferSize(Fs, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)

    // fft
    val WS = WINDOW_SIZE //  WS = window size
    private var signalServiceObj: SignalService = SignalService(WS, OVERLAP_FACTOR)

    private fun readWavFile(inputStream: InputStream): DoubleArray? {

        try {
            val `in` = inputStream
            val header = ByteArray(44)
            `in`.read(header)
            var bufLength = 0u
            for (idx in 40 until 44) {
                bufLength += (header[idx].toUInt() shl 8 * (idx - 4))
            }

            noiseLength = bufLength.toInt()/ 2

            val signal = DoubleArray(noiseLength)
            val buf = ByteArray(noiseLength*2)
            `in`.read(buf)
            for (i in 0 until noiseLength) {
                signal[i] =
                    (buf[i * 2].toInt() and 0xff or (buf[i * 2 + 1].toInt() shl 8)).toDouble()
            }
            `in`.close()
            return signal
        } catch (e: Exception) {
            System.err.println(e)
        }
        return null
    }
    private fun initTrack() {
        // Very similar to opening a stream in PyAudio
        // In Android create a AudioTrack instance and initialize it with different parameters

        // AudioTrack is deprecated for some android versions
        // Please look up for other alternatives if this does not work

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()

        val audioFormat = AudioFormat.Builder()
            .setSampleRate(Fs)
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .build()

// then, initialize with new constructor
        Track = AudioTrack(
            audioAttributes,
            audioFormat,
            buffLength,
            AudioTrack.MODE_STREAM,
            0
        )
    }

    private fun playback(signal: DoubleArray) {
        // simple sine wave generator

        val plotData = signalServiceObj.getSpectrogram(signal)
        // val screenReceiver = ScreenReceiver()

        var idxNoise = 0;
        var idxPlot = 0;
        while (isPlaying) {
            if (!isPause) {
                if (doUpdateView) {
                    val frameOut = ShortArray(buffLength)
                    for (i in 0 until buffLength) {
                        frameOut[i] = signal[idxNoise % noiseLength].toInt().toShort()
                        idxNoise++
                    }
                    Track.write(frameOut, 0, buffLength)
                    for (i in 0 until OVERLAP_FACTOR) {
                        _spectogramView.drawSpectogram(plotData[idxPlot % plotData.size])
                        idxPlot++
                    }
                } else {
                    // if the app is not in the foreground, it runs with lower priority.
                    // To avoid artefacts in this case, the plotting is disabled and the whole signal is played at once
                    // reset signal index
                    idxPlot = 0
                    idxNoise = 0

                    val frameOut: ShortArray = ShortArray(signal.size)
                    for (i in 0 until signal.size) {
                        frameOut[i] = signal[i].toInt().toShort()

                    }
                    Track.write(frameOut, 0, signal.size)
                }
            }
        }
    }

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
    private fun analyzeMicrophone()
    {
        val windowSize = 4096
        /*var signal = DoubleArray(minSize*OVERLAP_FACTOR)
        val buffer = ShortArray(minSize)*/
        val signal = DoubleArray(windowSize*OVERLAP_FACTOR)
        val buffer = ShortArray(windowSize)

        while (isRecording) {
            // recorder!!.read(buffer, 0, windowSize)
            ar!!.read(buffer, 0, windowSize)
            signal.copyInto(signal,0, windowSize,windowSize*OVERLAP_FACTOR-1)
            for (i in 0 until windowSize/*minSize*/) {
                signal[i+windowSize] = buffer[i].toDouble()
            }
            val plotData = signalServiceObj.getSpectrogram(signal)
            for (i in 0 until OVERLAP_FACTOR) {
                _spectogramView.drawSpectogram(plotData[i])
            }
        }
    }


    var isRecording = false
    fun startMicrophoneThread(context: Context)
    {
        Thread {
            startRecording(context)
            analyzeMicrophone()
        }.start()
    }

    fun startAudioThread(audioFileStream: InputStream)
    {
        val signal = readWavFile(audioFileStream)
        Thread {
            initTrack()
            startPlaying()
            if (signal != null) {
                playback(signal)
            }
        }.start()
    }

    fun pausePlaying() {
        isPause = true
    }
    fun continuePlaying() {
        isPause = false
    }

    private fun startPlaying() {
        Track.play()
        isPlaying = true
    }

    fun stopPlaying() {
        isRecording = false
        if (isPlaying) {
            isPlaying = false
            // Stop playing the audio data and release the resources
            Track.stop()
            Track.release()
        }
    }

    private fun generateRandomSignal(sampleSize: Int): DoubleArray {
        val random = Random()

        val signal = DoubleArray(sampleSize) { random.nextGaussian()*Short.MAX_VALUE*0.1F }
        return signal
    }

    fun onPause() {
        doUpdateView = false
    }
    fun onResume() {
        doUpdateView = true
    }
}