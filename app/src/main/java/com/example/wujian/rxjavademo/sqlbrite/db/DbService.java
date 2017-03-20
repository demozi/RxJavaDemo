package com.example.wujian.rxjavademo.sqlbrite.db;

import android.content.Context;

import com.example.wujian.rxjavademo.sqlbrite.entity.Person;
import com.squareup.sqlbrite.BriteDatabase;
import com.squareup.sqlbrite.SqlBrite;

import java.util.List;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by wujian on 2016/10/21.
 */

public class DbService {

    private BriteDatabase briteDatabase;

    public DbService(Context context) {

        DBOpenHelper dbOpenHelper = new DBOpenHelper(context);

        SqlBrite sqlBrite = SqlBrite.create();

        briteDatabase = sqlBrite.wrapDatabaseHelper(dbOpenHelper, Schedulers.io());

        briteDatabase.setLoggingEnabled(true);
    }


    public Observable<List<Person>> queryPerson() {
        return briteDatabase.createQuery(WJDb.PersonTable.TABLE_NAME, "SELECT * FROM " + WJDb.PersonTable.TABLE_NAME)
                .mapToList(WJDb.PersonTable.PERSON_MAPPER);
    }

    public long addPerson(Person person) {
        return briteDatabase.insert(WJDb.PersonTable.TABLE_NAME, WJDb.PersonTable.toContentValues(person));
    }

    public int deletePersonByName(String name) {
        return briteDatabase.delete(WJDb.PersonTable.TABLE_NAME, WJDb.PersonTable.COLUMN_NAME+ "=?", name);
    }
}
