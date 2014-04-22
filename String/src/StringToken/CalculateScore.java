package StringToken;

import java.util.*;

public class CalculateScore {

	static public int count = 0;
	List<Integer> tweetScore; 
	Map<String, List<Integer>> lexicon;
	Map<String, String> cache; // Key = original word. value = modified word. stores portered words
	Porter porterStemmer;

	public CalculateScore() throws Exception{
		lexicon = new Hashmap().getHM(); // assigns the hashmap
		cache = new HashMap(); // Key = original word. value = modified word. stores portered words
		porterStemmer = new Porter(); // porter object
	}

	public List<Integer> getScore(String s) throws Exception{
		String[] tweetCompare = parse(s);

		// create tweet score and set to 0
		tweetScore = new ArrayList<Integer>();
		for (int i = 0; i < 10; i ++){
			tweetScore.add(0);
		}

		for(String compare: tweetCompare){

			String porterRes = null; // stores the current porter results

			if (lexicon.containsKey(compare)){ 
				setScore(compare);
			}// lexicon compare
			else{
				if (cache.containsKey(compare)){
					setScore(cache.get(compare));
				}else{
					porterRes = porterStemmer.stripAffixes(compare);
					cache.put(compare,porterRes); // add word to cache
					setScore(porterRes);
				}// end porterRes if
			}// end compare else
			count++;
		}
		return tweetScore;
	} // end getScore


	public static String[] parse(String words){


		String s = words;
		String delims = "[ *.,?!#@]+";
		String[] tokens = s.split(delims);

		// converts strings to lower case
		for(int i = 0; i<tokens.length; i++){ tokens[i] = tokens[i].toLowerCase();} //converts all strings to loser case

		//System.out.println("////////////////");
		//System.out.println("Printing parsed string:");
		//for(String t:tokens){System.out.println(t);} // debugging purposes

		return tokens;
	}

	private void setScore(String word){
		if (lexicon.containsKey(word)){
			for (int i = 0; i < 10; i ++){
				tweetScore.set(i, tweetScore.get(i) + lexicon.get(word).get(i));
			}
		}
	}// end getScore
}// end class
