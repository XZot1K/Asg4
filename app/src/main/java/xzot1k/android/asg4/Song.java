package xzot1k.android.asg4;

import android.graphics.drawable.Drawable;

public class Song {

    // song attribute variable definitions
    private Drawable image;
    private final String name, artist;
    private final long duration;
    private final int id;

    public Song(int id, String name, String artist, long duration) {
        this.id = id;
        this.duration = duration;

        // trim name & artist in case of any excess whitespace
        this.name = name.trim();
        this.artist = artist.trim();
    }

    // getters & setters

    /**
     * @return The name of the song gotten from MetaData.
     */
    public String getName() {return name;}

    /**
     * @return The artist of the song found from MetaData.
     */
    public String getArtist() {return artist;}

    /**
     * @return The duration of the song in milliseconds.
     */
    public long getDuration() {return duration;}

    /**
     * @return The art of the song (can be null).
     */
    public Drawable getImage() {return image;}

    /**
     * @param image The new drawable image for the song's art.
     */
    public void setImage(Drawable image) {this.image = image;}

    /**
     * @return The raw id of the song.
     */
    public int getId() {return id;}

}
