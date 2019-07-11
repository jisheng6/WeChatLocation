package com.yufs.wechatlocation;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.SuggestionCity;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.yufs.wechatlocation.adapter.PoiAdapter;
import com.yufs.wechatlocation.bean.PoiBean;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by yufs on 2017/4/17.
 */

public class LocationSelectActivity extends BaseActivity implements PoiSearch.OnPoiSearchListener, AutoListView.OnRefreshListener, AutoListView.OnLoadListener, AMap.OnMapClickListener, AMap.OnMarkerClickListener, AMap.InfoWindowAdapter {
    private static final int REQUEST_SEARCH_CODE = 1;
    private static final int RESULT_INTENT_CODE=2;
    private AutoListView lv_list;
    private MapView mMapView;
    private TextView tv_title_back,tv_title_right,tv_title_search;
    private AMap mAMap;
    private Marker locationMarker; // 选择的点
    private LatLonPoint lp = new LatLonPoint(39.907775, 116.247522);//
    private PoiSearch.Query poiQuery;//poi搜索类

    private PoiSearch.Query query;// Poi查询条件类
    private PoiSearch poiSearch;

    private AMapLocationClient locationClient = null;//定位类
    private double mLatitude,mLongitude;//定位的经纬度
    private String mCity;//定位的城市
    private PoiBean mCurrPoiBean;

    private PoiAdapter mAdapter;
    private int currentPage=0;//页数从第0页开始
    private List<PoiBean> poiData=new ArrayList<>();
    private AMapLocation mLoc;//首次进入定位成功信息

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_address);
        initView();
        mMapView.onCreate(savedInstanceState);
        InitLocation();
        setListener();
    }

    private void initView() {
        mMapView= (MapView) findViewById(R.id.map);
        lv_list= (AutoListView) findViewById(R.id.lv_list);
        tv_title_back= (TextView) findViewById(R.id.tv_title_back);
        tv_title_right= (TextView) findViewById(R.id.tv_title_right);
        tv_title_search= (TextView) findViewById(R.id.tv_title_search);
    }

    private void setListener() {
        mAdapter=new PoiAdapter(this,poiData);
        lv_list.setAdapter(mAdapter);
        lv_list.setOnRefreshListener(this);
        lv_list.setOnLoadListener(this);
        lv_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PoiBean poiBean = poiData.get((int) id);
                mCurrPoiBean=poiBean;
                LatLonPoint point = poiBean.getPoint();
                addmark(point.getLatitude(),point.getLongitude());
                mAMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(point.getLatitude(), point.getLongitude()), 14));
                for (int i=0;i<poiData.size();i++){
                    poiData.get(i).setSelected(false);
                }
                poiBean.setSelected(true);
                mAdapter.notifyDataSetChanged();
            }
        });
        tv_title_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirm();
            }
        });

        tv_title_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openSearch();
            }
        });

        tv_title_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void InitLocation() {
        //初始化client
        locationClient = new AMapLocationClient(this.getApplicationContext());
        //设置定位参数
        locationClient.setLocationOption(getDefaultOption());
        // 设置定位监听
        locationClient.setLocationListener(locationListener);
        locationClient.startLocation();
    }
    private String savePath;//本地截图保存路径，待删除
    public void confirm(){
        if(mCurrPoiBean==null){
            Toast.makeText(this, "请选择详细地址", Toast.LENGTH_SHORT).show();
            return;
        }
        mAMap.getMapScreenShot(new AMap.OnMapScreenShotListener() {
            @Override
            public void onMapScreenShot(Bitmap bitmap) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
                String pathFolder= Environment.getExternalStorageDirectory().toString() + "/myou/cacheimage/";
                savePath=pathFolder+"t_" + sdf.format(new Date()) + ".png";
                Log.e("yufs","截图保存路径："+savePath);
                if(null == bitmap){
                    return;
                }
                try {
                    File file = new File(pathFolder);
                    if(!file.exists()){
                        file.mkdirs();
                    }
                    FileOutputStream fos = new FileOutputStream(new File(savePath));

                    boolean b = bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    try {
                        fos.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    StringBuffer buffer = new StringBuffer();
                    if (b) {
                        buffer.append("截屏成功 ");
                        Log.e("yufs", "截图完成。。。");
//                        //上传图片到七牛
//                        imageBytes = ImageUtils.bmpToByteArray(bitmap, false);
//                        //获取七牛token
//                        getQiniuToken();
                        submit();
                    }else {
                        buffer.append("截屏失败 ");
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    /**
     * 返回地址详细信息
     */
    private void submit() {
        Intent intent=new Intent();
        intent.putExtra("latitude",mCurrPoiBean.getPoint().getLatitude());
        intent.putExtra("longitude",mCurrPoiBean.getPoint().getLongitude());
        if(mCurrPoiBean.isLoc()) {
            intent.putExtra("address",mCurrPoiBean.getLocAddress());
        }else{
            intent.putExtra("address", mCurrPoiBean.getAd() + mCurrPoiBean.getSnippet());
        }
        setResult(RESULT_INTENT_CODE,intent);
        finish();
    }

    /**
     * 开始进行poi搜索
     */
    protected void doSearchQuery(String city,double latitude,double longitude) {
        String mType="汽车服务|汽车销售|汽车维修|摩托车服务|餐饮服务|购物服务|生活服务|体育休闲服务|医疗保健服务|住宿服务|风景名胜|商务住宅|政府机构及社会团体|科教文化服务|交通设施服务|金融保险服务|公司企业|道路附属设施|地名地址信息|公共设施";
        query = new PoiSearch.Query("", mType, city);// 第一个参数表示搜索字符串，第二个参数表示poi搜索类型，第三个参数表示poi搜索区域（空字符串代表全国）
        query.setPageSize(20);// 设置每页最多返回多少条poiitem
        query.setPageNum(currentPage);// 设置查第一页
        if (lp != null) {
            poiSearch = new PoiSearch(this, query);
            poiSearch.setOnPoiSearchListener(this);
            //以当前定位的经纬度为准搜索周围5000米范围
            // 设置搜索区域为以lp点为圆心，其周围5000米范围
            poiSearch.setBound(new PoiSearch.SearchBound(new LatLonPoint(latitude,longitude), 1000, true));//
            poiSearch.searchPOIAsyn();// 异步搜索
        }
    }

    /**
     * 定位监听
     */
    AMapLocationListener locationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation loc) {
            if (null != loc) {
                //解析定位结果
                String city = loc.getCity();
                Log.e("yufs","當前经度"+loc.getLongitude()+"当前维度："+loc.getLatitude());
                mLoc=loc;
                lp.setLongitude(loc.getLongitude());
                lp.setLatitude(loc.getLatitude());
                //得到定位信息
                Log.e("yufs","定位详细信息："+loc.toString());
                mLatitude=loc.getLatitude();
                mLongitude=loc.getLongitude();
                //初始化地图对象
                initMap(loc);
                //查询周边
                doSearchQuery(loc.getCity(),loc.getLatitude(),loc.getLongitude());
            } else {
                Toast.makeText(LocationSelectActivity.this, "定位失败，请打开位置权限", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private AMapLocationClientOption getDefaultOption(){
        AMapLocationClientOption mOption = new AMapLocationClientOption();
        mOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);//可选，设置定位模式，可选的模式有高精度、仅设备、仅网络。默认为高精度模式
        mOption.setGpsFirst(false);//可选，设置是否gps优先，只在高精度模式下有效。默认关闭
        mOption.setHttpTimeOut(30000);//可选，设置网络请求超时时间。默认为30秒。在仅设备模式下无效
        mOption.setInterval(2000);//可选，设置定位间隔。默认为2秒
        mOption.setNeedAddress(true);//可选，设置是否返回逆地理地址信息。默认是true
        mOption.setOnceLocation(true);//可选，设置是否单次定位。默认是false
        mOption.setOnceLocationLatest(false);//可选，设置是否等待wifi刷新，默认为false.如果设置为true,会自动变为单次定位，持续定位时不要使用
        AMapLocationClientOption.setLocationProtocol(AMapLocationClientOption.AMapLocationProtocol.HTTP);//可选， 设置网络请求的协议。可选HTTP或者HTTPS。默认为HTTP
        mOption.setSensorEnable(false);//可选，设置是否使用传感器。默认是false
        mOption.setWifiScan(true); //可选，设置是否开启wifi扫描。默认为true，如果设置为false会同时停止主动刷新，停止以后完全依赖于系统刷新，定位位置可能存在误差
        mOption.setLocationCacheEnable(false); //可选，设置是否使用缓存定位，默认为true
        return mOption;
    }

    private void initMap(AMapLocation loc) {
        if (mAMap == null) {
            mAMap = mMapView.getMap();
            mAMap.setOnMapClickListener(this);
            mAMap.setOnMarkerClickListener(this);
            mAMap.setInfoWindowAdapter(this);
            locationMarker = mAMap.addMarker(new MarkerOptions()
                    .anchor(0.5f, 0.5f)
                    .icon(BitmapDescriptorFactory
                            .fromBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.poi_marker_1)))
                    .position(new LatLng(loc.getLatitude(), loc.getLongitude())));
        }
//        setup();
        mAMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(loc.getLatitude(), loc.getLongitude()), 14));
    }

    private void addmark(double latitude, double longitude) {

        if (locationMarker == null) {
            locationMarker = mAMap.addMarker(new MarkerOptions()
                    .position(new LatLng(latitude, longitude))
                    .icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                            .decodeResource(getResources(), R.mipmap.poi_marker_1)))
                    .draggable(true));
        } else {
            locationMarker.setPosition(new LatLng(latitude, longitude));
            mAMap.invalidate();
        }
    }

    //poi搜索
    private void searchList(String cityCode, String road) {
        poiQuery = new PoiSearch.Query(road, "", cityCode);
        poiQuery.setPageSize(15);
        poiQuery.setPageNum(currentPage);
        PoiSearch poiSearch = new PoiSearch(this, poiQuery);
        poiSearch.setOnPoiSearchListener(onPoiSearchListener);
        poiSearch.searchPOIAsyn();
    }

    //索引搜索
    PoiSearch.OnPoiSearchListener onPoiSearchListener = new PoiSearch.OnPoiSearchListener() {
        @Override
        public void onPoiSearched(PoiResult result, int rCode) {
            if (rCode == 1000) {
                if (result != null && result.getQuery() != null) {// 搜索poi的结果
                    if (result.getQuery().equals(poiQuery)) {// 是否是同一条
                        lv_list.onLoadComplete();
                        List<PoiItem> poiItems = result.getPois();// 取得第一页的poiitem数据，页数从数字0开始
                        List<PoiBean> tem=new ArrayList<>();
                        if (poiItems != null && poiItems.size() > 0) {
                            for (int i = 0; i < poiItems.size(); i++) {
                                PoiItem poiItem = poiItems.get(i);
                                PoiBean bean=new PoiBean();
                                bean.setTitleName(poiItem.getTitle());
                                bean.setCityName(poiItem.getCityName());
                                bean.setAd(poiItem.getAdName());
                                bean.setSnippet(poiItem.getSnippet());
                                bean.setPoint(poiItem.getLatLonPoint());
                                Log.e("yufs",""+poiItem.getTitle()+","+poiItem.getProvinceName()+","
                                        +poiItem.getCityName()+","
                                        +poiItem.getAdName()+","//区
                                        +poiItem.getSnippet()+","
                                        +poiItem.getLatLonPoint()+"\n");
                                tem.add(bean);
                            }
                            poiData.addAll(tem);
                            mAdapter.notifyDataSetChanged();
                        /* if (isSearch){
                                moveMapCamera(poiItems.get(0).getLatLonPoint().getLatitude(),poiItems.get(0).getLatLonPoint().getLongitude());
                        }*/
                        }
                    }
                }
            }
        }



        @Override
        public void onPoiItemSearched(PoiItem poiItem, int i) {

        }
    };

    public void openSearch(){
        Intent intent=new Intent(this,SearchAddressActivity.class);
        startActivityForResult(intent,REQUEST_SEARCH_CODE);
    }
    private PoiItem searchPonItem;//搜索结果得到
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==REQUEST_SEARCH_CODE&&resultCode==6){
            searchPonItem=data.getParcelableExtra("poiItem");
            String title = searchPonItem.getTitle();
            LatLonPoint latLonPoint = searchPonItem.getLatLonPoint();
            //移动标志和地图
            addmark(latLonPoint.getLatitude(),latLonPoint.getLongitude());
            mAMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latLonPoint.getLatitude(), latLonPoint.getLongitude()), 14));
            //重新搜索附近
            //数据清空
            isSearch=true;
            poiData.clear();
            mAdapter.notifyDataSetChanged();
            currentPage=0;
            mCity=searchPonItem.getCityCode();
            mLatitude=latLonPoint.getLatitude();
            mLongitude=latLonPoint.getLongitude();
            doSearchQuery(searchPonItem.getCityName(),latLonPoint.getLatitude(), latLonPoint.getLongitude());
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    private boolean isSearch;//是否为搜索的结果
    private void latSearchList(double latitude, double longitude) {
        //设置周边搜索的中心点以及半径
        GeocodeSearch geocodeSearch = new GeocodeSearch(this);
        //地点范围5000米
        RegeocodeQuery query = new RegeocodeQuery(new LatLonPoint(latitude, longitude), 5000, GeocodeSearch.AMAP);
        geocodeSearch.getFromLocationAsyn(query);
        geocodeSearch.setOnGeocodeSearchListener(new GeocodeSearch.OnGeocodeSearchListener() {
            @Override
            public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
                if (rCode == 1000) {
                    if (result != null && result.getRegeocodeAddress() != null
                            && result.getRegeocodeAddress().getFormatAddress() != null) {
                        //result.getRegeocodeAddress().getTownship()
                        String keyWord="汽车服务|汽车销售|汽车维修|摩托车服务|餐饮服务|购物服务|生活服务|体育休闲服务|医疗保健服务|住宿服务|风景名胜|商务住宅|政府机构及社会团体|科教文化服务|交通设施服务|金融保险服务|公司企业|道路附属设施|地名地址信息|公共设施";
                        searchList(result.getRegeocodeAddress().getCityCode(),keyWord);
                    }
                }
            }

            @Override
            public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {
            }
        });

    }


    @Override
    public void onMapClick(LatLng latLng) {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

//    @Override
//    public void onClick(View v) {
//
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，实现地图生命周期管理
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onRefresh() {
        lv_list.onRefreshComplete();
    }

    @Override
    public void onLoad() {
        currentPage++;
        doSearchQuery(mCity,mLatitude,mLongitude);
    }


    //搜索结果回调
    @Override
    public void onPoiSearched(PoiResult result, int rcode) {
        if (rcode == AMapException.CODE_AMAP_SUCCESS) {
            if (result != null && result.getQuery() != null) {// 搜索poi的结果
                if (result.getQuery().equals(query)) {// 是否是同一条
                    // 取得第一页的poiitem数据，页数从数字0开始
                    lv_list.onLoadComplete();
                    //如果是第一页得加入定位的这一行,并且不是搜索点击过来的
                    if(currentPage==0&&mLoc!=null&&!isSearch){
                        PoiBean firBean=new PoiBean();
                        firBean.setLoc(true);
                        firBean.setSelected(true);
                        firBean.setTitleName(mLoc.getPoiName());
                        firBean.setPoint(new LatLonPoint(mLoc.getLatitude(),mLoc.getLongitude()));
                        firBean.setLocAddress(mLoc.getAddress());
                        poiData.add(firBean);
                        mCurrPoiBean=firBean;
                    }else if(currentPage==0&&isSearch&&searchPonItem!=null){
                        PoiBean firBean=new PoiBean();
                        firBean.setTitleName(searchPonItem.getTitle());
                        firBean.setCityName(searchPonItem.getCityName());
                        firBean.setAd(searchPonItem.getAdName());
                        firBean.setSnippet(searchPonItem.getSnippet());
                        firBean.setPoint(searchPonItem.getLatLonPoint());
                        firBean.setSelected(true);
                        poiData.add(firBean);
                        mCurrPoiBean=firBean;
                    }
                    List<PoiItem> poiItems = result.getPois();// 取得第一页的poiitem数据，页数从数字0开始
                    List<SuggestionCity> suggestionCities = result
                            .getSearchSuggestionCitys();// 当搜索不到poiitem数据时，会返回含有搜索关键字的城市信息
                    List<PoiBean> tem=new ArrayList<>();
                    if (poiItems != null && poiItems.size() > 0) {
                        for (int i = 0; i < poiItems.size(); i++) {
                            PoiItem poiItem = poiItems.get(i);
                            PoiBean bean=new PoiBean();
                            bean.setTitleName(poiItem.getTitle());
                            bean.setCityName(poiItem.getCityName());
                            bean.setAd(poiItem.getAdName());
                            bean.setSnippet(poiItem.getSnippet());
                            bean.setPoint(poiItem.getLatLonPoint());
                            Log.e("yufs",""+poiItem.getTitle()+","+poiItem.getProvinceName()+","
                                    +poiItem.getCityName()+","
                                    +poiItem.getAdName()+","//区
                                    +poiItem.getSnippet()+","
                                    +poiItem.getLatLonPoint()+"\n");
                            tem.add(bean);
                        }
                        poiData.addAll(tem);
                        mAdapter.notifyDataSetChanged();
                     /*   if (isSearch){
                                moveMapCamera(poiItems.get(0).getLatLonPoint().getLatitude(),poiItems.get(0).getLatLonPoint().getLongitude());
                        }*/
                    }


                } else {
                    Toast.makeText(this, "搜索失败", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "搜索失败", Toast.LENGTH_SHORT).show();
            }
        }

    }



    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }
}
