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

import com.google.devtools.build.lib.actions.ActionLookupKeyOrProxy;
import com.google.devtools.build.lib.analysis.TopLevelArtifactContext;
import com.google.devtools.build.skyframe.SkyKey;

/** The common interface for keys that wraps a top level action lookup keys. */
public interface TopLevelActionLookupKeyWrapper extends SkyKey {
  ActionLookupKeyOrProxy actionLookupKey();

  TopLevelArtifactContext topLevelArtifactContext();
}
