package com.example.wujian.rxjavademo.retrofit;

import java.util.List;

/**
 * Created by wujian on 2016/10/14.
 */

public class FamousInfo {


    public int total;
    public int error_code;
    public String reason;
    public List<ResultBean> result;

    public static class ResultBean {
        public String famous_name;
        public String famous_saying;
    }


}
