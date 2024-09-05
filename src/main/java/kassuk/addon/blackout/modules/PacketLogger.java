package kassuk.addon.blackout.modules;

import com.mojang.brigadier.suggestion.Suggestion;
import kassuk.addon.blackout.BlackOut;
import kassuk.addon.blackout.BlackOutModule;
import kassuk.addon.blackout.mixins.*;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.PacketListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.network.PacketUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.*;
import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;
import net.minecraft.network.packet.c2s.login.LoginHelloC2SPacket;
import net.minecraft.network.packet.c2s.login.LoginKeyC2SPacket;
import net.minecraft.network.packet.c2s.login.LoginQueryResponseC2SPacket;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.network.packet.c2s.query.QueryPingC2SPacket;
import net.minecraft.network.packet.c2s.query.QueryRequestC2SPacket;
import net.minecraft.network.packet.s2c.common.*;
import net.minecraft.network.packet.s2c.config.*;
import net.minecraft.network.packet.s2c.login.*;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.network.packet.s2c.query.PingResultS2CPacket;
import net.minecraft.network.packet.s2c.query.QueryResponseS2CPacket;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.stat.Stat;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;

import java.util.*;

/**
 * @author OLEPOSSU
 */

public class PacketLogger extends BlackOutModule {
    public PacketLogger() {
        super(BlackOut.BLACKOUT, "Logger", "Logs packets or whatever you want. (only packets rn)");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    // yoinked these settings from meteor
    private final Setting<Set<Class<? extends Packet<?>>>> receivePackets = sgGeneral.add(new PacketListSetting.Builder()
        .name("Receive")
        .description("Server-to-client packets to cancel.")
        .filter(aClass -> PacketUtils.getS2CPackets().contains(aClass))
        .build()
    );

    private final Setting<Set<Class<? extends Packet<?>>>> sendPackets = sgGeneral.add(new PacketListSetting.Builder()
        .name("Send")
        .description("Client-to-server packets to cancel.")
        .filter(aClass -> PacketUtils.getC2SPackets().contains(aClass))
        .build()
    );

    public void onSent(Packet<?> packet) {
        if (!isActive()) return;
        if (sendPackets.get().contains(packet.getClass())) {
            String message = packetMessage(packet);

            if (message == null) return;
            log(Formatting.AQUA + "Send: " + Formatting.GRAY + message);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST + 1000000000)
    private void onReceive(PacketEvent.Receive event) {
        if (receivePackets.get().contains(event.packet.getClass())) {
            String message = packetMessage(event.packet);

            if (message == null) return;
            log(Formatting.LIGHT_PURPLE + "Receive: " + Formatting.GRAY + message);
        }
    }

    private void log(String string) {
        sendMessage(Text.of(string), 0);
    }

    // this was not fun
    private String packetMessage(Packet<?> packet) {
        //----------SEND----------//

        if (packet instanceof ClientOptionsC2SPacket p) return "ClientOptions language:" + p.options().language() + " allowsServerListing:" + p.options().allowsServerListing() + " chatColorsEnabled:" + p.options().chatColorsEnabled() + " chatVisibility:" + p.options().chatVisibility().name() + " filtersText:" + p.options().filtersText() + " mainArm:" + p.options().mainArm().name() + " playerModelParts:" + p.options().playerModelParts() + " viewDistance:" + p.options().viewDistance();
        if (packet instanceof CommonPongC2SPacket p) return "CommonPong parameter:" + p.getParameter();
        //if (packet instanceof CookieResponseC2SPacket p) return "CookieResponse data:" + p.payload();
        if (packet instanceof CustomPayloadC2SPacket p) return "CustomPayload data:" + p.payload() + " channel:" + p.payload().getId();
        if (packet instanceof KeepAliveC2SPacket p) return "KeepAlive id:" + p.getId();
        if (packet instanceof ResourcePackStatusC2SPacket p) return "ResourcePackStatus status:" + p.status().name() + " id:" + p.id().toString();

        //if (packet instanceof ReadyC2SPacket p) return "Ready state:" + (p.getNewNetworkState() == null ? "null" : p.getNewNetworkState().name());

        if (packet instanceof HandshakeC2SPacket p) return "Handshake state:" + p.intendedState().name() + " address:" + p.address() + " protocol:" + p.protocolVersion();

        //if (packet instanceof EnterConfigurationC2SPacket p) return "EnterConfiguration state:" + (p.getNewNetworkState() == null ? "null" : p.getNewNetworkState().name());
        if (packet instanceof LoginHelloC2SPacket p) return "LoginHello id:" + p.profileId() + " name:" + p.name();
        if (packet instanceof LoginKeyC2SPacket) return "LoginKey";
        if (packet instanceof LoginQueryResponseC2SPacket p) return "LoginQueryResponse id:" + p.queryId();

        //if (packet instanceof AcknowledgeChunksC2SPacket p) return "AcknowledgeChunks state:" + p.getNewNetworkState() + " desired chunks per tick:" + p.desiredChunksPerTick();
        //if (packet instanceof AcknowledgeReconfigurationC2SPacket p) return "AcknowledgeReconfiguration state:" + p.getNewNetworkState();
        if (packet instanceof AdvancementTabC2SPacket p) return "AdvancementTab action:" + p.getAction() + " toOpen:" + p.getTabToOpen();
        if (packet instanceof BoatPaddleStateC2SPacket p) return "BoatPaddle isLeftPadling:" + p.isLeftPaddling() + " isRightPaddling:" + p.isRightPaddling();
        if (packet instanceof BookUpdateC2SPacket p) return "BookUpdate slot:" + p.slot() + " pages:" + p.pages().size() + " title:" + p.title();
        if (packet instanceof ButtonClickC2SPacket p) return "ButtonClick button:" + p.buttonId() + " syncId" + p.syncId();
        if (packet instanceof ChatCommandSignedC2SPacket p) return "ChatCommandSigned command:" + p.command() + " timestamp:" + p.timestamp() + " salt:" + p.salt() + " argumentSignatures:" + p.argumentSignatures() + " lastSeenMessages:" + p.lastSeenMessages();
        if (packet instanceof ChatMessageC2SPacket p) return "ChatMessage message:" + p.chatMessage();
        if (packet instanceof ClickSlotC2SPacket p) return "ClickSlot syncId:" + p.getSyncId() + " slot:" + p.getSlot() + " button:" + p.getButton() + " action:" + p.getActionType().name() + " revision" + p.getRevision() + " item:" + p.getStack().getItem() + " count:" + p.getStack().getCount();
        if (packet instanceof ClientCommandC2SPacket p) return "ClientCommand entityId:" + p.getEntityId() + " jumpHeight" + p.getMountJumpHeight() + " mode" + p.getMode().name();
        if (packet instanceof ClientStatusC2SPacket p) return "ClientStatus mode:" + p.getMode().name();
        if (packet instanceof CloseHandledScreenC2SPacket p) return "CloseHandledScreen syncId:" + p.getSyncId();
        if (packet instanceof CommandExecutionC2SPacket p) return "CommandExecution command:" + p.command();
        if (packet instanceof CraftRequestC2SPacket p) return "CraftRequest syncId:" + p.getSyncId() + " craftAll:" + p.shouldCraftAll() + " recipeId:" + p.getRecipeId();
        if (packet instanceof CreativeInventoryActionC2SPacket p) return "CreativeInventoryAction slot:" + p.slot() + " item:" + p.stack().getItem() + " count:" + p.stack().getCount();
        if (packet instanceof DebugSampleS2CPacket p) return "DebugSample sample:" + p.sample() + " sampleType:" + p.debugSampleType();
        if (packet instanceof HandSwingC2SPacket p) return "HandSwing hand:" + p.getHand().name();
        if (packet instanceof JigsawGeneratingC2SPacket p) return "JigsawGeneration x:" + p.getPos().getX() + " y:" + p.getPos().getY() + " z:" + p.getPos().getZ() + " maxDepth" + p.getMaxDepth() + " shouldKeepJigsaws:" + p.shouldKeepJigsaws();
        if (packet instanceof MessageAcknowledgmentC2SPacket p) return "MessageAcknowledgment offset:" + p.offset();
        if (packet instanceof PickFromInventoryC2SPacket p) return "PickFromInventory slot:" + p.getSlot();
        if (packet instanceof PlayerActionC2SPacket p) return "PlayerAction action:" + p.getAction() + " x:" + p.getPos().getX() + " y:" + p.getPos().getY() + " z:" + p.getPos().getZ() + " dir:" + p.getDirection().name() + " sequence:" + p.getSequence();
        if (packet instanceof PlayerInputC2SPacket p) return "PlayerInput forward:" + p.getForward() + " sideways:" + p.getSideways() + " sneaking:" + p.isSneaking() + " jumping:" + p.isJumping();
        if (packet instanceof PlayerInteractBlockC2SPacket p) return "PlayerInteractBlock type:" + p.getBlockHitResult().getType().name() + " blockX:" + p.getBlockHitResult().getBlockPos().getX() + " blockY:" + p.getBlockHitResult().getBlockPos().getY() + " blockZ:" + p.getBlockHitResult().getBlockPos().getZ() + " x:" + p.getBlockHitResult().getPos().getX() + " y:" + p.getBlockHitResult().getPos().getY() + " z:" + p.getBlockHitResult().getPos().getZ() + " side:" + p.getBlockHitResult().getSide().name() + " inside:" + p.getBlockHitResult().isInsideBlock() + " hand:" + p.getHand().name() + " sequence:" + p.getSequence();
        if (packet instanceof PlayerInteractEntityC2SPacket p) return "PlayerInteractEntity id:" + ((IInteractEntityC2SPacket) p).getId();
        if (packet instanceof PlayerInteractItemC2SPacket p) return "PlayerInteractItem hand:" + p.getHand() + " sequence:" + p.getSequence();
        if (packet instanceof PlayerMoveC2SPacket.Full p) return "PlayerMove.Full x:" + p.getX(0) + " y:" + p.getY(0) + " z:" + p.getZ(0) + " yaw:" + p.getYaw(0) + " pitch:" + p.getPitch(0) + " ground:" + p.isOnGround();
        if (packet instanceof PlayerMoveC2SPacket.PositionAndOnGround p) return "PlayerMove.Pos x:" + p.getX(0) + " y:" + p.getY(0) + " z:" + p.getZ(0) + " ground:" + p.isOnGround();
        if (packet instanceof PlayerMoveC2SPacket.LookAndOnGround p) return "PlayerMove.Look yaw:" + p.getYaw(0) + " pitch:" + p.getPitch(0) + " ground:" + p.isOnGround();
        if (packet instanceof PlayerMoveC2SPacket.OnGroundOnly p) return "PlayerMove.Ground ground:" + p.isOnGround();
        if (packet instanceof PlayerSessionC2SPacket p) return "PlayerSession sessionId:" + p.chatSession().sessionId();
        if (packet instanceof QueryBlockNbtC2SPacket p) return "QueryBlockNbt x:" + p.getPos().getX() + " y:" + p.getPos().getY() + " z:" + p.getPos().getZ() + " transactionId:" + p.getTransactionId();
        if (packet instanceof QueryEntityNbtC2SPacket p) return "QueryEntityNbt entityId" + p.getEntityId() + " transactionId" + p.getTransactionId();
        if (packet instanceof RecipeBookDataC2SPacket p) return "RecipeBookData identifier:" + p.getRecipeId();
        if (packet instanceof RecipeCategoryOptionsC2SPacket p) return "RecipeCategoryOptions guiOpen:" + p.isGuiOpen() + " filtering:" + p.isFilteringCraftable() + " category:" + p.getCategory().name();
        if (packet instanceof RenameItemC2SPacket p) return "RenameItem name:" + p.getName();
        if (packet instanceof RequestCommandCompletionsC2SPacket p) return "RequestCommandCompletions partialCommand:" + p.getPartialCommand() + " completionId:" + p.getCompletionId();
        if (packet instanceof SelectMerchantTradeC2SPacket p) return "SelectMerchantTrade tradeId:" + p.getTradeId();
        if (packet instanceof SlotChangedStateC2SPacket p) return "SlotChangedState newState:" + p.newState() + " slot:" + p.slotId();
        if (packet instanceof SpectatorTeleportC2SPacket p) return "SpectatorTeleport UUID:" + ((ISpectatorTeleportC2SPacket) p).getID();
        if (packet instanceof TeleportConfirmC2SPacket p) return "TeleportConfirm id:" + p.getTeleportId();
        if (packet instanceof UpdateBeaconC2SPacket p) return "UpdateBeacon primary:" + (p.primary().isPresent() ? p.primary().get().value().getName() : "null") + (p.secondary().isPresent() ? p.secondary().get().value().getName() : "null");
        if (packet instanceof UpdateCommandBlockC2SPacket p) return "UpdateCommandBlock x:" + p.getPos().getX() + " y:" + p.getPos().getY() + " z:" + p.getPos().getZ() + " command:" + p.getCommand() + " type:" + p.getType().name() + " alwaysActive:" + p.isAlwaysActive() + " conditional:" + p.isConditional() + " trackOutput:" + p.shouldTrackOutput();
        if (packet instanceof UpdateCommandBlockMinecartC2SPacket p) return "UpdateCommandBlockMinecart command:" + p.getCommand() + " trackOutput:" + p.shouldTrackOutput();
        if (packet instanceof UpdateDifficultyC2SPacket p) return "UpdateDifficulty difficulty:" + p.getDifficulty().name();
        if (packet instanceof UpdateDifficultyLockC2SPacket p) return "UpdateDifficultyLock locked:" + p.isDifficultyLocked();
        if (packet instanceof UpdateJigsawC2SPacket p) return "UpdateJigsaw x:" + p.getPos().getX() + " y:" + p.getPos().getY() + " z:" + p.getPos().getZ() + " name:" + p.getName() + " finalState:" + p.getFinalState() + " joint:" + p.getJointType().name() + " pool:" + p.getPool() + " target:" + p.getTarget();
        if (packet instanceof UpdatePlayerAbilitiesC2SPacket p) return "UpdatePlayerAbilities flying:" + p.isFlying();
        if (packet instanceof UpdateSelectedSlotC2SPacket p) return "UpdateSelectedSlot slot:" + p.getSelectedSlot();
        if (packet instanceof UpdateSignC2SPacket p) return "UpdateSign x:" + p.getPos().getX() + " y:" + p.getPos().getY() + " z:" + p.getPos().getZ() + " text:[\n" + signText(p.getText()) + " front:" + p.isFront() + "]";
        if (packet instanceof UpdateStructureBlockC2SPacket p) return "UpdateStructureBlock x:" + p.getPos().getX() + " y:" + p.getPos().getY() + " z:" + p.getPos().getZ() + " action" + p.getAction().name() + " tName:" + p.getTemplateName() + " mode:" + p.getMode().name() + " metadata:" + p.getMetadata() + " integrity:" + p.getIntegrity() + " mirror:" + p.getMirror().name() + " offsetX:" + p.getOffset().getX() + " offsetY:" + p.getOffset().getY() + " offsetZ:" + p.getOffset().getZ() + " rotation:" + p.getRotation().name() + " seed:" + p.getSeed() + " sizeX:" + p.getSize().getX() + " sizeY:" + p.getSize().getY() + " sizeZ:" + p.getSize().getZ() + " ignoreEntities:" + p.shouldIgnoreEntities() + " showAir:" + p.shouldShowAir() + " showBoundingBox:" + p.shouldShowBoundingBox();
        if (packet instanceof VehicleMoveC2SPacket p) return "VehicleMove x:" + p.getX() + " y:" + p.getY() + " z:" + p.getZ() + " yaw:" + p.getYaw() + " pitch:" + p.getPitch();

        if (packet instanceof QueryPingC2SPacket p) return "QueryPing startTime:" + p.getStartTime();
        if (packet instanceof QueryRequestC2SPacket) return "QueryRequest";

        //----------Receive----------//
        if (packet instanceof CommonPingS2CPacket p) return "PlayPing parameter:" + p.getParameter();
        if (packet instanceof CookieRequestS2CPacket p) return "CookieRequest key:" + p.key();
        if (packet instanceof CustomPayloadS2CPacket p) return "CustomPayload channel:" + p.payload().getId().id();
        if (packet instanceof CustomReportDetailsS2CPacket p) return "CustomReportDetails details:" + p.details();
        if (packet instanceof DisconnectS2CPacket p) return "Disconnect reason:" + p.reason().getString();
        if (packet instanceof KeepAliveS2CPacket p) return "KeepAlive id:" + p.getId();
        if (packet instanceof ResourcePackRemoveS2CPacket p) return "ResourcePackRemove id:" + p.id();
        if (packet instanceof ResourcePackSendS2CPacket p) return "ResourcePackSend URL:" + p.url() + " hash:" + p.hash() + " prompt:" + p.prompt() + " required:" + p.required() + " id:" + p.id();
        if (packet instanceof ServerLinksS2CPacket p) return "ServerLinks links" + p.links();
        if (packet instanceof ServerTransferS2CPacket p) return "ServerTransfer host" + p.host() + " port" + p.port();
        if (packet instanceof StoreCookieS2CPacket p) return "StoreCookie id" + p.key() + " paylod" + p.payload();
        if (packet instanceof SynchronizeTagsS2CPacket) return "SynchronizeTags";
        if (packet instanceof SelectKnownPacksS2CPacket) return "SelectKnownPacks";

        if (packet instanceof DynamicRegistriesS2CPacket p) return "DynamicRegistries manager:" + p.registry();
        if (packet instanceof FeaturesS2CPacket p) return "Features featureCount:" + p.features().size();
        //if (packet instanceof ReadyS2CPacket p) return "Ready state:" + (p.getNewNetworkState() == null ? "null" : p.getNewNetworkState().name());
        if (packet instanceof ResetChatS2CPacket) return "ResetChat";

        if (packet instanceof LoginCompressionS2CPacket p) return "LoginCompression compressionThreshold:" + p.getCompressionThreshold();
        if (packet instanceof LoginDisconnectS2CPacket p) return "LoginDisconnect reason:" + p.getReason();
        if (packet instanceof LoginHelloS2CPacket p) return "LoginHello serverId:" + p.getServerId();
        if (packet instanceof LoginQueryRequestS2CPacket p) return "LoginQueryRequest id:" + p.queryId() + " channel:" + p.payload().id();
        if (packet instanceof LoginSuccessS2CPacket p) return "LoginSuccess name:" + p.profile().getName() + " UUID:" + p.profile().getId();

        if (packet instanceof AdvancementUpdateS2CPacket p) return "AdvancementUpdate shouldClear:" + p.shouldClearCurrent();
        if (packet instanceof BlockBreakingProgressS2CPacket p) return "BlockBreakingProgress x:" + p.getPos().getX() + " y:" + p.getPos().getY() + " z:" + p.getPos().getZ() + " entityId:" + p.getEntityId() + " progress:" + p.getProgress();
        if (packet instanceof BlockEntityUpdateS2CPacket p) return "BlockEntityUpdate type:" + p.getBlockEntityType() + " x:" + p.getPos().getX() + " y:" + p.getPos().getY() + " z:" + p.getPos().getZ();
        if (packet instanceof BlockEventS2CPacket p) return "BlockEvent x:" + p.getPos().getX() + " y:" + p.getPos().getY() + " z:" + p.getPos().getZ() + " block:" + p.getBlock().getName() + " type:" + p.getType() + " data:" + p.getData();
        if (packet instanceof BlockUpdateS2CPacket p) return "BlockUpdate x:" + p.getPos().getX() + " y:" + p.getPos().getY() + " z:" + p.getPos().getZ() + " block:" + p.getState().getBlock();
        if (packet instanceof BossBarS2CPacket) return "BossBar";
        if (packet instanceof BundleDelimiterS2CPacket) return "BundleDelimiter";
        if (packet instanceof BundleS2CPacket) return "PacketBundle";
        if (packet instanceof ChangeUnlockedRecipesS2CPacket p) return "ChangeUnlockedRecipes action:" + p.getAction() + " recipeIdsToChange:" + p.getRecipeIdsToChange() + " recipeIdsToInit:" + p.getRecipeIdsToInit()  + " options:" + p.getOptions();
        if (packet instanceof ChatMessageS2CPacket p) return "ChatMessage message:" + p.unsignedContent() + " sender:" + p.sender() + " index:" + p.index();
        if (packet instanceof ChatSuggestionsS2CPacket p) return "ChatSuggestions action:" + p.action().name() + " entries:[\n" + merge(p.entries()) + "]";
        if (packet instanceof ChunkBiomeDataS2CPacket p) return "ChunkBiomeData size:" + p.chunkBiomeData().size();
        if (packet instanceof ChunkDataS2CPacket p) return "ChunkData x:" + p.getChunkX() + " z:" + p.getChunkZ();
        if (packet instanceof ChunkDeltaUpdateS2CPacket) return "ChunkDeltaUpdate";
        if (packet instanceof ChunkLoadDistanceS2CPacket p) return "ChunkLoadDistance distance:" + p.getDistance();
        if (packet instanceof ChunkRenderDistanceCenterS2CPacket p) return "ChunkRenderDistanceCenter x:" + p.getChunkX() + " z:" + p.getChunkZ();
        if (packet instanceof ChunkSentS2CPacket p) return "ChunkSent batchSize:" + p.batchSize();
        if (packet instanceof ClearTitleS2CPacket p) return "ClearTitle shouldReset:" + p.shouldReset();
        if (packet instanceof CloseScreenS2CPacket p) return "CloseScreen syncId" + p.getSyncId();
        if (packet instanceof CommandSuggestionsS2CPacket p) return "CommandSuggestions completionId:" + p.id() + " suggestions:[\n" + mergeSuggestions(p.getSuggestions().getList()) + "]";
        if (packet instanceof CommandTreeS2CPacket) return "CommandTree";
        if (packet instanceof CooldownUpdateS2CPacket p) return "CooldownUpdate item:" + p.item() + " cooldown:" + p.cooldown();
        if (packet instanceof CraftFailedResponseS2CPacket p) return "CraftFailedResponse syncId:" + p.getSyncId() + " recipeId:" + p.getRecipeId();
        if (packet instanceof DamageTiltS2CPacket p) return "DamageTilt yaw:" + p.yaw() + " id:" + p.id();
        if (packet instanceof DeathMessageS2CPacket p) return "DeathMessage entityId:" + p.playerId() + " message:" + p.message().getString();
        //if (packet instanceof DebugSampleS2CPacket p) return "DebugSample sample:" + p.sample() + " debugSampleType:" + p.debugSampleType();
        if (packet instanceof DifficultyS2CPacket p) return "Difficulty difficulty:" + p.getDifficulty().getName();
        if (packet instanceof EndCombatS2CPacket) return "EndCombat";
        if (packet instanceof EnterCombatS2CPacket) return "EnterCombat";
        //if (packet instanceof EnterReconfigurationS2CPacket p) return "EnterReconfiguration state:" + (p.getNewNetworkState() == null ? "null" : p.getNewNetworkState().name());
        if (packet instanceof EntitiesDestroyS2CPacket p) return "EntitiesDestroy size:" + p.getEntityIds().size();
        if (packet instanceof EntityAnimationS2CPacket p) return "EntityAnimation id:" + p.getAnimationId() + " animationId:" + p.getAnimationId();
        if (packet instanceof EntityAttachS2CPacket p) return "EntityAttack attackedId:" + p.getAttachedEntityId() + " holdingId:" + p.getHoldingEntityId();
        if (packet instanceof EntityAttributesS2CPacket p) return "EntityAttributes entityId:" + p.getEntityId();
        if (packet instanceof EntityDamageS2CPacket p) return "EntityDamage entityId:" + p.entityId() + " causeId:" + p.sourceCauseId() + " directId:" + p.sourceDirectId() + " sourceType:" + p.sourceType();
        if (packet instanceof EntityEquipmentUpdateS2CPacket p) return "EntityEquipment entityId:" + p.getEntityId();
        if (packet instanceof EntityPassengersSetS2CPacket p) return "EntityPassengersSet entityId:" + p.getEntityId() + mergeIds(p.getPassengerIds());
        if (packet instanceof EntityPositionS2CPacket p) return "EntityPosition id:" + p.getEntityId() + " x:" + p.getX() + " y:" + p.getY() + " z:" + p.getZ() + " yaw:" + p.getYaw() + " pitch:" + p.getPitch() + " ground:" + p.isOnGround();
        if (packet instanceof EntityS2CPacket p) return "Entity id:" + ((IEntityS2CPacket) p).getId() + " deltaX:" + p.getDeltaX() + " deltaY:" + p.getDeltaY() + " deltaZ:" + p.getDeltaZ() + " yaw:" + p.getYaw() + " pitch:" + p.getPitch() + " hasRotation:" + p.hasRotation() + " posChange:" + p.isPositionChanged() + " ground:" + p.isOnGround();
        if (packet instanceof EntitySetHeadYawS2CPacket p) return "EntitySetHeadYaw id:" + ((IEntitySetHeadYawS2CPacket) p).getId();
        if (packet instanceof EntitySpawnS2CPacket p) return "EntitySpawn id:" + p.getEntityId() + " type:" + p.getEntityType() + " x:" + p.getX() + " y:" + p.getY() + " z:" + p.getZ() + " yaw:" + p.getYaw() + " pitch:" + p.getPitch() + " velX:" + p.getVelocityX() + " velY:" + p.getVelocityY() + " velZ:" + p.getVelocityZ() + " headYaw:" + p.getHeadYaw() + " UUID:" + p.getUuid() + " data:" + p.getEntityData();
        if (packet instanceof EntityStatusEffectS2CPacket p) return "EntityStatusEffect entityId:" + p.getEntityId() + " effect:" + p.getEffectId().value().getName() + " amplifier:" + p.getAmplifier() + " duration:" + p.getDuration() + " showIcon:" + p.shouldShowIcon() + " particles:" + p.shouldShowParticles() + " ambient:" + p.isAmbient();
        if (packet instanceof EntityStatusS2CPacket p) return "EntityStatus status:" + p.getStatus() + " id:" + ((IEntityStatusS2CPacket) p).getId();
        if (packet instanceof EntityTrackerUpdateS2CPacket p) return "EntityTrackerUpdate id:" + p.id() ;
        if (packet instanceof EntityVelocityUpdateS2CPacket p) return "EntityVelocityUpdate id:" + p.getEntityId() + " x:" + p.getVelocityX() + " y:" + p.getVelocityY() + " z:" + p.getVelocityZ();
        if (packet instanceof ExperienceBarUpdateS2CPacket p) return "ExperienceBarUpdate exp:" + p.getExperience() + " level" + p.getExperienceLevel() + " barProgress:" + p.getBarProgress();
        if (packet instanceof ExperienceOrbSpawnS2CPacket p) return "ExperienceOrbSpawn id:" + p.getEntityId() + " x:" + p.getX() + " y:" + p.getY() + " z:" + p.getZ() + " expAmount" + p.getExperience();
        if (packet instanceof ExplosionS2CPacket p) return "Explosion x:" + p.getX() + " y:" + p.getY() + " z:" + p.getZ() + " radius:" + p.getRadius() + " playerVelX:" + p.getPlayerVelocityX() + " playerVelY:" + p.getPlayerVelocityY() + " playerVelZ:" + p.getPlayerVelocityZ();
        if (packet instanceof GameJoinS2CPacket p) return "GameJoin isDebug:" + p.commonPlayerSpawnInfo().isDebug() + " isFlat:" + p.commonPlayerSpawnInfo().isFlat() + " gameMode:" + p.commonPlayerSpawnInfo().gameMode().getName() + " viewDist:"  + p.viewDistance() + " simulationDist:" + p.simulationDistance() + " dimensionsId:" + p.dimensionIds() + " hardcode:" + p.hardcore() + " ownId:" + p.playerEntityId() + " maxPlayers:" + p.maxPlayers() + " portalCooldown:" + p.commonPlayerSpawnInfo().portalCooldown() + " prevGameMode:" + p.commonPlayerSpawnInfo().prevGameMode().getName() + " showDeathScreen:" + p.showDeathScreen() + " reducedDebugInfo:" + p.reducedDebugInfo();
        if (packet instanceof GameMessageS2CPacket p) return "GameMessage content:" + p.content().getString();
        if (packet instanceof GameStateChangeS2CPacket p) return "GameStateChange val:" + p.getValue() + " reason:" + p.getReason();
        if (packet instanceof HealthUpdateS2CPacket p) return "HealthUpdate health:" + p.getHealth() + " food:" + p.getFood() + " saturation:" + p.getSaturation();
        if (packet instanceof InventoryS2CPacket p) return "Inventory syncId:" + p.getSyncId() + " revision:" + p.getRevision() + " contents:[\n" + listStacks(p.getContents()) + "]";
        if (packet instanceof ItemPickupAnimationS2CPacket p) return "ItemPickupAnimation id:" + p.getEntityId() + " collectorEntity:" + p.getCollectorEntityId() + " stackAmount:" + p.getStackAmount();
        if (packet instanceof LightUpdateS2CPacket p) return "LightUpdate chunkX:" + p.getChunkX() + " chunkZ:" + p.getChunkZ();
        if (packet instanceof LookAtS2CPacket) return "LookAt";
        if (packet instanceof MapUpdateS2CPacket p) return "MapUpdate id:" + p.mapId() + " scale:" + p.scale() + " locked:" + p.locked();
        if (packet instanceof NbtQueryResponseS2CPacket p) return "NbtQueryResponse transactionId:" + p.getTransactionId();
        if (packet instanceof OpenHorseScreenS2CPacket p) return "OpenHorseScreen horseId:" + p.getHorseId() + " syncId:" + p.getSyncId() + " slotCount:" + p.getSlotColumnCount();
        if (packet instanceof OpenScreenS2CPacket p) return "OpenScreen name:" + p.getName().getString() + " syncId:" + p.getSyncId();
        if (packet instanceof OpenWrittenBookS2CPacket p) return "OpenWrittenBook hand:" + p.getHand().name();
        if (packet instanceof OverlayMessageS2CPacket p) return "OverlayMessage message:" + p.text().getString();
        if (packet instanceof ParticleS2CPacket p) return "Particle x:" + p.getX() + " y:" + p.getY() + " z:" + p.getZ() + " count:" + p.getCount() + " offsetX:" + p.getOffsetX() + " offsetY:" + p.getOffsetY() + " offsetZ:" + p.getOffsetZ() + " speed:" + p.getSpeed() + " parameters:" + p.getParameters().toString() + " longDist:" + p.isLongDistance();
        if (packet instanceof PlayerAbilitiesS2CPacket p) return "PlayerAbilities isFlying:" + p.isFlying() + " allowFlying:" + p.allowFlying() + " flySpeed:" + p.getFlySpeed() + " walkSpeed:" + p.getWalkSpeed() + " creative:" + p.isCreativeMode() + " invulnerable:" + p.isInvulnerable();
        if (packet instanceof PlayerActionResponseS2CPacket p) return "PlayerActionResponse sequence:" + p.sequence();
        if (packet instanceof PlayerListHeaderS2CPacket p) return "PlayerListHeader header:" + p.header().getString() + " footer:" + p.footer().getString();
        if (packet instanceof PlayerListS2CPacket p) return "PlayerList actions:" + p.getActions().size() + " entries:" + p.getEntries() + " additionalEntries:" + p.getPlayerAdditionEntries().size();
        if (packet instanceof PlayerPositionLookS2CPacket p) return "PlayerPositionLook x:" + p.getX() + " y:" + p.getY() + " z:" + p.getZ() + " yaw:" + p.getYaw() + " pitch:" + p.getPitch() + " id:" + p.getTeleportId();
        if (packet instanceof PlayerRemoveS2CPacket p) return "PlayerRemove UUIDs:[\n" + listProfiles(p.profileIds()) + "]";
        if (packet instanceof PlayerRespawnS2CPacket p) return "PlayerRespawn gameMode:" + p.commonPlayerSpawnInfo().gameMode().getName() + " prevGameMode:" + p.commonPlayerSpawnInfo().prevGameMode().getName() + (p.commonPlayerSpawnInfo().lastDeathLocation().isEmpty() ? " <deathPosNotPresent>" : " deathX:" + p.commonPlayerSpawnInfo().lastDeathLocation().get().pos().getX() + " deathY:" + p.commonPlayerSpawnInfo().lastDeathLocation().get().pos().getY() + " deathZ:" + p.commonPlayerSpawnInfo().lastDeathLocation().get().pos().getZ()) + " portalCooldown:" + p.commonPlayerSpawnInfo().portalCooldown() + " isDebug:" + p.commonPlayerSpawnInfo().isDebug() + " isFlat:" + p.commonPlayerSpawnInfo().isFlat();
        if (packet instanceof PlayerSpawnPositionS2CPacket p) return "PlayerSpawnPosition x:" + p.getPos().getX() + " y:" + p.getPos().getY() + " z:" + p.getPos().getZ() + " angle:" + p.getAngle();
        if (packet instanceof PlaySoundFromEntityS2CPacket p) return "PlaySoundFromEntity entityId:" + p.getEntityId() + " category:" + p.getCategory().getName() + " sound:" + p.getSound().getType().name() + " pitch:" + p.getPitch() + " volume:" + p.getVolume() + " seed:" + p.getSeed();
        if (packet instanceof PlaySoundS2CPacket p) return "PlaySound x:" + p.getX() + " y:" + p.getY() + " z:" + p.getZ() + " category:" + p.getCategory() + " sound:" + p.getSound().getType().name() + " pitch:" + p.getPitch() + " volume:" + p.getVolume() + " seed:" + p.getSeed();
        if (packet instanceof ProfilelessChatMessageS2CPacket p) return "ProfilelessChatMessage message:" + p.message();
        if (packet instanceof ProjectilePowerS2CPacket p) return "ProjectilePower id:" + p.getEntityId() + " accelerationPower:" + p.getAccelerationPower();
        if (packet instanceof RemoveEntityStatusEffectS2CPacket p) return "RemoveEntityStatusEffect effect:" + (p.effect().value() == null ? "null" : p.effect().value().getName());
        if (packet instanceof RemoveMessageS2CPacket p) return "RemoveMessage signature:" + p.messageSignature();
        if (packet instanceof ScoreboardDisplayS2CPacket p) return "ScoreboardDisplay name:" + p.getName() + " slot:" + p.getSlot();
        if (packet instanceof ScoreboardObjectiveUpdateS2CPacket p) return "ScoreboardObjectiveUpdate name:" + p.getName() + " type:" + p.getType() + " mode:" + p.getMode() + " displayName:" + p.getDisplayName();
        if (packet instanceof ScoreboardScoreResetS2CPacket p) return "ScoreboardScoreReset holder:" + p.scoreHolderName() + " objectiveName:" + p.objectiveName();
        if (packet instanceof ScoreboardScoreUpdateS2CPacket p) return "ScoreboardScoreUpdate holder:" + p.scoreHolderName() + " objectiveName:" + p.objectiveName() + " display:" + (p.display() == null ? "null" : p.display().get().getString()) + " numberFormat:" + p.numberFormat() + " score:" + p.score();
        if (packet instanceof ScreenHandlerPropertyUpdateS2CPacket p) return "ScreenHandlerPropertyUpdate syncId:" + p.getSyncId() + " propertyId:" + p.getPropertyId() + " value:" + p.getValue();
        if (packet instanceof ScreenHandlerSlotUpdateS2CPacket p) return "ScreenHandlerSlotUpdate syncId:" + p.getSyncId() + " slot:" + p.getSlot() + " item:" + p.getStack().getItem().getName() + " count:" + p.getStack().getCount() + " revision:" + p.getRevision();
        if (packet instanceof SelectAdvancementTabS2CPacket p) return "SelectAdvancementTab tabId:" + p.getTabId();
        if (packet instanceof ServerMetadataS2CPacket p) return "ServerMetadata description:" + p.description();
        if (packet instanceof SetCameraEntityS2CPacket p) return "SetCameraEntity id:" + ((ISetCameraEntityS2CPacket) p).getId();
        if (packet instanceof SetTradeOffersS2CPacket p) return "SetTradeOffers syncId:" + p.getSyncId() + " experience:" + p.getExperience() + " levelProgress:" + p.getLevelProgress() + " leveled:" + p.isLeveled() + " isRefreshable:" + p.isRefreshable() + " offers: [\n" + offers(p.getOffers()) + "]";
        if (packet instanceof SignEditorOpenS2CPacket p) return "SignEditorOpen x:" + p.getPos().getX() + " y:" + p.getPos().getY() + " z:" + p.getPos().getZ() + " front:" + p.isFront();
        if (packet instanceof SimulationDistanceS2CPacket p) return "SimulationDistance distance:" + p.simulationDistance();
        if (packet instanceof StartChunkSendS2CPacket) return "StartChunkSend";
        if (packet instanceof StatisticsS2CPacket p) return "Statistics" + stats(p.stats());
        if (packet instanceof StopSoundS2CPacket p) return "StopSound soundId:" + p.getSoundId() + " category:" + (p.getCategory() == null ? "null" : p.getCategory().getName());
        if (packet instanceof SubtitleS2CPacket p) return "Subtitle subtitle:" + (p.text() == null ? "null" : p.text().getString());
        if (packet instanceof SynchronizeRecipesS2CPacket p) return "SynchronizeRecipes recipes:[\n" + recipes(p.getRecipes()) + "]";
        if (packet instanceof TeamS2CPacket p) return "Team teamName:" + p.getTeamName() + " names:[\n" + fromCollection(p.getPlayerNames()) + "]";
        if (packet instanceof TickStepS2CPacket p) return "TickStep steps:" + p.tickSteps();
        if (packet instanceof TitleFadeS2CPacket p) return "TitleFade fadeInTicks:" + p.getFadeInTicks() + " stayTicks:" + p.getStayTicks() + " fadeOutTicks:" + p.getFadeOutTicks();
        if (packet instanceof TitleS2CPacket p) return "Title title:" + (p.text() == null ? "null" : p.text().getString());
        if (packet instanceof UnloadChunkS2CPacket p) return "UnloadChunk x:" + p.pos().x + " z:" + p.pos().z;
        if (packet instanceof UpdateSelectedSlotS2CPacket p) return "UpdateSelectedSlot slot:" + p.getSlot();
        if (packet instanceof UpdateTickRateS2CPacket p) return "UpdateTickRate tickRate:" + p.tickRate();
        if (packet instanceof VehicleMoveS2CPacket p) return "VehicleMove x:" + p.getX() + " y:" + p.getY() + " z:" + p.getZ() + " yaw:" + p.getYaw() + " pitch:" + p.getPitch();
        if (packet instanceof WorldBorderCenterChangedS2CPacket p) return "WorldBorderCenterChanged x:" + p.getCenterX() + " z:" + p.getCenterZ();
        if (packet instanceof WorldBorderInitializeS2CPacket p) return "WorldBorderInitialize centerX:" + p.getCenterX() + " centerZ:" + p.getCenterZ() + " size:" + p.getSize() + " sizeLerpTarget:" + p.getSizeLerpTarget() + " sizeLerpTime:" + p.getSizeLerpTime() + " warningBlocks:" + p.getWarningBlocks() + " warningTime:" + p.getWarningTime() + " maxRadius:" + p.getMaxRadius();
        if (packet instanceof WorldBorderInterpolateSizeS2CPacket p) return "WorldBorderInterpolateSize size:" + p.getSize() + " sizeLerpTarget:" + p.getSizeLerpTarget() + " sizeLerpTime:" + p.getSizeLerpTime();
        if (packet instanceof WorldBorderSizeChangedS2CPacket p) return "WorldBorderSizeChanged sizeLerpTarget:" + p.getSizeLerpTarget();
        if (packet instanceof WorldBorderWarningBlocksChangedS2CPacket p) return "WorldBorderWarningBlocksChanged warnignBlocks:" + p.getWarningBlocks();
        if (packet instanceof WorldBorderWarningTimeChangedS2CPacket p) return "WorldBorderWarningTimeChanged time:" + p.getWarningTime();
        if (packet instanceof WorldEventS2CPacket p) return "WorldEvent x:" + p.getPos().getX() + " y:" + p.getPos().getY() + " z:" + p.getPos().getZ() + " eventId:" + p.getEventId() + " data:" + p.getData() + " global:" + p.isGlobal();
        if (packet instanceof WorldTimeUpdateS2CPacket p)  return "WorldTimeUpdate time:" + p.getTime() + " timeOfDay:" + p.getTimeOfDay();

        if (packet instanceof PingResultS2CPacket p) return "PingResult startTime:" + p.startTime();
        if (packet instanceof QueryResponseS2CPacket p) return "QueryResponse description:" + p.metadata().description() + " onlinePlayers" + (p.metadata().players().isPresent() ? p.metadata().players().get().online() : "null") + " maxPlayers" + (p.metadata().players().isPresent() ? p.metadata().players().get().max() : "null");

        return null;
    }

    private String fromCollection(Collection<String> c) {
        StringBuilder builder = new StringBuilder();
        for (String s : c) builder.append(s).append("\n");
        return builder.toString();
    }

    private String recipes(List<RecipeEntry<?>> list) {
        StringBuilder builder = new StringBuilder();
        for (RecipeEntry<?> r : list) builder.append(r.value().createIcon().getItem().getName()).append("\n");
        return builder.toString();
    }

    private String stats(Map<Stat<?>, Integer> map) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<Stat<?>, Integer> o : map.entrySet())
            builder.append(o.getKey().getName()).append(" --- ").append(o.getValue()).append("\n");
        return builder.toString();
    }

    private String offers(TradeOfferList offers) {
        StringBuilder builder = new StringBuilder();
        for (TradeOffer o : offers)
            builder.append(o.copySellItem().getItem().getName()).append(" -> ").append(o.getOriginalFirstBuyItem().getItem().getName()).append(" & ").append(o.getSecondBuyItem().get().item().value().getName()).append("\n");
        return builder.toString();
    }

    private String listProfiles(List<UUID> uuids) {
        StringBuilder builder = new StringBuilder();
        for (UUID i : uuids) builder.append(i).append("\n");
        return builder.toString();
    }

    private String listStacks(List<ItemStack> itemStacks) {
        StringBuilder builder = new StringBuilder();
        for (ItemStack i : itemStacks)
            builder.append("name:").append(i.getName()).append("item").append(i.getItem().getName()).append("count").append(i.getCount()).append("\n");
        return builder.toString();
    }

    private String mergeIds(int[] ids) {
        StringBuilder builder = new StringBuilder();
        for (int i : ids) builder.append(i).append("\n");
        return builder.toString();
    }

    private String mergeSuggestions(List<Suggestion> list) {
        StringBuilder builder = new StringBuilder();
        for (Suggestion s : list) builder.append(s.getText()).append("\n");
        return builder.toString();
    }

    private String merge(List<String> list) {
        StringBuilder builder = new StringBuilder();
        for (String s : list) builder.append(s).append("\n");
        return builder.toString();
    }

    private String signText(String[] strings) {
        StringBuilder builder = new StringBuilder();
        for (String s : strings) builder.append(s).append("\n");
        return builder.toString();
    }
}
