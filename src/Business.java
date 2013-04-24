import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;
import java.net.*;

public class Business {
	public String business_id;
	public String name;
	public int review_count;
	public float stars;
	public String address1;
	public String address2;
	public String neighborhood; 
	public String categories;
	public String yelp_url;
	public String photo_url;
	public float latitude;
	public float longitude;
	public ArrayList<Review> allReviews;
	public ArrayList<Review> foodReviews;
	public ArrayList<Review> decorReviews;
	public ArrayList<Review> serviceReviews;
	public ArrayList<Review> costReviews;
	private ArrayList<String> used;
	
	public boolean gExists = false;
	public boolean gNums = true;
	public float gRating;
	public float gFood;
	public float gDecor;
	public float gService;
	public float gCost;
	public String open;
	public String website;

	public Business() {
		
	}
	
	public Business (String _business_id, String _name, int _review_count, float _stars, String _address1, 
			String _address2, String _neighborhood, String _categories, String _url, String _photo_url, 
			float _latitude, float _longitude, ArrayList<Review> _allReviews) {
		business_id = _business_id;
		name = _name;
		review_count = _review_count;
		stars = _stars;
		address1 = _address1;
		address2 = _address2;
		neighborhood = _neighborhood;
		categories = _categories; 
		yelp_url = _url;
		photo_url = _photo_url;
		latitude = _latitude;
		longitude = _longitude;
		
		allReviews = _allReviews;
		used = new ArrayList<String>();
		foodReviews = new ArrayList<Review>();
		parseReviews (foodReviews, Yelplus.FOODREGEX);
		decorReviews = new ArrayList<Review>();
		parseReviews (decorReviews, Yelplus.DECORREGEX);
		serviceReviews = new ArrayList<Review>();
		parseReviews (serviceReviews, Yelplus.SERVICEREGEX);
		costReviews = new ArrayList<Review>();
		parseReviews (costReviews, Yelplus.COSTREGEX);
		
		String searchgapi = "" + name.trim() + " " + neighborhood.trim() + " " + address1.trim();
		searchgapi = searchgapi.replaceAll ("\\s","+");
		String gsearch1 = null;
		try {
			 gsearch1 = readUrl ("https://maps.googleapis.com/maps/api/place/textsearch/json?query=" 
					 + searchgapi + "&sensor=true&key=AIzaSyBtt4NnA8aZ8CGDm_JCXx4Brhjs8hu8whI");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		String greference = getReference (gsearch1);
		
		if (greference != null) {
			gExists = true;
			String gsearch2 = null;
			try {
				gsearch2 = readUrl ("https://maps.googleapis.com/maps/api/place/details/json?reference=" 
						+ greference + "&sensor=true&key=AIzaSyBtt4NnA8aZ8CGDm_JCXx4Brhjs8hu8whI");
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			gRating = getRating (gsearch1);
			gFood = getRating (gsearch2, "food");
			gDecor = getRating (gsearch2, "decor");
			gService = getRating (gsearch2, "service");
			gCost = getCostRating (gsearch1);
			
			open = getOpenStatus (gsearch2);
			website = getWebsite (gsearch2);
		}
		
	}
	
	/**
	 * Parse review to find sentence containing a keyword.
	 */
	public void parseReviews (ArrayList<Review> reviews, Pattern p) {
		int numSentences = 0;
		if (allReviews != null) {
			for (int i = 0; i < allReviews.size(); i++) {
				if (numSentences == 5) break;
				Review currentReview = allReviews.get(i);
				Matcher m = p.matcher(currentReview.text);
				if (m.find()){
					String sentence = (m.group());
					String trimmedSentence = Pattern.compile("[.?!]*?[)\\s*]").matcher(sentence).replaceFirst("").trim();
					if (trimmedSentence.matches("[(][^)]*?[.?!]")) {
						StringBuilder build = new StringBuilder(trimmedSentence);
						build.append(")");
						trimmedSentence = build.toString();
					}
					if (!used.contains(trimmedSentence)) {
						reviews.add(new Review(currentReview, trimmedSentence));
						numSentences++;
						used.add(trimmedSentence);
					}
					
				}
			}
		}
	}
	
	/**
	 * Find Google Places url.
	 * @param urlString
	 * @return
	 * @throws Exception
	 */
	private String readUrl(String urlString) throws Exception {
	    BufferedReader reader = null;
	    try {
	        URL url = new URL(urlString);
	        reader = new BufferedReader(new InputStreamReader(url.openStream()));
	        StringBuffer buffer = new StringBuffer();
	        int read;
	        char[] chars = new char[1024];
	        while ((read = reader.read(chars)) != -1)
	            buffer.append(chars, 0, read); 

	        return buffer.toString();
	    } finally {
	        if (reader != null)
	            reader.close();
	    }
	}

	/**
	 * Find Google Places id.
	 * @param gsearch1
	 * @return
	 */
	private String getReference (String gsearch1) {
		System.out.println(gsearch1);
		Matcher m = Pattern.compile("\"reference\"\\s:\\s\"\\S*\"").matcher(gsearch1);
		String reference = null;
		if (m.find() == true) {
			reference = Pattern.compile("\"reference\"\\s:\\s\"").matcher(m.group()).replaceFirst("");
			reference = Pattern.compile("\"").matcher(reference).replaceFirst("");
		}
		return reference;
	}
	
	/**
	 * Find average Google Places rating.
	 * @param gsearch2
	 * @return
	 */
	private float getRating (String gsearch1) {
		Matcher m = Pattern.compile("rating\"\\s:\\s[0-9].[0-9][0-9],").matcher(gsearch1);
		int numMatches = 0;
		int sum = 0;
		while (m.find()) {
			sum += Float.parseFloat(m.group().substring(10,14)) + 1;
			numMatches++;
		}
		
		float rating = ((float) sum/numMatches * (5/4)); 
		if (Float.isNaN(rating)) {
			gNums = false;
		}
		return rating;
	}
	
	/**
	 * Find average Google Places food, decor, or service rating.
	 * @param gsearch2
	 * @return
	 */
	private float getRating (String gsearch2, String category) {
		//System.out.println(gsearch2);
		Matcher m = Pattern.compile("[0-9],\\s*?\"type\"\\s:\\s\"" + category + "\"").matcher(gsearch2);
		int numMatches = 0;
		int sum = 0;
		while (m.find()) {
			sum += Integer.parseInt(m.group().substring(0,1)) + 1;
			numMatches++;
		}
		
		float rating = ((float) sum/numMatches * (5/4)) ;
			if (Float.isNaN(rating)) {
				gNums = false;
			}
		return rating;
	}
	
	/**
	 * Find average Google Places cost rating.
	 * @param gsearch2
	 * @return
	 */
	private float getCostRating (String gsearch1) {
		Matcher m = Pattern.compile("price_level\"\\s:\\s[0-9],").matcher(gsearch1);
		int numMatches = 0;
		int sum = 0;
		while (m.find()) {
			sum += Integer.parseInt(m.group().substring(15,16)) + 1;
			numMatches++;
		}
		
		return ((float) sum/numMatches * (5/4));
	}

	/**
	 * Determine if business is currently open or closed
	 * @param gsearch2
	 * @return
	 */
	private static String getOpenStatus (String gsearch2) {
		Matcher m = Pattern.compile("\"open_now\"\\s:\\s[tf]").matcher(gsearch2);
		if (m.find() == true) {
			if (m.group().substring(13,14).equals("t")) {
				return "Open!";	
			} else {
				return "Closed";
			}
		}
		return null;
	}
	
	/**
	 * Find business website.
	 * @param gsearch2
	 * @return
	 */
	private static String getWebsite (String gsearch2) {
		Matcher m = Pattern.compile("\"website\"\\s:\\s\"\\S*\"").matcher(gsearch2);
		if (m.find() == true) {
			String _website = Pattern.compile("\"website\"\\s:\\s\"").matcher(m.group()).replaceFirst("");
			_website = Pattern.compile("\"").matcher(_website).replaceFirst("");
			return _website;	
		}
		return null;
	}
	
}
