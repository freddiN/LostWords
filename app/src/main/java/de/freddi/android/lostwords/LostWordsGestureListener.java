package de.freddi.android.lostwords;

import android.graphics.Point;
import android.view.GestureDetector;
import android.view.MotionEvent;

/**
 * Gesture-Detector Logik
 *
 * Created by freddi on 09.04.2016.
 */
class LostWordsGestureListener extends GestureDetector.SimpleOnGestureListener {
    private final MainActivity m_mainActivity;

    public LostWordsGestureListener(final MainActivity main) {
        this.m_mainActivity = main;
    }

    /** immer true, sonst werden nachfolgende Gestures ignoriert */
    @Override
    public boolean onDown(MotionEvent event) {
        return true;
    }

    /** Swipe-Links und -rechts erkennen, dann selbe Aktion wie mit den Buttons */
    @Override
    public boolean onFling(final MotionEvent event1, final MotionEvent event2, final float velocityX, final float velocityY) {
        /** keine Gestures wenn der Drawer offen ist */
        if (m_mainActivity.isDrawerOpen()) {
            return true;
        }

        final int nGestureMinimumSpeed = m_mainActivity.getResources().getInteger(R.integer.gesture_min_speed);
        if (velocityX > nGestureMinimumSpeed) {
            m_mainActivity.buttonPrev(null);
        } else if (velocityX < -nGestureMinimumSpeed) {
            m_mainActivity.buttonNext(null);
        }

        return true;
    }

    /** Doule-Tap Erkennung, aber über den Buttons und bei offenem Drawer ignorieren */
    @Override
    public boolean onDoubleTap(final MotionEvent e) {
        if (m_mainActivity.m_isSensorCheck.get()) {
            return true;
        }

        final Point pTouch = new Point((int)e.getAxisValue(0), (int)e.getAxisValue(1));
        if (!m_mainActivity.isDrawerOpen() &&
                !Helper.isTouchWithinButtons(pTouch,
                        m_mainActivity.findViewById(R.id.fab),
                        m_mainActivity.findViewById(R.id.fab_fav),
                        m_mainActivity.findViewById(R.id.buttonPrev),
                        m_mainActivity.findViewById(R.id.buttonNext),
                        m_mainActivity.findViewById(R.id.search))
                ) {
            m_mainActivity.newWordAndUpdateView(IndexType.RANDOM);
        }

        return true;
    }
}
