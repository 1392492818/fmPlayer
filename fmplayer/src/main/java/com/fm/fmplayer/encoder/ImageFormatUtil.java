package com.fm.fmplayer.encoder;

import android.media.Image;
import android.util.Log;

public class ImageFormatUtil {
    private final static String TAG = ImageFormatUtil.class.getSimpleName();

    /**
     * 提取 yuv420p格式
     *
     * @param image
     * @return
     */
    public static byte[] getYuv420p(Image image) {
        Image.Plane[] planes = image.getPlanes();
        int ySize = image.getWidth() * image.getHeight();
        int uvWidth = image.getWidth() / 2;
        int uvHeight = image.getHeight() / 2;
        byte[] data = new byte[ySize * 3 / 2];
        int index = 0;
        Image.Plane yPlane = planes[0];
        int yRemaining = yPlane.getBuffer().remaining();
        byte[] yData = new byte[yRemaining];
        yPlane.getBuffer().get(yData);
        int yRowStride = Math.min(image.getWidth(), yPlane.getRowStride());

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < yRowStride; x++) {
                int readIndex = y * yPlane.getRowStride() + x * yPlane.getPixelStride();
                data[index++] = yData[readIndex];
            }
        }

        Image.Plane uPlane = planes[1];
        int uRemaining = uPlane.getBuffer().remaining();
        byte[] uData = new byte[uRemaining];
        uPlane.getBuffer().get(uData);
        int uRowStride = Math.min(image.getWidth(), uPlane.getRowStride());
        for (int y = 0; y < uvHeight; y++) {
            for (int x = 0; x < uRowStride / uPlane.getPixelStride(); x++) {
                int readIndex = y * uPlane.getRowStride() + x * uPlane.getPixelStride();
                data[index++] = uData[readIndex];
            }
        }


        Image.Plane vPlane = planes[2];
        int vRemaining = vPlane.getBuffer().remaining();
        byte[] vData = new byte[vRemaining];
        vPlane.getBuffer().get(vData);
        int vRowStride = Math.min(image.getWidth(), vPlane.getRowStride());

        for (int y = 0; y < uvHeight; y++) {
            for (int x = 0; x < vRowStride / vPlane.getPixelStride(); x++) {
                int readIndex = y * vPlane.getRowStride() + x * vPlane.getPixelStride();
                data[index++] = vData[readIndex];
            }
        }
        return data;
    }


    /**
     * yuv 转换 nv21
     *
     * @param yuv420
     * @param width
     * @param height
     * @return
     */
    public static byte[] yuv420toNv21(byte[] yuv420, int width, int height) {
        byte[] nv21 = new byte[width * height * 3 / 2];
        int ySize = width * height;
        int uvWidth = width / 2;
        int uvHeight = height / 2;
        int uvSize = uvWidth * uvHeight;
        int index = 0;
        for (int i = 0; i < ySize; i++) {
            nv21[index] = yuv420[index];
            index++;
        }
        for (int i = 0; i < uvSize; i++) {
            nv21[index++] = yuv420[ySize + i];
            nv21[index++] = yuv420[ySize + uvSize + i];
        }


        return nv21;
    }
}
