// Copyright 2023 The Bazel Authors. All rights reserved.
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

/**
 * Indicates a mismatch between configurability of the target and nullness of the configuration.
 *
 * <p>Thrown if a rule is requested with a null configuration or a non-configurable target is
 * requested with a non-null configuration.
 */
// TODO(b/261521010): this should be removed after rule transitions are removed from dependency
// resolution as it will become unreachable.
public final class InconsistentNullConfigException extends Exception {}
