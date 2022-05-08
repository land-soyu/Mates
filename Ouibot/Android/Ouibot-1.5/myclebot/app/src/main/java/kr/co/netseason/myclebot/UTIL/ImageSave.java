package kr.co.netseason.myclebot.UTIL;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import kr.co.netseason.myclebot.R;
import kr.co.netseason.myclebot.Security.MediaScanner;
import kr.co.netseason.myclebot.openwebrtc.Config;

/**
 * Created by soyu on 15. 12. 1.
 */
public class ImageSave {
    public String getPictureFileName() {
        Date now = new Date(System.currentTimeMillis());
        SimpleDateFormat df = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        String formattedDate = df.format(now);
        String fileExtention = Config.IMAGE_FILE_EXTENTION;
        return formattedDate + fileExtention;
    }
    public void SaveBitmapToFileCache(Context context, Bitmap bitmap, String strFilePath, String strFileName) {
        File filepath = new File(strFilePath);

        if (!filepath.exists()) {
            filepath.mkdirs();
        }
        File fileCacheItem = new File(strFilePath + strFileName);
        OutputStream out = null;
        try {
            fileCacheItem.createNewFile();
            out = new FileOutputStream(fileCacheItem);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Toast.makeText(context, context.getResources().getText(R.string.image_save), Toast.LENGTH_SHORT).show();

        new MediaScanner(context, new File(strFilePath + strFileName));

    }
}
