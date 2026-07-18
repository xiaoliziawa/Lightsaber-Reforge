package fiskfille.utils.helper;

import com.fiskmods.lightsabers.Lightsabers;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;

public final class FiskServerUtils {
    private FiskServerUtils() {
    }

    public static String getActiveModId() {
        return Lightsabers.MODID;
    }

    public static String getActiveModName() {
        return Lightsabers.NAME;
    }

    public static boolean isMeleeDamage(DamageSource source) {
        return source.getEntity() != null
                && !source.is(DamageTypeTags.WITCH_RESISTANT_TO)
                && !source.is(DamageTypeTags.IS_EXPLOSION)
                && !source.is(DamageTypeTags.IS_PROJECTILE)
                && !source.is(DamageTypeTags.IS_FIRE);
    }

    public static double interpolate(double a, double b, double progress) {
        return a + (b - a) * progress;
    }

    public static float interpolate(float a, float b, float progress) {
        return (float) interpolate(a, b, (double) progress);
    }
}
