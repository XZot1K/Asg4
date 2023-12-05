package xzot1k.android.asg4;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class SongViewAdapter extends RecyclerView.Adapter<SongViewHolder> {

    private final ArrayList<Song> list;           // list of songs passed
    private final SongClickListener listener;     // the listener passed

    public SongViewAdapter(ArrayList<Song> list, SongClickListener listener) {

        // initialize all views from the xml
        this.list = list;
        this.listener = listener;

    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        // get the layout inflater from the parent's context & create a new song card
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final View songCard = inflater.inflate(R.layout.song_card, parent, false);

        return new SongViewHolder(songCard); // return a new wrapped song card
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder viewHolder, int position) {

        // get the song associated to the clicked index
        final int index = viewHolder.getAdapterPosition();
        final Song song = list.get(position);

        // set the art work of the song card
        viewHolder.getImage().setImageDrawable(song.getImage());
        viewHolder.getImage().setTag(song.getImage());

        // set the name, artist, & duration of the song card
        viewHolder.getName().setText(song.getName());
        viewHolder.getArtist().setText(song.getArtist());
        viewHolder.getDuration().setText(MainActivity.INSTANCE.convertToTimeText((int) song.getDuration()));

        // create a new click event listener for the song card.
        viewHolder.getView().setOnClickListener(view -> listener.click(index, song));
    }

    @Override
    public int getItemCount() {return list.size();} // return the size of the song list

    // return super with the recycler view
    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {super.onAttachedToRecyclerView(recyclerView);}

    public static class SongClickListener {

        /**
         * @param index The item index clicked.
         * @param song  The song at the index clicked.
         */
        public void click(int index, Song song) {}

    }

}
