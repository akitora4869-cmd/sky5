package jp.skycombat;

import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public class UniqueRoster extends SavedData {
    private final CompoundTag entries = new CompoundTag();
    public static UniqueRoster load(CompoundTag nbt){UniqueRoster r=new UniqueRoster();if(nbt.contains("Entries",10))r.entries.merge(nbt.getCompound("Entries"));return r;}
    @Override public CompoundTag save(CompoundTag nbt){nbt.put("Entries",entries.copy());return nbt;}
    public static UniqueRoster get(ServerLevel world){return world.getServer().overworld().getDataStorage().computeIfAbsent(UniqueRoster::load,UniqueRoster::new,"skycombat_unique_npcs");}
    public boolean registered(String id){return entries.hasUUID(id);}
    public UUID uuid(String id){return entries.getUUID(id);}
    public void register(String id,UUID uuid){entries.putUUID(id,uuid);setDirty();}
    public void forget(String id){entries.remove(id);setDirty();}
}
