package com.ovo.lastsongofelysian.client;

import com.ovo.lastsongofelysian.lastsongofelysian;
import com.ovo.lastsongofelysian.network.AbyssFlowerActionPacket;
import com.ovo.lastsongofelysian.network.AbyssFlowerStatePacket;
import com.ovo.lastsongofelysian.network.ModNetwork;
import com.ovo.lastsongofelysian.util.AbyssFlowerRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = lastsongofelysian.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class AbyssFlowerClient {
    private static final Map<Integer, Domain> DOMAINS = new HashMap<>();
    private static Object previousLevel;

    private AbyssFlowerClient() {
    }

    public static void accept(AbyssFlowerStatePacket packet) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) return;
        if (previousLevel != minecraft.level) {
            DOMAINS.clear();
            previousLevel = minecraft.level;
        }
        if (!packet.active()) {
            DOMAINS.remove(packet.casterId());
            return;
        }
        DOMAINS.put(packet.casterId(), new Domain(new Vec3(packet.x(), packet.y(), packet.z()),
                minecraft.level.getGameTime() + packet.duration()));
        if (packet.casterId() == minecraft.player.getId()) {
            minecraft.keyboardHandler.setClipboard(Component.translatable("tooltip.lastsongofelysian.abyss_flower."
                    + packet.power().name().toLowerCase(java.util.Locale.ROOT) + ".chant").getString());
            minecraft.player.displayClientMessage(Component.translatable("message.lastsongofelysian.abyss_flower.chant_copied"), true);
            minecraft.setScreen(new ChatScreen(""));
        }
    }

    public static boolean shouldFreeze(Entity entity) {
        if (entity.level() != previousLevel) return false;
        for (Map.Entry<Integer, Domain> entry : DOMAINS.entrySet()) {
            if (entry.getKey() != entity.getId() && entity.level().getGameTime() < entry.getValue().endTick
                    && entry.getValue().center.distanceToSqr(entity.position()) <= AbyssFlowerRules.CHANT_RADIUS * AbyssFlowerRules.CHANT_RADIUS) return true;
        }
        return false;
    }

    @SubscribeEvent
    public static void onEscape(ScreenEvent.KeyPressed.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (event.getScreen() instanceof ChatScreen && event.getKeyCode() == GLFW.GLFW_KEY_ESCAPE
                && minecraft.player != null && DOMAINS.containsKey(minecraft.player.getId())) {
            ModNetwork.CHANNEL.sendToServer(new AbyssFlowerActionPacket(AbyssFlowerActionPacket.Action.CANCEL));
        }
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (previousLevel != minecraft.level) {
            DOMAINS.clear();
            previousLevel = minecraft.level;
        }
        if (minecraft.level != null) DOMAINS.values().removeIf(domain -> minecraft.level.getGameTime() >= domain.endTick);
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        DOMAINS.clear();
        previousLevel = null;
    }

    @SubscribeEvent
    public static void onTooltip(RenderTooltipEvent.GatherComponents event) {
        if (event.getItemStack().is(com.ovo.lastsongofelysian.registry.ModItems.ABYSS_FLOWER.get())) {
            event.setMaxWidth(Math.min(600, Math.max(120, event.getScreenWidth() - 40)));
        }
    }

    @SubscribeEvent
    public static void onTooltipColor(RenderTooltipEvent.Color event) {
        if (event.getItemStack().is(com.ovo.lastsongofelysian.registry.ModItems.ABYSS_FLOWER.get())) {
            event.setBackground(0xF0797280);
            event.setBorderStart(0xFFC3A1EC);
            event.setBorderEnd(0xFFB1A6D2);
        }
    }

    private record Domain(Vec3 center, long endTick) {
    }
}
