package com.bkmsx.atestopengl;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by bkmsx on 1/25/2017.
 */

public class CustomRenderer implements GLSurfaceView.Renderer{
    Context mContext;
    int a = 1;
    float[] triangleParams =
            {-1f, -1f, 0.0f,
//                            1.0f, 0.0f, 0.0f, 1.0f,

                    1f, -1, 0.0f,
//                            0.0f, 0.0f, 1.0f, 1.0f,

                    -1, 1, 0.0f,
//                            0.0f, 1.0f, 0.0f, 1.0f,

                    1, 1f, 0.0f};
    //                            1f, 1f, 1f, 1f};
    short order[] = {0, 1, 2, 1, 2, 3};

    float[] texCoords = {
            0, 0,
            1, 0,
            0, 1,
            1, 1
    };

    FloatBuffer vertexBuffer;
    ShortBuffer orderBuffer;
    FloatBuffer texBuffer;
    int programHandle;

    String vertexCode = "uniform mat4 u_MVPMatrix; \n" +
            "attribute vec4 a_Position; \n" +
            "attribute vec4 a_Texture;" +
            "uniform mat4 u_TextureMatrix;" +
            "varying vec2 v_Texture;" +
//            "attribute vec4 a_Color; \n" +
//            "varying vec4 v_Color; \n" +

            "void main() \n" +
            "{ \n" +
            "v_Texture = (u_TextureMatrix * a_Texture).xy;" +
//            "v_Color = a_Color; \n" +
            "gl_Position = u_MVPMatrix * a_Position; \n" +
            "} \n";

    String fragmentCodeNegative = "#extension GL_OES_EGL_image_external : require\n" +
            "precision mediump float; \n" +
            "varying vec2 v_Texture;" +
            "uniform samplerExternalOES s_Texture;" +
//            "varying vec4 v_Color; \n" +

            "void main() \n" +
            "{ \n" +
            "vec4 color = texture2D(s_Texture, v_Texture);" +
            "gl_FragColor = vec4(1.0 - color.rgb, color.a); \n" +
            "} \n";

    String fragmentCodeNormal = "#extension GL_OES_EGL_image_external : require\n" +
            "precision mediump float; \n" +
            "varying vec2 v_Texture;" +
            "uniform samplerExternalOES s_Texture;" +
//            "varying vec4 v_Color; \n" +

            "void main() \n" +
            "{ \n" +
            "vec4 color = texture2D(s_Texture, v_Texture);" +
            "gl_FragColor = color; \n" +
            "} \n";
    String fragmentCodeGray = "#extension GL_OES_EGL_image_external : require\n" +
            "precision mediump float; \n" +
            "varying vec2 v_Texture;" +
            "uniform samplerExternalOES s_Texture;" +
//            "varying vec4 v_Color; \n" +

            "void main() \n" +
            "{ \n" +
            "vec4 color = texture2D(s_Texture, v_Texture);" +
            "gl_FragColor = vec4(color.r * 0.21 + color.g * 0.72 + color.b * 0.07," +
            "color.r * 0.21 + color.g * 0.72 + color.b * 0.07," +
            "color.r * 0.21 + color.g * 0.72 + color.b * 0.07," +
            " color.a);\n"
            + "}";
    String fragmentCode;
    int mMVPMatrixHandle, mPositionHandle, mColorHandle, mTexHandle, mTextureMatrixHandle;
    float[] mMVPMatrix = new float[16],
            mViewMatrix = new float[16],
            mModelMatrix = new float[16],
            mProjectionMatrix = new float[16],
            mTextureMatrix = new float[16];

    SurfaceTexture surfaceTexture;
    int[] textureNames;
    int GL_TEXTURE_EXTERNAL_OES = 0x8D65;
    OnSurfaceTextureCreated onSurfaceTextureCreated;

    CustomRenderer (Context context, int type) {
        mContext = context;
        switch (type) {
            case 0: fragmentCode = fragmentCodeNormal;
                break;
            case 1: fragmentCode = fragmentCodeNegative;
                break;
            case 2: fragmentCode = fragmentCodeGray;
                break;
        }
    }



    public SurfaceTexture getSurfaceTexture() {
        return surfaceTexture;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(1, 1, 0.5f, 1);

        vertexBuffer = ByteBuffer.allocateDirect(triangleParams.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        vertexBuffer.put(triangleParams).position(0);

        orderBuffer = ByteBuffer.allocateDirect(order.length * 2)
                .order(ByteOrder.nativeOrder()).asShortBuffer();
        orderBuffer.put(order).position(0);

        texBuffer = ByteBuffer.allocateDirect(texCoords.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        texBuffer.put(texCoords).position(0);

        setupImage();

        int vertextHandle = loadShader(GLES20.GL_VERTEX_SHADER, vertexCode);
        final int[] compileStatus = new int[1];
        GLES20.glGetShaderiv(vertextHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

        if (compileStatus[0] == 0) {
            log("Vertex compile error");
        } else {
            log("Vertex compile oke");
        }

        int fragmentHandle = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentCode);
        GLES20.glGetShaderiv(fragmentHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

        if (compileStatus[0] == 0) {
            log("Fragment compile error");
        } else {
            log("Fragment compile oke");
        }

        programHandle = GLES20.glCreateProgram();
        GLES20.glAttachShader(programHandle, vertextHandle);
        GLES20.glAttachShader(programHandle, fragmentHandle);

        GLES20.glLinkProgram(programHandle);

        mMVPMatrixHandle = GLES20.glGetUniformLocation(programHandle, "u_MVPMatrix");
        mTextureMatrixHandle = GLES20.glGetUniformLocation(programHandle, "u_TextureMatrix");
        mPositionHandle = GLES20.glGetAttribLocation(programHandle, "a_Position");
//        mColorHandle = GLES20.glGetAttribLocation(programHandle, "a_Color");
        mTexHandle = GLES20.glGetAttribLocation(programHandle, "a_Texture");
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, 1f, 0f, 0f, 0, 0f, 0.5f, 0);
        GLES20.glUseProgram(programHandle);
    }

    private void setupImage() {
        textureNames = new int[1];
        GLES20.glGenTextures(1, textureNames, 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, textureNames[0]);

        GLES20.glTexParameterf(GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GL_TEXTURE_EXTERNAL_OES,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
//        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
//        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
//
//        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
//        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        surfaceTexture = new SurfaceTexture(textureNames[0]);
        onSurfaceTextureCreated.onSurfaceTextureCreated();
//        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.icon_launcher_2);
//        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
//
//        bitmap.recycle();
    }

    public void setOnSurfaceTextureListener(OnSurfaceTextureCreated listener){
        onSurfaceTextureCreated = listener;
    }

    public interface OnSurfaceTextureCreated {
        void onSurfaceTextureCreated();
    }

    private void log(String msg) {
        Log.e("CustomRenderer", msg);
    }

    private int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;
        Matrix.frustumM(mProjectionMatrix, 0, -1.0f, 1.0f, -1.0f, 1.0f,
                1.0f, 10.0f);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        surfaceTexture.updateTexImage();
        surfaceTexture.getTransformMatrix(mTextureMatrix);
        GLES20.glClearColor(0.5f, 0f, 0.5f, 0.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        long time = SystemClock.uptimeMillis() % 1000L;
        float angleInDegrees = (360.0f / 1000.0f) * ((int) time);

        // Draw the triangle facing straight on.
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.rotateM(mModelMatrix, 0, angleInDegrees, 0f, 0f, 1.0f);

        float[] mvMatrix = new float[16];
//        Matrix.multiplyMM(mvMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
//        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        vertexBuffer.position(0);
        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer);
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        GLES20.glVertexAttribPointer(mTexHandle, 2, GLES20.GL_FLOAT, false, 0, texBuffer);
        GLES20.glEnableVertexAttribArray(mTexHandle);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, textureNames[0]);
        int sampleLoc = GLES20.glGetUniformLocation(programHandle, "s_Texture");
        GLES20.glUniform1i(sampleLoc, 0);
//        vertexBuffer.position(3);
//        GLES20.glVertexAttribPointer(mColorHandle, 4, GLES20.GL_FLOAT, false, 28, vertexBuffer);
//        GLES20.glEnableVertexAttribArray(mColorHandle);

        Matrix.setIdentityM(mMVPMatrix, 0);
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
        GLES20.glUniformMatrix4fv(mTextureMatrixHandle, 1, false, mTextureMatrix, 0);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, order.length, GLES20.GL_UNSIGNED_SHORT, orderBuffer);

        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mColorHandle);
    }


}
