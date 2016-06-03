package mail;

public class Student {

	private String id;
	private String name;
	private int grade;
	private int groupNo;

	public Student(String id,String name, int grade, int groupNo) {
		this.id = id;
		this.name = name;
		this.grade = grade;
		this.groupNo = groupNo;
	}

	public Student() {

	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public int getGrade() {
		return grade;
	}

	public void setGrade(int grade) {
		this.grade = grade;
	}
	
	public int getGroupNo() {
		return groupNo;
	}

	public void setGroupNo(int groupNo) {
		this.groupNo = groupNo;
	}
}
