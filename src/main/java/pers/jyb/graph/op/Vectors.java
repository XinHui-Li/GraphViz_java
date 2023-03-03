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

package pers.jyb.graph.op;

import java.util.Objects;
import pers.jyb.graph.def.FlatPoint;
import pers.jyb.graph.def.UnfeasibleException;
import pers.jyb.graph.util.Asserts;
import pers.jyb.graph.util.ValueUtils;

/**
 * Vector operations.
 *
 * @author jiangyb
 */
public class Vectors {

  private Vectors() {
  }

  // unit vector
  public static FlatPoint unitVector(double x, double y) {
    double len = len(x, y);

    if (len != 0) {
      return new FlatPoint(x / len, y / len);
    }

    return new FlatPoint(x, y);
  }

  // compute vector and normalize vector, avoid creating an intermediate vector
  public static FlatPoint twoPointUnitVector(FlatPoint v1, FlatPoint v2) {
    Asserts.nullArgument(v1, "v1");
    Asserts.nullArgument(v2, "v2");
    double x = v1.getX() - v2.getX();
    double y = v1.getY() - v2.getY();
    return unitVector(x, y);
  }

  // get vector by p1,p2
  public static FlatPoint subVector(FlatPoint v1, FlatPoint v2) {
    Asserts.nullArgument(v1, "v1");
    Asserts.nullArgument(v2, "v2");
    return new FlatPoint(v1.getX() - v2.getX(), v1.getY() - v2.getY());
  }

  // add two vector
  public static FlatPoint addVector(FlatPoint v1, FlatPoint v2) {
    Asserts.nullArgument(v1, "v1");
    Asserts.nullArgument(v2, "v2");
    return new FlatPoint(v1.getX() + v2.getX(), v1.getY() + v2.getY());
  }

  // vector length is stretched to the target length
  public static FlatPoint vectorScale(FlatPoint vector, double newLen) {
    Asserts.nullArgument(vector, "vector");
    double len = len(vector.getX(), vector.getY());

    if (len != 0) {
      return new FlatPoint(vector.getX() * newLen / len, vector.getY() * newLen / len);
    }

    return vector;
  }

  public static FlatPoint multipleVector(FlatPoint vector, double multiple) {
    Asserts.nullArgument(vector, "vector");
    return new FlatPoint(vector.getX() * multiple, vector.getY() * multiple);
  }

  // return the dot product of vectors a and b
  public static double vectorDot(FlatPoint v1, FlatPoint v2) {
    Asserts.nullArgument(v1, "v1");
    Asserts.nullArgument(v2, "v2");
    return v1.getX() * v2.getX() + v1.getY() * v2.getY();
  }

  /**
   * 根据线性方程和求解点的纵坐标求横坐标。
   *
   * @param p1 线段顶点
   * @param p2 线段顶点
   * @param y  求解点的纵坐标
   * @return 求解点的横坐标
   */
  public static double linerFuncGetX(FlatPoint p1, FlatPoint p2, double y) {
    Asserts.nullArgument(p1, "p1");
    Asserts.nullArgument(p2, "p2");

    return linerFuncGetX(p1.getX(), p1.getY(), p2.getX(), p2.getY(), y);
  }

  /**
   * 根据线性方程和求解点的纵坐标求横坐标。
   *
   * @param startX 线段顶点的横坐标
   * @param startY 线段顶点的纵坐标
   * @param endX   线段顶点的横坐标
   * @param endY   线段顶点的纵坐标
   * @param y      求解点的纵坐标
   * @return 求解点的横坐标
   */
  public static double linerFuncGetX(double startX, double startY,
                                     double endX, double endY, double y) {
    Asserts.illegalArgument(startY == endY, "There are countless solutions to linear equations");

    if (startX == endX) {
      return startX;
    }

    double slope = (endY - startY) / (endX - startX);
    double constant = startY - startX * slope;

    return (y - constant) / slope;
  }

  /**
   * 根据线性方程和求解点的横坐标求纵坐标。
   *
   * @param p1 线段顶点
   * @param p2 线段顶点
   * @param x  求解点的横坐标
   * @return 求解点的纵坐标
   */
  public static double linerFuncGetY(FlatPoint p1, FlatPoint p2, double x) {
    Asserts.nullArgument(p1, "p1");
    Asserts.nullArgument(p2, "p2");

    return linerFuncGetY(p1.getX(), p1.getY(), p2.getX(), p2.getY(), x);
  }

  /**
   * 根据线性方程和求解点的横坐标求纵坐标。
   *
   * @param startX 线段顶点的横坐标
   * @param startY 线段顶点的纵坐标
   * @param endX   线段顶点的横坐标
   * @param endY   线段顶点的纵坐标
   * @param x      求解点的横坐标
   * @return 求解点的纵坐标
   */
  public static double linerFuncGetY(double startX, double startY,
                                     double endX, double endY, double x) {
    Asserts.illegalArgument(startX == endX, "There are countless solutions to linear equations");

    if (startY == endY) {
      return startY;
    }

    double slope = (endY - startY) / (endX - startX);
    double constant = startY - startX * slope;

    return slope * x + constant;
  }

  /**
   * Solve the intersection of two line segments, that is, the common solution of the linear
   * equation.
   *
   * @param line1P1 line1 endpoint
   * @param line1P2 line1 endpoint
   * @param line2P1 line2 endpoint
   * @param line2P2 line2 endpoint
   * @return the intersection of two line segments
   * @throws IllegalArgumentException linear have empty point
   * @throws UnfeasibleException      two linear equations have no common solution
   */
  public static FlatPoint lineInters(FlatPoint line1P1, FlatPoint line1P2,
                                     FlatPoint line2P1, FlatPoint line2P2)
      throws UnfeasibleException {
    Asserts.illegalArgument(
        line1P1 == null || line1P2 == null || line2P1 == null || line2P2 == null,
        "The line segment description is incomplete and there are null points"
    );
    Asserts.illegalArgument(
        Objects.equals(line1P1, line1P2) || Objects.equals(line2P1, line2P2),
        "Two points must form a line segment"
    );

    double m1 = line1P1.getX() - line1P2.getX();
    double d1 = line1P1.getY() - line1P2.getY();
    double m2 = line2P1.getX() - line2P2.getX();
    double d2 = line2P1.getY() - line2P2.getY();

    if ((m1 == 0 && m2 == 0) || (d1 == 0 && d2 == 0)) {
      throw new UnfeasibleException("No intersection between two line segments");
    }

    if (ValueUtils.approximate(m1, 0, 0.001)) {
      return new FlatPoint(
          line1P1.getX(),
          linerFuncGetY(line2P1, line2P2, line1P1.getX())
      );
    }

    if (ValueUtils.approximate(m2, 0, 0.001)) {
      return new FlatPoint(
          line2P1.getX(),
          linerFuncGetY(line1P1, line1P2, line2P1.getX())
      );
    }

    if (ValueUtils.approximate(d1, 0, 0.001)) {
      return new FlatPoint(
          linerFuncGetX(line2P1, line2P2, line1P1.getY()),
          line1P1.getY()
      );
    }

    if (ValueUtils.approximate(d2, 0, 0.001)) {
      return new FlatPoint(
          linerFuncGetX(line1P1, line1P2, line2P1.getY()),
          line2P1.getY()
      );
    }

    double slope1 = d1 / m1;
    double slope2 = d2 / m2;

    if (ValueUtils.approximate(slope1, slope2, 0.001)) {
      throw new UnfeasibleException("No intersection between two line segments");
    }

    double constant1 = line1P1.getY() - line1P1.getX() * slope1;
    double constant2 = line2P1.getY() - line2P1.getX() * slope2;
    double x = (constant1 - constant2) / (slope2 - slope1);

    return new FlatPoint(x, slope1 * x + constant1);
  }

  public static boolean inAngle(FlatPoint corner, FlatPoint p1, FlatPoint p2, FlatPoint target) {
    return inAngle(corner.getX(), corner.getY(),
                   p1.getX(), p1.getY(),
                   p2.getX(), p2.getY(),
                   target.getX(), target.getY());
  }

  public static boolean inAngle(double cornerX, double cornerY,
                                double p1x, double p1y,
                                double p2x, double p2y,
                                double testX, double testY) {
    return onLineDown(cornerX, cornerY, p1x, p1y, testX, testY)
        == onLineDown(cornerX, cornerY, p1x, p1y, p2x, p2y)
        && onLineDown(cornerX, cornerY, p2x, p2y, testX, testY)
        == onLineDown(cornerX, cornerY, p2x, p2y, p1x, p1y);
  }

  public static boolean onLineDown(double startX, double startY,
                                   double endX, double endY,
                                   double targetX, double targetY) {
    if (startX == endX && startY == endY) {
      return false;
    }

    // All node locate at the line would return false (not below the line)
    if (startX == endX) {
      return targetX > startX;
    }

    if (startY == endY) {
      return targetY < endY;
    }

    double val = linerFuncGetY(startX, startY, endX, endY, targetX);
    return val > targetY && !ValueUtils.approximate(val, targetY, 0.01);
  }

  public static double squaredLen(double x, double y) {
    return x * x + y * y;
  }

  public static double len(double x, double y) {
    return Math.sqrt(squaredLen(x, y));
  }
}
