package com.coolweather.android;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

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
public class WeatherActivity extends AppCompatActivity {

    private ScrollView weatherLayout;

    private Button navButton;

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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        // 初始化各控件
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
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather", null);
        if (weatherString != null) {
            // 有缓存时直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
            showWeatherInfo(weather);
        } else {
            // 无缓存时去服务器查询天气
            String weatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
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
                    }
                });
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
