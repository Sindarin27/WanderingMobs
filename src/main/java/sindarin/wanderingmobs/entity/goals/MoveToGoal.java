package sindarin.wanderingmobs.entity.goals;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.EnumSet;

public class MoveToGoal extends Goal {
    final CreatureEntity entity;
    final BlockPos blockpos;
    final double maxDistance;
    final double speed;

    public MoveToGoal(CreatureEntity entityIn, double distanceIn, double speedIn, BlockPos pos) {
        this.entity = entityIn;
        this.maxDistance = distanceIn;
        this.speed = speedIn;
        this.blockpos = pos;
        this.setMutexFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    public void resetTask() {
        entity.getNavigator().clearPath();
    }

    public boolean shouldExecute() {
        return blockpos != null && this.isWithinDistance(blockpos, this.maxDistance);
    }

    public void tick() {
        if (blockpos != null && entity.getNavigator().noPath()) {
            if (this.isWithinDistance(blockpos, 10.0D)) {
                Vec3d vec3d = (new Vec3d((double) blockpos.getX() - this.entity.func_226277_ct_(), (double) blockpos.getY() - this.entity.func_226278_cu_(), (double) blockpos.getZ() - this.entity.func_226281_cx_())).normalize();
                Vec3d vec3d1 = vec3d.scale(10.0D).add(this.entity.func_226277_ct_(), this.entity.func_226278_cu_(), this.entity.func_226281_cx_());
                entity.getNavigator().tryMoveToXYZ(vec3d1.x, vec3d1.y, vec3d1.z, this.speed);
            } else {
                entity.getNavigator().tryMoveToXYZ(blockpos.getX(), blockpos.getY(), blockpos.getZ(), this.speed);
            }
        }

    }

    private boolean isWithinDistance(BlockPos pos, double distance) {
        return !pos.withinDistance(this.entity.getPositionVec(), distance);
    }
}
