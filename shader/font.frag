#version 330 core
layout(location = 0) out vec4 fColor;

in vec2 vTexCoord;
in vec4 vColor;

uniform sampler2D uTexture;

void main() {
    vec4 texColor = texture(uTexture, vTexCoord);
    fColor = vColor * texColor;
}