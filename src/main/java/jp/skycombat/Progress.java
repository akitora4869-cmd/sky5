package jp.skycombat;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class Progress {
    private Progress() {}
    public static final String KEY="SkyCombatStats";
    public static CompoundTag data(LivingEntity entity) {
        CompoundTag root=entity.getPersistentData();
        if (!root.contains(KEY,10)) { CompoundTag init=new CompoundTag(); init.putInt("Level",1); init.putInt("Xp",0); init.putInt("Cp",0); init.putInt("Ep",100); root.put(KEY,init); }
        return root.getCompound(KEY);
    }
    public static int level(LivingEntity entity) { return Math.max(1,data(entity).getInt("Level")); }
    public static int xpRequired(int level) { return 80+level*level*12; }
    public static int cp(LivingEntity entity) { return data(entity).getInt("Cp"); }
    public static int ep(LivingEntity entity) { return data(entity).getInt("Ep"); }
    public static boolean spendCp(LivingEntity e,int amount) { CompoundTag t=data(e); if(t.getInt("Cp")<amount)return false; t.putInt("Cp",t.getInt("Cp")-amount);return true; }
    public static boolean spendEp(LivingEntity e,int amount) { CompoundTag t=data(e); if(t.getInt("Ep")<amount)return false; t.putInt("Ep",t.getInt("Ep")-amount);return true; }
    public static void addCp(LivingEntity e,int amount) { CompoundTag t=data(e);t.putInt("Cp",Math.min(200,Math.max(0,t.getInt("Cp")+amount))); }
    public static void addXp(LivingEntity e,int amount) { CompoundTag t=data(e);int lv=level(e),xp=t.getInt("Xp")+Math.max(0,amount); while(lv<100&&xp>=xpRequired(lv)){xp-=xpRequired(lv);lv++;} t.putInt("Level",lv);t.putInt("Xp",xp); if (e instanceof net.minecraft.world.entity.player.Player || e instanceof Companion) RpgStats.apply(e); if(e instanceof Companion c)c.refreshStats(); }
    public static int reward(int base,int enemyLevel,int playerLevel){ int delta=enemyLevel-playerLevel;double mult=delta>=0?Math.min(8,Math.pow(1.35,delta)):Math.max(.01,Math.pow(.65,-delta));return Math.max(1,(int)Math.round(base*mult)); }
    public static int enemyLevel(LivingEntity enemy){ if(enemy.getPersistentData().contains("SkyEnemyLevel"))return Math.max(1,enemy.getPersistentData().getInt("SkyEnemyLevel"));return level(enemy); }
    public static void setEnemyLevel(LivingEntity enemy,int level){
        enemy.getPersistentData().putInt("SkyEnemyLevel",Math.max(1,Math.min(100,level)));
        if(enemy instanceof net.minecraft.world.entity.monster.Monster && !(enemy instanceof Companion)) {
            RpgStats.apply(enemy);
            if(enemy.getAttribute(Attributes.ATTACK_DAMAGE)!=null)
                enemy.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(Math.max(2.0,RpgStats.of(enemy).str()/10.0));
        }
    }
}
