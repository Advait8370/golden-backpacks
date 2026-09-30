package com.goldenbackpacks.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import net.neoforged.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ConfigManager {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_FILE = "goldenbackpacks.json";

    private ConfigManager() {
    }

    public static GoldenBackpacksConfig load() {
        Path configPath = FMLPaths.CONFIGDIR.get().resolve(CONFIG_FILE);
        if (Files.exists(configPath)) {
            try (BufferedReader reader = Files.newBufferedReader(configPath)) {
                GoldenBackpacksConfig config = GSON.fromJson(reader, GoldenBackpacksConfig.class);
                if (config != null) {
                    return config;
                }
            } catch (IOException | JsonSyntaxException e) {
                LOGGER.warn("Failed to read goldenbackpacks config, regenerating defaults.", e);
            }
        }

        GoldenBackpacksConfig defaultConfig = new GoldenBackpacksConfig();
        save(configPath, defaultConfig);
        return defaultConfig;
    }

    private static void save(Path path, GoldenBackpacksConfig config) {
        try {
            Files.createDirectories(path.getParent());
            try (BufferedWriter writer = Files.newBufferedWriter(path)) {
                GSON.toJson(config, writer);
            }
        } catch (IOException e) {
            LOGGER.warn("Failed to save goldenbackpacks config.", e);
        }
    }
}
