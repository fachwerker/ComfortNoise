package com.example.comfortnoise

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.widget.RemoteViews
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import com.example.comfortnoise.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var notificationView: RemoteViews

    private lateinit var audioService: PlayService

    // private var mReceiver: ScreenReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
}