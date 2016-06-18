package hello;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;

@RestController
public class EmployeeController {

	String username = null;

	@RequestMapping("/login")
	public EmployeeDetail loginDetails() {

		String user = "rajiv";
		String password = "rajivkr";
		EmployeeDetail employeeDetail = new EmployeeDetail();
		System.out.println("data received is" + user + "pass" + password);
		MongoClient client = DbUtility.getClient();
		System.out.println("dbname is " + DbUtility.dbname);
		MongoDatabase db = client.getDatabase(DbUtility.dbname);
		FindIterable<Document> employeeDetails = db.getCollection("employee_details").find(new Document("uname", user));
		if (employeeDetails == null) {
			System.out.println("Unable to login");
			client.close();
		}
		employeeDetails.forEach(new Block<Document>() {
			public void apply(final Document document) {
				System.out.println("fetched doc is" + document.toString());
				JSONObject json = new JSONObject(document.toJson());
				String username = json.getString("uname");
				String password = json.getString("password");
				if (user.equalsIgnoreCase(username) && password.equals(password)) {
					System.out.println("password matched");
					employeeDetail.setUsername(username);
					employeeDetail.setPassword(password);
					employeeDetail.setName(json.getString("name"));
					employeeDetail.setId(json.getString("id"));
				}
			}
		});

		FindIterable<Document> jobDetails = db.getCollection("job_details")
				.find(new Document("eng_id", employeeDetail.getId()));

		List<Customer> customerDetails = new ArrayList<Customer>();

		jobDetails.forEach(new Block<Document>() {
			public void apply(final Document document) {
				System.out.println("fetched doc is" + document.toString());
				JSONObject json = new JSONObject(document.toJson());
				Customer customer = new Customer();
				customer.setAddress(json.getString("address"));
				customer.setName(json.getString("customer_name"));
				customer.setMailId(json.getString("mail"));
				customer.setSurveyTime(json.getString("survey_time"));
				customer.setMobileNumber(json.getString("phone"));
				customer.setSurvey(json.getString("survey"));
				if (json.has("picture") && json.getJSONArray("picture").length() > 0) {
					List<String> picture = new ArrayList<>();
					JSONArray jsonArray = json.getJSONArray("picture");
					for (int i = 0; i < jsonArray.length(); i++) {
						picture.add(jsonArray.getString(i));
					}
					customer.setPictureUrl(picture);
				}
				customerDetails.add(customer);
			}
		});
		employeeDetail.setCustomerDetails(customerDetails);

		client.close();
		return employeeDetail;
	}

	@RequestMapping("/facilities")
	public Facilties getFacilities() {

		MongoClient client = DbUtility.getClient();
		System.out.println("dbname is " + DbUtility.dbname);
		MongoDatabase db = client.getDatabase(DbUtility.dbname);
		Facilties facilties = new Facilties();
		List<String> address = new ArrayList<>();
		FindIterable<Document> availableFacilties = db.getCollection("available_facilities").find();
		if (availableFacilties == null) {
			System.out.println("Unable to login");
			client.close();
		}
		availableFacilties.forEach(new Block<Document>() {
			public void apply(final Document document) {
				System.out.println("fetched doc is" + document.toString());
				JSONObject json = new JSONObject(document.toJson());
				address.add(json.getString("address"));
			}
		});
		client.close();
		facilties.setAddress(address);
		return facilties;
	}

}
