// Copyright 2017 The Bazel Authors. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org /licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.devtools.build.lib.rules.objc;

import com.google.devtools.build.lib.actions.Artifact;
import com.google.devtools.build.lib.collect.nestedset.Depset;
import com.google.devtools.build.lib.collect.nestedset.NestedSet;
import com.google.devtools.build.lib.concurrent.ThreadSafety.Immutable;
import com.google.devtools.build.lib.packages.BuiltinProvider;
import com.google.devtools.build.lib.packages.NativeInfo;
import com.google.devtools.build.lib.rules.cpp.CcInfo;
import com.google.devtools.build.lib.starlarkbuildapi.objc.AppleDynamicFrameworkInfoApi;
import javax.annotation.Nullable;

/**
 * Provider containing information about an Apple dynamic framework. This provider contains:
 *
 * <ul>
 *   <li>'framework_dirs': The framework path names used as link inputs in order to link against the
 *       dynamic framework
 *   <li>'framework_files': The full set of artifacts that should be included as inputs to link
 *       against the dynamic framework
 *   <li>'binary': The dylib binary artifact of the dynamic framework
 *   <li>'cc_info': A {@link CcInfo} which contains information about the transitive dependencies
 *       linked into the binary.
 *   <li>'objc': An {@link ObjcProvider} which contains information about the transitive
 *       dependencies linked into the binary, (intended so that bundle loaders depending on this
 *       executable may avoid relinking symbols included in the loadable binary
 * </ul>
 */
@Immutable
public final class AppleDynamicFrameworkInfo extends NativeInfo
    implements AppleDynamicFrameworkInfoApi<Artifact> {

  /** Starlark name for the AppleDynamicFrameworkInfo. */
  public static final String STARLARK_NAME = "AppleDynamicFramework";

  /** Starlark constructor and identifier for AppleDynamicFrameworkInfo. */
  public static final BuiltinProvider<AppleDynamicFrameworkInfo> STARLARK_CONSTRUCTOR =
      new BuiltinProvider<AppleDynamicFrameworkInfo>(
          STARLARK_NAME, AppleDynamicFrameworkInfo.class) {};

  private final NestedSet<String> dynamicFrameworkDirs;
  private final NestedSet<Artifact> dynamicFrameworkFiles;
  @Nullable private final Artifact dylibBinary;
  private final CcInfo depsCcInfo;
  private final ObjcProvider depsObjcProvider;

  public AppleDynamicFrameworkInfo(
      @Nullable Artifact dylibBinary,
      CcInfo depsCcInfo,
      ObjcProvider depsObjcProvider,
      NestedSet<String> dynamicFrameworkDirs,
      NestedSet<Artifact> dynamicFrameworkFiles) {
    this.dylibBinary = dylibBinary;
    this.depsCcInfo = depsCcInfo;
    this.depsObjcProvider = depsObjcProvider;
    this.dynamicFrameworkDirs = dynamicFrameworkDirs;
    this.dynamicFrameworkFiles = dynamicFrameworkFiles;
  }

  @Override
  public BuiltinProvider<AppleDynamicFrameworkInfo> getProvider() {
    return STARLARK_CONSTRUCTOR;
  }

  @Override
  public Depset /*<String>*/ getDynamicFrameworkDirs() {
    return Depset.of(String.class, dynamicFrameworkDirs);
  }

  @Override
  public Depset /*<Artifact>*/ getDynamicFrameworkFiles() {
    return Depset.of(Artifact.class, dynamicFrameworkFiles);
  }

  @Override
  public Artifact getAppleDylibBinary() {
    return dylibBinary;
  }

  @Override
  public CcInfo getDepsCcInfo() {
    return depsCcInfo;
  }

  @Override
  public ObjcProvider getDepsObjcProvider() {
    return depsObjcProvider;
  }
}
