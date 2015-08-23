package co.carlosandresjimenez.android.spotifystreamer;

import android.os.Parcel;
import android.os.Parcelable;

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
    private String imageUrlHq;
    private String description;
    private String songUrl;
    private String artist;
    private String previewUrl;

    public ListItem(String id, String imageUrl, String name, String description) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.imageUrlHq = "";
        this.name = name;
        this.description = description;
        this.songUrl = "";
        this.artist = "";
        this.previewUrl = "";
    }

    public ListItem(String id, String imageUrl, String imageUrlHq, String name, String description, String songUrl, String artist, String previewUrl) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.imageUrlHq = imageUrlHq;
        this.name = name;
        this.description = description;
        this.songUrl = songUrl;
        this.artist = artist;
        this.previewUrl = previewUrl;
    }

    public ListItem(Parcel parcel) {
        this.name = parcel.readString();
        this.id = parcel.readString();
        this.imageUrl = parcel.readString();
        this.imageUrlHq = parcel.readString();
        this.description = parcel.readString();
        this.songUrl = parcel.readString();
        this.artist = parcel.readString();
        this.previewUrl = parcel.readString();
    }

    @Override
    public String toString() {
        return "ListItem{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", imageUrlHq='" + imageUrlHq + '\'' +
                ", description='" + description + '\'' +
                ", songUrl='" + songUrl + '\'' +
                ", artist='" + artist + '\'' +
                ", previewUrl='" + previewUrl + '\'' +
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

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }

    public String getSongUrl() {
        return songUrl;
    }

    public void setSongUrl(String songUrl) {
        this.songUrl = songUrl;
    }

    public String getImageUrlHq() {
        return imageUrlHq;
    }

    public void setImageUrlHq(String imageUrlHq) {
        this.imageUrlHq = imageUrlHq;
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
        parcel.writeString(imageUrlHq);
        parcel.writeString(description);
        parcel.writeString(songUrl);
        parcel.writeString(artist);
        parcel.writeString(previewUrl);
    }
}
