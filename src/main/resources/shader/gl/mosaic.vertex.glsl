#version 150 core

uniform mat4 projection_matrix;

in vec2 v_position;
in vec4 v_color;
in int v_texture_index;
in vec2 v_texture_position;

out vec4 f_color;
flat out int f_texture_index;
out vec2 f_texture_position;

void main() {
    /* Calculating the position of the current vertex. */

    gl_Position = projection_matrix * vec4(v_position.xy, 0.0, 1.0);

    /*
     * Passing the required information to the fragment shader.
     *
     * NOTE:    Since we pass RGBA values of the range 0-255 to the shader, we
     *          need to normalize them as OpenGL expects them in the range 0-1.
     */

    f_color = vec4(
        v_color.r / 255.0,
        v_color.g / 255.0,
        v_color.b / 255.0,
        v_color.a / 255.0);

    f_texture_index = v_texture_index;
    f_texture_position = v_texture_position;
}
