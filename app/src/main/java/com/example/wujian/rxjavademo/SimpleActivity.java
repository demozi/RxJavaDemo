package com.example.wujian.rxjavademo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.wujian.rxjavademo.retrofit.FamousInfo;
import com.example.wujian.rxjavademo.retrofit.FamousService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

public class SimpleActivity extends AppCompatActivity {

    private final String TAG = SimpleActivity.class.getSimpleName();

    TextView textView;

    private String key = "孔子";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple);

        textView = (TextView) findViewById(R.id.textView);

        Log.e(TAG, "ui thread------" + Thread.currentThread().getId());

        retrofitRx();
    }


    /*****************************   Retrofit  ***************************/

    /**
     * 利用concat，first操作符，先从缓存中取数据，如果没有取到则请求网络数据
     */
    private void retrofitRx() {

        Observable.concat(loadCache(), loadNet())
                .first(new Func1<FamousInfo, Boolean>() {
                    @Override
                    public Boolean call(FamousInfo famousInfo) {
                        if (famousInfo != null && famousInfo.result != null){
                            return true;
                        }
                        return false;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<FamousInfo>() {
                    @Override
                    public void onCompleted() {
                        Log.e(TAG, "------ onCompleted ------");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, e.toString());
                    }

                    @Override
                    public void onNext(FamousInfo famousInfo) {
                        if (famousInfo != null && famousInfo.result != null){
                            StringBuilder sb = new StringBuilder();
                            for (int i = 0; i < famousInfo.result.size(); i++) {
                                sb.append(famousInfo.result.get(i).famous_saying).append("\n");
                            }
                            textView.setText(sb.toString());
                        }
                    }
                });
    }


    private Observable<FamousInfo> loadNet() {
        //构建Retrofit 域名、支持Gson格式转换
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://apis.baidu.com")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();

        //获取请求接口
        FamousService famousService = retrofit.create(FamousService.class);
        return famousService.getFamousResultRx("4c4f0c3c49e09d5578ae0ba49fa84a97", key, 1, 3);
    }

    private Observable<FamousInfo> loadCache() {
        return Observable.create(new Observable.OnSubscribe<FamousInfo>() {
            @Override
            public void call(Subscriber<? super FamousInfo> subscriber) {

                Log.e(TAG, "load from disk------" + Thread.currentThread().getId());

                FamousInfo famousInfo = new FamousInfo();
                List<FamousInfo.ResultBean> resultBeenList = new ArrayList<>();
                for (int i = 0; i < 3; i++) {
                    FamousInfo.ResultBean resultBean = new FamousInfo.ResultBean();
                    resultBean.famous_name = "cache name " + i;
                    resultBean.famous_saying = "cache saying " + i;
                    resultBeenList.add(resultBean);
                }
                famousInfo.result = resultBeenList;

                subscriber.onNext(famousInfo);
                subscriber.onCompleted();
            }
        });
    }






}
