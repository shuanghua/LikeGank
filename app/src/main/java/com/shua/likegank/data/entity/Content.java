package com.shua.likegank.data.entity;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Android Entity
 * Created by ShuangHua on 2017/5/2.
 */

public class Content extends RealmObject {

    @PrimaryKey
    public String url;
    public String content;

    public Content() {
    }

    public Content(String content, String url) {
        this.content = content;
        this.url = url;
    }
}
