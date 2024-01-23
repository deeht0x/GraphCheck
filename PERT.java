package adg200003;

import adg200003.Graph;
import adg200003.Graph.Vertex;
import adg200003.Graph.Edge;
import adg200003.Graph.GraphAlgorithm;
import adg200003.Graph.Factory;



import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Scanner;


public class PERT extends GraphAlgorithm<PERT.PERTVertex> {
    LinkedList<Vertex> finishList;
	
    public static class PERTVertex implements Factory {
	// Add fields to represent attributes of vertices here
    	boolean seen;
        Vertex parent;
        int distance;
        int es;
        int ef;
        int ls;
        int lf;
        int slack;
        int duration;
        String status;
	
	public PERTVertex(Vertex u) {
		seen = false;
        parent = null;
        distance = Integer.MAX_VALUE;
        es = 0;
        ef = 0;
        ls = 0;
        lf = 0;
        slack = 0;
        duration = 0;
        status = "not started";
	}
	
	public PERTVertex make(Vertex u) { 
		return new PERTVertex(u);
		}
    }

    // Constructor for PERT is private. Create PERT instances with static method pert().
    private PERT(Graph g) {
	super(g, new PERTVertex(null));
    }

    public void setDuration(Vertex u, int d) {
    	get(u).duration = d;
    }

    // Implement the PERT algorithm. Returns false if the graph g is not a DAG.
    public boolean pert() {
    	// Run DFS to store topological ordering of nodes in topList
        LinkedList<Vertex> topList = topologicalOrder();
        
        // Initialize earliest start times
        for (Vertex u : g) {
            get(u).es = 0;
        }

        // Calculate earliest finish times
        for (Vertex u : topList) {
        	// Earliest Finish = Earliest Start + Duration
            get(u).ef = get(u).es + get(u).duration;
            // Update the earliest start time
            for (Edge e : g.outEdges(u)) {
                Vertex v = e.otherEnd(u);
                // Update it to the earliest finish time IF ES < EF
                if (get(v).es < get(u).ef) {
                    get(v).es = get(u).ef;
                }
            }
        }

        // Calculate project finish time 
        int CPL = 0;
        for (Vertex u : g) {
            if (get(u).ef > CPL) {
                CPL = get(u).ef;
            }
        }

        // Initialize latest finish times
        for (Vertex u : g) {
            get(u).lf = CPL;
        }

        // Calculate latest start times and slack
        Collections.reverse(topList);
        for (Vertex u : topList) {
        	// Latest Start = Latest Finish + Duration
            get(u).ls = get(u).lf - get(u).duration;
            // Slack = Latest Finish - Earliest Finish
            get(u).slack = get(u).lf - get(u).ef;
            for (Edge e : g.inEdges(u)) {
                Vertex v = e.fromVertex();
                // Update it to the latest start time IF LF > LS
                if (get(v).lf > get(u).ls) {
                    get(v).lf = get(u).ls;
                }
            }
        }

        return true;
    }

    // This method initializes the graph
    // Sets the vertices as not seen
    // Sets the parent as null
    void initialize(Graph g) {
        for (Vertex u : g) {
            get(u).seen = false;
            get(u).parent = null;
        }
    }

    // This method performs a Depth-First Search (DFS) on a vertex u
    void dfs(Vertex u) {
        get(u).seen = true;
        // Loop through all the outgoing edges from vertex u
        for (Edge e : g.outEdges(u)) {
            Vertex w = e.otherEnd(u);
            if (!get(w).seen) {
                get(w).parent = u;
                dfs(w);
            }
        }
        finishList.addFirst(u);
    }

    // This method performs DFS on all vertices of the graph
    void dfsAll(Graph g) {
        initialize(g);
        // Perform DFS on each vertex
        for (Vertex u : g) {
            if (!get(u).seen) {
                dfs(u);
            }
        }
    }

    // Find a topological order of g using DFS
    LinkedList<Vertex> topologicalOrder() {
        finishList = new LinkedList<>();
        dfsAll(g);
        return finishList;
    }
  
    // The following methods are called after calling pert().

    // Earliest time at which task u can be completed
    public int ec(Vertex u) {
    	return get(u).ef;
    }

    // Latest completion time of u
    public int lc(Vertex u) {
    	return get(u).lf;
    }

    // Slack of u
    public int slack(Vertex u) {
    	return get(u).slack;
    }

    // Length of a critical path (time taken to complete project)
    public int criticalPath() {
    	// Initialize the length of the critical path
        int maxLength = 0;

        // Iterate over all vertices of the graph
        for (Vertex u : g) {
            PERTVertex pu = get(u);

            // Update the maximum length if necessary
            if (pu.ef > maxLength) {
                maxLength = pu.ef;
            }
        }

        // Return the length of the critical path
        return maxLength;
    }

    // Is u a critical vertex?
    public boolean critical(Vertex u) {
    	return get(u).slack == 0;
    }

    // Number of critical vertices of g
    public int numCritical() {
    	int count = 0;
        for (Vertex u : g) {
            if (critical(u)) {
                count++;
            }
        }
        return count;
    }

    /* Create a PERT instance on g, runs the algorithm.
     * Returns PERT instance if successful. Returns null if G is not a DAG.
     */
    public static PERT pert(Graph g, int[] duration) {
	PERT p = new PERT(g);
	for(Vertex u: g) {
	    p.setDuration(u, duration[u.getIndex()]);
	}
	// Run PERT algorithm.  Returns false if g is not a DAG
	if(p.pert()) {
	    return p;
	} else {
	    return null;
	}
    }
    
    public static void main(String[] args) throws Exception {
	String graph = "10 13   1 2 1   2 4 1   2 5 1   3 5 1   3 6 1   4 7 1   5 7 1   5 8 1   6 8 1   6 9 1   7 10 1   8 10 1   9 10 1      0 3 2 3 2 1 3 2 4 1";
	Scanner in;
	// If there is a command line argument, use it as file from which
	// input is read, otherwise use input from string.
	in = args.length > 0 ? new Scanner(new File(args[0])) : new Scanner(graph);
	Graph g = Graph.readDirectedGraph(in);
	g.printGraph(false);

	int[] duration = new int[g.size()];
	for(int i=0; i<g.size(); i++) {
	    duration[i] = in.nextInt();
	}
	PERT p = pert(g, duration);
	if(p == null) {
	    System.out.println("Invalid graph: not a DAG");
	} else {
	    System.out.println("Number of critical vertices: " + p.numCritical());
	    System.out.println("u\tEC\tLC\tSlack\tCritical");
	    for(Vertex u: g) {
		System.out.println(u + "\t" + p.ec(u) + "\t" + p.lc(u) + "\t" + p.slack(u) + "\t" + p.critical(u));
	    }
	}
    }
}
