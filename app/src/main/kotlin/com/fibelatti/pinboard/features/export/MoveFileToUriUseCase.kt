package com.fibelatti.pinboard.features.export

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import com.fibelatti.core.functional.UseCaseWithParams
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MoveFileToUriUseCase @Inject constructor(
    @ApplicationContext context: Context,
) : UseCaseWithParams<MoveFileToUriUseCase.Params, Long?> {

    private val contentResolver: ContentResolver = context.contentResolver

    override suspend fun invoke(params: Params): Long? {
        return runCatching {
            withContext(Dispatchers.IO) {
                contentResolver.openOutputStream(params.destinationUri)?.use { outputStream ->
                    params.sourceFile.inputStream().buffered().use { bufferedInputStream ->
                        bufferedInputStream.copyTo(outputStream)
                    }
                }.also { params.sourceFile.delete() }
            }
        }.getOrNull()
    }

    class Params(val sourceFile: File, val destinationUri: Uri)
}
