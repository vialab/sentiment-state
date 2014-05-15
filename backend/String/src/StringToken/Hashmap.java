/**
 * @author Taurean Scantlebury
 * @version 1.0
 */

package StringToken;

import java.util.*;
import java.io.*;

public class Hashmap {

	private Map<String, List<Integer>> hm; // main HashMap that stores the words and their emotion score


	/**
	 * Creates a HashMap of the file NRCemotionlexicon.txt
	 * key<String> = word
	 * value<List<Integer>> = emotion score
	 * @throws Exception
	 */
	@SuppressWarnings("resource")
	public Hashmap() throws Exception{
		String[] dataArray; 	// the split line from lexicon
		List<Integer> score; 	// emotion score of a word
		BufferedReader CSVFile; // reads lexicon
		InputStream rsc;
		String dataRow; 		// row read in from the lexicon

		hm = new HashMap<String, List<Integer>>();
		rsc = this.getClass().getResourceAsStream("NRCemotionlexicon.txt");
		CSVFile = new BufferedReader(new InputStreamReader(rsc));
		dataRow = CSVFile.readLine(); // Read the first line of data.
		// loops through each line of the lexicon, 
		// storing the word and the score in the HashMap 
		while (dataRow != null){

			dataArray = dataRow.split(",");
			score = new ArrayList<Integer>();

			// retrieves the score of the current word
			// storing it in the score list
			for (int i = 1; i < 11; i++){
				try{
					score.add(Integer.parseInt(dataArray[i].split(":")[1]));
				}catch (Exception e){
					System.out.println("There was an error saving the score for '" + dataArray[0] + "'");
				}
			} // end for


			hm.put(dataArray[0], score);
			dataRow = CSVFile.readLine(); // Read next line of data.


		}  // end while

	}// end class Hashmap
	
	/**
	 * returns a hashmap of words and their emotion score
	 * @return hm
	 */
	public Map<String, List<Integer>> getHM() {
		return hm;
	}

	public static void main(String[] arg) throws Exception {

		Map<String, List<Integer>> gh = new HashMap<String, List<Integer>>();
		Hashmap e = new Hashmap();
		gh = e.getHM();

		
		
//		String[] test = {"hello","tables", "soil", "studentsS"};
//
//		for(String compare: test){
//			if (gh.containsKey(compare)){
//				System.out.println(compare + ":Match found");
//			}else
//				System.out.println(compare + ":Match Not found");
//		}

				for (Map.Entry<String, List<Integer>> entry : gh.entrySet()) {
		
					String key = entry.getKey();
		
					List<Integer> values = entry.getValue();
		
					System.out.println("Key = " + key);
		
					System.out.println("Values = " + values);
					System.out.println();
		
				}// end for
	}// end main
}
