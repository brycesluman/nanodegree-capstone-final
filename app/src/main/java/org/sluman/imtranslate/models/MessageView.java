package org.sluman.imtranslate.models;

/**
 * Created by bryce on 3/23/17.
 */

public class MessageView extends Message {
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    private String key;
    public MessageView(String key, Message message) {
        super(
                message.uid,
                message.text,
                message.timestamp,
                message.translatedText,
                message.userAvatar,
                message.username);
        this.key = key;
    }
}
