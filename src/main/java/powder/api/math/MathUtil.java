package powder.api.math;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class MathUtil {

    private static float delta() {
        return MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(true);
    }

    public static Vec3d interpolate(Entity entity) {
        float d = delta();
        return new Vec3d(
                MathHelper.lerp(d, entity.lastRenderX, entity.getX()),
                MathHelper.lerp(d, entity.lastRenderY, entity.getY()),
                MathHelper.lerp(d, entity.lastRenderZ, entity.getZ())
        );
    }

    public static double interpolate(double prev, double now) {
        return MathHelper.lerp(delta(), prev, now);
    }

    public static float interpolate(float prev, float now) {
        return MathHelper.lerp(delta(), prev, now);
    }

    public static double absSinAnimation(double x) {
        return Math.abs(Math.sin(x));
    }

    public static double sin(double x) {
        return Math.sin(x);
    }

    public static double cos(double x) {
        return Math.cos(x);
    }

}
