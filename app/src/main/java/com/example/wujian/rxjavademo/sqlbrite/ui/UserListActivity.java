package com.example.wujian.rxjavademo.sqlbrite.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wujian.rxjavademo.R;
import com.example.wujian.rxjavademo.retrofit.FamousInfo;
import com.example.wujian.rxjavademo.retrofit.FamousService;
import com.example.wujian.rxjavademo.sqlbrite.db.DbService;
import com.example.wujian.rxjavademo.sqlbrite.entity.Person;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class UserListActivity extends AppCompatActivity {

    private final String TAG = UserListActivity.class.getSimpleName();


    EditText name_txt;
    EditText age_txt;

    Button add_btn;
    Button delete_btn;

    ListView listview;

    DbService dbService;

    List<Person> personList;
    UserAdapter mAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        dbService = new DbService(this);
        personList = new ArrayList<>();


        name_txt = (EditText) findViewById(R.id.name_txt);
        age_txt = (EditText) findViewById(R.id.age_txt);

        add_btn = (Button) findViewById(R.id.add_btn);
        delete_btn = (Button) findViewById(R.id.delete_btn);

        listview = (ListView) findViewById(R.id.listview);


        add_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPerson();
            }
        });

        delete_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deletePerson();
            }
        });

        queryPersons();
    }


    /**
     * 通过观察者模式， 在对数据库进行添加或删除操作后会自动调用查询操作，刷新界面
     */
    private void queryPersons() {
        dbService.queryPerson().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Person>>() {
                    @Override
                    public void call(List<Person> persons) {
                        //刷新数据
                        personList.clear();
                        personList.addAll(persons);
                        if (mAdapter == null) {
                            mAdapter = new UserAdapter();
                            listview.setAdapter(mAdapter);
                        }else {
                            mAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }



    private void addPerson() {
        String name = name_txt.getText().toString();
        String age = age_txt.getText().toString();

        if (TextUtils.isEmpty(age)) {
            Toast.makeText(this, "请输入年龄!", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "请输入姓名!", Toast.LENGTH_SHORT).show();
        } else {
            Person person = new Person();
            person.setAge(Integer.valueOf(age));
            person.setName(name);
            dbService.addPerson(person);
        }
    }

    private void deletePerson(){
        String name = name_txt.getText().toString();
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "请输入姓名!", Toast.LENGTH_SHORT).show();
        } else {
            dbService.deletePersonByName(name);
        }
    }



    class UserAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return personList.size();
        }

        @Override
        public Object getItem(int position) {
            return personList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_list, parent, false);
                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            }
            viewHolder = (ViewHolder) convertView.getTag();

            Person person = personList.get(position);

            viewHolder.nametxt.setText(person.getName());
            viewHolder.agetxt.setText(String.valueOf(person.getAge()));

            return convertView;
        }

        class ViewHolder {
            TextView nametxt;
            TextView agetxt;

            ViewHolder(View v) {
                nametxt = (TextView) v.findViewById(R.id.nametxt);
                agetxt = (TextView) v.findViewById(R.id.agetxt);
            }
        }
    }
}
