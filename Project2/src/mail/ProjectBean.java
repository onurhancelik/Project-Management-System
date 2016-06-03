package mail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.bson.Document;
import com.mongodb.client.MongoCursor;

import dbconnection.DatabaseCRUD;

@ManagedBean(name = "projectBean", eager = true)
@ViewScoped
public class ProjectBean {
	private ArrayList<Student> students;
	private Student student = new Student(); // to hold the information of object to be edited or added
	private Student student2 = new Student(); // to hold the id number of the record to be edited
	private boolean edit;

	// constructor
	public ProjectBean() {

	}

	public Student getStudent() {
		return student;
	}

	@PostConstruct // will be invoked only once
	public void init() {
		// initiates the object
		students = new ArrayList<Student>();

		// calls the loadList method to load the list from database
		loadList();
		
		sortByGroupNo(); // current appearing table will be sorted by group no
	}

	// loads project student list
	public void loadList() {
		// creates object for database connection
		DatabaseCRUD dbDAO = new DatabaseCRUD();
		// opens the database connection
		dbDAO.openDB();

		// gets the related collection
		boolean isCollectionAvailable = dbDAO.connectDBCollection("projectList");

		if (isCollectionAvailable) {
			if (dbDAO.getCollectionCount() > 0) {
				// gets all documents from the database object
				MongoCursor<Document> cursor = dbDAO.getAllDocs();

				// iterates all documents
				while (cursor.hasNext()) {
					Document doc = cursor.next();

					Student std = new Student(doc.get("_id").toString(), doc.get("name").toString(),
							Integer.parseInt(doc.get("grade").toString()),
							Integer.parseInt(doc.get("groupNo").toString()));
					// adds to list
					students.add(std);
				}
			}
		}
		dbDAO.closeDB();
	}

	public ArrayList<Student> getStudents() {
		return students;
	}

	public void updateGrade(Student std) { 
											
		DatabaseCRUD dbDAO = new DatabaseCRUD(); // creates object for database connection
		
		dbDAO.openDB(); // opens the database connection
		
		// gets the related collection 
		boolean isCollectionAvailable = dbDAO.connectDBCollection("projectList");
		
		//if collection is available
		if(isCollectionAvailable)
		{
			// get id number and grade from editing Student object
			String id = std.getId();

			int newGrade = std.getGrade();

			// query document to update the related field
			Document updateField = new Document().append("$set", new Document().append("grade", newGrade));
			
			// updates the database with id number
			dbDAO.update(id, updateField);			
		}
		// closes the database
		dbDAO.closeDB();
	}

	// this method is invoked from edit panel group to save the changes in the database
	public void save() {
		// student information is saved in the database
		DatabaseCRUD dbDAO = new DatabaseCRUD(); // opens the database connection

		dbDAO.openDB();

		// gets the related collection boolean isCollectionAvailable
		boolean isCollectionAvailable = dbDAO.connectDBCollection("projectList");

		if (isCollectionAvailable) {
			// blank Document object is created
			Document docStd = new Document();

			// student information is added to Document object
			docStd.append("_id", student.getId()).
				   append("name", student.getName()).
				   append("grade", student.getGrade()).
				   append("groupNo", student.getGroupNo());

			dbDAO.delete(student2.getId()); //firstly remove record that will be deleted from the database 
			dbDAO.insert(docStd); //then insert same record with the changes
		}
		dbDAO.closeDB();

		student = new Student(); // Reset placeholder
		student2 = new Student(); // Reset placeholder
		edit = false; // edit flag is set to false to display new panelGroup

		sortByGroupNo(); // current appearing table will be sorted by group no
	}

	public boolean isEdit() {
		return edit;
	}

	// this method is set edit flag to display edit panelGroup and assigns student object
	public void edit(Student student) {
		this.student = student;
		this.student2.setId(student.getId());
		edit = true; // edit flag is set
	}

	// adds new student
	public void add() {
		// new student will be added to the database
		DatabaseCRUD dbDAO = new DatabaseCRUD(); // opens the database connection
		dbDAO.openDB();

		// gets the related collection boolean isCollectionAvailable
		boolean isCollectionAvailable = dbDAO.connectDBCollection("projectList");

		if (isCollectionAvailable) {
			// blank Document object is created
			Document docStd = new Document();

			// student information is added to Document object
			docStd.append("_id", student.getId()).
				   append("name", student.getName()).
				   append("grade", student.getGrade()).
					append("groupNo", student.getGroupNo());
			dbDAO.insert(docStd);
		}
		dbDAO.closeDB();

		// new student is also added to current list to display
		students.add(student);

		student = new Student(); // Reset placeholder.

		sortByGroupNo(); // current appearing table will be sorted by group no
	}

	// deletes student from database and current list object
	public void delete(Student student) {
		// student is deleted from the database
		DatabaseCRUD dbDAO = new DatabaseCRUD(); // opens the database connection

		dbDAO.openDB();

		// gets the related collection boolean isCollectionAvailable
		boolean isCollectionAvailable = dbDAO.connectDBCollection("projectList");

		if (isCollectionAvailable) {
			dbDAO.delete(student.getId());
		}
		dbDAO.closeDB();

		// student is also deleted from current list to display
		students.remove(student);

		sortByGroupNo(); // current appearing table will be sorted by group no
	}

	// sorts the list by group no (ascending order)
	public String sortByGroupNo() {
		Collections.sort(students, new Comparator<Student>() {
			@Override
			public int compare(Student o1, Student o2) {

				return Integer.toString(o1.getGroupNo()).compareTo(Integer.toString(o2.getGroupNo()));
			}
		});
		return null;
	}
}
