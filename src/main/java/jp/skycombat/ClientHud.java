package jp.skycombat;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import com.mojang.blaze3d.systems.RenderSystem;

@Mod.EventBusSubscriber(modid = SkyCombat.ID, value = Dist.CLIENT)
public final class ClientHud {
    private static final ResourceLocation RING = new ResourceLocation(SkyCombat.ID, "textures/gui/portrait_ring.png");
    private static final ResourceLocation ESTELLE = new ResourceLocation(SkyCombat.ID, "textures/gui/estelle_face.png");
    private static volatile List<HudNetwork.Entry> latest = List.of();
    private static volatile long receivedAt;
    private ClientHud() {}
    static void accept(HudNetwork.Update update) {
        latest = List.copyOf(update.entries());
        receivedAt = System.currentTimeMillis();
    }
    @Mod.EventBusSubscriber(modid = SkyCombat.ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class Registration {
        @SubscribeEvent public static void register(RegisterGuiOverlaysEvent event) {
            event.registerAboveAll("party_status", (gui, graphics, partialTick, width, height) -> draw(graphics, width));
        }
    }
    @SubscribeEvent public static void hideHearts(RenderGuiOverlayEvent.Pre event) {
        if (event.getOverlay().id().equals(VanillaGuiOverlay.PLAYER_HEALTH.id())
                && !latest.isEmpty() && System.currentTimeMillis() - receivedAt < 3000) {
            event.setCanceled(true);
        }
    }
    private static void draw(GuiGraphics g, int screenWidth) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui || latest.isEmpty()
                || System.currentTimeMillis() - receivedAt > 3000) return;
        // Original entry is ~194x80 GUI pixels; render at 50% while keeping the right edge anchored.
        int x = screenWidth - 100;
        int y = 12;
        for (HudNetwork.Entry entry : latest) {
            g.pose().pushPose();
            g.pose().translate(x, y, 0);
            g.pose().scale(0.5f, 0.5f, 1.0f);
            drawEntry(g, mc, 0, 0, entry);
            g.pose().popPose();
            y += 42;
            if (y > mc.getWindow().getGuiScaledHeight() - 42) break;
        }
    }
    private static void drawEntry(GuiGraphics g, Minecraft mc, int x, int y, HudNetwork.Entry e) {
        int bx = x + 5;
        int portraitX = x + 114;
        // Dark gold-edged backing, two horizontal Trails-style bars.
        g.fill(bx, y + 21, x + 139, y + 67, 0xD012151C);
        g.fill(bx, y + 20, x + 139, y + 22, 0xFFBD9B4A);
        g.fill(bx, y + 66, x + 139, y + 68, 0xFFBD9B4A);
        drawBar(g, bx + 7, y + 38, 112, 7, e.hp(), e.maxHp(), 0xFFF9DA31);
        drawBar(g, bx + 7, y + 57, 112, 6, e.ep(), e.maxEp(), 0xFF2498F0);
        g.drawString(mc.font, "HP", bx + 8, y + 25, 0xFFF9DA31, true);
        g.drawString(mc.font, Integer.toString(e.hp()), bx + 42, y + 25, 0xFFFFFFFF, true);
        g.drawString(mc.font, "EP", bx + 8, y + 46, 0xFF78CCFF, true);
        g.drawString(mc.font, Integer.toString(e.ep()), bx + 42, y + 46, 0xFFFFFFFF, true);
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        if (e.id().equals("player")) {
            var info = mc.getConnection() == null ? null : mc.getConnection().getPlayerInfo(mc.player.getUUID());
            if (info != null) {
                ResourceLocation skin = info.getSkinLocation();
                // Classic Minecraft skin face + hat layer, texture size 64x64.
                g.blit(skin, portraitX + 12, y + 11, 56, 56, 8, 8, 8, 8, 64, 64);
                g.blit(skin, portraitX + 12, y + 11, 56, 56, 40, 8, 8, 8, 64, 64);
            }
        } else if (e.id().equals("estelle")) {
            g.blit(ESTELLE, portraitX + 10, y + 9, 60, 60, 0, 0, 64, 64, 64, 64);
        } else {
            g.fill(portraitX + 12, y + 11, portraitX + 68, y + 67, 0xFF35455A);
            g.drawString(mc.font, "?", portraitX + 35, y + 35, 0xFFFFFFFF, true);
        }
        // User-supplied circular CP ring; cyan interior has been made transparent.
        g.blit(RING, portraitX, y, 0, 0, 80, 80, 80, 80);
        // The supplied frame already contains a CP label at the lower right.
        g.drawString(mc.font, Integer.toString(e.cp()), portraitX + 60, y + 67, 0xFFFFFFFF, true);
        RenderSystem.disableBlend();
    }
    private static void drawBar(GuiGraphics g, int x, int y, int w, int h, int value, int max, int color) {
        g.fill(x, y, x + w, y + h, 0xFF0B0D12);
        int filled = (int)(w * Math.max(0.0, Math.min(1.0, value / (double)Math.max(1, max))));
        if (filled > 0) g.fill(x + 1, y + 1, x + Math.max(1, filled), y + h - 1, color);
    }
}
