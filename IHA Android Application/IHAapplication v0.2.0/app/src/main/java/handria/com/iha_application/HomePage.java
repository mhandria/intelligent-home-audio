package handria.com.iha_application;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class HomePage extends AppCompatActivity implements onComplete{

    String port = "14123";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        SocketConnection _connection = new SocketConnection(this, this);
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

    public void playB(View view) {
        SocketConnection _connection = new SocketConnection(this, this);
        _connection.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, port, "play b");
    }

    public void playA(View view){
        SocketConnection _connection = new SocketConnection(this, this);
        _connection.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, port, "play a");
    }


    public void goCustom(View view) {
        Intent testPage = new Intent(this, TestPage.class);
        startActivity(testPage);
    }
}
