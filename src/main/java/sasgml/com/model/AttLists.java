package sasgml.com.model;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;

import sasgml.com.exception.SASgmlException;
import sasgml.com.log.LogManager;

public class AttLists extends LinkedHashMap<String, AttList> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int size = 0;

	public void arrange(Entities pDTDEntities) throws SASgmlException,
			IOException {
		for (AttList cDTDATTList : values()) {
			String rContentText = pDTDEntities.getRealString(cDTDATTList
					.getTextContent());
			cDTDATTList.setTextContent(rContentText);
			cDTDATTList.parseTextContent();
			// System.out.println(cDTDATTList.GetDescription());
		}

	}

	public void perform(Entities pEntities, Notations pNotations) {
		HashMap<String, AttList> map = new HashMap<String, AttList>();

		for (AttList cDTDATTList : values()) {
			String cName = cDTDATTList.getName();
			for (String cNormalizedName : cName.split("[\\|&,]", -1)) {
				if (map.containsKey(cNormalizedName)) {
					map.get(cNormalizedName).getAttributes()
							.putAll(cDTDATTList.getAttributes());
				} else {
					map.put(cNormalizedName.trim(), new AttList(cDTDATTList));
				}
			}

			for (Attribute cAttribute : cDTDATTList.getAttributes().values()) {
				String defaultValue = cAttribute.getDefaultValue()
						.toUpperCase();
				switch (cAttribute.getTypeEnum()) {
				case ENTITIES:
					for (String cDefaultValuePart : defaultValue.split("//s")) {
						if (!cDefaultValuePart.equals("")) {
							if (!pEntities.containsKey(cDefaultValuePart)) {
								LogManager
										.writeWarning("la valeur par défaut ["
												+ cDefaultValuePart
												+ "] de l'attribut ["
												+ cAttribute.getName()
												+ "] de l'element ["
												+ cName
												+ "] n'existe pas en tantque ENTITY");
							}
						}
					}
					break;
				case ENTITY:
					if (!defaultValue.equals("")) {
						if (!pEntities.containsKey(defaultValue)) {
							LogManager.writeWarning("la valeur par défaut ["
									+ defaultValue + "] de l'attribut ["
									+ cAttribute.getName() + "] de l'element ["
									+ cName
									+ "] n'existe pas en tantque ENTITY");
						}
					}
					break;
				case NOTATION:
					if (!defaultValue.equals("")) {
						if (!pNotations.containsKey(defaultValue)) {
							LogManager.writeWarning("la valeur par défaut ["
									+ defaultValue + "] de l'attribut ["
									+ cAttribute.getName() + "] de l'element ["
									+ cName
									+ "] n'existe pas en tantque NOTATION");
						}
					}
					for (String cValue : cAttribute.getOriginalValues()) {
						if (!pNotations.containsKey(cValue.toUpperCase())) {
							LogManager
									.writeWarning("la valeur de la liste de choix ["
											+ cValue.toUpperCase()
											+ "] de l'attribut ["
											+ cAttribute.getName()
											+ "] de l'element ["
											+ cName
											+ "] n'existe pas en tantque NOTATION");
						}
					}
					break;
				default:
					break;

				}
			}
		}

		clear();
		putAll(map);

	}

	public void add(AttList lAttlist) {
		put(String.valueOf(size), lAttlist);
		size++;
	}

	public void addAll(AttLists lAttLists) {
		for (AttList lAttList : lAttLists.values()) {
			add(lAttList);
		}
	}

}
