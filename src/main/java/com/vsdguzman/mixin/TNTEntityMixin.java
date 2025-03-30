package com.vsdguzman.mixin;

import com.vsdguzman.TNTSpawner;
import net.minecraft.entity.TntEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.vsdguzman.gamerules.CustomGameRules;

@Mixin(TntEntity.class)
public abstract class TNTEntityMixin {


    @Inject(method = "explode", at = @At("HEAD"), cancellable = true)
    private void onExplode(CallbackInfo ci) {
        TntEntity tnt = (TntEntity)(Object)this;
        MinecraftServer server = tnt.getServer();
        Vec3d tntPos = tnt.getPos().add(0,1,0);
        World world = tnt.getWorld();
        int tntdupnumber = server.getGameRules().getInt(CustomGameRules.TNTDUPS);
        if (tntdupnumber != 0) {
            // Use the shadowed 'level' field to pass the world to TNTSpawner.spawnTntRing
            TNTSpawner.spawnTntRing(world, tntPos, 1, tntdupnumber, 80);
            // Optionally cancel the explosion:
            // ci.cancel();
        }
    }
}
