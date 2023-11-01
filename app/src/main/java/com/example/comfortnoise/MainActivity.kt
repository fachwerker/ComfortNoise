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
    val buffLength: Int = 4096//AudioTrack.getMinBufferSize(Fs, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)



    // Canvas
    private lateinit var spectogramview: CanvasSpectogram


    private lateinit var audioService: AudioService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        //  init sound pool
        initSoundPool()
        binding = ActivityMainBinding.inflate(layoutInflater)
        spectogramview = binding.myCanvas
        setContentView(binding.root)

        audioService = AudioService(spectogramview)

        /*streamID = soundPool!!.load(this, R.raw.white_noise, 1)

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
        }*/


        binding.WhiteNoise.setOnCheckedChangeListener() { compoundButton: CompoundButton, isChecked: Boolean ->
            if (isChecked) {
                compoundButton.setBackgroundColor(Color.GREEN)
                audioService.startAudioThread(R.raw.white_noise)
            } else {
                compoundButton.setBackgroundColor(Color.RED)
                audioService.stopPlaying()
            }
        }

        binding.PinkNoise.setOnCheckedChangeListener() { compoundButton: CompoundButton, isChecked: Boolean ->
            if (isChecked) {
                compoundButton.setBackgroundColor(Color.GREEN)
                audioService.startAudioThread(R.raw.white_noise)
            } else {
                compoundButton.setBackgroundColor(Color.RED)
                audioService.stopPlaying()
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