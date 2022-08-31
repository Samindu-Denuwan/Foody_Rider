package com.jiat.rider;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

public class MyFirebaseMessaging extends FirebaseMessagingService {
    private static final String NOTIFICATION_CHANNEL_ID = "MY_NOTIFICATION_CHANNEL_ID";
    private static final String TAG = "Noti";

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        //all notifications will be received here

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        //get data from notification

        String notificationType = remoteMessage.getData().get("notificationType");
        if(notificationType.equals("NewOrderRider")){
            String buyerUid = remoteMessage.getData().get("buyerUid");
            String sellerUid = remoteMessage.getData().get("sellerUid");
            String riderUid = remoteMessage.getData().get("riderUid");
            String orderId = remoteMessage.getData().get("orderId");
            String notificationTitle = remoteMessage.getData().get("notificationTitle");
            String notificationDescription = remoteMessage.getData().get("notificationMessage");

            if(firebaseUser != null && firebaseAuth.getUid().equals(riderUid)){
                //user is signed in and same user to which notification sent
                showNotification(orderId,riderUid, sellerUid, buyerUid,notificationTitle, notificationDescription, notificationType);
                Log.i(TAG, "rider");
            }else{
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
            }



        }
    /*    if(notificationType.equals("OrderStatusChanged")){
            String buyerUid = remoteMessage.getData().get("buyerUid");
            String sellerUid = remoteMessage.getData().get("sellerUid");
            String orderId = remoteMessage.getData().get("orderId");
            String notificationTitle = remoteMessage.getData().get("notificationTitle");
            String notificationDescription = remoteMessage.getData().get("notificationMessage");

            if(firebaseUser != null && firebaseAuth.getUid().equals(buyerUid)){
                //user is signed in and same user to which notification sent
                showNotification(orderId, sellerUid, buyerUid,notificationTitle, notificationDescription, notificationType);
                Log.i(TAG, "Customer");
            }
        }*/

        //new add
     /*   if(notificationType.equals("OrderPlaced")){
            String buyerUid = remoteMessage.getData().get("buyerUid");
            String sellerUid = remoteMessage.getData().get("sellerUid");
            String orderId = remoteMessage.getData().get("orderId");
            String notificationTitle = remoteMessage.getData().get("notificationTitle");
            String notificationDescription = remoteMessage.getData().get("notificationMessage");

            if(firebaseUser != null && firebaseAuth.getUid().equals(buyerUid)){
                //user is signed in and same user to which notification sent
                showNotification(orderId, sellerUid, buyerUid,notificationTitle, notificationDescription, notificationType);
            }


        }*/
        //new add

    }

    private void showNotification(String orderId,String riderUid, String sellerUid, String buyerUid, String notificationTitle, String notificationDescription, String notificationType) {
        //notification
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        Log.i(TAG, "show Notification");

        //id for notification, random
        int notificationID = new Random().nextInt(3000);

        //check if android version is ore0/0 or above
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            setUpNotificationChannel(notificationManager);
            Log.i(TAG, "Go to Build Version Code");

        }
        //handle notification click
        Intent intent = null;
        if(notificationType.equals("NewOrderRider")) {
            //open OrderDetailsRider Activity
            intent = new Intent(this, OrdersRiderDetailsActivity.class);
            intent.putExtra("orderId", orderId);
            intent.putExtra("orderBy", buyerUid);
            intent.putExtra("orderTo", sellerUid);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            Log.i(TAG, "New Order to Rider");
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        //Large icon

        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.logo_foody);

        //sound of notification
        Uri notificationSounUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        notificationBuilder.setSmallIcon(R.drawable.ic_logo_notification_foody)
                .setLargeIcon(largeIcon)
                .setContentTitle(notificationTitle)
                .setContentText(notificationDescription)
                .setSound(notificationSounUri)
                .setAutoCancel(true) //cancel , dismiss when clicked
                .setContentIntent(pendingIntent); //add intent

        //show  notification
        notificationManager.notify(notificationID, notificationBuilder.build());
       /* Toast.makeText(this, "Notification ...", Toast.LENGTH_SHORT).show();*/

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setUpNotificationChannel(NotificationManager notificationManager) {
        CharSequence channelName = "Some Sample Text";
        String channelDescription = "Channel Description here";

        NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName,NotificationManager.IMPORTANCE_HIGH);
        notificationChannel.setDescription(channelDescription);
        notificationChannel.enableLights(true);
        notificationChannel.setLightColor(Color.RED);
        notificationChannel.enableVibration(true);
        if(notificationManager != null){
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }
}
