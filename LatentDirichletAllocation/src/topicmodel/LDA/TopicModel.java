package topicmodel.LDA;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Pattern;

import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.CharSequenceLowercase;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;
import cc.mallet.pipe.iterator.CsvIterator;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.topics.TopicInferencer;
import cc.mallet.types.Alphabet;
import cc.mallet.types.FeatureSequence;
import cc.mallet.types.IDSorter;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.LabelSequence;

public abstract class TopicModel {

	protected static String LDAFILE =  "LDA_INPUT.txt";
	String fs = System.getProperty("file.separator");
	//String LOG_DIR = System.getProperty("user.home") + fs + "IPMResults";
	protected String workingDirectory = System.getProperty("user.dir");
	protected String ldaDirectory = workingDirectory + fs + "data" ; 
	protected File  mLdaInputDir = null;
	protected File  mLdaInputFile = null;
	PrintWriter mLDAWriter = null;
	protected FileWriter fwriter = null;
	protected boolean bConfigDone = false;
	
	ArrayList<Pipe> pipeList = null;
	InstanceList targetInstances = null;
	InstanceList srcInstances = null;
	ArrayList<TreeSet<IDSorter>> topicSortedWords = null;
	ParallelTopicModel model = null;
	int numTopics = 5;
	
	protected TopicModel(int maxTopic){
		configModelInput();
		numTopics = maxTopic;
	}
	private void configModelInput(){
		try {
			mLdaInputDir = new File(workingDirectory);
			if (!mLdaInputDir.exists()) {
				mLdaInputDir.mkdir();
			}			
			mLdaInputFile = new File(ldaDirectory + fs+ LDAFILE);
			mLDAWriter =  new PrintWriter(new FileWriter(mLdaInputFile,true));
	        //srcInstances = new InstanceList (new SerialPipes(pipeList));
	        pipeList = new ArrayList<Pipe>();
	        createTargetInstanceList();
		}catch (IOException e) {
				e.printStackTrace();
			}
		
	}
	
	protected InstanceList createTargetInstanceList(){
		
		pipeList.add(new CharSequenceLowercase());
		pipeList.add( new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")) );
        pipeList.add( new TokenSequenceRemoveStopwords(new File(ldaDirectory + fs+"en.txt"), "UTF-8", false, false, false) );
        pipeList.add( new TokenSequence2FeatureSequence() );
        targetInstances = new InstanceList (new SerialPipes(pipeList));
        return targetInstances;
        
	}
	protected Double[] boxedArray(double[] array) {
		Double[] result = new Double[array.length];
		for (int i = 0; i < array.length; i++)
			result[i] = array[i];
		return result;
	}
	
	protected void clearTargetInstance(){
		pipeList.clear();
        targetInstances = null;
	}
	
	public void stopLDAUpdate(){
		mLDAWriter.close();
	}
	
	//This is just a parent class for both types of TopicModel_Similarity computation Class
	public String updateLDAFile(String docId,String weight,String content){
		StringBuilder ldaContent = new StringBuilder();
		ldaContent.append(docId);
		ldaContent.append("  ");
		ldaContent.append(weight);
		ldaContent.append("  ");
		ldaContent.append(content+"\r\n");
		//System.out.println("LDA Content>>>>: "+ldaContent.toString());
		return ldaContent.toString();
	}
	
	abstract public Map<String, ArrayList<Double>> calTargetTopicModel(String ldaInput) throws IOException;
	abstract public ArrayList<Double> calSrcTopicModel(String source);
	abstract public double calculateDistance(ArrayList<Double> v1,ArrayList<Double> v2,int simMetric);	

}
