package micycle.kmeanscluster;

class BallTree {

	// When this function is called, the parameter should be null
	static Hypersphere buildAnInstance(Hypersphere cur) {
		if (cur == null) {
			cur = new Hypersphere();
			for (int i = 0; i < KMeans.POINTS.size(); ++i) {
				cur.addInstance(i);
			}
			cur.endAdding();
		}
		Hypersphere[] ch = cur.split();
		for (Hypersphere hp : ch) {
			if (hp.size() <= KMeans.MAX_INSTANCE_NUM_NOT_SPLIT) {
				continue;
			}
			buildAnInstance(hp);
		}
		return cur;
	}
}
