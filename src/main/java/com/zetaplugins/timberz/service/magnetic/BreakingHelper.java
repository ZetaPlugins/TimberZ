package com.zetaplugins.timberz.service.magnetic;

import org.apache.commons.lang3.mutable.MutableInt;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class BreakingHelper {

    public static void breakBlock(Block block, Player player, ItemStack tool, boolean shouldDamage) {
        // If magnetic is not present, simply break naturally
        if (!Bukkit.getPluginManager().isPluginEnabled("magnetic")) {
            block.breakNaturally(tool);
            return;
        }

        // Create magnetic drop event
        ArrayList<ItemStack> drops = new ArrayList<>(block.getDrops(tool, player));
        dev.nyon.magnetic.DropEvent dropEvent = new dev.nyon.magnetic.DropEvent(drops, new MutableInt(0), player, block.getLocation());
        dropEvent.callEvent();

        // Drop the remaining items that were not handled by magnetic
        List<ItemStack> remainingItems = dropEvent.getItems();
        World world = block.getWorld();
        Location location = block.getLocation();
        remainingItems.forEach(stack -> world.dropItem(location, stack));

        // Play effects on break
        world.playSound(location.toCenterLocation(), block.getBlockSoundGroup().getBreakSound(), 1f, 1f);

        // Actually destroy block
        block.setType(Material.AIR);

        // Damage tool if tool is damageable
        if (!tool.isEmpty() && tool.getType().getMaxDurability() > 0 && shouldDamage) tool.damage(1, player);
    }
}
