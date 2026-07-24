#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
in vec2 oneTexel;

uniform vec2 InSize;

uniform float Resolution;
uniform float Saturation;
uniform float MosaicSize;

out vec4 fragColor;

void main() {
    vec2 mosaicInSize = InSize / MosaicSize;
    vec2 fractPix = fract(texCoord * mosaicInSize) / mosaicInSize;
    
    vec4 baseTexel = texture(DiffuseSampler, texCoord - fractPix);
    
    baseTexel = baseTexel - fract(baseTexel * Resolution) / Resolution;
    float luma = dot(baseTexel.rgb, vec3(0.3, 0.59, 0.11));
    vec3 chroma = (baseTexel.rgb - luma) * Saturation;
    baseTexel = vec4(luma + chroma, baseTexel.a);

    fragColor = baseTexel;
}
