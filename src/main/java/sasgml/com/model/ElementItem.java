package sasgml.com.model;
import java.util.ArrayList;
import java.util.List;

public class ElementItem {
	private String name;
	private List<ElementItem> nextList;
	private List<ElementItem> previousList;
	private List<ElementItem> cleanedNextList;

	public ElementItem(String name) {
		this.name = name;
		setNextList(new ArrayList<ElementItem>());
		setPreviousList(new ArrayList<ElementItem>());
	}

	public ElementItem() {
		// TODO Auto-generated constructor stub
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<ElementItem> getNextList() {
		return nextList;
	}

	public void setNextList(List<ElementItem> nextList) {
		this.nextList = nextList;
	}

	public void addNext(ElementItem pDTDElementItem) {
		nextList.add(pDTDElementItem);
	}

	public void println(ArrayList<String> memory) {
		System.out.println(name);
		for (ElementItem lDTDElementItem : nextList) {
			System.out.println("->" + lDTDElementItem.getName());
		}
		for (ElementItem cDTDElementItem : nextList) {
			if (memory.contains(cDTDElementItem.getName())) {

			} else {
				memory.add(cDTDElementItem.getName());
				cDTDElementItem.println(memory);
			}
		}
	}

	public void performCleanedNextList(List<ElementItem> memory) {
		cleanedNextList = new ArrayList<ElementItem>();
		for (ElementItem cDTDElementItem : nextList) {
			if (cDTDElementItem == null) {
				continue;
			}
			if (cDTDElementItem.getName().equals("#exit")
					|| cDTDElementItem.getName().equals("#entry")) {
				if (!memory.contains(cDTDElementItem)) {
					memory.add(cDTDElementItem);
					cDTDElementItem.performCleanedNextList(memory);
					cleanedNextList
							.addAll(cDTDElementItem.getCleanedNextList());
				}
			} else {
				cleanedNextList.add(cDTDElementItem);
			}
		}
	}

	// public void print(int start, ArrayList<ElementItem> memory) {
	// String pretty = new String(new char[start]).replace('\0', ' ');
	// System.out.print(pretty + name + "->{\n");
	// for (int i = 0; i < nextList.size() - 1; i++) {
	// ElementItem cDTDElementItem = nextList.get(i);
	// if (!memory.contains(cDTDElementItem)) {
	// memory.add(cDTDElementItem);
	// cDTDElementItem.print(start + 1, memory);
	// System.out.print(",");
	// }
	// }
	// if (nextList.size() > 0) {
	// ElementItem cDTDElementItem = nextList.get(nextList.size() - 1);
	// if (!memory.contains(cDTDElementItem)) {
	// memory.add(cDTDElementItem);
	// cDTDElementItem.print(start + 1, memory);
	// }
	//
	// }
	//
	// System.out.print("\n" + pretty + "}\n");
	// }

	public void print(int start, ArrayList<ElementItem> memory) {
		String pretty = new String(new char[start]).replace('\0', ' ');
		System.out.print(pretty + name + "->{");
		performCleanedNextList(new ArrayList<ElementItem>());
		List<ElementItem> list = getCleanedNextList();
		for (int i = 0; i < list.size() - 1; i++) {
			ElementItem cDTDElementItem = list.get(i);
			System.out.print(cDTDElementItem.getName() + ",");
		}
		if (list.size() > 0) {
			ElementItem cDTDElementItem = list.get(list.size() - 1);
			System.out.print(cDTDElementItem.getName());
		}

		System.out.print("}\n");

		for (int i = 0; i < list.size(); i++) {
			ElementItem cDTDElementItem = list.get(i);
			if (!memory.contains(cDTDElementItem)) {
				memory.add(cDTDElementItem);
				cDTDElementItem.print(start + 1, memory);
			}
		}
	}

	public List<ElementItem> getPreviousList() {
		return previousList;
	}

	public void setPreviousList(List<ElementItem> previousList) {
		this.previousList = previousList;
	}

	public void addNext(ElementItems items) {
		nextList.addAll(items);
	}

	public List<ElementItem> getCleanedNextList() {
		return cleanedNextList;
	}

	public void setCleanedNextList(List<ElementItem> cleanedNextList) {
		this.cleanedNextList = cleanedNextList;
	}
}
