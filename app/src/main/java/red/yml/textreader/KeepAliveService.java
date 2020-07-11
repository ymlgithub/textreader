package red.yml.textreader;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.util.Log;

import static android.app.PendingIntent.getActivity;

public class KeepAliveService extends Service {
    private static final String TAG = "KeepAliveService";

    public KeepAliveService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel(TAG,"朗读",NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        Notification.Builder builder = new Notification.Builder(this.getApplicationContext(), TAG); //获取一个Notification构造器
        Intent nfIntent = new Intent(this, MainActivity.class);

        builder.setContentIntent(PendingIntent.getActivity(this, 0, nfIntent, 0))
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.mipmap.read))
                .setContentTitle("朗读")
                .setSmallIcon(R.mipmap.read)
                .setContentText("朗读后台运行中，请勿关闭。")
                .setWhen(System.currentTimeMillis()); // 设置该通知发生的时间

        Notification notification = builder.build(); // 获取构建好的Notification
//        notification.defaults = Notification.DEFAULT_SOUND; //设置为默认的声音
        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        startForeground((int) System.currentTimeMillis(), notification);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        super.onDestroy();
    }
}
