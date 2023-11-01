package com.example.comfortnoise

//import android.R

import android.graphics.Color
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.SoundPool
import android.os.Bundle
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatActivity
//import be.tarsos.dsp.filters.BandPass
import com.example.comfortnoise.databinding.ActivityMainBinding
import java.util.Random

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding


    private var soundPool: SoundPool? = null

    var streamID: Int? = -1

    // synthesize sound
    lateinit var Track: AudioTrack
    var isPlaying: Boolean = false
    val Fs: Int = 44100
    //val buffLength: Int = AudioTrack.getMinBufferSize(Fs, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)
    val noiseLength: Int = Fs*1 // 5s
    val buffLength: Int = AudioTrack.getMinBufferSize(Fs, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)


    // fft
    val WS = 2048 //WS = window size
    lateinit var fftObj: FFT

    // Canvas
    private lateinit var spectogramview: CanvasSpectogram

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        //  init sound pool
        initSoundPool()


        streamID = soundPool!!.load(this, R.raw.white_noise, 1)
        binding = ActivityMainBinding.inflate(layoutInflater)
        spectogramview = binding.myCanvas
        fftObj = FFT(WS,Fs.toDouble())
        setContentView(binding.root)
        binding.WhiteNoise.setOnCheckedChangeListener() { compoundButton: CompoundButton, isChecked: Boolean ->
            if (isChecked) {
                compoundButton.setBackgroundColor(Color.GREEN)
                this.soundPool?.play(streamID!!, 0.3F, 0.3F, 1, -1, 1.0F);
            } else {
                compoundButton.setBackgroundColor(Color.RED)
//                soundPool?.pause(streamID!!)
                soundPool?.autoPause()
//                soundPool?.stop(streamID!!)
            }
        }


        binding.PinkNoise.setOnCheckedChangeListener() { compoundButton: CompoundButton, isChecked: Boolean ->
            if (isChecked) {
                compoundButton.setBackgroundColor(Color.GREEN)
                Thread {
                    initTrack()
                    startPlaying()
                    playback(spectogramview)
                }.start()
            } else {
                compoundButton.setBackgroundColor(Color.RED)
                stopPlaying()
            }
        }
    }


    private fun initSoundPool() {
        if (soundPool == null) {
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build()
            soundPool = SoundPool.Builder()
                .setMaxStreams(1)
                .setAudioAttributes(audioAttributes)
                .build()
        }
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

    private fun playback(spectogramview: CanvasSpectogram) {
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
        val signal = DoubleArray(sampleSize) { random.nextGaussian()*Short.MAX_VALUE*0.1F }//(sampleSize) { random.nextGaussian()*Short.MAX_VALUE*0.1F }.toDoubleArray()
        //val signal = DoubleArray(sampleSize) //List(sampleSize) { 0.0 }.toMutableList()
        /*val bandPass = BandPass(100f, 3000f, Fs.toFloat())
        bandPass.*/
        for (i in 0 until sampleSize) {
            signal[i] = (amplitude * Math.sin(phase))
            phase += twopi * (frequency) / Fs
            if (phase > twopi) {
                phase -= twopi
            }
        }


        val plotData = fftObj.printSpectrogram(signal)

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

            spectogramview.drawSpectogram(plotData[idxPlot%plotData.size])
            idxPlot++
        }
    }



    private fun startPlaying() {
        Track.play()
        isPlaying = true
    }

    private fun stopPlaying() {
        if (isPlaying) {
            isPlaying = false
            // Stop playing the audio data and release the resources
            Track.stop()
            Track.release()
        }
    }


    override fun onDestroy() {
        soundPool?.autoPause()
        if (streamID != null && streamID!! > 0) {
            soundPool?.stop(streamID!!)
        }
        soundPool?.release()
        soundPool = null
        super.onDestroy()

    }

}