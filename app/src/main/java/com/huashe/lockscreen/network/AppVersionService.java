package com.huashe.lockscreen.network;


import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;


public interface AppVersionService {


    /**
     * 验证码核实
     **/
//    @Headers({"Content-type:application/json;charset=utf-8","Accept:application/json"})
//    @POST("/jeeplus_ccds/sms/smscodevalid")
//    Observable<Object> checkRequestCode(@Body RequestBody json);

    @Headers({"Content-type:application/json;charset=utf-8","Accept:application/json"})
    @POST("smscodevalid")
    Observable<Object> checkRequestCode(@Query("imei") String imei,@Query("code") String code);


    /***
     * */
    @Headers({"Content-type:application/json;charset=utf-8","Accept:application/json"})
    @POST("smscodeupdate")
    Observable<Object> updateUseRecords(@Query("imei") String imei,@Query("code") String code);



}
