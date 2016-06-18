package hello;

import java.util.List;

public class EmployeeDetail {

	private String name;
	private String password;
	private String id;
	private String username;
	private List<Customer> CustomerDetails;

	public void setName(String name) {
		this.name = name;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getName() {
		return name;
	}

	public String getPassword() {
		return password;
	}

	public String getId() {
		return id;
	}

	public String getUsername() {
		return username;
	}

	public List<Customer> getCustomerDetails() {
		return CustomerDetails;
	}

	public void setCustomerDetails(List<Customer> customerDetails) {
		CustomerDetails = customerDetails;
	}

}
