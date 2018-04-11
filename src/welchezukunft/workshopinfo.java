package welchezukunft;

public class workshopinfo {

	private int id;
	private String name;
	
	workshopinfo(Integer Value, String name){
		this.id = Value;
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	
}
