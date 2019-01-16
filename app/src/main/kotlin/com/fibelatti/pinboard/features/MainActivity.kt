package com.fibelatti.pinboard.features

import android.os.Bundle
import android.view.MenuItem
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.base.BaseActivity
import com.fibelatti.pinboard.core.android.inSupportFragmentManagerTransaction
import com.fibelatti.pinboard.features.auth.presentation.AuthFragment
import com.fibelatti.pinboard.features.navigation.NavigationDrawerFragment
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity :
    BaseActivity(),
    NavigationDrawerFragment.Callback {

    private val mainMenuFabListener = object : FloatingActionButton.OnVisibilityChangedListener() {
        override fun onHidden(fab: FloatingActionButton?) {
            super.onHidden(fab)

            fab?.setImageResource(R.drawable.ic_add)
            fab?.show()

            bottomAppBar.run {
                setNavigationIcon(R.drawable.ic_menu)
                replaceMenu(R.menu.menu_main)

                setOnMenuItemClickListener { item: MenuItem? ->
                    when (item?.itemId) {
                        R.id.menuItemSearch -> showLinkMenu()
                    }

                    return@setOnMenuItemClickListener true
                }

                setNavigationOnClickListener { showNavigation() }
            }
        }
    }

    private val linkMenuFabListener = object : FloatingActionButton.OnVisibilityChangedListener() {
        override fun onHidden(fab: FloatingActionButton?) {
            super.onHidden(fab)

            fab?.setImageResource(R.drawable.ic_share)
            fab?.show()

            bottomAppBar.run {
                navigationIcon = null
                replaceMenu(R.menu.menu_link)

                setOnMenuItemClickListener { item: MenuItem? ->
                    when (item?.itemId) {
                        R.id.menuItemDelete -> {
                            showMainMenu()
                        }
                        R.id.menuItemEditLink -> {
                        }
                        R.id.menuItemLinkTags -> {
                        }
                    }

                    return@setOnMenuItemClickListener true
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        inSupportFragmentManagerTransaction {
            add(fragmentHost.id, AuthFragment.newInstance(), AuthFragment.TAG)
        }

//        showMainMenu()
    }

    override fun onPublicClicked() {
        // TODO
    }

    override fun onPrivateClicked() {
        // TODO
    }

    override fun onUnreadClicked() {
        // TODO
    }

    override fun onUntaggedClicked() {
        // TODO
    }

    override fun onTagsClicked() {
        // TODO
    }

    override fun onAccountClicked() {
        // TODO
    }

    override fun onPreferencesClicked() {
        // TODO
    }

    fun showMainMenu() {
        bottomAppBar.fabAlignmentMode = BottomAppBar.FAB_ALIGNMENT_MODE_CENTER
        fabMain.hide(mainMenuFabListener)
    }

    fun showLinkMenu() {
        bottomAppBar.fabAlignmentMode = BottomAppBar.FAB_ALIGNMENT_MODE_END
        fabMain.hide(linkMenuFabListener)
    }

    private fun showNavigation() {
        NavigationDrawerFragment().run {
            callback = this@MainActivity
            show(supportFragmentManager, tag)
        }
    }
}
