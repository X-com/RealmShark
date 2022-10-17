#version 330 core
layout(location = 0) in vec4 position;
layout(location = 1) in vec2 texCoord;
layout(location = 2) in vec4 color;
layout(location = 3) in float id;

out vec2 vTexCoord;
out vec4 vColor;

uniform mat4 uMVP;

void main() {
    gl_Position = uMVP * position;
    vTexCoord = vec2((texCoord.x + id) / 13, texCoord.y);
    vColor = color;
}
