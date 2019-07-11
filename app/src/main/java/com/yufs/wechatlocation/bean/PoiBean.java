package com.yufs.wechatlocation.bean;

import com.amap.api.services.core.LatLonPoint;

/**
 * 地图搜索周边bean
 * Created by yufs on 2017/3/1.
 */

public class PoiBean {
    String titleName;
    String province;//省 p
    String cityName;//市 q
    String ad;//区
    String snippet;//详细地址
    boolean selected;//是否选中当前
    LatLonPoint point;//经纬度对象
    boolean loc;//当前地址为定位得到，非搜索周边得到
    String locAddress;//定位得到详细地址

    public String getLocAddress() {
        return locAddress;
    }

    public void setLocAddress(String locAddress) {
        this.locAddress = locAddress;
    }

    public boolean isLoc() {
        return loc;
    }

    public void setLoc(boolean loc) {
        this.loc = loc;
    }

    public LatLonPoint getPoint() {
        return point;
    }

    public void setPoint(LatLonPoint point) {
        this.point = point;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getTitleName() {
        return titleName;
    }

    public String getAd() {
        return ad;
    }

    public void setAd(String ad) {
        this.ad = ad;
    }

    public void setTitleName(String titleName) {
        this.titleName = titleName;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }
}
