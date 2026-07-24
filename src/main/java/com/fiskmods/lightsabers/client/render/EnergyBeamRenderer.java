package com.fiskmods.lightsabers.client.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public final class EnergyBeamRenderer {
    private static final double MIN_PERPENDICULAR_LENGTH_SQUARED = 1.0E-8D;

    private EnergyBeamRenderer() {
    }

    public static void renderCameraFacingQuad(
            VertexConsumer consumer,
            Matrix4f matrix,
            Vec3 start,
            Vec3 end,
            Vec3 camera,
            double halfWidth,
            Vec3 color,
            float alpha
    ) {
        Vec3 direction = end.subtract(start);
        Vec3 toCamera = camera.subtract(start.add(end).scale(0.5D));
        Vec3 perpendicular = direction.cross(toCamera);
        if (perpendicular.lengthSqr() < MIN_PERPENDICULAR_LENGTH_SQUARED) {
            perpendicular = direction.cross(new Vec3(0, 1, 0));
        }
        if (perpendicular.lengthSqr() < MIN_PERPENDICULAR_LENGTH_SQUARED) {
            return;
        }
        perpendicular = perpendicular.normalize().scale(halfWidth);

        vertex(consumer, matrix, start.add(perpendicular), color, alpha);
        vertex(consumer, matrix, start.subtract(perpendicular), color, alpha);
        vertex(consumer, matrix, end.subtract(perpendicular), color, alpha);
        vertex(consumer, matrix, end.add(perpendicular), color, alpha);
    }

    private static void vertex(
            VertexConsumer consumer,
            Matrix4f matrix,
            Vec3 position,
            Vec3 color,
            float alpha
    ) {
        consumer.addVertex(matrix, (float) position.x, (float) position.y, (float) position.z)
                .setColor((float) color.x, (float) color.y, (float) color.z, alpha);
    }
}
