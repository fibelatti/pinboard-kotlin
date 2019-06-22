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
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.fibelatti.core.archcomponents.extension.error
import com.fibelatti.core.archcomponents.extension.observe
import com.fibelatti.core.archcomponents.extension.observeEvent
import com.fibelatti.core.extension.gone
import com.fibelatti.core.extension.goneIf
import com.fibelatti.core.extension.navigateBack
import com.fibelatti.core.extension.visible
import com.fibelatti.core.extension.visibleIf
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.base.BaseFragment
import com.fibelatti.pinboard.core.extension.shareText
import com.fibelatti.pinboard.core.extension.show
import com.fibelatti.pinboard.core.extension.showStyledDialog
import com.fibelatti.pinboard.core.extension.toast
import com.fibelatti.pinboard.features.appstate.AppStateViewModel
import com.fibelatti.pinboard.features.appstate.PostDetail
import com.fibelatti.pinboard.features.mainActivity
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.fragment_post_detail.*
import kotlinx.android.synthetic.main.layout_file_view.*
import kotlinx.android.synthetic.main.layout_file_view.textViewDescription as fileViewUrlDescription
import kotlinx.android.synthetic.main.layout_file_view.textViewUrl as fileViewUrl
import kotlinx.android.synthetic.main.layout_progress_bar.*
import kotlinx.android.synthetic.main.layout_url_error.*
import kotlinx.android.synthetic.main.layout_url_error.textViewDescription as errorUrlDescription
import kotlinx.android.synthetic.main.layout_url_error.textViewUrl as errorUrl
import javax.inject.Inject

class PostDetailFragment @Inject constructor() : BaseFragment() {

    companion object {
        @JvmStatic
        val TAG: String = PostDetailFragment::class.java.simpleName
    }

    private val postDetailViewModel: PostDetailViewModel by lazy {
        viewModelFactory.get<PostDetailViewModel>(this)
    }

    private val knownFileExtensions =
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_post_detail, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewModels()
    }

    private fun setupViewModels() {
        viewModelFactory.get<AppStateViewModel>(requireActivity()).run {
            observe(getContent()) {
                if (it is PostDetail) {
                    updateViews(it.post)
                }
            }
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
            showWebView(post)
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
                show()
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

        fileViewUrlDescription.text = post.description
        fileViewUrl.text = post.url
        buttonOpenInFileViewer.setOnClickListener { openUrlInFileViewer(post.url) }
    }

    private fun showWebView(post: Post) {
        layoutRootFileViewer.gone()
        layoutScrollViewWeb.visible()

        webView.webViewClient = PostWebViewClient(post)
        webView.loadUrl(post.url)
    }

    private fun showErrorLayout(post: Post) {
        layoutRootFileViewer.gone()
        layoutScrollViewWeb.gone()
        layoutRootUrlError.visible()

        errorUrlDescription.text = post.description
        errorUrl.text = post.url
        buttonOpenInBrowser.setOnClickListener { openUrlInExternalBrowser(post) }
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

    private inner class PostWebViewClient(
        private val post: Post
    ) : WebViewClient() {

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            layoutProgressBar?.visible()
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            layoutProgressBar?.gone()
        }

        override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
            super.onReceivedError(view, request, error)
            showErrorLayout(post)
        }
    }
}
