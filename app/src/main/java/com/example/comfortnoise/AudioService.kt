package com.example.comfortnoise

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import java.io.InputStream
import java.util.Random


//import be.tarsos.dsp.filters.BandPass


class AudioService(spectogramView: CanvasSpectogram) {

    private var _spectogramView: CanvasSpectogram = spectogramView

    // synthesize sound
    lateinit var Track: AudioTrack
    var isPlaying: Boolean = false
    val Fs: Int = SAMPLING_FREQUENCY
    //val buffLength: Int = AudioTrack.getMinBufferSize(Fs, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)
    var noiseLength: Int = Fs*1 // 5s
    val buffLength: Int = 4096//AudioTrack.getMinBufferSize(Fs, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)

    // fft
    val WS = WINDOW_SIZE //  WS = window size
    lateinit var signalServiceObj: SignalService
    init {

        signalServiceObj = SignalService(WS, OVERLAP_FACTOR)
    }

    fun startAudioThread(audioFileStream: InputStream)
    {
        val signal = readWavfile(audioFileStream)
        Thread {
            initTrack()
            startPlaying()
            if (signal != null) {
                playback(signal)
            }
        }.start()
    }


    private fun readWavfile(inputStream: InputStream): DoubleArray? {

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
        val frameOut: ShortArray = ShortArray(buffLength)

        val plotData = signalServiceObj.getSpectrogram(signal)

        var idxNoise = 0;
        var idxPlot = 0;
        while (isPlaying) {
            for (i in 0 until buffLength) {
                frameOut[i] = signal[idxNoise%noiseLength].toInt().toShort()
                idxNoise++
            }
            Track.write(frameOut, 0, buffLength)

            for (i in 0 until OVERLAP_FACTOR) {
                if (true/*idxPlot%32==0*/)
                    _spectogramView.drawSpectogram(plotData[idxPlot % plotData.size])
                idxPlot++
            }
        }
    }



    private fun startPlaying() {
        Track.play()
        isPlaying = true
    }

    fun stopPlaying() {
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
}