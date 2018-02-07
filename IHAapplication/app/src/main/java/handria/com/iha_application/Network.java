package handria.com.iha_application;

import android.os.AsyncTask;

import java.net.Socket;

/**
 * Created by Handria on 2/4/18.
 */

public class Network extends AsyncTask<String, Void, Socket>{
    Exception e;

    protected Socket doInBackground(String... args){
        try {
            Socket mSocket = new Socket("192.168.1.131", 14124);
            return mSocket;
        }catch(Exception e){
            this.e = e;
            return null;
        }
    }

}
