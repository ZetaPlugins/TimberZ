package com.zetaplugins.timberz.bootstrap;

import com.zetaplugins.timberz.TimberZ;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.bootstrap.PluginProviderContext;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.data.EnchantmentRegistryEntry;
import io.papermc.paper.registry.event.RegistryEvents;
import io.papermc.paper.registry.keys.EnchantmentKeys;
import io.papermc.paper.registry.keys.tags.EnchantmentTagKeys;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import io.papermc.paper.tag.PreFlattenTagRegistrar;
import io.papermc.paper.tag.TagEntry;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class TimberZBootstrap implements PluginBootstrap {
    @Override
    public void bootstrap(BootstrapContext context) {
        var timberEnchantKey = EnchantmentKeys.create(Key.key("timberz:timber"));

        context.getLifecycleManager().registerEventHandler(RegistryEvents.ENCHANTMENT.compose().newHandler(event -> {
            event.registry().register(
                    timberEnchantKey,
                    b -> {
                        b
                                .maxLevel(1)
                                .weight(2)
                                .anvilCost(30)
                                .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(13, 1))
                                .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(50, 1))
                                .activeSlots(EquipmentSlotGroup.ANY)
                                .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.AXES))
                                .primaryItems(event.getOrCreateTag(ItemTypeTagKeys.AXES))
                                .description(Component.text("Timber"));
                    }
            );
        }));

        // add to #minecraft:in_enchanting_table tag
        context.getLifecycleManager().registerEventHandler(
                LifecycleEvents.TAGS.preFlatten(RegistryKey.ENCHANTMENT).newHandler(event -> {
                    PreFlattenTagRegistrar<Enchantment> registrar = event.registrar();
                    registrar.addToTag(
                            EnchantmentTagKeys.IN_ENCHANTING_TABLE,
                            List.of(TagEntry.valueEntry(
                                    timberEnchantKey
                            ))
                    );
                    // add to #minecraft:tradeable tag for villager trades
                    registrar.addToTag(
                            EnchantmentTagKeys.TRADEABLE,
                            List.of(TagEntry.valueEntry(
                                    timberEnchantKey
                            ))
                    );
                })
        );
    }

    @Override
    public JavaPlugin createPlugin(PluginProviderContext context) {
        return new TimberZ();
    }
}
