package com.ovo.lastsongofelysian.client.renderer;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;

public final class EdenStarRenderGeometry {
    private EdenStarRenderGeometry() {
    }

    public static void drawSphere(
            Matrix4f pose,
            VertexConsumer consumer,
            float radius,
            int latitudeSegments,
            int longitudeSegments,
            int red,
            int green,
            int blue,
            int alpha
    ) {
        for (int latitude = 0; latitude < latitudeSegments; latitude++) {
            double latitude0 = -Math.PI * 0.5D + Math.PI * latitude / latitudeSegments;
            double latitude1 = -Math.PI * 0.5D + Math.PI * (latitude + 1) / latitudeSegments;
            for (int longitude = 0; longitude < longitudeSegments; longitude++) {
                double longitude0 = Math.PI * 2.0D * longitude / longitudeSegments;
                double longitude1 = Math.PI * 2.0D * (longitude + 1) / longitudeSegments;
                vertexOnSphere(pose, consumer, radius, latitude0, longitude0, red, green, blue, alpha);
                vertexOnSphere(pose, consumer, radius, latitude0, longitude1, red, green, blue, alpha);
                vertexOnSphere(pose, consumer, radius, latitude1, longitude1, red, green, blue, alpha);
                vertexOnSphere(pose, consumer, radius, latitude1, longitude0, red, green, blue, alpha);
            }
        }
    }

    public static void drawAnnulus(
            Matrix4f pose,
            VertexConsumer consumer,
            float innerRadius,
            float outerRadius,
            int segments,
            int red,
            int green,
            int blue,
            int alpha
    ) {
        for (int segment = 0; segment < segments; segment++) {
            double angle0 = Math.PI * 2.0D * segment / segments;
            double angle1 = Math.PI * 2.0D * (segment + 1) / segments;
            vertex(pose, consumer,
                    (float) (Math.cos(angle0) * innerRadius), 0.0F,
                    (float) (Math.sin(angle0) * innerRadius), red, green, blue, alpha);
            vertex(pose, consumer,
                    (float) (Math.cos(angle1) * innerRadius), 0.0F,
                    (float) (Math.sin(angle1) * innerRadius), red, green, blue, alpha);
            vertex(pose, consumer,
                    (float) (Math.cos(angle1) * outerRadius), 0.0F,
                    (float) (Math.sin(angle1) * outerRadius), red, green, blue, alpha);
            vertex(pose, consumer,
                    (float) (Math.cos(angle0) * outerRadius), 0.0F,
                    (float) (Math.sin(angle0) * outerRadius), red, green, blue, alpha);
        }
    }

    private static void vertexOnSphere(
            Matrix4f pose,
            VertexConsumer consumer,
            float radius,
            double latitude,
            double longitude,
            int red,
            int green,
            int blue,
            int alpha
    ) {
        double latitudeRadius = Math.cos(latitude) * radius;
        vertex(
                pose,
                consumer,
                (float) (Math.cos(longitude) * latitudeRadius),
                (float) (Math.sin(latitude) * radius),
                (float) (Math.sin(longitude) * latitudeRadius),
                red,
                green,
                blue,
                alpha
        );
    }

    private static void vertex(
            Matrix4f pose,
            VertexConsumer consumer,
            float x,
            float y,
            float z,
            int red,
            int green,
            int blue,
            int alpha
    ) {
        consumer.vertex(pose, x, y, z).color(red, green, blue, alpha).endVertex();
    }
}
