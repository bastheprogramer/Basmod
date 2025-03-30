package com.vsdguzman.mixin;

import com.vsdguzman.TNTSpawner;
import com.vsdguzman.gamerules.CustomGameRules;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ArrowEntity.class)
public abstract class arrowtntmixin {

    /**
     * This injection adds a TNT arrow trail effect for arrows fired by admins,
     * then removes the arrow after a certain time or if it's on the ground.
     */
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void onTick(CallbackInfo ci) {
        ArrowEntity arrow = (ArrowEntity) (Object) this;
        World world = arrow.getWorld();
        // Only run on the server side and for players with permission level 2+
        if (world.isClient() || !(arrow.getOwner() instanceof ServerPlayerEntity serverPlayer
                && serverPlayer.getPermissionLevel() >= 2)) {
            return;
        }
        MinecraftServer server = world.getServer();

        // Handle TNT arrow trail if enabled.
        if (server.getGameRules().getBoolean(CustomGameRules.TNTARROWTRAIL)) {
            if (arrow.age >= 80 || arrow.isOnGround()) {
                arrow.remove(Entity.RemovalReason.DISCARDED);
                ci.cancel();
                return;
            }
            int fuseTime = Math.max(1, 80 - arrow.age);
            TNTSpawner.spawnTnt(world, arrow.getPos(), fuseTime);
        }
    }
}
