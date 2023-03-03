/*
 * Copyright 2022 The graph-support project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pers.jyb.graph.def;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;
import java.util.function.Function;
import pers.jyb.graph.op.ConnectedComponent;
import pers.jyb.graph.op.DirectedCycle;
import pers.jyb.graph.op.GraphCycle.EdgeOpGC;
import pers.jyb.graph.op.GraphCycle.VertexOpGC;
import pers.jyb.graph.op.MinSpanningTree;
import pers.jyb.graph.op.SourcePath;
import pers.jyb.graph.op.SourcePaths;
import pers.jyb.graph.op.StrongConnectedComponent;
import pers.jyb.graph.op.Topological;

/**
 * 图的一些辅助方法。在不需要了解{@code Graph}系别包下面的所有API的情况下，只需要检索此类， 看是否有合适的方法能够达到目的。
 *
 * @author jiangyb
 */
public class Graphs {

  private Graphs() {
  }

  /**
   * 判断是否是空图。
   *
   * @param graph 图
   * @return ture - 空图 false - 非空图
   */
  public static boolean isEmpty(BaseGraph<?> graph) {
    return graph == null || graph.vertexNum() == 0;
  }

  /**
   * 判断是否不是空图。
   *
   * @param graph 图
   * @return ture - 非空图 false - 空图
   */
  public static boolean isNotEmpty(BaseGraph<?> graph) {
    return !isEmpty(graph);
  }

  /**
   * 顶点一般只是作为内容存储的容器，通常有时候需要替换掉顶点的容器。使用一个{@code Function} 作为转换的中间过程，替换掉有向边{@code
   * Digraph.VertexDigraph}的所有顶点。
   *
   * @param <V>     顶点类型
   * @param <T>     替换顶点类型
   * @param digraph 有向图
   * @param change  转换方法
   * @return 顶点有向图
   */
  public static <V, T> Digraph.VertexDigraph<T> vertexChange(Digraph.VertexDigraph<V> digraph,
                                                             Function<? super V, ? extends T> change) {
    Map<V, T> vtMap = null;
    for (V v : digraph) {
      if (vtMap == null) {
        vtMap = new HashMap<>();
      }
      vtMap.put(v, change.apply(v));
    }
    Digraph.VertexDigraph<T> graph = new DirectedGraph<>(digraph.vertexNum());
    if (vtMap != null) {
      for (V v : digraph) {
        for (V w : digraph.adjacent(v)) {
          graph.addEdge(vtMap.get(v), vtMap.get(w));
        }
      }
    }
    return graph;
  }

  /**
   * 如果两个顶点之间存在路径的话，返回两个顶点之间的最少边路径。
   *
   * @param <V>    顶点类型
   * @param graph  需要检索的有向图
   * @param soure  开始顶点
   * @param target 结束顶点
   * @return 如过存在，返回路径，路径顺序等于数据索引的顺序，否则返回null
   */
  public static <V> V[] path(VertexOpGraph<V> graph, V soure, V target) {
    SourcePath.VertexOp<V> paths = SourcePaths.breadth(graph, soure);
    return paths.path(target);
  }

  /**
   * 如果顶点有向图中存在环，返回发现的第一个环。
   *
   * @param <V>     顶点类型
   * @param digraph 需要检索的顶点有向图
   * @return 发现的环，不存在返回null
   */
  public static <V> Stack<V> cycle(Digraph.VertexDigraph<V> digraph) {
    VertexOpGC<V> directedCycle = DirectedCycle.build(digraph);
    return directedCycle.cycle();
  }

  /**
   * 如果边有向图中存在环，返回发现的第一个环。
   *
   * @param <V>     顶点类型
   * @param <E>     边的类型
   * @param digraph 需要检索的边有向图
   * @return 发现的环，不存在返回null
   */
  public static <V, E extends DirectedEdge<V, E>> Stack<E> cycle(
      Digraph.EdgeDigraph<V, E> digraph) {
    EdgeOpGC<V, E> directedCycle = DirectedCycle.build(digraph);
    return directedCycle.cycle();
  }

  /**
   * 顶点有向图的顶点的拓扑排序，排序顺序等于数组的索引。
   *
   * @param <V>     顶点类型
   * @param digraph 需要检索的顶点有向图
   * @return 排序结果
   * @throws IllegalGraphException 图含有环
   */
  public static <V> V[] order(Digraph.VertexDigraph<V> digraph) {
    Topological<V> topological = Topological.build(digraph);
    return topological.order();
  }

  /**
   * 边有向图的顶点的拓扑排序，排序顺序等于数组的索引。
   *
   * @param <V>     顶点类型
   * @param <E>     边的类型
   * @param digraph 需要检索的边有向图
   * @return 排序结果
   * @throws IllegalGraphException 图含有环
   */
  public static <V, E extends DirectedEdge<V, E>> V[] order(
      Digraph.EdgeDigraph<V, E> digraph) {
    Topological<V> topological = Topological.build(digraph);
    return topological.order();
  }

  /**
   * 顶点有向图的顶点的等级分配
   *
   * @param <V>     顶点类型
   * @param digraph 需要检索的顶点有向图
   * @return 排序结果
   * @throws IllegalGraphException 图含有环
   */
  public static <V> Map<Integer, List<V>> rank(Digraph.VertexDigraph<V> digraph) {
    Topological<V> topological = Topological.build(digraph);
    return topological.rank();
  }

  /**
   * 边有向图的顶点的等级分配
   *
   * @param <V>     顶点类型
   * @param <E>     边的类型
   * @param digraph 需要检索的边有向图
   * @return 排序结果
   * @throws IllegalGraphException 图含有环
   */
  public static <V, E extends DirectedEdge<V, E>> Map<Integer, List<V>> rank(
      Digraph.EdgeDigraph<V, E> digraph) {
    Topological<V> topological = Topological.build(digraph);
    return topological.rank();
  }

  /**
   * 返回顶点图的顶点的连通分量匹配情况
   *
   * @param <V>   顶点类型
   * @param graph 顶点图
   * @return 顶点的连通分量匹配 <br> key - 连通分量ID <br> value - 同一个连通分量的顶点列表
   * @throws IllegalGraphException 不支持的图类型
   */
  @SuppressWarnings("unchecked")
  public static <V> Map<Integer, List<V>> connectedIdAlloc(VertexOpGraph<V> graph) {
    return (Map<Integer, List<V>>) baseConnectedIdAlloc(graph);
  }

  /**
   * 返回边图的顶点的连通分量匹配情况
   *
   * @param <V>   顶点类型
   * @param <E>   边的类型
   * @param graph 边图
   * @return 顶点的连通分量匹配 <br> key - 连通分量ID <br> value - 同一个连通分量的顶点列表
   * @throws IllegalGraphException 不支持的图类型
   */
  @SuppressWarnings("unchecked")
  public static <V, E extends DirectedEdge<V, E>> Map<Integer, List<V>> connectedIdAlloc(
      EdgeOpGraph<V, E> graph) {
    return (Map<Integer, List<V>>) baseConnectedIdAlloc(graph);
  }

  /**
   * 返回图的顶点的连通分量匹配情况
   *
   * @param <V>   顶点类型
   * @param graph 图
   * @return 顶点的连通分量匹配 <br> key - 连通分量ID <br> value - 同一个连通分量的顶点列表
   * @throws IllegalGraphException 不支持的图类型
   */
  private static <V> Map<Integer, ?> baseConnectedIdAlloc(BaseGraph<V> graph) {
    Map<Integer, List<Object>> map = null;
    ConnectedComponent<V> cc;
    if (graph instanceof Graph.VertexGraph) {
      cc = ConnectedComponent.build((Graph.VertexGraph<V>) graph);
    } else if (graph instanceof Digraph.VertexDigraph) {
      cc = ConnectedComponent.build((Digraph.VertexDigraph<V>) graph);
    } else if (graph instanceof Graph.EdgeGraph) {
      cc = ConnectedComponent.build((Graph.EdgeGraph<V, ?>) graph);
    } else if (graph instanceof Digraph.EdgeDigraph) {
      cc = ConnectedComponent.build((Digraph.EdgeDigraph<V, ?>) graph);
    } else {
      throw new IllegalGraphException("Unsupported graph");
    }
    for (V v : graph) {
      if (map == null) {
        map = new HashMap<>();
      }
      map.compute(cc.connectedId(v), (k, vs) -> {
        if (vs == null) {
          vs = new ArrayList<>();
        }
        vs.add(v);
        return vs;
      });
    }
    return map;
  }

  /**
   * 返回顶点有向图的顶点的连通分量匹配情况
   *
   * @param <V>     顶点类型
   * @param digraph 顶点有向图
   * @return 顶点的连通分量匹配 <br> key - 连通分量ID <br> value - 同一个连通分量的顶点列表
   */
  @SuppressWarnings("unchecked")
  public static <V> Map<Integer, List<V>> strongConnectedIdAlloc(Digraph.VertexDigraph<V> digraph) {
    return (Map<Integer, List<V>>) diStrongConnectedIdAlloc(digraph);
  }

  /**
   * 返回边有向图的顶点的连通分量匹配情况
   *
   * @param <V>     顶点类型
   * @param <E>     边的类型
   * @param digraph 边有向图
   * @return 顶点的连通分量匹配 <br> key - 连通分量ID <br> value - 同一个连通分量的顶点列表
   */
  @SuppressWarnings("unchecked")
  public static <V, E extends DirectedEdge<V, E>> Map<Integer, List<V>> strongConnectedIdAlloc(
      Digraph.EdgeDigraph<V, E> digraph) {
    return (Map<Integer, List<V>>) diStrongConnectedIdAlloc(digraph);
  }

  /**
   * 返回有向图的顶点的连通分量匹配情况
   *
   * @param <V>     顶点类型
   * @param digraph 有向图
   * @return 顶点的连通分量匹配 <br> key - 连通分量ID <br> value - 同一个连通分量的顶点列表
   */
  private static <V> Map<Integer, ?> diStrongConnectedIdAlloc(Digraph<V> digraph) {
    StrongConnectedComponent<V> cc;
    if (digraph instanceof Digraph.VertexDigraph) {
      cc = StrongConnectedComponent.build((Digraph.VertexDigraph<V>) digraph);
    } else if (digraph instanceof Digraph.EdgeDigraph) {
      cc = StrongConnectedComponent.build((Digraph.EdgeDigraph<V, ?>) digraph);
    } else {
      throw new IllegalGraphException("Unsupported graph");
    }
    Map<Integer, List<Object>> map = null;
    for (V v : digraph) {
      if (map == null) {
        map = new HashMap<>();
      }
      map.compute(cc.connectedId(v), (k, vs) -> {
        if (vs == null) {
          vs = new ArrayList<>();
        }
        vs.add(v);
        return vs;
      });
    }
    return map;
  }

  /**
   * 把一个无向图按照连通情况分解成多个独立的无向图
   *
   * @param <V>   顶点类型
   * @param graph 需要分解的无向图
   * @return 分解后的图列表
   */
  @SuppressWarnings("unchecked")
  public static <V> Graph.VertexGraph<V>[] splitGraph(UndirectedGraph<V> graph) {
    // 连通分量分配
    Map<Integer, List<V>> alloc = connectedIdAlloc(graph);
    if (alloc == null || alloc.size() == 1) {
      return new Graph.VertexGraph[]{graph};
    }
    Graph.VertexGraph<V> repl = graph.copy();
    Graph.VertexGraph<V>[] graphs = new Graph.VertexGraph[alloc.size()];
    int j = 0;
    // 直接提取对应的Bag组成新的无向图
    for (Map.Entry<Integer, List<V>> entry : alloc.entrySet()) {
      UndirectedGraph<V> g = new UndirectedGraph<>();
      List<V> connGraphs = entry.getValue();
      int size = connGraphs.size(), degree = 0;
      AdjVertexGraph.VertexBag<V>[] bags = new AdjVertexGraph.VertexBag[size];
      for (int i = 0; i < size; i++) {
        V v;
        AdjVertexGraph.VertexBag<V> bag
            = (AdjVertexGraph.VertexBag<V>)
            repl.adjacent(v = connGraphs.get(i));
        bags[i] = bag;
        degree += bag.degree;
        if (v instanceof VertexIndex) {
          ((VertexIndex) v).getGraphIndex()
              .put(g.checkAndReturnGraphRef(), i);
        }
      }
      g.vertexNum = size;
      g.edgeNum = degree / 2;
      g.bags = bags;
      graphs[j++] = g;
    }
    return graphs;
  }

  /**
   * 把一个有向图按照顶点之间是否存在边分成多个独立的有向图
   *
   * @param <V>     顶点类型
   * @param digraph 需要分解的有向图
   * @return 分解后的有向图
   */
  @SuppressWarnings("unchecked")
  public static <V> Digraph.VertexDigraph<V>[] splitDigraph(DirectedGraph<V> digraph) {
    Objects.requireNonNull(digraph, "graph cannot be null");
    UndirectedGraph<V> graph;
    if (digraph.vertexNum > 0) {
      graph = new UndirectedGraph<>(digraph.toArray());
    } else {
      graph = new UndirectedGraph<>(0);
    }
    // 有向图转无向图
    for (V v : digraph) {
      for (V w : digraph.adjacent(v)) {
        graph.addEdge(v, w);
      }
    }
    // 无向图连通情况分配
    Map<Integer, List<V>> alloc = connectedIdAlloc(graph);
    if (alloc == null || alloc.size() == 1) {
      return new Digraph.VertexDigraph[]{digraph};
    }
    Digraph.VertexDigraph<V> repl = digraph.copy();
    Digraph.VertexDigraph<V>[] graphs = new Digraph.VertexDigraph[alloc.size()];
    int j = 0;
    // 直接提取对应的Bag组成新的有向图
    for (Map.Entry<Integer, List<V>> entry : alloc.entrySet()) {
      DirectedGraph<V> g = new DirectedGraph<>();
      List<V> connGraphs = entry.getValue();
      int size = connGraphs.size(), degree = 0;
      AdjVertexGraph.VertexBag<V>[] bags
          = new AdjVertexGraph.VertexBag[size];
      for (int i = 0; i < size; i++) {
        V v;
        AdjVertexGraph.VertexBag<V> bag
            = (AdjVertexGraph.VertexBag<V>)
            repl.adjacent(v = connGraphs.get(i));
        bags[i] = bag;
        degree += bag.degree;
        if (v instanceof VertexIndex) {
          ((VertexIndex) v).getGraphIndex()
              .put(g.checkAndReturnGraphRef(), i);
        }
      }
      g.vertexNum = size;
      g.edgeNum = degree / 2;
      g.bags = bags;
      graphs[j++] = g;
    }
    return graphs;
  }

  /**
   * 随机的返回图中的一个顶点
   *
   * @param <V>   顶点类型
   * @param graph 图
   * @return 随机的顶点
   */
  public static <V> V random(BaseGraph<V> graph) {
    Objects.requireNonNull(graph);
    V[] vertexs = graph.toArray();
    if (vertexs == null || vertexs.length == 0) {
      return null;
    }
    int index = (int) (Math.random() * vertexs.length);
    return vertexs[index];
  }

  /**
   * 寻找无向图的最小生成树
   *
   * @param <V>   顶点类型
   * @param <E>   边类型
   * @param graph 图
   * @return 最小生成树
   */
  public static <V, E extends Edge<V, E>> Iterable<E> mst(Graph.EdgeGraph<V, E> graph) {
    MinSpanningTree<V, E> minSpanningTree = new MinSpanningTree<>(graph);
    return minSpanningTree.edges();
  }
}