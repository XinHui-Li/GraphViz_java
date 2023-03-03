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

package pers.jyb.graph.viz.layout;

import java.util.List;
import java.util.Set;
import pers.jyb.graph.def.FlatPoint;
import pers.jyb.graph.util.Asserts;
import pers.jyb.graph.util.CollectionUtils;
import pers.jyb.graph.viz.draw.ArrowDrawProp;
import pers.jyb.graph.viz.draw.ClusterDrawProp;
import pers.jyb.graph.viz.draw.GraphvizDrawProp;
import pers.jyb.graph.viz.draw.LineDrawProp;
import pers.jyb.graph.viz.draw.NodeDrawProp;
import pers.jyb.graph.viz.layout.dot.RouterBox;

public class CombineShifter implements Shifter {

  private Set<FlatPoint> pointMark;

  private final List<ShifterStrategy> shifterStrategies;

  public CombineShifter(Set<FlatPoint> pointMark, List<ShifterStrategy> shifterStrategies) {
    this.pointMark = pointMark;
    this.shifterStrategies = shifterStrategies;
  }

  @Override
  public void graph(GraphvizDrawProp graphvizDrawProp) {
    Asserts.nullArgument(graphvizDrawProp, "graphvizDrawProp");
    for (ShifterStrategy shifterStrategy : shifterStrategies) {
      shifterStrategy.moveContainerDrawProp(graphvizDrawProp);
    }
  }

  @Override
  public void cluster(ClusterDrawProp clusterDrawProp) {
    Asserts.nullArgument(clusterDrawProp, "clusterDrawProp");
    for (ShifterStrategy shifterStrategy : shifterStrategies) {
      shifterStrategy.moveContainerDrawProp(clusterDrawProp);
    }
  }

  @Override
  public void node(NodeDrawProp nodeDrawProp) {
    Asserts.nullArgument(nodeDrawProp, "nodeDrawProp");
    for (ShifterStrategy shifterStrategy : shifterStrategies) {
      shifterStrategy.moveContainerDrawProp(nodeDrawProp);
    }
  }

  @Override
  public void line(LineDrawProp lineDrawProp) {
    Asserts.nullArgument(lineDrawProp, "lineDrawProp");

    for (FlatPoint point : lineDrawProp) {
      if (isMark(point)) {
        continue;
      }

      for (ShifterStrategy shifterStrategy : shifterStrategies) {
        shifterStrategy.movePoint(point);
      }
      markFlatPoint(point);
    }

    ArrowDrawProp arrowTail = lineDrawProp.getArrowTail();
    ArrowDrawProp arrowHead = lineDrawProp.getArrowHead();
    if (arrowTail != null) {
      markArrowDrawProp(arrowTail);
    }
    if (arrowHead != null) {
      markArrowDrawProp(arrowHead);
    }

    for (ShifterStrategy shifterStrategy : shifterStrategies) {
      shifterStrategy.movePoint(lineDrawProp.getLabelCenter());
    }

    List<RouterBox> routerBoxes = lineDrawProp.getBoxes();
    if (CollectionUtils.isNotEmpty(routerBoxes)) {
      for (RouterBox routerBox : routerBoxes) {
        for (ShifterStrategy shifterStrategy : shifterStrategies) {
          shifterStrategy.moveBox(routerBox);
        }
      }
    }

    for (FlatPoint floatLabelCenter : lineDrawProp.getFloatLabelFlatCenters().values()) {
      for (ShifterStrategy shifterStrategy : shifterStrategies) {
        shifterStrategy.movePoint(floatLabelCenter);
      }
    }
  }

  private void markArrowDrawProp(ArrowDrawProp arrowTail) {
    for (ShifterStrategy shifterStrategy : shifterStrategies) {
      if (isMark(arrowTail.getAxisBegin())) {
        continue;
      }
      shifterStrategy.movePoint(arrowTail.getAxisBegin());
    }
    markFlatPoint(arrowTail.getAxisBegin());
    for (ShifterStrategy shifterStrategy : shifterStrategies) {
      if (isMark(arrowTail.getAxisEnd())) {
        continue;
      }
      shifterStrategy.movePoint(arrowTail.getAxisEnd());
    }
    markFlatPoint(arrowTail.getAxisEnd());
  }

  private void markFlatPoint(FlatPoint point) {
    if (point == null || pointMark == null) {
      return;
    }

    pointMark.add(point);
  }

  private boolean isMark(FlatPoint point) {
    if (pointMark == null) {
      return false;
    }
    return pointMark.contains(point);
  }
}
