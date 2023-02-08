package kassuk.addon.blackout.mixins;

import kassuk.addon.blackout.managers.Managers;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static meteordevelopment.meteorclient.MeteorClient.mc;

@Mixin(LivingEntityRenderer.class)
public abstract class MixinLivingEntityRenderer<T extends LivingEntity> {

    // Own Rotations
    @ModifyVariable(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
        ordinal = 3, at = @At(value = "STORE", ordinal = 0))
    public float changeHeadYaw(float oldValue, LivingEntity entity) {
        return entity.equals(mc.player) && Managers.ROTATION.lastDir != null && Managers.ROTATION.target != null && Managers.ROTATION.timer > 0 ? Managers.ROTATION.rot[0] : oldValue;
    }

    @ModifyVariable(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
        ordinal = 2, at = @At(value = "STORE", ordinal = 0))
    public float changeYaw(float oldValue, LivingEntity entity) {
        return entity.equals(mc.player) && Managers.ROTATION.lastDir != null && Managers.ROTATION.target != null && Managers.ROTATION.timer > 0 ? Managers.ROTATION.rot[0] : oldValue;
    }

    @ModifyVariable(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
        ordinal = 5, at = @At(value = "STORE", ordinal = 3))
    public float changePitch(float oldValue, LivingEntity entity) {
        return entity.equals(mc.player) && Managers.ROTATION.lastDir != null && Managers.ROTATION.target != null && Managers.ROTATION.timer > 0 ? Managers.ROTATION.rot[1] : oldValue;
    }
}
