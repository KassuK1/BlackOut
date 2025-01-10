/*
 *   This file is a part the best minecraft mod called Blackout Client (https://github.com/KassuK1/Blackout-Client)
 *   and licensed under the GNU GENERAL PUBLIC LICENSE (check LICENCE file or https://www.gnu.org/licenses/gpl-3.0.html)
 *   Copyright (C) 2024 KassuK and OLEPOSSU
 */

package kassuk.addon.blackout.utils;

import com.mojang.authlib.properties.Property;
import kassuk.addon.blackout.mixins.AccessorNbtCompound;
import kassuk.addon.blackout.mixins.IInteractEntityC2SPacket;
import meteordevelopment.meteorclient.mixininterface.IPlayerInteractEntityC2SPacket;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.item.ItemStack;
import net.minecraft.item.map.MapDecoration;
import net.minecraft.item.map.MapState;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.*;
import net.minecraft.network.packet.c2s.config.ReadyC2SPacket;
import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;
import net.minecraft.network.packet.c2s.login.EnterConfigurationC2SPacket;
import net.minecraft.network.packet.c2s.login.LoginHelloC2SPacket;
import net.minecraft.network.packet.c2s.login.LoginKeyC2SPacket;
import net.minecraft.network.packet.c2s.login.LoginQueryResponseC2SPacket;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.network.packet.c2s.query.QueryPingC2SPacket;
import net.minecraft.network.packet.c2s.query.QueryRequestC2SPacket;
import net.minecraft.network.packet.s2c.common.*;
import net.minecraft.network.packet.s2c.config.DynamicRegistriesS2CPacket;
import net.minecraft.network.packet.s2c.config.FeaturesS2CPacket;
import net.minecraft.network.packet.s2c.config.ReadyS2CPacket;
import net.minecraft.network.packet.s2c.login.*;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.network.packet.s2c.query.PingResultS2CPacket;
import net.minecraft.network.packet.s2c.query.QueryResponseS2CPacket;
import net.minecraft.server.ServerMetadata;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.TradedItem;

import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author OLEPOSSU
 */

public class PacketNames {
    public static final Map<Class<?>, PacketData<?>> s2c = new HashMap<>();
    public static final Map<Class<?>, PacketData<?>> c2s = new HashMap<>();

    static {
        /* ************************ C2S ************************ */

        // common
        c2s(ClientOptionsC2SPacket.class, "ClientOptions", packet -> "language: " + packet.options().language() + " allowsServerListing: " + packet.options().allowsServerListing() + " chatColorsEnabled: " + packet.options().chatColorsEnabled() + " chatVisibility: " + packet.options().chatVisibility() + " filtersText: " + packet.options().filtersText() + " mainArm: " + packet.options().mainArm().name() + " playerModelParts: " + packet.options().playerModelParts() + " viewDistance: " + packet.options().viewDistance());
        c2s(CommonPongC2SPacket.class, "CommonPong", packet -> "parameter: " + packet.getParameter());
        c2s(CustomPayloadC2SPacket.class, "CustomPayload", packet -> "identifier: " + packet.payload().getId().id().toString());
        c2s(KeepAliveC2SPacket.class, "KeepAlive", packet -> "id: " + packet.getId());
        c2s(ResourcePackStatusC2SPacket.class, "ResourcePackStatus", packet -> "id: " + packet.id().toString() + " status: " + packet.status().name());

        // config
        c2s(ReadyC2SPacket.class, "Ready", packet -> "transitionsNetworkState: " + packet.transitionsNetworkState());

        // handshake
        c2s(HandshakeC2SPacket.class, "Handshake", packet -> "address: " + packet.address() + " port: " + packet.port() + " protocolVersion: " + packet.protocolVersion() + " transitionsNetworkState: " + packet.transitionsNetworkState() + " intendedState: " + packet.intendedState());

        // login
        c2s(EnterConfigurationC2SPacket.class, "EnterConfiguration", packet -> "transitionsNetworkState: " + packet.transitionsNetworkState());
        c2s(LoginHelloC2SPacket.class, "LoginHello", packet -> "name: " + packet.name() + " id: " + packet.profileId().toString());
        c2s(LoginKeyC2SPacket.class, "LoginKey");
        c2s(LoginQueryResponseC2SPacket.class, "LoginQueryResponse", packet -> "queryId: " + packet.queryId());

        // play
        c2s(AcknowledgeChunksC2SPacket.class, "AcknowledgeChunks", packet -> "desiredChunksPerTick: " + packet.desiredChunksPerTick());
        c2s(AcknowledgeReconfigurationC2SPacket.class, "AcknowledgeReconfiguration", packet -> "transitionsNetworkState: " + packet.transitionsNetworkState());
        c2s(AdvancementTabC2SPacket.class, "AdvancementTab", packet -> "action: " + packet.getAction().name() + " tabToOpen: " + packet.getTabToOpen().toString());
        c2s(BoatPaddleStateC2SPacket.class, "BoatPaddleState", packet -> "isLeftPaddling: " + packet.isLeftPaddling() + " isRightPaddling: " + packet.isRightPaddling());
        c2s(BookUpdateC2SPacket.class, "BookUpdate", packet -> {
            StringBuilder builder = new StringBuilder("title: " + packet.title().orElse("null") + " slot: " + packet.slot());
            for (int page = 0; page < packet.pages().size(); page++)
                builder.append("\n").append(packet.pages().get(page));
            return builder.toString();
        });
        c2s(ButtonClickC2SPacket.class, "ButtonClick", packet -> "syncId: " + packet.syncId() + " buttonId: " + packet.buttonId());
        c2s(ChatMessageC2SPacket.class, "ChatMessage", packet -> "chatMessage: " + packet.chatMessage() + " timeStamp: " + packet.timestamp().toString() + " acknowledgementOffset: " + packet.acknowledgment().offset() + " signature: " + Objects.requireNonNull(packet.signature()) + " salt: " + packet.salt());
        c2s(ClickSlotC2SPacket.class, "ClickSlot", packet -> {
            StringBuilder builder = new StringBuilder("syncId: " + packet.getSyncId() + " slot: " + packet.getSlot() + " button: " + packet.getButton() + " action: " + packet.getActionType().name() + " revision: " + packet.getRevision());
            builder.append(" modified: {");
            packet.getModifiedStacks().forEach((i, stack) -> builder.append("\nslot ").append(i).append(": ").append(stack.getName().getString()).append(" ").append(stack.getCount()));
            builder.append("\n} stack: ");
            builder.append(packet.getStack().getName().getString()).append(" ").append(packet.getStack().getCount());
            return builder.toString();
        });
        c2s(ClientCommandC2SPacket.class, "ClientCommand", packet -> "entityId: " + packet.getEntityId() + " mountJumpTime: " + packet.getMountJumpHeight() + " mode: " + packet.getMode().name());
        c2s(ClientStatusC2SPacket.class, "ClientStatus", packet -> "mode: " + packet.getMode().name());
        c2s(CloseHandledScreenC2SPacket.class, "CloseHandledScreen", packet -> "syncId: " + packet.getSyncId());
        c2s(CommandExecutionC2SPacket.class, "CommandExecution", packet -> "command: " + packet.command());
        c2s(CraftRequestC2SPacket.class, "CraftRequest", packet -> "syncId: " + packet.getSyncId() + " shouldCraftAll: " + packet.shouldCraftAll() + " recipe: " + packet.getRecipeId().toString());
        c2s(CreativeInventoryActionC2SPacket.class, "CreativeInventoryAction", packet -> "slot: " + packet.slot() + " name: " + packet.stack().getName().getString() + " count: " + packet.stack().getCount());
        c2s(HandSwingC2SPacket.class, "HandSwing", packet -> "hand: " + packet.getHand().name());
        c2s(JigsawGeneratingC2SPacket.class, "JigsawGenerating", packet -> "pos: " + packet.getPos().toShortString() + " maxDepth: " + packet.getMaxDepth() + " shouldKeepJigsaws: " + packet.shouldKeepJigsaws());
        c2s(MessageAcknowledgmentC2SPacket.class, "MessageAcknowledgment", packet -> "offset: " + packet.offset());
        c2s(PickFromInventoryC2SPacket.class, "PickFromInventory", packet -> "slot: " + packet.getSlot());
        c2s(PlayerActionC2SPacket.class, "PlayerAction", packet -> "action: " + packet.getAction().name() + "pos: " + packet.getPos().toShortString() + " direction: " + packet.getDirection().getName() + " sequence: " + packet.getSequence());
        c2s(PlayerInputC2SPacket.class, "PlayerInput", packet -> "forward: " + packet.getForward() + " sideways: " + packet.getSideways() + " isJumping: " + packet.isJumping() + " isSneaking: " + packet.isSneaking());
        c2s(PlayerInteractBlockC2SPacket.class, "PlayerInteractBlock", packet -> "hand: " + packet.getHand().name() + " blockPos: " + packet.getBlockHitResult().getBlockPos().toShortString() + " pos: " + packet.getBlockHitResult().getPos().toString() + " side: " + packet.getBlockHitResult().getSide() + " isInsideBlock: " + packet.getBlockHitResult().isInsideBlock() + " type: " + packet.getBlockHitResult().getType().name() + " sequence: " + packet.getSequence());
        c2s(PlayerInteractEntityC2SPacket.class, "PlayerInteractEntity", packet -> "id: " + ((IInteractEntityC2SPacket) packet).getId() + " type: " + ((IInteractEntityC2SPacket) packet).getType().getType().name() + " isPlayerSneaking: " + packet.isPlayerSneaking());
        c2s(PlayerInteractItemC2SPacket.class, "PlayerInteractItem", packet -> "hand: " + packet.getHand().name() + " sequence: " + packet.getSequence());

        c2s(PlayerMoveC2SPacket.Full.class, "PlayerMove Full", packet -> "x: " + packet.getX(0) + " y: " + packet.getY(0) + " z: " + packet.getZ(0) + " yaw: " + packet.getYaw(0) + " pitch: " + packet.getPitch(0) + " isOnGround: " + packet.isOnGround());
        c2s(PlayerMoveC2SPacket.PositionAndOnGround.class, "PlayerMove PositionAndOnGround", packet -> "x: " + packet.getX(0) + " y: " + packet.getY(0) + " z: " + packet.getZ(0) + " isOnGround: " + packet.isOnGround());
        c2s(PlayerMoveC2SPacket.LookAndOnGround.class, "PlayerMove LookAndOnGround", packet -> "yaw: " + packet.getYaw(0) + " pitch: " + packet.getPitch(0) + " isOnGround: " + packet.isOnGround());
        c2s(PlayerMoveC2SPacket.OnGroundOnly.class, "PlayerMove OnGroundOnly", packet -> "isOnGround: " + packet.isOnGround());

        c2s(PlayerSessionC2SPacket.class, "PlayerSession", packet -> "sessionId: " + packet.chatSession().sessionId() + " isExpired: " + packet.chatSession().publicKeyData().isExpired() + " expiresAt: " + packet.chatSession().publicKeyData().expiresAt().toString() + " keySignature: " + byteArrToString(packet.chatSession().publicKeyData().keySignature()));
        c2s(QueryBlockNbtC2SPacket.class, "QueryBlockNbt", packet -> "pos: " + packet.getPos() + " transactionId: " + packet.getTransactionId());
        c2s(QueryEntityNbtC2SPacket.class, "QueryEntityNbt", packet -> "entityId: " + packet.getEntityId() + " transactionId: " + packet.getTransactionId());
        c2s(RecipeBookDataC2SPacket.class, "RecipeBookData", packet -> "recipeId: " + packet.getRecipeId().toString());
        c2s(RecipeCategoryOptionsC2SPacket.class, "RecipeCategoryOptions", packet -> "category: " + packet.getCategory().name() + " isFilteringCraftable: " + packet.isFilteringCraftable() + " isGuiOpen: " + packet.isGuiOpen());
        c2s(RenameItemC2SPacket.class, "RenameItem", packet -> "name: " + packet.getName());
        c2s(RequestCommandCompletionsC2SPacket.class, "RequestCommandCompletions", packet -> "partialCommand: " + packet.getPartialCommand() + " completionId: " + packet.getCompletionId());
        c2s(SelectMerchantTradeC2SPacket.class, "SelectMerchantTrade", packet -> "tradeId: " + packet.getTradeId());
        c2s(SlotChangedStateC2SPacket.class, "SlotChangedState", packet -> "slotId: " + packet.slotId() + " newState: " + packet.newState() + " screenHandlerId: " + packet.screenHandlerId());
        c2s(SpectatorTeleportC2SPacket.class, "SpectatorTeleport");
        c2s(TeleportConfirmC2SPacket.class, "TeleportConfirm", packet -> "teleportId: " + packet.getTeleportId());
        c2s(UpdateBeaconC2SPacket.class, "UpdateBeacon", packet -> {
            StringBuilder builder = new StringBuilder();

            builder.append("primary: ");
            if (packet.primary().isPresent())
                builder.append(packet.primary().get().getIdAsString());
            else
                builder.append("null");

            builder.append(" secondary: ");
            if (packet.secondary().isPresent())
                builder.append(packet.secondary().get().getIdAsString());
            else
                builder.append("null");

            return builder.toString();
        });
        c2s(UpdateCommandBlockC2SPacket.class, "UpdateCommandBlock", packet -> "command: " + packet.getCommand() + " pos: " + packet.getPos() + " isAlwaysActive: " + packet.isAlwaysActive() + " isConditional: " + packet.isConditional() + " shouldTrackOutput: " + packet.shouldTrackOutput() + " type: " + packet.getType().name());
        c2s(UpdateCommandBlockMinecartC2SPacket.class, "UpdateCommandBlockMinecart", packet -> "command: " + packet.getCommand() + " shouldTrackOutput: " + packet.shouldTrackOutput());
        c2s(UpdateDifficultyC2SPacket.class, "UpdateDifficulty", packet -> "difficulty: " + packet.getDifficulty().getName());
        c2s(UpdateDifficultyLockC2SPacket.class, "UpdateDifficultyLock", packet -> "isDifficultyLocked: " + packet.isDifficultyLocked());
        c2s(UpdateJigsawC2SPacket.class, "UpdateJigsaw", packet -> "name: " + packet.getName().toString() + " pos: " + packet.getPos().toShortString() + " finalState: " + packet.getFinalState() + " jointType: " + packet.getJointType().asString() + " target: " + packet.getTarget().toString() + " pool: " + packet.getPool().toString() + " placementPriority: " + packet.getPlacementPriority() + " selectionPriority: " + packet.getSelectionPriority());
        c2s(UpdatePlayerAbilitiesC2SPacket.class, "UpdatePlayerAbilities", packet -> "isFlying: " + packet.isFlying());
        c2s(UpdateSelectedSlotC2SPacket.class, "UpdateSelectedSlot", packet -> "selectedSlot: " + packet.getSelectedSlot());
        c2s(UpdateSignC2SPacket.class, "UpdateSign", packet -> {
            StringBuilder builder = new StringBuilder("pos: " + packet.getPos().toShortString() + " isFront: " + packet.isFront());
            for (String str : packet.getText()) builder.append("\n").append(str);
            return builder.toString();
        });
        c2s(UpdateStructureBlockC2SPacket.class, "UpdateStructureBlock", packet -> "pos: " + packet.getPos().toShortString() + " rotation: " + packet.getRotation().asString() + " offset: " + packet.getOffset().toShortString() + " size: " + packet.getSize().toShortString() + " seed: " + packet.getSeed() + " templateName: " + packet.getTemplateName() + " mode: " + packet.getMode().asString() + " action: " + packet.getAction().name() + " metaData: " + packet.getMetadata() + " integrity: " + packet.getIntegrity() + " mirror: " + packet.getMirror());
        c2s(VehicleMoveC2SPacket.class, "VehicleMove", packet -> "x: " + packet.getX() + " y: " + packet.getY() + " z: " + packet.getZ() + " yaw: " + packet.getYaw() + " pitch: " + packet.getPitch());

        // query
        c2s(QueryPingC2SPacket.class, "QueryPing", packet -> "startTime: " + packet.getStartTime());
        c2s(QueryRequestC2SPacket.class, "QueryRequest");

        /* ************************ S2C ************************ */

        // common
        s2c(CommonPingS2CPacket.class, "CommonPing", packet -> "parameter: " + packet.getParameter());
        s2c(CustomPayloadS2CPacket.class, "CustomPayload", packet -> "payloadId: " + packet.payload().getId().toString());
        s2c(DisconnectS2CPacket.class, "Disconnect", packet -> "reason: " + packet.reason().getString());
        s2c(KeepAliveS2CPacket.class, "KeepAlive", packet -> "id: " + packet.getId());
        s2c(ResourcePackRemoveS2CPacket.class, "ResourcePackRemove", packet -> "id: " + packet.id());
        s2c(ResourcePackSendS2CPacket.class, "ResourcePackSend", packet -> "url: " + packet.url() + " hash: " + packet.hash() + " id: " + packet.id().toString() + " prompt: " + packet.prompt().orElse(Text.of("null")).getString() + " required: " + packet.required());
        s2c(SynchronizeTagsS2CPacket.class, "SynchronizeTags", packet -> {
            StringBuilder builder = new StringBuilder("groups: ");
            packet.getGroups().forEach((key, serialized) -> builder.append("\n").append(key.toString()).append(" serializedSize: ").append(serialized.size()));
            return builder.toString();
        });

        // config
        s2c(DynamicRegistriesS2CPacket.class, "DynamicRegistries");
        s2c(FeaturesS2CPacket.class, "Features", packet -> {
            StringBuilder builder = new StringBuilder("features: ");
            for (Identifier v : packet.features())
                builder.append("\n").append(v.toString());
            return builder.toString();
        });
        s2c(ReadyS2CPacket.class, "Ready", packet -> "transitionsNetworkState: " + packet.transitionsNetworkState());

        // login
        s2c(LoginCompressionS2CPacket.class, "LoginCompression", packet -> "compressionThreshold: " + packet.getCompressionThreshold());
        s2c(LoginDisconnectS2CPacket.class, "LoginDisconnect", packet -> "reason: " + packet.getReason());
        s2c(LoginHelloS2CPacket.class, "LoginHello", packet -> "serverId: " + packet.getServerId() + " nonce: " + byteArrToString(packet.getNonce()));
        s2c(LoginQueryRequestS2CPacket.class, "LoginQueryRequest", packet -> "queryId: " + packet.queryId() + " payloadId: " + packet.payload().id());
        s2c(LoginSuccessS2CPacket.class, "LoginSuccess", packet -> {
            StringBuilder builder = new StringBuilder("name: " + packet.profile().getName() + " id: " + packet.profile().getId().toString() + " newNetworkState: " + packet.getPacketId().id().toString() + " properties: {");
            packet.profile().getProperties().asMap().forEach((str, collection) -> {
                builder.append("\n").append(str);
                for (Property v : collection) {
                    builder.append("\n  ").append(v.name()).append(" ").append(v.value()).append(" ").append(v.signature());
                }
            });
            builder.append("\n}");
            return builder.toString();
        });

        // play
        s2c(AdvancementUpdateS2CPacket.class, "AdvancementUpdate", packet -> {
            StringBuilder builder = new StringBuilder("shouldClearCurrent: " + packet.shouldClearCurrent());

            // this packet is gonna fill the whole log
            builder.append(" advancementIdsToRemove: {");
            for (Identifier v : packet.getAdvancementIdsToRemove())
                builder.append("\n  ").append(v.toString());
            builder.append("\n}");

            builder.append(" advancementsToEarn: {");
            for (AdvancementEntry v : packet.getAdvancementsToEarn())
                builder.append("\n  ").append(v.toString());
            builder.append("\n}");

            builder.append(" advancementsToEarn: {");
            packet.getAdvancementsToProgress().forEach((id, progress) -> {
                builder.append("\n  id: ").append(id.toString())
                    .append(" isDone: ").append(progress.isDone())
                    .append(" isAnyObtained: ").append(progress.isAnyObtained())
                    .append(" progressBarPercentage: ").append(progress.getProgressBarPercentage())
                    .append(" progressBarFraction: ").append(progress.getProgressBarFraction() == null ? "null" : progress.getProgressBarFraction().getString())
                    .append(" earliestProgressObtainDate: ").append(progress.getEarliestProgressObtainDate() == null ? "null" : progress.getEarliestProgressObtainDate().toString())
                    .append(" obtainedCriteria: {");

                for (String str : progress.getObtainedCriteria()) {
                    builder.append("\n    ").append(str);
                }

                builder.append("\n  }\n   unobtainedCriteria");
                for (String str : progress.getUnobtainedCriteria()) {
                    builder.append("\n    ").append(str);
                }
                builder.append("\n  }");
            });
            builder.append("\n}");
            return builder.toString();
        });
        s2c(BlockBreakingProgressS2CPacket.class, "BlockBreakingProgress", packet -> "pos: " + packet.getPos().toShortString() + " progress: " + packet.getProgress() + " entityId: " + packet.getEntityId());
        s2c(BlockEntityUpdateS2CPacket.class, "BlockEntityUpdate", packet -> {
            StringBuilder builder = new StringBuilder("pos: " + packet.getPos().toShortString() + " blockEntityType: ");
            builder.append(packet.getBlockEntityType().getRegistryEntry().getIdAsString());
            builder.append(" nbt: {");
            ((AccessorNbtCompound) packet.getNbt()).getEntries().forEach((string, element) -> builder.append("\n  ").append(string).append(" ").append(element.asString()));
            builder.append("\n}");
            return builder.toString();
        });
        s2c(BlockEventS2CPacket.class, "BlockEvent", packet -> "pos: " + packet.getPos().toShortString() + " block: " + packet.getBlock().getName() + " data: " + packet.getData() + " type: " + packet.getType());
        s2c(BlockUpdateS2CPacket.class, "BlockUpdate", packet -> "pos: " + packet.getPos() + " state: {" + packet.getState().toString() + "}");
        s2c(BossBarS2CPacket.class, "BossBar");
        s2c(BundleS2CPacket.class, "Bundle", packet -> {
            StringBuilder builder = new StringBuilder("BUNDLE START");
            packet.getPackets().forEach(p -> {
                builder.append("\n").append(getData(p).getName()).append(" ").append(getData(p).funnyApply(p));
            });
            builder.append("\nBUNDLE END");
            return builder.toString();
        });
        s2c(ChatMessageS2CPacket.class, "ChatMessage", packet -> "unsignedContent: " + packet.unsignedContent().getString() + " sender: " + packet.sender().toString() + " index: " + packet.index() + " isWritingErrorSkippable: " + packet.isWritingErrorSkippable() + " isFullyFiltered: " + packet.filterMask().isFullyFiltered() + " isPassThrough: " + packet.filterMask().isPassThrough() + " bodyContent: " + packet.body().content() + " bodySalt: " + packet.body().salt() + " bodyTimestamp: " + packet.body().timestamp() + " bodyLastSeenSize: " + packet.body().lastSeen().buf().size() + " signature: " + byteArrToString(packet.signature().data()) + " serializedParametersName: " + packet.serializedParameters().name().getString() + " serializedParametersTargetName: " + packet.serializedParameters().targetName().orElse(Text.of("null")).getString());
        s2c(ChatSuggestionsS2CPacket.class, "ChatSuggestions", packet -> {
            StringBuilder builder = new StringBuilder("action: " + packet.action().name() + " entries: {");
            for (String entry : packet.entries()) builder.append("\n  ").append(entry);
            builder.append("\n}");
            return builder.toString();
        });
        s2c(ChunkBiomeDataS2CPacket.class, "ChunkBiomeData", packet -> {
            StringBuilder builder = new StringBuilder("chunkBiomeData: {");
            for (var v : packet.chunkBiomeData())
                builder.append("\n  pos: ").append(v.pos()).append(" buffer: ").append(byteArrToString(v.buffer()));
            builder.append("\n}");
            return builder.toString();
        });
        s2c(ChunkDataS2CPacket.class, "ChunkData", packet -> {

            String builder = "chunkX: " + packet.getChunkX() + " chunkZ: " + packet.getChunkZ() + " chunkDataSectionsDataBuf: " + byteArrToString(packet.getChunkData().getSectionsDataBuf().array()) + " chunkDataHeightMap: {" + "\n} " +
                " lightDataBlockNibblesSize: " + packet.getLightData().getBlockNibbles().size() +
                " lightDataBlockNibblesSize: " + packet.getLightData().getSkyNibbles().size();

            return builder;
        });
        s2c(ChunkDeltaUpdateS2CPacket.class, "ChunkDeltaUpdate");
        s2c(ChunkLoadDistanceS2CPacket.class, "ChunkLoadDistance", packet -> "distance: " + packet.getDistance());
        s2c(ChunkRenderDistanceCenterS2CPacket.class, "ChunkRenderDistanceCenter", packet -> "chunkX: " + packet.getChunkX() + " chunkZ: " + packet.getChunkZ());
        s2c(ChunkSentS2CPacket.class, "ChunkSent", packet -> "batchSize: " + packet.batchSize());
        s2c(ClearTitleS2CPacket.class, "ClearTitle", packet -> "shouldReset: " + packet.shouldReset());
        s2c(CloseScreenS2CPacket.class, "CloseScreen", packet -> "syncId: " + packet.getSyncId());
        s2c(CommandSuggestionsS2CPacket.class, "CommandSuggestions", packet -> {
            StringBuilder builder = new StringBuilder("id: " + packet.id() + " length: " + packet.length() + " start: " + packet.start() + " suggestions: {");
            packet.suggestions().forEach(suggestion -> builder.append("\n  text: ").append(suggestion.text()).append(" toolTip: ").append(suggestion.tooltip().orElse(Text.of("null")).getString()));
            builder.append("\n}");
            return builder.toString();
        });
        s2c(CommandTreeS2CPacket.class, "CommandTree"); //TODO: should add something here
        s2c(CooldownUpdateS2CPacket.class, "CooldownUpdate", packet -> "item: " + packet.item().getName() + " cooldown: " + packet.cooldown());
        s2c(CraftFailedResponseS2CPacket.class, "CraftFailedResponse", packet -> "syncId: " + packet.getSyncId() + " recipeId: " + packet.getRecipeId());
        s2c(DamageTiltS2CPacket.class, "DamageTiltS2CPacket", packet -> "id: " + packet.id() + " yaw: " + packet.yaw());
        s2c(DeathMessageS2CPacket.class, "DeathMessage", packet -> "playerId: " + packet.playerId() + " message: " + packet.message().getString());
        s2c(DifficultyS2CPacket.class, "Difficulty", packet -> "difficulty: " + packet.getDifficulty().getName() + " isDifficultyLocked: " + packet.isDifficultyLocked());
        s2c(EndCombatS2CPacket.class, "EndCombat");
        s2c(EnterCombatS2CPacket.class, "EnterCombat");
        s2c(EnterReconfigurationS2CPacket.class, "EnterReconfiguration");
        s2c(EntitiesDestroyS2CPacket.class, "EntitiesDestroy", packet -> {
            StringBuilder builder = new StringBuilder("entityIds: {");
            packet.getEntityIds().forEach(id -> builder.append("\n  ").append(id));
            builder.append("\n}");
            return builder.toString();
        });
        s2c(EntityAnimationS2CPacket.class, "EntityAnimation", packet -> "entityId: " + packet.getEntityId() + " animationId: " + packet.getAnimationId());
        s2c(EntityAttachS2CPacket.class, "EntityAttach", packet -> "holdingEntityId: " + packet.getHoldingEntityId() + " attachedEntityId: " + packet.getAttachedEntityId());
        s2c(EntityAttributesS2CPacket.class, "EntityAttributes", packet -> {
            StringBuilder builder = new StringBuilder("entityId: " + packet.getEntityId() + " attributes: {");
            packet.getEntries().forEach(entry -> {
                String attribute = entry.attribute().getIdAsString();

                builder.append("\n  attribute: ").append(attribute).append(" base: ").append(entry.base()).append(" modifiers: {");
                entry.modifiers().forEach(modifier -> builder.append("\n    id: ").append(modifier.id().toString()).append(" value: ").append(modifier.value()));
                builder.append("\n  }");
            });
            builder.append("\n}");
            return builder.toString();
        });
        s2c(EntityDamageS2CPacket.class, "EntityDamage", packet -> {
            StringBuilder builder = new StringBuilder("entityId: " + packet.entityId() + " sourceType: ");
            builder.append(packet.sourceType().getIdAsString());

            builder.append(" sourcePosition: ");
            Optional<Vec3d> rur = packet.sourcePosition();
            builder.append(rur.isPresent() ? rur.get().toString() : "null");

            builder.append(" sourceCauseId: ").append(packet.sourceCauseId()).append(" sourceDirectId: ").append(packet.sourceDirectId());
            return builder.toString();
        });
        s2c(EntityEquipmentUpdateS2CPacket.class, "EntityEquipmentUpdate", packet -> {
            StringBuilder builder = new StringBuilder("entityId: " + packet.getEntityId() + " equipment: {");
            packet.getEquipmentList().forEach(pair -> builder.append("\n  type: ").append(pair.getFirst().getName()).append(" item: ").append(pair.getSecond().getItem().getName().getString()).append(" count: ").append(pair.getSecond().getCount()));
            builder.append("\n}");
            return builder.toString();
        });
        s2c(EntityPassengersSetS2CPacket.class, "EntityPassengersSet", packet -> {
            StringBuilder builder = new StringBuilder("entityId: " + packet.getEntityId() + " passengerIds: ");
            for (int id : packet.getPassengerIds()) builder.append("\n  ").append(id);
            builder.append("\n}");
            return builder.toString();
        });
        s2c(EntityPositionS2CPacket.class, "EntityPosition", packet -> "entityId: " + packet.getEntityId() + " x: " + packet.getX() + " y: " + packet.getY() + " z: " + packet.getZ() + " yaw: " + packet.getYaw() + " pitch: " + packet.getPitch() + " isOnGround: " + packet.isOnGround());
        s2c(EntityS2CPacket.class, "Entity", packet -> "deltaX: " + packet.getDeltaX() + " deltaY: " + packet.getDeltaY() + " deltaZ: " + packet.getDeltaZ() + " yaw: " + packet.getYaw() + " pitch: " + packet.getPitch() + " hasRotation: " + packet.hasRotation() + " isPositionChanged: " + packet.isPositionChanged() + " isOnGround: " + packet.isOnGround());
        s2c(EntitySetHeadYawS2CPacket.class, "EntitySetHeadYaw", packet -> "headYaw: " + packet.getHeadYaw());
        s2c(EntitySpawnS2CPacket.class, "EntitySpawn", packet -> "entityId: " + packet.getEntityId() + " entityData: " + packet.getEntityData() + " entityType: " + packet.getEntityType().getName() + " uuid: " + packet.getUuid().toString() + " x: " + packet.getX() + " y: " + packet.getY() + " z: " + packet.getZ() + " yaw: " + packet.getYaw() + " pitch: " + packet.getPitch() + " headYaw: " + packet.getHeadYaw() + " velocityX: " + packet.getVelocityX() + " velocityY: " + packet.getVelocityY() + " velocityZ: " + packet.getVelocityZ());
        s2c(EntityStatusEffectS2CPacket.class, "EntityStatusEffect", packet -> {
            String effect = packet.getEffectId().getIdAsString();
            return "effectId: " + effect + " entityId: " + packet.getEntityId() + " amplifier: " + packet.getAmplifier() + " duration: " + packet.getDuration() + " isAmbient: " + packet.isAmbient() + " shouldShowIcon: " + packet.shouldShowIcon() + " shouldShowParticles: " + packet.shouldShowParticles();
        });
        s2c(EntityStatusS2CPacket.class, "EntityStatus", packet -> String.valueOf(packet.getStatus()));
        s2c(EntityTrackerUpdateS2CPacket.class, "EntityTrackerUpdate", packet -> {
            StringBuilder builder = new StringBuilder("id: " + packet.id() + " trackedValues: {");
            packet.trackedValues().forEach(entry -> builder.append("\n  id: ").append(entry.id()).append(" value: ").append(entry.value()));
            builder.append("\n}");
            return builder.toString();
        });
        s2c(EntityVelocityUpdateS2CPacket.class, "EntityVelocityUpdate", packet -> "entityId: " + packet.getEntityId() + " velocityX: " + packet.getVelocityX() + " velocityY: " + packet.getVelocityY() + " velocityZ: " + packet.getVelocityZ());
        s2c(ExperienceBarUpdateS2CPacket.class, "ExperienceBarUpdate", packet -> "experience: " + packet.getExperience() + " barProgress: " + packet.getBarProgress() + " experienceLevel: " + packet.getExperienceLevel());
        s2c(ExperienceOrbSpawnS2CPacket.class, "ExperienceOrbSpawn", packet -> "entityId: " + packet.getEntityId() + " experience: " + packet.getExperience() + " x: " + packet.getX() + " y: " + packet.getY() + " z: " + packet.getZ());
        s2c(ExplosionS2CPacket.class, "Explosion", packet -> {
            StringBuilder builder = new StringBuilder("x: " + packet.getX() + " y: " + packet.getY() + " z: " + packet.getZ() + " playerVelocityX: " + packet.getPlayerVelocityX() + " playerVelocityY: " + packet.getPlayerVelocityY() + " playerVelocityZ: " + packet.getPlayerVelocityZ() + " destructionType: " + packet.getDestructionType().name() + " radius: " + packet.getRadius() + " particle: " + packet.getParticle().getType().toString() + " emitterParticle: " + packet.getEmitterParticle().getType().toString() + " soundEvent: ");
            builder.append(packet.getSoundEvent().getIdAsString());
            builder.append(" affectedBlocks: {");
            packet.getAffectedBlocks().forEach(pos -> builder.append("\n  ").append(pos.toShortString()));
            builder.append("\n}");
            return builder.toString();
        });
        s2c(GameJoinS2CPacket.class, "GameJoin", packet -> {
            StringBuilder builder = new StringBuilder("playerEntityId: " + packet.playerEntityId() + " maxPlayers: " + packet.maxPlayers() + " viewDistance: " + packet.viewDistance() + " simulationDistance: " + packet.simulationDistance() + " doLimitedCrafting: " + packet.doLimitedCrafting() + " enforcesSecureChat: " + packet.enforcesSecureChat() + " hardcore: " + packet.hardcore() + " showDeathScreen: " + packet.showDeathScreen() + " reducedDebugInfo: " + packet.reducedDebugInfo() + " commonPlayerSpawnInfoGameMode: " + packet.commonPlayerSpawnInfo().gameMode() + " commonPlayerSpawnInfoIsFlat: " + packet.commonPlayerSpawnInfo().isFlat() + " commonPlayerSpawnInfoPrevGameMode: " + packet.commonPlayerSpawnInfo().prevGameMode() + " commonPlayerSpawnInfoSeed: " + packet.commonPlayerSpawnInfo().seed() + " commonPlayerSpawnInfoPortalCooldown: " + packet.commonPlayerSpawnInfo().portalCooldown());
            builder.append(" commonPlayerSpawnInfoDimension: ").append(packet.commonPlayerSpawnInfo().dimension().getValue().toString());
            builder.append(" commonPlayerSpawnInfoDimensionType: ").append(packet.commonPlayerSpawnInfo().dimensionType().getIdAsString());
            builder.append(" commonPlayerSpawnInfoLastDeathPos: ").append(packet.commonPlayerSpawnInfo().lastDeathLocation().isPresent() ? packet.commonPlayerSpawnInfo().lastDeathLocation().get().toString() : "null");
            builder.append(" dimensionIds: {");
            packet.dimensionIds().forEach(key -> builder.append("\n  ").append(key.getValue().toString()));
            builder.append("\n}");
            return builder.toString();
        });
        s2c(GameMessageS2CPacket.class, "GameMessage", packet -> "content: " + packet.content().getString() + " overlay: " + packet.overlay());
        s2c(GameStateChangeS2CPacket.class, "GameStateChange", packet -> "value: " + packet.getValue());
        s2c(HealthUpdateS2CPacket.class, "HealthUpdate", packet -> "health: " + packet.getHealth() + " food: " + packet.getFood() + " saturation: " + packet.getSaturation());
        s2c(InventoryS2CPacket.class, "Inventory", packet -> {
            StringBuilder builder = new StringBuilder("syncId: " + packet.getSyncId() + " revision: " + packet.getRevision() + "cursorStackItem: " + packet.getCursorStack().getItem().getName().getString() + " cursorStackCount:" + packet.getCursorStack().getCount() + " contents: {");
            for (int i = 0; i < packet.getContents().size(); i++) {
                ItemStack stack = packet.getContents().get(i);
                builder.append("\n  slot: ").append(i).append(" item: ").append(stack.getItem().getName().getString()).append(" count: ").append(stack.getCount());
            }
            builder.append("\n}");
            return builder.toString();
        });
        s2c(ItemPickupAnimationS2CPacket.class, "ItemPickupAnimation", packet -> "entityId: " + packet.getEntityId() + " collectorEntityId: " + packet.getCollectorEntityId() + " stackAmount: " + packet.getStackAmount());
        s2c(LightUpdateS2CPacket.class, "LightUpdate", packet -> "chunkX: " + packet.getChunkX() + " chunkZ: " + packet.getChunkZ() + " blockNibblesSize: " + packet.getData().getBlockNibbles().size() + " skyNibblesSize: " + packet.getData().getSkyNibbles().size());
        s2c(LookAtS2CPacket.class, "LookAt");
        s2c(MapUpdateS2CPacket.class, "MapUpdate", packet -> {
            StringBuilder builder = new StringBuilder("mapId: " + packet.mapId().id() + " locked: " + packet.locked() + " scale: " + packet.scale());

            builder.append(" decorations: {");
            if (packet.decorations().isPresent()) {
                List<MapDecoration> decorations = packet.decorations().get();
                decorations.forEach(decoration -> builder.append("\n  name: ").append(decoration.name().orElse(Text.of("null")).getString()).append(" x: ").append(decoration.x()).append(" z: ").append(decoration.z()).append(" x: ").append(" type: ").append(decoration.type().getIdAsString()).append(" rotation: ").append(decoration.rotation()).append(" assetId: ").append(decoration.getAssetId()).append(" isAlwaysRendered: ").append(decoration.isAlwaysRendered()));
            }
            builder.append("\n}");

            String startX;
            String startZ;
            String width;
            String height;
            String colors;

            if (packet.updateData().isPresent()) {
                MapState.UpdateData data = packet.updateData().get();
                startX = String.valueOf(data.startX());
                startZ = String.valueOf(data.startZ());
                width = String.valueOf(data.width());
                height = String.valueOf(data.height());
                colors = byteArrToString(data.colors());
            } else {
                startX = "null";
                startZ = "null";
                width = "null";
                height = "null";
                colors = "null";
            }

            builder.append(" startX: ").append(startX).append(" startZ: ").append(startZ).append(" width: ").append(width).append(" height: ").append(height).append(" colors: ").append(colors);
            return builder.toString();
        });
        s2c(NbtQueryResponseS2CPacket.class, "NbtQueryResponse", packet -> {
            StringBuilder builder = new StringBuilder("transactionId: " + packet.getTransactionId());
            builder.append(" nbt: {");
            ((AccessorNbtCompound) packet.getNbt()).getEntries().forEach((string, element) -> builder.append("\n  ").append(string).append(" ").append(element.asString()));
            builder.append("\n}");
            return builder.toString();
        });
        s2c(OpenHorseScreenS2CPacket.class, "OpenHorseScreen", packet -> "syncId: " + packet.getSyncId() + " horseId: " + packet.getHorseId() + " slotColumnCount: " + packet.getSlotColumnCount());
        s2c(OpenScreenS2CPacket.class, "OpenScreen", packet -> "name: " + packet.getName().getString() + " syncId: " + packet.getSyncId() + " screenHandlerType: " + packet.getScreenHandlerType());
        s2c(OpenWrittenBookS2CPacket.class, "OpenWrittenBook", packet -> "hand: " + packet.getHand().name());
        s2c(OverlayMessageS2CPacket.class, "OverlayMessage", packet -> "text: " + packet.text().getString());
        s2c(ParticleS2CPacket.class, "Particle", packet -> "count: " + packet.getCount() + " x: " + packet.getX() + " y: " + packet.getY() + " z: " + packet.getZ() + " offsetX: " + packet.getOffsetX() + " offsetY: " + packet.getOffsetY() + " offsetZ: " + packet.getOffsetZ() + " speed: " + packet.getSpeed() + " isLongDistance: " + packet.isLongDistance() + " parameterType: " + packet.getParameters().getType());
        s2c(PlayerAbilitiesS2CPacket.class, "PlayerAbilities", packet -> "isCreativeMod: " + packet.isCreativeMode() + " allowFlying: " + packet.allowFlying() + " isInvulnerable: " + packet.isInvulnerable() + " isFlying: " + packet.isFlying() + " flySpeed: " + packet.getFlySpeed() + " walkSpeed: " + packet.getWalkSpeed());
        s2c(PlayerActionResponseS2CPacket.class, "PlayerActionResponse", packet -> "sequence: " + packet.sequence());
        s2c(PlayerListHeaderS2CPacket.class, "PlayerListHeader", packet -> "header: " + packet.header().getString() + " footer: " + packet.footer().getString());
        s2c(PlayerListS2CPacket.class, "PlayerList", packet -> {
            StringBuilder builder = new StringBuilder("entries: {");

            packet.getEntries().forEach(entry -> builder.append("\n  displayName: ").append(entry.displayName()).append(" profileId: ").append(entry.profileId()).append(" gameProfileName: ").append(entry.profile() == null ? "null" : entry.profile().getName()).append(" gameProfileId: ").append(entry.profile() == null ? "null" : entry.profile().getId()).append(" listed: ").append(entry.listed()).append(" gameMode: ").append(entry.gameMode()).append(" latency: ").append(entry.latency()).append(" chatSessionId: ").append(entry.chatSession() == null ? "null" : entry.chatSession().sessionId().toString()).append(" isExpired: ").append(entry.chatSession() == null ? "null" : entry.chatSession().publicKeyData().isExpired()).append(" expiresAt: ").append(entry.chatSession() == null ? "null" : entry.chatSession().publicKeyData().expiresAt().toString()).append(" keySignature: ").append(entry.chatSession() == null ? "null" : byteArrToString(entry.chatSession().publicKeyData().keySignature())));
            builder.append("\n} playerAdditionEntries: {");
            packet.getPlayerAdditionEntries().forEach(entry -> builder.append("\n  displayName: ").append(entry.displayName()).append(" profileId: ").append(entry.profileId()).append(" gameProfileName: ").append(entry.profile() == null ? "null" : entry.profile().getName()).append(" gameProfileId: ").append(entry.profile() == null ? "null" : entry.profile().getId()).append(" listed: ").append(entry.listed()).append(" gameMode: ").append(entry.gameMode()).append(" latency: ").append(entry.latency()).append(" chatSessionId: ").append(entry.chatSession() == null ? "null" : entry.chatSession().sessionId().toString()).append(" isExpired: ").append(entry.chatSession() == null ? "null" : entry.chatSession().publicKeyData().isExpired()).append(" expiresAt: ").append(entry.chatSession() == null ? "null" : entry.chatSession().publicKeyData().expiresAt().toString()).append(" keySignature: ").append(entry.chatSession() == null ? "null" : byteArrToString(entry.chatSession().publicKeyData().keySignature())));
            builder.append("\n} actions: {");
            packet.getActions().forEach(action -> builder.append("\n  ").append(action.name()));
            builder.append("\n}");
            return builder.toString();
        });
        s2c(PlayerPositionLookS2CPacket.class, "PlayerPositionLook", packet -> {
            StringBuilder builder = new StringBuilder("teleportId: " + packet.getTeleportId() + " x: " + packet.getX() + " y: " + packet.getY() + " z: " + packet.getZ() + " yaw: " + packet.getYaw() + " pitch: " + packet.getPitch() + " flags: {");
            packet.getFlags().forEach(flag -> builder.append("\n  ").append(flag.name()));
            builder.append("\n}");
            return builder.toString();
        });
        s2c(PlayerRemoveS2CPacket.class, "PlayerRemove", packet -> {
            StringBuilder builder = new StringBuilder("profileIds: {");
            packet.profileIds().forEach(id -> builder.append("\n  ").append(id.toString()));
            builder.append("\n}");
            return builder.toString();
        });
        s2c(PlayerRespawnS2CPacket.class, "PlayerRespawn", packet -> "flag: " + Integer.toBinaryString(packet.flag()) + " commonPlayerSpawnInfoGameMode: " + packet.commonPlayerSpawnInfo().gameMode() + " commonPlayerSpawnInfoIsFlat: " + packet.commonPlayerSpawnInfo().isFlat() + " commonPlayerSpawnInfoPrevGameMode: " + packet.commonPlayerSpawnInfo().prevGameMode() + " commonPlayerSpawnInfoSeed: " + packet.commonPlayerSpawnInfo().seed() + " commonPlayerSpawnInfoPortalCooldown: " + packet.commonPlayerSpawnInfo().portalCooldown() + " commonPlayerSpawnInfoDimension: " + packet.commonPlayerSpawnInfo().dimension().getValue().toString() + " commonPlayerSpawnInfoDimensionType: " + packet.commonPlayerSpawnInfo().dimensionType().getIdAsString() + " commonPlayerSpawnInfoLastDeathPos: " + (packet.commonPlayerSpawnInfo().lastDeathLocation().isPresent() ? packet.commonPlayerSpawnInfo().lastDeathLocation().get().toString() : "null"));
        s2c(PlayerSpawnPositionS2CPacket.class, "PlayerSpawnPosition", packet -> "pos: " + packet.getPos().toShortString() + " angle: " + packet.getAngle());
        s2c(PlaySoundFromEntityS2CPacket.class, "PlaySoundFromEntity", packet -> "entityId: " + packet.getEntityId() + " pitch: " + packet.getPitch() + " volume: " + packet.getVolume() + " seed: " + packet.getSeed() + " category: " + packet.getCategory().getName() + " sound: " + packet.getSound().getIdAsString());
        s2c(PlaySoundS2CPacket.class, "PlaySound", packet -> "x: " + packet.getX() + " y: " + packet.getY() + " z: " + packet.getZ() + " pitch: " + packet.getPitch() + " volume: " + packet.getVolume() + " seed: " + packet.getSeed() + " category: " + packet.getCategory().getName() + " sound: " + packet.getSound().getIdAsString());
        s2c(ProfilelessChatMessageS2CPacket.class, "ProfilelessChatMessage", packet -> "message: " + packet.message() + " chatTypeName: " + packet.chatType().name() + " chatTypeTargetName: " + packet.chatType().targetName().orElse(Text.of("null")).getString() + " chatTypeType: " + packet.chatType().type().getIdAsString());
        s2c(RemoveEntityStatusEffectS2CPacket.class, "RemoveEntityStatusEffect", packet -> " entityId: " + packet.entityId() + " effect: " + packet.effect().getIdAsString());
        s2c(RemoveMessageS2CPacket.class, "RemoveMessage", packet -> "messageSignatureId: " + packet.messageSignature().id() + " messageSignatureFullSignature: " + byteArrToString(packet.messageSignature().fullSignature().data()));
        s2c(ScoreboardDisplayS2CPacket.class, "ScoreboardDisplay", packet -> "name: " + packet.getName() + " slot: " + packet.getSlot().asString() + " slotId:" + packet.getSlot().getId());
        s2c(ScoreboardObjectiveUpdateS2CPacket.class, "ScoreboardObjectiveUpdate", packet -> "name: " + packet.getName() + " displayName: " + packet.getDisplayName() + " type: " + packet.getType().getName() + " mode: " + packet.getMode());
        s2c(ScoreboardScoreResetS2CPacket.class, "ScoreboardScoreReset", packet -> "objectiveName: " + packet.objectiveName() + " scoreHolderName: " + packet.scoreHolderName());
        s2c(ScoreboardScoreUpdateS2CPacket.class, "ScoreboardScoreUpdate", packet -> "objectiveName: " + packet.objectiveName() + " scoreHolderName: " + packet.scoreHolderName() + " score: " + packet.score() + " display: " + packet.display().orElse(Text.of("null")).getString());
        s2c(ScreenHandlerPropertyUpdateS2CPacket.class, "ScreenHandlerPropertyUpdate", packet -> "syncId: " + packet.getSyncId() + " propertyName: " + packet.getPropertyId() + " value: " + packet.getValue());
        s2c(ScreenHandlerSlotUpdateS2CPacket.class, "ScreenHandlerSlotUpdate", packet -> "syncId: " + packet.getSyncId() + " slot: " + packet.getSlot() + " revision: " + packet.getRevision() + " item: " + packet.getStack().getItem().getName() + " itemCount: " + packet.getStack().getCount());
        s2c(SelectAdvancementTabS2CPacket.class, "SelectAdvancementTab", packet -> "tabId: " + packet.getTabId());
        s2c(ServerMetadataS2CPacket.class, "ServerMetadata", packet -> "description: " + packet.description().getString() + " favicon: " + (packet.favicon().isPresent() ? byteArrToString(packet.favicon().get()) : "null"));
        s2c(SetCameraEntityS2CPacket.class, "SetCameraEntity");
        s2c(SetTradeOffersS2CPacket.class, "SetTradeOffers", packet -> {
            StringBuilder builder = new StringBuilder("syncId: " + packet.getSyncId() + " experience: " + packet.getExperience() + " levelProgress: " + packet.getLevelProgress() + " isLeveled: " + packet.isLeveled() + " isRefreshable: " + packet.isRefreshable() + " offers: {");
            packet.getOffers().forEach(offer -> {
                builder.append("\n  uses: " + offer.getUses() + " maxUses: " + offer.getMaxUses() + " demandBonus: " + offer.getDemandBonus() + " hasBeenUsed: " + offer.hasBeenUsed() + " disabled: " + offer.isDisabled() + " specialPrice: " + offer.getSpecialPrice() + " merchantExperience: " + offer.getMerchantExperience() + " shouldRewardPlayerExperience: " + offer.shouldRewardPlayerExperience() + " firstBuyItem: " + offer.getFirstBuyItem().item().getIdAsString() + " secondBuyItem: ");
                Optional<TradedItem> v = offer.getSecondBuyItem();
                builder.append(v.isPresent() ? v.get().item().getIdAsString() : "null").append(" sellItem: ").append(offer.getSellItem().getName().getString()).append(" sellItemCount: ").append(offer.getSellItem().getCount());
                builder.append(" displayedFirstBuyItem: ").append(offer.getDisplayedFirstBuyItem().getItem().getName().getString()).append(" displayedFirstBuyItemCount: ").append(offer.getDisplayedFirstBuyItem().getCount()).append(" displayedSecondBuyItem: ").append(offer.getDisplayedSecondBuyItem().getItem().getName().getString()).append(" displayedSecondBuyItemCount: ").append(offer.getDisplayedSecondBuyItem().getCount());
                builder.append(" originalFirstBuyItem: " + offer.getOriginalFirstBuyItem().getItem().getName().getString()).append(" originalFirstBuyItemCount: " + offer.getOriginalFirstBuyItem().getCount());
            });
            builder.append("\n}");
            return builder.toString();
        });
        s2c(SignEditorOpenS2CPacket.class, "SignEditorOpen", packet -> "pos: " + packet.getPos().toShortString() + " isFront: " + packet.isFront());
        s2c(SimulationDistanceS2CPacket.class, "SimulationDistance", packet -> "simulationDistance: " + packet.simulationDistance());
        s2c(StartChunkSendS2CPacket.class, "StartChunkSend");
        s2c(StatisticsS2CPacket.class, "Statistics", packet -> {
            StringBuilder builder = new StringBuilder("stats: {");
            packet.stats().forEach((stat, i) -> {
                builder.append("\n  index: ").append(i).append(" statType: ").append(stat.getType().getName().getString()).append(" value: ").append(stat.getValue().toString());
            });
            builder.append("\n}");
            return builder.toString();
        });
        s2c(StopSoundS2CPacket.class, "StopSound", packet -> "category: " + packet.getCategory() + " soundId: " + packet.getSoundId().toString());
        s2c(SubtitleS2CPacket.class, "Subtitle", packet -> "text: " + packet.text().getString());
        s2c(SynchronizeRecipesS2CPacket.class, "SynchronizeRecipes", packet -> {
            StringBuilder builder = new StringBuilder("recipes: {");
            packet.getRecipes().forEach(recipe -> builder.append("\n  id: ").append(recipe.id().toString()).append(" value: ").append(recipe.value().toString()));
            builder.append("\n}");
            return builder.toString();
        });
        s2c(TeamS2CPacket.class, "Team", packet -> {
            StringBuilder builder = new StringBuilder("teamName: " + packet.getTeamName() + " teamOperation: " + (packet.getTeamOperation() == null ? "null" : packet.getTeamOperation().name()) + " playerListOperation: " + (packet.getPlayerListOperation() == null ? "null" : packet.getPlayerListOperation().name()));

            String displayName;
            String collisionRule;
            String color;
            String friendlyFlagsBitwise;
            String nameTagVisibilityRule;
            String prefix;
            String suffix;
            if (packet.getTeam().isPresent()) {
                TeamS2CPacket.SerializableTeam v = packet.getTeam().get();
                displayName = v.getDisplayName().getString();
                collisionRule =  v.getCollisionRule();
                color = v.getColor().getName();
                friendlyFlagsBitwise = String.valueOf(v.getFriendlyFlagsBitwise());
                nameTagVisibilityRule = v.getNameTagVisibilityRule();
                prefix = v.getPrefix().getString();
                suffix = v.getSuffix().getString();
            } else {
                displayName = "null";
                collisionRule = "null";
                color = "null";
                friendlyFlagsBitwise = "null";
                nameTagVisibilityRule = "null";
                prefix = "null";
                suffix = "null";
            }
            builder
                .append(" teamDisplayName: ").append(displayName)
                .append(" teamCollisionRule: ").append(collisionRule)
                .append(" teamColor: ").append(color)
                .append(" teamFriendlyFlagsBitwise: ").append(friendlyFlagsBitwise)
                .append(" teamNameTagVisibilityRule: ").append(nameTagVisibilityRule)
                .append(" teamPrefix: ").append(prefix)
                .append(" teamSuffix: ").append(suffix);
            builder.append(" playerNames: {");
            packet.getPlayerNames().forEach(name -> builder.append("\n  ").append(name));
            builder.append("\n}");
            return builder.toString();
        });
        s2c(TickStepS2CPacket.class, "TickStep", packet -> "tickSteps: " + packet.tickSteps());
        s2c(TitleFadeS2CPacket.class, "TitleFade", packet -> "fadeInTicks: " + packet.getFadeInTicks() + " stayTicks: " + packet.getStayTicks() + " fadeOutTicks: " + packet.getFadeOutTicks());
        s2c(TitleS2CPacket.class, "Title", packet -> "text: " + packet.text().getString());
        s2c(UnloadChunkS2CPacket.class, "UnloadChunk", packet -> "chunkX: " + packet.pos().x + " chunkZ: " + packet.pos().z);
        s2c(UpdateSelectedSlotS2CPacket.class, "UpdateSelectedSlot", packet -> "slot: " + packet.getSlot());
        s2c(UpdateTickRateS2CPacket.class, "UpdateTickRate", packet -> "isFrozen: " + packet.isFrozen() + " tickRate: " + packet.tickRate());
        s2c(VehicleMoveS2CPacket.class, "VehicleMove", packet -> "x: " + packet.getX() + " y: " + packet.getY() + " z: " + packet.getZ() + " yaw: " + packet.getYaw() + " pitch: " + packet.getPitch());
        s2c(WorldBorderCenterChangedS2CPacket.class, "WorldBorderCenterChanged", packet -> "centerX: " + packet.getCenterX() + " centerZ: " + packet.getCenterZ());
        s2c(WorldBorderInitializeS2CPacket.class, "WorldBorderInitialize", packet -> "centerX: " + packet.getCenterX() + " centerZ: " + packet.getCenterZ() + " maxRadius: " + packet.getMaxRadius() + " size: " + packet.getSize() + " sizeLerpTarget: " + packet.getSizeLerpTarget() + " sizeLerpTime: " + packet.getSizeLerpTime() + " warningBlocks: " + packet.getWarningBlocks() + " warningTime: " + packet.getWarningTime());
        s2c(WorldBorderInterpolateSizeS2CPacket.class, "WorldBorderInterpolateSize", packet -> "size: " + packet.getSize() + " sizeLerpTarget: " + packet.getSizeLerpTarget() + " sizeLerpTime: " + packet.getSizeLerpTime());
        s2c(WorldBorderSizeChangedS2CPacket.class, "WorldBorderSizeChanged", packet -> "sizeLerpTarget: " + packet.getSizeLerpTarget());
        s2c(WorldBorderWarningBlocksChangedS2CPacket.class, "WorldBorderWarningBlocksChanged", packet -> "warningBlocks: " + packet.getWarningBlocks());
        s2c(WorldBorderWarningTimeChangedS2CPacket.class, "WorldBorderWarningTimeChanged", packet -> "warningTime: " + packet.getWarningTime());
        s2c(WorldEventS2CPacket.class, "WorldEvent", packet -> "pos: " + packet.getPos().toShortString() + " data: " + packet.getData() + " eventId: " + packet.getEventId() + " isGlobal: " + packet.isGlobal());
        s2c(WorldTimeUpdateS2CPacket.class, "WorldTimeUpdate", packet -> "time: " + packet.getTime() + " timeOfDay: " + packet.getTimeOfDay());

        // query
        s2c(PingResultS2CPacket.class, "PingResult", packet -> "startTime: " + packet.startTime());
        s2c(QueryResponseS2CPacket.class, "QueryResponse", packet -> {
            StringBuilder builder = new StringBuilder("description: " + packet.metadata().description().getString() + " favicon: " + (packet.metadata().favicon().isPresent() ? byteArrToString(packet.metadata().favicon().get().iconBytes()) : "null") + " secureChatEnforced: " + packet.metadata().secureChatEnforced());

            String gameVersion;
            String protocolVersion;

            Optional<ServerMetadata.Version> v = packet.metadata().version();
            if (v.isPresent()) {
                ServerMetadata.Version version = v.get();
                gameVersion = version.gameVersion();
                protocolVersion = String.valueOf(version.protocolVersion());
            } else {
                gameVersion = "null";
                protocolVersion = "null";
            }

            builder.append(" gameVersion: ").append(gameVersion).append(" protocolVersion: ").append(protocolVersion);

            Optional<ServerMetadata.Players> v2 = packet.metadata().players();
            if (v2.isPresent()) {
                ServerMetadata.Players players = v2.get();

                builder.append(" maxPlayers: ").append(players.max()).append(" online: ").append(players.online()).append(" players: {");
                players.sample().forEach(profile -> builder.append("\n  profileName: ").append(profile.getName()).append(" profileId: ").append(profile.getId().toString()).append(" propertiesSize: ").append(profile.getProperties().size()));
            } else {
                builder.append(" maxPlayers: null online: null players: {");
            }
            builder.append("\n}");
            return builder.toString();
        });
    }

    @SuppressWarnings("unchecked")
    public static <T extends Packet<?>> PacketData<T> getS2C(T packet) {
        return (PacketData<T>) s2c.get(packet.getClass());
    }

    @SuppressWarnings("unchecked")
    public static <T extends Packet<?>> PacketData<T> getC2S(T packet) {
        return (PacketData<T>) c2s.get(packet.getClass());
    }

    public static <T extends Packet<?>> PacketData<T> getData(T packet) {
        return isClient(packet) ? getC2S(packet) : getS2C(packet);
    }

    private static String byteArrToString(byte[] arr) {
        int length = arr.length;
        if (length > 30) return length + " bytes";

        StringBuilder builder = new StringBuilder();
        builder.append(arr[0]);
        for (int i = 1; i < arr.length; i++)
            builder.append(",").append(arr[i]);
        return builder.toString();
    }

    public static class PacketData<T> {
        private final String name;
        private final Function<T, String> function;

        public PacketData(String name, Function<T, String> function) {
            this.name = name;
            this.function = function;
        }

        public PacketData(String name) {
            this.name = name;
            this.function = p -> "";
        }

        public String getName() {
            return name;
        }

        @SuppressWarnings("unchecked")
        public String funnyApply(Object packet) {
            return apply((T) packet);
        }

        public String apply(T packet) {
            try {
                return function.apply(packet);
            } catch (Exception e) {
                System.out.println("crashing packet: " + name + " - " + packet.getClass().getSimpleName() + ".class");
                throw new RuntimeException(e);
            }
        }
    }

    private static <T> void s2c(Class<T> clazz, String str, Function<T, String> function) {
        s2c.put(clazz, new PacketData<>(str, function));
    }

    private static <T> void s2c(Class<T> clazz, String str) {
        s2c.put(clazz, new PacketData<>(str));
    }

    private static <T> void c2s(Class<T> clazz, String str, Function<T, String> function) {
        c2s.put(clazz, new PacketData<>(str, function));
    }

    private static <T> void c2s(Class<T> clazz, String str) {
        c2s.put(clazz, new PacketData<>(str));
    }

    public static String nameOf(Packet<?> packet) {
        return nameOf(packet.getClass());
    }

    public static String nameOf(Class<?> clazz) {
        if (c2s.containsKey(clazz)) return c2s.get(clazz).name;
        if (s2c.containsKey(clazz)) return s2c.get(clazz).name;
        Logger.getGlobal().log(Level.WARNING, "packet name for " + clazz.getSimpleName() + " couldn't be found");
        return clazz.getSimpleName();
    }

    public static boolean isClient(Packet<?> packet) {
        return isClient(packet.getClass());
    }

    public static boolean isClient(Class<?> clazz) {
        return c2s.containsKey(clazz);
    }
}
