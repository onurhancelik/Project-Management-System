package mail;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;

@ManagedBean
public class LoginBean {

	private String username;
	private String password;

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param username
	 *            the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password
	 *            the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	public String login() {
		if (username.equals("username") && password.equals("password")) {
			return "list?faces-redirect=true";
		} else if (!username.equals("username") || !password.equals("password")) {
			FacesContext.getCurrentInstance().addMessage("loginForm2:password",
					new FacesMessage("Error", "Lütfen kullanıcı adını(username) ve şifreyi(password) doğru giriniz!"));
		} 
		return null;
	}

}