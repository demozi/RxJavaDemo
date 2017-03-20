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
import com.google.gson.Gson;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;
import okio.Sink;
import okio.Source;
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

public class MainActivity extends AppCompatActivity {

    private final String TAG = MainActivity.class.getSimpleName();

    EditText editText;
    TextView textView2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = (EditText) findViewById(R.id.editText);
        textView2 = (TextView) findViewById(R.id.textView2);

        Button btn = (Button) findViewById(R.id.button);
        Button btn1 = (Button) findViewById(R.id.button1);

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                write_okio();
            }
        });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               rxOperator6();
//                startActivity(new Intent(MainActivity.this, UserListActivity.class));
//                okhttpPost();
//               read_okio();
//                startActivity(new Intent(MainActivity.this, RxLifecycleSimpleActivity.class));
            }
        });
    }


    /*****************************   Retrofit  ***************************/

    private void retrofitRx() {
        //构建Retrofit 域名、支持Gson格式转换
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://apis.baidu.com")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();

        //获取请求接口
        FamousService famousService = retrofit.create(FamousService.class);
        famousService.getFamousResultRx("4c4f0c3c49e09d5578ae0ba49fa84a97", editText.getText().toString(), 1, 3)
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
                            textView2.setText(sb.toString());
                        }
                    }
                });
    }


    private void retrofit() {

        //构建Retrofit 域名、支持Gson格式转换
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://apis.baidu.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        //获取请求接口
        FamousService famousService = retrofit.create(FamousService.class);

        //传参
        Call<FamousInfo> call = famousService.getFamousResult("4c4f0c3c49e09d5578ae0ba49fa84a97", editText.getText().toString(), 1, 3);

        //执行网络请求
        call.enqueue(new Callback<FamousInfo>() {
            @Override
            public void onResponse(Call<FamousInfo> call, Response<FamousInfo> response) {

                Log.e(TAG, "thread" + Thread.currentThread().getId());

                if (response.isSuccessful()) {
                    FamousInfo famousInfo = response.body();
                    if (famousInfo != null && famousInfo.result != null){
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < famousInfo.result.size(); i++) {
                            sb.append(famousInfo.result.get(i).famous_saying).append("\n");
                        }
                        textView2.setText(sb.toString());
                    }
                }
            }

            @Override
            public void onFailure(Call<FamousInfo> call, Throwable t) {

            }
        });
    }



    /*****************************   OKHttp  ***************************/
    private void okhttp() {

        /**
         * http://www.jcodecraeer.com/a/anzhuokaifa/androidkaifa/2015/0326/2643.html
         */

        String url = "http://apis.baidu.com/avatardata/mingrenmingyan/lookup?" +
                "&keyword=" + editText.getText().toString() + "&page=1&rows=3";

        //创建OKHttpClient对象
        OkHttpClient httpClient = new OkHttpClient();

        //设置拦截器
        /*httpClient.interceptors().add(new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {

                okhttp3.Request  request = chain.request();
                //do something...

                okhttp3.Response response = chain.proceed(request);

                return response;
            }
        });*/


        //创建一个Request
        final Request request = new Request.Builder()
                .url(url)
                .header("apiKey", "4c4f0c3c49e09d5578ae0ba49fa84a97")
                .build();


        //封装request
        okhttp3.Call call = httpClient.newCall(request);

        //请求加入调度
        call.enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {

            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {

                Log.e(TAG, "thread" + Thread.currentThread().getId());

                String res = response.body().string();
//                response.body().bytes();
//                response.body().byteStream();

                //解析数据
                Gson gson = new Gson();
                final FamousInfo famousInfo = gson.fromJson(res, FamousInfo.class);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (famousInfo != null && famousInfo.result != null){
                            StringBuilder sb = new StringBuilder();
                            for (int i = 0; i < famousInfo.result.size(); i++) {
                                sb.append(famousInfo.result.get(i).famous_saying).append("\n");
                            }
                            textView2.setText(sb.toString());
                        }
                        Log.e(TAG, "thread" + Thread.currentThread().getId());
                    }
                });

            }
        });
    }


    private void okhttpPost() {

        //创建OKHttpClient对象
        OkHttpClient httpClient = new OkHttpClient();

        okhttp3.RequestBody requestBody = new FormBody.Builder()
                .add("act", "mall_unlimited_action")
                .add("op", "get_mall_activities_page")
                .add("client_type", "android")
                .add("curpage", "1")
                .build();

        //创建一个Request
        final Request request = new Request.Builder()
                .url("http://malldev.aigegou.com/mobile/index.php")
                .post(requestBody)
                .build();


        //封装request
        okhttp3.Call call = httpClient.newCall(request);

        //请求加入调度
        call.enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {

            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {

                Log.e(TAG, "thread" + Thread.currentThread().getId());

                final String res = response.body().string();
//                response.body().bytes();
//                response.body().byteStream();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        textView2.setText(res);
                    }
                });

            }
        });
    }


    /*****************************   OKio  ***************************/
    /**
     * Sink     BufferedSink    RealBufferedSink
     * Source   BufferedSource  RealBufferedSource
     * Segment SegmentPool  双向列表
     * TimeOut  超时机制
     * Buffer  读写操作核心
     *
     * http://www.jianshu.com/p/f033a64539a1?nomobile=yes
     */
    private void write_okio() {
        Sink sink = null;
        BufferedSink bufferedSink = null;
        File file = new File(getCacheDir().getPath() + "/test.txt");
         try {
             if (!file.exists()) file.createNewFile();
//             sink = Okio.appendingSink(file); 追加数据
             sink = Okio.sink(file);
             bufferedSink = Okio.buffer(sink);
             bufferedSink.writeUtf8(editText.getText().toString());

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeQuietly(bufferedSink);
         }

    }

    private void read_okio() {
        Source source = null;
        BufferedSource bufferedSource = null;
        File file = new File(getCacheDir().getPath() + "/test.txt");
        try {
            source = Okio.source(file);
            bufferedSource = Okio.buffer(source);
            String data = bufferedSource.readUtf8();
            textView2.setText(data);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            textView2.setText("FileNotFoundException");
        } catch (IOException e) {
            e.printStackTrace();
            textView2.setText("IOException");
        } finally {
            closeQuietly(bufferedSource);
        }

    }


    private void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /*****************************   RxJava  ***************************/

    private void testRxJava() {
        //1.创建Observable
        Observable<String> observable = Observable.create(new Observable.OnSubscribe<String>(){

            @Override
            public void call(Subscriber<? super String> subscriber) {
                subscriber.onNext("Hello world!");
                subscriber.onCompleted();
            }
        });

        //2.创建Subscriber
        Subscriber<String> subscriber = new Subscriber<String>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(String s) {
                Log.e("MainActivity", s + Thread.currentThread().getId());
            }
        };

        //3.订阅Observable
        observable.subscribe(subscriber);
    }

    /**
     *  创建只发出一个事件就结束的Observable对象
     */
    private void rxJust() {
       /* Observable<String> observable = Observable.just("Hello World!");

        Action1<String> action1 = new Action1<String>() {
            @Override
            public void call(String s) {
                Log.e("MainActivity", s + Thread.currentThread().getId());
            }
        };

        observable.subscribe(action1);*/

        Observable.just("Hello", "World")
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        Log.e("MainActivity", s + Thread.currentThread().getId());
                    }
                });
    }

    /**
     * 操作符  ： 用于在Observable和最终的Subscriber之间修改Observable发出的事件
     *
     * map操作符不仅可以改变Observable发出事件的值，还能改变他的类型
     */
    private void rxOperators() {
        Observable.just("Hello world")
                .map(new Func1<String, String>() {
                    @Override
                    public String call(String s) {
                        //改变Observable发出的事件
                        return s + " RxJava";
                    }
                })
                .map(new Func1<String, Integer>() {
                    @Override
                    public Integer call(String s) {
                        return s.hashCode();
                    }
                })
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer s) {
                        Log.e("MainActivity", s + "");
                    }
                });
    }

    /**
     * from  把集合中的元素一次传给Subscriber
     */
    private void rxOperators1() {
        List<String> list = Arrays.asList("Android", "Java", "iOS", "Swift");
        Observable.from(list)
                .map(new Func1<String, String>() {
                    @Override
                    public String call(String s) {
                        return "hello " + s ;
                    }
                })
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        Log.e("MainActivity", s);
                    }
                 });
    }


    /**
     * flatMap 与 map 不同的是可以直接改变最终的Subscriber关联的Observable
     */
    private void rxOperators2() {
        List<String> list = Arrays.asList("Android", "Java", "iOS", "Swift");
        Observable.from(list)
                .flatMap(new Func1<String, Observable<String>>() {
                    @Override
                    public Observable<String> call(String s) {
                        return Observable.just("Hello " + s);
                    }
                })
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String o) {
                        Log.e("MainActivity", o);
                    }
                });
    }

    private Observable createObservable() {
        List<String> list = Arrays.asList("Android", "Java", "iOS", "Swift");
        return Observable.from(list);
    }

    /**
     * 用于创建Observable的操作符
     * create
     * defer
     * empty/never/throw
     * from
     * interval
     * just
     * range
     * timer  创建一个延时发射的Observable事件
     *
     * 更多操作符： http://reactivex.io/documentation/operators.html
     */

    private void rxOperators3() {
        Observable.timer(3, TimeUnit.SECONDS)
                .flatMap(new Func1<Long, Observable<?>>() {
                    @Override
                    public Observable<String> call(Long aLong) {
                        return createObservable();
                    }
                })
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        Log.e("MainActivity", o.toString());
                    }
                });
    }


    /**
     * filter 对发出的Observable事件进行过滤
     */
    private void rxOperator4() {
        List<String> list = Arrays.asList("Android", "Java", "iOS", "Swift");
        Observable.from(list)
                .filter(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(String s) {
                        if (s.equals("Android") || s.equals("Java")) {
                            return true;
                        }
                        return false;
                    }
                })
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        Log.e("MainActivity", s);
                    }
                });
    }

    /**
     * take 对Observable发出的事件进行提取前n个进行处理
     * TakeLast  提取后n个进行处理
     */
    private void rxOperator5() {
        List<String> list = Arrays.asList("Android", "Java", "iOS", "Swift");
        Observable.from(list)
                .take(1)
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        Log.e("MainActivity", s);
                    }
                });
    }

    /**
     * merge: 合并多个Observable，可能会交叉执行
     * concat: 合并多个Observable，只能顺序执行
     */
    private void rxOperator6() {
        Observable<Integer> odds = Observable.just(1).subscribeOn(Schedulers.newThread());
        Observable<Integer> evens = Observable.just(2).subscribeOn(Schedulers.newThread());

        Observable<Integer> other = Observable.just(0).subscribeOn(Schedulers.newThread());
        Observable<Integer> other2 = Observable.just(9).subscribeOn(Schedulers.newThread());

        Observable.merge(odds, evens, other, other2)
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer integer) {
                        Log.e("MainActivity", "merge----"  + String.valueOf(integer));
                    }
                });

        Observable.concat(odds, evens, other, other2)
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer integer) {
                        Log.e("MainActivity", "concat----" + String.valueOf(integer));
                    }
                });
    }

    /**
     * zip 将每个Observable事件进行成对合并，不成对的事件将被抛弃
     *
     * combineLatest  不太明白
     */
    private void rxOperator7() {
        Observable<Integer> odds = Observable.just(1, 2).subscribeOn(Schedulers.newThread());
        Observable<Integer> evens = Observable.just(2, 4, 6).subscribeOn(Schedulers.newThread());

        Observable.zip(evens, odds, new Func2<Integer, Integer, Integer>() {
            @Override
            public Integer call(Integer integer, Integer integer2) {

                return integer + integer2;
            }
        }).subscribe(new Action1<Integer>() {
            @Override
            public void call(Integer integer) {
                Log.e("MainActivity", "zip----" + String.valueOf(integer)); //3, 7, 11
            }
        });

        Observable.combineLatest(odds, evens, new Func2<Integer, Integer, Integer>() {
                    @Override
                    public Integer call(Integer integer, Integer integer2) {
                        return integer + integer2;
                    }
                })
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer integer) {
                        Log.e("MainActivity", "combineLatest----" + String.valueOf(integer));  //7, 9, 11
                    }
                });
    }


    /**
     * 取发出的事件中第一个符合条件的事件进行处理。 check one by one
     */
    private void rxOperator8() {
        Observable<Integer> observable1 = Observable.just(1);
        Observable<String> observable2 = Observable.just("hello");
        Observable<Integer> observable3 = Observable.just(3);

        Observable.concat(observable1, observable2, observable3)
                .first(new Func1<Serializable, Boolean>() {
                    @Override
                    public Boolean call(Serializable serializable) {
                        try{
                            String str = (String) serializable;
                            if (str != null && str.equals("hello")) {
                                return true;
                            }
                        }catch (Exception e) {
                            e.printStackTrace();
                        }
                        return false;
                    }
                }).subscribe(new Action1<Serializable>() {
                    @Override
                    public void call(Serializable serializable) {
                        Log.e(TAG, serializable.toString());
                    }
                });

       /* Observable.just(1, 2, 3)
                .first(new Func1<Integer, Boolean>() {
                    @Override
                    public Boolean call(Integer integer) {
                        if (integer > 1) {
                            return true;
                        }
                        return false;
                    }
                })
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer integer) {
                        Log.e(TAG, String.valueOf(integer));
                    }
                });*/
    }


    /***  线程控制 Scheduler **/
    private void rxScheduler() {
        Observable.create(new Observable.OnSubscribe<String>() {
                    @Override
                    public void call(Subscriber<? super String> subscriber) {
                        Log.e("MainActivity", "thread----" + Thread.currentThread().getId());
                        subscriber.onNext("observer");
                    }
                })
                .subscribeOn(Schedulers.io())       //指定订阅事件发射所在线程
                .observeOn(AndroidSchedulers.mainThread())  //指定订阅事件消费所在线程
                .subscribe(new Observer<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(String s) {
                        Log.e("MainActivity", s + "thread----" + Thread.currentThread().getId());
                    }
                });

    }

}
