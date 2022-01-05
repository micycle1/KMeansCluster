package micycle.kmeanscluster;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map.Entry;

import java.util.Random;

public class KMeans {

	static ArrayList<Cluster> CENTERS = new ArrayList<Cluster>();
	static ArrayList<Point> POINTS = new ArrayList<Point>();
	static ArrayList<Cluster> PRE_CENS;
	static int DIMENSION;
	static int MAX_INSTANCE_NUM_NOT_SPLIT = 10; // TODO investigate
	static Hypersphere BALL_TREE;
	// map cluster center results to its evaluation
	static ArrayList<Entry<ArrayList<Cluster>, Double>> RESULTS;

	public KMeans() {
		// TODO Auto-generated constructor stub
	}

	private static boolean terminate() {
		if (PRE_CENS == null) {
			return false;
		}
		for (Cluster cc : KMeans.CENTERS) {
			if (!PRE_CENS.contains(cc)) {
				return false;
			}
		}
		return true;
	}

	static void populate(int n) {
		KMeans.DIMENSION = 2;
		Random r = new Random(0); // NOTE seed
		POINTS = new ArrayList<Point>(n * 2);
		for (int i = 0; i < n; i++) {
//			double[] pos = new double[] { r.nextDouble(), r.nextDouble() };
			KMeans.POINTS.add(new Point2D(r.nextDouble(), r.nextDouble()));
		}
		BALL_TREE = BallTree.buildAnInstance(null);
	}

	/**
	 * Find SSD score across the clustering centers.
	 */
	private static double evaluate(ArrayList<Cluster> cens) {
		double ret = 0.0;
		for (Cluster cc : cens) {
			ret += cc.evaluate();
		}
		return ret;
	}

	public static void cluster(int k) {
		// TODO investigate centroid initialisation
		CENTERS.clear();
		Random rand = new Random(1); // TODO seed

		// initialise clusters using existing points
		for (int i = 0; i < k; i++) {
			CENTERS.add(new Cluster(POINTS.get(rand.nextInt(POINTS.size()))));
//			CENTERS.add(new Cluster(new Point(rand.nextDouble(), rand.nextDouble())));
		}

		// iterate until convergence
		while (!terminate()) {
			Hypersphere.locateAndAssign(BALL_TREE);
			PRE_CENS = new ArrayList<Cluster>(CENTERS);
			ArrayList<Cluster> newCenters = new ArrayList<Cluster>();
			for (Cluster cc : CENTERS) {
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
		RESULTS = new ArrayList<Entry<ArrayList<Cluster>, Double>>(trials);
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
				KMeans.CENTERS.add(new Cluster(KMeans.POINTS.get(index)));
			}

			// iterate until convergence
			while (!terminate()) {
				Hypersphere.locateAndAssign(BALL_TREE);
				PRE_CENS = new ArrayList<Cluster>(CENTERS);
				ArrayList<Cluster> newCenters = new ArrayList<Cluster>();
				for (Cluster cc : CENTERS) {
					cc = cc.getNewCenter();
					newCenters.add(cc);
				}
				CENTERS = newCenters;
			}
			KMeans.RESULTS
					.add(new SimpleEntry<ArrayList<Cluster>, Double>(PRE_CENS, KMeans.evaluate(PRE_CENS)));
			Hypersphere.ALL_COUNT = 0;
			Hypersphere.COUNT = 0;
		}

		// Find the smallest score among multiple trials
		double minEvaluate = Double.MAX_VALUE;
		int minIndex = 0, i = 0;
		for (Entry<ArrayList<Cluster>, Double> entry : RESULTS) {
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
			Cluster cc = CENTERS.get(cNum);
			for (int pi : cc.belongingPoints()) {
				ret[pi] = cNum;
			}
		}
		return new SimpleEntry<Integer[], Double>(ret, evaluate);
	}

}
