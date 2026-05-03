package org.fentanylsolutions.fentlib.util.sound;

/**
 * Implement this on a client {@code ISound} to override Minecraft's positional
 * sound fade distance without raising the sound's volume.
 */
public interface ICustomMaxDistanceSound {

    /**
     * @return max attenuation distance in blocks. Return {@code <= 0} to keep
     *         vanilla behavior.
     */
    float getMaxSoundDistance();
}
