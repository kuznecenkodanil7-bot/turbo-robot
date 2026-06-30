package ru.raidmine.chatwindows.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import ru.raidmine.chatwindows.ChatWindowsMod;
import ru.raidmine.chatwindows.config.ChatWindowConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ChatWindowSettingsScreen extends Screen {
    private final Screen parent;
    private final String windowId;
    private TextFieldWidget titleField;
    private TextFieldWidget keywordsField;
    private ButtonWidget enabledButton;
    private ButtonWidget hideButton;
    private ButtonWidget caseButton;
    private ButtonWidget exactButton;

    public ChatWindowSettingsScreen(Screen parent, String windowId) {
        super(Text.literal("Настройки окна чата"));
        this.parent = parent;
        this.windowId = windowId;
    }

    @Override
    protected void init() {
        ChatWindowConfig window = window();
        if (window == null) {
            close();
            return;
        }

        int centerX = width / 2;
        int formX = centerX - 150;
        int y = 54;

        titleField = new TextFieldWidget(textRenderer, formX, y, 300, 20, Text.literal("Название"));
        titleField.setMaxLength(64);
        titleField.setText(window.title == null ? "" : window.title);
        addDrawableChild(titleField);

        keywordsField = new TextFieldWidget(textRenderer, formX, y + 46, 300, 20, Text.literal("Ключевые слова"));
        keywordsField.setMaxLength(512);
        keywordsField.setText(window.keywords == null ? "" : String.join(", ", window.keywords));
        addDrawableChild(keywordsField);

        enabledButton = addDrawableChild(ButtonWidget.builder(Text.empty(), button -> {
            persistFields(false);
            ChatWindowConfig current = window();
            if (current != null) {
                current.enabled = !current.enabled;
                refreshButtonTexts();
            }
        }).dimensions(formX, y + 82, 145, 20).build());

        hideButton = addDrawableChild(ButtonWidget.builder(Text.empty(), button -> {
            persistFields(false);
            ChatWindowConfig current = window();
            if (current != null) {
                current.hideMatchesInMainChat = !current.hideMatchesInMainChat;
                refreshButtonTexts();
            }
        }).dimensions(formX + 155, y + 82, 145, 20).build());

        caseButton = addDrawableChild(ButtonWidget.builder(Text.empty(), button -> {
            persistFields(false);
            ChatWindowConfig current = window();
            if (current != null) {
                current.caseSensitive = !current.caseSensitive;
                refreshButtonTexts();
            }
        }).dimensions(formX, y + 108, 145, 20).build());

        exactButton = addDrawableChild(ButtonWidget.builder(Text.empty(), button -> {
            persistFields(false);
            ChatWindowConfig current = window();
            if (current != null) {
                current.exactWord = !current.exactWord;
                refreshButtonTexts();
            }
        }).dimensions(formX + 155, y + 108, 145, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Удалить окно"), button -> {
            ChatWindowsMod.manager().deleteWindow(windowId);
            client.setScreen(parent);
        }).dimensions(formX, y + 142, 145, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Готово"), button -> close()).dimensions(formX + 155, y + 142, 145, 20).build());

        refreshButtonTexts();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        int centerX = width / 2;
        int formX = centerX - 150;
        context.drawCenteredTextWithShadow(textRenderer, title, centerX, 20, 0xFFFFFFFF);
        context.drawTextWithShadow(textRenderer, "Название окна:", formX, 40, 0xFFDDDDDD);
        context.drawTextWithShadow(textRenderer, "Ключевые слова через запятую:", formX, 86, 0xFFDDDDDD);
        context.drawTextWithShadow(textRenderer, "Если слово найдено в сообщении, оно попадёт именно в это окно.", formX, 226, 0xFFAAAAAA);
        context.drawTextWithShadow(textRenderer, "Флаг скрытия нужен только если хочешь убирать совпадения из обычного чата.", formX, 238, 0xFFAAAAAA);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void close() {
        persistFields(true);
        if (client != null) {
            client.setScreen(parent);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private ChatWindowConfig window() {
        return ChatWindowsMod.manager() == null ? null : ChatWindowsMod.manager().findConfigById(windowId);
    }

    private void persistFields(boolean save) {
        ChatWindowConfig window = window();
        if (window == null || titleField == null || keywordsField == null) {
            return;
        }
        String title = titleField.getText().trim();
        window.title = title.isEmpty() ? "Фильтр" : title;
        window.keywords = parseKeywords(keywordsField.getText());
        if (save) {
            ChatWindowsMod.manager().save();
        }
    }

    private List<String> parseKeywords(String input) {
        ArrayList<String> result = new ArrayList<>();
        if (input == null || input.isBlank()) {
            return result;
        }
        Arrays.stream(input.split(","))
                .map(String::trim)
                .filter(keyword -> !keyword.isEmpty())
                .distinct()
                .forEach(result::add);
        return result;
    }

    private void refreshButtonTexts() {
        ChatWindowConfig window = window();
        if (window == null) {
            return;
        }
        enabledButton.setMessage(Text.literal(window.enabled ? "Включено: да" : "Включено: нет"));
        hideButton.setMessage(Text.literal(window.hideMatchesInMainChat ? "В основном: скрывать" : "В основном: оставить"));
        caseButton.setMessage(Text.literal(window.caseSensitive ? "Регистр: учитывать" : "Регистр: не важно"));
        exactButton.setMessage(Text.literal(window.exactWord ? "Слово: целиком" : "Слово: часть текста"));
    }
}
