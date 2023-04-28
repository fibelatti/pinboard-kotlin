package com.fibelatti.pinboard.features.posts.presentation

import com.fibelatti.pinboard.features.posts.domain.model.Post

val knownFileExtensions = listOf(
    "pdf",
    "doc", "docx",
    "ppt", "pptx",
    "xls", "xlsx",
    "zip", "rar",
    "txt", "rtf",
    "mp3", "wav",
    "gif", "jpg", "jpeg", "png", "svg",
    "mp4", "3gp", "mpg", "mpeg", "avi",
)

fun Post.isFile(): Boolean = url.substringAfterLast(".") in knownFileExtensions
