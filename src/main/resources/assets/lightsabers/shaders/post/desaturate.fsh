#version 330

uniform sampler2D InSampler;

in vec2 texCoord;

layout(std140) uniform DesaturateConfig {
    float Saturation;
};

out vec4 fragColor;

void main() {
    vec4 inputColor = texture(InSampler, texCoord);
    float luminance = dot(inputColor.rgb, vec3(0.3, 0.59, 0.11));
    vec3 outputColor = mix(vec3(luminance), inputColor.rgb, Saturation);
    fragColor = vec4(outputColor, inputColor.a);
}
