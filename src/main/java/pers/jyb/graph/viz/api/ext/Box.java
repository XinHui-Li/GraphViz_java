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

package pers.jyb.graph.viz.api.ext;

import pers.jyb.graph.def.FlatPoint;
import pers.jyb.graph.util.Asserts;

public interface Box {

	String HORIZONTAL_ERROR = "Box's right wall must be greater than left wall";

	String VERTICAL_ERROR = "Box's down wall must be greater than up wall";

	double getLeftBorder();

	double getRightBorder();

	double getUpBorder();

	double getDownBorder();

	default double getHeight() {
		return Math.abs(getUpBorder() - getDownBorder());
	}

	default double getWidth() {
		return Math.abs(getLeftBorder() - getRightBorder());
	}

	default double getX() {
		return (getLeftBorder() + getRightBorder()) / 2;
	}

	default double getY() {
		return (getUpBorder() + getDownBorder()) / 2;
	}

	default FlatPoint getLeftUp() {
		return new FlatPoint(getLeftBorder(), getUpBorder());
	}

	default FlatPoint getRightDown() {
		return new FlatPoint(getRightBorder(), getDownBorder());
	}

	default void check() {
		Asserts.illegalArgument(getLeftBorder() > getRightBorder(), HORIZONTAL_ERROR);
		Asserts.illegalArgument(getUpBorder() > getDownBorder(), VERTICAL_ERROR);
	}
}
