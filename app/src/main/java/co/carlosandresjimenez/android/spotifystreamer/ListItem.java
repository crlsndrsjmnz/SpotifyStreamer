package co.carlosandresjimenez.android.spotifystreamer;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by carlosjimenez on 7/3/15.
 */
public class ListItem implements Parcelable {

    public static final Parcelable.Creator<ListItem> CREATOR = new Parcelable.Creator<ListItem>() {
        public ListItem createFromParcel(Parcel parcel) {
            return new ListItem(parcel);
        }

        public ListItem[] newArray(int size) {
            return new ListItem[size];
        }
    };
    private String name;
    private String id;
    private String imageUrl;
    private String description;

    public ListItem(String id, String imageUrl, String name, String description) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.name = name;
        this.description = description;
    }

    public ListItem(Parcel parcel) {
        this.name = parcel.readString();
        this.id = parcel.readString();
        this.imageUrl = parcel.readString();
        this.description = parcel.readString();
    }

    @Override
    public String toString() {
        return "ListItem{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", description='" + description + '\'' +
                '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(name);
        parcel.writeString(id);
        parcel.writeString(imageUrl);
        parcel.writeString(description);
    }
}
