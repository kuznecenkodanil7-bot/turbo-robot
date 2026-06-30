package ru.raidmine.chatwindows.config;

import net.minecraft.text.Text;

public final class FilteredChatMessage {
    private final Text text;
    private final String plainText;
    private final long receivedAtMillis;

    public FilteredChatMessage(Text text) {
        this.text = text;
        this.plainText = text.getString();
        this.receivedAtMillis = System.currentTimeMillis();
    }

    public Text text() {
        return text;
    }

    public String plainText() {
        return plainText;
    }

    public long receivedAtMillis() {
        return receivedAtMillis;
    }
}
