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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

/**
 * 使用边的邻接数组的存储的图。对如下一个无向图：<br>
 * <pre>
 *        0 --------- 2
 *       / \        / | \
 *     /    \     /   |   \
 *    5      \  /     3 -- 4
 *            1
 * 使用邻接数组表示为：
 *    Bags Arrays
 *    bag[0] vertex: 0 adjs:Edge(0,5,0) -&gt; Edge(0,1,0) -&gt; Edge(0,2,0)
 *    bag[1] vertex: 1 adjs:Edge(1,0,0) -&gt; Edge(1,2,0)
 *    bag[2] vertex: 2 adjs:Edge(2,0,0) -&gt; Edge(2,1,0) -&gt; Edge(2,3,0) -&gt; Edge(2,4,0)
 *    bag[3] vertex: 3 adjs:Edge(3,2,0) -&gt; Edge(3,4,0)
 *    bag[4] vertex: 4 adjs:Edge(4,3,0) -&gt; Edge(4,2,0)
 *    bag[5] vertex: 5 adjs:Edge(5,0,0)
 * </pre>
 *
 * <p>有向图{@code Digraph}中，一条边只会被一个数组成员<tt>EdgeBag</tt>存储，而无向图{@code Graph}
 * 需要在<tt>source</tt>和<tt>target</tt>所在索引的<tt>EdgeBag</tt>中存储两次。
 *
 * @param <V> 顶点类型
 * @param <E> 边类型
 * @author jiangyb
 * @see Edge
 * @see DirectedEdge
 * @see EdgeOpGraph
 */
abstract class AdjEdgeGraph<V, E extends BaseEdge<V, E>>
    extends AbstractBaseGraph.AbstractEdgeOpBase<V, E>
    implements Serializable {

  private static final long serialVersionUID = -7783803325974568478L;

  /**
   * 默认的初始化容量
   */
  private static final int DEFAULT_CAPACITY = 1 << 5;

  /**
   * 顶点数量
   */
  int vertexNum;

  /**
   * 边数量
   */
  int edgeNum;

  /**
   * 邻接表数组对象，用来存储所有顶点以及顶点的所有边
   */
  transient EdgeBag<V, E>[] bags;

  /**
   * 修改次数记录，主要避免并发删除导致的无法预估的结构
   */
  protected transient int modCount;

  /**
   * 顶点索引{@code VertexIndex}的定位key
   */
  private transient VertexIndex.GraphRef graphRef;

  /**
   * 默认初始化
   */
  @SuppressWarnings("unchecked")
  AdjEdgeGraph() {
    bags = new EdgeBag[DEFAULT_CAPACITY];
  }

  /**
   * 使用指定的容量初始化。
   *
   * @param capacity 指定的容量
   * @throws IllegalArgumentException 容量小于等于0
   */
  @SuppressWarnings("unchecked")
  AdjEdgeGraph(int capacity) {
    if (capacity <= 0) {
      throw new IllegalArgumentException("Illegal Capacity: " + capacity);
    }
    bags = new EdgeBag[capacity];
  }

  /**
   * 使用边数组初始化。
   *
   * @param edges 边数组
   * @throws IllegalArgumentException 边数组为空
   */
  AdjEdgeGraph(E[] edges) {
    this();
    if (edges == null || edges.length == 0) {
      throw new IllegalArgumentException("edges can not be empty");
    }
    for (E edge : edges) {
      addEdge(edge);
    }
  }

  /**
   * 顶点数量
   *
   * @return 顶点个数
   */
  @Override
  public int vertexNum() {
    return vertexNum;
  }

  /**
   * 边的数量，包括环
   *
   * @return 边的个数
   */
  @Override
  public int edgeNum() {
    return edgeNum;
  }

  /**
   * 添加顶点
   *
   * @param v 顶点
   * @return 是否成功
   * @throws NullPointerException 空的顶点
   */
  @Override
  public boolean add(V v) {
    Objects.requireNonNull(v);
    EdgeBag<V, E> bag = (EdgeBag<V, E>) adjacent(v);
    if (bag != EdgeBag.EMPTY) {
      return false;
    }
    addBag(v);
    return true;
  }

  /**
   * 顶点的所有边
   *
   * @param v 需要获取的顶点
   * @return 顶点的所有边
   */
  @Override
  @SuppressWarnings("unchecked")
  public Iterable<E> adjacent(Object v) {
    if (v == null) {
      return (EdgeBag<V, E>) EdgeBag.EMPTY;
    }
    if (v instanceof VertexIndex) {
      Integer index;
      index = ((VertexIndex) v).getGraphIndex().get(checkAndReturnGraphRef());
      if (index == null) {
        return (EdgeBag<V, E>) EdgeBag.EMPTY;
      }
      if (index >= 0 && index < vertexNum
          && v.equals(bags[index].vertex)) {
        return bags[index];
      }
    }
    return position(v);
  }

  /**
   * 返回图的所有边
   *
   * @return 图的所有边
   */
  @Override
  public Iterable<E> edges() {
    Set<E> edges = null;
    for (int i = 0; i < vertexNum; i++) {
      if (edges == null) {
        edges = new HashSet<>(edgeNum);
      }
      for (E e : adjacent(bags[i].vertex)) {
        edges.add(e);
      }
    }
    return edges == null ? Collections.emptySet() : edges;
  }

  @Override
  public void forEachEdges(Consumer<E> consumer) {
    Objects.requireNonNull(consumer);
    for (int i = 0; i < vertexNum; i++) {
      for (E e : adjacent(bags[i].vertex)) {
        consumer.accept(e);
      }
    }
  }

  @Override
  public void forEach(Consumer<? super V> action) {
    Objects.requireNonNull(action);
    for (int i = 0; i < vertexNum; i++) {
      action.accept(bags[i].vertex);
    }
  }

  /**
   * 返回某个顶点的度
   *
   * @param vertex 顶点
   * @return 顶点度数
   */
  @Override
  public int degree(V vertex) {
    EdgeBag<V, E> v = (EdgeBag<V, E>) adjacent(vertex);
    return v != EdgeBag.EMPTY ? v.degree : 0;
  }

  /**
   * 返回顶点自环数量。
   *
   * @param v 顶点
   * @return 顶点自环数量
   */
  @Override
  public int selfLoops(V v) {
    return ((EdgeBag<V, E>) adjacent(v)).loopNum;
  }

  /**
   * 计算自环的个数
   *
   * @return 图中自环数量
   */
  @Override
  public int numberOfLoops() {
    int count = 0;
    for (int i = 0; i < vertexNum; i++) {
      count += bags[i].loopNum;
    }
    return count;
  }

  @Override
  public V[] toArray() {
    if (vertexNum <= 0) {
      return null;
    }
    Class<?> clazz = bags[0].vertex.getClass();
    @SuppressWarnings("unchecked")
    V[] vertexs = (V[]) Array.newInstance(clazz, vertexNum);
    for (int i = 0; i < vertexNum; i++) {
      vertexs[i] = bags[i].vertex;
    }
    return vertexs;
  }

  @Override
  public abstract AdjEdgeGraph<V, E> copy();

  @Override
  @SuppressWarnings("unchecked")
  public void clear() {
    vertexNum = 0;
    edgeNum = 0;
    modCount = 0;
    graphRef = null;
    bags = new EdgeBag[DEFAULT_CAPACITY];
  }

  /**
   * 添加EdgeBag并且返回对象
   *
   * @param v 添加的顶点
   * @return 顶点的邻接边包
   */
  protected EdgeBag<V, E> addBag(V v) {
    EdgeBag<V, E> bag;
    // 容量已满
    if (vertexNum == bags.length) {
      resize();
    }
    int vn = vertexNum++;
    bags[vn] = bag = new EdgeBag<>(v);
    // 添加顶点索引
    if (v instanceof VertexIndex) {
      ((VertexIndex) v).getGraphIndex().put(checkAndReturnGraphRef(), vn);
    }
    modCount++;
    return bag;
  }

  /**
   * 容量满的时候，按照如下规则扩充： critical = {@link #rightRangeMinPowerOf2()}; vertexNum < critical *
   * 3/4，扩充为critical; vertexNum >= critical * 3/4，扩充为2 * critical
   */
  private void resize() {
    int critical = rightRangeMinPowerOf2();
    int newCap = vertexNum < (critical - (critical >>> 2))
        ? critical
        : critical << 1;
    bags = Arrays.copyOf(bags, newCap);
  }

  /**
   * 右区间最小的2的幂
   */
  private int rightRangeMinPowerOf2() {
    int capacity = bags.length;
    return (capacity & (1 << (Integer.SIZE - Integer.numberOfLeadingZeros(capacity) - 1))) << 1;
  }

  /**
   * 验证引用，确认初始化
   *
   * @return 本邻接图的弱引用
   */
  protected VertexIndex.GraphRef checkAndReturnGraphRef() {
    if (graphRef == null) {
      graphRef = new VertexIndex.GraphRef(this);
    }
    return graphRef;
  }

  // O(n) find
  @SuppressWarnings("unchecked")
  private EdgeBag<V, E> position(Object v) {
    for (int i = 0; i < vertexNum; i++) {
      if (v.equals(bags[i].vertex)) {
        return bags[i];
      }
    }
    return (EdgeBag<V, E>) EdgeBag.EMPTY;
  }

  /**
   * 比较两个图是否相等
   *
   * @param obj 比较对象
   * @return true - 相等 false - 不相等
   */
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof AdjEdgeGraph)) {
      return false;
    }
    if (this instanceof Digraph && !(obj instanceof Digraph)
        || !(this instanceof Digraph) && obj instanceof Digraph) {
      return false;
    }
    AdjEdgeGraph<?, ?> target = (AdjEdgeGraph<?, ?>) obj;
    if (target.vertexNum != vertexNum
        || target.edgeNum != edgeNum) {
      return false;
    }
    for (int i = 0; i < vertexNum; i++) {
      EdgeBag<?, ?> bag = target.position(bags[i].vertex);
      if (bag == null || bag.degree != bags[i].degree
          || bag.loopNum != bags[i].loopNum) {
        return false;
      }
      for (Object edge : bag) {
        boolean find = false;
        for (E e : bags[i]) {
          find = Objects.equals(edge, e);
          if (find) {
            break;
          }
        }
        if (!find) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * 返回图的hashCode值
   *
   * @return 图的hashCode值
   */
  @Override
  public int hashCode() {
    int hash = 1;
    for (int i = 0; i < vertexNum; i++) {
      hash += bags[i].hashCode();
    }
    return hash;
  }

  /**
   * 返回bags的副本
   *
   * @return 相邻边包数组
   */
  protected EdgeBag<V, E>[] bagRepl() {
    @SuppressWarnings("unchecked")
    EdgeBag<V, E>[] newBag = new EdgeBag[bags.length];
    for (int i = 0; i < vertexNum; i++) {
      newBag[i] = bags[i].clone();
    }
    return newBag;
  }

  private void writeObject(ObjectOutputStream oos)
      throws IOException {
    oos.defaultWriteObject();
    // 只序列化顶点数量的Bag
    for (int i = 0; i < vertexNum; i++) {
      oos.writeObject(bags[i]);
    }
  }

  @SuppressWarnings("unchecked")
  private void readObject(ObjectInputStream ois)
      throws IOException, ClassNotFoundException {
    ois.defaultReadObject();
    if (vertexNum >= 0) {
      bags = new EdgeBag[vertexNum];
      for (int i = 0; i < vertexNum; i++) {
        bags[i] = (EdgeBag<V, E>) ois.readObject();
        if (bags[i].vertex instanceof VertexIndex) {
          VertexIndex vertexIndex = (VertexIndex) bags[i].vertex;
          vertexIndex.getGraphIndex().put(checkAndReturnGraphRef(), i); // 索引只添加当前图的
        }
      }
    }
  }

  /**
   * 节点遍历器
   */
  protected class AdjIterator implements Iterator<V> {

    int index; // 访问对象的索引
    int exceptModCount = modCount; // 下一个修改的次数

    @Override
    public boolean hasNext() {
      return index < vertexNum;
    }

    @Override
    public V next() {
      checkIsConcurrentModify();
      if (index > vertexNum) {
        throw new NoSuchElementException();
      }
      return bags[index++].vertex;
    }

    void checkIsConcurrentModify() {
      if (exceptModCount != modCount) {
        throw new ConcurrentModificationException();
      }
    }
  }

  /**
   * 边邻接表
   */
  protected static class EdgeBag<V, E extends BaseEdge<V, E>> extends Bag<V, E>
      implements Cloneable {

    private static final long serialVersionUID = -4204284993141072092L;

    static final EdgeBag<?, ?> EMPTY = new EdgeBag<>(true);

    // 顶点度数
    int degree;

    // 自环数量
    int loopNum;

    EdgeBag(V vertex) {
      super(vertex);
    }

    EdgeBag(Boolean unmodify) {
      super(unmodify);
    }

    @Override
    public Iterator<E> iterator() {
      return new EdgeBagIterator();
    }

    /**
     * 添加边
     */
    @Override
    void add(E e) {
      super.add(e);
      degree++;
    }

    /**
     * 移除元素
     *
     * @param edge 移除的元素
     * @return 是否成功
     */
    @Override
    boolean remove(Object edge) {
      if (super.remove(edge)) {
        degree--;
        return true;
      }
      return false;
    }

    @Override
    @SuppressWarnings("all")
    public EdgeBag<V, E> clone() {
      EdgeBag<V, E> bag = new EdgeBag<>(vertex);
      bag.loopNum = loopNum;
      bag.degree = degree;
      for (E e : this) {
        bag.add(e.copy());
        bag.degree--;
      }
      return bag;
    }

    @Override
    public int hashCode() {
      return super.hashCode() + EdgeBag.class.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      return super.equals(obj) && Objects.equals(obj.getClass(), EdgeBag.class);
    }

    private class EdgeBagIterator extends BagIterator {

      @Override
      public void remove() {
        throw new UnsupportedOperationException("Adjacent Iterator not support delete!");
      }
    }
  }
}
