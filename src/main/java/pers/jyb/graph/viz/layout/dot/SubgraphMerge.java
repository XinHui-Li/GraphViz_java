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

package pers.jyb.graph.viz.layout.dot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import pers.jyb.graph.def.Graph;
import pers.jyb.graph.def.UndirectedGraph;
import pers.jyb.graph.def.VertexIndex;
import pers.jyb.graph.util.Asserts;
import pers.jyb.graph.util.CollectionUtils;
import pers.jyb.graph.viz.api.GraphContainer;
import pers.jyb.graph.viz.api.Node;
import pers.jyb.graph.viz.api.Subgraph;
import pers.jyb.graph.viz.api.attributes.Rank;
import pers.jyb.graph.viz.draw.DrawGraph;

class SubgraphMerge {

  private final DotAttachment dotAttachment;

  private final GraphContainer container;

  private final List<SubNode> subNodes;

  private Map<DNode, MergeNode> mergeNodeMap;

  private boolean haveBorderNode;

  private static final SubgraphMerge EMPTY_SUBGRAPH_MERGE = new SubgraphMerge(null, null, null);

  private SubgraphMerge(GraphContainer container,
                        List<SubNode> subNodes,
                        DotAttachment dotAttachment) {
    this.container = container;
    this.subNodes = subNodes;
    this.dotAttachment = dotAttachment;

    /*
     * Use undirect connect graph, make sure the subgraphs who have common node use the same SubKey,
     * and let these subgrah's nodes map to the unique merge node.
     */
    subConnect();
  }

  boolean haveBorderNode() {
    return haveBorderNode;
  }

  MergeNode getMergeNode(DNode node) {
    if (mergeNodeMap == null) {
      return null;
    }

    return mergeNodeMap.get(node);
  }

  Iterable<DNode> nodes() {
    if (mergeNodeMap == null) {
      return Collections.emptyList();
    }

    return mergeNodeMap.keySet();
  }

  boolean isEmpty() {
    return mergeNodeMap == null;
  }

  static SubgraphMerge newSubgraphMerge(GraphContainer container,
                                        DotAttachment dotAttachment,
                                        Consumer<GraphContainer> containerConsumer) {
    Asserts.nullArgument(container, "container");
    Asserts.nullArgument(dotAttachment, "dotAttachment");

    List<SubNode> subNodes = new ArrayList<>(container.subgraphs().size());
    addSubNode(container, subNodes, dotAttachment, containerConsumer);

    if (CollectionUtils.isEmpty(subNodes)) {
      return EMPTY_SUBGRAPH_MERGE;
    }

    return new SubgraphMerge(container, subNodes, dotAttachment);
  }

  private static void addSubNode(GraphContainer container,
                                 List<SubNode> subNodes,
                                 DotAttachment dotAttachment,
                                 Consumer<GraphContainer> containerConsumer) {
    if (containerConsumer != null) {
      containerConsumer.accept(container);
    }

    for (Subgraph subgraph : container.subgraphs()) {
      if (subgraph.isTransparent()) {
        addSubNode(subgraph, subNodes, dotAttachment, containerConsumer);
        continue;
      }

      SubNode subNode = new SubNode(subgraph);
      subNodes.add(subNode);

      // add Subgraph node mask
      DrawGraph drawGraph = dotAttachment.getDrawGraph();
      for (Node node : subgraph.nodes()) {
        DNode dNode = dotAttachment.get(node);
        if (dNode.getContainer() != container) {
          continue;
        }

        subNode.addNodeId(drawGraph.nodeNo(node));
      }
    }
  }

  private void subConnect() {
    if (CollectionUtils.isEmpty(subNodes)) {
      return;
    }

    Graph.VertexGraph<SubNode> connectGraph = new UndirectedGraph<>(subNodes.size());

    // Use undirected graphs to represent intersections between subgraphs
    for (int i = 0; i < subNodes.size(); i++) {
      SubNode s1 = subNodes.get(i);

      for (int j = i + 1; j < subNodes.size(); j++) {
        SubNode s2 = subNodes.get(j);

        if (!s1.haveCommonNode(s2)) {
          continue;
        }

        connectGraph.addEdge(s1, s2);
      }
    }

    // Merge subgraphs using connected components
    Set<SubNode> mark = new HashSet<>(subNodes.size());
    for (SubNode subNode : subNodes) {
      if (mark.contains(subNode)) {
        continue;
      }

      dfs(newSubNodeKey(subNode, dotAttachment), subNode, mark, connectGraph);
    }

    for (SubNode subNode : subNodes) {
      for (Node node : subNode.subgraph.nodes()) {
        DNode dn = dotAttachment.get(node);
        if (dn.getContainer() != container) {
          continue;
        }

        if (mergeNodeMap == null) {
          mergeNodeMap = new HashMap<>();
        }

        MergeNode mergeNode = subNode.subKey.key;
        mergeNodeMap.put(dn, subNode.subKey.key);
        if (mergeNode.isBorder()) {
          this.haveBorderNode = true;
        }
      }
    }
  }

  private void dfs(SubKey subKey, SubNode subNode, Set<SubNode> mark,
                   Graph.VertexGraph<SubNode> connectGraph) {
    mark.add(subNode);
    subKey.key.rank = compareRankKey(subKey.key.rank, subNode.subgraph.getRank());
    subNode.subKey = subKey;

    for (SubNode node : connectGraph.adjacent(subNode)) {
      if (mark.contains(node)) {
        continue;
      }

      dfs(subKey, node, mark, connectGraph);
    }
  }

  private SubKey newSubNodeKey(SubNode s1, DotAttachment dotAttachment) {
    Node node = findFirst(s1.subgraph.nodes());
    MergeNode mergeNode = new MergeNode(dotAttachment.get(node), s1.subgraph.getRank());

    return new SubKey(mergeNode);
  }

  private Node findFirst(Iterable<Node> nodes) {
    for (Node node : nodes) {
      return node;
    }

    return null;
  }

  private Rank compareRankKey(Rank r1, Rank r2) {
    if (r1 == r2) {
      return r1;
    }

    if (r1 == Rank.SAME) {
      return r2;
    }

    if (r2 == Rank.SAME) {
      return r1;
    }

    if (r1 == Rank.MIN || r1 == Rank.SOURCE) {
      if (r2 == Rank.MAX || r2 == Rank.SINK) {
        throw new SubgrahOppositRankException();
      }

      return Rank.SOURCE;
    }

    if (r2 == Rank.MIN || r2 == Rank.SOURCE) {
      throw new SubgrahOppositRankException();
    }

    return Rank.SINK;
  }

  private static class SubNode extends VertexIndex {

    private static final long serialVersionUID = 6756911716157476290L;

    private SubKey subKey;

    private long[] bitNodeIds;

    private final Subgraph subgraph;

    private SubNode(Subgraph subgraph) {
      Asserts.nullArgument(subgraph, "subgraph");
      this.subgraph = subgraph;
      this.bitNodeIds = new long[((subgraph.nodeNum() - 1) / Long.SIZE) + 1];
    }

    private void addNodeId(int nodeId) {
      int segment = (nodeId - 1) / Long.SIZE;
      if (segment > bitNodeIds.length - 1) {
        bitNodeIds = Arrays.copyOf(bitNodeIds, segment + 1);
      }

      bitNodeIds[bitNodeIds.length - segment - 1] |= (1L << (nodeId - (Long.SIZE * segment) - 1));
    }

    boolean haveCommonNode(SubNode subNode) {
      if (subNode.bitNodeIds == null || bitNodeIds == null) {
        return false;
      }

      for (int i = bitNodeIds.length - 1, j = subNode.bitNodeIds.length - 1; i >= 0 && j >= 0;
          i--, j--) {
        if ((bitNodeIds[i] & subNode.bitNodeIds[j]) > 0) {
          return true;
        }
      }

      return false;
    }
  }

  private static class SubKey {

    private final MergeNode key;

    public SubKey(MergeNode key) {
      this.key = key;
    }
  }

  static class MergeNode {

    private final DNode node;

    private Rank rank;

    MergeNode(DNode node, Rank rank) {
      this.node = node;
      this.rank = rank;
    }

    DNode getNode() {
      return node;
    }

    Rank getRank() {
      return rank;
    }

    boolean shouldNoInDegree() {
      return rank == Rank.MIN || rank == Rank.SOURCE;
    }

    boolean shouldNoOutDegree() {
      return rank == Rank.MAX || rank == Rank.SINK;
    }

    boolean isBorder() {
      return rank == Rank.MIN || rank == Rank.SOURCE || rank == Rank.MAX || rank == Rank.SINK;
    }
  }
}
