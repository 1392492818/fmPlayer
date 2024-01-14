#version 320 es
in vec4 vPosition;
in vec4 vCoordinate;
uniform mat4 textureTransform;
out vec2 aCoordinate;
void main() {
    aCoordinate = (textureTransform * vCoordinate).xy;
//    aCoordinate = (vCoordinate).xy;

    gl_Position = vPosition;
}