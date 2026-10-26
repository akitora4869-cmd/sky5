package jp.skycombat;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.AABB;

public final class Commands {
    private Commands(){}
    public static void register(CommandDispatcher<CommandSourceStack> d){
        d.register(net.minecraft.commands.Commands.literal("sky")
            .then(net.minecraft.commands.Commands.literal("stats").executes(c->{ServerPlayer p=c.getSource().getPlayerOrException();var t=Progress.data(p);msg(c.getSource(),"Lv "+Progress.level(p)+" EXP "+t.getInt("Xp")+"/"+Progress.xpRequired(Progress.level(p))+" HP "+(int)p.getHealth()+"/"+RpgStats.of(p).hp()+" EP "+Progress.ep(p)+"/"+RpgStats.of(p).ep()+" CP "+Progress.cp(p)+" STR "+RpgStats.of(p).str()+" DEF "+RpgStats.of(p).def()+" ATS "+RpgStats.of(p).ats()+" ADF "+RpgStats.of(p).adf()+" SPD "+RpgStats.of(p).spd());return 1;}))
            .then(net.minecraft.commands.Commands.literal("enemy-stats").executes(c->{ServerPlayer p=c.getSource().getPlayerOrException();Monster m=p.serverLevel().getEntitiesOfClass(Monster.class,p.getBoundingBox().inflate(12),e->e.isAlive()&&!(e instanceof Companion)).stream().min((a,b)->Double.compare(p.distanceToSqr(a),p.distanceToSqr(b))).orElse(null);if(m==null){msg(c.getSource(),"No enemy within 12 blocks");return 0;}var v=RpgStats.of(m);msg(c.getSource(),m.getName().getString()+" Lv"+Progress.enemyLevel(m)+" HP "+(int)m.getHealth()+"/"+v.hp()+" EP "+Progress.ep(m)+"/"+v.ep()+" STR "+v.str()+" DEF "+v.def()+" ATS "+v.ats()+" ADF "+v.adf()+" SPD "+v.spd());return 1;}))
            .then(net.minecraft.commands.Commands.literal("craft").then(net.minecraft.commands.Commands.literal("whirlwind").executes(c->{ServerPlayer p=c.getSource().getPlayerOrException();if(!Progress.spendCp(p,30)){msg(c.getSource(),"Need 30 CP");return 0;}int count=0;for(LivingEntity e:p.level().getEntitiesOfClass(Monster.class,p.getBoundingBox().inflate(4),m->m.isAlive()&&!(m instanceof Companion))){e.hurt(p.damageSources().playerAttack(p),RpgStats.physicalDamage(p,e)*1.4f);count++;}msg(c.getSource(),"Whirlwind: "+count+" targets");return 1;})))
            .then(net.minecraft.commands.Commands.literal("arts").executes(c->{ServerPlayer p=c.getSource().getPlayerOrException();if(!Progress.spendEp(p,12)){msg(c.getSource(),"Need 12 EP");return 0;}LivingEntity target=p.level().getEntitiesOfClass(Monster.class,p.getBoundingBox().inflate(10),m->m.isAlive()&&!(m instanceof Companion)).stream().min((a,b)->Double.compare(p.distanceToSqr(a),p.distanceToSqr(b))).orElse(null);if(target!=null){target.hurt(p.damageSources().magic(),RpgStats.artsDamage(p,target));msg(c.getSource(),"Arts hit "+target.getName().getString());}else msg(c.getSource(),"No enemy in range (EP spent)");return 1;}))
            .then(net.minecraft.commands.Commands.literal("test").requires(s->s.hasPermission(2))
                .then(net.minecraft.commands.Commands.literal("npc").then(net.minecraft.commands.Commands.argument("name",StringArgumentType.word()).executes(c->spawnNpc(c.getSource(),StringArgumentType.getString(c,"name")))))
                .then(net.minecraft.commands.Commands.literal("enemy").then(net.minecraft.commands.Commands.argument("level",IntegerArgumentType.integer(1,100)).executes(c->{ServerPlayer p=c.getSource().getPlayerOrException();Monster enemy=(Monster)EntityType.ZOMBIE.create(p.serverLevel());if(enemy==null)return 0;enemy.moveTo(p.getX()+3,p.getY(),p.getZ()+3,p.getYRot(),0);Progress.setEnemyLevel(enemy,IntegerArgumentType.getInteger(c,"level"));p.serverLevel().addFreshEntity(enemy);msg(c.getSource(),"Spawned Lv "+Progress.enemyLevel(enemy)+" zombie (HP "+(int)enemy.getMaxHealth()+", STR "+RpgStats.of(enemy).str()+", DEF "+RpgStats.of(enemy).def()+")");return 1;})))
                .then(net.minecraft.commands.Commands.literal("cp").then(net.minecraft.commands.Commands.argument("amount",IntegerArgumentType.integer(0,200)).executes(c->{ServerPlayer p=c.getSource().getPlayerOrException();Progress.data(p).putInt("Cp",IntegerArgumentType.getInteger(c,"amount"));return 1;})))
                .then(net.minecraft.commands.Commands.literal("ep").then(net.minecraft.commands.Commands.argument("amount",IntegerArgumentType.integer(0,100000)).executes(c->{ServerPlayer p=c.getSource().getPlayerOrException();Progress.data(p).putInt("Ep",IntegerArgumentType.getInteger(c,"amount"));return 1;})))
                .then(net.minecraft.commands.Commands.literal("level").then(net.minecraft.commands.Commands.argument("amount",IntegerArgumentType.integer(1,100)).executes(c->{ServerPlayer p=c.getSource().getPlayerOrException();Progress.data(p).putInt("Level",IntegerArgumentType.getInteger(c,"amount"));RpgStats.apply(p);return 1;})))
                .then(net.minecraft.commands.Commands.literal("reset-npc").then(net.minecraft.commands.Commands.argument("name",StringArgumentType.word()).executes(c->{ServerLevel level=c.getSource().getLevel();String name=StringArgumentType.getString(c,"name");UniqueRoster r=UniqueRoster.get(level);if(!r.registered(name))return 0;Entity entity=find(c.getSource(),r.uuid(name));if(entity!=null)entity.discard();r.forget(name);msg(c.getSource(),"Cleared registry for "+name+". Ensure unloaded copies are not present before respawning.");return 1;})))
            ));
    }
    private static Entity find(CommandSourceStack src,java.util.UUID id){for(ServerLevel l:src.getServer().getAllLevels()){Entity e=l.getEntity(id);if(e!=null)return e;}return null;}
    private static int spawnNpc(CommandSourceStack src,String name) throws com.mojang.brigadier.exceptions.CommandSyntaxException{
        if(!name.equals("estelle")&&!name.equals("joshua")){msg(src,"Use estelle or joshua");return 0;}
        ServerPlayer player=src.getPlayerOrException();UniqueRoster roster=UniqueRoster.get(player.serverLevel());
        if(roster.registered(name)){Entity existing=find(src,roster.uuid(name));if(existing!=null){existing.teleportTo(player.getX()+2,player.getY(),player.getZ());msg(src,"Moved existing "+name);return 1;}msg(src,"NPC already registered in another/unloaded chunk; no duplicate spawned. Use /sky test reset-npc only after checking.");return 0;}
        Companion npc=SkyCombat.COMPANION.get().create(player.serverLevel());if(npc==null)return 0;npc.configure(name);npc.moveTo(player.getX()+2,player.getY(),player.getZ()+2,0,0);player.serverLevel().addFreshEntity(npc);msg(src,"Spawned "+name);return 1;
    }
    private static void msg(CommandSourceStack src,String text){src.sendSuccess(()->Component.literal(text),false);}
}
