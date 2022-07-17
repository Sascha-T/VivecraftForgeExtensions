package com.techjar.vivecraftforge.util;

import com.mojang.math.Vector3d;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.phys.Vec3;

public class AimFixHandler extends ChannelInboundHandlerAdapter {
	private final Connection netManager;
    private final ServerPlayer player;
	public AimFixHandler(Connection netManager, ServerPlayer player) {
		this.netManager = netManager;
        this.player = player;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		boolean isCapturedPacket = msg instanceof ServerboundUseItemPacket || msg instanceof ServerboundUseItemOnPacket || msg instanceof ServerboundPlayerActionPacket;

		if (!PlayerTracker.hasPlayerData(player) || !isCapturedPacket || player.getServer() == null) {
			// we don't need to handle this packet, just defer to the next handler in the pipeline
			ctx.fireChannelRead(msg);
			return;
		}

		LogHelper.debug("Captured message {}", msg.getClass().getSimpleName());
		player.getServer().doRunTask(new TickTask(0, new Runnable() {
            @Override
            public void run() {
                // Save all the current orientation data
                Vector3d oldPos = Util.fromVec3(player.getPosition(0f)); // @todo might be janky
                //Vector3d oldPrevPos = new Vector3d(player.getPre, player.prevPosY, player.prevPosZ);
                float oldPitch = player.getRotationVector().x; // @todo potential error
                float oldYaw = player.getRotationVector().y;
                float oldYawHead = player.yHeadRot;
                //float oldPrevPitch = player.prev;
                //float oldPrevYaw = player.prevRotationYaw;
                //float oldPrevYawHead = player.prevRotationYawHead;
                float oldEyeHeight = player.getEyeHeight();

                VRPlayerData data = null;
                if (PlayerTracker.hasPlayerData(player)) { // Check again in case of race condition
                    data = PlayerTracker.getPlayerDataAbsolute(player);
                    Vector3d pos = data.getController(0).getPos();
                    Vector3d aim = data.getController(0).getRot().multiply(new Vector3d(0, 0, -1));

                    // Inject our custom orientation data
                    player.setPos(pos.x, pos.y, pos.z);
                    player.setXRot((float)Math.toDegrees(Math.asin(-aim.y)));
                    player.setYRot((float)Math.toDegrees(Math.atan2(-aim.x, aim.z)));
                    // player.eyeHeight = 0;

                    // Set up offset to fix relative positions
                    data = PlayerTracker.getPlayerData(player);
                    oldPos.add(new Vector3d(-pos.x, -pos.y, -pos.z));
                    data.offset = oldPos;
                }

                // Call the packet handler directly
                // This is several implementation details that we have to replicate
                try {
                    if (netManager.channel().isOpen()) {
                        try {
                            ((Packet)msg).handle(netManager.getPacketListener());
                        } catch (Exception e) { // Apparently might get thrown and can be ignored (ThreadQuickExitException)
                        }
                    }
                } finally {
                    // Vanilla uses SimpleInboundChannelHandler, which automatically releases
                    // by default, so we're expected to release the packet once we're done.
                    ReferenceCountUtil.release(msg);
                }

                // Restore the original orientation data
                player.setPos(oldPos.x, oldPos.y, oldPos.z);
                player.setXRot(oldPitch);
                player.setYRot(oldYaw);
                player.setYHeadRot(oldYawHead);
			/*player.prevRotationPitch = oldPrevPitch;
			player.prevRotationYaw = oldPrevYaw;
			player.prevRotationYawHead = oldPrevYawHead;
			player.eyeHeight = oldEyeHeight;*/

                // Reset offset
                if (data != null)
                    data.offset = new Vector3d(0, 0, 0);
            }
        }));
	}
}
