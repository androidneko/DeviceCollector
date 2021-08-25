package com.androidcat.catlibs.net.http.response;


import com.androidcat.biz.consts.SDKConsts;

/**
 * Created by androidcat on 2019/7/11.
 */

public class BaseResponse {

    protected String returnCode = SDKConsts.CODE_SUCCESS;
    protected String returnDes = SDKConsts.DESC_SUCCESS;

    public BaseResponse(){

    }

    public BaseResponse(String returnCode,String returnDes){
        this.returnCode = returnCode;
        this.returnDes = returnDes;
    }

    public String getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(String returnCode) {
        this.returnCode = returnCode;
    }

    public String getReturnDes() {
        return returnDes;
    }

    public void setReturnDes(String returnDes) {
        this.returnDes = returnDes;
    }
}
