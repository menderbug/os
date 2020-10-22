package hw2;

public class Page implements Comparable<Page> {
	public long timestamp;
	public int ID;
	public boolean secondChance = false;
	
	public Page(long timestamp, int ID) {
		this.timestamp = timestamp;
		this.ID = ID;
	}

	@Override
	public int compareTo(Page other) {
		return Long.compare(timestamp, other.timestamp);
	}
	
	
}
