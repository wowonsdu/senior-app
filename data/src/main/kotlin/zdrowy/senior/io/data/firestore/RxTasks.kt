package zdrowy.senior.io.data.firestore

import com.google.android.gms.tasks.Task
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

internal fun Task<*>.toCompletable(): Completable {
    return Completable.create { emitter ->
        addOnSuccessListener { if (!emitter.isDisposed) emitter.onComplete() }
        addOnFailureListener { error -> if (!emitter.isDisposed) emitter.onError(error) }
    }
}

internal fun <T : Any> Task<T>.toSingle(): Single<T> {
    return Single.create { emitter ->
        addOnSuccessListener { result -> if (!emitter.isDisposed) emitter.onSuccess(result) }
        addOnFailureListener { error -> if (!emitter.isDisposed) emitter.onError(error) }
    }
}
