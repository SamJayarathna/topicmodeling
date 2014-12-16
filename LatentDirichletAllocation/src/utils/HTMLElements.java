package utils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class HTMLElements {
	Document doc;
	
	public HTMLElements(Document htmlDoc){
		this.doc = htmlDoc;
	}
	
	public int getImages(){
		Elements images = doc.select("img[src~=(?i)\\.(png|jpe?g|gif)]");
		return images.size();
	}
	
	public int getImports(){
		int imports = 0;
		Elements stylesheets = doc.select("link[href]");
		if(stylesheets.size()>0)
			imports = stylesheets.size();

		return imports;
	}
	
	public String getMetaDescription(){
		//get meta description content
		String	metaDescription = "";
		Elements description = doc.select("meta[name=description]");
		if(description.size()>0){
			metaDescription = doc.select("meta[name=description]").get(0).attr("content");
			//System.out.println("Meta description : " + metaDescription);
		}		
		return metaDescription;
	}
	
	public String getMetaKeywords(){
		
		String	metaKeywords = "";
		//get meta keyword content
		Elements keywords = doc.select("meta[name=keywords]");
		if(keywords.size()>0){
			metaKeywords = doc.select("meta[name=keywords]").first().attr("content");
		}
		return metaKeywords;
	}
	
	public String getHost(String url){
		String host ="";
		
		try {
			URL pageURL = new URL(url);
			host = pageURL.getHost();
			//System.out.println(host);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return host;
	}
	
	public String getPageTitle(){
		String title ="";
		if(!doc.title().isEmpty())
			title = doc.title();
		
		return  title;
	}
	
	public String getLastModifed(String url){
		String lastModified ="";
		
		try {
			URL pageURL = new URL(url);
			URLConnection connection;
			connection = pageURL.openConnection();
			System.out.println("Header = " + connection.getHeaderField("Last-Modified"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return lastModified;
		
	}
	
}
