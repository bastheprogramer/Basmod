package com.vsdguzman;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ARROWSpawner {
    public static void SpawnArrow(World world, Vec3d pos) {
        // Ensure that we are running on the server side.
        if (!(world instanceof ServerWorld)) {
            return;
        }
        ServerWorld serverWorld = (ServerWorld) world;

        // Create a new arrow entity at the specified position using the serverWorld.
        ArrowEntity arrow = EntityType.ARROW.create(serverWorld, SpawnReason.COMMAND);
        if (arrow == null) {
            return;
        }
        // Set the arrow's position and angles.
        arrow.setPos(pos.getX(),pos.getY(),pos.getZ());

        // Optionally modify the arrow's velocity (here, we simply neutralize it).
        Vec3d vel = arrow.getVelocity();
        arrow.addVelocity(-vel.getX(), -vel.getY(), -vel.getZ());

        // Spawn the arrow entity in the world.
        serverWorld.spawnEntity(arrow);
    }

    public static void spawnARROWRing(World world, Vec3d center, double radius, int count) {
        double angleIncrement = 2 * Math.PI / count;
        for (int i = 0; i < count; i++) {
            double angle = i * angleIncrement;
            double offsetX = radius * Math.cos(angle);
            double offsetZ = radius * Math.sin(angle);
            double x = center.getX() + offsetX;
            double y = center.getY();
            double z = center.getZ() + offsetZ;

            Vec3d pos = new Vec3d(x, y, z);
            SpawnArrow(world, pos);
        }
    }
}
