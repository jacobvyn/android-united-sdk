package com.loopme.controllers.view;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.loopme.Constants;
import com.loopme.R;
import com.loopme.controllers.display.DisplayControllerVast;
import com.loopme.utils.ImageUtils;
import com.loopme.utils.Utils;

public class ViewControllerVast {

    private final DisplayControllerVast mDisplayControllerVast;
    private ViewControllerVastListener mListener;
    private FrameLayout mContainerView;
    private PlayerLayout mPlayerLayout;
    private EndCardLayout mEndCardLayout;

    public ViewControllerVast(DisplayControllerVast displayControllerVast, ViewControllerVastListener listener) {
        mDisplayControllerVast = displayControllerVast;
        mListener = listener;
    }

    public void buildVideoAdView(FrameLayout containerView, Context context, WebView webView) {
        if (containerView != null && context != null) {
            mContainerView = containerView;
            mContainerView.removeAllViews();
            mPlayerLayout = new PlayerLayout(context, webView, initOnPlayerListener());
            mEndCardLayout = new EndCardLayout(context, initOnEndCardListener());
            mContainerView.addView(mPlayerLayout);
            mContainerView.addView(mEndCardLayout);
        }
    }

    private EndCardLayout.OnEndCardListener initOnEndCardListener() {
        return new EndCardLayout.OnEndCardListener() {
            @Override
            public void onEndCardClick() {
                openUrl();
            }

            @Override
            public void onCloseClick() {
                closeSelf();
            }

            @Override
            public void onReplayClick() {
                replayVideo();
            }
        };
    }

    private PlayerLayout.OnPlayerListener initOnPlayerListener() {
        return new PlayerLayout.OnPlayerListener() {
            @Override
            public void onSurfaceTextureReady(Surface surface) {
                onSurfaceReady(surface);
            }

            @Override
            public void onPlayerClick() {
                openUrl();
            }

            @Override
            public void onMuteClick(boolean mute) {
                onVolumeMute(mute);
            }

            @Override
            public void onSkipClick() {
                skipVideo();
            }
        };
    }

    private void onSurfaceReady(Surface surface) {
        if (mListener != null) {
            mListener.onSurfaceTextureReady(surface);
        }
    }

    public void adjustLayoutParams(int width, int height) {
        if (mPlayerLayout != null) {
            mPlayerLayout.adjustLayoutParams(width, height, mContainerView.getWidth(), mContainerView.getHeight());
        }
    }

    public Surface getSurface() {
        if (mPlayerLayout != null) {
            return mPlayerLayout.getSurface();
        }
        return null;
    }

    public void showEndCard(String imageUri) {
        setEndCardVisibility(View.VISIBLE);
        setVideoPlayerVisibility(View.GONE);
        if (mEndCardLayout != null) {
            mEndCardLayout.setEndCard(imageUri);
        }
    }

    private void setVideoPlayerVisibility(int visibility) {
        if (mPlayerLayout != null) {
            mPlayerLayout.setVisibility(visibility);
        }
    }

    private void setEndCardVisibility(int visibility) {
        if (mEndCardLayout != null) {
            mEndCardLayout.setVisibility(visibility);
        }
    }


    public boolean isEndCard() {
        return mEndCardLayout != null && mEndCardLayout.getVisibility() == View.VISIBLE;
    }

    public void dismiss() {
        if (mContainerView != null) {
            mContainerView.removeAllViews();
        }
    }

    private void openUrl() {
        if (mDisplayControllerVast != null) {
            mDisplayControllerVast.onRedirect(null, null);
        }
    }

    private void replayVideo() {
        setEndCardVisibility(View.GONE);
        setVideoPlayerVisibility(View.VISIBLE);
        onPlay();
    }


    private void skipVideo() {
        if (mDisplayControllerVast != null) {
            mDisplayControllerVast.skipVideo();
        }
    }

    private void closeSelf() {
        if (mDisplayControllerVast != null) {
            mDisplayControllerVast.closeSelf();
        }
    }

    private void onPlay() {
        if (mDisplayControllerVast != null) {
            mDisplayControllerVast.onPlay(Constants.START_POSITION);
        }
    }

    private void onVolumeMute(boolean muteState) {
        if (mDisplayControllerVast != null) {
            mDisplayControllerVast.onVolumeMute(muteState);
        }
    }

    public TextureView getPlayerView() {
        if (mPlayerLayout != null) {
            return mPlayerLayout.getPlayerView();
        } else {
            return null;
        }
    }

    public void destroy() {
        if (mEndCardLayout != null) {
            mEndCardLayout.destroy();
        }
    }

    public void setMaxProgress(int duration) {
        if (mPlayerLayout != null) {
            mPlayerLayout.setMaxProgress(duration);
        }
    }

    public void setProgress(int duration) {
        if (mPlayerLayout != null) {
            mPlayerLayout.setProgress(duration);
        }
    }

    public void showSkipButton() {
        if (mPlayerLayout != null) {
            mPlayerLayout.showSkipButton();
        }
    }

    public boolean isMute() {
        return mPlayerLayout != null && mPlayerLayout.isMute();
    }

    public interface ViewControllerVastListener {
        void onSurfaceTextureReady(Surface surface);
    }
}