package inference.manager;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import cc.mallet.grmm.learning.templates.SimilarTokensTemplate;
import topicmodel.LDA.*;
import utils.HTMLParser;
import utils.csvreader.CsvWriter;
import utils.data.CommonUtils;

public class LDAInferenceManager {
	private TopicModel mTopicModel = null;
	private int numberOfTopics = 4; // default number of topics
	private int mSimMetric = 1; // default distance type = Hellinger
	private double similarityThreshold = 0.5; // default distance type = Hellinger
	String fs = CommonUtils.fs;
	String workingDirectory = CommonUtils.workingDirectory;
	String dataDirectory = CommonUtils.dataDirectory;
	StringBuilder LDAInput = null;
	HashMap<String, ArrayList<Double>> documentTopicDist = null;
	static HashMap<String,String> similarInvalidPages = new HashMap<String,String>();
	
	HashMap<String,String> invalidPages = null;
	HashMap<String,String> validPages = null;
	
	public LDAInferenceManager(){
		mTopicModel = TopicModelHellinger.getInstance(numberOfTopics); // define the number of Topics
		LDAInput = new StringBuilder();
		
	}
	
	public void getPageData(){
		String invalid = dataDirectory + fs + "invalid";
		String valid = dataDirectory + fs + "valid";
		invalidPages = HTMLParser.readFiles(invalid,"invalid");
		validPages = HTMLParser.readFiles(valid,"valid");
		
		Iterator validIt = validPages.entrySet().iterator();
		while(validIt.hasNext()){
			Map.Entry<String,String> pairs = (Map.Entry<String,String>)validIt.next();
			LDAInput.append(mTopicModel.updateLDAFile(pairs.getKey(), "X", pairs.getValue()));
		}
		
		Iterator invalidIt = invalidPages.entrySet().iterator();
		while(invalidIt.hasNext()){
			Map.Entry<String,String> pairs = (Map.Entry<String,String>)invalidIt.next();
			LDAInput.append(mTopicModel.updateLDAFile(pairs.getKey(), "X", pairs.getValue()));
		}
		
	}
	
	public void getTopicModel(){
		System.out.println("\n\n");
		System.out.println("****************************************************");
		System.out.println("*       Latent Dirichlet Allocation (LDA)          *");
		System.out.println("****************************************************");
		
		try {
			documentTopicDist = (HashMap)mTopicModel.calTargetTopicModel(LDAInput.toString());

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void calculateSimilarity(String fileLbl){
		
		CsvWriter simFile = null;
		String fileCSVPath = dataDirectory + fs + "LDA-similarity"+fileLbl+".txt"; 
		simFile = new CsvWriter(fileCSVPath);
		try {
			simFile.write("Similarity");
			simFile.write("Similarity-Flag");
			simFile.write("Valid-Page");
			simFile.write("Inavlid-Page");
			simFile.endRecord();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		System.out.println("\n\n");
		System.out.println("****************************************************");
		System.out.println("*  Latent Dirichlet Allocation (LDA) Similarity    *");
		System.out.println("****************************************************");
		
		Iterator validIt = validPages.entrySet().iterator();
		while(validIt.hasNext()){
			Map.Entry<String,String> valPairs = (Map.Entry<String,String>)validIt.next();
			ArrayList<Double> validPageDist =  documentTopicDist.get(valPairs.getKey());
			
			Iterator invalidIt = invalidPages.entrySet().iterator();
			while(invalidIt.hasNext()){
				Map.Entry<String,String> invalPairs = (Map.Entry<String,String>)invalidIt.next();
				ArrayList<Double> invalidPageDist = documentTopicDist.get(invalPairs.getKey());
				
				double simScore = 1- mTopicModel.calculateDistance(validPageDist, invalidPageDist,mSimMetric);
				int similarityFlag = 0;
				if(simScore >= similarityThreshold){
					similarityFlag = 1;
					String invalidPage = invalPairs.getKey().replace("invalid-", ""); // remove additional flag from the page name
					similarInvalidPages.put(invalidPage, invalidPage); // add this to a map and use it for further enhancements
				}
				System.out.println(valPairs.getKey()+" / "+ invalPairs.getKey()+" similarity:"+simScore);
				try {
					simFile.write(Double.toString(simScore));
					simFile.write(Integer.toString(similarityFlag)); // if 0 then not similar, if ==1 then similar
					simFile.write(valPairs.getKey());
					simFile.write(invalPairs.getKey());
					simFile.endRecord();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}
		
		simFile.close();
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
			/*LDAInferenceManager ldaManager = new LDAInferenceManager();
			ldaManager.getPageData();
			ldaManager.getTopicModel();
			ldaManager.calculateSimilarity("");*/
			
			// Now run again to load outlink text from the similar invalid pages to further analysis 
			LDAInferenceManager ldaManager2 = new LDAInferenceManager();
			int mapsize = similarInvalidPages.size();
			ldaManager2.getPageData();
			ldaManager2.getTopicModel();
			ldaManager2.calculateSimilarity("-Outlinks");
		}

}
