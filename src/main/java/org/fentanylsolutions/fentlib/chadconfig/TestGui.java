package org.fentanylsolutions.fentlib.chadconfig;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;

public class TestGui {
    public static ModularScreen createGUI() {
        ModularPanel panel = ModularPanel.defaultPanel("tutorial_panel");
        panel.child(IKey.str("My first screen").asWidget()
            .top(7).left(7));
        return new ModularScreen(panel);
    }
}
