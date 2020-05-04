package bj.modules;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import androidx.annotation.Nullable;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;

import bj.modules.bj_file_objcets.file_object;


public class bj_file_classes {
	public static class PasteFunctions{
		public static final byte Paste_Move=1;
		public static final byte Paste_Copy=0;
	}
	public static interface OnFilesListingCompletedListener {
		// you can define any parameter as per your requirement
		void OnCompleted(ArrayList<File> AllFiles);
		void OnCompletedList(ArrayList<String> AllFilesList);

	}
	public static interface OnFoldersListingCompletedListener {
		// you can define any parameter as per your requirement
		void OnCompleted(ArrayList<File> AllFolders);
		void OnCompletedList(ArrayList<String> AllFoldersList);

	}
	public static interface OnGFileDialogResultListener {
		// you can define any parameter as per your requirement
		void OnResult(int MyResult,String ResultDescriptiopn);
		void OnResult(int MyResult,String ResultDescriptiopn,String DestinationFilePath);
		void OnResult(int MyResult,String ResultDescriptiopn,@Nullable File DestinationFile);
		void OnResult(int MyResult,String ResultDescriptiopn,@Nullable file_object DestinationFile);
		void OnSelect(Boolean IsSelected,@Nullable file_object SelectedFile);
	}
	public static @interface FileTransfer_File_Kinds {
		Integer PersonalImage=81;
		Integer TheImage=82;
		Integer TheVideo=83;
		Integer TheMusic=84;
		Integer TheAudio=85;
		Integer TheFile=86;
		Integer TheBackup=87;
	}

	public static @interface  GFileDialogsResults{
		int Completed=0;
		int Cancelled=1;
		int GiveError=2;
		int DontCompleted=3;
		int Selected=4;


	}
	public static @interface GFileDialogProcesKind{
		int Copy=0;
		int Move=1;
		int Dellete=2;
		int Rename=3;
	}
	public static class PathUtil {
		public static String getPath(Context context, Uri uri) throws URISyntaxException {
			final boolean needToCheckUri = Build.VERSION.SDK_INT >= 19;
			String selection = null;
			String[] selectionArgs = null;
			// Uri is different in versions after KITKAT (Android 4.4), we need to
			// deal with different Uris.
			if (needToCheckUri && DocumentsContract.isDocumentUri(context.getApplicationContext(), uri)) {
				if (isExternalStorageDocument(uri)) {
					final String docId = DocumentsContract.getDocumentId(uri);
					final String[] split = docId.split(":");
					return Environment.getExternalStorageDirectory() + "/" + split[1];
				} else if (isDownloadsDocument(uri)) {
					final String id = DocumentsContract.getDocumentId(uri);
					uri = ContentUris.withAppendedId(
							Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
				} else if (isMediaDocument(uri)) {
					final String docId = DocumentsContract.getDocumentId(uri);
					final String[] split = docId.split(":");
					final String type = split[0];
					if ("image".equals(type)) {
						uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
					} else if ("video".equals(type)) {
						uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
					} else if ("audio".equals(type)) {
						uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
					}
					selection = "_id=?";
					selectionArgs = new String[]{ split[1] };
				}
			}
			if ("content".equalsIgnoreCase(uri.getScheme())) {
				String[] projection = { MediaStore.Images.Media.DATA };
				Cursor cursor = null;
				try {
					cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
					int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
					if (cursor.moveToFirst()) {
						return cursor.getString(column_index);
					}
				} catch (Exception e) {
				}
			} else if ("file".equalsIgnoreCase(uri.getScheme())) {
				return uri.getPath();
			}
			return null;
		}


		/**
		 * @param uri The Uri to check.
		 * @return Whether the Uri authority is ExternalStorageProvider.
		 */
		public static boolean isExternalStorageDocument(Uri uri) {
			return "com.android.externalstorage.documents".equals(uri.getAuthority());
		}

		/**
		 * @param uri The Uri to check.
		 * @return Whether the Uri authority is DownloadsProvider.
		 */
		public static boolean isDownloadsDocument(Uri uri) {
			return "com.android.providers.downloads.documents".equals(uri.getAuthority());
		}

		/**
		 * @param uri The Uri to check.
		 * @return Whether the Uri authority is MediaProvider.
		 */
		public static boolean isMediaDocument(Uri uri) {
			return "com.android.providers.media.documents".equals(uri.getAuthority());
		}
	}
}
