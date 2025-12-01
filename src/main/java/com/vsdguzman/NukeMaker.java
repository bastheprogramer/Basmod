package com.vsdguzman;

import com.vsdguzman.mixin.arrowAccessorMixin;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class NukeMaker {

    public static void MakeNuke(ServerCommandSource source, World world, Vec3d center, int size, String type) {
        int ArrowTntCount = (int) Math.floor(75 * Math.log(size + 1));

        switch (type) {
            case "Spiral": {
                TNTSpawner.spawnSpiralTnt(world, center, size);
                break;
            }
            case "Arrow": {
                ARROWSpawner.SpawnArrow(world, center);
                for (int i = 1; i < size + 1; i++) {
                    ARROWSpawner.spawnARROWRing(world, center, i * 0.01, i * 16, arrow ->
                            ((arrowAccessorMixin) arrow).invokeSetPierceLevel((byte) 127)
                    );
                }
                for (int i = 1; i < ArrowTntCount; i++) {
                    TNTSpawner.spawnTnt(world, center, 10);
                }
                break;
            }
            case "OrbitalVersion": {
                for (int i = size + 1; i >= 1; i--) {
                    TNTSpawner.spawnTntRing(world, center, 0.002, i * 16, 30 + i);
                    TNTSpawner.spawnTnt(world, center, 21);
                }
                TNTSpawner.spawnTnt(world, center, 30);
                for (int i = 1; i < 25; i++) {
                    TNTSpawner.spawnTnt(world, center, 20);
                }
                break;
            }
            case "BunkerBuster": {
                for (int i = 1; i < size + 1; i++) {
                    TNTSpawner.spawnTntRing(world, center, 0.0001, i * 32, 43 + i);
                    make8tnt(world, center, 43 + i);
                    make8tnt(world, center, 41);
                }
                for (int i = 1; i < size; i++) {
                    make8tnt(world, center, 40);
                }
                for (int x = 0; x < 8; x++) {
                    TNTSpawner.spawnTnt(world, center, x + 32);
                }
                break;
            }
            case "TunnelDigger": {
                for (int i = size; i > 0; i--) {
                    TNTSpawner.spawnTntRing(world, center, 0.01, i * 16, 20);
                    make8tnt(world, center, 19);
                }
                break;
            }
            case "RailGun": {
                double offset = 0.0625;
                ARROWSpawner.spawnARROWRing(world, center.subtract(0, offset, 0), 0.5,
                        (int) (size * (Math.floor(size / 3.5) + 1) * 16),
                        arrow -> ((arrowAccessorMixin) arrow).invokeSetPierceLevel((byte) 127)
                );
                for (int i = 1; i < ArrowTntCount + new int[]{0, 2, 1}[ArrowTntCount % 3]; i++) {
                    TNTSpawner.spawnTnt(world, center.add(0, 0.0625D, 0), 1).setNoGravity(true);
                }
                break;
            }
            case "Normal":
            default: {
                TNTSpawner.spawnTnt(world, center, 80);
                for (int i = 0; i < size; i++) {
                    TNTSpawner.spawnTntRing(world, center, (i + 1) * 8, (i + 1) * 16, 80 + (i + 1));
                }
                break;
            }
        }
    }

    private static void make8tnt(World world, Vec3d center, int fuse) {
        for (int i = 0; i < 8; i++) {
            TNTSpawner.spawnTnt(world, center, fuse);
        }
        return;
    }
}
