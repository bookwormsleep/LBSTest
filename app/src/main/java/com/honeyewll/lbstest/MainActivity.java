package com.honeyewll.lbstest;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;

import java.util.ArrayList;
import java.util.IllegalFormatCodePointException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public LocationClient mLocationClient;
    private TextView positionText ;

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
        setContentView(R.layout.activity_main);
        positionText = (TextView) findViewById(R.id.position_text_view);
        /**
         * List<>集合是在运行时一可申请多个权限，如果没被授权就添加到List集合中，最后将List转换成数组，
         * 再调用ActivityCompat.requestPermissions()方法一次性申请。
         */
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (!permissionList.isEmpty()){
            String [] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this, permissions, 1);
        }else {
            requestLocation();
        }
    }

    /**
     * 调用start()方法就能开始定位，定位结果会回调到我们前面注册的监听器当中，也就是MyLocationListener.
     */
    private void requestLocation() {
        mLocationClient.start();//调用start()方法就能开始定位。
    }

    /**
     * onRequestPermissionsResult()方法对权限申请结果的逻辑处理和之前有所不同，
     * 这次通过一个for循环将申请的每个权限进行了判断，如果有任何一个权限被拒绝，那么就直接
     * 调用finish()方法关闭当前程序，只有所有权限被用户同意，才会调用requestLocation()方法开始地理位置定位。
     *
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        switch (requestCode){
            case 1 :
                if (grantResults.length > 0 ){
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "必须同意所有权限才能使用本程序",Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    requestLocation();//用户同意所有权限后才能调用requestLocation()方法
                }else {
                    Toast.makeText(this, "发生未知错误", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }
    public class MyLocationListener implements BDLocationListener{

        @Override
        public void onReceiveLocation(BDLocation Location) {
            StringBuilder currentPosition = new StringBuilder();
            currentPosition.append("纬度：").append(Location.getLatitude()).append("\n");
            currentPosition.append("经度: ").append(Location.getLongitude()).append("\n");
            currentPosition.append("定位方式：");
            if (Location.getLocType() == BDLocation.TypeGpsLocation){
                currentPosition.append("GPS") ;
            }else if (Location.getLocType() == BDLocation.TypeNetWorkLocation){
                currentPosition.append("网络");
            }
            positionText.setText(currentPosition);

        }
    }

}
