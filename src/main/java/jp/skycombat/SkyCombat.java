package jp.skycombat;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;

@Mod(SkyCombat.ID)
public class SkyCombat {
    public static final String ID = "skycombat";
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, ID);
    public static final RegistryObject<EntityType<Companion>> COMPANION = ENTITIES.register("companion", () -> EntityType.Builder.<Companion>of(Companion::new, MobCategory.CREATURE).sized(0.6f,1.95f).clientTrackingRange(8).build("skycombat:companion"));
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ID);
    public static final RegistryObject<Item> ESTELLE_STAFF = ITEMS.register("estelle_staff", () -> new SwordItem(Tiers.IRON, 3, -2.4f, new Item.Properties()));
    public SkyCombat() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ENTITIES.register(bus);
        ITEMS.register(bus);
        HudNetwork.register();
    }
}
