package com.fm.fmplayer.view;

import android.content.Context;
import android.content.res.Resources;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.fm.fmplayer.render.VideoRender;
import com.fm.fmplayer.render.VideoRender2;


//import com.video.a3dlibrary.gl.image.MyRenderer;

public class FmGLSurfaceView extends GLSurfaceView {

    private VideoRender videoRender;

    private VideoRender2 videoRender2;

    private Context context;


    private int version = 3;

    public FmGLSurfaceView(Context context) {
        this(context,null);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public FmGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();

    }

    public void init() {
        //使用OpenGL ES 2.0
        if(OpenglUtil.isGLESVersionSupported(version)) {
            setEGLContextClientVersion(3);
        } else {
            this.version = 2;
            setEGLContextClientVersion(2);
        }


        //设置Renderer
//        this.myRenderer = new MyRenderer(context, 2560,1600);

        int screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;//屏幕高度
        int screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;//屏幕宽度
        Log.e("fuweicong", "screenWidth : " + screenWidth +"  ,screenHeight :" +screenHeight);
        if(this.version == 3) {
            this.videoRender = new VideoRender(context,  screenWidth,screenHeight);
            setRenderer(this.videoRender);
            //设置刷新模式
            setRenderMode(RENDERMODE_WHEN_DIRTY);
        } else {
            this.videoRender2 = new VideoRender2(context,  screenWidth,screenHeight);

            setRenderer(this.videoRender2);
            //设置刷新模式
            setRenderMode(RENDERMODE_WHEN_DIRTY);
        }
    }

    public void draw(int width, int height,byte[] yData, byte[] uData, byte[] vData){
//        this.videoRender.setIs3D(is3d);
//        this.videoRender.setLeftRight(leftRight);
        if(this.version == 3)
            this.videoRender.draw(width, height ,yData, uData, vData);
        else if (this.version == 2)
            this.videoRender2.draw(width, height ,yData, uData, vData);
//        this.videoRender.setOrientation(orientation);
        this.requestRender();
    }
}
