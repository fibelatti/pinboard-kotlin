package com.fibelatti.pinboard.features.posts.presentation

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.webkit.WebView
import android.webkit.WebViewClient
import com.fibelatti.core.archcomponents.extension.error
import com.fibelatti.core.archcomponents.extension.observe
import com.fibelatti.core.archcomponents.extension.observeEvent
import com.fibelatti.core.extension.gone
import com.fibelatti.core.extension.goneIf
import com.fibelatti.core.extension.visible
import com.fibelatti.core.extension.visibleIf
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.base.BaseFragment
import com.fibelatti.pinboard.core.extension.navigateBack
import com.fibelatti.pinboard.core.extension.shareText
import com.fibelatti.pinboard.core.extension.showStyledDialog
import com.fibelatti.pinboard.core.extension.toast
import com.fibelatti.pinboard.features.mainActivity
import com.fibelatti.pinboard.features.navigation.NavigationViewModel
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.fragment_post_detail.*
import kotlinx.android.synthetic.main.layout_file_view.*
import kotlinx.android.synthetic.main.layout_progress_bar.*
import javax.inject.Inject

class PostDetailFragment @Inject constructor() : BaseFragment() {

    companion object {
        @JvmStatic
        val TAG: String = PostDetailFragment::class.java.simpleName
    }

    private val navigationViewModel: NavigationViewModel by lazy {
        viewModelFactory.get<NavigationViewModel>(requireActivity())
    }
    private val postDetailViewModel: PostDetailViewModel by lazy {
        viewModelFactory.get<PostDetailViewModel>(this)
    }

    private val knownFileExtensions by lazy {
        listOf(
            "pdf",
            "doc", "docx",
            "ppt", "pptx",
            "xls", "xlsx",
            "zip", "rar",
            "txt", "rtf",
            "mp3", "wav",
            "gif", "jpg", "jpeg", "png", "svg",
            "mp4", "3gp", "mpg", "mpeg", "avi"
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_post_detail, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        webView.webViewClient = PostWebViewClient()
        with(navigationViewModel) {
            observe(post, ::updateViews)
        }
        with(postDetailViewModel) {
            observeEvent(loading) {
                layoutProgressBar.visibleIf(it, otherwiseVisibility = View.GONE)
                layoutRootFileViewer.goneIf(it)
                layoutScrollViewWeb.goneIf(it)
            }
            observeEvent(deleted) {
                mainActivity?.toast(getString(R.string.posts_deleted_feedback))
                navigateBack()
            }
            error(error, ::handleError)
        }
    }

    private fun updateViews(post: Post) {
        if (post.url.substringAfterLast(".") in knownFileExtensions) {
            showFileView(post)
        } else {
            showWebView(post.url)
        }

        mainActivity?.updateTitleLayout {
            setTitle("")
            setNavigateUp { navigateBack() }
        }
        mainActivity?.updateViews { bottomAppBar: BottomAppBar, fab: FloatingActionButton ->
            bottomAppBar.run {
                fabAlignmentMode = BottomAppBar.FAB_ALIGNMENT_MODE_END
                navigationIcon = null
                replaceMenu(R.menu.menu_link)
                setOnMenuItemClickListener { item: MenuItem? -> handleMenuClick(item, post) }
            }
            fab.run {
                setImageResource(R.drawable.ic_share)
                setOnClickListener { requireActivity().shareText(R.string.posts_share_title, post.url) }
            }
        }
    }

    private fun showFileView(post: Post) {
        layoutRootFileViewer.visible()
        layoutScrollViewWeb.gone()

        textViewDescription.text = post.description
        textViewUrl.text = post.url
        buttonOpenInFileViewer.setOnClickListener { openUrlInFileViewer(post.url) }
    }

    private fun showWebView(url: String) {
        layoutRootFileViewer.gone()
        layoutScrollViewWeb.visible()
        webView.loadUrl(url)
    }

    private fun openUrlInFileViewer(url: String) {
        val mimeType = MimeTypeMap.getSingleton()
            .getMimeTypeFromExtension(url.substringAfterLast("."))
        val newIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(Uri.parse(url), mimeType)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        try {
            startActivity(newIntent)
        } catch (e: ActivityNotFoundException) {
            context?.toast(getString(R.string.posts_open_with_file_viewer_error))
        }
    }

    private fun handleMenuClick(item: MenuItem?, post: Post): Boolean {
        when (item?.itemId) {
            R.id.menuItemDelete -> deletePost(post)
            R.id.menuItemEditLink -> {
            }
            R.id.menuItemLinkTags -> {
            }
            R.id.menuItemOpenInBrowser -> openUrlInExternalBrowser(post)
        }

        return true
    }

    private fun deletePost(post: Post) {
        context?.showStyledDialog {
            setMessage(R.string.alert_confirm_deletion)
            setPositiveButton(R.string.hint_yes) { _, _ -> postDetailViewModel.deletePost(post) }
            setNegativeButton(R.string.hint_no) { dialog, _ -> dialog?.dismiss() }
        }
    }

    private fun openUrlInExternalBrowser(post: Post) {
        startActivity(Intent(Intent.ACTION_VIEW).apply { data = Uri.parse(post.url) })
    }

    private inner class PostWebViewClient : WebViewClient() {
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            layoutProgressBar?.visible()
            layoutScrollViewWeb?.gone()
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            layoutProgressBar?.gone()
            layoutScrollViewWeb?.visible()
        }
    }
}
