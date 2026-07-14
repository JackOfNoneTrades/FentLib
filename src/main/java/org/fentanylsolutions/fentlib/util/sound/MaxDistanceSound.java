package org.fentanylsolutions.fentlib.util.sound;

import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.ITickableSound;
import net.minecraft.util.ResourceLocation;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Small delegating wrapper for sounds that need a custom max attenuation
 * distance.
 */
@SideOnly(Side.CLIENT)
public class MaxDistanceSound implements ITickableSound, ICustomMaxDistanceSound, ICustomSourceRadiusSound {

    private final ISound delegate;
    private final ITickableSound tickableDelegate;
    private final float maxDistance;

    public MaxDistanceSound(ISound delegate, float maxDistance) {
        if (delegate == null) {
            throw new IllegalArgumentException("delegate must not be null");
        }

        this.delegate = delegate;
        this.tickableDelegate = delegate instanceof ITickableSound ? (ITickableSound) delegate : null;
        this.maxDistance = maxDistance;
    }

    public ISound getDelegate() {
        return this.delegate;
    }

    @Override
    public float getMaxSoundDistance() {
        return this.maxDistance;
    }

    @Override
    public float getSoundSourceRadius() {
        return SoundUtil.getSourceRadius(this.delegate);
    }

    @Override
    public ResourceLocation getPositionedSoundLocation() {
        return this.delegate.getPositionedSoundLocation();
    }

    @Override
    public boolean canRepeat() {
        return this.delegate.canRepeat();
    }

    @Override
    public int getRepeatDelay() {
        return this.delegate.getRepeatDelay();
    }

    @Override
    public float getVolume() {
        return this.delegate.getVolume();
    }

    @Override
    public float getPitch() {
        return this.delegate.getPitch();
    }

    @Override
    public float getXPosF() {
        return this.delegate.getXPosF();
    }

    @Override
    public float getYPosF() {
        return this.delegate.getYPosF();
    }

    @Override
    public float getZPosF() {
        return this.delegate.getZPosF();
    }

    @Override
    public ISound.AttenuationType getAttenuationType() {
        return this.delegate.getAttenuationType();
    }

    @Override
    public void update() {
        if (this.tickableDelegate != null) {
            this.tickableDelegate.update();
        }
    }

    @Override
    public boolean isDonePlaying() {
        return this.tickableDelegate != null && this.tickableDelegate.isDonePlaying();
    }
}
