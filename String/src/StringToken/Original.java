package StringToken;

import java.io.*;


public class Original{

	public static void main(String[] arg) throws Exception {

		String[] tweet = parse("Good luck to all students writing exams ! Look on the bright side, holidays are coming up, you just have to get through exams first! =P");
		//String[] tweet = parse("LOL! YES! RT @realmumrocks @KiSS925 @BlakesShow @AdamWylde   Creepy? Like 'I'll climb on you and give you a massage' creepy? #BWBach");
		//String[] tweet = parse("@rdrake1  Wow - the y axis is just nominal data?  What a terrible chart!  Thanks!");


		BufferedReader CSVFile = null ;

		System.out.println("////////////////");
		System.out.println("compared words");

		for(String compare:tweet){

			String porterRes = null;

			// Resets the boolean to false for each run
			boolean found1,found2;
			found1 = found2 = false;

			// loads the text file for each run
			CSVFile = new BufferedReader(new FileReader("C:/Users/100428653/Documents/GitHub/String/data/NRCemotionlexicon.txt"));


			String dataRow = CSVFile.readLine(); // Read the first line of data.
			// The while checks to see if the data is null. If it is, we've hit
			//  the end of the file. If not, process the data.

			System.out.println(compare);   	// printing current word in the search
			Porter porterStemmer = new Porter();
			porterRes = porterStemmer.stripAffixes(compare);
			System.out.println("After porter: " + porterRes);

			while (dataRow != null){
				String[] dataArray = dataRow.split(",");

				if (dataArray[0].equals(compare)){
					for (String item:dataArray) { System.out.print(item + "\t"); } // prints contents fo the line
					System.out.println(); // prints new line
					found1 = true;
				}else if (dataArray[0].equals(porterRes)){
					for (String item:dataArray) { System.out.print(item + "\t"); } // prints contents of the line
					System.out.println(); // prints new line
					found2 = true;
					
				}


				dataRow = CSVFile.readLine(); // Read next line of data.

			}// end while

			if (found1 == false){ System.out.println(compare + " was not found 1st try");} //displays when current word is not found
			if (found2 == false){ System.out.println(porterRes + " was not found 2nd try");} //displays when current word is not found
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
