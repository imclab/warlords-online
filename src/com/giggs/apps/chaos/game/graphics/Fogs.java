package com.giggs.apps.chaos.game.graphics;

import org.andengine.entity.primitive.Rectangle;
import org.andengine.opengl.shader.PositionTextureCoordinatesShaderProgram;
import org.andengine.opengl.shader.ShaderProgram;
import org.andengine.opengl.shader.constants.ShaderProgramConstants;
import org.andengine.opengl.shader.exception.ShaderProgramException;
import org.andengine.opengl.shader.exception.ShaderProgramLinkException;
import org.andengine.opengl.shader.source.IShaderSource;
import org.andengine.opengl.shader.source.StringShaderSource;
import org.andengine.opengl.util.GLState;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.opengl.vbo.attribute.VertexBufferObjectAttributes;

import android.opengl.GLES20;

public class Fogs extends Rectangle {

    public Fogs(float pWidth, float pHeight, VertexBufferObjectManager vertexBufferObjectManager) {
        super(0, 0, pWidth, pHeight, vertexBufferObjectManager);
        setShaderProgram(shader);
    }

    private static IShaderSource fShader = new StringShaderSource(
            "precision lowp float;                 \n"
                    + "mediump vec2 center = vec2(0.5, 0.5);                                                                            \n"
                    + "varying mediump vec2 "
                    + ShaderProgramConstants.VARYING_TEXTURECOORDINATES
                    + ";\n"
                    + "void main()                                                                                  \n"
                    + "{                                                                                            \n"
                    + "   mediump vec2 texCoord = "
                    + ShaderProgramConstants.VARYING_TEXTURECOORDINATES
                    + ";\n"
                    + "   float distance = distance(texCoord, center);   \n"
                    + "   vec4 NoFogColor = vec4(0.0,0.0,0.0,0.0);                                                  \n"
                    + "   if (distance > 0.07) {                               \n"
                    + "   float FogDensity = 100.;                                                                   \n"
                    + "   vec4 FogColor = vec4(0.0,0.0,0.0,0.5);                                                    \n"
                    + "   float FogFactor = exp(-abs(distance * 30.));                                    \n"
                    + "   gl_FragColor = mix(FogColor, NoFogColor, FogFactor);                                                   \n"
                    + "} else {                     \n"
                    + "   gl_FragColor = NoFogColor;                                                   \n"
                    +"}}");

    public static int centerLocation = 0;

    public static ShaderProgram shader = new ShaderProgram(new StringShaderSource(
            PositionTextureCoordinatesShaderProgram.VERTEXSHADER), fShader) {

        @Override
        protected void link(GLState pGLState) throws ShaderProgramLinkException {
            GLES20.glBindAttribLocation(this.mProgramID, ShaderProgramConstants.ATTRIBUTE_POSITION_LOCATION,
                    ShaderProgramConstants.ATTRIBUTE_POSITION);
            GLES20.glBindAttribLocation(this.mProgramID, ShaderProgramConstants.ATTRIBUTE_TEXTURECOORDINATES_LOCATION,
                    ShaderProgramConstants.ATTRIBUTE_TEXTURECOORDINATES);
            super.link(pGLState);
            PositionTextureCoordinatesShaderProgram.sUniformModelViewPositionMatrixLocation = this
                    .getUniformLocation(ShaderProgramConstants.UNIFORM_MODELVIEWPROJECTIONMATRIX);
        }

        @Override
        public void bind(GLState pGLState, VertexBufferObjectAttributes pVertexBufferObjectAttributes)
                throws ShaderProgramException {
            super.bind(pGLState, pVertexBufferObjectAttributes);
            GLES20.glUniformMatrix4fv(PositionTextureCoordinatesShaderProgram.sUniformModelViewPositionMatrixLocation,
                    1, false, pGLState.getModelViewProjectionGLMatrix(), 0);
        }

    };

}
