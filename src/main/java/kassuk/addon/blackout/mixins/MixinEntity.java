package kassuk.addon.blackout.mixins;

import kassuk.addon.blackout.modules.AntiCrawl;
import kassuk.addon.blackout.modules.ForceSneak;
import kassuk.addon.blackout.modules.StepPlus;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.Step;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

import static meteordevelopment.meteorclient.MeteorClient.mc;

@Mixin(Entity.class)
public abstract class MixinEntity {
    @Shadow
    public abstract boolean isInPose(EntityPose pose);

    @Shadow
    public abstract Text getName();

    @Shadow
    public abstract World getWorld();

    @Shadow
    public abstract ActionResult interact(PlayerEntity player, Hand hand);

    @Shadow
    protected abstract void fall(double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition);

    @Shadow
    protected abstract boolean stepOnBlock(BlockPos pos, BlockState state, boolean playSound, boolean emitEvent, Vec3d movement);

    @Shadow
    public abstract float getStepHeight();

    @Shadow
    public abstract boolean isOnGround();

    @Shadow
    public abstract Box getBoundingBox();

    @Inject(method = "adjustMovementForCollisions(Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;", at = @At("HEAD"), cancellable = true)
    private void inject(Vec3d movement, CallbackInfoReturnable<Vec3d> cir) {
        StepPlus step = Modules.get().get(StepPlus.class);
        Entity entity = (Entity)(Object)this;

        if (step.isActive() && step.slow.get()) {
            step.slowStep(entity, movement, cir);
            return;
        }

        Box box = this.getBoundingBox();
        List<VoxelShape> list = this.getWorld().getEntityCollisions(entity, box.stretch(movement));
        Vec3d vec3d = movement.lengthSquared() == 0.0 ? movement : Entity.adjustMovementForCollisions(entity, movement, box, this.getWorld(), list);
        boolean bl = movement.x != vec3d.x;
        boolean bl2 = movement.y != vec3d.y;
        boolean bl3 = movement.z != vec3d.z;
        boolean bl4 = this.isOnGround() || (!step.isActive() && bl2 && movement.y < 0.0);
        if ((step.isActive() ? step.height.get() : this.getStepHeight()) > 0.0F && bl4 && (bl || bl3)) {
            Vec3d vec3d2 = Entity.adjustMovementForCollisions(entity, new Vec3d(movement.x, step.isActive() ? step.height.get() : this.getStepHeight(), movement.z), box, this.getWorld(), list);
            Vec3d vec3d3 = Entity.adjustMovementForCollisions(entity, new Vec3d(0.0, step.isActive() ? step.height.get() : this.getStepHeight(), 0.0), box.stretch(movement.x, 0.0, movement.z), this.getWorld(), list);
            if (vec3d3.y < (step.isActive() ? step.height.get() : this.getStepHeight())) {
                Vec3d vec3d4 = Entity.adjustMovementForCollisions(entity, new Vec3d(movement.x, 0.0, movement.z), box.offset(vec3d3), this.getWorld(), list).add(vec3d3);
                if (vec3d4.horizontalLengthSquared() > vec3d2.horizontalLengthSquared()) {
                    vec3d2 = vec3d4;
                }
            }

            if (vec3d2.horizontalLengthSquared() > vec3d.horizontalLengthSquared()) {
                Vec3d v = vec3d2.add(Entity.adjustMovementForCollisions(entity, new Vec3d(0.0, -vec3d2.y + movement.y, 0.0), box.offset(vec3d2), entity.getWorld(), list));
                step.step(step.getOffsets(v.y));
                cir.setReturnValue(v);
                return;
            }
        }

        cir.setReturnValue(vec3d);
    }

    private void i(Vec3d movement, CallbackInfoReturnable<Vec3d> cir) {
        Entity entity = ((Entity)(Object) this);
        StepPlus step = Modules.get().get(StepPlus.class);

        if (step.isActive()) {
            // Step

            Box box = this.getBoundingBox();
            List<VoxelShape> list = this.getWorld().getEntityCollisions(entity, box.stretch(movement));
            Vec3d vec3d = movement.lengthSquared() == 0.0 ? movement : Entity.adjustMovementForCollisions(entity, movement, box, this.getWorld(), list);
            boolean bl = movement.x != vec3d.x;
            boolean bl3 = movement.z != vec3d.z;
            boolean bl4 = this.isOnGround() || (step.slow.get() && step.stepping);
            if (bl4 && (bl || bl3)) {
                Vec3d vec3d2 = Entity.adjustMovementForCollisions(entity, new Vec3d(movement.x, step.stepping && step.currentOffsets != null ? step.currentOffsets[step.index] : step.height.get(), movement.z), box, this.getWorld(), list);
                Vec3d vec3d3 = Entity.adjustMovementForCollisions(entity, new Vec3d(0.0, step.stepping && step.currentOffsets != null ? step.currentOffsets[step.index] : step.height.get(), 0.0), box.stretch(movement.x, 0.0, movement.z), this.getWorld(), list);
                if (vec3d3.y < step.height.get() || step.stepping) {
                    Vec3d vec3d4 = Entity.adjustMovementForCollisions(entity, new Vec3d(movement.x, 0.0, movement.z), box.offset(vec3d3), this.getWorld(), list).add(vec3d3);
                    if (vec3d4.horizontalLengthSquared() > vec3d2.horizontalLengthSquared() || step.stepping) {
                        if (step.slow.get() && !step.stepping) {
                            step.currentOffsets = step.getOffsets(vec3d3.y);

                            if (step.currentOffsets != null) {
                                step.stepping = true;
                                step.index = 0;
                            }
                        }
                        vec3d2 = vec3d4;
                    }
                }

                if (vec3d2.horizontalLengthSquared() > vec3d.horizontalLengthSquared() || (step.stepping && step.currentOffsets != null)) {
                    Vec3d m = vec3d2.add(Entity.adjustMovementForCollisions(entity, new Vec3d(0.0, -vec3d2.y + movement.y, 0.0), box.offset(vec3d2), this.getWorld(), list));

                    if (!step.slow.get()) {
                        step.step(step.getOffsets(m.y));
                        cir.setReturnValue(m);
                        return;
                    } else {
                        ((IVec3d) m).setY(step.currentOffsets[step.index]);
                        if (step.index >= step.currentOffsets.length) {
                            step.stepping = false;
                        }
                        cir.setReturnValue(m);
                        return;
                    }
                }
            }

            cir.setReturnValue(vec3d);
            return;
        }

        // Vanilla
        Box box = this.getBoundingBox();
        List<VoxelShape> list = this.getWorld().getEntityCollisions(entity, box.stretch(movement));
        Vec3d vec3d = movement.lengthSquared() == 0.0 ? movement : Entity.adjustMovementForCollisions(entity, movement, box, this.getWorld(), list);
        boolean bl = movement.x != vec3d.x;
        boolean bl2 = movement.y != vec3d.y;
        boolean bl3 = movement.z != vec3d.z;
        boolean bl4 = this.isOnGround() || bl2 && movement.y < 0.0;
        if (getStepHeight() > 0.0F && bl4 && (bl || bl3)) {
            Vec3d vec3d2 = Entity.adjustMovementForCollisions(entity, new Vec3d(movement.x, getStepHeight(), movement.z), box, this.getWorld(), list);
            Vec3d vec3d3 = Entity.adjustMovementForCollisions(entity, new Vec3d(0.0, getStepHeight(), 0.0), box.stretch(movement.x, 0.0, movement.z), this.getWorld(), list);
            if (vec3d3.y < getStepHeight()) {
                Vec3d vec3d4 = Entity.adjustMovementForCollisions(entity, new Vec3d(movement.x, 0.0, movement.z), box.offset(vec3d3), this.getWorld(), list).add(vec3d3);
                if (vec3d4.horizontalLengthSquared() > vec3d2.horizontalLengthSquared()) {
                    vec3d2 = vec3d4;
                }
            }

            if (vec3d2.horizontalLengthSquared() > vec3d.horizontalLengthSquared()) {
                cir.setReturnValue(vec3d2.add(Entity.adjustMovementForCollisions(entity, new Vec3d(0.0, -vec3d2.y + movement.y, 0.0), box.offset(vec3d2), this.getWorld(), list)));
                return;
            }
        }

        cir.setReturnValue(vec3d);
    }

    @Inject(method = "isInSneakingPose", at = @At(value = "RETURN"), cancellable = true)
    private void isSneaking(CallbackInfoReturnable<Boolean> cir) {
        if (mc.player == null || this.getName() != mc.player.getName()) {
            cir.setReturnValue(Modules.get().get(ForceSneak.class).isActive() || this.isInPose(EntityPose.CROUCHING));
        }
    }

    @Inject(method = "wouldPoseNotCollide", at = @At("RETURN"), cancellable = true)
    private void poseNotCollide(EntityPose pose, CallbackInfoReturnable<Boolean> cir) {
        if (Modules.get().isActive(AntiCrawl.class)) {
            cir.setReturnValue(true);
        }
    }
}
