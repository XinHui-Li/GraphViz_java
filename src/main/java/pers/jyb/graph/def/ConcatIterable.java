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

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import pers.jyb.graph.util.Asserts;
import pers.jyb.graph.util.CollectionUtils;

/**
 * An Iterable object that can assemble the remaining Iterable objects without requiring additional
 * element space.
 *
 * @param <P> primitive element type
 * @param <T> element type
 * @author jiangyb
 */
public class ConcatIterable<P, T> implements Iterable<T> {

  private final Collection<? extends Iterable<? extends P>> iterables;

  private final Predicate<T> filter;

  private final Function<P, T> transform;

  @SafeVarargs
  public ConcatIterable(Function<P, T> transform, Iterable<? extends P>... iterables) {
    this(Stream.of(iterables).filter(Objects::nonNull).collect(Collectors.toList()), transform);
  }

  @SafeVarargs
  public ConcatIterable(Predicate<T> filter, Function<P, T> transform,
                        Iterable<? extends P>... iterables) {
    this(Stream.of(iterables).filter(Objects::nonNull).collect(Collectors.toList()), filter,
        transform);
  }

  public ConcatIterable(Collection<? extends Iterable<? extends P>> iterables,
                        Function<P, T> transform) {
    this(iterables, null, transform);
  }

  public ConcatIterable(Collection<? extends Iterable<? extends P>> iterables,
                        Predicate<T> filter, Function<P, T> transform) {
    Asserts.illegalArgument(CollectionUtils.isEmpty(iterables), "iterables is empty!");
    Asserts.nullArgument(transform, "transform");
    this.iterables = iterables;
    this.filter = filter;
    this.transform = transform;
  }

  @Override
  public Iterator<T> iterator() {
    Iterator<? extends Iterator<? extends P>> iterator = iterables.stream()
        .map(Iterable::iterator)
        .filter(Iterator::hasNext)
        .collect(Collectors.toList())
        .iterator();

    if (!iterator.hasNext()) {
      return Collections.emptyIterator();
    }
    if (filter == null) {
      return new ConcatIterator<>(iterator, transform);
    } else {
      return new FilterContactIterator<>(iterator, transform, filter);
    }
  }

  private static class ConcatIterator<P, T> implements Iterator<T> {

    private final Iterator<? extends Iterator<? extends P>> iterators;

    private Iterator<? extends P> curItr;

    private final Function<P, T> transform;

    private ConcatIterator(Iterator<? extends Iterator<? extends P>> iterators,
                           Function<P, T> transform) {
      this.iterators = iterators;
      this.transform = transform;
    }

    @Override
    public boolean hasNext() {
      if (iterators.hasNext()) {
        return true;
      }
      if (curItr == null) {
        return false;
      }

      return curItr.hasNext();
    }

    @Override
    public T next() {
      if (curItr == null) {
        curItr = iterators.next();
      }

      if (curItr.hasNext()) {
        return transform.apply(curItr.next());
      }

      if (!iterators.hasNext()) {
        throw new NoSuchElementException();
      }

      curItr = iterators.next();
      return transform.apply(curItr.next());
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("Concat Iterator not support delete!");
    }
  }

  public static final class FilterContactIterator<P, T> extends ConcatIterator<P, T> {

    private T current;

    private boolean currentIsConsumer = true;

    private final Predicate<T> filter;

    private FilterContactIterator(Iterator<? extends Iterator<? extends P>> iterators,
                                  Function<P, T> transform, Predicate<T> filter) {
      super(iterators, transform);
      Asserts.nullArgument(filter, "filter");
      this.filter = filter;
    }

    @Override
    public boolean hasNext() {
      if (!super.hasNext()) {
        if (current == null) {
          return false;
        }

        return !currentIsConsumer;
      }

      if (!currentIsConsumer) {
        return true;
      }

      do {
        current = safeNext();
      } while (!filter.test(current) && super.hasNext());

      if (!super.hasNext() && !filter.test(current)) {
        return false;
      }

      currentIsConsumer = false;
      return true;
    }

    @Override
    public T next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }

      currentIsConsumer = true;
      return current;
    }

    private T safeNext() {
      if (super.hasNext()) {
        return super.next();
      }

      return null;
    }
  }
}