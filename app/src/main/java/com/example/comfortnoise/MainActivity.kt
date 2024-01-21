package com.example.comfortnoise

import android.graphics.Color
import android.os.Bundle
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatActivity
import com.example.comfortnoise.databinding.ActivityMainBinding

/*import android.content.Intent
import android.content.IntentFilter
import com.example.comfortnoise.AudioService.ScreenReceiver*/


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding


    // Canvas
    private lateinit var spectogramview: CanvasSpectogram

    private lateinit var audioService: AudioService

    // private var mReceiver: ScreenReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater)
        spectogramview = binding.myCanvas
        setContentView(binding.root)

        /*val intentFilter = IntentFilter(Intent.ACTION_SCREEN_ON)
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF)
        mReceiver = ScreenReceiver()
        registerReceiver(mReceiver, intentFilter)*/
        audioService = AudioService(spectogramview/*, mReceiver!!*/)

        class Buttons(val button: android.widget.ToggleButton,val filename: String)
        val buttons: Array<Buttons> = arrayOf(
            Buttons(binding.blueNoise,"blue_noise"),
            Buttons(binding.brownNoise,"brown_noise"),
            Buttons(binding.fuzz,"fuzz"),
            Buttons(binding.greyNoise,"grey_noise"),
            Buttons(binding.sineSweep,"sine_sweep"),
            Buttons(binding.WhiteNoise,"white_noise_short"),
            Buttons(binding.PinkNoise,"pink_noise"),
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

    override fun onPause() {
        audioService.onPause()
        super.onPause()
    }


    override fun onResume() {
        super.onResume()
        audioService.onResume()
    }


    override fun onDestroy() {
        audioService.stopPlaying()

        super.onDestroy()

    }

}