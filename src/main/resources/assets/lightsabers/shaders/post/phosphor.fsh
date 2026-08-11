#version 330

uniform sampler2D InSampler;
uniform sampler2D PrevSampler;

in vec2 texCoord;

layout(std140) uniform PhosphorConfig {
    vec3 Phosphor;
};

out vec4 fragColor;

void main() {
    vec4 currentColor = texture(InSampler, texCoord);
    vec4 previousColor = texture(PrevSampler, texCoord);
    fragColor = vec4(
        max(previousColor.rgb * Phosphor, currentColor.rgb),
        currentColor.a
    );
}
