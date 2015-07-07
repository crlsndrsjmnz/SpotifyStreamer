package co.carlosandresjimenez.android.spotifystreamer;

/**
 * Created by carlosjimenez on 7/3/15.
 */
public class ListItem {

    public String name;
    public String id;
    public String imageUrl;

    public ListItem(String id, String imageUrl, String name) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.name = name;
    }

    @Override
    public String toString() {
        return "ListItem{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                '}';
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;

    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getName() {
        return name;
    }
}
