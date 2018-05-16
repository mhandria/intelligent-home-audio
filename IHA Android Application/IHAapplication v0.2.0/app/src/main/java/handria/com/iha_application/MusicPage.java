package handria.com.iha_application;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;

import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;


import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.service.ArmaRssiFilter;
import org.altbeacon.beacon.service.RunningAverageRssiFilter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class MusicPage extends AppCompatActivity implements onComplete, BeaconConsumer, SideviewControl, RangeNotifier, MonitorNotifier{

    private boolean isPlaying;
    private int currentPlaying;
    private boolean disableSpeakerTap = false;
    private ArrayList<Song> _songList;
    private ArrayList<TableRow> _songRow;
    private SocketConnection _connection;

    private Typeface icon;


    private String hostName;
    private String port = "14123";

    private ViewGroup selectedSong;

    private boolean needSongs;
    private onComplete postExecute = this;
    private BeaconManager beaconManager;
    private HashMap<String, Speaker> beacons = new HashMap<>();
    private HashMap<String, ArrayList<Double>> calculation = new HashMap<>();
    private String[] beaconAddress = {"0C:F3:EE:B3:B6:9F","0C:F3:EE:B3:B8:4E", "0C:F3:EE:B3:B8:3F", "0C:F3:EE:B3:B6:BE", "0C:F3:EE:B3:BB:54", "C2:00:10:00:01:55"};

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 456;

    private boolean followMode = true;
    private double outOfRange = 1.5;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_page);

        isPlaying = false;
        currentPlaying = -1;
        needSongs = true;

        _connection = getSocket();
        icon = ResourcesCompat.getFont(MusicPage.this, R.font.icon);
        hostName = extractIp();

        beacons.put("0C:F3:EE:B3:B6:9F", new Speaker("1", 0));
        beacons.put("0C:F3:EE:B3:B8:4E", new Speaker("2", 0));
        beacons.put("0C:F3:EE:B3:B8:3F", new Speaker("3", 0));
        beacons.put("0C:F3:EE:B3:B6:BE", new Speaker("4", 0));
        beacons.put("0C:F3:EE:B3:BB:54", new Speaker("test 1", 0));
        beacons.put("C2:00:10:00:01:55", new Speaker("test 2", 0));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
        }

        if(hostName.equals("INVALID")){
            _connection.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "false", port, "getSongList");
        }else{
            _connection.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "true", port, "getSongList", hostName);
        }
        ToggleButton followModeControl = (ToggleButton) findViewById(R.id.enableFollow);
        followModeControl.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                followMode = isChecked;
            }
        });
        SeekBar calibration = (SeekBar)findViewById(R.id.calibrateFollow);
        calibration.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                outOfRange = (progress/100.0)*4.0;
                TextView calibrateVal = (TextView)findViewById(R.id.calibrateValue);
                calibrateVal.setText(Double.toString(outOfRange));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        TimerTask _task = new TimerTask() {
            @Override
            public void run() {
                for(String s: beaconAddress){
                    if(calculation.containsKey(s) && calculation.get(s).size() > 5) {
                        double decide = calculateAvg(calculation.get(s));
                        Log.e("Beacon Calculation", beacons.get(s).getSpeakerId()+" distance rounded: "+Math.round(decide)+" actual calculation: "+decide);
                        if (decide >= outOfRange) disableSpeaker(beacons.get(s));
                        else enableSpeaker(beacons.get(s));
                        calculation.put(s, new ArrayList<Double>());
                    }
                }
            }
        };
        Timer timer = new Timer();
        timer.schedule(_task, 1000L, 1000L);


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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    beaconManager = BeaconManager.getInstanceForApplication(this);

                    beaconManager.getBeaconParsers().add(new BeaconParser()
                            .setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT));
                    beaconManager.getBeaconParsers().add(new BeaconParser()
                            .setBeaconLayout(BeaconParser.ALTBEACON_LAYOUT));
                    beaconManager.getBeaconParsers().add(new BeaconParser()
                            .setBeaconLayout("m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24"));
                    beaconManager.setRssiFilterImplClass(ArmaRssiFilter.class);
                    beaconManager.bind(this);
                } else {
                    // Alert the user that this application requires the location permission to perform the scan.
                    ToggleButton followModeControl = (ToggleButton) findViewById(R.id.enableFollow);
                    followModeControl.setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //beaconManager.unbind(this);
    }

    @Override
    public void onBeaconServiceConnect() {

        final Region regionSpeaker1 = new Region("1", "0C:F3:EE:B3:B6:9F");
        final Region regionSpeaker2 = new Region("2", "0C:F3:EE:B3:B8:4E");
        final Region regionSpeaker3 = new Region("3", "0C:F3:EE:B3:B8:3F");
        final Region regionSpeaker4 = new Region("4", "0C:F3:EE:B3:B6:BE");
        final Region regionSpeakerTest = new Region("Test", "0C:F3:EE:B3:BB:54");
        final Region regionSpeakerTest2 = new Region("Test2", "C2:00:10:00:01:55");
        final Region region = new Region("all-beacon", null, null, null);
        try {
            beaconManager.startMonitoringBeaconsInRegion(regionSpeaker1);
            beaconManager.startMonitoringBeaconsInRegion(regionSpeaker2);
            beaconManager.startMonitoringBeaconsInRegion(regionSpeaker3);
            beaconManager.startMonitoringBeaconsInRegion(regionSpeaker4);


            beaconManager.startRangingBeaconsInRegion(regionSpeaker1);
            beaconManager.startRangingBeaconsInRegion(regionSpeaker2);
            beaconManager.startRangingBeaconsInRegion(regionSpeaker3);
            beaconManager.startRangingBeaconsInRegion(regionSpeaker4);
            beaconManager.startRangingBeaconsInRegion(region);

        } catch (Exception e) {
            e.printStackTrace();
        }
        beaconManager.addMonitorNotifier(this);
        beaconManager.addRangeNotifier(this);

    }

    @Override
    public void didEnterRegion(Region region) {

    }

    @Override
    public void didExitRegion(final Region region) {
        disableSpeaker(beacons.get(region.getBluetoothAddress()));
        Log.e("Beacon Exit", "beacon exiting: "+region.getUniqueId());
        //disable once more just incase...
        TimerTask _task = new TimerTask() {
            @Override
            public void run() {
                final Region _temp = region;
                disableSpeaker(beacons.get(_temp.getBluetoothAddress()));
            }
        };
        Timer timer = new Timer();
        timer.schedule(_task, 1000L);
    }

    @Override
    public void didDetermineStateForRegion(int i, Region region) {

    }

    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> collection, Region region) {
        if(followMode) {
            for (Beacon beacon : collection) {
                //Log.e("Beacon Distance", "distance: "+beacon.getDistance());
                if(calculation.containsKey(beacon.getBluetoothAddress())){
                    ArrayList<Double> list = calculation.get(beacon.getBluetoothAddress());
                    list.add(beacon.getDistance());
                }else{
                    ArrayList<Double> _new = new ArrayList<>();
                    _new.add(beacon.getDistance());
                    calculation.put(beacon.getBluetoothAddress(), _new);
                }
            }
        }
    }
    @Override
    protected void onResume(){
        super.onResume();
        beaconManager = BeaconManager.getInstanceForApplication(this);

        beaconManager.getBeaconParsers().add(new BeaconParser()
                .setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT));
        beaconManager.getBeaconParsers().add(new BeaconParser()
                .setBeaconLayout(BeaconParser.ALTBEACON_LAYOUT));
        beaconManager.getBeaconParsers().add(new BeaconParser()
                .setBeaconLayout("m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.setRssiFilterImplClass(ArmaRssiFilter.class);
        beaconManager.bind(this);
    }

    @Override
    protected void onPause(){
        super.onPause();
        beaconManager.unbind(this);
    }

    private double calculateAvg(ArrayList<Double> values){
        Double ans = 0.0;

        for(int i = values.size()-1; i >= values.size()-5; i--){
            ans+= values.get(i);
        }
        return (ans/((double)values.size()));

    }
    private void enableSpeaker(Speaker speaker){
        if(speaker.status == 1) return;
        _connection = getSocket();
        _connection.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "true", port, "enableSpeaker " + speaker.getSpeakerId(), hostName);
        //Log.e("SPEAKER CONTROL", "enableSpeaker " + speaker.getSpeakerId());
    }

    private void disableSpeaker(Speaker speaker){
        if(speaker.status == 0) return;
        _connection = getSocket();
        _connection.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "true", port, "disableSpeaker " + speaker.getSpeakerId(), hostName);
        //Log.e("SPEAKER CONTROL", "disableSpeaker " + speaker.getSpeakerId());
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


    public void getCurrentSongPalying(String hostName){
        _connection = getSocket();
        _connection.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "true", port, "getCurrentSong", hostName);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){

        if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN){
            _connection = getSocket();
            _connection.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "true", port, "decVolume", hostName);
        }
        if(keyCode == KeyEvent.KEYCODE_VOLUME_UP){
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
        super.onConfigurationChanged(newConfig);
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
            disableSpeakerTap = false;
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
                    getCurrentSongPalying(res[0]);
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
            }else if(res[1].contains("enableSpeaker") || res[1].contains("disableSpeaker")){
                if(res[2].equals("true")) {
                    String speakerId = res[1].split(" ")[1];
                    for (String s : beaconAddress) {
                        if(speakerId.equals(beacons.get(s).getSpeakerId())){
                            beacons.get(s).toggleStat();
                        }
                    }
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
                if(!disableSpeakerTap) {
                    _connection = getSocket();
                    final Speaker _speaker = speaker;
                    if (_speaker.getStatusColor() == Color.RED) {
                        _connection.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "true", port, "enableSpeaker " + _speaker.getSpeakerId(), hostName);
                    } else {
                        _connection.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "true", port, "disableSpeaker " + _speaker.getSpeakerId(), hostName);
                    }
                    _speaker.toggleStat();
                    TextView speakerIcon = (TextView) ((ViewGroup) view).getChildAt(0);
                    speakerIcon.setTextColor(_speaker.getStatusColor());
                    disableSpeakerTap = true;
                }
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
        _connection.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "false", port, "getSongList", hostName);
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
