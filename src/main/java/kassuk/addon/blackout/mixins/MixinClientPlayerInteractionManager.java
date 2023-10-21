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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
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

    @Unique
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
        if (bl && currentBreakingProgress == 0.0F) {
            blockState.onBlockBreakStart(client.world, position, client.player);
        }

        if (bl && blockState.calcBlockBreakingDelta(client.player, client.player.getWorld(), position) >= 1.0F) {
            breakBlock(position);
        } else {
            breakingBlock = true;
            currentBreakingPos = position;
            selectedStack = client.player.getMainHandStack();
            currentBreakingProgress = 0.0F;
            blockBreakingSoundCooldown = 0.0F;
            client.world.setBlockBreakingInfo(client.player.getId(), currentBreakingPos, getBlockBreakingProgress());
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
