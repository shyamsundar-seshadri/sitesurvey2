package hello;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;

@RestController
public class EmployeeController {

	String username = null;

	@RequestMapping("/login")
	@CrossOrigin(origins = { "http://localhost:6001",
			"http://sitesurvey.mybluemix.net" })
	public Response loginDetails(@QueryParam("username") String user,
			@QueryParam("password") String password) {

		EmployeeDetail employeeDetail = new EmployeeDetail();
		System.out.println("data received is" + user + "pass" + password);
		MongoClient client = DbUtility.getClient();
		System.out.println("dbname is " + DbUtility.dbname);
		MongoDatabase db = client.getDatabase(DbUtility.dbname);
		FindIterable<Document> employeeDetails = db.getCollection(
				"employee_details").find(new Document("uname", user));
		if (employeeDetails == null) {
			System.out.println("Unable to login");
			client.close();
			return Response.status(Status.SERVICE_UNAVAILABLE).build();
		}
		employeeDetails.forEach(new Block<Document>() {
			public void apply(final Document document) {
				System.out.println("fetched doc is" + document.toString());
				JSONObject json = new JSONObject(document.toJson());
				String username = json.getString("uname");
				String password = json.getString("password");
				if (user.equalsIgnoreCase(username)
						&& password.equals(password)) {
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
				if(json.has("survey_time")){
				customer.setSurveyTime(json.get("survey_time").toString());
				}
				customer.setMobileNumber(json.getString("phone"));
				customer.setSurvey(json.getString("survey"));
				customer.setJobId(json.getString("jobid"));
				if (json.has("picture")
						&& json.getJSONArray("picture").length() > 0) {
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
		return Response.ok(employeeDetail).build();
	}

	@RequestMapping("/facilities")
	@CrossOrigin(origins = { "http://localhost:6001",
			"http://sitesurvey.mybluemix.net" })
	public Facilties getFacilities() {

		MongoClient client = DbUtility.getClient();
		System.out.println("dbname is " + DbUtility.dbname);
		MongoDatabase db = client.getDatabase(DbUtility.dbname);
		Facilties facilties = new Facilties();
		List<String> address = new ArrayList<>();
		FindIterable<Document> availableFacilties = db.getCollection(
				"available_facilities").find();
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

	@RequestMapping("/sendEmail")
	@CrossOrigin(origins = { "http://localhost:6001",
			"http://sitesurvey.mybluemix.net" })
	public String sendMail(@QueryParam("name") String custName,
			@QueryParam("to") String to, @QueryParam("time") String time,
			@QueryParam("mailType") String type) {
		try {
			String body = null;
			if("appointment".equalsIgnoreCase(type)){
			body = "<html><body>Dear "
					+ custName
					+ " , <br> Thank you for your interest in Verizon.<br>"
					+ "We will visit your place on "
					+ time
					+ " for survey. <BR> Thanks for your co-operation. <br>"
					+ "<b>Verizon Survey Team  </b><br> <br> "
					+ "<img src=\"http://ss7.vzw.com/is/image/VerizonWireless/vzw-logo-156-130-c\"><br> "
					+ "</body> <H1> Better Matters </H1></html>";
			}else{
				body = "<html><body>Dear "
						+ custName
						+ " , <br> Thank you for your interest in Verizon.<br>"
						+ "We tried Reaching you number please Contant us "
						+  "<BR> Thanks for your co-operation. <br>"
						+ "<b>Verizon Survey Team  </b><br> <br> "
						+ "<img src=\"http://ss7.vzw.com/is/image/VerizonWireless/vzw-logo-156-130-c\"><br> "
						+ "</body> <H1> Better Matters </H1></html>";
				
			}
			String sub = "Verizon Site Survey";

			final String fromEmail = "shyam.hackathon@gmail.com";
			final String password = "HackMe@123";
			System.out.println("TLSEmail Start");
			Properties props = new Properties();
			props.put("mail.smtp.host", "smtp.gmail.com"); // SMTP Host
			props.put("mail.smtp.port", "587"); // TLS Port
			props.put("mail.smtp.auth", "true"); // enable authentication
			props.put("mail.smtp.starttls.enable", "true"); // enable STARTTLS

			Authenticator auth = new Authenticator() {
				// override the getPasswordAuthentication method
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(fromEmail, password);
				}
			};
			Session session = Session.getInstance(props, auth);
			MimeMessage msg = new MimeMessage(session);

			// set message headers
			msg.addHeader("Content-type", "text/HTML; charset=UTF-8");
			msg.addHeader("format", "flowed");
			msg.addHeader("Content-Transfer-Encoding", "8bit");

			msg.setFrom(new InternetAddress("shyam.hackathon@gmail.com",
					"DonotReply@Verizon"));
			msg.setReplyTo(InternetAddress.parse("shyam.hackathon@gmail.com",
					false));
			msg.setSubject(sub, "UTF-8");
			msg.setContent(body, "text/html");
			msg.setSentDate(new Date());
			msg.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(to, false));
			System.out.println("Message is ready");
			Transport.send(msg);

			System.out.println("EMail Sent Successfully!!");
		} catch (MessagingException mex) {
			mex.printStackTrace();
			return "Couldnot send mail to user";
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return "Couldnot send mail to user";
		}
		// storing in DB
		MongoClient client = DbUtility.getClient();
		MongoDatabase db = client.getDatabase(DbUtility.dbname);
		db.getCollection("job_details").updateMany(
				new Document("customer_name", "shyam"),
				new Document("$set", new Document("survey_time", time).append(
						"survey", "yes")));
		return "Mail Sent Succesfully";

	}

	@RequestMapping("/getAssignees")
	@CrossOrigin(origins = { "http://localhost:6001",
			"http://sitesurvey.mybluemix.net" })
	public Engineers getEngineers() {

		MongoClient client = DbUtility.getClient();
		MongoDatabase db = client.getDatabase(DbUtility.dbname);
		Engineers eng = new Engineers();

		FindIterable<Document> engAvailable = db.getCollection(
				"employee_details").find();
		if (engAvailable == null) {
			System.out.println("Unable to find engineers");
			client.close();
		}
		List<Engineer> engineers = new ArrayList<Engineer>();
		engAvailable.forEach(new Block<Document>() {
			public void apply(final Document document) {
				Engineer engineer = new Engineer();
				System.out.println("fetched doc is" + document.toString());
				JSONObject json = new JSONObject(document.toJson());
				engineer.setId(json.getString("id"));
				engineer.setName(json.getString("name"));
				engineers.add(engineer);
			}
		});
		client.close();
		eng.setEngineers(engineers);
		return eng;
	}

	@RequestMapping("/reAssign")
	@CrossOrigin(origins = { "http://localhost:6001",
			"http://sitesurvey.mybluemix.net" })
	public String reAssign(@QueryParam("jobId") String jobId,
			@QueryParam("fromEng") String fromEng,
			@QueryParam("toEng") String toEng) {
		String response = "failure";
		MongoClient client = DbUtility.getClient();
		System.out.println("job id is " + jobId + " from eng is" + fromEng);
		MongoDatabase db = client.getDatabase(DbUtility.dbname);
		UpdateResult updResult = db.getCollection("job_details").updateOne(
				(new Document("jobid", jobId).append("eng_id", fromEng)),
				new Document("$set", new Document("eng_id", toEng)));
		if (updResult.getModifiedCount() == 1)
			response = "sucess";
		client.close();
		return response;
	}
	

}
