package com.example.comfortnoise

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.CompoundButton
import android.widget.RemoteViews
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.example.comfortnoise.databinding.ActivityMainBinding

/*import android.content.Intent
import android.content.IntentFilter
import com.example.comfortnoise.AudioService.ScreenReceiver*/


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var notificationView: RemoteViews

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

        registerButtonOnCheckedCallback()

        registerNotificationButtons()
        sendNotification()
    }

    private fun registerButtonOnCheckedCallback()
    {
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
            button.button.setBackgroundResource(android.R.drawable.btn_default)
            button.button.setOnCheckedChangeListener() { compoundButton: CompoundButton, isChecked: Boolean ->
                if (isChecked) {
                    audioService.stopPlaying()
                    for (button in buttons)
                    {
                        button.button.setBackgroundResource(android.R.drawable.btn_default)
                    }
                    compoundButton.setBackgroundColor(Color.GREEN)
                    val resId = resources.getIdentifier(button.filename, "raw", packageName)
                    audioService.startAudioThread(resources.openRawResource(resId))
                } else {
                    compoundButton.setBackgroundResource(android.R.drawable.btn_default);
                    audioService.stopPlaying()
                }
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
        // Erstelle eine RemoteViews-Instanz mit dem Layout deiner benutzerdefinierten Ansicht
        // val notification_view = RemoteViews(packageName, R.layout.notification_view)

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
                .setContentTitle("You've been notified!")
                .setContentText("This is your notification text.")
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