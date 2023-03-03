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

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * 有向边图。
 *
 * <p>当顶点数量比较多的时候，建议使用顶点索引{@link VertexIndex}。当顶点数量比较多的
 * 时候，优化效果是相当明显的。
 *
 * @param <V> 顶点类型
 * @param <E> 边类型
 * @author jiangyb
 * @see AdjEdgeGraph
 * @see DirectedEdge
 * @see EdgeOpGraph
 * @see Digraph
 */
public class DirectedEdgeGraph<V, E extends DirectedEdge<V, E>> extends AdjEdgeGraph<V, E>
    implements Digraph.EdgeDigraph<V, E> {

  private static final long serialVersionUID = 7489284046620730360L;

  public DirectedEdgeGraph() {
    super();
  }

  /**
   * 使用指定的容量初始化
   *
   * @param capacity 指定的容量
   * @throws IllegalArgumentException 容量小于等于0
   */
  public DirectedEdgeGraph(int capacity) {
    super(capacity);
  }

  /**
   * 使用边数组初始化
   *
   * @param edges 边数组
   * @throws IllegalArgumentException 边数组为空
   */
  public DirectedEdgeGraph(E[] edges) {
    super(edges);
  }

  /**
   * 添加一条边
   *
   * @param edge 添加的边
   * @throws NullPointerException 空的边
   */
  @Override
  public void addEdge(E edge) {
    Objects.requireNonNull(edge, "Edge can not be null");
    V source, target;
    EdgeBag<V, E> bagSource = (EdgeBag<V, E>) adjacent(source = edge.from());
    if (bagSource == EdgeBag.EMPTY) {
      bagSource = addBag(source);
    }
    EdgeBag<V, E> bagTarget = (EdgeBag<V, E>) adjacent(target = edge.to());
    if (bagTarget == EdgeBag.EMPTY) {
      bagTarget = addBag(target);
    }
    bagSource.add(edge);
    bagTarget.degree++; // 增加bagTarget的入度
    if (bagSource == bagTarget) {
      bagSource.loopNum++;
    }
    edgeNum++;
  }

  /**
   * 移除有向边e
   *
   * @param e 有向边
   * @return true - 边存在并且移除成功 false - 边不存在
   */
  @Override
  public boolean removeEdge(E e) {
    Objects.requireNonNull(e);
    if (vertexNum == 0 || edgeNum == 0) {
      return false;
    }

    EdgeBag<V, E> bagSource, bagTarget;
    if ((bagSource = (EdgeBag<V, E>) adjacent(e.from())) == EdgeBag.EMPTY
        || (bagTarget = (EdgeBag<V, E>) adjacent(e.to())) == EdgeBag.EMPTY) {
      return false;
    }
    if (!bagSource.remove(e)) {
      return false;
    }
    bagTarget.degree--; // 减少bagTarget的入度
    if (bagSource == bagTarget) {
      bagSource.loopNum--;
    }
    edgeNum--;
    return true;
  }

  /**
   * 删除图中的顶点,这会导致到扫面所有节点和边
   *
   * @param vertex 需要移除的顶点
   * @return true - 顶点存在并且移除成功 false - 顶点不存在
   */
  @Override
  public boolean remove(Object vertex) {
    int index = 0;
    EdgeBag<V, E> bag = null, tBag;
    if (vertex instanceof VertexIndex) {
      Integer i = ((VertexIndex) vertex).getGraphIndex()
          .get(checkAndReturnGraphRef());
      if (i != null
          && i >= 0
          && i < vertexNum
          && Objects.equals(bags[i].vertex, vertex)
      ) {
        bag = bags[index = i];
      }
    } else {
      for (; index < vertexNum; index++) {
        if (Objects.equals((bag = bags[index]).vertex, vertex)) {
          break;
        }
      }
      return false;
    }
    if (bag == null) {
      return false;
    }
    // 删除的顶点的边的数量
    int bagEdges = bag.degree - bag.loopNum;
    for (E e : bag) {
      tBag = (EdgeBag<V, E>) adjacent(e.other(bag.vertex));
      if (tBag != bag && tBag != EdgeBag.EMPTY) {
        tBag.degree--; // 减少tBag的入度
      }
    }
    if (index != vertexNum) {
      System.arraycopy(bags, index + 1, bags, index, vertexNum - index - 1);
    }
    int nv = --vertexNum;
    // 更新索引和移除指向移除顶点的边
    V bagVertex = bag.vertex;
    for (int i = 0; i < nv; i++) {
      if (bags[i].vertex instanceof VertexIndex) {
        ((VertexIndex) bags[i].vertex).getGraphIndex().put(checkAndReturnGraphRef(), i);
      }
      bags[i].removeIf(e -> Objects.equals(e.to(), bagVertex));
    }
    bags[nv] = null;
    edgeNum -= bagEdges;
    modCount++;
    bag.bModCount++;
    return true;
  }

  /**
   * 反转有向图中的边
   *
   * @param edge 需要反转的边
   * @return 反转后的边，反转失败返回null
   */
  @Override
  public E reverseEdge(E edge) {
    Objects.requireNonNull(edge);
    EdgeBag<V, E> sBag = (EdgeBag<V, E>) adjacent(edge.from());
    if (sBag == EdgeBag.EMPTY) {
      return null;
    }

    if (!sBag.remove(edge)) {
      return null;
    }
    EdgeBag<V, E> tBag = (EdgeBag<V, E>) adjacent(edge.to());
    E reserveEdge = edge.reverse();
    tBag.add(reserveEdge);
    sBag.degree++;
    tBag.degree--;
    return reserveEdge;
  }

  /**
   * 克隆图的副本。
   *
   * @return 图的副本
   */
  @Override
  public DirectedEdgeGraph<V, E> copy() {
    DirectedEdgeGraph<V, E> graph = new DirectedEdgeGraph<>(this.bags.length);
    graph.bags = bagRepl();
    graph.vertexNum = vertexNum;
    graph.edgeNum = edgeNum;
    if (vertexNum > 0
        && graph.bags[0].vertex instanceof VertexIndex) {
      VertexIndex.GraphRef gf = graph.checkAndReturnGraphRef();
      for (int i = 0; i < graph.vertexNum; i++) {
        VertexIndex v = ((VertexIndex) graph.bags[i].vertex);
        v.getGraphIndex().put(gf, v.index(checkAndReturnGraphRef()));
      }
    }
    return graph;
  }

  /**
   * 反转有向图。有向图中，一般顶点只会记录自己指向的顶点，对于顶点本身来说， 指向自己的顶点通常是未知的。通过反转有向图，这样就可以找出所有指向该顶点的边。
   *
   * @return 反转后的有向图
   */
  @Override
  public DirectedEdgeGraph<V, E> reverse() {
    DirectedEdgeGraph<V, E> digraph = new DirectedEdgeGraph<>();
    for (int i = 0; i < vertexNum; i++) {
      EdgeBag<V, E> bag = bags[i];
      V v = bag.vertex;
      for (E e : adjacent(v)) {
        E re = e.reverse();
        digraph.addEdge(re);
      }
    }
    return digraph;
  }

  /**
   * 顶点迭代器
   *
   * @return 顶点迭代器
   */
  @Override
  public Iterator<V> iterator() {
    return new DirectionEdgeIterator();
  }

  private class DirectionEdgeIterator extends AdjIterator {

    @Override
    public void remove() {
      checkIsConcurrentModify();
      if (index == 0) {
        throw new IllegalStateException();
      }
      EdgeBag<V, E> bag = bags[index - 1], tBag;
      if (bag == null) {
        throw new NoSuchElementException();
      }

      // 删除的顶点的边的数量
      int bagEdges = bag.degree - bag.loopNum;
      for (E e : bag) {
        tBag = (EdgeBag<V, E>) adjacent(e.other(bag.vertex));
        if (tBag != bag && tBag != EdgeBag.EMPTY) {
          tBag.degree--; // 减少tBag的入度
        }
      }
      if (index != vertexNum) {
        System.arraycopy(bags, index, bags, index - 1, vertexNum - index);
      }
      int nv = --vertexNum;
      // 更新索引和移除指向移除顶点的边
      V bagVertex = bag.vertex;
      for (int i = 0; i < nv; i++) {
        if (bags[i].vertex instanceof VertexIndex) {
          ((VertexIndex) bags[i].vertex).getGraphIndex().put(checkAndReturnGraphRef(), i);
        }
        bags[i].removeIf(e -> Objects.equals(e.to(), bagVertex));
      }
      bags[nv] = null;
      index--;
      exceptModCount++;
      modCount++;
      edgeNum -= bagEdges;
    }
  }
}
