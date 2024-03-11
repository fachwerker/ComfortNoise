package com.example.comfortnoise

import android.content.BroadcastReceiver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.media.AudioManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.ToggleButton

class RecordMicFragment : Fragment() {
    private lateinit var spectogramview: CanvasSpectogram
    private lateinit var audioService: AudioService
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_record_mic, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        spectogramview = view.findViewById(R.id.canvasMic)
        audioService = AudioService(spectogramview/*, mReceiver!!*/)

        registerButtonOnCheckedCallback()
    }

    lateinit var am: AudioManager
    private fun registerButtonOnCheckedCallback()
    {
        val micButton = view?.findViewById<ToggleButton>(R.id.microphone)
        val btcMicButton = view?.findViewById<ToggleButton>(R.id.bluetoothMic)

        micButton?.setOnCheckedChangeListener() { compoundButton: CompoundButton, isChecked: Boolean ->
            if (isChecked) {
                audioService.stopPlaying()
                btcMicButton?.setBackgroundResource(android.R.drawable.btn_default)
                micButton.setBackgroundResource(android.R.drawable.btn_default)

                compoundButton.setBackgroundColor(Color.GREEN)

                this.context?.let { audioService.startMicrophoneThread(it) }
            } else {
                compoundButton.setBackgroundResource(android.R.drawable.btn_default);
                audioService.stopPlaying()
            }
        }
        am = requireActivity().getSystemService(Context.AUDIO_SERVICE) as AudioManager
        // am = getSystemService(Fragment.AUDIO_SERVICE) as AudioManager

        btcMicButton?.setOnCheckedChangeListener() { compoundButton: CompoundButton, isChecked: Boolean ->
            if (isChecked) {
                audioService.stopPlaying()
                btcMicButton.setBackgroundResource(android.R.drawable.btn_default)
                micButton?.setBackgroundResource(android.R.drawable.btn_default)
                compoundButton.setBackgroundColor(Color.GREEN)


                activity?.registerReceiver(object : BroadcastReceiver() {
                    override fun onReceive(context: Context, intent: Intent) {
                        val state = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, -1)
                        Log.d(ContentValues.TAG, "Audio SCO state: $state")
                        if (AudioManager.SCO_AUDIO_STATE_CONNECTED == state) {
                            audioService.startMicrophoneThread(context)
                            activity?.unregisterReceiver(this)
                        }
                    }
                }, IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED ))

                Log.d(ContentValues.TAG, "starting bluetooth")
                am.startBluetoothSco()
            } else {
                compoundButton.setBackgroundResource(android.R.drawable.btn_default);
                audioService.stopPlaying()

                am.stopBluetoothSco()
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