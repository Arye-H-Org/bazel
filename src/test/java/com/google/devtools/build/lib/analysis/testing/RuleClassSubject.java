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
package com.google.devtools.build.lib.analysis.testing;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.truth.Truth.assertAbout;

import com.google.common.base.Functions;
import com.google.common.collect.ImmutableMap;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.MapSubject;
import com.google.common.truth.Subject;
import com.google.devtools.build.lib.analysis.config.ToolchainTypeRequirement;
import com.google.devtools.build.lib.cmdline.Label;
import com.google.devtools.build.lib.packages.RuleClass;
import java.util.Map;

/** A Truth {@link Subject} for {@link RuleClass}. */
public class RuleClassSubject extends Subject {
  // Static data.

  /** Entry point for test assertions related to {@link RuleClass}. */
  public static RuleClassSubject assertThat(RuleClass ruleClass) {
    return assertAbout(RuleClassSubject::new).that(ruleClass);
  }

  /** Static method for getting the subject factory (for use with assertAbout()). */
  public static Subject.Factory<RuleClassSubject, RuleClass> ruleClasses() {
    return RuleClassSubject::new;
  }

  // Instance fields.

  private final RuleClass actual;
  private final Map<Label, ToolchainTypeRequirement> toolchainTypesMap;

  protected RuleClassSubject(FailureMetadata failureMetadata, RuleClass subject) {
    super(failureMetadata, subject);
    this.actual = subject;
    this.toolchainTypesMap = makeToolchainTypesMap(subject);
  }

  private static ImmutableMap<Label, ToolchainTypeRequirement> makeToolchainTypesMap(
      RuleClass subject) {
    return subject.getToolchainTypes().stream()
        .collect(toImmutableMap(ToolchainTypeRequirement::toolchainType, Functions.identity()));
  }

  public MapSubject toolchainTypes() {
    return check("toolchainTypes()").that(toolchainTypesMap);
  }

  public ToolchainTypeRequirementSubject toolchainType(String toolchainTypeLabel) {
    return toolchainType(Label.parseCanonicalUnchecked(toolchainTypeLabel));
  }

  public ToolchainTypeRequirementSubject toolchainType(Label toolchainType) {
    return check("toolchainType(%s)", toolchainType)
        .about(ToolchainTypeRequirementSubject.toolchainTypeRequirements())
        .that(toolchainTypesMap.get(toolchainType));
  }

  public void hasToolchainType(String toolchainTypeLabel) {
    toolchainType(toolchainTypeLabel).isNotNull();
  }

  public void hasToolchainType(Label toolchainType) {
    toolchainType(toolchainType).isNotNull();
  }

  // TODO(blaze-team): Add more useful methods.
}
