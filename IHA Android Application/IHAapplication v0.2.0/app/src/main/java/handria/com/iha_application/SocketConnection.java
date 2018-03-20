package handria.com.iha_application;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Display;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Enumeration;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.WIFI_SERVICE;

/**
 * Created by Handria on 2/27/18.
 */

public class SocketConnection extends AsyncTask<String, Void, String[]> {

    private onComplete then;
    WeakReference<Context> _mContextRef;
    private String fileName;
    private Socket _socket;

    SocketConnection(onComplete then, Context _context){
        this.then = then;
        _mContextRef = new WeakReference<>(_context);
        fileName = "ip.src";
    }

    @Override
    protected String[] doInBackground(String... params){
        String[] info = {"", "", ""};
        try {
            int port = Integer.parseInt(params[0]);
            String msg = params[1];
            String[] hostNames = extractIp();


            if(hostNames[1].equals("INVALID") && hostNames[0].equals("INVALID")){
                hostNames = findIp(port);
                saveIp(hostNames[0], hostNames[1]);
            }

            try {
                if(hostNames[1].equals("Invalid Command")){
                    _socket = new Socket(hostNames[0], port);
                }else {
                    _socket = new Socket(hostNames[1], port);
                    if(!testExternalHost(_socket)){
                        _socket = new Socket(hostNames[0], port);
                    }else{
                        _socket = new Socket(hostNames[1], port);
                    }
                }
            }catch(Exception e){
                hostNames = findIp(port);
                saveIp(hostNames[0], hostNames[1]);
                _socket = new Socket(hostNames[1], port);
                if(!testExternalHost(_socket)){
                    _socket = new Socket(hostNames[0], port);
                }else{
                    _socket = new Socket(hostNames[1], port);
                }
            }
            //send the commands to the server here.
            BufferedReader buffRead = new BufferedReader(new InputStreamReader(_socket.getInputStream()));
            PrintWriter out = new PrintWriter(_socket.getOutputStream(), true);

            out.println(msg);
            while(!buffRead.ready());
            String response = buffRead.readLine();
            _socket.close();
            info[2] = response;
            info[0] = hostNames[0];
            info[1] = hostNames[1];
        }catch(IndexOutOfBoundsException iob){
            Log.e("CONNECTION", "not enough params...");
            info[2] = "error something wrong with connection";
        }catch(IOException e){

            Log.e("CONNECTION", "failed to connect to socket");
            info[2] = "failed to connect";
        }catch(Exception e){

            Log.e("PARAMETER", "failed to grab param");
            info[2] = "failed";
        }
        return info;
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        try {
            if (_socket != null)
                _socket.close();
        }catch(Exception e){
            Log.d("Cancel AsyncTask", "tried closing any sockets out there but exception thrown");
        }
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
            Context _context = _mContextRef.get();
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
            Context _context = _mContextRef.get();
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


    private String[] extractIp(){
        StringBuilder ipName = new StringBuilder();
        StringBuilder externalIpName = new StringBuilder();
        try{
            Context _context = _mContextRef.get();
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

    private String[] findIp(int port){
        String hostName = "INVALID";
        String externalHostName = "INVALID";
        try {
            Context _context = _mContextRef.get();
            WifiManager wm = (WifiManager) _context.getApplicationContext().getSystemService(WIFI_SERVICE);
            String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
            String _prefix = ip.substring(0, ip.lastIndexOf('.') + 1);

            for (int i = 0; i < 255; i++) {
                //when running on an virtual machine device, comment the one below
                //this and uncomment the other testIp.
                String testIp = _prefix + String.valueOf(i);
                //String testIp = "192.168.1."+String.valueOf(i);
                InetAddress address = InetAddress.getByName(testIp);
                boolean reachable = address.isReachable(1000);
                if (reachable) {
                    try {
                        Socket _test = new Socket(testIp, port);
                        hostName = testIp;
                        externalHostName = getExternalHostName(_test);
                        _test.close();
                        break;
                    } catch (Exception e) {
                        continue;
                    }
                }
            }
        }catch(UnknownHostException unknown){
            Log.e("asyncTask.findIp", unknown.toString());
        }catch(IOException io){
            Log.e("asyncTask.findIp", io.toString());
        }
        String[] names = {hostName, externalHostName};
        return names;
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

    private boolean testExternalHost(Socket _socket){
        try {
            BufferedReader buffRead = new BufferedReader(new InputStreamReader(_socket.getInputStream()));
            PrintWriter out = new PrintWriter(_socket.getOutputStream(), true);
            out.println("stat");
            while (!buffRead.ready()) ;
            String response = buffRead.readLine();
            _socket.close();
            return response.equals("OK");
        }catch(IOException e){
            Log.e("testExternalHostName", "No reachable external ip try the other ip");
            return false;
        }
    }

    private String getExternalHostName(Socket _socket){
        try {
            BufferedReader buffRead = new BufferedReader(new InputStreamReader(_socket.getInputStream()));
            PrintWriter out = new PrintWriter(_socket.getOutputStream(), true);
            out.println("getExtIP");
            while (!buffRead.ready()) ;
            String response = buffRead.readLine();
            _socket.close();
            return response;
        }catch(IOException e){
            Log.e("getExternalHostName", "COULDN'T GET EXTERNAL HOST NAME::FATAL ERROR");
            return "";
        }

    }

    @Override
    protected void onPostExecute(String[] info){
        then.onConnectAttempt(info);
    }
}
