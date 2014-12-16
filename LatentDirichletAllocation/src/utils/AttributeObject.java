package utils;

import java.util.ArrayList;

/**
 * Thus class usually represents a document. The id is generally a document URL.
 * The class holds a list of AttributeCollections.
 * @author sam
 *
 */

public class AttributeObject {

	private String m_id; // url 
	private String wholeDocTextContent;
	private int numberOfOutlinks; // number of outlinks from the base page
	private String classificaiton;
	private String parentURL;
	private String linkType;
	private ArrayList<String> allOutlinkText;
	private int numberOfImages;
	private String metaDescription;
	private String metaKeywords;
	private String host;
	private String title;
	private int imports;
	
	public AttributeObject(){
		this.m_id = "";
		this.wholeDocTextContent="";
		this.numberOfOutlinks = -1;
		this.classificaiton = "";
		this.parentURL = "";
		this.linkType ="";
		this.numberOfImages = -1;
		this.metaDescription = "";
		this.metaKeywords = "";
		this.host = "";
		this.title ="";
		this.imports = -1;
		
	}
	
	public void setImports(int numberOfImports){
		this.imports = numberOfImports;
	}
	
	public int getImports(){
		return this.imports;
	}
	
	public void setPageTitle(String title){
		this.title = title;
	}
	
	public String getPageTitle(){
		return this.title;
	}
	
	public void setPageHost(String host){
		this.host = host;
	}
	
	public String getPageHost(){
		return this.host;
	}
	
	public void setMetaDescription(String description){
		this.metaDescription = description;
	}
	
	public String getMetaDescription(){
		return this.metaDescription;
	}
	
	public void setMetaKeywords(String keywords){
		this.metaKeywords = keywords;
	}
	
	public String getMetaKeywords(){
		return this.metaKeywords;
	}
	
	
	public void setParentURL(String parent){
		this.parentURL = parent;
	}
	
	public String getParentURL(){
		return this.parentURL;
	}
	
	public void setLinkType(String type){
		this.linkType = type;
	}
	
	public String getLinkType(){
		return this.linkType;
	}
	
	public void setAllOutlinkText(ArrayList<String> outlinks){
		this.allOutlinkText = outlinks;
	}
	
	public ArrayList<String> getAllOutlinkText(){
		return this.allOutlinkText;
	}
	
	public void setId(String id) {
		m_id = id.replace(" ", "").trim();
	}

	public String getId() {
		return m_id;
	}
	
	public void setClassification(String pageLable){
		this.classificaiton = pageLable;
	}
	
	public String getClassification(){
		return this.classificaiton;
	}
	
	public void setWholeDocTextContent(String textContent) {
		this.wholeDocTextContent = textContent;
	}
	
	public String getWholeDocTextContent() {
		return this.wholeDocTextContent;
	}
	
	public void setNumberOfOutlinks(int numLinks){
		this.numberOfOutlinks = numLinks;
	}
	
	public int getNumberOfOutlinks(){
		return this.numberOfOutlinks;
	}
	
	public void setNumberOfImages(int numImages){
		this.numberOfImages = numImages;
	}
	
	public int getNumberOfImages(){
		return this.numberOfImages;
	}
}
