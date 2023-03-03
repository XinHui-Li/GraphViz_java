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
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * 使用邻接数组的存储的图。对如下一个无向图：<br>
 * <pre>
 *        0 --------- 2
 *       / \        / | \
 *     /    \     /   |   \
 *    5      \  /     3 -- 4
 *            1
 * 使用邻接数组表示为：
 *    Bags Arrays
 *    bag[0] vertex: 0 adjs:5 -&gt; 1 -&gt; 2
 *    bag[1] vertex: 1 adjs:0 -&gt; 2
 *    bag[2] vertex: 2 adjs:0 -&gt; 1 -&gt; 3 -&gt; 4
 *    bag[3] vertex: 3 adjs:2 -&gt; 4
 *    bag[4] vertex: 4 adjs:3 -&gt; 2
 *    bag[5] vertex: 5 adjs:0
 * </pre>
 *
 * <p>有向图{@code Digraph}中，一条边只会被一个数组成员<tt>VertexBag</tt>存储，而无向图{@code Graph}
 * 需要在<tt>source</tt>和<tt>target</tt>所在索引的<tt>VertexBag</tt>中存储两次。
 *
 * @param <V> 顶点类型
 * @author jiangyb
 * @see VertexOpGraph
 */
abstract class AdjVertexGraph<V> extends AbstractBaseGraph.AbstractVertexOpBase<V>
    implements Serializable {

  private static final long serialVersionUID = -4561713639260362179L;

  /**
   * 默认的初始化容量
   */
  static final int DEFAULT_CAPACITY = 1 << 5;

  /**
   * 顶点数量
   */
  int vertexNum;

  /**
   * 边数量
   */
  int edgeNum;

  /**
   * 邻接表数组对象，用来存储所有顶点以及顶点的所有邻近顶点
   */
  transient VertexBag<V>[] bags;

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
  AdjVertexGraph() {
    bags = new VertexBag[DEFAULT_CAPACITY];
  }

  /**
   * 使用指定的容量初始化。
   *
   * @param capacity 指定的容量
   * @throws IllegalArgumentException 容量小于等于0
   */
  @SuppressWarnings("unchecked")
  AdjVertexGraph(int capacity) {
    if (capacity <= 0) {
      throw new IllegalArgumentException("Illegal Capacity: " + capacity);
    }
    bags = new VertexBag[capacity];
  }

  /**
   * 使用顶点数组初始化。
   *
   * @param vertices 顶点数组
   * @throws IllegalArgumentException 顶点数组为空
   */
  @SuppressWarnings("unchecked")
  AdjVertexGraph(V[] vertices) {
    if (vertices == null || vertices.length == 0) {
      throw new IllegalArgumentException("vertices can not be empty");
    }
    int length;
    bags = new VertexBag[length = vertices.length];
    for (int i = 0; i < length; i++) {
      V v;
      if (null == (v = vertices[i])) {
        continue;
      }
      bags[i] = new VertexBag<>(v);
      if (v instanceof VertexIndex) {
        ((VertexIndex) v).getGraphIndex().put(checkAndReturnGraphRef(), i);
      }
      vertexNum++;
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
    VertexBag<V> bag = (VertexBag<V>) adjacent(v);
    if (bag != VertexBag.EMPTY) {
      return false;
    }
    addBag(v);
    return true;
  }

  /**
   * 相邻所有顶点
   *
   * @param v 需要获取的顶点
   * @return 相邻顶点的遍历对象
   */
  @Override
  @SuppressWarnings("unchecked")
  public Iterable<V> adjacent(Object v) {
    if (v == null) {
      return (VertexBag<V>) VertexBag.EMPTY;
    }
    if (v instanceof VertexIndex) {
      Integer index;
      index = ((VertexIndex) v).getGraphIndex().get(checkAndReturnGraphRef());
      if (index == null) {
        return (VertexBag<V>) VertexBag.EMPTY;
      }
      if (index >= 0 && index < vertexNum
          && v.equals(bags[index].vertex)) {
        return bags[index];
      }
    }
    return position(v);
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
    VertexBag<V> v = (VertexBag<V>) adjacent(vertex);
    return v != VertexBag.EMPTY ? v.degree : 0;
  }

  /**
   * 返回顶点自环数量。
   *
   * @param v 顶点
   * @return 顶点自环数量
   */
  @Override
  public int selfLoops(V v) {
    return ((VertexBag<V>) adjacent(v)).loopNum;
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

  /**
   * 返回顶点数组
   *
   * @return 顶点数组
   */
  @Override
  public V[] toArray() {
    if (vertexNum <= 0) {
      return null;
    }
    Class<?> clazz = bags[0].vertex.getClass();
    @SuppressWarnings("unchecked") V[] vertexs = (V[]) Array.newInstance(clazz, vertexNum);
    for (int i = 0; i < vertexNum; i++) {
      vertexs[i] = bags[i].vertex;
    }
    return vertexs;
  }

  @Override
  public abstract AdjVertexGraph<V> copy();

  @Override
  @SuppressWarnings("unchecked")
  public void clear() {
    vertexNum = 0;
    edgeNum = 0;
    modCount = 0;
    graphRef = null;
    bags = new VertexBag[DEFAULT_CAPACITY];
  }

  /**
   * 添加VertexBag并且返回对象
   *
   * @param v 添加的顶点
   * @return 顶点的邻接顶点包
   */
  protected VertexBag<V> addBag(V v) {
    VertexBag<V> bag;
    // 容量已满
    if (vertexNum == bags.length) {
      resize();
    }
    int vn = vertexNum++;
    bags[vn] = bag = new VertexBag<>(v);
    // 添加顶点索引
    if (v instanceof VertexIndex) {
      ((VertexIndex) v).getGraphIndex().put(checkAndReturnGraphRef(), vn);
    }
    modCount++;
    return bag;
  }

  /**
   * 容量满的时候，按照如下规则扩充： critical = {@link #rightRangeMinPowerOf2()}; vertexNum < critical *
   * 3/4，扩充为critical vertexNum >= critical * 3/4，扩充为2 * critical
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
  private VertexBag<V> position(Object v) {
    for (int i = 0; i < vertexNum; i++) {
      if (v.equals(bags[i].vertex)) {
        return bags[i];
      }
    }
    return (VertexBag<V>) VertexBag.EMPTY;
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
    if (!(obj instanceof AdjVertexGraph)) {
      return false;
    }
    if (this instanceof Digraph && !(obj instanceof Digraph)
        || !(this instanceof Digraph) && obj instanceof Digraph) {
      return false;
    }
    AdjVertexGraph<?> target = (AdjVertexGraph<?>) obj;
    if (target.vertexNum != vertexNum
        || target.edgeNum != edgeNum) {
      return false;
    }
    AdjVertexGraph<V> replGrap = copy();
    for (Object v : target) {
      for (Object w : target.adjacent(v)) {
        replGrap.removeEdge(v, w);
      }
    }
    return replGrap.edgeNum == 0;
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
   * @return 相邻顶点包数组
   */
  protected VertexBag<V>[] bagRepl() {
    @SuppressWarnings("unchecked")
    VertexBag<V>[] newBag = new VertexBag[bags.length];
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
      bags = new VertexBag[vertexNum];
      for (int i = 0; i < vertexNum; i++) {
        bags[i] = (VertexBag<V>) ois.readObject();
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
   * 顶点邻接表
   */
  protected static class VertexBag<V> extends Bag<V, V> implements Cloneable {

    private static final long serialVersionUID = -2484420246227776869L;

    static final VertexBag<?> EMPTY = new VertexBag<>(true);

    // 顶点度数
    int degree;

    // 自环数量
    int loopNum;

    VertexBag(V vertex) {
      super(vertex);
    }

    VertexBag(Boolean unmodify) {
      super(unmodify);
    }

    @Override
    public Iterator<V> iterator() {
      return new VertexBagIterator();
    }

    /**
     * 添加相邻顶点
     */
    @Override
    void add(V vertex) {
      super.add(vertex);
      degree++;
    }

    /**
     * 移除相邻顶点
     */
    @Override
    boolean remove(Object vertex) {
      if (super.remove(vertex)) {
        degree--;
        return true;
      }
      return false;
    }

    @Override
    @SuppressWarnings("all")
    public VertexBag<V> clone() {
      VertexBag<V> bag = new VertexBag<>(vertex);
      bag.loopNum = loopNum;
      bag.degree = degree;
      for (V e : this) {
        bag.add(e);
        bag.degree--;
      }
      return bag;
    }

    @Override
    public int hashCode() {
      return super.hashCode() + VertexBag.class.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      return super.equals(obj) && Objects.equals(obj.getClass(), VertexBag.class);
    }

    private class VertexBagIterator extends BagIterator {

      @Override
      public void remove() {
        throw new UnsupportedOperationException("Adjacent Iterator not support delete!");
      }
    }
  }
}
