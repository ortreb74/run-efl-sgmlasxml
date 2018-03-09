package sasgml.com.log;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogManager {

	private static SimpleDateFormat sdf = new SimpleDateFormat(
			"yyyy-MM-dd_HH'h'mm'm'ss");

	private static Logger logger = new Logger();

	public static void writeInfo(String msg) {
		logger.writeInfo(msg);
	}

	public static void writeWarning(String msg) {
		logger.writeWarning(msg);
	}

	public static void writeError(String msg) {
		logger.writeError(msg);
	}

	public static String getPath() {
		return logger.getPath();
	}

	public static PrintStream getPrintStream() {
		return logger.getPrintStream();
	}

	public static void setDirectoryPath(String path) {
		logger = new Logger(path + "/" + sdf.format(new Date()) + ".log", true);
	}

	public static void setFilePath(String path) {
		logger = new Logger(path, true);
	}

	public static void setPrintStream(PrintStream printStream) {
		logger = new Logger(printStream, true);
	}

	public static void setLogInfo(boolean log) {
		logger.setLogInfo(log);
	}

	public static void setLogWarning(boolean log) {
		logger.setLogWarning(log);
	}

	public static void setLogError(boolean log) {
		logger.setLogError(log);
	}

}
