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

/**
 * 表示一个接受单个输入参数且不返回结果的操作。与大多数其他功能接口不同，{@code ByteConsumer} 有望通过副作用进行操作。
 *
 * @author jiangyb
 */
@FunctionalInterface
public interface ByteConsumer {

  /**
   * 对给定的参数执行此操作。
   *
   * @param t 输入参数
   */
  void accept(Byte t);

  /**
   * 返回组成的{@code ByteConsumer}，依次执行此操作，然后执行{@code after}操作。 如果执行任何一个操作均引发异常，则将其中继到组合操作的调用方。如果执行此操作引发异常，
   * 则将不会执行{@code after}操作。
   *
   * @param after 该操作后要执行的操作
   * @return 组成的{@code Consumer}依次执行操作，后跟{@code after}操作
   * @throws NullPointerException 如果{@code after}为空
   */
  default ByteConsumer andThen(Consumer<? super Byte> after) {
    Objects.requireNonNull(after);
    return (Byte t) -> {
      accept(t);
      after.accept(t);
    };
  }
}