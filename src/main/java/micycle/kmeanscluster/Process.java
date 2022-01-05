package micycle.kmeanscluster;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Random;

public class Process {

	public static void main(String[] args) {
		populate(10000);
		cluster(25);
		CENTERS.forEach(c -> System.out.println(Arrays.toString(c.pos)));
	}

	static ArrayList<ClusteringCenter> CENTERS = new ArrayList<ClusteringCenter>();
	static ArrayList<Point> POINTS = new ArrayList<Point>();
	static ArrayList<ClusteringCenter> PRE_CENS;
	static int DIMENSION;
	static int MAX_INSTANCE_NUM_NOT_SPLIT = 100; // TODO investigate
	static Hypersphere BALL_TREE;
	// map cluster center results to its evaluation
	static ArrayList<Entry<ArrayList<ClusteringCenter>, Double>> RESULTS;

	private static boolean terminate() {
		if (PRE_CENS == null) {
			return false;
		}
		for (ClusteringCenter cc : Process.CENTERS) {
			if (!PRE_CENS.contains(cc)) {
				return false;
			}
		}
		return true;
	}

	private static void populate(int n) {
		Process.DIMENSION = 2;
		Random r = new Random(0); // NOTE seed
		POINTS = new ArrayList<Point>(n * 2);
		for (int i = 0; i < n; i++) {
//			double[] pos = new double[] { r.nextDouble(), r.nextDouble() };
			Process.POINTS.add(new Point2D(r.nextDouble(), r.nextDouble()));
		}
		BALL_TREE = BallTree.buildAnInstance(null);
	}

	/**
	 * Find SSD score across the clustering centers.
	 */
	private static double evaluate(ArrayList<ClusteringCenter> cens) {
		double ret = 0.0;
		for (ClusteringCenter cc : cens) {
			ret += cc.evaluate();
		}
		return ret;
	}

	public static void cluster(int k) {
		// TODO investigate centroid initialisation
		CENTERS.clear();
		Random rand = new Random(0); // TODO seed

		// initialise clusters using existing points
		for (int i = 0; i < k; i++) {
			Process.CENTERS.add(new ClusteringCenter(Process.POINTS.get(rand.nextInt(POINTS.size()))));
		}

		// iterate until convergence
		while (!terminate()) {
			Hypersphere.locateAndAssign(BALL_TREE);
			PRE_CENS = new ArrayList<ClusteringCenter>(CENTERS);
			ArrayList<ClusteringCenter> newCenters = new ArrayList<ClusteringCenter>();
			for (ClusteringCenter cc : CENTERS) {
				cc = cc.getNewCenter();
				newCenters.add(cc);
			}
			CENTERS = newCenters;
		}
	}

	/**
	 * @param k the initial number of clustering centers
	 * @return an entry:the key is the result of clustering.The label starts from
	 *         0.The value is the evaluation of the clustering result
	 */
	public static Entry<Integer[], Double> cluster(int k, int trials) {
		RESULTS = new ArrayList<Entry<ArrayList<ClusteringCenter>, Double>>(trials);
		for (int t = 0; t < trials; t++) {
			// randomly choose initial cluster centers
			CENTERS.clear();
			if (PRE_CENS != null) {
				PRE_CENS = null;
			}

			Random rand = new Random(0); // TODO seed
			HashSet<Integer> rSet = new HashSet<Integer>();
			int size = POINTS.size();
			while (rSet.size() < k) {
				rSet.add(rand.nextInt(size));
			}
			for (int index : rSet) {
				Process.CENTERS.add(new ClusteringCenter(Process.POINTS.get(index)));
			}

			// iterate until convergence
			while (!terminate()) {
				Hypersphere.locateAndAssign(BALL_TREE);
				PRE_CENS = new ArrayList<ClusteringCenter>(CENTERS);
				ArrayList<ClusteringCenter> newCenters = new ArrayList<ClusteringCenter>();
				for (ClusteringCenter cc : CENTERS) {
					cc = cc.getNewCenter();
					newCenters.add(cc);
				}
				CENTERS = newCenters;
			}
			Process.RESULTS
					.add(new SimpleEntry<ArrayList<ClusteringCenter>, Double>(PRE_CENS, Process.evaluate(PRE_CENS)));
			Hypersphere.ALL_COUNT = 0;
			Hypersphere.COUNT = 0;
		}

		// Find the smallest score among multiple trials
		double minEvaluate = Double.MAX_VALUE;
		int minIndex = 0, i = 0;
		for (Entry<ArrayList<ClusteringCenter>, Double> entry : RESULTS) {
			double e = entry.getValue();
			if (e < minEvaluate) {
				minEvaluate = e;
				minIndex = i;
			}
			i++;
		}
		CENTERS = RESULTS.get(minIndex).getKey();
		double evaluate = RESULTS.get(minIndex).getValue();
		// Return the cluster number corresponding to the instance
		Integer[] ret = new Integer[POINTS.size()];
		for (int cNum = 0; cNum < CENTERS.size(); cNum++) {
			ClusteringCenter cc = CENTERS.get(cNum);
			for (int pi : cc.belongingPoints()) {
				ret[pi] = cNum;
			}
		}
		return new SimpleEntry<Integer[], Double>(ret, evaluate);
	}

}
