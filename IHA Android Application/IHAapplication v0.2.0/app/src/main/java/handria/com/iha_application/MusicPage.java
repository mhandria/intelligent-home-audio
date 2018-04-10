package handria.com.iha_application;

import android.content.res.Resources;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class MusicPage extends AppCompatActivity {

    private boolean isPlaying;
    private ArrayList<String> songList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_page);
        isPlaying = false;
        songList = new ArrayList<>();
        songList.add("Shooting Stars.mp3");
        songList.add("Midnight City.mp3");
        songList.add("Paranoid.mp3");
        songList.add("Closer.mp3");
        songList.add("Back In Black.mp3");
        songList.add("Wonderwall.mp3");
        initializeTableView(songList);

    }



    public void initializeTableView(ArrayList<String> list){
        TableLayout tableView = (TableLayout) findViewById(R.id.songList);
        for(final String title: list){

            TableRow row = new TableRow(MusicPage.this);
            TableLayout.LayoutParams layoutParams = new TableLayout.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(0 , 10, 0, 10);
            row.setPadding(convertDpToPixel(25), 0, 0, 0);
            row.setLayoutParams(layoutParams);



            ImageView albumView = new ImageView(MusicPage.this);
            TableRow.LayoutParams imageLayout = new TableRow.LayoutParams();
            imageLayout.width = 0;
            imageLayout.height = TableRow.LayoutParams.MATCH_PARENT;
            imageLayout.weight = 0.25f;

            albumView.setImageResource(R.drawable.mutemusicon);
            albumView.setBackgroundColor(getResources().getColor(R.color.backgroundGray));
            albumView.setPadding(0, convertDpToPixel(5), 0, convertDpToPixel(5));
            albumView.setLayoutParams(imageLayout);


            TextView songTitle = new TextView(MusicPage.this);
            TableRow.LayoutParams titleLayout = new TableRow.LayoutParams();
            titleLayout.width = 0;
            titleLayout.height = TableRow.LayoutParams.WRAP_CONTENT;
            titleLayout.weight = 0.75f;
            songTitle.setPadding(convertDpToPixel(30),convertDpToPixel(20),0,convertDpToPixel(20));
            songTitle.setText(title);
            songTitle.setTextSize(20);

            songTitle.setIncludeFontPadding(true);
            songTitle.setLayoutParams(titleLayout);
            tableView.addView(row);
            row.addView(albumView);
            row.addView(songTitle);


        }

    }

    private float intToDp(int dp){
        Resources resources = this.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }

    public static int convertDpToPixel(float dp){
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return Math.round(px);
    }
    /**
     * TODO: this function's purpose is to play/pause the current playing song.
     * @param view
     */
    public void playMusic(View view){
        TextView playButton = (TextView)findViewById(R.id.musicControl);
        if(!isPlaying)
            playButton.setText(R.string.pause);
        else
            playButton.setText(R.string.play_arrow);
        isPlaying = !isPlaying;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){
            //TODO: change volumes of speaker BRING IT DOWN
        }
        if(keyCode == KeyEvent.KEYCODE_VOLUME_UP){
            //TODO: change volumes of speaker RISE IT UP
        }
        return true;
    }
}
