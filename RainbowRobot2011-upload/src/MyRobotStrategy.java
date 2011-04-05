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
	private static final double RED_THRESHOLD = 0.85;
	private static final double YELLOW_THRESHOLD = 0.75;
	
	private int redAmmo = 1, yellowAmmo = 2;
	
	// A cache of the last sensor reading
	private boolean[][] sensorCache = {{false, false, false, false},
									   {false, false, false, false}};
	
	/**
	 * Rename your bot as you please. This name will show up in the GUI.
	 */
	public String getName() { 
		return "Sir Killalot"; 
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
		if (!sensor[0] && !sensor[1] && !sensor[2] && !sensor[3])
			return;
		
		// Populate remaining states with new base probability
		
		
		// OBSERVATION PROBABILITY
		
		
		/* ************* Rule out known boundary crossing ************* */
		// If N-S or E-W sensor values have changed from state before last, 
		// we know the enemy is within one row or column 
		// Enemy has moved from north to south
		if ((sensor[SOUTH] && sensorCache[1][NORTH])) {
			for (int x = 0; x < w; x++) {
				for (int y = 0; y < h; y++) {
					if (y != yPos && y != yPos+1)
						beliefState[x][y] = ZERO;
				}
			}
		}
		
		// Enemy has moved from east to west
		if ((sensor[WEST] && sensorCache[1][EAST])) {
			for (int x = 0; x < w; x++) {
				for (int y = 0; y < h; y++) {
					if (x != xPos && x != xPos-1)
						beliefState[x][y] = ZERO;
				}
			}
		}
		
		// Enemy has moved from south to north
		if ((sensor[NORTH] && sensorCache[1][SOUTH])) {
			for (int x = 0; x < w; x++) {
				for (int y = 0; y < h; y++) {
					if (y != yPos && y != yPos-1)
						beliefState[x][y] = ZERO;
				}
			}
		}
		
		// Enemy has moved from west to east
		if ((sensor[EAST] && sensorCache[1][WEST])) {
			for (int x = 0; x < w; x++) {
				for (int y = 0; y < h; y++) {
					if (x != xPos && x != xPos+1)
						beliefState[x][y] = ZERO;
				}
			}
		}
		
		
		/* ************* Rule out impossible quadrants ************* */
		// If south sensor triggered, rule out north
		if (sensor[SOUTH]) {
			for (int x = 0; x < w; x++) {
				for (int y = 0; y < yPos; y++) {
					if (beliefState[x][y] != ZERO)
						beliefState[x][y] = ZERO;
				}
			}
		}
		
		// If west sensor triggered, rule out east
		if (sensor[WEST]) {
			for (int x = xPos; x < w; x++) {
				for (int y = 0; y < h; y++)
					beliefState[x][y] = ZERO;
			}
		}
		
		// If north sensor triggered, rule out south
		if (sensor[NORTH]) {
			for (int x = 0; x < w; x++) {
				for (int y = yPos; y < h; y++)
					beliefState[x][y] = ZERO;
			}
		}
		
		// If east sensor triggered, rule out west
		if (sensor[EAST]) {
			for (int x = 0; x < xPos; x++) {
				for (int y = 0; y < h; y++)
					beliefState[x][y] = ZERO;
			}
		}
		
		normalize();
	
		// Cycle the sensor cache
		sensorCache[1] = sensorCache[0];
		sensorCache[0] = sensor;
		
	}
	
	public Order giveOrder() {
		int max_X =  0, max_Y = 0;
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				if (beliefState[x][y] > beliefState[max_X][max_Y]) {
					max_X = x;
					max_Y = y;
				}
			}
		}
		
		int order = Order.GREEN_CANNON;
		if (beliefState[max_X][max_Y] > RED_THRESHOLD && redAmmo > 0)
			order = Order.RED_CANNON;
		else if (beliefState[max_X][max_Y] > YELLOW_THRESHOLD && yellowAmmo > 0)
			order = Order.YELLOW_CANNON;
			
		return new Order(order, max_X, max_Y);
	}
	
	public void reset() {
		for (int x = 0; x < w; x++)
			for (int y = 0; y < h; y++)
				if (beliefState[x][y] == ZERO)
					beliefState[x][y] = 1.0 / ((double)(w*h));
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++)
				builder.append("[" + (float)beliefState[x][y] + "] ");
			builder.append("\n");
		}
		return builder.toString();
	}
	
}