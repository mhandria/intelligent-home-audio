package handria.com.iha_application;

/**
 * Created by michaelhandria on 4/10/18.
 */

public class Song {

    public String title;
    public String fileName;
    public String artist;
    public int songId;
    public static int songCount = 0;


    public Song(String f){
        fileName = f;
        title = f;
        artist = "<unknown>";
        songId = songCount;
        updateSongCount();
    }
    public Song(String f, String a){
        fileName = f;
        title = f;
        artist = a;
        songId = songCount;
        updateSongCount();
    }

    public Song(String f, String t, String a){
        fileName  = f;
        title = t;
        artist = a;
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

    public int getSongId(){ return this.songId; }

    public static void updateSongCount(){
        songCount++;
    }

    public static int getSongCount(){
        return songCount;
    }
}
