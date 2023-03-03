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
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import pers.jyb.graph.def.EdgeDedigraph;
import pers.jyb.graph.def.FlatPoint;
import pers.jyb.graph.util.CollectionUtils;
import pers.jyb.graph.viz.api.attributes.Port;
import pers.jyb.graph.viz.layout.dot.RankContent.RankNode;
import pers.jyb.graph.viz.layout.dot.SameRankAdjacentRecord.SameRankAdjacentInfo;

class LabelSupplement {

  private final RankContent rankContent;

  private final DotAttachment dotAttachment;

  private final EdgeDedigraph<DNode, DLine> digraphProxy;

  public LabelSupplement(RankContent rankContent,
                         DotAttachment dotAttachment,
                         EdgeDedigraph<DNode, DLine> digraphProxy) {
    this.rankContent = rankContent;
    this.dotAttachment = dotAttachment;
    this.digraphProxy = digraphProxy;

    // if have any label line, may be create some relay rank
    insertLabelNodeRank();

    // flat parallel edge handle
    flatParallelEdge();
  }

  /*
   * 插入label节点所在的内部层级。
   */
  private void insertLabelNodeRank() {
    if (CollectionUtils.isEmpty(dotAttachment.getLabelLines())) {
      return;
    }

    Set<Integer> needInsertLabelRankIdxs = null;

    for (DLine labelLine : dotAttachment.getLabelLines()) {
      DNode from = labelLine.from();
      DNode to = labelLine.to();

      if (!labelLine.haveLabel() || to.getRank() - from.getRank() != 1) {
        continue;
      }

      if (needInsertLabelRankIdxs == null) {
        needInsertLabelRankIdxs = new HashSet<>();
      }
      needInsertLabelRankIdxs.add(from.getRank());
    }

    if (CollectionUtils.isEmpty(needInsertLabelRankIdxs)) {
      return;
    }

    List<DLine> addLines = null;
    List<DLine> removeLines = null;

    for (Integer rankIdx : needInsertLabelRankIdxs) {
      RankNode rankNode = rankContent.get(rankIdx);
      RankNode labelRankNode = new RankNode(rankNode, rankNode.next(), 0, true);

      if (rankNode.next != null) {
        rankNode.next.pre = labelRankNode;
      }

      rankNode.next = labelRankNode;
      rankNode.rankSep /= 2;
      labelRankNode.rankSep = rankNode.rankSep;

      for (DNode node : rankNode) {
        for (DLine line : digraphProxy.outAdjacent(node)) {
          if (removeLines == null) {
            removeLines = new ArrayList<>();
            addLines = new ArrayList<>();
          }

          // cut line
          recordNewRemoveLines(line, labelRankNode, addLines, removeLines);
        }
      }
    }

    if (CollectionUtils.isEmpty(removeLines)) {
      return;
    }

    for (DLine removeLine : removeLines) {
      digraphProxy.removeEdge(removeLine);
    }

    for (DLine addLine : addLines) {
      digraphProxy.addEdge(addLine);
    }

    rankContent.rankIndexSync();

    RankNode current = rankContent.get(rankContent.minRank());
    while (current != null) {
      if (current.isLabelRank()) {
        current.sort(this::labelNodeComparator);
      }

      for (int j = 0; j < current.size(); j++) {
        DNode node = current.get(j);
        node.setRank(current.rankIndex());
        node.setRankIndex(j);
      }

      current = current.next();
    }
  }

  private void recordNewRemoveLines(DLine line, RankNode rankNode,
                                    List<DLine> addLines, List<DLine> removeLines) {
    removeLines.add(line);

    for (int i = 0; i < line.getParallelNums(); i++) {
      DLine edge = line.parallelLine(i);

      DNode virtual;
      FlatPoint labelSize = edge.getLabelSize();
      if (labelSize == null) {
        virtual = DNode.newVirtualNode(
            20,
            dotAttachment.commonParent(edge.from(), edge.to())
        );
      } else {
        virtual = new DNode(
            null,
            labelSize.getWidth(),
            labelSize.getHeight(),
            edge.from().getNodeSep(),
            edge.getLine()
        );
        virtual.setContainer(dotAttachment.commonParent(edge.from(), edge.to()));
      }

      addLines.add(
          new DLine(edge.from(), virtual,
                    edge.getLine(), edge.lineAttrs(),
                    edge.weight(), edge.limit())
      );
      addLines.add(
          new DLine(virtual, edge.to(),
                    edge.getLine(), edge.lineAttrs(),
                    edge.weight(), edge.limit())
      );

      rankNode.add(virtual);
    }
  }

  private int labelNodeComparator(DNode left, DNode right) {
    DNode leftPreNode = null;
    DNode leftNextNode = null;
    DNode rightPreNode = null;
    DNode rightNextNode = null;
    DLine leftLine = null;
    DLine rightLine = null;

    for (DLine line : digraphProxy.inAdjacent(left)) {
      leftPreNode = line.from();
    }

    for (DLine line : digraphProxy.outAdjacent(left)) {
      leftNextNode = line.to();
      leftLine = line;
    }

    for (DLine line : digraphProxy.inAdjacent(right)) {
      rightPreNode = line.from();
    }

    for (DLine line : digraphProxy.outAdjacent(right)) {
      rightNextNode = line.to();
      rightLine = line;
    }

    if (leftPreNode == null || leftNextNode == null
        || rightPreNode == null || rightNextNode == null) {
      return 0;
    }

    int r = Double.compare(leftPreNode.getRankIndex() + leftNextNode.getRankIndex(),
                           rightPreNode.getRankIndex() + rightNextNode.getRankIndex());

    if (r != 0 || left.name() == null || right.name() == null) {
      return r;
    }

    if (leftPreNode == rightPreNode && leftNextNode == rightNextNode) {
      Port leftPrePort = PortHelper.getLineEndPointPort(leftPreNode.getNode(),
                                                        leftLine.getLine(),
                                                        dotAttachment.getDrawGraph());
      Port leftNextPort = PortHelper.getLineEndPointPort(leftNextNode.getNode(),
                                                         leftLine.getLine(),
                                                         dotAttachment.getDrawGraph());
      Port rightPrePort = PortHelper.getLineEndPointPort(rightPreNode.getNode(),
                                                         rightLine.getLine(),
                                                         dotAttachment.getDrawGraph());
      Port rightNextPort = PortHelper.getLineEndPointPort(rightNextNode.getNode(),
                                                          rightLine.getLine(),
                                                          dotAttachment.getDrawGraph());
      r = Integer.compare(
          PortHelper.crossPortNo(leftPrePort) + PortHelper.crossPortNo(leftNextPort),
          PortHelper.crossPortNo(rightPrePort) + PortHelper.crossPortNo(rightNextPort)
      );
      if (r != 0) {
        return r;
      }
    }

    return left.name().compareTo(right.name());
  }

  private void flatParallelEdge() {
    if (dotAttachment.getSameRankAdjacentRecord() == null
        || !dotAttachment.getSameRankAdjacentRecord().haveSameRank()) {
      return;
    }

    SameRankAdjacentRecord sameRankAdjacentRecord = dotAttachment.getSameRankAdjacentRecord();

    /*
     * 同层级顶点之间的唯一边 DLine 记录
     * {
     *    node1 : {
     *        node2 : edge1, // node1和node2之间的唯一边edge1
     *        node3 : edge2, // node1和node3之间的唯一边edge2
     *    }
     * }
     * */
    Map<DNode, Map<DNode, DLine>> parallelEdgeRecord = null;
    Map<DLine, DNode> flatLabelNodeRecord = null;

    for (Entry<DNode, SameRankAdjacentInfo> rankAdjacentInfoEntry :
        sameRankAdjacentRecord.getOutSameRankAdjacent().entrySet()) {
      DNode node = rankAdjacentInfoEntry.getKey();
      // 节点同层级的节点和边的一些信息
      SameRankAdjacentInfo sameRankAdjacentInfo = rankAdjacentInfoEntry.getValue();

      if (CollectionUtils.isEmpty(sameRankAdjacentInfo.lines)) {
        continue;
      }

      // 顶点所有同层级出边
      for (DLine line : sameRankAdjacentInfo.lines) {
        DNode other = line.other(node);

        if (node == other) {
          continue;
        }

        if (parallelEdgeRecord == null) {
          parallelEdgeRecord = new HashMap<>();
        }

        DLine mergeLine = null;
        // 获取顶点对应的所有出边顶点的记录
        Map<DNode, DLine> linePair = parallelEdgeRecord.get(node);
        if (linePair != null) {
          // 所有出边顶点当中，是否已经有当前顶点的记录边，如果有，此边作为合并边
          mergeLine = linePair.get(line.other(node));
        }

        if (mergeLine == null) {
          // 不存在合并边，当前边作为合并边
          if (linePair == null) {
            linePair = new HashMap<>(1);
          }
          // 存放两个点之间的唯一边记录
          linePair.put(line.other(node), line);
          parallelEdgeRecord.put(node, linePair);
        } else {
          if (line.from() != mergeLine.from()) {
            line = line.reverse();
          }
          mergeLine.addParallelEdge(line);
        }
      }
    }

    if (parallelEdgeRecord == null) {
      return;
    }

    for (Map<DNode, DLine> value : parallelEdgeRecord.values()) {
      for (DLine line : value.values()) {
        // 如果合并的FlatEdge当中含有labelLine，当前的mergeLine会变成Label Node
        if (line.haveLabel()) {
          if (flatLabelNodeRecord == null) {
            flatLabelNodeRecord = new HashMap<>();
          }

          DNode flatLabelNode = flatLabelNodeRecord.computeIfAbsent(
              line,
              ml -> new DNode(
                  null, 0, 0,
                  ml.isSameRankAdj()
                      ? ml.from().getNodeSep() / 2
                      : ml.from().getNodeSep(),
                  ml
              )
          );

          flatLabelNode.setContainer(dotAttachment.commonParent(line.from(), line.to()));

          if (line.isSameRankAdj()) {
            // nodesep减半
            line.from().nodeSepHalving();
          }
        }
      }
    }

    // 插入LabelNode
    insertFlatLabelNode(flatLabelNodeRecord);
  }

  private void insertFlatLabelNode(Map<DLine, DNode> flatLabelNodeRecord) {
    if (flatLabelNodeRecord == null) {
      return;
    }

    // 需要在下一个层级插入新的虚拟顶点的层级
    List<RankNode> needInsertVirtualRank = null;
    // 新插入的层级的层级索引，处于原始两个层级当中的中值
    Map<RankNode, Double> newRankNodeIndex = null;
    // 层级需要插入的label node的优先队列映射
    Map<RankNode, Queue<DNode>> rankLabelNodeQueue = null;
    double minRankIndex = Double.MAX_VALUE;
    RankNode minRankNode = null;

    for (DNode flatLabelNode : flatLabelNodeRecord.values()) {
      DLine labelLine = flatLabelNode.getFlatLabelLine();
      if (labelLine == null) {
        continue;
      }

      // 设置labelNode的中值
      flatLabelNode.setMedian(
          (double) (labelLine.from().getRankIndex() + labelLine.to().getRankIndex()) / 2
      );

      RankNode rankNode = rankContent.get(labelLine.from().getRank());

      // 同层级相邻，直接插入
      if (!labelLine.isSameRankAdj()) {
        // 前一个层级为空，或者前一个层级含有正常节点，需要插入新的层级
        if (rankNode.pre() == null || !rankNode.pre().noNormalNode()) {
          if (rankNode.pre() != null) {
            if (needInsertVirtualRank == null) {
              needInsertVirtualRank = new ArrayList<>(1);
            }

            needInsertVirtualRank.add(rankNode.pre());
          }

          double ri = rankNode.pre() != null
              ? (double) (rankNode.pre().rankIndex() + rankNode.rankIndex()) / 2
              : rankNode.rankIndex() - 1;
          rankNode = rankContent.insertLabelRankNode(rankNode.rankIndex());
          if (newRankNodeIndex == null) {
            newRankNodeIndex = new HashMap<>(1);
          }
          newRankNodeIndex.put(rankNode, ri);
        } else { // 前一个层级不为空，并且只含有虚拟顶点
          rankNode = rankNode.pre();
        }
      }

      if (rankNode == null) {
        continue;
      }

      // 最小层级
      Double ri;
      if (minRankNode == null
          || (newRankNodeIndex != null && (ri = newRankNodeIndex.get(rankNode)) != null
          && ri < minRankIndex)
          || rankNode.rankIndex() < minRankIndex
      ) {
        minRankNode = rankNode;
        if (newRankNodeIndex != null) {
          ri = newRankNodeIndex.get(rankNode);
          if (ri != null) {
            minRankIndex = ri;
          } else {
            minRankIndex = rankNode.rankIndex();
          }
        } else {
          minRankIndex = rankNode.rankIndex();
        }
      }

      if (rankLabelNodeQueue == null) {
        rankLabelNodeQueue = new HashMap<>(4);
      }
      rankLabelNodeQueue.computeIfAbsent(
          rankNode,
          k -> new PriorityQueue<>(Comparator.comparing(DNode::getMedian))
      ).offer(flatLabelNode);
    }

    // 同步层级属性
    rankContent.rankIndexSync();

    // 新插入的层级的前一个层级，如果存在的话需要插入新的虚拟节点
    newRankAddVirtualNode(needInsertVirtualRank);

    // 同步层级中的节点的rank和rankIndex
    syncNodeProp(minRankNode, rankLabelNodeQueue);
  }

  private void newRankAddVirtualNode(List<RankNode> needInsertVirtualRank) {
    if (CollectionUtils.isEmpty(needInsertVirtualRank)) {
      return;
    }

    // 把需要移除的边记录下来
    List<DLine> removeLines = new ArrayList<>();
    for (RankNode rankNode : needInsertVirtualRank) {

      // 获取层级的所有待移除的边
      for (int i = 0; i < rankNode.size(); i++) {
        DNode node = rankNode.get(i);

        for (DLine line : digraphProxy.outAdjacent(node)) {
          if (line.isSameRank()) {
            continue;
          }
          removeLines.add(line);
        }
      }

      RankNode next = rankNode.next();

      // Graph上打断边，并且在对应的RankNode上面加入节点
      for (DLine removeLine : removeLines) {
        // 平行边处理
        if (removeLine.isParallelMerge()) {
          for (int i = 0; i < removeLine.getParallelNums(); i++) {
            cutLine(next, removeLine.parallelLine(i));
          }
        } else {
          cutLine(next, removeLine);
        }
      }

      // 移除
      removeLines.clear();
    }
  }

  private void syncNodeProp(RankNode rankNode, Map<RankNode, Queue<DNode>> rankLabelNodeQueue) {
    if (rankNode == null || rankLabelNodeQueue == null) {
      return;
    }

    while (rankNode != null) {
      // 获取层级的待插入的虚拟节点的优先队列，排序值为中值
      Queue<DNode> labelNodes = rankLabelNodeQueue.get(rankNode);
      int i = 0;
      for (; i < rankNode.size(); i++) {
        DNode node = rankNode.get(i);
        // 获取当前节点的中值
        double median = outMedian(node, rankNode.noNormalNode());

        // 找到第一个比当前节点中值小的节点，插入到当前节点的前面
        while (labelNodes != null && !labelNodes.isEmpty()) {
          DNode peek = labelNodes.peek();
          if (peek.getMedian() > median) {
            break;
          }
          labelNodes.poll();
          peek.setRank(rankNode.rankIndex());
          peek.setRankIndex(i);
          rankNode.add(i++, peek);
          digraphProxy.add(peek);

          // 添加像拱桥一样的FlatLine的辅助边
          addArchBridgeFlatLine(peek, rankNode);
        }
        node.setRank(rankNode.rankIndex());
        node.setRankIndex(i);
      }

      // 剩余的节点都添加进去
      while (labelNodes != null && !labelNodes.isEmpty()) {
        DNode poll = labelNodes.poll();
        poll.setRank(rankNode.rankIndex());
        poll.setRankIndex(i++);
        rankNode.add(poll);
        digraphProxy.add(poll);

        // 添加像拱桥一样的FlatLine的辅助边
        addArchBridgeFlatLine(poll, rankNode);
      }

      rankNode = rankNode.next();
    }
  }

  private void cutLine(RankNode next, DLine removeLine) {
    DNode virtual = DNode.newVirtualNode(
        20,
        dotAttachment.commonParent(removeLine.from(), removeLine.to())
    );
    digraphProxy.removeEdge(removeLine);
    digraphProxy.addEdge(
        new DLine(removeLine.from(), virtual,
                  removeLine.getLine(), removeLine.lineAttrs(),
                  removeLine.weight(), removeLine.limit())
    );
    digraphProxy.addEdge(
        new DLine(virtual, removeLine.to(),
                  removeLine.getLine(), removeLine.lineAttrs(),
                  removeLine.weight(), removeLine.limit())
    );
    // RankNode增加记录
    next.add(virtual);
  }

  private void addArchBridgeFlatLine(DNode flatLabelNode, RankNode rankNode) {
    if (flatLabelNode.getFlatLabelLine() == null) {
      return;
    }

    DLine flatLabelLine = flatLabelNode.getFlatLabelLine();
    DNode next = rankNode.get(flatLabelNode.getRankIndex() + 1);
    if (next != null && (flatLabelLine.from() == next || flatLabelLine.to() == next)) {
      return;
    }

    digraphProxy.addEdge(
        new DLine(flatLabelNode, flatLabelLine.from(),
                  flatLabelLine.getLine(), flatLabelLine.lineAttrs(),
                  flatLabelLine.weight(), flatLabelLine.limit())
    );
    digraphProxy.addEdge(
        new DLine(flatLabelNode, flatLabelLine.to(),
                  flatLabelLine.getLine(), flatLabelLine.lineAttrs(),
                  flatLabelLine.weight(), flatLabelLine.limit())
    );
  }

  private double outMedian(DNode node, boolean isLabelRank) {
    double median = 0;
    if (isLabelRank) {
      for (DLine line : digraphProxy.outAdjacent(node)) {
        median = line.other(node).getRankIndex();
      }
    } else {
      median = node.getRankIndex();
    }

    return median;
  }
}
