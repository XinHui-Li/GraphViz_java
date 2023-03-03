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

package pers.jyb.graph.viz.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import pers.jyb.graph.def.BiConcatIterable;
import pers.jyb.graph.def.VertexIndex;
import pers.jyb.graph.util.Asserts;
import pers.jyb.graph.util.ClassUtils;
import pers.jyb.graph.util.CollectionUtils;
import pers.jyb.graph.viz.api.Cluster.IntegrationClusterBuilder;
import pers.jyb.graph.viz.api.Subgraph.IntegrationSubgraphBuilder;

/**
 * 原图、集群、子图的公共容器。{@code GraphContainer}具有层级结构，当前的容器内可以拥有n个子图或者集群。
 *
 * @author jiangyb
 * @see Cluster
 * @see Subgraph
 * @see Graphviz
 */
@SuppressWarnings("all")
public abstract class GraphContainer extends VertexIndex {

  protected String id;

  protected volatile List<Subgraph> subgraphs;

  protected volatile List<Cluster> clusters;

  protected volatile Set<Line> lines;

  protected volatile Set<Node> nodes;

  protected Map<String, Object> nodeAttrsMap;

  protected Map<String, Object> lineAttrsMap;

  public String id() {
    return id;
  }

  /**
   * 返回容器是否是{@link Graphviz}。
   *
   * @return 返回容器是否是{@code Graphviz}
   */
  public boolean isGraphviz() {
    return this instanceof Graphviz;
  }

  /**
   * 返回容器是否是{@link Subgraph}。
   *
   * @return 返回容器是否是{@code Subgraph}
   */
  public boolean isSubgraph() {
    return this instanceof Subgraph;
  }

  /**
   * 返回容器是否是{@link Cluster}。
   *
   * @return 返回容器是否是{@code Cluster}
   */
  public boolean isCluster() {
    return this instanceof Cluster;
  }

  /**
   * 返回容器是否是空的，空的代表不含有任何顶点和边。
   *
   * @return 容器是否是空的
   */
  public boolean isEmpty() {
    if (absoluteEmpty()) {
      return true;
    }

    for (Subgraph subgraph : subgraphs()) {
      if (!subgraph.isEmpty()) {
        return false;
      }
    }

    for (Cluster cluster : clusters()) {
      if (!cluster.isEmpty()) {
        return false;
      }
    }

    return CollectionUtils.isEmpty(nodes);
  }

  /**
   * Determines whether the container is absolutely empty. This method is non-recursive.
   *
   * @return container is absolutely empty
   */
  public boolean absoluteEmpty() {
    return CollectionUtils.isEmpty(subgraphs)
        && CollectionUtils.isEmpty(clusters)
        && CollectionUtils.isEmpty(nodes)
        && CollectionUtils.isEmpty(lines);
  }

  /**
   * 返回当前图的所有子图。
   *
   * @return 当前图的所有子图
   */
  public List<Subgraph> subgraphs() {
    return CollectionUtils.isNotEmpty(subgraphs)
        ? Collections.unmodifiableList(subgraphs)
        : Collections.emptyList();
  }

  /**
   * 返回当前图的所有集群。
   *
   * @return 当前图的所有集群
   */
  public List<Cluster> clusters() {
    return CollectionUtils.isNotEmpty(clusters)
        ? Collections.unmodifiableList(clusters)
        : Collections.emptyList();
  }

  /**
   * 返回当前容器内部所有的节点。
   *
   * @return 当前容器内部所有的节点
   */
  public Iterable<Node> nodes() {
    if (CollectionUtils.isEmpty(subgraphs) && CollectionUtils.isEmpty(clusters)) {
      return CollectionUtils.isEmpty(nodes)
          ? Collections.emptyList()
          : Collections.unmodifiableSet(nodes);
    }

    List<Iterable<Node>> iterables = null;
    if (CollectionUtils.isNotEmpty(nodes)) {
      iterables = new ArrayList<>(1);
      iterables.add(nodes);
    }
    for (Subgraph subgraph : subgraphs()) {
      if (iterables == null) {
        iterables = new ArrayList<>(1);
      }
      iterables.add(subgraph.nodes());
    }
    for (Cluster cluster : clusters()) {
      if (iterables == null) {
        iterables = new ArrayList<>(1);
      }
      iterables.add(cluster.nodes());
    }

    return new BiConcatIterable<>(iterables);
  }

  /**
   * 返回容器当中直接添加的节点。
   *
   * @return 容器中直接添加的节点
   */
  public Set<Node> directNodes() {
    return CollectionUtils.isEmpty(nodes)
        ? Collections.emptySet()
        : Collections.unmodifiableSet(nodes);
  }

  /**
   * 返回当前容器内的节点数量。
   *
   * @return 当前容器内的节点数量
   */
  public int nodeNum() {
    int n = CollectionUtils.isNotEmpty(nodes) ? nodes.size() : 0;
    for (Subgraph subgraph : subgraphs()) {
      n += subgraph.nodeNum();
    }
    for (Cluster cluster : clusters()) {
      n += cluster.nodeNum();
    }
    return n;
  }

  /**
   * 返回当前容器内部所有的边。
   *
   * @return 当前容器内部所有的边
   */
  public Iterable<Line> lines() {
    if (CollectionUtils.isEmpty(subgraphs) && CollectionUtils.isEmpty(clusters)) {
      return CollectionUtils.isEmpty(lines)
          ? Collections.emptyList()
          : Collections.unmodifiableSet(lines);
    }
    List<Iterable<Line>> iterables = null;
    if (CollectionUtils.isNotEmpty(lines)) {
      iterables = new ArrayList<>(1);
      iterables.add(lines);
    }
    for (Subgraph subgraph : subgraphs()) {
      if (iterables == null) {
        iterables = new ArrayList<>(1);
      }
      iterables.add(subgraph.lines());
    }
    for (Cluster cluster : clusters()) {
      if (iterables == null) {
        iterables = new ArrayList<>(1);
      }
      iterables.add(cluster.lines());
    }

    return new BiConcatIterable<>(iterables);
  }


  /**
   * 返回容器当中直接添加的边。
   *
   * @return 容器中直接添加的边
   */
  public Set<Line> directLines() {
    return CollectionUtils.isEmpty(lines)
        ? Collections.emptySet()
        : Collections.unmodifiableSet(lines);
  }

  /**
   * 返回当前容器内的边数量。
   *
   * @return 当前容器内的边数量
   */
  public int lineNum() {
    int n = CollectionUtils.isNotEmpty(lines) ? lines.size() : 0;
    for (Subgraph subgraph : subgraphs()) {
      n += subgraph.lineNum();
    }
    for (Cluster cluster : clusters()) {
      n += cluster.lineNum();
    }
    return n;
  }

  /**
   * 返回节点在没在当前容器内。
   *
   * @param node 节点
   * @return true - 节点在容器内部 false - 节点不在容器内部
   */
  public boolean containsNode(Node node) {
    if (nodes == null) {
      return false;
    }

    if (nodes.contains(node)) {
      return true;
    }

    for (Subgraph subgraph : subgraphs()) {
      if (subgraph.containsNode(node)) {
        return true;
      }
    }

    for (Cluster cluster : clusters()) {
      if (cluster.containsNode(node)) {
        return true;
      }
    }

    return false;
  }

  /**
   * 返回边在没在当前容器内。
   *
   * @param line 边
   * @return true - 边在容器内部 false - 边不在容器内部
   */
  public boolean containsLine(Line line) {
    if (lines == null) {
      return false;
    }

    if (lines.contains(line)) {
      return true;
    }

    for (Subgraph subgraph : subgraphs()) {
      if (subgraph.containsLine(line)) {
        return true;
      }
    }

    for (Cluster cluster : clusters()) {
      if (cluster.containsLine(line)) {
        return true;
      }
    }

    return false;
  }

  /**
   * 返回当前容器是否是透明的。
   *
   * @return 当前容器是否是透明的
   */
  public boolean isTransparent() {
    return this instanceof Subgraph && ((Subgraph) this).getRank() == null;
  }

  /**
   * 返回{@link Node#nodeAttrs()}对应字段在当前容器的模板值。
   *
   * @param fieldName 字段名
   * @return 模板属性值
   */
  public Object getNodeAttr(String fieldName) {
    if (nodeAttrsMap == null) {
      return null;
    }
    return nodeAttrsMap.get(fieldName);
  }

  /**
   * 返回{@link Line#lineAttrs()}对应字段在当前容器的模板值。
   *
   * @param fieldName 字段名
   * @return 模板属性值
   */
  public Object getLineAttr(String fieldName) {
    if (lineAttrsMap == null) {
      return null;
    }
    return lineAttrsMap.get(fieldName);
  }

  /**
   * 返回是否存在节点属性模板。
   *
   * @return 是否存在节点属性模板
   */
  public boolean haveNodeTemp() {
    return nodeAttrsMap != null;
  }

  /**
   * 返回是否存在边属性模板。
   *
   * @return 是否存在边属性模板
   */
  public boolean haveLineTemp() {
    return lineAttrsMap != null;
  }

  /**
   * 返回是否有子的{@link Cluster}。
   *
   * @return 是否有子的{@code Cluster}
   */
  public boolean haveChildCluster() {
    return CollectionUtils.isNotEmpty(clusters);
  }

  synchronized void addSubgraph(Subgraph subgraph) {
    Asserts.nullArgument(subgraph, "subgraph");
    if (subgraphs == null) {
      subgraphs = new ArrayList<>();
    }

    subgraphs.add(subgraph);
  }

  synchronized void addCluster(Cluster cluster) {
    Asserts.nullArgument(cluster, "cluster");
    if (clusters == null) {
      clusters = new ArrayList<>();
    }

    clusters.add(cluster);
  }

  synchronized void addLine(Line line) {
    Asserts.nullArgument(line, "line");
    if (lines == null) {
      lines = new TreeSet<>();
    }
    if (nodes == null) {
      nodes = new TreeSet<>();
    }

    lines.add(line);
  }

  synchronized void addNode(Node node) {
    Asserts.nullArgument(node, "node");
    if (nodes == null) {
      nodes = new TreeSet<>();
    }

    nodes.add(node);
  }

  /**
   * {@code GraphContainer}的建造者。
   *
   * @param <G> 当前生产的类型
   * @param <B> 当前建造者类型
   * @see Cluster.ClusterBuilder
   * @see Subgraph.SubgraphBuilder
   * @see Graphviz.GraphvizBuilder
   * @see IntegrationSubgraphBuilder
   * @see IntegrationClusterBuilder
   */
  public abstract static class GraphContainerBuilder<
      G extends GraphContainer,
      B extends GraphContainerBuilder<G, B>> {

    protected volatile Map<String, Object> nodeAttrsMap;

    protected volatile Map<String, Object> lineAttrsMap;

    protected volatile G container;

    protected abstract B self();

    protected abstract G newContainer();

    public B id(String id) {
      initContainer().id = id;
      return self();
    }

    /**
     * 添加一个子图。
     *
     * @param subgraph 子图
     * @return 建造者自身
     */
    public B subgraph(Subgraph subgraph) {
      Asserts.nullArgument(subgraph, "subgraph");
      initContainer().addSubgraph(subgraph);
      return self();
    }

    /**
     * 添加一个集群子图。
     *
     * @param cluster 集群子图
     * @return 建造者自身
     */
    public B cluster(Cluster cluster) {
      Asserts.nullArgument(cluster, "cluster");
      initContainer().addCluster(cluster);
      return self();
    }

    /**
     * 创建子图。
     *
     * @return {@code IntegrationSubgraphBuilder}
     */
    public IntegrationSubgraphBuilder<G, B> startSub() {
      return new IntegrationSubgraphBuilder(this);
    }

    /**
     * 创建集群子图。
     *
     * @return {@code IntegrationClusterBuilder}
     */
    public IntegrationClusterBuilder<G, B> startClus() {
      return new IntegrationClusterBuilder(this);
    }

    /**
     * 创建一个模板节点，后续所有的节点的样式属性在没有设置的时候会继承此模板节点样式。
     *
     * @param node 样式模板节点
     * @return 建造者自身
     */
    public synchronized B tempNode(Node node) {
      Asserts.nullArgument(node, "node");
      NodeAttrs nodeAttrs = node.nodeAttrs();

      if (nodeAttrs != null) {
        try {
          this.nodeAttrsMap = ClassUtils.propValMap(nodeAttrs);
        } catch (IllegalAccessException ignore) {
        }
      }

      return self();
    }

    /**
     * 创建一个模板边，后续所有边的样式属性在没有设置的时候会继承此模板边样式。
     *
     * @param line 样式模板边
     * @return 建造者自身
     */
    public synchronized B tempLine(Line line) {
      Asserts.nullArgument(line, "line");
      LineAttrs lineAttrs = line.lineAttrs();

      if (lineAttrs != null) {
        try {
          this.lineAttrsMap = ClassUtils.propValMap(lineAttrs);
        } catch (IllegalAccessException ignore) {
        }
      }

      return self();
    }

    /**
     * 添加一个节点。
     *
     * @param node 节点
     * @return 建造者自身
     */
    public synchronized B addNode(Node node) {
      Asserts.nullArgument(node, "node");
      initContainer().addNode(node);
      return self();
    }

    /**
     * 添加节点。
     *
     * @param nodes 节点
     * @return 建造者自身
     */
    public synchronized B addNode(Node... nodes) {
      Asserts.illegalArgument(nodes == null || nodes.length == 0, "nodes can not be empty");
      for (Node node : nodes) {
        if (node != null) {
          initContainer().addNode(node);
        }
      }
      return self();
    }

    /**
     * 添加一条由{@code tail}和{@code head}组成的边。
     *
     * @param tail tail节点
     * @param head head节点
     * @return 建造者自身
     */
    public synchronized B addLine(Node tail, Node head) {
      return addLine(Line.builder(tail, head).build());
    }

    /**
     * 添加边，{@code nodes}当中，相邻的两个顶点会组成一条边。
     *
     * @param nodes 节点
     * @return 建造者自身
     */
    public synchronized B addLine(Node... nodes) {
      Asserts.illegalArgument(nodes == null || nodes.length == 0, "nodes can not be empty");
      Asserts.illegalArgument(nodes.length < 2, "nodes can not be lower than 2");

      for (int i = 0; i < nodes.length - 1; i++) {
        addLine(Line.builder(nodes[i], nodes[i + 1]).build());
      }

      return self();
    }

    /**
     * 添加一条边。
     *
     * @param line 添加的边
     * @return 建造者自身
     */
    public synchronized B addLine(Line line) {
      Asserts.nullArgument(line, "line");
      initContainer().addNode(line.head());
      initContainer().addNode(line.tail());
      initContainer().addLine(line);

      return self();
    }

    public synchronized G build() {
      G repl = copy();
      repl.id = initContainer().id;
      if (initContainer().subgraphs != null) {
        repl.subgraphs = new ArrayList<>(initContainer().subgraphs);
      }
      if (initContainer().clusters != null) {
        repl.clusters = new ArrayList<>(initContainer().clusters);
      }
      if (initContainer().nodes != null) {
        repl.nodes = new TreeSet<>(initContainer().nodes);
      }
      if (initContainer().lines != null) {
        repl.lines = new TreeSet<>(initContainer().lines);
      }
      repl.nodeAttrsMap = nodeAttrsMap;
      repl.lineAttrsMap = lineAttrsMap;
      return repl;
    }

    protected abstract G copy();

    protected synchronized G initContainer() {
      if (container == null) {
        container = newContainer();
      }

      return container;
    }
  }
}
