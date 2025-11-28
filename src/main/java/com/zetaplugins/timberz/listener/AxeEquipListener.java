package com.zetaplugins.timberz.listener;

import com.zetaplugins.timberz.TimberZ;
import com.zetaplugins.timberz.service.PlayerStateService;
import com.zetaplugins.zetacore.annotations.AutoRegisterListener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;

import static com.zetaplugins.timberz.service.MaterialTypeChecks.isAxe;
import static com.zetaplugins.timberz.service.MaterialTypeChecks.isValidAxe;

@AutoRegisterListener
public final class AxeEquipListener implements Listener {
    private final TimberZ plugin;
    private final PlayerStateService playerStateService;
    private final boolean toggleTimber;
    private final boolean requireAxeMaterial;

    public AxeEquipListener(TimberZ plugin) {
        this.plugin = plugin;
        this.playerStateService = plugin.getPlayerStateService();
        this.toggleTimber = plugin.getConfig().getBoolean("toggleTimber");
        this.requireAxeMaterial = plugin.getConfig().getBoolean("requireAxeMaterial");
    }

    @EventHandler
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {
        if (!requireAxeMaterial) return;

        Player player = event.getPlayer();
        if (player.isSneaking()) return;

        if (!player.hasPermission("timberz.useTimber") && toggleTimber) return;

        if (!toggleTimber) return;

        ItemStack main = event.getMainHandItem();
        ItemStack off = event.getOffHandItem();

        if (isValidAxe(main, plugin) || isValidAxe(off, plugin)) {
            event.setCancelled(true);
            playerStateService.toggleTimberState(player);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player) || !toggleTimber || !requireAxeMaterial) return;

        ItemStack cursor = event.getCursor();

        if (event.getSlot() == 40 && isAxeFromItemStack(cursor)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player) || !toggleTimber || !requireAxeMaterial) return;

        ItemStack dragged = event.getOldCursor();
        if (!isAxeFromItemStack(dragged)) return;

        if (event.getRawSlots().contains(40)) {
            event.setCancelled(true);
            player.sendMessage(plugin.getMessageService().getAndFormatMsg(
                    false,
                    "cannotDragAxesIntoOffHand",
                    "&cYou cannot drag axes into your offhand!"
            ));
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        playerStateService.cleanupPlayer(player);
    }

    private boolean isAxeFromItemStack(ItemStack item) {
        return isAxe(item.getType(), plugin.getConfigService().getBlocksConfig());
    }
}