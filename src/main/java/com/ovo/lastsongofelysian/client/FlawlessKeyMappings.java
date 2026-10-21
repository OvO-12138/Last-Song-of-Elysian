package com.ovo.lastsongofelysian.client;

import com.ovo.lastsongofelysian.network.FenghuangDownActionPacket;
import com.ovo.lastsongofelysian.network.AbyssFlowerActionPacket;
import com.ovo.lastsongofelysian.network.FlawlessWeaponActionPacket;
import com.ovo.lastsongofelysian.network.ModNetwork;
import com.ovo.lastsongofelysian.network.SkyfireGreatswordActionPacket;
import com.ovo.lastsongofelysian.network.EdenStarActionPacket;
import com.ovo.lastsongofelysian.registry.ModItems;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.event.TickEvent;
import org.lwjgl.glfw.GLFW;

public final class FlawlessKeyMappings {

    private static final String CATEGORY =
            "key.categories.lastsongofelysian";

    public static final KeyMapping WEAPON_SKILL =
            new KeyMapping(
                    "key.lastsongofelysian.flawless_weapon_skill",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_R,
                    CATEGORY
            );

    public static final KeyMapping SWITCH_MODE =
            new KeyMapping(
                    "key.lastsongofelysian.flawless_switch_mode",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_C,
                    CATEGORY
            );

    public static final KeyMapping ULTIMATE =
            new KeyMapping(
                    "key.lastsongofelysian.flawless_ultimate",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_V,
                    CATEGORY
            );

    private FlawlessKeyMappings() {
    }

    public static final class ForgeEvents {

        private ForgeEvents() {
        }

        public static void onClientTick(
                TickEvent.ClientTickEvent event
        ) {
            if (
                    event.phase != TickEvent.Phase.END ||
                    Minecraft.getInstance().player == null
            ) {
                return;
            }

            while (WEAPON_SKILL.consumeClick()) {
                if (isHoldingEdenStar()) {
                    send(EdenStarActionPacket.Action.ZEROTH);
                } else if (isHoldingSkyfireGreatsword()) {
                    send(SkyfireGreatswordActionPacket.Action.ZEROTH);
                } else if (isHoldingAbyssFlower()) {
                    send(AbyssFlowerActionPacket.Action.ZEROTH);
                } else if (isHoldingFenghuangDown()) {
                    send(FenghuangDownActionPacket.Action.ZEROTH);
                } else {
                    send(FlawlessWeaponActionPacket.Action.ULTIMATE);
                }
            }

            while (SWITCH_MODE.consumeClick()) {
                if (isHoldingEdenStar()) {
                    send(EdenStarActionPacket.Action.FIRST);
                } else if (isHoldingAbyssFlower()) {
                    send(AbyssFlowerActionPacket.Action.FIRST);
                } else if (isHoldingFenghuangDown()) {
                    send(FenghuangDownActionPacket.Action.FIRST);
                } else {
                    send(FlawlessWeaponActionPacket.Action.SWITCH_MODE);
                }
            }

            while (ULTIMATE.consumeClick()) {
                if (isHoldingEdenStar()) {
                    send(EdenStarActionPacket.Action.THIRD);
                } else if (isHoldingAbyssFlower()) {
                    send(AbyssFlowerActionPacket.Action.SECOND);
                } else if (isHoldingFenghuangDown()) {
                    send(FenghuangDownActionPacket.Action.SECOND);
                } else {
                    send(FlawlessWeaponActionPacket.Action.WEAPON_SKILL);
                }
            }
        }

        private static boolean isHoldingFenghuangDown() {
            Minecraft minecraft = Minecraft.getInstance();
            return minecraft.player != null
                    && (minecraft.player.getMainHandItem().is(ModItems.FENGHUANG_DOWN.get())
                    || minecraft.player.getOffhandItem().is(ModItems.FENGHUANG_DOWN.get()));
        }

        private static boolean isHoldingAbyssFlower() {
            return Minecraft.getInstance().player != null
                    && !com.ovo.lastsongofelysian.util.AbyssFlowerEnergy.findHeld(Minecraft.getInstance().player).isEmpty();
        }

        private static boolean isHoldingSkyfireGreatsword() {
            return Minecraft.getInstance().player != null
                    && !com.ovo.lastsongofelysian.util.SkyfireGreatswordEnergy
                    .findHeld(Minecraft.getInstance().player).isEmpty();
        }

        private static boolean isHoldingEdenStar() {
            return Minecraft.getInstance().player != null
                    && !com.ovo.lastsongofelysian.util.EdenStarEnergy
                    .findHeld(Minecraft.getInstance().player).isEmpty();
        }

        private static void send(AbyssFlowerActionPacket.Action action) {
            ModNetwork.CHANNEL.sendToServer(new AbyssFlowerActionPacket(action));
        }

        private static void send(
                FlawlessWeaponActionPacket.Action action
        ) {
            ModNetwork.CHANNEL.sendToServer(
                    new FlawlessWeaponActionPacket(
                            action
                    )
            );
        }

        private static void send(FenghuangDownActionPacket.Action action) {
            ModNetwork.CHANNEL.sendToServer(new FenghuangDownActionPacket(action));
        }

        private static void send(SkyfireGreatswordActionPacket.Action action) {
            ModNetwork.CHANNEL.sendToServer(new SkyfireGreatswordActionPacket(action));
        }

        private static void send(EdenStarActionPacket.Action action) {
            ModNetwork.CHANNEL.sendToServer(new EdenStarActionPacket(action));
        }
    }
}
