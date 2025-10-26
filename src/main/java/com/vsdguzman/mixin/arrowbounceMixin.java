package com.vsdguzman.mixin;

import com.vsdguzman.Basmod;
import com.vsdguzman.gamerules.CustomGameRules;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;

import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PersistentProjectileEntity.class)
public abstract class arrowbounceMixin {
    @Inject(method = "onBlockHit", at = @At("HEAD"), cancellable = true)
    private void onBlockHit(BlockHitResult blockHit, CallbackInfo ci) {
        // Check if this instance is actually an ArrowEntity
        PersistentProjectileEntity projectile = (PersistentProjectileEntity) (Object) this;

        if (projectile.getEntityWorld().isClient()){
            return;
        }

        if (!(projectile instanceof ArrowEntity)) {
            return;
        }

        // Cast explicitly
        ArrowEntity arrow = (ArrowEntity) (Object) this;

        // Check your custom gamerule.
        if (!arrow.getEntityWorld().getServer().getGameRules().getBoolean(CustomGameRules.Boucing_Arrows)) {
            return;
        }

        // Get the arrow's current velocity.
        Vec3d velocity = arrow.getVelocity();
        Vec3d reflected;
        double speed = Math.pow(velocity.squaredDistanceTo(0,0,0),0.5);

        if (speed < 0.25){
            return;
        }

        // Reflect velocity based on the block face hit.
        switch (blockHit.getSide()) {
            case UP:
            case DOWN:
                reflected = new Vec3d(velocity.x, -velocity.y, velocity.z);
                break;
            case NORTH:
            case SOUTH:
                reflected = new Vec3d(velocity.x, velocity.y, -velocity.z);
                break;
            case EAST:
            case WEST:
                reflected = new Vec3d(-velocity.x, velocity.y, velocity.z);
                break;
            default:
                reflected = velocity;
                break;
        }

        // Apply a bounce factor.
        double bounceFactor = 1.1;
        reflected = reflected.multiply(bounceFactor);

        // Update the arrow's velocity and position.
        arrow.setVelocity(reflected);
        arrow.setPosition(
                arrow.getX() + reflected.x * 0.1,
                arrow.getY() + reflected.y * 0.1,
                arrow.getZ() + reflected.z * 0.1
        );
        // Play sound
        arrow.playSound(SoundEvents.BLOCK_SLIME_BLOCK_BREAK, 2f, 1f);

        // Cancel the default behavior.
        ci.cancel();
    }
}
