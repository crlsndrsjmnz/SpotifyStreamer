package co.carlosandresjimenez.android.spotifystreamer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by carlosjimenez on 7/3/15.
 */
public class StreamerListAdapter extends BaseAdapter {

    private final String LOG_TAG = StreamerListAdapter.class.getSimpleName();
    private final LayoutInflater inflater;
    private ArrayList<ListItem> items;

    public StreamerListAdapter(Context context) {
        items = new ArrayList<ListItem>();
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public ListItem getItem(int position) {
        return items.get(position);
    }

    public boolean add(ListItem item) {
        boolean returnValue = items.add(item);
        notifyDataSetChanged();
        return returnValue;
    }

    public void clear() {
        items.clear();
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder;
        if (view != null) {
            holder = (ViewHolder) view.getTag();
        } else {
            view = inflater.inflate(R.layout.list_detail_main, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        }

        if (items != null) {
            ListItem item = items.get(position);

            Picasso.with(inflater.getContext())
                    .load(item.getImageUrl())
                    .placeholder(R.drawable.ic_artist)
                    .error(R.drawable.ic_artist)
                    .into(holder.item_image);

            holder.item_name.setText(item.getName());
            holder.item_description.setText(item.getDescription());
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
