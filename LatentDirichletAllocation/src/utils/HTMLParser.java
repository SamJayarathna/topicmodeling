package utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileReader;
import java.io.Reader;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import utils.csvreader.CsvWriter;

public class HTMLParser {
	static String fs = System.getProperty("file.separator");
	static String workingDirectory = System.getProperty("user.dir");
	static String dataDirectory = workingDirectory + fs + "data";
	
	private List<AttributeObject> m_attributeObjectPool;
	
	private String wholePageText = "";
	private boolean addOutLinkText = false;
	private static ArrayList<String> allOutlinkText;
	private static String currentClassification;
	private static String currentParentURL;
	
	private HTMLParser() {}

	/**
	 * This function extract all the features from the given pages and write them back to external csv file. 
	 * Each feature value is recorded in the AttributeObject. 
	 * @param reader
	 * @param m_attributeObject
	 * @return
	 * @throws IOException
	 */
	public static ArrayList<String> extractFeatures(Reader reader, AttributeObject m_attributeObject) throws IOException {
		
		StringBuilder sb = new StringBuilder();
		BufferedReader br = new BufferedReader(reader);
		String line;
		while ( (line=br.readLine()) != null) {
			sb.append(line);
		}
		Document doc = Jsoup.parse(sb.toString());
		m_attributeObject.setWholeDocTextContent(doc.text());
		
		Elements links = doc.select("a[href]");
		ArrayList<String> linkList = new ArrayList<String>();
		
		
		allOutlinkText  = new ArrayList<String>();
		for(Element link: links){
			//String linkText = link.html();
			String outlink = link.attr("abs:href");
			if(!outlink.isEmpty()){
				System.out.println("extarcting outlink....."+ outlink);
				linkList.add(link.attr("abs:href"));
				//allOutlinkText.add(extractFeaturesFromURL(link.attr("abs:href")));
			}
		}
		//m_attributeObject.setAllOutlinkText(allOutlinkText);
		m_attributeObject.setNumberOfOutlinks(linkList.size()); // outlinks - 1. Remove the base link of this page
		System.out.println("total no of links:"+ linkList.size());
		
		HTMLElements htmlElements = new HTMLElements(doc);
		m_attributeObject.setNumberOfImages(htmlElements.getImages());
		m_attributeObject.setPageTitle(htmlElements.getPageTitle());
		m_attributeObject.setMetaDescription(htmlElements.getMetaDescription());
		m_attributeObject.setMetaKeywords(htmlElements.getMetaKeywords());
		m_attributeObject.setImports(htmlElements.getImports()); // stylesheets, shortcut icons
		
		return linkList;
	}
	
	/**
	 * This function gets features from given absolute URL. Also print these features to file
	 * @param url
	 * @return
	 * @throws IOException
	 */
	public static String extractFeaturesFromURL(String url){

		System.out.println("Extracting text from outlink....."+ url);
		Document doc;
		String textOnly = "";
		try {
			AttributeObject outlink_attributeObject = new AttributeObject();
			outlink_attributeObject.setId(url);
			outlink_attributeObject.setClassification(currentClassification); // each folder name is the known classificaiton. Put each file to correcttly classified folder.
			outlink_attributeObject.setLinkType("outlink");
			outlink_attributeObject.setParentURL(currentParentURL);
			
			doc = Jsoup.connect(url).get();
			textOnly = doc.text();
			
			outlink_attributeObject.setWholeDocTextContent(textOnly);
			Elements links = doc.select("a[href]");
			ArrayList<String> linkList = new ArrayList<String>();
			outlink_attributeObject.setNumberOfOutlinks(links.size()); // check whether this includes base outlink
			
			HTMLElements o_htmlElements = new HTMLElements(doc);
			outlink_attributeObject.setNumberOfImages(o_htmlElements.getImages());
			
			outlink_attributeObject.setMetaDescription(o_htmlElements.getMetaDescription());
			outlink_attributeObject.setMetaKeywords(o_htmlElements.getMetaKeywords());
			outlink_attributeObject.setPageHost(o_htmlElements.getHost(url));
			outlink_attributeObject.setPageTitle(o_htmlElements.getPageTitle());
			outlink_attributeObject.setImports(o_htmlElements.getImports());

			printFeatureProfile(outlink_attributeObject); // now print the features of this outlink
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return textOnly;
	}
	
	/**
	 * This function gets all the text from given external file Reader
	 * @param reader
	 * @return
	 * @throws IOException
	 */
	public static String extractText(Reader reader) throws IOException {
		StringBuilder sb = new StringBuilder();
		BufferedReader br = new BufferedReader(reader);
		String line;
		while ( (line=br.readLine()) != null) {
			sb.append(line);
		}
		Document doc = Jsoup.parse(sb.toString());
		String textOnly = doc.text();
		
		return textOnly;
	}

	/**
	 * This function gets all the text from given absolute URL
	 * @param url
	 * @return
	 * @throws IOException
	 */
	public static String extractText(String url) throws IOException {
		
		System.out.println("Extracting text from outlink....."+ url);
		Document doc = Jsoup.connect(url).get();
		String textOnly = doc.text();
			
		return textOnly;
	}
	
	public static ArrayList<String> extractLinks(Reader reader) throws IOException {
		
		StringBuilder sb = new StringBuilder();
		BufferedReader br = new BufferedReader(reader);
		String line;
		while ( (line=br.readLine()) != null) {
			sb.append(line);
		}
		Document doc = Jsoup.parse(sb.toString());
		Elements links = doc.select("a[href]");
		ArrayList<String> linkList = new ArrayList<String>();
		for(Element link: links){
			//String linkText = link.html();
			String outlink = link.attr("abs:href");
			if(!outlink.isEmpty()){
				System.out.println("extarcting outlink....."+ outlink);
				linkList.add(link.attr("abs:href"));
			}
		}
		System.out.println("total no of links:"+ linkList.size());
		return linkList;
	}
	
	/**
	 * This will read all the files in the specified directory and rename them using the given label. 
	 * @param dataDirectory, path to data directory 
	 * @param lbl= "invalid" or "valid"
	 * @return
	 */
	public static HashMap<String,String> readFiles(String dataDirectory, String lbl){
		File folder = new File(dataDirectory);
		File[] listOfFiles = folder.listFiles();
		
		HashMap<String,String> htmlPageMap = new HashMap<String,String>();
		
		for(File file: listOfFiles){
			if(file.isFile()){
				System.out.println("Reading file....."+file.getName());
				FileReader reader;
				try {
					reader = new FileReader(file);
					String pageText = HTMLParser.extractText(reader);
					htmlPageMap.put(lbl+"-"+file.getName(), pageText);
					//System.out.println(pageText);
					reader.close();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}
		
		return htmlPageMap;
	}
	
	
	/**
	 * This will read all the files in the specified directory and rename them using the given label. Also add the text from the out links pages
	 * to the HashMap
	 * @param dataDirectory, path to data directory 
	 * @param lbl= "invalid" or "valid"
	 * @param linkFlag = flag set to true if the text from all the links added to the text, false if no text added from the links
	 * @return
	 */
	public static HashMap<String,String> readFiles(String dataDirectory, String lbl, boolean linkFlag, HashMap<String,String> similarInvalidPages){
		File folder = new File(dataDirectory);
		File[] listOfFiles = folder.listFiles();
		
		HashMap<String,String> htmlPageMap = new HashMap<String,String>();
		
		for(File file: listOfFiles){
			if(file.isFile()){
				System.out.println("Reading file....."+file.getName());
				FileReader reader;
				try {
					reader = new FileReader(file);
					String pageText = HTMLParser.extractText(reader);
					StringBuffer linkTextBuffer = new StringBuffer();
					linkTextBuffer.append(pageText+" ");// add base page text first. 
					if(linkFlag && similarInvalidPages.containsKey(file.getName())){
						FileReader linkReader = new FileReader(file);
						ArrayList<String> linkList = HTMLParser.extractLinks(linkReader);
						linkReader.close();
						// Now add the text from outlinks from the base page. 
						int linklistsize = linkList.size();
						for(int i=1;i<linkList.size();i++){
							String linkText = extractText(linkList.get(i)); // avoid i=0 which is the base url which text is already extracted. 
							linkTextBuffer.append(linkText+" ");
							if(i==21) // check only upto 20 links
								break;
						}
					}
					
					htmlPageMap.put(lbl+"-"+file.getName(), linkTextBuffer.toString().trim());
					//System.out.println(pageText);
					reader.close();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}
		
		return htmlPageMap;
	}
	
	
	/**
	 * This will read all the files in the specified directory and rename them using the given label. Also add the text from the out links pages
	 * to the HashMap
	 * @param dataDirectory, path to data directory 
	 * @param lbl= "invalid" or "valid"
	 * @param linkFlag = flag set to true if the text from all the links added to the text, false if no text added from the links
	 * @return
	 */
	public static HashMap<String,String> readFiles(String dataDirectory, String lbl, boolean linkFlag){
		File folder = new File(dataDirectory);
		currentClassification = folder.getName();
		
		File[] listOfFiles = folder.listFiles();
		
		HashMap<String,String> htmlPageMap = new HashMap<String,String>();
		
		for(File file: listOfFiles){
			if(file.isFile()){
				System.out.println("Reading file....."+file.getName());
				
				AttributeObject m_attributeObject = new AttributeObject();
				m_attributeObject.setId(file.getName());
				currentParentURL = file.getName();
				m_attributeObject.setClassification(currentClassification); // each folder name is the known classificaiton. Put each file to correcttly classified folder.
				m_attributeObject.setLinkType("baselink");
				
				FileReader reader;
				try {
					reader = new FileReader(file);
					HTMLParser.extractFeatures(reader,m_attributeObject);
					
					StringBuffer linkTextBuffer = new StringBuffer();
					linkTextBuffer.append(m_attributeObject.getWholeDocTextContent()+" ");// add base page text first.

					if(linkFlag){
						// Now add the text from outlinks from the base page. 
						ArrayList<String> allOutlinks = m_attributeObject.getAllOutlinkText();
						for(int i=1;i< allOutlinks.size();i++){  // avoid i=0 which is the base url which text is already extracted. 
							linkTextBuffer.append(allOutlinks.get(i)+" ");
						}
					}
					
					htmlPageMap.put(lbl+"-"+file.getName(), linkTextBuffer.toString().trim());
					//System.out.println(pageText);
					reader.close();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				printFeatureProfile(m_attributeObject);
			}
			
		}
		
		return htmlPageMap;
	}
	
	public static void printFeatureProfile(AttributeObject attributeObj){
		CsvWriter simFile = null;
		String fileCSVPath = dataDirectory + fs + "featureList.csv"; 
		boolean alreadyExists = new File(fileCSVPath).exists();
		try {
			simFile = new CsvWriter(new FileWriter(fileCSVPath,true),',');
			if(!alreadyExists){
				simFile.write("Id");
				simFile.write("Classification");
				simFile.write("Link_type"); // baselink or outlink
				simFile.write("Parent_URL");
				simFile.write("Number_of_outlinks");
				simFile.write("Number_of_images");
				simFile.write("Meta_description");
				simFile.write("Meta_keywords");
				simFile.write("host");
				simFile.write("Title");
				simFile.write("Imports");
				simFile.write("Page_text");
				simFile.endRecord();
			}

			simFile.write(attributeObj.getId());
			simFile.write(attributeObj.getClassification());
			simFile.write(attributeObj.getLinkType());
			simFile.write(attributeObj.getParentURL());
			simFile.write(Integer.toString(attributeObj.getNumberOfOutlinks()));
			simFile.write(Integer.toString(attributeObj.getNumberOfImages()));
			simFile.write(attributeObj.getMetaDescription());
			simFile.write(attributeObj.getMetaKeywords());
			simFile.write(attributeObj.getPageHost());
			simFile.write(attributeObj.getPageTitle());
			simFile.write(Integer.toString(attributeObj.getImports()));
			simFile.write(attributeObj.getWholeDocTextContent());
			simFile.endRecord();
			simFile.close();	
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		 
	}
	
	/**
	 * This will read a single file in the specified directory.
	 * @param dataDirectory, path to file in the specific directory
	 * @return
	 */
	public static void readFiles(String dataFile){
		File file = new File(dataFile);

		if(file.isFile()){
			System.out.println("Reading file....."+file.getName());
			FileReader reader;
			try {
				reader = new FileReader(file);
				//String pageText = HTMLParser.extractText(reader);
			    
				//htmlPageMap.put(lbl+"-"+file.getName(), pageText);
				//System.out.println(pageText);
				ArrayList<String> linkLists = HTMLParser.extractLinks(reader);
				
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
 
	}
	
	public final static void main(String[] args) throws Exception{
		
		//String fileName = dataDirectory + fs + "invalid" + fs + "cmqk3l70unth8owz4.html";
		String invalid = dataDirectory + fs + "invalid";
		String valid = dataDirectory + fs + "valid";
		readFiles(valid,"valid",false);
		//readFiles(fileName);
		
		
	}
}
