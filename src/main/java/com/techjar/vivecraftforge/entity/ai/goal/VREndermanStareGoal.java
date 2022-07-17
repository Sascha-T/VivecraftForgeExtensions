package com.techjar.vivecraftforge.entity.ai.goal;

import com.techjar.vivecraftforge.util.LogHelper;
import com.techjar.vivecraftforge.util.PlayerTracker;
import com.techjar.vivecraftforge.util.Quaternion;
import com.techjar.vivecraftforge.util.Util;
import com.techjar.vivecraftforge.util.VRPlayerData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;

public class VREndermanStareGoal extends LookAtPlayerGoal {

    public VREndermanStareGoal(Mob p_25520_, Class<? extends LivingEntity> p_25521_, float p_25522_) {
        super(p_25520_, p_25521_, p_25522_);
    }

    @Override
	public boolean canUse() {
		boolean orig = super.canUse(); // call this always so stuff gets set up

		LivingEntity target = this.mob.getTarget();
		if (target instanceof Player && PlayerTracker.hasPlayerData((Player)target)) {
			double dist = target.distanceToSqr(this.mob);
			return dist <= 256.0D && Util.shouldEndermanAttackVRPlayer((EnderMan) this.mob, (Player)target);
		}

		return orig;
	}

	@Override
	public void tick() {
		LivingEntity target = this.mob.getTarget();
		if (target instanceof Player && PlayerTracker.hasPlayerData((Player)target)) {
			VRPlayerData data = PlayerTracker.getPlayerDataAbsolute((Player)target);
			this.mob.getLookControl().setLookAt(data.head.posX, data.head.posY, data.head.posZ);
		} else {
			super.tick();
		}
	}
}
