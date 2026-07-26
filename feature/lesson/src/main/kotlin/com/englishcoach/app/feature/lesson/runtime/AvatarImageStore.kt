package com.englishcoach.app.feature.lesson.runtime

import android.content.Context
import android.net.Uri
import com.englishcoach.app.core.model.LessonType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

/**
 * Persists a user-picked avatar photo into the app's private storage, keyed by [LessonType].
 *
 * The system Photo Picker's `content://media/picker/...` URI is not eligible for
 * `takePersistableUriPermission` (Android throws `SecurityException` if you try), so instead
 * of holding onto that transient URI, the image bytes are copied once into `filesDir` - this
 * survives restarts and needs no persisted grant.
 */
class AvatarImageStore @Inject constructor(@ApplicationContext private val context: Context) {

    suspend fun persist(sourceUri: Uri, lessonType: LessonType): String = withContext(Dispatchers.IO) {
        val file = avatarFile(lessonType)
        file.parentFile?.mkdirs()
        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        }
        file.absolutePath
    }

    suspend fun clear(lessonType: LessonType) = withContext(Dispatchers.IO) {
        avatarFile(lessonType).delete()
        Unit
    }

    private fun avatarFile(lessonType: LessonType): File =
        File(File(context.filesDir, "avatars"), "${lessonType.name}.jpg")
}
