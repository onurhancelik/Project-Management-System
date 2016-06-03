package mail;

import java.util.Properties;

import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.search.FlagTerm;

import org.bson.Document;
import dbconnection.DatabaseCRUD;

@Singleton
public class MailListener {
	//this thread runs in a minute
	@Schedule(hour = "*", minute = "*/1", second = "0", persistent = false)
	public void run() {
		try {
			// create properties field
			Properties properties = new Properties();
			properties.put("mail.store.protocol", "imaps");

			Session emailSession = Session.getDefaultInstance(properties);
			Store store = emailSession.getStore();

			store.connect("imap.gmail.com", "sytgrup13@gmail.com", "132016syt");

			// create the folder object and open it
			Folder emailFolder = store.getFolder("INBOX");
			
			// READ_WRITE is used to be marked as read after retrieving its content
			emailFolder.open(Folder.READ_WRITE);

			// search for all "unseen" messages
			Flags seen = new Flags(Flags.Flag.SEEN);
			FlagTerm unseenFlagTerm = new FlagTerm(seen, false);

			// retrieve the unread messages from the folder in an array
			Message[] unreadMessages = emailFolder.search(unseenFlagTerm);

			if (unreadMessages.length > 0)
			{
				for (int i = 0; i < unreadMessages.length; i++) {
					boolean isMatched; 

					//parses message content and get students information from the content 
					//if unreadMessages[i] content matches the project group mail syntax, it returns true
					isMatched = parseMessage(unreadMessages[i]);
					
					//if the mail is project group mail
					if(isMatched)
					{
						//sends the confirmation mail
						sendConfirmMail(unreadMessages[i].getFrom());						
					}

				}				
			}

			// close the store and folder objects
			emailFolder.close(true);
			store.close();

		} catch (NoSuchProviderException e) {
			e.printStackTrace();
		} catch (MessagingException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void sendConfirmMail(Address[] to) {
		Properties props = new Properties();
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.socketFactory.port", "465");
		props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.port", "465");

		Session session = Session.getInstance(props, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication("sytgrup13@gmail.com", "132016syt");
			}
		});

		try {

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress("sytgrup13@gmail.com"));
			message.setRecipients(Message.RecipientType.TO, to);
			message.setSubject("Proje Kaydı");
			message.setText("Merhaba, \nProje grup kaydınız onaylandı.");

			Transport.send(message);

		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}	
	}
	
	public boolean parseMessage(Message message)
	{
		String mailContent = null;
		String courseName = null;
		int projectNo = 0;
		boolean groupStringFlag=false;
		try{
			
			Object content = message.getContent();
			if (content instanceof String)  
			{  
				mailContent = (String)content;
			}
			else if (content instanceof Multipart)  
			{  
				BodyPart bp = ((Multipart) content).getBodyPart(0);
				mailContent = bp.getContent().toString();	
			} 			
			
			//console information about message read
			System.out.println("SENT DATE:" + message.getSentDate());
			System.out.println("SUBJECT:" + message.getSubject());
		} catch (MessagingException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (mailContent != null) {
			//splits the mail in two parts with ":" character, first part includes course name and project no information, 
			//second part includes student information
			String parts[] = mailContent.split(":");
			
			//if the mail content is not match with project group mail syntax, returns from this method
			if(parts.length!=2)
			{
				System.out.println("The mail is not about project group.");
				return false;
			}
				
			
			//splits the first part line by line
			String[] part0_lines = parts[0].split(System.getProperty("line.separator"));
			
			String courseProjectLine[] = null;
			
			
			int index=0; //index of filled lines
			for (int j = 0; j < part0_lines.length; j++) {
				//passes the empty lines of the first part
				if(!part0_lines[j].isEmpty()){
					index++;
				}
				//second filled line must contain course name and project number information depending on syntax
				if(index==2){
					//splits the second filled line of the first part with space, it includes course name and project no
					courseProjectLine = part0_lines[j].split(" ");
					System.out.println("The mail is not about project group.");
					break; 
				}	
			}

			//iterates courseProjectLine, checks the keywords for the course name and project no
			for (int z = 0; z < courseProjectLine.length; z++) {
				//gets the course name by keywords (dersinin,dersi,ders)
				if (courseProjectLine[z].equalsIgnoreCase("dersinin") || courseProjectLine[z].equalsIgnoreCase("dersi") || courseProjectLine[z].equalsIgnoreCase("ders") )
					courseName = courseProjectLine[z - 1];
				
				//gets the project number by keyword (proje)
				if (courseProjectLine[z].contains("proje")) {
					//splits the string which contains the project number 
					String projectPart[] = courseProjectLine[z].split("\\.");
					//first string is project number
					projectNo = Integer.parseInt(projectPart[0]);
				}
				
				//checks the project information line contains the keywords (grup,grubu)
				if (courseProjectLine[z].equalsIgnoreCase("grup") || courseProjectLine[z].equalsIgnoreCase("grubu"))
					groupStringFlag = true;
			}

			//if the project information line misses the some keywords, parser cancels the parse processing
			if(projectNo==0 || courseName.isEmpty() || groupStringFlag==false)
			{
				System.out.println("The mail is not about project group.");
				return false;
			}
				
			
			//if everything is OK in terms of syntax, database connection is made 
			//creates object for database connection
			DatabaseCRUD dbDAO = new DatabaseCRUD();
			//opens the database connection
			dbDAO.openDB();
			
			//connects with projectList collection
			dbDAO.connectDBCollection("projectList");
			
			//splits the second part line by line, each line has individual student information
			String[] part1_lines = parts[1].split(System.getProperty("line.separator"));
			
			//getting number of last(max) project group no
			int newGroupNo = 0;
			if(dbDAO.getCollectionCount()<1) //if there is no data in project list collection
				newGroupNo = 1;
			else{
				int lastGroupNo = dbDAO.getMaxGroupNo(); //gets the maximum group no
				newGroupNo = lastGroupNo + 1; //increases the group number for new project group
			}
			
			//iterates the student information lines, splits the student name and student number with "-" character
			for (int k = 0; k < part1_lines.length; k++) {
				//passes the empty lines
				if (!part1_lines[k].isEmpty()) {
					//splits the student information line to name and number
					String nameNo[] = part1_lines[k].split("-");
					
					//add new student information to database depending on mail content 
					addDatabase(nameNo,newGroupNo,dbDAO);
				}
			}
			dbDAO.closeDB();
			return true;
		}
		return false; //if message content is null
	}
	
	public void addDatabase(String[] nameNo, int groupNo, DatabaseCRUD dbDAO)
	{
		//first string of nameNo array is number field
		String number = nameNo[0];
		//second string of nameNo array is name field
		String name = nameNo[1];
		
		//blank Document object is created
		Document student = new Document();
		
		//student information is added to Document object
		student.append("_id", number).
				append("name", name).
				append("grade", 0). //grade is null when document is created
				append("groupNo", groupNo);
		
		//adds new student data to database
		dbDAO.insert(student);		
	}
}
