package com.lglf77.library.view;

import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ScrollView;
import android.widget.Scroller;

import androidx.core.view.MotionEventCompat;
import androidx.core.view.VelocityTrackerCompat;

public class ScrollViewPager implements View.OnTouchListener {

    private static final boolean DEBUG = false;
    private static final String TAG = "ScrollViewPager";
    // Velocidade
    private final int mMinVelocity;
    private final int mMaxVelocity;
    private final int mMinAnimationDuration = 400;
    // Rastreamento
    private VelocityTracker mVelocityTracker;
    private float mInitialY = -1;
    private float mLastY = -1;
    // A classe encapsula a rolagem.(Overshoot)
    private Scroller mScroller;
    // O mScrollRunnable torna a visualização de rolagem rolada.
    private Runnable mScrollRunnable;
    private ScrollView mScrollView;
    private ViewGroup mContentView;
    // Estado
    private int mCurrentPage = 0;
    private boolean mIsScrolling = false;


    public ScrollViewPager(final ScrollView scrollView, final ViewGroup contentView) {
        mScrollView = scrollView;
        mContentView = contentView;
        // Configuração
        mScrollView.setOnTouchListener(this);
        mScroller = new Scroller(mScrollView.getContext(), new DecelerateInterpolator(1.f));
        // O Scroller Executável
        mScrollRunnable = new Runnable() {
            @Override
            public void run() {
                if (mScroller.computeScrollOffset())
                    mScrollView.scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
                // Se não terminar, certifique-se de tentar calcular a próxima vez que executar
                if (!mScroller.isFinished())
                    mScrollView.post(this);
                else
                    mIsScrolling = false;
            }
        };

        // Ver configuração
        final ViewConfiguration vc = ViewConfiguration.get(scrollView.getContext());
        mMinVelocity = vc.getScaledMinimumFlingVelocity();
        mMaxVelocity = vc.getScaledMaximumFlingVelocity();
    }

    @Override
    public boolean onTouch(final View v, final MotionEvent ev) {
        if (v != mScrollView) return false;

        final int index = MotionEventCompat.getActionIndex(ev);
        final int action = MotionEventCompat.getActionMasked(ev);
        final int pointerId = MotionEventCompat.getPointerId(ev, index);

        // Comece a rastrear
        if (mVelocityTracker == null)
            mVelocityTracker = VelocityTracker.obtain();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                // Reiniciar tudo
                resetPager();

                // Adicionar movimento
                mVelocityTracker.addMovement(ev);

                // Obtenha os pontos de contato iniciais
                mLastY = mInitialY = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                // Adicionar movimento
                mVelocityTracker.addMovement(ev);
                mLastY = ev.getY();
                break;
            case MotionEvent.ACTION_UP:
                // Adicionar movimento
                mVelocityTracker.addMovement(ev);
                // Calcule a velocidade atual
                mVelocityTracker.computeCurrentVelocity(1000, mMaxVelocity);
                final float velocityY = VelocityTrackerCompat.getYVelocity(mVelocityTracker, pointerId);
                if (DEBUG) Log.d(TAG, String.format("VelocityY [%f]", velocityY));


                // A altura da visualização de rolagem, em pixels
                final int scrollHeight = mScrollView.getHeight();
                final int fullHeight = mContentView.getHeight();
                final int pageHeight = scrollHeight;

                // O topo da última página, em pixels.
                final int lastPageTopY = fullHeight - pageHeight;
                // A posição superior rolada da visualização de rolagem, em pixels.
                final int currScrollY = getCurrentScrollY();
                // A página atual que estamos vendo ao deixar de lado a visualização da rolagem
                mCurrentPage = calculateCurrentPage(scrollHeight, currScrollY);
                if (DEBUG) Log.v(TAG, String.format("CurrentPage [%d]", mCurrentPage));


                mIsScrolling = true;
                // Verifique não rolar para além da primeira / última página
                if (getCurrentScrollY() < 0)
                    snapToPage(0, 0);
                else if (getCurrentScrollY() > lastPageTopY)
                    snapToPage(getPageCount() - 1, 0);
                else {

                    // A posição intermediária rolada da visualização de rolagem, em pixels.
                    /**
                    final int currScrollMiddleY = calculateScrollYMiddle(scrollHeight, currScrollY);
                        if (DEBUG)
                        Log.d(TAG, String.format("ScrollY [%d], ScrollMiddleY [%d]", currScrollY, currScrollMiddleY));
                        */
                        //  Número da próxima página.
                    /**
                        final int nextPage = mCurrentPage; // The top of next page, in pixels.
                        final int nextPageTop = nextPage * scrollHeight;
                        if (DEBUG) Log.d(TAG, String.format("NextPageTopY [%d]", nextPageTop));
                    */
                    if (Math.abs(velocityY) < mMinVelocity) {
                        snapToPage(mCurrentPage, velocityY);
                    }
                    else if (Math.abs(velocityY) >= mMinVelocity) {
                        mIsScrolling = true;
                        // Comece o cálculo de rolagem.
                        mScroller.fling(0, getCurrentScrollY(), 0,
                                (int) -velocityY, 0, 0, 0, fullHeight - pageHeight);
                        calculateWhereFlingShouldStops();
                        mScrollView.post(mScrollRunnable);
                    }
                        // mScroller.startScroll(0, currScrollY, 0, Math.min(lastPageTopY, nextPageTop)
                        // - currScrollY, mMinAnimationDuration);
                }
                resetVelocityTracker();
                // Consumir evento de toque
                return true;
            case MotionEvent.ACTION_CANCEL:
                resetVelocityTracker();
                resetPager();
                break;
        }
        return false;
    }

    void calculateWhereFlingShouldStops()
    {
        final int initFinishY = mScroller.getFinalY();
        int finishTopY = initFinishY;
        View child;
        int smallestDelta = mScrollView.getHeight();
        int currentDelta;
        for (int i = 0; i < getPageCount(); i++) {
            child = mContentView.getChildAt(i);
            currentDelta = child.getTop() - initFinishY;
            if (DEBUG)
                Log.d(TAG, String.format("ClosestTop[%d],CurrTop[%d],ClosestDelta[%d],CurrDelta[%d]",
                        finishTopY, child.getTop(), smallestDelta, currentDelta));
            if (Math.abs(currentDelta) < Math.abs(smallestDelta)) {
                smallestDelta = currentDelta;
                finishTopY = child.getTop();
            }
        }
        mScroller.setFinalY(finishTopY);
    }

    void snapToPage(int whichPage, final float velocity) {
        if (whichPage < 0) whichPage = 0;
        if (whichPage >= getPageCount()) whichPage = getPageCount() - 1;

        final boolean changingPages = whichPage != mCurrentPage;

        final int delta = mContentView.getChildAt(whichPage).getTop() - getCurrentScrollY();
        int time = mMinAnimationDuration;
        if (velocity != 0)
            time = Math.max(mMinAnimationDuration, (int) ((float) delta / Math.abs(velocity)));

        mScroller.startScroll(0, getCurrentScrollY(), 0, delta, time);
        mScrollView.post(mScrollRunnable); // Comece a animação.
    }

    void resetPager() {
        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
            mScrollView.removeCallbacks(mScrollRunnable);
        }
        mIsScrolling = false;
        // resetVelocityTracker();
    }

    void resetVelocityTracker() {
        mInitialY = mLastY = -1;
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    final void calculateNextPage() {
        // if (velocityY < -mMinVelocity)
        // snapToPage(mCurrentPage + 1, velocityY);
        // else if (velocityY > mMinVelocity)
        // snapToPage(mCurrentPage - 1, velocityY);
    }

    public int getPageCount() {
        return mContentView.getChildCount();
        // return Math.round((float) mContentView.getHeight() / (float) mScrollView.getHeight());
    }

    final int getCurrentScrollY() {
        return mScrollView.getScrollY();
    }
    final int calculateCurrentPage(final int scrollViewHeight, final int currentScrollY) {
        // Current page num.
        mCurrentPage = calculateScrollYMiddle(scrollViewHeight, currentScrollY) / scrollViewHeight;
        return mCurrentPage;
    }
    final int calculateScrollYMiddle(final int scrollViewHeight, final int currentScrollY) {
        // return currScrollY + displayHeight / 2;
        return currentScrollY + scrollViewHeight / 2;
    }
    public int getCurrentPage() {
        return mCurrentPage;
    }
}
