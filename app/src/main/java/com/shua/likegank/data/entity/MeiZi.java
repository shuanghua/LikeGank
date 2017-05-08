package com.shua.likegank.data.entity;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * MeiZi
 * Created by SHUA on 2017/4/26.
 */

public class MeiZi extends RealmObject {

    @PrimaryKey
    public String url;

    public MeiZi() {
    }

    public MeiZi(String url) {
        this.url = url;
    }
}
