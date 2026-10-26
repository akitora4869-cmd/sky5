package jp.skycombat;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class HudNetwork {
    private static final String VERSION = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(SkyCombat.ID, "hud"), () -> VERSION, VERSION::equals, VERSION::equals);
    private HudNetwork() {}
    public record Entry(String id, int hp, int maxHp, int ep, int maxEp, int cp, int level) {
        public static Entry of(String id, LivingEntity entity) {
            return new Entry(id, Math.max(0, Math.round(entity.getHealth())),
                Math.max(1, Math.round(entity.getMaxHealth())), Progress.ep(entity), RpgStats.of(entity).ep(),
                Progress.cp(entity), Progress.level(entity));
        }
    }
    public record Update(List<Entry> entries) {}
    public static void register() {
        CHANNEL.registerMessage(0, Update.class, HudNetwork::encode, HudNetwork::decode,
            HudNetwork::handle, java.util.Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }
    private static void encode(Update msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.entries().size());
        for (Entry e : msg.entries()) {
            buf.writeUtf(e.id()); buf.writeVarInt(e.hp()); buf.writeVarInt(e.maxHp());
            buf.writeVarInt(e.ep()); buf.writeVarInt(e.maxEp()); buf.writeVarInt(e.cp()); buf.writeVarInt(e.level());
        }
    }
    private static Update decode(FriendlyByteBuf buf) {
        int count = Math.min(16, buf.readVarInt());
        List<Entry> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(new Entry(buf.readUtf(64), buf.readVarInt(), buf.readVarInt(),
                buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt()));
        }
        return new Update(list);
    }
    private static void handle(Update msg, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> ClientHud.accept(msg));
        ctx.setPacketHandled(true);
    }
    public static void send(ServerPlayer player, List<Entry> entries) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new Update(entries));
    }
}
