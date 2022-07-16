package uz.mobiler.lesson65.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import uz.mobiler.lesson65.fragment.MessageViewPagerFragment
import uz.mobiler.lesson65.model.User

class ViewPager2FragmentAdapter(
    fragment: Fragment,
    private val list: List<String>,
    val user: User
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int {
        return list.size
    }

    override fun createFragment(position: Int): Fragment {
        return MessageViewPagerFragment.newInstance(list[position], user)
    }
}