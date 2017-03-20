package com.example.wujian.rxjavademo.sqlbrite.db;

import android.content.ContentValues;
import android.database.Cursor;

import com.example.wujian.rxjavademo.sqlbrite.entity.Person;

import rx.functions.Func1;

/**
 * Created by wujian on 2016/10/21.
 */

public class WJDb {

    public static class PersonTable {
        // 表名
        public static final String TABLE_NAME = "person";

        // 表字段
        public static final String ID = "_id";
        public static final String COLUMN_NAME = "name";
        public static final String COLUME_AGE = "age";

        // 建表语句
        public static final String CREATE =
                "CREATE TABLE "
                        + TABLE_NAME
                        + " ("
                        + ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + COLUMN_NAME + " TEXT NOT NULL,"
                        + COLUME_AGE + " INT,"
                        + "UNIQUE (" + COLUMN_NAME + ")  ON CONFLICT REPLACE"
                        + " ); ";

        // 对象转字段,放入表中
        public static ContentValues toContentValues(Person person) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME, person.getName());
            values.put(COLUME_AGE, person.getAge());
            return values;
        }

        // 响应式的查询,根据表中的row生成一个对象
        static Func1<Cursor, Person> PERSON_MAPPER = new Func1<Cursor, Person>() {
            @Override
            public Person call(Cursor cursor) {
                Person person = new Person();
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
                person.setName(name);
                int age = cursor.getInt(cursor.getColumnIndexOrThrow(COLUME_AGE));
                person.setAge(age);
                return person;
            }
        };

    }

}
