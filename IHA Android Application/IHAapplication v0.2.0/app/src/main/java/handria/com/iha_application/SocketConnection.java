package handria.com.iha_application;

import android.content.Context;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import static android.content.Context.WIFI_SERVICE;

/**
 * Created by michaelhandria on 4/8/18.
 */

public class SocketConnection extends AsyncTask<String, String, String[]> {

    private onComplete then;
    WeakReference<Context> _mContextRef;
    private FrameLayout layout;
    private ProgressBar loadBar;
    private TextView txtProgress;
    private LinearLayout homeView;
    private final int EOT = 4;
    private final int ACK = 6;

    SocketConnection(onComplete _then, Context _context, FrameLayout _frame, ProgressBar _prog, TextView _stat, LinearLayout _rootView){
        then = _then;
        _mContextRef = new WeakReference<>(_context);
        layout = _frame;
        loadBar = _prog;
        txtProgress = _stat;
        homeView = _rootView;
    }

    @Override
    protected void onCancelled(String... result){
        homeView.setVisibility(View.VISIBLE);
        layout.setBackgroundColor(Color.TRANSPARENT);
        loadBar.setVisibility(View.INVISIBLE);
        txtProgress.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onPreExecute(){
        loadBar.setVisibility(View.VISIBLE);
        loadBar.animate();
        txtProgress.setVisibility(View.VISIBLE);
        txtProgress.setText("Loading...");
        homeView.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onPostExecute(String[] res){
        homeView.setVisibility(View.VISIBLE);
        layout.setBackgroundColor(Color.TRANSPARENT);
        loadBar.setVisibility(View.INVISIBLE);
        txtProgress.setVisibility(View.INVISIBLE);
        then.onConnectAttempt(res);
    }

    @Override
    protected void onProgressUpdate(String... disp){
        txtProgress.setText(disp[0]);
    }

    @Override
    protected String[] doInBackground(String... params){
        int port = Integer.parseInt(params[1]);
        String cmd = params[2];
        String[] hostNames;
        ArrayList<String> response = new ArrayList<>();
        response.add("INVALID");
        response.add("INVALID");
        if(params[0].equals("true")){
            hostNames = new String[] {params[3], params[4]};
            String _hostName = testConnection(hostNames, port);
            if(_hostName.equals("")){
                hostNames = findIp(port);
                _hostName = testConnection(hostNames, port);
            }
            response.set(0, hostNames[0]);
            response.set(1, hostNames[1]);
            response.add(cmd);
            ArrayList<String> res = sendCmd(_hostName, port, cmd);
            if(res.size() > 0 && res.get(0).equals("INVALID CONNECTION")){
                response.set(0, "INVALID");
                response.set(1, "INVALID");
            }else {
                response.addAll(res);
            }
        }else{
            hostNames = findIp(port);
            response.set(0, hostNames[0]);
            response.set(1, hostNames[1]);
            response.add(cmd);
            String _hostName = testConnection(hostNames, port);
            response.addAll(sendCmd(_hostName, port, cmd));
        }
        return response.toArray(new String[response.size()]);
    }



    private String testConnection(String[] hostNames, int port){
        publishProgress("Testing Connection");
        String correctHost = "";
        //nested try catch to attempt socket connection.
        try {
            publishProgress("First Testing "+hostNames[0]);
            InetAddress address = InetAddress.getByName(hostNames[0]);
            boolean reachable = address.isReachable(1000);
            if(reachable)
                correctHost = hostNames[0];
            else
                throw new Exception();
        }catch(Exception locSocket){
            Log.e("Socket Attempt", "local host socket failed");
            try{
                publishProgress("Second Testing "+hostNames[1]);
                InetAddress address = InetAddress.getByName(hostNames[0]);
                boolean reachable = address.isReachable(1000);
                if(reachable)
                    correctHost = hostNames[1];
                else
                    throw new Exception();
            }catch(Exception extSocket){
                Log.e("Socket Attempt", "external host socket failed");
            }
        }
        return correctHost;
    }

    private ArrayList<String> sendCmd(String hostName, int port, String cmd){
        publishProgress("Sending Commands: "+cmd);
        ArrayList<String> res = new ArrayList<>();

        if(cmd.equals("")){
            res.add("No CMD sent");
            return res;
        }

        try {
            Socket socket = new Socket(hostName, port);
            BufferedReader buffRead = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            if(cmd.equals("getSongList")) {
                boolean finishTx = false;
                while (!finishTx) {
                    out.println(cmd);
                    publishProgress("Waiting for Response...("+cmd+")");
                    long endTimeMillis = System.currentTimeMillis() + 500;

                    while(!buffRead.ready()){
                        if(System.currentTimeMillis() >= endTimeMillis){
                            socket = new Socket(hostName, port);
                            buffRead = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                            out = new PrintWriter(socket.getOutputStream(), true);
                            out.println(cmd);
                            break;
                        }
                        if(this.isCancelled()) return res;
                    }

                    String currentData = buffRead.readLine();
                    StringBuilder songName = new StringBuilder();
                    boolean endOfSong = false;
                    boolean endOfSongDetect = endOfSong;
                    for(char c: currentData.toCharArray()){
                        if(c == ACK) continue;
                        finishTx = c == EOT;
                        //check endSong in the data String
                        //determine when the nextSongInfo starts up again.
                        endOfSong = (c == 29 || c == 0);

                        //refresh string builder
                        //save the song derived from the data String.
                        if(!endOfSongDetect && endOfSong){
                            if(songName.length() != 0) res.add(songName.toString());
                            songName = new StringBuilder();
                        }

                        //if the end of the song has not been reached
                        //keep appending to string builder.
                        if(!endOfSong){
                            songName.append(c);
                        }
                        endOfSongDetect = endOfSong;

                    }
                }
            }else {
                out.println(cmd);
                publishProgress("Waiting for Response...(" + cmd + ")");
                if(!cmd.equals("stat")) {
                    long endTimeMillis = System.currentTimeMillis() + 500;
                    while (!buffRead.ready()) {
                        if(System.currentTimeMillis() >= endTimeMillis){
                            socket = new Socket(hostName, port);
                            buffRead = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                            out = new PrintWriter(socket.getOutputStream(), true);
                            out.println(cmd);
                            break;
                        }
                        if (this.isCancelled()) return res;
                    }
                    //int ResponseCheck = buffRead.read();
                    if (buffRead.read() == ACK) {
                        res.add("true");
                        res.add(buffRead.readLine());
                    } else {
                        res.add("false");
                        res.add(buffRead.readLine());

                    }
                }
            }
            socket.close();
            publishProgress("Done Sending Commands");
        }catch(Exception e){
            Log.e("Sending Cmd", "Something went wrong when trying to send a command");
            res.add("INVALID CONNECTION");
        }
        return res;
    }

    /**
     * Ping sweep function.
     * this function will ping all the available subnets in the area with port: 14123
     * once a socket is reachable with port 14123, then return the local ip name address
     * along with the external ip ( if available )
     * @param port - 14123
     * @return  String[] - local ip address, external ip address, (respectively)
     */
    private String[] findIp(int port){
        publishProgress("Ping Sweep");
        String hostName = "INVALID";
        String externalHostName = "INVALID";
        String[] names = {hostName, externalHostName};
        try {
            Context _context = _mContextRef.get();
            WifiManager wm = (WifiManager) _context.getApplicationContext().getSystemService(WIFI_SERVICE);
            String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
            String _prefix = ip.substring(0, ip.lastIndexOf('.') + 1);

            for (int i = 0; i < 255; i++) {
                //when running on an virtual machine device, comment the one below
                //this and uncomment the other testIp.
                String testIp = _prefix + String.valueOf(i);
                if(this.isCancelled()){
                    return names;
                }
                //String testIp = "192.168.1."+String.valueOf(i);
                publishProgress("Pinging: "+testIp);
                InetAddress address = InetAddress.getByName(testIp);
                boolean reachable = address.isReachable(1000);
                if (reachable) {
                    try {
                        Socket _test = new Socket();
                        _test.connect(new InetSocketAddress(testIp, port), 1000);
                        hostName = testIp;
                        _test.close();
                        externalHostName = getExternalHostName(hostName, port);
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
        String[] _names = {hostName, externalHostName};
        return _names;
    }

    private String getExternalHostName(String hostName, int port){
        ArrayList<String> response = sendCmd(hostName, port, "getExtIP");
        if(response.get(0).equals("true")){
            return response.get(1);
        }
        return "INVALID";
    }


}
