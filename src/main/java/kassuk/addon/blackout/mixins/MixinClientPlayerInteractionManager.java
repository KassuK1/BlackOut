package kassuk.addon.blackout.mixins;

import kassuk.addon.blackout.modules.AutoMine;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.network.SequencedPacketCreator;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class MixinClientPlayerInteractionManager {
    @Shadow
    public abstract void sendSequencedPacket(ClientWorld world, SequencedPacketCreator packetCreator);

    @Shadow
    public abstract boolean breakBlock(BlockPos pos);

    @Shadow
    @Final
    private MinecraftClient client;
    @Shadow
    private float blockBreakingSoundCooldown;
    @Shadow
    private float currentBreakingProgress;
    @Shadow
    private ItemStack selectedStack;
    @Shadow
    private BlockPos currentBreakingPos;
    @Shadow
    private boolean breakingBlock;

    @Shadow
    public abstract int getBlockBreakingProgress();

    private BlockPos position = null;

    @Inject(method = "attackBlock", at = @At("HEAD"))
    private void onAttack(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        position = pos;
    }

    @Redirect(method = "attackBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;sendSequencedPacket(Lnet/minecraft/client/world/ClientWorld;Lnet/minecraft/client/network/SequencedPacketCreator;)V", ordinal = 1))
    private void onStart(ClientPlayerInteractionManager instance, ClientWorld world, SequencedPacketCreator packetCreator) {
        AutoMine autoMine = Modules.get().get(AutoMine.class);

        if (!autoMine.isActive()) {
            sendSequencedPacket(world, packetCreator);
            return;
        }

        BlockState blockState = world.getBlockState(position);
        boolean bl = !blockState.isAir();
        if (bl && this.currentBreakingProgress == 0.0F) {
            blockState.onBlockBreakStart(this.client.world, position, this.client.player);
        }

        if (bl && blockState.calcBlockBreakingDelta(this.client.player, this.client.player.getWorld(), position) >= 1.0F) {
            this.breakBlock(position);
        } else {
            breakingBlock = true;
            currentBreakingPos = position;
            selectedStack = this.client.player.getMainHandStack();
            currentBreakingProgress = 0.0F;
            blockBreakingSoundCooldown = 0.0F;
            client.world.setBlockBreakingInfo(this.client.player.getId(), this.currentBreakingPos, getBlockBreakingProgress());
        }

        autoMine.onStart(position);
    }

    @Redirect(method = "attackBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V", ordinal = 0))
    private void onAbort(ClientPlayNetworkHandler instance, Packet<?> packet) {
        AutoMine autoMine = Modules.get().get(AutoMine.class);

        if (!autoMine.isActive()) {
            instance.sendPacket(packet);
            return;
        }

        autoMine.onAbort(position);
    }

    @Inject(method = "updateBlockBreakingProgress", at = @At("HEAD"))
    private void onUpdateProgress(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        position = pos;
    }

    @Redirect(method = "updateBlockBreakingProgress", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;sendSequencedPacket(Lnet/minecraft/client/world/ClientWorld;Lnet/minecraft/client/network/SequencedPacketCreator;)V", ordinal = 1))
    private void onStop(ClientPlayerInteractionManager instance, ClientWorld world, SequencedPacketCreator packetCreator) {
        AutoMine autoMine = Modules.get().get(AutoMine.class);

        if (!autoMine.isActive()) {
            sendSequencedPacket(world, packetCreator);
            return;
        }

        autoMine.onStop(position);
    }

    @Redirect(method = "cancelBlockBreaking", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V"))
    private void cancel(ClientPlayNetworkHandler instance, Packet<?> packet) {
        AutoMine autoMine = Modules.get().get(AutoMine.class);

        if (!autoMine.isActive()) {
            instance.sendPacket(packet);
            return;
        }

        autoMine.onAbort(currentBreakingPos);
    }
}
