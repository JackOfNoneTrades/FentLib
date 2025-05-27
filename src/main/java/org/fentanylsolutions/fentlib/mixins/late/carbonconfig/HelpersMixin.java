package org.fentanylsolutions.fentlib.mixins.late.carbonconfig;

import static carbonconfiglib.utils.Helpers.generateIndent;

import java.util.List;
import java.util.StringJoiner;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import carbonconfiglib.utils.Helpers;

@SuppressWarnings("unused")
@Mixin(Helpers.class)
public class HelpersMixin {

    /**
     * @author jack
     * @reason Fixes array serialization
     */
    @Overwrite(remap = false)
    public static String[] scanForElements(CharSequence text, char splitter, char opener, char closer) {
        String str = text.toString()
            .trim();
        if (str.startsWith("[") && str.endsWith("]")) {
            str = str.substring(1, str.length() - 1);
        }

        String[] parts = str.split(Character.toString(splitter));
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim();
        }
        return parts;
    }

    /**
     * @author jack
     * @reason Fixes array serialization
     */
    @Overwrite(remap = false)
    public static String mergeCompoundArray(List<String> list, boolean newLine, int indentLevel) {
        String indent = newLine ? generateIndent(indentLevel + 1) : "";
        String delimiter = newLine ? ",\n" : ", ";
        StringJoiner joiner = new StringJoiner(delimiter);

        for (String item : list) {
            joiner.add(indent + item);
        }

        return joiner.toString();
    }
}
