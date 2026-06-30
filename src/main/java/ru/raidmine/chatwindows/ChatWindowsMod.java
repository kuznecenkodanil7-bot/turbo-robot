package ru.raidmine.chatwindows;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import ru.raidmine.chatwindows.config.ChatWindowManager;
import ru.raidmine.chatwindows.gui.ChatWindowEditorScreen;

public final class ChatWindowsMod implements ClientModInitializer {
    public static final String MOD_ID = "chatwindows";
    private static ChatWindowManager manager;
    private static KeyBinding openEditorKey;

    public static ChatWindowManager manager() {
        return manager;
    }

    @Override
    public void onInitializeClient() {
        manager = new ChatWindowManager(MinecraftClient.getInstance());
        manager.load();

        openEditorKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.chatwindows.open_editor",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.chatwindows"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openEditorKey.wasPressed()) {
                client.setScreen(new ChatWindowEditorScreen(null));
            }
        });
    }

    public static void clientMessage(String message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.player.sendMessage(Text.literal("[ChatWindows] " + message), false);
        }
    }
}
