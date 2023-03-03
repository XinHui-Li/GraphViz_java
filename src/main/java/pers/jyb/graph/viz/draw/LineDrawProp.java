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

package pers.jyb.graph.viz.draw;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import pers.jyb.graph.def.FlatPoint;
import pers.jyb.graph.util.Asserts;
import pers.jyb.graph.util.CollectionUtils;
import pers.jyb.graph.viz.api.FloatLabel;
import pers.jyb.graph.viz.api.Line;
import pers.jyb.graph.viz.api.LineAttrs;
import pers.jyb.graph.viz.api.Node;
import pers.jyb.graph.viz.layout.dot.RouterBox;

public class LineDrawProp extends ArrayList<FlatPoint> implements Serializable {

  private static final long serialVersionUID = 5529902024948360413L;

  private boolean isHeadStart;

  private final DrawGraph drawGraph;

  private final Line line;

  private FlatPoint start;

  private FlatPoint end;

  // label container center
  private FlatPoint labelCenter;

  private ArrowDrawProp arrowHead;

  private ArrowDrawProp arrowTail;

  private LineAttrs lineAttrs;

  private String id;

  private boolean isBesselCurve;

  private HashMap<FloatLabel, FlatPoint> floatLabelFlatCenters;

  private List<RouterBox> routerBoxes;

  public LineDrawProp(Line line, LineAttrs lineAttrs, DrawGraph drawGraph) {
    Asserts.nullArgument(line, "line");
    Asserts.nullArgument(lineAttrs, "lineAttrs");
    Asserts.nullArgument(drawGraph, "drawGraph");
    this.line = line;
    this.lineAttrs = lineAttrs;
    this.drawGraph = drawGraph;
  }

  @Override
  public boolean addAll(Collection<? extends FlatPoint> c) {
    if (CollectionUtils.isEmpty(c)) {
      return false;
    }

    for (FlatPoint point : c) {
      add(point);
    }
    return true;
  }

  @Override
  public boolean add(FlatPoint point) {
    if (super.add(point)) {
      refreshDrawGraphArea(point);
      return true;
    }

    return false;
  }

  @Override
  public void add(int index, FlatPoint point) {
    super.add(index, point);
    refreshDrawGraphArea(point);
  }

  @Override
  public FlatPoint set(int index, FlatPoint point) {
    FlatPoint p = super.set(index, point);
    refreshDrawGraphArea(point);
    return p;
  }

  public boolean addAndNotRefreshDrawGraph(FlatPoint point) {
    return super.add(point);
  }

  public void addFloatLabelCenter(FloatLabel floatLabel, FlatPoint center) {
    if (floatLabel == null || center == null) {
      return;
    }

    if (floatLabelFlatCenters == null) {
      floatLabelFlatCenters = new HashMap<>();
    }
    floatLabelFlatCenters.put(floatLabel, center);
  }

  private void refreshDrawGraphArea(FlatPoint point) {
    if (point != null) {
      drawGraph.updateXAxisRange(point.getX() - 10);
      drawGraph.updateXAxisRange(point.getX() + 10);
      drawGraph.updateYAxisRange(point.getY() - 10);
      drawGraph.updateYAxisRange(point.getY() + 10);
    }
  }

  public String id() {
    return Optional.ofNullable(lineAttrs.getId()).orElse(id);
  }

  public Line getLine() {
    return line;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public FlatPoint getStart() {
    return start;
  }

  public void setStart(FlatPoint start) {
    this.start = start;
  }

  public FlatPoint getEnd() {
    return end;
  }

  public void setEnd(FlatPoint end) {
    this.end = end;
  }

  public ArrowDrawProp getArrowHead() {
    return arrowHead;
  }

  public void setArrowHead(ArrowDrawProp arrowHead) {
    this.arrowHead = arrowHead;
  }

  public ArrowDrawProp getArrowTail() {
    return arrowTail;
  }

  public void setArrowTail(ArrowDrawProp arrowTail) {
    this.arrowTail = arrowTail;
  }

  public FlatPoint getLabelCenter() {
    return labelCenter;
  }

  public void setLabelCenter(FlatPoint labelCenter) {
    this.labelCenter = labelCenter;
  }

  public LineAttrs lineAttrs() {
    return lineAttrs;
  }

  public boolean isBesselCurve() {
    return isBesselCurve;
  }

  public void markIsBesselCurve() {
    isBesselCurve = true;
  }

  public void markIsLineSegment() {
    isBesselCurve = false;
  }

  public List<RouterBox> getBoxes() {
    return routerBoxes;
  }

  public void setBoxes(List<RouterBox> routerBoxes) {
    this.routerBoxes = routerBoxes;
  }

  public Map<FloatLabel, FlatPoint> getFloatLabelFlatCenters() {
    return floatLabelFlatCenters != null ? floatLabelFlatCenters : Collections.emptyMap();
  }

  public void fakeInit() {
    this.start = FlatPoint.ZERO;
    this.end = FlatPoint.ZERO;
  }

  public boolean isInit() {
    return start != null && end != null;
  }

  public boolean isHeadStart() {
    return isHeadStart;
  }

  public void setIsHeadStart(Node node) {
    if (node == null) {
      return;
    }
    this.isHeadStart = node == getLine().head();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    LineDrawProp that = (LineDrawProp) o;
    return isBesselCurve == that.isBesselCurve &&
        Objects.equals(line, that.line) &&
        Objects.equals(start, that.start) &&
        Objects.equals(end, that.end) &&
        Objects.equals(labelCenter, that.labelCenter) &&
        Objects.equals(arrowHead, that.arrowHead) &&
        Objects.equals(arrowTail, that.arrowTail) &&
        Objects.equals(lineAttrs, that.lineAttrs) &&
        Objects.equals(id, that.id) &&
        Objects.equals(routerBoxes, that.routerBoxes);
  }

  @Override
  public int hashCode() {
    return Objects
        .hash(super.hashCode(), line, start, end, labelCenter,
              arrowHead, arrowTail, lineAttrs, id, isBesselCurve, routerBoxes);
  }

  @Override
  public String toString() {
    return "LineDrawProp{" +
        "line=" + line +
        ", start=" + start +
        ", end=" + end +
        ", labelCenter=" + labelCenter +
        ", arrowHead=" + arrowHead +
        ", arrowTail=" + arrowTail +
        ", lineAttrs=" + lineAttrs +
        ", id='" + id + '\'' +
        ", isBesselCurve=" + isBesselCurve +
        ", routerBoxes=" + routerBoxes +
        '}';
  }
}
