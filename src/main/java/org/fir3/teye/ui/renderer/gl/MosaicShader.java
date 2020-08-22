package org.fir3.teye.ui.renderer.gl;

import org.fir3.teye.Resources;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;

class MosaicShader implements Disposable {
    // TODO:    Uniform/Variable names should rather be specified inside the
    //          Resources class.

    private static final String UNIFORM_PROJECTION_MATRIX =
            "projection_matrix";

    private static final String UNIFORM_TEXTURES = "textures";
    private static final String IN_V_POSITION = "v_position";
    private static final String IN_V_COLOR = "v_color";
    private static final String IN_V_TEXTURE_INDEX = "v_texture_index";
    private static final String IN_V_TEXTURE_POSITION = "v_texture_position";

    static final int ATTRIB_LOCATION_V_POSITION = 0;
    static final int ATTRIB_LOCATION_V_COLOR = 1;
    static final int ATTRIB_LOCATION_V_TEXTURE_INDEX = 2;
    static final int ATTRIB_LOCATION_V_TEXTURE_POSITION = 3;

    static final int TEXTURES_ARRAY_SIZE = 16;

    private static int createShader(int shaderType, String source) {
        int shaderId = GL20.glCreateShader(shaderType);
        GL20.glShaderSource(shaderId, source);
        GL20.glCompileShader(shaderId);

        int state = GL20.glGetShaderi(shaderId, GL20.GL_COMPILE_STATUS);

        if (state != GL11.GL_TRUE)
            throw new IllegalStateException(
                    "Shader compilation failed! Info Log: "
                            + GL20.glGetShaderInfoLog(shaderId));

        return shaderId;
    }

    private final int[] texturesLocations;
    private int vertexShaderId, fragmentShaderId, programId;
    private int projectionMatrixLocation;

    MosaicShader() {
        this.texturesLocations = new int[MosaicShader.TEXTURES_ARRAY_SIZE];
    }

    @Override
    public void dispose() {
        if (this.programId == 0)
            return;

        GL20.glDetachShader(this.programId, this.vertexShaderId);
        GL20.glDetachShader(this.programId, this.fragmentShaderId);

        GL20.glDeleteProgram(this.programId);
        GL20.glDeleteShader(this.vertexShaderId);
        GL20.glDeleteShader(this.fragmentShaderId);

        this.programId = 0;
        this.vertexShaderId = 0;
        this.fragmentShaderId = 0;
        this.projectionMatrixLocation = 0;

        for (int i = 0; i < MosaicShader.TEXTURES_ARRAY_SIZE; i++)
            this.texturesLocations[i] = 0;
    }

    /**
     * Initializes the shader.
     *
     * @throws IllegalStateException    If the shader sources are invalid.
     */
    void initialize() {
        // Shader preparation

        this.vertexShaderId = MosaicShader.createShader(
                GL20.GL_VERTEX_SHADER,
                Resources.readComplete(
                        Resources.SHADER_GL_MOSAIC_VERTEX_GLSL,
                        StandardCharsets.UTF_8));

        this.fragmentShaderId = MosaicShader.createShader(
                GL20.GL_FRAGMENT_SHADER,
                Resources.readComplete(
                        Resources.SHADER_GL_MOSAIC_FRAGMENT_GLSL,
                        StandardCharsets.UTF_8));

        // Program preparation and linking

        this.programId = GL20.glCreateProgram();
        GL20.glAttachShader(this.programId, this.vertexShaderId);
        GL20.glAttachShader(this.programId, this.fragmentShaderId);

        GL20.glBindAttribLocation(
                this.programId,
                MosaicShader.ATTRIB_LOCATION_V_POSITION,
                MosaicShader.IN_V_POSITION);

        GL20.glBindAttribLocation(
                this.programId,
                MosaicShader.ATTRIB_LOCATION_V_COLOR,
                MosaicShader.IN_V_COLOR);

        GL20.glBindAttribLocation(
                this.programId,
                MosaicShader.ATTRIB_LOCATION_V_TEXTURE_INDEX,
                MosaicShader.IN_V_TEXTURE_INDEX);

        GL20.glBindAttribLocation(
                this.programId,
                MosaicShader.ATTRIB_LOCATION_V_TEXTURE_POSITION,
                MosaicShader.IN_V_TEXTURE_POSITION);

        GL20.glLinkProgram(this.programId);
        int state = GL20.glGetProgrami(this.programId, GL20.GL_LINK_STATUS);

        if (state != GL11.GL_TRUE)
            throw new IllegalStateException(
                    "Program linkage failed! Info log: "
                            + GL20.glGetProgramInfoLog(this.programId));

        GL20.glValidateProgram(this.programId);
        state = GL20.glGetProgrami(this.programId, GL20.GL_VALIDATE_STATUS);

        if (state != GL11.GL_TRUE)
            throw new IllegalStateException("Program validation failed!");

        // Retrieving the uniform locations

        this.projectionMatrixLocation = GL20.glGetUniformLocation(
                this.programId,
                MosaicShader.UNIFORM_PROJECTION_MATRIX);

        for (int i = 0; i < MosaicShader.TEXTURES_ARRAY_SIZE; i++)
            this.texturesLocations[i] = GL20.glGetUniformLocation(
                    this.programId,
                    MosaicShader.UNIFORM_TEXTURES + "[" + i + "]");

        // Binding the texture units to the texture locations

        GL20.glUseProgram(this.programId);

        for (int i = 0; i < MosaicShader.TEXTURES_ARRAY_SIZE; i++)
            GL20.glUniform1i(this.texturesLocations[i], i);
    }

    /**
     * Enables this shader program.
     */
    void use() {
        this.requireProgram();
        GL20.glUseProgram(this.programId);
    }

    void setProjectionMatrix(Matrix4f matrix) {
        if (matrix == null)
            throw new NullPointerException("matrix is null!");

        this.requireProgram();

        FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
        matrix.get(buffer);

        GL20.glUseProgram(this.programId);
        GL20.glUniformMatrix4fv(this.projectionMatrixLocation, false, buffer);
    }

    private void requireProgram() {
        if (this.programId > 0)
            return;

        throw new IllegalStateException("Program not initialized!");
    }
}
