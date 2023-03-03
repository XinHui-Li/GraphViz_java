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

package pers.jyb.graph.def;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * 二维数组矩阵。
 *
 * @param <T> 矩阵存储元素类型
 * @author jiangyb
 */
public class ArrayMatrix<T> implements
    Matrix.SfMatrix<T, Consumer<? super T>, ArrayMatrix<T>>, Cloneable, Serializable {

  private static final long serialVersionUID = -3631631496856398254L;

  /**
   * 矩阵存储值
   */
  private transient Object[][] matrix;

  /**
   * 行数量
   */
  private int rowNum;

  /**
   * 列数量
   */
  private int colNum;

  /**
   * 默认实例
   */
  public ArrayMatrix() {
    matrix = new Object[0][];
  }

  /**
   * 根据一个容量初始化
   *
   * @param cap 容量
   * @throws IllegalArgumentException 容量小于0
   */
  public ArrayMatrix(int cap) {
    if (cap < 0) {
      throw new IllegalArgumentException("Capacity must >= 0");
    }
    matrix = new Object[cap][];
  }

  /**
   * 根据二维数组初始化矩阵
   *
   * @param array 矩阵
   * @throws NullPointerException     空二维数组或空行或者空内容
   * @throws IllegalArgumentException 不合法的二维数组
   */
  public ArrayMatrix(T[][] array) {
    this.colNum = checkArray(array);
    this.rowNum = array.length;
    matrix = array;
  }

  /**
   * 返回行数量
   *
   * @return 行数量
   */
  @Override
  public int rowCount() {
    return rowNum;
  }

  /**
   * 返回列数量
   *
   * @return 列数量
   */
  @Override
  public int columnCount() {
    return colNum;
  }

  /**
   * 返回矩阵某一行
   *
   * @param index 行索引
   * @return 矩阵行
   * @throws IndexOutOfBoundsException 非法的索引范围
   */
  @Override
  @SuppressWarnings("unchecked")
  public T[] row(int index) {
    checkIsEmpty();
    checkIndex(index, rowNum);
    Object[] row = matrix[index];
    return (T[]) Arrays.copyOf(row, colNum, row.getClass());
  }

  /**
   * 返回矩阵某一列
   *
   * @param index 列索引
   * @return 矩阵列
   * @throws IndexOutOfBoundsException 非法的索引范围
   */
  @Override
  @SuppressWarnings("unchecked")
  public T[] column(int index) {
    checkIsEmpty();
    checkIndex(index, colNum);
    T[] column = null;
    for (int i = 0; i < rowNum; i++) {
      Object[] row = matrix[i];
      if (column == null) {
        column = (T[]) Array.newInstance(row[0].getClass(), rowNum);
      }
      column[i] = (T) row[index];
    }
    return column;
  }

  /**
   * 在矩阵的末尾插入行
   *
   * @param row 行
   * @return 索引值
   * @throws NullPointerException     空行或者空内容
   * @throws IllegalArgumentException 行元素的数量不正确
   */
  @Override
  @SafeVarargs
  public final int addRow(T... row) {
    checkVal(row);
    if (row.length == 0) {
      throw new IllegalArgumentException("Row is empty");
    }
    if (colNum != 0) {
      if (row.length != colNum) {
        throw new IllegalArgumentException("Row's length must be equals to " + colNum);
      }
    } else {
      colNum = row.length;
    }
    if (rowNum >= matrix.length) {
      resize();
    }
    matrix[rowNum++] = row;
    return rowNum;
  }

  /**
   * 在矩阵的末尾插入列
   *
   * @param column 列
   * @return 索引值
   * @throws NullPointerException     空列或者空内容
   * @throws IllegalArgumentException 列元素的数量不正确
   */
  @Override
  @SafeVarargs
  public final int addColumn(T... column) {
    checkVal(column);
    if (column.length == 0) {
      throw new IllegalArgumentException("Column is empty");
    }
    if (rowNum != 0) {
      if (column.length != rowNum) {
        throw new IllegalArgumentException("Column's length must be equals to " + rowNum);
      }
    } else {
      rowNum = column.length;
    }
    if (rowNum > matrix.length) {
      resize();
    }
    for (int i = 0; i < rowNum; i++) {
      Object[] row = matrix[i];
      if (row == null) {
        row = new Object[1];
      } else {
        row = Arrays.copyOf(row, colNum + 1);
      }
      matrix[i] = row;
      row[colNum] = column[i];
    }
    colNum++;
    return colNum;
  }

  /**
   * 在指定位置插入行
   *
   * @param index 索引
   * @param row   行
   * @return 是否成功
   * @throws IndexOutOfBoundsException 非法的索引范围
   * @throws NullPointerException      空行或者空内容
   * @throws IllegalArgumentException  行元素的数量不正确
   */
  @Override
  @SafeVarargs
  public final boolean insertRow(int index, T... row) {
    checkVal(row);
    checkIndex(index, rowNum + 1);
    if (row.length == 0) {
      throw new IllegalArgumentException("Row is empty");
    }
    if (colNum != 0) {
      if (row.length != colNum) {
        throw new IllegalArgumentException("Row's length must be equals to " + colNum);
      }
    } else {
      colNum = row.length;
    }
    if (rowNum >= matrix.length) {
      resize();
    }
    System.arraycopy(matrix, index, matrix, index + 1, rowNum - index);
    matrix[index] = row;
    rowNum++;
    return true;
  }

  /**
   * 在指定位置插入列
   *
   * @param index  索引
   * @param column 列
   * @return 是否成功
   * @throws IndexOutOfBoundsException 非法的索引范围
   * @throws NullPointerException      空列空内容
   * @throws IllegalArgumentException  列元素的数量不正确
   */
  @Override
  @SafeVarargs
  public final boolean insertColumn(int index, T... column) {
    checkVal(column);
    checkIndex(index, colNum + 1);
    if (column.length == 0) {
      throw new IllegalArgumentException("Column is empty");
    }
    if (rowNum != 0) {
      if (column.length != rowNum) {
        throw new IllegalArgumentException("Column's length must be equals to " + rowNum);
      }
    } else {
      rowNum = column.length;
    }
    if (rowNum > matrix.length) {
      resize();
    }
    for (int i = 0; i < rowNum; i++) {
      Object[] row = matrix[i];
      if (row == null) {
        row = new Object[1];
      } else {
        row = Arrays.copyOf(row, colNum + 1);
      }
      System.arraycopy(row, index, row, index + 1, colNum - index);
      matrix[i] = row;
      row[index] = column[i];
    }
    colNum++;
    return true;
  }

  /**
   * 移除指定行
   *
   * @param index 索引
   * @return 是否成功
   * @throws IndexOutOfBoundsException 非法的索引范围
   */
  @Override
  public boolean removeRow(int index) {
    checkIsEmpty();
    checkIndex(index, rowNum);
    System.arraycopy(matrix, index + 1, matrix, index, rowNum - index - 1);
    matrix[--rowNum] = null;
    return true;
  }

  /**
   * 移除指定列
   *
   * @param index 索引
   * @return 是否成功
   * @throws IndexOutOfBoundsException 非法的索引范围
   */
  @Override
  public boolean removeColumn(int index) {
    checkIsEmpty();
    checkIndex(index, colNum);
    for (int i = 0; i < rowNum; i++) {
      Object[] row = matrix[i];
      System.arraycopy(row, index + 1, row, index, colNum - index - 1);
      row = Arrays.copyOf(row, colNum - 1);
      matrix[i] = row;
    }
    colNum--;
    return true;
  }

  /**
   * 填充元素
   *
   * @param row    行索引
   * @param column 列索引
   * @param val    填充值
   * @return 是否成功
   * @throws IndexOutOfBoundsException 非法的索引范围、
   * @throws NullPointerException      空值{@code val}
   */
  @Override
  public boolean fill(int row, int column, T val) {
    Objects.requireNonNull(val);
    checkIsEmpty();
    checkIndex(row, rowNum);
    checkIndex(column, colNum);
    matrix[row][column] = val;
    return true;
  }

  /**
   * 从指定的行和列获取元素值
   *
   * @param row    行索引
   * @param column 列索引
   * @return 对应位置的元素
   * @throws IndexOutOfBoundsException 非法的索引范围
   */
  @Override
  @SuppressWarnings("unchecked")
  public T get(int row, int column) {
    checkIsEmpty();
    checkIndex(row, rowNum);
    checkIndex(column, colNum);
    return (T) matrix[row][column];
  }

  /**
   * 交换索引位置的行
   *
   * @param i 行索引
   * @param j 行索引
   * @return 是否成功
   * @throws IndexOutOfBoundsException 非法的索引范围
   */
  @Override
  public boolean exchangeRow(int i, int j) {
    checkIndex(i, rowNum);
    checkIndex(j, rowNum);
    if (i == j) {
      return false;
    }
    Object[] tmp = matrix[i];
    matrix[i] = matrix[j];
    matrix[j] = tmp;
    return true;
  }

  /**
   * 交换索引位置的列
   *
   * @param i 列索引
   * @param j 列索引
   * @return 是否成功
   * @throws IndexOutOfBoundsException 非法的索引范围
   */
  @Override
  public boolean exchangeCol(int i, int j) {
    checkIndex(i, colNum);
    checkIndex(j, colNum);
    if (i == j) {
      return false;
    }
    for (int k = 0; k < rowNum; k++) {
      Object[] row = matrix[k];
      Object tmp = row[i];
      row[i] = row[j];
      row[j] = tmp;
    }
    return true;
  }

  /**
   * 获取子矩阵
   *
   * @param startRow 起始行
   * @param endRow   结束行
   * @param startCol 起始列
   * @param endCol   结束列
   * @return 子矩阵
   * @throws IndexOutOfBoundsException 非法的索引范围
   * @throws IllegalArgumentException  结束比开始索引小
   */
  @Override
  @SuppressWarnings("unchecked")
  public ArrayMatrix<T> child(int startRow, int endRow, int startCol, int endCol) {
    checkIsEmpty();
    checkIndex(startRow, rowNum);
    checkIndex(endRow, rowNum);
    checkIndex(startCol, colNum);
    checkIndex(endCol, colNum);
    if (startRow > endRow) {
      throw new IllegalArgumentException(
          "Start row index " + startRow + " must < end row index " + endRow);
    }
    if (startCol > endCol) {
      throw new IllegalArgumentException(
          "Start column index " + startCol + " must < end column index " + endCol);
    }
    ArrayMatrix<T> child = new ArrayMatrix<>(endRow - startRow + 1);
    int childColNum = endCol - startCol + 1;
    for (int i = startRow; i <= endRow; i++) {
      Object[] row = matrix[i];
      Object[] childRow = (Object[]) Array.newInstance(row[0].getClass(), childColNum);
      System.arraycopy(row, startCol, childRow, 0, childColNum);
      child.addRow((T[]) childRow);
    }
    return child;
  }

  /**
   * 循环每个元素执行某个动作
   *
   * @param action 动作
   * @throws NullPointerException 空动作
   */
  @Override
  public void forEach(Consumer<? super T> action) {
    Objects.requireNonNull(action);
    int rowNum = rowCount();
    int column = columnCount();
    for (int i = 0; i < rowNum; i++) {
      T[] r = row(i);
      for (int j = 0; j < column; j++) {
        action.accept(r[j]);
      }
    }
  }

  @Override
  public int hashCode() {
    int hashCode = 0;
    for (int i = 0; i < rowNum; i++) {
      for (int j = 0; j < colNum; j++) {
        hashCode += matrix[i][j].hashCode();
      }
    }
    return hashCode;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof ArrayMatrix)) {
      return false;
    }
    ArrayMatrix<?> arrayMatrix = (ArrayMatrix<?>) obj;
    for (int i = 0; i < rowNum; i++) {
      for (int j = 0; j < colNum; j++) {
        if (!Objects.equals(matrix[i][j], arrayMatrix.get(i, j))) {
          return false;
        }
      }
    }
    return true;
  }

  @Override
  @SuppressWarnings("unchecked")
  public ArrayMatrix<T> clone() {
    ArrayMatrix<T> arrayMatrix = null;
    try {
      arrayMatrix = (ArrayMatrix<T>) super.clone();
      arrayMatrix.matrix = Arrays.copyOf(matrix, rowNum);
    } catch (CloneNotSupportedException ignored) {
    }
    return arrayMatrix;
  }

  private void writeObject(ObjectOutputStream oos)
      throws IOException {
    oos.defaultWriteObject();
    for (int i = 0; i < rowNum; i++) {
      for (int j = 0; j < colNum; j++) {
        oos.writeObject(matrix[i][j]);
      }
    }
  }

  private void readObject(ObjectInputStream ois)
      throws IOException, ClassNotFoundException {
    ois.defaultReadObject();
    if (rowNum >= 0) {
      matrix = new Object[rowNum][];
      for (int i = 0; i < rowNum; i++) {
        for (int j = 0; j < colNum; j++) {
          if (matrix[i] == null) {
            matrix[i] = new Object[colNum];
          }
          matrix[i][j] = ois.readObject();
        }
      }
    }
  }

  /**
   * 验证矩阵是否合法。长度必须大于0，并且每一行的长度必须一致
   *
   * @param array 二维数组
   * @return 列数量
   * @throws IllegalArgumentException 不合法的二维数组
   * @throws NullPointerException     空二维数组或空行或者空内容
   */
  private int checkArray(T[][] array) {
    Objects.requireNonNull(array);
    if (array.length == 0) {
      throw new IllegalArgumentException("Array is empty");
    }
    int c = -1;
    for (T[] a : array) {
      checkVal(a);
      if (c == -1) {
        c = a.length;
      }
      if (a.length != c) {
        throw new IllegalArgumentException("The number of rows in the matrix must be equal");
      }
    }
    return c;
  }

  /**
   * 检查矩阵是否为空
   *
   * @throws IndexOutOfBoundsException 矩阵为空
   */
  private void checkIsEmpty() {
    if (rowNum == 0 || colNum == 0) {
      throw new IndexOutOfBoundsException("Matrix is empty");
    }
  }

  /**
   * 检查索引是否在合法范围内
   *
   * @param index 索引
   * @param limit 限制
   * @throws IndexOutOfBoundsException 超出限制
   */
  private void checkIndex(int index, int limit) {
    if (index < 0 || index >= limit) {
      throw new IndexOutOfBoundsException("Index must >= 0 and < " + limit);
    }
  }

  /**
   * 矩阵中不能存null。
   *
   * @param val 矩阵中的值
   * @throws NullPointerException 空行或者空内容
   */
  @SafeVarargs
  private final void checkVal(T... val) {
    Objects.requireNonNull(val);
    for (T t : val) {
      if (t == null) {
        throw new NullPointerException("Array has null value");
      }
    }
  }

  /**
   * 重新设置{@code matrix}，已row的右区间最小2的幂扩张
   */
  private void resize() {
    if (matrix.length == 0 && rowNum == 0) {
      matrix = new Object[1][];
      return;
    }
    int cap = (rowNum & (1 << (Integer.SIZE - Integer.numberOfLeadingZeros(rowNum) - 1))) << 1;
    matrix = Arrays.copyOf(matrix, cap);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("Matrix print:").append("\n");
    for (int i = 0; i < rowNum; i++) {
      Object[] row = matrix[i];
      for (int j = 0; j < colNum; j++) {
        String val = Objects.toString(row[j]);
        sb.append(val).append(" ");
      }
      sb.append("\n");
    }
    return sb.toString();
  }
}
