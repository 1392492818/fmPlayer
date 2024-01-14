#version 320 es

precision highp float;
in vec2 aCoordinate;
uniform sampler2D yTexture;
uniform sampler2D uTexture;
uniform sampler2D vTexture;
out vec4 vFragColor;



void main(void)
{

    float y = texture(yTexture, aCoordinate).x;
    float u = texture(uTexture, aCoordinate).x- 0.5;
    float v = texture(vTexture, aCoordinate).x- 0.5;

    vec3 rgb;
    rgb.r = y + 1.4022 * v;
    rgb.g = y - 0.3456 * u - 0.7145 * v;
    rgb.b = y + 1.771 * u;

    vFragColor = vec4(rgb, 1.0);

}