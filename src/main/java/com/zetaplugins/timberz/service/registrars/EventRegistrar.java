package com.zetaplugins.timberz.service.registrars;

import com.zetaplugins.timberz.TimberZ;
import com.zetaplugins.timberz.listener.AxeEquipListener;
import com.zetaplugins.timberz.listener.LeafBreakListener;
import com.zetaplugins.timberz.listener.PlayerJoinListener;
import com.zetaplugins.timberz.listener.TreeBreakListener;
import org.bukkit.event.Listener;

public final class EventRegistrar {
    private final TimberZ plugin;

    public EventRegistrar(TimberZ plugin) {
        this.plugin = plugin;
    }

    /**
     * Registers all listeners
     */
    public void registerListeners() {
        registerListener(new TreeBreakListener(plugin));
        registerListener(new AxeEquipListener(plugin));
        registerListener(new PlayerJoinListener(plugin));
        registerListener(new LeafBreakListener(plugin));
    }

    /**
     * Registers a listener
     *
     * @param listener The listener to register
     */
    private void registerListener(Listener listener) {
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
    }
}
