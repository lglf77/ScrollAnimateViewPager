package com.lglf77.scrollanimate_01;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ScrollView;

import com.lglf77.library.view.ScrollViewPager;

public class MainActivity extends AppCompatActivity {

    private ScrollViewPager mPager;
    private ScrollView mScrollView;
    private ViewGroup mScrollContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mScrollView = (ScrollView) findViewById(R.id.scroll_view);
        mScrollContent = (ViewGroup) findViewById(R.id.content);

        mPager = new ScrollViewPager(mScrollView, mScrollContent);
    }
}