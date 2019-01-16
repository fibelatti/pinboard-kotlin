package com.fibelatti.pinboard.features.auth.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.base.BaseFragment

class AuthFragment : BaseFragment() {

    companion object {
        @JvmStatic
        val TAG = AuthFragment::class.java.simpleName

        fun newInstance(): AuthFragment = AuthFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_auth, container, false)
}
