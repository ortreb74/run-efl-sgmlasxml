package sasgml.com.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.List;

public class FileUtil {
	public static File createFile(String pFilePath) {
		File lFile = new File(pFilePath);
		File lParentFile = lFile.getParentFile();

		if (lParentFile != null) {
			if (!lParentFile.exists()) {
				lParentFile.mkdirs();
			}
		}

		try {
			lFile.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return lFile;
	}

	public static boolean createDirectory(String pDirPath) {
		File lFile = new File(pDirPath);
		if (lFile != null) {
			if (!lFile.exists()) {
				return lFile.mkdirs();
			}
		}
		return false;
	}

	public static boolean delete(File pFile) {
		if (pFile.isDirectory()) {
			for (File c : pFile.listFiles()) {
				delete(c);
			}
		}
		return pFile.delete();
	}

	public static boolean delete(String pFilePath) {
		return delete(new File(pFilePath));
	}

	public static boolean exists(String path) {
		return new File(path).exists();
	}

	@SuppressWarnings("resource")
	public static void copyFile(String sFilePath, String dFilePath)
			throws IOException {
		FileUtil.createFile(dFilePath);
		FileChannel in = new FileInputStream(sFilePath).getChannel();
		FileChannel out = new FileOutputStream(dFilePath).getChannel();
		in.transferTo(0, in.size(), out);
		out.close();
		in.close();
	}

	public static void copyDirectory(String sFilePath, String dFilePath)
			throws IOException {
		FileUtil.createDirectory(dFilePath);
		for (File cFile : new File(sFilePath).listFiles()) {
			if (cFile.isDirectory()) {
				copyDirectory(cFile.getAbsolutePath(),dFilePath + "/" + cFile.getName());
			} else {
				copyFile(cFile.getAbsolutePath(),
						dFilePath + "/" + cFile.getName());
			}
		}
	}

	public static ArrayList<String> getFilesNameList(String pDirPath,
			List<String> patterns) {

		ArrayList<String> result = new ArrayList<String>();
		if (!FileUtil.exists(pDirPath)) {
			return result;
		}

		File pInputDir = new File(pDirPath);

		for (File cFile : pInputDir.listFiles()) {
			Path cFileName = cFile.toPath().getFileName();
			for (String regex : patterns) {
				PathMatcher matcher = FileSystems.getDefault().getPathMatcher(
						"glob:" + regex);
				if (matcher.matches(cFileName)) {
					result.add(cFile.getName());
				}
			}
		}

		return result;

	}

}
