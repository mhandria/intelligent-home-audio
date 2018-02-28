package handria.com.iha_application;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class HomePage extends AppCompatActivity implements onComplete{

    String hostname;
    String portnumber;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

    }


    @Override
    public void onConnectAttempt(String status){
        Toast.makeText(this, status, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTaskComplete(){
        //TODO: not used unless needed for other features.
    }

    public void playB(View view) {
        SocketConnection _connection = new SocketConnection(this);
        _connection.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, hostname, portnumber, "play a");

    }

    public void playA(View view){
        SocketConnection _connection = new SocketConnection(this);
        _connection.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, hostname, portnumber, "play b");
    }

    public void updateHostPort(){
        hostname = ((EditText)findViewById(R.id.hostname)).getText().toString();
        portnumber = ((EditText)findViewById(R.id.portnumber)).getText().toString();
    }
}
