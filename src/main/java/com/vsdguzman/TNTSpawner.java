package com.vsdguzman;

import net.minecraft.entity.TntEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.server.world.ServerWorld;

public class TNTSpawner {

    /**
     * Spawns a ring of TNT entities around the given center position.
     *
     * @param world  The world in which to spawn the TNT.
     * @param center The center position for the ring.
     * @param radius The radius of the ring (in blocks).
     * @param count  The number of TNT entities to spawn.
     * @param fuse   The fuse value (in ticks) for each TNT.
     */
    public static void spawnTntRing(World world, Vec3d center, double radius, int count, int fuse) {
        double angleIncrement = 2 * Math.PI / count;
        for (int i = 0; i < count; i++) {
            double angle = i * angleIncrement;
            double offsetX = radius * Math.cos(angle);
            double offsetZ = radius * Math.sin(angle);
            // Center the TNT on the block; cast to int for BlockPos.
            double x = (center.getX() + offsetX);
            double y = center.getY();
            double z = (center.getZ() + offsetZ);

            Vec3d pos = new Vec3d(x, y, z);
            spawnTnt(world, pos, fuse);
        }
    }

    public static void spawnSpiralTnt(World world, Vec3d center, double radius) {
        // For extremely large radii, only spawn one TNT to avoid overload.

        // Spawn the center TNT first (this explodes first).
        spawnTnt(world, center, 80);

        // Total number of TNT based on radius.
        int totalCount = (int) (4 * radius * (radius+1));

        for (int i = 1; i < totalCount; i++) {
            double xoffset = 0;
            double zoffset = 0;

            xoffset = Math.cos(8*i)*Math.pow(i*Math.PI,0.5)*2.5;
            zoffset = Math.sin(8*i)*Math.pow(i*Math.PI,0.5)*2.5;

            double x = xoffset+center.getX();
            double z = zoffset+center.getZ();

            spawnTnt(world, new Vec3d(x,center.getY(),z),80+i);
            spawnTnt(world, new Vec3d(-xoffset+center.getX(),center.getY(),z),80+i);
        }
    }





    public static void stabTNT(World world, Vec3d center,int depth){
        int fuse = 20;

        for (int i = 0; i < 8*depth; i++) {
            spawnTnt(world, center, fuse);
        }

        for (int i = 0; i < 8*depth; i++) {
            spawnTnt(world,center,fuse-1);
        }

        fuse--;
        for (int x = 0; x < 8; x++) {
            spawnTnt(world,center,fuse);
            fuse--;
        }
    }

    /**
     * Spawns a single TNT entity at the given position with the specified fuse.
     *
     * @param world The world in which to spawn the TNT.
     * @param pos   The position where the TNT will be spawned.
     * @param fuse  The fuse value (in ticks) for the TNT.
     */
    public static void spawnTnt(World world, Vec3d pos, int fuse) {
        // Ensure that we are running on the server side.
        if (!(world instanceof ServerWorld)) {
            return;
        }
        ServerWorld serverWorld = (ServerWorld) world;
        // Use the spawn method with a SpawnReason.

        TntEntity tnt = new TntEntity(world, pos.getX(), pos.getY(), pos.getZ(),null);
        if (tnt != null) {
            tnt.refreshPositionAndAngles(pos.getX(), pos.getY(), pos.getZ(), 0.0F, 0.0F);
            Vec3d vel = tnt.getVelocity();
            tnt.addVelocity(-vel.getX(),-vel.getY(),-vel.getZ());
            tnt.setFuse(fuse);
            serverWorld.spawnEntity(tnt);
        }
    }
}
