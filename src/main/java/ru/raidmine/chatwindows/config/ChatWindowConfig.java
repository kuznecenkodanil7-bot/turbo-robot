package ru.raidmine.chatwindows.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public final class ChatWindowConfig {
    public String id = UUID.randomUUID().toString();
    public String title = "Фильтр";
    public int x = 8;
    public int y = 40;
    public int width = 260;
    public int height = 120;
    public boolean enabled = true;
    public boolean hideMatchesInMainChat = false;
    public boolean caseSensitive = false;
    public boolean exactWord = false;
    public int maxMessages = 100;
    public List<String> keywords = new ArrayList<>();

    public boolean matches(String message) {
        if (!enabled || keywords == null || keywords.isEmpty()) {
            return false;
        }

        String source = caseSensitive ? message : message.toLowerCase(Locale.ROOT);
        for (String rawKeyword : keywords) {
            if (rawKeyword == null) {
                continue;
            }

            String keyword = rawKeyword.trim();
            if (keyword.isEmpty()) {
                continue;
            }

            String testedKeyword = caseSensitive ? keyword : keyword.toLowerCase(Locale.ROOT);
            if (exactWord) {
                if (containsExactWord(source, testedKeyword)) {
                    return true;
                }
            } else if (source.contains(testedKeyword)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsExactWord(String source, String keyword) {
        int index = source.indexOf(keyword);
        while (index >= 0) {
            boolean leftOk = index == 0 || !Character.isLetterOrDigit(source.charAt(index - 1));
            int rightIndex = index + keyword.length();
            boolean rightOk = rightIndex >= source.length() || !Character.isLetterOrDigit(source.charAt(rightIndex));
            if (leftOk && rightOk) {
                return true;
            }
            index = source.indexOf(keyword, index + 1);
        }
        return false;
    }

    public boolean isInside(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public boolean isTitleBar(double mouseX, double mouseY) {
        return isInside(mouseX, mouseY) && mouseY <= y + 18;
    }

    public boolean isResizeCorner(double mouseX, double mouseY) {
        return mouseX >= x + width - 12 && mouseX <= x + width && mouseY >= y + height - 12 && mouseY <= y + height;
    }

    public void clampToScreen(int screenWidth, int screenHeight) {
        width = Math.max(120, Math.min(width, Math.max(120, screenWidth)));
        height = Math.max(60, Math.min(height, Math.max(60, screenHeight)));
        x = Math.max(0, Math.min(x, Math.max(0, screenWidth - width)));
        y = Math.max(0, Math.min(y, Math.max(0, screenHeight - height)));
    }
}
