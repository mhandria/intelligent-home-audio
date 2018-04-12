package handria.com.iha_application;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
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
import java.util.ArrayList;

public class MusicPage extends AppCompatActivity implements onComplete, SideviewControl{

    private boolean isPlaying;
    private int currentPlaying;
    private ArrayList<Song> _songList;
    private ArrayList<TableRow> _songRow;
    private SocketConnection _connection;


    private String[] hostNames = new String[2];
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

        hostNames = extractIp();
        if(hostNames[0].equals("INVALID") && hostNames[1].equals("INVALID")){
            _connection.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "false", port, "stat");
        }else{
            _connection.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "true", port, "stat", hostNames[0], hostNames[1]);
        }
    }


    private void playSong(Song song, View view){
        currentPlaying = song.getSongId();
        _connection = getSocket();
        _connection.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "true", port, "play "+song.getFileName(), hostNames[0], hostNames[1]);
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
        songArtist.setText(song.getArtist());
        songArtist.setTextSize(15);
        songArtist.setTextColor(Color.BLACK);
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
        TextView playButton = (TextView)findViewById(R.id.musicControl);
        _connection = getSocket();
        if(!isPlaying) {
            _connection.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "true", port, "resume", hostNames[0], hostNames[1]);
            playButton.setText(R.string.pause);
        }else {
            _connection.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "true", port, "pause", hostNames[0], hostNames[1]);
            playButton.setText(R.string.play_arrow);
        }
        isPlaying = !isPlaying;
    }



    public void getCurrentSongPalying(){
        _connection = getSocket();
        _connection.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "true", port, "getCurrentSong", hostNames[0], hostNames[1]);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){

        if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){
            //TODO: change volumes of speaker BRING IT DOWN
            _connection = getSocket();
            _connection.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "true", port, "decVolume", hostNames[0], hostNames[1]);
        }
        if(keyCode == KeyEvent.KEYCODE_VOLUME_UP){
            //TODO: change volumes of speaker RISE IT UP
            _connection = getSocket();
            _connection.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "true", port, "incVolume", hostNames[0], hostNames[1]);

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
        if(res[0].equals("INVALID") && res[1].equals("INVALID")){
            statusConnection.setText("status: Disconnected");
        }else{
            if(res[2].equals("getSongList")){
                if(res.length > 4) {
                    _songList = new ArrayList<>();
                    _songRow = new ArrayList<>();
                    for (int i = 4; i < res.length; i++){
                        String[] songInfo = splitUnitSeparator(res[i]);
                        if(songInfo.length == 0) continue;
                        if(songInfo.length == 1 && (songInfo[0].equals("")||songInfo[0].equals(" ")))
                            continue;
                        if(songInfo.length >= 3){
                            addToSongList(new Song(songInfo[0], songInfo[1], songInfo[2]));
                        }else if(songInfo.length >= 2){
                            addToSongList(new Song(songInfo[0], songInfo[1]));
                        }else{
                            addToSongList(new Song(res[i]));
                        }
                    }
                    getCurrentSongPalying();
                }
            }else if(res[2].contains("play")){
                if(res[3].equals("true")){
                    isPlaying = true;
                    Song toPlay = _songList.get(currentPlaying);
                    changeCurrentSongPlayingDisplay(toPlay, _songRow.get(toPlay.getSongId()));
                }

            }else if(res[2].equals("getCurrentSong")){
                if(res[3].equals("true")) {
                    isPlaying = true;
                    TextView playButton = (TextView)findViewById(R.id.musicControl);
                    playButton.setText(R.string.pause);
                    for (Song s : _songList) {
                        if (s.getFileName().equals(res[4])) {
                            currentPlaying = s.getSongId();
                            changeCurrentSongPlayingDisplay(s, _songRow.get(s.getSongId()));
                            break;
                        }
                    }
                }else{
                    TextView songBanner = (TextView) findViewById(R.id.currentPlayingBanner);
                    songBanner.setText("Not Playing Anything");
                }
            }else if(res[2].equals("getSpeakerList")){

            }
            statusConnection.setText("status: Connected");
        }
        hostNames=new String[]{res[0], res[1]};
        saveIp(res[0], res[1]);
        if(needSongs){
            fetchSongList();
        }

    }

    @Override
    public void onTaskComplete(){
    }


    @Override
    public void onSideviewOpen(View view){
        LinearLayout sidenav = (LinearLayout)findViewById(R.id.sidenav);
        sidenav.setVisibility(View.VISIBLE);
    }
    @Override
    public void onSideviewClose(View view){
        LinearLayout sidenav = (LinearLayout)findViewById(R.id.sidenav);
        sidenav.setVisibility(View.INVISIBLE);
    }

    private void fetchSongList(){
        needSongs = false;
        _connection = getSocket();
        _connection.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "true", port, "getSongList", hostNames[0], hostNames[1]);
    }
    /**
     * this function will save the ip and external ip names to
     * an internal file on the contex's directory.
     *
     * @param ip - the ip given to the server being connected to
     * @param exIp - the router's port 14123 forwarded ip name.
     */
    private void saveIp(String ip, String exIp){
        try{
            Context _context = this;
            FileOutputStream _out = _context.openFileOutput("ip.src", Context.MODE_PRIVATE);
            byte[] ipData = combineArray(ip.getBytes(), exIp.getBytes());
            _out.write(ipData);
            _out.close();
        }catch(Exception e){
            Log.e("SAVE IP", e.toString());
            createNewFile(ip, exIp);
        }
    }

    /**
     * This function will
     * 1) create a new file to save ip data strings into
     * 2) save the ip data strings (ip and exIp) to the file.
     *
     * @param ip - the ip given to the server being connected to
     * @param exIp - the router's port 14123 forwarded ip name.
     */
    private void createNewFile(String ip, String exIp){
        try{
            Context _context = this;
            File directory = _context.getFilesDir();
            File file = new File(directory, "ip.src");
            file.setWritable(true);
            FileOutputStream _out = _context.openFileOutput("ip.src", Context.MODE_PRIVATE);
            byte[] ipData = combineArray(ip.getBytes(), exIp.getBytes());
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
    private String[] extractIp(){
        StringBuilder ipName = new StringBuilder();
        StringBuilder externalIpName = new StringBuilder();
        try{
            Context _context = this;
            FileInputStream _in = _context.openFileInput("ip.src");

            while(true) {
                int data = _in.read();
                if(data != '\n' && data != -1) ipName.append((char) data);
                else break;
            }
            while(true){
                int data = _in.read();
                if(data != -1) externalIpName.append((char) data);
                else break;
            }
            String[] names = {ipName.toString(), externalIpName.toString()};
            return names;
        }catch(Exception e){
            String[] names = {"INVALID", "INVALID"};
            return names;
        }
    }

    /**
     * This will take two saved data from context
     *  - hostName = save data
     * and will combine the bytes into a string to be displayed.
     *
     * @param a - hostName[0] == localIp
     * @param b = hostName[1] == externalIp
     * @return  the combined byte of localIp & externalIp.
     */
    private byte[] combineArray(byte[] a, byte[] b){
        byte[] newData = new byte[a.length+b.length+1];
        for(int i = 0; i < a.length; i++){
            newData[i] = a[i];
        }
        newData[a.length] = '\n';
        for(int i = 0; i < b.length; i++){
            newData[i+a.length+1] = b[i];
        }
        return newData;
    }

    public void refreshConnection(View view) {
        _connection = getSocket();
        _connection.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "false", port, "stat");
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
