package sasgml.com.model;
import java.util.LinkedList;

import sasgml.com.parsing.Token;

public class ElementExpression {
	private ElementOperationTypeEnum opType;
	private ElementOccurenceTypeEnum ocType;
	private String name;
	private ElementExpression parent;
	private LinkedList<ElementExpression> childs;

	private ElementItem entry;
	private ElementItem exit;

	public ElementExpression() {
		super();
		childs = new LinkedList<ElementExpression>();
		opType = ElementOperationTypeEnum.NONE;
		ocType = ElementOccurenceTypeEnum.ONE;
	}

	public ElementExpression(String pElementName) {
		childs = new LinkedList<ElementExpression>();
		setName(pElementName);
		setOType(ElementOperationTypeEnum.NONE);
	}

	private static LinkedList<LinkedList<Integer>> getAllScenarii(
			LinkedList<Integer> list) {
		LinkedList<LinkedList<Integer>> res = new LinkedList<LinkedList<Integer>>();
		for (int i = 0; i < list.size(); i++) {
			LinkedList<Integer> sList = new LinkedList<Integer>(list);
			sList.remove(i);
			LinkedList<LinkedList<Integer>> nList = getAllScenarii(sList);
			if (nList.size() == 0) {
				LinkedList<Integer> rList = new LinkedList<Integer>();
				rList.add(list.get(i));
				res.add(rList);
			} else {
				for (int j = 0; j < nList.size(); j++) {
					LinkedList<Integer> rList = new LinkedList<Integer>();
					rList.add(list.get(i));
					rList.addAll(nList.get(j));
					res.add(rList);
				}
			}
		}
		return res;
	}

	public void loadRule() {
		entry = new ElementItem("#entry");
		exit = new ElementItem("#exit");

		for (ElementExpression lDTDElementExpression : childs) {
			lDTDElementExpression.loadRule();
		}

		switch (opType) {
		case AND:
			LinkedList<Integer> list = new LinkedList<Integer>();
			for (int i = 0; i < childs.size(); i++) {
				list.add(i);
			}
			LinkedList<LinkedList<Integer>> scenarii = getAllScenarii(list);

			for (LinkedList<Integer> cList : scenarii) {
				if (cList.size() > 0) {
					entry.addNext(childs.get(cList.get(0)).getEntry());
					for (int i = 1; i < cList.size(); i++) {
						childs.get(cList.get(i - 1)).getExit()
								.addNext(childs.get(cList.get(i)).getEntry());
					}
					childs.get(cList.getLast()).getExit().addNext(exit);
				} else {
					entry.addNext(exit);
				}
				switch (ocType) {
				case ASTERIX:
					entry.addNext(exit);
					exit.addNext(entry);
					break;
				case ONE:
					break;
				case PLUS:
					exit.addNext(entry);
					break;
				case QUID:
					entry.addNext(exit);
					break;
				default:
					break;
				}
			}

			break;
		case CHOICE:
			for (ElementExpression lDTDElementExpression : childs) {
				entry.addNext(lDTDElementExpression.getEntry());
				lDTDElementExpression.getExit().addNext(exit);
			}
			switch (ocType) {
			case ASTERIX:
				entry.addNext(exit);
				exit.addNext(entry);
				break;
			case ONE:
				break;
			case PLUS:
				exit.addNext(entry);
				break;
			case QUID:
				entry.addNext(exit);
				break;
			default:
				break;
			}
			break;
		case EXPRESSION:
			entry.setName("#ENTRY");
			exit.setName("#EXIT");
			if (childs.size() > 0) {
				entry.addNext(childs.get(0).getEntry());
				childs.get(0).getExit().addNext(exit);
			}

			// entry = entry.getCleanedItem(new
			// ArrayList<ElementItem>());
			// entry.print(0, new ArrayList<ElementItem>());
			break;
		case GROUP:
			switch (ocType) {
			case ASTERIX:
				entry.addNext(exit);
				exit.addNext(entry);
				if (childs.size() > 0) {
					entry.addNext(childs.get(0).getEntry());
					childs.get(0).getExit().addNext(exit);
				}
				break;
			case ONE:
				if (childs.size() > 0) {
					entry.addNext(childs.get(0).getEntry());
					childs.get(0).getExit().addNext(exit);
				}
				break;
			case PLUS:
				exit.addNext(entry);
				if (childs.size() > 0) {
					entry.addNext(childs.get(0).getEntry());
					childs.get(0).getExit().addNext(exit);
				}
				break;
			case QUID:
				entry.addNext(exit);
				if (childs.size() > 0) {
					entry.addNext(childs.get(0).getEntry());
					childs.get(0).getExit().addNext(exit);
				}
				break;
			default:
				break;
			}
			break;
		case NONE:
			ElementItem lDTDElementItem = new ElementItem(name);
			entry.addNext(lDTDElementItem);
			lDTDElementItem.addNext(exit);
			switch (ocType) {
			case ASTERIX:
				entry.addNext(exit);
				exit.addNext(entry);
				break;
			case ONE:
				if (Token.TEXT_TAGS.contains(name)) {
					entry.addNext(exit);
					exit.addNext(entry);
				}
				break;
			case PLUS:
				if (Token.TEXT_TAGS.contains(name)) {
					entry.addNext(exit);
					exit.addNext(entry);
				}
				exit.addNext(entry);
				break;
			case QUID:
				entry.addNext(exit);
				break;
			default:
				break;
			}
			break;
		case SEQUENCE:
			if (childs.size() > 0) {
				entry.addNext(childs.get(0).getEntry());
				for (int i = 1; i < childs.size(); i++) {
					childs.get(i - 1).getExit()
							.addNext(childs.get(i).getEntry());
				}
				childs.get(childs.size() - 1).getExit().addNext(exit);
			} else {
				entry.addNext(exit);
			}
			switch (ocType) {
			case ASTERIX:
				entry.addNext(exit);
				exit.addNext(entry);
				break;
			case ONE:
				break;
			case PLUS:
				exit.addNext(entry);
				break;
			case QUID:
				entry.addNext(exit);
				break;
			default:
				break;
			}
			break;
		default:
			break;

		}

	}

	public void println(int start) {
		if (opType.equals(ElementOperationTypeEnum.NONE)) {
			System.out.println(new String(new char[start]).replace('\0', '-')
					+ name + " => " + ocType.toString());
		} else {
			System.out.println(new String(new char[start]).replace('\0', '-')
					+ opType.toString() + " => " + ocType.toString());

		}
		for (ElementExpression cDTDElementModel : childs) {
			cDTDElementModel.println(start + 1);
		}

	}

	public LinkedList<ElementExpression> getChilds() {
		return childs;
	}

	public void setOType(ElementOperationTypeEnum oType) {
		this.opType = oType;
	}

	public void setNames(LinkedList<ElementExpression> names) {
		this.childs = names;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ElementExpression getParent() {
		return parent;
	}

	public void setParent(ElementExpression parent) {
		this.parent = parent;
	}

	public ElementOccurenceTypeEnum getOcType() {
		return ocType;
	}

	public void setOcType(ElementOccurenceTypeEnum ocType) {
		this.ocType = ocType;
	}

	public ElementOperationTypeEnum getOpType() {
		return opType;
	}

	public void setOpType(ElementOperationTypeEnum opType) {
		this.opType = opType;
	}

	public void addChild(ElementExpression lDTDElementModel) {
		lDTDElementModel.setParent(this);
		childs.add(lDTDElementModel);
	}

	public ElementItem getEntry() {
		return entry;
	}

	public ElementItem getExit() {
		return exit;
	}

	public void setEntry(ElementItem entry) {
		this.entry = entry;
	}

	public void setExit(ElementItem exit) {
		this.exit = exit;
	}

}
