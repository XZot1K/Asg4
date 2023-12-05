package xzot1k.android.asg4;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    // static declarations to get important instance variables across classes
    public static MainActivity INSTANCE;
    public static MediaPlayer MEDIA_PLAYER;
    public static Song CURRENT_SONG;
    private ArrayList<Song> songs; // the loaded song list

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        INSTANCE = this; // initialize global instance

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        songs = loadSongs(); // load the songs to the song list

        RecyclerView recyclerView = findViewById(R.id.songs);

        // handle what happens when a song card is clicked
        SongViewAdapter.SongClickListener songClickListener = new SongViewAdapter.SongClickListener() {
            @Override
            public void click(int index, Song song) {

                // check if media player is valid, if so stop, release, & set to null
                if (MEDIA_PLAYER != null) {
                    MEDIA_PLAYER.stop();
                    MEDIA_PLAYER.release();
                    MEDIA_PLAYER = null;
                }

                CURRENT_SONG = song; // set the current song to what was clicked

                // start a new intent and start the music player activity
                Intent intent = new Intent(INSTANCE, PlayerActivity.class);
                INSTANCE.startActivity(intent);
            }
        };

        // setup the recycler view's adapter & layout manager
        recyclerView.setAdapter(new SongViewAdapter(songs, songClickListener));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    /**
     * @return A list of all found songs from the raw resource folder.
     */
    public ArrayList<Song> loadSongs() {

        ArrayList<Song> songs = new ArrayList<>(); // a list to store found songs

        // loop all declared field in R.raw (each song file)
        for (Field field : R.raw.class.getDeclaredFields()) {

            // get the song id from R.raw using generics
            int songId;
            try {
                songId = field.getInt(null);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                continue;            // print error and continue to next song since id is undefined
            }

            try {
                MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever(); // new receiver to get metadata of the file

                // get the song Uri from the raw
                final Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + songId);
                mediaMetadataRetriever.setDataSource(MainActivity.this, uri);

                // get specific metadata and form a song out of it
                final Song song = new Song(songId, mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE),
                        mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST),
                        TimeUnit.MILLISECONDS.toSeconds(Long.parseLong(mediaMetadataRetriever
                                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION))));

                final byte[] imageData = mediaMetadataRetriever.getEmbeddedPicture(); // get the embedded art, if found

                // determine if embedded art was found, if so decode it; otherwise, get default art
                song.setImage((imageData != null) ? new BitmapDrawable(getResources(),
                        BitmapFactory.decodeByteArray(imageData, 0, imageData.length))
                        : ContextCompat.getDrawable(getApplicationContext(), R.drawable.no_album));

                songs.add(song); // append song to the song list

            } catch (IllegalArgumentException e) {e.printStackTrace();} // print error since this should never occur
        }

        // TODO look through all files to find music (I decided not to do this since permissions would need to be retrieved)

        return songs; // return list
    }

    /**
     * Converts the provided duration to a seek bar style string.
     *
     * @param duration The duration to convert.
     * @return The converted duration string.
     */
    public String convertToTimeText(int duration) {

        StringBuilder sb = new StringBuilder(); // result string

        // convert duration to days, hours, minutes, & finally minutes back to seconds after applying some math to determine current progress
        long seconds = duration,
                days = TimeUnit.SECONDS.toDays(seconds),
                hours = TimeUnit.SECONDS.toHours(seconds -= TimeUnit.DAYS.toSeconds(days)),
                minutes = TimeUnit.SECONDS.toMinutes(seconds -= TimeUnit.HOURS.toSeconds(hours));
        seconds -= TimeUnit.MINUTES.toSeconds(minutes);

        // append days if > 0
        if (days > 0) sb.append(days);

        // append hours if > 0
        if (hours > 0) {
            if (sb.length() > 0) sb.append(":");        // add colon if result length is > 0
            if (days > 0 && hours < 10) sb.append("0"); // add 0 in front, if single digit
            sb.append(hours);
        }

        if (sb.length() > 0) sb.append(":");           // add colon if result length is > 0
        if (hours > 0 && minutes < 10) sb.append("0"); // add 0 in front, if single digit
        sb.append(minutes);

        if (sb.length() > 0) sb.append(":"); // add colon if result length is > 0
        if (seconds < 10) sb.append("0");    // add 0 in front, if single digit
        sb.append(seconds);

        return sb.toString(); // return result string
    }

    /**
     * @return The list of loaded song objects.
     */
    public ArrayList<Song> getSongs() {return songs;}

}