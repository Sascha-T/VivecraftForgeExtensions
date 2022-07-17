package com.techjar.vivecraftforge.util;

import com.mojang.math.Vector3d;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

public class Util {
	public static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
	static {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				scheduler.shutdownNow();
			}
		});
	}

	private Util() {
	}

	/*
	 * This is mostly copied from VSE
	 */
	public static boolean isHeadshot(LivingEntity target, Arrow arrow) {
		if (target.isPassenger()) return false;
		if (target instanceof Player) {
            Player player = (Player)target;
			if (player.isCrouching()) {
				//totalHeight = 1.65;
				//bodyHeight = 1.20;
				//headHeight = 0.45;
				if (arrow.getY() >= player.getY() + 1.20) return true;
			} else {
				//totalHeight = 1.80;
				//bodyHeight = 1.35;
				//headHeight = 0.45;
				if (arrow.getY() >= player.getY() + 1.35) return true;
			}
		} else {
			// TODO: mobs
		}
		return false;
	}

	public static boolean shouldEndermanAttackVRPlayer(EnderMan enderman, Player player) {
		ItemStack itemstack = player.getInventory().getArmor(3);
		if (!itemstack.isEnderMask(player, enderman)) {
			VRPlayerData data = PlayerTracker.getPlayerDataAbsolute(player);
			Quaternion quat = data.head.getRot();
			Vec3 vector3d = Util.fromVector3d(quat.multiply(new Vector3d(0, 0, -1)));
            Vec3 vector3d1 = new Vec3(enderman.getX() - data.head.posX, enderman.getEyeY() - data.head.posY, enderman.getX() - data.head.posZ);
			double d0 = vector3d1.length();
			vector3d1 = vector3d1.normalize();
			double d1 = vector3d.dot(vector3d1);
			return d1 > 1.0D - 0.025D / d0 && canEntityBeSeen(enderman, data.head.getPos());
		}

		return false;
	}

	public static boolean canEntityBeSeen(Entity entity, Vector3d playerEyePos) {
		Vector3d entityEyePos = new Vector3d(entity.getX(), entity.getEyeY(), entity.getZ());
		return entity.level.clip(new ClipContext(Util.fromVector3d(playerEyePos), Util.fromVector3d(entityEyePos), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity)).getType() == HitResult.Type.MISS;
	}

	public static void replaceAIGoal(Mob entity, GoalSelector goalSelector, Class<? extends Goal> targetGoal, Supplier<Goal> newGoalSupplier) {
		// @todo maybe mistake
        WrappedGoal goal = goalSelector.getAvailableGoals().stream().filter((g) -> targetGoal.isInstance(g.getGoal())).findFirst().orElse(null);
		if (goal != null) {
			goalSelector.removeGoal(goal.getGoal());
			goalSelector.addGoal(goal.getPriority(), newGoalSupplier.get());
			LogHelper.debug("Replaced {} in {}", targetGoal.getSimpleName(), entity);
		} else {
			LogHelper.debug("Couldn't find {} in {}", targetGoal.getSimpleName(), entity);
		}
	}

	public static String getMoney() {
        /*return "\n||====================================================================||\n" +
			"||//$\\\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\//$\\\\||\n" +
			"||(100)==================| FEDERAL RESERVE NOTE |================(100)||\n" +
			"||\\\\$//        ~         '------========--------'                \\\\$//||\n" +
			"||<< /        /$\\              // ____ \\\\                         \\ >>||\n" +
			"||>>|  12    //L\\\\            // ///..) \\\\         L38036133B   12 |<<||\n" +
			"||<<|        \\\\ //           || <||  >\\  ||                        |>>||\n" +
			"||>>|         \\$/            ||  $$ --/  ||        One Hundred     |<<||\n" +
			"||<<|      L38036133B        *\\\\  |\\_/  //* series                 |>>||\n" +
			"||>>|  12                     *\\\\/___\\_//*   1989                  |<<||\n" +
			"||<<\\      Treasurer     ______/Franklin\\________     Secretary 12 />>||\n" +
			"||//$\\                 ~|UNITED STATES OF AMERICA|~               /$\\\\||\n" +
			"||(100)===================  ONE HUNDRED DOLLARS =================(100)||\n" +
			"||\\\\$//\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\/\\\\$//||\n" +
			"||====================================================================||";*/
        return "             _.oood\"\"\"\"\"\"\"booo._\n" +
            "         _.o\"\"      _____    * \"\"o._\n" +
            "       oP\"  _.ooo\"\"\"\"   \"\"\"\"o|o*_* \"Yo\n" +
            "     o8   oP                 | |\"._* `8o\n" +
            "    d'  o8'_.--._            | |/  ,\\* `b\n" +
            "   d'  d'.' __   \".          | |: (( `\\\n" +
            "  8'  d'/,-\"  `.   :         | |  ||\\_/* `8\n" +
            " 8   8'|/      :   :    |)   _ |  || |`|   8\n" +
            ",8  8          :  :   /)| \\ || |\\_|| | |8  8.\n" +
            "8' ,8         /  :    \" /_) |`:' | | | |8. `8\n" +
            "8  8'        /  /       _ _='  \\ ' __   __  8\n" +
            "8  8        /  /        \\|__ |  | |  | | 8| 8\n" +
            "8  8.      /  /         ||   |  | |-:' | 8| 8\n" +
            "8. `8    ,' ,'       __/ |__ |__| |  \\ |__|,8\n" +
            "`8  8  ,' ,'      _ /     __ . . . . . .8LL8'\n" +
            " 8   8\"   `------'/(    ,'  `.`. | | ,-|8  8\n" +
            "  8.(_________dd_/  \\__/ '  0|`.`: |: (8 ,8\n" +
            "   Y.  Y.                    | :/| |,\\|* .P\n" +
            "    Y.  \"8.          .,o     | | |,|\"*  ,P\n" +
            "     \"8.  \"Yo_               | |p|\"* ,8\"\n" +
            "       \"Y_   `\"ooo.__   __.oo|\"* * _P\"\n" +
            "         `'\"oo_     \"\"\"\"\"    * _oo\"\"'\n" +
            "              `\"\"\"boooooood\"\"\"'"; // hehehe
	}

    public static Vector3d fromVec3(Vec3 a) {
        return new Vector3d(a.x, a.y, a.z);
    }
    public static Vec3 fromVector3d(Vector3d a) {
        return new Vec3(a.x, a.y, a.z);
    }
}
