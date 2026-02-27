package zdrowy.senior.io.data.firestore

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

internal fun Task<*>.toCompletable(): Completable {
    return Completable.create { emitter ->
        try {
            Tasks.await(this)
            if (!emitter.isDisposed) {
                emitter.onComplete()
            }
        } catch (error: InterruptedException) {
            Thread.currentThread().interrupt()
            if (!emitter.isDisposed) {
                emitter.tryOnError(error)
            }
        } catch (error: Throwable) {
            if (!emitter.isDisposed) {
                emitter.tryOnError(error)
            }
        }
    }
}

internal fun <T : Any> Task<T>.toSingle(): Single<T> {
    return Single.create { emitter ->
        try {
            val result = Tasks.await(this)
            if (!emitter.isDisposed) {
                emitter.onSuccess(result)
            }
        } catch (error: InterruptedException) {
            Thread.currentThread().interrupt()
            if (!emitter.isDisposed) {
                emitter.tryOnError(error)
            }
        } catch (error: Throwable) {
            if (!emitter.isDisposed) {
                emitter.tryOnError(error)
            }
        }
    }
}
