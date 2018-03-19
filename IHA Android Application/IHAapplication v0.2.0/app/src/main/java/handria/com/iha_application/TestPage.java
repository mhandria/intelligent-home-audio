package handria.com.iha_application;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class TestPage extends AppCompatActivity implements onComplete{

    String port = "14123";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_page);

    }
    @Override
    public void onConnectAttempt(String[] info){
        Toast.makeText(this, info[2], Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTaskComplete(){
        //TODO: not used unless needed for other features.
    }


    public void sendData(View view) {
        EditText data = (EditText)findViewById(R.id.commandString);
        SocketConnection _connection = new SocketConnection(this, this);
        _connection.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, port, data.getText().toString());
    }
}
