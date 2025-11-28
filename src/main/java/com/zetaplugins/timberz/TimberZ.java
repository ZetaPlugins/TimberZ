package com.zetaplugins.timberz;

import com.zetaplugins.timberz.commands.TimberZCommand;
import com.zetaplugins.timberz.dev.DevMode;
import com.zetaplugins.timberz.service.*;
import com.zetaplugins.timberz.service.auraskills.AuraSkillsManager;
import com.zetaplugins.zetacore.services.bStats.Metrics;
import com.zetaplugins.timberz.service.papi.PapiExpansion;
import com.zetaplugins.timberz.service.worldguard.WorldGuardManager;
import com.zetaplugins.zetacore.services.events.AutoEventRegistrar;
import com.zetaplugins.zetacore.services.updatechecker.ModrinthUpdateChecker;
import com.zetaplugins.zetacore.services.updatechecker.UpdateChecker;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.List;

public final class TimberZ extends JavaPlugin {
    private final static String PACKAGE_PREFIX = "com.zetaplugins.timberz";

    private PlayerStateService playerStateService;
    private MessageService messageService;
    private LocalizationService localizationService;
    private TreeFellerService treeFellerService;
    private TreeDetectionService treeDetectionService;
    private UpdateChecker updateChecker;
    private TzConfigService configService;
    private WorldGuardManager worldGuardManager;
    private AuraSkillsManager auraSkillsManager;
    private final boolean hasWorldGuard = Bukkit.getPluginManager().getPlugin("WorldGuard") != null;
    private final boolean hasAuraSkills = Bukkit.getPluginManager().getPlugin("AuraSkills") != null;

    private boolean devMode = false;

    @Override
    public void onLoad() {
        getLogger().info("Loading TimberZ...");

        if (Bukkit.getName().toLowerCase().contains("spigot") || Bukkit.getName().toLowerCase().contains("craftbukkit")) {
            getLogger().severe("---------------------------------------------------");
            getLogger().severe("TimberZ does not support Spigot or Bukkit!");
            getLogger().severe("Please use Paper or any fork of Paper (like Purpur). If you need further assistance, please join our Discord server:");
            getLogger().severe("https://strassburger.org/discord");
            getLogger().severe("---------------------------------------------------");
        }

        if (hasWorldGuard()) {
            getLogger().info("WorldGuard found! Enabling WorldGuard support...");
            worldGuardManager = new WorldGuardManager();
            getLogger().info("WorldGuard support enabled!");
        } else {
            getLogger().warning("WorldGuard not found! Disabling WorldGuard support.");
        }
    }

    @Override
    public void onEnable() {
        // Check if dev mode is enabled via JVM property
        String mode = System.getProperty("plugin.env", "release");
        devMode = mode.equalsIgnoreCase("dev");

        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        this.configService = new TzConfigService(this);

        this.localizationService = new LocalizationService(this);
        this.messageService = new MessageService(this);
        this.playerStateService = new PlayerStateService(this);
        this.treeFellerService = new TreeFellerService(this);
        this.treeDetectionService = new TreeDetectionService(this);

        this.updateChecker = new ModrinthUpdateChecker(this, "hjNMOOnF");
        updateChecker.checkForUpdates(true);

        List<String> registeredListeners = new AutoEventRegistrar(this, PACKAGE_PREFIX).registerAllListeners();
        getLogger().info("Registered " + registeredListeners.size() + " listeners.");
        registerCommands();

        initializeBStats();

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            getLogger().info("PlaceholderAPI found! Registering placeholders...");
            new PapiExpansion(this).register();
            getLogger().info("Placeholders registered successfully!");
        }

        if (hasAuraSkills) {
            getLogger().info("AuraSkills found! Enabling AuraSkills support...");
            this.auraSkillsManager = new AuraSkillsManager(this);
            getLogger().info("AuraSkills support enabled!");
        }

        if (devMode) {
            new DevMode(this).enable();
        }

        getLogger().info("TimberZ has been enabled!");
    }

    @Override
    public @NotNull Path getDataPath() {
        return super.getDataPath();
    }

    @Override
    public void onDisable() {
        if (playerStateService != null) playerStateService.cleanupAll();
        getLogger().info("TimberZ has been disabled!");
    }

    private void registerCommands() {
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register(new TimberZCommand(this).buildRootCommand("timberz"));
            commands.registrar().register(new TimberZCommand(this).buildRootCommand("tz"));
        });
    }

    private void initializeBStats() {
        int pluginId = 25743;
        Metrics metrics = new Metrics(this, pluginId);

        metrics.addCustomChart(new Metrics.SimplePie("language", () -> getConfig().getString("lang")));
    }

    public PlayerStateService getPlayerStateService() {
        return playerStateService;
    }

    public MessageService getMessageService() {
        return messageService;
    }

    public LocalizationService getLocalizationService() {
        return localizationService;
    }

    public TreeFellerService getTreeFellerService() {
        return treeFellerService;
    }

    public UpdateChecker getUpdateChecker() {
        return updateChecker;
    }

    public TreeDetectionService getTreeDetectionService() {
        return treeDetectionService;
    }

    public TzConfigService getConfigService() {
        return configService;
    }

    public WorldGuardManager getWorldGuardManager() {
        return worldGuardManager;
    }

    public boolean hasWorldGuard() {
        return hasWorldGuard;
    }

    public AuraSkillsManager getAuraSkillsManager() {
        return auraSkillsManager;
    }

    public boolean hasAuraSkills() {
        return hasAuraSkills;
    }
}
