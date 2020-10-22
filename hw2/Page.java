package hw2;

public class Page implements Comparable<Page> {
	public long timestamp;
	public int ID;
	public int frequency;
	public boolean secondChance = false;
	
	public Page(long timestamp, int ID) {
		this.timestamp = timestamp;
		this.ID = ID;
		this.frequency = 0;
	}
	
	@Override
	public boolean equals(Object o) {
		return o instanceof Page && ID == ((Page) o).ID;
	}
	
	@Override
	public int hashCode() {return ID;}

	@Override
	public int compareTo(Page other) {
		return Long.compare(timestamp, other.timestamp);
	}
	
	
}
