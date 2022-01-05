package micycle.kmeanscluster;

public class DimensionNotMatchException extends Exception {

	private static final long serialVersionUID = 1L;

	@Override
	public String toString() {
		return "The two operators' dimension are not matched.";
	}
}
