package org.fentanylsolutions.fentlib.util.sound;

/**
 * Implement this on a client {@code ISound} to make its mono OpenAL source
 * behave like an emitter with physical width instead of a dimensionless point.
 */
public interface ICustomSourceRadiusSound {

    /**
     * @return apparent source radius in blocks. Return {@code <= 0} to keep
     *         point-source spatialization.
     */
    float getSoundSourceRadius();
}
