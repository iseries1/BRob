/* Connect to balancing robot and allow moving it around
 * Programmer: Michael Burmeister
 * Date: March 18, 2017
 * Version: 1.0
 * This is free ware an you may use it as you like
 *
 */

package org.mburm.brob;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Message;
import android.os.Process;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class BROBActivity extends AppCompatActivity implements JoystickListener {

    JoystickView jv;
    UdpTask robotRec;
    UdpTask robotSnd;
    float Kp, Ki, Kd;
    float SKp, SKi, SKd;
    static Backgrd bk;
    int xValue, yValue;
    boolean Released;
    int Angle;
    float Adjustment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brob);

        Kp = 0;
        Ki = 0;
        Kd = 0;
        SKp = Kp;
        SKi = Ki;
        SKd = Kd;
        Angle = 0;
        Adjustment = 0;

        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo.isConnected()) {
            robotRec = new UdpTask("UDPR", Process.THREAD_PRIORITY_BACKGROUND, null);
            robotRec.start();
            robotSnd = new UdpTask("UDPS", Process.THREAD_PRIORITY_BACKGROUND, "localhost");
            robotSnd.start();
            robotRec.u = this;
        }

        bk = new Backgrd(this.getMainLooper());
        jv = (JoystickView) findViewById(R.id.joystick);
        jv.setJoystickListener(this);
    }

    @Override
    public void setOnTouchListener(int xValue, int yValue) {
        newPosition(xValue, yValue, false);
    }

    @Override
    public void setOnMovedListener(int xValue, int yValue) {
        newPosition(xValue, yValue, false);
    }

    @Override
    public void setOnReleaseListener(int xValue, int yValue) {
        newPosition(xValue, yValue, true);
    }

    private void newPosition(int x, int y, boolean r)
    {
        if (x == 0 && y == 0)
            r = true;
        Released = r;
        xValue = x;
        yValue = y;
        String d = String.format("X%d,Y%d,", y, x);
        if (robotSnd.Connected()) {
            if (robotRec.getClient() != null)
                robotSnd.setClient(robotRec.getClient());
            robotSnd.Send(d);
        }
    }

    public void doSend(View v)
    {
        String s = null;
        String d = "";

        TextView t = (TextView) findViewById(R.id.Kp);
        s = t.getText().toString();
        Kp = Float.parseFloat(s);
        d = String.format("P%1.4f,", Kp);
        t = (TextView) findViewById(R.id.Ki);
        s = t.getText().toString();
        Ki = Float.parseFloat(s);
        d = d + String.format("I%1.4f,", Ki);
        t = (TextView) findViewById(R.id.Kd);
        s = t.getText().toString();
        Kd = Float.parseFloat(s);
        d = d + String.format("D%1.4f,", Kd);

        if (robotSnd.Connected()) {
            robotSnd.Send(d);
        }
        robotRec.Receive();
    }

    public void update(String s)
    {
        int i, j;

        j = 0;
        for (i=0;i<s.length();i++) {
            if (s.charAt(i) == ',') {
                doParse(s.substring(j, i));
                j = i+1;
            }
        }
        Message msg = bk.obtainMessage(1, this);
        msg.sendToTarget();
    }

    private void doParse(String s)
    {
        float v;
        int i;
        TextView t;

        char c = s.charAt(0);
        s = s.substring(1);
        v = Float.parseFloat(s);
        i = (int)v;
        switch (c)
        {
            case 'P' : Kp = v;
                break;
            case 'I' : Ki = v;
                break;
            case 'D' : Kd = v;
                break;
            case 'S' : Adjustment = v;
                break;
            case 'A' : Angle = i;
                break;
        }
    }

    void refresh()
    {
        TextView t;
        String s;

        if (SKp != Kp) {
            t = (TextView) findViewById(R.id.Kd);
            s = fmt(Kd);
            t.setText(s);
            SKp = Kp;
        }

        if (SKi != Ki) {
            t = (TextView) findViewById(R.id.Ki);
            s = fmt(Ki);
            t.setText(s);
            SKi = Ki;
        }

        if (SKd != Kd) {
            t = (TextView) findViewById(R.id.Kp);
            s = fmt(Kp);
            t.setText(s);
            SKd = Kd;
        }
    }

    String fmt(float v)
    {
        int i;

        String s = String.format("%1.4f", v);
        for (i=s.length()-1;i>0;i--)
        {
            if (s.charAt(i) != '0')
                break;
        }
        s = s.substring(0,i+1);
        return s;
    }
}
