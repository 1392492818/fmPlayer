package com.fm.fmplayer.render;

import static android.opengl.GLES20.glUniformMatrix4fv;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.fm.fmplayer.R;
import com.fm.fmplayer.view.OpenglUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.function.Consumer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class VideoRenderOES implements GLSurfaceView.Renderer{

    private   String VERTEX_SHADER_CODE ;
    private  String FRAGMENT_SHADER_CODE;
    private final Consumer<SurfaceTexture> initCompleteRunnable;


    private int imageWidth;
    private int imageHeight;
    private double isBgr;
    private ShortBuffer drawListBuffer;

    private float[] imageTextureTransform = {
            1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, -1.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f
    };
    private int orientation = 0;
    private int width;
    private int height;
    private int newHeight;
    private int newWidth;

    private SurfaceTexture surfaceTexture;


    public void setStop(boolean stop) {
        isStop = stop;
    }

    private boolean isStop = false;
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

    private final static String TAG = VideoRenderOES.class.getSimpleName();

    private FloatBuffer mFragmentBuffer;

    private int mProgram;
    private float[] mMvpMatrix = new float[16];


    private int mVPosition;

    private int mVCoordinate;

    private int yTexture;



    private Context mContext;
    private static final float SQUARE_SIZE = 1.0f;
    private static final float[] SQUARE_COORDINATES = {-SQUARE_SIZE, SQUARE_SIZE, 0.0f,   // top left
            -SQUARE_SIZE, -SQUARE_SIZE, 0.0f,   // bottom left
            SQUARE_SIZE, -SQUARE_SIZE, 0.0f,   // bottom right
            SQUARE_SIZE, SQUARE_SIZE, 0.0f}; // top right

    private static final short[] DRAW_ORDER = {0, 1, 2, 0, 2, 3};



    private static final float[] TEXTURE_COORDINATES = {0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 1.0f, 0.0f, 1.0f};

    private float[] videoTextureTransform;
    private FloatBuffer vertexBuffer;
    private FloatBuffer textureBuffer;

    int[] textures = new int[1];



    public VideoRenderOES(Context context, int width, int height,Consumer<SurfaceTexture> initCompleteRunnable) {
//        this.width = width;
//        this.height = height;
        this.imageWidth = width;
        this.imageHeight = height;
        VERTEX_SHADER_CODE = OpenglUtil.readShaderFromRaw(context, R.raw.vertexshader20);
        FRAGMENT_SHADER_CODE = OpenglUtil.readShaderFromRaw(context, R.raw.fragmentshader20);
        mContext = context;
        videoTextureTransform = new float[16];


        this.initCompleteRunnable = initCompleteRunnable;

    }


    private void setupVertexBuffer() {
        ByteBuffer texturebb = ByteBuffer.allocateDirect(TEXTURE_COORDINATES.length * 4);
        texturebb.order(ByteOrder.nativeOrder());

        textureBuffer = texturebb.asFloatBuffer();
        textureBuffer.put(TEXTURE_COORDINATES);
        textureBuffer.position(0);


        // Draw list buffer
        ByteBuffer dlb = ByteBuffer.allocateDirect(DRAW_ORDER.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(DRAW_ORDER);
        drawListBuffer.position(0);

        // Initialize the texture holder
        ByteBuffer bb = ByteBuffer.allocateDirect(SQUARE_COORDINATES.length * 4);
        bb.order(ByteOrder.nativeOrder());

        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(SQUARE_COORDINATES);
        vertexBuffer.position(0);
    }



    public void genShaders() {
        int vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        GLES20.glShaderSource(vertexShaderHandle, VERTEX_SHADER_CODE);
        GLES20.glCompileShader(vertexShaderHandle);

        int fragmentShaderHandle = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        GLES20.glShaderSource(fragmentShaderHandle, FRAGMENT_SHADER_CODE);
        GLES20.glCompileShader(fragmentShaderHandle);

        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vertexShaderHandle);
        GLES20.glAttachShader(mProgram, fragmentShaderHandle);
        GLES20.glLinkProgram(mProgram);

        int[] status = new int[1];
        GLES20.glGetProgramiv(mProgram, GLES20.GL_LINK_STATUS, status, 0);
        if (status[0] != GLES20.GL_TRUE) {
            Log.e("status", String.valueOf(status[0]));
            String error = GLES20.glGetProgramInfoLog(mProgram);
            Log.e("SurfaceTest2", "Error while linking program:" + error);
        }
    }


    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        setupVertexBuffer();
        genShaders();


        //创建和绑定纹理
        GLES20.glGenTextures(1, textures, 0);
        //激活第0个纹理

        //设置环绕和过滤方式
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textures[0]);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);


        //使用源程序
        surfaceTexture = new SurfaceTexture(textures[0]);
        initCompleteRunnable.accept(surfaceTexture);
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
        this.width = width;
        this.height = height;
        GLES20.glViewport(0, 0, width, height);
    }

    public void draw(int width, int height) {
        this.imageHeight = height;
        this.imageWidth = width;

    }
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
        GLES20.glViewport(-xOffset, -yOffset, newWidth, (int) newHeight);
    }


    @Override
    public void onDrawFrame(GL10 gl) {
//        synchronized (this) {
//            if(isStop) {
//                Log.e(TAG, "不渲染了");
//                return;
//            }
//        }
//        Log.e(TAG, "onDrawFrame");

        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        surfaceTexture.updateTexImage();

//        if (adjustViewport)
        adjustViewport(imageWidth, imageHeight);
        GLES20.glUniform1f(GLES20.glGetUniformLocation(mProgram, "width"), (float)(this.newWidth));

        GLES20.glUniform1f(GLES20.glGetUniformLocation(mProgram, "height"), (float)this.height);

        GLES20.glUniform1f(GLES20.glGetUniformLocation(mProgram, "status"), (float)isBgr);



        // Draw texture
        GLES20.glUseProgram(mProgram);
        int textureParamHandle = GLES20.glGetUniformLocation(mProgram, "vTexture");


        int textureCoordinateHandle = GLES20.glGetAttribLocation(mProgram, "vTexCoordinate");
        int positionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        int textureTranformHandle = GLES20.glGetUniformLocation(mProgram, "textureTransform");

        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 4 * 3, vertexBuffer);

        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textures[0]);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glUniform1i(textureParamHandle, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);


        GLES20.glEnableVertexAttribArray(textureCoordinateHandle);
        GLES20.glVertexAttribPointer(textureCoordinateHandle, 4, GLES20.GL_FLOAT, false, 0, textureBuffer);

        this.videoTextureTransform = new float[]{
                1.0f ,0.0f ,0.0f ,0.0f ,
                0.0f ,-1.0f ,0.0f ,0.0f ,
                0.0f ,0.0f ,1.0f ,0.0f ,
                0.0f ,1.0f ,0.0f ,1.0f ,
        };

        GLES20.glUniformMatrix4fv(textureTranformHandle, 1, false, videoTextureTransform, 0);
        Matrix.scaleM(mMvpMatrix, 0, 1.5f, 1.5f, 1.5f);
        glUniformMatrix4fv(GLES20.glGetUniformLocation(mProgram, "uMvpMatrix"), 1, false, mMvpMatrix, 0);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, DRAW_ORDER.length, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);
        GLES20.glDisableVertexAttribArray(positionHandle);
        GLES20.glDisableVertexAttribArray(textureCoordinateHandle);
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }
}
