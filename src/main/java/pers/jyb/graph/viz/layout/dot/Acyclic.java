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

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.jyb.graph.viz.layout.Mark;

class Acyclic extends Mark<DNode> {

  private static final Logger log = LoggerFactory.getLogger(Acyclic.class);

  private DotDigraph digraph;

  private Set<DNode> accecssStack;

  Acyclic(DotDigraph digraph) {
    this.digraph = digraph;

    acyclic();
  }

  private void acyclic() {
    DLine line;
    while ((line = reverseLine()) != null) {

      if (log.isDebugEnabled()) {
        log.debug("Cycle line: {}", line);
      }

      // 自环
      if (Objects.equals(line.from(), line.to())) {
        digraph.removeEdge(line);
        if (!line.isVirtual()) {
          line.from().addSelfLine(line);
        }
      } else {
        // 翻转环边
        digraph.reverseEdge(line);
      }
    }
  }

  private DLine reverseLine() {
    if (accecssStack == null) {
      accecssStack = new HashSet<>(digraph.vertexNum());
    } else {
      accecssStack.clear();
    }
    clear();

    DLine line;
    for (DNode v : digraph) {
      if (isMark(v)) {
        continue;
      }

      if ((line = dfs(v)) != null) {
        return line;
      }
    }

    return null;
  }

  private DLine dfs(DNode v) {
    mark(v);

    accecssStack.add(v);

    DLine l;
    for (DLine line : digraph.adjacent(v)) {
      DNode w = line.other(v);

      if (accecssStack.contains(w)) {
        return line;
      }

      if (isMark(w)) {
        continue;
      }

      if ((l = dfs(w)) != null) {
        return l;
      }
    }

    accecssStack.remove(v);

    return null;
  }
}
