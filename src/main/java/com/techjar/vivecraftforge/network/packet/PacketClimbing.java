package com.techjar.vivecraftforge.network.packet;

import com.techjar.vivecraftforge.Config;

import com.techjar.vivecraftforge.network.IPacket;
import com.techjar.vivecraftforge.util.BlockListMode;
import com.techjar.vivecraftforge.util.LogHelper;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Supplier;

/*
 * For whatever reason this uses a serializer instead of just
 * packing the data into the buffer.
 */
public class PacketClimbing implements IPacket {
	public BlockListMode blockListMode;
	public List<? extends String> blockList;

	public PacketClimbing() {
	}

	public PacketClimbing(BlockListMode blockListMode, List<? extends String> blockList) {
		this.blockListMode = blockListMode;
		this.blockList = blockList;
	}

	@Override
	public void encode(final FriendlyByteBuf buffer) {
		buffer.writeByte(1); // allow climbey
		buffer.writeByte(Config.blockListMode.get().ordinal());
		for (String s : Config.blockList.get()) {
            buffer.writeVarInt(s.length());
			buffer.writeBytes(s.getBytes(StandardCharsets.UTF_8));
		}
	}

	@Override
	public void decode(final FriendlyByteBuf buffer) {
	}

	@Override
	public void handleClient(final Supplier<NetworkEvent.Context> context) {
	}

	@Override
	public void handleServer(final Supplier<NetworkEvent.Context> context) {
		ServerPlayer player = context.get().getSender();
		player.fallDistance = 0;
		LogHelper.info("Climbing!!");
		player.connection.aboveGroundTickCount = 0;
	}
}
