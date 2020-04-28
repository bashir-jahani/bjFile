package bj.modules;

import android.Manifest;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import bj.modules.bjfile.R;

import static bj.modules.bj_messageBox.MessageBoxAsError;
import static bj.modules.bj_permission.CheckPermision;

public class bj_path_files {
	private static String TAG="bj_path_files";

	public static String customPath;
	public  static File createTemporaryFile(String part, String ext) throws Exception
	{
		File tempDir= Environment.getExternalStorageDirectory();
		tempDir=new File(tempDir.getAbsolutePath()+"/.temp/");
		if(!tempDir.exists())
		{
			tempDir.mkdirs();
		}

		return File.createTempFile(part, ext, tempDir);
	}
	public  static File createTemporaryFile(Context context, String part, String ext) throws Exception	{
		File tempDir= new File(Paths_Temp(context));

		File tempFile=new File(tempDir,part+".tmp");

		//Log.i(TAG, "createTemporaryFile path 1: "+tempFile.getAbsolutePath());
		//Log.i(TAG, "createTemporaryFile exist 1: "+tempFile.exists());
		return tempFile;
	}
	public  static File createTemporaryFile(Context context, String part, String ext, @bj_file_classes.FileTransfer_File_Kinds int fileKind,String memeberID, boolean asThumbnails) throws Exception	{
		File tempDir;
		String tempDirPath= Paths_Temp(context,fileKind,memeberID);
		if (asThumbnails) {

			if (tempDirPath.endsWith(File.separator)){
				tempDirPath=tempDirPath+"thumbnails";
			}else {
				tempDirPath=tempDirPath+File.separator+"thumbnails";
			}

		}
		tempDir= new File(tempDirPath);
		if (!tempDir.exists()){
			tempDir.mkdirs();
		}
		File tempFile=new File(tempDir,part+".tmp");

		if (!tempFile.exists()){
			tempFile.createNewFile();
		}
		//Log.i(TAG, "createTemporaryFile path 2: "+tempFile.getAbsolutePath());
		//Log.i(TAG, "createTemporaryFile exist 2: "+tempFile.exists());
		//Log.i(TAG, "createTemporaryFile: tempDir is directory "+tempDir.isDirectory());
		//Log.i(TAG, "createTemporaryFile: tempFile is file "+tempFile.isFile());
		return tempFile;
	}
	public static void writeBytesToTempFile(File tempFile,byte[] bytes){
		Log.i(TAG, "writeBytesToTempFile: temp file size 1: "+tempFile.length());
		OutputStream out = null;
		try {

			Log.i(TAG, "writeBytesToTempFile: temp file size 2: "+tempFile.length());
			out = new FileOutputStream(tempFile);
			out.write(bytes, 0, bytes.length);
			out.close();
		} catch (IOException e) {
			Log.i(TAG, "writeBytesToTempFile error: "+tempFile.length());
			e.printStackTrace();
		}
		//Log.i(TAG, "writeBytesToTempFile: temp file size 3: "+tempFile.length());
	}
	public  static Boolean File_Copy(File src, File dst) throws IOException {
		if (!src.exists()){
			Log.e(TAG,"Source File Dont Exist!");
			return false;
		}
		Log.e(TAG,src.getAbsolutePath() + "\n" + dst.getAbsolutePath());

		if (!dst.getParentFile().exists()){
			dst.getParentFile().mkdirs();

		}
		Log.e(TAG,dst.getParentFile().exists()+"");
		InputStream in = new FileInputStream(src);

		try {
			OutputStream out = new FileOutputStream(dst);
			try {
				// Transfer bytes from in to out
				byte[] buf = new byte[1024];
				int len;
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
			} finally {
				out.close();
			}
		} finally {
			in.close();
		}
		return (dst.exists() & dst.isFile());
	}
	public  static Boolean File_Copy(File src, File Parendirectory,@Nullable String dstName) throws IOException {
		if (!src.exists()){
			Log.e(TAG,"Source File Dont Exist!");
			return false;
		}
		Log.e(TAG,src.getAbsolutePath() + "\n" + Parendirectory.getAbsolutePath());

		if (!Parendirectory.exists()){
			Parendirectory.mkdirs();

		}
		Log.e(TAG,Parendirectory.exists()+"");
		InputStream in = new FileInputStream(src);
		String FileName;
		if(dstName == null) {
			FileName = src.getName();
		}else {
			FileName=dstName;
		}
		File dst=new File(Parendirectory.getPath(),FileName);
		try {
			OutputStream out = new FileOutputStream(dst);
			try {
				// Transfer bytes from in to out
				byte[] buf = new byte[1024];
				int len;
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
			} finally {
				out.close();
			}
		} finally {
			in.close();
		}
		return (dst.exists() & dst.isFile());
	}

	public  static String FileNameFromPath(String FilePath){
		return FilePath.substring(FilePath.lastIndexOf(File.separator)+1);
	}
	public  static Boolean FileExists(Context context, String FilePath){
		final boolean[] _HavePermission = {false};
		//Log.e(TAG,"Load_PictureTo_ImageView " );
		CheckPermision(context, Manifest.permission.READ_EXTERNAL_STORAGE, R.string.permission_save_images, new bj_permission.OnGetPermissionListener() {
			@Override
			public void onPermissionProcesComplated(String PermissionNeeded, Boolean HavePermission) {
				_HavePermission[0] =HavePermission;
			}
		});



		boolean Exist=false;

		if (_HavePermission[0]){
			try {
				File file=new File(FilePath);
				Exist=file.exists();
				file=null;
			}catch (Exception e){
				Log.e(TAG,"FileExists error:" + e.getMessage());
			}

		}else {
//			MessageBoxAsError(context.getResources().getString(R.string.permission_not_accept),context);
			Toast.makeText(context, R.string.permission_not_accept, Toast.LENGTH_SHORT).show();
		}



		return Exist;

	}

	public static Boolean directoryCreateIfNotExists(String directoryPath){
		File file=new File(directoryPath);
		if (file.exists()){
			return true;
		}
		if (file.isDirectory()) {
			return file.mkdirs();
		}else {
			return false;
		}
	}
	public  static String Path_Temp_New(Context context){
		ContextWrapper cw = new ContextWrapper(context);

		File directory = cw.getDir("temp" , Context.MODE_PRIVATE);

		return directory.getAbsolutePath();
	}
	public  static String Paths_Temp(Context context ){
		String p=Path_Temp_New(context);
		if (customPath!=null){
			if (customPath.length()>0){
				p=customPath;
				if (!p.endsWith(File.separator)){
					p=p+File.separator;
				}
				p=p+context.getString(R.string.app_name) + File.separator+".temp";

			}
		}
		if (!p.endsWith(File.separator)){
			p=p+File.separator;
		}
		File file=new File(p);
		if (!file.exists()){    file.mkdirs();}

		return file.getAbsolutePath();
	}
	public  static String Paths_Temp(Context contex, int FileKind,String memberID){
		String p=Paths_Temp(contex);
		if (FileKind== bj_file_classes.FileTransfer_File_Kinds.PersonalImage) {
			p=p+File.separator + "P_Images"+ File.separator+memberID+ File.separator;
		}else if(FileKind== bj_file_classes.FileTransfer_File_Kinds.TheFile){
			p=p+File.separator + "Files"+ File.separator;
		}else if(FileKind== bj_file_classes.FileTransfer_File_Kinds.TheAudio){
			p=p+File.separator + "Audio"+ File.separator;
		}else if(FileKind== bj_file_classes.FileTransfer_File_Kinds.TheBackup){
			p=p+File.separator + "Backup"+ File.separator;
		}else if(FileKind== bj_file_classes.FileTransfer_File_Kinds.TheImage){
			p=p+File.separator + "Images"+ File.separator;
		}else if(FileKind== bj_file_classes.FileTransfer_File_Kinds.TheMusic){
			p=p+File.separator + "Music"+ File.separator;
		}else if(FileKind== bj_file_classes.FileTransfer_File_Kinds.TheVideo){
			p=p+File.separator + "Video"+ File.separator;
		}
		File file=new File(p);
		if (!file.exists()){    file.mkdirs();}

		return p;
	}
	public  static String Camera_Temp_Image_Paths(Context contex){
		return  Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_PICTURES) + File.separator +
				 contex.getPackageName()+File.separator + "TempImage_FromCamera.jpg";
	}
	public static File  Camera_Temp_Image(Context contex){
		File tempFile= new File(Camera_Temp_Image_Paths(contex));
		if(!tempFile.getParentFile().exists())
		{
			tempFile.getParentFile().mkdirs();
		}
		if (!tempFile.exists()){
			tempFile.delete();
		}
		try {
			tempFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return tempFile;
	}
	public  static String Paths_Temp_file(Context contex, int FileKind,String fileName,String memberID,boolean asThumbnails){
		String p=Paths_Temp(contex);
		if (FileKind== bj_file_classes.FileTransfer_File_Kinds.PersonalImage) {
			p=p+File.separator + "P_Images"+ File.separator+memberID+ File.separator;
		}else if(FileKind== bj_file_classes.FileTransfer_File_Kinds.TheFile){
			p=p+File.separator + "Files"+ File.separator;
		}else if(FileKind== bj_file_classes.FileTransfer_File_Kinds.TheAudio){
			p=p+File.separator + "Audio"+ File.separator;
		}else if(FileKind== bj_file_classes.FileTransfer_File_Kinds.TheBackup){
			p=p+File.separator + "Backup"+ File.separator;
		}else if(FileKind== bj_file_classes.FileTransfer_File_Kinds.TheImage){
			p=p+File.separator + "Images"+ File.separator;
		}else if(FileKind== bj_file_classes.FileTransfer_File_Kinds.TheMusic){
			p=p+File.separator + "Music"+ File.separator;
		}else if(FileKind== bj_file_classes.FileTransfer_File_Kinds.TheVideo){
			p=p+File.separator + "Video"+ File.separator;
		}
		File file=new File(p);
		if (!file.exists()){    file.mkdirs();}
		if (asThumbnails){
			return p+"thumbnails"+File.separator+fileName;
		}else {
			return p+fileName;
		}

	}
	public  static String Paths_Temp_MemberImages(Context contex,String memberID){

		String GPath= Paths_Temp(contex, bj_file_classes.FileTransfer_File_Kinds.PersonalImage,memberID)+File.separator;
		File file=new File(GPath);
		if (!file.exists()){    file.mkdirs();}
		GPath=GPath+memberID;
		return GPath;
	}
	public  static String Paths_Temp_MembersImages(Context contex){

		String GPath= Paths_Temp(contex)+ File.separator+"Images"+File.separator+"P_Images"+File.separator;
		File file=new File(GPath);
		if (!file.exists()){    file.mkdirs();}

		return GPath;
	}
	public  static String JPGImageInPath_Last(Context context, String Path, @Nullable Boolean OnlyFileName){
		final boolean[] _HavePermission = {false};
		//Log.e(TAG,"Load_PictureTo_ImageView " );
		CheckPermision( context, Manifest.permission.READ_EXTERNAL_STORAGE, R.string.permission_save_images, new bj_permission.OnGetPermissionListener() {
			@Override
			public void onPermissionProcesComplated(String PermissionNeeded, Boolean HavePermission) {
				_HavePermission[0] =HavePermission;
			}
		});
		String RS="";
		if (_HavePermission[0]){
			File file=new File( Path);

			try{
				if (file.exists() & file.isDirectory()){
					FilenameFilter only=new OnlyExt("jpg");
					String files[]= file.list(only);
					if(files.length>0){
						Arrays.sort(files);

						if(OnlyFileName) {
							RS = files[files.length - 1];
						}else {
							RS=Path+"/"+files[files.length-1];
						}
					}
				}
			}catch (Exception e){
				Log.e(TAG,"JPGImageInPath_Last error:" + e.getMessage());
			}
		}else {
			MessageBoxAsError(context.getResources().getString(R.string.permission_not_accept),context);
		}




		return RS;
	}
	public  static File JPGImageInPath_Last(Context context, String Path){
		final boolean[] _HavePermission = {false};
		//Log.e(TAG,"Load_PictureTo_ImageView " );
		CheckPermision(context, Manifest.permission.READ_EXTERNAL_STORAGE, R.string.permission_save_images, new bj_permission.OnGetPermissionListener() {
			@Override
			public void onPermissionProcesComplated(String PermissionNeeded, Boolean HavePermission) {
				_HavePermission[0] =HavePermission;
			}
		});
		File RS=null;
		if (_HavePermission[0]){
			File file=new File( Path);

			try{
				if (file.exists() & file.isDirectory()){
					FilenameFilter only=new OnlyExt(".jpg");
					File files[]= file.listFiles(only);
					if(files.length>0){
						Arrays.sort(files);

						RS = files[files.length - 1];
					}
				}
			}catch (Exception e){
				Log.e(TAG,"JPGImageInPath_Last error:" + e.getMessage());
			}
		}else {
			MessageBoxAsError(context.getResources().getString(R.string.permission_not_accept),context);
		}




		return RS;
	}
	public  static File[] JPGImagesInPath(Context context, String Path){
		final boolean[] _HavePermission = {false};
		//Log.e(TAG,"Load_PictureTo_ImageView " );
		CheckPermision(context, Manifest.permission.READ_EXTERNAL_STORAGE, R.string.permission_save_images, new bj_permission.OnGetPermissionListener() {
			@Override
			public void onPermissionProcesComplated(String PermissionNeeded, Boolean HavePermission) {
				_HavePermission[0] =HavePermission;
			}
		});
		File files[]=null;
		if (_HavePermission[0]){
			File file=new File( Path);

			try{
				if (file.exists() & file.isDirectory()){
					FilenameFilter only=new OnlyExt(".jpg");
					 files= file.listFiles(only);
					if(files.length>0){
						Arrays.sort(files);


					}
				}
			}catch (Exception e){
				Log.e(TAG,"JPGImageInPath_Last error:" + e.getMessage());
			}
		}else {
			MessageBoxAsError(context.getResources().getString(R.string.permission_not_accept),context);
		}




		return files;
	}
	public  static File JPGImageInPath_ThisIndex(Context context, String Path,Integer ImageIndex){
		final boolean[] _HavePermission = {false};
		//Log.e(TAG,"Load_PictureTo_ImageView " );
		CheckPermision(context, Manifest.permission.READ_EXTERNAL_STORAGE, R.string.permission_save_images, new bj_permission.OnGetPermissionListener() {
			@Override
			public void onPermissionProcesComplated(String PermissionNeeded, Boolean HavePermission) {
				_HavePermission[0] =HavePermission;
			}
		});
		File RS=null;
		if (_HavePermission[0]){
			File file=new File( Path);

			try{
				if (file.exists() & file.isDirectory()){
					FilenameFilter only=new OnlyExt(".jpg");
					File files[]= file.listFiles(only);
					if(files.length>0){
						RS = files[ImageIndex % files.length];
					}
				}

			}catch (Exception e){
				Log.e(TAG,"JPGImageInPath_ThisIndex error:" + e.getMessage());
			}
		}else {
			MessageBoxAsError(context.getResources().getString(R.string.permission_not_accept),context);
		}



		return RS;
	}
	public  static String JPGImageInPath_ThisIndex(Context context, String Path,Integer ImageIndex, @Nullable Boolean OnlyFileName){
		final boolean[] _HavePermission = {false};
		//Log.e(TAG,"Load_PictureTo_ImageView " );
		CheckPermision(context, Manifest.permission.READ_EXTERNAL_STORAGE, R.string.permission_save_images, new bj_permission.OnGetPermissionListener() {
			@Override
			public void onPermissionProcesComplated(String PermissionNeeded, Boolean HavePermission) {
				_HavePermission[0] =HavePermission;
			}
		});
		String RS=null;
		if (_HavePermission[0]){
			File file=new File( Path);
			try{
				if (file.exists() & file.isDirectory()){
					FilenameFilter only=new OnlyExt(".jpg");
					String files[]= file.list(only);
					if(files.length>0){
						Log.d("bj modules","Index:"+ImageIndex + "/Leng:"+files.length+"/Calcute Index:"+ImageIndex % files.length);
						if(OnlyFileName) {
							RS = files[(int) (ImageIndex % files.length)];
						}else {
							RS=Path+"/"+files[(int) (ImageIndex % files.length)];
						}
					}
				}
			}catch (Exception e){
				Log.e(TAG,"JPGImageInPath_ThisIndex error:" + e.getMessage());
			}
		}else {
			MessageBoxAsError(context.getResources().getString(R.string.permission_not_accept),context);
		}




		return RS;
	}

	public static class OnlyExt implements FilenameFilter {
		String Ext;
		List<String>  ExtL;
		public OnlyExt(String ext){
			Ext=ext;
			ExtL=null;
		}
		public OnlyExt(List<String> ext){
			ExtL=ext;
			Ext=null;
		}
		public  boolean accept(File dir,String name){
			if (ExtL==null) {
				return name.endsWith(Ext);
			}else {
				Boolean RS;
				for (int i=0;i<ExtL.size();i++){
					if (name.endsWith(ExtL.get(i))){
						return true;
					}
				}
				return false;
			}
		}

	}
}
