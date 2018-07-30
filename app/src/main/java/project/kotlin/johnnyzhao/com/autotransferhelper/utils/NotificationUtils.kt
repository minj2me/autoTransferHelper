package project.kotlin.johnnyzhao.com.autotransferhelper.utils

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.support.v4.app.NotificationCompat
import project.kotlin.johnnyzhao.com.autotransferhelper.MyApplication

class NotificationUtils {
    private fun NotificationUtils() {
        //no instance
    }

    companion object {//companion object doing the job like static

        fun buildNotification(context: Context, smallIcon: Int, contentTitle: String,
                              contentText: String, autoCancel: Boolean, intent: PendingIntent): Notification {
            return buildNotification(context, smallIcon, contentTitle, contentText, autoCancel, 0, intent)
        }

        fun buildNotification(context: Context, smallIcon: Int, contentTitle: String,
                              contentText: String, autoCancel: Boolean, defaultId: Int, intent: PendingIntent): Notification {
            val builder = NotificationCompat.Builder(context)
            return builder.setSmallIcon(smallIcon).setContentTitle(contentTitle)
                    .setContentText(contentText).setContentIntent(intent)
                    .setDefaults(defaultId)
                    .setAutoCancel(autoCancel).build()
        }

        fun sendNotification(context: Context, smallIcon: Int, contentTitle: String, contentText: String,
                             autoCancel: Boolean, defaultId: Int, intent: PendingIntent, notificationId: Int) {
            val notification = NotificationUtils.buildNotification(
                    context, smallIcon, contentTitle,
                    contentText, autoCancel, defaultId, intent)
            val manager = MyApplication.getApplicationContext(context)
                    .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(notificationId, notification)
        }
    }
}