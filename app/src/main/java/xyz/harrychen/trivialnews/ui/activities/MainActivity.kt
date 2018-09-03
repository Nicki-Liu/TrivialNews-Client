package xyz.harrychen.trivialnews.ui.activities

import android.os.Bundle
import android.support.design.internal.BottomNavigationItemView
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import com.miguelcatalan.materialsearchview.MaterialSearchView
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.startActivity
import xyz.harrychen.trivialnews.R
import xyz.harrychen.trivialnews.ui.fragments.BaseTimeLineFragment
import xyz.harrychen.trivialnews.ui.fragments.FavoriteFragment
import xyz.harrychen.trivialnews.ui.fragments.MainTimeLineFragment
import xyz.harrychen.trivialnews.ui.fragments.RecommendFragment

class MainActivity : AppCompatActivity(), AnkoLogger {

    private var nowFragmentId = -1
    private var fragments: HashMap<Int, BaseTimeLineFragment> = HashMap()

    private fun initNavigation() {
        main_navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        findViewById<BottomNavigationItemView>(R.id.navigation_timeline).callOnClick()
    }

    private val createFragment = { id: Int ->
        when (id) {
            R.id.navigation_timeline -> {
                // message.setText(R.string.title_home)
                MainTimeLineFragment()
            }
            R.id.navigation_favorite -> {
                // message.setText(R.string.title_dashboard)
                FavoriteFragment()
            }
            R.id.navigation_recommend -> {
                // message.setText(R.string.title_notifications)
                RecommendFragment()
            }
            else -> null
        }
    }

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        if (nowFragmentId == item.itemId) {
            fragments[nowFragmentId]!!.scrollAndRefresh()
        } else {
            var fragment = fragments[item.itemId]
            if (fragment == null) {
                fragment = createFragment(item.itemId)
            }
            fragments[item.itemId] = fragment!!
            supportFragmentManager.beginTransaction().replace(R.id.main_frame, fragment).commit()
        }

        nowFragmentId = item.itemId
        true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(main_toolbar)

        initNavigation()
        initSearch()

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_search, menu)
        val item = menu.findItem(R.id.action_search)
        main_search.setMenuItem(item)
        return true
    }

    override fun onBackPressed() {
        when (main_search.isSearchOpen) {
            true -> main_search.closeSearch()
            false -> super.onBackPressed()
        }
    }

    private fun initSearch() {
        main_search.setOnQueryTextListener(object:MaterialSearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                startActivity<SearchResultActivity>("query" to query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
    }

}
