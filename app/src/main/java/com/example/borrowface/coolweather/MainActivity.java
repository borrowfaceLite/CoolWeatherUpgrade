package com.example.borrowface.coolweather;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.bumptech.glide.Glide;
import com.example.borrowface.coolweather.db.CityList;
import com.example.borrowface.coolweather.util.HttpUtil;
import com.example.borrowface.coolweather.util.LBSCallback;
import com.example.borrowface.coolweather.util.LBSUtil;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends FragmentActivity {

    private static final String TAG = "MainActivity";

    public LocationClient mLoctionClient;
    List<Fragment> mFragments;
    ViewPager weatherView;
    FragAdapter fragAdapter;
    ImageView bingPicImg;
    String mWeatherId;
    public TextView titleCity;
    TextView updateTime;
    private List<String> permissonList;
    private int MAIN_CITY=1;
    private int OTHER_CITY=2;
    private Button homeButton;
    private int mCurrentPosition;
    private List<String> titleCityList;
    private List<String> updateTimeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT>=21){
            View decorView=getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        mLoctionClient = new LocationClient(getApplicationContext());
        mLoctionClient.registerLocationListener(new MylocationListener());
        bingPicImg = (ImageView) findViewById(R.id.bing_pic_img);
        permissonList=new ArrayList<>();
        mFragments=new ArrayList<>();
        weatherView= (ViewPager) findViewById(R.id.weather_view_pager);
        titleCity=findViewById(R.id.title_city);
        updateTime=findViewById(R.id.title_update_time);
        homeButton=findViewById(R.id.nav_button);
        com.github.clans.fab.FloatingActionButton fabAdd=findViewById(R.id.fab_add);
        com.github.clans.fab.FloatingActionButton fabDelete=findViewById(R.id.fab_delete);
        titleCityList=new ArrayList<>();
        updateTimeList=new ArrayList<>();

        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                weatherView.setCurrentItem(0);
            }
        });

        loadBingPic();

        //向fragment传递数据案例
//        Bundle args = new Bundle();
//        args.putString("text","Hello");
//        mFragments.add(WeatherFragment.newInstance(args));
        fragAdapter= new FragAdapter(getSupportFragmentManager(),mFragments);
        weatherView.setAdapter(fragAdapter);

        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this,ChooseAreaActivity.class);
                startActivityForResult(intent,1);
            }
        });
        fabDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mCurrentPosition>0){
                    DataSupport.deleteAll(CityList.class,"cityListName=?",
                            fragAdapter.getCurrentFragment(mCurrentPosition).cityName);
                    titleCity.setText(titleCityList.get(mCurrentPosition-1));
                    titleCityList.remove(mCurrentPosition);
                    weatherView.setCurrentItem(mCurrentPosition-1);
                    mFragments.remove(mCurrentPosition+1);
                    fragAdapter.notifyDataSetChanged();
                }else {
                    Toast.makeText(MainActivity.this,"不能删除主城市天气",Toast.LENGTH_SHORT).show();
                }
            }
        });

        weatherView.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                titleCity.setText(titleCityList.get(position));
//                titleCity.setText(fragAdapter.getCurrentFragment(position).cityName);
                updateTime.setText(fragAdapter.getCurrentFragment(position).updateTime);
                mCurrentPosition=position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            permissonList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            permissonList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            permissonList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissonList.isEmpty()) {
            String[] permissons = permissonList.toArray(new String[permissonList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this, permissons, 1);
        } else {
            requestLocation();
        }
    }

    private void loadBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                //保留图片地址，如果其他可以用到则不用再去请求网络
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(
                        MainActivity.this).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(MainActivity.this).load(bingPic).into(bingPicImg);
                    }
                });
            }
        });
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if (grantResults.length>0){
                    for (int result:grantResults){
                        if (result!=PackageManager.PERMISSION_GRANTED){
                            Toast.makeText(MainActivity.this,"必须同意所有权限申请",Toast.LENGTH_SHORT)
                                    .show();
                            finish();
                            return;
                        }
                    }
                    requestLocation();
                }else {
                    Toast.makeText(MainActivity.this,"出现未知错误",Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }


    public class MylocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(final BDLocation location) {
            String province = location.getProvince().split("省")[0];
            String city = location.getCity().split("市")[0];
            String county = location.getDistrict().split("区")[0];
            if (province != null && city != null && county != null) {
                LBSUtil lbsUtil = new LBSUtil(province, city, county, MainActivity.this,
                        new LBSCallback() {
                            @Override
                            public void onGetLoction(String weatherId) {
                                mWeatherId = weatherId;
                                requestWeatherByLBS(mWeatherId);
                            }
                        });
                titleCityList.add(county);
                titleCity.setText(county);
                lbsUtil.getWeatherId();
                List<CityList> cityLists= DataSupport.findAll(CityList.class);
                if(cityLists.size()>0){
                    for (CityList cityList:cityLists){
                        String weatherId=cityList.getWeatherListId();
                        String cityName=cityList.getCityListName();
                        mFragments.add(WeatherFragment.newInstance(weatherId,OTHER_CITY));
                        fragAdapter.notifyDataSetChanged();
                        titleCityList.add(cityName);
                    }
                }
            }else {
                Toast.makeText(MainActivity.this,"获取位置失败",Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void requestWeatherByLBS(final String mWeatherId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mFragments.add(WeatherFragment.newInstance(mWeatherId,OTHER_CITY));
                fragAdapter.notifyDataSetChanged();
            }
        });
    }

    public class FragAdapter extends FragmentStatePagerAdapter {
        List<Fragment> mFragments;
        public FragAdapter(FragmentManager fm, List<Fragment> mFragments) {
            super(fm);
            this.mFragments=mFragments;
        }

        @Override
        public int getItemPosition(Object object) {
            return PagerAdapter.POSITION_NONE;
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }


        public WeatherFragment getCurrentFragment(int position){
            return (WeatherFragment) mFragments.get(position);
        }
    }

    public void requestLocation() {
        initLocation();
        mLoctionClient.start();
    }

    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setIsNeedAddress(true);
        mLoctionClient.setLocOption(option);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case 1:
                if (resultCode==RESULT_OK){
                    String weatherId=data.getStringExtra("weatherId_return");
                    String countyName=data.getStringExtra("countyName_return");
                    mFragments.add(WeatherFragment.newInstance(weatherId,OTHER_CITY));
                    fragAdapter.notifyDataSetChanged();
                    titleCityList.add(countyName);
                    CityList cityList=new CityList();
                    cityList.setCityListName(countyName);
                    cityList.setWeatherListId(weatherId);
                    cityList.save();
                }
                break;
            default:
        }
    }
}
