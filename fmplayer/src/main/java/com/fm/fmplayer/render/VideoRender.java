package com.fm.fmplayer.render;

import static java.lang.Math.pow;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES30;
import android.opengl.GLES32;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;


import com.fm.fmplayer.R;
import com.fm.fmplayer.view.OpenglUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


public class VideoRender implements GLSurfaceView.Renderer {

    private  String VERTEX_SHADER_CODE ;
    private  String FRAGMENT_SHADER_CODE;



    private double width;
    private double height;
    private byte[] yData;
    private byte[] uData;
    private byte[] vData;
    private int imageWidth;
    private int imageHeight;
    private ShortBuffer drawListBuffer;

    private float[] imageTextureTransform = {
            1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f
    };
    private int orientation = 0;


    private int select;
    private int newHeight;
    private int newWidth;


    private void adjustViewport(int imageWidth, int imageHeight) {
        float surfaceAspect = (float) (height / (float) width);
        float videoAspect = imageHeight / (float) imageWidth;
        int xOffset = 0;
        int yOffset = 0;
        float imageRatio = (float) imageWidth / (float) imageHeight;
        if (surfaceAspect > videoAspect) {
            //屏的高宽比 比 图像的高宽比大，代表着屏的高比较长，这时要拉宽和屏一样大
            newWidth = (int) width;
            float widthRatio = (float) (width / (float) imageWidth);
            newHeight = (int) (widthRatio * imageHeight);
        } else {
            //代表着屏的宽比 图像的大，这时要拉高和屏一样大
            newHeight = (int) height;
            float heightRatio = (float) (height / (float) imageHeight);
            newWidth = (int) ((float) heightRatio * imageWidth);
        }
        if (newHeight > height) {
            newWidth = (int) ((float) newWidth / ((float) newHeight / (float) height));
            newHeight = (int) height;
        }
        if (newWidth > width) {
            Log.e("fuweicong", String.valueOf(newWidth));
            Log.e("fuweicong", String.valueOf(newHeight));

            newHeight = (int) ((float) newHeight / ((float) newWidth / (float) width));
            newWidth = (int) width;
        }
        xOffset = (int) ((newWidth - width) / 2);
        yOffset = (int) ((newHeight - height) / 2);
        GLES30.glViewport(-xOffset, -yOffset, newWidth, (int) newHeight);
    }

    private float[] vertex = {
            -1.0f, 1.0f,    //左上角
            -1.0f, -1.0f,   //左下角
            1.0f, 1.0f,     //右上角
            1.0f, -1.0f     //右下角
    };

    private final float[] sCoord = {
            0f, 0f, //左上角
            0f, 1f, //左下角
            1f, 0f, //右上角
            1f, 1f //右下角
    };

    private FloatBuffer mVertexBuffer;

    private FloatBuffer mFragmentBuffer;

    private int mProgram;

    private Bitmap bitmap = null;

    private int mVPosition;

    private int mVCoordinate;

    private int yTexture;
    private int uTexture;
    private int vTexture;

    private int maskTexture;

    private Context mContext;


    private int mTextureid;



    private static final short[] DRAW_ORDER = {0, 1, 2, 0, 2, 3};


    private static final float SQUARE_SIZE = 1.0f;
    private static final float[] SQUARE_COORDINATES = {
            -SQUARE_SIZE, SQUARE_SIZE, 0.0f,   // top left
            -SQUARE_SIZE, -SQUARE_SIZE, 0.0f,   // bottom left
            SQUARE_SIZE, -SQUARE_SIZE, 0.0f,  // bottom right
            SQUARE_SIZE, SQUARE_SIZE, 0.0f}; // top right

    private static final float[] TEXTURE_COORDINATES = {
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 1.0f, 0.0f, 1.0f
    };


    int[] textures = new int[3];

    private void init() {

    }

    public VideoRender(Context context,int width, int height) {
        this.width = width;
        this.height = height;
        this.imageWidth = width;
        this.imageHeight = height;
        this.VERTEX_SHADER_CODE = OpenglUtil.readShaderFromRaw(context, R.raw.imagevertexshader);
        this.FRAGMENT_SHADER_CODE = OpenglUtil.readShaderFromRaw(context,R.raw.fmvideoshader);
        init();
        mContext = context;

        ByteBuffer bb = ByteBuffer.allocateDirect(SQUARE_COORDINATES.length * 4);
        bb.order(ByteOrder.nativeOrder());

        mVertexBuffer = bb.asFloatBuffer();
        mVertexBuffer.put(SQUARE_COORDINATES);
        mVertexBuffer.position(0);


//        mFragmentBuffer = ByteBuffer.allocateDirect(sCoord.length * 4)
//                .order(ByteOrder.nativeOrder())
//                .asFloatBuffer().put(sCoord);
//        mFragmentBuffer.position(0);

        ByteBuffer texturebb = ByteBuffer.allocateDirect(TEXTURE_COORDINATES.length * 4);
        texturebb.order(ByteOrder.nativeOrder());

        mFragmentBuffer = texturebb.asFloatBuffer();
        mFragmentBuffer.put(TEXTURE_COORDINATES);
        mFragmentBuffer.position(0);

        ByteBuffer dlb = ByteBuffer.allocateDirect(DRAW_ORDER.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(DRAW_ORDER);
        drawListBuffer.position(0);

    }

//    public VideoRender(Context context, int width, int height) {
//        this.width = width;
//        this.height = height;
//        mContext = context;
//        mVertexBuffer = ByteBuffer.allocateDirect(vertex.length * 4)
//                .order(ByteOrder.nativeOrder())
//                .asFloatBuffer().put(vertex);
//        mVertexBuffer.position(0);
//
//
//        mFragmentBuffer = ByteBuffer.allocateDirect(sCoord.length * 4)
//                .order(ByteOrder.nativeOrder())
//                .asFloatBuffer().put(sCoord);
//        mFragmentBuffer.position(0);
//    }


    public void genShaders() {
        int vertexShaderHandle = GLES32.glCreateShader(GLES32.GL_VERTEX_SHADER);
        GLES32.glShaderSource(vertexShaderHandle, VERTEX_SHADER_CODE);
        GLES32.glCompileShader(vertexShaderHandle);

        int fragmentShaderHandle = GLES32.glCreateShader(GLES32.GL_FRAGMENT_SHADER);
        GLES32.glShaderSource(fragmentShaderHandle, FRAGMENT_SHADER_CODE);
        GLES32.glCompileShader(fragmentShaderHandle);

        mProgram = GLES32.glCreateProgram();
        GLES32.glAttachShader(mProgram, vertexShaderHandle);
        GLES32.glAttachShader(mProgram, fragmentShaderHandle);
        GLES32.glLinkProgram(mProgram);

        int[] status = new int[1];
        GLES32.glGetProgramiv(mProgram, GLES32.GL_LINK_STATUS, status, 0);
        if (status[0] != GLES32.GL_TRUE) {
            String error = GLES32.glGetProgramInfoLog(mProgram);
            Log.e("SurfaceTest", "Error while linking program:" + error);
        }
    }


    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        genShaders();

        mVPosition = GLES30.glGetAttribLocation(mProgram, "vPosition");
        mVCoordinate = GLES30.glGetAttribLocation(mProgram, "vCoordinate");
        yTexture = GLES30.glGetUniformLocation(mProgram, "yTexture");
        uTexture = GLES30.glGetUniformLocation(mProgram, "uTexture");
        vTexture = GLES30.glGetUniformLocation(mProgram, "vTexture");
//        maskTexture = GLES30.glGetUniformLocation(mProgram, "maskTexture");

        //创建和绑定纹理
        GLES30.glGenTextures(3, textures, 0);
        //激活第0个纹理
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textures[0]);

        //设置环绕和过滤方式
        //环绕（超出纹理坐标范围）：（s==x t==y GL_REPEAT 重复）
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);


        // Mask
        GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textures[1]);

        //激活第0个纹理
//        //缩小的过滤器
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);


        // Mask
        GLES30.glActiveTexture(GLES30.GL_TEXTURE2);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textures[2]);

        //激活第0个纹理
//        //缩小的过滤器
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);

    }


    public void orientation() {
        this.imageTextureTransform = new float[]{
                1.0f, 0.0f, 0.0f, 0.0f,
                0.0f, -1.0f, 0.0f, 0.0f,
                0.0f, 0.0f, 1.0f, 0.0f,
                0.0f, 1.0f, 0.0f, 1.0f,
        };
//        Log.e("orientation", String.valueOf(this.orientation));
        if (this.orientation == 90) {
            Matrix.rotateM(imageTextureTransform, 0, 90, 0, 0, 1);
            Matrix.translateM(imageTextureTransform, 0, 0, -1, 0);
        }

        if (this.orientation == 180) {
            Matrix.rotateM(imageTextureTransform, 0, 180, 0, 0, 1);
            Matrix.translateM(imageTextureTransform, 0, -1, -1, 0);
        }

        if (this.orientation == 270) {
            Matrix.rotateM(imageTextureTransform, 0, 270, 0, 0, 1);
            Matrix.translateM(imageTextureTransform, 0, -1, 0, 0);
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES30.glViewport(0, 0, width, height);
    }

    public void draw(int width, int height,byte[] yData, byte[] uData, byte[] vData) {
        this.yData = yData;
        this.uData = uData;
        this.vData = vData;
        this.imageHeight = height;
        this.imageWidth = width;
    }


    @Override
    public void onDrawFrame(GL10 gl) {

        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT);
        GLES30.glClearColor(0.0f, 0, 0, 1f);
        if(this.yData == null) return;
        //使用源程序
        GLES30.glUseProgram(mProgram);

        this.newWidth = (int) width;
        this.newHeight = (int) height;

        adjustViewport(this.imageWidth, this.imageHeight);



        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textures[0]);
        if(this.yData != null) {
            ByteBuffer byteBuffer =  ByteBuffer.wrap(this.yData);
            GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_LUMINANCE, imageWidth, imageHeight, 0, GLES30.GL_LUMINANCE, GLES30.GL_UNSIGNED_BYTE, byteBuffer);
        }

        GLES30.glUniform1i(yTexture, 0);






        GLES30.glActiveTexture(GLES30.GL_TEXTURE1);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textures[1]);
        if(this.uData != null) {
            ByteBuffer byteBuffer = ByteBuffer.wrap(this.uData);

            GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_LUMINANCE, (int)imageWidth / 2, (int)imageHeight / 2, 0, GLES30.GL_LUMINANCE, GLES30.GL_UNSIGNED_BYTE, byteBuffer);
        }
        GLES30.glUniform1i(uTexture, 1);


        GLES30.glActiveTexture(GLES30.GL_TEXTURE2);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textures[2]);
        if(this.vData != null) {
            ByteBuffer byteBuffer = ByteBuffer.wrap(this.vData);
            GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_LUMINANCE, (int)imageWidth / 2, (int)imageHeight / 2, 0, GLES30.GL_LUMINANCE, GLES30.GL_UNSIGNED_BYTE, byteBuffer);
        }
        GLES30.glUniform1i(vTexture, 2);


        //使顶点属性数组有效
        GLES30.glEnableVertexAttribArray(mVPosition);
        //为顶点属性赋值
//        GLES30.glVertexAttribPointer(mVPosition, 2, GLES30.GL_FLOAT, false, 8, mVertexBuffer);
        GLES32.glVertexAttribPointer(mVPosition, 3, GLES32.GL_FLOAT, false, 4 * 3, mVertexBuffer);

        GLES30.glEnableVertexAttribArray(mVCoordinate);
//        GLES30.glVertexAttribPointer(mVCoordinate, 2, GLES30.GL_FLOAT, false, 8, mFragmentBuffer);
        GLES32.glVertexAttribPointer(mVCoordinate, 4, GLES32.GL_FLOAT, false, 4 * 4, mFragmentBuffer);


//        GLES30.glUniform1f(GLES30.glGetUniformLocation(mProgram, "width"), (float)width);
//        GLES30.glUniform1f(GLES30.glGetUniformLocation(mProgram, "height"), (float)height);

//        GLES30.glUniform1f(GLES30.glGetUniformLocation(mProgram, "originWidth"), (float) this.newWidth);
//        GLES30.glUniform1f(GLES30.glGetUniformLocation(mProgram, "originHeight"), (float) this.newHeight);
//        GLES30.glUniform1f(GLES30.glGetUniformLocation(mProgram, "width"), (float) this.newWidth);
//        GLES30.glUniform1f(GLES30.glGetUniformLocation(mProgram, "height"), (float) this.newHeight);
//        GLES30.glUniform1f(GLES30.glGetUniformLocation(mProgram, "nviews"), (float) nviews);
//        GLES30.glUniform1f(GLES30.glGetUniformLocation(mProgram, "k_off"), (float) koff);
//
//        GLES30.glUniform1f(GLES30.glGetUniformLocation(mProgram, "is3D"), (float) is3D);
////        Log.e("leftRight", String.valueOf((float) leftRight));
//        GLES30.glUniform1f(GLES30.glGetUniformLocation(mProgram, "leftRight"), (float) leftRight);
//
//        GLES30.glUniform1f(GLES30.glGetUniformLocation(mProgram, "index"), (float) index);
//        GLES30.glUniform1f(GLES30.glGetUniformLocation(mProgram, "status"), (float) isBgr);
//        GLES30.glUniform1f(GLES30.glGetUniformLocation(mProgram, "orientation"), (float) orientation);

//        Matrix.setIdentityM(imageTextureTransform, 0);
        this.orientation();


        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4);

        //矩阵变换
        GLES32.glUniformMatrix4fv(GLES32.glGetUniformLocation(mProgram, "textureTransform"), 1, false, imageTextureTransform, 0);
        //矩阵变换必须调用此方法
        GLES32.glDrawElements(GLES32.GL_TRIANGLES, DRAW_ORDER.length, GLES32.GL_UNSIGNED_SHORT, drawListBuffer);

        //解绑纹理
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }
}
