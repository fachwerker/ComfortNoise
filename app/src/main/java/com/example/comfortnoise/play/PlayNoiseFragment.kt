package com.example.comfortnoise.play

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.ToggleButton
import com.example.comfortnoise.CanvasSpectogram
import com.example.comfortnoise.R

class PlayNoiseFragment : Fragment() {
    private lateinit var spectogramview: CanvasSpectogram
    private lateinit var audioService: PlayService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_noise, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        spectogramview = view.findViewById(R.id.canvasNoise)
        audioService = PlayService(spectogramview/*, mReceiver!!*/)

        registerButtonOnCheckedCallback()
    }

    private fun registerButtonOnCheckedCallback() {
        class ToggleButtons(val buttonId: Int, val filename: String)

        val toggleButtons: Array<ToggleButtons> = arrayOf(
            ToggleButtons(R.id.blue_noise, "blue_noise"),
            ToggleButtons(R.id.blue_noise, "blue_noise"),
            ToggleButtons(R.id.brown_noise, "brown_noise"),
            ToggleButtons(R.id.grey_noise, "grey_noise_itu468"),
            ToggleButtons(R.id.sine_sweep, "sine_sweep"),
            ToggleButtons(R.id.white_noise, "white_noise"),
            ToggleButtons(R.id.pink_noise, "pink_noise"),
        )
        for (toggleButton in toggleButtons) {
            val button = view?.findViewById<ToggleButton>(toggleButton.buttonId)
            button?.setBackgroundResource(android.R.drawable.btn_default)
            button?.setOnCheckedChangeListener() { compoundButton: CompoundButton, isChecked: Boolean ->
                if (isChecked) {
                    audioService.stopPlaying()
                    for (cbToggleButton in toggleButtons) {
                        val cbButton = view?.findViewById<ToggleButton>(cbToggleButton.buttonId)
                        cbButton?.setBackgroundResource(android.R.drawable.btn_default)
                    }
                    compoundButton.setBackgroundColor(Color.GREEN)
                    val resId = resources.getIdentifier(
                        toggleButton.filename,
                        "raw",
                        requireActivity().packageName
                    )
                    audioService.startAudioThread(resources.openRawResource(resId))
                } else {
                    compoundButton.setBackgroundResource(android.R.drawable.btn_default);
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