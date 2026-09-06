package cc.cosmetica.cosmetica.mixin.render;

import cc.cosmetica.core.api.Accessory;
import cc.cosmetica.core.api.Cosmetics;
import cc.cosmetica.core.api.CosmeticaModel;
import cc.cosmetica.core.mixin.PlayerModelAccessor;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AvatarRenderer.class)
public abstract class AvatarRendererMixin {
        // ============================================================
        // RIGHT HAND
        // Cosmetic/Skin -> RIGHT HAND
        // ============================================================

        @Inject(method = "renderRightHand", at = @At("TAIL"))
        private void cosmetica$renderRightHand(
                        PoseStack poseStack,
                        SubmitNodeCollector collector,
                        int light,
                        Identifier texture,
                        boolean sleeve,
                        CallbackInfo ci) {
                AvatarRenderer<?> renderer = (AvatarRenderer<?>) (Object) this;

                PlayerModel model = (PlayerModel) renderer.getModel();

                // Render cosmetic on RIGHT hand
                cosmetica$renderArmAccessories(
                                model,
                                model.rightArm,
                                poseStack,
                                collector,
                                light,
                                HumanoidArm.RIGHT);
        }

        // ============================================================
        // LEFT HAND
        // Cosmetic/Skin -> LEFT HAND
        // ============================================================

        @Inject(method = "renderLeftHand", at = @At("TAIL"))
        private void cosmetica$renderLeftHand(
                        PoseStack poseStack,
                        SubmitNodeCollector collector,
                        int light,
                        Identifier texture,
                        boolean sleeve,
                        CallbackInfo ci) {
                AvatarRenderer<?> renderer = (AvatarRenderer<?>) (Object) this;

                PlayerModel model = (PlayerModel) renderer.getModel();

                // Render the SAME cosmetic on LEFT hand
                // with mirror enabled
                cosmetica$renderArmAccessories(
                                model,
                                model.leftArm,
                                poseStack,
                                collector,
                                light,
                                HumanoidArm.LEFT);
        }

        // ============================================================
        // RENDER COSMETICS ON BOTH HANDS
        // ============================================================

        private static void cosmetica$renderArmAccessories(
                        PlayerModel model,
                        ModelPart arm,
                        PoseStack poseStack,
                        SubmitNodeCollector collector,
                        int light,
                        HumanoidArm renderSide) {
                Minecraft minecraft = Minecraft.getInstance();

                if (minecraft.player == null) {
                        return;
                }

                Cosmetics.getCosmetics(minecraft.player).ifPresent(cosmetics -> {

                        for (Accessory accessory : cosmetics.getAccessories()) {

                                if (accessory == null) {
                                        continue;
                                }

                                if (accessory.getAttachment() == null) {
                                        continue;
                                }

                                CosmeticaModel cosmeticModel = accessory.getModel();

                                if (cosmeticModel == null) {
                                        continue;
                                }

                                // ====================================================
                                // ARM ACCESSORY CHECK
                                // ====================================================

                                String attachment = accessory.getAttachment().name();

                                boolean isLeftArm = "LEFT_ARM".equals(attachment);

                                boolean isRightArm = "RIGHT_ARM".equals(attachment);

                                // Ignore non-arm cosmetics
                                if (!isLeftArm && !isRightArm) {
                                        continue;
                                }

                                // ====================================================
                                // IMPORTANT:
                                //
                                // Render the arm cosmetic on BOTH sides.
                                //
                                // We intentionally DO NOT skip RIGHT_ARM when
                                // rendering the LEFT hand.
                                //
                                // This makes the same cosmetic appear on both hands.
                                // ====================================================

                                Vec3 offset = accessory.getOffset();

                                float x = (float) offset.x;
                                float y = (float) offset.y;
                                float z = (float) offset.z;

                                // ====================================================
                                // SLIM ARM CORRECTION
                                // ====================================================

                                if (((PlayerModelAccessor) (Object) model).isSlim()) {

                                        if (renderSide == HumanoidArm.LEFT) {
                                                x += 0.03125f;
                                        } else {
                                                x -= 0.03125f;
                                        }
                                }

                                // ====================================================
                                // MIRROR LEFT HAND
                                // RIGHT = normal
                                // LEFT = mirrored
                                // ====================================================

                                boolean mirrored = (renderSide == HumanoidArm.RIGHT);

                                // ====================================================
                                // RENDER
                                // ====================================================

                                cosmeticModel.submitOnPart(
                                                arm,
                                                poseStack,
                                                collector,
                                                light,
                                                x,
                                                y,
                                                z,
                                                mirrored);
                        }
                });
        }
}