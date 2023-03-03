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

import static pers.jyb.graph.viz.layout.dot.AbstractLineRouter.besselCurveClipShape;
import static pers.jyb.graph.viz.layout.dot.AbstractDotLineRouter.findInOutPair;
import static pers.jyb.graph.viz.layout.dot.AbstractDotLineRouter.getFirst;
import static pers.jyb.graph.viz.layout.dot.AbstractDotLineRouter.getLast;
import static pers.jyb.graph.viz.layout.dot.AbstractDotLineRouter.newArrowShapePosition;

import pers.jyb.graph.def.FlatPoint;
import pers.jyb.graph.op.Curves.ThirdOrderBezierCurve;
import pers.jyb.graph.util.CollectionUtils;
import pers.jyb.graph.viz.api.ext.ShapePosition;
import pers.jyb.graph.viz.draw.ClusterDrawProp;
import pers.jyb.graph.viz.draw.LineDrawProp;
import pers.jyb.graph.viz.draw.NodeDrawProp;
import pers.jyb.graph.viz.layout.dot.AbstractDotLineRouter.InOutPointPair;

public class CurvePathClip extends PathClip<LineDrawProp> {

  @Override
  protected FlatPoint pathFrom(LineDrawProp path) {
    return getFirst(path);
  }

  @Override
  protected FlatPoint pathTo(LineDrawProp path) {
    return getLast(path);
  }

  @Override
  protected LineDrawProp fromArrowClip(double arrowSize, LineDrawProp path) {
    ShapePosition arrowShapePosition = newArrowShapePosition(getFirst(path), arrowSize);
    InOutPointPair inOutPair = findInOutPair(3, path, true, arrowShapePosition);

    if (inOutPair != null) {
      ThirdOrderBezierCurve curve = getCurve(path, inOutPair);

      subPath(path, inOutPair, besselCurveClipShape(arrowShapePosition, curve));
    } else {
      path.clear();
    }

    return path;
  }

  @Override
  protected LineDrawProp toArrowClip(double arrowSize, LineDrawProp path) {
    ShapePosition arrowShapePosition = newArrowShapePosition(getLast(path), arrowSize);
    InOutPointPair inOutPair = findInOutPair(3, path, false, arrowShapePosition);

    if (inOutPair != null) {
      ThirdOrderBezierCurve curve = getCurve(path, inOutPair);

      subPath(path, inOutPair, besselCurveClipShape(arrowShapePosition, curve));
    } else {
      path.clear();
    }

    return path;
  }

  @Override
  protected LineDrawProp clusterClip(ClusterDrawProp clusterDrawProp, LineDrawProp path) {
    InOutPointPair inOutPair = findInOutPair(3, path, true, clusterDrawProp);

    if (inOutPair != null) {
      ThirdOrderBezierCurve curve = getCurve(path, inOutPair);

      subPath(path, inOutPair, besselCurveClipShape(clusterDrawProp, curve));
    } else {
      path.clear();
    }

    return path;
  }

  @Override
  protected LineDrawProp nodeClip(NodeDrawProp node, LineDrawProp path, boolean firstStart) {
    InOutPointPair inOutPair = findInOutPair(3, path, firstStart, node);

    if (inOutPair != null) {
      ThirdOrderBezierCurve curve = getCurve(path, inOutPair);

      subPath(path, inOutPair, besselCurveClipShape(node, curve));
    }

    return path;
  }

  @Override
  protected boolean isNull(LineDrawProp path) {
    return CollectionUtils.isEmpty(path);
  }

  private void subPath(LineDrawProp path, InOutPointPair inOutPair,
                       ThirdOrderBezierCurve curve) {
    if (inOutPair.isDeleteBefore()) {
      for (int i = 0; i <= inOutPair.getIdx() + 3; i++) {
        path.remove(0);
      }

      path.add(0, curve.getV4());
      path.add(0, curve.getV3());
      path.add(0, curve.getV2());
      path.add(0, curve.getV1());
    } else {
      int time = path.size() - inOutPair.getIdx() + 3;
      for (int i = 0; i < time; i++) {
        path.remove(path.size() - 1);
      }

      path.add(curve.getV1());
      path.add(curve.getV2());
      path.add(curve.getV3());
      path.add(curve.getV4());
    }
  }

  private ThirdOrderBezierCurve getCurve(LineDrawProp path, InOutPointPair inOutPair) {
    ThirdOrderBezierCurve curve;
    if (inOutPair.isDeleteBefore()) {
      curve = new ThirdOrderBezierCurve(
          inOutPair.getIn(),
          path.get(inOutPair.getIdx() + 1),
          path.get(inOutPair.getIdx() + 2),
          inOutPair.getOut()
      );
    } else {
      curve = new ThirdOrderBezierCurve(
          inOutPair.getOut(),
          path.get(inOutPair.getIdx() - 2),
          path.get(inOutPair.getIdx() - 1),
          inOutPair.getIn()
      );
    }
    return curve;
  }
}
