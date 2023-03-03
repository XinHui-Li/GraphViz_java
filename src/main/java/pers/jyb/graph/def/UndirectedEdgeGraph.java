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
 * 无向边图。
 *
 * <p>当顶点数量比较多的时候，建议使用顶点索引{@link VertexIndex}。当顶点数量比较多的
 * 时候，优化效果是相当明显的。
 *
 * @param <V> 顶点类型
 * @param <E> 边类型
 * @author jiangyb
 * @see AdjEdgeGraph
 * @see Edge
 * @see EdgeOpGraph
 * @see Graph
 */
public class UndirectedEdgeGraph<V, E extends Edge<V, E>> extends AdjEdgeGraph<V, E>
    implements Graph.EdgeGraph<V, E> {

  private static final long serialVersionUID = -3215868703245301095L;

  public UndirectedEdgeGraph() {
    super();
  }

  /**
   * 使用指定的容量初始化。
   *
   * @param capacity 指定的容量
   * @throws IllegalArgumentException 容量小于等于0
   */
  public UndirectedEdgeGraph(int capacity) {
    super(capacity);
  }

  /**
   * 使用边数组初始化。
   *
   * @param edges 边数组
   * @throws IllegalArgumentException 边数组为空
   */
  public UndirectedEdgeGraph(E[] edges) {
    super(edges);
  }

  /**
   * 向图中添加一条边e。
   *
   * @param e 边
   * @throws NullPointerException 空的边
   */
  @Override
  public void addEdge(E e) {
    Objects.requireNonNull(e);
    V v, w;
    EdgeBag<V, E> bagV = (EdgeBag<V, E>) adjacent(v = e.either());
    if (bagV == EdgeBag.EMPTY) {
      bagV = addBag(v);
    }
    EdgeBag<V, E> bagW = (EdgeBag<V, E>) adjacent(w = e.other(v));
    if (bagW == EdgeBag.EMPTY) {
      bagW = addBag(w);
    }
    bagV.add(e);
    bagW.add(e);
    // 添加的边为自环
    if (bagV == bagW) {
      bagV.loopNum++;
    }
    edgeNum++;
  }

  /**
   * 移除边e。
   *
   * @param edge 需要移除的边
   * @return true - 边存在并且移除成功 false - 边不存在
   */
  @Override
  public boolean removeEdge(E edge) {
    Objects.requireNonNull(edge);
    if (vertexNum == 0 || edgeNum == 0) {
      return false;
    }

    EdgeBag<V, E> bagV, bagW;
    if ((bagV = (EdgeBag<V, E>) adjacent(edge.either())) == EdgeBag.EMPTY
        || (bagW = (EdgeBag<V, E>) adjacent(edge.other(edge.either()))) == EdgeBag.EMPTY) {
      return false;
    }
    if (!bagV.remove(edge) || !bagW.remove(edge)) {
      return false;
    }
    // 移除掉的边为自环
    if (bagV == bagW) {
      bagV.loopNum--;
    }
    edgeNum--;
    return true;
  }

  /**
   * 删除图中的顶点。
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
    // 删除相邻顶点边的记录
    for (E e : bag) {
      tBag = (EdgeBag<V, E>) adjacent(e.other(bag.vertex));
      if (bag == tBag) {
        continue;
      }
      if (tBag != EdgeBag.EMPTY) {
        tBag.remove(e);
      }
    }
    if (index != vertexNum) {
      System.arraycopy(bags, index + 1, bags, index, vertexNum - index - 1);
    }
    int nv = --vertexNum;
    if (bag.vertex instanceof VertexIndex) {
      for (int i = index; i < nv; i++) {
        ((VertexIndex) bags[i].vertex)
            .getGraphIndex()
            .computeIfPresent(checkAndReturnGraphRef(), (k, v) -> v - 1);// 更新索引
      }
    }
    bags[nv] = null;
    edgeNum -= bagEdges;
    modCount++;
    bag.bModCount++;
    return true;
  }

  /**
   * 克隆图的副本。
   *
   * @return 图的副本
   */
  @Override
  public UndirectedEdgeGraph<V, E> copy() {
    UndirectedEdgeGraph<V, E> graph = new UndirectedEdgeGraph<>(this.bags.length);
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

  @Override
  public Iterator<V> iterator() {
    return new UndirectedEdgeIterator();
  }

  private class UndirectedEdgeIterator extends AdjIterator {

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
      // 删除相邻顶点边的记录
      for (E e : bag) {
        tBag = (EdgeBag<V, E>) adjacent(e.other(bag.vertex));
        // 自环不需要处理
        if (tBag == bag) {
          continue;
        }
        if (tBag != EdgeBag.EMPTY) {
          tBag.remove(e);
        }
      }
      if (index != vertexNum) {
        System.arraycopy(bags, index, bags, index - 1, vertexNum - index);
      }
      int nv = --vertexNum;
      --index;
      if (bag.vertex instanceof VertexIndex) {
        for (int i = index; i < nv; i++) {
          ((VertexIndex) bags[i].vertex)
              .getGraphIndex()
              .computeIfPresent(checkAndReturnGraphRef(), (k, v) -> v - 1);// 更新索引
        }
      }
      bags[nv] = null;
      exceptModCount++;
      modCount++;
      edgeNum -= bagEdges;
    }
  }
}
