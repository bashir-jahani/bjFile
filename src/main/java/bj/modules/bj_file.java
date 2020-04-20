package bj.modules;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.Writer;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;

import bj.modules.bj_file_objcets.file_object;
import bj.modules.bj_messageBox_objcets.inputBox;
import bj.modules.bj_messageBox_objcets.messageBox;
import bj.modules.bjfile.R;
import bj.modules.bj_file_classes.*;

import static bj.modules.bj_messageBox.*;
import static bj.modules.bj_permission.CheckPermision;

public class bj_file  extends java.io.File {
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
	public static String getRealPathFromURI_API19(Context context, Uri uri){
		String filePath = "";
		String wholeID = DocumentsContract.getDocumentId(uri);

		// Split at colon, use second item in the array
		String id = wholeID.split(":")[1];

		String[] column = { MediaStore.Images.Media.DATA };

		// where id is equal to
		String sel = MediaStore.Images.Media._ID + "=?";

		Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
				column, sel, new String[]{ id }, null);

		int columnIndex = cursor.getColumnIndex(column[0]);

		if (cursor.moveToFirst()) {
			filePath = cursor.getString(columnIndex);
		}
		cursor.close();
		return filePath;
	}
	Context mContext;
	private static OnGFileDialogResultListener mOnGFileDialogResultListener;
	private OnFilesListingCompletedListener mOnFilesListingCompletedListener;
	private OnFoldersListingCompletedListener mOnFoldersListingCompletedListener;

	public bj_file(@NonNull String pathname, Context context) {
		super(pathname);
		mContext=context;

	}
	public bj_file(@NonNull File file, Context context) {
		super(file.getAbsolutePath());
		mContext=context;

	}
	public  void AllFolders( OnFoldersListingCompletedListener onFoldersListingCompletedListener){
		mOnFoldersListingCompletedListener=onFoldersListingCompletedListener;

		AsyncTask<Void,Void,Void> async=new AsyncTask<Void, Void, Void>() {
			ArrayList<File> MyFolders;
			ArrayList<String> MyFoldersList;
			@Override
			protected void onPostExecute(Void aVoid) {
				super.onPostExecute(aVoid);
				mOnFoldersListingCompletedListener.OnCompleted(MyFolders);
				mOnFoldersListingCompletedListener.OnCompletedList(MyFoldersList);
			}

			@Override
			protected Void doInBackground(Void... params) {
				MyFolders=AllFolders();
				MyFoldersList=AllFoldersList();
				return null;
			}
		};
	}
	public  void AllFiles( OnFilesListingCompletedListener onFilesListingCompletedListener){
		mOnFilesListingCompletedListener=onFilesListingCompletedListener;
		ArrayList<File> mAllFiles = new ArrayList<File>();
		AsyncTask <Void,Void,Void> async=new AsyncTask<Void, Void, Void>() {
			ArrayList<File> MyFiles;
			ArrayList<String> MyFilesList;
			@Override
			protected void onPostExecute(Void aVoid) {
				super.onPostExecute(aVoid);
				mOnFilesListingCompletedListener.OnCompleted(MyFiles);
				mOnFilesListingCompletedListener.OnCompletedList(MyFilesList);
			}

			@Override
			protected Void doInBackground(Void... params) {
				MyFiles=AllFiles();
				MyFilesList=AllFilesList();
				return null;
			}
		};
	}
	public byte[] readBytes()  {
		RandomAccessFile f = null;
		try {
			f = new RandomAccessFile(this, "r");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		byte[] b = new byte[0];
		try {
			b = new byte[(int)f.length()];
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			f.readFully(b);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return b;
	}
	public static byte[] readBytesofFile(File file)  {
		RandomAccessFile f = null;
		try {
			f = new RandomAccessFile(file, "r");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		byte[] b = new byte[0];
		try {
			b = new byte[(int)f.length()];
		} catch (NullPointerException| IOException e) {
			e.printStackTrace();
		}
		try {
			f.readFully(b);
		} catch (NullPointerException | IOException e) {
			e.printStackTrace();
		}
		return b;
	}
	public  ArrayList<File> AllFolders(){

		ArrayList<File> mAllFolders = new ArrayList<File>();
		if (exists()){
			if (listFiles().length>0){
				for (File file: listFiles()){
					if (file.isDirectory()){
						mAllFolders.add(file);
						if (file.listFiles().length>0) {
							ArrayList<File> mAllFolders1;
							mAllFolders1=AllFolders(file);

							if(mAllFolders1!=null){
								for (File file1:mAllFolders1){
									mAllFolders.add(file1);
								}
							}
						}
					}
				}
			}

		}

		return mAllFolders;

	}
	public  ArrayList<File> AllFiles(){

		ArrayList<File> mAllFiles = new ArrayList<File>();
		if (exists()){
			if (listFiles().length>0){
				for (File file: listFiles()){
					if (file.isDirectory()) {
						ArrayList<File> mAllFiles1;
						mAllFiles1 = AllFiles(file);
						if(mAllFiles1!=null){
							for (File file1:mAllFiles1){
								mAllFiles.add(file1);
							}
						}

					}else {
						mAllFiles.add(file);
					}
				}
			}

		}

		return mAllFiles;
	}
	public  ArrayList<String> AllFoldersList(){

		ArrayList<String> mAllFolders = new ArrayList<String>();
		if (exists()){
			if (listFiles().length>0){
				for (File file: listFiles()){
					if (file.isDirectory()){
						mAllFolders.add(file.getAbsolutePath());
						if (file.listFiles().length>0) {
							ArrayList<File> mAllFolders1;
							mAllFolders1=AllFolders(file);

							if(mAllFolders1!=null){
								for (File file1:mAllFolders1){
									mAllFolders.add(file1.getAbsolutePath());
								}
							}
						}
					}
				}
			}

		}

		return mAllFolders;

	}
	public  ArrayList<String> AllFilesList(){
		ArrayList<String> mAllFiles = new ArrayList<String>();
		if (exists()){
			if (listFiles().length>0){
				for (File file: listFiles()){
					if (file.isDirectory()) {
						ArrayList<File> mAllFiles1;
						mAllFiles1 = AllFiles(file);
						if(mAllFiles1!=null){
							for (File file1:mAllFiles1){
								mAllFiles.add(file1.getAbsolutePath());
							}
						}

					}else {
						mAllFiles.add(file.getAbsolutePath());
					}
				}
			}

		}

		return mAllFiles;
	}
	public  ArrayList<bj_file> listGFiles(FilenameFilter filter, final Boolean dscSort){

		ArrayList<bj_file> mFiles = new ArrayList<bj_file>();
		if (exists()){
			if (listFiles().length>0){

				for (File file: listFiles()){
					if (file.isDirectory()) {
						mFiles.add(new bj_file(file.getAbsolutePath(), mContext));
					}else {

					}


				}
				for (File file: listFiles()){
					if (file.isDirectory()) {

					}else {
						if (filter.accept(file.getParentFile(),file.getName())){
							mFiles.add(new bj_file(file.getAbsolutePath(), mContext));
						}
					}


				}
			}

		}
		mFiles.sort(new Comparator<bj_file>() {
			@Override
			public int compare(bj_file o1, bj_file o2) {
				if (o1.isDirectory() == o2.isDirectory()) {
					if (!dscSort) {
						return o1.getName().compareTo(o2.getName());
					}else {
						return o2.getName().compareTo(o1.getName());
					}
				}else {
					if (o1.isDirectory()) {
						return 1;
					}else {
						return 0;
					}
				}
			}
		});
		return mFiles;
	}
	public  ArrayList<bj_file> listGFiles(Boolean OnlyDirectory, final Boolean dscSort){

		ArrayList<bj_file> mFiles = new ArrayList<bj_file>();
		if (exists()){
			if (listFiles().length>0){

				for (File file: listFiles()){
					if (file.isDirectory()) {
						mFiles.add(new bj_file(file.getAbsolutePath(), mContext));
					}else {

					}


				}
				if (!OnlyDirectory){
					for (File file: listFiles()){
						if (file.isDirectory()) {

						}else {
							mFiles.add(new bj_file(file.getAbsolutePath(), mContext));
						}
					}
				}

			}

		}
		mFiles.sort(new Comparator<bj_file>() {
			@Override
			public int compare(bj_file o1, bj_file o2) {
				if (o1.isDirectory() == o2.isDirectory()) {
					if (!dscSort) {
						return o1.getName().compareTo(o2.getName());
					}else {
						return o2.getName().compareTo(o1.getName());
					}
				}else {
					if (o1.isDirectory()) {
						return 1;
					}else {
						return 0;
					}
				}
			}
		});
		return mFiles;
	}
	public  int CountFiles(FilenameFilter filter){

		int count=0;

		if (exists()){
			if (listFiles(filter).length>0){

				for (File file: listFiles(filter)){
					if (file.isFile()){
						count++;
					}

				}
			}

		}

		return count;
	}
	public  int CountFiles(){

		int count=0;

		if (exists()){
			if (listFiles().length>0){

				for (File file: listFiles()){
					if (file.isFile()){
						count++;
					}

				}
			}

		}

		return count;
	}
	public  int CountDirectories(){
		int count=0;

		if (exists()){
			if (listFiles().length>0){

				for (File file: listFiles()){
					if (file.isDirectory()){
						count++;
					}

				}
			}

		}

		return count;
	}
	public static class StorageInfo {

		public final String path;
		public final boolean readonly;
		public final boolean removable;
		public final int number;

		StorageInfo(String path, boolean readonly, boolean removable, int number) {
			this.path = path;
			this.readonly = readonly;
			this.removable = removable;
			this.number = number;
		}

		public String getDisplayName() {
			StringBuilder res = new StringBuilder();
			if (!removable) {
				res.append("Internal SD card");
			} else if (number > 1) {
				res.append("SD card " + number);
			} else {
				res.append("SD card");
			}
			if (readonly) {
				res.append(" (Read only)");
			}
			return res.toString();
		}
	}

	public static ArrayList<StorageInfo> getStorageList() {

		ArrayList<StorageInfo> list = new ArrayList<StorageInfo>();
		String def_path = Environment.getExternalStorageDirectory().getPath();
		boolean def_path_removable = Environment.isExternalStorageRemovable();
		String def_path_state = Environment.getExternalStorageState();
		boolean def_path_available = def_path_state.equals(Environment.MEDIA_MOUNTED)
				|| def_path_state.equals(Environment.MEDIA_MOUNTED_READ_ONLY);
		boolean def_path_readonly = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED_READ_ONLY);

		HashSet<String> paths = new HashSet<String>();
		int cur_removable_number = 1;

		if (def_path_available) {
			paths.add(def_path);
			list.add(0, new StorageInfo(def_path, def_path_readonly, def_path_removable, def_path_removable ? cur_removable_number++ : -1));
		}

		BufferedReader buf_reader = null;
		try {
			buf_reader = new BufferedReader(new FileReader("/proc/mounts"));
			String line;

			while ((line = buf_reader.readLine()) != null) {

				if (line.contains("vfat") || line.contains("/mnt")) {
					StringTokenizer tokens = new StringTokenizer(line, " ");
					String unused = tokens.nextToken(); //device
					String mount_point = tokens.nextToken(); //mount point
					if (paths.contains(mount_point)) {
						continue;
					}
					unused = tokens.nextToken(); //file system
					List<String> flags = Arrays.asList(tokens.nextToken().split(",")); //flags
					boolean readonly = flags.contains("ro");

					if (line.contains("/dev/block/vold")) {
						if (!line.contains("/mnt/secure")
								&& !line.contains("/mnt/asec")
								&& !line.contains("/mnt/obb")
								&& !line.contains("/dev/mapper")
								&& !line.contains("tmpfs")) {
							paths.add(mount_point);
							list.add(new StorageInfo(mount_point, readonly, true, cur_removable_number++));
						}
					}
				}
			}

		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (buf_reader != null) {
				try {
					buf_reader.close();
				} catch (IOException ex) {}
			}
		}
		return list;
	}
	private ArrayList<File> AllFolders(File SubDir){

		ArrayList<File> mAllFolders = new ArrayList<File>();
		if (SubDir.exists()){
			if (SubDir.listFiles().length>0){
				for (File file:SubDir.listFiles()){
					if (file.isDirectory()){
						mAllFolders.add(file);
						if (file.listFiles().length>0) {
							ArrayList<File> mAllFolders1;
							mAllFolders1=AllFolders(file);

							if(mAllFolders1!=null){
								for (File file1:mAllFolders1){
									mAllFolders.add(file1);
								}
							}
						}
					}
				}
			}

		}
		mAllFolders.sort(new Comparator<File>() {
			@Override
			public int compare(File o1, File o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		return mAllFolders;

	}
	private ArrayList<File> AllFiles(File SubDir){

		ArrayList<File> mAllFiles = new ArrayList<File>();
		if (SubDir.exists()){
			if (SubDir.listFiles().length>0){
				for (File file: SubDir.listFiles()){
					if (file.isDirectory()) {
						ArrayList<File> mAllFiles1;
						mAllFiles1 = AllFiles(file);
						if(mAllFiles1!=null){
							for (File file1:mAllFiles1){
								mAllFiles.add(file1);
							}
						}

					}else {
						mAllFiles.add(file);
					}
				}
			}

		}
		mAllFiles.sort(new Comparator<File>() {
			@Override
			public int compare(File o1, File o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		return mAllFiles;
	}
	public static void OpenChoicerDirectory(Context context,Boolean MultiChoice, OnGFileDialogResultListener listener){
		GFileDialogsChoicer gfdc=new GFileDialogsChoicer(context,true,MultiChoice,listener);
		gfdc.show();
	}
	public static void OpenChoicerFile(Context context,Boolean MultiChoice,OnGFileDialogResultListener listener,FilenameFilter Filefilter){
		GFileDialogsChoicer gfdc=new GFileDialogsChoicer(context,Filefilter,MultiChoice,listener);
		gfdc.show();
	}
	public  String GetFileExtension(){
		if (isDirectory()) {
			return "Directory";
		}else {
			return MimeTypeMap.getFileExtensionFromUrl( getAbsolutePath());
		}
	}
	public static String GetFileExtension(File file){
		if (file.isDirectory()) {
			return "Directory";
		}else {
			return MimeTypeMap.getFileExtensionFromUrl( file.getAbsolutePath());
		}

	}
	public static String GetFileExtension(String path){
		if ((new File(path)).isDirectory()) {
			return "Directory";
		}else {
			return MimeTypeMap.getFileExtensionFromUrl( path);
		}

	}
	public static FilenameFilter FilenameFilterForFileExtensions(final String[] FileExtensions){
		FilenameFilter fn=new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				for (String extension : FileExtensions) {
					if (name.toLowerCase().endsWith(extension)) {
						return true;
					}
				}
				return false;
			}
		};
		return fn;
	}
	public static String getMimeType(String url) {
		String type = null;
		String extension = MimeTypeMap.getFileExtensionFromUrl(url);

		if (extension != null) {
			type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
		}
		return type;
	}
	public  String getMimeType() {
		String type = null;
		String extension = MimeTypeMap.getFileExtensionFromUrl( getAbsolutePath());

		if (extension != null) {
			type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
		}
		return type;
	}

	public void GFCopy(final String destPath,@Nullable OnGFileDialogResultListener onGFileDialogResultListener){
		mOnGFileDialogResultListener=onGFileDialogResultListener;
		GFileDialogsProces mDialog=new GFileDialogsProces(mContext,destPath,GFileDialogProcesKind.Copy);


		mDialog.show();
	}
	public void GFCopy(final File dscDirectory,@Nullable  OnGFileDialogResultListener onGFileDialogResultListener){
		mOnGFileDialogResultListener=onGFileDialogResultListener;
		GFileDialogsProces mDialog=new GFileDialogsProces(mContext,dscDirectory.getAbsolutePath(),GFileDialogProcesKind.Copy);


		mDialog.show();
	}
	public void GFMove(final String destPath,@Nullable OnGFileDialogResultListener onGFileDialogResultListener){
		mOnGFileDialogResultListener=onGFileDialogResultListener;
		GFileDialogsProces mDialog=new GFileDialogsProces(mContext,destPath,GFileDialogProcesKind.Move);


		mDialog.show();
	}
	public void GFMove(final File dscDirectory,@Nullable  OnGFileDialogResultListener onGFileDialogResultListener){
		mOnGFileDialogResultListener=onGFileDialogResultListener;
		GFileDialogsProces mDialog=new GFileDialogsProces(mContext,dscDirectory.getAbsolutePath(),GFileDialogProcesKind.Move);


		mDialog.show();
	}
	public void GFDellete(@Nullable  OnGFileDialogResultListener onGFileDialogResultListener){
		mOnGFileDialogResultListener=onGFileDialogResultListener;
		GFileDialogsProces mDialog=new GFileDialogsProces(mContext,GFileDialogProcesKind.Dellete);


		mDialog.show();
	}
	public void GFRename(@Nullable OnGFileDialogResultListener onGFileDialogResultListener){
		mOnGFileDialogResultListener=onGFileDialogResultListener;

		if (this.exists()){
			if (isDirectory()){
				InputBox(R.string.title_new_name,R.string.message_new_folderename,R.string.hint_folder_name,getName(), InputType.TYPE_CLASS_TEXT,mContext,new inputBox.OnDialogResultListener() {
					@Override
					public boolean OnResult(Boolean dialogResult, String ValueResult) {

						if (dialogResult){
							File dscf=new File(getParent()+ File.separator+ValueResult);
							if(dscf.exists()){
								if (mOnGFileDialogResultListener!=null){
									String Notice;
									if (isDirectory()) {
										Notice=mContext.getResources().getString(R.string.promp_Folder_Exist)+"\n"+ValueResult;
									}else {
										Notice=mContext.getResources().getString(R.string.promp_File_Exist)+"\n"+ValueResult;
									}
									mOnGFileDialogResultListener.OnResult(GFileDialogsResults.GiveError,Notice,getAbsoluteFile());
									mOnGFileDialogResultListener.OnResult(GFileDialogsResults.GiveError,Notice,getAbsolutePath());
									mOnGFileDialogResultListener.OnResult(GFileDialogsResults.GiveError,Notice);

								}
							}else {
								try{
									if( renameTo(dscf)) {
										if (mOnGFileDialogResultListener!=null){
											mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Completed, "Success", dscf);
											mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Completed, "Success", dscf.getAbsolutePath());
											mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Completed, "Success");
										}

									}else {
										if (mOnGFileDialogResultListener!=null){
											mOnGFileDialogResultListener.OnResult(GFileDialogsResults.DontCompleted,"Cant Rename that",getAbsoluteFile());
											mOnGFileDialogResultListener.OnResult(GFileDialogsResults.DontCompleted,"Cant Rename that",getAbsolutePath());
											mOnGFileDialogResultListener.OnResult(GFileDialogsResults.DontCompleted,"Cant Rename that");
										}

									}

								}catch (Exception e){
									if (mOnGFileDialogResultListener!=null){
										mOnGFileDialogResultListener.OnResult(GFileDialogsResults.GiveError,e.getMessage(),getAbsoluteFile());
										mOnGFileDialogResultListener.OnResult(GFileDialogsResults.GiveError,e.getMessage(),getAbsolutePath());
										mOnGFileDialogResultListener.OnResult(GFileDialogsResults.GiveError,e.getMessage());
									}

								}





							}
						}else {
							if (mOnGFileDialogResultListener!=null){
								mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Cancelled,"Was canceled by the user",getAbsoluteFile());
								mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Cancelled,"Was canceled by the user",getAbsolutePath());
								mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Cancelled,"Was canceled by the user");
							}
						}
						return false;
					}
				});
			}else {
				InputBox(R.string.title_new_name,R.string.message_new_filerename, R.string.hint_folder_name,getName().replace("."+GetFileExtension(),""), InputType.TYPE_CLASS_TEXT,mContext,new inputBox.OnDialogResultListener() {
					@Override
					public boolean OnResult(Boolean dialogResult, String ValueResult) {

						if (dialogResult){
							File dscf=new File(getParent()+ File.separator+ValueResult+"."+GetFileExtension());
							if(dscf.exists()){
								if (mOnGFileDialogResultListener!=null){
									String Notice;
									if (isDirectory()) {
										Notice=mContext.getResources().getString(R.string.promp_Folder_Exist)+"\n"+ValueResult+"."+GetFileExtension();
									}else {
										Notice=mContext.getResources().getString(R.string.promp_File_Exist)+"\n"+ValueResult+"."+GetFileExtension();
									}
									mOnGFileDialogResultListener.OnResult(GFileDialogsResults.GiveError,Notice,getAbsoluteFile());
									mOnGFileDialogResultListener.OnResult(GFileDialogsResults.GiveError,Notice,getAbsolutePath());
									mOnGFileDialogResultListener.OnResult(GFileDialogsResults.GiveError,Notice);

								}
							}else {
								try{
									if( renameTo(dscf)) {
										if (mOnGFileDialogResultListener!=null){
											mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Completed, "Success", dscf);
											mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Completed, "Success", dscf.getAbsolutePath());
											mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Completed, "Success");
										}

									}else {
										if (mOnGFileDialogResultListener!=null){
											mOnGFileDialogResultListener.OnResult(GFileDialogsResults.DontCompleted,"Cant Rename that",getAbsoluteFile());
											mOnGFileDialogResultListener.OnResult(GFileDialogsResults.DontCompleted,"Cant Rename that",getAbsolutePath());
											mOnGFileDialogResultListener.OnResult(GFileDialogsResults.DontCompleted,"Cant Rename that");
										}

									}

								}catch (Exception e){
									if (mOnGFileDialogResultListener!=null){
										mOnGFileDialogResultListener.OnResult(GFileDialogsResults.GiveError,e.getMessage(),getAbsoluteFile());
										mOnGFileDialogResultListener.OnResult(GFileDialogsResults.GiveError,e.getMessage(),getAbsolutePath());
										mOnGFileDialogResultListener.OnResult(GFileDialogsResults.GiveError,e.getMessage());
									}

								}





							}
						}else {
							if (mOnGFileDialogResultListener!=null){
								mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Cancelled,"Was canceled by the user",getAbsoluteFile());
								mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Cancelled,"Was canceled by the user",getAbsolutePath());
								mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Cancelled,"Was canceled by the user");
							}
						}
						return false;
					}
				});
			}

		}else {
			if (mOnGFileDialogResultListener!=null){
				mOnGFileDialogResultListener.OnResult(GFileDialogsResults.GiveError,"Sorce file not found",getAbsoluteFile());
				mOnGFileDialogResultListener.OnResult(GFileDialogsResults.GiveError,"Sorce file not found",getAbsolutePath());
				mOnGFileDialogResultListener.OnResult(GFileDialogsResults.GiveError,"Sorce file not found");
			}

		}
	}
	public String GFSize(){

		long s1=GFSizeInByte();
		long s2=s1;
		if (s1<1025){

			return s1 + " Byte";
		}
		s2=s1/1024;
		if (s2<1025){

			return (s1/1024) +"."+ (s1 % 1024) + " KB";
		}
		s1=s2/1024;
		if (s1<1025){

			return (s2/1024) +"."+ (s2 % 1024) + " MB";
		}
		s2=s1/1024;
		if (s2<1025){

			return (s1/1024) +"."+ (s1 % 1024) + " GB";
		}

		return (s2/1024) +"."+ (s2 % 1024) + " TB";


	}
	public long GFSizeInByte(){
		if(isFile()) {
			return length();
		}else {
			long s = 0;
			for (File file:AllFiles()){
				s=s+file.length();
			}
			return s;
		}
	}
	public long GFSizeInKByte(){
		return GFSizeInByte()/1024;
	}
	public long GFSizeInMByte(){
		return GFSizeInKByte()/1024;
	}
	public long GFSizeInGByte(){
		return GFSizeInMByte()/1024;
	}
	public long GFSizeInTByte(){
		return GFSizeInGByte()/1024;
	}




	private   @Nullable AsyncTask AsyncTasks_CopyAndmkdirs(final File DestinationDirectory, final ProgressBar ProgressBarForShowProgress1, final ProgressBar ProgressBarForShowProgress2, final TextView TextViewForShowProgressNotice1, final TextView TextViewForShowProgressNotice2, final Handler HandlerForSetViews){
		//final File Source=new File(this.getAbsolutePath());

		AsyncTask <Void,Void,Void> asyncTask = null;

		if (isDirectory()) {
			asyncTask = new AsyncTask<Void, Void, Void>() {
				InputStream in = null;
				OutputStream out = null;

				@Override
				protected void onCancelled() {
					super.onCancelled();
					try {
						in.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					try {
						out.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				@Override
				protected Void doInBackground(Void... params) {
					(new File(DestinationDirectory + File.separator + getName())).mkdirs();

					ArrayList<File> DirectoryFiles = new ArrayList<File>();

					DirectoryFiles = AllFiles();
					final int DMaxP = DirectoryFiles.size();
					int DP = 0;
					String DestinationPathForFile;
					if (HandlerForSetViews != null) {
						HandlerForSetViews.post(new Runnable() {
							@Override
							public void run() {
								if (ProgressBarForShowProgress1!=null){
									ProgressBarForShowProgress1.setVisibility(View.VISIBLE);
									ProgressBarForShowProgress1.setProgress(0);
									ProgressBarForShowProgress1.setMax(DMaxP);

								}
								if (TextViewForShowProgressNotice1!=null){

									TextViewForShowProgressNotice1.setVisibility(View.VISIBLE);
								}
								if (TextViewForShowProgressNotice2!=null){
									TextViewForShowProgressNotice2.setVisibility(View.VISIBLE);
								}
								if (ProgressBarForShowProgress2!=null){
									ProgressBarForShowProgress2.setVisibility(View.VISIBLE);
								}



							}
						});
					}

					for ( File file : DirectoryFiles) {
						DestinationPathForFile = file.getAbsolutePath().replace(getParent(), DestinationDirectory.getAbsolutePath());
						if (TextViewForShowProgressNotice1 != null & HandlerForSetViews != null) {
							final String SourceD;
							SourceD = file.getParent().replace(getParent(),"") + " to " + DestinationDirectory;
							HandlerForSetViews.post(new Runnable() {
								@Override
								public void run() {
									TextViewForShowProgressNotice1.setText(SourceD);

								}
							});
						}
						if (TextViewForShowProgressNotice2 != null & HandlerForSetViews != null) {
							final String SourceF;
							SourceF = file.getName();
							HandlerForSetViews.post(new Runnable() {
								@Override
								public void run() {

									TextViewForShowProgressNotice2.setText(SourceF);
								}
							});
						}

						File SourceFile = file;
						File DestinationFile = new File(DestinationPathForFile + File.separator + SourceFile.getName());
						DestinationFile.getParentFile().mkdirs();
						long TotlaByte = 0;
						long CopyByte = 0;
						TotlaByte = SourceFile.length();

						byte[] buf = new byte[1024];
						int len;


						// Copy File ************************************************
						try {

							try {
								in = new FileInputStream(SourceFile);
							} catch (FileNotFoundException e) {
								e.printStackTrace();
							}

							try {

								try {
									out = new FileOutputStream(DestinationFile);
								} catch (FileNotFoundException e) {
									e.printStackTrace();
								}

								try {
									while ((len = in.read(buf)) > 0) {


										out.write(buf, 0, len);
										CopyByte = CopyByte + len;

										//Set Progress Bar
										if (HandlerForSetViews != null) {
											if (ProgressBarForShowProgress2 != null) {
												final int pers = Math.round(((float) ((CopyByte * ProgressBarForShowProgress2.getMax()) / TotlaByte)));
												final String FileNotice;
												FileNotice=SourceFile.getName() + ": Progress is " + ((pers*100)/ProgressBarForShowProgress2.getMax()) + " %";

												HandlerForSetViews.post(new Runnable() {
													@Override
													public void run() {
														try {
															ProgressBarForShowProgress2.setProgress(pers);
														} catch (Exception e) {

														}
														if (TextViewForShowProgressNotice2!=null) {  TextViewForShowProgressNotice2.setText(FileNotice);}

													}
												});
											} else if (ProgressBarForShowProgress1 != null) {
												final int pers = Math.round(((float) ((CopyByte * ProgressBarForShowProgress1.getMax()) / TotlaByte)));
												Log.d("bj_file", SourceFile.getName() + ": Progress is " + pers + "/" + ProgressBarForShowProgress1.getMax());
												HandlerForSetViews.post(new Runnable() {
													@Override
													public void run() {
														try {
															ProgressBarForShowProgress1.setSecondaryProgress(pers);
														} catch (Exception e) {

														}

													}
												});
											}
										}

									}
								} catch (Exception e) {
									e.printStackTrace();
								}
							} finally {
								try {
									out.close();
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						} finally {
							try {
								in.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}

						//***********************************************************
						DP++;
						if (ProgressBarForShowProgress1 != null & HandlerForSetViews != null) {
							final int finalDP = DP;
							HandlerForSetViews.post(new Runnable() {
								@Override
								public void run() {

									ProgressBarForShowProgress1.setProgress(finalDP);

								}
							});
						}
					}


					return null;
				}
			};

		}else {
			asyncTask = new AsyncTask<Void, Void, Void>() {
				InputStream in = null;
				OutputStream out = null;

				@Override
				protected void onCancelled() {
					super.onCancelled();
					try {
						in.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					try {
						out.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				@Override
				protected Void doInBackground(Void... params) {

					if (HandlerForSetViews != null) {
						final String SourceD;
						SourceD = getName() + " to " +DestinationDirectory;
						HandlerForSetViews.post(new Runnable() {
							@Override
							public void run() {
								if (ProgressBarForShowProgress1!=null){
									ProgressBarForShowProgress1.setVisibility(View.VISIBLE);
									ProgressBarForShowProgress1.setProgress(0);
								}
								if (TextViewForShowProgressNotice1!=null){
									TextViewForShowProgressNotice1.setText(SourceD);
									TextViewForShowProgressNotice1.setVisibility(View.VISIBLE);
								}
								if (TextViewForShowProgressNotice2!=null){
									TextViewForShowProgressNotice2.setVisibility(View.GONE);
								}
								if (ProgressBarForShowProgress2!=null){
									ProgressBarForShowProgress2.setVisibility(View.GONE);
								}


							}
						});
					}



					File DestinationFile = new File(DestinationDirectory + File.separator + getName());
					DestinationFile.getParentFile().mkdirs();
					long TotlaByte = 0;
					long CopyByte = 0;
					TotlaByte = length();

					byte[] buf = new byte[1024];
					int len;


					// Copy File ************************************************
					try {

						try {
							in = new FileInputStream(getAbsoluteFile());
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						}

						try {

							try {
								out = new FileOutputStream(DestinationFile);
							} catch (FileNotFoundException e) {
								e.printStackTrace();
							}

							try {
								while ((len = in.read(buf)) > 0) {



									out.write(buf, 0, len);
									CopyByte = CopyByte + len;

									//Set Progress Bar
									if (HandlerForSetViews != null) {
										final int pers = Math.round(((float) ((CopyByte * ProgressBarForShowProgress2.getMax()) / TotlaByte)));
										Log.d("bj_file", getName() + ": Progress is " + pers + "/" + ProgressBarForShowProgress2.getMax());
										HandlerForSetViews.post(new Runnable() {
											@Override
											public void run() {

												try {
													if (ProgressBarForShowProgress1!=null){    ProgressBarForShowProgress1.setProgress(pers);}

												} catch (Exception e) {

												}

											}
										});

									}

								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						} finally {
							try {
								out.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					} finally {
						try {
							in.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					return null;
				}
			};
		}

		return asyncTask;
	}

	// Base 1 GFileDialogsChoicer
	private class GFileDialogsProces extends Dialog {
		private class GFileDialogsProcesView extends View {
			public int getpixels(int dp){

				//Resources r = boardContext.getResources();
				//float px = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpis, r.getDisplayMetrics());

				final float scale = getResources().getDisplayMetrics().density;
				int px = (int) (dp * scale + 0.5f);



				return px;

			}
			Context vContext;

			LinearLayout mView,LL_Border,LL_Button,LL_Title;
			Button GF_BTN_Cancel;
			TextView GF_TXV_Title,GF_TXV_Notice1,GF_TXV_Notice2;
			ProgressBar GF_PRGB_1,GF_PRGB_2;

			public GFileDialogsProcesView(Context context, String MyTitle) {
				super(context);
				vContext=context;


				mView=new LinearLayout(vContext);
				mView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
				mView.setPadding(5,5,5,5);
				mView.setOrientation(LinearLayout.VERTICAL);

				LL_Border=new LinearLayout(vContext);
				LL_Border.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
				LL_Border.setBackgroundResource(R.drawable.border);
				LL_Border.setOrientation(LinearLayout.VERTICAL);
				LL_Border.setPadding(5,5,5,5);

				LL_Button=new LinearLayout(vContext);
				LL_Button.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
				LL_Button.setGravity(Gravity.CENTER);
				LL_Button.setPadding(10,35,10,10);

				LL_Title=new LinearLayout(vContext);
				LL_Title.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
				LL_Title.setGravity(Gravity.CENTER);


				GF_BTN_Cancel= new Button(vContext);
				GF_BTN_Cancel.setWidth(getpixels(200));
				GF_BTN_Cancel.setHeight(getpixels(30));
				GF_BTN_Cancel.setBackgroundColor(vContext.getResources().getColor(R.color.colorAccent));
				GF_BTN_Cancel.setTextColor(Color.WHITE);
				GF_BTN_Cancel.setText(R.string.Action_Cancel);

				GF_TXV_Title=new TextView(vContext);
				GF_TXV_Title.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
				GF_TXV_Title.setBackgroundColor(vContext.getResources().getColor(R.color.colorPrimaryDark));
				GF_TXV_Title.setTextColor(Color.WHITE);
				GF_TXV_Title.setPadding(5,5,5,5);
				GF_TXV_Title.setText(MyTitle);
				GF_TXV_Title.setGravity(Gravity.CENTER_VERTICAL);
				GF_TXV_Title.setTextAppearance(R.style.TextAppearance_AppCompat_Title);


				GF_TXV_Notice1=new TextView(vContext);
				GF_TXV_Notice1.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

				GF_TXV_Notice2=new TextView(vContext);
				GF_TXV_Notice2.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));


				GF_PRGB_1=new ProgressBar(vContext,null,android.R.attr.progressBarStyleHorizontal);
				GF_PRGB_1.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
				GF_PRGB_1.setScrollBarStyle(SCROLL_AXIS_HORIZONTAL);
				GF_PRGB_1.setMax(100);
				GF_PRGB_1.setIndeterminate(false);



				GF_PRGB_2=new ProgressBar(vContext,null,android.R.attr.progressBarStyleHorizontal);
				GF_PRGB_2.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
				GF_PRGB_2.setScrollBarStyle(SCROLL_AXIS_HORIZONTAL);
				GF_PRGB_2.setMax(100);
				GF_PRGB_2.setIndeterminate(false);

				LL_Title.addView(GF_TXV_Title);
				LL_Button.addView(GF_BTN_Cancel);



				LL_Border.addView(GF_PRGB_1);
				LL_Border.addView(GF_TXV_Notice1);
				LL_Border.addView(GF_PRGB_2);
				LL_Border.addView(GF_TXV_Notice2);


				LL_Border.addView(LL_Button);

				mView.addView(LL_Title);
				mView.addView(LL_Border);
			}





		}
		Context mContex;
		String mdestPath;
		File mDscDirectory;
		File mDscFile;
		ProgressBar PRGB1,PRGB2;
		TextView TXV_Notice1,TXV_Notice2,TXV_Title;
		Button BTNCancel;
		private AsyncTask<Void, Integer, String> async1;
		private Handler mHandler;
		@GFileDialogProcesKind int mDialogProcesKind;



		public GFileDialogsProces(Context context, String DscDirectoryPath,@GFileDialogProcesKind int DialogProcesKind) {
			super(context);
			// Required empty public constructor
			mContex=context;
			mdestPath=DscDirectoryPath;
			mDscDirectory=new File(DscDirectoryPath);
			mDscFile=new File(mDscDirectory + File.separator+ bj_file.this.getName());
			mDialogProcesKind=DialogProcesKind;
		}
		public GFileDialogsProces(Context context,@GFileDialogProcesKind int DialogProcesKindOnlyForDelete) {
			super(context);
			// Required empty public constructor
			mContex=context;
			mDialogProcesKind=GFileDialogProcesKind.Dellete;
		}
		public class FileCopy extends AsyncTask<Void,Integer,String>{

			InputStream in = null;
			OutputStream out = null;
			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						PRGB2.setVisibility(View.GONE);
						TXV_Notice2.setVisibility(View.GONE);
						TXV_Title.setText("Copying File...");
						TXV_Notice1.setText(getName() + "\n"+ "to"+ mdestPath);
						PRGB1.setProgress(0);
						PRGB1.setMax(100);
					}
				});
			}

			@Override
			protected void onProgressUpdate(final Integer... values) {
				super.onProgressUpdate(values);
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						PRGB1.setProgress(values[0]);
						TXV_Title.setText("Copying File..." + values[0] + " %");
					}
				});

			}

			@Override
			protected void onCancelled() {
				super.onCancelled();
				if (mOnGFileDialogResultListener != null) {
					mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Cancelled, "Was canceled by the user", getAbsoluteFile());
					mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Cancelled, "Was canceled by the user", getAbsolutePath());
					mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Cancelled, "Was canceled by the user");
					mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Cancelled, "Was canceled by the user", bj_file.this);
				}
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				dismiss();
			}

			@Override
			protected void onPostExecute(String s) {
				super.onPostExecute(s);

				if (mOnGFileDialogResultListener != null) {
					if(s.equals("")) {
						mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Completed, "The file was successfully copied", mDscFile);
						mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Completed, "The file was successfully copied", mDscFile.getAbsolutePath());
						mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Completed, "The file was successfully copied");
						mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Completed, "The file was successfully copied", new bj_file(mDscFile.getAbsolutePath(),mContext));
					}else {
						mOnGFileDialogResultListener.OnResult(GFileDialogsResults.GiveError, s, getAbsoluteFile());
						mOnGFileDialogResultListener.OnResult(GFileDialogsResults.GiveError, s, getAbsolutePath());
						mOnGFileDialogResultListener.OnResult(GFileDialogsResults.GiveError, s);
						mOnGFileDialogResultListener.OnResult(GFileDialogsResults.GiveError, s, bj_file.this);
					}

				}
				dismiss();
			}

			@Override
			protected String doInBackground(Void... params) {
				Log.d("bj_file",getAbsolutePath() + "\n" + "to" + mDscFile.getAbsolutePath());
				mDscFile.getParentFile().mkdirs();
				long TotlaByte = 0;
				long CopyByte = 0;
				TotlaByte = bj_file.this.length();




				byte[] buf = new byte[1024];
				int len;

				// Copy File ************************************************
				if (!getAbsolutePath().equals(mDscFile.getAbsolutePath())){
					try {

						try {
							in = new FileInputStream(getAbsoluteFile());
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						}

						try {

							try {
								out = new FileOutputStream(mDscFile);
							} catch (FileNotFoundException e) {
								e.printStackTrace();
							}

							try {
								while ((len = in.read(buf)) > 0) {



									out.write(buf, 0, len);
									CopyByte = CopyByte + len;
									float pers;
									pers=(CopyByte*100)/TotlaByte;
									//Set Progress Bar
									publishProgress(Math.round(pers));

								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						} finally {
							try {
								if (out!=null){   out.close();}
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					} finally {
						try {
							in.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}

				if(mDscFile.exists()) {
					if (mDscFile.length() == bj_file.this.length()) {
						return "";
					} else {
						return "File is incompletely copied";
					}
				}else {
					return "For some reason, the file could not be copied";
				}
			}
		}
		public class FolderCopy extends AsyncTask<Void,Integer,String>{

			InputStream in = null;
			OutputStream out = null;
			ArrayList<File> SourceFiles;
			ArrayList<File> SourceFolders=new ArrayList<File>();

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				SourceFiles=AllFiles();
				for (File f:AllFolders()){
					if (f.list().length==0){
						SourceFolders.add(f);
					}
				}
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						PRGB2.setVisibility(View.VISIBLE);
						TXV_Notice2.setVisibility(View.VISIBLE);
						TXV_Title.setText("Copying Folder...");
						TXV_Notice1.setText(getName() + "\n"+ "to"+ mDscDirectory.getName());
						PRGB1.setProgress(0);
						int c = 0;
						if (SourceFiles!=null){
							c=SourceFiles.size();
						}
						if (SourceFolders!=null){
							c=c+SourceFolders.size();
						}
						PRGB1.setMax(c);
						PRGB2.setMax(100);
						PRGB2.setProgress(0);
					}
				});
			}

			@Override
			protected void onProgressUpdate(final Integer... values) {
				super.onProgressUpdate(values);
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						PRGB1.setProgress(values[0]);

					}
				});

			}

			@Override
			protected void onCancelled() {
				super.onCancelled();
				if (mOnGFileDialogResultListener != null) {
					mOnGFileDialogResultListener.OnResult(GFileDialogsResults.DontCompleted, "Was canceled by the user", getAbsoluteFile());
					mOnGFileDialogResultListener.OnResult(GFileDialogsResults.DontCompleted, "Was canceled by the user", getAbsolutePath());
					mOnGFileDialogResultListener.OnResult(GFileDialogsResults.DontCompleted, "Was canceled by the user");
					mOnGFileDialogResultListener.OnResult(GFileDialogsResults.DontCompleted, "Was canceled by the user", bj_file.this);
				}
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				dismiss();
			}

			@Override
			protected void onPostExecute(String s) {
				super.onPostExecute(s);

				if (mOnGFileDialogResultListener != null) {
					if(s.equals("")) {
						mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Completed, "The file was successfully copied", mDscFile);
						mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Completed, "The file was successfully copied", mDscFile.getAbsolutePath());
						mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Completed, "The file was successfully copied");
						mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Completed, "The file was successfully copied", new bj_file(mDscFile.getAbsolutePath(),mContext));
					}else {
						mOnGFileDialogResultListener.OnResult(GFileDialogsResults.GiveError, s, getAbsoluteFile());
						mOnGFileDialogResultListener.OnResult(GFileDialogsResults.GiveError, s, getAbsolutePath());
						mOnGFileDialogResultListener.OnResult(GFileDialogsResults.GiveError, s);
						mOnGFileDialogResultListener.OnResult(GFileDialogsResults.GiveError, s, bj_file.this);
					}

				}
				dismiss();
			}

			@Override
			protected String doInBackground(Void... params) {

				mDscFile.mkdirs();
				if (SourceFolders!=null){
					for (File f : SourceFolders){
						File dDscFile=new File(f.getAbsolutePath().replace(getAbsolutePath(),mDscFile.getAbsolutePath()));
						dDscFile.mkdirs();
						publishProgress(PRGB1.getProgress()+1);
					}
				}
				if (SourceFiles!=null){
					for (final File file:SourceFiles){
						long TotlaByte = 0;
						long CopyByte = 0;
						TotlaByte = file.length();
						File fDscFile=new File(file.getAbsolutePath().replace(getAbsolutePath(),mDscFile.getAbsolutePath()));
						Log.d("bj_file",getAbsolutePath() + "\n" + mDscFile.getAbsolutePath() + "\n"+ fDscFile.getAbsolutePath());
						fDscFile.getParentFile().mkdirs();
						mHandler.post(new Runnable() {
							@Override
							public void run() {
								TXV_Notice2.setText(file.getParentFile().getName() + "\n" + file.getName());
								PRGB2.setProgress(0);
							}
						});

						byte[] buf = new byte[1024];
						int len;

						// Copy File ************************************************
						if (!file.getAbsolutePath().equals(fDscFile.getAbsolutePath())){
							try {

								try {
									in = new FileInputStream(file);
								} catch (FileNotFoundException e) {
									e.printStackTrace();
								}

								try {

									try {
										out = new FileOutputStream(fDscFile);
									} catch (FileNotFoundException e) {
										e.printStackTrace();
									}

									try {
										while ((len = in.read(buf)) > 0) {



											out.write(buf, 0, len);
											CopyByte = CopyByte + len;
											final float pers;
											pers=(CopyByte*100)/TotlaByte;
											//Set Progress Bar
											mHandler.post(new Runnable() {
												@Override
												public void run() {
													TXV_Notice2.setText(file.getParentFile().getName() + "\n" + file.getName() + " " + Math.round(pers) + " %");
													PRGB2.setProgress(Math.round(pers));
												}
											});



										}
									} catch (Exception e) {
										e.printStackTrace();
									}
								} finally {
									try {
										out.close();
									} catch (IOException e) {
										e.printStackTrace();
									}
								}
							} finally {
								try {
									in.close();
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}


						publishProgress(PRGB1.getProgress()+1);
					}
				}


				if(mDscFile.exists()) {
					return "";
				}else {
					return "The Folder could not be copied";
				}
			}
		}
		public class FileMove extends AsyncTask<Void,Integer,String>{

			InputStream in = null;
			OutputStream out = null;
			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						PRGB2.setVisibility(View.GONE);
						TXV_Notice2.setVisibility(View.GONE);
						TXV_Title.setText("Copying File...");
						TXV_Notice1.setText(getName() + "\n"+ "to"+ mdestPath);
						PRGB1.setProgress(0);
						PRGB1.setMax(100);
					}
				});
			}

			@Override
			protected void onProgressUpdate(final Integer... values) {
				super.onProgressUpdate(values);
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						PRGB1.setProgress(values[0]);
						TXV_Title.setText("Copying File..." + values[0] + " %");
					}
				});

			}

			@Override
			protected void onCancelled() {
				super.onCancelled();
				if (mOnGFileDialogResultListener != null) {
					mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Cancelled, "Was canceled by the user", getAbsoluteFile());
					mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Cancelled, "Was canceled by the user", getAbsolutePath());
					mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Cancelled, "Was canceled by the user");
					mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Cancelled, "Was canceled by the user", bj_file.this);
				}
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				dismiss();
			}

			@Override
			protected void onPostExecute(String s) {
				super.onPostExecute(s);

				if (mOnGFileDialogResultListener != null) {
					if(s.equals("")) {
						if (!mDscFile.getAbsolutePath().equals(getAbsolutePath())) {
							if (delete()) {
								mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Completed, "The file was successfully Moved", mDscFile);
								mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Completed, "The file was successfully Moved", mDscFile.getAbsolutePath());
								mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Completed, "The file was successfully Moved");
								mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Completed, "The file was successfully Moved", new bj_file(mDscFile.getAbsolutePath(), mContext));
							} else {
								mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Completed, "The file was successfully copied ,BUT DONT REMOVE", mDscFile);
								mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Completed, "The file was successfully copied, BUT DONT REMOVE", mDscFile.getAbsolutePath());
								mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Completed, "The file was successfully copied, BUT DONT REMOVE");
								mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Completed, "The file was successfully copied, BUT DONT REMOVE", new bj_file(mDscFile.getAbsolutePath(), mContext));

							}
						}else {
							mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Completed, "The file was successfully Moved", mDscFile);
							mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Completed, "The file was successfully Moved", mDscFile.getAbsolutePath());
							mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Completed, "The file was successfully Moved");
							mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Completed, "The file was successfully Moved", new bj_file(mDscFile.getAbsolutePath(), mContext));

						}

					}else {
						mOnGFileDialogResultListener.OnResult(GFileDialogsResults.GiveError, s, getAbsoluteFile());
						mOnGFileDialogResultListener.OnResult(GFileDialogsResults.GiveError, s, getAbsolutePath());
						mOnGFileDialogResultListener.OnResult(GFileDialogsResults.GiveError, s);
						mOnGFileDialogResultListener.OnResult(GFileDialogsResults.GiveError, s, bj_file.this);
					}

				}
				dismiss();
			}

			@Override
			protected String doInBackground(Void... params) {

				mDscFile.getParentFile().mkdirs();
				long TotlaByte = 0;
				long CopyByte = 0;
				TotlaByte = bj_file.this.length();




				byte[] buf = new byte[1024];
				int len;

				// Copy File ************************************************
				if (!getAbsolutePath().equals(mDscFile.getAbsolutePath())){
					try {

						try {
							in = new FileInputStream(getAbsoluteFile());
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						}

						try {

							try {
								out = new FileOutputStream(mDscFile);
							} catch (FileNotFoundException e) {
								e.printStackTrace();
							}

							try {
								while ((len = in.read(buf)) > 0) {



									out.write(buf, 0, len);
									CopyByte = CopyByte + len;
									float pers;
									pers=(CopyByte*100)/TotlaByte;
									//Set Progress Bar
									publishProgress(Math.round(pers));

								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						} finally {
							try {
								out.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					} finally {
						try {
							in.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}

				if(mDscFile.exists()) {
					if (mDscFile.length() == bj_file.this.length()) {
						return "";
					} else {
						return "File is incompletely Moved";
					}
				}else {
					return "For some reason, the file could not be Moved";
				}
			}
		}
		public class FolderMove extends AsyncTask<Void,Integer,String>{

			InputStream in = null;
			OutputStream out = null;
			ArrayList<File> SourceFiles;
			ArrayList<File> SourceFolders=new ArrayList<File>();

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				SourceFiles=AllFiles();
				SourceFolders=AllFolders();
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						PRGB2.setVisibility(View.VISIBLE);
						TXV_Notice2.setVisibility(View.VISIBLE);
						TXV_Title.setText("Copying Folder...");
						TXV_Notice1.setText(getName() + "\n"+ "to"+ mDscDirectory.getName());
						PRGB1.setProgress(0);
						int c = 0;
						if (SourceFiles!=null){
							c=SourceFiles.size();
						}
						if (SourceFolders!=null){
							c=c+SourceFolders.size();
						}
						PRGB1.setMax(c);
						PRGB2.setMax(100);
						PRGB2.setProgress(0);
					}
				});
			}

			@Override
			protected void onProgressUpdate(final Integer... values) {
				super.onProgressUpdate(values);
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						PRGB1.setProgress(values[0]);

					}
				});

			}

			@Override
			protected void onCancelled() {
				super.onCancelled();
				if (mOnGFileDialogResultListener != null) {
					mOnGFileDialogResultListener.OnResult(GFileDialogsResults.DontCompleted, "Was canceled by the user", getAbsoluteFile());
					mOnGFileDialogResultListener.OnResult(GFileDialogsResults.DontCompleted, "Was canceled by the user", getAbsolutePath());
					mOnGFileDialogResultListener.OnResult(GFileDialogsResults.DontCompleted, "Was canceled by the user");
					mOnGFileDialogResultListener.OnResult(GFileDialogsResults.DontCompleted, "Was canceled by the user", bj_file.this);
				}
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				dismiss();
			}

			@Override
			protected void onPostExecute(String s) {
				super.onPostExecute(s);

				if (mOnGFileDialogResultListener != null) {
					if(s.equals("")) {
						if (!mDscFile.getAbsolutePath().equals(getAbsolutePath())) {
							SourceFolders=AllFolders();
							if (SourceFolders!=null){
								if (SourceFolders.size()==0) {
									SourceFolders=null;
								}else {
									SourceFolders.sort(new Comparator<File>() {
										@Override
										public int compare(File o1, File o2) {
											if (o1.getAbsolutePath().length()>o2.getAbsolutePath().length()) {
												return 0;
											}else {
												return 1;
											}
										}
									});
								}
							}
							while (SourceFolders!=null ){
								for (File file:SourceFolders){
									file.delete();
								}
								SourceFolders=AllFolders();
								if (SourceFolders!=null){
									if (SourceFolders.size()==0) {
										SourceFolders=null;
									}else {
										SourceFolders.sort(new Comparator<File>() {
											@Override
											public int compare(File o1, File o2) {
												if (o1.getAbsolutePath().length()>o2.getAbsolutePath().length()) {
													return 0;
												}else {
													return 1;
												}
											}
										});
									}
								}
							}



							if (delete()) {
								mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Completed, "The Folder was successfully Moved", mDscFile);
								mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Completed, "The Folder was successfully Moved", mDscFile.getAbsolutePath());
								mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Completed, "The Folder was successfully Moved");
								mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Completed, "The Folder was successfully Moved", new bj_file(mDscFile.getAbsolutePath(), mContext));
							} else {
								mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Completed, "The Folder was successfully copied ,BUT DONT REMOVE", mDscFile);
								mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Completed, "The Folder was successfully copied ,BUT DONT REMOVE", mDscFile.getAbsolutePath());
								mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Completed, "The Folder was successfully copied ,BUT DONT REMOVE");
								mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Completed, "The Folder was successfully copied ,BUT DONT REMOVE", new bj_file(mDscFile.getAbsolutePath(), mContext));

							}
						}else {
							mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Completed, "The Folder was successfully Moved", mDscFile);
							mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Completed, "The Folder was successfully Moved", mDscFile.getAbsolutePath());
							mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Completed, "The Folder was successfully Moved");
							mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Completed, "The Folder was successfully Moved", new bj_file(mDscFile.getAbsolutePath(), mContext));

						}

					}else {
						mOnGFileDialogResultListener.OnResult(GFileDialogsResults.GiveError, s, getAbsoluteFile());
						mOnGFileDialogResultListener.OnResult(GFileDialogsResults.GiveError, s, getAbsolutePath());
						mOnGFileDialogResultListener.OnResult(GFileDialogsResults.GiveError, s);
						mOnGFileDialogResultListener.OnResult(GFileDialogsResults.GiveError, s, bj_file.this);
					}

				}
				dismiss();
			}

			@Override
			protected String doInBackground(Void... params) {
				if (!mDscFile.getAbsolutePath().equals(getAbsolutePath())){
					mDscFile.mkdirs();
					if (SourceFolders!=null){
						for (File f : SourceFolders){
							File dDscFile=new File(f.getAbsolutePath().replace(getAbsolutePath(),mDscFile.getAbsolutePath()));
							Log.d("bj_file",dDscFile.getAbsolutePath());
							dDscFile.mkdir();
							publishProgress(PRGB1.getProgress()+1);
						}
					}
					if (SourceFiles!=null){
						for (final File file:SourceFiles){
							long TotlaByte = 0;
							long CopyByte = 0;
							TotlaByte = file.length();
							File fDscFile=new File(file.getAbsolutePath().replace(getAbsolutePath(),mDscFile.getAbsolutePath()));
							Log.d("bj_file",getAbsolutePath() + "\n" + mDscFile.getAbsolutePath() + "\n"+ fDscFile.getAbsolutePath());

							fDscFile.getParentFile().mkdirs();
							mHandler.post(new Runnable() {
								@Override
								public void run() {
									TXV_Notice2.setText(file.getParentFile().getName() + "\n" + file.getName());
									PRGB2.setProgress(0);
								}
							});

							byte[] buf = new byte[1024];
							int len;

							// Copy File ************************************************
							if (!file.getAbsolutePath().equals(fDscFile.getAbsolutePath())){
								try {

									try {
										in = new FileInputStream(file);
									} catch (FileNotFoundException e) {
										e.printStackTrace();
									}

									try {

										try {
											out = new FileOutputStream(fDscFile);
										} catch (FileNotFoundException e) {
											e.printStackTrace();
										}

										try {
											while ((len = in.read(buf)) > 0) {



												out.write(buf, 0, len);
												CopyByte = CopyByte + len;
												final float pers;
												pers=(CopyByte*100)/TotlaByte;
												//Set Progress Bar
												mHandler.post(new Runnable() {
													@Override
													public void run() {
														TXV_Notice2.setText(file.getParentFile().getName() + "\n" + file.getName() + " " + Math.round(pers) + " %");
														PRGB2.setProgress(Math.round(pers));
													}
												});



											}
										} catch (Exception e) {
											e.printStackTrace();
										}
									} finally {
										try {
											out.close();
										} catch (IOException e) {
											e.printStackTrace();
										}
									}
								} finally {
									try {
										in.close();
									} catch (IOException e) {
										e.printStackTrace();
									}
								}

							}

							if (fDscFile.exists() & fDscFile.length()==file.length()){
								file.delete();
							}
							publishProgress(PRGB1.getProgress()+1);
						}
					}
				}



				if(mDscFile.exists()) {
					return "";
				}else {
					return "The Folder could not be Moved";
				}
			}
		}
		public class FolderDellete extends AsyncTask<Void,Integer,String>{


			ArrayList<File> SourceFiles;
			ArrayList<File> SourceFolders=new ArrayList<File>();

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				SourceFiles=AllFiles();
				SourceFolders=AllFolders();
				SourceFolders.sort(new Comparator<File>() {
					@Override
					public int compare(File o1, File o2) {
						if (o1.getAbsolutePath().length()>o2.getAbsolutePath().length()) {
							return 0;
						}else {
							return 1;
						}
					}
				});
				SourceFiles.sort(new Comparator<File>() {
					@Override
					public int compare(File o1, File o2) {
						if (o1.getAbsolutePath().length()>o2.getAbsolutePath().length()) {
							return 0;
						}else {
							return 1;
						}
					}
				});
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						PRGB2.setVisibility(View.GONE);
						TXV_Notice2.setVisibility(View.GONE);
						TXV_Title.setText("Delleting Folder...");
						TXV_Notice1.setText("Delleting the "+ getName());
						PRGB1.setProgress(0);
						int c = 0;
						if (SourceFiles!=null){
							c=SourceFiles.size();
						}
						if (SourceFolders!=null){
							c=c+SourceFolders.size();
						}
						PRGB1.setMax(c);
						PRGB2.setMax(100);
						PRGB2.setProgress(0);
					}
				});
			}

			@Override
			protected void onProgressUpdate(final Integer... values){
				super.onProgressUpdate(values);
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						try{
							PRGB1.setProgress(values[0]);
						}catch (Exception e){

						}

					}
				});

			}

			@Override
			protected void onCancelled() {
				super.onCancelled();
				if (mOnGFileDialogResultListener != null) {
					mOnGFileDialogResultListener.OnResult(GFileDialogsResults.DontCompleted, "Was canceled by the user", getAbsoluteFile());
					mOnGFileDialogResultListener.OnResult(GFileDialogsResults.DontCompleted, "Was canceled by the user", getAbsolutePath());
					mOnGFileDialogResultListener.OnResult(GFileDialogsResults.DontCompleted, "Was canceled by the user");
					mOnGFileDialogResultListener.OnResult(GFileDialogsResults.DontCompleted, "Was canceled by the user", bj_file.this);
				}

				dismiss();
			}

			@Override
			protected void onPostExecute(String s) {
				super.onPostExecute(s);

				if (mOnGFileDialogResultListener != null) {
					if(s.equals("")) {

						mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Completed, "The Folder was successfully Delleted");


					}else {

						mOnGFileDialogResultListener.OnResult(GFileDialogsResults.GiveError, s);

					}

				}
				dismiss();
			}

			@Override
			protected String doInBackground(Void... params) {
				int m=SourceFiles.size()+SourceFolders.size();
				while (SourceFiles!=null ){
					publishProgress(m-(SourceFiles.size()+SourceFolders.size()));
					for (final File file:SourceFiles){
						file.delete();
						mHandler.post(new Runnable() {
							@Override
							public void run() {
								TXV_Notice1.setText("Delleting the "+ file.getName());
							}
						});
						publishProgress(PRGB1.getProgress()+1);
					}

					SourceFiles=AllFiles();
					if (SourceFiles!=null){
						if (SourceFiles.size()==0) {
							SourceFiles=null;
						}else {
							SourceFiles.sort(new Comparator<File>() {
								@Override
								public int compare(File o1, File o2) {
									if (o1.getAbsolutePath().length()>o2.getAbsolutePath().length()) {
										return 0;
									}else {
										return 1;
									}
								}
							});
						}
					}
				}


				while (SourceFolders!=null ){
					publishProgress(m-SourceFolders.size());
					for (final File file:SourceFolders){
						file.delete();
						mHandler.post(new Runnable() {
							@Override
							public void run() {
								TXV_Notice1.setText("Delleting the "+ file.getName());
							}
						});
						publishProgress(PRGB1.getProgress()+1);
					}

					SourceFolders=AllFolders();
					if (SourceFolders!=null){
						if (SourceFolders.size()==0) {
							SourceFolders=null;
						}else {
							SourceFolders.sort(new Comparator<File>() {
								@Override
								public int compare(File o1, File o2) {
									if (o1.getAbsolutePath().length()>o2.getAbsolutePath().length()) {
										return 0;
									}else {
										return 1;
									}
								}
							});
						}
					}
				}
				delete();
				if(!exists()) {
					return "";
				}else {
					return "The Folder could not be Dellete";
				}
			}
		}

		public AsyncTask<Void,Integer,String> AsyncTaskForFileCopy=new AsyncTask<Void, Integer, String>() {
			InputStream in = null;
			OutputStream out = null;
			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						PRGB2.setVisibility(View.GONE);
						TXV_Notice2.setVisibility(View.GONE);
						TXV_Title.setText("Copying File...");
						TXV_Notice1.setText(getName() + "\n"+ "to"+ mdestPath);
						PRGB1.setProgress(0);
						PRGB1.setMax(100);
					}
				});
			}

			@Override
			protected void onProgressUpdate(final Integer... values) {
				super.onProgressUpdate(values);
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						PRGB1.setProgress(values[0]);
						TXV_Title.setText("Copying File..." + values[0] + " %");
					}
				});

			}

			@Override
			protected void onCancelled() {
				super.onCancelled();
				if (mOnGFileDialogResultListener != null) {
					mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Cancelled, "Was canceled by the user", getAbsoluteFile());
					mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Cancelled, "Was canceled by the user", getAbsolutePath());
					mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Cancelled, "Was canceled by the user");
					mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Cancelled, "Was canceled by the user", bj_file.this);
				}
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				dismiss();
			}

			@Override
			protected void onPostExecute(String s) {
				super.onPostExecute(s);

				if (mOnGFileDialogResultListener != null) {
					if(s.equals("")) {
						mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Completed, "The file was successfully copied", mDscFile);
						mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Completed, "The file was successfully copied", mDscFile.getAbsolutePath());
						mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Completed, "The file was successfully copied");
						mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Completed, "The file was successfully copied", new bj_file(mDscFile.getAbsolutePath(),mContext));
					}else {
						mOnGFileDialogResultListener.OnResult(GFileDialogsResults.GiveError, s, getAbsoluteFile());
						mOnGFileDialogResultListener.OnResult(GFileDialogsResults.GiveError, s, getAbsolutePath());
						mOnGFileDialogResultListener.OnResult(GFileDialogsResults.GiveError, s);
						mOnGFileDialogResultListener.OnResult(GFileDialogsResults.GiveError, s, bj_file.this);
					}

				}
				dismiss();
			}

			@Override
			protected String doInBackground(Void... params) {
				Log.d("bj_file",getAbsolutePath() + "\n" + "to" + mDscFile.getAbsolutePath());
				mDscFile.getParentFile().mkdirs();
				long TotlaByte = 0;
				long CopyByte = 0;
				TotlaByte = bj_file.this.length();




				byte[] buf = new byte[1024];
				int len;

				// Copy File ************************************************
				if (!getAbsolutePath().equals(mDscFile.getAbsolutePath())){
					try {

						try {
							in = new FileInputStream(getAbsoluteFile());
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						}

						try {

							try {
								out = new FileOutputStream(mDscFile);
							} catch (FileNotFoundException e) {
								e.printStackTrace();
							}

							try {
								while ((len = in.read(buf)) > 0) {



									out.write(buf, 0, len);
									CopyByte = CopyByte + len;
									float pers;
									pers=(CopyByte*100)/TotlaByte;
									//Set Progress Bar
									publishProgress(Math.round(pers));

								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						} finally {
							try {
								if (out!=null){   out.close();}
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					} finally {
						try {
							in.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}

				if(mDscFile.exists()) {
					if (mDscFile.length() == bj_file.this.length()) {
						return "";
					} else {
						return "File is incompletely copied";
					}
				}else {
					return "For some reason, the file could not be copied";
				}
			}
		};
		public AsyncTask<Void,Integer,String> AsyncTaskForFolderCopy=new AsyncTask<Void, Integer, String>() {
			InputStream in = null;
			OutputStream out = null;
			ArrayList<File> SourceFiles;
			ArrayList<File> SourceFolders=new ArrayList<File>();

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				SourceFiles=AllFiles();
				for (File f:AllFolders()){
					if (f.list().length==0){
						SourceFolders.add(f);
					}
				}
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						PRGB2.setVisibility(View.VISIBLE);
						TXV_Notice2.setVisibility(View.VISIBLE);
						TXV_Title.setText("Copying Folder...");
						TXV_Notice1.setText(getName() + "\n"+ "to"+ mDscDirectory.getName());
						PRGB1.setProgress(0);
						int c = 0;
						if (SourceFiles!=null){
							c=SourceFiles.size();
						}
						if (SourceFolders!=null){
							c=c+SourceFolders.size();
						}
						PRGB1.setMax(c);
						PRGB2.setMax(100);
						PRGB2.setProgress(0);
					}
				});
			}

			@Override
			protected void onProgressUpdate(final Integer... values) {
				super.onProgressUpdate(values);
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						PRGB1.setProgress(values[0]);

					}
				});

			}

			@Override
			protected void onCancelled() {
				super.onCancelled();
				if (mOnGFileDialogResultListener != null) {
					mOnGFileDialogResultListener.OnResult(GFileDialogsResults.DontCompleted, "Was canceled by the user", getAbsoluteFile());
					mOnGFileDialogResultListener.OnResult(GFileDialogsResults.DontCompleted, "Was canceled by the user", getAbsolutePath());
					mOnGFileDialogResultListener.OnResult(GFileDialogsResults.DontCompleted, "Was canceled by the user");
					mOnGFileDialogResultListener.OnResult(GFileDialogsResults.DontCompleted, "Was canceled by the user", bj_file.this);
				}
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				dismiss();
			}

			@Override
			protected void onPostExecute(String s) {
				super.onPostExecute(s);

				if (mOnGFileDialogResultListener != null) {
					if(s.equals("")) {
						mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Completed, "The file was successfully copied", mDscFile);
						mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Completed, "The file was successfully copied", mDscFile.getAbsolutePath());
						mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Completed, "The file was successfully copied");
						mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Completed, "The file was successfully copied", new bj_file(mDscFile.getAbsolutePath(),mContext));
					}else {
						mOnGFileDialogResultListener.OnResult(GFileDialogsResults.GiveError, s, getAbsoluteFile());
						mOnGFileDialogResultListener.OnResult(GFileDialogsResults.GiveError, s, getAbsolutePath());
						mOnGFileDialogResultListener.OnResult(GFileDialogsResults.GiveError, s);
						mOnGFileDialogResultListener.OnResult(GFileDialogsResults.GiveError, s, bj_file.this);
					}

				}
				dismiss();
			}

			@Override
			protected String doInBackground(Void... params) {

				mDscFile.mkdirs();
				if (SourceFolders!=null){
					for (File f : SourceFolders){
						File dDscFile=new File(f.getAbsolutePath().replace(getAbsolutePath(),mDscFile.getAbsolutePath()));
						dDscFile.mkdirs();
						publishProgress(PRGB1.getProgress()+1);
					}
				}
				if (SourceFiles!=null){
					for (final File file:SourceFiles){
						long TotlaByte = 0;
						long CopyByte = 0;
						TotlaByte = file.length();
						File fDscFile=new File(file.getAbsolutePath().replace(getAbsolutePath(),mDscFile.getAbsolutePath()));
						Log.d("bj_file",getAbsolutePath() + "\n" + mDscFile.getAbsolutePath() + "\n"+ fDscFile.getAbsolutePath());
						fDscFile.getParentFile().mkdirs();
						mHandler.post(new Runnable() {
							@Override
							public void run() {
								TXV_Notice2.setText(file.getParentFile().getName() + "\n" + file.getName());
								PRGB2.setProgress(0);
							}
						});

						byte[] buf = new byte[1024];
						int len;

						// Copy File ************************************************
						if (!file.getAbsolutePath().equals(fDscFile.getAbsolutePath())){
							try {

								try {
									in = new FileInputStream(file);
								} catch (FileNotFoundException e) {
									e.printStackTrace();
								}

								try {

									try {
										out = new FileOutputStream(fDscFile);
									} catch (FileNotFoundException e) {
										e.printStackTrace();
									}

									try {
										while ((len = in.read(buf)) > 0) {



											out.write(buf, 0, len);
											CopyByte = CopyByte + len;
											final float pers;
											pers=(CopyByte*100)/TotlaByte;
											//Set Progress Bar
											mHandler.post(new Runnable() {
												@Override
												public void run() {
													TXV_Notice2.setText(file.getParentFile().getName() + "\n" + file.getName() + " " + Math.round(pers) + " %");
													PRGB2.setProgress(Math.round(pers));
												}
											});



										}
									} catch (Exception e) {
										e.printStackTrace();
									}
								} finally {
									try {
										out.close();
									} catch (IOException e) {
										e.printStackTrace();
									}
								}
							} finally {
								try {
									in.close();
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}


						publishProgress(PRGB1.getProgress()+1);
					}
				}


				if(mDscFile.exists()) {
					return "";
				}else {
					return "The Folder could not be copied";
				}
			}
		};

		public AsyncTask<Void,Integer,String> AsyncTaskForFileMove=new AsyncTask<Void, Integer, String>() {
			InputStream in = null;
			OutputStream out = null;
			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						PRGB2.setVisibility(View.GONE);
						TXV_Notice2.setVisibility(View.GONE);
						TXV_Title.setText("Copying File...");
						TXV_Notice1.setText(getName() + "\n"+ "to"+ mdestPath);
						PRGB1.setProgress(0);
						PRGB1.setMax(100);
					}
				});
			}

			@Override
			protected void onProgressUpdate(final Integer... values) {
				super.onProgressUpdate(values);
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						PRGB1.setProgress(values[0]);
						TXV_Title.setText("Copying File..." + values[0] + " %");
					}
				});

			}

			@Override
			protected void onCancelled() {
				super.onCancelled();
				if (mOnGFileDialogResultListener != null) {
					mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Cancelled, "Was canceled by the user", getAbsoluteFile());
					mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Cancelled, "Was canceled by the user", getAbsolutePath());
					mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Cancelled, "Was canceled by the user");
					mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Cancelled, "Was canceled by the user", bj_file.this);
				}
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				dismiss();
			}

			@Override
			protected void onPostExecute(String s) {
				super.onPostExecute(s);

				if (mOnGFileDialogResultListener != null) {
					if(s.equals("")) {
						if (!mDscFile.getAbsolutePath().equals(getAbsolutePath())) {
							if (delete()) {
								mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Completed, "The file was successfully Moved", mDscFile);
								mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Completed, "The file was successfully Moved", mDscFile.getAbsolutePath());
								mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Completed, "The file was successfully Moved");
								mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Completed, "The file was successfully Moved", new bj_file(mDscFile.getAbsolutePath(), mContext));
							} else {
								mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Completed, "The file was successfully copied ,BUT DONT REMOVE", mDscFile);
								mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Completed, "The file was successfully copied, BUT DONT REMOVE", mDscFile.getAbsolutePath());
								mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Completed, "The file was successfully copied, BUT DONT REMOVE");
								mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Completed, "The file was successfully copied, BUT DONT REMOVE", new bj_file(mDscFile.getAbsolutePath(), mContext));

							}
						}else {
							mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Completed, "The file was successfully Moved", mDscFile);
							mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Completed, "The file was successfully Moved", mDscFile.getAbsolutePath());
							mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Completed, "The file was successfully Moved");
							mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Completed, "The file was successfully Moved", new bj_file(mDscFile.getAbsolutePath(), mContext));

						}

					}else {
						mOnGFileDialogResultListener.OnResult(GFileDialogsResults.GiveError, s, getAbsoluteFile());
						mOnGFileDialogResultListener.OnResult(GFileDialogsResults.GiveError, s, getAbsolutePath());
						mOnGFileDialogResultListener.OnResult(GFileDialogsResults.GiveError, s);
						mOnGFileDialogResultListener.OnResult(GFileDialogsResults.GiveError, s, bj_file.this);
					}

				}
				dismiss();
			}

			@Override
			protected String doInBackground(Void... params) {

				mDscFile.getParentFile().mkdirs();
				long TotlaByte = 0;
				long CopyByte = 0;
				TotlaByte = bj_file.this.length();




				byte[] buf = new byte[1024];
				int len;

				// Copy File ************************************************
				if (!getAbsolutePath().equals(mDscFile.getAbsolutePath())){
					try {

						try {
							in = new FileInputStream(getAbsoluteFile());
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						}

						try {

							try {
								out = new FileOutputStream(mDscFile);
							} catch (FileNotFoundException e) {
								e.printStackTrace();
							}

							try {
								while ((len = in.read(buf)) > 0) {



									out.write(buf, 0, len);
									CopyByte = CopyByte + len;
									float pers;
									pers=(CopyByte*100)/TotlaByte;
									//Set Progress Bar
									publishProgress(Math.round(pers));

								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						} finally {
							try {
								out.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					} finally {
						try {
							in.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}

				if(mDscFile.exists()) {
					if (mDscFile.length() == bj_file.this.length()) {
						return "";
					} else {
						return "File is incompletely Moved";
					}
				}else {
					return "For some reason, the file could not be Moved";
				}
			}
		};
		public AsyncTask<Void,Integer,String> AsyncTaskForFolderMove=new AsyncTask<Void, Integer, String>() {
			InputStream in = null;
			OutputStream out = null;
			ArrayList<File> SourceFiles;
			ArrayList<File> SourceFolders=new ArrayList<File>();

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				SourceFiles=AllFiles();
				SourceFolders=AllFolders();
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						PRGB2.setVisibility(View.VISIBLE);
						TXV_Notice2.setVisibility(View.VISIBLE);
						TXV_Title.setText("Copying Folder...");
						TXV_Notice1.setText(getName() + "\n"+ "to"+ mDscDirectory.getName());
						PRGB1.setProgress(0);
						int c = 0;
						if (SourceFiles!=null){
							c=SourceFiles.size();
						}
						if (SourceFolders!=null){
							c=c+SourceFolders.size();
						}
						PRGB1.setMax(c);
						PRGB2.setMax(100);
						PRGB2.setProgress(0);
					}
				});
			}

			@Override
			protected void onProgressUpdate(final Integer... values) {
				super.onProgressUpdate(values);
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						PRGB1.setProgress(values[0]);

					}
				});

			}

			@Override
			protected void onCancelled() {
				super.onCancelled();
				if (mOnGFileDialogResultListener != null) {
					mOnGFileDialogResultListener.OnResult(GFileDialogsResults.DontCompleted, "Was canceled by the user", getAbsoluteFile());
					mOnGFileDialogResultListener.OnResult(GFileDialogsResults.DontCompleted, "Was canceled by the user", getAbsolutePath());
					mOnGFileDialogResultListener.OnResult(GFileDialogsResults.DontCompleted, "Was canceled by the user");
					mOnGFileDialogResultListener.OnResult(GFileDialogsResults.DontCompleted, "Was canceled by the user", bj_file.this);
				}
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				dismiss();
			}

			@Override
			protected void onPostExecute(String s) {
				super.onPostExecute(s);

				if (mOnGFileDialogResultListener != null) {
					if(s.equals("")) {
						if (!mDscFile.getAbsolutePath().equals(getAbsolutePath())) {
							SourceFolders=AllFolders();
							if (SourceFolders!=null){
								if (SourceFolders.size()==0) {
									SourceFolders=null;
								}else {
									SourceFolders.sort(new Comparator<File>() {
										@Override
										public int compare(File o1, File o2) {
											if (o1.getAbsolutePath().length()>o2.getAbsolutePath().length()) {
												return 0;
											}else {
												return 1;
											}
										}
									});
								}
							}
							while (SourceFolders!=null ){
								for (File file:SourceFolders){
									file.delete();
								}
								SourceFolders=AllFolders();
								if (SourceFolders!=null){
									if (SourceFolders.size()==0) {
										SourceFolders=null;
									}else {
										SourceFolders.sort(new Comparator<File>() {
											@Override
											public int compare(File o1, File o2) {
												if (o1.getAbsolutePath().length()>o2.getAbsolutePath().length()) {
													return 0;
												}else {
													return 1;
												}
											}
										});
									}
								}
							}



							if (delete()) {
								mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Completed, "The Folder was successfully Moved", mDscFile);
								mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Completed, "The Folder was successfully Moved", mDscFile.getAbsolutePath());
								mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Completed, "The Folder was successfully Moved");
								mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Completed, "The Folder was successfully Moved", new bj_file(mDscFile.getAbsolutePath(), mContext));
							} else {
								mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Completed, "The Folder was successfully copied ,BUT DONT REMOVE", mDscFile);
								mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Completed, "The Folder was successfully copied ,BUT DONT REMOVE", mDscFile.getAbsolutePath());
								mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Completed, "The Folder was successfully copied ,BUT DONT REMOVE");
								mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Completed, "The Folder was successfully copied ,BUT DONT REMOVE", new bj_file(mDscFile.getAbsolutePath(), mContext));

							}
						}else {
							mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Completed, "The Folder was successfully Moved", mDscFile);
							mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Completed, "The Folder was successfully Moved", mDscFile.getAbsolutePath());
							mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Completed, "The Folder was successfully Moved");
							mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Completed, "The Folder was successfully Moved", new bj_file(mDscFile.getAbsolutePath(), mContext));

						}

					}else {
						mOnGFileDialogResultListener.OnResult(GFileDialogsResults.GiveError, s, getAbsoluteFile());
						mOnGFileDialogResultListener.OnResult(GFileDialogsResults.GiveError, s, getAbsolutePath());
						mOnGFileDialogResultListener.OnResult(GFileDialogsResults.GiveError, s);
						mOnGFileDialogResultListener.OnResult(GFileDialogsResults.GiveError, s, bj_file.this);
					}

				}
				dismiss();
			}

			@Override
			protected String doInBackground(Void... params) {
				if (!mDscFile.getAbsolutePath().equals(getAbsolutePath())){
					mDscFile.mkdirs();
					if (SourceFolders!=null){
						for (File f : SourceFolders){
							File dDscFile=new File(f.getAbsolutePath().replace(getAbsolutePath(),mDscFile.getAbsolutePath()));
							Log.d("bj_file",dDscFile.getAbsolutePath());
							dDscFile.mkdir();
							publishProgress(PRGB1.getProgress()+1);
						}
					}
					if (SourceFiles!=null){
						for (final File file:SourceFiles){
							long TotlaByte = 0;
							long CopyByte = 0;
							TotlaByte = file.length();
							File fDscFile=new File(file.getAbsolutePath().replace(getAbsolutePath(),mDscFile.getAbsolutePath()));
							Log.d("bj_file",getAbsolutePath() + "\n" + mDscFile.getAbsolutePath() + "\n"+ fDscFile.getAbsolutePath());

							fDscFile.getParentFile().mkdirs();
							mHandler.post(new Runnable() {
								@Override
								public void run() {
									TXV_Notice2.setText(file.getParentFile().getName() + "\n" + file.getName());
									PRGB2.setProgress(0);
								}
							});

							byte[] buf = new byte[1024];
							int len;

							// Copy File ************************************************
							if (!file.getAbsolutePath().equals(fDscFile.getAbsolutePath())){
								try {

									try {
										in = new FileInputStream(file);
									} catch (FileNotFoundException e) {
										e.printStackTrace();
									}

									try {

										try {
											out = new FileOutputStream(fDscFile);
										} catch (FileNotFoundException e) {
											e.printStackTrace();
										}

										try {
											while ((len = in.read(buf)) > 0) {



												out.write(buf, 0, len);
												CopyByte = CopyByte + len;
												final float pers;
												pers=(CopyByte*100)/TotlaByte;
												//Set Progress Bar
												mHandler.post(new Runnable() {
													@Override
													public void run() {
														TXV_Notice2.setText(file.getParentFile().getName() + "\n" + file.getName() + " " + Math.round(pers) + " %");
														PRGB2.setProgress(Math.round(pers));
													}
												});



											}
										} catch (Exception e) {
											e.printStackTrace();
										}
									} finally {
										try {
											out.close();
										} catch (IOException e) {
											e.printStackTrace();
										}
									}
								} finally {
									try {
										in.close();
									} catch (IOException e) {
										e.printStackTrace();
									}
								}

							}

							if (fDscFile.exists() & fDscFile.length()==file.length()){
								file.delete();
							}
							publishProgress(PRGB1.getProgress()+1);
						}
					}
				}



				if(mDscFile.exists()) {
					return "";
				}else {
					return "The Folder could not be Moved";
				}
			}
		};

		public AsyncTask<Void,Integer,String> AsyncTaskForFolderDellete=new AsyncTask<Void, Integer, String>() {

			ArrayList<File> SourceFiles;
			ArrayList<File> SourceFolders=new ArrayList<File>();

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				SourceFiles=AllFiles();
				SourceFolders=AllFolders();
				SourceFolders.sort(new Comparator<File>() {
					@Override
					public int compare(File o1, File o2) {
						if (o1.getAbsolutePath().length()>o2.getAbsolutePath().length()) {
							return 0;
						}else {
							return 1;
						}
					}
				});
				SourceFiles.sort(new Comparator<File>() {
					@Override
					public int compare(File o1, File o2) {
						if (o1.getAbsolutePath().length()>o2.getAbsolutePath().length()) {
							return 0;
						}else {
							return 1;
						}
					}
				});
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						PRGB2.setVisibility(View.GONE);
						TXV_Notice2.setVisibility(View.GONE);
						TXV_Title.setText("Delleting Folder...");
						TXV_Notice1.setText("Delleting the "+ getName());
						PRGB1.setProgress(0);
						int c = 0;
						if (SourceFiles!=null){
							c=SourceFiles.size();
						}
						if (SourceFolders!=null){
							c=c+SourceFolders.size();
						}
						PRGB1.setMax(c);
						PRGB2.setMax(100);
						PRGB2.setProgress(0);
					}
				});
			}

			@Override
			protected void onProgressUpdate(final Integer... values){
				super.onProgressUpdate(values);
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						try{
							PRGB1.setProgress(values[0]);
						}catch (Exception e){

						}

					}
				});

			}

			@Override
			protected void onCancelled() {
				super.onCancelled();
				if (mOnGFileDialogResultListener != null) {
					mOnGFileDialogResultListener.OnResult(GFileDialogsResults.DontCompleted, "Was canceled by the user", getAbsoluteFile());
					mOnGFileDialogResultListener.OnResult(GFileDialogsResults.DontCompleted, "Was canceled by the user", getAbsolutePath());
					mOnGFileDialogResultListener.OnResult(GFileDialogsResults.DontCompleted, "Was canceled by the user");
					mOnGFileDialogResultListener.OnResult(GFileDialogsResults.DontCompleted, "Was canceled by the user", bj_file.this);
				}

				dismiss();
			}

			@Override
			protected void onPostExecute(String s) {
				super.onPostExecute(s);

				if (mOnGFileDialogResultListener != null) {
					if(s.equals("")) {

						mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Completed, "The Folder was successfully Delleted");


					}else {

						mOnGFileDialogResultListener.OnResult(GFileDialogsResults.GiveError, s);

					}

				}
				dismiss();
			}

			@Override
			protected String doInBackground(Void... params) {
				int m=SourceFiles.size()+SourceFolders.size();
				while (SourceFiles!=null ){
					publishProgress(m-(SourceFiles.size()+SourceFolders.size()));
					for (final File file:SourceFiles){
						file.delete();
						mHandler.post(new Runnable() {
							@Override
							public void run() {
								TXV_Notice1.setText("Delleting the "+ file.getName());
							}
						});
						publishProgress(PRGB1.getProgress()+1);
					}

					SourceFiles=AllFiles();
					if (SourceFiles!=null){
						if (SourceFiles.size()==0) {
							SourceFiles=null;
						}else {
							SourceFiles.sort(new Comparator<File>() {
								@Override
								public int compare(File o1, File o2) {
									if (o1.getAbsolutePath().length()>o2.getAbsolutePath().length()) {
										return 0;
									}else {
										return 1;
									}
								}
							});
						}
					}
				}


				while (SourceFolders!=null ){
					publishProgress(m-SourceFolders.size());
					for (final File file:SourceFolders){
						file.delete();
						mHandler.post(new Runnable() {
							@Override
							public void run() {
								TXV_Notice1.setText("Delleting the "+ file.getName());
							}
						});
						publishProgress(PRGB1.getProgress()+1);
					}

					SourceFolders=AllFolders();
					if (SourceFolders!=null){
						if (SourceFolders.size()==0) {
							SourceFolders=null;
						}else {
							SourceFolders.sort(new Comparator<File>() {
								@Override
								public int compare(File o1, File o2) {
									if (o1.getAbsolutePath().length()>o2.getAbsolutePath().length()) {
										return 0;
									}else {
										return 1;
									}
								}
							});
						}
					}
				}
				delete();
				if(!exists()) {
					return "";
				}else {
					return "The Folder could not be Dellete";
				}
			}
		};
		@Override
		public void onBackPressed() {
			if (mDialogProcesKind==GFileDialogProcesKind.Dellete) {
				async1.cancel(true);
				if (mOnGFileDialogResultListener != null) {
					mOnGFileDialogResultListener.OnResult(GFileDialogsResults.DontCompleted, "Was canceled by the user", getAbsoluteFile());
					mOnGFileDialogResultListener.OnResult(GFileDialogsResults.DontCompleted, "Was canceled by the user", getAbsolutePath());
					mOnGFileDialogResultListener.OnResult(GFileDialogsResults.DontCompleted, "Was canceled by the user");
				}

				dismiss();
			}else {
				MessageBox(R.string.Title_Attention,R.string.message_Cancel, BJMessagesButtonKind.Yes_No,R.drawable.icon_attention,mContex,new messageBox.OnDialogResultListener(){
					@Override
					public boolean OnResult(Boolean dialogResult) {

						if (dialogResult) {
							async1.cancel(true);
							if (mOnGFileDialogResultListener != null) {
								mOnGFileDialogResultListener.OnResult(GFileDialogsResults.DontCompleted, "Was canceled by the user", getAbsoluteFile());
								mOnGFileDialogResultListener.OnResult(GFileDialogsResults.DontCompleted, "Was canceled by the user", getAbsolutePath());
								mOnGFileDialogResultListener.OnResult(GFileDialogsResults.DontCompleted, "Was canceled by the user");
							}

							dismiss();

						}

						return super.OnResult(dialogResult);
					}
				});
			}


		}
		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			getWindow().requestFeature(Window.FEATURE_NO_TITLE);
			GFileDialogsProcesView MyView = null;
			switch (mDialogProcesKind){
				case GFileDialogProcesKind.Copy:
					MyView=new GFileDialogsProcesView(mContex,"Copying...");
					break;
				case GFileDialogProcesKind.Move:
					MyView=new GFileDialogsProcesView(mContex,"Moving...");
					break;
				case GFileDialogProcesKind.Dellete:
					MyView=new GFileDialogsProcesView(mContex,"Delleting...");
					break;

			}

			setContentView(MyView.mView);
			BTNCancel=MyView.GF_BTN_Cancel;

			PRGB1=MyView.GF_PRGB_1;
			PRGB2=MyView.GF_PRGB_2;

			TXV_Notice1=MyView.GF_TXV_Notice1;
			TXV_Notice2=MyView.GF_TXV_Notice2;

			TXV_Title=MyView.GF_TXV_Title;
			mHandler = new Handler();
			PRGB1.setProgress(0);
			PRGB1.setSecondaryProgress(0);
			PRGB2.setProgress(0);
			PRGB2.setSecondaryProgress(0);

			setCanceledOnTouchOutside(false);

		}

		@Override
		public void onAttachedToWindow() {
			super.onAttachedToWindow();
			if (!exists()) {
				String thisNotice;
				if(isDirectory()) {
					thisNotice=mContex.getResources().getString(R.string.promp_Folder_DontExist);
				} else {
					thisNotice=mContex.getResources().getString(R.string.promp_File_DonExist);

				}
				if (mOnGFileDialogResultListener != null) {
					mOnGFileDialogResultListener.OnResult(GFileDialogsResults.GiveError, thisNotice, getAbsoluteFile());
					mOnGFileDialogResultListener.OnResult(GFileDialogsResults.GiveError, thisNotice, getAbsolutePath());
					mOnGFileDialogResultListener.OnResult(GFileDialogsResults.GiveError, thisNotice);
				}

				dismiss();

				return;
			}

			switch (mDialogProcesKind){
				case GFileDialogProcesKind.Copy:
					TXV_Title.setText("Copying... " );

					break;
				case GFileDialogProcesKind.Move:
					TXV_Title.setText("Moving... " );


					break;
				case GFileDialogProcesKind.Dellete:
					TXV_Title.setText("Delleting... " );
					break;

			}
			if (mDialogProcesKind==GFileDialogProcesKind.Copy | mDialogProcesKind==GFileDialogProcesKind.Move){

				if (mDscDirectory.isFile()) {
					if (mOnGFileDialogResultListener != null) {
						mOnGFileDialogResultListener.OnResult(GFileDialogsResults.GiveError, "Destination is not a directory", getAbsoluteFile());
						mOnGFileDialogResultListener.OnResult(GFileDialogsResults.GiveError, "Destination is not a directory", getAbsolutePath());
						mOnGFileDialogResultListener.OnResult(GFileDialogsResults.GiveError, "Destination is not a directory");
					}
					dismiss();
					return;

				}else if (mDscFile.exists() & (mDscFile.isDirectory()==isDirectory())){
					String thisNotice;
					if (isDirectory()) {
						thisNotice=mContex.getResources().getString(R.string.promp_Folder_Exist_Replace)+"\n"+getName();
					}else {
						thisNotice=mContex.getResources().getString(R.string.promp_File_Exist_Replace)+"\n"+getName();
					}
					MessageBox(mContex.getResources().getString( R.string.Title_Attention),thisNotice, BJMessagesButtonKind.Yes_No_Cancel,mContex,R.drawable.icon_attention,new messageBox.OnDialogResultListener(){
						@Override
						public boolean OnResult(int PressedButton) {
							switch (PressedButton){
								case BJMessagesButtonKind.PressedButton.Button_Yes:
									RunProces();
									break;
								case BJMessagesButtonKind.PressedButton.Button_No:
									int i=0;
									while (mDscFile.exists()){
										if (i==0) {
											if (isDirectory()) {
												mDscFile = new File(mdestPath + File.separator + getName() + " Copy");
											}else {

												mDscFile=new File(mdestPath + File.separator +  "Copy_" + getName());
											}
										}else {
											if (isDirectory()) {
												mDscFile = new File(mdestPath + File.separator + getName() + " Copy"+i);
											}else {

												mDscFile=new File(mdestPath + File.separator +  "Copy" +i + "_" + getName());
											}
										}
										i++;
									}

									RunProces();
									break;
								default:
									if (mOnGFileDialogResultListener!=null){
										mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Cancelled,"Was canceled by the user",getAbsoluteFile());
										mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Cancelled,"Was canceled by the user",getAbsolutePath());
										mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Cancelled,"Was canceled by the user");
									}
									dismiss();
									break;
							}
							return super.OnResult(PressedButton);
						}
					});
				}else {
					RunProces();
				}
			}else {
				RunProces();
			}
		}
		private void RunProces(){
			switch (mDialogProcesKind){
				case GFileDialogProcesKind.Copy:
					if (bj_file.this.isFile()) {
						async1 = AsyncTaskForFileCopy;
					}else {
						async1=AsyncTaskForFolderCopy;
					}
					break;
				case GFileDialogProcesKind.Move:
					if (bj_file.this.isFile()) {
						async1 = AsyncTaskForFileMove;
					}else {
						async1=AsyncTaskForFolderMove;
					}
					break;
				case GFileDialogProcesKind.Dellete:
					if (bj_file.this.isFile()) {
						if (mOnGFileDialogResultListener != null) {
							if(delete()) {

								mOnGFileDialogResultListener.OnResult(GFileDialogsResults.Completed, "The Folder was successfully Delleted");


							}else {

								mOnGFileDialogResultListener.OnResult(GFileDialogsResults.GiveError, "Cant dellete the folder");

							}

						}
						dismiss();
						return;

					}else {
						async1=AsyncTaskForFolderDellete;
					}
					break;

			}


			BTNCancel.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mDialogProcesKind==GFileDialogProcesKind.Dellete) {
						async1.cancel(true);


						dismiss();
					}else {
						MessageBox(R.string.Title_Attention,R.string.message_Cancel, BJMessagesButtonKind.Yes_No,R.drawable.icon_attention,mContex,new messageBox.OnDialogResultListener(){
							@Override
							public boolean OnResult(Boolean dialogResult) {

								if (dialogResult) {
									async1.cancel(true);


									dismiss();

								}
								return super.OnResult(dialogResult);
							}
						});
					}

				}
			});
			async1.execute();

		}

		@Override
		protected void onStop() {
			super.onStop();
			try{
				async1.cancel(true);
			}catch (Exception e){

			}

		}
	}

	// Base 2 GFileDialogsChoicer
	public static class GFileDialogsChoicer extends Dialog {
		Context mContext;
		FilenameFilter mFilter;
		PopupMenu popup;
		Boolean mAsDirectorySelector,mMultiChoice;
		String mTitle="Please Select.";

		ImageView IMG_Back,IMG_Up,IMG_New,IMG_Paste,IMG_Select;
		TextView TXVPath,TXVTitle;
		ListView LV_FolderList;

		String Paste_SelectedFolderPath=null,Paste_SelectedFolderName=null;
		byte Paste_Function=0;


		bj_file root,CurrentFolder;

		//private ArrayList<GFolderNotice>FoldersList=new ArrayList<GFolderNotice>();
		//private ArrayList<GFileNotice>FilesList=new ArrayList<GFileNotice>();
		private ArrayList<bj_file> bj_filesList =new ArrayList<bj_file>();

		private List<StorageInfo> MyStorageList;
		public GFileDialogsChoicer(@NonNull Context context, @Nullable FilenameFilter MyFilter,Boolean MultiChoice,OnGFileDialogResultListener onGFileDialogResultListener ) {
			super(context);
			mContext=context;
			mFilter=MyFilter;
			mAsDirectorySelector=false;
			mOnGFileDialogResultListener=onGFileDialogResultListener;
			mMultiChoice=MultiChoice;

		}

		public GFileDialogsChoicer(@NonNull Context context, Boolean AsDirectorySelector,Boolean MultiChoice,OnGFileDialogResultListener onGFileDialogResultListener ) {
			super(context);
			mContext=context;
			mAsDirectorySelector=AsDirectorySelector;
			mOnGFileDialogResultListener=onGFileDialogResultListener;
			mMultiChoice=MultiChoice;
			mFilter=null;

		}

		@Override
		protected void onCreate(Bundle savedInstanceState) {

			super.onCreate(savedInstanceState);
			getWindow().requestFeature(Window.FEATURE_NO_TITLE);
			GDirectoryChoicerDialogView MyView;
			MyView=new GDirectoryChoicerDialogView(mContext,"Select a directory");
			setContentView(MyView.mView);

			IMG_Back=MyView.GDCD_IMG_Back;
			IMG_Up=MyView.GDCD_IMG_Up;
			IMG_New=MyView.GDCD_IMG_New;//(ImageView)findViewById(R.id.GDCD_IMG_New);
			IMG_Paste=MyView.GDCD_IMG_Paste;//(ImageView)findViewById(R.id.GDCD_IMG_Paste);
			IMG_Select=MyView.GDCD_IMG_Select;//(ImageView)findViewById(R.id.GDCD_IMG_Select);
			TXVPath=MyView.GDCD_TXV_Path;//(TextView)findViewById(R.id.GDCD_TXV_Path);
			TXVTitle=MyView.GDCD_TXV_Title;//(TextView)findViewById(R.id.GDCD_TXV_Title);
			LV_FolderList=MyView.GDCD_LV_FolderList;//(ListView)findViewById(R.id.GDCD_LV_FolderList);

			if (mAsDirectorySelector) {
				IMG_Select.setVisibility(View.VISIBLE);
				mTitle = "Select a Directory";
			}else {
				IMG_Select.setVisibility(View.GONE);
				mTitle = "Click on a File";
			}


			TXVTitle.setText(mTitle);

			MyStorageList=getStorageList();

			root=null;

			CurrentFolder= null;
			//FilesList=FilesListProcess(CurrentFolder);
			//FoldersList=FolderListProcess(CurrentFolder);
			//FilesList=FilesListProcess(CurrentFolder)

			LoadToListView();
			IMG_Back.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mOnGFileDialogResultListener!=null){


						mOnGFileDialogResultListener.OnSelect(false,null);
					}
					dismiss();
				}
			});
			IMG_Up.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					ProcesUp();
				}
			});
			IMG_Paste.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					ProcesPaste();
				}
			});
			IMG_New.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					ProcesNew();
				}
			});
			IMG_Select.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mOnGFileDialogResultListener!=null){
						mOnGFileDialogResultListener.OnSelect(true,new file_object(TXVPath.getText().toString(),mContext));

					}
					dismiss();
				}
			});

		}

		@Override
		public void onAttachedToWindow() {
			super.onAttachedToWindow();
			//BJPermissions.GetPermissions(mContext,Manifest.permission.MEDIA_CONTENT_CONTROL,"Media",false);

			bj_getPermissionREAD_EXTERNAL_STORAGE(mContext,null);

		}



		@Override
		public void onBackPressed() {
			if (CurrentFolder==null) {
				super.onBackPressed();
			}else{
				ProcesUp();
			}
		}


		private void ProcesNew(){
			InputBox(R.string.title_new_folder,R.string.message_new_folder,R.string.hint_folder_name,"New Folder", InputType.TYPE_CLASS_TEXT,mContext,new inputBox.OnDialogResultListener(){
				@Override
				public boolean OnResult(Boolean dialogResult, String ValueResult) {
					java.io.File file = null;
					if(dialogResult){
						if(!ValueResult.equals("")){
							file=new java.io.File(TXVPath.getText().toString() + java.io.File.separator + ValueResult);
							if(!file.exists()) {
								file.mkdirs();
							}else {
								MessageBox(R.string.Title_Attention,R.string.promp_Folder_Exist, BJMessagesButtonKind.Ok,R.drawable.icon_attention,mContext,new messageBox.OnDialogResultListener(){
									@Override
									public boolean OnResult(Boolean dialogResult) {
										return super.OnResult(dialogResult);
									}
								});
							}
							//FoldersList = FolderListProcess(CurrentFolder);
							//FilesList=FilesListProcess(CurrentFolder)
							//LoadFoldersToListView();
							//bj_filesList=GFileListProcess(CurrentFolder);
							LoadToListView();
						}
					}
					if( file.exists()) {
						GFilePositionSet(ValueResult);
						return true;
					}else {
						return false;
					}
				}

			});
		}
		private void ProcesDellete(String Path, final int position){
			file_object GF=new file_object(Path,mContext);
			GF.GFDellete(new bj_file_classes.OnGFileDialogResultListener(){
				@Override
				public void OnResult(int MyResult, String ResultDescriptiopn) {
					if (MyResult!= GFileDialogsResults.Cancelled){
						int p = position;
						//bj_filesList=GFileListProcess(CurrentFolder);
						LoadToListView();
						if (bj_filesList.size() > 0 & p > 0) {
							p = p - 1;
						} else {
							p = 0;
						}
						LV_FolderList.setSelection(p);
					}
					Toast.makeText(mContext, ResultDescriptiopn, Toast.LENGTH_LONG).show();
				}

				@Override
				public void OnResult(int MyResult, String ResultDescriptiopn, String DestinationFilePath) {

				}

				@Override
				public void OnResult(int MyResult, String ResultDescriptiopn, File DestinationFile) {

				}

				@Override
				public void OnResult(int MyResult, String ResultDescriptiopn, file_object DestinationFile) {

				}

				@Override
				public void OnSelect(Boolean IsSelected, @Nullable file_object SelectedFile) {

				}
			});


		}
		private void ProcesRename(String Path){
			file_object GF=new file_object(Path,mContext);
			GF.GFRename(new bj_file_classes.OnGFileDialogResultListener() {
				@Override
				public void OnResult(int MyResult, String ResultDescriptiopn) {

				}

				@Override
				public void OnResult(int MyResult, String ResultDescriptiopn, String DestinationFilePath) {

				}

				@Override
				public void OnResult(int MyResult, String ResultDescriptiopn, File DestinationFile) {
					if (MyResult!= GFileDialogsResults.Cancelled) {

						//bj_filesList=GFileListProcess(CurrentFolder);
						LoadToListView();
						GFilePositionSet(DestinationFile.getName());

					}
					Toast.makeText(mContext, ResultDescriptiopn, Toast.LENGTH_LONG).show();
				}
				@Override
				public void OnResult(int MyResult, String ResultDescriptiopn, file_object DestinationFile) {

				}
				@Override
				public void OnSelect(Boolean IsSelected, @Nullable file_object SelectedFile) {

				}
			});

		}
		private void ProcesCopy(){

			file_object GF=new file_object(Paste_SelectedFolderPath,mContext);
			GF.GFCopy(TXVPath.getText().toString(), new bj_file_classes.OnGFileDialogResultListener() {
				@Override
				public void OnResult(int MyResult, String ResultDescriptiopn) {

				}

				@Override
				public void OnResult(int MyResult, String ResultDescriptiopn, String DestinationFilePath) {

				}

				@Override
				public void OnResult(int MyResult, String ResultDescriptiopn, File DestinationFile) {
					LoadToListView();
					if (MyResult!= GFileDialogsResults.Cancelled & MyResult!= GFileDialogsResults.GiveError) {


						GFilePositionSet(DestinationFile.getName());
						Paste_Function=0;
						Paste_SelectedFolderName=null;
						Paste_SelectedFolderPath=null;
						IMG_Paste.setImageResource(R.drawable.icon_folder_paste_disable);
						IMG_Paste.setEnabled(false);
					}



					Toast.makeText(mContext, ResultDescriptiopn, Toast.LENGTH_LONG).show();
				}
				@Override
				public void OnResult(int MyResult, String ResultDescriptiopn, file_object DestinationFile) {

				}
				@Override
				public void OnSelect(Boolean IsSelected, @Nullable file_object SelectedFile) {

				}
			});


		}
		private void ProcesMove(){
			file_object GF=new file_object(Paste_SelectedFolderPath,mContext);
			GF.GFMove(TXVPath.getText().toString(), new bj_file_classes.OnGFileDialogResultListener() {
				@Override
				public void OnResult(int MyResult, String ResultDescriptiopn) {

				}

				@Override
				public void OnResult(int MyResult, String ResultDescriptiopn, String DestinationFilePath) {

				}

				@Override
				public void OnResult(int MyResult, String ResultDescriptiopn, File DestinationFile) {
					if (MyResult!= GFileDialogsResults.Cancelled) {

						//bj_filesList=GFileListProcess(CurrentFolder);
						LoadToListView();
						//Toast.makeText(mContext, NewFile.getName(), Toast.LENGTH_LONG).show();
						GFilePositionSet(DestinationFile.getName());
						Paste_Function=0;
						Paste_SelectedFolderName=null;
						Paste_SelectedFolderPath=null;
						IMG_Paste.setImageResource(R.drawable.icon_folder_paste_disable);
						IMG_Paste.setEnabled(false);
					}
					Toast.makeText(mContext, ResultDescriptiopn, Toast.LENGTH_LONG).show();
				}
				@Override
				public void OnResult(int MyResult, String ResultDescriptiopn, file_object DestinationFile) {

				}
				@Override
				public void OnSelect(Boolean IsSelected, @Nullable file_object SelectedFile) {

				}
			});
		}
		private void ProcesPaste(){
			if (Paste_SelectedFolderPath!=null){
				if (Paste_Function== PasteFunctions.Paste_Copy) {
					ProcesCopy();
				}else if(Paste_Function== PasteFunctions.Paste_Move){
					ProcesMove();
				}
			}

		}
		private void ProcesUp(){
			//Log.d("bj modules",CurrentFolder.getAbsolutePath() + " == "+ root.getAbsolutePath());
			String FN;
			FN=CurrentFolder.getName();
			if(CurrentFolder.getAbsolutePath().equals(root.getAbsolutePath())) {
				root = null;
				CurrentFolder = null;
				LoadToListView();

			}else {
				CurrentFolder=new bj_file(CurrentFolder.getParentFile().getAbsolutePath(),mContext);
				//FilesList=FilesListProcess(CurrentFolder);
				LoadToListView();

				GFilePositionSet(FN);
			}
			//bj_filesList=GFileListProcess(CurrentFolder);

		}




		public void LoadToListView()   {
			if (CurrentFolder==null) {
				TXVPath.setText("Select a Storage device");
				TXVPath.setTextColor(mContext.getResources().getColor(R.color.colorPrimaryDark));
				TXVPath.setBackgroundColor(Color.TRANSPARENT);
			}else {
				TXVPath.setText(CurrentFolder.getAbsolutePath());
				TXVPath.setTextColor(Color.WHITE);
				TXVPath.setBackgroundColor(mContext.getResources().getColor(R.color.colorAccent));
			}

			if(Paste_SelectedFolderPath==null) {
				IMG_Paste.setImageResource(R.drawable.icon_folder_paste_disable);
				IMG_Paste.setEnabled(false);
			}else {
				if(Paste_Function== bj_file_classes.PasteFunctions.Paste_Copy) {
					IMG_Paste.setImageResource(R.drawable.icon_folder_paste);
				}else {
					IMG_Paste.setImageResource(R.drawable.icon_folder_paste_cut);
				}

				IMG_Paste.setEnabled(true);
			}
			//Toast.makeText(mContext,CurrentFolder.getAbsolutePath() + " == " + root.getAbsolutePath(), Toast.LENGTH_LONG).show();
			if (CurrentFolder==null) {
				IMG_Up.setEnabled(false);
				IMG_Up.setImageResource(R.drawable.icon_folder_parent_disable);
				IMG_Paste.setEnabled(false);
				IMG_Paste.setImageResource(R.drawable.icon_folder_paste_disable);
				IMG_New.setEnabled(false);
				IMG_New.setImageResource(R.drawable.icon_folder_add_disable);
				IMG_Select.setEnabled(false);
				IMG_Select.setImageResource(R.drawable.icon_select_disable);
				//Toast.makeText(mContext, "disable", Toast.LENGTH_LONG).show();
			}else {
				IMG_Up.setEnabled(true);
				IMG_Up.setImageResource(R.drawable.icon_folder_parent);
				if(Paste_SelectedFolderPath!=null ) {
					IMG_Paste.setEnabled(true);
					if(Paste_Function== PasteFunctions.Paste_Copy) {
						IMG_Paste.setImageResource(R.drawable.icon_folder_paste);
					}else {
						IMG_Paste.setImageResource(R.drawable.icon_folder_paste_cut);
					}
				}else {
					IMG_Paste.setEnabled(false);
					IMG_Paste.setImageResource(R.drawable.icon_folder_paste_disable);
				}

				IMG_New.setEnabled(true);
				IMG_New.setImageResource(R.drawable.icon_folder_add);
				IMG_Select.setEnabled(true);
				IMG_Select.setImageResource(R.drawable.icon_select);
			}
			if (CurrentFolder!=null){
				if (mAsDirectorySelector) {
					bj_filesList = CurrentFolder.listGFiles(true,false);
				}else {
					bj_filesList =CurrentFolder.listGFiles(mFilter,false);
				}
				ArrayAdapter adapter = new ArrayAdapter(mContext, android.R.layout.simple_list_item_2, android.R.id.text1, bj_filesList) {
					@Override
					public View getView(int position, View convertView, ViewGroup parent) {
						View view = super.getView(position, convertView, parent);
						TextView text1 = (TextView) view.findViewById(android.R.id.text1);
						TextView text2 = (TextView) view.findViewById(android.R.id.text2);
						bj_file FN= bj_filesList.get(position);
						text1.setText(FN.getName());
						String notice = null;
						if (FN.isDirectory()){

							int fc,dc;
							dc=FN.CountDirectories();
							fc=FN.CountFiles(mFilter);
							if (fc==0 & dc==0) {
								notice = "Empty directory";
							}else {
								if (dc>0){
									notice = "Directories: " +dc;
								}
								if (notice != null) {
									notice = notice + "     Files: " + fc ;
								} else {
									notice = "Files: " + fc;
								}
							}


						}else {
							notice = "("+ FN.getMimeType() + ")   " +FN.GFSize();


						}
						text2.setTextColor(Color.GRAY);
						text2.setText(notice);

						return view;
					}
				};
				LV_FolderList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						//TXVPath.setText(FoldersList.get(position).FolderPath);
						bj_file f= bj_filesList.get(position);
						if( f.exists() ){
							if (f.isDirectory()) {
								CurrentFolder = f;// new bj_file(FoldersList.get(position).FolderPath,mContext);
								if (root == null) {
									root = CurrentFolder;
								}
								//FilesList=FilesListProcess(CurrentFolder);
								LoadToListView();
							}else {
								TXVPath.setText(f.getAbsolutePath());
								if (mOnGFileDialogResultListener!=null){
									mOnGFileDialogResultListener.OnSelect(true,new file_object(f.getAbsolutePath(),mContext));

								}
								dismiss();


							}

						}else{
							Toast.makeText(mContext, "There is no folder!", Toast.LENGTH_LONG).show();
							LoadToListView();
						}

					}
				});


				LV_FolderList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
					@Override
					public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
						final bj_file f= bj_filesList.get(position);
						if( f.exists()){
							if (CurrentFolder!=null){
								popup = new PopupMenu(mContext, view);
								popup.getMenu().add(0, GFileDialogProcesKind.Rename,0,"Rename");
								popup.getMenu().add(0, GFileDialogProcesKind.Copy,1,"Copy");
								popup.getMenu().add(0, GFileDialogProcesKind.Move,2,"Move");
								popup.getMenu().add(0, GFileDialogProcesKind.Dellete,3,"Dellete");


								popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
									@Override
									public boolean onMenuItemClick(MenuItem item) {


										int i = item.getItemId();
										if (i == GFileDialogProcesKind.Copy) {
											Paste_SelectedFolderPath = f.getAbsolutePath();
											Paste_SelectedFolderName = f.getName();
											Paste_Function = PasteFunctions.Paste_Copy;
											IMG_Paste.setImageResource(R.drawable.icon_folder_paste);
											IMG_Paste.setEnabled(true);

										} else if (i == GFileDialogProcesKind.Move) {
											Paste_SelectedFolderPath = f.getAbsolutePath();
											Paste_SelectedFolderName = f.getName();
											Paste_Function = PasteFunctions.Paste_Move;
											IMG_Paste.setImageResource(R.drawable.icon_folder_paste_cut);
											IMG_Paste.setEnabled(true);

										} else if (i == GFileDialogProcesKind.Dellete) {
											ProcesDellete(f.getAbsolutePath(),position);


										} else if (i == GFileDialogProcesKind.Rename) {
											ProcesRename(f.getAbsolutePath());
										}


										return true;
									}
								});
								popup.show();
							}

						}else {
							Toast.makeText(mContext, "There is no folder!", Toast.LENGTH_LONG).show();
							LoadToListView();
						}


						return true;
					}
				});
				LV_FolderList.setAdapter(adapter);
			}else {
				bj_filesList =null;
				final ArrayList<StorageInfo> mStoragsList;
				mStoragsList=getStorageList();
				ArrayAdapter adapter = new ArrayAdapter(mContext, android.R.layout.simple_list_item_2, android.R.id.text1,mStoragsList) {
					@Override
					public View getView(int position, View convertView, ViewGroup parent) {
						View view = super.getView(position, convertView, parent);
						TextView text1 = (TextView) view.findViewById(android.R.id.text1);
						TextView text2 = (TextView) view.findViewById(android.R.id.text2);
						StorageInfo FN=mStoragsList.get(position);

						String notice = null;
						if (FN.removable==false) {
							text1.setText("Device");


						}else {
							text1.setText(FN.getDisplayName());

						}
						notice="ReadOnly: " +FN.readonly + "   Removable: " + FN.removable;

						text2.setText(notice);
						text2.setTextColor(Color.GRAY);
						return view;
					}
				};
				LV_FolderList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						//TXVPath.setText(FoldersList.get(position).FolderPath);
						StorageInfo f=mStoragsList.get(position);

						CurrentFolder = new bj_file(f.path,mContext);// new bj_file(FoldersList.get(position).FolderPath,mContext);
						if (root == null) {
							root = CurrentFolder;
						}
						//FilesList=FilesListProcess(CurrentFolder);
						LoadToListView();

					}
				});
				LV_FolderList.setAdapter(adapter);
			}



		}




		private int FindGFilePosition(bj_file mbj_file){
			int i=0;
			for (bj_file fn: bj_filesList){

				if (fn.getName().toLowerCase().equals(mbj_file.getName().toLowerCase())){
					return i;
				}
				i++;
			}
			return -1;
		}
		private int FindGFilePosition(String GFileName){
			int i=0;
			for (bj_file fn: bj_filesList){

				if (fn.getName().toLowerCase().equals(GFileName.toLowerCase())){
					return i;
				}
				i++;
			}
			return -1;
		}


		private void GFilePositionSet(bj_file mbj_file){
			LV_FolderList.setSelection(FindGFilePosition(mbj_file));
		}
		private void GFilePositionSet(String GFileName){
			LV_FolderList.setSelection(FindGFilePosition(GFileName));
		}
		@Override
		public void setTitle(@Nullable CharSequence title) {
			super.setTitle(title);
			mTitle=title.toString();
			TXVTitle.setText(mTitle);

		}

		@Override
		public void setTitle(@StringRes int titleId) {
			super.setTitle(titleId);
			mTitle=mContext.getResources().getString(titleId);
			TXVTitle.setText(mTitle);
		}

		public String getTitle() {

			return mTitle;
		}

		private class GDirectoryChoicerDialogView extends View {
			public int getpixels(int dp){

				//Resources r = boardContext.getResources();
				//float px = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpis, r.getDisplayMetrics());

				final float scale = getResources().getDisplayMetrics().density;
				int px = (int) (dp * scale + 0.5f);



				return px;

			}
			Context vContext;

			LinearLayout mView,LL_Base,LL_Buttons1,LL_Buttons2,LL_Buttons3,LL_PathContect,LL_ButtonsContent;
			ListView GDCD_LV_FolderList;
			TextView GDCD_TXV_Title,GDCD_TXV_Path;
			ImageView GDCD_IMG_Back,GDCD_IMG_Up,GDCD_IMG_New,GDCD_IMG_Paste,GDCD_IMG_Select;

			public GDirectoryChoicerDialogView(Context context, String MyTitle) {
				super(context);
				vContext=context;


				mView=new LinearLayout(vContext);
				mView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
				mView.setPadding(0,0,0,0);
				mView.setOrientation(LinearLayout.VERTICAL);

				LL_Base=new LinearLayout(vContext);
				LL_Base.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
				LL_Base.setPadding(5,5,5,5);
				LL_Base.setOrientation(LinearLayout.VERTICAL);

				LL_PathContect=new LinearLayout(vContext);
				LL_PathContect.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
				LL_PathContect.setPadding(0,5,0,5);
				LL_PathContect.setOrientation(LinearLayout.VERTICAL);

				LL_ButtonsContent=new LinearLayout(vContext);
				LL_ButtonsContent.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
				LL_ButtonsContent.setPadding(0,5,0,5);
				LL_ButtonsContent.setOrientation(LinearLayout.VERTICAL);

				LL_Buttons1=new LinearLayout(vContext);
				LL_Buttons1.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getpixels(50)));
				LL_Buttons1.setBackgroundResource(R.drawable.border_corner5);
				LL_Buttons1.setGravity(Gravity.LEFT);
				LL_Buttons1.setPadding(5,10,5,10);
				LL_Buttons1.setLayoutDirection(LAYOUT_DIRECTION_LTR);


				LL_Buttons2=new LinearLayout(vContext);
				LL_Buttons2.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
				LL_Buttons2.setGravity(Gravity.RIGHT);
				LL_Buttons2.setLayoutDirection(LAYOUT_DIRECTION_RTL);

				LL_Buttons3=new LinearLayout(vContext);
				LL_Buttons3.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
				LL_Buttons3.setGravity(Gravity.CENTER);



				GDCD_TXV_Title=new TextView(vContext);
				GDCD_TXV_Title.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
				GDCD_TXV_Title.setTextAppearance(R.style.TextAppearance_AppCompat_Title);
				GDCD_TXV_Title.setBackgroundColor(vContext.getResources().getColor(R.color.colorPrimaryDark));
				GDCD_TXV_Title.setTextColor(Color.WHITE);
				GDCD_TXV_Title.setPadding(5,5,5,5);
				GDCD_TXV_Title.setText(MyTitle);
				GDCD_TXV_Title.setGravity(Gravity.CENTER_VERTICAL);


				GDCD_TXV_Path=new TextView(vContext);
				GDCD_TXV_Path.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
				GDCD_TXV_Path.setTextAppearance(R.style.TextAppearance_AppCompat_Title);
				GDCD_TXV_Path.setBackgroundColor(vContext.getResources().getColor(R.color.colorAccent));
				GDCD_TXV_Path.setTextColor(Color.WHITE);
				GDCD_TXV_Path.setPadding(5,5,5,5);
				GDCD_TXV_Path.setText(MyTitle);
				GDCD_TXV_Path.setGravity(Gravity.CENTER_VERTICAL);




				GDCD_LV_FolderList=new ListView(vContext);
				GDCD_LV_FolderList.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
				GDCD_LV_FolderList.setBackgroundResource(R.drawable.border_corner5);

				GDCD_IMG_Back=new ImageView(vContext);
				GDCD_IMG_Back.setLayoutParams(new LinearLayout.LayoutParams( getpixels(70),ViewGroup.LayoutParams.MATCH_PARENT));
				GDCD_IMG_Back.setClickable(true);
				GDCD_IMG_Back.setPadding(5,0,5,0);
				GDCD_IMG_Back.setScaleType(ImageView.ScaleType.FIT_CENTER);
				GDCD_IMG_Back.setImageResource(R.drawable.icon_cancel1);

				GDCD_IMG_Select=new ImageView(vContext);
				GDCD_IMG_Select.setLayoutParams(new LinearLayout.LayoutParams(getpixels(70),ViewGroup.LayoutParams.MATCH_PARENT));
				GDCD_IMG_Select.setClickable(true);
				GDCD_IMG_Select.setPadding(0,0,0,0);
				GDCD_IMG_Select.setScaleType(ImageView.ScaleType.FIT_CENTER);
				GDCD_IMG_Select.setImageResource(R.drawable.icon_select);

				GDCD_IMG_Up=new ImageView(vContext);
				GDCD_IMG_Up.setLayoutParams(new LinearLayout.LayoutParams(getpixels(60),ViewGroup.LayoutParams.MATCH_PARENT));
				GDCD_IMG_Up.setClickable(true);
				GDCD_IMG_Up.setPadding(0,0,0,0);
				GDCD_IMG_Up.setScaleType(ImageView.ScaleType.FIT_CENTER);
				GDCD_IMG_Up.setImageResource(R.drawable.icon_folder_parent);

				GDCD_IMG_New=new ImageView(vContext);
				GDCD_IMG_New.setLayoutParams(new LinearLayout.LayoutParams(getpixels(60),ViewGroup.LayoutParams.MATCH_PARENT));
				GDCD_IMG_New.setClickable(true);
				GDCD_IMG_New.setPadding(0,0,0,0);
				GDCD_IMG_New.setScaleType(ImageView.ScaleType.FIT_CENTER);
				GDCD_IMG_New.setImageResource(R.drawable.icon_folder_add);

				GDCD_IMG_Paste=new ImageView(vContext);
				GDCD_IMG_Paste.setLayoutParams(new LinearLayout.LayoutParams(getpixels(60),ViewGroup.LayoutParams.MATCH_PARENT));
				GDCD_IMG_Paste.setClickable(true);
				GDCD_IMG_Paste.setPadding(0,0,0,0);
				GDCD_IMG_Paste.setScaleType(ImageView.ScaleType.FIT_CENTER);
				GDCD_IMG_Paste.setImageResource(R.drawable.icon_folder_paste_disable);

				LL_Buttons3.addView(GDCD_IMG_Paste);
				LL_Buttons3.addView(GDCD_IMG_New);
				LL_Buttons3.addView(GDCD_IMG_Up);

				LL_Buttons2.addView(GDCD_IMG_Select);
				LL_Buttons2.addView(LL_Buttons3);

				LL_Buttons1.addView(GDCD_IMG_Back);
				LL_Buttons1.addView(LL_Buttons2);

				LL_ButtonsContent.addView(LL_Buttons1);
				LL_PathContect.addView(GDCD_TXV_Path);

				LL_Base.addView(LL_PathContect);
				LL_Base.addView(LL_ButtonsContent);
				LL_Base.addView(GDCD_LV_FolderList);

				mView.addView(GDCD_TXV_Title);
				mView.addView(LL_Base);
			}

		}

	}

	public static void GFileWriteToTextFile(String FilePath,String MyText,Boolean AddToEnd,Boolean InNewLine) throws IOException {
		File file=new File(FilePath);
		file.getParentFile().mkdirs();




		if (!AddToEnd) {
			//replace
			Writer writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file, false), "UTF-8"));
			writer.append(MyText);
			writer.close();
		}else {

			if (InNewLine) {
				//new line
				Writer writer = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(file, true), "UTF-8"));
				if (new File(FilePath).length()==0) {
					writer.append(MyText);
					writer.close();
				}else {
					writer.append(System.lineSeparator()+MyText);
					writer.close();
				}

			}else {
				//add to end
				Writer writer = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(file, true), "UTF-8"));

				writer.append(MyText);
				writer.close();
			}
		}
	}
	public static void bj_getPermissionREAD_EXTERNAL_STORAGE(Context context, bj_permission.OnGetPermissionListener onGetPermissionListener) {
		CheckPermision(context, Manifest.permission.READ_EXTERNAL_STORAGE, R.string.permission_show_directories, onGetPermissionListener);
	}
	public static void bj_getPermissionWRITE_EXTERNAL_STORAGE(Context context, bj_permission.OnGetPermissionListener onGetPermissionListener) {
		CheckPermision(context, Manifest.permission.WRITE_EXTERNAL_STORAGE, R.string.permission_write_directories, onGetPermissionListener);
	}
	public static void bj_getPermissionMEDIA_CONTENT_CONTROL(Context context, bj_permission.OnGetPermissionListener onGetPermissionListener) {
		CheckPermision(context, Manifest.permission.MEDIA_CONTENT_CONTROL, R.string.permission_show_content, onGetPermissionListener);
	}
	public static void bj_getPermissionCAMERA(Context context, bj_permission.OnGetPermissionListener onGetPermissionListener) {
		CheckPermision(context, Manifest.permission.CAMERA, R.string.permission_use_camera, onGetPermissionListener);
	}
}

