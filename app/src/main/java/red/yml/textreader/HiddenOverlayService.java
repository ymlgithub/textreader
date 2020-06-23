package red.yml.textreader;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * 隐藏的浮窗，确保Android Q上能获取焦点，读取剪贴板
 */
public class HiddenOverlayService extends Service {
    private static final String TAG = "HiddenOverlayService";
    public static boolean isOverlayStart = false;

    //    private static int FLAGS_ON = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
//            | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
//            | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
//    private static int FLAGS_OFF = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
//            | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
//            | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
//            | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
    private static int FLAGS_ON = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
    private static int FLAGS_OFF = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
            | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;
    //    private ImageButton btnCtl;
    private TextView btnCtl;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isOverlayStart) {
            showFloatingWindow();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @SuppressLint("InflateParams")
    private void showFloatingWindow() {
        Log.d(TAG, "showFloatingWindow: ");
        isOverlayStart = true;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(getApplicationContext())) {
            // 获取WindowManager服务
            windowManager = (WindowManager) getApplicationContext().getSystemService(WINDOW_SERVICE);
            // 设置LayoutParam
            layoutParams = new WindowManager.LayoutParams();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
            }
            layoutParams.format = PixelFormat.RGBA_8888;
            layoutParams.flags = FLAGS_OFF;
//            layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;

            //宽高自适应
            layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
//            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            //显示的位置
            DisplayMetrics outMetrics = new DisplayMetrics();
            windowManager.getDefaultDisplay().getRealMetrics(outMetrics);
//            layoutParams.x = outMetrics.widthPixels / 6;
//            Log.d(TAG, String.format("showFloatingWindow: (%d,%d)", outMetrics.widthPixels, outMetrics.heightPixels));
//            layoutParams.y = -outMetrics.heightPixels / 2 + (int) (getStatusBarHeight(getApplicationContext()) * 3 / 2);

            //隐藏
//            layoutParams.width = 0;
//            layoutParams.height = 0;
            layoutParams.x = 0;
            layoutParams.y = 0;

            Typeface font = Typeface.createFromAsset(getAssets(), "fontawesome-webfont.ttf");

            // 新建悬浮窗控件
            final View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.hidden_float_window, null);
            btnCtl = view.findViewById(R.id.btn_ctl);
//            btnCtl.setTextSize(getStatusBarHeight(getApplicationContext()) / outMetrics.density);
//            btnCtl.setTextSize(getStatusBarHeight(getApplicationContext()) / outMetrics.scaledDensity);


            btnCtl.setTypeface(font);
            btnCtl.setOnTouchListener(new FloatingOnTouchListener(view));

            btnCtl.setOnClickListener(new View.OnClickListener() {
                boolean flag = false;

                @Override
                public void onClick(View v) {
                    if (flag) {
                        layoutParams.flags = FLAGS_OFF;
                        windowManager.updateViewLayout(view, layoutParams);
                        btnCtl.setText(R.string.btn_off);
                        btnCtl.setTextColor(getBaseContext().getColor(R.color.btnOff));
//                        btnCtl.setImageResource(R.mipmap.stop_circle_line);
                    } else {
                        layoutParams.flags = FLAGS_ON;
                        windowManager.updateViewLayout(view, layoutParams);
                        btnCtl.setText(R.string.btn_on);
                        btnCtl.setTextColor(getBaseContext().getColor(R.color.btnOn));
//                        btnCtl.setImageResource(R.mipmap.play_circle_line);
                    }
                    flag = !flag;
                }
            });
            // 将悬浮窗控件添加到WindowManager
            windowManager.addView(view, layoutParams);
        }
    }


    private class FloatingOnTouchListener implements View.OnTouchListener {
        private int x;
        private int y;
        private View overlay;

        public FloatingOnTouchListener(View overlay) {
            this.overlay = overlay;
        }

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View view, MotionEvent event) {
//            Log.i(TAG, "onTouch: ");
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    x = (int) event.getRawX();
                    y = (int) event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    int nowX = (int) event.getRawX();
                    int nowY = (int) event.getRawY();
                    Log.d(TAG, String.format("(%d,%d)", nowX, nowY));
                    int movedX = nowX - x;
                    int movedY = nowY - y;
                    x = nowX;
                    y = nowY;
                    layoutParams.x = layoutParams.x + movedX;
                    layoutParams.y = layoutParams.y + movedY;
                    // 更新悬浮窗控件布局
                    windowManager.updateViewLayout(overlay, layoutParams);
                    break;
                default:
                    break;
            }
            return false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isOverlayStart = false;
    }

    /**
     * 获取状态栏高度
     *
     * @param context
     * @return
     */
    public static int getStatusBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        return resources.getDimensionPixelSize(resourceId);
    }
}
