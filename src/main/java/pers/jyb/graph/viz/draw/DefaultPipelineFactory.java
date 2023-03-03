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

public class DefaultPipelineFactory implements PipelineFactory {

  @Override
  public <B extends Brush, T extends NodeEditor<B>> NodeExecutePipeline<B, T> nodeExecutePipeline(
      List<T> editors, DrawGraph graphviz) {
    return new NodeExecutePipeline<>(editors, graphviz);
  }

  @Override
  public <B extends Brush, T extends LineEditor<B>> LineExecutePipeline<B, T> lineExecutePipeline(
      List<T> editors, DrawGraph graphviz) {
    return new LineExecutePipeline<>(editors, graphviz);
  }

  @Override
  public <B extends Brush, T extends ClusterEditor<B>> ClusterExecutePipeline<B, T> clusterExecutePipeline(
      List<T> editors, DrawGraph graphviz) {
    return new ClusterExecutePipeline<>(editors, graphviz);
  }

  @Override
  public <B extends Brush, T extends GraphEditor<B>> GraphExecutePipeline<B, T> graphExecutePipeline(
      List<T> editors, DrawGraph graphviz) {
    return new GraphExecutePipeline<>(editors, graphviz);
  }
}
