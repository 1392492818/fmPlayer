attribute vec4 vPosition;
attribute vec4 vTexCoordinate;
uniform mat4 textureTransform;
uniform mat4 uMvpMatrix;
varying vec2 v_TexCoordinate;
void main() {
    //纹理坐标转换，以左上角为原点的纹理坐标转换成以左下角为原点的纹理坐标，
    // 比如以左上角为原点的（0，0）对应以左下角为原点的纹理坐标的（0，1）
    v_TexCoordinate = (textureTransform * vTexCoordinate).xy;

    gl_Position = vPosition;
}
