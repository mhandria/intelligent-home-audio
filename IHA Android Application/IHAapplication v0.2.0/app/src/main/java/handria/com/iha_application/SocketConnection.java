package handria.com.iha_application;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.ProgressBar;

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

public class SocketConnection extends AsyncTask<String, Void, String[]> {

    private onComplete then;
    WeakReference<Context> _mContextRef;
    private final int EOT = 4;
    private final int ACK = 6;
    private final int NAK = 21;

    SocketConnection(onComplete _then, Context _context){
        then = _then;
        _mContextRef = new WeakReference<>(_context);
    }

    @Override
    protected String[] doInBackground(String... params){
        int port = Integer.parseInt(params[1]);
        String cmd = params[2];
        String[] hostNames;
        ArrayList<String> response = new ArrayList<>();
        if(params[0].equals("true")){
            hostNames = new String[] {params[3], params[4]};
            String _hostName = testConnection(hostNames, port);
            if(_hostName.equals("")){
                hostNames = findIp(port);
                _hostName = testConnection(hostNames, port);
            }
            response.add(hostNames[0]);
            response.add(hostNames[1]);
            response.addAll(sendCmd(_hostName, port, cmd));
        }else{
            hostNames = findIp(port);
            response.add(hostNames[0]);
            response.add(hostNames[1]);
            String _hostName = testConnection(hostNames, port);
            response.addAll(sendCmd(_hostName, port, cmd));
        }
        return response.toArray(new String[response.size()]);
    }

    private String testConnection(String[] hostNames, int port){
        Socket socket;
        String correctHost = "";
        //nested try catch to attempt socket connection.
        try {
            socket = new Socket(hostNames[0], port);
            correctHost = hostNames[0];
            socket.close();
        }catch(Exception locSocket){
            Log.e("Socket Attempt", "local host socket failed");
            try{
                socket = new Socket(hostNames[1], port);
                correctHost = hostNames[1];
                socket.close();
            }catch(Exception extSocket){
                Log.e("Socket Attempt", "external host socket failed");
            }
        }
        return correctHost;
    }

    private ArrayList<String> sendCmd(String hostName, int port, String cmd){
        ArrayList<String> res = new ArrayList<>();

        if(cmd.equals("")){
            res.add("No CMD sent");
            return res;
        }

        try {
            Socket socket = new Socket(hostName, port);
            BufferedReader buffRead = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(cmd);
            if(cmd.equals("getSongList")) {
                boolean finishTx = false;
                while (!finishTx) {
                    while(!buffRead.ready());
                    String currentData = buffRead.readLine();
                    StringBuilder songName = new StringBuilder();
                    boolean endOfSong = false;
                    boolean endOfSongDetect = endOfSong;
                    for(char c: currentData.toCharArray()){
                        if(c == ACK) continue;
                        finishTx = c == EOT;
                        //check endSong in the data String
                        //determine when the nextSongInfo starts up again.
                        endOfSong = (c <= 31);

                        //refresh string builder
                        //save the song derived from the data String.
                        if(!endOfSongDetect && endOfSong){
                            res.add(songName.toString());
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
            }else{
                while(!buffRead.ready());
                //int ResponseCheck = buffRead.read();
                if(buffRead.read() == ACK)
                    res.add(buffRead.readLine());
                else
                    res.add(buffRead.readLine());
            }
            socket.close();
        }catch(Exception e){
            Log.e("Sending Cmd", "Something went wrong when trying to send a command");
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
                //String testIp = _prefix + String.valueOf(i);
                String testIp = "192.168.1."+String.valueOf(i);
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
        String[] names = {hostName, externalHostName};
        return names;
    }

    private String getExternalHostName(String hostName, int port){
        return sendCmd(hostName, port, "getExtIP").get(0);
    }

    @Override
    protected void onPostExecute(String[] res){
        then.onConnectAttempt(res);
    }
}
