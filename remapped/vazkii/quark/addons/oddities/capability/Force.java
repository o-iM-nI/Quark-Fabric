package vazkii.quark.addons.oddities.capability;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;

/**
 * @author WireSegal
 * Created at 4:30 PM on 3/1/20.
 */
public class Force {
    private final int magnitude;
    private final boolean pushing;
    private final Direction direction;
    private final int distance;
    private final BlockPos origin;

    public Force(int magnitude, boolean pushing, Direction direction, int distance, BlockPos origin) {
        this.magnitude = magnitude;
        this.pushing = pushing;
        this.direction = direction;
        this.distance = distance;
        this.origin = origin;
    }

    public int getMagnitude() {
        return magnitude;
    }

    public boolean isPushing() {
        return pushing;
    }

    public Direction getDirection() {
        return direction;
    }

    public int getDistance() {
        return distance;
    }

    public BlockPos getOrigin() {
        return origin;
    }


    public Vec3i add(Vec3i force) {
        return new Vec3i(force.getX() + direction.getOffsetX() * magnitude,
                force.getY() + direction.getOffsetY() * magnitude,
                force.getZ() + direction.getOffsetZ() * magnitude);
    }
}
