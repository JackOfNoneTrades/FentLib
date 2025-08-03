package org.fentanylsolutions.fentlib.configmaxxing.configpickers;

import java.util.ArrayList;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;

public class SearchBox extends GuiTextField {

    CommonSelectionGui parent;
    public String searchText;

    public SearchBox(FontRenderer fontRenderer, int xpos, int ypos, int width, int height, CommonSelectionGui parent) {
        super(fontRenderer, xpos, ypos, width, height);
        this.parent = parent;
        this.searchText = "Search ...";
        this.setText(this.searchText);
    }

    @Override
    public void mouseClicked(int xpos, int ypos, int button) {
        super.mouseClicked(xpos, ypos, button);

        /* Removing the initial text from the box if clicked */
        if (xpos >= this.xPosition && xpos <= this.xPosition + this.width
            && ypos >= this.yPosition
            && ypos <= this.yPosition + this.height) {
            clear();
        }
    }

    /* This is a separate function so CommonSelectionGui can conveniently use it */
    public void clear() {
        if (this.getText()
            .equals(this.searchText)) {
            this.setText("");
        }
    }

    @Override
    public boolean textboxKeyTyped(char eventChar, int eventKey) {
        boolean res = super.textboxKeyTyped(eventChar, eventKey);

        /* Filtering elements based on search box text */
        /* if the super func returns true, it probably means the user has successfully changed the text in the box */
        if (res) {
            /* this boolean tracks whether something got added or removed */
            boolean changed = false;

            /* This array stores list entries that will be added or removed */
            ArrayList<ListEntry> toModify = new ArrayList<>();

            /* handling removal */
            for (ListEntry le : parent.availableEntries) {
                String displayString = le.getDisplayString()
                    .toLowerCase();
                String text = getText().toLowerCase();
                if (!displayString.startsWith(text)) {
                    // parent.availableEntries.remove(le);
                    toModify.add(le);
                }
            }
            for (ListEntry le : toModify) {
                le.hide();
            }
            changed = toModify.size() > 0;

            /* readding all "hidden" entries that match current search */
            toModify = new ArrayList<>();
            for (ListEntry le : parent.hiddenList) {
                String displayString = le.getDisplayString()
                    .toLowerCase();
                String text = getText().toLowerCase();
                if (displayString.startsWith(text)) {
                    toModify.add(le);
                }
            }
            for (ListEntry le : toModify) {
                le.show();
            }

            /* if something moved, we select the first list entry */
            changed = changed || toModify.size() > 0;
            if (changed) {
                parent.availableEntriesListGui.selectEntry(0, true);
            }
        }

        return res;
    }
}
