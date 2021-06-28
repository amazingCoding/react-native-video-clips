package com.reactnativevideoclips

import android.annotation.TargetApi
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import androidx.loader.content.CursorLoader
import java.io.File


/**
 * Create By songhang in 2021/5/31
 */
object FileController {
  //复杂版处理  (适配多种API)
  fun getRealPathFromUri(context: Context, uri: Uri): String? {
    val sdkVersion = Build.VERSION.SDK_INT
    if (sdkVersion < 11) return getRealPathFromUri_BelowApi11(context, uri)
    return if (sdkVersion < 19) getRealPathFromUri_Api11To18(
      context,
      uri
    ) else getRealPathFromUri_AboveApi19(context, uri)
  }

  /**
   * 适配api19以上,根据uri获取图片的绝对路径
   */
  @TargetApi(Build.VERSION_CODES.KITKAT)
  private fun getRealPathFromUri_AboveApi19(context: Context, uri: Uri): String? {
    if (DocumentsContract.isDocumentUri(context, uri)) {
      if (isExternalStorageDocument(uri)) {
        val docId = DocumentsContract.getDocumentId(uri)
        val split = docId.split(":").toTypedArray()
        val type = split[0]
        if ("primary".equals(type, ignoreCase = true)) {
          return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
        }
      } else if (isDownloadsDocument(uri)) {
        val id = DocumentsContract.getDocumentId(uri)
        val contentUri: Uri = ContentUris.withAppendedId(
          Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id)
        )
        return getDataColumn(context, contentUri, null, null)
      } else if (isMediaDocument(uri)) {
        val docId = DocumentsContract.getDocumentId(uri)
        val split = docId.split(":").toTypedArray()
        val type = split[0]
        val contentUri: Uri
        contentUri = if ("image" == type) {
          MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        } else if ("video" == type) {
          MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        } else if ("audio" == type) {
          MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        } else {
          MediaStore.Files.getContentUri("external")
        }
        val selection = "_id=?"
        val selectionArgs = arrayOf(split[1])
        return getDataColumn(context, contentUri, selection, selectionArgs)
      }
    } else if ("content".equals(uri.getScheme(), ignoreCase = true)) {
      return getDataColumn(context, uri, null, null)
    } else if ("file".equals(uri.getScheme(), ignoreCase = true)) {
      return uri.getPath()
    }
    return null
  }

  /**
   * 适配api11-api18,根据uri获取图片的绝对路径
   */
  private fun getRealPathFromUri_Api11To18(context: Context, uri: Uri): String? {
    var filePath: String? = null
    val projection = arrayOf(MediaStore.Images.Media.DATA)
    val loader = CursorLoader(context, uri, projection, null, null, null)
    val cursor: Cursor? = loader.loadInBackground()
    if (cursor != null) {
      cursor.moveToFirst()
      filePath = cursor.getString(cursor.getColumnIndex(projection[0]))
      cursor.close()
    }
    return filePath
  }

  /**
   * 适配api11以下(不包括api11),根据uri获取图片的绝对路径
   */
  private fun getRealPathFromUri_BelowApi11(context: Context, uri: Uri): String? {
    var filePath: String? = null
    val projection = arrayOf(MediaStore.Images.Media.DATA)
    val cursor: Cursor? = context.getContentResolver().query(uri, projection, null, null, null)
    if (cursor != null && cursor.moveToFirst()) {
      filePath = cursor.getString(cursor.getColumnIndex(projection[0]))
      cursor.close()
    }
    return filePath
  }

  /**
   * Get the value of the data column for this Uri. This is useful for
   * MediaStore Uris, and other file-based ContentProviders.
   *
   * @param context       The context.
   * @param uri           The Uri to query.
   * @param selection     (Optional) Filter used in the query.
   * @param selectionArgs (Optional) Selection arguments used in the query.
   * @return The value of the _data column, which is typically a file path.
   */
  fun getDataColumn(context: Context, uri: Uri?, selection: String?,  selectionArgs: Array<String>?): String? {
    var cursor: Cursor? = null
    val column = MediaStore.MediaColumns.DATA
    val projection = arrayOf(column)
    try {
      cursor = context.getContentResolver().query(uri!!, projection, selection, selectionArgs, null)
      if (cursor != null && cursor.moveToFirst()) {
        val column_index: Int = cursor.getColumnIndexOrThrow(column)
        return cursor.getString(column_index)
      }
    } catch (e: Exception) {
      e.printStackTrace()
    } finally {
      if (cursor != null) cursor.close()
    }
    return null
  }

  /**
   * @param uri The Uri to check.
   * @return Whether the Uri authority is ExternalStorageProvider.
   */
  fun isExternalStorageDocument(uri: Uri): Boolean {
    return "com.android.externalstorage.documents" == uri.getAuthority()
  }

  /**
   * @param uri The Uri to check.
   * @return Whether the Uri authority is DownloadsProvider.
   */
  fun isDownloadsDocument(uri: Uri): Boolean {
    return "com.android.providers.downloads.documents" == uri.getAuthority()
  }

  /**
   * @param uri The Uri to check.
   * @return Whether the Uri authority is MediaProvider.
   */
  fun isMediaDocument(uri: Uri): Boolean {
    return "com.android.providers.media.documents" == uri.getAuthority()
  }

  fun clearDir(file: File) {
    if (file.isDirectory) {
      for (f in file.listFiles()) clearDir(f)
    }
    file.delete()
  }

}
