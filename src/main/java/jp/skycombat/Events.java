package jp.skycombat;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraft.client.renderer.entity.ZombieRenderer;

@Mod.EventBusSubscriber(modid=SkyCombat.ID)
public class Events {
    @SubscribeEvent public static void commands(RegisterCommandsEvent e){Commands.register(e.getDispatcher());}
    @SubscribeEvent public static void hurt(LivingHurtEvent e){
        if(e.getSource().getEntity() instanceof LivingEntity attacker) {
            Progress.addCp(attacker,5);
            // Scale ordinary melee attacks to the RPG HP range; arts/crafts have their own damage.
            if ((attacker instanceof Player || attacker instanceof Companion)
                    && e.getSource().is(net.minecraft.tags.DamageTypeTags.IS_PROJECTILE) == false
                    && e.getSource().getMsgId().equals("player")) {
                e.setAmount(RpgStats.physicalDamage(attacker,e.getEntity()));
            }
        }
        if (e.getSource().getEntity() instanceof Monster mob && !(mob instanceof Companion)
                && (e.getEntity() instanceof Player || e.getEntity() instanceof Companion)
                && e.getSource().getMsgId().equals("mob")) {
            e.setAmount(RpgStats.physicalDamage(mob, e.getEntity()));
        }
        // Environmental hazards remain meaningful despite the higher HP scale.
        if ((e.getEntity() instanceof Player || e.getEntity() instanceof Companion)
                && e.getSource().getEntity() == null) e.setAmount(e.getAmount()*8f);
        if(e.getEntity() instanceof Player||e.getEntity() instanceof Companion)Progress.addCp(e.getEntity(),3);
    }
    @SubscribeEvent public static void death(LivingDeathEvent e){
        LivingEntity victim=e.getEntity();if(!(victim.level() instanceof ServerLevel server))return;
        if(victim instanceof Companion c){
            // Allow the normal death sequence to finish; do not play a death sound
            // and then silently restore HP by cancelling LivingDeathEvent.
            UniqueRoster roster=UniqueRoster.get(server);
            if(roster.registered(c.character()) && c.getUUID().equals(roster.uuid(c.character()))) {
                roster.forget(c.character());
            }
            return;
        }
        if(!(victim instanceof Monster))return;
        int lv=Progress.enemyLevel(victim),base=20+lv*5;
        for(Player p:server.players())if(p.distanceToSqr(victim)<24*24)Progress.addXp(p,Progress.reward(base,lv,Progress.level(p)));
        for(Companion c:server.getEntitiesOfClass(Companion.class,victim.getBoundingBox().inflate(24)))Progress.addXp(c,Progress.reward(base,lv,Progress.level(c)));
    }
    @SubscribeEvent public static void join(EntityJoinLevelEvent e){
        if(e.getLevel().isClientSide())return;
        if(e.getEntity() instanceof Monster monster && !(monster instanceof Companion) && e.getLevel() instanceof ServerLevel server){
            // Keep explicitly assigned levels; existing monsters without a level get a nearby-player-based level.
            if (!monster.getPersistentData().contains("SkyEnemyLevel")) {
                int lv = server.players().stream().min((a,b)->Double.compare(a.distanceToSqr(monster),b.distanceToSqr(monster)))
                    .map(Progress::level).orElse(1);
                Progress.setEnemyLevel(monster, lv);
            } else {
                Progress.setEnemyLevel(monster, Progress.enemyLevel(monster));
            }
        }
        if(e.getEntity() instanceof Companion c && e.getLevel() instanceof ServerLevel s){
            UniqueRoster roster=UniqueRoster.get(s);String id=c.character();
            if(roster.registered(id)&&!roster.uuid(id).equals(c.getUUID())){e.setCanceled(true);return;}
            if(!roster.registered(id))roster.register(id,c.getUUID());
        }
    }
    @Mod.EventBusSubscriber(modid=SkyCombat.ID,bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class ModEvents {
        @SubscribeEvent public static void attributes(EntityAttributeCreationEvent e){e.put(SkyCombat.COMPANION.get(),Companion.attributes().build());}
    }
    @Mod.EventBusSubscriber(modid=SkyCombat.ID,bus=Mod.EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
    public static class ClientEvents {
        @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers e){e.registerEntityRenderer(SkyCombat.COMPANION.get(), EstelleRenderer::new);}
    }
}
