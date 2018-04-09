package handria.com.iha_application;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;

public class HomePage extends AppCompatActivity implements onComplete{

    String port = "14123";
    ArrayList<String> songList;
    private  onComplete then;
    String[] hostNames = new String[2];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        songList = new ArrayList<String>();
        songList.add("Shooting Stars.mp3");
        songList.add("Midnight City.mp3");
        songList.add("Paranoid.mp3");
        songList.add("Closer.mp3");
        songList.add("Back In Black.mp3");
        songList.add("Wonderwall.mp3");
        initializeTableView();
        then = this;
        FrameLayout layout = (FrameLayout) findViewById(R.id.layover);
        layout.setBackgroundColor(Color.argb(255, 198, 199, 201));
        ProgressBar loadingSign = (ProgressBar) findViewById(R.id.isLoading);
        loadingSign.animate();


        SocketConnection _connection = new SocketConnection(then, this);
        hostNames = extractIp();
        if(hostNames[0].equals("INVALID") && hostNames[1].equals("INVALID")){
            _connection.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "false", port, "stat");
        }else{
            _connection.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "true", port, "stat", hostNames[0], hostNames[1]);
        }
    }


    @Override
    public void onConnectAttempt(String[] res){
        //update the ui to stop the circle loading bar.
        FrameLayout layout = (FrameLayout) findViewById(R.id.layover);
        layout.setBackgroundColor(Color.TRANSPARENT);
        ProgressBar loadingSign = (ProgressBar) findViewById(R.id.isLoading);
        loadingSign.setVisibility(View.INVISIBLE);
        TextView ipName = (TextView) findViewById(R.id.ipName);
        TextView exIpName = (TextView) findViewById(R.id.exIpName);


        //change UI display of Local IP Name & External Ip Name
        //toast the response gotten from the socket cmd.
        ipName.setText("Local IP NAME: "+res[0]);
        exIpName.setText("External IP NAME: "+res[1]);
        Toast.makeText(this, res[2], Toast.LENGTH_SHORT).show();

        //update and save the new hostNames
        hostNames = new String[] {res[0], res[1]};
        saveIp(res[0], res[1]);
    }

    @Override
    public void onTaskComplete(){
        //TODO: not used unless needed for other features.
    }

    public void initializeTableView(){
        TableLayout tableView = (TableLayout) findViewById(R.id.songDisplay);
        boolean toogle = true;
        for(final String title: songList){
            TableRow row = new TableRow(this);
            TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
            row.setLayoutParams(lp);
            TextView songTitle = new TextView(this);
            songTitle.setText(title);
            songTitle.setTextSize(20);
            songTitle.setPadding(10, 5, 0, 10);
            row.setPadding(100, 5, 0, 10);
            row.addView(songTitle);

            if(toogle){
                row.setBackgroundColor(Color.GRAY);
                toogle = false;
            }else{
                toogle = true;
            }
            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d("SONG CLICK", "song name with "+title+" was clicked");
                    FrameLayout layout = (FrameLayout) findViewById(R.id.layover);
                    layout.setBackgroundColor(Color.argb(255, 198, 199, 201));
                    ProgressBar loadingSign = (ProgressBar) findViewById(R.id.isLoading);
                    loadingSign.setVisibility(View.VISIBLE);
                    loadingSign.animate();
                    SocketConnection _connection = new SocketConnection(then, view.getContext());
                    _connection.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "true", port, "play "+title, hostNames[0], hostNames[1]);
                }
            });
            tableView.addView(row);
        }
    }



    public void goCustom(View view) {
        Intent testPage = new Intent(this, TestPage.class);
        if(hostNames[0].equals("") && hostNames[1].equals("")) return;
        testPage.putExtra("localHost", hostNames[0]);
        testPage.putExtra("externalHost", hostNames[1]);
        startActivity(testPage);
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
}
