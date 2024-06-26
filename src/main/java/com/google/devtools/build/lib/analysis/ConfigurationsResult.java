// Copyright 2020 The Bazel Authors. All rights reserved.
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
package com.google.devtools.build.lib.analysis;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.devtools.build.lib.analysis.config.BuildConfigurationValue;
import com.google.devtools.build.lib.analysis.config.BuildOptions;
import com.google.devtools.build.lib.events.ExtendedEventHandler;

/**
 * The result of {@link #getConfigurations(ExtendedEventHandler, BuildOptions, Iterable)} which also
 * registers if an error was recorded.
 */
public class ConfigurationsResult {
  private final ListMultimap<BaseDependencySpecification, BuildConfigurationValue> configurations;
  private final boolean hasError;

  private ConfigurationsResult(
      ListMultimap<BaseDependencySpecification, BuildConfigurationValue> configurations,
      boolean hasError) {
    this.configurations = configurations;
    this.hasError = hasError;
  }

  public boolean hasError() {
    return hasError;
  }

  public ListMultimap<BaseDependencySpecification, BuildConfigurationValue> getConfigurationMap() {
    return configurations;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  /** Builder for {@link ConfigurationsResult} */
  public static class Builder {
    private final ListMultimap<BaseDependencySpecification, BuildConfigurationValue>
        configurations = ArrayListMultimap.create();
    private boolean hasError = false;

    public void put(BaseDependencySpecification key, BuildConfigurationValue value) {
      configurations.put(key, value);
    }

    public void setHasError() {
      this.hasError = true;
    }

    public ConfigurationsResult build() {
      return new ConfigurationsResult(configurations, hasError);
    }
  }
}
