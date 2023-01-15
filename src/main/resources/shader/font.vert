#version 330 core
layout(location = 0) in vec4 position;
layout(location = 1) in vec2 texCoord;
layout(location = 2) in vec4 colorL;
layout(location = 3) in vec4 colorR;

out vec2 vTexCoord;
out vec4 vColorL;
out vec4 vColorR;

uniform mat4 uMVP;

void main() {
    vTexCoord = texCoord;
    vColorL = colorL;
    vColorR = colorR;
    gl_Position = uMVP * position;
}