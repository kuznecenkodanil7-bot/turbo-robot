package ru.raidmine.chatwindows.config;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public final class ChatWindowRuntime {
    private final ChatWindowConfig config;
    private final Deque<FilteredChatMessage> messages = new ArrayDeque<>();

    public ChatWindowRuntime(ChatWindowConfig config) {
        this.config = config;
    }

    public ChatWindowConfig config() {
        return config;
    }

    public void addMessage(FilteredChatMessage message) {
        messages.addLast(message);
        int limit = Math.max(20, config.maxMessages);
        while (messages.size() > limit) {
            messages.removeFirst();
        }
    }

    public List<FilteredChatMessage> newestFirst() {
        ArrayList<FilteredChatMessage> copy = new ArrayList<>(messages);
        return copy;
    }

    public void clear() {
        messages.clear();
    }
}
