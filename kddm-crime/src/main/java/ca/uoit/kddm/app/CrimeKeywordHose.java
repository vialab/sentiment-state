package ca.uoit.kddm.app;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import scala.actors.threadpool.Arrays;
import nsidc.spheres.LatLonBoundingBox;
import ca.uoit.kddm.auth.Credentials;
import ca.uoit.kddm.client.StreamClient;
import ca.uoit.kddm.client.TwitterCredentials;
import ca.uoit.kddm.client.callback.MongoDBCallback;
import ca.uoit.kddm.client.callback.PrintCallback;
import ca.uoit.kddm.client.filter.CityFilter;
import ca.uoit.kddm.client.filter.LocationFilter;

public class CrimeKeywordHose {

	public static void main(String[] args) throws FileNotFoundException, UnknownHostException, InterruptedException {
		Scanner scanner = new Scanner(new File("data/keywords.txt"));
		List<String> keywords = new ArrayList<String>();
		
		while (scanner.hasNext()){
			keywords.add(scanner.next());
		}
		scanner.close();
		
		TwitterCredentials credentials = Credentials.getCredentials("rafaveguim");
		
		// from http://boundingbox.klokantech.com/
//		LatLonBoundingBox location = new LatLonBoundingBox(41.4407, 42.4153, -88.1406, -87.3221);
//		List<LatLonBoundingBox> locations = new ArrayList<>();
//		locations.add(location);
//		LocationFilter filter = new LocationFilter(locations);
		
		CityFilter filter = new CityFilter(Arrays.asList(new String[]{"Chicago"}));
			
		StreamClient client = new StreamClient(credentials, 
				new PrintCallback(filter));
				//new MongoDBCallback(filter, "keyword_stream"));
		
		client.setKeywords(keywords);
		
		client.run();

	}

}
