package ca.uoit.kddm.app;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import ca.uoit.kddm.auth.Credentials;
import ca.uoit.kddm.client.StreamClient;
import ca.uoit.kddm.client.TwitterCredentials;
import ca.uoit.kddm.client.callback.MongoDBCallback;
import ca.uoit.kddm.client.filter.CityFilter;
import ca.uoit.kddm.client.filter.CompoundFilter;
import ca.uoit.kddm.client.filter.KeywordFilter;

import com.twitter.hbc.core.endpoint.Location;
import com.twitter.hbc.core.endpoint.Location.Coordinate;

public class CrimeLocationHose {
	
	public static void main(String[] args) throws InterruptedException, UnknownHostException, FileNotFoundException{
		
		// Load keywords
		Scanner scanner = new Scanner(new File("data/keywords.txt"));
		List<String> keywords = new ArrayList<String>();
		
		while (scanner.hasNext()){
			keywords.add(scanner.next());
		}
		scanner.close();
		
		// Load credentials 
		TwitterCredentials credentials = Credentials.getCredentials("rafaveguim");
		
		// Compose a filter by city and keyword criteria
		KeywordFilter kwordFilter = new KeywordFilter(keywords);
		CityFilter cityFilter = new CityFilter(Arrays.asList(new String[]{"Chicago"}));
		CompoundFilter compoundFilter = new CompoundFilter(kwordFilter, cityFilter);
			
		StreamClient client = new StreamClient(credentials, new MongoDBCallback(compoundFilter, "location_stream"));
		
		// bounding box from http://boundingbox.klokantech.com/
		Location chicago = new Location(new Coordinate(-87.940101,41.643919), new Coordinate(-87.523984,42.023022));
		
		List<Location> locations = Arrays.asList(chicago);
		client.setLocations(locations);
		
		client.run();

	}
}
