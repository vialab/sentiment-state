package StringToken;

public class Token {

	public static void main(String[] args) {
		
		// Reads the string from the json and parse
		String s = "Hey this is a test. I am trying to parse a string *()87 with characters";
		String delims = "[ *.,?!]+";
		String[] tokens = s.split(delims);
		
		for (int i = 0; i < tokens.length; i++)
		    System.out.println(tokens[i]);

	}

}
