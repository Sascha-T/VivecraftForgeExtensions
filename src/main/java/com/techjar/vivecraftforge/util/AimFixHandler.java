package com.techjar.vivecraftforge.util;

import net.minecraft.server.network.ServerGamePacketListenerImpl;
import com.mojang.math.Vector3d;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;


public class AimFixHandler extends ChannelInboundHandlerAdapter {
	private final Connection netManager;
	private ServerPlayer  player;
	public AimFixHandler(Connection netManager) {
		this.netManager = netManager;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		ServerPlayer player = ((ServerGamePacketListenerImpl)netManager.getPacketListener()).player;
		boolean isCapturedPacket = msg instanceof ServerboundUseItemPacket || msg instanceof ServerboundUseItemOnPacket || msg instanceof ServerboundPlayerActionPacket;

		if (!PlayerTracker.hasPlayerData(player) || !isCapturedPacket || player.getServer() == null) {
			// we don't need to handle this packet, just defer to the next handler in the pipeline
			ctx.fireChannelRead(msg);
			return;
		}

		LogHelper.debug("Captured message {}", msg.getClass().getSimpleName());
		player.getServer().submitAsync(()-> {
                // Save all the current orientation data
            	
    			Vector3d oldPos = Util.fromVec3(player.position());// TODO: This may work. It used to be "getPositionVec" in 1.16.5.
    			//Vector3d oldPrevPos = new Vector3d(player.xOld, player.yOld, player.zOld);
    			float oldPitch = player.getXRot();//rotationPitch;
    			float oldYaw = player.yBodyRot;//rotationYaw;
    			float oldYawHead = player.yHeadRot;//rotationYawHead;
    			//float oldPrevPitch = player.xRotO;//prevRotationPitch;
    			//float oldPrevYaw = player.yBodyRotO;//prevRotationYaw;
    			//float oldPrevYawHead = player.yHeadRotO;//prevRotationYawHead;
    			//float oldEyeHeight = player.getEyeHeight();//same as before

                VRPlayerData data = null;
                if (PlayerTracker.hasPlayerData(player)) { // Check again in case of race condition
                    data = PlayerTracker.getPlayerDataAbsolute(player);
                    Vector3d pos = data.getController(0).getPos();
                    Vector3d aim = data.getController(0).getRot().multiply(new Vector3d(0, 0, -1));

                    // Inject our custom orientation data
                    /*player.setPosRaw(pos.x, pos.y, pos.z);
    				player.xOld = pos.x;
    				player.yOld = pos.y;
    				player.zOld = pos.z;
    				player.setXRot((float)Math.toDegrees(Math.asin(-aim.y)));
    				player.yBodyRot = (float)Math.toDegrees(Math.atan2(-aim.x, aim.z));
    				player.xRotO = player.getXRot();
    				player.yBodyRotO = player.yHeadRotO = player.yHeadRot = player.yBodyRot;*/
    				//player.eyeHeight = 0;
					
                    //player.setPos(pos.x, pos.y, pos.z);
                    player.setXRot((float)Math.toDegrees(Math.asin(-aim.y)));
                    player.setYRot((float)Math.toDegrees(Math.atan2(-aim.x, aim.z)));
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
                /*player.setPosRaw(oldPos.x, oldPos.y, oldPos.z);
    			player.xOld = oldPrevPos.x;
    			player.yOld = oldPrevPos.y;
    			player.zOld = oldPrevPos.z;
    			player.setXRot(oldPitch);
    			player.yBodyRot = oldYaw;
    			player.yHeadRot = oldYawHead;
    			player.xRotO = oldPrevPitch;*/
    			//player.eyeHeight = oldEyeHeight;
    			
                //player.setPos(oldPos.x, oldPos.y, oldPos.z);
                player.setXRot(oldPitch);
                player.setYRot(oldYaw);
                player.setYHeadRot(oldYawHead);

                // Reset offset
                if (data != null)
                    data.offset = new Vector3d(0, 0, 0);
            
        });
	}
}
