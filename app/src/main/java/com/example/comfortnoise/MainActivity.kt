package com.example.comfortnoise

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.CompoundButton
import android.widget.RemoteViews
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import com.example.comfortnoise.databinding.ActivityMainBinding
import com.example.comfortnoise.databinding.FragmentNoiseBinding
import com.example.comfortnoise.databinding.FragmentRecordMicBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView


/*import android.content.Intent
import android.content.IntentFilter
import com.example.comfortnoise.AudioService.ScreenReceiver*/


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var bindingNoise: FragmentNoiseBinding
    private lateinit var bindingMicFragment: FragmentRecordMicBinding
    private lateinit var notificationView: RemoteViews

    // Canvas
    private lateinit var spectogramview: CanvasSpectogram

    private lateinit var audioService: AudioService

    // private var mReceiver: ScreenReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater)
        //spectogramview = binding.myCanvas
        setContentView(binding.root)

        // bindingNoise = FragmentNoiseBinding.inflate(layoutInflater)
        bindingMicFragment = FragmentRecordMicBinding.inflate(layoutInflater)
        spectogramview = bindingMicFragment.myCanvas
        //setContentView(bindingNoise.root)

        // Screen_on/off was replaced by overwrite of event onPause/onResume
        /*val intentFilter = IntentFilter(Intent.ACTION_SCREEN_ON)
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF)
        mReceiver = ScreenReceiver()
        registerReceiver(mReceiver, intentFilter)*/
        audioService = AudioService(spectogramview/*, mReceiver!!*/)


        registerButtonOnCheckedCallback()

        registerNotificationButtons()
        sendNotification()
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        //bottomNav.setOnNavigationItemSelectedListener(navListener)
        bottomNav.setOnItemSelectedListener(navListener)

        // as soon as the application opens the first fragment should
        // be shown to the user in this case it is algorithm fragment
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, PlayNoiseFragment()).commit()
    }


    private val navListener = NavigationBarView.OnItemSelectedListener{ item: MenuItem ->
        // By using switch we can easily get
        // the selected fragment
        // by using there id.
        var selectedFragment: Fragment? = null
        val itemId = item.itemId
        if (itemId == R.id.playNoise) {
            selectedFragment = PlayNoiseFragment()
        } else if (itemId == R.id.recordMic) {
            selectedFragment = RecordMicFragment()
        }
        // It will help to replace the
        // one fragment to other.
        if (selectedFragment != null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, selectedFragment).commit()
        }
        true
    }

    lateinit var am: AudioManager
    private fun registerButtonOnCheckedCallback()
    {

        bindingMicFragment.microphone.setOnCheckedChangeListener() { compoundButton: CompoundButton, isChecked: Boolean ->
            if (isChecked) {
                audioService.stopPlaying()
                /*for (button in buttons)
                {
                    button.button.setBackgroundResource(android.R.drawable.btn_default)
                }*/
                compoundButton.setBackgroundColor(Color.GREEN)

                audioService.startMicrophoneThread(this)
            } else {
                compoundButton.setBackgroundResource(android.R.drawable.btn_default);
                audioService.stopPlaying()
            }
        }

        am = getSystemService(AUDIO_SERVICE) as AudioManager

        bindingMicFragment.bluetoothMic.setOnCheckedChangeListener() { compoundButton: CompoundButton, isChecked: Boolean ->
            if (isChecked) {
                audioService.stopPlaying()
                /*for (button in buttons)
                {
                    button.button.setBackgroundResource(android.R.drawable.btn_default)
                }*/
                compoundButton.setBackgroundColor(Color.GREEN)


                registerReceiver(object : BroadcastReceiver() {
                    override fun onReceive(context: Context, intent: Intent) {
                        val state = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, -1)
                        Log.d(TAG, "Audio SCO state: $state")
                        if (AudioManager.SCO_AUDIO_STATE_CONNECTED == state) {
                            audioService.startMicrophoneThread(context)
                            unregisterReceiver(this)
                        }
                    }
                }, IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED ))

                Log.d(TAG, "starting bluetooth")
                am.startBluetoothSco()
            } else {
                compoundButton.setBackgroundResource(android.R.drawable.btn_default);
                audioService.stopPlaying()

                am.stopBluetoothSco()
            }
        }

    }

    // This ID can be the value you want.
    private val NOTIFICATION_ID = 0

    // This ID can be the value you want.
    private val NOTIFICATION_ID_STRING = "My Notifications"
    private lateinit var mNotifyManager : NotificationManager //  = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

    private fun registerNotificationButtons()
    {
        notificationView = RemoteViews(packageName, R.layout.notification_view)
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if ( intent.action.equals("com.example.comfortnoise.ACTION_PLAY")){
                    audioService.continuePlaying()
                }else if( intent.action.equals("com.example.comfortnoise.ACTION_PAUSE")) {
                    audioService.pausePlaying()
                }
            }
        }

        class NotificationButtons(val viewId: Int, val action: String)
        val buttons: Array<NotificationButtons> = arrayOf(
            NotificationButtons(R.id.notificationPlay,"com.example.comfortnoise.ACTION_PLAY"),
            NotificationButtons(R.id.notificationPause,"com.example.comfortnoise.ACTION_PAUSE"),
        )
        for (button in buttons) {
            registerReceiver(receiver, IntentFilter(button.action))
            val switchIntent = Intent(button.action)
            val pendingSwitchIntent =
                PendingIntent.getBroadcast(this, 0, switchIntent, PendingIntent.FLAG_IMMUTABLE)
            notificationView.setOnClickPendingIntent(button.viewId, pendingSwitchIntent)
        }
    }
    private fun sendNotification() {
        mNotifyManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        //Create the channel. Android will automatically check if the channel already exists
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_ID_STRING,
                "My Channel Name",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = "My notification channel description"
            mNotifyManager.createNotificationChannel(channel)
        }
        val notifyBuilder: NotificationCompat.Builder =
            NotificationCompat.Builder(this, NOTIFICATION_ID_STRING)
                .setContentTitle("Play/Pause Noise")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setCustomContentView(notificationView)
                .setSilent(true)
        //.setPriority(NotificationCompat.PRIORITY_HIGH)
        val myNotification = notifyBuilder.build()
        mNotifyManager.notify(NOTIFICATION_ID, myNotification)
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