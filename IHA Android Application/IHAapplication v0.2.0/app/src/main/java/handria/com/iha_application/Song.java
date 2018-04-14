package handria.com.iha_application;

/**
 * Created by michaelhandria on 4/10/18.
 */

public class Song {

    public String title;
    public String fileName;
    public String artist;
    public String album;

    public int songId;
    public static int songCount = 0;


    public Song(String f){
        fileName = f;
        title = f;
        artist = "<unknown>";
        album = "<unknown>";
        songId = songCount;
        updateSongCount();
    }

    public Song(String f, String a){
        fileName = f;
        title = f;
        artist = a;
        album = "<unknown>";
        songId = songCount;
        updateSongCount();
    }

    public Song(String f, String t, String a){
        fileName  = f;
        title = t;
        artist = a;
        album = "<unknown>";
        songId = songCount;
        updateSongCount();
    }

    public Song(String f, String t, String a, String alb){
        fileName = f;
        title = t;
        artist = a;
        album = alb;
        songId = songCount;
        updateSongCount();
    }

    public String getTitle(){
        return this.title;
    }

    public String getArtist(){
        return this.artist;
    }

    public String getFileName(){ return this.fileName; }

    public String getAlbum(){ return this.album; }

    public int getSongId(){ return this.songId; }

    public static void updateSongCount(){
        songCount++;
    }
    public static void resetSongCount(){songCount = 0;}

    public static int getSongCount(){
        return songCount;
    }
}
