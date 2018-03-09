package sasgml.com.model;
import java.util.ArrayList;
import java.util.List;

public class ElementObserver {
	private ElementItem state;
	private ElementModel model;

	public ElementObserver(ElementModel model) {
		super();
		this.setModel(model);
		this.setState(model.getContent().getEntry());
	}

	public ElementItem getState() {
		return state;
	}

	public void setState(ElementItem state) {
		this.state = state;
	}

	public boolean acceptNext(String qName) {
		// return acceptNext(qName, new ArrayList<ElementItem>());
		state.performCleanedNextList(new ArrayList<ElementItem>());
		List<ElementItem> list = state.getCleanedNextList();
		for (ElementItem cDTDElementItem : list) {
			if (cDTDElementItem.getName().equals(qName)) {
				state = cDTDElementItem;
				return true;
			}
		}
		for (ElementItem cDTDElementItem : list) {
			if (cDTDElementItem.getName().equals("ANY")) {
				return true;
			}
		}
		return false;
	}

	public ElementModel getModel() {
		return model;
	}

	public void setModel(ElementModel model) {
		this.model = model;
	}

	public boolean acceptAsInclusion(String qName) {
		return model.getInclusions().contains(qName);
	}

	public boolean acceptAsExclusion(String qName) {
		return model.getExclusions().contains(qName);
	}

	public List<ElementItem> getPlausibleNextName() {
		return state.getCleanedNextList();
	}

	// public boolean acceptNext(String qName, ArrayList<ElementItem> memory)
	// {
	// ElementItem lastState = state;
	// for (ElementItem cDTDElementItem : state.getNextList()) {
	// if (cDTDElementItem.getName().equals(qName)) {
	// state = cDTDElementItem;
	// return true;
	// } else if (cDTDElementItem.getName().equals("#entry")
	// || cDTDElementItem.getName().equals("#exit")) {
	// state = cDTDElementItem;
	// if(!memory.contains(state))
	// {
	// memory.add(state);
	// if (acceptNext(qName,memory)) {
	// return true;
	// } else {
	// state = lastState;
	// }
	// }
	// }
	// }
	// return false;
	// }

	// public boolean acceptNext(String qName) {
	// for (ElementItem cDTDElementItem : state.getNextList()) {
	// if (cDTDElementItem.getName().equals(qName)) {
	// state = cDTDElementItem;
	// return true;
	// }
	// }
	// return false;
	// }

}
