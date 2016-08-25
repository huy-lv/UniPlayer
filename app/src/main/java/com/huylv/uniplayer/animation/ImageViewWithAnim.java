package com.huylv.uniplayer.animation;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.huylv.uniplayer.R;

/**
 * Created by HuyLV-CT on 23-Aug-16.
 */
public class ImageViewWithAnim extends ImageView {

    public ImageViewWithAnim(Context context) {
        super(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ImageViewWithAnim(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public ImageViewWithAnim(Context context, AttributeSet attrs, int defStyleAttr) {

        super(context, attrs, defStyleAttr);
    }

    public ImageViewWithAnim(Context context, AttributeSet attrs) {

        super(context, attrs);
    }

    public void startAnimation(final boolean play) {
        Animation in = AnimationUtils.loadAnimation(getContext(), R.anim.transition_scale_small);
        final Animation out = AnimationUtils.loadAnimation(getContext(), R.anim.transition_scale_big);
        super.startAnimation(in);
        in.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (play) {
                    setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_pause));
                } else {
                    setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_play));
                }
                startAnimation(out);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    public void startAnimation(final int toResId) {
        Animation in = AnimationUtils.loadAnimation(getContext(), R.anim.transition_scale_small);
        final Animation out = AnimationUtils.loadAnimation(getContext(), R.anim.transition_scale_big);
        super.startAnimation(in);
        in.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                setImageDrawable(ContextCompat.getDrawable(getContext(), toResId));
                startAnimation(out);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }
}
