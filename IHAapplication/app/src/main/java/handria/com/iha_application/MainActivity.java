package handria.com.iha_application;


import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import java.net.Socket;
import java.io.Writer;
import java.io.OutputStream;
import java.io.*;
import java.io.PrintWriter;

import handria.com.iha_application.Network;


public class MainActivity extends AppCompatActivity {

    private Socket mSocket;
    private static final String TAG = "MAIN ACTIVITY";
    private EditText textEdit;
    private EditText iNetAddrs;
    private EditText portVal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);
        textEdit = (EditText) findViewById(R.id.stringSend);
        iNetAddrs = (EditText) findViewById(R.id.inetAddrs);
        portVal = (EditText) findViewById(R.id.portNum);
    }

    public void clickConnect(View v) {
        try{
            mSocket = new Socket(iNetAddrs.getText().toString(), Integer.parseInt(portVal.getText().toString()));
            Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
        }catch(Exception e){
            String n = "ERROR\n"+iNetAddrs.getText().toString()+":"+portVal.getText().toString();
            Toast.makeText(this, n, Toast.LENGTH_SHORT).show();
            Log.e(TAG, e.toString());
        }
    }

    public void clickDisconect(View v){
        try{
            mSocket.close();
        }catch (Exception e){
            Log.e(TAG, e.toString());
        }
    }

    public void advertiseConsole(View view) {
        Log.d(TAG, textEdit.getText().toString());
        try {
            String str = textEdit.getText().toString();
            textEdit.setText("");
            BufferedReader br = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
            PrintWriter pw = new PrintWriter(mSocket.getOutputStream(), true);
            pw.println(str);
        }catch(Exception e){
            Log.e(TAG, e.toString());
        }
    }
}
