package bj.modules;

import android.Manifest;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileInputStream;
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

public class bj_pth_files {
	public  static File createTemporaryFile(String part, String ext) throws Exception	{
		File tempDir= Environment.getExternalStorageDirectory();
		tempDir=new File(tempDir.getAbsolutePath()+"/.temp/");
		if(!tempDir.exists())
		{
			tempDir.mkdirs();
		}

		return File.createTempFile(part, ext, tempDir);
	}
	public  static Boolean File_Copy(File src, File dst) throws IOException {
		if (!src.exists()){
			Log.e("GGN","Source File Dont Exist!");
			return false;
		}
		Log.e("GGN",src.getAbsolutePath() + "\n" + dst.getAbsolutePath());

		if (!dst.getParentFile().exists()){
			dst.getParentFile().mkdirs();

		}
		Log.e("GGN",dst.getParentFile().exists()+"");
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
			Log.e("GGN","Source File Dont Exist!");
			return false;
		}
		Log.e("GGN",src.getAbsolutePath() + "\n" + Parendirectory.getAbsolutePath());

		if (!Parendirectory.exists()){
			Parendirectory.mkdirs();

		}
		Log.e("GGN",Parendirectory.exists()+"");
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
		Log.e("GGN","Load_PictureTo_ImageView " );
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
				Log.e("GGN","FileExists error:" + e.getMessage());
			}

		}else {
			MessageBoxAsError(context.getResources().getString(R.string.permission_not_accept),context);
		}



		return Exist;

	}
	public  static String Path_Temp_New(Context context){
		ContextWrapper cw = new ContextWrapper(context);

		File directory = cw.getDir(R.string.app_name + "", Context.MODE_PRIVATE);

		return directory.getAbsolutePath();
	}
	public  static String Paths_Temp(){
		bj_Exceptions.bjLog.Read("");
		String GPath= Environment.getExternalStorageDirectory().getAbsolutePath() + "/g_apps/temp";
		File file=new File(GPath);
		if (!file.exists()){    file.mkdirs();}

		return GPath;
	}
	public  static String Paths_Camera_Temp_Image(){
		return  Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_PICTURES) + File.separator +
				"G_Apps_TempImage_FromCamera.jpg";
	}

	public  static String Paths_Temp_MemberImages(){

		String GPath= Paths_Temp()+ File.separator+"Images"+File.separator+"P_Images"+File.separator;
		File file=new File(GPath);
		if (!file.exists()){    file.mkdirs();}

		return GPath;
	}
	public  static String JPGImageInPath_Last(Context context, String Path, @Nullable Boolean OnlyFileName){
		final boolean[] _HavePermission = {false};
		Log.e("GGN","Load_PictureTo_ImageView " );
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
				Log.e("GGN","JPGImageInPath_Last error:" + e.getMessage());
			}
		}else {
			MessageBoxAsError(context.getResources().getString(R.string.permission_not_accept),context);
		}




		return RS;
	}
	public  static File JPGImageInPath_Last(Context context, String Path){
		final boolean[] _HavePermission = {false};
		Log.e("GGN","Load_PictureTo_ImageView " );
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
				Log.e("GGN","JPGImageInPath_Last error:" + e.getMessage());
			}
		}else {
			MessageBoxAsError(context.getResources().getString(R.string.permission_not_accept),context);
		}




		return RS;
	}
	public  static File JPGImageInPath_ThisIndex(Context context, String Path,Integer ImageIndex){
		final boolean[] _HavePermission = {false};
		Log.e("GGN","Load_PictureTo_ImageView " );
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
				Log.e("GGN","JPGImageInPath_ThisIndex error:" + e.getMessage());
			}
		}else {
			MessageBoxAsError(context.getResources().getString(R.string.permission_not_accept),context);
		}



		return RS;
	}
	public  static String JPGImageInPath_ThisIndex(Context context, String Path,Integer ImageIndex, @Nullable Boolean OnlyFileName){
		final boolean[] _HavePermission = {false};
		Log.e("GGN","Load_PictureTo_ImageView " );
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
						Log.d("GGN","Index:"+ImageIndex + "/Leng:"+files.length+"/Calcute Index:"+ImageIndex % files.length);
						if(OnlyFileName) {
							RS = files[(int) (ImageIndex % files.length)];
						}else {
							RS=Path+"/"+files[(int) (ImageIndex % files.length)];
						}
					}
				}
			}catch (Exception e){
				Log.e("GGN","JPGImageInPath_ThisIndex error:" + e.getMessage());
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
