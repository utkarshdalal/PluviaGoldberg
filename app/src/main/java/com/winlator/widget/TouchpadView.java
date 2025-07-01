package com.winlator.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Handler;
import android.util.Log;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.PointerIcon;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import app.gamenative.R;
import com.winlator.core.AppUtils;
import com.winlator.math.Mathf;
import com.winlator.math.XForm;
import com.winlator.renderer.ViewTransformation;
import com.winlator.winhandler.MouseEventFlags;
import com.winlator.winhandler.WinHandler;
import com.winlator.xserver.Pointer;
import com.winlator.xserver.ScreenInfo;
import com.winlator.xserver.XServer;

public class TouchpadView extends View implements View.OnCapturedPointerListener {
    private static final byte MAX_FINGERS = 4;
    private static final short MAX_TWO_FINGERS_SCROLL_DISTANCE = 350;
    public static final byte MAX_TAP_TRAVEL_DISTANCE = 10;
    public static final short MAX_TAP_MILLISECONDS = 200;
    public static final float CURSOR_ACCELERATION = 1.5f;
    public static final byte CURSOR_ACCELERATION_THRESHOLD = 6;
    private Finger fingerPointerButtonLeft;
    private Finger fingerPointerButtonRight;
    private final Finger[] fingers;
    private Runnable fourFingersTapCallback;
    private boolean moveCursorToTouchpoint;
    private byte numFingers;
    private boolean pointerButtonLeftEnabled;
    private boolean pointerButtonRightEnabled;
    private float scrollAccumY;
    private boolean scrolling;
    private float sensitivity;
    private final XServer xServer;
    private final float[] xform;

    public TouchpadView(Context context, XServer xServer, boolean capturePointerOnExternalMouse) {
        super(context);
        this.fingers = new Finger[4];
        this.numFingers = (byte) 0;
        this.sensitivity = 1.0f;
        this.pointerButtonLeftEnabled = true;
        this.pointerButtonRightEnabled = true;
        this.moveCursorToTouchpoint = false;
        this.scrollAccumY = 0.0f;
        this.scrolling = false;
        this.xform = XForm.getInstance();
        this.xServer = xServer;
        setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setBackground(createTransparentBackground());
        setClickable(true);
        setFocusable(true);
        setFocusableInTouchMode(false);
        int screenWidth = AppUtils.getScreenWidth();
        int screenHeight = AppUtils.getScreenHeight();
        ScreenInfo screenInfo = xServer.screenInfo;
        updateXform(screenWidth, screenHeight, screenInfo.width, screenInfo.height);
        if (capturePointerOnExternalMouse) {
            setOnCapturedPointerListener(this);
            setOnClickListener(new View.OnClickListener() { // from class: com.winlator.widget.TouchpadView$$ExternalSyntheticLambda0
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    requestPointerCapture();
                }
            });
        }
    }

    private static StateListDrawable createTransparentBackground() {
        StateListDrawable stateListDrawable = new StateListDrawable();
        ColorDrawable focusedDrawable = new ColorDrawable(0);
        ColorDrawable defaultDrawable = new ColorDrawable(0);
        stateListDrawable.addState(new int[]{android.R.attr.state_focused}, focusedDrawable);
        stateListDrawable.addState(new int[0], defaultDrawable);
        return stateListDrawable;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        ScreenInfo screenInfo = this.xServer.screenInfo;
        updateXform(w, h, screenInfo.width, screenInfo.height);
    }

    private void updateXform(int outerWidth, int outerHeight, int innerWidth, int innerHeight) {
        ViewTransformation viewTransformation = new ViewTransformation();
        viewTransformation.update(outerWidth, outerHeight, innerWidth, innerHeight);
        float invAspect = 1.0f / viewTransformation.aspect;
        if (!this.xServer.getRenderer().isFullscreen()) {
            XForm.makeTranslation(this.xform, -viewTransformation.viewOffsetX, -viewTransformation.viewOffsetY);
            XForm.scale(this.xform, invAspect, invAspect);
        } else {
            XForm.makeScale(this.xform, invAspect, invAspect);
        }
    }

    private class Finger {
        private int lastX;
        private int lastY;
        private final int startX;
        private final int startY;
        private final long touchTime;
        private int x;
        private int y;

        public Finger(float x, float y) {
            float[] transformedPoint = XForm.transformPoint(TouchpadView.this.xform, x, y);
            int i = (int) transformedPoint[0];
            this.lastX = i;
            this.startX = i;
            this.x = i;
            int i2 = (int) transformedPoint[1];
            this.lastY = i2;
            this.startY = i2;
            this.y = i2;
            this.touchTime = System.currentTimeMillis();
        }

        public void update(float x, float y) {
            this.lastX = this.x;
            this.lastY = this.y;
            float[] transformedPoint = XForm.transformPoint(TouchpadView.this.xform, x, y);
            this.x = (int)transformedPoint[0];
            this.y = (int)transformedPoint[1];
        }

        public int deltaX() {
            float dx = (this.x - this.lastX) * TouchpadView.this.sensitivity;
            if (Math.abs(dx) > CURSOR_ACCELERATION_THRESHOLD) dx *= CURSOR_ACCELERATION;
            return Mathf.roundPoint(dx);
        }

        public int deltaY() {
            float dy = (this.y - this.lastY) * TouchpadView.this.sensitivity;
            if (Math.abs(dy) > CURSOR_ACCELERATION_THRESHOLD) dy *= CURSOR_ACCELERATION;
            return Mathf.roundPoint(dy);
        }

        public boolean isTap() {
            return (System.currentTimeMillis() - touchTime) < MAX_TAP_MILLISECONDS && travelDistance() < MAX_TAP_TRAVEL_DISTANCE;
        }

        public float travelDistance() {
            return (float) Math.hypot(this.x - this.startX, this.y - this.startY);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int actionIndex = event.getActionIndex();
        int pointerId = event.getPointerId(actionIndex);
        int actionMasked = event.getActionMasked();
        if (pointerId >= MAX_FINGERS) return true;

        switch (actionMasked) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                if (event.isFromSource(InputDevice.SOURCE_MOUSE)) return true;
                this.scrollAccumY = 0;
                this.scrolling = false;
                this.fingers[pointerId] = new Finger(event.getX(actionIndex), event.getY(actionIndex));
                this.numFingers = (byte) (this.numFingers + 1);
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                Finger[] fingerArr = this.fingers;
                if (fingerArr[pointerId] != null) {
                    fingerArr[pointerId].update(event.getX(actionIndex), event.getY(actionIndex));
                    handleFingerUp(this.fingers[pointerId]);
                    this.fingers[pointerId] = null;
                    this.numFingers = (byte) (this.numFingers - 1);
                }
                return true;
            case MotionEvent.ACTION_MOVE:
                if (event.isFromSource(InputDevice.SOURCE_MOUSE)) {
                    float[] transformedPoint = XForm.transformPoint(this.xform, event.getX(), event.getY());
                    if (isEnabled()) {
                        this.xServer.injectPointerMove((int) transformedPoint[0], (int) transformedPoint[1]);
                    }
                } else {
                    for (byte i = 0; i < MAX_FINGERS; i= (byte) (i + 1)) {
                        if (this.fingers[i] != null) {
                            int pointerIndex = event.findPointerIndex(i);
                            if (pointerIndex >= 0) {
                                fingers[i].update(event.getX(pointerIndex), event.getY(pointerIndex));
                                handleFingerMove(this.fingers[i]);
                            } else {
                                handleFingerUp(this.fingers[i]);
                                this.fingers[i] = null;
                                this.numFingers = (byte) (this.numFingers - 1);
                            }
                        }
                    }
                }
                return true;
            case MotionEvent.ACTION_CANCEL:
                for (byte i2 = 0; i2 < MAX_FINGERS; i2 = (byte) (i2 + 1)) {
                    this.fingers[i2] = null;
                }
                this.numFingers = (byte) 0;
                return true;
            case MotionEvent.ACTION_OUTSIDE:
            default:
                return true;
        }
    }

    private void handleFingerUp(Finger finger1) {
        switch (this.numFingers) {
            case 1:
                if (finger1.isTap()) {
                    if (this.moveCursorToTouchpoint) {
                        this.xServer.injectPointerMove(finger1.x, finger1.y);
                }
                    pressPointerButtonLeft(finger1);
                    break;
                }
                break;
            case 2:
                Finger finger2 = findSecondFinger(finger1);
                if (finger2 != null && finger1.isTap()) {
                    pressPointerButtonRight(finger1);
                    break;
                }
                break;
            case 4:
                if (this.fourFingersTapCallback != null) {
                    for (byte i = 0; i < 4; i = (byte) (i + 1)) {
                        Finger[] fingerArr = this.fingers;
                        if (fingerArr[i] != null && !fingerArr[i].isTap()) {
                            return;
                    }
                }
                this.fourFingersTapCallback.run();
                break;
            }
            break;
        }
        releasePointerButtonLeft(finger1);
        releasePointerButtonRight(finger1);
    }

    private void handleFingerMove(Finger finger1) {
        byte b;
        if (isEnabled()) {
            boolean skipPointerMove = false;
            Finger finger2 = this.numFingers == 2 ? findSecondFinger(finger1) : null;
            if (finger2 != null) {
                ScreenInfo screenInfo = this.xServer.screenInfo;
                float resolutionScale = 1000.0f / Math.min((int) screenInfo.width, (int) screenInfo.height);
                float currDistance = ((float) Math.hypot(finger1.x - finger2.x, finger1.y - finger2.y)) * resolutionScale;
                if (currDistance < MAX_TWO_FINGERS_SCROLL_DISTANCE) {
                    float f = this.scrollAccumY + (((finger1.y + finger2.y) * 0.5f) - ((finger1.lastY + finger2.lastY) * 0.5f));
                    this.scrollAccumY = f;
                    if (f < -100.0f) {
                        XServer xServer = this.xServer;
                        Pointer.Button button = Pointer.Button.BUTTON_SCROLL_DOWN;
                        xServer.injectPointerButtonPress(button);
                        this.xServer.injectPointerButtonRelease(button);
                        this.scrollAccumY = 0.0f;
                    } else if (f > 100.0f) {
                        XServer xServer2 = this.xServer;
                        Pointer.Button button2 = Pointer.Button.BUTTON_SCROLL_UP;
                        xServer2.injectPointerButtonPress(button2);
                        this.xServer.injectPointerButtonRelease(button2);
                        this.scrollAccumY = 0.0f;
                    }
                    scrolling = true;
                } else if (currDistance >= MAX_TWO_FINGERS_SCROLL_DISTANCE && !this.xServer.pointer.isButtonPressed(Pointer.Button.BUTTON_LEFT) && finger2.travelDistance() < MAX_TAP_TRAVEL_DISTANCE) {
                    pressPointerButtonLeft(finger1);
                    skipPointerMove = true;
                }
            }
            if (!this.scrolling && (b = this.numFingers) <= 2 && !skipPointerMove) {
                if (!this.moveCursorToTouchpoint || b != 1) {
                int dx = finger1.deltaX();
                int dy = finger1.deltaY();
                    WinHandler winHandler = this.xServer.getWinHandler();
                    if (this.xServer.isRelativeMouseMovement()) {
                        winHandler.mouseEvent(MouseEventFlags.MOVE, dx, dy, 0);
                        return;
                    } else {
                        this.xServer.injectPointerMoveDelta(dx, dy);
                        return;
                    }
                }
                this.xServer.injectPointerMove(finger1.x, finger1.y);
            }
        }
    }

    private Finger findSecondFinger(Finger finger) {
        for (byte i = 0; i < MAX_FINGERS; i++) {
            Finger[] fingerArr = this.fingers;
            if (fingerArr[i] != null && fingerArr[i] != finger) {
                return fingerArr[i];
            }
        }
        return null;
    }

    private void pressPointerButtonLeft(Finger finger) {
        if (isEnabled() && this.pointerButtonLeftEnabled) {
            Pointer pointer = this.xServer.pointer;
            Pointer.Button button = Pointer.Button.BUTTON_LEFT;
            if (!pointer.isButtonPressed(button)) {
                this.xServer.injectPointerButtonPress(button);
                this.fingerPointerButtonLeft = finger;
            }
        }
    }

    private void pressPointerButtonRight(Finger finger) {
        if (isEnabled() && this.pointerButtonRightEnabled) {
            Pointer pointer = this.xServer.pointer;
            Pointer.Button button = Pointer.Button.BUTTON_RIGHT;
            if (!pointer.isButtonPressed(button)) {
                this.xServer.injectPointerButtonPress(button);
                this.fingerPointerButtonRight = finger;
            }
        }
    }

    private void releasePointerButtonLeft(Finger finger) {
        if (isEnabled() && this.pointerButtonLeftEnabled && finger == this.fingerPointerButtonLeft && this.xServer.pointer.isButtonPressed(Pointer.Button.BUTTON_LEFT)) {
            postDelayed(() -> {
                xServer.injectPointerButtonRelease(Pointer.Button.BUTTON_LEFT);
                fingerPointerButtonLeft = null;
            }, 30);
        }
    }

    private void releasePointerButtonRight(Finger finger) {
        if (isEnabled() && this.pointerButtonRightEnabled && finger == this.fingerPointerButtonRight && this.xServer.pointer.isButtonPressed(Pointer.Button.BUTTON_RIGHT)) {
            postDelayed(() -> {
                xServer.injectPointerButtonRelease(Pointer.Button.BUTTON_RIGHT);
                fingerPointerButtonRight = null;
            }, 30);
        }
    }

    public void setSensitivity(float sensitivity) {
        this.sensitivity = sensitivity;
    }

    public void setPointerButtonLeftEnabled(boolean pointerButtonLeftEnabled) {
        this.pointerButtonLeftEnabled = pointerButtonLeftEnabled;
    }

    public void setPointerButtonRightEnabled(boolean pointerButtonRightEnabled) {
        this.pointerButtonRightEnabled = pointerButtonRightEnabled;
    }

    public void setFourFingersTapCallback(Runnable fourFingersTapCallback) {
        this.fourFingersTapCallback = fourFingersTapCallback;
    }

    public void setMoveCursorToTouchpoint(boolean moveCursorToTouchpoint) {
        this.moveCursorToTouchpoint = moveCursorToTouchpoint;
    }

    public boolean onExternalMouseEvent(MotionEvent event) {
        if (!isEnabled() || !event.isFromSource(InputDevice.SOURCE_MOUSE)) {
            return false;
        }
        int actionButton = event.getActionButton();
        switch (event.getAction()) {
            case MotionEvent.ACTION_HOVER_MOVE:
                float[] transformedPoint = XForm.transformPoint(this.xform, event.getX(), event.getY());
                this.xServer.injectPointerMove((int)transformedPoint[0], (int)transformedPoint[1]);
                return true;
            case MotionEvent.ACTION_SCROLL:
                float scrollY = event.getAxisValue(MotionEvent.AXIS_VSCROLL);
                if (scrollY <= -1.0f) {
                    XServer xServer = this.xServer;
                    Pointer.Button button = Pointer.Button.BUTTON_SCROLL_DOWN;
                    xServer.injectPointerButtonPress(button);
                    this.xServer.injectPointerButtonRelease(button);
                }
                else if (scrollY >= 1.0f) {
                    XServer xServer2 = this.xServer;
                    Pointer.Button button2 = Pointer.Button.BUTTON_SCROLL_UP;
                    xServer2.injectPointerButtonPress(button2);
                    this.xServer.injectPointerButtonRelease(button2);
                }
                return true;
            case MotionEvent.ACTION_HOVER_ENTER:
            case MotionEvent.ACTION_HOVER_EXIT:
            default:
                return false;
            case MotionEvent.ACTION_BUTTON_PRESS:
                if (actionButton == MotionEvent.BUTTON_PRIMARY) {
                    this.xServer.injectPointerButtonPress(Pointer.Button.BUTTON_LEFT);
                } else if (actionButton == MotionEvent.BUTTON_SECONDARY) {
                    this.xServer.injectPointerButtonPress(Pointer.Button.BUTTON_RIGHT);
                }
                return true;
            case MotionEvent.ACTION_BUTTON_RELEASE:
                if (actionButton == MotionEvent.BUTTON_PRIMARY) {
                    this.xServer.injectPointerButtonRelease(Pointer.Button.BUTTON_LEFT);
                } else if (actionButton == MotionEvent.BUTTON_SECONDARY) {
                    this.xServer.injectPointerButtonRelease(Pointer.Button.BUTTON_RIGHT);
                }
                return true;
        }
    }

    public float[] computeDeltaPoint(float lastX, float lastY, float x, float y) {
        float[] result = {0, 0};
        XForm.transformPoint(this.xform, lastX, lastY, result);
        float lastX2 = result[0];
        float lastY2 = result[1];
        XForm.transformPoint(this.xform, x, y, result);
        float x2 = result[0];
        float y2 = result[1];
        result[0] = x2 - lastX2;
        result[1] = y2 - lastY2;
        return result;
    }

    @Override // android.view.View.OnCapturedPointerListener
    public boolean onCapturedPointer(View view, MotionEvent event) {
        if (event.getAction() == 2) {
            float dx = event.getX() * this.sensitivity;
            if (Math.abs(dx) > 6.0f) {
                dx *= CURSOR_ACCELERATION;
            }
            float dy = event.getY() * this.sensitivity;
            if (Math.abs(dy) > 6.0f) {
                dy *= CURSOR_ACCELERATION;
            }
            this.xServer.injectPointerMoveDelta(Mathf.roundPoint(dx), Mathf.roundPoint(dy));
            return true;
        }
        event.setSource(event.getSource() | 8194);
        return onExternalMouseEvent(event);
    }
}
