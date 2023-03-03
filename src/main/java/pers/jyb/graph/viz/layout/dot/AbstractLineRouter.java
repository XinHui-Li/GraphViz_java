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
import java.util.List;
import pers.jyb.graph.def.FlatPoint;
import pers.jyb.graph.op.Curves;
import pers.jyb.graph.op.Curves.MultiBezierCurve;
import pers.jyb.graph.op.Curves.ThirdOrderBezierCurve;
import pers.jyb.graph.util.Asserts;
import pers.jyb.graph.viz.api.attributes.NodeShape;
import pers.jyb.graph.viz.api.attributes.Splines;
import pers.jyb.graph.viz.api.ext.Box;
import pers.jyb.graph.viz.api.ext.ShapePosition;

public abstract class AbstractLineRouter extends LineClip {

  // distance deviation tolerance
  protected static final double CLIP_DIST_ERROR = 0.1;

  protected boolean isSplineNone() {
    return drawGraph.getGraphviz().graphAttrs().getSplines() == Splines.NONE;
  }

  /**
   * A piecewise cubic Bessel converted to control points, adjacent curves share the same control
   * point.
   *
   * @param curves piecewise cubic Bessel
   * @return cubic Bessel control points
   */
  protected List<FlatPoint> multiBezierCurveToPoints(MultiBezierCurve curves) {
    List<FlatPoint> splines = new ArrayList<>(curves.size() * 3 + 1);

    for (int i = 0; i < curves.size(); i++) {
      ThirdOrderBezierCurve curve = curves.get(i);
      if (i == 0) {
        splines.add(curve.getV1());
      }
      splines.add(curve.getV2());
      splines.add(curve.getV3());
      splines.add(curve.getV4());
    }

    return splines;
  }

  /**
   * Convert the four control points of the curve into an array.
   *
   * @param curve cubic Bezier
   * @return control points
   */
  protected List<FlatPoint> thirdOrderBezierCurveToPoints(ThirdOrderBezierCurve curve) {
    List<FlatPoint> splines = new ArrayList<>(4);
    splines.add(curve.getV1());
    splines.add(curve.getV2());
    splines.add(curve.getV3());
    splines.add(curve.getV4());

    return splines;
  }

  /**
   * Divide the path using the tangent vector that the path intersects at the node boundary to fit
   * the path to the node shape.
   *
   * @param shapePosition shape position information
   * @param inPoint       point inside node
   * @param outPoint      point outside node
   * @return border crossing point
   */
  public static FlatPoint straightLineClipShape(ShapePosition shapePosition,
                                                FlatPoint inPoint, FlatPoint outPoint) {
    Asserts.nullArgument(shapePosition, "shapePosition");
    return straightLineClipShape(shapePosition, shapePosition.nodeShape(), inPoint, outPoint);
  }

  /**
   * Divide the path using the tangent vector that the path intersects at the node boundary to fit
   * the path to the node shape.
   *
   * @param box       node box
   * @param nodeShape node shape
   * @param inPoint   point inside node
   * @param outPoint  point outside node
   * @return border crossing point
   */
  public static FlatPoint straightLineClipShape(Box box, NodeShape nodeShape,
                                                FlatPoint inPoint, FlatPoint outPoint) {
    Asserts.nullArgument(inPoint, "inPoint");
    Asserts.nullArgument(outPoint, "outPoint");
    Asserts.nullArgument(box, "shapePosition");
    Asserts.nullArgument(nodeShape, "shapePosition.nodeShape()");

    Asserts.illegalArgument(
        !nodeShape.in(box, inPoint),
        "The specified internal node is not inside the node"
    );
    Asserts.illegalArgument(
        nodeShape.in(box, outPoint),
        "The specified external node is inside the node"
    );

    FlatPoint midPoint;
    FlatPoint in = inPoint;
    FlatPoint out = outPoint;

    do {
      midPoint = new FlatPoint((in.getX() + out.getX()) / 2, (in.getY() + out.getY()) / 2);

      if (nodeShape.in(box, midPoint)) {
        in = midPoint;
      } else {
        out = midPoint;
      }

    } while (FlatPoint.twoFlatPointDistance(in, out) > CLIP_DIST_ERROR);

    return midPoint;
  }

  /**
   * According to the shape object of the specified coordinates and size, cut the specified bessel
   * curve to ensure that the curve fits the specified shape.
   *
   * @param shapePosition shape position information
   * @param bezierCurve   the curve to be clip
   * @return curve after clip
   */
  public static ThirdOrderBezierCurve besselCurveClipShape(ShapePosition shapePosition,
                                                           ThirdOrderBezierCurve bezierCurve) {
    Asserts.nullArgument(shapePosition, "shapePosition");
    Asserts.nullArgument(shapePosition.nodeShape(), "shapePosition.nodeShape()");
    Asserts.nullArgument(bezierCurve, "bezierCurve");

    if (shapePosition.getHeight() <= 0 || shapePosition.getWidth() <= 0) {
      return bezierCurve;
    }

    FlatPoint v1 = bezierCurve.getV1();
    FlatPoint v2 = bezierCurve.getV2();
    FlatPoint v3 = bezierCurve.getV3();
    FlatPoint v4 = bezierCurve.getV4();

    NodeShape nodeShape = shapePosition.nodeShape();

    boolean v1In = nodeShape.in(shapePosition, v1);
    boolean v4In = nodeShape.in(shapePosition, v4);

    if (v1In && v4In) {
      return null;
    }

    if (!v1In && !v4In) {
      return bezierCurve;
    }

    double in = v1In ? 0 : 1;
    double out = v4In ? 0 : 1;
    FlatPoint[] points = {v1, v2, v3, v4};

    do {
      FlatPoint midPoint = Curves.besselEquationCalc((in + out) / 2, points);

      if (nodeShape.in(shapePosition, midPoint)) {
        in = (in + out) / 2;
      } else {
        out = (in + out) / 2;
      }

    } while (FlatPoint.twoFlatPointDistance(Curves.besselEquationCalc(in, points),
                                            Curves.besselEquationCalc(out, points))
        > CLIP_DIST_ERROR);

    return Curves.divideThirdBesselCurve(in, v4In, bezierCurve);
  }
}
