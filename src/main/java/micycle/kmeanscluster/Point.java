package micycle.kmeanscluster;

/**
 * An N-dimensional point.
 */
class Point {

	protected double[] pos;
	protected int dimension;

	public Point(int size) {
		pos = new double[size];
		this.dimension = size;
	}

	public Point(double... p) {
		this.pos = p;
		this.dimension = pos.length;
	}

	int getDimension() {
		return this.dimension;
	}

	double[] getPosition() {
		return pos;
	}

	/**
	 * Calculates the euclidean distance between this point and the other point.
	 * Assumes the dimensions of each point are the same (this is not checked).
	 * 
	 * @param o other point
	 * @return
	 */
	double dist(Point o) {
		double sum = 0.0;
		for (int i = 0; i < pos.length; ++i) {
			final double d = pos[i] - o.pos[i];
			sum += d * d;
		}
		return Math.sqrt(sum);
	}

	double distSquared(Point o) {
		double sum = 0.0;
		for (int i = 0; i < pos.length; ++i) {
			final double d = pos[i] - o.pos[i];
			sum += d * d;
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
