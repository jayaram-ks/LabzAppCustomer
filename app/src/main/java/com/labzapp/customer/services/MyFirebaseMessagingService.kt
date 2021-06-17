package com.labzapp.customer.services

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.labzapp.customer.models.FbIdUpdateResponse
import com.labzapp.customer.storage.SharedPrefManager
import com.labzapp.customer.utilities.NotificationHelper
import com.labzapp.customer.utilities.toastz
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyFirebaseMessagingService : FirebaseMessagingService() {


    override fun onMessageReceived(remotMessg: RemoteMessage) {
        super.onMessageReceived(remotMessg)
        if (remotMessg!!.notification != null) {
            val title = remotMessg.notification!!.title
            val body = remotMessg.notification!!.body
            NotificationHelper.displayNotification(applicationContext, title!!, body!!)
        }
    }





}