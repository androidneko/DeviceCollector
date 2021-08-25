package com.androidcat.dc.demo.ui.fragment;

import android.view.View;

import com.androidcat.dc.demo.R;
import com.androidcat.dc.demo.base.BaseFragment;
import com.androidcat.dc.demo.base.BaseHeadFragment;
import com.androidcat.dc.demo.utils.JxlExcelUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.OnClick;

public class DebugFragment extends BaseHeadFragment {

    private static final String TAG = "DebugFragment";
    List<Order> orders = new ArrayList<Order>();
    @Override
    public String getTitle() {
        return "首页";
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_debug;
    }

    @Override
    public void initView(View view) {
    }

    public static BaseFragment newInstance() {
        return new DebugFragment();
    }


    @OnClick({R.id.h5_demo_btn})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.h5_demo_btn:
                createExcelRecord();
                break;
        }
    }

    private void createExcelRecord(){
        //String fileName = DateFormaUtil.paramTimeStamp(System.currentTimeMillis())+".xlsx";
        //ExcelUtil.initWorkbook(fileName);
        initList();
        saveExcel();
    }

    private void initList(){
        int length = Const.OrderInfo.orderOne.length;
        for(int i = 0;i < length;i++){
            Order order = new Order( Const.OrderInfo.orderOne[i][0],  Const.OrderInfo.orderOne[i][1],  Const.OrderInfo.orderOne[i][2],  Const.OrderInfo.orderOne[i][3]);
            orders.add(order);
        }
    }

    private void saveExcel(){
        try {
            JxlExcelUtil.writeExcel(getActivity(),
                    orders, "excel_"+new Date().toString());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
