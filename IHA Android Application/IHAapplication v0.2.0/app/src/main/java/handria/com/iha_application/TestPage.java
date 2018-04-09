package handria.com.iha_application;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.Console;

public class TestPage extends AppCompatActivity implements onComplete{

    String port = "14123";
    String localHost = "";
    String externalHost = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_page);
        SocketConnection _connection = new SocketConnection(this, this);
        Intent testIntent = getIntent();
        localHost = testIntent.getStringExtra("localHost");
        externalHost = testIntent.getStringExtra("externalHost");

    }
    @Override
    public void onConnectAttempt(String[] info){
        Toast.makeText(this, info[2], Toast.LENGTH_SHORT).show();
        for(char c: info[2].toCharArray()){
            Log.d("Debug Response", Character.toString(c));
        }

    }

    @Override
    public void onTaskComplete(){
        //TODO: not used unless needed for other features.
    }


    public void sendData(View view) {
        EditText data = (EditText)findViewById(R.id.commandString);
        SocketConnection _connection = new SocketConnection(this, this);
        _connection.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "true", port, data.getText().toString(), localHost, externalHost);
    }
}
