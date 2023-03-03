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

package pers.jyb.graph.viz.api.ext;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.jyb.graph.def.FlatPoint;
import pers.jyb.graph.def.UnfeasibleException;
import pers.jyb.graph.op.Vectors;
import pers.jyb.graph.util.Asserts;
import pers.jyb.graph.util.CollectionUtils;
import pers.jyb.graph.viz.api.NodeAttrs;
import pers.jyb.graph.viz.api.attributes.NodeShape;

public class RegularPolylinePropCalc implements ShapePropCalc, Serializable {

  private static final long serialVersionUID = 8672914749052162330L;

  private static final Logger log = LoggerFactory.getLogger(RegularPolylinePropCalc.class);

  private int side = 4;

  private List<FlatPoint> flatPoints;

  public RegularPolylinePropCalc() {
  }

  private RegularPolylinePropCalc(int side) {
    Asserts.illegalArgument(side <= 0, "side can not be lower than 0");
    this.side = side;
  }

  @Override
  public FlatPoint minContainerSize(double innerHeight, double innerWidth) {
    double diameter = Math.sqrt(Math.pow(innerHeight, 2) + Math.pow(innerWidth, 2));
    diameter += expansion(diameter / 2, innerHeight, innerWidth);
    return new FlatPoint(diameter, diameter);
  }

  @Override
  public boolean in(Box box, FlatPoint point) {
    Asserts.nullArgument(box, "box");
    Asserts.nullArgument(point, "point");
    initPoints(box);

    for (int i = 0; i < flatPoints.size(); i++) {
      FlatPoint pre = adjPoint(i, true);
      FlatPoint next = adjPoint(i, false);
      FlatPoint current = flatPoints.get(i);
      if (!Vectors.inAngle(current, pre, next, point)) {
        return false;
      }
    }

    return true;
  }

  @Override
  public NodeShape post(NodeAttrs nodeAttrs) {
    Asserts.nullArgument(nodeAttrs, "nodeAttrs");
    NodeShape nodeShape = nodeAttrs.getNodeShape();
    Integer nodeSide = nodeAttrs.getSide();
    if (nodeSide == null) {
      return nodeShape;
    }

    RegularPolylinePropCalc propCalc = new RegularPolylinePropCalc(nodeSide);
    return (NodeShape) Proxy.newProxyInstance(
        NodeShape.class.getClassLoader(),
        new Class[]{NodeShape.class},
        new RegularProxyInvoker(nodeShape, propCalc));
  }

  public void initPoints(Box box) {
    Asserts.nullArgument(box, "box");
    if (flatPoints == null) {
      flatPoints = new ArrayList<>(side);
    }
    if (CollectionUtils.isNotEmpty(flatPoints)) {
      return;
    }

    flatPoints = calcPoints(box);
  }

  public List<FlatPoint> calcPoints(Box box) {
    Asserts.nullArgument(box, "box");
    List<FlatPoint> points = new ArrayList<>(side);

    double radius = box.getHeight() / 2;
    double perSideArc = 2 * Math.PI / side;
    double arc = getStartArc(perSideArc);

    for (int i = 0; i < side; i++) {
      double x = box.getX() + Math.cos(arc) * radius;
      double y = box.getY() - Math.sin(arc) * radius;
      points.add(new FlatPoint(x, y));
      arc += perSideArc;
    }

    return points;
  }

  public List<FlatPoint> getPoints() {
    return flatPoints;
  }

  // -------------------------- Shape proxy handler --------------------------
  private double getStartArc(double perSideArc) {
    double arc = Math.PI / 2;
    if (side % 2 == 1) {
      return arc;
    }

    return arc + (perSideArc / 2);
  }

  private FlatPoint adjPoint(int idx, boolean isPre) {
    Asserts.illegalArgument(idx < 0 || idx >= flatPoints.size(), "Wrong index");
    if (isPre) {
      if (idx == 0) {
        return flatPoints.get(flatPoints.size() - 1);
      } else {
        return flatPoints.get(idx - 1);
      }
    }

    if (idx == flatPoints.size() - 1) {
      return flatPoints.get(0);
    } else {
      return flatPoints.get(idx + 1);
    }
  }

  private double expansion(double radius, double height, double width) {
    double perSideArc = 2 * Math.PI / side;
    double arc = getStartArc(perSideArc);
    double pre = arc - perSideArc;
    double leftUpCornerArc = Math.PI / 2 + Math.atan(width / height);

    // Find the arc range that the left up corner belongs to.
    for (int i = 0; i < side; i++) {
      if (pre <= leftUpCornerArc && arc > leftUpCornerArc) {
        break;
      }
      pre = arc;
      arc += perSideArc;
    }

    FlatPoint p1 = new FlatPoint(Math.cos(pre) * radius, -Math.sin(pre) * radius);
    FlatPoint p2 = new FlatPoint(Math.cos(arc) * radius, -Math.sin(arc) * radius);
    FlatPoint leftUp = new FlatPoint(-width / 2, -height / 2);

    try {
      FlatPoint intersection = Vectors.lineInters(p1, p2, leftUp, FlatPoint.ZERO);
      double leftUpToCenterLen = FlatPoint.twoFlatPointDistance(leftUp, FlatPoint.ZERO);
      double interToCenterLen = FlatPoint.twoFlatPointDistance(intersection, FlatPoint.ZERO);

      if (leftUpToCenterLen <= interToCenterLen) {
        return 0;
      }

      return 2 * (leftUpToCenterLen - interToCenterLen) / Math.cos(perSideArc / 2);
    } catch (UnfeasibleException e) {
      log.error("Can not verify whether the left up is in polyline internal", e);
      return 0;
    }
  }

  // -------------------------- Shape proxy handler --------------------------
  private static class RegularProxyInvoker implements InvocationHandler {

    private NodeShape nodeShape;

    private RegularPolylinePropCalc propCalc;

    public RegularProxyInvoker(NodeShape nodeShape,
                               RegularPolylinePropCalc propCalc) {
      this.nodeShape = nodeShape;
      this.propCalc = propCalc;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      if (method.getName().equals("in")) {
        return propCalc.in((Box) args[0], (FlatPoint) args[1]);
      }
      if (method.getName().equals("minContainerSize")) {
        return propCalc.minContainerSize((double) args[0], (double) args[1]);
      }
      if (method.getName().equals("getShapePropCalc")) {
        return propCalc;
      }
      return method.invoke(nodeShape, args);
    }
  }
}
