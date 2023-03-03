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
 * 无向顶点图
 *
 * <p>构造的时候，建议尽可能使用{@code UndirectedGraph(V[])}，并且一次包含所有的节点。
 * 因为{@link #addEdge}检测到当前没有顶点信息，会动态的添加，而此时可能会产生额外的添加扩容计算等等。 此外当顶点数量比较多的时候，建议使用顶点索引{@link
 * VertexIndex}。当顶点数量比较多的时候，优化效果是相当明显的。
 *
 * @param <V> 顶点类型
 * @author jiangyb
 * @see AdjVertexGraph
 * @see VertexOpGraph
 * @see Graph
 */
public class UndirectedGraph<V> extends AdjVertexGraph<V>
    implements Graph.VertexGraph<V> {

  private static final long serialVersionUID = -1768121664171529422L;

  public UndirectedGraph() {
    super();
  }

  /**
   * 使用指定的容量初始化。
   *
   * @param capacity 指定的容量
   * @throws IllegalArgumentException 容量小于等于0
   */
  public UndirectedGraph(int capacity) {
    super(capacity);
  }

  /**
   * 使用顶点数组初始化。
   *
   * @param vertices 顶点数组
   * @throws IllegalArgumentException 顶点数组为空
   */
  public UndirectedGraph(V[] vertices) {
    super(vertices);
  }

  /**
   * 向图中添加一条边v-w
   *
   * @param v 源顶点
   * @param w 目标顶点
   */
  @Override
  public void addEdge(V v, V w) {
    if (v == null || w == null) {
      throw new NullPointerException();
    }
    VertexBag<V> bagV = (VertexBag<V>) adjacent(v);
    if (bagV == VertexBag.EMPTY) {
      bagV = addBag(v);
    }
    VertexBag<V> bagW = (VertexBag<V>) adjacent(w);
    if (bagW == VertexBag.EMPTY) {
      bagW = addBag(w);
    }
    bagV.add(w);
    bagW.add(v);
    // 添加的边为自环
    if (bagV == bagW) {
      bagV.loopNum++;
    }
    edgeNum++;
  }

  /**
   * 移除某条边，针对平行边，需要手动调用多次移除
   *
   * @param v 边的端点
   * @param w 边的端点
   * @return true - 边存在并且移除成功 false - 边不存在
   */
  @Override
  public boolean removeEdge(Object v, Object w) {
    VertexBag<V> bagV, bagW;
    if ((bagV = (VertexBag<V>) adjacent(v)) == VertexBag.EMPTY
        || (bagW = (VertexBag<V>) adjacent(w)) == VertexBag.EMPTY) {
      return false;
    }
    if (!bagV.remove(w) || !bagW.remove(v)) {
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
   * 删除图中的顶点
   *
   * @param vertex 需要移除的顶点
   * @return true - 顶点存在并且移除成功 false - 顶点不存在
   */
  @Override
  public boolean remove(Object vertex) {
    int index = 0;
    VertexBag<V> bag = null, tBag;
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
    for (V v : bag) {
      tBag = (VertexBag<V>) adjacent(v);
      if (bag == tBag) {
        continue;
      }
      if (tBag != VertexBag.EMPTY) {
        tBag.remove(bag.vertex);
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
  public UndirectedGraph<V> copy() {
    UndirectedGraph<V> undirectedGraph = new UndirectedGraph<>(this.bags.length);
    undirectedGraph.bags = bagRepl();
    undirectedGraph.vertexNum = vertexNum;
    undirectedGraph.edgeNum = edgeNum;
    if (vertexNum > 0
        && undirectedGraph.bags[0].vertex instanceof VertexIndex) {
      VertexIndex.GraphRef gf = undirectedGraph.checkAndReturnGraphRef();
      for (int i = 0; i < undirectedGraph.vertexNum; i++) {
        VertexIndex v = ((VertexIndex) undirectedGraph.bags[i].vertex);
        v.getGraphIndex().put(gf, v.index(checkAndReturnGraphRef()));
      }
    }
    return undirectedGraph;
  }

  @Override
  public Iterator<V> iterator() {
    return new UndirectionIterator();
  }

  private class UndirectionIterator extends AdjIterator {

    @Override
    public void remove() {
      checkIsConcurrentModify();
      if (index == 0) {
        throw new IllegalStateException();
      }
      VertexBag<V> bag = bags[index - 1], tBag;
      if (bag == null) {
        throw new NoSuchElementException();
      }

      // 删除的顶点的边的数量
      int bagEdges = bag.degree - bag.loopNum;
      // 删除相邻顶点边的记录
      for (V v : bag) {
        tBag = (VertexBag<V>) adjacent(v);
        // 自环不需要处理
        if (tBag == bag) {
          continue;
        }
        if (tBag != VertexBag.EMPTY) {
          tBag.remove(bag.vertex);
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
