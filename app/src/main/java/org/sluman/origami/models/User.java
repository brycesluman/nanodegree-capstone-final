package org.sluman.origami.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by bryce on 2/17/17.
 */

public class User implements Parcelable {

    public String uid;
    public String username;
    public String displayName;
    public String userAvatar;
    public long timestamp;
    public boolean online;
    public String language;

    public User() {
    }

    public User(String uid, String username, String displayName, String userAvatar, long timestamp, boolean online, String language) {
        this.uid = uid;
        this.username = username;
        this.displayName = displayName;
        this.userAvatar = userAvatar;
        this.timestamp = timestamp;
        this.online = online;
        this.language = language;
    }

    public User(Parcel in){
        // the order needs to be the same as in writeToParcel() method
        this.uid = in.readString();
        this.username = in.readString();
        this.displayName = in.readString();
        this.userAvatar = in.readString();
        this.timestamp = in.readLong();
        this.online = in.readByte() != 0;
        this.language = in.readString();
    }

    public String getUid() {
        return uid;
    }

    public String getLanguage() {
        return language;
    }

    public String getUsername() {
        return username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getUserAvatar() {
        return userAvatar;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isOnline() {
        return online;
    }

    public boolean searchUser(String searchString) {
        if (username.contains(searchString)) {
            return true;
        }
        if (displayName.contains(searchString)) {
            return true;
        }
        return false;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("username", username);
        result.put("displayName", displayName);
        result.put("userAvatar", userAvatar);
        result.put("timestamp", timestamp);
        result.put("online", online);
        result.put("language", language);

        return result;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uid);
        dest.writeString(username);
        dest.writeString(displayName);
        dest.writeString(userAvatar);
        dest.writeLong(timestamp);
        dest.writeByte((byte) (online ? 1 : 0));
        dest.writeString(language);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        public User[] newArray(int size) {
            return new User[size];
        }
    };
}


