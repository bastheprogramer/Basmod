package com.vsdguzman.mixin;

import com.vsdguzman.TNTSpawner;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.TntEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.vsdguzman.gamerules.CustomGameRules;

import java.util.ArrayList;
import java.util.List;

@Mixin(TntEntity.class)
public abstract class TNTEntityMixin {

    @Inject(method = "explode", at = @At("HEAD"), cancellable = true)
    private void onExplode(CallbackInfo ci) {
        TntEntity tnt = (TntEntity)(Object)this;
        MinecraftServer server = tnt.getServer();
        // Adjust TNT position by adding 1 to Y as before (for TNT duplication)
        Vec3d tntPos = tnt.getPos().add(0, 1, 0);
        World world = tnt.getWorld();
        if (!server.getGameRules().getBoolean(GameRules.TNT_EXPLODES)){
            return;
        }
        if (server.getGameRules().getBoolean(CustomGameRules.Realistic_tnt)) {
            // Compute explosion center with the correct Y offset using getBodyY(0.0625D)
            Vec3d explosionCenter = new Vec3d(tnt.getX(), tnt.getBodyY(0.0625D), tnt.getZ());
            float explosionRadius = 4.0F; // Adjust to your desired range

            // Container for blocks that will be affected by the explosion
            List<BlockPos> affectedBlocks = new ArrayList<>();

            // Calculate the area to scan (a cube around the explosion center)
            int minX = MathHelper.floor(explosionCenter.x - explosionRadius);
            int minY = MathHelper.floor(explosionCenter.y - explosionRadius);
            int minZ = MathHelper.floor(explosionCenter.z - explosionRadius);
            int maxX = MathHelper.floor(explosionCenter.x + explosionRadius);
            int maxY = MathHelper.floor(explosionCenter.y + explosionRadius);
            int maxZ = MathHelper.floor(explosionCenter.z + explosionRadius);

            // Iterate through potential affected blocks
            for (BlockPos pos : BlockPos.iterate(minX, minY, minZ, maxX, maxY, maxZ)) {
                double distance = explosionCenter.distanceTo(Vec3d.ofCenter(pos));
                if (distance <= explosionRadius) {
                    // Compute explosion force as a function of distance
                    double force = 1.0 - (distance / explosionRadius);
                    // Use a random chance based on force to determine if the block is destroyed
                    if (world.random.nextDouble() < force) {
                        affectedBlocks.add(pos.toImmutable());
                    }
                }
            }

            // Process block destruction realistically
            for (BlockPos pos : affectedBlocks) {
                BlockState state = world.getBlockState(pos);
                // Skip if block is air or has a non-empty fluid state (indicating a liquid)
                if (state.isAir() || !state.getFluidState().isEmpty()) {
                    continue;
                }
                // Remove the block from the world
                world.setBlockState(pos, Blocks.AIR.getDefaultState());
                // Spawn the falling block entity with the current block state
                FallingBlockEntity fallingBlock = FallingBlockEntity.spawnFromBlock(world, pos, state);
                // Optionally, apply a random velocity to simulate explosion debris
                Vec3d velocity = new Vec3d(
                        (world.random.nextDouble() - 0.5) * 0.5,
                        world.random.nextDouble() * 0.5,
                        (world.random.nextDouble() - 0.5) * 0.5
                );
                fallingBlock.setVelocity(velocity.x, velocity.y, velocity.z);
            }

            // Apply directional knockback to nearby entities
            List<Entity> nearbyEntities = world.getEntitiesByClass(Entity.class,
                    new Box(
                            explosionCenter.x - explosionRadius,
                            explosionCenter.y - explosionRadius,
                            explosionCenter.z - explosionRadius,
                            explosionCenter.x + explosionRadius,
                            explosionCenter.y + explosionRadius,
                            explosionCenter.z + explosionRadius
                    ),
                    entity -> entity != tnt
            );
            for (Entity entity : nearbyEntities) {
                Vec3d direction = entity.getPos().subtract(explosionCenter).normalize();
                double distance = Math.max(entity.getPos().distanceTo(explosionCenter), 0.1);
                // Calculate force using an inverse relation to distance (tweak as needed)
                double appliedForce = 4.0 * (1.0 / distance);
                if (entity instanceof FallingBlockEntity) {
                    entity.addVelocity(-direction.x * appliedForce, -direction.y * appliedForce, -direction.z * appliedForce);
                    continue;
                }
                entity.addVelocity(direction.x * appliedForce, direction.y * appliedForce, direction.z * appliedForce);
            }

            // Optionally, you can call the parent explosion logic if desired:
            // super.explode();
        }

        int tntdupnumber = server.getGameRules().getInt(CustomGameRules.TNTDUPS);
        if (tntdupnumber != 0) {
            // Use the shadowed 'level' field to pass the world to TNTSpawner.spawnTntRing
            TNTSpawner.spawnTntRing(world, tntPos, 1, tntdupnumber, 80);
            // Optionally cancel the explosion:
            // ci.cancel();
        }
    }
}
