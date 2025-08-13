package com.vsdguzman;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.lang.reflect.Field;
import java.lang.reflect.Method;


public class command implements ModInitializer {


    @Override
    public void onInitialize() {

    }

    public static void registerCommands() {
        // Register the command using Fabric's v2 command registration callback.
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated, environment) -> registerNukeCommand(dispatcher));
        Basmod.LOGGER.info("nuke command registered");

        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated, environment) -> registerStabCommand(dispatcher));
        Basmod.LOGGER.info("stab command registered");
    }

    private static void registerStabCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("stab")
                        // Restrict command usage to ops (permission level 2 or higher)
                        .requires(source -> source.hasPermissionLevel(2))
                        // Require an integer argument "depth" (0 to 128)
                        .then(CommandManager.argument("depth", IntegerArgumentType.integer(0, 1024))
                                // If only depth is provided, default to the player's position.
                                .executes(context -> {
                                    int depth = IntegerArgumentType.getInteger(context, "depth");
                                    ServerCommandSource source = context.getSource();
                                    World world = source.getWorld();
                                    PlayerEntity Player = source.getPlayer();

                                    assert Player != null;
                                    Vec3d spawnPos = Player.getPos();

                                    TNTSpawner.stabTNT(world, spawnPos, depth);
                                    return 1;
                                })
                                // Optionally allow a Vec3 argument named "position" for custom spawn location.
                                .then(CommandManager.argument("position", Vec3ArgumentType.vec3())
                                        .executes(context -> {
                                            int depth = IntegerArgumentType.getInteger(context, "depth");
                                            ServerCommandSource source = context.getSource();
                                            World world = source.getWorld();

                                            // Retrieve the Vec3d position from the command context.
                                            Vec3d spawnPos = Vec3ArgumentType.getVec3(context, "position").add(0, 1, 0);
                                            TNTSpawner.stabTNT(world, spawnPos, depth);
                                            return 1;
                                        })
                                )
                        )
        );
    }


    private static void registerNukeCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("nuke")
                        .requires(source -> source.hasPermissionLevel(2))
                        // Require the "size" argument.
                        .then(CommandManager.argument("size", IntegerArgumentType.integer(0, 1024))
                                // Optionally accept a "position" argument as a BlockPos.
                                .then(CommandManager.argument("position", Vec3ArgumentType.vec3())
                                        .then(CommandManager.argument("type", StringArgumentType.string())
                                                .suggests(new SuggestionsProvider())


                                                .executes(context -> {
                                                    int size = IntegerArgumentType.getInteger(context, "size");
                                                    Vec3d posProvided = Vec3ArgumentType.getVec3(context, "position");
                                                    String type = StringArgumentType.getString(context, "type");
                                                    ServerCommandSource source = context.getSource();
                                                    World world = source.getWorld();
                                                    Vec3d spawnPos = posProvided.add(0, 1, 0);
                                                    MakeNuke(source, world, spawnPos, size, type);
                                                    return 1;
                                                })
                                        )
                                        // If "centered" argument is not provided, use default (spiral = false).
                                        .executes(context -> {
                                            int size = IntegerArgumentType.getInteger(context, "size");
                                            Vec3d posProvided = Vec3ArgumentType.getVec3(context, "position");
                                            ServerCommandSource source = context.getSource();
                                            World world = source.getWorld();
                                            Vec3d spawnPos = posProvided.add(0, 1, 0);
                                            MakeNuke(source, world, spawnPos, size, "");
                                            return 1;
                                        })
                                )
                                // If the "position" argument is not provided, default to the player's position.
                                .executes(context -> {
                                    int size = IntegerArgumentType.getInteger(context, "size");
                                    ServerCommandSource source = context.getSource();
                                    World world = source.getWorld();
                                    BlockPos playerpos = source.getPlayer().getBlockPos().up();
                                    Vec3d spawnPos = new Vec3d(playerpos.getX(), playerpos.getY(), playerpos.getZ());

                                    MakeNuke(source, world, spawnPos, size, "");
                                    return 1;
                                })
                        )
        );
    }

    private static void make8tnt(World world, Vec3d center, int fuse) {
        for (int i = 0; i < 8; i++) {
            TNTSpawner.spawnTnt(world, center, fuse);
        }
        return;
    }


    private static void MakeNuke(ServerCommandSource source, World world, Vec3d center, int size, String type) {
        int ArrowTntCount = (int) Math.floor(75 * Math.log(size + 1));
        switch (type) {
            case "Spiral": {
                TNTSpawner.spawnSpiralTnt(world, center, size);
                break;
            }
            case "Arrow": {
                ARROWSpawner.SpawnArrow(world,center);
                for (int i = 1; i < size+1; i++) {
                    ARROWSpawner.spawnARROWRing(world,center,i*0.01,i*16, arrow -> {});
                }
                for (int i = 1; i < ArrowTntCount; i++) {
                    TNTSpawner.spawnTnt(world,center,10);
                }
                break;
                }
            case "OrbitalVersion": {
                for (int i = size+1; i >= 1; i--) {

                    TNTSpawner.spawnTntRing(world,center,0.002,i*16, 30+i);
                    TNTSpawner.spawnTnt(world,center,21);


                }
                TNTSpawner.spawnTnt(world,center,30);
                for (int i = 1; i < 25; i++) {
                    TNTSpawner.spawnTnt(world,center,20);
                }
                break;
            }
            case "BunkerBuster":{
                for (int i = 1; i < size+1; i++) {
                    TNTSpawner.spawnTntRing(world,center,0.0001,i*32, 80+i);
                    make8tnt(world,center,41);
                }
                for (int i = 1; i < 75; i++) {
                    TNTSpawner.spawnTnt(world,center,40);
                }
                for (int x = 0; x < 8; x++) {
                    TNTSpawner.spawnTnt(world, center, x+32);
                }
                break;
            }
            case "TunnelDigger":{
                for (int i = size; i > 0; i--) {
                    TNTSpawner.spawnTntRing(world, center, 0.01, i * 16, 20);
                    make8tnt(world, center, 19);
                }
                break;
            }
            case "RailGun":{

                double offset =  0.0625;

                ARROWSpawner.spawnARROWRing(world,center.subtract(0,offset,0),0.5, (int) (size*(Math.floor(size/3.5)+1)*16), arrow -> {
                    try {
                        Method setPierceLevel = PersistentProjectileEntity.class.getDeclaredMethod("setPierceLevel", byte.class);
                        setPierceLevel.setAccessible(true);
                        setPierceLevel.invoke(arrow, (byte) 15);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                        });
                for (int i = 1; i < ArrowTntCount + new int[]{2, 1, 0}[ArrowTntCount % 3]; i++) {
                    TNTSpawner.spawnTnt(world,center.add(0,0.0625D,0),1).setNoGravity(true);
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

        return;
    }
}




