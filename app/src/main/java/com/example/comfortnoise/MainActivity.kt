package com.example.comfortnoise

//import android.R

import android.graphics.Color
import android.graphics.Paint
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.Image
import android.media.SoundPool
import android.os.Bundle
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColor
import com.example.comfortnoise.databinding.ActivityMainBinding
import java.io.IOException
import java.util.Arrays
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
    val buffLength: Int = Fs*1 // 5s


    // fft
    val WS = 2048 //WS = window size
    val fftObj = FFT(WS,Fs.toDouble()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //  init sound pool
        initSoundPool()

        streamID = soundPool!!.load(this, R.raw.white_noise, 1)
        binding = ActivityMainBinding.inflate(layoutInflater)
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
                    playback()
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

    private fun playback() {
        // simple sine wave generator
        val frame_out: ShortArray = ShortArray(buffLength)
        val amplitude: Int = 32767
        val frequency: Int = 440
        val twopi: Double = 8.0 * Math.atan(1.0)
        var phase: Double = 0.0

        val sampleSize = buffLength // Anzahl der Rauschwerte
        val minValue = 0 // Minimaler Wert des Rauschens
        val maxValue = amplitude // Maximaler Wert des Rauschens

        val random = Random() // Initialisiere den Zufallsgenerator mit einer Seed (kann angepasst werden)

        // Generiere das weiße Rauschen
        val whiteNoise = List(sampleSize) { random.nextGaussian()*Short.MAX_VALUE*0.1F }

        while (isPlaying) {
            for (i in 0 until buffLength) {
                /*frame_out[i] = (amplitude * Math.sin(phase)).toInt().toShort()
                phase += twopi * frequency / Fs
                if (phase > twopi) {
                    phase -= twopi
                }*/
                frame_out[i] = whiteNoise[i].toInt().toShort()
                //frame_out[i] = whiteNoise[i]
            }
            Track.write(frame_out, 0, buffLength)
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


    private fun printSpectrogram()
    {
        /*try {

            //get raw double array containing .WAV data
            val audioTest = readWAV2Array(filepath, true)
            val rawData: DoubleArray = audioTest.getByteArray()
            val length = buffLength

            //initialize parameters for FFT
            val OF = 8 //OF = overlap factor
            val windowStep = WS / OF

            //calculate FFT parameters
            val SR: Double = audioTest.getSR()
            val time_resolution = WS / SR
            val frequency_resolution = SR / WS
            val highest_detectable_frequency = SR / 2.0
            val lowest_detectable_frequency = 5.0 * SR / WS
            println("time_resolution:              " + time_resolution * 1000 + " ms")
            println("frequency_resolution:         $frequency_resolution Hz")
            println("highest_detectable_frequency: $highest_detectable_frequency Hz")
            println("lowest_detectable_frequency:  $lowest_detectable_frequency Hz")

            //initialize plotData array
            val nX = (length - WS) / windowStep
            val plotData = Array(nX) {
                DoubleArray(
                    WS
                )
            }
            val fftObj = FFT(WS)
            //apply FFT and find MAX and MIN amplitudes
            var maxAmp = Double.MIN_VALUE
            var minAmp = Double.MAX_VALUE
            var amp_square: Double
            val inputImag = DoubleArray(length)
            for (i in 0 until nX) {
                Arrays.fill(inputImag, 0.0)
                val WS_array = DoubleArray(length)
                fftObj.fft(
                    Arrays.copyOfRange(rawData, i * windowStep, i * windowStep + WS),
                    WS_array
                )
                for (j in 0 until WS) {
                    amp_square =
                        WS_array[2 * j] * WS_array[2 * j] + WS_array[2 * j + 1] * WS_array[2 * j + 1]
                    if (amp_square == 0.0) {
                        plotData[i][j] = amp_square
                    } else {
                        plotData[i][j] = 10 * Math.log10(amp_square)
                    }

                    //find MAX and MIN amplitude
                    if (plotData[i][j] > maxAmp) maxAmp =
                        plotData[i][j] else if (plotData[i][j] < minAmp) minAmp =
                        plotData[i][j]
                }
            }
            println("---------------------------------------------------")
            println("Maximum amplitude: $maxAmp")
            println("Minimum amplitude: $minAmp")
            println("---------------------------------------------------")

            //Normalization
            val diff = maxAmp - minAmp
            for (i in 0 until nX) {
                for (j in 0 until WS) {
                    plotData[i][j] = (plotData[i][j] - minAmp) / diff
                }
            }

            //plot image
            val anImage: Image
            val paint = Paint()
            val theImage = BufferedImage(nX, WS, BufferedImage.TYPE_INT_RGB)
            var ratio: Double
            for (x in 0 until nX) {
                for (y in 0 until WS) {
                    ratio = plotData[x][y]

                    //theImage.setRGB(x, y, new Color(red, green, 0).getRGB());
                    val newColor: Color = getColor((1.0 - ratio).toInt())
                    theImage.setRGB(x, y, newColor.getRGB())
                }
            }
            val outputfile = File("saved.png")
            ImageIO.write(theImage, "png", outputfile)
        } catch (e: IOException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }*/
    }
}