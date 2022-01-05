package micycle.kmeanscluster;

import java.util.AbstractMap.SimpleEntry;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.PriorityQueue;

class Hypersphere extends Point {

	private double radius;
	private LinkedList<Integer> instances;
	private Hypersphere[] children;
	private double[] sumOfPoints;
	static int COUNT = 0, ALL_COUNT = 0;

	Hypersphere(Point center, double r, LinkedList<Integer> ins) {
		super(center.pos);
		this.radius = r;
		this.instances = ins;
		sumOfPoints = new double[KMeans.DIMENSION];
	}

	Hypersphere() {
		super(new double[KMeans.DIMENSION]);
		instances = new LinkedList<Integer>();
		sumOfPoints = new double[KMeans.DIMENSION];
	}

	void addInstance(int index) {
		instances.add(index);
		double[] pos = KMeans.POINTS.get(index).getPosition();
		for (int i = 0; i < KMeans.DIMENSION; i++) {
			sumOfPoints[i] += pos[i];
		}
	}

	void endAdding() {
		int size = instances.size();
		for (int i = 0; i < KMeans.DIMENSION; i++) {
			this.pos[i] = this.sumOfPoints[i] / size;
		}
		this.radius = this.dist(KMeans.POINTS.get(this.getFarestPoint(this)));
	}

	int size() {
		return instances.size();
	}

	double maxDistance(Point p) {
		return radius + this.dist(p);
	}

	double minDistance(Point p) {
		return this.dist(p) - radius;
	}

	/**
	 * @return -1 If not in a separate cluster, otherwise the index of the cluster
	 *         center
	 */
	int isInSingleCluster() {
		ALL_COUNT++;
		PriorityQueue<Entry<Integer, Double>> maxpq = new PriorityQueue<Entry<Integer, Double>>(KMeans.CENTERS.size(),
				(e1, e2) -> Double.compare(e1.getValue(), e2.getValue()));
		PriorityQueue<Entry<Integer, Double>> minpq = new PriorityQueue<Entry<Integer, Double>>(KMeans.CENTERS.size(),
				(e1, e2) -> Double.compare(e1.getValue(), e2.getValue()));

		int index = 0;
		for (Cluster cen : KMeans.CENTERS) {
			maxpq.add(new SimpleEntry<Integer, Double>(index, this.maxDistance(cen)));
			minpq.add(new SimpleEntry<Integer, Double>(index, this.minDistance(cen)));
			index++;
		}
		Entry<Integer, Double> the = maxpq.poll(), comp;
		index = the.getKey();
		double theDist = the.getValue();
		while ((comp = minpq.poll()) != null) {
			int ind = comp.getKey();
			double dis = comp.getValue();
			if (theDist < dis) {
				if (ind != index) {
					COUNT++;
					return index;
				} else {
					continue;
				}
			} else {
				if (ind == index) {
					continue;
				}
				return -1;
			}
		}
		return -1;
	}

	private int getFarestPoint(Point p) {
		double maxDist = 0.0;
		int maxIndex = -1;
		for (int i : this.instances) {
			double dist = p.dist(KMeans.POINTS.get(i));
			if (dist >= maxDist) {
				maxDist = dist;
				maxIndex = i;
			}
		}
		return maxIndex;
	}

	/**
	 * split and store it to this node's children field, & return the children.
	 *
	 * @return
	 */
	Hypersphere[] split() {
		int firstCenter = this.getFarestPoint(this);
		Point fir = KMeans.POINTS.get(firstCenter);
		int secondCenter = this.getFarestPoint(fir);
		Point sec = KMeans.POINTS.get(secondCenter);
		this.children = new Hypersphere[2];
		this.children[0] = new Hypersphere();
		this.children[1] = new Hypersphere();
		this.children[0].addInstance(firstCenter);
		this.children[1].addInstance(secondCenter);
		for (int i : this.instances) {
			if (i == firstCenter || i == secondCenter) {
				continue;
			}
			final Point p = KMeans.POINTS.get(i);
			final double dist1 = p.distSquared(fir);
			final double dist2 = p.distSquared(sec);
			if (dist1 < dist2) {
				this.children[0].addInstance(i);
			} else {
				this.children[1].addInstance(i);
			}
		}
		this.children[0].endAdding();
		this.children[1].endAdding();
		return this.children;
	}

	Hypersphere[] getChildren() {
		return this.children;
	}

	static void locateAndAssign(Hypersphere hp) {
		int clusterIndex = hp.isInSingleCluster();
		if (clusterIndex != -1) {
			Cluster cc = KMeans.CENTERS.get(clusterIndex);
			for (int pi : hp.instances) {
				cc.addPointToCluster(pi);
			}
			return;
		}
		if (hp.children == null) {
			for (int pi : hp.instances) {
				Point p = KMeans.POINTS.get(pi);
				double minDist = Double.MAX_VALUE;
				int minCenIndex = 0, index = 0;
				for (Cluster cc : KMeans.CENTERS) {
					final double dist = cc.dist(p);
					if (dist < minDist) {
						minDist = dist;
						minCenIndex = index;
					}
					index++;
				}
				Cluster cen = KMeans.CENTERS.get(minCenIndex);
				cen.addPointToCluster(pi);
			}
		} else {
			for (Hypersphere chp : hp.children) {
				Hypersphere.locateAndAssign(chp);
			}
		}
	}

}
