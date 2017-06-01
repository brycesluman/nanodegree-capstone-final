package org.sluman.origami.models;

/**
 * Created by bryce on 3/23/17.
 */

public class ConversationMessageView extends ConversationMessage {
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    private String key;
    public ConversationMessageView(String key, ConversationMessage message) {
        super(message.otherUid,
                message.otherUsername,
                message.otherAvatar,
                message.uid,
                message.text,
                message.timestamp,
                message.translatedText,
                message.userAvatar,
                message.username,
                message.isUnread);
        this.key = key;
    }
}
