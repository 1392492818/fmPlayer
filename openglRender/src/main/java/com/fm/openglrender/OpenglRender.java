package com.fm.openglrender;

import android.opengl.GLSurfaceView;
import android.view.Surface;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class OpenglRender  implements GLSurfaceView.Renderer {
    static {
        System.loadLibrary("opengl-render");
    }
    public static final int SAMPLE_TYPE  =  200;

    public static final int SAMPLE_TYPE_TRIANGLE                = SAMPLE_TYPE;
    public static final int SAMPLE_TYPE_3D_MODEL                = SAMPLE_TYPE + 1;
    public static final int SAMPLE_TYPE_3D_MODEL_ANIM           = SAMPLE_TYPE + 2;
    public static final int SAMPLE_TYPE_TEXT                    = SAMPLE_TYPE + 3;
    public static final int SAMPLE_TYPE_TEXT_ENGLISH            = SAMPLE_TYPE + 4;

    public static final int SAMPLE_TYPE_SET_TOUCH_LOC           = SAMPLE_TYPE + 999;
    public static final int SAMPLE_TYPE_SET_GRAVITY_XY          = SAMPLE_TYPE + 1000;

    public native void native_Init();

    public native void native_UnInit();

    public native void native_SetParamsInt(int paramType, int value0, int value1);

    public native void native_SetParamsFloat(int paramType, float value0, float value1);

    public native void native_UpdateTransformMatrix(float rotateX, float rotateY, float scaleX, float scaleY);

    public native void native_SetImageData(int format, int width, int height, byte[] bytes);

    public native void native_SetImageDataWithIndex(int index, int format, int width, int height, byte[] bytes);

    public native void native_SetAudioData(short[] audioData);

    public native void native_OnSurfaceCreated();

    public native void native_OnSurfaceChanged(int width, int height);

    public native void native_OnDrawFrame();

    public void init() {
        native_Init();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        native_OnSurfaceCreated();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        native_OnSurfaceChanged(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        native_OnDrawFrame();
    }

    public void updateTransformMatrix(int mXAngle, int mYAngle, float mCurScale, float mCurScale1) {
        native_UpdateTransformMatrix(mXAngle,mYAngle, mCurScale, mCurScale1);
    }

    public void setTouchLoc(float touchX, float touchY) {
        native_SetParamsFloat(SAMPLE_TYPE_SET_TOUCH_LOC, touchX, touchY);
    }

    public void unInit() {
        native_UnInit();
    }
}
