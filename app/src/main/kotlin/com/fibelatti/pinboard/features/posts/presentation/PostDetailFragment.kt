package com.fibelatti.pinboard.features.posts.presentation

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.webkit.MimeTypeMap
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.MenuRes
import com.fibelatti.core.archcomponents.extension.observe
import com.fibelatti.core.archcomponents.extension.observeEvent
import com.fibelatti.core.extension.gone
import com.fibelatti.core.extension.navigateBack
import com.fibelatti.core.extension.setOnClickListener
import com.fibelatti.core.extension.showStyledDialog
import com.fibelatti.core.extension.toast
import com.fibelatti.core.extension.visible
import com.fibelatti.core.extension.visibleIf
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.ConnectivityInfoProvider
import com.fibelatti.pinboard.core.android.base.BaseFragment
import com.fibelatti.pinboard.core.extension.activityViewModel
import com.fibelatti.pinboard.core.extension.shareText
import com.fibelatti.pinboard.core.extension.show
import com.fibelatti.pinboard.core.extension.viewModel
import com.fibelatti.pinboard.features.appstate.EditPost
import com.fibelatti.pinboard.features.appstate.PopularPostDetailContent
import com.fibelatti.pinboard.features.appstate.PostDetailContent
import com.fibelatti.pinboard.features.mainActivity
import com.fibelatti.pinboard.features.posts.domain.model.Post
import kotlinx.android.synthetic.main.fragment_post_detail.*
import kotlinx.android.synthetic.main.layout_file_view.*
import kotlinx.android.synthetic.main.layout_progress_bar.*
import kotlinx.android.synthetic.main.layout_url_error.*
import javax.inject.Inject

class PostDetailFragment @Inject constructor(
    private val connectivityInfoProvider: ConnectivityInfoProvider
) : BaseFragment(R.layout.fragment_post_detail) {

    companion object {
        @JvmStatic
        val TAG: String = "PostDetailFragment"
    }

    private val appStateViewModel by activityViewModel { viewModelProvider.appStateViewModel() }
    private val postDetailViewModel by viewModel { viewModelProvider.postDetailsViewModel() }
    private val popularPostsViewModel by viewModel { viewModelProvider.popularPostsViewModel() }

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
    private var postWebViewClient: PostWebViewClient? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewModels()
    }

    override fun onDestroyView() {
        postWebViewClient?.callback = null
        super.onDestroyView()
    }

    private fun setupViewModels() {
        viewLifecycleOwner.observe(appStateViewModel.postDetailContent, ::updateViews)
        viewLifecycleOwner.observe(appStateViewModel.popularPostDetailContent, ::updateViews)

        with(postDetailViewModel) {
            viewLifecycleOwner.observe(loading) {
                layoutProgressBar.visibleIf(it, otherwiseVisibility = View.GONE)
            }
            viewLifecycleOwner.observeEvent(deleted) {
                mainActivity?.toast(getString(R.string.posts_deleted_feedback))
            }
            viewLifecycleOwner.observe(error, ::handleError)
        }
        with(popularPostsViewModel) {
            viewLifecycleOwner.observe(loading) {
                layoutProgressBar.visibleIf(it, otherwiseVisibility = View.GONE)
            }
            viewLifecycleOwner.observe(saved) {
                requireActivity().toast(getString(R.string.posts_saved_feedback))
            }
            viewLifecycleOwner.observe(error, ::handleError)
        }
    }

    private fun updateViews(postDetailContent: PostDetailContent) {
        updateViews(postDetailContent.post, R.menu.menu_link)
    }

    private fun updateViews(popularPostDetailContent: PopularPostDetailContent) {
        updateViews(popularPostDetailContent.post, R.menu.menu_popular)
    }

    private fun updateViews(post: Post, @MenuRes menu: Int) {
        if (post.url.substringAfterLast(".") in knownFileExtensions) {
            showFileView(post)
        } else {
            showWebView(post)
        }

        mainActivity?.updateTitleLayout {
            setTitle("")
            setNavigateUp { navigateBack() }
        }

        mainActivity?.updateViews { bottomAppBar, fab ->
            bottomAppBar.run {
                navigationIcon = null
                replaceMenu(menu)
                setOnMenuItemClickListener { item -> handleMenuClick(item, post) }
                visible()
                show()
            }
            fab.run {
                setImageResource(R.drawable.ic_share)
                setOnClickListener {
                    requireActivity().shareText(R.string.posts_share_title, post.url)
                }
                show()
            }
        }
    }

    private fun showFileView(post: Post) {
        layoutRootFileViewer.visible()
        layoutScrollViewWeb.gone()
        layoutProgressBar.gone()

        textViewFileUrlTitle.text = post.title
        textViewFileUrl.text = post.url
        buttonOpenInFileViewer.setOnClickListener { openUrlInFileViewer(post.url) }
    }

    private fun showWebView(post: Post) {
        layoutRootFileViewer.gone()
        layoutScrollViewWeb.visible()

        if (!connectivityInfoProvider.isConnected()) {
            layoutProgressBar.gone()
            layoutRootUrlError.visible()

            textViewErrorUrlTitle.text = post.title
            textViewErrorUrl.text = post.url
            textViewErrorDescription.setText(R.string.posts_url_offline_error)
            buttonErrorAction.setOnClickListener(R.string.offline_retry) { showWebView(post) }

            return
        }

        if (webView.url != post.url) {
            postWebViewClient = PostWebViewClient(object : PostWebViewClient.Callback {

                override fun onPageStarted() {
                    layoutProgressBar?.visible()
                }

                override fun onPageFinished() {
                    layoutProgressBar?.gone()
                    layoutRootUrlError?.gone()
                }

                override fun onError() {
                    showErrorLayout(post)
                }
            }).also(webView::setWebViewClient)

            webView.loadUrl(post.url)
        }
    }

    private fun showErrorLayout(post: Post) {
        layoutRootUrlError.visible()

        textViewErrorUrlTitle.text = post.title
        textViewErrorUrl.text = post.url
        buttonErrorAction.setOnClickListener { openUrlInExternalBrowser(post) }
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
            R.id.menuItemEditLink -> appStateViewModel.runAction(EditPost(post))
            R.id.menuItemSave -> popularPostsViewModel.saveLink(post)
            R.id.menuItemOpenInBrowser -> openUrlInExternalBrowser(post)
        }

        return true
    }

    private fun deletePost(post: Post) {
        context?.showStyledDialog(
            dialogStyle = R.style.AppTheme_AlertDialog,
            dialogBackground = R.drawable.background_contrast_rounded
        ) {
            setMessage(R.string.alert_confirm_deletion)
            setPositiveButton(R.string.hint_yes) { _, _ -> postDetailViewModel.deletePost(post) }
            setNegativeButton(R.string.hint_no) { dialog, _ -> dialog?.dismiss() }
        }
    }

    private fun openUrlInExternalBrowser(post: Post) {
        startActivity(Intent(Intent.ACTION_VIEW).apply { data = Uri.parse(post.url) })
    }
}

private class PostWebViewClient(callback: Callback) : WebViewClient() {

    interface Callback {
        fun onPageStarted()

        fun onPageFinished()

        fun onError()
    }

    var callback: Callback? = callback

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        callback?.onPageStarted()
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        callback?.onPageFinished()
    }

    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?
    ) {
        super.onReceivedError(view, request, error)
        callback?.onError()
    }
}
