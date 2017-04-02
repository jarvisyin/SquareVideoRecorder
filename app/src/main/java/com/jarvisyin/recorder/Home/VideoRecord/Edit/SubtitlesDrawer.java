package com.jarvisyin.recorder.Home.VideoRecord.Edit;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

import com.jarvisyin.recorder.Home.VideoRecord.BlockInfo;
import com.jarvisyin.recorder.Home.VideoRecord.Common.Utils.ShaderUtils;
import com.jarvisyin.recorder.Home.VideoRecord.VideoRecordActivity;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.List;

/**
 * Created by jarvisyin on 16/9/10.
 */
public class SubtitlesDrawer {
    private final VideoRecordActivity mContext;
    private String TAG = "VShopVideo SubtitlesDrawer";

    private FloatBuffer mCubeColors;
    private FloatBuffer mCubeTextureCoordinates;
    private FloatBuffer mExplainCubePositions;

    private float[] mMVPMatrix = new float[16];
    private float[] mViewMatrix = new float[16];
    private float[] mModelMatrix = new float[16];
    private int mMVPMatrixHandle;
    private int mPositionHandle;
    private int mColorHandle;
    private int mTextureUniformHandle;
    private int mTextureCoordinateHandle;
    private int mProgramHandle;
    private final int POSITION_DATA_SIZE = 3;
    private final int COLOR_DATA_SIZE = 4;
    private final int TEXTURE_COORDINATE_DATA_SIZE = 2;

    private int explainTextureDataHandles[];

    public SubtitlesDrawer(VideoRecordActivity context) {
        mContext = context;
    }

    public void create() {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        final float eyeX = 0.0f;
        final float eyeY = 0.0f;
        final float eyeZ = -0.5f;

        final float lookX = 0.0f;
        final float lookY = 0.0f;
        final float lookZ = -5.0f;
        final float upX = 0.0f;
        final float upY = 1.0f;
        final float upZ = 0.0f;
        Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);

        final String vertexShader = getVertexShader();
        final String fragmentShader = getFragmentShader();
        final int vertexShaderHandle = ShaderUtils.loadShader(GLES20.GL_VERTEX_SHADER, vertexShader);
        final int fragmentShaderHandle = ShaderUtils.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShader);
        mProgramHandle = createAndLinkProgram(vertexShaderHandle, fragmentShaderHandle, new String[]{"a_Position", "a_Color", "a_TexCoordinate"});

        final float cubeColor[] =
                {
                        1.0f, 0.0f, 0.0f, 1.0f,
                        1.0f, 0.0f, 0.0f, 1.0f,
                        1.0f, 0.0f, 0.0f, 1.0f,
                        1.0f, 0.0f, 0.0f, 1.0f,
                        1.0f, 0.0f, 0.0f, 1.0f,
                        1.0f, 0.0f, 0.0f, 1.0f,
                };
        final float cubeTextureCoordinate[] =
                {
                        0.0f, 0.0f,
                        0.0f, 1.0f,
                        1.0f, 0.0f,
                        0.0f, 1.0f,
                        1.0f, 1.0f,
                        1.0f, 0.0f,
                };


        final float explainCubePosition[] =
                {
                        -1.0f, -0.7f, 1.0f,//A
                        -1.0f, -0.95f, 1.0f,//B
                        1.0f, -0.7f, 1.0f,//C
                        -1.0f, -0.95f, 1.0f,//D
                        1.0f, -0.95f, 1.0f,//E
                        1.0f, -0.7f, 1.0f,//F
                };

        mExplainCubePositions = ByteBuffer.allocateDirect(explainCubePosition.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mExplainCubePositions.put(explainCubePosition).position(0);

        mCubeColors = ByteBuffer.allocateDirect(cubeColor.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mCubeColors.put(cubeColor).position(0);

        mCubeTextureCoordinates = ByteBuffer.allocateDirect(cubeTextureCoordinate.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mCubeTextureCoordinates.put(cubeTextureCoordinate).position(0);


        final int[] textureHandle = new int[1];
        List<BlockInfo> blockInfos = mContext.getBlockInfos();

        int length = blockInfos.size();
        explainTextureDataHandles = new int[length];
        for (int i = 0; i < length; i++) {
            BlockInfo block = blockInfos.get(i);
            File explainFile = block.getExplainFile();
            if (explainFile == null || !explainFile.exists()) {
                explainTextureDataHandles[i] = 0;
                break;
            }

            GLES20.glGenTextures(1, textureHandle, 0);
            if (textureHandle[0] != 0) {
                final Bitmap bitmap = BitmapFactory.decodeFile(block.getExplainFile().getPath());
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
                GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
                bitmap.recycle();
            }
            if (textureHandle[0] == 0) {
                throw new RuntimeException("failed to load texture");
            }
            explainTextureDataHandles[i] = textureHandle[0];
        }
    }


    public void draw(int postion) {
        if (postion < 0) {
            return;
        }

        int explainTextureDataHandle = explainTextureDataHandles[postion];
        if (explainTextureDataHandle == 0) {
            return;
        }

        GLES20.glUseProgram(mProgramHandle);
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_MVPMatrix");
        mTextureUniformHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_Texture");
        mPositionHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Position");
        mColorHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Color");
        mTextureCoordinateHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_TexCoordinate");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        Matrix.setIdentityM(mModelMatrix, 0);

        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE);

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0, 0.0f, -1.8f);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, explainTextureDataHandle);
        GLES20.glUniform1i(mTextureUniformHandle, 0);

        mExplainCubePositions.position(0);
        GLES20.glVertexAttribPointer(mPositionHandle, POSITION_DATA_SIZE, GLES20.GL_FLOAT, false, 0, mExplainCubePositions);
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        mCubeColors.position(0);
        GLES20.glVertexAttribPointer(mColorHandle, COLOR_DATA_SIZE, GLES20.GL_FLOAT, false, 0, mCubeColors);
        GLES20.glEnableVertexAttribArray(mColorHandle);
        mCubeTextureCoordinates.position(0);
        GLES20.glVertexAttribPointer(mTextureCoordinateHandle, TEXTURE_COORDINATE_DATA_SIZE, GLES20.GL_FLOAT, false, 0, mCubeTextureCoordinates);
        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);
    }

    private String getVertexShader() {
        final String vertexShader =
                "uniform mat4 u_MVPMatrix; \n" // A constant representing the combined model/view/projection matrix.
                        + "attribute vec4 a_Position; \n" // Per-vertex position information we will pass in.
                        + "attribute vec4 a_Color; \n" // Per-vertex color information we will pass in.
                        + "attribute vec2 a_TexCoordinate;\n" // Per-vertex texture coordinate information we will pass in.
                        + "varying vec4 v_Color; \n" // This will be passed into the fragment shader.
                        + "varying vec2 v_TexCoordinate; \n" // This will be passed into the fragment shader.
                        + "void main() \n" // The entry point for our vertex shader.
                        + "{ \n"
                        + " v_Color = a_Color; \n" // Pass the color through to the fragment shader.
                        // It will be interpolated across the triangle.
                        + " v_TexCoordinate = a_TexCoordinate;\n"// Pass through the texture coordinate.
                        + " gl_Position = u_MVPMatrix \n" // gl_Position is a special variable used to store the Final position.
                        + " * a_Position; \n" // Multiply the vertex by the matrix to get the Final point in
                        + "} \n"; // normalized screen coordinates. \n";
        return vertexShader;
    }

    private String getFragmentShader() {
        final String fragmentShader = "precision mediump float; \n"
                + "uniform sampler2D u_Texture; \n" // The input texture.
                + "varying vec4 v_Color; \n" // This is the color from the vertex shader interpolated across the
                + "varying vec2 v_TexCoordinate; \n" // Interpolated texture coordinate per fragment.
                + "void main() \n" // The entry point for our fragment shader.
                + "{ \n"
                + " gl_FragColor = texture2D(u_Texture, v_TexCoordinate); \n" // Pass the color directly through the pipeline.
                + "} \n";
        return fragmentShader;
    }

    private int createAndLinkProgram(final int vertexShaderHandle, final int fragmentShaderHandle, final String[] attributes) {
        int programHandle = GLES20.glCreateProgram();
        if (programHandle != 0) {
            GLES20.glAttachShader(programHandle, vertexShaderHandle);
            GLES20.glAttachShader(programHandle, fragmentShaderHandle);
            if (attributes != null) {
                final int size = attributes.length;
                for (int i = 0; i < size; i++) {
                    GLES20.glBindAttribLocation(programHandle, i, attributes[i]);
                }
            }
            GLES20.glLinkProgram(programHandle);
            final int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] == 0) {
                Log.e(TAG, "Error compiling program: " + GLES20.glGetProgramInfoLog(programHandle));
                GLES20.glDeleteProgram(programHandle);
                programHandle = 0;
            }
        }
        if (programHandle == 0) {
            throw new RuntimeException("Error creating program.");
        }
        return programHandle;
    }


}
