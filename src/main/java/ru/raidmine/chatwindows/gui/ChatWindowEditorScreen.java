package ru.raidmine.chatwindows.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import ru.raidmine.chatwindows.ChatWindowsMod;
import ru.raidmine.chatwindows.config.ChatWindowConfig;

public final class ChatWindowEditorScreen extends Screen {
    private final Screen parent;
    private String selectedId;
    private ChatWindowConfig dragging;
    private ChatWindowConfig resizing;
    private double dragOffsetX;
    private double dragOffsetY;

    public ChatWindowEditorScreen(Screen parent) {
        super(Text.literal("Chat Windows"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        addDrawableChild(ButtonWidget.builder(Text.literal("+ Окно"), button -> {
            ChatWindowConfig created = ChatWindowsMod.manager().createWindow();
            selectedId = created.id;
            rebuildWidgets();
        }).dimensions(10, 10, 70, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Фильтры"), button -> {
            ChatWindowConfig selected = selectedWindow();
            if (selected != null) {
                client.setScreen(new ChatWindowSettingsScreen(this, selected.id));
            }
        }).dimensions(85, 10, 75, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Сохранить"), button -> {
            ChatWindowsMod.manager().save();
            close();
        }).dimensions(165, 10, 85, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Закрыть"), button -> close()).dimensions(255, 10, 75, 20).build());
    }

    private void rebuildWidgets() {
        clearChildren();
        init();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0x55000000);
        ChatWindowsRenderer.renderEditor(context, mouseX, mouseY, selectedId);
        super.render(context, mouseX, mouseY, delta);

        int y = 36;
        context.drawTextWithShadow(textRenderer, "Right Shift — открыть редактор", 10, y, 0xFFFFFFFF);
        context.drawTextWithShadow(textRenderer, "ЛКМ по верхней полоске — двигать окно", 10, y + 12, 0xFFDDDDDD);
        context.drawTextWithShadow(textRenderer, "Нижний правый угол — менять размер", 10, y + 24, 0xFFDDDDDD);
        context.drawTextWithShadow(textRenderer, "Кнопка «Фильтры» — слова для выбранного окна", 10, y + 36, 0xFFDDDDDD);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT || ChatWindowsMod.manager() == null) {
            return false;
        }

        ChatWindowConfig clicked = ChatWindowsMod.manager().windowAt(mouseX, mouseY);
        if (clicked == null) {
            selectedId = null;
            return false;
        }

        selectedId = clicked.id;
        if (clicked.isResizeCorner(mouseX, mouseY)) {
            resizing = clicked;
            return true;
        }
        if (clicked.isTitleBar(mouseX, mouseY)) {
            dragging = clicked;
            dragOffsetX = mouseX - clicked.x;
            dragOffsetY = mouseY - clicked.y;
            return true;
        }
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            if (dragging != null) {
                dragging.x = (int) Math.round(mouseX - dragOffsetX);
                dragging.y = (int) Math.round(mouseY - dragOffsetY);
                dragging.clampToScreen(width, height);
                return true;
            }
            if (resizing != null) {
                resizing.width = Math.max(120, (int) Math.round(mouseX - resizing.x));
                resizing.height = Math.max(60, (int) Math.round(mouseY - resizing.y));
                resizing.clampToScreen(width, height);
                return true;
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && (dragging != null || resizing != null)) {
            dragging = null;
            resizing = null;
            ChatWindowsMod.manager().save();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
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
        if (ChatWindowsMod.manager() != null) {
            ChatWindowsMod.manager().save();
        }
        if (client != null) {
            client.setScreen(parent);
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private ChatWindowConfig selectedWindow() {
        return ChatWindowsMod.manager() == null ? null : ChatWindowsMod.manager().findConfigById(selectedId);
    }
}
