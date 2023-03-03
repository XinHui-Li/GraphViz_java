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

package pers.jyb.graph.viz.draw.svg.cluster;

import org.w3c.dom.Element;
import pers.jyb.graph.viz.draw.ClusterDrawProp;
import pers.jyb.graph.viz.draw.ClusterEditor;
import pers.jyb.graph.viz.draw.svg.SvgBrush;
import pers.jyb.graph.viz.draw.svg.SvgConstants;
import pers.jyb.graph.viz.draw.svg.SvgEditor;

public class ClusterBorderEditor extends SvgEditor<ClusterDrawProp, SvgBrush>
    implements ClusterEditor<SvgBrush> {

  @Override
  public boolean edit(ClusterDrawProp cluster, SvgBrush brush) {
    cluster.checkBox();

    Element clusterEle = brush.getOrCreateChildElementById(
        SvgBrush.getId(cluster.id(), SvgConstants.POLYGON_ELE),
        SvgConstants.POLYGON_ELE
    );

    String points = generateBox(cluster);
    clusterEle.setAttribute(SvgConstants.POINTS, points);

    return true;
  }
}
