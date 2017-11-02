package com.example.notis.likeviewdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import java.util.Hashtable;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private LinearLayout llLike;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        llLike = (LinearLayout) findViewById(R.id.llLike);
        llLike.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.llLike:
                int childCount = llLike.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    if (llLike.getChildAt(i) instanceof Like) {
                        ((Like) llLike.getChildAt(i)).changeLike();
                    }
                }
                break;
        }
    }

}
