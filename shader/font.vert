#version 330 core
layout(location = 0) in vec4 position;
layout(location = 1) in vec2 texCoord;
layout(location = 2) in vec4 color;

out vec2 vTexCoord;
out vec4 vColor;

uniform mat4 uMVP;

void main() {
    vTexCoord = texCoord;
    vColor = color;
    gl_Position = uMVP * position;
}