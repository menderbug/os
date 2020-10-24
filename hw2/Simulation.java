package hw2;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;

//REQUIRED: FIFO
//REQUIRED: second chance (clock)
//OPTIONAL: optimal
//OPTIONAL: LRU
//OPTIONAL: LFU, MFU
//OPTIONAL: enhanced second chance

public class Simulation {
	
	public enum Request {EQUIPROBABLE, EXPONENTIAL, BIASED}
	
	
	//public final int FAULT_TIME = 10;		//TODO optional parameter
	public final int NUM_FRAMES = 5;
	public final int NUM_PAGES = 20;
	public final long NUM_REQUESTS = 10000;
	
	public final double LAMBDA = 0.6;
	
	private long faults;
	private LinkedList<Page> requestMaster = new LinkedList<Page>();
	
	public static void main(String[] args) {
		
		Simulation sim = new Simulation();
		sim.generateProcesses(Request.EXPONENTIAL);
		sim.FIFO(new LinkedList<Page>(sim.requestMaster));
		sim.secondChance(new LinkedList<Page>(sim.requestMaster));
		
	}
	
	
	public void generateProcesses(Request type) {
		requestMaster.clear();
		Random r = new Random();
		
		if (type == Request.EQUIPROBABLE)
			for (long l = 0; l < NUM_REQUESTS; l++)		
				requestMaster.add(new Page(l, r.nextInt(NUM_PAGES) + 1));	//maybe loss of precision from Math.random TODO
		else if (type == Request.EXPONENTIAL)
			for (long l = 0; l < NUM_REQUESTS; l++)		
				requestMaster.add(new Page(l, exponential()));
		else if (type == Request.BIASED)
			for (long l = 0; l < NUM_REQUESTS; l++)			//some hard coded numbers here TODO
				requestMaster.add(new Page(l, r.nextFloat() < 0.8 ? r.nextInt(5) + 100 : exponential()));
	}
	
	public void FIFO(LinkedList<Page> request) {
		faults = 0;
		PriorityQueue<Page> memory = new PriorityQueue<Page>();
		
		while (!request.isEmpty()) {
			Page current = request.poll();
			if (memory.contains(current)) continue;
			faults++;
			if (memory.size() >= NUM_FRAMES)
				memory.poll();
			memory.add(current);
		}
		
		System.out.println((double) faults / NUM_REQUESTS);
	}
	
	public void secondChance(LinkedList<Page> request) {		//repeat code but i think its fine for clarity?
		faults = 0;
		long max = NUM_REQUESTS;
		PriorityQueue<Page> memory = new PriorityQueue<Page>();
		
		while (!request.isEmpty()) {
			Page current = request.poll();
			if (memory.contains(current)) {
				memory.stream().filter(p -> p.equals(current)).findAny().get().hasSecondChance = true;
				continue;
			}
			faults++;
			if (memory.size() >= NUM_FRAMES) {
				while (memory.peek().hasSecondChance)
					memory.add(new Page(max++, memory.poll().ID));
				memory.poll();
			}
			memory.add(current);
		}
		
		System.out.println((double) faults / NUM_REQUESTS);
	}
	
	public void LRU(LinkedList<Page> request) {
		faults = 0;
		long max = NUM_REQUESTS;
		PriorityQueue<Page> memory = new PriorityQueue<Page>();
		
		while (!request.isEmpty()) {
			Page current = request.poll();	//only LRU difference from FIFO, i think
			current.timestamp = max++;
			if (memory.contains(current)) continue;
			faults++;
			if (memory.size() >= NUM_FRAMES)
				memory.poll();
			memory.add(current);
		}
		
		System.out.println((double) faults / NUM_REQUESTS);
	}
		
	public void optimal(LinkedList<Page> request) {
		
		Map<Integer, LinkedList<Page>> ordering = new HashMap<Integer, LinkedList<Page>>();
		while (!request.isEmpty()) {
			Page current = request.pop();
			ordering.putIfAbsent(current.ID, new LinkedList<Page>());
			ordering.get(current.ID).add(current);					
		}
		
		faults = 0;
		List<Page> memory = new LinkedList<Page>();
		
		while (!request.isEmpty()) {
			Page current = ordering.values().stream().sorted(Comparator.comparing(l -> l.peek())).findFirst().get().pop();
			if (memory.contains(current)) continue;
			faults++;
			if (memory.size() >= NUM_FRAMES) {
				Page victim = null;
				for (Page page : memory)
					if (ordering.containsKey(page.ID)) {
						victim = page;
						break;
					}
				if (victim == null) {/* TODO basically now it compares by timestamp ugghhhh */}
				for (Page p : memory) if (p.equals(victim)) memory.remove(p);
			}
			memory.add(current);
		}
		
		System.out.println((double) faults / NUM_REQUESTS);
		
		
	}

	private int exponential() {
		int k = (int) Math.ceil((Math.log(Math.random()) / -LAMBDA));
		return k > NUM_PAGES ? exponential() : k;	//TODO this needs work
	}

	

}
