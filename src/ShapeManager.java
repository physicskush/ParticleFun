import java.util.ArrayList;
import java.util.Iterator;

/**
 * ShapeManager.java
 * 
 */

public class ShapeManager {
	String	shapeType; // circle , square, pentagon , hexagon 
	Point2D	center;
	ArrayList<Point2D>	coordinates = new ArrayList<Point2D>();

	boolean	shapeIsDraggable;
	double	currentAngle;
	double	startAngle; 
	double	anchorX;
	double	anchorY;

	/**
	 * Constructor
	 * 
	 * @param panelSize
	 */
	public ShapeManager(Point2D panelSize) { this.center = panelSize; }

	
	/** 
	 * Method to calculate the angle formed between the mouse point and the origin point.
	 * 
	 * @param xc	x-value of the origin (center in this case)
	 * @param yc	y-value of the origin (center in this case)
	 * @param x 	x-value of the current point (pointed with the mouse)
	 * @param y 	y-value of the current point (pointed with the mouse)
	 * @return double	the angle
	 */
	public double getAngle(double xc, double yc, double x, double y){
		double	angle;
		double	dy = y - yc;
		double	dx = x - xc;
		if ( dx == 0 ) {
			angle = dy >= 0 ? Math.PI/2 : -Math.PI/2;
		} else {
			angle = Math.atan(dy/dx);
			if (dx < 0 ) angle += Math.PI;
		}
		return angle;
	}

	
	/** 
	 * Defensive copy
	 * 
	 * @return ArrayList<Point2D>	a copy of Point2D coordinated
	 */
	public ArrayList<Point2D> getCopy() { return (ArrayList<Point2D>) coordinates.clone(); }

	
	/** 
	 * Method that sets the value for the anchor, to be used for shape rotation through the mouse.
	 * 
	 * @param x		the x-value for the anchor
	 * @param y 	the y-value for the anchor
	 */
	public void setAnchor(double x, double y){
		this.anchorX = x;
		this.anchorY = y;
	}

	
	/** 
	 * Sets the flag for the shapes to be rotatable throught the mouse.
	 * 
	 * @param particles 	the list of particles on the canvas
	 */
	public void setShapeIsDraggable(boolean shapeIsDraggable) { this.shapeIsDraggable = shapeIsDraggable; }

	
	/** 
	 * Method that computes the coordinates on the canvas that form a circular shape.
	 * 
	 * @param particles 	the list of particles elements present on the canvas
	 */
	public void getCircleCoords(ArrayList<Particle> particles) {
		int 	n = particles.size();
		double	alpha = Math.toRadians(360.0/n);    // angle of each triangle in the polygon
		float	angle = 0;
		double	pW = particles.get(0).width;
		double	pH = particles.get(0).height;
		float	side = (float) (particles.get(0).getWidth());
		float	radius = (float) (side / (2*Math.sin(Math.PI / n)));

		if (!(radius >= center.x || radius >= center.y)) {

			for (int i = 0 ; i < n ; i++) {
				this.coordinates.add(new Point2D((center.x + Math.sin(angle)*radius -pW/2),
						(center.y -Math.cos(angle)*radius) - pH/2));
				angle += alpha;
			}
		}
	}
	
	/** 
	 * Method that computes the coordinates on the canvas that form a square shape.
	 * 
	 * @param particles 	the list of particles elements present on the canvas
	 */
	public void getSquareCoords(ArrayList<Particle> particles) {
		int	n = particles.size();
		int	layer = n/4;
		double	width = particles.get(0).width;
		double	rescale = 0;

		if ( layer %2 == 0 ) rescale = 0.5; 

		double k = -(layer/2+0.5);

		for (double i = k; i <= k+layer ;i+=1) {
			for (double j = k ; j <= k+layer ;j+=1) {
				if (!((i > k && i < (k+layer)) && (j > k && j < (k + layer)))) {
					this.coordinates.add(new Point2D(center.x - (j+rescale)*width,
							center.y - (i-rescale)*width));
				}

			}
		}

	}

	
	/** 
	 * Method that computes the coordinates on the canvas that form a diamond shape.
	 * 
	 * @param particles 	the list of particles elements present on the canvas
	 */
	public void getDiamondCoords(ArrayList<Particle> particles) {
		getSquareCoords(particles);

		for (Point2D point : this.coordinates){

			//initial coords 
			double x = point.x;
			double y = point.y;

			//offset to center
			double rescaleX = center.x - point.x ;
			double rescaleY = center.y - point.y ;
			point.x = rescaleX;
			point.y = rescaleY;

			//rotation
			double newPosX = point.x*Math.cos(Math.toRadians(45)) - point.y*Math.sin(Math.toRadians(45));
			double newPosY = point.x*Math.sin(Math.toRadians(45)) + point.y*Math.cos(Math.toRadians(45));

			// stretch 
			newPosX *= 0.6;
			newPosY *= 1.3;

			double diffX = newPosX - rescaleX; 
			double diffY = newPosY - rescaleY; 

			point.x = x - diffX;
			point.y = y - diffY;


		}
	}

	
	/** 
	 * Method that computes the coordinates on the canvas that form a spiral shape.
	 * 
	 * @param particles 	the list of particles elements present on the canvas
	 */
	public void getSpiralCoords(ArrayList<Particle> particles){
		double	rotation = -Math.PI / 2;
		int	awayStep = (int) particles.get(0).width/2;
		int	chord = awayStep*3; //distance between points
		double	theta = chord / awayStep;
		
		this.coordinates.add(new Point2D(center.x, center.y));

		for (int i = 1 ; i < particles.size() ; i++) {
			double	away = awayStep*theta;  //how far away from center
			double	around = theta + rotation; //how far around the center
			double	x = center.x + Math.cos(around) * away;
			double	y = center.y + Math.sin(around) * away;

			this.coordinates.add(new Point2D(x, y));
			theta += chord / away;
		}
	}

	
	/** 
	 * Method that computes the coordinates on the canvas that form a loose spiral shape.
	 * 
	 * @param particles 	the list of particles elements present on the canvas
	 */
	public void getLooseSpiralCoords(ArrayList<Particle> particles) { 
		int awayStep = (int) particles.get(0).width*2;
		double rotation = -Math.PI / 2;
		int chord = awayStep/2; //distance between points
		double delta; 
		double theta = chord / awayStep;

		this.coordinates.add(new Point2D(center.x, center.y));

		for (int i = 1 ; i < particles.size() ; i++) {
			double away = awayStep*theta;  //how far away from center
			double around = theta + rotation; //how far around the center
			double x = center.x + Math.cos(around) * away;
			double y = center.y + Math.sin(around) * away;

			this.coordinates.add(new Point2D(x, y));

			delta = ( -2 * away + Math.sqrt( 4 * away * away + 8 * awayStep * chord ) ) / ( 2 * awayStep );
			theta += delta;
		}
	}

	
	/** 
	 * Method that computes the coordinates on the canvas that form a sunflower spiral shape.
	 * 
	 * @param particles 	the list of particles elements present on the canvas
	 * @param angle 		the base angle in the coordinates computation
	 */
	public void getSunflowerCoords(ArrayList<Particle> particles ,double angle) {
		double	localMultiplier = 1.2*particles.get(0).width; //guess
		double 	baseAngle = angle;

		for (int i = 0 ; i < particles.size() ; i++){
			double	angle2 = baseAngle * i;
			double	x = Math.sqrt(i) * Math.cos(angle2) * localMultiplier;
			double	y = Math.sqrt(i) * Math.sin(angle2) * localMultiplier;

			this.coordinates.add(new Point2D(x + center.x, y + center.y));
		}
	}

	
	/** 
	 * Method that performs a rotation on a shape present on the canvas.
	 * 
	 * @param x 		x-value on the screen pointed by the mouse
	 * @param y			y-value on the screen pointed by the mouse
	 * @param particles the list of particles present on the canvas
	 */
	public void rotateShape(double x, double y, ArrayList<Particle> particles) {
		currentAngle = getAngle(center.x, center.y, x, y);
		double angle = (currentAngle - startAngle) / 10;

		if (Math.abs(angle) >= 0.4) angle /= -10;

		for (int i = 0 ; i < particles.size() ; i++) {
			Particle particle = particles.get(i);

			//initial coords 
			double initialX = particle.x;
			double initialY = particle.y;

			//rescale origin to center
			double rescaleX  = center.x - particle.x;
			double rescaleY  = center.y - particle.y;
			particle.x = rescaleX;
			particle.y = rescaleY;

			//rotation
			double newPosX = particle.x*Math.cos(angle) - particle.y*Math.sin(angle);
			double newPosY = particle.x*Math.sin(angle) + particle.y*Math.cos(angle);

			double diffX = newPosX - rescaleX; 
			double diffY = newPosY - rescaleY; 

			particle.x = initialX - diffX;
			particle.y = initialY - diffY;
		}
	} 

	
	/** 
	 * Method that computes the distances between all particles and all the coordinates.
	 * 
	 * @param pList 	list of particles on the canvas
	 * @param second 	list of Point2D coordinates representing the shape's coordinates.
	 * @return ArrayList<Double>	all the distances between all points and all coordinates
	 */
	public ArrayList<Double> calculateDistance(ArrayList<Particle> pList, ArrayList<Point2D> second) {
		ArrayList<Double>	distances = new ArrayList<Double>();
		Iterator<Particle>	iterator1 = pList.iterator();
		while (iterator1.hasNext()) {
			Particle iterated = iterator1.next();

			for (int i = 0 ; i<second.size() ; i++){
				Point2D	point = second.get(i);
				double	d = Math.sqrt(Math.pow(iterated.x -point.x, 2)
						+ Math.pow(iterated.y - point.y, 2));

				distances.add(d);
			}
		}
		return distances;
	}
	
	/** 
	 * Method that sets for each point an associated closest coordinate.
	 *  
	 * @param particles 	list of particles on the canvas
	 */
	public void setProximity(ArrayList<Particle> particles){
		ArrayList<Particle>	particlesCopy = (ArrayList<Particle>) particles.clone();
		ArrayList<Point2D>	coordinatesCopy = this.getCopy();
		ArrayList<Double>	distances;
		Iterator<Particle>	iterator1 = particlesCopy.iterator();

		while (iterator1.hasNext()) {
			Particle particle = iterator1.next(); 			
			Iterator<Point2D> iterator2 = coordinatesCopy.iterator();
			distances = calculateDistance(particlesCopy, coordinatesCopy);

			int i = 0;
			while (iterator2.hasNext()) {

				Point2D	coordinate = iterator2.next();
				double	d = Math.sqrt(Math.pow(particle.x -coordinate.x, 2)
						+ Math.pow(particle.y - coordinate.y, 2));

				if (d == distances.get(i)) {
					//int index = distances.indexOf((double) Collections.min(distances));
					coordinate.particle = particle;
					iterator1.remove();
					iterator2.remove();
					i++;
					break;
				}
			}

		}


	}

	
	/** 
	 * TODO 
	 * 
	 * @param particles 	list of particles on the canvas
	 */
	public void jiggle(ArrayList<Particle> particles) {
		// if odd number of particles , then need to set last 2 in the list 
		// to get smaller but at a slower rate then the other before 

		int k = 0;
		for (int i = 0 ; i < particles.size() ; i++) {
			if (k % 2 ==0 ) {
				for (Particle p : particles) {

				}
				//enable particle bigger

				//particles are not in a linked list though 
			}
		}
	}

	
	/** 
	 * Method that sets the speed of particles in direction of it's respective coordinate
	 * 
	 * @param particles 	list of particles on the canvas
	 */
	public void setSpeed(ArrayList<Particle> particles) {
		for (int i = 0 ; i < coordinates.size() ; i++) {
			Point2D point = coordinates.get(i);

			if (point.particle.x - point.x <= 0) {
				point.particle.vx = (-point.particle.x + point.x)/(1000/16);
			} else if (point.particle.x - point.x > 0 ) {
				point.particle.vx = -(point.particle.x - point.x)/(1000/16);
			}

			if (point.particle.y - point.y <= 0) {
				point.particle.vy = (-point.particle.y + point.y)/(1000/16);
			} else if (point.particle.y - point.y > 0 ) {
				point.particle.vy = -(point.particle.y - point.y)/(1000/16);
			}
		}
	}


	
	/** 
	 * Method that sets the speed of particles to 0 if they arrived at its associated coordinate.
	 * 
	 * @return boolean 	true if all the particles have arrived, false otherwise
	 */
	public boolean checkArrival() {
		boolean allParticlesArrived = true;

		for (int i = 0 ; i < coordinates.size() ; i++){
			Point2D p = coordinates.get(i);

			if (p.particle.x >= p.x - p.particle.width/25 && p.particle.x <= p.x + p.particle.width/25) {
				p.particle.vx = 0;
			} else {
				allParticlesArrived = false;
			}

			if (p.particle.y >= p.y - p.particle.height/25 && p.particle.y <= p.y + p.particle.height/25) {
				p.particle.vy = 0;
			} else {
				allParticlesArrived = false;
			}
		}

		return allParticlesArrived;
	}


	/** 
	 * Method that reinitializes the coordinates.
	 */
	public void reinitializeCoordinates() { this.coordinates = new ArrayList<Point2D>(); }


}
