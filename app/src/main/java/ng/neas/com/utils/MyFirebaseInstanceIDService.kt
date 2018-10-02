package ng.neas.com.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessaging
import ng.neas.com.MainActivity
import ng.neas.com.R




class MyFirebaseInstanceIDService :  FirebaseMessagingService() {



    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        super.onMessageReceived(remoteMessage)
        if (remoteMessage?.data?.size ?:0 >  0)
            handleData( remoteMessage?.data)
        if (remoteMessage?.notification != null)
           handleMessage(remoteMessage.notification?.body, remoteMessage.notification?.title)
    }

    private fun handleData(data: Map<String, String>?) {
        showNotification(data?.get("title"), data?.get("message"), applicationContext)
    }

    private fun handleMessage(body: String?, title: String?) {
        showNotification(title, body, applicationContext)
    }

    override fun onNewToken(token: String?) {
        super.onNewToken(token)
        sendRegistrationToServer(token)
    }

    private fun sendRegistrationToServer(token: String?) {
        FirebaseMessaging.getInstance().subscribeToTopic("Alert")
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                    }
                }
    }
    private fun showNotification(title: String?, message: String?, context: Context?) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channelName = context?.getString(R.string.app_name)
            val description = context?.getString(R.string.full_meaning)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            var channel = NotificationChannel(channelName, channelName, importance)
            channel.description = description
            var notificationManager = context?.getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }

//        var intent = Intent(context, MainActivity::class.java)
//        intent.putExtra(getString(R.string.data), title)
//        intent.putExtra(getString(R.string.full_meaning), message)
//        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//        var pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)


        val intent = Intent(null, Uri.parse("some data"), context, MainActivity::class.java)
        intent.putExtra(getString(R.string.data), title)
        intent.putExtra(getString(R.string.full_meaning), message)
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        var mBuilder = NotificationCompat.Builder(context!!, context.getString(R.string.app_name))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()
        var notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(100, mBuilder)

    }
}