package com.zetaplugins.timberz.handler;

import com.zetaplugins.timberz.TimberZ;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public final class TreeAnimationHandler {

    private final TimberZ plugin;
    private final Random random = new Random();

    public TreeAnimationHandler(TimberZ plugin) {
        this.plugin = plugin;
    }

    /**
     * Animates the tree felling process with advanced particles and effects
     */
    public void animateTreeFelling(Player player, Set<Block> treeBlocks) {
        // Convert set to list for ordered processing
        List<Block> orderedBlocks = new ArrayList<>(treeBlocks);

        // Sort blocks from bottom to top for more realistic felling
        orderedBlocks.sort(Comparator.comparingInt(Block::getY));

        // Calculate the tree's center position for directional effects
        Vector direction = player.getLocation().getDirection().normalize();

        // Play initial axe swing effect
        playAxeSwingEffect(player);

        // Schedule sequential block breaking with animation
        int delay = 0;
        int baseIncrement = 1; // Base ticks between breaking each block

        for (int i = 0; i < orderedBlocks.size(); i++) {
            Block block = orderedBlocks.get(i);

            // Dynamic timing - breaks faster as we go higher (tree falling acceleration)
            int dynamicIncrement = Math.max(1, baseIncrement - (i / 10));

            new BukkitRunnable() {
                @Override
                public void run() {
                    // Create breaking effect
                    createBreakEffect(player, block, direction);

                    // Break the block naturally to drop items
                    Material blockType = block.getType();
                    player.breakBlock(block);

                    // Random chance to spawn floating wood chips
                    if (random.nextInt(5) == 0) {
                        spawnWoodChips(block.getLocation().add(0.5, 0.5, 0.5), direction);
                    }
                }
            }.runTaskLater(plugin, delay);

            delay += dynamicIncrement;
        }

        // Play final crashing sound with delay
        new BukkitRunnable() {
            @Override
            public void run() {
                World world = player.getWorld();
                Location lastBlockLoc = orderedBlocks.isEmpty() ? player.getLocation() :
                        orderedBlocks.get(orderedBlocks.size()-1).getLocation();

                world.playSound(lastBlockLoc, Sound.BLOCK_WOOD_FALL, 1.0f, 0.8f);

                // Final dust cloud effect
                world.spawnParticle(Particle.CLOUD, lastBlockLoc.add(0.5, 0.5, 0.5),
                        20, 1.5, 1.0, 1.5, 0.05);
            }
        }.runTaskLater(plugin, delay + 5);
    }

    /**
     * Creates a more dramatic break effect for each block
     */
    private void createBreakEffect(Player player, Block block, Vector direction) {
        World world = block.getWorld();
        Location loc = block.getLocation().add(0.5, 0.5, 0.5);

        // Main break particles
        world.spawnParticle(
                Particle.BLOCK,
                loc,
                15, 0.4, 0.4, 0.4, 0.1,
                block.getBlockData());

        // Add directional velocity to particles (wood chips flying away from player)
        Vector particleDir = direction.clone().multiply(0.15);

        // Wood dust
        world.spawnParticle(
                Particle.SMOKE,
                loc,
                8, 0.3, 0.3, 0.3, 0.02);

        // Leaf particles for leaves blocks
        if (block.getType().name().contains("LEAVES")) {
            world.spawnParticle(
                    Particle.COMPOSTER,
                    loc,
                    10, 0.5, 0.5, 0.5, 0.1);
        }

        // Dynamic sound effects
        float pitch = 0.8f + random.nextFloat() * 0.4f; // Randomized pitch
        world.playSound(
                loc,
                Sound.BLOCK_WOOD_BREAK,
                0.8f, pitch);

        // Add occasional cracking sounds
        if (random.nextInt(4) == 0) {
            world.playSound(
                    loc,
                    Sound.BLOCK_WOOD_HIT,
                    0.5f, 0.6f);
        }
    }

    /**
     * Creates small wood chips that fly out from the tree
     */
    private void spawnWoodChips(Location location, Vector direction) {
        World world = location.getWorld();

        // Create small brown dust particles flying in the direction of fall
        Vector chipVelocity = direction.clone()
                .add(new Vector(
                        random.nextFloat() * 0.4 - 0.2,
                        random.nextFloat() * 0.2,
                        random.nextFloat() * 0.4 - 0.2))
                .multiply(0.3);

        // Wood chip particles
        for (int i = 0; i < 3; i++) {
            Vector randomOffset = new Vector(
                    random.nextDouble() * 0.4 - 0.2,
                    random.nextDouble() * 0.4 - 0.2,
                    random.nextDouble() * 0.4 - 0.2
            );

            Location chipLoc = location.clone().add(randomOffset);

            world.spawnParticle(
                    Particle.DUST,
                    chipLoc,
                    2,
                    0.1, 0.1, 0.1,
                    0,
                    new Particle.DustOptions(Color.fromRGB(139, 69, 19), 1.0f));
        }
    }

    /**
     * Play an effect when the player swings their axe
     */
    private void playAxeSwingEffect(Player player) {
        World world = player.getWorld();
        Location handLoc = player.getEyeLocation().add(
                player.getLocation().getDirection().multiply(1.0));

        // Swing particles
        world.spawnParticle(
                Particle.SWEEP_ATTACK,
                handLoc,
                1, 0.0, 0.0, 0.0, 0.0);

        // Swing sound
        world.playSound(
                player.getLocation(),
                Sound.ENTITY_PLAYER_ATTACK_SWEEP,
                0.8f, 1.2f);

        // Additional "powerful swing" effect
        world.spawnParticle(
                Particle.CRIT,
                handLoc,
                10, 0.3, 0.3, 0.3, 0.2);
    }
}
