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
import java.util.function.Function;
import java.util.function.Predicate;

public final class BiConcatIterable<T> extends ConcatIterable<T, T> {

	@SafeVarargs
	public BiConcatIterable(Iterable<? extends T>... iterables) {
		super(Function.identity(), iterables);
	}

	@SafeVarargs
	public BiConcatIterable(Predicate<T> filter, Iterable<? extends T>... iterables) {
		super(filter, Function.identity(), iterables);
	}

	public BiConcatIterable(Collection<? extends Iterable<? extends T>> iterables) {
		super(iterables, Function.identity());
	}

	public BiConcatIterable(Collection<? extends Iterable<? extends T>> iterables,
	                        Predicate<T> filter) {
		super(iterables, filter, Function.identity());
	}
}
