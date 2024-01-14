attribute vec4 vPosition;
attribute vec4 vCoordinate;
uniform mat4 textureTransform;
varying vec2 aCoordinate;
void main() {
    aCoordinate = (textureTransform * vCoordinate).xy;
//    aCoordinate = (vCoordinate).xy;

    gl_Position = vPosition;
}