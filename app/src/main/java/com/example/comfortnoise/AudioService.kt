package com.example.comfortnoise

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import java.io.File
import java.io.InputStream
import java.util.Random
//import be.tarsos.dsp.filters.BandPass


class AudioService(spectogramView: CanvasSpectogram) {

    private var _spectogramView: CanvasSpectogram = spectogramView

    // synthesize sound
    lateinit var Track: AudioTrack
    var isPlaying: Boolean = false
    val Fs: Int = 44100
    //val buffLength: Int = AudioTrack.getMinBufferSize(Fs, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)
    var noiseLength: Int = Fs*1 // 5s
    val buffLength: Int = 4096//AudioTrack.getMinBufferSize(Fs, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)

    // fft
    val WS = 2048 //  WS = window size
    lateinit var signalServiceObj: SignalService
    init {

        signalServiceObj = SignalService(WS,Fs.toDouble())
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
        Track = AudioTrack(
            AudioManager.MODE_NORMAL, Fs, AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT, buffLength, AudioTrack.MODE_STREAM
        )
    }

    private fun playback(signal: DoubleArray) {
        // simple sine wave generator
        val frame_out: ShortArray = ShortArray(buffLength)
        val amplitude: Int = 32767
        val frequency: Int = 3000
        val twopi: Double = 8.0 * Math.atan(1.0)
        var phase: Double = 0.0

        val sampleSize = noiseLength // Anzahl der Rauschwerte
        val minValue = 0 // Minimaler Wert des Rauschens
        val maxValue = amplitude // Maximaler Wert des Rauschens

        val random = Random() // Initialisiere den Zufallsgenerator mit einer Seed (kann angepasst werden)

        // Generiere das weiße Rauschen
        //val signal = DoubleArray(sampleSize) { random.nextGaussian()*Short.MAX_VALUE*0.1F }//(sampleSize) { random.nextGaussian()*Short.MAX_VALUE*0.1F }.toDoubleArray()
        //val signal = DoubleArray(sampleSize) //List(sampleSize) { 0.0 }.toMutableList()
        /*val bandPass = BandPass(100f, 3000f, Fs.toFloat())
        bandPass.*/
        /*for (i in 0 until sampleSize) {
            signal[i] = (amplitude * Math.sin(phase))
            phase += twopi * (frequency) / Fs
            if (phase > twopi) {
                phase -= twopi
            }
        }*/


        val plotData = signalServiceObj.printSpectrogram(signal)

        var idxNoise = 0;
        var idxPlot = 0;
        while (isPlaying) {
            for (i in 0 until buffLength) {
                /*frame_out[i] = (amplitude * Math.sin(phase)).toInt().toShort()
                phase += twopi * frequency / Fs
                if (phase > twopi) {
                    phase -= twopi
                }*/
                frame_out[i] = signal[idxNoise%sampleSize].toInt().toShort()
                idxNoise++
                //frame_out[i] = whiteNoise[i]
            }
            Track.write(frame_out, 0, buffLength)

            _spectogramView.drawSpectogram(plotData[idxPlot%plotData.size])
            idxPlot++
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
}