package jp.skycombat;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SkyCombat.ID)
public final class HudSync {
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)
                || player.tickCount % 10 != 0) return;
        RpgStats.apply(player);
        List<HudNetwork.Entry> entries = new ArrayList<>();
        entries.add(HudNetwork.Entry.of("player", player));
        // Party ownership is authoritative on the server. Roaming NPCs are not included.
        for (var level : player.server.getAllLevels()) {
            for (var entity : level.getAllEntities()) {
                if (entity instanceof Companion npc && npc.isAlive()
                        && player.getUUID().equals(npc.partyOwner())) {
                    RpgStats.apply(npc);
                    entries.add(HudNetwork.Entry.of(npc.character(), npc));
                }
            }
        }
        HudNetwork.send(player, entries);
    }
}
