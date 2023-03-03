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

import java.math.BigDecimal;
import java.util.Arrays;
import pers.jyb.graph.def.ArrayMatrix;
import pers.jyb.graph.def.Matrix;
import pers.jyb.graph.def.UnfeasibleException;

/**
 * <p>单纯形算法是一种在给定线性目标函数的情况下获得约束线性系统最优解的方法。它从可行区域的
 * 基本顶点开始，然后迭代地移至相邻顶点，每次求解时都进行改进，直到找到最佳解为止。
 *
 * <p>使用{@code Simplex}之前，需要掌握下述的概念，把对应的线性规划问题转换为标准的矩阵描述。
 *
 * <p>单纯形算法首先将约束条件和目标函数方程组。这是通过引入新的变量称为松弛变量。松弛变量代表
 * 与极值（最大或最小）的差值。例如：2x+3y &le; 90 变为 2x + 3y + s1 = 90。其中s1就是松弛变量。
 *
 * <p>一个极大化的线性规划问题转换为增广矩阵的例子如下：
 * <pre>
 * 约束条件：
 * {
 *      2x + 3y &le; 90
 *      3x + 2y &le; 120
 *      x  &ge; 0
 *      y  &ge; 0
 * }
 * 目标方程： f(x,y) = 7x + 5y.
 *
 * 加入松弛变量后变为方程组：
 * {
 *      z - 7x - 5y           = 0
 *          2x + 3y + s1      = 90
 *          3x + 2y      + s2 = 120
 * }
 *
 * 转换为增广矩阵：
 * [
 * 1  -7  -5  0  0 | 0
 * 0  2   3   1  0 | 90
 * 0  3   2   0  1 | 120
 * ]
 * </pre>
 *
 * <p>线性规划的标准型如下：
 * <div style="text-align:center;font-size:16px;background-color:#CCCCCC">
 *      Maximize: c<sup>T</sup> * x <br>
 *      Subject to: Ax &le; b,x<sub>i</sub> &ge; 0;
 * </div>
 * 其中c是包含目标函数系数的向量，x是包含问题变量的向量，A是包含约束系数的矩阵，并且b
 * 是一个向量，包含这些约束的最大值。
 *
 * @author jiangyb
 * @see Matrix
 */
public class Simplex {

  /**
   * 计算当中保留多少小数位
   */
  private final int scale;

  /**
   * 约束系数矩阵
   */
  private final Matrix<BigDecimal> consCoeff;

  /**
   * 最大约束向量
   */
  private final BigDecimal[] maxVector;

  /**
   * 目标函数向量
   */
  private final BigDecimal[] objVector;

  /**
   * 最优解
   */
  private BigDecimal[] optimalSolu;

  /**
   * 目标函数最大值
   */
  private BigDecimal maxObjVal;

  /**
   * 使用线性规划标准型初始化，构建之后并不会立即执行单纯形计算，需要手动调用{@link #calc()}开启计算。
   *
   * @param scale     小数位数精确度
   * @param consCoeff 约束系数矩阵
   * @param maxVector 最大约束向量
   * @param objVector 目标函数向量
   * @throws NullPointerException     空类型参数
   * @throws IllegalArgumentException 最大约束向量长度跟约束系数矩阵的行数量无法对应， 或目标函数向量的长度跟约束系数矩阵列数量无法对应
   */
  private Simplex(
      int scale,
      Matrix<BigDecimal> consCoeff,
      BigDecimal[] maxVector,
      BigDecimal[] objVector
  ) {
    if (consCoeff == null || maxVector == null || objVector == null) {
      throw new NullPointerException();
    }

    if (consCoeff.rowCount() != maxVector.length) {
      throw new IllegalArgumentException(
          "The number of rows of the constraint coefficient matrix " + consCoeff.rowCount() +
              " is not equal to the number of the largest constraint vector "
              + maxVector.length);
    }

    if (consCoeff.columnCount() != objVector.length) {
      throw new IllegalArgumentException(
          "The number of columns of the constraint coefficient matrix " + consCoeff
              .columnCount() +
              " is not equal to the number of the object function vector " + objVector.length);
    }

    this.consCoeff = consCoeff;
    this.maxVector = maxVector;
    this.objVector = objVector;
    this.scale = scale;
  }

  /**
   * 使用线性规划标准型创建一个{@code Simplex}。
   *
   * @param scale     小数位数精确度
   * @param consCoeff 约束系数矩阵
   * @param maxVector 最大约束向量
   * @param objVector 目标函数向量
   * @return 线性规划的单纯形法对象
   * @throws NullPointerException     空类型参数
   * @throws UnfeasibleException      线性规划是不可行的
   * @throws UnboundedException       线性规划是无界的
   * @throws IllegalArgumentException 最大约束向量长度跟约束系数矩阵的行数量无法对应， 或目标函数向量的长度跟约束系数矩阵列数量无法对应
   */
  public static Simplex init(
      int scale,
      Matrix<BigDecimal> consCoeff,
      BigDecimal[] maxVector,
      BigDecimal[] objVector
  ) throws UnboundedException, UnfeasibleException {
    Simplex simplex = new Simplex(scale, consCoeff, maxVector, objVector);
    simplex.calc();
    return simplex;
  }

  /**
   * 开始计算线性规划标准型的最大值。
   *
   * @throws UnfeasibleException 线性规划是不可行的
   * @throws UnboundedException  线性规划是无界的
   */
  private void calc() throws UnfeasibleException, UnboundedException {
    Slack slack = initalize(consCoeff, maxVector, objVector);
    // 当前最优的松弛型
    Slack optimalSlack = simplex(slack);
    optimalSolu = new BigDecimal[optimalSlack.nobasics.length];
    for (int i = 0; i < optimalSolu.length; i++) {
      if (optimalSlack.varIndices[i].isBasic) {
        optimalSolu[i] = optimalSlack.maxVector[varIndex(optimalSlack, i)];
      } else {
        optimalSolu[i] = BigDecimal.valueOf(0L);
      }
    }
    this.maxObjVal = optimalSlack.objectVal;
  }

  /**
   * 如果线性规划存在最大值，返回目标函数最大值对应的解，否则返回{@code NULL}。
   *
   * @return 目标函数最大值对应的解
   */
  public BigDecimal[] optimalSolution() {
    return optimalSolu;
  }

  /**
   * 如果线性规划存在最大值，返回目标函数最大值，否则返回{@code NULL}。
   *
   * @return 目标函数最大值
   */
  public BigDecimal maxObjectValue() {
    return maxObjVal;
  }

  /**
   * 使用单纯形法找到初始基本解为最优可行解的松弛型。
   *
   * @param slack 松弛型
   * @return 返回最优松弛型
   * @throws UnboundedException 线性规划是无界的
   */
  private Slack simplex(Slack slack) throws UnboundedException {
    Slack current = slack;
    for (; ; ) {
      int out = -1, outIndex, compareVal; // 需要替出的变量的编号
      for (int i = 0; i < current.objVector.length; i++) {
        // 目标函数系数大于0的
        if (current.objVector[i].compareTo(BigDecimal.ZERO) > 0) {
          out = current.nobasics[i];
          break;
        }
      }

      // 已经找到基本解为最优的松弛型
      if (out < 0) {
        return current;
      }

      outIndex = varIndex(current, out);
      BigDecimal minScaleCoeff = null, tmp;
      int minIndex = 0;
      for (int i = 0; i < current.basics.length; i++) {
        if ((tmp = current.consCoeff.get(i, outIndex)).compareTo(BigDecimal.ZERO) == 0) {
          continue;
        }
        tmp = current.maxVector[i]
            .divide(tmp, scale, BigDecimal.ROUND_HALF_UP);
        if (minScaleCoeff == null || (compareVal = minScaleCoeff.compareTo(tmp)) > 0
            || (compareVal == 0 && current.basics[minIndex] > current.basics[i])// Bland规则，避免退化
        ) {
          minScaleCoeff = tmp;
          minIndex = i;
        }
      }

      // 此线性规划是无界的
      if (minScaleCoeff == null || minScaleCoeff.compareTo(BigDecimal.ZERO) < 0) {
        throw new UnboundedException("The maximum value of the objective function is infinity");
      }

      // 转动
      current = pivot(current, indexVarNo(current, minIndex, true), out);
    }
  }

  /**
   * 如果线性规划是可行的，返回一个初始基本解可行的松弛型，否则抛出{@link UnfeasibleException}异常。
   *
   * @param consCoeff 约束系数矩阵
   * @param maxVector 最大约束向量
   * @param objVector 目标函数向量
   * @return 返回一个初始基本解可行的松弛型
   * @throws UnfeasibleException 线性规划是不可行的
   */
  private Slack initalize(Matrix<BigDecimal> consCoeff, BigDecimal[] maxVector,
      BigDecimal[] objVector)
      throws UnfeasibleException {
    // 最小负数约束值
    int minIndex = 0;
    BigDecimal min = null;
    // 找出最小值
    for (int i = 0; i < maxVector.length; i++) {
      if (min == null || min.compareTo(maxVector[i]) > 0) {
        min = maxVector[i];
        minIndex = i;
      }
    }

    // 最小值大于0，表示基本解是可行的
    if (min == null || min.compareTo(BigDecimal.ZERO) >= 0) {
      Slack slack = new Slack();
      initSlack(slack, consCoeff, maxVector, objVector);
      initBasicAndVarIndex(slack, consCoeff);

      return slack;
    }

    /*
     * 基本初始解不可行，构造特殊线性规划。若此线性规划的最大值小于0，
     * 原线性规划无可行解{@code UnfeasibleException}。
     * */
    Slack aux = auxSlack(consCoeff, maxVector);
    // 使构造后的松弛型初始解可行
    aux = pivot(aux, indexVarNo(aux, minIndex, true), 0);
    try {
      aux = simplex(aux);
    } catch (UnboundedException ignore) {
    }

    // 最优结果小于0，原线性规划无可行解
    if (basicResult(aux).compareTo(BigDecimal.ZERO) < 0) {
      throw new UnfeasibleException("Linear programming has no feasible solution");
    }

    // 如果第0编号的变量为基础变量，需要转动让它成为非基变量
    int zeroIndex = varIndex(aux, 0);
    if (aux.varIndices[0].isBasic) {
      int out = 0;
      for (int i = 0; i < aux.consCoeff.columnCount(); i++) {
        if (!aux.consCoeff.get(zeroIndex, i).equals(BigDecimal.ZERO)) {
          out = indexVarNo(aux, i, false);
          break;
        }
      }

      aux = pivot(aux, 0, out);
    }

    /*移除掉编号为0的变量，形成原始线性规划基本解可行的松弛型 start*/
    aux.consCoeff.removeColumn(zeroIndex); // 调整系数矩阵
    int[] newNoBasics = new int[aux.nobasics.length - 1];
    if (zeroIndex > 0) {
      System.arraycopy(aux.nobasics, 0, newNoBasics, 0, zeroIndex);
    }
    if (zeroIndex < aux.nobasics.length - 1) {
      System.arraycopy(aux.nobasics, zeroIndex + 1, newNoBasics, zeroIndex,
          aux.nobasics.length - 1 - zeroIndex);
    }
    aux.nobasics = newNoBasics; // 新的非基变量
    VarIndex[] newVarIndexs = new VarIndex[aux.varIndices.length - 1];
    System.arraycopy(aux.varIndices, 1, newVarIndexs, 0, newVarIndexs.length);
    aux.varIndices = newVarIndexs; // 新的变量编号和位置映射
    for (int i = 0; i < aux.nobasics.length; i++) {
      aux.nobasics[i]--;
      aux.varIndices[aux.nobasics[i]].index = i;
    }
    for (int i = 0; i < aux.basics.length; i++) {
      aux.basics[i]--;
      aux.varIndices[aux.basics[i]].index = i;
    }

    BigDecimal[] newObjVector = new BigDecimal[objVector.length];
    BigDecimal newObjectVal = BigDecimal.ZERO;
    int currentVarIndex;
    for (int i = 0; i < newObjVector.length; i++) {
      currentVarIndex = varIndex(aux, i); // 当前编号的变量的索引
      if (aux.varIndices[i].isBasic) {
        BigDecimal[] row = aux.consCoeff.row(currentVarIndex);
        for (int j = 0; j < row.length; j++) {
          if (newObjVector[j] == null) {
            newObjVector[j] = row[j].multiply(objVector[i]).negate();
          } else {
            newObjVector[j] = newObjVector[j].add(
                row[j].multiply(objVector[i]).negate()
            );
          }
        }
        newObjectVal = newObjectVal.add(
            objVector[i].multiply(aux.maxVector[currentVarIndex])
        );
      } else {
        newObjVector[currentVarIndex] = objVector[i];
      }
    }

    aux.objVector = newObjVector;
    aux.objectVal = newObjectVal;
    /* 移除掉编号为0的变量，形成原始线性规划基本解可行的松弛型 end*/

    return aux;
  }

  /**
   * 使用一个基本变量作为替入变量替出一个非基变量。
   *
   * @param slack 需要转动的松弛型
   * @param in    替入变量
   * @param out   替出变量
   * @return 转动后的松弛型
   */
  private Slack pivot(Slack slack, int in, int out) {
    Slack newSlack = new Slack();
    BigDecimal[][] fs = new BigDecimal[slack.consCoeff.rowCount()][];
    for (int i = 0; i < fs.length; i++) {
      fs[i] = new BigDecimal[slack.consCoeff.columnCount()];
      Arrays.fill(fs[i], BigDecimal.ZERO);
    }

    initSlack(
        newSlack,
        new ArrayMatrix<>(fs),
        new BigDecimal[slack.maxVector.length],
        new BigDecimal[slack.objVector.length]
    );

    int inIndex, outIndex;
    /*交换索引位置 start*/
    newSlack.varIndices[in] = new VarIndex(false, outIndex = varIndex(slack, out));
    newSlack.varIndices[out] = new VarIndex(true, inIndex = varIndex(slack, in));
    for (int i = 0; i < slack.varIndices.length; i++) {
      if (i == in || i == out) {
        continue;
      }

      newSlack.varIndices[i] = slack.varIndices[i].clone();
    }
    /*交换索引位置 end*/

    /*交换非基变量和基本变量的变量编号映射 start*/
    newSlack.nobasics = Arrays.copyOf(slack.nobasics, slack.nobasics.length);
    newSlack.basics = Arrays.copyOf(slack.basics, slack.basics.length);
    newSlack.basics[inIndex] = out;
    newSlack.nobasics[outIndex] = in;
    /*交换非基变量和基本变量的变量编号映射 end*/

    /*最大约束值修改 start*/
    BigDecimal newMaxIn, outConsCoeff;
    newMaxIn = newSlack.maxVector[inIndex] = slack.maxVector[inIndex]
        .divide(
            outConsCoeff = slack.consCoeff.get(
                inIndex,
                outIndex
            ),
            scale,
            BigDecimal.ROUND_HALF_UP
        );
    for (int i = 0; i < slack.consCoeff.rowCount(); i++) {
      if (i == inIndex) {
        continue;
      }

      newSlack.maxVector[i] = slack.maxVector[i]
          .subtract(
              slack.consCoeff.get(
                  i,
                  outIndex
              ).multiply(newMaxIn)
          );
    }
    /*最大约束值修改 end*/

    /*约束系数修改 start*/
    for (int i = 0; i < slack.consCoeff.rowCount(); i++) {
      for (int j = 0; j < slack.consCoeff.columnCount(); j++) {
        // 非交换行
        if (i != inIndex) {
          if (j == outIndex) {
            newSlack.consCoeff.fill(
                i, j,
                slack.consCoeff.get(i, j).divide(outConsCoeff, scale, BigDecimal.ROUND_HALF_UP)
                    .negate()
            );
          } else {
            newSlack.consCoeff.fill(
                i, j,
                slack.consCoeff.get(i, j).subtract(
                    slack.consCoeff.get(i, outIndex).multiply(
                        slack.consCoeff.get(inIndex, j)
                            .divide(outConsCoeff, scale, BigDecimal.ROUND_HALF_UP)
                    )
                )
            );
          }
        } else {
          if (j == outIndex) {
            newSlack.consCoeff.fill(
                i, j,
                BigDecimal.ONE.divide(outConsCoeff, scale, BigDecimal.ROUND_HALF_UP)
            );
          } else {
            newSlack.consCoeff.fill(
                i, j,
                slack.consCoeff.get(inIndex, j)
                    .divide(outConsCoeff, scale, BigDecimal.ROUND_HALF_UP)
            );
          }
        }
      }
    }
    /*约束系数修改 end*/

    /*目标函数系数修改 start*/
    for (int i = 0; i < slack.objVector.length; i++) {
      if (i == outIndex) {
        newSlack.objVector[i] = slack.objVector[i]
            .divide(outConsCoeff, scale, BigDecimal.ROUND_HALF_UP).negate();
      } else {
        newSlack.objVector[i] = slack.objVector[i].subtract(
            slack.objVector[outIndex].multiply(
                slack.consCoeff.get(inIndex, i)
                    .divide(outConsCoeff, scale, BigDecimal.ROUND_HALF_UP)
            )
        );
      }
    }
    /*目标函数系数修改 end*/

    // 目标值更新
    newSlack.objectVal = slack.objectVal.add(
        slack.objVector[outIndex].multiply(
            slack.maxVector[inIndex].divide(outConsCoeff, scale, BigDecimal.ROUND_HALF_UP)
        )
    );
    return newSlack;
  }

  /**
   * 构建用于判断原线性规划是否有初始可行解的特殊松弛型。
   *
   * @param consCoeff 约束系数矩阵
   * @param maxVector 最大约束向量
   * @return 判断是否有初始可行解的特殊松弛型
   */
  private Slack auxSlack(Matrix<BigDecimal> consCoeff, BigDecimal[] maxVector) {
    BigDecimal[][] fractions = new BigDecimal[consCoeff.rowCount()][];
    for (int i = 0; i < fractions.length; i++) {
      BigDecimal[] row = consCoeff.row(i);
      fractions[i] = new BigDecimal[consCoeff.columnCount() + 1];
      System.arraycopy(row, 0, fractions[i], 1, row.length);
      fractions[i][0] = BigDecimal.valueOf(-1L);
    }

    BigDecimal[] newObjVector = new BigDecimal[consCoeff.columnCount() + 1];
    for (int i = 0; i < newObjVector.length; i++) {
      newObjVector[i] = i == 0 ? BigDecimal.valueOf(-1L) : BigDecimal.ZERO;
    }

    Slack slack = new Slack();
    Matrix<BigDecimal> newConsCoeff;
    initSlack(slack, newConsCoeff = new ArrayMatrix<>(fractions),
        Arrays.copyOf(maxVector, maxVector.length), newObjVector);
    initBasicAndVarIndex(slack, newConsCoeff);
    return slack;
  }

  /**
   * 变量对应的索引位置。
   *
   * @param slack 松弛型
   * @param varNo 变量编号
   * @return 变量对应的索引位置
   */
  private int varIndex(Slack slack, int varNo) {
    return slack.varIndices[varNo].index;
  }

  /**
   * 返回索引上对应的变量编号。
   *
   * @param slack   松弛型
   * @param index   索引
   * @param isBasic 是否是基础变量
   * @return 变量编号
   */
  private int indexVarNo(Slack slack, int index, boolean isBasic) {
    return isBasic ? slack.basics[index] : slack.nobasics[index];
  }

  /**
   * 返回当前松弛型的基本解的目标值
   */
  private BigDecimal basicResult(Slack slack) {
    return slack.objectVal;
  }

  private void initSlack(Slack slack, Matrix<BigDecimal> consCoeff, BigDecimal[] maxVector,
      BigDecimal[] objVector) {
    slack.consCoeff = consCoeff;
    slack.maxVector = maxVector;
    slack.objVector = objVector;
    slack.objectVal = BigDecimal.ZERO;
    slack.varIndices = new VarIndex[consCoeff.rowCount() + consCoeff.columnCount()];
  }

  private void initBasicAndVarIndex(Slack slack, Matrix<BigDecimal> consCoeff) {
    slack.nobasics = new int[consCoeff.columnCount()];
    slack.basics = new int[consCoeff.rowCount()];
    int allVarNum = consCoeff.rowCount() + consCoeff.columnCount();
    for (int i = 0; i < slack.nobasics.length; i++) {
      slack.nobasics[i] = i;
    }
    for (int i = 0; i < slack.basics.length; i++) {
      slack.basics[i] = i + consCoeff.columnCount();
    }
    for (int j = 0; j < allVarNum; j++) {
      slack.varIndices[j] = new VarIndex();
      if (j >= slack.consCoeff.columnCount()) {
        slack.varIndices[j].index = j - consCoeff.columnCount();
        slack.varIndices[j].isBasic = true;
      } else {
        slack.varIndices[j].index = j;
        slack.varIndices[j].isBasic = false;
      }
    }
  }

  /**
   * 描述线性规划的松弛型结构
   */
  private static class Slack {

    /**
     * 非基变量对应的变量编号
     */
    int[] nobasics;

    /**
     * 基本变量对应的变量编号
     */
    int[] basics;

    /**
     * 约束系数
     */
    Matrix<BigDecimal> consCoeff;

    /**
     * 约束值
     */
    BigDecimal[] maxVector;

    /**
     * 目标函数系数
     */
    BigDecimal[] objVector;

    /**
     * 目标函数常数项
     */
    BigDecimal objectVal;

    /**
     * 变量索引记录
     */
    VarIndex[] varIndices;
  }

  /**
   * 记录基础变量和非基础变量的索引位置
   */
  private static class VarIndex implements Cloneable {

    /**
     * 是否是基础变量，如果为false，则是非基础变量
     */
    boolean isBasic;

    /**
     * 对应的索引位置
     */
    int index;

    VarIndex() {
    }

    VarIndex(boolean isBasic, int index) {
      this.isBasic = isBasic;
      this.index = index;
    }

    public VarIndex clone() {
      try {
        return (VarIndex) super.clone();
      } catch (CloneNotSupportedException e) {
        return new VarIndex(isBasic, index);
      }
    }

    @Override
    public String toString() {
      return "VarIndex{" +
          "isBasic=" + isBasic +
          ", index=" + index +
          '}';
    }
  }
}