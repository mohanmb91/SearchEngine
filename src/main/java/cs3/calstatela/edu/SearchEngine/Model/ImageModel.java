package cs3.calstatela.edu.SearchEngine.Model;

public class ImageModel {
	
	public String getSrc() {
		return src;
	}
	public void setSrc(String src) {
		this.src = src;
	}
	public String getHeight() {
		return height;
	}
	public void setHeight(String height) {
		this.height = height;
	}
	public String getWidth() {
		return width;
	}
	public void setWidth(String width) {
		this.width = width;
	}
	public String getAlt() {
		return alt;
	}
	public void setAlt(String alt) {
		this.alt = alt;
	}
	public ImageModel(){}
	public ImageModel(String src,String height,	String width,	String alt ){
		this.src = src;
		this.height = height;
		this.width = width;
		this.alt = alt;
	}
	String src;
	String height;
	String width;
	String alt;
}
