package com.example.comfortnoise.record

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
import com.example.comfortnoise.CanvasSpectogram
import com.example.comfortnoise.R

class RecordMicFragment : Fragment() {
    private lateinit var spectogramview: CanvasSpectogram
    private lateinit var recordService: RecordService
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
        recordService = RecordService(spectogramview/*, mReceiver!!*/)

        registerButtonOnCheckedCallback()
    }
    lateinit var micButton: ToggleButton
    lateinit var btcMicButton: ToggleButton

    lateinit var am: AudioManager
    private fun registerButtonOnCheckedCallback()
    {
        micButton = view?.findViewById<ToggleButton>(R.id.microphone)!!
        btcMicButton = view?.findViewById<ToggleButton>(R.id.bluetoothMic)!!

        micButton?.setOnCheckedChangeListener() { compoundButton: CompoundButton, isChecked: Boolean ->
            if (isChecked) {
                recordService.stopRecording()
                btcMicButton.setBackgroundResource(android.R.drawable.btn_default)
                micButton.setBackgroundResource(android.R.drawable.btn_default)

                compoundButton.setBackgroundColor(Color.GREEN)

                this.context?.let { recordService.startMicrophoneThread(it) }
            } else {
                compoundButton.setBackgroundResource(android.R.drawable.btn_default);
                recordService.stopRecording()
            }
        }
        am = requireActivity().getSystemService(Context.AUDIO_SERVICE) as AudioManager
        // am = getSystemService(Fragment.AUDIO_SERVICE) as AudioManager

        btcMicButton?.setOnCheckedChangeListener() { compoundButton: CompoundButton, isChecked: Boolean ->
            if (isChecked) {
                recordService.stopRecording()
                btcMicButton.setBackgroundResource(android.R.drawable.btn_default)
                micButton.setBackgroundResource(android.R.drawable.btn_default)
                compoundButton.setBackgroundColor(Color.GREEN)


                activity?.registerReceiver(object : BroadcastReceiver() {
                    override fun onReceive(context: Context, intent: Intent) {
                        val state = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, -1)
                        Log.d(ContentValues.TAG, "Audio SCO state: $state")
                        if (AudioManager.SCO_AUDIO_STATE_CONNECTED == state) {
                            recordService.startMicrophoneThread(context)
                            activity?.unregisterReceiver(this)
                        }
                    }
                }, IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED ))

                Log.d(ContentValues.TAG, "starting bluetooth")
                am.startBluetoothSco()
            } else {
                compoundButton.setBackgroundResource(android.R.drawable.btn_default);
                recordService.stopRecording()

                am.stopBluetoothSco()
            }
        }
    }

    override fun onPause() {
        recordService.onPause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        recordService.onResume()
    }

    override fun onDestroy() {
        recordService.stopRecording()
        super.onDestroy()
    }

}