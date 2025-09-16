package org.fentanylsolutions.fentlib.mixininterfaces;

import org.fentanylsolutions.fentlib.util.GifUtil;

public interface IAnimatedServerData {

    boolean getIsAnimatedIcon();

    void setIsAnimatedIcon(boolean val);

    GifUtil.StitchedAnimationData getStitchedAnimationData();

    void setStitchedAnimationData(GifUtil.StitchedAnimationData data);

}
