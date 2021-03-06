package algorithm

import scala.annotation.tailrec
import data.Structures.Graph
import scala.collection.mutable

/**
  * A simple implementation of the A* Search Heuristic Algorithm
  *
  * https://en.wikipedia.org/wiki/A*_search_algorithm
  *
  * Created by Alessandro on 12/07/2017.
  */
object AStarSearch {
    
    /**
      *
      * @param graph     The graph from which nodes are picked up
      * @param source    The Node acting as starting point for the shortest path to be found
      * @param target    The node acting as ending point for the shortest path to be found
      * @param extractor A companion function that specifies the desired output format of the Shortest Path
      * @tparam N The Type of the Nodes of the Graph
      * @tparam R The Return Type of this Function
      * @return A collection of type R containing the shortest path in the given graph that goes from source to target
      */
    def apply[N, R](graph: Graph[N])(source: N, target: N)
                   (heuristic: (N, N) => Double)(extractor: (Graph[N], Map[N, N], N, N) => R): R = {
    
        if (source == target) return extractor(graph, Map.empty, source, target)
        
        //val heuristicCost: (N, N) => Double = (src, dst) => 0.0
        
        // The set of nodes already evaluated
        val closed: mutable.Set[N] = mutable.HashSet.empty[N]
        
        // The set of currently discovered nodes that are not evaluated yet.
        // Initially, only the start node is known.
        val fringe: mutable.Set[N] = mutable.HashSet(source)
        
        // For each node, which node it can most efficiently be reached from.
        // If a node can be reached from many nodes, parents will eventually contain the
        // most efficient previous step.
        val parents: mutable.Map[N, N] = mutable.HashMap.empty[N, N]
        
        // For each node, the cost of getting from the start node to that node.
        // In this version instead of adding nodes with Infinity Weight,
        // They just are absent, thus need to be added when computed.
        // The cost of going from start to start is zero.
        val gScores: mutable.Map[N, Double] = mutable.HashMap(source -> 0.0)
        
        // For each node, the total cost of getting from the start node to the goal
        // by passing by that node. That value is partly known, partly heuristic.
        // For the first node, that value is completely heuristic.
        val fScores: mutable.Map[N, Double] = mutable.HashMap(source -> heuristic(source, target))
        
        @tailrec def go(current: N): R =
            if (fringe.isEmpty) throw new Exception("Empty Fringe, disconnected nodes...")
            else if (current == target) extractor(graph, parents.toMap, source, target)
            else {
                fringe -= current
                closed += current
                
                graph(current).filter(neighbor => !closed(neighbor._1))
                    .foreach(neighbor => {
                        fringe += neighbor._1
                        
                        val gScore = gScores(current) + neighbor._2
                        
                        if (!gScores.contains(neighbor._1) || gScore <= gScores(neighbor._1)) {
                            parents += neighbor._1 -> current
                            gScores += neighbor._1 -> gScore
                            fScores += neighbor._1 -> (gScore + heuristic(neighbor._1, target))
                        }
                    })
                
                go(fScores.filter(e => !closed(e._1)).minBy(e => e._2)._1) //
            }
        
        go(source)
        
    }
    
    /**
      * An object containing some predefined extractors for the A* Search Algorithm
      *
      * Actually only a Map and a List Extractor are available
      *
      */
    object Extractors {
        
        /**
          * Extract the SHP as a Map, like "A" -> "B", "B" -> "C", etc..., 'till the target node,
          * where the the SHP is give by chaining all the Pairs
          *
          * @param parents The Map containing the evaluated Nodes from the Algorithm,
          *                each of them associated with the respective predecessor
          * @param source  The starting Node of the Path that was calculated from the A* Algorithm
          * @param target  The ending Node of tha Path that was calculated from the A* Algorithm
          * @tparam N The Type of the Node that compose the Graph
          * @return A Map of Node by Transforming the given parents Map and the cost of the path
          */
        def toMap[N](graph: Graph[N], parents: Map[N, N], source: N, target: N): (Map[N, N], Double) = {
    
            if (source == target) (Map(source -> target), 0.0)
            else {
                @tailrec def go(sp: Map[N, N], current: N, successor: N, cost: Double): (Map[N, N], Double) = {
                    if (current == source) (sp.updated(source, successor), cost + graph(current)(successor))
                    else go(sp.updated(current, successor), parents(current), current, cost + graph(current)(successor))
                }
    
                go(sp = Map.empty, current = parents(target), successor = target, cost = 0.0)
            }
        }
        
        /**
          * Extract the SHP as a List, like "A", "B", "C", etc..., 'till the target node,
          * where the order of the members is the order in which the SHP is to be followed
          *
          * @param parents The Map containing the evaluated Nodes from the Algorithm,
          *                each of them associated with the respective predecessor
          * @param source  The starting Node of the Path that was calculated from the A* Algorithm
          * @param target  The ending Node of tha Path that was calculated from the A* Algorithm
          * @tparam N The Type of the Node that compose the Graph
          * @return A List of Node by Transforming the given parents Map and the cost of the path
          */
        def toList[N](graph: Graph[N], parents: Map[N, N], source: N, target: N): (List[N], Double) = {
    
            if (source == target) (List(target), 0.0)
            else {
                @tailrec def go(sp: List[N], current: N, successor: N, cost: Double): (List[N], Double) = {
                    if (current == source) (source :: sp, cost + graph(current)(successor))
                    else go(current :: sp, parents(current), current, cost + graph(current)(successor))
                }
    
                go(sp = List(target), current = parents(target), successor = target, cost = 0.0)
            }
        }
    }
    
}
