package com.zetaplugins.timberz.service;

import com.zetaplugins.timberz.TimberZ;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class MaterialTypeChecks {
    private MaterialTypeChecks() {}

    /**
     * Checks if a material is a leaf block based on the configuration
     * @param material The material to check
     * @param blocksConfig The blocks configuration
     * @return True if the material is a leaf block, false otherwise
     */
    public static boolean isLeafBlock(Material material, FileConfiguration blocksConfig) {
        List<String> leafBlocks = blocksConfig.getStringList("logToLeafMap").stream()
                .filter(entry ->entry.contains(":"))
                .map(entry -> entry.split(":")[1])
                .filter(leaf -> !leaf.isEmpty())
                .toList();

        for (String leafBlock : leafBlocks) {
            if (material.toString().equalsIgnoreCase(leafBlock)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isAxe(Material material, FileConfiguration blocksConfig) {
        List<String> axeBlocks = blocksConfig.getStringList("axes");

        for (String axeBlock : axeBlocks) {
            if (material.toString().equalsIgnoreCase(axeBlock)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isValidAxe(ItemStack item, TimberZ plugin) {
        boolean isAxe = isAxe(item.getType(), plugin.getConfigService().getBlocksConfig());
        boolean requireAxeMaterial = plugin.getConfig().getBoolean("requireAxeMaterial");
        if (item == null || (!isAxe && requireAxeMaterial)) return false;

        if (plugin.getConfig().getBoolean("restrictAxeModelData")) {
            if (!item.getItemMeta().hasCustomModelData()) return false;
            int itemCustomModelData = item.getItemMeta().getCustomModelData();
            List<Integer> allowedModelData = plugin.getConfig().getIntegerList("allowedModelData");
            if (!allowedModelData.contains(itemCustomModelData)) return false;
        }

        if (plugin.getConfig().getBoolean("requireCustomEnchant")) {
            Map<Enchantment, Integer> enchants = item.getEnchantments();
            Set<String> enchantKeys = enchants.keySet().stream()
                .map(Enchantment::getKey)
                .map(Object::toString)
                .collect(Collectors.toSet());

            if (!enchantKeys.contains("timberz:timber")) return false;
        }

        return true;
    }
}
