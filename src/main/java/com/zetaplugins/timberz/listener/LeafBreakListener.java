package com.zetaplugins.timberz.listener;

import com.zetaplugins.timberz.TimberZ;
import com.zetaplugins.timberz.service.MaterialTypeChecks;
import com.zetaplugins.timberz.service.PlayerStateService;
import com.zetaplugins.zetacore.annotations.AutoRegisterListener;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

@AutoRegisterListener
public class LeafBreakListener implements Listener {
    private final TimberZ plugin;
    private final PlayerStateService playerStateService;

    public LeafBreakListener(TimberZ plugin) {
        this.plugin = plugin;
        this.playerStateService = plugin.getPlayerStateService();
    }

    @EventHandler
    public void onBlockBreak(BlockDamageEvent event) {
        Block brokenBlock = event.getBlock();
        Material blockType = brokenBlock.getType();
        Player player = event.getPlayer();

        if (!plugin.getConfig().getBoolean("instaBreakLeavesWithTimber", false)) return;

        List<Material> leafTypes = getLeafTypes();
        if (!leafTypes.contains(blockType)) return;

        if (!playerStateService.isAllowedToTimber(player)) return;

        ItemStack tool = player.getInventory().getItemInMainHand();
        if (tool.getType() == Material.AIR || !MaterialTypeChecks.isValidAxe(tool, plugin)) return;

        brokenBlock.breakNaturally(tool);
        player.playSound(player.getLocation(), Sound.BLOCK_GRASS_BREAK, 1.0f, 1.0f);
    }

    private List<Material> getLeafTypes() {
        List<String> leafTypes = plugin.getConfigService().getBlocksConfig().getStringList("leafBlocks");
        List<Material> leafMaterials = new ArrayList<>();

        for (String leafType : leafTypes) {
            try {
                Material material = Material.getMaterial(leafType);
                if (material != null) leafMaterials.add(material);
                else plugin.getLogger().warning("Invalid leaf block type in config: " + leafType);
            } catch (IllegalArgumentException ignored) {}
        }

        return leafMaterials;
    }
}
