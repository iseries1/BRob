/* Just a background stub class to handle
 * background events that need to be pushed
 * to the user interface in real time
 */

package org.mburm.brob;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

/**
 * Created by mbmis006 on 3/10/2017.
 */

public class Backgrd extends Handler {
    public Backgrd(Looper looper) {
        super(looper);
    }

    @Override
    public void handleMessage(Message msg) {
        if (msg.what == 1) {
            BROBActivity x = (BROBActivity) msg.obj;
            x.refresh();
            return;
        }
        super.handleMessage(msg);
    }
}
