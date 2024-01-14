#extension GL_OES_EGL_image_external : require
uniform samplerExternalOES vTexture;
precision mediump float;
varying vec2 v_TexCoordinate;
uniform float width;
uniform float height;
uniform float nviews;
uniform float pwh;
uniform float slant;
uniform float loop;
uniform float mindex;
uniform float status;
uniform float image;
uniform float is3D;
uniform float leftRight;
uniform float videoRatio;

void main(void)
{

    vec3 rgb;
    rgb.r = 0.0;
    rgb.g = 0.0;
    rgb.b = 0.0;

   // rgb = texture2D(vTexture, vec2(v_TexCoordinate.x * videoRatio,  v_TexCoordinate.y)).rgb;
    rgb = texture2D(vTexture, v_TexCoordinate).rgb;
    gl_FragColor = vec4(rgb, 1.0);

}