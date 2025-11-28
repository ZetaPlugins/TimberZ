package com.zetaplugins.timberz.service;

import com.zetaplugins.timberz.TimberZ;
import com.zetaplugins.zetacore.services.config.ConfigService;
import org.bukkit.configuration.file.FileConfiguration;

public final class TzConfigService extends ConfigService {
    public TzConfigService(TimberZ plugin) {
        super(plugin);
    }

    public FileConfiguration getBlocksConfig() {
        return getConfig("blocks");
    }
}
