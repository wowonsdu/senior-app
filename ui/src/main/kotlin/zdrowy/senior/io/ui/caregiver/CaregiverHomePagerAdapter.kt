package zdrowy.senior.io.ui.caregiver

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class CaregiverHomePagerAdapter(
    fragment: Fragment
) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> CaregiverDashboardFragment()
            1 -> CaregiverDependentsFragment()
            else -> CaregiverVisitsFragment()
        }
    }
}
