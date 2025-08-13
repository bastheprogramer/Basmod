package com.vsdguzman;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.function.Consumer;

public class ARROWSpawner {
    public static ArrowEntity SpawnArrow(World world, Vec3d pos) {
        if (!(world instanceof ServerWorld serverWorld)) return null;

        ArrowEntity arrow = EntityType.ARROW.create(serverWorld, SpawnReason.COMMAND);
        arrow.setVelocity(0, 0, 0);
        arrow.setPosition(pos);
        serverWorld.spawnEntity(arrow);

        return arrow;
    }

    public static void spawnARROWRing(World world, Vec3d center, double radius, int count, Consumer<ArrowEntity> arrowModifier) {
        double angleIncrement = 2 * Math.PI / count;
        for (int i = 0; i < count; i++) {
            double angle = i * angleIncrement;
            double offsetX = radius * Math.cos(angle);
            double offsetZ = radius * Math.sin(angle);
            Vec3d pos = center.add(offsetX, 0, offsetZ);

            ArrowEntity arrow = SpawnArrow(world, pos);
            if (arrow != null) {
                arrowModifier.accept(arrow); // Apply user-defined logic
            }
        }
    }
}
