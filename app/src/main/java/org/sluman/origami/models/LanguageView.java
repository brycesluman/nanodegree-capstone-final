package org.sluman.origami.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by bryce on 4/5/17.
 */

public class LanguageView implements Parcelable {
    private boolean selected;
    private String code;
    private String name;

    public LanguageView() {

    }

    public LanguageView(String code, String name, boolean selected) {
        this.selected = selected;
        this.code = code;
        this.name = name;
    }

    public LanguageView(Parcel in){
        // the order needs to be the same as in writeToParcel() method
        this.code = in.readString();
        this.name = in.readString();
        this.selected = in.readByte() != 0;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("code", code);
        result.put("name", name);
        result.put("selected", selected);

        return result;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(code);
        dest.writeString(name);
        dest.writeByte((byte) (selected ? 1 : 0));
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public LanguageView createFromParcel(Parcel in) {
            return new LanguageView(in);
        }

        public LanguageView[] newArray(int size) {
            return new LanguageView[size];
        }
    };
}
