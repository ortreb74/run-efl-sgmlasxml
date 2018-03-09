package sasgml.com.parsing;

import java.io.File;

public class CaseInsensitiveFileSearcher {

	private boolean found;
	private File file;

	public CaseInsensitiveFileSearcher(String pathname) {
		File lFile = new File(pathname);
		File pFile = lFile.getParentFile();

		if (pFile.exists()) {
			for (File cFile : pFile.listFiles()) {
				if (cFile.getAbsolutePath().toLowerCase()
						.equals(lFile.getAbsolutePath().toLowerCase())) {
					file = cFile;
					break;
				}
			}
		}

		if (file == null) {
			found = false;
		} else {
			found = true;
		}
		// TODO Auto-generated constructor stub
	}

	public boolean find() {
		return found;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

}
