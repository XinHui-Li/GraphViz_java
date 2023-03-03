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

package pers.jyb.graph.util;

public class ValueUtils {

	private ValueUtils() {
	}

	public static boolean approximate(double source, double target, double tolerance) {
		return Math.abs(target - source) <= tolerance;
	}

	public static double tan(double angle) {
		return Math.tan(toRadians(angle));
	}

	public static double cos(double angle) {
		return Math.cos(toRadians(angle));
	}

	public static double sin(double angle) {
		return Math.sin(toRadians(angle));
	}

	private static double toRadians(double angle) {
		return Math.toRadians(angle);
	}
}
