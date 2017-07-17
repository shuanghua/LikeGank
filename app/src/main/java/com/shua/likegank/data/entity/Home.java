package com.shua.likegank.data.entity;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by SHUA on 2017/4/29.
 */

public class Home extends RealmObject {

    @PrimaryKey
    private String _id;
    public String title;
    public String createdAt;

    public String type;
    public String url;
    public String who;

    public Home() {
    }

    public Home(String _id
            , String title
            , String createdAt
            , String type
            , String url
            , String who) {
        this._id = _id;
        this.title = title;
        this.createdAt = createdAt;
        this.type = type;
        this.url = url;
        this.who = who;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    private int page;

}
