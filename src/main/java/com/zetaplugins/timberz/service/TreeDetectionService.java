package com.zetaplugins.timberz.service;

import com.zetaplugins.timberz.TimberZ;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.*;

public final class TreeDetectionService {
    private final TimberZ plugin;

    private final int MAX_TREE_SIZE;
    private final int MAX_SEARCH_RADIUS;
    private final int DIAGONAL_SEARCH_RANGE;
    private final int MIN_LEAVES_REQUIRED;
    private final int MIN_LOGS_REQUIRED;

    // Maps log types to their corresponding leaf types
    private final Map<Material, List<Material>> LOG_TO_LEAF_MAP = new HashMap<>();

    public TreeDetectionService(TimberZ plugin) {
        this.plugin = plugin;

        MAX_TREE_SIZE = plugin.getConfig().getInt("maxTreeSize", 1000);
        MAX_SEARCH_RADIUS = plugin.getConfig().getInt("maxSearchRadius", 1);
        DIAGONAL_SEARCH_RANGE = plugin.getConfig().getInt("diagonalSearchRange", 2);
        MIN_LEAVES_REQUIRED = plugin.getConfig().getInt("minLeavesRequired", 5);
        MIN_LOGS_REQUIRED = plugin.getConfig().getInt("minLogsRequired", 3);

        fetchLogToLeaveMap();
    }

    /**
     * Fetches the log-to-leaf mapping from the configuration file.
     */
    public void fetchLogToLeaveMap() {
        LOG_TO_LEAF_MAP.clear();

        List<String> logToLeaveList = plugin.getConfigService().getBlocksConfig().getStringList("logToLeafMap");

        for (String entry : logToLeaveList) {
            String[] parts = entry.split(":");
            if (parts.length != 2) continue;

            Material logType = Material.getMaterial(parts[0].toUpperCase());
            Material leafType = Material.getMaterial(parts[1].toUpperCase());
            if (logType != null && leafType != null) {
                LOG_TO_LEAF_MAP.computeIfAbsent(logType, k -> new ArrayList<>()).add(leafType);
            }
        }
    }

    /**
     * Core algorithm to identify a complete tree structure from a source log block.
     *
     * @param sourceBlock The log block that was broken by a player
     * @return A set of all blocks that make up the tree, or empty set if not a valid tree
     */
    public Set<Block> identifyTreeStructure(Block sourceBlock) {
        Material sourceType = sourceBlock.getType();
        List<Material> matchingLeafTypes = LOG_TO_LEAF_MAP.get(sourceType);

        if (matchingLeafTypes == null) {
            return Collections.emptySet(); // Not a valid log type
        }

        // Initial validation: Check if there's a log above the source
        // Skip fallen trees or single horizontal logs
        Block blockAbove = sourceBlock.getRelative(BlockFace.UP);
        if (!isMatchingLog(blockAbove, sourceType)) {
            Block diagonallyAbove1 = sourceBlock.getRelative(1, 1, 0);
            Block diagonallyAbove2 = sourceBlock.getRelative(-1, 1, 0);
            Block diagonallyAbove3 = sourceBlock.getRelative(0, 1, 1);
            Block diagonallyAbove4 = sourceBlock.getRelative(0, 1, -1);

            // Check if there are any diagonal logs going upward
            boolean hasDiagonalUpward = isMatchingLog(diagonallyAbove1, sourceType) ||
                    isMatchingLog(diagonallyAbove2, sourceType) ||
                    isMatchingLog(diagonallyAbove3, sourceType) ||
                    isMatchingLog(diagonallyAbove4, sourceType);

            if (!hasDiagonalUpward) {
                return Collections.emptySet(); // No vertical component, not a tree
            }
        }

        // Track visited blocks to prevent infinite loops
        Set<Block> visited = new HashSet<>();

        // Use BFS to find all connected logs
        Set<Block> connectedLogs = findConnectedLogs(sourceBlock, sourceType, visited);

        // Validate tree structure
        if (connectedLogs.size() < MIN_LOGS_REQUIRED) {
            return Collections.emptySet(); // Too small to be a tree
        }

        // Verify the existence of matching leaf blocks
        boolean hasLeaves = validateLeaves(connectedLogs, matchingLeafTypes);
        if (!hasLeaves) {
            return Collections.emptySet(); // No leaves found, not a tree
        }

        return connectedLogs;
    }

    /**
     * Find all connected logs of the same type, including diagonal connections.
     */
    private Set<Block> findConnectedLogs(Block sourceBlock, Material logType, Set<Block> visited) {
        Queue<Block> queue = new LinkedList<>();
        Set<Block> connectedLogs = new HashSet<>();

        queue.add(sourceBlock);
        visited.add(sourceBlock);
        connectedLogs.add(sourceBlock);

        while (!queue.isEmpty() && connectedLogs.size() < MAX_TREE_SIZE) {
            Block current = queue.poll();

            // Check direct adjacents (6 directions)
            for (BlockFace face : Arrays.asList(
                    BlockFace.UP, BlockFace.DOWN,
                    BlockFace.NORTH, BlockFace.SOUTH,
                    BlockFace.EAST, BlockFace.WEST)) {

                Block adjacent = current.getRelative(face);
                if (!visited.contains(adjacent) && isMatchingLog(adjacent, logType)) {
                    queue.add(adjacent);
                    visited.add(adjacent);
                    connectedLogs.add(adjacent);
                }
            }

            // Check diagonal connections within range
            for (int x = -DIAGONAL_SEARCH_RANGE; x <= DIAGONAL_SEARCH_RANGE; x++) {
                for (int y = -DIAGONAL_SEARCH_RANGE; y <= DIAGONAL_SEARCH_RANGE; y++) {
                    for (int z = -DIAGONAL_SEARCH_RANGE; z <= DIAGONAL_SEARCH_RANGE; z++) {
                        // Skip non-diagonal positions and the center
                        if ((x == 0 && y == 0 && z == 0) ||
                                (Math.abs(x) + Math.abs(y) + Math.abs(z) <= 1)) {
                            continue;
                        }

                        Block diagonal = current.getRelative(x, y, z);

                        if (!visited.contains(diagonal) && isMatchingLog(diagonal, logType)) {
                            // Extra validation for diagonal connections
                            if (isDiagonalConnectionValid(current, diagonal, logType, connectedLogs)) {
                                queue.add(diagonal);
                                visited.add(diagonal);
                                connectedLogs.add(diagonal);
                            }
                        }
                    }
                }
            }
        }

        return connectedLogs;
    }

    /**
     * Additional validation for diagonal connections to avoid false positives.
     */
    private boolean isDiagonalConnectionValid(Block block1, Block block2, Material logType, Set<Block> knownTreeLogs) {
        // Calculate the distance between blocks
        int xDiff = Math.abs(block1.getX() - block2.getX());
        int yDiff = Math.abs(block1.getY() - block2.getY());
        int zDiff = Math.abs(block1.getZ() - block2.getZ());

        // Check if this is a valid diagonal (maximum 2 blocks apart in any dimension)
        if (xDiff > DIAGONAL_SEARCH_RANGE || yDiff > DIAGONAL_SEARCH_RANGE || zDiff > DIAGONAL_SEARCH_RANGE) {
            return false;
        }

        // prefer upward diagonals and check if there are any other tree logs nearby that could connect these diagonals
        boolean isUpwardDiagonal = block2.getY() > block1.getY();


        int bridgeLogsFound = 0;

        for (int x = Math.min(block1.getX(), block2.getX()); x <= Math.max(block1.getX(), block2.getX()); x++) {
            for (int y = Math.min(block1.getY(), block2.getY()); y <= Math.max(block1.getY(), block2.getY()); y++) {
                for (int z = Math.min(block1.getZ(), block2.getZ()); z <= Math.max(block1.getZ(), block2.getZ()); z++) {
                    Block between = block1.getWorld().getBlockAt(x, y, z);
                    if (between.equals(block1) || between.equals(block2)) {
                        continue;
                    }

                    if (knownTreeLogs.contains(between) || isMatchingLog(between, logType)) {
                        bridgeLogsFound++;
                    }
                }
            }
        }

        // Apply different validation rules based on the diagonal type and bridge logs
        if (isUpwardDiagonal) {
            return true;
        } else if (bridgeLogsFound > 0) {
            return true;
        } else {
            return xDiff + yDiff + zDiff <= 3;
        }
    }

    /**
     * Validate that the connected logs have appropriate leaf blocks nearby.
     */
    private boolean validateLeaves(Set<Block> connectedLogs, List<Material> leafTypes) {
        int leafCount = 0;
        Set<Block> checkedBlocks = new HashSet<>();

        for (Block log : connectedLogs) {
            for (int x = -MAX_SEARCH_RADIUS; x <= MAX_SEARCH_RADIUS; x++) {
                for (int y = -MAX_SEARCH_RADIUS; y <= MAX_SEARCH_RADIUS; y++) {
                    for (int z = -MAX_SEARCH_RADIUS; z <= MAX_SEARCH_RADIUS; z++) {
                        // Skip blocks we already checked
                        Block checkBlock = log.getRelative(x, y, z);
                        if (checkedBlocks.contains(checkBlock)) continue;

                        checkedBlocks.add(checkBlock);

                        if (leafTypes.contains(checkBlock.getType())) {
                            leafCount++;

                            if (leafCount >= MIN_LEAVES_REQUIRED) return true;
                        }
                    }
                }
            }
        }

        return leafCount >= MIN_LEAVES_REQUIRED;
    }

    /**
     * Check if a block is a log of the specified type.
     */
    private boolean isMatchingLog(Block block, Material logType) {
        return block.getType() == logType;
    }

    public boolean containsLog(Material blockType){
        return LOG_TO_LEAF_MAP.containsKey(blockType);
    }

    /**
     * Get the corresponding leaf type for a log type
     */
    public List<Material> getLeafTypes(Material logType) {
        return LOG_TO_LEAF_MAP.get(logType);
    }
}