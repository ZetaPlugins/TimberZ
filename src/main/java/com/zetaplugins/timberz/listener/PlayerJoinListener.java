package com.zetaplugins.timberz.listener;

import com.zetaplugins.timberz.TimberZ;
import com.zetaplugins.zetacore.annotations.AutoRegisterListener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

@AutoRegisterListener
public final class PlayerJoinListener implements Listener {
    private final TimberZ plugin;

    public PlayerJoinListener(TimberZ plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (player.isOp() && plugin.getConfig().getBoolean("checkForUpdates") && plugin.getUpdateChecker().isNewVersionAvailable()) {
            player.sendMessage(plugin.getMessageService().getAndFormatMsg(
                    true,
                    "newVersionAvailable",
                    "&7A new version of TimberZ is available!\\n&c<click:OPEN_URL:https://modrinth.com/plugin/timberz/versions>https://modrinth.com/plugin/timberz/versions</click>"
            ));
        }
    }
}
