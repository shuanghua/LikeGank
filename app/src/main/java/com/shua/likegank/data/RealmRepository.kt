package com.shua.likegank.data

import com.shua.likegank.data.entity.Girl
import com.shua.likegank.data.entity.Home
import io.reactivex.rxjava3.core.Observable
import io.realm.kotlin.Realm
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.query.RealmResults
import io.realm.kotlin.types.TypedRealmObject
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.rx3.asObservable
import timber.log.Timber

object RealmRepository {

    fun subscribeHome(realm: Realm): Observable<RealmResults<Home>> {
        return realm.query(Home::class)
            .find()
            .asFlow()
            .map { it.list }
            .asObservable()
    }

    fun subscribeGirl(realm: Realm): Observable<RealmResults<Girl>> {
        return realm.query(Girl::class)
            .find()
            .asFlow()
            .map { it.list }
            .asObservable()
    }

    fun <T : TypedRealmObject> queryEntitySize(realm: Realm, clazz: Class<T>): Int {
        return realm.query(clazz.kotlin).find().size
    }

    fun <T : TypedRealmObject> hasExists(realm: Realm, id: String, clazz: Class<T>): Boolean {
        val data = realm.query(clazz.kotlin, "_id = $0", id).first().find()
        return data != null
    }

    /**
     * writeBlocking 内部使用 kotlin 协程切换线程，所以不需要调用线程
     */
    fun saveHomes(realm: Realm, homes: List<Home>) {
        // realm kotlin 不支持直接存储 list !!
        // https://github.com/realm/realm-kotlin/issues/938
        Timber.d("-------->>saveHomes$homes")
        realm.writeBlocking {
            homes.map { copyToRealm(it) }
        }
    }

    fun saveGirls(realm: Realm, girls: List<Girl>) {
        realm.writeBlocking {
            girls.map { copyToRealm(it, UpdatePolicy.ALL) }
        }
    }
}
