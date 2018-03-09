package sasgml.com.model;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;

import sasgml.com.exception.SASgmlException;
import sasgml.com.log.LogManager;

public class Notations extends LinkedHashMap<String, Notation> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int size = 0;

	public void arrange(Entities pDTDEntities) throws IOException,
			SASgmlException {
		for (Notation cDTDNotation : values()) {
			String rContentText = pDTDEntities.getRealString(cDTDNotation
					.getTextContent());
			cDTDNotation.setTextContent(rContentText);
			cDTDNotation.parseTextContent();
			// System.out.println(cDTDATTList.GetDescription());
		}
	}

	public void perform(Entities pDTDEntities) {
		HashMap<String, Notation> map = new HashMap<String, Notation>();

		for (Notation cDtdNotation : values()) {
			String cName = cDtdNotation.getName();
			map.put(cName, new Notation(cDtdNotation));
		}

		for (Entity cDtdEntity : pDTDEntities.values()) {
			if (cDtdEntity.getTypeEnum().equals(TypeEnum.NDATA)) {
				if (!map.containsKey(cDtdEntity.getTypeName())) {
					LogManager
					.writeWarning("le nom ["
									+ cDtdEntity.getTypeName()
									+ "] de l'entité ["
									+ cDtdEntity.getName()
									+ "] fait reference à une notation qui n'existe pas !");
				}
			}
		}

		clear();
		this.putAll(map);
	}

	public void add(Notation lNotation) {
		put(String.valueOf(size), lNotation);
		size++;
	}

	public void addAll(Notations notations) {
		for (Notation lDtdNotation : notations.values()) {
			add(lDtdNotation);
		}
	}
}
