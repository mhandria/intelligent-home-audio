package handria.com.iha_application;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.icu.text.UnicodeSetSpanner;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.support.design.widget.TabLayout;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class MusicPage extends AppCompatActivity implements onComplete, SideviewControl{

    private boolean isPlaying;
    private int currentPlaying;
    private ArrayList<Song> _songList;
    private ArrayList<TableRow> _songRow;
    private SocketConnection _connection;
    private Context room = this;

    private Typeface icon;


    private String hostName;
    private String port = "14123";

    private ViewGroup selectedSong;

    private boolean needSongs;
    private onComplete postExecute = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_page);

        isPlaying = false;
        currentPlaying = -1;
        needSongs = true;

        _connection = getSocket();
        //icon = getResources().getFont(R.font.icon);
        icon = ResourcesCompat.getFont(MusicPage.this, R.font.icon);
        hostName = extractIp();
        if(hostName.equals("INVALID")){
            _connection.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "false", port, "stat");
        }else{
            _connection.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "true", port, "stat", hostName);
        }
    }


    private void playSong(Song song, View view){
        currentPlaying = song.getSongId();
        _connection = getSocket();
        _connection.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "true", port, "play "+song.getFileName(), hostName);
    }

    public void changeCurrentSongPlayingDisplay(Song song, View view){
        currentPlaying = song.getSongId();
        //change UI for music controls.
        TextView playButton = (TextView)findViewById(R.id.musicControl);
        playButton.setText(R.string.pause);
        isPlaying = true;
        //change now playing banner here
        TextView songBanner = (TextView) findViewById(R.id.currentPlayingBanner);
        if(selectedSong != null){
            selectedSong.setBackgroundColor(Color.WHITE);
            ViewGroup prevSongInfo = (ViewGroup) selectedSong.getChildAt(1);
            ((TextView) prevSongInfo.getChildAt(0)).setTextColor(Color.BLACK);
            ((TextView) prevSongInfo.getChildAt(1)).setTextColor(Color.BLACK);
        }

        view.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        selectedSong = (ViewGroup) view;
        ViewGroup songInfo = (ViewGroup) selectedSong.getChildAt(1);
        ((TextView) songInfo.getChildAt(0)).setTextColor(Color.WHITE);
        ((TextView) songInfo.getChildAt(1)).setTextColor(Color.WHITE);
        songBanner.setText("Playing: "+song.getTitle());
    }

    public void addToSongList(final Song song){
        _songList.add(song);
        TableLayout tableView = (TableLayout) findViewById(R.id.songList);


        TableRow row = new TableRow(MusicPage.this);
        TableLayout.LayoutParams layoutParams = new TableLayout.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0 , 10, 0, 10);
        row.setPadding(convertDpToPixel(25), 0, 0, 0);
        row.setBackgroundColor(Color.WHITE);
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


        LinearLayout songInfo = new LinearLayout(MusicPage.this);
        TableRow.LayoutParams silayoutParams = new TableRow.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.75f);
        songInfo.setPadding(convertDpToPixel(30), convertDpToPixel(15), 0, convertDpToPixel(15));
        songInfo.setOrientation(LinearLayout.VERTICAL);
        songInfo.setLayoutParams(silayoutParams);

        TextView songTitle = new TextView(MusicPage.this);
        LinearLayout.LayoutParams titleLayout = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        songTitle.setText(song.getTitle());
        songTitle.setTextSize(20);
        songTitle.setTextColor(Color.BLACK);
        songTitle.setSingleLine(true);
        songTitle.setEllipsize(TextUtils.TruncateAt.END);
        songTitle.setLayoutParams(titleLayout);

        TextView songArtist = new TextView(MusicPage.this);
        LinearLayout.LayoutParams artistLayout = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        songArtist.setText(song.getArtist()+" - "+song.getAlbum());
        songArtist.setTextSize(15);
        songArtist.setTextColor(Color.BLACK);
        songArtist.setSingleLine(true);
        songArtist.setEllipsize(TextUtils.TruncateAt.END);
        songArtist.setLayoutParams(artistLayout);

        songInfo.addView(songTitle);
        songInfo.addView(songArtist);

        row.addView(albumView);
        row.addView(songInfo);

        row.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                playSong(song, view);
            }
        });
        _songRow.add(row);
        tableView.addView(row);
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
    public void playPauseMusic(View view){
        _connection = getSocket();
        if(!isPlaying) {
            _connection.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "true", port, "resume", hostName);
        }else {
            _connection.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "true", port, "pause", hostName);
        }
    }

    public void updateMusicControl(){

        TextView playButton = (TextView)findViewById(R.id.musicControl);
        if(!isPlaying) playButton.setText(R.string.pause);
        else           playButton.setText(R.string.play_arrow);
        isPlaying = !isPlaying;
    }


    public void getCurrentSongPalying(){
        _connection = getSocket();
        _connection.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "true", port, "getCurrentSong", hostName);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){

        if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){
            //TODO: change volumes of speaker BRING IT DOWN
            _connection = getSocket();
            _connection.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "true", port, "decVolume", hostName);
        }
        if(keyCode == KeyEvent.KEYCODE_VOLUME_UP){
            //TODO: change volumes of speaker RISE IT UP
            _connection = getSocket();
            _connection.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "true", port, "incVolume", hostName);

        }
        if(keyCode == KeyEvent.KEYCODE_BACK){
            if(_connection == null){
                super.onBackPressed();
            }else {
                _connection.cancel(true);
                _connection = null;
            }
        }
        return true;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig){
        return;
    }

    /**
     * this method is needed to execute after you have sent the commands.
     * @param res - res will follow the guidline below:
     *              res[0] - hostName local
     *              res[1] - hostName external
     *              res[2] - cmd sent to execute.
     *              res[3:N] - response gotten from command.
     */
    @Override
    public void onConnectAttempt(String[] res){
        TextView statusConnection = (TextView) findViewById(R.id.statusDisplay);
        if(res[0].equals("INVALID")){
            statusConnection.setText("status: Disconnected");
        }else{
            if(res[1].equals("getSongList")){
                if(res.length > 3) {

                    _songList = new ArrayList<>();
                    _songRow = new ArrayList<>();

                    Song.resetSongCount();
                    TableLayout  tb = (TableLayout)findViewById(R.id.songList);
                    tb.removeAllViews();
                    for (int i = 3; i < res.length; i++){
                        String[] songInfo = splitUnitSeparator(res[i]);
                        if(songInfo.length == 0) continue;
                        if(songInfo.length == 1 && (songInfo[0].equals("")||songInfo[0].equals(" ")))
                            continue;
                        else if(songInfo.length >= 4){
                            addToSongList(new Song(songInfo[0], songInfo[1], songInfo[2], songInfo[3]));
                        }
                        else if(songInfo.length >= 3){
                            addToSongList(new Song(songInfo[0], songInfo[1], songInfo[2]));
                        }else if(songInfo.length >= 2){
                            addToSongList(new Song(songInfo[0], songInfo[1]));
                        }else{
                            addToSongList(new Song(res[i]));
                        }
                    }
                    getCurrentSongPalying();
                }
            }else if(res[1].contains("play")){
                if(res.length > 2) {
                    if (res[2].equals("true")) {
                        isPlaying = true;
                        Song toPlay = _songList.get(currentPlaying);
                        changeCurrentSongPlayingDisplay(toPlay, _songRow.get(toPlay.getSongId()));
                    }
                }

            }else if(res[1].equals("getCurrentSong")){
                if(res[2].equals("true")) {
                    isPlaying = true;
                    TextView playButton = (TextView)findViewById(R.id.musicControl);
                    playButton.setText(R.string.pause);
                    for (Song s : _songList) {
                        if ((s.getFileName().trim()).equals(res[3])) {
                            currentPlaying = s.getSongId();
                            changeCurrentSongPlayingDisplay(s, _songRow.get(s.getSongId()));
                            break;
                        }
                    }
                }else{
                    TextView songBanner = (TextView) findViewById(R.id.currentPlayingBanner);
                    songBanner.setText("Not Playing Anything");
                }
            }else if(res[1].equals("getSpeakerListd") || res[1].equals("getSpeakerList")){
                if(res.length > 3){
                    TableLayout speakerList = (TableLayout)findViewById(R.id.speakerList);
                    speakerList.removeAllViews();
                    for(int i = 3; i < res.length; i++){
                        try {
                            String[] speakerInfo = res[i].split(":");
                            if(speakerInfo[1].equals("n")) continue;
                            int status = (speakerInfo[2].equals("y")) ? 1 : 0;
                            populateSpeakerList(new Speaker(speakerInfo[0], status));
                        }catch(IndexOutOfBoundsException io){
                            continue;
                        }
                    }
                }
                LinearLayout sidenav = (LinearLayout)findViewById(R.id.sidenav);
                sidenav.setVisibility(View.VISIBLE);
            }else if(res[1].equals("resume") || res[1].equals("pause")){
                if(res[2].equals("true")){
                    updateMusicControl();
                }else{
                    isPlaying = true;
                    updateMusicControl();
                }
            }
            statusConnection.setText("status: Connected");
        }
        hostName=res[0];
        saveIp(res[0]);
        if(needSongs){
            fetchSongList();
        }

    }

    @Override
    public void onTaskComplete(){
    }

    @Override
    public void onSideviewOpen(View view){
        _connection = getSocket();
        _connection.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "true", port, "getSpeakerList", hostName);
    }

    @Override
    public void populateSpeakerList(final Speaker speaker){
        TableLayout tableLayout = (TableLayout)findViewById(R.id.speakerList);
        TableRow row = new TableRow(MusicPage.this);
        TableLayout.LayoutParams layoutParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(convertDpToPixel(10), convertDpToPixel(10), convertDpToPixel(10), convertDpToPixel(10));
        row.setBackgroundColor(Color.WHITE);
        row.setGravity(Gravity.CENTER);
        row.setLayoutParams(layoutParams);

        TextView speakerIcon = new TextView(MusicPage.this);
        TableRow.LayoutParams iconLayout = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        iconLayout.setMargins(0, 0, convertDpToPixel(10), 0);
        speakerIcon.setLayoutParams(iconLayout);
        speakerIcon.setTypeface(icon);
        speakerIcon.setTextSize(30);
        speakerIcon.setTextColor(speaker.getStatusColor());
        speakerIcon.setText(getResources().getText(R.string.speaker));

        TextView speakerInfo = new TextView(MusicPage.this);
        TableRow.LayoutParams infoLayout = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        speakerInfo.setLayoutParams(infoLayout);
        speakerInfo.setTextColor(Color.BLACK);
        speakerInfo.setText("Speaker #"+speaker.speakerId);
        speakerInfo.setTextSize(20);

        row.addView(speakerIcon);
        row.addView(speakerInfo);
        row.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                _connection = getSocket();
                final Speaker _speaker = speaker;
                if(_speaker.getStatusColor() == Color.RED){
                    _connection.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "true", port, "enableSpeaker "+_speaker.getSpeakerId(), hostName);
                }else{
                    _connection.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "true", port, "disableSpeaker "+_speaker.getSpeakerId(), hostName);
                }
                _speaker.toggleStat();
                TextView speakerIcon = (TextView)((ViewGroup)view).getChildAt(0);
                speakerIcon.setTextColor(_speaker.getStatusColor());
            }
        });
        tableLayout.addView(row);
    }
    @Override
    public void onSideviewClose(View view){
        LinearLayout sidenav = (LinearLayout)findViewById(R.id.sidenav);
        sidenav.setVisibility(View.INVISIBLE);
    }

    private void fetchSongList(){
        needSongs = false;
        _connection = getSocket();
        _connection.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "true", port, "getSongList", hostName);
    }
    /**
     * this function will save the ip and external ip names to
     * an internal file on the contex's directory.
     *
     * @param ip - the ip given to the server being connected to
     */
    private void saveIp(String ip){
        try{
            Context _context = this;
            FileOutputStream _out = _context.openFileOutput("ip.src", Context.MODE_PRIVATE);
            byte[] ipData = ip.getBytes();
            _out.write(ipData);
            _out.close();
        }catch(Exception e){
            Log.e("SAVE IP", e.toString());
            createNewFile(ip);
        }
    }

    /**
     * This function will
     * 1) create a new file to save ip data strings into
     * 2) save the ip data strings (ip and exIp) to the file.
     *
     * @param ip - the ip given to the server being connected to
     */
    private void createNewFile(String ip){
        try{
            Context _context = this;
            File directory = _context.getFilesDir();
            File file = new File(directory, "ip.src");
            file.setWritable(true);
            FileOutputStream _out = _context.openFileOutput("ip.src", Context.MODE_PRIVATE);
            byte[] ipData = ip.getBytes();
            _out.write(ipData);
            _out.close();
        }catch(Exception e){
            Log.e("WRITE FILE", e.toString());
        }
    }

    /**
     * extract the ip names stored inside the application context.
     * @return  String[] - local ip address, external ip address, (respectively)
     */
    private String extractIp(){
        StringBuilder ipName = new StringBuilder();
        try{
            Context _context = this;
            FileInputStream _in = _context.openFileInput("ip.src");

            while(true) {
                int data = _in.read();
                if(data != '\n' && data != -1) ipName.append((char) data);
                else break;
            }

            return ipName.toString();
        }catch(Exception e){
            return "INVALID";
        }
    }

    public void refreshConnection(View view) {
        Song.resetSongCount();
        _connection = getSocket();
        _connection.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "false", port, "getSongList");
    }

    private SocketConnection getSocket(){
        return new SocketConnection(postExecute, this,
                                    (FrameLayout)findViewById(R.id.layoverMusic),
                                    (ProgressBar)findViewById(R.id.isLoadingMusic),
                                    (TextView)findViewById(R.id.statusLoading),
                                    (LinearLayout)findViewById(R.id.layoutMusicPage));
    }

    public void goBackMusic(View view) {
        if(currentPlaying == 0){
            currentPlaying = _songList.size()-1;
            playSong(_songList.get(currentPlaying), _songRow.get(currentPlaying));
            return;
        }
        currentPlaying--;
        playSong(_songList.get(currentPlaying), _songRow.get(currentPlaying));
    }

    public void goForwardMusic(View view) {
        if(currentPlaying == _songList.size()-1){
            currentPlaying = 0;
            playSong(_songList.get(currentPlaying), _songRow.get(currentPlaying));
            return;
        }
        currentPlaying++;
        playSong(_songList.get(currentPlaying), _songRow.get(currentPlaying));
    }

    private String[] splitUnitSeparator(String songInfo){
        ArrayList<String> info = new ArrayList<>();
        StringBuilder ss = new StringBuilder();
        for(char c: songInfo.toCharArray()){
            if(c == 31){
                if(!ss.toString().equals("") && !ss.toString().equals(" ")){
                    info.add(ss.toString());
                }
                ss = new StringBuilder();

            }else{
                ss.append(c);
            }
        }
        if(ss.toString().length() > 0 && !ss.toString().equals(" "))
            info.add(ss.toString());
        return info.toArray(new String[info.size()]);
    }
}
