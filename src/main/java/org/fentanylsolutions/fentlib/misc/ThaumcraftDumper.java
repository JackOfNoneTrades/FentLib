package org.fentanylsolutions.fentlib.misc;

// import codechicken.nei.ItemList;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import org.fentanylsolutions.fentlib.FentLib;
import org.fentanylsolutions.fentlib.Tags;
import org.fentanylsolutions.fentlib.util.FileUtil;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL14;

import cpw.mods.fml.common.registry.GameRegistry;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.CrucibleRecipe;
import thaumcraft.api.crafting.IArcaneRecipe;
import thaumcraft.api.crafting.InfusionEnchantmentRecipe;
import thaumcraft.api.crafting.InfusionRecipe;
import thaumcraft.api.crafting.ShapedArcaneRecipe;
import thaumcraft.api.crafting.ShapelessArcaneRecipe;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ResearchCategoryList;
import thaumcraft.api.research.ResearchItem;
import thaumcraft.api.research.ResearchPage;
import thaumcraft.common.lib.utils.InventoryUtils;

public class ThaumcraftDumper {

    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final RenderItem renderItem = new RenderItem();
    private static final String OUTPUT_FOLDER_NAME = "thaumonomicon_dump";

    public static BufferedImage renderItemToImage(ItemStack stack, int size) {
        Minecraft mc = Minecraft.getMinecraft();
        RenderItem renderItem = RenderItem.getInstance();
        FontRenderer font = mc.fontRenderer;
        TextureManager tex = mc.getTextureManager();

        if (size <= 0) size = 32;

        // === Create FBO ===
        int fbo = EXTFramebufferObject.glGenFramebuffersEXT();
        EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, fbo);

        // Color texture
        int texId = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texId);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexImage2D(
            GL11.GL_TEXTURE_2D,
            0,
            GL11.GL_RGBA8,
            size,
            size,
            0,
            GL11.GL_RGBA,
            GL11.GL_UNSIGNED_BYTE,
            (ByteBuffer) null);

        EXTFramebufferObject.glFramebufferTexture2DEXT(
            EXTFramebufferObject.GL_FRAMEBUFFER_EXT,
            EXTFramebufferObject.GL_COLOR_ATTACHMENT0_EXT,
            GL11.GL_TEXTURE_2D,
            texId,
            0);

        // Depth buffer
        int depthRB = EXTFramebufferObject.glGenRenderbuffersEXT();
        EXTFramebufferObject.glBindRenderbufferEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, depthRB);

        EXTFramebufferObject
            .glRenderbufferStorageEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, GL14.GL_DEPTH_COMPONENT24, size, size);

        EXTFramebufferObject.glFramebufferRenderbufferEXT(
            EXTFramebufferObject.GL_FRAMEBUFFER_EXT,
            EXTFramebufferObject.GL_DEPTH_ATTACHMENT_EXT,
            EXTFramebufferObject.GL_RENDERBUFFER_EXT,
            depthRB);

        // === Setup rendering ===
        GL11.glViewport(0, 0, size, size);

        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glOrtho(0, size, size, 0, 1000, 3000);

        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glTranslatef(0, 0, -2000);

        GL11.glClearColor(0f, 0f, 0f, 0f);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        // Bind item atlas
        tex.bindTexture(TextureMap.locationItemsTexture);

        RenderHelper.enableGUIStandardItemLighting();

        // scale to fill image
        float scale = size / 16f;
        GL11.glPushMatrix();
        GL11.glScalef(scale, scale, 1f);

        renderItem.renderItemIntoGUI(font, tex, stack, 0, 0);

        GL11.glPopMatrix();
        RenderHelper.disableStandardItemLighting();

        // === Read pixels ===
        ByteBuffer buf = BufferUtils.createByteBuffer(size * size * 4);
        GL11.glReadPixels(0, 0, size, size, GL12.GL_BGRA, GL11.GL_UNSIGNED_BYTE, buf);

        // === Restore ===
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPopMatrix();
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPopMatrix();

        EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, 0);

        // === Convert to image ===
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                int i = (x + y * size) * 4;
                int b = buf.get(i) & 0xff;
                int g = buf.get(i + 1) & 0xff;
                int r = buf.get(i + 2) & 0xff;
                int a = buf.get(i + 3) & 0xff;
                img.setRGB(x, size - y - 1, (a << 24) | (r << 16) | (g << 8) | b);
            }
        }

        // cleanup
        GL11.glDeleteTextures(texId);
        EXTFramebufferObject.glDeleteRenderbuffersEXT(depthRB);
        EXTFramebufferObject.glDeleteFramebuffersEXT(fbo);

        return img;
    }

    public static BufferedImage renderItemStackToImage(ItemStack stack, int size) {
        // Create OpenGL FBO
        int fbo = EXTFramebufferObject.glGenFramebuffersEXT();
        int tex = GL11.glGenTextures();

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, tex);
        GL11.glTexImage2D(
            GL11.GL_TEXTURE_2D,
            0,
            GL11.GL_RGBA8,
            size,
            size,
            0,
            GL11.GL_RGBA,
            GL11.GL_UNSIGNED_BYTE,
            (ByteBuffer) null);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

        EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, fbo);
        EXTFramebufferObject.glFramebufferTexture2DEXT(
            EXTFramebufferObject.GL_FRAMEBUFFER_EXT,
            EXTFramebufferObject.GL_COLOR_ATTACHMENT0_EXT,
            GL11.GL_TEXTURE_2D,
            tex,
            0);

        // Set up transparent background
        GL11.glViewport(0, 0, size, size);
        GL11.glClearColor(0f, 0f, 0f, 0f);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        GL11.glPushMatrix();
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glOrtho(0, size, size, 0, 1000, 3000);

        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glTranslatef(0, 0, -2000);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        RenderHelper.enableGUIStandardItemLighting();

        // Scale the item to fit the requested size
        float scale = size / 16f;
        GL11.glPushMatrix();
        GL11.glScalef(scale, scale, 1f);
        renderItem.renderItemAndEffectIntoGUI(mc.fontRenderer, mc.renderEngine, stack, 0, 0);
        GL11.glPopMatrix();

        RenderHelper.disableStandardItemLighting();

        GL11.glPopMatrix();
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPopMatrix();
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPopMatrix();

        // Read pixels from FBO
        ByteBuffer buffer = BufferUtils.createByteBuffer(size * size * 4);
        GL11.glReadPixels(0, 0, size, size, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

        // Cleanup FBO
        EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, 0);
        EXTFramebufferObject.glDeleteFramebuffersEXT(fbo);
        GL11.glDeleteTextures(tex);

        // Convert to BufferedImage
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                int i = (x + (size - y - 1) * size) * 4;
                int r = buffer.get(i) & 0xFF;
                int g = buffer.get(i + 1) & 0xFF;
                int b = buffer.get(i + 2) & 0xFF;
                int a = buffer.get(i + 3) & 0xFF;
                image.setRGB(x, y, (a << 24) | (r << 16) | (g << 8) | b);
            }
        }

        return image;
    }

    public static void saveIcon(BufferedImage img, ItemStack stack) {
        try {
            String id = GameRegistry.findUniqueIdentifierFor(stack.getItem())
                .toString();
            int meta = stack.getItemDamage();

            File out = new File("icon_dump/" + id + "_" + meta + ".png");
            out.getParentFile()
                .mkdirs();

            ImageIO.write(img, "png", out);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * public static void dumpAllNEIIcons() {
     * for (ItemStack s : ItemList.items) {
     * BufferedImage img;
     * IItemRenderer tesrRenderer = MinecraftForgeClient.getItemRenderer(s, IItemRenderer.ItemRenderType.ENTITY);
     * if (tesrRenderer != null) {
     * img = renderItemStackToImage(s, 256);
     * } else {
     * img = renderItemToImage(s, 32);
     * }
     * saveIcon(img, s);
     * }
     * }
     */

    private static String convertFormattingOld(String text) {
        if (text == null) return "";
        // Basic replacements
        text = text.replace("<LINE>", "<br>")
            .replace("<BR>", "<br>");

        // Bold
        text = text.replace("§l", "<b>")
            .replace("§r", "</b>");

        // Colors
        text = text.replace("§0", "<span style='color:#d4c5a9'>")
            .replace("§1", "<span style='color:#0000AA'>")
            .replace("§2", "<span style='color:#00AA00'>")
            .replace("§3", "<span style='color:#00AAAA'>")
            .replace("§4", "<span style='color:#AA0000'>")
            .replace("§5", "<span style='color:#AA00AA'>")
            .replace("§6", "<span style='color:#FFAA00'>")
            .replace("§7", "<span style='color:#AAAAAA'>")
            .replace("§8", "<span style='color:#555555'>")
            .replace("§9", "<span style='color:#5555FF'>")
            .replace("§a", "<span style='color:#55FF55'>")
            .replace("§b", "<span style='color:#55FFFF'>")
            .replace("§c", "<span style='color:#FF5555'>")
            .replace("§d", "<span style='color:#FF55FF'>")
            .replace("§e", "<span style='color:#FFFF55'>")
            .replace("§f", "<span style='color:#FFFFFF'>");

        // Close all spans at end (simple approach)
        text += "</span>";

        return text;
    }

    private static String convertFormattingOOOLD(String text) {
        if (text == null) return "";
        // Basic replacements
        text = text.replace("<LINE>", "<br>")
            .replace("<BR>", "<br>");

        // Bold
        text = text.replace("§l", "<b>")
            .replace("§r", "</b>");

        // Colors - adjusted to fit the warm, parchment theme
        text = text.replace("§0", "<span style='color:#d4c5a9'>") // Black -> Default parchment
            .replace("§1", "<span style='color:#6b8bb5'>") // Dark Blue -> Muted blue
            .replace("§2", "<span style='color:#7a9b6a'>") // Dark Green -> Sage green
            .replace("§3", "<span style='color:#6b9b9b'>") // Dark Aqua -> Teal
            .replace("§4", "<span style='color:#b85a50'>") // Dark Red -> Brick red
            .replace("§5", "<span style='color:#9b6b8b'>") // Dark Purple -> Dusty purple
            .replace("§6", "<span style='color:#d4a574'>") // Gold -> Warm gold (matches recipe titles)
            .replace("§7", "<span style='color:#a0907a'>") // Gray -> Warm gray
            .replace("§8", "<span style='color:#6b5d54'>") // Dark Gray -> Brown gray
            .replace("§9", "<span style='color:#8ba3c9'>") // Blue -> Sky blue
            .replace("§a", "<span style='color:#9bbb7a'>") // Green -> Light sage
            .replace("§b", "<span style='color:#7abbb5'>") // Aqua -> Light teal
            .replace("§c", "<span style='color:#d47a6a'>") // Red -> Warm red
            .replace("§d", "<span style='color:#c98bb5'>") // Light Purple -> Rose
            .replace("§e", "<span style='color:#e8d89a'>") // Yellow -> Pale gold
            .replace("§f", "<span style='color:#f0e8d8'>"); // White -> Cream white

        // Close all spans at end (simple approach)
        text += "</span>";

        return text;
    }

    private static String convertFormatting(String text) {
        if (text == null) return "";
        // Basic replacements
        text = text.replace("<LINE>", "<br>")
            .replace("<BR>", "<br>");

        // Track open tags to close them properly
        java.util.Stack<String> openTags = new java.util.Stack<>();
        StringBuilder result = new StringBuilder();

        // Process character by character
        for (int i = 0; i < text.length(); i++) {
            if (i < text.length() - 1 && text.charAt(i) == '§') {
                char code = text.charAt(i + 1);
                i++; // Skip the next character

                // Close previous formatting if reset or new color
                if (code == 'r' || (code >= '0' && code <= '9') || (code >= 'a' && code <= 'f')) {
                    // Close all open tags
                    while (!openTags.isEmpty()) {
                        result.append(openTags.pop());
                    }
                }

                // Handle formatting codes
                switch (code) {
                    // Colors (using your pastel theme)
                    case '0':
                        result.append("<span style='color:#d4c5a9'>");
                        openTags.push("</span>");
                        break; // Black -> Default parchment
                    case '1':
                        result.append("<span style='color:#6b8bb5'>");
                        openTags.push("</span>");
                        break; // Dark Blue -> Muted blue
                    case '2':
                        result.append("<span style='color:#7a9b6a'>");
                        openTags.push("</span>");
                        break; // Dark Green -> Sage green
                    case '3':
                        result.append("<span style='color:#6b9b9b'>");
                        openTags.push("</span>");
                        break; // Dark Aqua -> Teal
                    case '4':
                        result.append("<span style='color:#b85a50'>");
                        openTags.push("</span>");
                        break; // Dark Red -> Brick red
                    case '5':
                        result.append("<span style='color:#9b6b8b'>");
                        openTags.push("</span>");
                        break; // Dark Purple -> Dusty purple
                    case '6':
                        result.append("<span style='color:#d4a574'>");
                        openTags.push("</span>");
                        break; // Gold -> Warm gold
                    case '7':
                        result.append("<span style='color:#a0907a'>");
                        openTags.push("</span>");
                        break; // Gray -> Warm gray
                    case '8':
                        result.append("<span style='color:#6b5d54'>");
                        openTags.push("</span>");
                        break; // Dark Gray -> Brown gray
                    case '9':
                        result.append("<span style='color:#8ba3c9'>");
                        openTags.push("</span>");
                        break; // Blue -> Sky blue
                    case 'a':
                        result.append("<span style='color:#9bbb7a'>");
                        openTags.push("</span>");
                        break; // Green -> Light sage
                    case 'b':
                        result.append("<span style='color:#7abbb5'>");
                        openTags.push("</span>");
                        break; // Aqua -> Light teal
                    case 'c':
                        result.append("<span style='color:#d47a6a'>");
                        openTags.push("</span>");
                        break; // Red -> Warm red
                    case 'd':
                        result.append("<span style='color:#c98bb5'>");
                        openTags.push("</span>");
                        break; // Light Purple -> Rose
                    case 'e':
                        result.append("<span style='color:#e8d89a'>");
                        openTags.push("</span>");
                        break; // Yellow -> Pale gold
                    case 'f':
                        result.append("<span style='color:#f0e8d8'>");
                        openTags.push("</span>");
                        break; // White -> Cream white

                    // Formatting
                    case 'k':
                        result.append("<span style='font-family: monospace;'>");
                        openTags.push("</span>");
                        break; // Obfuscated
                    case 'l':
                        result.append("<b>");
                        openTags.push("</b>");
                        break; // Bold
                    case 'm':
                        result.append("<s>");
                        openTags.push("</s>");
                        break; // Strikethrough
                    case 'n':
                        result.append("<u>");
                        openTags.push("</u>");
                        break; // Underline
                    case 'o':
                        result.append("<i>");
                        openTags.push("</i>");
                        break; // Italic
                    case 'r':
                        break; // Reset (already handled above)

                    default:
                        // Unknown code, keep it as-is
                        result.append('§')
                            .append(code);
                        break;
                }
            } else {
                result.append(text.charAt(i));
            }
        }

        // Close any remaining open tags
        while (!openTags.isEmpty()) {
            result.append(openTags.pop());
        }

        return result.toString();
    }

    private static String formatText(String text) {
        if (text == null) return "";

        text = convertFormatting(text);

        // Process image tags FIRST before escaping HTML
        text = processImageTags(text);

        // Then escape HTML (but not the img tags we just created)
        text = text.replace("<BR>", "<br>")
            .replaceAll("§[0-9a-fk-or]", ""); // Remove Minecraft color codes

        return text;
    }

    private static String processImageTags(String text) {
        Pattern imgPattern = Pattern.compile("<IMG>(.*?)</IMG>");
        Matcher matcher = imgPattern.matcher(text);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String imgData = matcher.group(1);
            String[] parts = imgData.split(":");

            if (parts.length >= 5) {
                String modid = parts[0];
                String texturePath = parts[1]; // e.g., "thaumcraft:textures/misc/research5.png"
                int texX = Integer.parseInt(parts[2]);
                int texY = Integer.parseInt(parts[3]);
                int texWidth = Integer.parseInt(parts[4]);
                int texHeight = Integer.parseInt(parts[5]);
                float scale = parts.length > 5 ? Float.parseFloat(parts[6]) : 1.0f;

                // Extract and save the image
                String savedImagePath = extractTextureRegion(
                    modid,
                    texturePath,
                    texX,
                    texY,
                    texWidth,
                    texHeight,
                    scale);

                String style = "display: block; margin: 10px auto; image-rendering: crisp-edges; min-width: 200px;";
                if (texturePath.contains("items")) {
                    style = "display: block; margin: 10px auto; image-rendering: crisp-edges; min-width: 60px;";
                }

                if (savedImagePath != null) {
                    String replacement = "<img src='" + savedImagePath + "' style=' " + style + " ' />";
                    matcher.appendReplacement(result, replacement);
                } else {
                    matcher.appendReplacement(result, "[Image: " + texturePath + "]");
                }
            } else {
                matcher.appendReplacement(result, "[Image: " + imgData + "]");
            }
        }
        matcher.appendTail(result);

        return result.toString();
    }

    private static String extractTextureRegion(String modid, String texturePath, int x, int y, int width, int height,
        float scale) {
        try {
            File outputDir = new File(Minecraft.getMinecraft().mcDataDir, OUTPUT_FOLDER_NAME);
            File imagesDir = new File(outputDir, "images");
            if (!imagesDir.exists()) imagesDir.mkdirs();

            // Convert resource location to file path
            ResourceLocation resourceLocation;
            if (texturePath.contains(":")) {
                String[] parts = texturePath.split(":", 2);
                resourceLocation = new ResourceLocation(parts[0], parts[1]);
            } else {
                resourceLocation = new ResourceLocation(modid + ":" + texturePath);
            }

            // Read the texture file directly from resources
            InputStream textureStream = null;
            try {
                textureStream = Minecraft.getMinecraft()
                    .getResourceManager()
                    .getResource(resourceLocation)
                    .getInputStream();
            } catch (Exception e) {
                System.err.println("Could not find texture: " + texturePath);
                return null;
            }

            // Load the full texture image
            BufferedImage fullImage = ImageIO.read(textureStream);
            textureStream.close();

            if (fullImage == null) {
                System.err.println("Could not read texture image: " + texturePath);
                return null;
            }

            // Validate coordinates
            if (x < 0 || y < 0) {// || x + width > fullImage.getWidth() || y + height > fullImage.getHeight()) {
                System.err.println(
                    "Invalid texture coordinates for " + texturePath
                        + ": x="
                        + x
                        + ", y="
                        + y
                        + ", w="
                        + width
                        + ", h="
                        + height
                        + " (texture size: "
                        + fullImage.getWidth()
                        + "x"
                        + fullImage.getHeight()
                        + ")");
                return null;
            }

            // Extract the region
            if (y == 255) {
                y = 0;
            }
            if (x == 255) {
                width = 0;
            }
            BufferedImage regionImage = fullImage
                .getSubimage(x, y, Math.min(width, fullImage.getWidth()), Math.min(height, fullImage.getHeight()));

            // Scale if needed
            scale = 1.0f;
            if (scale != 1.0f) {
                int scaledWidth = (int) (width * scale);
                int scaledHeight = (int) (height * scale);
                BufferedImage scaledImage = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_ARGB);

                java.awt.Graphics2D g2d = scaledImage.createGraphics();
                g2d.setRenderingHint(
                    java.awt.RenderingHints.KEY_INTERPOLATION,
                    java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                g2d.drawImage(regionImage, 0, 0, scaledWidth, scaledHeight, null);
                g2d.dispose();

                regionImage = scaledImage;
            }

            // Generate unique filename
            String filename = "img_" + texturePath
                .replaceAll("[^a-zA-Z0-9]", "_") + "_" + x + "_" + y + "_" + width + "_" + height + ".png";
            File imageFile = new File(imagesDir, filename);

            // Save the image
            ImageIO.write(regionImage, "PNG", imageFile);

            return "images/" + filename;

        } catch (Exception e) {
            System.err.println("Failed to extract texture region from " + texturePath + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private static String extractCategoryIcon(ResourceLocation iconRes, String categoryKey) {
        if (iconRes == null) return null;

        try {
            File outputDir = new File(Minecraft.getMinecraft().mcDataDir, OUTPUT_FOLDER_NAME);
            File categoryIconsDir = new File(outputDir, "category_icons");
            if (!categoryIconsDir.exists()) categoryIconsDir.mkdirs();

            String filename = "category_" + categoryKey + ".png";
            File iconFile = new File(categoryIconsDir, filename);

            if (iconFile.exists()) {
                return "category_icons/" + filename;
            }

            InputStream textureStream = Minecraft.getMinecraft()
                .getResourceManager()
                .getResource(iconRes)
                .getInputStream();
            BufferedImage iconImg = ImageIO.read(textureStream);
            textureStream.close();

            if (iconImg == null) return null;

            ImageIO.write(iconImg, "PNG", iconFile);
            return "category_icons/" + filename;

        } catch (Exception e) {
            System.err.println("Failed to extract category icon: " + e.getMessage());
            return null;
        }
    }

    private static String getResearchIcon(ResearchItem research) {
        if (research.icon_item != null) {
            return getItemIcon(research.icon_item);
        } else if (research.icon_resource != null) {
            try {
                File outputDir = new File(Minecraft.getMinecraft().mcDataDir, OUTPUT_FOLDER_NAME);
                File researchIconsDir = new File(outputDir, "research_icons");
                if (!researchIconsDir.exists()) researchIconsDir.mkdirs();

                String filename = "research_" + research.key + ".png";
                File iconFile = new File(researchIconsDir, filename);

                if (iconFile.exists()) {
                    return "research_icons/" + filename;
                }

                InputStream textureStream = Minecraft.getMinecraft()
                    .getResourceManager()
                    .getResource(research.icon_resource)
                    .getInputStream();
                BufferedImage iconImg = ImageIO.read(textureStream);
                textureStream.close();

                if (iconImg == null) return null;

                ImageIO.write(iconImg, "PNG", iconFile);
                return "research_icons/" + filename;

            } catch (Exception e) {
                System.err.println("Failed to extract research icon for " + research.key + ": " + e.getMessage());
                return null;
            }
        }
        return null;
    }

    public static void dumpThaumonomicon(String customMessage) {
        File outputDir = new File(Minecraft.getMinecraft().mcDataDir, OUTPUT_FOLDER_NAME);

        FileUtil.deleteDirectory(outputDir);
        FileUtil.createFolderIfNotExists(outputDir);
        File htmlFile = new File(outputDir, "index.html");

        try (PrintWriter writer = new PrintWriter(new FileWriter(htmlFile))) {
            writer.println("<!DOCTYPE html>");
            writer.println("<html>");
            writer.println("<head>");
            writer.println("<!--Dumped with FentLib version " + Tags.VERSION + "-->");
            writer.println("<meta charset='UTF-8'>");
            writer.println("<title>Thaumonomicon</title>");
            writer.println("<style>");
            writer.println(
                "body { font-family: 'Courier New', monospace; background-color: #1a1a1a; color: #d4c5a9; padding: 20px; }");
            writer.println("h1 { color: #8b4513; text-align: center; border-bottom: 3px solid #8b4513; }");
            writer.println("h2 { color: #9b7653; border-bottom: 2px solid #9b7653; }");
            writer.println("h3 { color: #a68b6a; }");
            writer.println(
                ".category { margin: 30px 0; padding: 20px; background-color: #2a2520; border: 2px solid #4a3520; }");
            writer.println(
                ".research-item { margin: 20px 0; padding: 15px; background-color: #332b24; border-left: 4px solid #6b5d54; }");
            writer.println(
                ".research-header { font-size: 1.2em; font-weight: bold; color: #c9a876; margin-bottom: 10px; }");
            writer.println(".research-text { color: #b0a090; font-style: italic; margin-bottom: 15px; }");
            writer.println(
                ".page { margin: 10px 0; padding: 10px; background-color: #3a3228; border: 1px solid #5a4a38; }");
            writer.println(".page-separator { border-top: 2px dashed #6b5d54; margin: 15px 0; }");
            writer.println(
                ".recipe { background-color: #443a30; padding: 10px; margin: 10px 0; border: 1px solid #6b5d54; }");
            writer.println(".recipe-title { font-weight: bold; color: #d4a574; margin-bottom: 5px; }");
            writer.println(
                ".aspect { display: inline-block; padding: 3px 8px; margin: 2px; background-color: #4a3a5a; color: #e0d0ff; border-radius: 3px; font-size: 0.9em; }");
            writer.println(
                ".aspect-display { display: inline-flex; align-items: center; gap: 6px; padding: 5px 10px; margin: 3px; background-color: rgba(74, 58, 90, 0.6); border: 1px solid rgba(107, 93, 116, 0.8); border-radius: 4px; font-size: 1em; vertical-align: middle; }");
            writer.println(".aspect-icon { width: 32px; height: 32px; image-rendering: crisp-edges; }");
            writer.println(".aspect-amount { font-weight: bold; margin-left: 4px; color: #ffffff; }");
            writer.println(".ingredient { margin: 5px 0; padding: 3px; color: #c9b896; }");
            writer.println(".special { color: #ff6600; font-weight: bold; }");
            writer.println(".hidden { color: #6b5d54; font-style: italic; }");
            writer.println(".concealed { color: #8b7d74; }");
            writer.println(".lost { color: #8b0000; }");
            writer.println(
                ".warp-warning { background-color: #3a1a1a; color: #ff6b6b; padding: 5px; border-left: 3px solid #8b0000; margin: 5px 0; }");
            writer.println(".parent-link { color: #6ba3d4; text-decoration: none; }");
            writer.println(".parent-link:hover { text-decoration: underline; }");
            writer
                .println(".image-caption { font-style: italic; color: #a0907a; text-align: center; margin-top: 5px; }");
            writer.println(".complexity { color: #d4a574; font-weight: bold; }");
            writer.println("ul { list-style-type: square; }");
            writer.println(
                ".crafting-grid { display: inline-grid; grid-template-columns: repeat(3, 40px); gap: 2px; background-color: #2a2520; padding: 5px; border: 2px solid #4a3520; margin: 10px 0; }");
            writer.println(
                ".crafting-slot { width: 40px; height: 40px; background-color: #3a3228; border: 1px solid #5a4a38; display: flex; align-items: center; justify-content: center; }");
            writer.println(
                ".crafting-arrow { font-size: 24px; color: #c9a876; margin: 0 10px; display: inline-block; vertical-align: middle; }");
            writer.println(
                ".crafting-result { display: inline-block; width: 50px; height: 50px; background-color: #443a30; border: 2px solid #6b5d54; display: inline-flex; align-items: center; justify-content: center; vertical-align: middle; }");
            writer.println(".item-cycler { position: relative; width: 32px; height: 32px; }");
            writer.println(".item-cycler img { position: absolute;}"); // top: 0; left: 0; }");
            writer.println(".item-clickable { cursor: pointer; }");
            writer.println(".item-clickable:hover { filter: brightness(1.3); }");
            writer.println(
                ".custom-tooltip { z-index: 9999999; position: fixed; background-color: #2a2520; border: 2px solid #6b5d54; padding: 8px 12px; pointer-events: none; display: none; color: #d4c5a9; font-size: 14px; box-shadow: 0 4px 8px rgba(0,0,0,0.5); max-width: 300px; }");
            writer.println(".custom-tooltip .tooltip-name { color: #c9a876; font-weight: bold; margin-bottom: 4px; }");
            writer.println(
                ".custom-tooltip .tooltip-hint { color: #8b7d74; font-style: italic; font-size: 12px; margin-top: 4px; }");
            writer.println(
                ".category-header { display: flex; align-items: center; gap: 10px; cursor: pointer; padding: 10px; background-color: #3a3228; border-radius: 5px; margin-bottom: 10px; }");
            writer.println(".category-header:hover { background-color: #4a4238; }");
            writer.println(".category-icon { width: 32px; height: 32px; image-rendering: crisp-edges; }");
            writer.println(".category-content { display: none; }");
            writer.println(".category-content.expanded { display: block; }");
            writer.println(
                ".research-header-wrapper { display: flex; align-items: center; gap: 10px; cursor: pointer; }");
            writer.println(".research-header-wrapper:hover { background-color: #3a3228; }");
            writer
                .println(".research-icon { width: 24px; height: 24px; image-rendering: crisp-edges; flex-shrink: 0; }");
            writer.println(".research-content { display: none; margin-top: 10px; }");
            writer.println(".research-content.expanded { display: block; }");
            writer.println(
                ".collapse-arrow { color: #c9a876; font-weight: bold; font-size: 1.2em; transition: transform 0.2s; }");
            writer.println(".collapse-arrow.expanded { transform: rotate(90deg); }");

            writer.println(
                ".search-container { margin: 20px 0; padding: 15px; background-color: #2a2520; border: 2px solid #4a3520; border-radius: 5px; }");
            // writer.println(".search-input { width: 100%; padding: 10px; font-family: 'Courier New', monospace;
            // font-size: 1em; background-color: #1a1a1a; color: #d4c5a9; border: 2px solid #6b5d54; border-radius: 3px;
            // box-sizing: border-box; }");
            writer.println(
                ".search-input { flex: 1; padding: 10px; font-family: 'Courier New', monospace; font-size: 1em; background-color: #1a1a1a; color: #d4c5a9; border: 2px solid #6b5d54; border-radius: 3px; box-sizing: border-box; }");
            writer.println(".search-input:focus { outline: none; border-color: #c9a876; }");
            writer.println(".search-results { margin-top: 10px; color: #a0907a; font-size: 0.9em; }");
            writer.println(
                ".highlight { background-color: #6b5d54; color: #fff; padding: 2px 4px; border-radius: 2px; }");
            writer.println(".hidden-by-search { display: none !important; }");

            writer.println(".search-controls { display: flex; gap: 10px; align-items: center; margin-bottom: 10px; }");
            writer.println(
                ".expand-button { padding: 10px 20px; font-family: 'Courier New', monospace; font-size: 1em; background-color: #4a3520; color: #d4c5a9; border: 2px solid #6b5d54; border-radius: 3px; cursor: pointer; transition: background-color 0.2s; }");
            writer.println(".expand-button:hover { background-color: #5a4530; }");

            writer.println("</style>");

            writer.println("<script>");
            writer.println("var cyclers = [];");
            writer.println("var recipeCyclers = [];");

            // Tooltip functionality
            writer.println("var tooltip = null;");
            writer.println("document.addEventListener('DOMContentLoaded', function() {");
            writer.println("  tooltip = document.createElement('div');");
            writer.println("  tooltip.className = 'custom-tooltip';");
            writer.println("  document.body.appendChild(tooltip);");
            writer.println("});");

            writer.println("function showTooltip(e, name, hint) {");
            writer.println("  if (!tooltip) return;");
            writer.println("  var html = '<div class=\"tooltip-name\">' + name + '</div>';");
            writer.println("  if (hint) {");
            writer.println("    html += '<div class=\"tooltip-hint\">' + hint + '</div>';");
            writer.println("  }");
            writer.println("  tooltip.innerHTML = html;");
            writer.println("  tooltip.style.display = 'block';");
            writer.println("  updateTooltipPosition(e);");
            writer.println("}");

            writer.println("function hideTooltip() {");
            writer.println("  if (tooltip) tooltip.style.display = 'none';");
            writer.println("}");

            writer.println("function updateTooltipPosition(e) {");
            writer.println("  if (!tooltip || tooltip.style.display === 'none') return;");
            writer.println("  var x = e.clientX + 15;");
            writer.println("  var y = e.clientY + 15;");
            writer.println("  var rect = tooltip.getBoundingClientRect();");
            writer.println("  if (x + rect.width > window.innerWidth) {");
            writer.println("    x = e.clientX - rect.width - 15;");
            writer.println("  }");
            writer.println("  if (y + rect.height > window.innerHeight) {");
            writer.println("    y = e.clientY - rect.height - 15;");
            writer.println("  }");
            writer.println("  tooltip.style.left = x + 'px';");
            writer.println("  tooltip.style.top = y + 'px';");
            writer.println("}");

            writer.println("document.addEventListener('mousemove', updateTooltipPosition);");

            // Recipe variant cycling
            writer.println("function addRecipeVariantCycler(id, count) {");
            writer.println("  recipeCyclers.push({ id: id, count: count, current: 0 });");
            writer.println("}");

            writer.println("function recipeVariantTick() {");
            writer.println("  recipeCyclers.forEach(function(cycler) {");
            writer.println("    var container = document.getElementById(cycler.id);");
            writer.println("    if (!container) return;");
            writer.println("    var variants = container.getElementsByClassName('recipe-variant');");
            writer.println("    variants[cycler.current].style.display = 'none';");
            writer.println("    cycler.current = (cycler.current + 1) % cycler.count;");
            writer.println("    variants[cycler.current].style.display = 'block';"); // Change from 'flex' to 'block'
            writer.println("  });");
            writer.println("}");

            // Item variant cycling
            writer.println("function addCycler(id, count) {");
            writer.println("  cyclers.push({ id: id, count: count, current: 0 });");
            writer.println("}");

            writer.println("function cycleTick() {");
            writer.println("  cyclers.forEach(function(cycler) {");
            writer.println("    var container = document.getElementById(cycler.id);");
            writer.println("    if (!container) return;");
            writer.println("    var images = container.getElementsByTagName('img');");
            writer.println("    images[cycler.current].style.display = 'none';");
            writer.println("    cycler.current = (cycler.current + 1) % cycler.count;");
            writer.println("    images[cycler.current].style.display = 'block';");
            writer.println("  });");
            writer.println("}");

            writer.println("setInterval(recipeVariantTick, 2000);");
            writer.println("setInterval(cycleTick, 1000);");

            writer.println("function toggleCategory(categoryId) {");
            writer.println("  var content = document.getElementById('content-category-' + categoryId);");
            writer.println("  var arrow = document.getElementById('arrow-category-' + categoryId);");
            writer.println("  content.classList.toggle('expanded');");
            writer.println("  arrow.classList.toggle('expanded');");
            writer.println("}");

            writer.println("function toggleResearch(researchId) {");
            writer.println("  var content = document.getElementById('content-research-' + researchId);");
            writer.println("  var arrow = document.getElementById('arrow-research-' + researchId);");
            writer.println("  content.classList.toggle('expanded');");
            writer.println("  arrow.classList.toggle('expanded');");
            writer.println("}");

            writer.println("// Handle anchor links - expand categories and research when jumping to them");
            writer.println("function expandToElement(elementId) {");
            writer.println("  var element = document.getElementById(elementId);");
            writer.println("  if (!element) return;");
            writer.println("  ");
            writer.println("  // Find parent category");
            writer.println("  var categoryContent = element.closest('.category-content');");
            writer.println("  if (categoryContent && !categoryContent.classList.contains('expanded')) {");
            writer.println("    var categoryId = categoryContent.id.replace('content-category-', '');");
            writer.println("    toggleCategory(categoryId);");
            writer.println("  }");
            writer.println("  ");
            writer.println("  // Find research content");
            writer.println("  var researchContent = element.querySelector('.research-content');");
            writer.println("  if (researchContent && !researchContent.classList.contains('expanded')) {");
            writer.println("    var researchId = researchContent.id.replace('content-research-', '');");
            writer.println("    toggleResearch(researchId);");
            writer.println("  }");
            writer.println("}");
            writer.println("");
            writer.println("// Handle page load with hash");
            writer.println("window.addEventListener('DOMContentLoaded', function() {");
            writer.println("  if (window.location.hash) {");
            writer.println("    var elementId = window.location.hash.substring(1);");
            writer.println("    expandToElement(elementId);");
            writer.println("  }");
            writer.println("});");
            writer.println("");
            writer.println("// Handle hash changes (clicking links)");
            writer.println("window.addEventListener('hashchange', function() {");
            writer.println("  if (window.location.hash) {");
            writer.println("    var elementId = window.location.hash.substring(1);");
            writer.println("    expandToElement(elementId);");
            writer.println("  }");
            writer.println("});");

            writer.println("var searchTimeout = null;");
            writer.println("var allExpanded = false;");
            writer.println("");
            writer.println("function toggleExpandAll() {");
            writer.println("  var categories = document.querySelectorAll('.category');");
            writer.println("  var button = document.getElementById('expand-all-btn');");
            writer.println("  ");
            writer.println("  if (allExpanded) {");
            writer.println("    // Collapse all");
            writer.println("    categories.forEach(function(cat) {");
            writer.println("      var categoryContent = cat.querySelector('.category-content');");
            writer.println("      if (categoryContent && categoryContent.classList.contains('expanded')) {");
            writer.println("        var categoryId = categoryContent.id.replace('content-category-', '');");
            writer.println("        toggleCategory(categoryId);");
            writer.println("      }");
            writer.println("      ");
            writer.println("      cat.querySelectorAll('.research-item').forEach(function(r) {");
            writer.println("        var researchContent = r.querySelector('.research-content');");
            writer.println("        if (researchContent && researchContent.classList.contains('expanded')) {");
            writer.println("          var researchId = researchContent.id.replace('content-research-', '');");
            writer.println("          toggleResearch(researchId);");
            writer.println("        }");
            writer.println("      });");
            writer.println("    });");
            writer.println("    button.textContent = 'Expand All';");
            writer.println("    allExpanded = false;");
            writer.println("  } else {");
            writer.println("    // Expand all");
            writer.println("    categories.forEach(function(cat) {");
            writer.println("      var categoryContent = cat.querySelector('.category-content');");
            writer.println("      if (categoryContent && !categoryContent.classList.contains('expanded')) {");
            writer.println("        var categoryId = categoryContent.id.replace('content-category-', '');");
            writer.println("        toggleCategory(categoryId);");
            writer.println("      }");
            writer.println("      ");
            writer.println("      cat.querySelectorAll('.research-item').forEach(function(r) {");
            writer.println("        var researchContent = r.querySelector('.research-content');");
            writer.println("        if (researchContent && !researchContent.classList.contains('expanded')) {");
            writer.println("          var researchId = researchContent.id.replace('content-research-', '');");
            writer.println("          toggleResearch(researchId);");
            writer.println("        }");
            writer.println("      });");
            writer.println("    });");
            writer.println("    button.textContent = 'Collapse All';");
            writer.println("    allExpanded = true;");
            writer.println("  }");
            writer.println("}");
            writer.println("function performSearch() {");
            writer.println("  var query = document.getElementById('search-input').value.toLowerCase();");
            writer.println("  var categories = document.querySelectorAll('.category');");
            writer.println("  var totalResults = 0;");
            writer.println("  ");
            writer.println("  // Clear previous highlights");
            writer.println("  document.querySelectorAll('.highlight').forEach(function(el) {");
            writer.println("    var parent = el.parentNode;");
            writer.println("    parent.replaceChild(document.createTextNode(el.textContent), el);");
            writer.println("    parent.normalize();");
            writer.println("  });");
            writer.println("  ");
            writer.println("  if (query.length === 0) {");
            writer.println("    // Show everything and collapse all");
            writer.println("    categories.forEach(function(cat) {");
            writer.println("      cat.classList.remove('hidden-by-search');");
            writer.println("      ");
            writer.println("      // Collapse category if expanded");
            writer.println("      var categoryContent = cat.querySelector('.category-content');");
            writer.println("      if (categoryContent && categoryContent.classList.contains('expanded')) {");
            writer.println("        var categoryId = categoryContent.id.replace('content-category-', '');");
            writer.println("        toggleCategory(categoryId);");
            writer.println("      }");
            writer.println("      ");
            writer.println("      // Collapse all research items");
            writer.println("      cat.querySelectorAll('.research-item').forEach(function(r) {");
            writer.println("        r.classList.remove('hidden-by-search');");
            writer.println("        var researchContent = r.querySelector('.research-content');");
            writer.println("        if (researchContent && researchContent.classList.contains('expanded')) {");
            writer.println("          var researchId = researchContent.id.replace('content-research-', '');");
            writer.println("          toggleResearch(researchId);");
            writer.println("        }");
            writer.println("      });");
            writer.println("    });");
            writer.println("    document.getElementById('search-results').textContent = '';");
            writer.println("    return;");
            writer.println("  }");
            writer.println("  ");
            writer.println("  categories.forEach(function(category) {");
            writer.println("    var categoryHasMatch = false;");
            writer.println("    var researchItems = category.querySelectorAll('.research-item');");
            writer.println("    ");
            writer.println("    researchItems.forEach(function(item) {");
            writer
                .println("      var researchName = item.querySelector('.research-header').textContent.toLowerCase();");
            writer.println(
                "      var researchText = item.querySelector('.research-text')?.textContent.toLowerCase() || '';");
            writer.println("      var allText = item.textContent.toLowerCase();");
            writer.println("      ");
            writer.println(
                "      if (researchName.includes(query) || researchText.includes(query) || allText.includes(query)) {");
            writer.println("        item.classList.remove('hidden-by-search');");
            writer.println("        categoryHasMatch = true;");
            writer.println("        totalResults++;");
            writer.println("        ");
            writer.println("        // Expand the item");
            writer.println("        var researchContent = item.querySelector('.research-content');");
            writer.println("        if (researchContent && !researchContent.classList.contains('expanded')) {");
            writer.println("          var researchId = researchContent.id.replace('content-research-', '');");
            writer.println("          toggleResearch(researchId);");
            writer.println("        }");
            writer.println("        ");
            writer.println("        // Highlight matches in research name");
            writer.println("        highlightText(item.querySelector('.research-header'), query);");
            writer.println("      } else {");
            writer.println("        item.classList.add('hidden-by-search');");
            writer.println("      }");
            writer.println("    });");
            writer.println("    ");
            writer.println("    if (categoryHasMatch) {");
            writer.println("      category.classList.remove('hidden-by-search');");
            writer.println("      // Expand category");
            writer.println("      var categoryContent = category.querySelector('.category-content');");
            writer.println("      if (categoryContent && !categoryContent.classList.contains('expanded')) {");
            writer.println("        var categoryId = categoryContent.id.replace('content-category-', '');");
            writer.println("        toggleCategory(categoryId);");
            writer.println("      }");
            writer.println("    } else {");
            writer.println("      category.classList.add('hidden-by-search');");
            writer.println("    }");
            writer.println("  });");
            writer.println("  ");
            writer.println(
                "  document.getElementById('search-results').textContent = totalResults + ' result(s) found';");
            writer.println("}");
            writer.println("");
            writer.println("function highlightText(element, query) {");
            writer.println("  if (!element || !query) return;");
            writer.println("  var text = element.textContent;");
            writer.println("  var lowerText = text.toLowerCase();");
            writer.println("  var index = lowerText.indexOf(query);");
            writer.println("  ");
            writer.println("  if (index !== -1) {");
            writer.println("    var before = text.substring(0, index);");
            writer.println("    var match = text.substring(index, index + query.length);");
            writer.println("    var after = text.substring(index + query.length);");
            writer.println("    ");
            writer
                .println("    element.innerHTML = before + '<span class=\"highlight\">' + match + '</span>' + after;");
            writer.println("  }");
            writer.println("}");
            writer.println("");
            writer.println("function onSearchInput() {");
            writer.println("  clearTimeout(searchTimeout);");
            writer.println("  searchTimeout = setTimeout(performSearch, 300);");
            writer.println("}");

            writer.println("// Handle page load with hash");
            writer.println("window.addEventListener('DOMContentLoaded', function() {");
            writer.println("  if (window.location.hash) {");
            writer.println("    var elementId = window.location.hash.substring(1);");
            writer.println("    expandToElement(elementId);");
            writer.println("    // Scroll after DOM updates");
            writer.println("    requestAnimationFrame(function() {");
            writer.println("      requestAnimationFrame(function() {");
            writer.println("        var element = document.getElementById(elementId);");
            writer.println("        if (element) element.scrollIntoView({ behavior: 'smooth', block: 'start' });");
            writer.println("      });");
            writer.println("    });");
            writer.println("  }");
            writer.println("});");
            writer.println("");
            writer.println("// Handle hash changes (clicking links)");
            writer.println("window.addEventListener('hashchange', function() {");
            writer.println("  if (window.location.hash) {");
            writer.println("    var elementId = window.location.hash.substring(1);");
            writer.println("    expandToElement(elementId);");
            writer.println("    // Scroll after DOM updates");
            writer.println("    requestAnimationFrame(function() {");
            writer.println("      requestAnimationFrame(function() {");
            writer.println("        var element = document.getElementById(elementId);");
            writer.println("        if (element) element.scrollIntoView({ behavior: 'smooth', block: 'start' });");
            writer.println("      });");
            writer.println("    });");
            writer.println("  }");
            writer.println("});");

            writer.println("</script>");
            writer.println("</head>");
            writer.println("<body>");
            writer.println("<h1>Thaumonomicon</h1>");

            if (customMessage != null && !customMessage.isEmpty()) {
                writer.println("<div style='text-align: center; color: #a0907a; font-style: italic; margin: 10px 0;'>");
                writer.println(customMessage);
                writer.println("</div>");
            }

            writer.println("<div class='search-container'>");
            writer.println("<div class='search-controls'>");
            writer.println(
                "<button id='expand-all-btn' class='expand-button' onclick='toggleExpandAll()'>Expand All</button>");
            writer.println(
                "<input type='text' id='search-input' class='search-input' placeholder='Search research...' oninput='onSearchInput()' />");
            writer.println("</div>");
            writer.println("<div id='search-results' class='search-results'></div>");
            writer.println("</div>");

            for (String categoryKey : ResearchCategories.researchCategories.keySet()) {
                ResearchCategoryList category = ResearchCategories.getResearchList(categoryKey);

                writer.println("<div class='category' id='category-" + categoryKey + "'>");

                // Category header with icon
                writer.println("<div class='category-header' onclick='toggleCategory(\"" + categoryKey + "\")'>");
                writer.println("<span class='collapse-arrow' id='arrow-category-" + categoryKey + "'>▶</span>");

                // Extract category icon
                String categoryIconPath = extractCategoryIcon(category.icon, categoryKey);
                if (categoryIconPath != null) {
                    writer.println("<img src='" + categoryIconPath + "' class='category-icon' />");
                }

                writer.println("<h2 style='margin: 0;'>" + ResearchCategories.getCategoryName(categoryKey) + "</h2>");
                writer.println("</div>");

                // Category content (collapsible)
                writer.println("<div class='category-content' id='content-category-" + categoryKey + "'>");

                // Sort research items by display position
                List<ResearchItem> sortedResearch = new ArrayList<>(category.research.values());
                Collections.sort(sortedResearch, new Comparator<ResearchItem>() {

                    @Override
                    public int compare(ResearchItem r1, ResearchItem r2) {
                        int rowCompare = Integer.compare(r1.displayRow, r2.displayRow);
                        if (rowCompare != 0) return rowCompare;
                        return Integer.compare(r1.displayColumn, r2.displayColumn);
                    }
                });

                for (ResearchItem research : sortedResearch) {
                    if (research.isVirtual()) continue;

                    writer.println("<div class='research-item' id='research-" + research.key + "'>");

                    // Research header with icon and collapse
                    writer.println(
                        "<div class='research-header-wrapper' onclick='toggleResearch(\"" + research.key + "\")'>");
                    writer.println("<span class='collapse-arrow' id='arrow-research-" + research.key + "'>▶</span>");

                    // Get research icon
                    String researchIconPath = getResearchIcon(research);
                    if (researchIconPath != null) {
                        writer.println("<img src='" + researchIconPath + "' class='research-icon' />");
                    }

                    writer.print("<div class='research-header'>");
                    if (research.isSpecial()) writer.print("<span class='special'>[★] </span>");
                    if (research.isHidden()) writer.print("<span class='hidden'>[Hidden] </span>");
                    if (research.isConcealed()) writer.print("<span class='concealed'>[Concealed] </span>");
                    if (research.isLost()) writer.print("<span class='lost'>[Lost] </span>");
                    writer.print(research.getName());
                    writer.println("</div>");

                    writer.println("</div>"); // Close research-header-wrapper

                    // Research content (collapsible, hidden by default)
                    writer.println("<div class='research-content' id='content-research-" + research.key + "'>");

                    // ... all the existing research content (text, complexity, warp, parents, aspects, pages) ...
                    writer.println("<div class='research-text'>" + research.getText() + "</div>");

                    // Complexity
                    if (research.getComplexity() > 0) {
                        writer.println("<div class='complexity'>Complexity: " + research.getComplexity() + "</div>");
                    }

                    // Warp warning
                    int warp = ThaumcraftApi.getWarp(research.key);
                    if (warp > 0) {
                        String warpLevel = warp > 5 ? "EXTREME" : (warp > 3 ? "HIGH" : (warp > 1 ? "MODERATE" : "LOW"));
                        writer.println(
                            "<div class='warp-warning'>⚠ Forbidden Knowledge - Warp Risk: " + warpLevel + "</div>");
                    }

                    // Parent research links
                    if (research.parents != null && research.parents.length > 0) {
                        writer.print("<div><strong>Prerequisites:</strong> ");
                        for (int i = 0; i < research.parents.length; i++) {
                            ResearchItem parent = ResearchCategories.getResearch(research.parents[i]);
                            if (parent != null) {
                                writer.print(
                                    "<a class='parent-link' href='#research-" + parent.key
                                        + "' onclick='goToResearch(event, \""
                                        + parent.key
                                        + "\")'>"
                                        + parent.getName()
                                        + "</a>");

                                if (i < research.parents.length - 1) writer.print(", ");
                            }
                        }
                        writer.println("</div>");
                    }

                    // Aspects
                    writeAspectList(writer, research.tags, "Aspects:");

                    // Pages
                    ResearchPage[] pages = research.getPages();
                    if (pages != null && pages.length > 0) {
                        writer.println("<div style='margin-top: 15px;'><strong>Research Pages:</strong></div>");

                        for (int i = 0; i < pages.length; i++) {
                            ResearchPage page = pages[i];
                            if (page == null) continue;

                            if (i > 0) {
                                writer.println("<div class='page-separator'></div>");
                            }

                            writer.println("<div class='page'>");
                            writer.println("<div style='color: #8b7d74; font-size: 0.9em;'>Page " + (i + 1) + "</div>");

                            switch (page.type) {
                                case TEXT, TEXT_CONCEALED:
                                    writer.println("<p>" + formatText(page.getTranslatedText()) + "</p>");
                                    break;

                                case IMAGE:
                                    writer.println("<div style='text-align: center;'>");
                                    writer.println("<div>[Image: " + page.image + "]</div>");
                                    if (page.text != null && !page.text.isEmpty()) {
                                        writer.println(
                                            "<div class='image-caption'>" + formatText(page.getTranslatedText())
                                                + "</div>");
                                    }
                                    writer.println("</div>");
                                    break;

                                case ASPECTS:
                                    writer.println("<div class='recipe'>");
                                    writer.println("<div class='recipe-title'>Aspect Discovery</div>");
                                    if (page.aspects != null) {
                                        for (Aspect aspect : page.aspects.getAspects()) {
                                            String aspectIcon = getAspectIcon(aspect);
                                            writer.println(
                                                "<div style='margin: 12px 0; display: flex; align-items: center; gap: 12px;'>");

                                            if (aspectIcon != null) {
                                                writer.print(
                                                    "<img src='" + aspectIcon
                                                        + "' style='width: 48px; height: 48px; image-rendering: crisp-edges;' "
                                                        + "onmouseenter=\"showTooltip(event, '"
                                                        + aspect.getName()
                                                        + "', '"
                                                        + aspect.getLocalizedDescription()
                                                            .replace("'", "\\'")
                                                        + "')\" "
                                                        + "onmouseleave=\"hideTooltip()\" />");
                                            }

                                            writer.print(
                                                "<span style='font-weight: bold; color: #c9a876; font-size: 1.2em;'>"
                                                    + aspect.getName()
                                                    + "</span>");

                                            if (aspect.getComponents() != null) {
                                                writer
                                                    .print("<span style='color: #a0907a; font-size: 1.2em;'>=</span>");
                                                String comp1Icon = getAspectIcon(aspect.getComponents()[0]);
                                                String comp2Icon = getAspectIcon(aspect.getComponents()[1]);

                                                if (comp1Icon != null) {
                                                    writer.print(
                                                        "<img src='" + comp1Icon
                                                            + "' style='width: 32px; height: 32px; image-rendering: crisp-edges;' "
                                                            + "onmouseenter=\"showTooltip(event, '"
                                                            + aspect.getComponents()[0].getName()
                                                            + "')\" "
                                                            + "onmouseleave=\"hideTooltip()\" />");
                                                }
                                                writer
                                                    .print("<span style='color: #a0907a; font-size: 1.2em;'>+</span>");
                                                if (comp2Icon != null) {
                                                    writer.print(
                                                        "<img src='" + comp2Icon
                                                            + "' style='width: 32px; height: 32px; image-rendering: crisp-edges;' "
                                                            + "onmouseenter=\"showTooltip(event, '"
                                                            + aspect.getComponents()[1].getName()
                                                            + "')\" "
                                                            + "onmouseleave=\"hideTooltip()\" />");
                                                }
                                            } else {
                                                writer.print(
                                                    "<span style='color: #8b7d74; font-style: italic;'>(Primal Aspect)</span>");
                                            }

                                            writer.println("</div>");
                                        }
                                    }
                                    writer.println("</div>");
                                    break;

                                case NORMAL_CRAFTING:
                                    dumpNormalCrafting(writer, page);
                                    break;

                                case ARCANE_CRAFTING:
                                    dumpArcaneCrafting(writer, page);
                                    break;

                                case CRUCIBLE_CRAFTING:
                                    dumpCrucibleCrafting(writer, page);
                                    break;

                                case INFUSION_CRAFTING:
                                    dumpInfusionCrafting(writer, page);
                                    break;

                                case INFUSION_ENCHANTMENT:
                                    dumpInfusionEnchantment(writer, page);
                                    break;

                                case SMELTING:
                                    dumpSmelting(writer, page);
                                    break;

                                case COMPOUND_CRAFTING:
                                    dumpCompoundCrafting(writer, page);
                                    break;
                            }

                            writer.println("</div>");
                        }
                    }

                    writer.println("</div>"); // Close research-content
                    writer.println("</div>"); // Close research-item
                }

                writer.println("</div>"); // Close category-content
                writer.println("</div>"); // Close category
            }

            String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
            writer.println(
                "<div style='text-align: center; margin: 40px 0 20px 0; padding: 20px; border-top: 2px solid #4a3520; color: #6b5d54; font-size: 0.9em;'>");
            writer.println(
                "Generated using <strong style='color: #8b7d74;'><a href=\"https://github.com/JackOfNoneTrades/FentLib\">FentLib</a></strong> version "
                    + Tags.VERSION);
            writer.println("<br><span style='font-size: 0.85em;'>on " + timestamp + "</span>");
            writer.println("</div>");

            writer.println("</body>");
            writer.println("</html>");

            FentLib.LOG.info("Thaumonomicon dumped to: " + htmlFile.getAbsolutePath());

            if (mc.thePlayer != null) {
                net.minecraft.util.ChatComponentText message = new net.minecraft.util.ChatComponentText(
                    "§aThaumonomicon dumped to: §f");

                net.minecraft.util.ChatComponentText linkComponent = new net.minecraft.util.ChatComponentText(
                    outputDir.getAbsolutePath());

                net.minecraft.event.ClickEvent clickEvent = new net.minecraft.event.ClickEvent(
                    net.minecraft.event.ClickEvent.Action.OPEN_FILE,
                    Paths.get(outputDir.getAbsolutePath(), "index.html")
                        .toString());
                linkComponent.getChatStyle()
                    .setChatClickEvent(clickEvent);
                linkComponent.getChatStyle()
                    .setUnderlined(true);
                linkComponent.getChatStyle()
                    .setColor(net.minecraft.util.EnumChatFormatting.AQUA);

                message.appendSibling(linkComponent);
                mc.thePlayer.addChatMessage(message);
            }

        } catch (Exception e) {
            e.printStackTrace();
            if (mc.thePlayer != null) {
                mc.thePlayer.addChatMessage(
                    new net.minecraft.util.ChatComponentText("§cFailed to dump Thaumonomicon: " + e.getMessage()));
            }
        }
    }

    private static String getItemIcon(ItemStack stack) {
        if (stack == null) return null;

        try {
            // Handle wildcard metadata
            if (stack.getItemDamage() == 32767) {
                stack = InventoryUtils.cycleItemStack(stack);
                if (stack == null) return null;
            }

            File outputDir = new File(Minecraft.getMinecraft().mcDataDir, OUTPUT_FOLDER_NAME);
            File iconsDir = new File(outputDir, "icons");
            if (!iconsDir.exists()) iconsDir.mkdirs();

            // Generate filename from item
            String id = GameRegistry.findUniqueIdentifierFor(stack.getItem())
                .toString();
            int meta = stack.getItemDamage();
            String filename = "item_" + id.replaceAll("[^a-zA-Z0-9]", "_") + "_" + meta + ".png";
            File iconFile = new File(iconsDir, filename);

            // Return cached if exists
            if (iconFile.exists()) {
                return "icons/" + filename;
            }

            // Render the item using your existing function
            /// BufferedImage img = renderItemToImage(stack, 32);
            BufferedImage img;
            IItemRenderer tesrRenderer = MinecraftForgeClient
                .getItemRenderer(stack, IItemRenderer.ItemRenderType.INVENTORY);
            if (tesrRenderer != null) {
                img = renderItemStackToImage(stack, 480);
            } else {
                img = renderItemToImage(stack, 32);
            }

            if (img == null) {
                System.err.println("Failed to render item icon for: " + stack.getDisplayName());
                return null;
            }

            // Save
            ImageIO.write(img, "PNG", iconFile);

            return "icons/" + filename;

        } catch (Exception e) {
            System.err.println("Failed to extract item icon for " + stack.getDisplayName() + ": " + e.getMessage());
            return null;
        }
    }

    private static List<ItemStack> getAllItemVariants(Object input) {
        List<ItemStack> variants = new ArrayList<>();

        if (input instanceof ItemStack) {
            ItemStack stack = (ItemStack) input;

            if (stack.getItemDamage() == 32767 && stack.getItem()
                .getHasSubtypes()) {
                // Get all subtypes
                List<ItemStack> subItems = new ArrayList<>();
                stack.getItem()
                    .getSubItems(
                        stack.getItem(),
                        stack.getItem()
                            .getCreativeTab(),
                        subItems);
                for (ItemStack subItem : subItems) {
                    ItemStack copy = subItem.copy();
                    copy.stackSize = stack.stackSize;
                    if (stack.getTagCompound() != null) {
                        copy.setTagCompound(
                            (NBTTagCompound) stack.getTagCompound()
                                .copy());
                    }
                    variants.add(copy);
                }
            } else {
                variants.add(stack);
            }
        } else if (input instanceof ArrayList || input instanceof List) {
            List<?> list = (List<?>) input;
            for (Object obj : list) {
                if (obj instanceof ItemStack) {
                    variants.addAll(getAllItemVariants(obj));
                }
            }
        } else if (input instanceof String) {
            // Ore dictionary
            ArrayList<ItemStack> oreItems = OreDictionary.getOres((String) input);
            for (ItemStack oreItem : oreItems) {
                variants.addAll(getAllItemVariants(oreItem));
            }
        }

        return variants;
    }

    // New method to find recipe reference (from GuiResearchRecipe):
    private static String findRecipeReference(ItemStack item) {
        Object[] ref = ThaumcraftApi.getCraftingRecipeKey(Minecraft.getMinecraft().thePlayer, item);
        if (ref != null && ref.length >= 1) {
            return (String) ref[0];
        }
        return null;
    }

    private static String buildTooltipAttrs(ItemStack stack, String hint) {
        if (stack == null) return "";

        // Build tooltip text with ore dict info
        StringBuilder tooltipText = new StringBuilder();
        tooltipText.append(
            stack.getDisplayName()
                .replace("'", "\\'"));

        // Add ore dictionary info if present
        int[] oreIds = OreDictionary.getOreIDs(stack);
        if (oreIds.length > 0) {
            tooltipText.append("<br><span style=\\'color:#6b5d54; font-size:0.9em;\\'>OreDict: ");
            for (int i = 0; i < oreIds.length; i++) {
                tooltipText.append(OreDictionary.getOreName(oreIds[i]));
                if (i < oreIds.length - 1) tooltipText.append(", ");
            }
            tooltipText.append("</span>");
        }

        String stackSize = stack.stackSize > 1 ? " × " + stack.stackSize : "";

        return "onmouseenter=\"showTooltip(event, '" + tooltipText.toString()
            + stackSize
            + "'"
            + (hint != null ? ", '" + hint + "'" : ", null")
            + ")\" "
            + "onmouseleave=\"hideTooltip()\"";
    }

    private static void dumpNormalCrafting(PrintWriter writer, ResearchPage page) {
        writer.println("<div class='recipe'>");
        writer.println("<div class='recipe-title'>Crafting Recipe</div>");

        Object recipe = page.recipe;
        Object[] recipeVariants = null;

        // Check if we have multiple recipe variants
        if (recipe instanceof Object[]) {
            recipeVariants = (Object[]) recipe;
        } else {
            recipeVariants = new Object[] { recipe };
        }

        // Generate unique ID for this recipe set
        String recipeSetId = "recipe_set_" + System.identityHashCode(page);

        writer.println("<div id='" + recipeSetId + "' class='recipe-variants'>");

        // Render all recipe variants (hidden except first)
        for (int variantIndex = 0; variantIndex < recipeVariants.length; variantIndex++) {
            IRecipe irecipe = null;
            Object currentRecipe = recipeVariants[variantIndex];

            if (!(currentRecipe instanceof IRecipe)) continue;
            irecipe = (IRecipe) currentRecipe;

            String display = variantIndex == 0 ? "flex" : "none";
            writer.println(
                "<div class='recipe-variant' style='display: " + display
                    + "; align-items: center; margin: 10px 0;' data-variant='"
                    + variantIndex
                    + "'>");

            // Draw crafting grid
            writer.println("<div class='crafting-grid'>");

            Object[] inputs = null;
            int width = 3, height = 3;

            if (currentRecipe instanceof ShapedRecipes) {
                ShapedRecipes shaped = (ShapedRecipes) currentRecipe;
                inputs = shaped.recipeItems;
                width = shaped.recipeWidth;
                height = shaped.recipeHeight;
            } else if (currentRecipe instanceof ShapedOreRecipe) {
                ShapedOreRecipe shaped = (ShapedOreRecipe) currentRecipe;
                inputs = shaped.getInput();
                width = (Integer) getPrivateField(shaped, "width");
                height = (Integer) getPrivateField(shaped, "height");
            } else if (currentRecipe instanceof ShapelessRecipes) {
                ShapelessRecipes shapeless = (ShapelessRecipes) currentRecipe;
                inputs = shapeless.recipeItems.toArray();
            } else if (currentRecipe instanceof ShapelessOreRecipe) {
                ShapelessOreRecipe shapeless = (ShapelessOreRecipe) currentRecipe;
                inputs = shapeless.getInput()
                    .toArray();
            }

            // Draw 3x3 grid
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 3; col++) {
                    writer.print("<div class='crafting-slot'>");

                    int index = row * width + col;
                    if (inputs != null && row < height
                        && col < width
                        && index < inputs.length
                        && inputs[index] != null) {
                        List<ItemStack> variants = getAllItemVariants(inputs[index]);

                        if (!variants.isEmpty()) {
                            // Generate unique ID for this slot
                            String slotId = "craft_slot_v" + variantIndex
                                + "_"
                                + row
                                + "_"
                                + col
                                + "_"
                                + System.identityHashCode(page)
                                + "_"
                                + System.identityHashCode(inputs[index]);

                            writer.print("<div class='item-cycler' id='" + slotId + "'>");

                            // Add all variant images (hidden except first)
                            for (int i = 0; i < variants.size(); i++) {
                                ItemStack variant = variants.get(i);
                                String iconPath = getItemIcon(variant);
                                String recipeRef = findRecipeReference(variant);

                                if (iconPath != null) {
                                    String displayStyle = i == 0 ? "block" : "none";
                                    String clickable = recipeRef != null ? "item-clickable" : "";
                                    String onclick = recipeRef != null
                                        ? "onclick=\"location.href='#research-" + recipeRef + "'\""
                                        : "";
                                    String hint = recipeRef != null ? "Click for research" : null;
                                    String tooltipAttrs = buildTooltipAttrs(variant, hint);

                                    writer.print(
                                        "<img src='" + iconPath
                                            + "' "
                                            + "class='craft-item "
                                            + clickable
                                            + "' "
                                            + "style='width: 32px; height: 32px; display: "
                                            + displayStyle
                                            + ";' "
                                            + "data-index='"
                                            + i
                                            + "' "
                                            + tooltipAttrs
                                            + " "
                                            + onclick
                                            + " />");
                                }
                            }

                            writer.print("</div>");

                            // Add cycling script data
                            if (variants.size() > 1) {
                                writer.print("<script>addCycler('" + slotId + "', " + variants.size() + ");</script>");
                            }
                        }
                    }

                    writer.println("</div>");
                }
            }

            writer.println("</div>");

            // Arrow
            writer.println("<div class='crafting-arrow'>→</div>");

            // Result
            writer.println("<div class='crafting-result'>");
            String resultIcon = getItemIcon(irecipe.getRecipeOutput());
            if (resultIcon != null) {
                ItemStack result = irecipe.getRecipeOutput();
                String recipeRef = findRecipeReference(result);
                String clickable = recipeRef != null ? "item-clickable" : "";
                String onclick = recipeRef != null ? "onclick=\"location.href='#research-" + recipeRef + "'\"" : "";

                String hint = recipeRef != null ? "Click for research" : null;
                String stackSize = result.stackSize > 1 ? " × " + result.stackSize : "";

                String tooltipAttrs = buildTooltipAttrs(result, hint);

                writer.print(
                    "<img src='" + resultIcon
                        + "' "
                        + "class='"
                        + clickable
                        + "' "
                        + "style='width: 40px; height: 40px;' "
                        + tooltipAttrs
                        + " "
                        + onclick
                        + " />");
            }
            writer.println("</div>");

            writer.println("</div>"); // Close recipe-variant
        }

        writer.println("</div>"); // Close recipe-variants

        // Add cycling script if multiple variants
        if (recipeVariants.length > 1) {
            writer.println(
                "<script>addRecipeVariantCycler('" + recipeSetId + "', " + recipeVariants.length + ");</script>");
        }

        writer.println("</div>");
    }

    // Helper to access private fields:
    private static Object getPrivateField(Object obj, String fieldName) {
        try {
            java.lang.reflect.Field field = obj.getClass()
                .getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (Exception e) {
            return 3; // default to 3x3
        }
    }

    private static String getAspectIcon(Aspect aspect) {
        if (aspect == null) return null;

        try {
            File outputDir = new File(Minecraft.getMinecraft().mcDataDir, OUTPUT_FOLDER_NAME);
            File aspectsDir = new File(outputDir, "aspects");
            if (!aspectsDir.exists()) aspectsDir.mkdirs();

            // Generate filename from aspect
            String filename = "aspect_" + aspect.getTag() + ".png";
            File iconFile = new File(aspectsDir, filename);

            // Return cached if exists
            if (iconFile.exists()) {
                return "aspects/" + filename;
            }

            // Get the aspect image resource
            ResourceLocation aspectImage = aspect.getImage();
            if (aspectImage == null) {
                return null;
            }

            // Read the texture file
            InputStream textureStream = Minecraft.getMinecraft()
                .getResourceManager()
                .getResource(aspectImage)
                .getInputStream();
            BufferedImage aspectImg = ImageIO.read(textureStream);
            textureStream.close();

            if (aspectImg == null) {
                return null;
            }

            // Apply aspect color tinting
            int color = aspect.getColor();
            float r = ((color >> 16) & 0xFF) / 255.0f;
            float g = ((color >> 8) & 0xFF) / 255.0f;
            float b = (color & 0xFF) / 255.0f;

            BufferedImage tintedImg = new BufferedImage(
                aspectImg.getWidth(),
                aspectImg.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
            for (int y = 0; y < aspectImg.getHeight(); y++) {
                for (int x = 0; x < aspectImg.getWidth(); x++) {
                    int pixel = aspectImg.getRGB(x, y);
                    int alpha = (pixel >> 24) & 0xFF;
                    int gray = pixel & 0xFF; // Grayscale value

                    // Apply tint
                    int newR = (int) (gray * r);
                    int newG = (int) (gray * g);
                    int newB = (int) (gray * b);

                    int newPixel = (alpha << 24) | (newR << 16) | (newG << 8) | newB;
                    tintedImg.setRGB(x, y, newPixel);
                }
            }

            // Save
            ImageIO.write(tintedImg, "PNG", iconFile);

            return "aspects/" + filename;

        } catch (Exception e) {
            System.err.println("Failed to extract aspect icon for " + aspect.getName() + ": " + e.getMessage());
            return null;
        }
    }

    private static void writeAspectList(PrintWriter writer, AspectList aspects, String label) {
        if (aspects != null && aspects.size() > 0) {
            writer.print("<div style='margin: 8px 0;'><strong>" + label + "</strong> ");
            for (Aspect aspect : aspects.getAspects()) {
                String aspectIcon = getAspectIcon(aspect);
                writer.print("<span class='aspect-display'>");
                if (aspectIcon != null) {
                    writer.print(
                        "<img src='" + aspectIcon
                            + "' class='aspect-icon' "
                            + "onmouseenter=\"showTooltip(event, '"
                            + aspect.getName()
                            + "', '"
                            + aspect.getLocalizedDescription()
                                .replace("'", "\\'")
                            + "')\" "
                            + "onmouseleave=\"hideTooltip()\" />");
                }
                writer.print("<span class='aspect-amount'>" + aspects.getAmount(aspect) + "</span>");
                writer.print("</span>");
            }
            writer.println("</div>");
        }
    }

    private static void dumpArcaneCrafting(PrintWriter writer, ResearchPage page) {
        writer.println("<div class='recipe'>");
        writer.println("<div class='recipe-title'>Arcane Crafting</div>");

        Object recipe = page.recipe;
        Object[] recipeVariants = null;

        // Check if we have multiple recipe variants
        if (recipe instanceof Object[]) {
            recipeVariants = (Object[]) recipe;
        } else {
            recipeVariants = new Object[] { recipe };
        }

        // Generate unique ID for this recipe set
        String recipeSetId = "arcane_recipe_set_" + System.identityHashCode(page);

        writer.println("<div id='" + recipeSetId + "' class='recipe-variants'>");

        // Render all recipe variants (hidden except first)
        for (int variantIndex = 0; variantIndex < recipeVariants.length; variantIndex++) {
            IArcaneRecipe arcane = null;
            Object currentRecipe = recipeVariants[variantIndex];

            if (!(currentRecipe instanceof IArcaneRecipe)) continue;
            arcane = (IArcaneRecipe) currentRecipe;

            String display = variantIndex == 0 ? "block" : "none";
            writer.println(
                "<div class='recipe-variant' style='display: " + display + ";' data-variant='" + variantIndex + "'>");

            // Vis Cost
            AspectList aspects = arcane.getAspects();
            /*
             * if (aspects != null && aspects.size() > 0) {
             * writer.print("<div style='margin: 5px 0;'><strong>Vis Cost:</strong> ");
             * for (Aspect aspect : aspects.getAspects()) {
             * writer.print("<span class='aspect'>" + aspect.getName() + " × " + aspects.getAmount(aspect) + "</span>");
             * }
             * writer.println("</div>");
             * }
             */
            writeAspectList(writer, aspects, "Vis Cost:");

            writer.println("<div style='display: flex; align-items: center; margin: 10px 0;'>");

            // Draw crafting grid
            writer.println("<div class='crafting-grid'>");

            Object[] inputs = null;
            int width = 3, height = 3;

            if (currentRecipe instanceof ShapedArcaneRecipe) {
                ShapedArcaneRecipe shaped = (ShapedArcaneRecipe) currentRecipe;
                inputs = shaped.getInput();
                width = shaped.width;
                height = shaped.height;
            } else if (currentRecipe instanceof ShapelessArcaneRecipe) {
                ShapelessArcaneRecipe shapeless = (ShapelessArcaneRecipe) currentRecipe;
                inputs = shapeless.getInput()
                    .toArray();
            }

            // Draw 3x3 grid
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 3; col++) {
                    writer.print("<div class='crafting-slot'>");

                    int index = row * width + col;
                    if (inputs != null && row < height
                        && col < width
                        && index < inputs.length
                        && inputs[index] != null) {
                        List<ItemStack> variants = getAllItemVariants(inputs[index]);

                        if (!variants.isEmpty()) {
                            // Generate unique ID for this slot
                            String slotId = "arcane_slot_v" + variantIndex
                                + "_"
                                + row
                                + "_"
                                + col
                                + "_"
                                + System.identityHashCode(page)
                                + "_"
                                + System.identityHashCode(inputs[index]);

                            writer.print("<div class='item-cycler' id='" + slotId + "'>");

                            // Add all variant images (hidden except first)
                            for (int i = 0; i < variants.size(); i++) {
                                ItemStack variant = variants.get(i);
                                String iconPath = getItemIcon(variant);
                                String recipeRef = findRecipeReference(variant);

                                if (iconPath != null) {
                                    String displayStyle = i == 0 ? "block" : "none";
                                    String clickable = recipeRef != null ? "item-clickable" : "";
                                    String onclick = recipeRef != null
                                        ? "onclick=\"location.href='#research-" + recipeRef + "'\""
                                        : "";
                                    String hint = recipeRef != null ? "Click for research" : null;
                                    String tooltipAttrs = buildTooltipAttrs(variant, hint);

                                    writer.print(
                                        "<img src='" + iconPath
                                            + "' "
                                            + "class='craft-item "
                                            + clickable
                                            + "' "
                                            + "style='width: 32px; height: 32px; display: "
                                            + displayStyle
                                            + ";' "
                                            + "data-index='"
                                            + i
                                            + "' "
                                            + tooltipAttrs
                                            + " "
                                            + onclick
                                            + " />");
                                }
                            }

                            writer.print("</div>");

                            // Add cycling script data
                            if (variants.size() > 1) {
                                writer.print("<script>addCycler('" + slotId + "', " + variants.size() + ");</script>");
                            }
                        }
                    }

                    writer.println("</div>");
                }
            }

            writer.println("</div>");

            // Arrow
            writer.println("<div class='crafting-arrow'>→</div>");

            // Result
            writer.println("<div class='crafting-result'>");
            String resultIcon = getItemIcon(arcane.getRecipeOutput());
            if (resultIcon != null) {
                ItemStack result = arcane.getRecipeOutput();
                String recipeRef = findRecipeReference(result);
                String clickable = recipeRef != null ? "item-clickable" : "";
                String onclick = recipeRef != null ? "onclick=\"location.href='#research-" + recipeRef + "'\"" : "";
                String hint = recipeRef != null ? "Click for research" : null;
                String stackSize = result.stackSize > 1 ? " × " + result.stackSize : "";
                String tooltipAttrs = buildTooltipAttrs(result, hint);

                writer.print(
                    "<img src='" + resultIcon
                        + "' "
                        + "class='"
                        + clickable
                        + "' "
                        + "style='width: 40px; height: 40px;' "
                        + tooltipAttrs
                        + " "
                        + onclick
                        + " />");
            }
            writer.println("</div>");

            writer.println("</div>"); // Close flex container

            writer.println("</div>"); // Close recipe-variant
        }

        writer.println("</div>"); // Close recipe-variants

        // Add cycling script if multiple variants
        if (recipeVariants.length > 1) {
            writer.println(
                "<script>addRecipeVariantCycler('" + recipeSetId + "', " + recipeVariants.length + ");</script>");
        }

        writer.println("</div>");
    }

    private static void dumpCrucibleCrafting(PrintWriter writer, ResearchPage page) {
        writer.println("<div class='recipe'>");
        writer.println("<div class='recipe-title'>Crucible Recipe</div>");

        Object recipe = page.recipe;
        Object[] recipeVariants = null;

        // Check if we have multiple recipe variants
        if (recipe instanceof Object[]) {
            recipeVariants = (Object[]) recipe;
        } else {
            recipeVariants = new Object[] { recipe };
        }

        // Generate unique ID for this recipe set
        String recipeSetId = "crucible_recipe_set_" + System.identityHashCode(page);

        writer.println("<div id='" + recipeSetId + "' class='recipe-variants'>");

        // Render all recipe variants (hidden except first)
        for (int variantIndex = 0; variantIndex < recipeVariants.length; variantIndex++) {
            CrucibleRecipe crucible = null;
            Object currentRecipe = recipeVariants[variantIndex];

            if (!(currentRecipe instanceof CrucibleRecipe)) continue;
            crucible = (CrucibleRecipe) currentRecipe;

            String display = variantIndex == 0 ? "flex" : "none";
            writer.println(
                "<div class='recipe-variant' style='display: " + display
                    + "; align-items: center; margin: 10px 0;' data-variant='"
                    + variantIndex
                    + "'>");

            // Catalyst item
            writer.println("<div style='text-align: center;'>");
            writer.println("<div style='color: #d4a574; font-size: 0.85em; margin-bottom: 3px;'>Catalyst</div>");
            writer.println(
                "<div style='width: 50px; height: 50px; background-color: #2a2520; border: 2px solid #6b5d54; display: flex; align-items: center; justify-content: center;'>");

            List<ItemStack> catalystVariants = getAllItemVariants(crucible.catalyst);
            if (!catalystVariants.isEmpty()) {
                String catalystSlotId = "crucible_catalyst_v" + variantIndex
                    + "_"
                    + System.identityHashCode(page)
                    + "_"
                    + System.identityHashCode(crucible.catalyst);
                writer.print(
                    "<div class='item-cycler' id='" + catalystSlotId
                        + "' style='position: static; display: flex; align-items: center; justify-content: center; width: 40px; height: 40px;'>");

                for (int i = 0; i < catalystVariants.size(); i++) {
                    ItemStack variant = catalystVariants.get(i);
                    String iconPath = getItemIcon(variant);
                    String recipeRef = findRecipeReference(variant);

                    if (iconPath != null) {
                        String displayStyle = i == 0 ? "block" : "none";
                        String clickable = recipeRef != null ? "item-clickable" : "";
                        String onclick = recipeRef != null ? "onclick=\"location.href='#research-" + recipeRef + "'\""
                            : "";
                        String hint = recipeRef != null ? "Click for research" : null;
                        String tooltipAttrs = buildTooltipAttrs(variant, hint);

                        writer.print(
                            "<img src='" + iconPath
                                + "' "
                                + "class='craft-item "
                                + clickable
                                + "' "
                                + "style='width: 40px; height: 40px; display: "
                                + displayStyle
                                + "; position: absolute;' "
                                + tooltipAttrs
                                + " "
                                + onclick
                                + " />");
                    }
                }

                writer.print("</div>");
                if (catalystVariants.size() > 1) {
                    writer.print(
                        "<script>addCycler('" + catalystSlotId + "', " + catalystVariants.size() + ");</script>");
                }
            }

            writer.println("</div>");
            writer.println("</div>");

            // Crucible icon/indicator
            writer.println("<div style='margin: 0 15px; text-align: center;'>");
            writer.println("<div style='font-size: 32px;'>🍲</div>");
            writer.println("<div style='color: #a0907a; font-size: 0.8em;'>Crucible</div>");
            writer.println("</div>");

            // Result
            writer.println("<div style='text-align: center;'>");
            writer.println("<div style='color: #d4a574; font-size: 0.85em; margin-bottom: 3px;'>Result</div>");
            writer.println(
                "<div style='width: 50px; height: 50px; background-color: #443a30; border: 2px solid #d4a574; display: flex; align-items: center; justify-content: center;'>");

            String resultIcon = getItemIcon(crucible.getRecipeOutput());
            if (resultIcon != null) {
                ItemStack result = crucible.getRecipeOutput();
                String recipeRef = findRecipeReference(result);
                String clickable = recipeRef != null ? "item-clickable" : "";
                String onclick = recipeRef != null ? "onclick=\"location.href='#research-" + recipeRef + "'\"" : "";
                String hint = recipeRef != null ? "Click for research" : null;
                String stackSize = result.stackSize > 1 ? " × " + result.stackSize : "";
                String tooltipAttrs = buildTooltipAttrs(result, hint);

                writer.print(
                    "<img src='" + resultIcon
                        + "' "
                        + "class='"
                        + clickable
                        + "' "
                        + "style='width: 40px; height: 40px;' "
                        + tooltipAttrs
                        + " "
                        + onclick
                        + " />");
            }

            writer.println("</div>");
            writer.println("</div>");

            // Aspects below the recipe
            writeAspectList(writer, crucible.aspects, "Aspects Required:");
            writer.println("</div>"); // Close recipe-variant
        }

        writer.println("</div>"); // Close recipe-variants

        // Add cycling script if multiple variants
        if (recipeVariants.length > 1) {
            writer.println(
                "<script>addRecipeVariantCycler('" + recipeSetId + "', " + recipeVariants.length + ");</script>");
        }

        writer.println("</div>");
    }

    private static void dumpInfusionCrafting(PrintWriter writer, ResearchPage page) {
        writer.println("<div class='recipe'>");
        writer.println("<div class='recipe-title'>Infusion Crafting</div>");

        Object recipe = page.recipe;
        Object[] recipeVariants = null;

        // Check if we have multiple recipe variants
        if (recipe instanceof Object[]) {
            recipeVariants = (Object[]) recipe;
        } else {
            recipeVariants = new Object[] { recipe };
        }

        // Generate unique ID for this recipe set
        String recipeSetId = "infusion_recipe_set_" + System.identityHashCode(page);

        writer.println("<div id='" + recipeSetId + "' class='recipe-variants'>");

        // Render all recipe variants (hidden except first)
        for (int variantIndex = 0; variantIndex < recipeVariants.length; variantIndex++) {
            InfusionRecipe infusion = null;
            Object currentRecipe = recipeVariants[variantIndex];

            if (!(currentRecipe instanceof InfusionRecipe)) continue;
            infusion = (InfusionRecipe) currentRecipe;

            String display = variantIndex == 0 ? "block" : "none";
            writer.println(
                "<div class='recipe-variant' style='display: " + display + ";' data-variant='" + variantIndex + "'>");

            // Instability with color coding
            String instabilityLevel = getInstabilityLevel(infusion.getInstability());
            String instabilityColor = getInstabilityColor(infusion.getInstability());
            writer.println(
                "<div style='margin: 5px 0; text-align: center;'><strong>Instability:</strong> <span style='color: "
                    + instabilityColor
                    + "; font-weight: bold;'>"
                    + instabilityLevel
                    + "</span></div>");

            // Result (above the circle)
            writer.println("<div style='text-align: center; margin: 10px 0;'>");
            writer.println(
                "<div style='color: #d4a574; font-size: 0.9em; margin-bottom: 5px; font-weight: bold;'>Result</div>");
            writer.println(
                "<div style='width: 60px; height: 60px; background-color: #443a30; border: 3px solid #d4a574; box-shadow: 0 0 10px rgba(212, 165, 116, 0.5); display: inline-flex; align-items: center; justify-content: center;'>");

            ItemStack resultStack = null;
            if (infusion.getRecipeOutput() instanceof ItemStack) {
                resultStack = (ItemStack) infusion.getRecipeOutput();
            } else {
                resultStack = infusion.getRecipeInput();
            }

            String resultIcon = getItemIcon(resultStack);
            if (resultIcon != null) {
                String recipeRef = findRecipeReference(resultStack);
                String clickable = recipeRef != null ? "item-clickable" : "";
                String onclick = recipeRef != null ? "onclick=\"location.href='#research-" + recipeRef + "'\"" : "";
                String hint = recipeRef != null ? "Click for research" : null;
                String stackSize = resultStack.stackSize > 1 ? " × " + resultStack.stackSize : "";
                String tooltipAttrs = buildTooltipAttrs(resultStack, hint);

                writer.print(
                    "<img src='" + resultIcon
                        + "' "
                        + "class='"
                        + clickable
                        + "' "
                        + "style='width: 40px; height: 40px;' "
                        + tooltipAttrs
                        + " "
                        + onclick
                        + " />");
            }

            writer.println("</div>");
            writer.println("</div>");

            // Main infusion layout - circular arrangement
            writer.println("<div style='position: relative; width: 280px; height: 280px; margin: 20px auto;'>");

            // Central item (center of circle)
            writer.println("<div style='position: absolute; left: 50%; top: 50%; transform: translate(-50%, -50%);'>");
            writer.println(
                "<div style='width: 50px; height: 50px; background-color: #2a2520; border: 2px solid #6b5d54; display: flex; align-items: center; justify-content: center;'>");

            List<ItemStack> centralVariants = getAllItemVariants(infusion.getRecipeInput());
            if (!centralVariants.isEmpty()) {
                String centralSlotId = "infusion_central_v" + variantIndex
                    + "_"
                    + System.identityHashCode(page)
                    + "_"
                    + System.identityHashCode(infusion.getRecipeInput());
                writer.print(
                    "<div class='item-cycler' id='" + centralSlotId
                        + "' style='position: static; display: flex; align-items: center; justify-content: center; width: 40px; height: 40px;'>");

                for (int i = 0; i < centralVariants.size(); i++) {
                    ItemStack variant = centralVariants.get(i);
                    String iconPath = getItemIcon(variant);
                    String recipeRef = findRecipeReference(variant);

                    if (iconPath != null) {
                        String displayStyle = i == 0 ? "block" : "none";
                        String clickable = recipeRef != null ? "item-clickable" : "";
                        String onclick = recipeRef != null ? "onclick=\"location.href='#research-" + recipeRef + "'\""
                            : "";
                        String hint = recipeRef != null ? "Click for research" : null;
                        String tooltipAttrs = buildTooltipAttrs(variant, hint);

                        writer.print(
                            "<img src='" + iconPath
                                + "' "
                                + "class='craft-item "
                                + clickable
                                + "' "
                                + "style='width: 40px; height: 40px; display: "
                                + displayStyle
                                + "; position: absolute;' "
                                + tooltipAttrs
                                + " "
                                + onclick
                                + " />");
                    }
                }

                writer.print("</div>");
                if (centralVariants.size() > 1) {
                    writer
                        .print("<script>addCycler('" + centralSlotId + "', " + centralVariants.size() + ");</script>");
                }
            }

            writer.println("</div>");
            writer.println("</div>");

            // Pedestal items in a circle
            if (infusion.getComponents() != null && infusion.getComponents().length > 0) {
                int itemCount = infusion.getComponents().length;
                double radius = 110; // Distance from center
                double centerX = 140;
                double centerY = 140;

                for (int i = 0; i < itemCount; i++) {
                    ItemStack pedestalItem = infusion.getComponents()[i];
                    if (pedestalItem == null) continue;

                    // Calculate position in circle
                    double angle = (2 * Math.PI * i / itemCount) - (Math.PI / 2); // Start from top
                    double x = centerX + radius * Math.cos(angle) - 20; // -20 to center the 40px item
                    double y = centerY + radius * Math.sin(angle) - 20;

                    List<ItemStack> pedestalVariants = getAllItemVariants(pedestalItem);
                    if (!pedestalVariants.isEmpty()) {
                        String pedestalSlotId = "infusion_pedestal_v" + variantIndex
                            + "_"
                            + i
                            + "_"
                            + System.identityHashCode(page)
                            + "_"
                            + System.identityHashCode(pedestalItem);

                        writer.println("<div style='position: absolute; left: " + x + "px; top: " + y + "px;'>");
                        writer.println(
                            "<div style='width: 40px; height: 40px; background-color: #3a3228; border: 1px solid #5a4a38; display: flex; align-items: center; justify-content: center;'>");
                        writer.print("<div class='item-cycler' id='" + pedestalSlotId + "'>");

                        for (int j = 0; j < pedestalVariants.size(); j++) {
                            ItemStack variant = pedestalVariants.get(j);
                            String iconPath = getItemIcon(variant);
                            String recipeRef = findRecipeReference(variant);

                            if (iconPath != null) {
                                String displayStyle = j == 0 ? "block" : "none";
                                String clickable = recipeRef != null ? "item-clickable" : "";
                                String onclick = recipeRef != null
                                    ? "onclick=\"location.href='#research-" + recipeRef + "'\""
                                    : "";
                                String hint = recipeRef != null ? "Click for research" : null;
                                String tooltipAttrs = buildTooltipAttrs(variant, hint);

                                writer.print(
                                    "<img src='" + iconPath
                                        + "' "
                                        + "class='craft-item "
                                        + clickable
                                        + "' "
                                        + "style='width: 32px; height: 32px; display: "
                                        + displayStyle
                                        + ";' "
                                        + tooltipAttrs
                                        + " "
                                        + onclick
                                        + " />");
                            }
                        }

                        writer.print("</div>");
                        writer.println("</div>");
                        writer.println("</div>");

                        if (pedestalVariants.size() > 1) {
                            writer.print(
                                "<script>addCycler('" + pedestalSlotId
                                    + "', "
                                    + pedestalVariants.size()
                                    + ");</script>");
                        }
                    }
                }
            }

            writer.println("</div>"); // Close circular layout

            // Aspects at the bottom
            writeAspectList(writer, infusion.getAspects(), "Aspects Required:");

            writer.println("</div>"); // Close recipe-variant
        }

        writer.println("</div>"); // Close recipe-variants

        // Add cycling script if multiple variants
        if (recipeVariants.length > 1) {
            writer.println(
                "<script>addRecipeVariantCycler('" + recipeSetId + "', " + recipeVariants.length + ");</script>");
        }

        writer.println("</div>");
    }

    // Add helper method for instability color:
    private static String getInstabilityColor(int instability) {
        int level = Math.min(5, instability / 2);
        switch (level) {
            case 0:
                return "#55ff55"; // Green - Negligible
            case 1:
                return "#99ff55"; // Light green - Minor
            case 2:
                return "#ffff55"; // Yellow - Moderate
            case 3:
                return "#ffaa00"; // Orange - High
            case 4:
                return "#ff5555"; // Red - Dangerous
            case 5:
                return "#ff0000"; // Dark red - Very Dangerous
            default:
                return "#aaaaaa";
        }
    }

    private static void dumpInfusionEnchantment(PrintWriter writer, ResearchPage page) {
        writer.println("<div class='recipe'>");
        writer.println("<div class='recipe-title'>Infusion Enchantment</div>");

        Object recipe = page.recipe;
        Object[] recipeVariants = null;

        // Check if we have multiple recipe variants
        if (recipe instanceof Object[]) {
            recipeVariants = (Object[]) recipe;
        } else {
            recipeVariants = new Object[] { recipe };
        }

        // Generate unique ID for this recipe set
        String recipeSetId = "infusion_enchant_set_" + System.identityHashCode(page);

        writer.println("<div id='" + recipeSetId + "' class='recipe-variants'>");

        // Render all recipe variants (hidden except first)
        for (int variantIndex = 0; variantIndex < recipeVariants.length; variantIndex++) {
            InfusionEnchantmentRecipe enchant = null;
            Object currentRecipe = recipeVariants[variantIndex];

            if (!(currentRecipe instanceof InfusionEnchantmentRecipe)) continue;
            enchant = (InfusionEnchantmentRecipe) currentRecipe;

            String display = variantIndex == 0 ? "block" : "none";
            writer.println(
                "<div class='recipe-variant' style='display: " + display + ";' data-variant='" + variantIndex + "'>");

            // Enchantment info
            writer.println("<div style='text-align: center; margin: 10px 0;'>");
            writer.println(
                "<div style='color: #c9a876; font-size: 1.1em; font-weight: bold;'>"
                    + enchant.enchantment.getTranslatedName(1)
                    + "</div>");
            writer.println(
                "<div style='color: #b0a090; font-size: 0.9em;'>Max Level: " + enchant.enchantment.getMaxLevel()
                    + "</div>");
            writer.println(
                "<div style='color: #b0a090; font-size: 0.9em;'>XP Cost per Level: " + enchant.recipeXP + "</div>");
            writer.println("</div>");

            // Instability with color coding
            String instabilityLevel = getInstabilityLevel(enchant.instability);
            String instabilityColor = getInstabilityColor(enchant.instability);
            writer.println(
                "<div style='margin: 5px 0; text-align: center;'><strong>Instability:</strong> <span style='color: "
                    + instabilityColor
                    + "; font-weight: bold;'>"
                    + instabilityLevel
                    + "</span></div>");

            // Main infusion layout - circular arrangement (no central item for enchantments)
            writer.println("<div style='position: relative; width: 280px; height: 280px; margin: 20px auto;'>");

            // Central icon or text indicating enchantment target
            writer.println(
                "<div style='position: absolute; left: 50%; top: 50%; transform: translate(-50%, -50%); text-align: center;'>");
            writer.println(
                "<div style='width: 80px; height: 80px; background-color: #2a2520; border: 2px solid #8b4c8b; display: flex; align-items: center; justify-content: center; color: #c9a876; font-weight: bold; font-size: 0.85em;'>");
            writer.println("Enchant<br>Item");
            writer.println("</div>");
            writer.println("</div>");

            // Pedestal items in a circle
            if (enchant.components != null && enchant.components.length > 0) {
                int itemCount = enchant.components.length;
                double radius = 110; // Distance from center
                double centerX = 140;
                double centerY = 140;

                for (int i = 0; i < itemCount; i++) {
                    ItemStack pedestalItem = enchant.components[i];
                    if (pedestalItem == null) continue;

                    // Calculate position in circle
                    double angle = (2 * Math.PI * i / itemCount) - (Math.PI / 2); // Start from top
                    double x = centerX + radius * Math.cos(angle) - 20; // -20 to center the 40px item
                    double y = centerY + radius * Math.sin(angle) - 20;

                    List<ItemStack> pedestalVariants = getAllItemVariants(pedestalItem);
                    if (!pedestalVariants.isEmpty()) {
                        String pedestalSlotId = "infusion_enchant_pedestal_v" + variantIndex
                            + "_"
                            + i
                            + "_"
                            + System.identityHashCode(page)
                            + "_"
                            + System.identityHashCode(pedestalItem);

                        writer.println("<div style='position: absolute; left: " + x + "px; top: " + y + "px;'>");
                        writer.println(
                            "<div style='width: 40px; height: 40px; background-color: #3a3228; border: 1px solid #5a4a38; display: flex; align-items: center; justify-content: center;'>");
                        writer.print(
                            "<div class='item-cycler' id='" + pedestalSlotId
                                + "' style='position: static; display: flex; align-items: center; justify-content: center; width: 32px; height: 32px;'>");

                        for (int j = 0; j < pedestalVariants.size(); j++) {
                            ItemStack variant = pedestalVariants.get(j);
                            String iconPath = getItemIcon(variant);
                            String recipeRef = findRecipeReference(variant);

                            if (iconPath != null) {
                                String displayStyle = j == 0 ? "block" : "none";
                                String clickable = recipeRef != null ? "item-clickable" : "";
                                String onclick = recipeRef != null
                                    ? "onclick=\"location.href='#research-" + recipeRef + "'\""
                                    : "";
                                String hint = recipeRef != null ? "Click for research" : null;
                                String tooltipAttrs = buildTooltipAttrs(variant, hint);

                                writer.print(
                                    "<img src='" + iconPath
                                        + "' "
                                        + "class='craft-item "
                                        + clickable
                                        + "' "
                                        + "style='width: 32px; height: 32px; display: "
                                        + displayStyle
                                        + "; position: absolute;' "
                                        + tooltipAttrs
                                        + " "
                                        + onclick
                                        + " />");
                            }
                        }

                        writer.print("</div>");
                        writer.println("</div>");
                        writer.println("</div>");

                        if (pedestalVariants.size() > 1) {
                            writer.print(
                                "<script>addCycler('" + pedestalSlotId
                                    + "', "
                                    + pedestalVariants.size()
                                    + ");</script>");
                        }
                    }
                }
            }

            writer.println("</div>"); // Close circular layout

            // Aspects at the bottom (per level)
            writeAspectList(writer, enchant.aspects, "Aspects Required (per level):");

            writer.println("</div>"); // Close recipe-variant
        }

        writer.println("</div>"); // Close recipe-variants

        // Add cycling script if multiple variants
        if (recipeVariants.length > 1) {
            writer.println(
                "<script>addRecipeVariantCycler('" + recipeSetId + "', " + recipeVariants.length + ");</script>");
        }

        writer.println("</div>");
    }

    private static void dumpSmelting(PrintWriter writer, ResearchPage page) {
        writer.println("<div class='recipe'>");
        writer.println("<div class='recipe-title'>Smelting Recipe</div>");

        Object recipe = page.recipe;
        Object[] recipeVariants = null;

        // Check if we have multiple recipe variants
        if (recipe instanceof Object[]) {
            recipeVariants = (Object[]) recipe;
        } else {
            recipeVariants = new Object[] { recipe };
        }

        // Generate unique ID for this recipe set
        String recipeSetId = "smelting_recipe_set_" + System.identityHashCode(page);

        writer.println("<div id='" + recipeSetId + "' class='recipe-variants'>");

        // Render all recipe variants (hidden except first)
        for (int variantIndex = 0; variantIndex < recipeVariants.length; variantIndex++) {
            ItemStack input = null;
            Object currentRecipe = recipeVariants[variantIndex];

            if (!(currentRecipe instanceof ItemStack)) continue;
            input = (ItemStack) currentRecipe;
            ItemStack output = FurnaceRecipes.smelting()
                .getSmeltingResult(input);

            if (output == null) continue;

            String display = variantIndex == 0 ? "flex" : "none";
            writer.println(
                "<div class='recipe-variant' style='display: " + display
                    + "; align-items: center; margin: 10px 0;' data-variant='"
                    + variantIndex
                    + "'>");

            // Input slot
            writer.println("<div style='text-align: center;'>");
            writer.println("<div style='color: #d4a574; font-size: 0.85em; margin-bottom: 3px;'>Input</div>");
            writer.println(
                "<div style='width: 50px; height: 50px; background-color: #2a2520; border: 2px solid #6b5d54; display: flex; align-items: center; justify-content: center;'>");

            List<ItemStack> inputVariants = getAllItemVariants(input);
            if (!inputVariants.isEmpty()) {
                String inputSlotId = "smelting_input_v" + variantIndex
                    + "_"
                    + System.identityHashCode(page)
                    + "_"
                    + System.identityHashCode(input);
                writer.print(
                    "<div class='item-cycler' id='" + inputSlotId
                        + "' style='position: static; display: flex; align-items: center; justify-content: center; width: 40px; height: 40px;'>");

                for (int i = 0; i < inputVariants.size(); i++) {
                    ItemStack variant = inputVariants.get(i);
                    String iconPath = getItemIcon(variant);
                    String recipeRef = findRecipeReference(variant);

                    if (iconPath != null) {
                        String displayStyle = i == 0 ? "block" : "none";
                        String clickable = recipeRef != null ? "item-clickable" : "";
                        String onclick = recipeRef != null ? "onclick=\"location.href='#research-" + recipeRef + "'\""
                            : "";
                        String hint = recipeRef != null ? "Click for research" : null;
                        String tooltipAttrs = buildTooltipAttrs(variant, hint);

                        writer.print(
                            "<img src='" + iconPath
                                + "' "
                                + "class='craft-item "
                                + clickable
                                + "' "
                                + "style='width: 40px; height: 40px; display: "
                                + displayStyle
                                + "; position: absolute;' "
                                + tooltipAttrs
                                + " "
                                + onclick
                                + " />");
                    }
                }

                writer.print("</div>");
                if (inputVariants.size() > 1) {
                    writer.print("<script>addCycler('" + inputSlotId + "', " + inputVariants.size() + ");</script>");
                }
            }

            writer.println("</div>");
            writer.println("</div>");

            // Furnace icon/indicator
            writer.println("<div style='margin: 0 15px; text-align: center;'>");
            writer.println("<div style='font-size: 32px;'>🔥</div>");
            writer.println("<div style='color: #a0907a; font-size: 0.8em;'>Furnace</div>");
            writer.println("</div>");

            // Output slot
            writer.println("<div style='text-align: center;'>");
            writer.println("<div style='color: #d4a574; font-size: 0.85em; margin-bottom: 3px;'>Output</div>");
            writer.println(
                "<div style='width: 50px; height: 50px; background-color: #443a30; border: 2px solid #d4a574; display: flex; align-items: center; justify-content: center;'>");

            String outputIcon = getItemIcon(output);
            if (outputIcon != null) {
                String recipeRef = findRecipeReference(output);
                String clickable = recipeRef != null ? "item-clickable" : "";
                String onclick = recipeRef != null ? "onclick=\"location.href='#research-" + recipeRef + "'\"" : "";
                String hint = recipeRef != null ? "Click for research" : null;
                String stackSize = output.stackSize > 1 ? " × " + output.stackSize : "";
                String tooltipAttrs = buildTooltipAttrs(output, hint);

                writer.print(
                    "<img src='" + outputIcon
                        + "' "
                        + "class='"
                        + clickable
                        + "' "
                        + "style='width: 40px; height: 40px;' "
                        + tooltipAttrs
                        + " "
                        + onclick
                        + " />");
            }

            writer.println("</div>");
            writer.println("</div>");

            writer.println("</div>"); // Close recipe-variant
        }

        writer.println("</div>"); // Close recipe-variants

        // Add cycling script if multiple variants
        if (recipeVariants.length > 1) {
            writer.println(
                "<script>addRecipeVariantCycler('" + recipeSetId + "', " + recipeVariants.length + ");</script>");
        }

        writer.println("</div>");
    }

    private static void dumpCompoundCrafting(PrintWriter writer, ResearchPage page) {
        writer.println("<div class='recipe'>");
        writer.println("<div class='recipe-title'>Mystical Construct</div>");

        Object recipe = page.recipe;
        Object[] recipeVariants = null;

        // Check if we have multiple recipe variants
        if (recipe instanceof Object[]) {
            recipeVariants = (Object[]) recipe;
        } else {
            recipeVariants = new Object[] { recipe };
        }

        // Generate unique ID for this recipe set
        String recipeSetId = "compound_recipe_set_" + System.identityHashCode(page);

        writer.println("<div id='" + recipeSetId + "' class='recipe-variants'>");

        // Render all recipe variants (hidden except first)
        for (int variantIndex = 0; variantIndex < recipeVariants.length; variantIndex++) {
            List recipeList = null;
            Object currentRecipe = recipeVariants[variantIndex];

            if (!(currentRecipe instanceof List)) continue;
            recipeList = (List) currentRecipe;

            if (recipeList.size() < 5) continue;

            AspectList aspects = (AspectList) recipeList.get(0);
            int dx = (Integer) recipeList.get(1);
            int dy = (Integer) recipeList.get(2);
            int dz = (Integer) recipeList.get(3);
            List<Object> items = (List<Object>) recipeList.get(4);

            String display = variantIndex == 0 ? "block" : "none";
            writer.println(
                "<div class='recipe-variant' style='display: " + display + ";' data-variant='" + variantIndex + "'>");

            // Dimensions
            writer.println("<div style='text-align: center; margin: 10px 0; color: #b0a090;'>");
            writer.println("<strong>Dimensions:</strong> " + dx + " × " + dy + " × " + dz);
            writer.println("</div>");

            // Aspects
            writeAspectList(writer, aspects, "Aspects Required:");

            // Isometric-style multiblock visualization
            int blockSize = 40; // visual size of each block icon
            int isoX = 24; // horizontal isometric offset
            int isoY = 12; // vertical isometric offset
            int layerHeight = 100; // vertical distance between Y levels

            int xoff = 200 - (dx * isoX + dz * isoX) / 2;
            int yoff = 50;

            int count = 0;

            // Calculate actual height needed for isometric projection
            // Base height + layers + extra padding for blocks at bottom
            int containerHeight = Math.max(200, 100 + dy * layerHeight + 60);
            writer.println(
                "<div style='position: relative; width: 400px; height: " + containerHeight
                    + "px; margin: 20px auto;'>");

            // Loop through layers back-to-front (correct isometric ordering)
            for (int j = 0; j < dy; j++) { // height (Y)
                for (int k = 0; k < dz; k++) { // depth (Z)
                    for (int i = 0; i < dx; i++) { // width (X)

                        if (count >= items.size()) break;

                        Object item = items.get(count);
                        if (item != null) {

                            // --- Calculate isometric projection ---
                            int px = xoff + (i - k) * isoX;
                            int py = yoff + (i + k) * isoY + j * layerHeight;

                            // --- Correct z-index ---
                            // Higher j = above
                            // Higher i,k = further forward
                            int zIndex = j * 10000 + // layers dominate everything
                                (i + k) * 100 + // blocks in front overlap those behind
                                (dx * dz - (i + k));

                            List<ItemStack> blockVariants = getAllItemVariants(item);
                            if (!blockVariants.isEmpty()) {

                                String blockSlotId = "compound_block_" + variantIndex
                                    + "_"
                                    + count
                                    + "_"
                                    + System.identityHashCode(page)
                                    + "_"
                                    + System.identityHashCode(item);

                                writer.println(
                                    "<div style='" + "position: absolute;"
                                        + " left: "
                                        + px
                                        + "px;"
                                        + " top: "
                                        + py
                                        + "px;"
                                        + " width:"
                                        + blockSize
                                        + "px;"
                                        + " height:"
                                        + blockSize
                                        + "px;"
                                        + " z-index: "
                                        + zIndex
                                        + ";'>");

                                // Container for cycling variants
                                writer.print(
                                    "<div class='item-cycler' id='" + blockSlotId
                                        + "' "
                                        + "style='width:"
                                        + blockSize
                                        + "px; height:"
                                        + blockSize
                                        + "px; position: relative;'>");

                                // Render block variants
                                for (int v = 0; v < blockVariants.size(); v++) {
                                    ItemStack variant = blockVariants.get(v);
                                    String iconPath = getItemIcon(variant);
                                    String recipeRef = findRecipeReference(variant);

                                    if (iconPath != null) {
                                        String displayStyle = v == 0 ? "block" : "none";
                                        String clickable = recipeRef != null ? "item-clickable" : "";
                                        String onclick = recipeRef != null
                                            ? "onclick=\"location.href='#research-" + recipeRef + "'\""
                                            : "";
                                        String hint = recipeRef != null ? "Click for research" : null;
                                        String tooltipAttrs = buildTooltipAttrs(variant, hint);

                                        writer.print(
                                            "<img src='" + iconPath
                                                + "' "
                                                + "class='craft-item "
                                                + clickable
                                                + "' "
                                                + "style='"
                                                + "width:"
                                                + blockSize
                                                + "px;"
                                                + "height:"
                                                + blockSize
                                                + "px;"
                                                + "display:"
                                                + displayStyle
                                                + ";"
                                                + "position:absolute;"
                                                + "' "
                                                + tooltipAttrs
                                                + " "
                                                + onclick
                                                + " />");
                                    }
                                }

                                writer.print("</div>"); // end cycler
                                writer.println("</div>"); // end block container

                                // Add JS cycler if needed
                                if (blockVariants.size() > 1) {
                                    writer.println(
                                        "<script>addCycler('" + blockSlotId
                                            + "', "
                                            + blockVariants.size()
                                            + ");</script>");
                                }
                            }
                        }
                        count++;
                    }
                }
            }

            writer.println("</div>"); // Close isometric layout

            // Legend showing unique blocks needed
            writer.println("<div style='margin: 20px 0;'><strong>Blocks Required:</strong></div>");
            writer.println("<div style='display: flex; flex-wrap: wrap; gap: 10px; justify-content: center;'>");

            // Count unique items
            java.util.Map<String, ItemStack> uniqueBlocks = new java.util.HashMap<>();
            java.util.Map<String, Integer> blockCounts = new java.util.HashMap<>();

            for (Object item : items) {
                if (item != null) {
                    List<ItemStack> variants = getAllItemVariants(item);
                    if (!variants.isEmpty()) {
                        ItemStack stack = variants.get(0);
                        String key = stack.getDisplayName();
                        uniqueBlocks.put(key, stack);
                        blockCounts.put(key, blockCounts.getOrDefault(key, 0) + 1);
                    }
                }
            }

            // Display unique blocks with counts
            for (java.util.Map.Entry<String, ItemStack> entry : uniqueBlocks.entrySet()) {
                ItemStack stack = entry.getValue();
                int count_ = blockCounts.get(entry.getKey());
                String iconPath = getItemIcon(stack);

                if (iconPath != null) {
                    String recipeRef = findRecipeReference(stack);
                    String clickable = recipeRef != null ? "item-clickable" : "";
                    String onclick = recipeRef != null ? "onclick=\"location.href='#research-" + recipeRef + "'\"" : "";
                    String hint = recipeRef != null ? "Click for research" : null;
                    String tooltipAttrs = "onmouseenter=\"showTooltip(event, '" + stack.getDisplayName()
                        .replace("'", "\\'")
                        + "'"
                        + (hint != null ? ", '" + hint + "'" : ", null")
                        + ")\" "
                        + "onmouseleave=\"hideTooltip()\"";

                    writer.println(
                        "<div style='display: flex; align-items: center; gap: 5px; padding: 5px 10px; background-color: #3a3228; border: 1px solid #5a4a38; border-radius: 3px;'>");
                    writer.print(
                        "<img src='" + iconPath
                            + "' "
                            + "class='"
                            + clickable
                            + "' "
                            + "style='width: 24px; height: 24px;' "
                            + tooltipAttrs
                            + " "
                            + onclick
                            + " />");
                    writer.println(
                        "<span style='color: #c9b896;'>" + stack.getDisplayName() + " × " + count_ + "</span>");
                    writer.println("</div>");
                }
            }

            writer.println("</div>");

            writer.println("</div>"); // Close recipe-variant
        }

        writer.println("</div>"); // Close recipe-variants

        // Add cycling script if multiple variants
        if (recipeVariants.length > 1) {
            writer.println(
                "<script>addRecipeVariantCycler('" + recipeSetId + "', " + recipeVariants.length + ");</script>");
        }

        writer.println("</div>");
    }

    private static String getInstabilityLevel(int instability) {
        int level = Math.min(5, instability / 2);
        switch (level) {
            case 0:
                return "Negligible";
            case 1:
                return "Minor";
            case 2:
                return "Moderate";
            case 3:
                return "High";
            case 4:
                return "Dangerous";
            case 5:
                return "Very Dangerous";
            default:
                return "Unknown";
        }
    }
}
