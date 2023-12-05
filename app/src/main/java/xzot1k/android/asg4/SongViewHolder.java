package xzot1k.android.asg4;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SongViewHolder extends RecyclerView.ViewHolder{

    // global initialized view containers
    private final View view;
    private final TextView name, artist, duration;
    private final ImageView image;

    public SongViewHolder(@NonNull View itemView) {
        super(itemView);

        // initialize all views from the xml
        view = itemView;
        image = itemView.findViewById(R.id.image);
        name = itemView.findViewById(R.id.name);
        artist = itemView.findViewById(R.id.artistName);
        duration = itemView.findViewById(R.id.duration);
    }

    /**
     * @return The card's name text view.
     */
    public TextView getName() {
        return name;
    }

    /**
     * @return The card's artists text view.
     */
    public TextView getArtist() {
        return artist;
    }

    /**
     * @return The card's duration text view.
     */
    public TextView getDuration() {
        return duration;
    }

    /**
     * @return The card's art image view.
     */
    public ImageView getImage() {
        return image;
    }

    /**
     * @return The entire card view.
     */
    public View getView() {
        return view;
    }

}
