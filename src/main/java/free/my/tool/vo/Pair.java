package free.my.tool.vo;

import java.io.Serializable;

public class Pair<T1, T2> implements Serializable, Comparable<Pair<T1, T2>> {
	private static final long serialVersionUID = 3017415964904573977L;
	
	private T1 first;
	private T2 second;

	public static <T1, T2> Pair<T1, T2> of(T1 first, T2 second) {
		return new Pair<T1, T2>(first, second);
	}

	public Pair() {
	}
	
	public Pair(T1 first, T2 second) {
		this.first = first;
		this.second = second;
	}
	
	public final void setFirst(T1 first) {
		this.first = first;
	}
	
	public final T1 getFirst() {
		return this.first;
	}

	public final void setSecond(T2 second) {
		this.second = second;
	}
	
	public final T2 getSecond() {
		return this.second;
	}
	
	public String toString() {
		return "(" + getFirst() + "," + getSecond() + ")";
	}
	
	public int hashCode() {
		return (((getFirst() == null) ? 0 : getFirst().hashCode()) ^ ((getSecond() == null) ? 0 : getSecond().hashCode()));
	}

	@Override
	public int compareTo(Pair<T1, T2> obj) {
		String first1 = (String)first;
		String first2 = (String)obj.getFirst();
		return first1.compareTo(first2);
	}
}
