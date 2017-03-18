/* create a view with the button in the middle that
 * can be used to send positions to an object just
 * like a joystick would.
 */

package org.mburm.brob;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by mbmis006 on 3/3/2017.
 */

public class JoystickView extends View {

    private JoystickListener listener;
    private int x, y;
    private int lastX, lastY;
    private int buttonRadius;
    private int joystickRadius;
    private int centerX;
    private int centerY;

    private Paint pt = new Paint();

    public JoystickView(Context context) {
        super(context);
    }

    public JoystickView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public JoystickView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth()); //square
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (joystickRadius == 0 ) {
            joystickRadius = getWidth()/3;
            buttonRadius = joystickRadius / 2;
            centerX = getWidth() / 2;
            centerY = getHeight() / 2;
            x = centerX;
            y = centerY;
        }
        pt.setStyle(Paint.Style.STROKE);
        pt.setStrokeWidth(3);
        pt.setColor(Color.LTGRAY);
        canvas.drawCircle(centerX, centerY, joystickRadius, pt);
        canvas.drawCircle(centerX, centerY, buttonRadius, pt);

        pt.setColor(Color.BLUE);
        pt.setStyle(Paint.Style.FILL);
        pt.setTextSize(24);
        canvas.drawCircle(x, y, buttonRadius, pt);

        canvas.drawText("X: " + getXValue(), 1, 30, pt);
        canvas.drawText("Y: " + getYValue(), 65, 30, pt);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x1, y1;
        x = (int)event.getX();
        y = (int)event.getY();
        x1 = x - centerX;
        y1 = y - centerY;
        int abs = (int)Math.sqrt(x1 * x1 + y1 * y1);
        if (abs > joystickRadius) {
            x = x1 * joystickRadius / abs + centerX;
            y = y1 * joystickRadius / abs + centerY;
        }

        if (lastX == 0 && lastY == 0) {
            if (Math.abs(getXValue()) > 22 || Math.abs(getYValue()) > 22) {
                x = centerX;
                y = centerY;
                return true;
            }
        }

        lastX = getXValue();
        lastY = getYValue();

        invalidate();

        if (listener == null)
            return false;

        int actionMask = event.getActionMasked();
        if (actionMask == MotionEvent.ACTION_DOWN) {
            listener.setOnTouchListener(getXValue(), getYValue());
            return true;
        }

        if (actionMask == MotionEvent.ACTION_MOVE) {
            listener.setOnMovedListener(getXValue(), getYValue());
            return true;
        }

        if (actionMask == MotionEvent.ACTION_UP) {
            x = centerX;
            y = centerY;
            lastX = 0;
            lastY = 0;
            listener.setOnReleaseListener(0, 0);
            return true;
        }

        if (actionMask == MotionEvent.ACTION_CANCEL) {
            x = centerX;
            y = centerY;
            lastX = 0;
            lastY = 0;
            listener.setOnReleaseListener(0, 0);
            return true;
        }

        return false;
    }

    public int getXValue() {
        int r;

        r = ((x - centerX) * 45 / joystickRadius);
        return r;
    }

    public int getYValue() {
        int r;

        r = ((y - centerY) * 45 /joystickRadius);
        return -r;
    }

    public void setJoystickListener(JoystickListener listener) {
        this.listener = listener;
    }
}
