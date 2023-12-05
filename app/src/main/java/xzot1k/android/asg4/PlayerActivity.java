package xzot1k.android.asg4;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.concurrent.TimeUnit;

public class PlayerActivity extends AppCompatActivity {

    // initialize all views, bars, buttons, tasks, etc. for general access
    private SeekBar seekBar;
    private FloatingActionButton playButton, nextButton, previousButton;
    private Button songListButton;
    private TextView duration, seekDuration, songName, artistName;

    private Handler handler;
    private Runnable seekUpdateTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.song_player);

        handler = new Handler(Looper.getMainLooper()); // new handler instance off the main looper

        // get each of the views defined in the song_player xml
        songName = findViewById(R.id.songName);
        artistName = findViewById(R.id.artistName);
        duration = findViewById(R.id.currentSeekText);
        seekDuration = findViewById(R.id.seekDuration);
        seekBar = findViewById(R.id.seekbar);
        playButton = findViewById(R.id.button);
        nextButton = findViewById(R.id.nextButton);
        previousButton = findViewById(R.id.prevButton);
        songListButton = findViewById(R.id.songsButton);

        play(); // play current song

        playButton.setOnClickListener(view -> play()); // play/pause song & change appearance of button when clicked

        // setup the song list button listener
        songListButton.setOnClickListener(view -> {

            // clear the current seek bar task & current song (despite the song currently playing)
            clearSeekTask();
            MainActivity.CURRENT_SONG = null;

            // create new MainActivity intent and start the activity
            Intent intent = new Intent(MainActivity.INSTANCE, MainActivity.class);
            MainActivity.INSTANCE.startActivity(intent);
        });

        // handle next & previous button click
        nextButton.setOnClickListener(view -> seekButtonHelper(true));
        previousButton.setOnClickListener(view -> seekButtonHelper(false));

        // setup what happens when the seek bar changes
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

                if (MainActivity.MEDIA_PLAYER == null) return; // return if media player is invalid

                // pause media player, if playing, and update play/pause button to pause mode
                if (MainActivity.MEDIA_PLAYER.isPlaying()) MainActivity.MEDIA_PLAYER.pause();
                playButton.setImageDrawable(ContextCompat.getDrawable(PlayerActivity.this, android.R.drawable.ic_media_play));

                clearSeekTask(); // clear the seek bar update task
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {

                if (MainActivity.MEDIA_PLAYER == null) return; // return if media player is invalid

                duration.setText(MainActivity.INSTANCE.convertToTimeText(progress)); // update the duration text to the current progress

                // check if the progress meets the end of the track
                if (progress >= seekBar.getMax()) resetSeekBar();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                // check if the progress meets the end of the track, if so reset the seek bar, media player, etc.
                if (seekBar.getProgress() >= seekBar.getMax()) {
                    resetSeekBar();
                    return;
                }

                if (MainActivity.MEDIA_PLAYER == null) play(); // if the media player is invalid, run the play function to fix it

                // set position of the media player to the last tracked touch location
                final int newSeekTime = (int) TimeUnit.SECONDS.toMillis(seekBar.getProgress());
                MainActivity.MEDIA_PLAYER.seekTo(newSeekTime);

                duration.setText(MainActivity.INSTANCE.convertToTimeText(seekBar.getProgress())); // update current seek position text

                // start the media player, if paused, and start a new seek bar update task
                if (!MainActivity.MEDIA_PLAYER.isPlaying()) MainActivity.MEDIA_PLAYER.start();
                startSeekUpdateTask();

                // update play/pause button to playing mode
                playButton.setImageDrawable(ContextCompat.getDrawable(PlayerActivity.this, android.R.drawable.ic_media_pause));
            }
        });
    }

    /**
     * Resets the seek bar progress, text, media player, and related buttons
     */
    private void resetSeekBar() {
        resetMedia(false); // reset the media player, but not the current song

        seekBar.setProgress(0); // reset the seek bar progress to 0

        // update play/pause button to pause mode and reset duration text to 0:00
        playButton.setImageDrawable(ContextCompat.getDrawable(PlayerActivity.this, android.R.drawable.ic_media_play));
        duration.setText(R.string.defaultSeek);
    }

    /**
     * Determines whether to get next or previous song based on what button was clicked.
     *
     * @param isNext Whether the next button was clicked or not.
     */
    private void seekButtonHelper(boolean isNext) {

        final int currentSongId = getCurrentSongIdList(),                           // the current song's identifier
                newSongValue = (isNext ? 1 : -1),                                   // determine whether to add or subtract based on what button

                // determine the new song's identifier whether is next or previous
                newSongId = ((currentSongId + newSongValue) < 0)
                        ? (MainActivity.INSTANCE.getSongs().size() - 1)
                        : (((currentSongId + newSongValue) > (MainActivity.INSTANCE.getSongs().size() - 1))
                        ? 0 : (currentSongId + newSongValue));

        // check if a the new song was found, if not state there was an issue
        if (newSongId == -1) {
            Toast.makeText(this, "There was an issue retrieving the "
                    + (isNext ? "next" : "previous") + " song.", Toast.LENGTH_LONG).show();
            return;
        }

        // reset the media player and update current song
        resetMedia(true);
        MainActivity.CURRENT_SONG = MainActivity.INSTANCE.getSongs().get(newSongId);

        // update song name, artist, and set the play/pause button to playing mode
        songName.setText(MainActivity.CURRENT_SONG.getName());
        artistName.setText(MainActivity.CURRENT_SONG.getArtist());
        playButton.setImageDrawable(ContextCompat.getDrawable(this, android.R.drawable.ic_media_pause));

        play(); // play the song
    }

    /**
     * @return The Identifier of the current song.
     */
    private int getCurrentSongIdList() {

        // loop through all loaded songs and return current index if the song matches the current song
        for (int i = -1; ++i < MainActivity.INSTANCE.getSongs().size(); ) {
            final Song song = MainActivity.INSTANCE.getSongs().get(i);
            if (song == MainActivity.CURRENT_SONG) return i;
        }

        return -1; // return -1 if no match was found
    }

    /**
     * Clear the seek task, if possible.
     */
    private void clearSeekTask() {

        if (seekUpdateTask == null) return; // return if seek task is invalid

        // clear all tasks and set the task variable to null
        handler.removeCallbacks(seekUpdateTask);
        seekUpdateTask = null;
    }

    /**
     * Resets the media player.
     *
     * @param clearCurrentSong Whether to also clear the current song.
     */
    private void resetMedia(boolean clearCurrentSong) {
        clearSeekTask();

        // check if media player is valid
        if (MainActivity.MEDIA_PLAYER != null) {

            // if media player is playing, stop it
            if (MainActivity.MEDIA_PLAYER.isPlaying()) MainActivity.MEDIA_PLAYER.stop();

            // release and set media player to null
            MainActivity.MEDIA_PLAYER.release();
            MainActivity.MEDIA_PLAYER = null;
        }

        if (clearCurrentSong) MainActivity.CURRENT_SONG = null; // clear the current song, if told to
    }

    /**
     * Play/Pause the current song (Acts as a toggle).
     */
    public void play() {

        final Song song = MainActivity.CURRENT_SONG; // get current song

        // print what exactly is playing
        Toast.makeText(this, ("Now Playing \"" + song.getName() + "\" by " + song.getArtist()), Toast.LENGTH_SHORT).show();

        // update song name & artist text
        songName.setText(song.getName());
        artistName.setText(song.getArtist());

        // check if media player is invalid
        if (MainActivity.MEDIA_PLAYER == null) {
            MainActivity.MEDIA_PLAYER = MediaPlayer.create(this, song.getId()); // create new media player using current song's identifier
            MainActivity.MEDIA_PLAYER.start();                                         // start the song

            // update the seek bar to use the duration of the song
            seekBar.setMax((int) song.getDuration());

            // update the seek duration text
            seekDuration.setText(MainActivity.INSTANCE.convertToTimeText((int) song.getDuration()));

            startSeekUpdateTask(); // create new seek updating task to update the seek bar

            // set the play/pause button to playing mode
            playButton.setImageDrawable(ContextCompat.getDrawable(this, android.R.drawable.ic_media_pause));

            return;
        }

        // if media player is playing pause it; otherwise, start it
        if (MainActivity.MEDIA_PLAYER.isPlaying()) {

            MainActivity.MEDIA_PLAYER.pause();
            playButton.setImageDrawable(ContextCompat.getDrawable(this, android.R.drawable.ic_media_play)); // update button to paused mode

        } else {

            MainActivity.MEDIA_PLAYER.start();
            playButton.setImageDrawable(ContextCompat.getDrawable(this, android.R.drawable.ic_media_pause)); // update button to playing mode
        }
    }

    /**
     * create a new repeating task that updates the seek bar based on song progress.
     */
    private void startSeekUpdateTask() {

        // new repeating task
        handler.post(seekUpdateTask = new Runnable() {
            @Override
            public void run() {

                final MediaPlayer mediaPlayer = MainActivity.MEDIA_PLAYER;  // the global media player
                if (mediaPlayer == null) return;                            // return if media player is invalid

                // the current position of the media player
                final long currentPosition = TimeUnit.MILLISECONDS.toSeconds(mediaPlayer.getCurrentPosition());

                // update the seek bar progress if the media player is playing & its less than or equal to the max duration of the song
                if (mediaPlayer.isPlaying() && currentPosition <= TimeUnit.MILLISECONDS.toSeconds(mediaPlayer.getDuration()))
                    seekBar.setProgress((int) currentPosition);

                handler.postDelayed(this, 500); // delay by half a second
            }
        });

    }

}
