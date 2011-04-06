import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * This is the stub code for what you need to implement. All your code should go
 * into this file. Look at the abstract class implementation in RobotStategy.java
 * and read the comments thoroughly. 
 * 
 */
public class MyRobotStrategy extends RobotStrategy {
	Random rand = new Random();
	
	private static final double ZERO = 0.0;
	private static final double P_STATIONARY = 0.52;
	
	private static double IMPATIENCE = 0.06;
	private static double RED_THRESHOLD = 0.61;
	private static double YELLOW_THRESHOLD = 0.65;
	
//	IMPATIENCE = 0.09
//	RED_THRESHOLD = 0.5699999999999998
//	YELLOW_THRESHOLD = 0.65
//	10000/10000 games played; 56.158690176322416% wins
	
//	IMPATIENCE = 0.060000000000000005
//	RED_THRESHOLD = 0.5999999999999999
//	YELLOW_THRESHOLD = 0.65
//	10000/10000 games played; 56.056477582363144% wins
	
//	IMPATIENCE = 0.060000000000000005
//	RED_THRESHOLD = 0.6099999999999999
//	YELLOW_THRESHOLD = 0.65
//	10000/10000 games played; 56.343330405320614% wins	

	private int redAmmo = 1, yellowAmmo = 2;
	private List<Observation> observations = new ArrayList<Observation>();
	/**
	 * Rename your bot as you please. This name will show up in the GUI.
	 */
	public String getName() { 
		// Ideas: Sir Killalot, Aimbot, Maphack
		return "Aimbot"; 
	}
	
	public static void main(String args[]) {
		YELLOW_THRESHOLD = 0.45;
		for (int i = 0; i < 20; i++) {
			RED_THRESHOLD = 0.50;
			for (int j = 0; j < 20; j++) {
				IMPATIENCE = 0.00;
				for (int k = 0; k < 20; k++) {
					System.out.println("\nIMPATIENCE = " + IMPATIENCE);
					System.out.println("RED_THRESHOLD = " + RED_THRESHOLD);
					System.out.println("YELLOW_THRESHOLD = " + YELLOW_THRESHOLD);
					try {
						RobotTester.main(new String[] {""});
					} catch (Exception e) {
						e.printStackTrace();
					}
					IMPATIENCE += 0.01;
				}
				RED_THRESHOLD -= 0.01;
			}
			YELLOW_THRESHOLD -= 0.01;
		}
	}

	/** 
	 * In this function, we provide you with the sensor information (in the form
	 * of 4 binary values), and the grid coordinates of your robot.
	 * 
	 * The grid is a 6x6 grid, starting at (x=0,y=0) in the top left corner.
	 * We represent this as a flat array "beliefs" with 36 probabilities.
	 * index(x,y) is the index of position (x,y) into the array beliefs  
	 * 
	 * The sensors are in the following order: SWNE, so sensor[1] is West. The
	 * sensors are probabilistic. The vector between your robot and the other
	 * robot is mapped into probabilities of your sensor firing. So if the other
	 * robot is two squares South, and a single square West, then there is a 2/3
	 * chance of the South sensor firing, and a 1/3 chance of the West sensor
	 * firing. Note if the other robot was four squares South and 2 squares
	 * West, your robot's sensors would fire with the same probability.
	 * 
	 * @param sensor
	 *            This round's sensor information
	 * @param xPos
	 *            Robot's X coord
	 * @param yPos
	 *            Robot's Y coord
	 */
	public void updateBeliefState(boolean[] sensor, int xPos, int yPos) {
		// If no sensor data, keep old belief state
//		if (!sensor[0] && !sensor[1] && !sensor[2] && !sensor[3])
//			return;
		
		Observation o = new Observation(sensor, xPos, yPos);
		
		/* ************************** OBSERVATION PROBABILITY ************************** */
		double[][] observation = getObservation(o.sensor, o.pos.x, o.pos.y);
		
		/* ************************** TRANSITION PROBABILITY ************************** */
		double[][] transition = getTransition();
		
		for (int x = 0; x < w; x++)
			for (int y = 0; y < h; y++)
				beliefState[x][y] = transition[x][y] * observation[x][y];
		
		beliefState = normalize(beliefState);
		
		for (int x = 0; x < w; x++)
			for (int y = 0; y < h; y++)
				if (Double.isNaN(beliefState[x][y])) {
					beliefState = observation;
					break;
				}
		
//		System.out.println(stateToString(getSumTransition()));
//		System.out.println(stateToString(getObservation(new boolean[] {false, false, true, false}, 5, 5)));
		
		observations.add(o);
	}
	
	private double[][] getObservation(boolean[] sensor, int xPos, int yPos) {
		double[][] observation = new double[w][h];
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				observation[x][y] = 1.0;
				if ((sensor[WEST] && x >= xPos) || (sensor[EAST] && x <= xPos) ||
					(sensor[SOUTH] && y <= yPos) || (sensor[NORTH] && y >= yPos) ||
					(!sensor[NORTH] && x == xPos && y <= yPos) || 
					(!sensor[SOUTH] && x == xPos && y >= yPos) ||
					(!sensor[WEST] && y == yPos && x <= xPos) ||
					(!sensor[EAST] && y == yPos && x >= xPos) ||
					(x == xPos && y == yPos))
						observation[x][y] = ZERO;
				else {
					double value = 0.0;
					if (sensor[NORTH] || sensor[SOUTH]) {
						value = ((double)Math.abs(y - yPos)) / 
							((double)Math.abs(x - xPos) + (double)Math.abs(y  - yPos));
						if (value != ZERO)
							observation[x][y] *= value;
					}
					if (sensor[EAST] || sensor[WEST]) {
						value = ((double)Math.abs(x - xPos)) / 
							((double)Math.abs(x - xPos) + (double)Math.abs(y  - yPos));
						if (value != ZERO)
							observation[x][y] *= value;
					}
//					int count = 0;
//					for(boolean fire : sensor)
//						if (fire)
//							count++;
//					if (count > 1)
//						observation[x][y] *= 2;
				}
			}
		}
		return observation;
	}
	
	public double[][] getTransition() {
		double[][] transition = new double[w][h];
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				double p_stationary = P_STATIONARY; 
				if (x == 0 || x == w-1)
					p_stationary += P_STATIONARY/4.0;
				if (y == 0 || y == h-1)
					p_stationary += P_STATIONARY/4.0;
				transition[x][y] = beliefState[x][y]*p_stationary;
				// "Bleed" into each square, each adjacent square's probability * 
				// the probability the enemy will move to this one
				if (x > 0)
					transition[x][y] += ((1.0-P_STATIONARY)/4.0)*beliefState[x-1][y];
				if (y > 0)
					transition[x][y] += ((1.0-P_STATIONARY)/4.0)*beliefState[x][y-1];
				if (y < h-1)
					transition[x][y] += ((1.0-P_STATIONARY)/4.0)*beliefState[x][y+1];
				if (x < w-1)
					transition[x][y] += ((1.0-P_STATIONARY)/4.0)*beliefState[x+1][y];
			}
		}
		return transition;
	}
	
	private Coordinate getMax() {
		int max_X =  0, max_Y = 0;
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				if (beliefState[x][y] > beliefState[max_X][max_Y]) {
					max_X = x;
					max_Y = y;
				} else if (beliefState[x][y] == beliefState[max_X][max_Y] &&
						rand.nextDouble() > 0.5) {
					max_X = x;
					max_Y = y;
				}
			}
		}
		return new Coordinate(max_X, max_Y);
	}
	
	public Order giveOrder() {
		Coordinate max = getMax();
		int order = Order.GREEN_CANNON;
		if (beliefState[max.x][max.y] > (RED_THRESHOLD - (observations.size()*IMPATIENCE)) && redAmmo > 0) {
			order = Order.RED_CANNON;
			redAmmo--;
		} else if (beliefState[max.x][max.y] > (YELLOW_THRESHOLD - observations.size()*IMPATIENCE) && yellowAmmo > 0) {
			order = Order.YELLOW_CANNON;
			yellowAmmo--;
		}
			
		return new Order(order, max.x, max.y);
	}
	
	private double[][] normalize(double[][] belief) {
		double[][] state = belief.clone();
		double sum = 0.0;
		for(int x = 0; x < w; x++)
			for(int y = 0; y < h; y++)
				sum += state[x][y];
		
		for(int x = 0 ; x < w; x++)
			for(int y = 0; y < h; y++)
				state[x][y] /= sum;
		return state;
	}
	
	public String stateToString(double[][] array) {
		final int DIGITS = 5;
		StringBuilder builder = new StringBuilder();
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				String state = Double.toString(array[x][y]);
				if (state.equals("0.0")) {
					for (int i = 3; i < DIGITS; i++)
						state += "0";
				} else {
					try {
						state = state.subSequence(0, DIGITS).toString();
					} catch (Exception e) {
						while(state.length() < DIGITS)
							state += "0";
					}
				}
				builder.append("[" + state + "] ");
			}
			builder.append("\n");
		}
		return builder.toString();
	}
	
	private class Coordinate {
		public int x, y;
		
		public Coordinate(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}
	
	private class Observation {
		public Coordinate pos;
		public boolean[] sensor;
		
		public Observation(boolean[] sensor, int x, int y) {
			this.pos = new Coordinate(x, y);
			this.sensor = sensor;
		}
	}
}
