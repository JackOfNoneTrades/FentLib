package org.fentanylsolutions.fentlib.mixininterfaces;

import org.fentanylsolutions.fentlib.util.GifUtil;

public interface IAnimatedServerData {

    boolean getIsAnimatedIcon();

    void setIsAnimatedIcon(boolean val);

    GifUtil.GifAnimationData getGifAnimationData();

    void setGifAnimationData(GifUtil.GifAnimationData data);

}
