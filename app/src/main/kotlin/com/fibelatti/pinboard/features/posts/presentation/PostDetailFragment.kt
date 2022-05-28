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
import androidx.annotation.MenuRes
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.fibelatti.core.extension.navigateBack
import com.fibelatti.core.extension.shareText
import com.fibelatti.pinboard.R
import com.fibelatti.pinboard.core.android.ConnectivityInfoProvider
import com.fibelatti.pinboard.core.android.base.BaseFragment
import com.fibelatti.pinboard.core.extension.show
import com.fibelatti.pinboard.core.extension.showBanner
import com.fibelatti.pinboard.core.extension.viewBinding
import com.fibelatti.pinboard.databinding.FragmentPostDetailBinding
import com.fibelatti.pinboard.features.appstate.AppStateViewModel
import com.fibelatti.pinboard.features.appstate.EditPost
import com.fibelatti.pinboard.features.appstate.PopularPostDetailContent
import com.fibelatti.pinboard.features.appstate.PostDetailContent
import com.fibelatti.pinboard.features.bottomBarHost
import com.fibelatti.pinboard.features.posts.domain.model.Post
import com.fibelatti.pinboard.features.titleLayoutHost
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PostDetailFragment @Inject constructor(
    private val connectivityInfoProvider: ConnectivityInfoProvider,
) : BaseFragment() {

    companion object {

        @JvmStatic
        val TAG: String = "PostDetailFragment"
    }

    private val appStateViewModel: AppStateViewModel by activityViewModels()
    private val postDetailViewModel: PostDetailViewModel by viewModels()
    private val popularPostsViewModel: PopularPostsViewModel by viewModels()

    private val binding by viewBinding(FragmentPostDetailBinding::bind)

    private val knownFileExtensions = listOf(
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = FragmentPostDetailBinding.inflate(inflater, container, false).root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewModels()
    }

    override fun onDestroyView() {
        titleLayoutHost.update { hideActionButton() }
        postWebViewClient?.callback = null
        super.onDestroyView()
    }

    private fun setupViewModels() {
        lifecycleScope.launch {
            appStateViewModel.postDetailContent.collect(::updateViews)
        }
        lifecycleScope.launch {
            appStateViewModel.popularPostDetailContent.collect(::updateViews)
        }
        lifecycleScope.launch {
            postDetailViewModel.loading.collect {
                binding.layoutProgressBar.root.isVisible = it
            }
        }
        lifecycleScope.launch {
            postDetailViewModel.deleted.collect { binding.root.showBanner(getString(R.string.posts_deleted_feedback)) }
        }
        lifecycleScope.launch {
            postDetailViewModel.deleteError.collect {
                MaterialAlertDialogBuilder(requireContext()).apply {
                    setMessage(R.string.posts_deleted_error)
                    setPositiveButton(R.string.hint_ok) { dialog, _ -> dialog?.dismiss() }
                }.show()
            }
        }
        lifecycleScope.launch {
            postDetailViewModel.updated.collect {
                binding.root.showBanner(getString(R.string.posts_marked_as_read_feedback))
                titleLayoutHost.update { hideActionButton() }
            }
        }
        lifecycleScope.launch {
            postDetailViewModel.updateError.collect {
                binding.root.showBanner(getString(R.string.posts_marked_as_read_error))
            }
        }
        lifecycleScope.launch {
            postDetailViewModel.error.collect(::handleError)
        }
        lifecycleScope.launch {
            popularPostsViewModel.loading.collect {
                binding.layoutProgressBar.root.isVisible = it
            }
        }
        lifecycleScope.launch {
            popularPostsViewModel.saved.collect { binding.root.showBanner(getString(R.string.posts_saved_feedback)) }
        }
        lifecycleScope.launch {
            popularPostsViewModel.error.collect(::handleError)
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

        titleLayoutHost.update {
            hideTitle()
            hideSubTitle()
            setNavigateUp { navigateBack() }
        }

        bottomBarHost.update { bottomAppBar, fab ->
            bottomAppBar.run {
                navigationIcon = null
                replaceMenu(menu)
                setOnMenuItemClickListener { item -> handleMenuClick(item, post) }
                isVisible = true
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
        titleLayoutHost.update { hideActionButton() }

        binding.layoutFileView.root.isVisible = true
        binding.layoutScrollViewWeb.isGone = true
        binding.layoutProgressBar.root.isGone = true

        binding.layoutFileView.textViewFileUrlTitle.text = post.title
        binding.layoutFileView.textViewFileUrl.text = post.url
        binding.layoutFileView.buttonOpenInFileViewer.setOnClickListener { openUrlInFileViewer(post.url) }
    }

    private fun showWebView(post: Post) {
        binding.layoutFileView.root.isGone = true
        binding.layoutScrollViewWeb.isVisible = true

        if (!connectivityInfoProvider.isConnected()) {
            binding.layoutProgressBar.root.isGone = true
            binding.layoutUrlError.root.isVisible = true

            binding.layoutUrlError.textViewErrorUrlTitle.text = post.title
            binding.layoutUrlError.textViewErrorUrl.text = post.url
            binding.layoutUrlError.textViewErrorDescription.setText(R.string.posts_url_offline_error)
            binding.layoutUrlError.buttonErrorAction.setText(R.string.offline_retry)
            binding.layoutUrlError.buttonErrorAction.setOnClickListener {
                showWebView(post)
            }

            return
        }

        if (post.readLater) {
            titleLayoutHost.update {
                setActionButton(R.string.hint_mark_as_read) {
                    postDetailViewModel.markAsRead(post)
                }
            }
        }

        if (binding.webView.url != post.url) {
            postWebViewClient = PostWebViewClient(object : PostWebViewClient.Callback {

                override fun onPageStarted() {
                    binding.layoutProgressBar.root.isVisible = true
                }

                override fun onPageFinished() {
                    binding.layoutProgressBar.root.isGone = true
                    binding.layoutUrlError.root.isGone = true
                }

                override fun onError() {
                    showErrorLayout(post)
                }
            }).also(binding.webView::setWebViewClient)

            binding.webView.loadUrl(post.url)
        }
    }

    private fun showErrorLayout(post: Post) {
        binding.layoutUrlError.root.isVisible = true

        binding.layoutUrlError.textViewErrorUrlTitle.text = post.title
        binding.layoutUrlError.textViewErrorUrl.text = post.url
        binding.layoutUrlError.buttonErrorAction.setOnClickListener { openUrlInExternalBrowser(post) }
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
        } catch (ignored: ActivityNotFoundException) {
            binding.root.showBanner(getString(R.string.posts_open_with_file_viewer_error))
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
        MaterialAlertDialogBuilder(requireContext()).apply {
            setMessage(R.string.alert_confirm_deletion)
            setPositiveButton(R.string.hint_yes) { _, _ -> postDetailViewModel.deletePost(post) }
            setNegativeButton(R.string.hint_no) { dialog, _ -> dialog?.dismiss() }
        }.show()
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
        error: WebResourceError?,
    ) {
        super.onReceivedError(view, request, error)
        callback?.onError()
    }
}
