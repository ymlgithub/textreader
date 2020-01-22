package red.yml.textreader;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.TextUtils;

public class ClipboardUtils {
    private static ClipboardManager clipboardManager = null;

    public static void init(Context context) {
        clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
    }

    public static void setClipboard(String data) {
        ClipData clipData = ClipData.newPlainText("textreader", data);
        clipboardManager.setPrimaryClip(clipData);
    }

    public static String getClipboard() {
        ClipData clipData = clipboardManager.getPrimaryClip();
        if (clipData != null) {
            ClipData.Item item = clipData.getItemAt(0);
            if (item != null) {
                String res = item.getText().toString();
                if (!TextUtils.isEmpty(res)) {
                    return res;
                }
            }
        }
        return "";
    }
}
