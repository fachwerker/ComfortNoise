package com.example.comfortnoise

//import android.R

import android.graphics.Color
import android.media.AudioTrack
import android.media.SoundPool
import android.os.Bundle
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatActivity
import com.example.comfortnoise.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding


    // Canvas
    private lateinit var spectogramview: CanvasSpectogram


    private lateinit var audioService: AudioService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater)
        spectogramview = binding.myCanvas
        setContentView(binding.root)

        audioService = AudioService(spectogramview)

        class Buttons(val button: android.widget.ToggleButton,val filename: String)
        val buttons: Array<Buttons> = arrayOf(
            Buttons(binding.PinkNoise,"pink_noise"),
            Buttons(binding.WhiteNoise,"white_noise")
        )
        for (button in buttons)
        {
            button.button.setOnCheckedChangeListener() { compoundButton: CompoundButton, isChecked: Boolean ->
                if (isChecked) {
                    compoundButton.setBackgroundColor(Color.GREEN)

                    val resId = resources.getIdentifier(button.filename, "raw", packageName)

                    audioService.startAudioThread(resources.openRawResource(resId))
                } else {
                    compoundButton.setBackgroundColor(Color.RED)
                    audioService.stopPlaying()
                }
            }
        }

    }

    override fun onDestroy() {
        audioService.stopPlaying()

        super.onDestroy()

    }

}