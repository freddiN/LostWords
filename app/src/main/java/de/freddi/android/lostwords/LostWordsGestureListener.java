package de.freddi.android.lostwords;

import android.graphics.Point;
import android.view.GestureDetector;
import android.view.MotionEvent;

import de.freddi.android.lostwords.words.IndexType;

/**
 * Created by freddi on 09.04.2016.
 * 
 * gestures detector logic
 */
class LostWordsGestureListener extends GestureDetector.SimpleOnGestureListener {
    private final MainActivity m_mainActivity;

    /**
     * 
     * @param main mainactivity
     */
    public LostWordsGestureListener(final MainActivity main) {
        this.m_mainActivity = main;
    }

    /** always true! otherwise upcoming gestures will get ignored */
    @Override
    public boolean onDown(MotionEvent event) {
        return true;
    }

    /** detect swipe-left and swipe-right, which results in the same action as a button press */
    @Override
    public boolean onFling(final MotionEvent event1, final MotionEvent event2, final float velocityX, final float velocityY) {
        /** ignore gestures when drawer is open */
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

    /** detect double taps. ignore when tapped on a button, during a shake or drawer is open */
    @Override
    public boolean onDoubleTap(final MotionEvent e) {
        /** ignore double taps during a shake or opened drawer */
        if (m_mainActivity.m_isSensorCheck.get() || m_mainActivity.isDrawerOpen()) {
            return true;
        }

        final Point pTouch = new Point((int)e.getAxisValue(0), (int)e.getAxisValue(1));
        if (!Helper.isTouchWithinButtons(pTouch,
                        m_mainActivity.findViewById(R.id.fab_speak),
                        m_mainActivity.findViewById(R.id.fab_fav),
                        m_mainActivity.findViewById(R.id.fab_own),
                        m_mainActivity.findViewById(R.id.buttonPrev),
                        m_mainActivity.findViewById(R.id.buttonNext),
                        m_mainActivity.findViewById(R.id.search))
                ) {
            m_mainActivity.newWordAndUpdateView(IndexType.RANDOM);
        }

        return true;
    }
}
