package sasgml.com.catalog;
public class CatalogItem {
	private String id;
	private String type;
	private String uri;
	public CatalogItem(String id, String type, String uri) {
		super();
		this.id = id;
		this.type = type;
		this.uri = uri;
	}
	public String getId() {
		return id;
	}
	public String getType() {
		return type;
	}
	public String getUri() {
		return uri;
	}
	public void setId(String id) {
		this.id = id;
	}
	public void setType(String type) {
		this.type = type;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
}
