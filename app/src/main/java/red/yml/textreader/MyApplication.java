package red.yml.textreader;

import android.app.Application;
import android.content.ClipboardManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.widget.Toast;

import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.AccessToken;

import java.util.Arrays;
import java.util.Locale;

public class MyApplication extends Application {
    private static final String TAG = "MyApplication";
    private ClipboardManager.OnPrimaryClipChangedListener clipChangedListener;
    private ClipboardManager manager;
    private TextToSpeech tts;
    private UtteranceProgressListener utteranceProgressListener;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: 启动");
        initClipboard();
        initTTS();
        initOCR();
    }

    private void initClipboard() {
        ClipboardUtils.init(getApplicationContext());
    }

    private void initOCR() {
        OCR.getInstance(this).initAccessToken(new OnResultListener<AccessToken>() {
            @Override
            public void onResult(AccessToken accessToken) {
                String token = accessToken.getAccessToken();
                Log.d(TAG, "OCR init result: " + token);
            }

            @Override
            public void onError(OCRError ocrError) {
                Log.e(TAG, "OCR init error: ", ocrError);
            }
        }, getApplicationContext());
    }

    private void initTTS() {
        manager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        clipChangedListener = new ClipboardManager.OnPrimaryClipChangedListener() {
            @Override
            public void onPrimaryClipChanged() {
                if (manager.hasPrimaryClip() && manager.getPrimaryClip().getItemCount() > 0) {

                    CharSequence addedText = manager.getPrimaryClip().getItemAt(0).getText();

                    if (addedText != null) {
                        Log.d(TAG, "copied text: " + addedText);
                        read(addedText);
                    }
                }
            }
        };
        registerClipEvents();

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                Log.d(TAG, "onInit: " + Arrays.toString(tts.getEngines().toArray()));
                Log.d(TAG, "onInit: " + tts.getDefaultEngine());

                if (i == TextToSpeech.SUCCESS) {
                    int result1 = tts.setLanguage(Locale.US);
                    int result2 = tts.setLanguage(Locale.CHINESE);
                    if (result1 == TextToSpeech.LANG_MISSING_DATA || result1 == TextToSpeech.LANG_NOT_SUPPORTED
                            || result2 == TextToSpeech.LANG_MISSING_DATA || result2 == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Toast.makeText(MyApplication.this, "不支持！", Toast.LENGTH_SHORT).show();
                    }
                    read("进入其它软件，复制文字即可朗读！");
//                    read(getString(R.string.instructions));
                }
            }
        });
        utteranceProgressListener = new UtteranceProgressListener() {
            @Override
            public void onStart(String s) {
                Log.d(TAG, "onStart: " + s);
            }

            @Override
            public void onDone(String s) {
                Log.d(TAG, "onDone: " + s);
            }

            @Override
            public void onError(String utteranceId, int errorCode) {
                Log.d(TAG, "onError: " + errorCode + " : " + utteranceId);
            }

            @Override
            public void onError(String utteranceId) {
                Log.d(TAG, "onError: " + utteranceId);
            }
        };
        tts.setOnUtteranceProgressListener(utteranceProgressListener);
    }

    @Override
    public void onTerminate() {
        unregisterClipEvents();
        tts.stop();
        tts.shutdown();
        tts = null;
        super.onTerminate();
    }

    public void registerClipEvents() {
        manager.addPrimaryClipChangedListener(clipChangedListener);
        Log.d(TAG, "registerClipEvents: ");
    }

    public void unregisterClipEvents() {
        manager.removePrimaryClipChangedListener(clipChangedListener);
        Log.d(TAG, "unregisterClipEvents: ");
    }

    public void read(final CharSequence sequence) {
        Log.d(TAG, "read: " + sequence);
        int r = tts.speak(sequence, TextToSpeech.QUEUE_FLUSH, null, sequence.toString());
        Log.d(TAG, "read: " + r);
        if (r == TextToSpeech.ERROR) {
            Log.d(TAG, "read: 失败：" + r);
            tts.stop();
            tts.shutdown();
            tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int i) {
                    Log.d(TAG, "onInit: " + Arrays.toString(tts.getEngines().toArray()));
                    Log.d(TAG, "onInit: " + tts.getDefaultEngine());
                    if (i == TextToSpeech.SUCCESS) {
                        int result1 = tts.setLanguage(Locale.US);
                        int result2 = tts.setLanguage(Locale.CHINESE);
                        if (result1 == TextToSpeech.LANG_MISSING_DATA || result1 == TextToSpeech.LANG_NOT_SUPPORTED
                                || result2 == TextToSpeech.LANG_MISSING_DATA || result2 == TextToSpeech.LANG_NOT_SUPPORTED) {
                            Toast.makeText(MyApplication.this, "不支持！", Toast.LENGTH_SHORT).show();
                        }
                        read(sequence);
                    } else {
                        Log.d(TAG, "onInit: 初始化失败！");
                    }
                }
            });
            tts.setOnUtteranceProgressListener(utteranceProgressListener);
        }
    }

    public void cancelRead() {
        tts.stop();
    }

}
