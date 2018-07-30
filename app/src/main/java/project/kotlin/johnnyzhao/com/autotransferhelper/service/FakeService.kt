package project.kotlin.johnnyzhao.com.autotransferhelper.service

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.app.PendingIntent
import android.util.Log
import project.kotlin.johnnyzhao.com.autotransferhelper.MainActivity
import project.kotlin.johnnyzhao.com.autotransferhelper.R
import project.kotlin.johnnyzhao.com.autotransferhelper.utils.NotificationUtils

/**
 * 用于挂在前台，尽量保证AutoTransferService不被kill
 * */
class FakeService : Service() {

    val TAG: String = "FakeService"

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //return super.onStartCommand(intent, flags, startId)
        keepServiceAlive()
//        val notification = Notification()
//        startForeground(-29000, notification)
        Log.d(TAG, "onStartCommand")
        return Service.START_STICKY
    }

    fun keepServiceAlive() {
//        val notificationIntent = Intent(this, MainActivity::class.java)
//        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)
//        val notification = NotificationCompat.Builder(this).setContentTitle(getString(R.string.app_name))
//                .setContentText(getString(R.string.accessibility_description))
//                .setSmallIcon(R.mipmap.ic_launcher)
//                .setContentIntent(pendingIntent)
//                .build()
        val notification = NotificationUtils.buildNotification(this,
                R.mipmap.ic_launcher,
                getString(R.string.app_name),
                getString(R.string.accessibility_description),
                false,
                PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), 0))
        startForeground(Notification.FLAG_ONGOING_EVENT, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true)
    }
}