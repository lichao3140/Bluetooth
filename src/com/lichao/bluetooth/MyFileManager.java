package com.lichao.bluetooth;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import android.os.Environment;
import android.text.format.Time;
import android.util.Log;

public class MyFileManager {
	private static final String TAG = "MyFileManager";
	public static String internalSdCard = null;

	/**
	 * 判断SDCard是否存在 [当没有外挂SD卡时，内置ROM也被识别为存在sd卡]
	 * 
	 * @return
	 */
	public boolean isSdCardExist() {
		return Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED);
	}

	/**
	 * 获取SD卡根目录路径
	 * 
	 * @return
	 */
	public String getSdCardPath() {
		boolean exist = isSdCardExist();
		String sdpath = "";
		if (exist) {
			sdpath = Environment.getExternalStorageDirectory()
					.getAbsolutePath();
		} else {
			sdpath = "不适用";
		}
		return sdpath;

	}

	/**
	 * 获取系统当前时间，作为新建文件的默认文件名
	 * 
	 * @return
	 */
	public String getDefaultFileName() {
		String str, s = "";		
		Time time = new Time();
		time.setToNow();
		if (time.hour < 9) {
			s = "0" + (time.hour);
		} else {
			s = "" + (time.hour);
		}
		str = time.toString().substring(0, 8) + s
				+ time.toString().substring(11, 15);
		return str;
	}

	/**
	 * 获取默认的文件路径
	 * 
	 * @return
	 */
	public String getDefaultFilePath() {
		String filepath = "";
		File file = new File(Environment.getExternalStorageDirectory(),
				"abc.txt");
		if (file.exists()) {
			filepath = file.getAbsolutePath();
		} else {
			filepath = "不适用";
		}
		return filepath;
	}

	/**
	 * 在SD卡上创建目录
	 */
	public File createDir(String dir) {
		File dirFile = new File(dir);
		if (BluetoothChat.Debuggable)
			Log.d(TAG, dir);
		dirFile.mkdirs();
		return dirFile;
	}

	/**
	 * 在SD卡上创建文件
	 */
	public File createFile(String fileName, String dir) throws IOException {
		File file = new File(File.separator + dir + File.separator + fileName);
		if (BluetoothChat.Debuggable)
			Log.d(TAG, File.separator + dir + File.separator + fileName);
		file.createNewFile();
		return file;
	}

	/**
	 * 判断SD卡上文件是否存在
	 */
	public boolean isFileExist(String fileName, String dir) {
		File file = new File(File.separator + dir + File.separator + fileName);
		return file.exists();
	}

	/**
	 * 删除SD卡上的文件
	 * 
	 * @param fileName
	 */
	public boolean deleteFile(String fileName, String dir) {
		File file = new File(File.separator + dir + File.separator + fileName);
		if (file == null || !file.exists() || file.isDirectory())
			return false;
		return file.delete();
	}

	/**
	 * 删除SD卡上的文件
	 * 
	 * @param fileName
	 */
	public boolean deleteFile(String dir) {
		File file = new File(File.separator + dir);
		if (file == null || !file.exists() || file.isFile())
			return false;
		return file.delete();
	}

	/**
	 * 写入内容到SD卡中的txt文本中 str为内容
	 */
	public void writeTxtFile(String str, String fileName, boolean append) {
		try {
			// 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
			FileWriter writer = new FileWriter(fileName, append);
			writer.write(str);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 读取SD卡中文本文件
	 * 
	 * @param fileName
	 * @return
	 */
	public String readTxtFile(String fileName) {
		byte[] buff = new byte[1024];
		String sb = "";
		File file = new File(File.separator + fileName);
		if (file.isDirectory()) {
			return null;
		}
		try {
			int b;
			BufferedInputStream bis = new BufferedInputStream(
					new FileInputStream(file));
			while ((b = bis.read(buff, 0, buff.length)) != -1) {
				sb += new String(buff, 0, b);
			}
			bis.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sb;
	}

	/**
	 * 写入数据到SD卡中
	 */
	public File writeData(String path, String fileName, InputStream data) {
		File file = null;
		OutputStream output = null;

		try {
			if (isFileExist(path, "")) {
				createDir(path); // 创建目录
			}

			if (isFileExist(fileName, "")) {
				file = createFile(fileName, path); // 创建文件
			} else {
				file = new File(fileName, path); // 创建文件
			}
			output = new FileOutputStream(file);
			byte buffer[] = new byte[2 * 1024]; // 每次写2K数据
			int temp;
			while ((temp = data.read(buffer)) != -1) {
				output.write(buffer, 0, temp);
			}
			output.flush();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				output.close(); // 关闭数据流操作
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}

		return file;
	}

	/**
	 * 追加文件：使用FileOutputStream，在构造FileOutputStream时，把第二个参数设为true
	 * 
	 * @param fileName
	 * @param content
	 */
	public static void writeTxtFile(String conent, String file) {
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file, true)));
			out.write(conent);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
