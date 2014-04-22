package StringToken;

import java.io.*;


public class Read{

	public static void main(String[] arg) throws Exception {

		String[] tweet = parse("LOL! YES! RT @realmumrocks @KiSS925 @BlakesShow @AdamWylde   Creepy? Like 'I'll climb on you and give you a massage' creepy? #BWBach");

		BufferedReader CSVFile = null ;

		System.out.println("////////////////");
		System.out.println("compared words");

		for(String compare:tweet){

			// Resets the boolean to false for each run
			boolean found = false;  

			if (compare.length() != 1){

				// loads the text file for each run
				CSVFile = new BufferedReader(new FileReader("C:/Users/100428653/Documents/GitHub/String/data/NRCemotionlexicon.txt"));


				String dataRow = CSVFile.readLine(); // Read the first line of data.
				// The while checks to see if the data is null. If it is, we've hit
				//  the end of the file. If not, process the data.

				System.out.println(compare);   	// printing current word in the search
				//int count = 0;  
				if (true){
					
					while (dataRow != null){
						String[] dataArray = dataRow.split(",");

						if (dataArray[0].equals(compare)){
							//count ++; 
							//System.out.println(compare + ":" + count + ":" + dataArray[0]);
							for (String item:dataArray) { System.out.print(item + "\t"); } // prints contents fo the line
							System.out.println(); // prints new line
							found = true;
						}


						dataRow = CSVFile.readLine(); // Read next line of data.
					} // if that checks word length 
				}// end while
			}
			if (found == false){ System.out.println(compare + " was not found");} //displays when current word is not found
			System.out.println();
		}// end search for loop 

		// Close the file once all data has been read.
		CSVFile.close(); 

		// End the printout with a blank line.
		System.out.println("Done...");

	} //end main()


	public static String[] parse(String words){


		String s = words;
		String delims = "[ *.,?!#@]+";
		String[] tokens = s.split(delims);

		// converts strings to lower case
		for(int i = 0; i<tokens.length; i++){ tokens[i] = tokens[i].toLowerCase();} //converts all strings to loser case

		System.out.println("////////////////");
		System.out.println("Printing parsed string:");
		for(String t:tokens){System.out.println(t);} // debugging purposes

		return tokens;

	}


} // Read
