package hamdiutils.os;

public class OperSystem {

	private static String Name;
	private static OSType Type;

	static {
		setName(System.getProperty("os.name").toLowerCase());
		if (Name.contains("win")) {
			setType(OSType.Windows);
		} else if (Name.contains("mac")) {
			setType(OSType.MacOS);
		} else if (Name.contains("nux")) {
			setType(OSType.Linux);
		} else {
			setType(OSType.Other);
		}
	}

	public static String getName() {
		return Name;
	}

	private static void setName(String name) {
		Name = name;
	}

	public static OSType getType() {
		return Type;
	}

	private static void setType(OSType type) {
		Type = type;
	}

}
