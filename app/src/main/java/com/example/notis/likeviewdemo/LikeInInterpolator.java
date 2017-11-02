package com.example.notis.likeviewdemo;

import android.view.animation.Interpolator;

public class LikeInInterpolator implements Interpolator {
    @Override
    public float getInterpolation(float input) {
        if (input < 0.8)
            return 1.25f * input;
        else if (input >= 0.8f && input < 0.95f)
            return (float) (1 + 0.2f * Math.sin(-2 * Math.PI * input * 10));
        else if (input >= 0.95f && input <= 1)
            return (float) (1 + 0.1f * Math.sin(20 * Math.PI));
        return 0;
    }

    static int x=10;
    static {
        x+=5;
    }

    public static void main(String[] args){
        {
            System.out.println("x="+x);
        }
    }
}
