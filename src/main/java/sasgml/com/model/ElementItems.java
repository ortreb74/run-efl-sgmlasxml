package sasgml.com.model;
import java.util.LinkedList;

public class ElementItems extends LinkedList<ElementItem> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void addNext(ElementItems entries) {
		for (ElementItem lDTDElementItem : this) {
			lDTDElementItem.addNext(entries);
		}
	}

	public void addNext(ElementItem entry) {
		for (ElementItem lDTDElementItem : this) {
			lDTDElementItem.getNextList().add(entry);
		}
	}
}
