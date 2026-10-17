#version 330
#extension GL_ARB_separate_shader_objects : require

uniform sampler2D InSampler;

layout(location = 0) in vec2 texCoord;

layout(std140) uniform DesaturateConfig {
    float Saturation;
};

layout(location = 0) out vec4 fragColor;

void main() {
    vec4 inputColor = texture(InSampler, texCoord);
    float luminance = dot(inputColor.rgb, vec3(0.3, 0.59, 0.11));
    vec3 outputColor = mix(vec3(luminance), inputColor.rgb, Saturation);
    fragColor = vec4(outputColor, inputColor.a);
}
