package handria.com.iha_application;

import android.graphics.Color;
import android.widget.TextView;

/**
 * Created by michaelhandria on 4/12/18.
 */

public class Speaker {

    public String speakerId;
    public int status;

    public Speaker(String id, int stat){
        speakerId = id;
        status = stat;
    }

    public String getSpeakerId(){
        return speakerId;
    }
    public void toggleStat(){
        status = (status == 1) ? 0: 1;
    }
    public int getStatusColor(){
        if(status == 0){
            return Color.RED;
        }else{
            return Color.GREEN;
        }
    }
}
