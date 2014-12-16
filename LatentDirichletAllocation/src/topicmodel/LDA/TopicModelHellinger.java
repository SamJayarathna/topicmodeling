package topicmodel.LDA;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import cc.mallet.pipe.iterator.CsvIterator;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.topics.TopicInferencer;
import cc.mallet.types.Alphabet;
import cc.mallet.types.FeatureSequence;
import cc.mallet.types.IDSorter;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.LabelSequence;

/**
 * LDA Topic modeling framework. Includes model creation, document-topic and topic-term inference methods and model inference method for new sample text. 
 * For document-topic distribution, comparison is done using Hellinger Distance. 
 * @author Sampath
 *
 */

public class TopicModelHellinger extends TopicModel{
	private static TopicModelHellinger mStatsCompManager;
	static ArrayList<String> TopicWordList = null;
	static String topTopicWordList = null;
	InstanceList instances = null;

	public static String getTopTopicWordList()
	{
		return topTopicWordList;
	}

	private TopicModelHellinger(int numTopic){
		super(numTopic);
	}
	public static TopicModelHellinger getInstance(int numTopic){
		if(mStatsCompManager == null){
			mStatsCompManager = new TopicModelHellinger(numTopic);

		}else{
			System.out.println("Logger Present");
		}
		return mStatsCompManager;
	}

	public double computeMagnitude(ArrayList<Double> v1){
		double magnitude = 0;
		for (int i = 0; i < v1.size(); i++) {
			magnitude =  magnitude+v1.get(i)*v1.get(i);

		}

		return Math.sqrt(magnitude);


	}
	public double calculateDistance(ArrayList<Double> v1,ArrayList<Double> v2,int simType){
		double result =0;
		//simType HELLINGER = 1
		//simType COSINE = 2

		if(simType == 1){
			for(int i=0;i<v1.size();i++){
				double p1 = v1.get(i);
				double p2 = v2.get(i);
				result += (Math.sqrt(p1)- Math.sqrt(p2))*(Math.sqrt(p1)- Math.sqrt(p2));
			}
			result = Math.sqrt(result)/Math.sqrt(2);
		}else if (simType == 2){
			double v1Magn = computeMagnitude(v1);
			double v2Magn = computeMagnitude(v2);

			for (int i = 0; i < v1.size(); i++) {
				double p1 = v1.get(i);
				double p2 = v2.get(i);
				result += v1.get(i) * v2.get(i);
			}
			result = result/(v1Magn*v2Magn);

		}
		return result;


	}


	public void inferTopicTermDistribution(){
		// Show the words and topics in the first instance

		// The data alphabet maps word IDs to strings
		Alphabet dataAlphabet = instances.getDataAlphabet();

		FeatureSequence tokens = (FeatureSequence) model.getData().get(0).instance.getData();
		LabelSequence topics = model.getData().get(0).topicSequence;

		Formatter out = new Formatter(new StringBuilder(), Locale.US);
		for (int position = 0; position < tokens.getLength(); position++) {
			out.format("%s-%d ", dataAlphabet.lookupObject(tokens.getIndexAtPosition(position)), topics.getIndexAtPosition(position));
		}
		//System.out.println(out);

		// Estimate the topic distribution of the first instance, 
		//  given the current Gibbs state.
		double[] topicDistribution = model.getTopicProbabilities(0);

		//CLear first
		if (topicSortedWords != null)
			topicSortedWords.clear();
		// Get an array of sorted sets of word ID/count pairs
		topicSortedWords = model.getSortedWords();

		// Show top 5 words in topics with proportions for the first document
		StringBuffer ldaWordDist = new StringBuffer();
		TopicWordList = new ArrayList<String>();
		System.out.println ("\n\ntopic_# \t topic_term_probability_distribution");
		for (int topic = 0; topic < numTopics; topic++) {
			Iterator<IDSorter> iterator = topicSortedWords.get(topic).iterator();

			out = new Formatter(new StringBuilder(), Locale.US);
			out.format("%d\t%.3f\t", topic, topicDistribution[topic]);
			int rank = 0;
			StringBuffer ldaWordList = new StringBuffer();
			while (iterator.hasNext() && rank < 10) {
				IDSorter idCountPair = iterator.next();
				out.format("%s (%.0f) ", dataAlphabet.lookupObject(idCountPair.getID()), idCountPair.getWeight());
				rank++;
				ldaWordList.append(dataAlphabet.lookupObject(idCountPair.getID())+" ");
			}
			TopicWordList.add(ldaWordList.toString());
			ldaWordDist.append(out+"\n");

			System.out.println(out);

		}
	}


	public Map<String, ArrayList<Double>> inferDocumentTopicDistribution(){
		// Display the document topic probability distribtuion
		System.out.println ("\n\ndocument_# \tdocument_name \t topic_probability_distribution");
		int[] topicCounts = new int[ numTopics ];
		int docLen;


		IDSorter[] sortedTopics = new IDSorter[ numTopics ];
		for (int topic = 0; topic < numTopics; topic++) {
			// Initialize the sorters with dummy values
			sortedTopics[topic] = new IDSorter(topic, topic);
		}
		Map<String, ArrayList<Double>> targetVector = new HashMap<String, ArrayList<Double>>();

		for (int doc = 0; doc < model.data.size(); doc++) {
			LabelSequence topicSequence = (LabelSequence) model.data.get(doc).topicSequence;
			int[] currentDocTopics = topicSequence.getFeatures();

			StringBuilder builder = new StringBuilder();

			builder.append(doc);
			builder.append("\t");
			String docId = null;
			if (model.data.get(doc).instance.getName() != null) {
				docId = (String) model.data.get(doc).instance.getName(); 
			}
			else {
				docId = "no-name";

			}
			builder.append(docId);

			builder.append("\t");
			docLen = currentDocTopics.length;

			// Count up the tokens
			for (int token=0; token < docLen; token++) {
				topicCounts[ currentDocTopics[token] ]++;
			}

			// And normalize
			ArrayList<Double> targetProbDist = new ArrayList<Double>();
			for (int topic = 0; topic < numTopics; topic++) {
				sortedTopics[topic].set(topic, (model.alpha[topic] + topicCounts[topic]) / (docLen + model.alphaSum) );
				targetProbDist.add((model.alpha[topic] + topicCounts[topic]) / (docLen + model.alphaSum));// TODO: is this the topic probability
			}
			targetVector.put(docId,targetProbDist);

			Arrays.sort(sortedTopics);
			for (int i = 0; i < numTopics; i++) {
				builder.append(sortedTopics[i].getID() + "\t" + 
						sortedTopics[i].getWeight() + "\t");
			}
			System.out.println(builder);
			Arrays.fill(topicCounts, 0);
		}

		return targetVector;
	}

	public Map<String, ArrayList<Double>> calTargetTopicModel(String ldaInput) throws IOException
	{

		String ldaOut =null;
		if( targetInstances != null){
			clearTargetInstance();
		}
		instances = createTargetInstanceList();
		//System.out.println("Using Target Topic Model");

		ByteArrayInputStream inputStream = new ByteArrayInputStream(ldaInput.getBytes());
		Reader myReader = new InputStreamReader(inputStream);
		BufferedReader br = new BufferedReader(myReader);

		instances.addThruPipe(new CsvIterator (br, Pattern.compile("^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$"),
				3, 2, 1)); // data, label, name fields
		// Create a model with "numTopics" topics, alpha_t = 0.01, beta_w = 0.01
		//  Note that the first parameter is passed as the sum over topics, while
		//  the second is the parameter for a single dimension of the Dirichlet prior.

		if(model != null)
			model = null;
		model = new ParallelTopicModel(numTopics, 0.01*numTopics, 0.01);

		model.addInstances(instances);

		// Use two parallel samplers, which each look at one half the corpus and combine
		//  statistics after every iteration.
		model.setNumThreads(2);

		// Run the model for 50 iterations and stop (this is for testing only, 
		//  for real applications, use 1000 to 2000 iterations)
		model.setNumIterations(200);
		model.estimate();

		//---------Now get 2 distributions fro document-topic and topic-term from the model estimate
		Map<String, ArrayList<Double>> docTopicDistribution = inferDocumentTopicDistribution();
		inferTopicTermDistribution();

		return docTopicDistribution;

	}


	// This is the LDA Inferencer. If there's a new text needs to infer the topic model, use this method to infer both 
	// document-topic distribution and topic-term distribution
	public ArrayList<Double> calSrcTopicModel(String source){
		Alphabet dataAlphabet = targetInstances.getAlphabet();
		// Create a new instance with high probability of topic 0
		System.out.println("Source Text for LDA");
		System.out.println(source);
		// Create a new instance named "test instance" with empty target and source fields.
		InstanceList srcInstance = new InstanceList(targetInstances.getPipe());
		srcInstance.addThruPipe(new Instance(source, null, "Src Instance", null));

		TopicInferencer inferencer = model.getInferencer();
		double[] srcDisVector = inferencer.getSampledDistribution(srcInstance.get(0), 10, 1, 5); //TODO: Check these magic numbers are proper or not
		//System.out.println("length"+srcDisVector.length);
		double maxTopicDistribution = 0.0;
		int maxTopic = 0;
		for (int i = 0; i < srcDisVector.length; i++) {
			if(maxTopicDistribution <= srcDisVector[i]){
				maxTopicDistribution = srcDisVector[i];
				maxTopic = i;
			}
			System.out.println(i+"\t" + srcDisVector[i]);
		}
		System.out.println("Source Document Probability distribution Done. Most similar Topic Distribution for soruce:"+maxTopic);
		System.out.println("Most similar word list for soruce: "+TopicWordList.get(maxTopic));
		topTopicWordList = TopicWordList.get(maxTopic);
		ArrayList<Double> srcVector = new ArrayList<Double>(Arrays.asList(boxedArray(srcDisVector)));
		return srcVector;
	}

}
