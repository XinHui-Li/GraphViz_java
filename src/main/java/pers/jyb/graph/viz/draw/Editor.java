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

package pers.jyb.graph.viz.draw;

import java.util.List;
import java.util.function.Consumer;
import pers.jyb.graph.util.Asserts;
import pers.jyb.graph.viz.api.attributes.Color;
import pers.jyb.graph.viz.api.attributes.Color.ColorItem;
import pers.jyb.graph.viz.api.attributes.Color.FusionColor;
import pers.jyb.graph.viz.api.attributes.Color.MultiColor;

public interface Editor<T, B extends Brush> {

	boolean edit(T attachment, B brush);

	default void fusionColorProcess(FusionColor fusionColor, Consumer<Color> singleColorAction,
	                                Consumer<List<ColorItem>> colorListAction) {
		fusionColorProcess(fusionColor, Color.BLACK, singleColorAction, colorListAction);
	}

	default void fusionColorProcess(FusionColor fusionColor, Color defaultColor,
	                                Consumer<Color> singleColorAction,
	                                Consumer<List<ColorItem>> colorListAction) {
		Asserts.nullArgument(defaultColor, "defaultColor");
		fusionColorProcess(fusionColor, () -> singleColorAction.accept(defaultColor),
                       singleColorAction, colorListAction);
	}

	default void fusionColorProcess(FusionColor fusionColor, Runnable nullAction,
	                                Consumer<Color> singleColorAction,
	                                Consumer<List<ColorItem>> colorListAction) {
		if (fusionColor == null) {
			if (nullAction != null) {
				nullAction.run();
			}
			return;
		}

		Color color = fusionColor.getColor();
		if (color != null) {
			if (singleColorAction != null) {
				singleColorAction.accept(color);
			}
			return;
		}

		MultiColor colorList = fusionColor.getColorList();
		if (colorListAction != null) {
			colorListAction.accept(colorList.getColorItems());
		}
	}
}
