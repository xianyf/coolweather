package com.coolweather.android.gson;

import com.google.gson.annotations.SerializedName;

/**  (1)

  "basic" :{
                "city":"苏州"
               "id":"CN101190401"
               "update":{
                        "loc":"2017-01-01  13:14"
                    }
             }

 @SerializedName :由于JOSN中的一些字段可能不太适合直接作为Java字段来命名 ,因此使用这个注解的方式 ,让
                JSON字段 和java字段之间建立映射关系
    其余几个实体类也是类似
 */
public class Basic {

    @SerializedName("city")
    public String cityName;

    @SerializedName("id")
    public String weatherId;

    public Update update;

    public class Update {

        @SerializedName("loc")
        public String updateTime;

    }

}
