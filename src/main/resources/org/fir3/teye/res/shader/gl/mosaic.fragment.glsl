#version 150 core

uniform sampler2D textures[16];

in vec4 f_color;
flat in int f_texture_index;
in vec2 f_texture_position;

out vec4 o_color;

void main() {
    o_color = f_color;

    /*
     * If v_texture_index is greater or equal to zero, a texture has been
     * specified.
     */

    if (f_texture_index < 0)
        return;

    /*
     * NOTE:    Since the texture coordinates are not normalized, but OpenGL
                requires them normalized, we need to perform the normalization
                here.
     */

    vec2 texture_size = textureSize(textures[f_texture_index], 0);

    o_color *= texture(textures[f_texture_index], vec2(
        f_texture_position.s / texture_size.s,
        f_texture_position.t / texture_size.t));
}
