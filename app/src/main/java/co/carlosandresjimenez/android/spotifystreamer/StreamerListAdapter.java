package co.carlosandresjimenez.android.spotifystreamer;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by carlosjimenez on 7/3/15.
 */
public class StreamerListAdapter extends ArrayAdapter<ListItem> {

    private final String LOG_TAG = StreamerListAdapter.class.getSimpleName();

    private ArrayList<ListItem> items;

    /**
     * This is our own custom constructor (it doesn't mirror a superclass constructor).
     * The context is used to inflate the layout file, and the List is the data we want
     * to populate into the lists
     *
     * @param context The current context. Used to inflate the layout file.
     * @param items   A List of ListItems objects to display in a list
     */
    public StreamerListAdapter(Context context, ArrayList<ListItem> items) {
        super(context, 0, items);
        this.items = items;
    }

    public ArrayList<ListItem> getAllItems() {
        return items;
    }

    /**
     * Provides a view for an AdapterView (ListView, GridView, etc.)
     *
     * @param position The AdapterView position that is requesting a view
     * @param view     The recycled view to populate.
     * @param parent   The parent ViewGroup that is used for inflation.
     * @return The View for the position in the AdapterView.
     */
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder;
        if (view != null) {
            holder = (ViewHolder) view.getTag();
        } else {
            view = LayoutInflater.from(getContext()).inflate(R.layout.list_detail_main, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        }

        ListItem item = getItem(position);
        Uri imgUri;

        if (item != null) {
            try {
                imgUri = Uri.parse(item.getImageUrl());

                Picasso.with(LayoutInflater.from(getContext()).getContext())
                        .load(imgUri)
                        .placeholder(R.drawable.ic_artist)
                        .error(R.drawable.ic_artist)
                        .into(holder.item_image);

                holder.item_name.setText(item.getName());
                holder.item_description.setText(item.getDescription());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return view;
    }

    static class ViewHolder {

        @Bind(R.id.item_image)
        ImageView item_image;
        @Bind(R.id.item_name)
        TextView item_name;
        @Bind(R.id.item_description)
        TextView item_description;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

}
