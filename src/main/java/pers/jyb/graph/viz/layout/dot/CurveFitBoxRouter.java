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

import java.util.List;
import java.util.function.IntPredicate;
import pers.jyb.graph.def.FlatPoint;
import pers.jyb.graph.def.UnfeasibleException;
import pers.jyb.graph.op.Curves;
import pers.jyb.graph.op.Curves.MultiBezierCurve;
import pers.jyb.graph.op.Curves.ThirdOrderBezierCurve;
import pers.jyb.graph.op.Vectors;

abstract class CurveFitBoxRouter extends BoxGuideLineRouter {

  protected static final int MAX_ITERATORS = 24;

  protected void straightenSpline(ThirdOrderBezierCurve curve) {
    curve.adjust(0.6, 0.6);
  }

  protected void refineSpline(SplineFitInfo splineFitInfo) {
    splineFitInfo.curve.adjust(0.9, 0.9);
  }

  protected SplineFitInfo splineIsFit(MultiBezierCurve curves, List<RouterBox> lineRouterBoxes,
                                    int boxStart, int boxEnd, boolean needOffset) {
    return splineCheck(curves, lineRouterBoxes, needOffset, true, boxStart, boxEnd);
  }

  protected SplitInfo splineSplit(MultiBezierCurve curves, List<RouterBox> lineRouterBoxes,
                                List<ThroughPoint> throughPoints, int pointStart,
                                int pointEnd, int boxStart, int boxEnd) {
    if (pointEnd - pointStart < 2) {
      return null;
    }

    // 距离最长的
    SplineFitInfo splineFitInfo = splineCheck(curves, lineRouterBoxes, true, false, boxStart, boxEnd);

    // 循环当前所有控制点，寻找切换的控制点和引导box
    for (int i = pointStart; i <= pointEnd; i++) {
      ThroughPoint point = throughPoints.get(i);

      if (point.getBoxIndex() <= splineFitInfo.boxIndex) {
        continue;
      }

      if (i == pointStart) {
        return null;
      }

      // 找到分界控制点，上一个控制点的box索引比最长超出的box小，当前的控制点的box索引比最长超出的box大
      SplitInfo splitInfo = new SplitInfo();

      if (i == pointStart + 1) {
        splitInfo.pointsSplitIndex = i;
        splitInfo.boxSplitIndex = point.getBoxIndex();
      } else if (i == pointEnd) {
        splitInfo.pointsSplitIndex = i - 1;
        splitInfo.boxSplitIndex = throughPoints.get(splitInfo.pointsSplitIndex).getBoxIndex();
      } else {
        int pointSplitIndex;
        ThroughPoint pre = throughPoints.get(pointSplitIndex = (i - 1));

        // 选取距离box较近的一个控制点
        if (splineFitInfo.boxIndex - pre.getBoxIndex()
            < point.getBoxIndex() - splineFitInfo.boxIndex) {
          point = pre;
        } else {
          pointSplitIndex = i;
        }

        splitInfo.pointsSplitIndex = pointSplitIndex;
        splitInfo.boxSplitIndex = point.getBoxIndex();
      }

      // 计算分离出的切线向量
      splitInfo.splitVector = splitCurveTangent(
          throughPoints.get(splitInfo.pointsSplitIndex - 1),
          throughPoints.get(splitInfo.pointsSplitIndex),
          throughPoints.get(splitInfo.pointsSplitIndex + 1)
      );

      return splitInfo;
    }

    return null;
  }

  private SplineFitInfo splineCheck(MultiBezierCurve curves, List<RouterBox> lineRouterBoxes,
                                    boolean needOffset, boolean firstOrFurthest,
                                    int boxStart, int boxEnd) {
    SplineFitInfo tmp = null;
    SplineFitInfo splineFitInfo = new SplineFitInfo();
    splineFitInfo.needOffset = needOffset;

    RouterBox first = lineRouterBoxes.get(0);
    RouterBox last = lineRouterBoxes.get(lineRouterBoxes.size() - 1);
    double boxMinY = (first.getUpBorder() + first.getDownBorder()) / 2;
    double boxMaxY = (last.getUpBorder() + last.getDownBorder()) / 2;

    for (int j = 0; j < curves.size(); j++) {
      ThirdOrderBezierCurve curve = curves.get(j);

      // 根据曲线曲率估计线段数量
      int segmentNum = curveLineSegmentNum(curve);
      for (int i = boxStart; i <= boxEnd; i++) {

        if (i < 0 || i >= lineRouterBoxes.size()) {
          continue;
        }

        RouterBox lineRouterBox = lineRouterBoxes.get(i);
        double minY = Math.min(curve.getV1().getY(), curve.getV4().getY());
        minY = Math.min(curve.getV2().getY(), minY);
        minY = Math.min(curve.getV3().getY(), minY);
        double maxY = Math.max(curve.getV1().getY(), curve.getV4().getY());
        maxY = Math.max(curve.getV2().getY(), maxY);
        maxY = Math.max(curve.getV3().getY(), maxY);

        // Y轴必须有公共区间
        if (minY != maxY
            && (minY <= lineRouterBox.getUpBorder() && maxY >= lineRouterBox.getUpBorder())
            || (minY <= lineRouterBox.getDownBorder() && maxY >= lineRouterBox.getDownBorder())
        ) {

          double ratio = Math.abs(
              minY != maxY
                  ? (Math.abs(lineRouterBox.getUpBorder() + lineRouterBox.getDownBorder()) / 2 - minY) /
                  (maxY - minY)
                  : 0.5
          );
          ratio = ratio > 1 ? 1 : ratio;

          if (!firstOrFurthest) {
            tmp = new SplineFitInfo(splineFitInfo);
          }
          if (!isInBox(curve, lineRouterBox, (int) (ratio * segmentNum),
                       segmentNum, boxMinY, boxMaxY, splineFitInfo)) {
            splineFitInfo.routerBox = lineRouterBox;
            splineFitInfo.boxIndex = i;
            splineFitInfo.curve = curve;
            if (firstOrFurthest) {
              return splineFitInfo;
            }
            if (tmp.curve != null && tmp.offset > splineFitInfo.offset) {
              splineFitInfo = tmp;
            }
          }
        }

        if (lineRouterBox.getUpBorder() > curve.getV4().getY()) {
          break;
        }
      }
    }

    return splineFitInfo;
  }

  private boolean isInBox(ThirdOrderBezierCurve curve, RouterBox routerBox,
                          int currentSegment, int segmentNum,
                          double minY, double maxY,
                          SplineFitInfo splineFitInfo) {
    double unit = (double) 1 / segmentNum;
    FlatPoint p1 = Curves.besselEquationCalc(
        unit * currentSegment,
        curve.getV1(),
        curve.getV2(),
        curve.getV3(),
        curve.getV4()
    );

    // 至少存在一个顶点的坐标在y区间内部，从两个方向开始访问，直到超出y区域
    if (!inBox(curve, p1, routerBox, unit, currentSegment, -1, minY, maxY, c -> 0 < c, splineFitInfo)
        && !splineFitInfo.needOffset) {
      return false;
    }

    return inBox(curve, p1, routerBox, unit, currentSegment, 1,
                 minY, maxY, c -> c < segmentNum, splineFitInfo);
  }

  private boolean inBox(ThirdOrderBezierCurve curve, FlatPoint p1, RouterBox routerBox, double unit,
                        int currentSegment, int addNum, double minY, double maxY,
                        IntPredicate breakCondition, SplineFitInfo splineFitInfo) {
    FlatPoint p2;
    boolean result = true;
    Integer count = null;
    while (breakCondition.test(currentSegment)) {
      p2 = Curves.besselEquationCalc(
          unit * (currentSegment += addNum),
          curve.getV1(),
          curve.getV2(),
          curve.getV3(),
          curve.getV4()
      );

      // 任意一个点位跳出了箱子的边界
      if (!RouterBox.inRange(minY, maxY, p1.getY()) || !RouterBox.inRange(minY, maxY, p2.getY())) {
        result = false;
        break;
      }

      if (routerBox.getUpBorder() > p1.getY() && routerBox.getUpBorder() > p2.getY()) {
        continue;
      }

      if (!approximatelyInBoxY(routerBox, p1.getY()) && !approximatelyInBoxY(routerBox, p2.getY())) {
        break;
      }

      if ((approximatelyInBoxY(routerBox, p1.getY()) && !approximatelyInBox(routerBox, p1))
          || (approximatelyInBoxY(routerBox, p2.getY()) && !approximatelyInBox(routerBox, p2))) {
        result = false;
        if (!splineFitInfo.needOffset) {
          break;
        }

        if (count == null) {
          count = 1;
        } else {
          count++;
        }
        splineFitInfo.offset += offset(p1, routerBox);
        splineFitInfo.offset += offset(p2, routerBox);
      }
      p1 = p2;
    }

    if (splineFitInfo.needOffset && count != null) {
      splineFitInfo.offset /= count;
    }

    return result;
  }

  private double offset(FlatPoint p, RouterBox routerBox) {
    if (approximatelyInBoxX(routerBox, p.getX())) {
      return 0;
    }

    return Math.abs(routerBox.closerHorizontalWall(p.getX()) - p.getX());
  }

  private boolean approximatelyInBoxX(RouterBox routerBox, double p) {
    return approximatelyInRange(routerBox.getLeftBorder(), routerBox.getRightBorder(), p);
  }

  private boolean approximatelyInBoxY(RouterBox routerBox, double p) {
    return approximatelyInRange(routerBox.getUpBorder(), routerBox.getDownBorder(), p);
  }

  private boolean approximatelyInRange(double left, double right, double v) {
    return RouterBox.inRange(left, right, v)
        || RouterBox.inRange(left, right, v - 1)
        || RouterBox.inRange(left, right, v + 1);
  }

  private boolean approximatelyInBox(RouterBox routerBox, FlatPoint p) {
    return approximatelyInRange(routerBox.getLeftBorder(), routerBox.getRightBorder(), p.getX())
        && approximatelyInRange(routerBox.getUpBorder(), routerBox.getDownBorder(), p.getY());
  }

  private int curveLineSegmentNum(ThirdOrderBezierCurve curve) {
    FlatPoint vector = Vectors.subVector(curve.getV4(), curve.getV1());
    FlatPoint leftTangent = Vectors.subVector(curve.getV2(), curve.getV1());
    FlatPoint rightTangent = Vectors.subVector(curve.getV3(), curve.getV4());

    double dist = vector.dist();
    double curvature = ((leftTangent.dist() / dist) + (rightTangent.dist() / dist)) / 2;

    FlatPoint intersection;
    try {
      intersection = Vectors.lineInters(curve.getV1(), curve.getV2(), curve.getV3(), curve.getV4());
      curvature += (angleRatio(leftTangent, vector) + (leftTangent.dist() / Vectors
          .subVector(intersection, curve.getV1()).dist())) / 2;
      curvature += (angleRatio(rightTangent, vector) + (rightTangent.dist() / Vectors
          .subVector(intersection, curve.getV4()).dist())) / 2;
      curvature /= 3;
    } catch (UnfeasibleException ignored) {
    }

    int n = (int) (curvature * 10);
    return n == 0 ? 1 : n << 1;
  }

  private double angleRatio(FlatPoint v1, FlatPoint v2) {
    return Math.abs(slopToDegree(v1) - slopToDegree(v2)) / 90;
  }

  private double slopToDegree(FlatPoint p) {
    return Math.toDegrees(Math.atan(p.getX() != 0 ? p.getY() / p.getX() : Double.MAX_VALUE));
  }

  private FlatPoint splitCurveTangent(FlatPoint up, FlatPoint p, FlatPoint down) {
    FlatPoint referVector = Vectors.addVector(
        Vectors.subVector(up, p),
        Vectors.subVector(down, p)
    );

    if (referVector.getY() == 0) {
      return Vectors.subVector(up, p);
    }

    double x = Math.sqrt(
        4 / (1 + Math.pow(referVector.getX(), 2) / Math.pow(referVector.getY(), 2))
    );
    double y = -x * referVector.getX() / referVector.getY();

    // 向量的方向如果向下，取向上位置的向量
    if (y > 0) {
      y = -y;
      x = -x;
    }

    return new FlatPoint(x, y);
  }

  protected static class SplineFitInfo {

    protected ThirdOrderBezierCurve curve;

    protected RouterBox routerBox;

    protected int boxIndex;

    protected double offset;

    protected boolean needOffset;

    protected SplineFitInfo() {
    }

    private SplineFitInfo(SplineFitInfo splineFitInfo) {
      this.curve = splineFitInfo.curve;
      this.routerBox = splineFitInfo.routerBox;
      this.boxIndex = splineFitInfo.boxIndex;
      this.offset = splineFitInfo.offset;
      this.needOffset = splineFitInfo.needOffset;
    }

    protected boolean isFit() {
      return curve == null;
    }
  }

  protected static class SplitInfo {

    protected int pointsSplitIndex;

    protected int boxSplitIndex;

    protected FlatPoint splitVector;
  }
}
