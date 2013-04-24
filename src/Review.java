
public class Review {
	public String business_id;
	public String user_id;
	public float stars;
	public int useful;
	public String text;
	
	public Review (String _business_id, String _user_id, float _stars, Integer _useful, 
			String _text) {
		business_id = _business_id;
		user_id = _user_id;
		stars = _stars;
		useful = _useful;
		text = _text;
	}
	
	public Review (Review oldReview, String _text) {
		business_id = oldReview.business_id;
		user_id = oldReview.user_id;
		stars = oldReview.stars;
		useful = oldReview.useful;
		text = _text;
	}

}
