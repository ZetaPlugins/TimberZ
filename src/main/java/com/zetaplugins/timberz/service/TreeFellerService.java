package com.zetaplugins.timberz.service;

import com.zetaplugins.timberz.TimberZ;
import com.zetaplugins.timberz.handler.SaplingReplanter;
import com.zetaplugins.timberz.handler.TreeAnimationHandler;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

import static com.zetaplugins.timberz.service.MaterialTypeChecks.isLeafBlock;

public final class TreeFellerService {
    private final TimberZ plugin;
    private final TreeAnimationHandler animationHandler;
    private final SaplingReplanter saplingReplanter;
    private final Random random = new Random();

    public TreeFellerService(TimberZ plugin) {
        this.plugin = plugin;
        this.animationHandler = new TreeAnimationHandler(plugin);
        this.saplingReplanter = new SaplingReplanter(plugin);
    }

    /**
     * Handles the tree felling process
     */
    public void fellTree(Player player, Block sourceBlock, Set<Block> treeBlocks, ItemStack tool, int durabilityCost) {
        Material logType = sourceBlock.getType();

        // Get corresponding leaf type
        List<Material> leafTypes = plugin.getTreeDetectionService().getLeafTypes(logType);

        // Store information about tree type for replanting
        SaplingReplanter.TreeInfo treeInfo = saplingReplanter.analyzeTreeType(sourceBlock, treeBlocks);

        // Collect leaf blocks associated with the tree
        Set<Block> leafBlocks = collectLeaves(treeBlocks, leafTypes);

        // Break logs with animation (one by one with delay)
        breakTreeWithAnimation(player, treeBlocks, tool, durabilityCost);

        // Schedule leaf decay
        if (!leafBlocks.isEmpty() && plugin.getConfig().getBoolean("instantLeafDecay")) {
            scheduleLeafDecay(leafBlocks);
        }

        boolean shouldReplant = plugin.getConfig().getBoolean("replant");

        // Schedule sapling replanting
        if (treeInfo != null && shouldReplant) {
            saplingReplanter.scheduleSaplingReplant(treeInfo);
        }
    }


    /**
     * Calculate the Euclidean distance between two blocks
     */
    private double distanceBetween(Block block1, Block block2) {
        double dx = block1.getX() - block2.getX();
        double dy = block1.getY() - block2.getY();
        double dz = block1.getZ() - block2.getZ();
        return Math.sqrt(dx*dx + dy*dy + dz*dz);
    }

    /**
     * Collects leaf blocks that most likely belong to the tree being cut down
     */
    private Set<Block> collectLeaves(Set<Block> treeBlocks, List<Material> leafType) {
        Set<Block> leafBlocks = new HashSet<>();
        Set<Block> checkedBlocks = new HashSet<>();
        Set<Block> otherTreeLogs = new HashSet<>();

        // Search radius for leaves around each log
        final int SEARCH_RADIUS = plugin.getConfig().getInt("leavesSearchRadius", 4);

        // First pass: Collect all leaves and identify potential other tree logs
        for (Block log : treeBlocks) {
            // Check surrounding blocks for leaves and other logs
            for (int x = -SEARCH_RADIUS; x <= SEARCH_RADIUS; x++) {
                for (int y = -SEARCH_RADIUS; y <= SEARCH_RADIUS; y++) {
                    for (int z = -SEARCH_RADIUS; z <= SEARCH_RADIUS; z++) {
                        Block checkBlock = log.getRelative(x, y, z);

                        // Skip blocks we already checked
                        if (checkedBlocks.contains(checkBlock)) {
                            continue;
                        }

                        checkedBlocks.add(checkBlock);

                        if (leafType.contains(checkBlock.getType())) {
                            leafBlocks.add(checkBlock);
                        } else if (plugin.getTreeDetectionService().containsLog(checkBlock.getType()) && !treeBlocks.contains(checkBlock)) {
                            // This is a log block that's not part of our tree - potential other tree
                            otherTreeLogs.add(checkBlock);
                        }
                    }
                }
            }
        }

        // If we found logs from other trees, filter leaves that might belong to them
        if (!otherTreeLogs.isEmpty()) {
            // Second pass: Filter leaves that are closer to other tree logs than to our tree logs
            Set<Block> filteredLeaves = new HashSet<>();

            for (Block leaf : leafBlocks) {
                // Find distance to closest log in our tree
                double minDistToOurTree = Double.MAX_VALUE;
                for (Block ourLog : treeBlocks) {
                    double dist = distanceBetween(leaf, ourLog);
                    if (dist < minDistToOurTree) {
                        minDistToOurTree = dist;
                    }
                }

                // Find distance to closest log in other trees
                double minDistToOtherTree = Double.MAX_VALUE;
                for (Block otherLog : otherTreeLogs) {
                    double dist = distanceBetween(leaf, otherLog);
                    if (dist < minDistToOtherTree) {
                        minDistToOtherTree = dist;
                    }
                }

                // If the leaf is closer to our tree than other trees, keep it
                // Add a small bias factor to favor our tree (to handle edge cases)
                if (minDistToOurTree <= minDistToOtherTree * 1.2) {
                    filteredLeaves.add(leaf);
                }
            }

            return filteredLeaves;
        }

        // If no other tree logs found, return all leaf blocks
        return leafBlocks;
    }

    /**
     * Schedules leaf decay with a natural-looking pattern
     */
    private void scheduleLeafDecay(Set<Block> leafBlocks) {
        List<Block> sortedLeaves = new ArrayList<>(leafBlocks);

        // Sort leaves from bottom to top for more natural decay
        sortedLeaves.sort(Comparator.comparingInt(Block::getY));

        // Schedule decay with random delays for natural effect
        for (Block leaf : sortedLeaves) {
            // Only decay leaves that aren't supported by remaining logs
            new BukkitRunnable() {
                @Override
                public void run() {
                    // Check if the leaf still exists
                    // could also add  && !isConnectedToLog(leaf) to fix the issue of neighboring trees, but I shouldnt have to.
                    if (isLeafBlock(leaf.getType(), plugin.getConfigService().getBlocksConfig())) {
                        // Create breaking particles
                        leaf.getWorld().spawnParticle(
                                Particle.BLOCK,
                                leaf.getLocation().add(0.5, 0.5, 0.5),
                                5, 0.3, 0.3, 0.3, 0.05,
                                leaf.getBlockData());

                        // Play break sound with randomization
                        float pitch = 0.8f + (random.nextFloat() * 0.4f);
                        leaf.getWorld().playSound(
                                leaf.getLocation(),
                                Sound.BLOCK_GRASS_BREAK,
                                0.6f, pitch);

                        // Break the leaf naturally
                        leaf.breakNaturally();
                    }
                }
            }.runTaskLater(plugin, 5 + random.nextInt(40)); // Random delay between 5-45 ticks
        }
    }

    /**
     * Checks if a leaf block is still connected to a log block
     * We need this to make sure we don't decay leaves attached to other trees
     */
    private boolean isConnectedToLog(Block leafBlock) {
        // Check nearby blocks for logs (up to 4 blocks away)
        for (int x = -4; x <= 4; x++) {
            for (int y = -4; y <= 4; y++) {
                for (int z = -4; z <= 4; z++) {
                    // Skip checking too far away (Manhattan distance > 4)
                    if (Math.abs(x) + Math.abs(y) + Math.abs(z) > 4) {
                        continue;
                    }

                    Block nearbyBlock = leafBlock.getRelative(x, y, z);
                    Material blockType = nearbyBlock.getType();

                    // If we find any log block, the leaf is still supported
                    if (plugin.getTreeDetectionService().containsLog(blockType)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Breaks the tree logs with animation and applies durability to tool
     */
    private void breakTreeWithAnimation(Player player, Set<Block> treeBlocks, ItemStack tool, int durabilityCost) {
        // Apply durability damage to the tool
        applyToolDamage(tool, durabilityCost);

        // Break blocks with animation
        animationHandler.animateTreeFelling(player, treeBlocks);
    }

    /**
     * Apply damage to the tool
     */
    private void applyToolDamage(ItemStack tool, int durabilityCost) {
        if (tool.getItemMeta() instanceof Damageable) {
            Damageable meta = (Damageable) tool.getItemMeta();
            int newDamage = meta.getDamage() + durabilityCost;
            meta.setDamage(newDamage);
            tool.setItemMeta((ItemMeta) meta);
        }
    }
}