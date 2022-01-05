package micycle.kmeanscluster;

/**
 * TODO make as interface; implement 2d, 3d, Nd
 */
class Point {
	
	protected double[] pos;
	protected int dimension;

	public Point(int size) {
		pos = new double[size];
		this.dimension = size;
	}

	public Point(double[] p) {
		this.pos = p;
		this.dimension = pos.length;
	}

	int getDimension() {
		return this.dimension;
	}

	double[] getPosition() {
		return pos.clone();
	}

	public static double euclideanDistance(Point p1, Point p2) {
		double[] p = new double[p1.pos.length];
		for (int i = 0; i < p1.pos.length; ++i) {
			p[i] = p1.pos[i] - p2.pos[i];
		}
		double sum = 0.0;
		for (int i = 0; i < p1.pos.length; ++i) {
			sum += p[i] * p[i];
		}
		return Math.sqrt(sum);
	}

	public static double squareDistance(Point p1, Point p2) {
		double[] p = new double[p1.pos.length];
		for (int i = 0; i < p1.pos.length; ++i) {
			p[i] = p1.pos[i] - p2.pos[i];
		}
		double sum = 0.0;
		for (int i = 0; i < p1.pos.length; ++i) {
			sum += p[i] * p[i];
		}
		return sum;
	}

	@Override
	public boolean equals(Object o) {
		Point p = (Point) o;
		if (this.dimension != p.dimension) {
			return false;
		}
		for (int i = 0; i < this.dimension; i++) {
			if (this.pos[i] != p.pos[i]) {
				return false;
			}
		}
		return true;
	}
}
