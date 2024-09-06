/*
 * MIT License
 *
 * Copyright (c) 2019 Andreas Guther
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.aguther.dds.support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class PartitionGroup {

  private HashSet<String> partitionList;
  private List<PartitionGroupEntity> partitionGroupEntityList;

  public PartitionGroup(
    PartitionGroupEntity... entities
  ) {
    // create list for entities
    partitionGroupEntityList = new ArrayList<>();
    // create hash set for partitions
    partitionList = new HashSet<>();

    // add provided entities
    if (entities != null) {
      partitionGroupEntityList.addAll(Arrays.asList(entities));
    }
  }

  public synchronized void addPartitions(
    String... partitions
  ) {
    if (partitionList.addAll(Arrays.asList(partitions))) {
      setPartitions(new ArrayList<>(partitionList));
    }
  }

  public synchronized void removePartitions(
    String... partitions
  ) {
    if (partitionList.removeAll(Arrays.asList(partitions))) {
      setPartitions(new ArrayList<>(partitionList));
    }
  }

  public synchronized void setPartitions(
    String... partitions
  ) {
    // set partitions on entities
    setPartitions(Arrays.asList(partitions));
  }

  public synchronized void setPartitions(
    List<String> partitions
  ) {
    // set partitions on entities
    for (PartitionGroupEntity entity : partitionGroupEntityList) {
      entity.setPartitions(partitions);
    }
  }
}
