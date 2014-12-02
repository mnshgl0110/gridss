package au.edu.wehi.idsv.graph;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;

/**
 * Weighted rectangle graph vertex.
 * Start and end positions are both inclusive 
 * @author Daniel Cameron
 *
 */
public class GraphNode {
	public final long startX;
	public final long endX;
	public final long startY;
	public final long endY;
	public float weight;
	public GraphNode(long startX, long endX, long startY, long endY, float weight) {
		this.startX = startX;
		this.endX = endX;
		this.startY = startY;
		this.endY = endY;
		this.weight = weight;
	}
	public boolean isSameCoordinate(GraphNode node) {
		return startX == node.startX &&
				endX == node.endX &&
				startY == node.startY &&
				endY == node.endY;
	}
	/**
	 * Flips the axes for the given graph node
	 * @return
	 */
	public GraphNode flipAxis() {
		return new GraphNode(startY, endY, startX, endX, weight);
	}
	@Override
	public String toString() {
		return String.format("(x=[%d, %d], y=[%d, %d], %f)", startX, endX, startY, endY, weight);
	}
	public static Ordering<GraphNode> ByEndXYStartXY = new Ordering<GraphNode>() {
		public int compare(GraphNode o1, GraphNode o2) {
			  return ComparisonChain.start()
			        .compare(o1.endX, o2.endX)
			        .compare(o1.endY, o2.endY)
			        .compare(o1.startX, o2.startX)
			        .compare(o1.startY, o2.startY)
			        .result();
		  }
	};
	public static Ordering<GraphNode> ByStartXYEndXY = new Ordering<GraphNode>() {
		public int compare(GraphNode o1, GraphNode o2) {
			  return ComparisonChain.start()
				  	.compare(o1.startX, o2.startX)
			        .compare(o1.startY, o2.startY)
			        .compare(o1.endX, o2.endX)
			        .compare(o1.endY, o2.endY)
			        .result();
		  }
	};
	public static Ordering<GraphNode> ByStartXY = new Ordering<GraphNode>() {
		public int compare(GraphNode o1, GraphNode o2) {
			  return ComparisonChain.start()
				  	.compare(o1.startX, o2.startX)
			        .compare(o1.startY, o2.startY)
			        .result();
		  }
	};
	public static Ordering<GraphNode> ByEndYXStartYX = new Ordering<GraphNode>() {
		public int compare(GraphNode o1, GraphNode o2) {
			  return ComparisonChain.start()
					 .compare(o1.endY, o2.endY)
					 .compare(o1.endX, o2.endX)
					 .compare(o1.startY, o2.startY)
					 .compare(o1.startX, o2.startX)
					 .result();
		  }
	};
	public static Ordering<GraphNode> ByStartYXEndYX = new Ordering<GraphNode>() {
		public int compare(GraphNode o1, GraphNode o2) {
			return ComparisonChain.start()
					.compare(o1.startY, o2.startY)
					.compare(o1.startX, o2.startX)
					.compare(o1.endY, o2.endY)
					.compare(o1.endX, o2.endX)
					.result();
		  }
	};
	public static Ordering<GraphNode> ByEndXStartYEndY = new Ordering<GraphNode>() {
		public int compare(GraphNode o1, GraphNode o2) {
			return ComparisonChain.start()
					.compare(o1.endX, o2.endX)
					.compare(o1.startY, o2.startY)
					.compare(o1.endY, o2.endY)
					.result();
		  }
	};
	public static Ordering<GraphNode> ByEndY = new Ordering<GraphNode>() {
		public int compare(GraphNode o1, GraphNode o2) {
			return ComparisonChain.start()
					.compare(o1.endY, o2.endY)
					.result();
		  }
	};
}
