package com.huashe.lockscreen.network;

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Description:
 * created by Jamil
 * Time: 2019/5/15
 **/
public  class RetrofitHelper<T>{



    private static OkHttpClient mOkHttpClient;

    static {
        initOkHttpClient();
    }



    public static AppVersionService getHttpAPITest() {

        return createApi(AppVersionService.class, CONSTANTS.BaseUrl);
    }


    /**
     * 根据传入的baseUrl，和api创建retrofit
     */
    private static <T> T createApi(Class<T> clazz, String baseUrl) {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(mOkHttpClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit.create(clazz);
    }

    /**
     * 初始化OKHttpClient,设置缓存,设置超时时间,设置打印日志,设置UA拦截器
     */
    private static void initOkHttpClient() {

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        LoggingInterceptor loggingInterceptor=new LoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);

        if (mOkHttpClient == null) {
            synchronized (RetrofitHelper.class) {
                if (mOkHttpClient == null) {
                    //设置Http缓存
//                    Cache cache = new Cache(new File(savePath), 1024 * 1024 * 10);
                    mOkHttpClient = new OkHttpClient.Builder()
//                            .cache(cache)
                            .addInterceptor(interceptor)
                            .addInterceptor(loggingInterceptor)
//                            .addNetworkInterceptor(new CacheInterceptor())
//                            .addNetworkInterceptor(new StethoInterceptor())暂时不需要 需要时开启
                            .retryOnConnectionFailure(true)
                            .connectTimeout(8, TimeUnit.SECONDS)
                            .writeTimeout(8, TimeUnit.SECONDS)
                            .readTimeout(8, TimeUnit.SECONDS)
                            .addInterceptor(new UserAgentInterceptor())//暂时不需要 需要时开启
                            .build();
                }
            }
        }
    }

    /**
     * 添加UA拦截器，某些请求API需要加上UA才能正常使用
     */
    private static class UserAgentInterceptor implements Interceptor {

        @Override
        public Response intercept(Chain chain) throws IOException {

            Request originalRequest = chain.request();
            Request requestWithUserAgent = originalRequest.newBuilder()
                    .removeHeader("User-Agent")
                    .addHeader("User-Agent", "xxx")
                    .build();
            return chain.proceed(requestWithUserAgent);
        }
    }

//    /**
//     * 为okhttp添加缓存，这里是考虑到服务器不支持缓存时，从而让okhttp支持缓存
//     */
//    private static class CacheInterceptor implements Interceptor {
//
//        @Override
//        public Response intercept(@NonNull Chain chain) throws IOException {
//
//            // 有网络时 设置缓存超时时间1个小时
//            int maxAge = 60 * 60;
//            // 无网络时，设置超时为1天
//            int maxStale = 60 * 60 * 24;
//            Request request = chain.request();
//            if (NetStateUtils.isNetworkConnected(BaseApplication.getInstance())) {
//                //有网络时只从网络获取
//                request = request.newBuilder()
//                        .cacheControl(CacheControl.FORCE_NETWORK)
//                        .build();
//            } else {
//                //无网络时只从缓存中读取
//                request = request.newBuilder()
//                        .cacheControl(CacheControl.FORCE_CACHE)
//                        .build();
//            }
//            Response response = chain.proceed(request);
//            if (NetStateUtils.isNetworkConnected(BaseApplication.getInstance())) {
//                response = response.newBuilder()
//                        .removeHeader("Pragma")
//                        .header("Cache-Control", "public, max-age=" + maxAge)
//                        .build();
//            } else {
//                response = response.newBuilder()
//                        .removeHeader("Pragma")
//                        .header("Cache-Control", "public, only-if-cached, max-stale=" + maxStale)
//                        .build();
//            }
//            return response;
//        }
//    }




    public static class LoggingInterceptor implements Interceptor {
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();

            long t1 = System.nanoTime();
            //Log.v("发送请求: [%s] %s%n%s",
            request.url().toString();

            Response response = chain.proceed(request);

            long t2 = System.nanoTime();
//            System.out.println(String.format("接收响应: [%s] %.1fms%n%s",
//                    response.request().url(), (t2 - t1) / 1e6d, response.headers()));headers

            return response;
        }
    }



}
