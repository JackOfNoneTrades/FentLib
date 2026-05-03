package org.fentanylsolutions.fentlib.util.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.util.ResourceLocation;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class SoundUtil {

    private static final float VANILLA_SOUND_DISTANCE = 16.0F;

    private SoundUtil() {}

    public static MaxDistanceSound withMaxDistance(ISound sound, float maxDistance) {
        return new MaxDistanceSound(sound, maxDistance);
    }

    public static MaxDistanceSound playWithMaxDistance(ISound sound, float maxDistance) {
        return playWithMaxDistance(
            Minecraft.getMinecraft()
                .getSoundHandler(),
            sound,
            maxDistance);
    }

    public static MaxDistanceSound playWithMaxDistance(SoundHandler soundHandler, ISound sound, float maxDistance) {
        MaxDistanceSound wrapped = withMaxDistance(sound, maxDistance);
        soundHandler.playSound(wrapped);
        return wrapped;
    }

    public static MaxDistanceSound playAt(ResourceLocation soundResource, double x, double y, double z, float volume,
        float pitch, float maxDistance) {

        PositionedSoundRecord sound = new PositionedSoundRecord(
            soundResource,
            volume,
            pitch,
            (float) x,
            (float) y,
            (float) z);
        return playWithMaxDistance(sound, maxDistance);
    }

    public static float getEffectiveMaxDistance(ISound sound, float vanillaMaxDistance) {
        if (sound instanceof ICustomMaxDistanceSound) {
            float customMaxDistance = ((ICustomMaxDistanceSound) sound).getMaxSoundDistance();
            if (customMaxDistance > 0.0F) {
                return customMaxDistance;
            }
        }

        return vanillaMaxDistance;
    }

    public static float getVanillaMaxDistance(ISound sound) {
        if (sound == null) {
            throw new IllegalArgumentException("sound must not be null");
        }
        return getVanillaMaxDistance(sound.getVolume());
    }

    public static float getVanillaMaxDistance(float volume) {
        return volume > 1.0F ? VANILLA_SOUND_DISTANCE * volume : VANILLA_SOUND_DISTANCE;
    }
}
