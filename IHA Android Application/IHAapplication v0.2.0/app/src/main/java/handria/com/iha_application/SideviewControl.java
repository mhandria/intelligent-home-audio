package handria.com.iha_application;

import android.view.View;
import android.view.ViewGroup;

/**
 * Created by michaelhandria on 4/12/18.
 */

public interface SideviewControl {
    void onSideviewOpen(View view);
    void onSideviewClose(View view);
    void populateSpeakerList(Speaker speaker);
}
