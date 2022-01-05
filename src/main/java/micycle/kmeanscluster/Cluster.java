package micycle.kmeanscluster;

import java.util.ArrayList;
import java.util.List;

// At the beginning of each iteration, clusterPoints is empty, and the center is the new mean point
class Cluster extends Point {

	private List<Integer> clusterPoints;
	private double[] sumOfPoints;

	Cluster(Point p) {
		super(p.pos);
		clusterPoints = new ArrayList<Integer>();
		this.sumOfPoints = new double[this.dimension];
	}

	void addPointToCluster(int index) {
		Point p = KMeans.POINTS.get(index);
		clusterPoints.add(index);
		double[] po = p.getPosition();
		for (int i = 0; i < this.dimension; ++i) {
			sumOfPoints[i] += po[i];
		}
	}

	Cluster getNewCenter() {
		double[] pos = new double[KMeans.DIMENSION];
		for (int i = 0; i < this.dimension; ++i) {
			pos[i] = sumOfPoints[i] / this.clusterPoints.size();
		}
		return new Cluster(new Point(pos));
	}

	double evaluate() {
		double ret = 0.0;
		for (int in : clusterPoints) {
			ret += this.dist(KMeans.POINTS.get(in));
		}
		return ret;
	}

	ArrayList<Integer> belongingPoints() {
		return new ArrayList<Integer>(this.clusterPoints);
	}
}
