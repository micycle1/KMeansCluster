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
		cluster(20);
		CENTERS.forEach(c -> System.out.println(Arrays.toString(c.pos)));
	}

	static ArrayList<ClusteringCenter> CENTERS = new ArrayList<ClusteringCenter>();
	static ArrayList<Point> POINTS = new ArrayList<Point>();
	static ArrayList<ClusteringCenter> PRE_CENS;
	static int DIMENSION;
	static int MAX_INSTANCE_NUM_NOT_SPLIT = 100; // TODO investigate
	static int TRY_TIMES = 1;
	static Hypersphere BALL_TREE;
	// map cluster center results to its evaluation
	static ArrayList<Entry<ArrayList<ClusteringCenter>, Double>> RESULTS = new ArrayList<Entry<ArrayList<ClusteringCenter>, Double>>(
			TRY_TIMES);

	static boolean timeToEnd() {
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

	public static void populate(int n) {
		for (int i = 0; i < n; i++) {
			double[] pos = new double[] { Math.random(), Math.random() };
			Process.DIMENSION = 2; // TODO or 2?
			Process.POINTS.add(new Point(pos));
		}
		BALL_TREE = BallTree.buildAnInstance(null);
	}

	static double evaluate(ArrayList<ClusteringCenter> cens) {
		double ret = 0.0;
		for (ClusteringCenter cc : cens) {
			ret += cc.evaluate();
		}
		return ret;
	}

	/**
	 * @param k the initial number of clustering centers
	 * @return an entry:the key is the result of clustering.The label starts from
	 *         0.The value is the evaluation of the clustering result
	 */
	public static Entry<Integer[], Double> cluster(int k) {
		for (int t = 0; t < Process.TRY_TIMES; t++) {
			// randomly choose initial cluster centers
			CENTERS.clear();
			if (PRE_CENS != null) {
				PRE_CENS = null;
			}

			Random rand = new Random();
			HashSet<Integer> rSet = new HashSet<Integer>();
			int size = POINTS.size();
			while (rSet.size() < k) {
				rSet.add(rand.nextInt(size));
			}
			for (int index : rSet) {
				Process.CENTERS.add(new ClusteringCenter(Process.POINTS.get(index)));
			}

			// iteration until convergence
			while (!timeToEnd()) {
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
			for (int pi : cc.belongedPoints()) {
				ret[pi] = cNum;
			}
		}
		return new SimpleEntry<Integer[], Double>(ret, evaluate);
	}

	/**
	 * Gives the evaluation and differential of each k in specific range. You can
	 * use these infos to choose a good k for your clustering
	 *
	 * @param startK gives the start point of k for the our try on k(inclusive)
	 * @param endK   gives the end point(exclusive)
	 * @return Entry's key is the evaluation of clustering of each k.The value is
	 *         the differential of the evaluations--evaluation of k(i) - evaluation
	 *         of k(i+1) for i in range(startK, endK - 1)
	 */
	public static Entry<ArrayList<Double>, ArrayList<Double>> cluster(int startK, int endK) {
		ArrayList<Integer[]> results = new ArrayList<Integer[]>();
		ArrayList<Double> evals = new ArrayList<Double>();
		for (int k = startK; k < endK; k++) {
			System.out.println("now k = " + k);
			Entry<Integer[], Double> en = Process.cluster(k);
			results.add(en.getKey());
			evals.add(en.getValue());
		}

		ArrayList<Double> subs = new ArrayList<Double>();
		for (int i = 0; i < evals.size() - 1; i++) {
			subs.add(evals.get(i) - evals.get(i + 1));
		}

		return new SimpleEntry<ArrayList<Double>, ArrayList<Double>>(evals, subs);

	}
}
