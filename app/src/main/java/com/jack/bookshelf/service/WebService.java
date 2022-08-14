package com.jack.bookshelf.service;

import static com.jack.bookshelf.constant.AppConstant.ActionDoneService;
import static com.jack.bookshelf.constant.AppConstant.ActionStartService;

import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.jack.bookshelf.MApplication;
import com.jack.bookshelf.R;
import com.jack.bookshelf.utils.NetworkUtils;
import com.jack.bookshelf.web.HttpServer;
import com.jack.bookshelf.web.WebSocketServer;

import java.io.IOException;
import java.net.InetAddress;

public class WebService extends Service {
    private static boolean sIsRunning = false;
    private HttpServer httpServer;
    private WebSocketServer webSocketServer;

    /**
     * Start the web service, return true if the service can be started normally, false if it is started.
     *
     * @param context Indicates component context.
     * @return true if the service can be started normally, false if it is started.
     */
    public static boolean startThis(Context context) {
        if (sIsRunning) {
            return false;
        } else {
            Intent intent = new Intent(context, WebService.class);
            intent.setAction(ActionStartService);
            context.startService(intent);
            return true;
        }
    }

    public static void upHttpServer(Activity activity) {
        if (sIsRunning) {
            Intent intent = new Intent(activity, WebService.class);
            intent.setAction(ActionStartService);
            activity.startService(intent);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        updateNotification(getString(R.string.web_service_starting_hint_short));
        new Handler(Looper.getMainLooper())
                .post(() -> Toast.makeText(this, R.string.web_service_starting_hint_long, Toast.LENGTH_SHORT).show());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case ActionStartService:
                        upServer();
                        break;
                    case ActionDoneService:
                        stopSelf();
                        break;
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void upServer() {
        if (httpServer != null && httpServer.isAlive()) {
            httpServer.stop();
        }
        if (webSocketServer != null && webSocketServer.isAlive()) {
            webSocketServer.stop();
        }
        int port = getPort();
        httpServer = new HttpServer(port);
        webSocketServer = new WebSocketServer(port + 1);
        InetAddress inetAddress = NetworkUtils.getLocalIPAddress();
        if (inetAddress != null) {
            try {
                httpServer.start();
                webSocketServer.start(1000 * 30); // 通信超时设置
                sIsRunning = true;
                updateNotification(getString(R.string.http_ip, inetAddress.getHostAddress(), port));
            } catch (IOException e) {
                stopSelf();
            }
        } else {
            stopSelf();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sIsRunning = false;
        if (httpServer != null && httpServer.isAlive()) {
            httpServer.stop();
        }
        if (webSocketServer != null && webSocketServer.isAlive()) {
            webSocketServer.stop();
        }
    }

    private PendingIntent getThisServicePendingIntent() {
        Intent intent = new Intent(this, this.getClass());
        intent.setAction(ActionDoneService);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private int getPort() {
        int port = MApplication.getConfigPreferences().getInt("webPort", 1122);
        if (port > 65530 || port < 1024) {
            port = 1122;
        }
        return port;
    }

    /**
     * 更新通知
     */
    private void updateNotification(String content) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, MApplication.channelIdWeb)
                .setSmallIcon(R.drawable.ic_web_service_noti)
                .setOngoing(true)
                .setContentTitle(getString(R.string.web_service))
                .setContentText(content);
        builder.addAction(R.drawable.ic_stop_black_24dp, getString(R.string.cancel), getThisServicePendingIntent());
        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        Notification notification = builder.build();
        int notificationId = 1122;
        startForeground(notificationId, notification);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
