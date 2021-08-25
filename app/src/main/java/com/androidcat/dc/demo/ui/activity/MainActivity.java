package com.androidcat.dc.demo.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.androidcat.dc.demo.R;
import com.androidcat.dc.demo.base.BaseActivity;
import com.androidcat.dc.demo.base.BaseFragment;
import com.androidcat.dc.demo.ui.fragment.DebugFragment;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import me.yokeyword.fragmentation.ISupportFragment;
import me.yokeyword.fragmentation.SupportFragment;
import me.yokeyword.fragmentation.anim.DefaultHorizontalAnimator;
import me.yokeyword.fragmentation.anim.FragmentAnimator;

public class MainActivity extends BaseActivity {
    private static final String TAG = "MainActivity";

    @BindView(R.id.fl_container)
    FrameLayout flContainer;

    @BindView(R.id.tooBar_title)
    TextView tooBarTitle;

    @BindView(R.id.title_view)
    View titleView;

    protected Unbinder mUnbinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mUnbinder = ButterKnife.bind(this);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }

        loadRootFragment(R.id.fl_container, DebugFragment.newInstance());
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mUnbinder != null) {
            mUnbinder.unbind();
        }
        mUnbinder = null;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }
    @Override
    public FragmentAnimator onCreateFragmentAnimator() {
        return new DefaultHorizontalAnimator();
    }

    public void setTitle(String title) {
        if(!TextUtils.isEmpty(title)){
            if(tooBarTitle.getText().toString().equals(title)){
                return;
            }
            tooBarTitle.setText(title);
        }
    }

    public void hideTitle(boolean needToHide) {
        if (needToHide){
            titleView.setVisibility(View.GONE);
        }else {
            titleView.setVisibility(View.VISIBLE);
        }
    }

    public void changeFragment(BaseFragment baseFragment) {
        ISupportFragment topFragment = getTopFragment();
        BaseFragment myHome = (BaseFragment) topFragment;
        BaseFragment changeFragment = findFragment(baseFragment.getClass());
        if (changeFragment == null) {
            if(baseFragment instanceof DebugFragment){
                myHome.startWithPopTo(baseFragment, DebugFragment.class, false);
            }else{
                myHome.start(baseFragment, SupportFragment.SINGLETASK);
            }
        } else {
            myHome.start(changeFragment, SupportFragment.SINGLETASK);
        }
    }

    public void popToFragment(BaseFragment target) {
        ISupportFragment topFragment = getTopFragment();
        BaseFragment myHome = (BaseFragment) topFragment;
        BaseFragment targetFragment = findFragment(target.getClass());
        if (targetFragment == null) {
            if(target instanceof  DebugFragment){
                myHome.startWithPopTo(target, DebugFragment.class, false);
            }else{
                myHome.start(target, SupportFragment.SINGLETASK);
            }
        } else {
            myHome.popTo(target.getClass(),false);
        }
    }

    public void setBackgroudColor(int color) {
        flContainer.setBackgroundColor(color);
    }

    public void showView(int id,int visib){
           if(findViewById(id)!=null){
               findViewById(id).setVisibility(visib);
           }
    }

}