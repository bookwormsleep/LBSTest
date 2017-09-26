package com.honeyewll.lbstest;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;
import java.util.IllegalFormatCodePointException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public LocationClient mLocationClient;
    private TextView positionText;
    private MapView mapView;
    private BaiduMap baiduMap;  //BaiduMap地图的总控制器
    private boolean isFirstLocate = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /**
         * 创建LocationClient的实例，LocationClient的构造函数接收一个Content参数，
         * 调用getApplicationContext()方法来获取一个全局的Content参数并传入
         */
        mLocationClient = new LocationClient(getApplicationContext()); //创建LocationClient的实例
        /**
         * 调用LocationClient的registerLocationListener()方法注册一个定位监听器，
         * 当获取到位置的时候，就回调这个定位监听器
         */
        mLocationClient.registerLocationListener(new MyLocationListener());
        SDKInitializer.initialize(getApplicationContext());  //调用SDKInitializer的initialize()方法进行初始化操作
        setContentView(R.layout.activity_main);
        mapView = (MapView) findViewById(R.id.bmapView);  //获取视图
        baiduMap = mapView.getMap();   //调用MapView的getMap()方法获取BaiduMap的实例
        baiduMap.setMyLocationEnabled(true);//封装设备当前所在位置
        positionText = (TextView) findViewById(R.id.position_text_view);
        /**
         * List<>集合是在运行时一可申请多个权限，如果没被授权就添加到List集合中，最后将List转换成数组，
         * 再调用ActivityCompat.requestPermissions()方法一次性申请。
         */
        List<String> permissionList = new ArrayList<>();
        /**
         * PERMISSION_GRANTED 许可授权
         * ACCESS_FINE_LOCATION  获取 好 位置
         * WRITE_EXTERNAL_STORAGE  写 外部 存储器
         * READ_PHONE_STATE  读取 手机 状态
         */
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);//获得好位置
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (!permissionList.isEmpty()) {
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this, permissions, 1);
        } else {
            requestLocation();
        }
    }

    /**
     * 调用start()方法就能开始定位，定位结果会回调到我们前面注册的监听器当中，也就是MyLocationListener.
     */
    private void requestLocation() {
        initLocation();        //确保移动中能获取到实时位置，回调方法
        mLocationClient.start();//调用start()方法就能开始定位。
    }

    /**
     * initLocation()方法确保移动中能获取到实时位置
     * 创建LocationClientOption对象，调用它的setScanSpan()方法来设置更新的间隔，5000表示每5秒更新一下当前位置
     */
    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setScanSpan(5000);       //设置扫描周期
        option.setIsNeedAddress(true);  //传入true获取当前位置详细的地址信息
        /**
         * 调用setLocationMode()方法来将定位模式指定传感器模式，GPS定位
         * Device_Sensors  设备传感器
         */
        option.setLocationMode(LocationClientOption.LocationMode.Device_Sensors); //使用GPS定位
        mLocationClient.setLocOption(option);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    /**
     * 活动销毁的时候一定要调用LocationClient的stop()方法来停止定位，
     * 不然程序会在后台持续定位，消耗手机的电量。
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();
        mapView.onDestroy();
        baiduMap.setMyLocationEnabled(false);
    }

    /**
     * onRequestPermissionsResult()方法对权限申请结果的逻辑处理和之前有所不同，
     * 这次通过一个for循环将申请的每个权限进行了判断，如果有任何一个权限被拒绝，那么就直接
     * 调用finish()方法关闭当前程序，只有所有权限被用户同意，才会调用requestLocation()方法开始地理位置定位。
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "必须同意所有权限才能使用本程序", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    requestLocation();//用户同意所有权限后才能调用requestLocation()方法
                } else {
                    Toast.makeText(this, "发生未知错误", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }

    /**
     * 有了BaiduMap后就能对地图进行各种各样的操作，比如设置地图的缩放级别以及将地图移动到某个经纬度上
     * @param location
     */
    private void navigateTo(BDLocation location) {
        if (isFirstLocate) {//isFirstLocate变量作用是为了防止多次调用animateMapStatus()方法
            /**
             *LatLng类主要用于存放经纬度，它的构造方法接收两个参数，第一个是纬度值，第二个是经度值。
             * 调用MapStatusUpdateFactory的newLatLng()方法将LatLng对象传入，
             * newLatLng()方法返回一个MapStatusUpdate对象，我们把这个对象传入BaiduMap的animateMapStatus()方法中。就能在地图上指定经纬度了
             */
            LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());//LanLng()方法接收经纬度参数
            MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);//调用MapStatusUpdateFactory的newLatLng()方法将LatLng对象传入，
            baiduMap.animateMapStatus(update);//把MapStatusUpdate对象传入BaiduMap的animateMapStatus()方法中完成指定经纬度
            /**
             * MapStatusUpdateFactory的zoomTo()方法接收一个float型的参数，用于设置绽放级别，传入16.
             * zoomTo()方法返回MapStatusUpdate对象，再把这个对象传入BaiduMap的animateMapStatus()方法中即可完成缩放
             */
            update = MapStatusUpdateFactory.zoomTo(16f);//缩放级别3到19之间，值越大，地图信息越精细
            baiduMap.animateMapStatus(update);//把MapStatusUpdate对象传入BaiduMap的animateMapStatus()方法中完成缩放
            isFirstLocate = false;
        }
        /**
         * 在novigateTo()方法中，我们添加了MyLocationData的构造逻辑
         * 将Location中包含的经度和纬度分别封装到MyLocationDate.Builder当中
         * 最后把MyLocationData设置到了BaiduMap的setMyLocationData()方法当中。
         * 注意这段逻辑必须写在ifFirstLocate这个if条件语句的外面，因为第一次只需要定位，后面设备
         * 在地图上显示的位置却是随着设备的移动而实时改变
         */
        MyLocationData.Builder locationBuilder = new MyLocationData.Builder();
        locationBuilder.latitude(location.getLatitude());
        locationBuilder.longitude(location.getLongitude());
        MyLocationData locationData = locationBuilder.build();
        baiduMap.setMyLocationData(locationData);
    }


    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            /**StringBuilder currentPosition = new StringBuilder();
             currentPosition.append("纬度：").append(Location.getLatitude()).append("\n");
             currentPosition.append("经度: ").append(Location.getLongitude()).append("\n");
             currentPosition.append("国家：").append(Location.getCountry()).append("\n");
             currentPosition.append("省：").append(Location.getProvince()).append("\n");
             currentPosition.append("市：").append(Location.getCity()).append("\n");
             currentPosition.append("区：").append(Location.getDistrict()).append("\n");
             currentPosition.append("街道：").append(Location.getStreet()).append("\n");
             currentPosition.append("定位方式：");
             if (Location.getLocType() == BDLocation.TypeGpsLocation){
             currentPosition.append("GPS") ;
             }else if (Location.getLocType() == BDLocation.TypeNetWorkLocation){
             currentPosition.append("网络");
             }
             positionText.setText(currentPosition);*/
            /**
             *
             */
            if (location.getLocType() == BDLocation.TypeGpsLocation|| location.getLocType() == BDLocation.TypeNetWorkLocation){
                    navigateTo(location);
            }
        }
    }
}
