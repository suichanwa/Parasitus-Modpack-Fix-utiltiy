package com.toomda.parasitusfix.techguns;

import com.toomda.parasitusfix.ParasitusFix;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.HashSet;
import java.util.Set;

@Mod.EventBusSubscriber(modid = ParasitusFix.MODID, value = Side.CLIENT)
public class TechgunsBunkerDoorSoundSuppressor {
    private static final ResourceLocation BUNKER_DOOR_ID = new ResourceLocation("techguns", "bunkerdoor");
    private static final ResourceLocation PF_OPEN = new ResourceLocation(ParasitusFix.MODID, "bunkerdoor_open");
    private static final ResourceLocation PF_CLOSE = new ResourceLocation(ParasitusFix.MODID, "bunkerdoor_close");
    private static final Set<ResourceLocation> DOOR_SOUNDS = new HashSet<>();

    static {
        DOOR_SOUNDS.add(new ResourceLocation("techguns", "blocks.metaldooropen"));
        DOOR_SOUNDS.add(new ResourceLocation("minecraft", "block.wooden_door.open"));
        DOOR_SOUNDS.add(new ResourceLocation("minecraft", "block.wooden_door.close"));
        DOOR_SOUNDS.add(new ResourceLocation("minecraft", "block.iron_door.open"));
        DOOR_SOUNDS.add(new ResourceLocation("minecraft", "block.iron_door.close"));
    }

    @SubscribeEvent
    public static void onPlaySound(PlaySoundEvent event) {
        ISound sound = event.getSound();
        if (sound == null) return;
        ResourceLocation soundId = sound.getSoundLocation();
        if (soundId == null || !DOOR_SOUNDS.contains(soundId)) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.world == null) return;

        BlockPos pos = new BlockPos(sound.getXPosF(), sound.getYPosF(), sound.getZPosF());
        IBlockState state = findBunkerDoorState(mc.world, pos);
        if (state == null) return;

        Block block = state.getBlock();
        ResourceLocation id = Block.REGISTRY.getNameForObject(block);
        if (!BUNKER_DOOR_ID.equals(id)) return;

        SoundEvent open = ForgeRegistries.SOUND_EVENTS.getValue(PF_OPEN);
        SoundEvent close = ForgeRegistries.SOUND_EVENTS.getValue(PF_CLOSE);
        if (open == null || close == null) return;

        boolean isOpen = state.getValue(BlockDoor.OPEN);
        float volume = 1.0f;
        float pitch = 1.0f;
        try {
            volume = sound.getVolume();
            pitch = sound.getPitch();
        } catch (Throwable ignored) {
        }

        SoundEvent replacement = isOpen ? close : open;
        event.setResultSound(new PositionedSoundRecord(replacement, SoundCategory.BLOCKS, volume, pitch, pos));
    }

    private static IBlockState findBunkerDoorState(World world, BlockPos pos) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    BlockPos p = pos.add(dx, dy, dz);
                    IBlockState state = world.getBlockState(p);
                    if (!(state.getBlock() instanceof BlockDoor)) continue;

                    if (state.getValue(BlockDoor.HALF) == BlockDoor.EnumDoorHalf.UPPER) {
                        BlockPos down = p.down();
                        IBlockState lower = world.getBlockState(down);
                        if (lower.getBlock() instanceof BlockDoor) {
                            return lower;
                        }
                    } else {
                        return state;
                    }
                }
            }
        }
        return null;
    }
}
