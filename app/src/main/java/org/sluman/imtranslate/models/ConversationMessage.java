package org.sluman.imtranslate.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by bryce on 3/2/17.
 */

@IgnoreExtraProperties
public class ConversationMessage implements Parcelable{
    public String otherUid;
    public String otherUsername;
    public String otherAvatar;
    public String uid;
    public long timestamp;
    public String username;
    public String userAvatar;
    public String text;
    public String translatedText;
    public boolean isNew;
    public ConversationMessage() {
    }

    public ConversationMessage(String otherUid, String otherUsername, String otherAvatar, String uid, String text, long timestamp, String translatedText, String userAvatar, String username) {
        this.otherUid = otherUid;
        this.otherUsername = otherUsername;
        this.otherAvatar = otherAvatar;
        this.uid = uid;
        this.text = text;
        this.timestamp = timestamp;
        this.translatedText = translatedText;
        this.userAvatar = userAvatar;
        this.username = username;
    }

    public ConversationMessage(Parcel in) {
        this.otherUid = in.readString();
        this.otherUsername = in.readString();
        this.otherAvatar = in.readString();
        this.uid = in.readString();
        this.text = in.readString();
        this.timestamp = in.readLong();
        this.translatedText = in.readString();
        this.userAvatar = in.readString();
        this.username = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.otherUid);
        dest.writeString(this.otherUsername);
        dest.writeString(this.otherAvatar);
        dest.writeString(this.uid);
        dest.writeString(this.text);
        dest.writeLong(this.timestamp);
        dest.writeString(this.translatedText);
        dest.writeString(this.userAvatar);
        dest.writeString(this.username);
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("otherUid", this.otherUid);
        result.put("otherUsername", this.otherUsername);
        result.put("otherAvatar", this.otherAvatar);
        result.put("uid", this.uid);
        result.put("text", this.text);
        result.put("timestamp", this.timestamp);
        result.put("translatedText", this.translatedText);
        result.put("userAvatar", this.userAvatar);
        result.put("username", this.username);

        return result;
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public ConversationMessage createFromParcel(Parcel in) {
            return new ConversationMessage(in);
        }

        public ConversationMessage[] newArray(int size) {
            return new ConversationMessage[size];
        }
    };
}
