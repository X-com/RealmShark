#version 330 core
layout (location = 0) out vec4 fColor;

in vec2 vTexCoord;
in vec4 vColorL;
in vec4 vColorR;

uniform sampler2D uTexture;

void main() {
    vec4 texColor = texture(uTexture, vTexCoord);
    if (vTexCoord.y < 0.5) {
        fColor = vColorL * texColor;
    } else {
        fColor = vColorR * texColor;
    }
}