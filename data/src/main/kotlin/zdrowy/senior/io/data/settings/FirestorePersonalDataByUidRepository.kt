package zdrowy.senior.io.data.settings

import com.google.firebase.firestore.FirebaseFirestore
import io.reactivex.rxjava3.core.Observable
import zdrowy.senior.io.data.firestore.FirestorePaths
import zdrowy.senior.io.domain.settings.PersonalData
import zdrowy.senior.io.domain.settings.PersonalDataByUidRepository

class FirestorePersonalDataByUidRepository : PersonalDataByUidRepository {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun observePersonalData(uid: String): Observable<PersonalData> {
        val fallback = PersonalData(
            firstName = "",
            lastName = "",
            pesel = "",
            phoneNumber = "",
            email = "",
            address = ""
        )

        return Observable.create { emitter ->
            val doc = firestore.collection(FirestorePaths.USERS)
                .document(uid)
                .collection(FirestorePaths.SETTINGS)
                .document(FirestorePaths.PERSONAL_DATA)

            val registration = doc.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (!emitter.isDisposed) emitter.onError(error)
                    return@addSnapshotListener
                }
                if (snapshot == null || !snapshot.exists()) {
                    if (!emitter.isDisposed) emitter.onNext(fallback)
                    return@addSnapshotListener
                }
                val data = PersonalData(
                    firstName = snapshot.getString("firstName").orEmpty(),
                    lastName = snapshot.getString("lastName").orEmpty(),
                    pesel = snapshot.getString("pesel").orEmpty(),
                    phoneNumber = snapshot.getString("phoneNumber").orEmpty(),
                    email = snapshot.getString("email").orEmpty(),
                    address = snapshot.getString("address").orEmpty()
                )
                if (!emitter.isDisposed) emitter.onNext(data)
            }
            emitter.setCancellable { registration.remove() }
        }
    }
}
