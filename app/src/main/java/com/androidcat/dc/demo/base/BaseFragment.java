package com.androidcat.dc.demo.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androidcat.catlibs.utils.SafeHandler;
import com.androidcat.dc.demo.ui.activity.MainActivity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import me.yokeyword.fragmentation.SupportFragment;

public abstract class BaseFragment extends SupportFragment {
    private Unbinder mUnbinder;

    public abstract int getLayoutId();

    public abstract void initView(View view);

    protected abstract void initTitle();

    protected SafeHandler baseHandler;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutId(), container, false);
        mUnbinder = ButterKnife.bind(this, view);
        baseHandler = ((BaseActivity) getActivity()).baseHandler;
        initView(view);
        return view;
    }

    protected void changeFragment(final BaseFragment baseFragment) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (getActivity() instanceof MainActivity) {
                    MainActivity activity = (MainActivity) getActivity();
                    activity.changeFragment(baseFragment);
                }
            }
        });
    }

    protected void popToFragment(final BaseFragment baseFragment) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (getActivity() instanceof MainActivity) {
                    MainActivity activity = (MainActivity) getActivity();
                    activity.popToFragment(baseFragment);
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mUnbinder != null) {
            mUnbinder.unbind();
        }
        mUnbinder = null;
    }
}
