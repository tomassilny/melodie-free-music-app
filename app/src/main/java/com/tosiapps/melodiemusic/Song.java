package com.tosiapps.melodiemusic;

/**
 * Created by Tomas on 8. 12. 2018.
 */

public class Song {
    String id, title, genres, thumb, duration;
    public Song(String id, String title, String genres, String thumb, String duration){
        this.id = id;
        this.title = title;
        this.genres = genres;
        this.thumb = thumb;
        this.duration = duration;
    }

    public String getId(){
        return id;
    }

    public String getTitle(){
        return title;
    }

    public String getThumb(){
        return  thumb;
    }

    public String getGengres(){
        return genres;
    }

    public String getDuration(){
        return duration;
    }
}
