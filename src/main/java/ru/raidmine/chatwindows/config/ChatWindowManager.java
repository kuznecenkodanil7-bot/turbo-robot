package ru.raidmine.chatwindows.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ChatWindowManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final MinecraftClient client;
    private final Path configPath;
    private ChatWindowsConfig config = new ChatWindowsConfig();
    private final List<ChatWindowRuntime> runtimes = new ArrayList<>();

    public ChatWindowManager(MinecraftClient client) {
        this.client = client;
        this.configPath = client.runDirectory.toPath().resolve("config").resolve("chatwindows.json");
    }

    public void load() {
        try {
            if (Files.exists(configPath)) {
                try (Reader reader = Files.newBufferedReader(configPath)) {
                    ChatWindowsConfig loaded = GSON.fromJson(reader, ChatWindowsConfig.class);
                    if (loaded != null) {
                        config = loaded;
                    }
                }
            } else {
                createDefaultConfig();
                save();
            }
        } catch (Exception ignored) {
            createDefaultConfig();
        }
        rebuildRuntimes();
    }

    public void save() {
        try {
            Files.createDirectories(configPath.getParent());
            try (Writer writer = Files.newBufferedWriter(configPath)) {
                GSON.toJson(config, writer);
            }
        } catch (IOException ignored) {
        }
    }

    private void createDefaultConfig() {
        config = new ChatWindowsConfig();

        ChatWindowConfig moderation = new ChatWindowConfig();
        moderation.title = "Модерация";
        moderation.x = 8;
        moderation.y = 45;
        moderation.width = 300;
        moderation.height = 120;
        moderation.keywords.add("чит");
        moderation.keywords.add("репорт");
        moderation.keywords.add("жалоба");
        moderation.keywords.add("админ");

        ChatWindowConfig mentions = new ChatWindowConfig();
        mentions.title = "Упоминания";
        mentions.x = 8;
        mentions.y = 175;
        mentions.width = 300;
        mentions.height = 100;
        mentions.keywords.add("@ник");
        mentions.keywords.add("ник");

        config.windows.add(moderation);
        config.windows.add(mentions);
    }

    public boolean routeIncomingMessage(Text text) {
        if (text == null) {
            return false;
        }

        FilteredChatMessage message = new FilteredChatMessage(text);
        boolean hideFromMain = false;
        for (ChatWindowRuntime runtime : runtimes) {
            ChatWindowConfig window = runtime.config();
            if (window.matches(message.plainText())) {
                runtime.addMessage(message);
                if (window.hideMatchesInMainChat) {
                    hideFromMain = true;
                }
            }
        }
        return hideFromMain;
    }

    public List<ChatWindowRuntime> windows() {
        return Collections.unmodifiableList(runtimes);
    }

    public ChatWindowConfig findConfigById(String id) {
        if (id == null) {
            return null;
        }
        for (ChatWindowConfig window : config.windows) {
            if (id.equals(window.id)) {
                return window;
            }
        }
        return null;
    }

    public ChatWindowConfig windowAt(double mouseX, double mouseY) {
        for (int i = config.windows.size() - 1; i >= 0; i--) {
            ChatWindowConfig window = config.windows.get(i);
            if (window.isInside(mouseX, mouseY)) {
                return window;
            }
        }
        return null;
    }

    public ChatWindowConfig createWindow() {
        ChatWindowConfig window = new ChatWindowConfig();
        window.title = "Окно " + (config.windows.size() + 1);
        window.x = 25 + config.windows.size() * 16;
        window.y = 55 + config.windows.size() * 16;
        window.width = 280;
        window.height = 115;
        window.keywords.add("слово");
        config.windows.add(window);
        rebuildRuntimes();
        save();
        return window;
    }

    public void deleteWindow(String id) {
        config.windows.removeIf(window -> window.id.equals(id));
        rebuildRuntimes();
        save();
    }

    public void rebuildRuntimes() {
        runtimes.clear();
        if (config.windows == null) {
            config.windows = new ArrayList<>();
        }
        for (ChatWindowConfig window : config.windows) {
            if (window.id == null || window.id.isBlank()) {
                window.id = java.util.UUID.randomUUID().toString();
            }
            if (window.keywords == null) {
                window.keywords = new ArrayList<>();
            }
            runtimes.add(new ChatWindowRuntime(window));
        }
    }

    public Path configPath() {
        return configPath;
    }
}
