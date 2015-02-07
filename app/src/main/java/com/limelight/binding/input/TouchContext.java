package com.limelight.binding.input;

import android.graphics.Point;

import com.limelight.nvstream.NvConnection;
import com.limelight.nvstream.input.MouseButtonPacket;

public class TouchContext {
    private static final int TAP_MOVEMENT_THRESHOLD = 10;
    private static final int TAP_TIME_THRESHOLD = 250;
    private final NvConnection conn;
    private final int actionIndex;
    private final double xFactor;
    private final double yFactor;
    private final Point screenSize;
    private int lastTouchX = 0;
    private int lastTouchY = 0;
	private int originalTouchX = 0;
	private int originalTouchY = 0;
	private long originalTouchTime = 0;
    private boolean cancelled;
    private boolean calibrating = false;
    private boolean holding = false;

    public TouchContext(NvConnection conn, int actionIndex, double xFactor, double yFactor, Point screenSize) {
        this.conn = conn;
		this.actionIndex = actionIndex;
        this.xFactor = xFactor;
        this.yFactor = yFactor;
        this.screenSize = screenSize;

	}

    public int getActionIndex()
    {
        return actionIndex;
    }
	
	private boolean isTap()
	{
		int xDelta = Math.abs(lastTouchX - originalTouchX);
		int yDelta = Math.abs(lastTouchY - originalTouchY);
		long timeDelta = System.currentTimeMillis() - originalTouchTime;
		
		return xDelta <= TAP_MOVEMENT_THRESHOLD &&
				yDelta <= TAP_MOVEMENT_THRESHOLD &&
				timeDelta <= TAP_TIME_THRESHOLD;
	}
	
	private byte getMouseButtonIndex()
	{
		if (actionIndex == 1) {
			return MouseButtonPacket.BUTTON_RIGHT;
		}
		else {
			return MouseButtonPacket.BUTTON_LEFT;
		}
	}
	
	public boolean touchDownEvent(int eventX, int eventY)
	{
        moveEvent(eventX, eventY);
        originalTouchX = lastTouchX = eventX;
        originalTouchY = lastTouchY = eventY;
		originalTouchTime = System.currentTimeMillis();
        cancelled = false;

        return true;
	}
	
	public void touchUpEvent(int eventX, int eventY)
	{
        byte buttonIndex = getMouseButtonIndex();
        if (cancelled) {
            return;
        }

		if (isTap())
		{
			// Lower the mouse button
			conn.sendMouseButtonDown(buttonIndex);
			
			// We need to sleep a bit here because some games
			// do input detection by polling
			try {
				Thread.sleep(100);
			} catch (InterruptedException ignored) {}
			
			// Raise the mouse button
			conn.sendMouseButtonUp(buttonIndex);
        } else if (calibrating) {
            calibrating = false;
            lastTouchX = Math.round(screenSize.x / 2);
            lastTouchY = Math.round(screenSize.y / 2) - 10;
        } else if (holding) {
            conn.sendMouseButtonUp(buttonIndex);
            holding = false;
        }
    }

    public boolean touchMoveEvent(int eventX, int eventY)
    {
        if (calibrating) {
            moveEvent(eventX, eventY);
        } else if ((eventX <= screenSize.x + 100 && eventX >= screenSize.x - 100) && (eventY <= screenSize.y + 100 && eventY >= screenSize.y - 100)) {
            calibrating = true;
        } else {
            if (holding) {
                moveEvent(eventX, eventY);
            } else {
                byte buttonIndex = getMouseButtonIndex();
                conn.sendMouseButtonDown(buttonIndex);
                holding = true;
            }
        }
        return true;
    }

    private boolean moveEvent(int eventX, int eventY) {
        if (eventX != lastTouchX || eventY != lastTouchY) {
            // We only send moves for the primary touch point
            if (actionIndex == 0) {
                int deltaX = eventX - lastTouchX;
                int deltaY = eventY - lastTouchY;

                // Scale the deltas based on the factors passed to our constructor
                deltaX = (int)Math.round((double)deltaX * xFactor);
                deltaY = (int)Math.round((double)deltaY * yFactor);

                conn.sendMouseMove((short) deltaX, (short) deltaY);
            }

            lastTouchX = eventX;
            lastTouchY = eventY;
        }

        return true;
    }

    public void cancelTouch() {
        cancelled = true;
    }

    public boolean isCancelled() {
        return cancelled;
    }
}
