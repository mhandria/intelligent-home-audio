package handria.com.iha_application;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by Handria on 2/27/18.
 */

public class SocketConnection extends AsyncTask<String, Void, String> {

    private onComplete then;

    SocketConnection(onComplete then){
        this.then = then;
    }

    @Override
    protected String doInBackground(String... params){
        try {
            String host = params[0];
            int port = Integer.parseInt(params[1]);
            String msg = params[2];
            Socket _socket = new Socket(host, port);
            BufferedReader buffRead = new BufferedReader(new InputStreamReader(_socket.getInputStream()));
            PrintWriter out = new PrintWriter(_socket.getOutputStream(), true);
            out.println(msg);
            _socket.close();
            return "success";
        }catch(IndexOutOfBoundsException iob){

            Log.e("CONNECTION", "not enough params...");
            return "error something wrong with connection";
        }catch(IOException e){

            Log.e("CONNECTION", "failed to connect to socket");
            return "failed to connect";
        }catch(Exception e){

            Log.e("PARAMETER", "failed to grab param");
            return "failed";
        }
    }

    @Override
    protected void onPostExecute(String status){
        then.onConnectAttempt(status);
    }
}
