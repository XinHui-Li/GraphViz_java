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

import java.io.Serializable;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * 维护一个{@code V}类型的数值以及{@code E}类型的数组的数据结构。
 *
 * @param <V> 顶点类型
 * @param <E> 边类型
 * @author jiangyb
 */
class Bag<V, E> implements Iterable<E>, Serializable {

  private static final long serialVersionUID = -3433776704595616074L;

  Boolean unmodify;

  V vertex;

  Node<E> header;

  Node<E> tail;

  transient int bModCount;

  Bag(V vertex) {
    this.vertex = vertex;
  }

  Bag(boolean unmodify) {
    this.vertex = null;
    this.unmodify = unmodify;
  }

  @Override
  public Iterator<E> iterator() {
    return new BagIterator();
  }

  @Override
  public void forEach(Consumer<? super E> action) {
    Objects.requireNonNull(action);
    Node<E> current = header;
    while (current != null) {
      action.accept(current.value);
      current = current.next;
    }
  }

  /**
   * 添加元素
   */
  void add(E e) {
    checkIsUnmodify();
    if (header == null) {
      header = new Node<>(e, null, null);
      tail = header;
    } else {
      tail = tail.next = new Node<>(e, tail, null);
    }
    bModCount++;
  }

  /**
   * 移除元素
   *
   * @param obj 移除的元素
   * @return 是否成功
   */
  boolean remove(Object obj) {
    checkIsUnmodify();
    Node<E> pre;
    Node<E> current = header;
    // 查找需要删除的相邻顶点
    while (current != null) {
      if (Objects.equals(current.value, obj)) {
        break;
      }
      current = current.next;
    }
    if (current == null) {
      return false;
    }
    // 迭代完成
    if (current == tail) {
      tail = tail.pre;
      if (tail != null) {
        tail.next = null;
      } else {
        header = null;  // header和tail重叠
      }
    }
    // 删除header
    else if (current == header) {
      header = header.next;
      header.pre = null;
    } else if ((pre = current.pre) != null) {
      pre.next = current.next;
      current.next.pre = pre;
    }
    bModCount++;
    return true;
  }

  /**
   * 移除符合条件的元素
   *
   * @param predicate 判断断言
   * @return 是否成功
   */
  boolean removeIf(Predicate<E> predicate) {
    checkIsUnmodify();
    Objects.requireNonNull(predicate);
    Node<E> point = header;
    while (point != null) {
      if (predicate.test(point.value)) {
        if (!remove(point.value)) {
          return false;
        }
      }
      point = point.next;
    }
    return false;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof Bag)) {
      return false;
    }
    if (!Objects.equals(unmodify, ((Bag<?, ?>) obj).unmodify)) {
      return true;
    }
    if (!Objects.equals(this.vertex, ((Bag<?, ?>) obj).vertex)) {
      return false;
    }
    int c1 = 0;
    for (E ignore : this) {
      c1++;
    }
    for (Object ignored : (Bag<?, ?>) obj) {
      c1--;
    }
    if (c1 != 0) {
      return false;
    }
    for (E e : this) {
      Object fe = null;
      for (Object oe : (Bag<?, ?>) obj) {
        if (Objects.equals(e, oe)) {
          fe = oe;
          break;
        }
      }
      if (fe == null) {
        return false;
      }
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hashCode = vertex != null ? vertex.hashCode() : 1;
    Node<E> point = header;
    while (point != null) {
      hashCode += point.value.hashCode();
      point = point.next;
    }
    return hashCode;
  }

  // 判断bag是否可以改动
  void checkIsUnmodify() {
    if (unmodify != null && unmodify) {
      throw new UnmodifiableBagException("Bag cannot be modify");
    }
  }

  protected class BagIterator implements Iterator<E> {

    Node<E> point; // 迭代器指针
    private int exceptModCount = bModCount; // 下一个修改的次数

    BagIterator() {
      this.point = header;
    }

    @Override
    public boolean hasNext() {
      return point != null;
    }

    @Override
    public E next() {
      checkIsConcurrentModify();
      if (point == null) {
        throw new NoSuchElementException();
      }
      E value = point.value;
      point = point.next;
      return value;
    }

    @Override
    public void remove() {
      checkIsUnmodify();
      checkIsConcurrentModify();
      Node<E> pre;
      Node<E> prepre;
      if (point == header) {
        throw new IllegalStateException("Iterator not specified");
      }
      // 迭代完成
      if (point == null) {
        if (tail != null) {
          tail = tail.pre;
          if (tail != null) {
            tail.next = null;
          } else {
            header = null;  // header和tail重叠
          }
        }
      }
      // 删除header
      else if ((pre = point.pre) == header) {
        header = header.next;
        header.pre = null;
      } else if (pre != null && (prepre = pre.pre) != null) {
        prepre.next = point;
        point.pre = prepre;
      }
      exceptModCount++;
      bModCount++;
    }

    private void checkIsConcurrentModify() {
      if (exceptModCount != bModCount) {
        throw new ConcurrentModificationException();
      }
    }
  }

  /**
   * 一个用于检索顶点所有相邻顶点的双向链表
   */
  private static final class Node<E> implements Serializable {

    private static final long serialVersionUID = 6069018214912306217L;

    E value;

    Node<E> pre;

    Node<E> next;

    Node(E value) {
      this.value = value;
    }

    Node(E value, Node<E> pre, Node<E> next) {
      this(value);
      this.pre = pre;
      this.next = next;
    }
  }
}
