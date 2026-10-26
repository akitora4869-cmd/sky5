package jp.skycombat;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

/** Shared, deterministic Trails-inspired RPG stat formulas. */
public final class RpgStats {
    private RpgStats() {}
    public record Values(int hp, int ep, int str, int def, int ats, int adf, int spd, int dex, int agl, int mov, int rng) {}
    public static Values of(LivingEntity e) {
        int lv = e instanceof net.minecraft.world.entity.monster.Monster && !(e instanceof Companion) ? Progress.enemyLevel(e) : Progress.level(e);
        boolean joshua = e instanceof Companion c && c.character().equals("joshua");
        boolean companion = e instanceof Companion;
        boolean enemy = e instanceof net.minecraft.world.entity.monster.Monster && !companion;
        int baseHp = enemy ? 140 : joshua ? 135 : companion ? 160 : 120;
        int hp = baseHp + (enemy ? 40 : 36) * (lv - 1) + (enemy ? 3 : 3) * (lv - 1) * (lv - 1);
        int ep = (enemy ? 60 : joshua ? 105 : companion ? 115 : 100) + 7 * (lv - 1);
        int str = (enemy ? 42 : joshua ? 48 : companion ? 44 : 40) + 6 * (lv - 1);
        int def = (enemy ? 40 : companion ? 42 : 38) + 5 * (lv - 1);
        int ats = (enemy ? 40 : joshua ? 46 : 42) + 6 * (lv - 1);
        int adf = (enemy ? 38 : 40) + 5 * (lv - 1);
        int spd = (enemy ? 27 : joshua ? 32 : 28) + lv / 3;
        return new Values(hp, ep, str, def, ats, adf, spd, 100 + lv / 2, 5 + lv / 8, 4, 1);
    }
    /** Keep existing health percentage on stat growth, heal fully on first initialization. */
    public static void apply(LivingEntity e) {
        if (e.getAttribute(Attributes.MAX_HEALTH) == null) return;
        int max = of(e).hp();
        var tag = Progress.data(e);
        int old = tag.getInt("AppliedHpMax");
        if (old != max) {
            float ratio = old <= 0 ? 1f : Math.min(1f, e.getHealth() / Math.max(1f, e.getMaxHealth()));
            e.getAttribute(Attributes.MAX_HEALTH).setBaseValue(max);
            if (e.isAlive()) e.setHealth(Math.max(1f, max * ratio));
            tag.putInt("AppliedHpMax", max);
        }
        if (!tag.getBoolean("EpInitialized")) {
            tag.putInt("Ep", of(e).ep());
            tag.putBoolean("EpInitialized", true);
        }
        tag.putInt("Ep", Math.min(of(e).ep(), Math.max(0, tag.getInt("Ep"))));
    }
    public static float physicalDamage(LivingEntity attacker, LivingEntity victim) {
        Values a = of(attacker), d = of(victim);
        return Math.max(2f, (float)(a.str() * 0.34 - d.def() * 0.12));
    }
    public static float artsDamage(LivingEntity attacker, LivingEntity victim) {
        Values a = of(attacker), d = of(victim);
        return Math.max(3f, (float)(a.ats() * 0.40 - d.adf() * 0.13));
    }
}
