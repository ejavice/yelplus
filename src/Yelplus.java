import java.io.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.regex.Pattern;

import javax.servlet.*;
import javax.servlet.http.*;

public class Yelplus extends HttpServlet { 
	
	private static final long serialVersionUID = 1L;
	private static Connection _conn;
	
	public static Pattern FOODREGEX;
	public static Pattern DECORREGEX;
	public static Pattern SERVICEREGEX;
	public static Pattern COSTREGEX;

	private boolean review = false;
	private boolean compare = false;
	private static Business business;
	
	/**
	 * Constructor. Initialize FOODREGEX, DECORREGEX, SERVICEREGEX, COSTREGEX.
	 */
	public Yelplus() {
		try {
			Statement st = openDBConnection();
			
			ArrayList<String> foodKeywords = getKeywords(st, "food");
			FOODREGEX = createRegex(foodKeywords);
		
			ArrayList<String> decorKeywords = getKeywords(st, "decor");
			DECORREGEX = createRegex(decorKeywords);
						
			ArrayList<String> serviceKeywords = getKeywords(st, "service");
			SERVICEREGEX = createRegex(serviceKeywords);
			
			ArrayList<String> costKeywords = getKeywords(st, "cost");
			COSTREGEX = createRegex(costKeywords);
			
			closeDBConnection();
		} catch (SQLException sqle) {
			System.err.println("Constructor encountered an exception: " + sqle.toString());	
		}
	}
	
	/**
	 * Print HTML for home page.
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws IOException, ServletException {
		//System.out.println("doGet");
		review = false;
		compare = false;
		business = null;

		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		
        out.println("<html>");
        out.println("<head>");
        out.println("<meta charset=\"utf-8\">");
        out.println("<title>Yelplus</title>");
        out.println("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
        out.println("<link href=\"http://twitter.github.io/bootstrap/assets/css/bootstrap.css\" rel=\"stylesheet\">");
        out.println("<style type=\"text/css\">");
        out.println("body {padding-top: 60px; padding-bottom: 40px;}");
        out.println("</style>");
        //out.println("<link href=\"http://twitter.github.io/bootstrap/assets/css/bootstrap-responsive.css\" rel=\"stylesheet\">");
        
        /* commented out for now because main.css not found */
        	out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"main.css\">");
        out.println("</head>");
        out.println("<body>");
        
        //Top nav bar
        out.println("<div class=\"navbar navbar-inverse navbar-fixed-top\">");
        out.println("<div class=\"navbar-inner\">");
        out.println("<div class=\"container\">");
        out.println("<a class=\"brand\" href=\"#\">Yelplus</a>");
        out.println("</div>");
        out.println("</div>");
        out.println("</div>");
        
        //Center heading
        out.println("<div class=\"container\">");
        out.println("<div class=\"hero-unit\">");
        /*out.println("<script src=\"http://code.jquery.com/jquery.js\"></script>");
        out.println("<script src=\"text/js/bootstrap.min.js\"></script>");*/
		out.println("<center><h1 id=\"title\"><img src=\"yelplus_logo.png\"></h1></center>");
		out.println("<P>");
		
		out.println("<center>");
		out.println("<p id=\"geolocation\"></p>");
		out.println("<div id=\"mapholder\"></div>");
		out.println("<script type=\"text/javascript\" src=\"http://maps.google.com/maps/api/js?sensor=false\"></script>");
		out.println("<script type=\"text/javascript\">");
		out.println("var x=document.getElementById(\"geolocation\");");
		out.println("var results;");
		out.println("function getLocation(){if(navigator.geolocation){navigator.geolocation.getCurrentPosition(showPosition)}else{x.innerHTML=\"Geolocation is not supported by this browser.\";}}");
		out.println("function showPosition(position){lat=position.coords.latitude;lon=position.coords.longitude;latlon=new google.maps.LatLng(lat,lon);mapholder=document.getElementById(\"mapholder\");mapholder.style.height='250px';mapholder.style.width='500px';var myOptions={center:latlon,zoom:14,mapTypeId:google.maps.MapTypeId.ROADMAP,mapTypeControl:false,navigationControlOptions:{style:google.maps.NavigationControlStyle.SMALL}};var map=new google.maps.Map(document.getElementById(\"mapholder\"),myOptions);var marker=new google.maps.Marker({position:latlon,map:map});"
				+ "geocoder = new google.maps.Geocoder();geocoder.geocode({'latLng':latlon},function(results,status){if(status==google.maps.GeocoderStatus.OK){if(results[6]){var addr = results[6].formatted_address;var beforeComma = addr.substr(0,addr.indexOf(\",\"));initContinued(beforeComma);}}else{alert('Geocoder failed due to: ' + status);}});}");
		out.println("function initContinued(addr){var elem = document.getElementById(\"locationQuery\"); elem.value = addr;}");
		out.println("getLocation()");
		out.println("</script>");
		out.println("</center>");
		
		out.println("<center>");
		out.print("<form action=\"Yelplus\" method=POST class=\"form-inline\">");
		out.println("Business Name");
		out.println("<input type=text size=20 name=businessQuery>");
	//	out.println("City, ZipCode, or Neighborhood");
		out.println("City or Neighborhood");
		out.println("<input type=text size=20 name=locationQuery id=\"locationQuery\">");
		/*
		out.println("<br>");
		out.println("Category");
		out.println("<input type=text size=20 name=categoryQuery>");
		*/
		out.println("<input type=submit value=\"Search\">");
		out.println("</form>");
		out.println("</center>");
		
		out.println("</div>");
		
		//Bottom columns 
		//if geolocation not enabled
		String cityQuery = "Philadelphia";
		out.println("<div class=\"row\">");
		try {
			ResultSet rs = getTopBusinesses (cityQuery);
			while (rs.next()) {
				out.println("<div class=\"span4\">");
				String url = "\"Yelplus?" + rs.getString("business_id") + "\"";
				out.println("<form action="+ url + " method=POST>");
				
				out.println("<center>");
				out.println("<img src=\"" + (rs.getString("photo_url")) + "\">");
				out.println("<br><br>");
				out.println("<input type=submit class=\"home_submit_top\" value =\"" + rs.getString("name") + "\">");
				out.println("<br>");
				out.println("</center>");
				out.println("<div class=\"top_review_text\">");
				out.println("Category: " + rs.getString("categories"));
				out.println("<br>");
				out.println("Neighborhood: " + rs.getString("neighborhood"));
				out.println("<br>");
				out.println(rs.getString("city") + ", " + rs.getString("state"));
				out.println("<br>");
				out.println("Rating: " + rs.getFloat("stars"));
				out.println("<p>");
				out.println("</div>");
				out.println("</form>");
				out.println("</div>");
			}
		} catch (SQLException sqle) {
			System.err.println("doGet encountered an exception: " + sqle.toString());	
		}
		out.println("</div>");
		
		out.println("</div>");
		out.println("</body>");
		out.println("</html>");	
	}
	
	/**
	 * Print HTML for other pages.
	 */
	public void doPost(HttpServletRequest req, HttpServletResponse res) 
			throws ServletException,IOException {
		
		res.setContentType("text/html");
		String businessQuery = req.getParameter("businessQuery");
		String locationQuery = req.getParameter("locationQuery");
	//	String categoryQuery = req.getParameter("categoryQuery");
	//	System.out.println(businessQuery + " " + locationQuery + categoryQuery);
		PrintWriter out = res.getWriter();
	//	HttpSession hs = req.getSession(true);
		//businessQuery = (String) hs.getAttribute("businessQuery");
		//locationQuery = (String) hs.getAttribute("locationQuery");
				
		out.println("<html>");
		
		out.println("<head>");
		out.println("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
        out.println("<link href=\"http://twitter.github.io/bootstrap/assets/css/bootstrap.css\" rel=\"stylesheet\">");
        out.println("<style type=\"text/css\">");
        out.println("body {padding-top: 60px; padding-bottom: 40px;}");
        out.println("</style>");
       // out.println("<link href=\"http://twitter.github.io/bootstrap/assets/css/bootstrap-responsive.css\" rel=\"stylesheet\">");
       // out.println("<h1 class=\"span8 offset1\">Your top results...</h1>");
        out.println("</head>");
        
		out.println("<body class=\"other_pages\">");
		
		//Nav bar
        out.println("<div class=\"navbar navbar-inverse navbar-fixed-top\">");
        out.println("<div class=\"navbar-inner\">");
        out.println("<div class=\"container\">");
        out.println("<a class=\"brand\" href=\"#\">Yelplus</a>");
        out.println("</div>");
        out.println("</div>");
        out.println("</div>");
		
	//	out.print("<form action=\"");
	//	out.print("Yelplus\" ");
	//	out.println("method=POST>");
		
		String query = req.getQueryString();
		if (query != null) {
			review = true;
		}
		
		if (review == false) {
			try {
			//	ResultSet rs = getBusinesses (businessQuery, locationQuery, categoryQuery);
				ResultSet rs = getBusinesses (businessQuery, locationQuery, null);
				int rsSize = 0;
				while (rs.next()) {
					rsSize++;
					String url = "\"Yelplus?" + rs.getString("business_id") + "\"";
					
					out.println("<ul class=\"thumbnails\">");
						out.println("<li class=\"span8\">");
							out.println("<div class=\"thumbnail offset2\">");
								out.println("<div class=\"row-fluid\">");
									out.println("<div class=\"span2\">");
										out.println("<img alt=\"\" src=\"" + (rs.getString("photo_url")) + "\" class=\"pull-left\">");
									out.println("</div>");
									out.println("<div class=\"span8 caption offset2\">");
										out.println("<form action="+ url + " method=POST>");
											out.println("<input type=submit value =\"" + rs.getString("name") + "\">");
											out.println("<p>");
												out.println("Category: " + rs.getString("categories"));
												out.println("<br>");
												out.println("Neighborhood: " + rs.getString("neighborhood"));
												out.println("<br>");
												out.println(rs.getString("city") + ", " + rs.getString("state"));
												out.println("<br>");
												out.println("Rating: " + rs.getFloat("stars"));
											out.println("</p>");
										out.println("</form>");
									out.println("</div>");
								out.println("</div>");
							out.println("</div>");
						out.println("</li>");
					out.println("</ul>");
				}
				
				if (rsSize == 0) {
					if (compare == true) {
						out.print("<form action=\" Yelplus\" method=POST>");
						out.println("<input type=submit value=\"No businesses were found :( Go back.\">");
						compare = false;
						out.print("</form>");
					} else {
						out.print("<form action=\" Yelplus\" method=GET>");
						out.println("No businesses matched your search :( Please Try Again!");
						out.println("<br>");
						out.println("<input type=submit value=\"Search Again\">");
						out.print("</form>");
					}
				} else {
					out.print("<form action=\" Yelplus\" method=GET>");
					out.println("<br>");
					out.println("<input type=submit value=\"Search Again\">");
					out.print("</form>");
				}
			} catch (SQLException sqle) {
				System.err.println("doPost encountered an exception: " + sqle.toString());	
			} finally {
				closeDBConnection();	
				review = true;
			}
		} else if (review == true && compare == false) {
			
			out.print("<form action=\"Yelplus\" method=POST>");
			
			String business_id = req.getQueryString();
			//business = processBusiness("RaE9ir386tfLGwtDN9W6QA");
			if (business_id != null) {
				business = processBusiness(business_id);
			}
			
			printBusinessInfo(business, out);
			
			
			int totalCount = 5;
			out.println("<ul class=\"thumbnails\">");
			out.println("<li class=\"span12\">");
				out.println("<div class=\"thumbnail offset2\">");
					out.println("<div class=\"row-fluid\">");
						out.println("<div class=\"span11 caption offset1\">");
							out.println("<h5>Food Reviews</h5><br>");
							printReviews(business.foodReviews, totalCount, out);
						out.println("</div>");
					out.println("</div>");
				out.println("</div>");
			out.println("</li>");
			out.println("</ul>");
			
			out.println("<ul class=\"thumbnails\">");
			out.println("<li class=\"span12\">");
				out.println("<div class=\"thumbnail offset2\">");
					out.println("<div class=\"row-fluid\">");
						out.println("<div class=\"span11 caption offset1\">");
							out.println("<h5>Decor Reviews<br></h5>");
							printReviews(business.decorReviews, totalCount, out);
						out.println("</div>");
					out.println("</div>");
				out.println("</div>");
			out.println("</li>");
			out.println("</ul>");
			
			out.println("<ul class=\"thumbnails\">");
			out.println("<li class=\"span12\">");
				out.println("<div class=\"thumbnail offset2\">");
					out.println("<div class=\"row-fluid\">");
						out.println("<div class=\"span11 caption offset1\">");
							out.println("<h5>Service Reviews</h5><br>");
							printReviews(business.serviceReviews, totalCount, out);
						out.println("</div>");
					out.println("</div>");
				out.println("</div>");
			out.println("</li>");
			out.println("</ul>");
			
			out.println("<ul class=\"thumbnails\">");
			out.println("<li class=\"span12\">");
				out.println("<div class=\"thumbnail offset2\">");
					out.println("<div class=\"row-fluid\">");
						out.println("<div class=\"span11 caption offset1\">");
							out.println("<h5>Cost Reviews</h5><br>");
							printReviews(business.costReviews, totalCount, out);
						out.println("</div>");
					out.println("</div>");
				out.println("</div>");
			out.println("</li>");
			out.println("</ul>");
			
			
		
		
		
			
			out.println("<ul class=\"thumbnails\">");
			out.println("<li class=\"span12\">");
				out.println("<div class=\"thumbnail offset2\">");
					out.println("<div class=\"row-fluid\">");
						out.println("<div class=\"span12 caption\">");
							out.println("<p id=\"geolocation\"></p>");
							out.println("<div id=\"mapholder\"></div>");
							out.println("<script type=\"text/javascript\" src=\"http://maps.google.com/maps/api/js?sensor=false\"></script>");
							out.println("<script type=\"text/javascript\">");
							out.println("var x=document.getElementById(\"geolocation\");");
							out.println("var results;");
							out.println("function showPosition(){lat="+ business.latitude +";lon="+ business.longitude +";latlon=new google.maps.LatLng(lat,lon);mapholder=document.getElementById(\"mapholder\");mapholder.style.height='250px';mapholder.style.width='500px';var myOptions={center:latlon,zoom:14,mapTypeId:google.maps.MapTypeId.ROADMAP,mapTypeControl:false,navigationControlOptions:{style:google.maps.NavigationControlStyle.SMALL}};var map=new google.maps.Map(document.getElementById(\"mapholder\"),myOptions);var marker=new google.maps.Marker({position:latlon,map:map});"
									+ "geocoder = new google.maps.Geocoder();geocoder.geocode({'latLng':latlon},function(results,status){if(status==google.maps.GeocoderStatus.OK){if(results[5]){var addr = results[5].formatted_address;var beforeComma = addr.substr(0,addr.indexOf(\",\"));initContinued(beforeComma);}}else{alert('Geocoder failed due to: ' + status);}});}");
							out.println("function initContinued(addr){var elem = document.getElementById(\"locationQuery\"); elem.value = addr;}");
							out.println("showPosition()");
							out.println("</script>");
						out.println("</div>");
					out.println("</div>");
				out.println("</div>");
			out.println("</li>");
			out.println("</ul>");
			
			
			out.println("<ul class=\"thumbnails\">");
			out.println("<li class=\"span12\">");
				out.println("<div class=\"thumbnail offset2\">");
					out.println("<div class=\"row-fluid\">");
						out.println("<div class=\"span11 caption offset1\">");
							out.println("Compare with Another Business:");
							out.println("<br>");
							out.println("Business Name");
							out.println("<input type=text size=20 name=businessQuery>");
							out.println("<br>");
						//	out.println("City, ZipCode, or Neighborhood");
							out.println("City or Neighborhood");
							out.println("<input type=text size=20 name=locationQuery>");
							out.println("<br>");
						//	out.println("Category");
						//	out.println("<input type=text size=20 name=categoryQuery>");
						//	out.println("<br>");
							out.println("<input type=submit value=\"Compare\">");
							out.println("</form>");
						out.println("</div>");
					out.println("</div>");
				out.println("</div>");
			out.println("</li>");
			out.println("</ul>");

			
			out.println("<ul class=\"thumbnails\">");
			out.println("<li class=\"span12\">");
				out.println("<div class=\"thumbnail offset2\">");
					out.println("<div class=\"row-fluid\">");
						out.println("<div class=\"span11 caption offset1\">");
							out.print("<form action=\"Yelplus\" method=GET>");
							out.println("<input type=submit value=\"Search Again\">");
							out.println("</form>");
						out.println("</div>");
					out.println("</div>");
				out.println("</div>");
			out.println("</li>");
			out.println("</ul>");
			
			
			
			review = false;
			compare = true;
			business_id = null;
		
		}	else if (review == true && compare == true) {
			
			out.print("<form action=\"Yelplus\" method=DO>");
			
			printBusinessInfo(business, out);
			
			int totalCount = 2;
			out.println("<ul class=\"thumbnails\">");
			out.println("<li class=\"span12\">");
				out.println("<div class=\"thumbnail offset2\">");
					out.println("<div class=\"row-fluid\">");
						out.println("<div class=\"span11 caption offset1\">");
							out.println("<h5>Food Reviews</h5><br>");
							printReviews(business.foodReviews, totalCount, out);
						out.println("</div>");
					out.println("</div>");
				out.println("</div>");
			out.println("</li>");
			out.println("</ul>");
			
			out.println("<ul class=\"thumbnails\">");
			out.println("<li class=\"span12\">");
				out.println("<div class=\"thumbnail offset2\">");
					out.println("<div class=\"row-fluid\">");
						out.println("<div class=\"span11 caption offset1\">");
							out.println("<h5>Decor Reviews<br></h5>");
							printReviews(business.decorReviews, totalCount, out);
						out.println("</div>");
					out.println("</div>");
				out.println("</div>");
			out.println("</li>");
			out.println("</ul>");
			
			out.println("<ul class=\"thumbnails\">");
			out.println("<li class=\"span12\">");
				out.println("<div class=\"thumbnail offset2\">");
					out.println("<div class=\"row-fluid\">");
						out.println("<div class=\"span11 caption offset1\">");
							out.println("<h5>Service Reviews</h5><br>");
							printReviews(business.serviceReviews, totalCount, out);
						out.println("</div>");
					out.println("</div>");
				out.println("</div>");
			out.println("</li>");
			out.println("</ul>");
			
			out.println("<ul class=\"thumbnails\">");
			out.println("<li class=\"span12\">");
				out.println("<div class=\"thumbnail offset2\">");
					out.println("<div class=\"row-fluid\">");
						out.println("<div class=\"span11 caption offset1\">");
							out.println("<h5>Cost Reviews</h5><br>");
							printReviews(business.costReviews, totalCount, out);
						out.println("</div>");
					out.println("</div>");
				out.println("</div>");
			out.println("</li>");
			out.println("</ul>");
			

			Business comparedBusiness = processBusiness(req.getQueryString());
			printBusinessInfo(comparedBusiness, out);
			////from here
			out.println("<ul class=\"thumbnails\">");
			out.println("<li class=\"span12\">");
				out.println("<div class=\"thumbnail offset2\">");
					out.println("<div class=\"row-fluid\">");
						out.println("<div class=\"span11 caption offset1\">");
							out.println("<h5>Food Reviews</h5><br>");
							printReviews(comparedBusiness.foodReviews, totalCount, out);
						out.println("</div>");
					out.println("</div>");
				out.println("</div>");
			out.println("</li>");
			out.println("</ul>");
			
			out.println("<ul class=\"thumbnails\">");
			out.println("<li class=\"span12\">");
				out.println("<div class=\"thumbnail offset2\">");
					out.println("<div class=\"row-fluid\">");
						out.println("<div class=\"span11 caption offset1\">");
							out.println("<h5>Decor Reviews<br></h5>");
							printReviews(comparedBusiness.decorReviews, totalCount, out);
						out.println("</div>");
					out.println("</div>");
				out.println("</div>");
			out.println("</li>");
			out.println("</ul>");
			
			out.println("<ul class=\"thumbnails\">");
			out.println("<li class=\"span12\">");
				out.println("<div class=\"thumbnail offset2\">");
					out.println("<div class=\"row-fluid\">");
						out.println("<div class=\"span11 caption offset1\">");
							out.println("<h5>Service Reviews</h5><br>");
							printReviews(comparedBusiness.serviceReviews, totalCount, out);
						out.println("</div>");
					out.println("</div>");
				out.println("</div>");
			out.println("</li>");
			out.println("</ul>");
			
			out.println("<ul class=\"thumbnails\">");
			out.println("<li class=\"span12\">");
				out.println("<div class=\"thumbnail offset2\">");
					out.println("<div class=\"row-fluid\">");
						out.println("<div class=\"span11 caption offset1\">");
							out.println("<h5>Cost Reviews</h5><br>");
							printReviews(comparedBusiness.costReviews, totalCount, out);
						out.println("</div>");
					out.println("</div>");
				out.println("</div>");
			out.println("</li>");
			out.println("</ul>");
			//
			out.println("<input type=submit value=\"Search Again\">");
			out.println("</form>");
		}
		
		out.println("</body>");
		out.println("</html>");		
	
	}
	
	/**
	 * Find keywords.
	 * @param st
	 * @param category
	 * @return
	 */
	public static ArrayList<String> getKeywords(Statement st, String category) {
		ArrayList<String> keywords = new ArrayList<String>();
		
		try {
			ResultSet rs = st.executeQuery("SELECT keyword FROM keywords WHERE category = '" + 
					category + "'");
			while (rs.next()) {
		    	keywords.add(rs.getString("keyword"));
			}
		} catch (SQLException sqle) {
			System.err.println("getKeywords encountered an exception: " + sqle.toString());	
		}
		return keywords;
	}	
	
	/**
	 * Create regex for parsing reviews based on list of keywords.
	 * @param keywords
	 * @return
	 */
	private static Pattern createRegex (ArrayList<String> keywords) {
		StringBuilder builder = new StringBuilder();
		builder.append("("+ keywords.get(0));
		for (int i = 1; i < keywords.size(); i++) {
			builder.append("|" + keywords.get(i));
		}
		builder.append(")");
		String keywordsRegex = builder.toString();
		Pattern regex = Pattern.compile("[.?!][^0-9a-z\\.\'\"][^.?!\'\"]*?\\b"+keywordsRegex+
				"\\b[^.?!\'\"]*?[.!]", Pattern.CASE_INSENSITIVE);
		return regex;
	}
	
	/**
	 * Find top 3 businesses based on geolocation.
	 * @param business
	 * @param location
	 * @return
	 */
	public ResultSet getTopBusinesses (String cityQuery) {
		try {
			Statement st = openDBConnection();
			String query = ("SELECT DISTINCT * FROM business "
					+ "WHERE LOWER(city) LIKE LOWER('%" + cityQuery + "%') " 
					+ "AND (review_count > 35) "
					+ "ORDER BY stars DESC, review_count DESC LIMIT 0,3");
			ResultSet rs = st.executeQuery(query);
			return rs;
		} catch (SQLException sqle) {
			System.err.println("getBusinesses encountered an exception: " + sqle.toString());			
		} 
		return null;
	}
	
	/**
	 * Find top 10 businesses matching query for business name, location, and/or category.
	 * @param business
	 * @param location
	 * @return
	 */
	public ResultSet getBusinesses (String businessQuery, String locationQuery, String categoryQuery) {
		
		businessQuery = businessQuery.replaceAll("\'", "\'\'");
		businessQuery = businessQuery.replaceAll("\"", "\"\"");
		locationQuery = locationQuery.replaceAll("\'", "\'\'");
		locationQuery = locationQuery.replaceAll("\"", "\"\"");
		
		try {
			Class.forName("com.mysql.jdbc.Driver");
			_conn = DriverManager.getConnection ("jdbc:mysql://fling.seas.upenn.edu:3306/ctedi","ctedi", "Christine123");
			/*String query = ("SELECT * FROM business "
					+ "WHERE (LOWER(city) LIKE LOWER('%" + locationQuery + "%') "
							+ "OR LOWER(neighborhood) LIKE LOWER('%" + locationQuery + "%') "
							+ "OR LOWER(zipcode) LIKE LOWER('" + locationQuery + "')) "
					+ "AND (LOWER(categories) LIKE LOWER('%" + categoryQuery + "%')) "
					+ "AND (LOWER(name) SOUNDS LIKE LOWER('%" + businessQuery + "%'))"
					+ "ORDER BY stars DESC, review_count DESC LIMIT 0,10");*/
			String query = ("SELECT * FROM business "
			+ "WHERE (LOWER(city) LIKE LOWER('%" + locationQuery + "%') "
					+ "OR LOWER(neighborhood) LIKE LOWER('%" + locationQuery + "%')) "
			+ "AND (LOWER(name) SOUNDS LIKE LOWER('%" + businessQuery + "%') "
					+ "OR LOWER(name) LIKE LOWER('%" + businessQuery + "%')) "
			+ "ORDER BY stars DESC, review_count DESC LIMIT 0,10");
			ResultSet rs = _conn.prepareStatement(query).executeQuery();
			return rs;
			
		} catch (SQLException sqle) {
			System.err.println("getBusinesses encountered an exception: " + sqle.toString());			
		} catch (ClassNotFoundException cnfe) {
			System.err.println("openConnection encountered an exception: " + cnfe.toString());	
		}
		return null;
	}
	
	/**
	 * Create new business.
	 * @param i
	 * @return
	 */
	public static Business processBusiness (String i) {
		ArrayList<Review> reviews = new ArrayList<Review>();
		Business newBusiness = new Business();
		
		try {
			Statement st = openDBConnection();
			ResultSet rs = st.executeQuery("SELECT * FROM review WHERE business_id = \"" + 
					i + "\" ORDER BY useful DESC, funny DESC, cool DESC"); 
			while (rs.next()) {
		    	Review review = new Review(rs.getString("business_id"), rs.getString("user_id"), 
		    			rs.getFloat("stars"), rs.getInt("useful"), rs.getString("review_text"));
		    	reviews.add(review);
		    } 
			ResultSet rsCompany = st.executeQuery("SELECT * FROM business WHERE business_id = \"" + i + "\"");
			rsCompany.next();
			newBusiness = new Business(i, rsCompany.getString("name"), rsCompany.getInt("review_count"), 
					rsCompany.getFloat("stars"), rsCompany.getString("address1"), rsCompany.getString("address2"), 
					rsCompany.getString("neighborhood"), rsCompany.getString("categories"), rsCompany.getString("url"), 
					rsCompany.getString("photo_url"), rsCompany.getFloat("latitude"), rsCompany.getFloat("longitude"), reviews);
			closeDBConnection();
		} catch (SQLException sqle) {
			System.err.println("processBusiness encountered an exception: " + sqle.toString());	
			
		}
		return newBusiness;
	}		
	
	/**
	 * Print HTML for describing business.
	 * @param b
	 * @param out
	 */
	public static void printBusinessInfo (Business b, PrintWriter out) {
			out.println("<ul class=\"thumbnails\">");
			out.println("<li class=\"span12\">");
				out.println("<div class=\"thumbnail offset2\">");
					out.println("<div class=\"row-fluid\">");
						out.println("<div class=\"span2\">");
							out.println("<img src=\"" + b.photo_url + "\">");
						out.println("</div>");
						out.println("<div class=\"span4 caption offset1\">");
							if (b.gExists == true) {
								out.println("<a href=\"" + b.website + "\">");
								out.print(b.name);
								out.println("<br>");
							} else {
								out.println("<a href=\"" + b.yelp_url + "\">");
								out.print(b.name);
								out.println("<br>");
							}
							out.print("</a>");
							out.println("Category: " + b.categories);
							out.println("<br>");
							out.println("Neighborhood: " + b.neighborhood);
							out.println("<br>");
							out.println(b.address1);
							out.println("<br>");
							out.println(b.address2);
							out.println("<br>");
							
							if (b.gExists == true && b.open != null) {
								out.println(b.open);
								out.println("<br>");
							}

							out.println("<p>");
							out.println("Yelp Rating: " + b.stars);
							out.println("<br>");
						out.println("</div>");
						out.println("<div class=\"span4 caption offset1\"");
						if (b.gExists == true && b.gNums == true) {
							out.println("Google Rating: " + b.gRating);
							out.println("<br>");
							out.println("Google Food Rating: " + b.gFood);
							out.println("<br>");
							out.println("Google Decor Rating: " + b.gDecor);
							out.println("<br>");
							out.println("Google Service Rating: " + b.gService);
							out.println("<br>");
							out.println("Google Cost Rating: " + b.gCost);
							out.println("<br>");
						}
						out.println("</div>");
					out.println("</div>");
				out.println("</div>");
			out.println("</li>");
		out.println("</ul>");
		
	}	
		
	/**
	 * Print HTML for listing parsed reviews.
	 * @param reviews
	 * @param totalCount
	 * @param out
	 */
	public static void printReviews (ArrayList<Review> reviews, int totalCount, PrintWriter out) {
		int count = 0;
		if (business.foodReviews != null) {	
			for (Review i : reviews) {
				out.println("Rating: " + i.stars + " Votes: " + i.useful);
				out.println("<br>");
				out.println(i.text);
				out.println("<br>");
				count++;
				if (count == totalCount) break;
			}
		}
		out.println("<p>");
	}
	
	/**
	 * Open the database connection.
	 * @throws SQLException if connection cannot be closed.
	 */
	public static Statement openDBConnection() throws SQLException {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			_conn = DriverManager.getConnection ("jdbc:mysql://fling.seas.upenn.edu:3306/ctedi","ctedi", "Christine123");
			Statement st = _conn.createStatement();
			return st;
		} catch (SQLException sqle) {
			System.err.println("openConnection encountered an exception: " + sqle.toString());	
		} catch (ClassNotFoundException cnfe) {
			System.err.println("openConnection encountered an exception: " + cnfe.toString());	
		}
		return null;
	}

	/**
	 * Close the database connection.
	 * @throws SQLException if connection cannot be closed.
	 */
	public static void closeDBConnection() {		 
		try{
			if ((_conn != null) && !_conn.isClosed()) {
				_conn.close();		 
			}
		} catch (SQLException sqle) {
			System.err.println("closeDBConnection encountered an exception: " + sqle.toString());	
		}
	}

	}