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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import pers.jyb.graph.util.CollectionUtils;

class SameRankAdjacentRecord {

  private Map<DNode, SameRankAdjacentInfo> outSameRankAdjacent;

  private Map<DNode, Boolean> inSameRankRecord;

  public Map<DNode, SameRankAdjacentInfo> getOutSameRankAdjacent() {
    return outSameRankAdjacent;
  }

  void addOutAdjacent(DNode node, DLine line) {
    if (outSameRankAdjacent == null || line.other(node) == null) {
      outSameRankAdjacent = new HashMap<>();
    }

    SameRankAdjacentInfo sameRankAdjacentInfo = outSameRankAdjacent
        .computeIfAbsent(node, n -> new SameRankAdjacentInfo());

    // 另外一个顶点不能包含
    if (!outContains(line.other(node), node)) {
      if (sameRankAdjacentInfo.nodes == null) {
        sameRankAdjacentInfo.nodes = new TreeSet<>(Comparator.comparing(DNode::getNode));
      }
      sameRankAdjacentInfo.nodes.add(line.other(node));

      // 边记录
      if (sameRankAdjacentInfo.lines == null) {
        sameRankAdjacentInfo.lines = new ArrayList<>();
      }
      sameRankAdjacentInfo.lines.add(line);

      markHaveIn(line.other(node));
    } else {
      // 添加到other节点记录
      addOutAdjacent(line.other(node), line);
    }
  }

  void markHaveIn(DNode node) {
    if (inSameRankRecord == null) {
      inSameRankRecord = new HashMap<>();
    }

    inSameRankRecord.put(node, Boolean.TRUE);
  }

  void clearMarkIn() {
    if (inSameRankRecord != null) {
      inSameRankRecord.clear();
    }
  }

  boolean outContains(DNode node, DNode outNode) {
    if (outNode.isVirtual()) {
      return false;
    }
    if (outSameRankAdjacent == null) {
      return false;
    }

    SameRankAdjacentInfo sameRankAdjacentInfo = outSameRankAdjacent.get(node);
    if (sameRankAdjacentInfo == null) {
      return false;
    }

    Set<DNode> adjacent = sameRankAdjacentInfo.nodes;
    if (CollectionUtils.isEmpty(adjacent)) {
      return false;
    }

    return adjacent.contains(outNode);
  }

  Set<DNode> outAdjacent(DNode node) {
    if (outSameRankAdjacent == null) {
      return Collections.emptySet();
    }

    SameRankAdjacentInfo sameRankAdjacentInfo = outSameRankAdjacent.get(node);
    if (sameRankAdjacentInfo == null) {
      return Collections.emptySet();
    }

    Set<DNode> adjacent = sameRankAdjacentInfo.nodes;
    if (CollectionUtils.isEmpty(adjacent)) {
      return Collections.emptySet();
    }

    return adjacent;
  }

  boolean haveSameRank() {
    return outSameRankAdjacent != null;
  }

  boolean haveOut(DNode node) {
    return CollectionUtils.isNotEmpty(outAdjacent(node));
  }

  boolean haveIn(DNode node) {
    if (inSameRankRecord == null) {
      return false;
    }

    return Objects.equals(inSameRankRecord.get(node), Boolean.TRUE);
  }


  static class SameRankAdjacentInfo {

    Set<DNode> nodes;

    List<DLine> lines;
  }
}
