package hamdiutils.log;

import hamdiutils.file.FileUtil;
import hamdiutils.os.OperSystem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {

	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_BLACK = "\u001B[30m";
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_YELLOW = "\u001B[33m";
	public static final String ANSI_BLUE = "\u001B[34m";
	public static final String ANSI_PURPLE = "\u001B[35m";
	public static final String ANSI_CYAN = "\u001B[36m";
	public static final String ANSI_WHITE = "\u001B[37m";

	private String path;
	private PrintStream printStream;

	private boolean includeSystemOut = true;

	private boolean logInfo = true;
	private boolean logError = true;
	private boolean logWarning = true;

	private final static SimpleDateFormat logSDF = new SimpleDateFormat(
			"yyyyMMdd HH:mm:ss");

	public Logger() {
		this.includeSystemOut = true;
	}

	public Logger(PrintStream printStream, boolean includeSystemOut) {
		super();
		this.printStream = printStream;
		this.includeSystemOut = includeSystemOut;
		writeStart();
	}

	public Logger(PrintStream printStream) {
		super();
		this.printStream = printStream;
		this.includeSystemOut = false;
		writeStart();
	}

	public Logger(String path) {
		super();
		this.path = path;
		this.includeSystemOut = false;

		if (!FileUtil.exists(path)) {
			FileUtil.createFile(path);
		}

		try {
			printStream = new PrintStream(new FileOutputStream(new File(path),
					true), true, "UTF-8");
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
			return;
		}
		writeStart();
	}

	public Logger(String path, boolean includeSystemOut) {
		super();
		this.path = path;
		this.includeSystemOut = includeSystemOut;

		if (!FileUtil.exists(path)) {
			FileUtil.createFile(path);
		}

		try {
			printStream = new PrintStream(new FileOutputStream(new File(path),
					true), true, "UTF-8");
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
			return;
		}
		writeStart();
	}

	private void writeStart() {
		if (printStream != null) {
			printStream.println("::::::::::::::::::::::::::::::::");
			printStream.println("::::::::::::::::LOG:::::::::::::");
			printStream.println("::::::::::::::::::::::::::::::::");
		}
	}

	public void writeInfo(String msg) {
		writeLog("INFO", msg, logInfo);
	}

	public void writeWarning(String msg) {
		writeLog("ALERTE", msg, logWarning);
	}

	public void writeError(String msg) {
		writeLog("ERREUR", msg, logError);
	}

	private void writeLog(String type, String msg, boolean logType) {
		String lMsg = "[" + logSDF.format(new Date()) + "] ::: " + type
				+ " ::: " + msg;

		String lColorMsg = lMsg;
		switch (type) {
		case "ERREUR":
			switch (OperSystem.getType()) {
			case Linux:
				lColorMsg = ANSI_RED + lColorMsg + ANSI_RESET;
				break;
			case MacOS:
				break;
			case Other:
				break;
			case Windows:
				break;
			default:
				break;
			}
			break;
		case "ALERTE":
			switch (OperSystem.getType()) {
			case Linux:
				lColorMsg = ANSI_YELLOW + lColorMsg + ANSI_RESET;
				break;
			case MacOS:
				break;
			case Other:
				break;
			case Windows:
				break;
			default:
				break;
			}
			break;
		case "INFO":
			switch (OperSystem.getType()) {
			case Linux:
				break;
			case MacOS:
				break;
			case Other:
				break;
			case Windows:
				break;
			default:
				break;
			}
			break;
		default:
			break;
		}

		if (includeSystemOut && logType) {
			System.out.println(lColorMsg);
		}

		if (printStream == null) {
			return;
		}

		printStream.println(lMsg);

	}

	public String getPath() {
		return path;
	}

	public PrintStream getPrintStream() {
		return printStream;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public void setPrintStream(PrintStream printStream) {
		this.printStream = printStream;
	}

	public boolean isIncludeSystemOut() {
		return includeSystemOut;
	}

	public boolean isLogInfo() {
		return logInfo;
	}

	public boolean isLogError() {
		return logError;
	}

	public boolean isLogWarning() {
		return logWarning;
	}

	public void setIncludeSystemOut(boolean includeSystemOut) {
		this.includeSystemOut = includeSystemOut;
	}

	public void setLogInfo(boolean logInfo) {
		this.logInfo = logInfo;
	}

	public void setLogError(boolean logError) {
		this.logError = logError;
	}

	public void setLogWarning(boolean logWarning) {
		this.logWarning = logWarning;
	}

}
