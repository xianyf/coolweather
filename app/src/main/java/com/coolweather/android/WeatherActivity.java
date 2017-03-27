package com.coolweather.android;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.coolweather.android.gson.Forecast;
import com.coolweather.android.gson.Weather;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**  天气界面
 布局: 将页面不同部分写在不同的布局文件 , 最后 使用引入布局 的方式,集成到activity_weather.xml
    title.xml    标题布局
    now.xml     当前天气信息布局
    forecast.xml   未来几天天气布局
    forecast_item.xml  未来天气信息的子项布局
    aqi.xml     空气质量信息布局
    suggestion.xml   生活建议信息的布局

 把它们全部引入weather布局 :
     外层使用FrameLayout,嵌套一个ScrollView,由于ScrollView内部只允许一个子布局,因此
 又嵌套一个垂直方向的LinearLayout,然后将所有布局引入.
 */



//-------------------------------------------------------------------------------------------------


/**  将天气显示到界面上
111
   需要在Utility类中添加一个 解析天气 JSON 数据的方法  :将返回的JSON数据解析成Weather实体类

222
   接下来就是在活动中请求天气数据 ,和 显示数据
 (1) oncreate()方法获取控件实例 ,尝试从本地缓存中读取天气数据  ((有就直接解析, 没有就网络请求))
 (2) 第一次肯定是没有缓存的 , 因此就要从Intent 取出天气 id ,并调用requestWeather(weatherId) 方法取服务器
        注意,请求数据的时候 先将 ScrollView隐藏 ,因为空数据的界面 看上去很奇怪 ,将ScrollView隐藏
 (3) 在requestWeather() 参数传入天气id + APIkey 就是接口地址, 接着就是HttpUtil.sendOkHttpRequest()向
    该地址发请求,服务器返回相应城市的天气信息 以JSON格式
 (4) 然后在onResponse() 回调使用Utility 的HandlerWeatherResponse() 方法将 JSON数据转换成 Weather对象,
 (5)  将线程切换回主线程, 判断 , 返回的status 状态 ok ,说明请求成功
 (6) 将返回的数据缓存到 SharePreference 当中, 并创建一个 showWeatherInfo() 方法 进行内容显示
 (7) showWeatherInfo() 方法 就是 从Weather 对象 中获取数据, 然后显示到相应的控件上
 (8) 未来几天天气预报部分 ,我们使用了一个for循环来处理每天的天气信息 ,在循环中动态加载forecast_item.xml布局
    并设置相应的数据 ,然后添加到父布局当中
    设置完所有的数据后 , 记得要将ScrollView 重新变成可见

333
   从省市县列表界面 跳转到 天气界面的 代码
   ~ onItemClick()方法中加入 一个 if 判断 ,如果当前级别是 LEVEL_COUNTY ,就启动WeatherActivity, 并把天气id传过来

444
   另外 ,还需要在MainActivity 中 加入一个 缓存数据 的判断才行 //
 */


//-------------------------------------------------------------------------------------------------


/**  每日一图
  [0] 在FrameLayout布局下,添加一个ImageView,宽高match_parent, 默认情况都是将控件放置左上角,因此ScrollView完全覆盖ImageView, ImageView就成了背景图
 FrameLayout 是一层一层叠的
  [1] 首先在onCreate()方法中获取新增控件 ImageView 的实例 ,然后尝试从SharePreference 中读取缓存的背景图片
    如果有缓存的话 ,就直接使用Glide 来加载这张图片 ,如果没有的话就调用 loadBingPic() 方法去请求每日必应背景图
  [2] loadBingPic() 方法中 ,先调用 HttpUtil.sendOkHttpRequest() 方法获取到必应背景图的连接 ,然后将这个链接
    缓存到 SharePreference当中, 再将当前线程 切换到主线程




 ** 将背景图和状态栏融合?
 onCreate加载布局时 加入:
            if (Build.VERSION.SDK_INT >= 21) {
                    View decorView = getWindow().getDecorView();
                   decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
                    getWindow().setStatusBarColor(Color.TRANSPARENT);}

 **状态栏 是否与 头布局 紧贴在一起?
 怎么为状态栏单独留出空间?
   weather.xml 里面 的 ScrollView 的 LinearLayout  设置  fitsSystemWindows = "true"

 */


//--------------------------------------------------------------------------------------------------

/** 下拉刷新 功能  (SwipeRefreshLayout)
 1.在ScrollView布局外面 嵌套 一层 SwipeRefreshLayout ,这样ScrollView就自动 拥有下拉刷新 功能
 2.onCreate获取SwipeRefreshLayout的实例,然后调用 setColorSchemeResource()方法 设置 下拉刷新进度条颜色(colorPrimary)
 3.接着调用 setOnRefreshListener() 方法来设置 一个下拉刷新的监听器
  当触发下拉刷新操作的时候,回调监听器的onRefresh()方法 ,我们这里调用requestWeather()方法 [这里需要一个 天气id weather]
 一种是 定义一个私有变量
 一种是 外部直接String变量 ,由于内部类需要使用该变量,所以还需要 final 修饰
 4.不要忘记 ,请求结束后,还需要SwipeRefreshLayout 的 setRefreshing() 方法并 传入false ,用于刷新事件结束,把刷新进度条隐藏




 ** 切换城市 功能  (DrawerLayout)
 1.既然是切换城市功能 , 之前在ChooseAreaFragment 已经实现过了 ,现在其实需要在WeatherActivity 引入这个碎片, 就可以快速集成切换城市功能
   (之前考虑为了后面的复用 ,特地选择在碎片当中实现)
 2.使用DrawerLayout / 把碎片放入 滑动菜单 是最合适
   2.1 首先按照 Material Design 的建议, 需要在头布局 加入一个 "切换城市"按钮 ,让用户知道屏幕左边缘是可以拖动的
   2.2 修改 title.xml 加入一个 button ...(之前已经加了)
   2.3 修改 activity_weather.xml 布局来加入滑动菜单功能 (在SwipeRefreshLayout的外面 又嵌套了一层 DrawerLayout)
        DrawerLayout 第一个子控件作为主屏幕显示的内容  ;  第二个子控件用于作为 滑动菜单中显示的内容
        因此 ,在第二个子控件的位置 添加了 用于遍历 省市县数据的碎片
   2.4 在WeatherActivity 处理加入滑动菜单的逻辑 ;
        onCreate()方法获取 Btton和 DrawerLayout实例
        Button点击事件中 调用 DrawerLayout的 openDrawer(GravityCompat.START)方法 打开滑动菜单 即可
 3.还没有结束 ..上面仅仅只是在打开 滑动菜单而已 ,还需要处理切换城市后的逻辑
   3.1 在ChooseAreaFragment中进行,因为之前是选中了某个城市才跳转到 WeatherActivity ,由于现在本身就是在WeatherActivity中,不需要跳转
       (只需要请求新选择城市 的天气信息 )
   3.2 根据ChooseAreaFragment 的不同状态 进行不同的逻辑处理 :
            if (getActivity() instanceof MainActivity)
        else if (getActivity() instanceof WeatherActivity)     ---判断出碎片是在MainActivity中 还是 WeatherLayout中
    #.----------------instanceof: 用来判断一个对象是否属于某个类的实例------------------
      如果是在MainActivity中,处理逻辑不变
      如果是在WeatherActivity中, 就关闭滑动菜单,显示下拉刷新进度条,然后请求新城市天气信息

 现在点击按钮或者滑动左侧边缘 ,滑动菜单界面会显示出来;
 你可以切换其他省市县 ,当最后选中"县"这个级别的城市时,滑动菜单会关闭,显示下拉刷新进度条,然后会请求新的城市天气信息 ,这样天气界面的信息会更新成你选中的城市
  */

public class WeatherActivity extends AppCompatActivity {

//  滑动控件
    public DrawerLayout drawerLayout;
    private Button navButton;

//  下拉刷新控件
    public SwipeRefreshLayout swipeRefresh;
    private String weatherId;                     //私有成员变量,  天气id

    private ScrollView weatherLayout;

    private TextView titleCity;

    private TextView titleUpdateTime;

    private TextView degreeText;

    private TextView weatherInfoText;

    private LinearLayout forecastLayout;

    private TextView aqiText;

    private TextView pm25Text;

    private TextView comfortText;

    private TextView carWashText;

    private TextView sportText;

    private ImageView bingPicImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //背景图融合状态栏
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();                            //当前veiw
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN  //改UI显示 :这里表示活动布局显示在状态栏上面
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);                       //状态栏设置成透明色
        }
        setContentView(R.layout.activity_weather);



        // 初始化各控件
        bingPicImg = (ImageView) findViewById(R.id.bing_pic_img);

        weatherLayout = (ScrollView) findViewById(R.id.weather_layout);
        titleCity = (TextView) findViewById(R.id.title_city);
        titleUpdateTime = (TextView) findViewById(R.id.title_update_time);
        degreeText = (TextView) findViewById(R.id.degree_text);
        weatherInfoText = (TextView) findViewById(R.id.weather_info_text);
        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        aqiText = (TextView) findViewById(R.id.aqi_text);
        pm25Text = (TextView) findViewById(R.id.pm25_text);
        comfortText = (TextView) findViewById(R.id.comfort_text);
        carWashText = (TextView) findViewById(R.id.car_wash_text);
        sportText = (TextView) findViewById(R.id.sport_text);

//    下拉控件
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather", null);


//      final String weatherId;                                          在类中定义 String  weatherId 也可以
        if (weatherString != null) {
            // 有缓存时直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
              weatherId = weather.basic.weatherId;
            showWeatherInfo(weather);
        } else {
            // 无缓存时去服务器查询天气  (第一次,肯定是从传入的Intent 参数 获取 weatherId)
             weatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
        }

//   下拉刷新
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(weatherId);                                  //但是 一旦在 内部类 使用 变量 ,外部类定义应该fianl修饰
            }
        });

//   滑动菜单
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navButton = (Button) findViewById(R.id.nav_button);
        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });



        String bingPic = prefs.getString("bing_pic", null);
        if (bingPic != null) {
            Glide.with(this).load(bingPic).into(bingPicImg);
        } else {
            loadBingPic();
        }
    }


    /**
     * 根据天气id请求城市天气信息。
     */
    public void requestWeather(final String weatherId) {
        String weatherUrl = "https://api.heweather.com/x3/weather?cityid=" + weatherId + "&key=bc0418b57b2d4918819d3974ac1285d9";

        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)) {

                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather", responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                        } else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        }
//   请求结束, 刷新事件也跟着结束 并且隐藏刷新进度条
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
//   请求结束, 刷新事件也跟着结束 并且隐藏刷新进度条
                        swipeRefresh.setRefreshing(false);
                    }

                });
            }
        });

        loadBingPic();
    }



    /**
     * 加载必应每日一图
     */
    private void loadBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();

            //SharePreference存
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();

            //切换回主线程
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
        });
    }



    /**
     * 处理并展示Weather实体类中的数据。
     */
    private void showWeatherInfo(Weather weather) {
        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.temperature + "℃";
        String weatherInfo = weather.now.more.info;
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();

        for (Forecast forecast : weather.forecastList) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);
            TextView dateText = (TextView) view.findViewById(R.id.date_text);
            TextView infoText = (TextView) view.findViewById(R.id.info_text);
            TextView maxText = (TextView) view.findViewById(R.id.max_text);
            TextView minText = (TextView) view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            forecastLayout.addView(view);
        }

        if (weather.aqi != null) {
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }
        String comfort = "舒适度：" + weather.suggestion.comfort.info;
        String carWash = "洗车指数：" + weather.suggestion.carWash.info;
        String sport = "运行建议：" + weather.suggestion.sport.info;
        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);
    }

}
