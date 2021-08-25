package com.androidcat.catlibs.net.http.request;

/**
 * Created by androidcat on 2019/8/28.
 */

public class GetShortcutInfoRequest extends BaseRequest{
    private String canteenSite;
    private String mealType;

    public String getCanteenSite() {
        return canteenSite;
    }

    public void setCanteenSite(String canteenSite) {
        this.canteenSite = canteenSite;
    }

    public String getMealType() {
        return mealType;
    }

    public void setMealType(String mealType) {
        this.mealType = mealType;
    }
}
