package red.yml.textreader;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.baidu.ocr.demo.FileUtil;
import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.GeneralBasicParams;
import com.baidu.ocr.sdk.model.GeneralResult;
import com.baidu.ocr.sdk.model.WordSimple;
import com.baidu.ocr.ui.camera.CameraActivity;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private EditText et;

    private static final String TAG = "MainActivity";

    private static final int REQUEST_CODE_GENERAL = 105;
    private static final int REQUEST_CODE_GENERAL_BASIC = 106;
    private static final int REQUEST_CODE_ACCURATE_BASIC = 107;
    private static final int REQUEST_CODE_ACCURATE = 108;
    private static final int REQUEST_CODE_GENERAL_ENHANCED = 109;
    private static final int REQUEST_CODE_GENERAL_WEBIMAGE = 110;
    private static final int REQUEST_CODE_BANKCARD = 111;
    private static final int REQUEST_CODE_VEHICLE_LICENSE = 120;
    private static final int REQUEST_CODE_DRIVING_LICENSE = 121;
    private static final int REQUEST_CODE_LICENSE_PLATE = 122;
    private static final int REQUEST_CODE_BUSINESS_LICENSE = 123;
    private static final int REQUEST_CODE_RECEIPT = 124;

    private static final int REQUEST_CODE_PASSPORT = 125;
    private static final int REQUEST_CODE_NUMBERS = 126;
    private static final int REQUEST_CODE_QRCODE = 127;
    private static final int REQUEST_CODE_BUSINESSCARD = 128;
    private static final int REQUEST_CODE_HANDWRITING = 129;
    private static final int REQUEST_CODE_LOTTERY = 130;
    private static final int REQUEST_CODE_VATINVOICE = 131;
    private static final int REQUEST_CODE_CUSTOM = 132;

    private static final int REQUEST_CODE_OVERLAY = 201;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        et = findViewById(R.id.editText);
        startHiddenOverlayService();
    }

    /**
     * 设置悬浮窗
     */
    private void startHiddenOverlayService() {
        Log.d(TAG, "startHiddenOverlayService: " + Build.VERSION.SDK_INT);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Log.d(TAG, "startFloatingService: 未获得悬浮窗权限");
            Toast.makeText(this, "请授予悬浮窗权限！", Toast.LENGTH_SHORT);
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQUEST_CODE_OVERLAY);
            return;
        }
        if (!MyApplication.isOverlayStart) {
            Log.d(TAG, "startFloatingService: 有悬浮窗权限");
            startService(new Intent(MainActivity.this, HiddenOverlayService.class));
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(false);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void cancelRead(View v) {
        ((MyApplication) getApplication()).cancelRead();
    }

    public void startRead(View v) {
        ((MyApplication) getApplication()).read(et.getText().toString());
    }

    public void paste(View v) {
        String clipboard = ClipboardUtils.getClipboard();
        et.setText(clipboard);
    }

    public void copy(View v) {
        ClipboardUtils.setClipboard(et.getText().toString());
    }

    public void clear(View v) {
        et.setText("");
    }

    public void readImage(View view) {
        Intent intent = new Intent(MainActivity.this, CameraActivity.class);
        intent.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH, FileUtil.getSaveFile(getApplication()).getAbsolutePath());
        intent.putExtra(CameraActivity.KEY_CONTENT_TYPE, CameraActivity.CONTENT_TYPE_GENERAL);
        startActivityForResult(intent, REQUEST_CODE_ACCURATE_BASIC);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_CODE_ACCURATE_BASIC:
                Log.d(TAG, "onActivityResult: 通用文字识别（高精度版）");
                if (resultCode == RESULT_OK) {
//                    // 获取调用参数
//                    String contentType = data.getStringExtra(CameraActivity.KEY_CONTENT_TYPE);
                    // 通过临时文件获取拍摄的图片
                    String filePath = FileUtil.getSaveFile(getApplicationContext()).getAbsolutePath();
                    recognizeAccurateBasic(data, filePath);
                }
                break;
            case REQUEST_CODE_OVERLAY:
                Log.d(TAG, "onActivityResult: 悬浮窗权限申请");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                    Log.d(TAG, "onActivityResult: 悬浮窗权限获取失败");
                    Toast.makeText(this, "悬浮窗权限获取失败", Toast.LENGTH_LONG).show();
                } else {
                    Log.d(TAG, "onActivityResult: 悬浮窗权限获取成功");
                    Toast.makeText(this, "悬浮窗权限获取成功", Toast.LENGTH_SHORT).show();
                    if (!MyApplication.isOverlayStart)
                        startService(new Intent(MainActivity.this, HiddenOverlayService.class));
                }
                break;
            default:
                break;
        }

//            // 获取调用参数
//            String contentType = data.getStringExtra(CameraActivity.KEY_CONTENT_TYPE);
//            // 通过临时文件获取拍摄的图片
//            String filePath = FileUtil.getSaveFile(getApplicationContext()).getAbsolutePath();
//            // 判断拍摄类型（通用，身份证，银行卡等）
////        if (requestCode == REQUEST_CODE_GENERAL && resultCode == Activity.RESULT_OK) {
////            // 判断是否是身份证正面
////            if (CameraActivity.CONTENT_TYPE_ID_CARD_FRONT.equals(contentType)) {
////                // 获取图片文件调用sdk数据接口，见数据接口说明
////            }
////        }
//            if (requestCode == REQUEST_CODE_ACCURATE_BASIC && resultCode == Activity.RESULT_OK) {
//                Log.d(TAG, "onActivityResult: 通用文字识别（高精度版）");
//                recognizeAccurateBasic(data, filePath);
//            }
    }

    private void recognizeAccurateBasic(Intent data, String filePath) {
        GeneralBasicParams param = new GeneralBasicParams();
        param.setDetectDirection(true);
        param.setImageFile(new File(filePath));

        OCR.getInstance(getApplicationContext()).recognizeAccurateBasic(param, new OnResultListener<GeneralResult>() {
            @Override
            public void onResult(GeneralResult generalResult) {
                Log.d(TAG, "OCR onResult: \n" + generalResult.getJsonRes());
                StringBuilder sb = new StringBuilder();
                for (WordSimple wb : generalResult.getWordList()) {
                    sb.append(wb.getWords());
                }
                String res = sb.toString();
                if (generalResult.getWordsResultNumber() == 0) {
                    res = "文字识别失败！";
                }
                ClipboardUtils.setClipboard(res);
            }

            @Override
            public void onError(OCRError ocrError) {
                Log.e(TAG, "onError: ", ocrError);
                ClipboardUtils.setClipboard("文字识别失败！");
            }
        });
    }
}
