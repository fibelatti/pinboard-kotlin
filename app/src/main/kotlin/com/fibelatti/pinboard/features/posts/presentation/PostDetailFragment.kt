package com.fibelatti.pinboard.features.posts.presentation

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.webkit.WebView
import android.webkit.WebViewClient
import com.fibelatti.core.extension.gone
import com.fibelatti.core.extension.visible
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.base.BaseFragment
import com.fibelatti.pinboard.core.extension.toast
import com.fibelatti.pinboard.features.navigation.NavigationViewModel
import com.fibelatti.pinboard.features.posts.domain.model.Post
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
    ): View? = inflater.inflate(com.fibelatti.pinboard.R.layout.fragment_post_detail, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupLayout()
    }

    private fun setupLayout() {
        webView.webViewClient = PostWebViewClient()
        withPost {
            if (it.url.substringAfterLast(".") in knownFileExtensions) {
                showFileView(it)
            } else {
                showWebView(it.url)
            }
        }
    }

    private fun showFileView(post: Post) {
        layoutRootFileViewer.visible()
        layoutScrollView.gone()

        textViewDescription.text = post.description
        textViewUrl.text = post.url
        buttonOpenInFileViewer.setOnClickListener { openUrlInFileViewer(post.url) }
    }

    private fun showWebView(url: String) {
        layoutRootFileViewer.gone()
        layoutScrollView.visible()
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

    private fun withPost(block: (Post) -> Unit) {
        navigationViewModel.post.value?.let(block)
    }

    private inner class PostWebViewClient : WebViewClient() {
        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            layoutProgressBar?.visible()
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            layoutProgressBar?.gone()
        }
    }
}
