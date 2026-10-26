package jp.skycombat;

import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class Companion extends Zombie {
    private String character="estelle";
    private UUID owner;
    private int attackTick=0;
    private int healItems=3;
    private int artsCooldown=0;
    public Companion(EntityType<? extends Zombie> type,Level level){super(type,level);setPersistenceRequired();setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(SkyCombat.ESTELLE_STAFF.get()));}
    public static AttributeSupplier.Builder attributes(){return Zombie.createAttributes().add(Attributes.MAX_HEALTH,30).add(Attributes.MOVEMENT_SPEED,.28).add(Attributes.ATTACK_DAMAGE,4);}
    @Override protected void registerGoals(){
        goalSelector.addGoal(1,new FloatGoal(this));
        targetSelector.addGoal(1,new HurtByTargetGoal(this));
        goalSelector.addGoal(3,new MeleeAttackGoal(this,1.05,true));
        goalSelector.addGoal(6,new RandomStrollGoal(this,.8));
        goalSelector.addGoal(7,new LookAtPlayerGoal(this,Player.class,7));
    }
    // Do not inherit zombie ambient, hurt, or death sounds for human companions.
    @Override protected net.minecraft.sounds.SoundEvent getAmbientSound(){return null;}
    @Override protected net.minecraft.sounds.SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource source){return null;}
    @Override protected net.minecraft.sounds.SoundEvent getDeathSound(){return null;}
    @Override protected void playStepSound(net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState state){
        super.playStepSound(pos, state);
    }
    @Override protected boolean isSunSensitive(){return false;}
    @Override public boolean canAttackType(EntityType<?> type){return type!=SkyCombat.COMPANION.get() && super.canAttackType(type);}
    public void configure(String name){character=name;setCustomName(Component.literal(name.equals("joshua")?"Joshua Bright":"Estelle Bright"));setCustomNameVisible(true);refreshStats();}
    public String character(){return character;}
    public UUID partyOwner(){return owner;}
    public void refreshStats(){
        RpgStats.apply(this);
        if (getAttribute(Attributes.ATTACK_DAMAGE) != null)
            getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(Math.max(2, RpgStats.of(this).str() / 10.0));
    }

    @Override protected InteractionResult mobInteract(Player player,InteractionHand hand){
        if(!level().isClientSide&&hand==InteractionHand.MAIN_HAND){
            if(owner==null){owner=player.getUUID();player.sendSystemMessage(Component.literal(getName().getString()+" joined your party. Right click again to dismiss."));}
            else if(owner.equals(player.getUUID())){owner=null;player.sendSystemMessage(Component.literal(getName().getString()+" is now roaming freely."));}
            else player.sendSystemMessage(Component.literal("This companion is following another player."));
            return InteractionResult.CONSUME;
        }
        return InteractionResult.SUCCESS;
    }
    @Override public void tick(){super.tick();if(level().isClientSide||!(level() instanceof ServerLevel server))return;
        if(artsCooldown>0)artsCooldown--;
        if(tickCount%10!=0)return;
        if(owner!=null){Player player=server.getServer().getPlayerList().getPlayer(owner);if(player!=null&&player.level()==level()){
            if(distanceToSqr(player)>144&&distanceToSqr(player)<2500){getNavigation().moveTo(player,1.1);}
            if(distanceToSqr(player)>=2500&&getTarget()==null){teleportTo(player.getX()+1,player.getY(),player.getZ()+1);}
            if(player.getLastHurtByMob() instanceof Monster attacker && attacker.isAlive())setTarget(attacker);
            else if(player.getLastHurtMob() instanceof Monster victim && victim.isAlive())setTarget(victim);
        }}
        if(getTarget()==null||!getTarget().isAlive()){
            LivingEntity enemy=level().getEntitiesOfClass(Monster.class,new AABB(blockPosition()).inflate(12),m->m!=this&&!(m instanceof Companion)&&m.isAlive()).stream().min((a,b)->Double.compare(distanceToSqr(a),distanceToSqr(b))).orElse(null);
            if(enemy!=null)setTarget(enemy);
        }
        LivingEntity target=getTarget();
        if(getHealth()<getMaxHealth()*.35){
            if(healItems>0){healItems--;heal(Math.min(getMaxHealth()*.20f,getMaxHealth()-getHealth()));}
            else if(Progress.spendEp(this,15))heal(Math.min(getMaxHealth()*.15f,getMaxHealth()-getHealth()));
            if(target!=null){Vec3 away=position().subtract(target.position()).normalize().scale(7);getNavigation().moveTo(getX()+away.x,getY(),getZ()+away.z,1.3);}
            return;
        }
        if(target!=null&&target.isAlive()&&distanceToSqr(target)<64&&artsCooldown==0){
            if(Progress.cp(this)>=30&&Progress.spendCp(this,30)){
                target.hurt(damageSources().mobAttack(this),RpgStats.physicalDamage(this,target)*1.5f);artsCooldown=50;
            }else if(distanceToSqr(target)>12&&Progress.spendEp(this,12)){
                target.hurt(damageSources().magic(),RpgStats.artsDamage(this,target));artsCooldown=60;
            }
        }
    }
    @Override public boolean doHurtTarget(net.minecraft.world.entity.Entity entity){boolean success=super.doHurtTarget(entity);if(success)Progress.addCp(this,10);return success;}
    @Override public void addAdditionalSaveData(CompoundTag nbt){super.addAdditionalSaveData(nbt);nbt.putString("SkyCharacter",character);if(owner!=null)nbt.putUUID("SkyOwner",owner);nbt.putInt("SkyItems",healItems);}
    @Override public void readAdditionalSaveData(CompoundTag nbt){super.readAdditionalSaveData(nbt);character=nbt.getString("SkyCharacter");if(character.isEmpty())character="estelle";owner=nbt.hasUUID("SkyOwner")?nbt.getUUID("SkyOwner"):null;healItems=nbt.contains("SkyItems")?nbt.getInt("SkyItems"):3;refreshStats();}
}
