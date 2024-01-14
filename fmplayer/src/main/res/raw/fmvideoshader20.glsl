precision highp float;
varying vec2 aCoordinate;
uniform sampler2D yTexture;
uniform sampler2D uTexture;
uniform sampler2D vTexture;

void main(void)
{
    float y = texture2D(yTexture, aCoordinate).r;
    float u = texture2D(uTexture, aCoordinate).r- 0.5;
    float v = texture2D(vTexture, aCoordinate).r- 0.5;

    vec3 rgb;
    rgb.r = y + 1.4022 * v;
    rgb.g = y - 0.3456 * u - 0.7145 * v;
    rgb.b = y + 1.771 * u;

    gl_FragColor = vec4(rgb, 1.0);

}