package handria.com.iha_application;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;

public class HomePage extends AppCompatActivity implements onComplete{

    String port = "14123";
    ArrayList<String> songList;
    private  onComplete then;

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
        SocketConnection _connection = new SocketConnection(then, this);
        _connection.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, port, "stat");
    }


    @Override
    public void onConnectAttempt(String[] info){
        TextView ipName = (TextView) findViewById(R.id.ipName);
        TextView exIpName = (TextView) findViewById(R.id.exIpName);
        ipName.setText("Local IP NAME: "+info[0]);
        exIpName.setText("External IP NAME: "+info[1]);
        Toast.makeText(this, info[2], Toast.LENGTH_SHORT).show();
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
                    SocketConnection _connection = new SocketConnection(then, view.getContext());
                    _connection.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, port,"play "+title);
                }
            });
            tableView.addView(row);
        }
    }



    public void goCustom(View view) {
        Intent testPage = new Intent(this, TestPage.class);
        startActivity(testPage);
    }
}
