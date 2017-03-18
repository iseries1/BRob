package org.mburm.brob;

/**
 * Created by mbmis006 on 3/5/2017.
 */

public interface JoystickListener {

    public void setOnTouchListener(int xValue, int yValue);

    public void setOnMovedListener(int xValue, int yValue);

    public void setOnReleaseListener(int xValue, int yValue);

}
