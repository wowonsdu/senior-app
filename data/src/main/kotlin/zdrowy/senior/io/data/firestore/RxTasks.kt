package zdrowy.senior.io.data.firestore

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

internal fun Task<*>.toCompletable(): Completable {
    return Completable.fromAction { Tasks.await(this) }
}

internal fun <T : Any> Task<T>.toSingle(): Single<T> {
    return Single.fromCallable { Tasks.await(this) }
}
