package com.mvp.myeffecttools.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by 爱的LUICKY on 2016/9/8.
 */
public class StreamUtils {

    public static String getStringFromStream(InputStream is) {
        if (is != null) {
            byte[] bytes = new byte[1024];
            int b = -1;
            try {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                while ((b = is.read(bytes)) != -1) {
                    bos.write(bytes, 0, b);
                }
                is.close();
                return new String(bos.toByteArray());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
