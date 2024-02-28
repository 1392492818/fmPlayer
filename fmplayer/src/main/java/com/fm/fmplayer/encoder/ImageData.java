package com.fm.fmplayer.encoder;

import android.media.Image;

public class ImageData {
    public ImageData(byte[] yData, byte[] uData, byte[] vData, int width, int height, int uvWidth, int uvHeight, int ySize, int yRowStride, int yPlanRowStride, int yPlanPixelStride, int uRowStride, int uPlanRowStride, int uPlanPixelStride, int vRowStride, int vPlanRowStride, int vPlanPixelStride, long pts) {
        this.yData = yData;
        this.uData = uData;
        this.vData = vData;
        this.width = width;
        this.height = height;
        this.uvWidth = uvWidth;
        this.uvHeight = uvHeight;
        this.ySize = ySize;
        this.yRowStride = yRowStride;
        this.yPlanRowStride = yPlanRowStride;
        this.yPlanPixelStride = yPlanPixelStride;
        this.uRowStride = uRowStride;
        this.uPlanRowStride = uPlanRowStride;
        this.uPlanPixelStride = uPlanPixelStride;
        this.vRowStride = vRowStride;
        this.vPlanRowStride = vPlanRowStride;
        this.vPlanPixelStride = vPlanPixelStride;
        this.pts = pts;
    }

    public long getPts() {
        return pts;
    }

    private long pts;

    private byte[] yData;
    private byte[] uData;
    private byte[] vData;

    private int width;

    private int height;

    private int uvWidth;

    private int uvHeight;

    private int ySize;

    private int yRowStride;

    private int yPlanRowStride;

    private int yPlanPixelStride;

    private int uRowStride;

    private int uPlanRowStride;

    private int uPlanPixelStride;


    private int vRowStride;

    private int vPlanRowStride;

    private int vPlanPixelStride;

    public byte[] getData() {
        byte[] data = new byte[ySize * 3 / 2];
        int index = 0;

        for (int y = 0; y < height; y++) {
            int yPlanRow = y * yPlanRowStride;
            for (int x = 0; x < yRowStride; x++) {
                int readIndex =  yPlanRow+ x * yPlanPixelStride;
                data[index++] = yData[readIndex];
            }
        }

        int uvIndex = 0;
        int uvSize = uvWidth * uvHeight;
        int uvRow = uRowStride / uPlanPixelStride;

        for (int y = 0; y < uvHeight; y++) {
            int yPlanRow = y * uPlanRowStride;
            for (int x = 0; x < uvRow; x++) {
                int readIndex = yPlanRow + x * uPlanPixelStride;
                data[index++] = uData[readIndex];
                data[uvSize + ySize + uvIndex++] = vData[readIndex];
            }
        }


//        for (int y = 0; y < uvHeight; y++) {
//            for (int x = 0; x < vRowStride / vPlanPixelStride; x++) {
//                int readIndex = y * vPlanRowStride + x * vPlanPixelStride;
//                data[index++] = vData[readIndex];
//            }
//        }


        return data;
    }

    public static class Builder{
        public  Builder setyData(byte[] yData) {
            this.yData = yData;
            return this;
        }

        public Builder setuData(byte[] uData) {
            this.uData = uData;
            return this;
        }

        public Builder setvData(byte[] vData) {
            this.vData = vData;
            return this;
        }

        public Builder setWidth(int width) {
            this.width = width;
            return this;
        }

        public Builder setHeight(int height) {
            this.height = height;
            return this;
        }

        public Builder setUvWidth(int uvWidth) {
            this.uvWidth = uvWidth;
            return this;
        }

        public Builder setUvHeight(int uvHeight) {
            this.uvHeight = uvHeight;
            return this;
        }

        public Builder setySize(int ySize) {
            this.ySize = ySize;
            return this;
        }

        public Builder setyRowStride(int yRowStride) {
            this.yRowStride = yRowStride;
            return this;
        }

        public Builder setyPlanRowStride(int yPlanRowStride) {
            this.yPlanRowStride = yPlanRowStride;
            return this;
        }

        public Builder setyPlanPixelStride(int yPlanPixelStride) {
            this.yPlanPixelStride = yPlanPixelStride;
            return this;
        }

        public Builder setuRowStride(int uRowStride) {
            this.uRowStride = uRowStride;
            return this;
        }

        public Builder setuPlanRowStride(int uPlanRowStride) {
            this.uPlanRowStride = uPlanRowStride;
            return this;
        }

        public Builder setuPlanPixelStride(int uPlanPixelStride) {
            this.uPlanPixelStride = uPlanPixelStride;
            return this;
        }

        public Builder setvRowStride(int vRowStride) {
            this.vRowStride = vRowStride;
            return this;
        }

        public Builder setvPlanRowStride(int vPlanRowStride) {
            this.vPlanRowStride = vPlanRowStride;
            return this;
        }

        public Builder setvPlanPixelStride(int vPlanPixelStride) {
            this.vPlanPixelStride = vPlanPixelStride;
            return this;
        }

        private byte[] yData;
        private byte[] uData;
        private byte[] vData;

        private int width;

        private int height;

        private int uvWidth;

        private int uvHeight;

        private int ySize;

        private int yRowStride;

        private int yPlanRowStride;

        private int yPlanPixelStride;

        private int uRowStride;

        private int uPlanRowStride;

        private int uPlanPixelStride;


        private int vRowStride;

        private int vPlanRowStride;

        private int vPlanPixelStride;

        public Builder setPts(long pts) {
            this.pts = pts;
            return this;
        }

        private long pts;

        public ImageData build(){
            return new ImageData( this.yData,
            this.uData,
            this.vData ,
            this.width ,
            this.height ,
            this.uvWidth ,
            this.uvHeight ,
            this.ySize ,
            this.yRowStride ,
            this.yPlanRowStride ,
            this.yPlanPixelStride,
            this.uRowStride ,
            this.uPlanRowStride ,
            this.uPlanPixelStride,
            this.vRowStride ,
            this.vPlanRowStride ,
            this.vPlanPixelStride,
                    this.pts);
        }

    }
}
