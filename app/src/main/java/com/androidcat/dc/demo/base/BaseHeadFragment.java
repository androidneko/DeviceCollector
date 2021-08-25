package com.androidcat.dc.demo.base;

import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import com.androidcat.catlibs.flyco.animation.FlipEnter.FlipVerticalSwingEnter;
import com.androidcat.catlibs.flyco.dialog.listener.OnBtnClickL;
import com.androidcat.catlibs.flyco.dialog.widget.NormalDialog;
import com.androidcat.dc.demo.R;
import com.androidcat.dc.demo.ui.activity.MainActivity;
import com.androidcat.dc.demo.ui.fragment.DebugFragment;

public abstract class BaseHeadFragment extends BaseFragment {
    public abstract String getTitle();


    @Override
    public void onSupportVisible() {
        super.onSupportVisible();
        hideSoftInput();
        initTitle();
    }

    protected void setBackgroundColor(int color){
        if( getActivity() instanceof MainActivity){
            MainActivity activity =(MainActivity) getActivity();
            activity.setBackgroudColor(color);
        }
    }

    @Override
    protected void initTitle() {
        if (getActivity() == null){
            return;
        }
        if( getActivity() instanceof MainActivity){
            MainActivity activity =(MainActivity) getActivity();
            activity.hideTitle(false);
            activity.setTitle(getTitle());
            if(this instanceof DebugFragment){
                activity.showView(R.id.back_img,View.GONE);
            }else{
                activity.showView(R.id.back_img,View.VISIBLE);
            }
            if (this instanceof DebugFragment){
                activity.hideTitle(true);
            }
        }
    }

    protected void showToast(final String msg){
        if (getActivity() == null){
            return;
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(),msg,Toast.LENGTH_SHORT).show();
            }
        });
    }

    protected boolean isMainThread() {
        return Looper.getMainLooper().getThread().getId() == Thread.currentThread().getId();
    }

    protected void showSuccessDialog(final String msg){
        if (getActivity() == null){
            return;
        }
        if (isMainThread()){
            doShowSuccessDialog(msg);
        }else {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    doShowSuccessDialog(msg);
                }
            });
        }
    }

    private void doShowSuccessDialog(String msg){
        final NormalDialog dialog = new NormalDialog(getActivity());
        dialog.btnNum(1)
                .content(msg)
                .btnText("好的")
                .style(NormalDialog.STYLE_TWO)
                .showAnim(new FlipVerticalSwingEnter());
        dialog.setOnBtnClickL(new OnBtnClickL() {
            @Override
            public void onBtnClick() {
                dialog.dismiss();
            }
        });
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }
}
