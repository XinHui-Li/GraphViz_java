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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import pers.jyb.graph.def.DedirectedEdgeGraph.ReserveEdge;
import pers.jyb.graph.util.CollectionUtils;

/**
 * 边操作的双向有向图。
 *
 * @param <V> 顶点类型
 * @param <E> 边类型
 * @author jiangyb
 */
public class DedirectedEdgeGraph<V, E extends DirectedEdge<V, E>>
    extends ProxyDedigraph<V, DirectedEdgeGraph<V, E>, DirectedEdgeGraph<V, ReserveEdge<V, E>>>
    implements EdgeDedigraph<V, E> {

  private static final long serialVersionUID = -5712574722294920575L;

  private final HashMap<E, List<ReserveEdge<V, E>>> reserveEdgeMap;

  public DedirectedEdgeGraph() {
    this(new DirectedEdgeGraph<>(), new DirectedEdgeGraph<>());
  }

  /**
   * 使用指定的容量初始化。
   *
   * @param capacity 指定的容量
   * @throws IllegalArgumentException 容量小于等于0
   */
  public DedirectedEdgeGraph(int capacity) {
    this(new DirectedEdgeGraph<>(capacity), new DirectedEdgeGraph<>(capacity));
  }

  /**
   * 使用顶点数组初始化。
   *
   * @param edges 边数组
   * @throws IllegalArgumentException 边数组为空
   */
  public DedirectedEdgeGraph(E[] edges) {
    this(new DirectedEdgeGraph<>(), new DirectedEdgeGraph<>());

    if (edges == null || edges.length == 0) {
      throw new IllegalArgumentException("edges can not be empty");
    }

    for (E edge : edges) {
      addEdge(edge);
    }
  }

  /**
   * 使用边集合初始化。
   *
   * @param edges 边集合
   * @throws IllegalArgumentException 边集合对象为空
   */
  public DedirectedEdgeGraph(Collection<E> edges) {
    this(new DirectedEdgeGraph<>(), new DirectedEdgeGraph<>());

    if (CollectionUtils.isEmpty(edges)) {
      throw new IllegalArgumentException("edges can not be empty");
    }

    for (E edge : edges) {
      addEdge(edge);
    }
  }

  private DedirectedEdgeGraph(
      DirectedEdgeGraph<V, E> digraph,
      DirectedEdgeGraph<V, ReserveEdge<V, E>> reDigraph
  ) {
    super(digraph, reDigraph);

    this.reserveEdgeMap = new HashMap<>(digraph.edgeNum());
    reDigraph.forEachEdges(edge -> putEdgeMap(edge.edge, edge));
  }

  @Override
  public void clear() {
    super.clear();
    if (reserveEdgeMap != null) {
      reserveEdgeMap.clear();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  protected Iterable<ReserveEdge<V, E>> inIte(Object v) {
    return reDigraph.adjacent(v);
  }

  @Override
  @SuppressWarnings("unchecked")
  protected Iterable<E> outIte(Object v) {
    return digraph.adjacent(v);
  }

  @Override
  public void addEdge(E e) {
    Objects.requireNonNull(e);
    digraph.addEdge(e);
    ReserveEdge<V, E> re = new ReserveEdge<>(e.to(), e.from(), e.weight(), e);
    putEdgeMap(e, re);
    reDigraph.addEdge(re);
  }

  @Override
  public boolean removeEdge(E e) {
    Objects.requireNonNull(e);

    boolean result = digraph.removeEdge(e);

    if (!result) {
      return false;
    }

    List<ReserveEdge<V, E>> reserveEdges = reserveEdgeMap.get(e);
    ReserveEdge<V, E> reserveEdge = reserveEdges.remove(reserveEdges.size() - 1);
    reDigraph.removeEdge(reserveEdge);

    if (CollectionUtils.isEmpty(reserveEdges)) {
      reserveEdgeMap.remove(e);
    }

    return true;
  }

  @Override
  public Iterable<E> edges() {
    return digraph.edges();
  }

  @Override
  public void forEachEdges(Consumer<E> consumer) {
    digraph.forEachEdges(consumer);
  }

  @Override
  public E reverseEdge(E e) {
    Objects.requireNonNull(e);

    if (!removeEdge(e)) {
      return null;
    }

    E reverse = e.reverse();
    addEdge(reverse);

    return reverse;
  }

  @Override
  public int selfLoops(V v) {
    return digraph.selfLoops(v);
  }

  @Override
  public DedirectedEdgeGraph<V, E> copy() {
    return new DedirectedEdgeGraph<>(digraph.copy(), reDigraph.copy());
  }

  @Override
  public DedirectedEdgeGraph<V, E> reverse() {
    if (edgeNum() == 0) {
      return new DedirectedEdgeGraph<>();
    }

    List<E> res = new ArrayList<>(edgeNum());
    digraph.forEachEdges(edge -> res.add(edge.reverse()));
    return new DedirectedEdgeGraph<>(res);
  }

  @Override
  public Iterable<E> adjacent(Object v) {
    return new BiConcatIterable<>(outAdjacent(v), inAdjacent(v));
  }

  @Override
  public Iterable<E> inAdjacent(Object v) {
    Iterable<ReserveEdge<V, E>> adjacent = reDigraph.adjacent(v);

    return () -> new ReserveIterator<>(adjacent.iterator());
  }

  @Override
  public Iterable<E> outAdjacent(Object v) {
    return new BiConcatIterable<>(digraph.adjacent(v), Collections.emptyList());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    DedirectedEdgeGraph<?, ?> that = (DedirectedEdgeGraph<?, ?>) o;
    return Objects.equals(reserveEdgeMap, that.reserveEdgeMap);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), reserveEdgeMap);
  }

  private void putEdgeMap(E edge, ReserveEdge<V, E> reserveEdge) {
    reserveEdgeMap.compute(edge, (k, v) -> {
      if (v == null) {
        v = new ArrayList<>();
      }
      v.add(reserveEdge);
      return v;
    });
  }

  // ------------------------------------------- Subclass -------------------------------------------

  static class ReserveIterator<V, E extends DirectedEdge<V, E>> implements Iterator<E> {

    private final Iterator<ReserveEdge<V, E>> reserveEdgeIterator;

    private ReserveIterator(Iterator<ReserveEdge<V, E>> reserveEdgeIterator) {
      Objects.requireNonNull(reserveEdgeIterator);
      this.reserveEdgeIterator = reserveEdgeIterator;
    }

    @Override
    public boolean hasNext() {
      return reserveEdgeIterator.hasNext();
    }

    @Override
    public E next() {
      return reserveEdgeIterator.next().edge;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("Adjacent Iterator not support delete!");
    }
  }

  static class ReserveEdge<V, B extends DirectedEdge<V, B>>
      extends AbstractEdge<V, ReserveEdge<V, B>>
      implements DirectedEdge<V, ReserveEdge<V, B>>, Serializable {

    private static final long serialVersionUID = 4362288930468885917L;

    private final B edge;

    protected ReserveEdge(V from, V to, double weight, B edge) {
      super(from, to, weight);
      Objects.requireNonNull(edge);
      this.edge = edge;
    }

    @Override
    public V from() {
      return left;
    }

    @Override
    public V to() {
      return right;
    }

    @Override
    public ReserveEdge<V, B> reverse() {
      return new ReserveEdge<>(to(), from(), weight, edge.reverse());
    }

    @Override
    public ReserveEdge<V, B> copy() {
      return new ReserveEdge<>(from(), to(), weight, edge);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      if (!super.equals(o)) {
        return false;
      }
      ReserveEdge<?, ?> that = (ReserveEdge<?, ?>) o;
      return Objects.equals(edge, that.edge);
    }

    @Override
    public int hashCode() {
      return Objects.hash(super.hashCode(), edge);
    }
  }
}
