package com.fm.fmplayer.view;

import static android.opengl.EGL14.EGL_OPENGL_ES2_BIT;
import static android.opengl.EGL15.EGL_OPENGL_ES3_BIT;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

public class OpenglUtil {
    private static final int EGL_CONTEXT_CLIENT_VERSION = 0x3098;
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static boolean isGLESVersionSupported(int version) {
        EGL10 egl = (EGL10) EGLContext.getEGL();
        EGLDisplay display = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);

        int[] configAttributes = {
                EGL10.EGL_RENDERABLE_TYPE,
                version == 3 ? EGL_OPENGL_ES3_BIT : EGL_OPENGL_ES2_BIT,
                EGL10.EGL_NONE
        };

        int[] numConfig = new int[1];
        EGLConfig[] configs = new EGLConfig[1];

        if (!egl.eglChooseConfig(display, configAttributes, configs, 1, numConfig)) {
            Log.e("GLESUtils", "eglChooseConfig failed");
            return false;
        }

        if (numConfig[0] == 0) {
            Log.e("GLESUtils", "No suitable EGLConfig found");
            return false;
        }

        int[] contextAttributes = {
                EGL_CONTEXT_CLIENT_VERSION,
                version,
                EGL10.EGL_NONE
        };

        EGLContext context = egl.eglCreateContext(display, configs[0], EGL10.EGL_NO_CONTEXT, contextAttributes);

        if (context == EGL10.EGL_NO_CONTEXT) {
            Log.e("GLESUtils", "eglCreateContext failed");
            return false;
        }

        // Release the context
        egl.eglDestroyContext(display, context);

        return true;
    }


    public static String readShaderFromRaw(Context context, int resId) {
        BufferedReader br = null;
        StringBuffer stringBuffer = new StringBuffer();
        try {
            br = new BufferedReader(new InputStreamReader(context.getResources().openRawResource(resId)));
            String line = null;
            while ((line = br.readLine()) != null) {
                stringBuffer.append(line + "\n");
            }
        } catch (IOException e) {
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                }
            }
        }
        return stringBuffer.toString();
    }
}
