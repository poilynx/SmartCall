package zhexian.app.smartcall.ui;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

import zhexian.app.smartcall.R;
import zhexian.app.smartcall.tools.Utils;

/**
 * 通用下拉通知类
 */
public class NotifyBar {
    public static final int DURATION_SHORT = 2500;
    public static final int DURATION_MIDDLE = 5000;
    public static final int DURATION_LONG = 8000;
    private static final int SLIDE_ANIMATION_MILLION_SECONDS = 500;
    private static final int HANDLER_HIDE_NOTIFY_BAR = 1;
    private static Handler hideHandler;
    private static Handler hideAnimationHandler;
    private Activity mActivity;
    private WindowManager mWindowManager = null;
    private View mNotifyView = null;
    private View mProgress;
    private ImageView mIcon;
    private TextView mNotifyText;
    private Timer hideTimer;
    private TimerTask hideTimerTask;
    private TimerTask hideAnimationTimerTask;
    private ObjectAnimator slideAnimation;
    private boolean mIsAddedView;
    private boolean mIsShow;

    public NotifyBar(Activity activity) {
        mActivity = activity;
        mIsAddedView = false;
    }


    private void initNotifyView() {
        if (mIsAddedView)
            return;

        hideTimer = new Timer();
        hideHandler = new HideHandler(this);
        hideAnimationHandler = new HideAnimationHandler(this);

        WindowManager.LayoutParams mNotifyViewLayout = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.TYPE_TOAST,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.RGBA_8888);

        mNotifyViewLayout.height = mActivity.getResources().getDimensionPixelOffset(R.dimen.action_bar_height);
        mNotifyViewLayout.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        mWindowManager = (WindowManager) mActivity.getSystemService(Context.WINDOW_SERVICE);
        LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mNotifyView = inflater.inflate(R.layout.notify_bar, null);
        mNotifyText = (TextView) mNotifyView.findViewById(R.id.notify_text);
        mProgress = mNotifyView.findViewById(R.id.notify_progress);
        mIcon = (ImageView) mNotifyView.findViewById(R.id.notify_icon);
        slideAnimation = Utils.GenerateSlideAnimator(mActivity, R.animator.notify_bar_vertical_slide, mNotifyView);

        mWindowManager.addView(mNotifyView, mNotifyViewLayout);
        mIsAddedView = true;
    }

    public void show(int StringID, int duration, IconType iconType) {
        show(mActivity.getString(StringID), duration, iconType);
    }

    public void show(String text, int duration, IconType iconType) {
        show(text, iconType);
        hide(duration - SLIDE_ANIMATION_MILLION_SECONDS);
    }

    public void show(int StringID, IconType iconType) {
        show(mActivity.getString(StringID), iconType);
    }

    public void show(String text, IconType iconType) {
        initNotifyView();

        switch (iconType) {
            case None:
                mProgress.setVisibility(View.GONE);
                mIcon.setVisibility(View.GONE);
                break;
            case Progress:
                mProgress.setVisibility(View.VISIBLE);
                mIcon.setVisibility(View.GONE);
                break;
            default: {
                mProgress.setVisibility(View.GONE);
                mIcon.setVisibility(View.VISIBLE);

                if (iconType == IconType.Error)
                    mIcon.setImageResource(R.drawable.icon_error);
                else if (iconType == IconType.Success)
                    mIcon.setImageResource(R.drawable.icon_success);
            }
            break;
        }
        mNotifyText.setText(text);

        if (hideTimerTask != null)
            hideTimerTask.cancel();

        if (hideAnimationTimerTask != null)
            hideAnimationTimerTask.cancel();

        if (!mIsShow) {
            slideAnimation.start();
            mNotifyView.setVisibility(View.VISIBLE);
        }
        mIsShow = true;
    }

    private void hide() {

        if (mIsShow) {
            if (hideTimerTask != null)
                hideTimerTask.cancel();

            hideTimerTask = new TimerTask() {
                @Override
                public void run() {
                    hideHandler.sendEmptyMessage(HANDLER_HIDE_NOTIFY_BAR);
                }
            };

            hideTimer.schedule(hideTimerTask, SLIDE_ANIMATION_MILLION_SECONDS);
            slideAnimation.reverse();
        }
    }

    public void hide(int delay) {
        if (hideAnimationTimerTask != null)
            hideAnimationTimerTask.cancel();

        if (hideTimerTask != null)
            hideTimerTask.cancel();

        hideAnimationTimerTask = new TimerTask() {
            @Override
            public void run() {
                hideAnimationHandler.sendEmptyMessage(HANDLER_HIDE_NOTIFY_BAR);
            }
        };
        hideTimer.schedule(hideAnimationTimerTask, delay);
    }

    public void destroy() {
        if (hideTimerTask != null)
            hideTimerTask.cancel();

        if (hideAnimationTimerTask != null)
            hideAnimationTimerTask.cancel();

        if (mIsAddedView) {
            mActivity = null;
            mWindowManager.removeViewImmediate(mNotifyView);
            mWindowManager = null;
            mNotifyView = null;
        }
    }

    public enum IconType {
        None,
        Progress,
        Error,
        Success
    }

    static class HideHandler extends Handler {
        WeakReference<NotifyBar> notifyBar;

        HideHandler(NotifyBar notifyBar) {
            this.notifyBar = new WeakReference<>(notifyBar);
        }

        @Override
        public void handleMessage(Message msg) {
            NotifyBar _notifyBar = notifyBar.get();

            if (_notifyBar == null)
                return;

            if (msg.what == HANDLER_HIDE_NOTIFY_BAR) {
                _notifyBar.mNotifyView.setVisibility(View.GONE);
                _notifyBar.mIsShow = false;
            }
        }
    }

    static class HideAnimationHandler extends Handler {
        WeakReference<NotifyBar> notifyBar;

        HideAnimationHandler(NotifyBar notifyBar) {
            this.notifyBar = new WeakReference<>(notifyBar);
        }

        @Override
        public void handleMessage(Message msg) {
            NotifyBar _notifyBar = notifyBar.get();

            if (_notifyBar == null)
                return;

            if (msg.what == HANDLER_HIDE_NOTIFY_BAR) {
                _notifyBar.hide();
            }
        }
    }
}
