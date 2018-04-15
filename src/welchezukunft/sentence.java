package welchezukunft;

public class sentence {
	private int sentence_id;
	private int count;
	
	public sentence(int sentence_id) {
		this.sentence_id = sentence_id;
		this.count = 1;
	}

	public int getSentence_id() {
		return sentence_id;
	}

	public void setSentence_id(int sentence_id) {
		this.sentence_id = sentence_id;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}
	public void addCount() {
		this.count++;
	}
	
	
	
	
	
}
