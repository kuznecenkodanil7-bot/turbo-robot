package ru.raidmine.chatwindows.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import ru.raidmine.chatwindows.ChatWindowsMod;
import ru.raidmine.chatwindows.config.ChatWindowConfig;
import ru.raidmine.chatwindows.config.ChatWindowRuntime;
import ru.raidmine.chatwindows.config.FilteredChatMessage;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public final class ChatWindowsRenderer {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault());

    private ChatWindowsRenderer() {
    }

    public static void renderHud(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.options == null || client.options.hudHidden || client.currentScreen instanceof ChatWindowEditorScreen) {
            return;
        }
        renderWindows(context, false, -1, -1, null);
    }

    public static void renderEditor(DrawContext context, int mouseX, int mouseY, String selectedId) {
        renderWindows(context, true, mouseX, mouseY, selectedId);
    }

    private static void renderWindows(DrawContext context, boolean editorMode, int mouseX, int mouseY, String selectedId) {
        if (ChatWindowsMod.manager() == null) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer textRenderer = client.textRenderer;
        int screenWidth = context.getScaledWindowWidth();
        int screenHeight = context.getScaledWindowHeight();

        for (ChatWindowRuntime runtime : ChatWindowsMod.manager().windows()) {
            ChatWindowConfig window = runtime.config();
            if (!window.enabled && !editorMode) {
                continue;
            }
            window.clampToScreen(screenWidth, screenHeight);
            renderWindow(context, textRenderer, runtime, editorMode, mouseX, mouseY, selectedId);
        }
    }

    private static void renderWindow(DrawContext context, TextRenderer textRenderer, ChatWindowRuntime runtime, boolean editorMode, int mouseX, int mouseY, String selectedId) {
        ChatWindowConfig window = runtime.config();
        boolean selected = editorMode && window.id.equals(selectedId);
        boolean hovered = editorMode && window.isInside(mouseX, mouseY);

        int x = window.x;
        int y = window.y;
        int width = window.width;
        int height = window.height;
        int borderColor = selected ? 0xFFFFFFFF : hovered ? 0xCCFFFFFF : 0x66000000;

        context.fill(x, y, x + width, y + height, 0xAA050505);
        context.fill(x, y, x + width, y + 18, 0xCC111111);
        context.fill(x, y, x + width, y + 1, borderColor);
        context.fill(x, y + height - 1, x + width, y + height, borderColor);
        context.fill(x, y, x + 1, y + height, borderColor);
        context.fill(x + width - 1, y, x + width, y + height, borderColor);

        String keywords = window.keywords == null || window.keywords.isEmpty() ? "нет слов" : String.join(", ", window.keywords);
        String title = window.title + "  | " + keywords;
        context.drawTextWithShadow(textRenderer, textRenderer.trimToWidth(title, Math.max(20, width - 10)), x + 5, y + 5, window.enabled ? 0xFFFFFFFF : 0xFF888888);

        if (editorMode) {
            context.fill(x + width - 10, y + height - 10, x + width - 2, y + height - 2, 0x88FFFFFF);
            context.drawTextWithShadow(textRenderer, "↘", x + width - 10, y + height - 12, 0xFFFFFFFF);
        }

        int contentTop = y + 21;
        int contentBottom = y + height - 4;
        if (contentBottom <= contentTop) {
            return;
        }

        context.enableScissor(x + 3, contentTop, x + width - 3, contentBottom);
        List<FilteredChatMessage> messages = new ArrayList<>(runtime.newestFirst());
        int lineHeight = textRenderer.fontHeight + 2;
        int maxLines = Math.max(1, (contentBottom - contentTop) / lineHeight);
        int start = Math.max(0, messages.size() - maxLines);
        int drawY = contentTop;
        for (int i = start; i < messages.size(); i++) {
            FilteredChatMessage message = messages.get(i);
            String time = TIME_FORMAT.format(Instant.ofEpochMilli(message.receivedAtMillis()));
            String line = "[" + time + "] " + message.plainText();
            context.drawTextWithShadow(textRenderer, textRenderer.trimToWidth(line, Math.max(20, width - 10)), x + 5, drawY, 0xFFFFFFFF);
            drawY += lineHeight;
        }
        context.disableScissor();

        if (messages.isEmpty()) {
            String hint = editorMode ? "Сообщения появятся после совпадения ключевых слов" : "";
            if (!hint.isEmpty()) {
                context.drawTextWithShadow(textRenderer, textRenderer.trimToWidth(hint, Math.max(20, width - 10)), x + 5, contentTop, 0xFFAAAAAA);
            }
        }
    }
}
