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

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

/**
 * 二维矩阵。
 *
 * @param <T> 矩阵存储元素类型
 * @author jiangyb
 */
public interface Matrix<T> {

  /**
   * 返回行数量
   *
   * @return 行数量
   */
  int rowCount();

  /**
   * 返回列数量
   *
   * @return 列数量
   */
  int columnCount();

  /**
   * 返回矩阵某一行
   *
   * @param index 行索引
   * @return 矩阵行
   * @throws IndexOutOfBoundsException 非法的索引范围
   */
  T[] row(int index);

  /**
   * 返回矩阵某一列
   *
   * @param index 列索引
   * @return 矩阵列
   * @throws IndexOutOfBoundsException 非法的索引范围
   */
  T[] column(int index);

  /**
   * 在矩阵的末尾插入行
   *
   * @param row 行
   * @return 索引值
   * @throws NullPointerException     空行
   * @throws IllegalArgumentException 行元素的数量不正确
   */
  @SuppressWarnings("unchecked")
  int addRow(T... row);

  /**
   * 在矩阵的末尾插入列
   *
   * @param column 列
   * @return 索引值
   * @throws NullPointerException     空列
   * @throws IllegalArgumentException 列元素的数量不正确
   */
  @SuppressWarnings("unchecked")
  int addColumn(T... column);

  /**
   * 在指定位置插入行
   *
   * @param index 索引
   * @param row   行
   * @return 是否成功
   * @throws IndexOutOfBoundsException 非法的索引范围
   * @throws NullPointerException      空行
   * @throws IllegalArgumentException  行元素的数量不正确
   */
  @SuppressWarnings("unchecked")
  boolean insertRow(int index, T... row);

  /**
   * 在指定位置插入列
   *
   * @param index  索引
   * @param column 列
   * @return 是否成功
   * @throws IndexOutOfBoundsException 非法的索引范围
   * @throws NullPointerException      空列
   * @throws IllegalArgumentException  列元素的数量不正确
   */
  @SuppressWarnings("unchecked")
  boolean insertColumn(int index, T... column);

  /**
   * 移除指定行
   *
   * @param index 索引
   * @return 是否成功
   * @throws IndexOutOfBoundsException 非法的索引范围
   */
  boolean removeRow(int index);

  /**
   * 移除指定列
   *
   * @param index 索引
   * @return 是否成功
   * @throws IndexOutOfBoundsException 非法的索引范围
   */
  boolean removeColumn(int index);

  /**
   * 填充元素
   *
   * @param row    行索引
   * @param column 列索引
   * @param val    填充值
   * @return 是否成功
   * @throws IndexOutOfBoundsException 非法的索引范围
   */
  boolean fill(int row, int column, T val);

  /**
   * 从指定的行和列获取元素值
   *
   * @param row    行索引
   * @param column 列索引
   * @return 对应位置的元素
   * @throws IndexOutOfBoundsException 非法的索引范围
   */
  T get(int row, int column);

  /**
   * 交换索引位置的行
   *
   * @param i 行索引
   * @param j 行索引
   * @return 是否成功
   * @throws IndexOutOfBoundsException 非法的索引范围
   */
  boolean exchangeRow(int i, int j);

  /**
   * 交换索引位置的列
   *
   * @param i 列索引
   * @param j 列索引
   * @return 是否成功
   * @throws IndexOutOfBoundsException 非法的索引范围
   */
  boolean exchangeCol(int i, int j);

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
  Matrix<T> child(int startRow, int endRow, int startCol, int endCol);

  /**
   * 循环每个元素执行某个动作
   *
   * @param action 动作
   * @throws NullPointerException 空动作
   */
  default void forEach(Consumer<? super T> action) {
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

  /**
   * 子矩阵指定为自类型{@code T_SELF}，并且重载了{@code forEach}方法
   *
   * @param <T>      元素类型
   * @param <T_CONS> 动作类型
   * @param <T_SELF> 自类型
   */
  interface SfMatrix<T, T_CONS, T_SELF extends SfMatrix<T, T_CONS, T_SELF>> extends Matrix<T> {

    @Override
    T_SELF child(int startRow, int endRow, int startCol, int endCol);

    @SuppressWarnings("overloads")
    void forEach(T_CONS action);
  }

  /**
   * 矩阵元素为{@code Byte}类型，子矩阵指定为自类型{@code ByteSfMatrix}， 重载{@code forEach}的参数为{@code ByteConsumer}
   */
  interface ByteSfMatrix extends SfMatrix<Byte, ByteConsumer, ByteSfMatrix> {

    @Override
    ByteSfMatrix child(int startRow, int endRow, int startCol, int endCol);

    @Override
    default void forEach(ByteConsumer action) {
      Consumer<Byte> consumer = action::accept;
      forEach(consumer);
    }
  }

  /**
   * 矩阵元素为{@code Integer}类型，子矩阵指定为自类型{@code IntSfMatrix}， 重载{@code forEach}的参数为{@code IntConsumer}
   */
  interface IntSfMatrix extends SfMatrix<Integer, IntConsumer, IntSfMatrix> {

    @Override
    IntSfMatrix child(int startRow, int endRow, int startCol, int endCol);

    @Override
    default void forEach(IntConsumer action) {
      Consumer<Integer> consumer = action::accept;
      forEach(consumer);
    }
  }
}