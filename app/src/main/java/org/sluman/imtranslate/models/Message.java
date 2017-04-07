package org.sluman.imtranslate.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.os.ParcelableCompat;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by bryce on 2/6/17.
 */


@IgnoreExtraProperties
public class Message implements Parcelable {
    public String uid;
    public long timestamp;
    public String username;
    public String userAvatar;
    public String text;
    public String translatedText;
    public boolean isNew;

    public Message() {
    }

    public Message(String uid, String text, long timestamp, String translatedText, String userAvatar, String username) {
        this.uid = uid;
        this.text = text;
        this.timestamp = timestamp;
        this.translatedText = translatedText;
        this.userAvatar = userAvatar;
        this.username = username;
    }

    public Message(Parcel in){
        // the order needs to be the same as in writeToParcel() method
        this.uid = in.readString();
        this.text = in.readString();
        this.timestamp = in.readLong();
        this.translatedText = in.readString();
        this.userAvatar = in.readString();
        this.username = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
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
        result.put("uid", uid);
        result.put("text", text);
        result.put("timestamp", timestamp);
        result.put("translatedText", translatedText);
        result.put("userAvatar", userAvatar);
        result.put("username", username);

        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }



    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Message createFromParcel(Parcel in) {
            return new Message(in);
        }

        public Message[] newArray(int size) {
            return new Message[size];
        }
    };
}
