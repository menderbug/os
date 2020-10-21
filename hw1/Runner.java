package hw1;
import java.util.Arrays;

public class Runner {

	public static void main(String[] args) {
		for (int i = 2; i < 40; i++) {
			String[] s = {Integer.toString(i)};
			Simulation2.main(s);
		}
	}
	
}
