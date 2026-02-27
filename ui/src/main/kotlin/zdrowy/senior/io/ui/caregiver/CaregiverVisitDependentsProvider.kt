package zdrowy.senior.io.ui.caregiver

import io.reactivex.rxjava3.core.Observable
import zdrowy.senior.io.domain.carelink.CareLinkStatus
import zdrowy.senior.io.domain.carelink.ObserveCareLinksUseCase
import zdrowy.senior.io.domain.settings.ObservePersonalDataByUidUseCase
import zdrowy.senior.io.domain.settings.PersonalData
import zdrowy.senior.io.ui.caregiver.model.CaregiverVisitDependentUi

internal object CaregiverVisitDependentsProvider {
    fun stream(
        observeCareLinks: ObserveCareLinksUseCase,
        observePersonalDataByUid: ObservePersonalDataByUidUseCase
    ): Observable<List<CaregiverVisitDependentUi>> {
        return observeCareLinks()
            .switchMap { links ->
                val patientUids = links
                    .filter { it.status == CareLinkStatus.ACTIVE }
                    .map { it.patientUid }
                    .distinct()
                if (patientUids.isEmpty()) {
                    return@switchMap Observable.just(emptyList())
                }
                val streams = patientUids.map { uid ->
                    observePersonalDataByUid(uid)
                        .onErrorReturnItem(fallbackPersonalData())
                        .map { data -> mapDependent(uid, data) }
                }
                Observable.combineLatest(streams) { array ->
                    array.map { it as CaregiverVisitDependentUi }
                        .sortedBy { it.fullName.lowercase() }
                }
            }
    }

    private fun mapDependent(uid: String, data: PersonalData): CaregiverVisitDependentUi {
        val fullName = listOf(data.firstName.trim(), data.lastName.trim())
            .filter { it.isNotBlank() }
            .joinToString(" ")
            .ifBlank { "-" }
        val phone = data.phoneNumber.trim().ifBlank { "-" }
        return CaregiverVisitDependentUi(
            uid = uid,
            fullName = fullName,
            phone = phone
        )
    }

    private fun fallbackPersonalData(): PersonalData {
        return PersonalData(
            firstName = "",
            lastName = "",
            pesel = "",
            phoneNumber = "",
            email = "",
            address = ""
        )
    }
}
