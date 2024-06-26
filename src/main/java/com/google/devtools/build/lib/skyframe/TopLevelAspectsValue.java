// Copyright 2022 The Bazel Authors. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.devtools.build.lib.skyframe;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import com.google.devtools.build.lib.actions.ActionAnalysisMetadata;
import com.google.devtools.build.lib.actions.ActionLookupValue;
import com.google.devtools.build.lib.analysis.AspectValue;

/**
 * SkyValue for {@code TopLevelAspectsKey} wraps a list of the {@code AspectValue} of the top level
 * aspects applied on the same top level target.
 */
public final class TopLevelAspectsValue implements ActionLookupValue {
  private final ImmutableList<AspectValue> topLevelAspectsValues;

  public TopLevelAspectsValue(ImmutableList<AspectValue> topLevelAspectsValues) {
    this.topLevelAspectsValues = checkNotNull(topLevelAspectsValues);
  }

  public ImmutableList<AspectValue> getTopLevelAspectsValues() {
    return topLevelAspectsValues;
  }

  @Override
  public ImmutableList<ActionAnalysisMetadata> getActions() {
    return ImmutableList.of();
  }
}
