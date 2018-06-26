package com.shua.likegank.data.entity;

import io.realm.RealmObject;

/**
 * Open source license
 * Created by ShuangHua on 2017/5/6.
 */

public class License extends RealmObject {

    public String projectName;
    public String author;
    public String linkAddress;

    public License() {
    }

    public License(String projectName) {
        this.projectName = projectName;
        this.author = author;
        this.linkAddress = linkAddress;
    }
}
