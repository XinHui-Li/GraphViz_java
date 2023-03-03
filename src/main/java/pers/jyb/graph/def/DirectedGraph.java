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
 * 有向图
 *
 * <p>构造的时候，建议尽可能使用{@code DirectedGraph(V[])}，并且一次包含所有的节点。
 * 因为{@link #addEdge}检测到当前没有顶点信息，会动态的添加，而此时可能会产生额外的添加扩容计算等等。
 * 此外当顶点数量比较多的时候，建议使用顶点索引{@link VertexIndex}。当顶点数量比较多的时候，优化效果是相当明显的。
 *
 * @param <V> 顶点类型
 * @author jiangyb
 * @see AdjVertexGraph
 * @see VertexOpGraph
 * @see Digraph
 */
public class DirectedGraph<V> extends AdjVertexGraph<V>
    implements Digraph.VertexDigraph<V> {

  private static final long serialVersionUID = 1062819133746040326L;

  public DirectedGraph() {
    super();
  }

  /**
   * 使用指定的容量初始化。
   *
   * @param capacity 指定的容量
   * @throws IllegalArgumentException 容量小于等于0
   */
  public DirectedGraph(int capacity) {
    super(capacity);
  }

  /**
   * 使用顶点数组初始化。
   *
   * @param vertices 顶点数组
   * @throws IllegalArgumentException 顶点数组为空
   */
  public DirectedGraph(V[] vertices) {
    super(vertices);
  }

  /**
   * 向图中添加一条有向边source -&gt; target
   *
   * @param source 源顶点
   * @param target 目标顶点
   */
  @Override
  public void addEdge(V source, V target) {
    if (source == null || target == null) {
      throw new NullPointerException();
    }
    VertexBag<V> bagSource = (VertexBag<V>) adjacent(source);
    if (bagSource == VertexBag.EMPTY) {
      bagSource = addBag(source);
    }
    VertexBag<V> bagTarget = (VertexBag<V>) adjacent(target);
    if (bagTarget == VertexBag.EMPTY) {
      bagTarget = addBag(target);
    }
    bagSource.add(target);
    bagTarget.degree++; // 增加bagTarget的入度
    if (bagSource == bagTarget) {
      bagSource.loopNum++;
    }
    edgeNum++;
  }

  /**
   * 移除有向边v -&gt; w
   *
   * @param source 边的端点
   * @param target 边的端点
   * @return true - 边存在并且移除成功 false - 边不存在
   */
  @Override
  public boolean removeEdge(Object source, Object target) {
    VertexBag<V> bagSource, bagTarget;
    if ((bagSource = (VertexBag<V>) adjacent(source)) == VertexBag.EMPTY
        || (bagTarget = (VertexBag<V>) adjacent(target)) == VertexBag.EMPTY) {
      return false;
    }
    if (!bagSource.remove(target)) {
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
    for (V v : bag) {
      tBag = (VertexBag<V>) adjacent(v);
      if (tBag != bag && tBag != VertexBag.EMPTY) {
        tBag.degree--; // 减少tBag的入度
      }
    }
    if (index != vertexNum) {
      System.arraycopy(bags, index + 1, bags, index, vertexNum - index - 1);
    }
    int nv = --vertexNum;
    // 更新索引和移除指向移除顶点的边
    for (int i = 0; i < nv; i++) {
      if (bags[i].vertex instanceof VertexIndex) {
        ((VertexIndex) bags[i].vertex).getGraphIndex().put(checkAndReturnGraphRef(), i);
      }
      bags[i].remove(bag.vertex);
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
  public DirectedGraph<V> copy() {
    DirectedGraph<V> directedGraph = new DirectedGraph<>(this.bags.length);
    directedGraph.bags = bagRepl();
    directedGraph.vertexNum = vertexNum;
    directedGraph.edgeNum = edgeNum;
    if (vertexNum > 0
        && directedGraph.bags[0].vertex instanceof VertexIndex) {
      VertexIndex.GraphRef gf = directedGraph.checkAndReturnGraphRef();
      for (int i = 0; i < directedGraph.vertexNum; i++) {
        VertexIndex v = ((VertexIndex) directedGraph.bags[i].vertex);
        v.getGraphIndex().put(gf, v.index(checkAndReturnGraphRef()));
      }
    }
    return directedGraph;
  }

  /**
   * 反转有向图。有向图中，一般顶点只会记录自己指向的顶点，对于顶点本身来说， 指向自己的顶点通常是未知的。通过反转有向图，这样就可以找出所有指向该顶点的边。
   *
   * @return 反转后的有向图
   */
  @Override
  public DirectedGraph<V> reverse() {
    DirectedGraph<V> digraph = new DirectedGraph<>(toArray());
    for (int i = 0; i < vertexNum; i++) {
      VertexBag<V> bag = bags[i];
      V v = bag.vertex;
      for (V w : bag) {
        digraph.addEdge(w, v);
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
    return new DirectionIterator();
  }

  private class DirectionIterator extends AdjIterator {

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
      for (V v : bag) {
        tBag = (VertexBag<V>) adjacent(v);
        if (tBag != bag && tBag != VertexBag.EMPTY) {
          tBag.degree--; // 减少tBag的入度
        }
      }
      if (index != vertexNum) {
        System.arraycopy(bags, index, bags, index - 1, vertexNum - index);
      }
      int nv = --vertexNum;
      // 更新索引和移除指向移除顶点的边
      for (int i = 0; i < nv; i++) {
        if (bags[i].vertex instanceof VertexIndex) {
          ((VertexIndex) bags[i].vertex).getGraphIndex().put(checkAndReturnGraphRef(), i);
        }
        bags[i].remove(bag.vertex);
      }
      bags[nv] = null;
      index--;
      exceptModCount++;
      modCount++;
      edgeNum -= bagEdges;
    }
  }

  @Override
  public String toString() {
    StringBuilder print = new StringBuilder("vertices " + vertexNum() + ", edges:\n");
    for (V v : this) {
      print.append("[").append(v).append("] ");
      for (V n : adjacent(v)) {
        print.append(v).append("->").append(n).append(" ");
      }
      print.append("\n");
    }
    return print.toString();
  }
}
