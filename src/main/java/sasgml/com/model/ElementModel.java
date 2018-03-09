package sasgml.com.model;
import java.util.ArrayList;


public class ElementModel {

	public ElementModel() {
		super();
		plusExpression = "";
		minusExpression = "";
		inclusions = new ArrayList<String>();
		exclusions = new ArrayList<String>();
	}

	private String name;
	private ElementExpression content;
	private String plusExpression;
	private String minusExpression;
	
	private ArrayList<String> inclusions;
	private ArrayList<String> exclusions;
	
	public ElementExpression getContent() {
		return content;
	}
	public void setContent(ElementExpression content) {
		this.content = content;
	}
	public void println() {
		if(content!=null)
		{
			System.out.println("Content :");
			content.println(0);
		}
		if(plusExpression!=null)
		{
			System.out.println("Plus :" + plusExpression);
		}
		if(minusExpression!=null)
		{
			System.out.println("Minus :" + minusExpression);
		}
	}
	public ArrayList<String> getInclusions() {
		return inclusions;
	}
	public void setInclusions(ArrayList<String> inclusions) {
		this.inclusions = inclusions;
	}
	public ArrayList<String> getExclusions() {
		return exclusions;
	}
	public void setExclusions(ArrayList<String> exclusions) {
		this.exclusions = exclusions;
	}
	
	public void loadInclusionAndExclusion()
	{
		for (String cNormalizedName : plusExpression.replaceAll("[()]", "").split("[\\|&,]", -1)) {
			inclusions.add(cNormalizedName.trim());
		}
		
		for (String cNormalizedName : minusExpression.replaceAll("[()]", "").split("[\\|&,]", -1)) {
			exclusions.add(cNormalizedName.trim());
		}
	}
	public String getPlusExpression() {
		return plusExpression;
	}
	public String getMinusExpression() {
		return minusExpression;
	}
	public void setPlusExpression(String plusExpression) {
		this.plusExpression = plusExpression;
	}
	public void setMinusExpression(String minusExpression) {
		this.minusExpression = minusExpression;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
}
