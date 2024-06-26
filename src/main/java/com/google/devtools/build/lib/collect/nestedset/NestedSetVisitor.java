// Copyright 2014 The Bazel Authors. All rights reserved.
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
package com.google.devtools.build.lib.collect.nestedset;

import com.google.common.base.Preconditions;
import java.util.Collection;

/**
 * NestedSetVisitor facilitates a transitive visitation over a NestedSet. The callback may be called
 * from multiple threads, and must be thread-safe.
 *
 * <p>The visitation is iterative: The caller may invoke a NestedSet within the top-level NestedSet
 * in any order.
 *
 * @param <E> the data type
 */
public final class NestedSetVisitor<E> {

  /**
   * For each element of the NestedSet the {@code Receiver} will receive one element during the
   * visitation.
   */
  public interface Receiver<E> {
    void accept(E arg);
  }

  private final Receiver<E> callback;

  private final VisitedState visited;

  public NestedSetVisitor(Receiver<E> callback, VisitedState visited) {
    this.callback = Preconditions.checkNotNull(callback);
    this.visited = Preconditions.checkNotNull(visited);
  }

  /**
   * Transitively visit a nested set.
   *
   * @param nestedSet the nested set to visit transitively.
   */
  public void visit(NestedSet<E> nestedSet) throws InterruptedException {
    // We can short-circuit empty nested set visitation here, avoiding load on the shared map
    // VisitedState#seenNodes.
    if (!nestedSet.isEmpty()) {
      visitRaw(nestedSet.getChildrenInterruptibly());
    }
  }

  /** Visit every entry in a collection. */
  public void visit(Collection<E> collection) {
    for (E e : collection) {
      if (visited.add(e)) {
        callback.accept(e);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private void visitRaw(Object node) {
    if (visited.add(node)) {
      if (node instanceof Object[]) {
        for (Object child : (Object[]) node) {
          visitRaw(child);
        }
      } else {
        callback.accept((E) node);
      }
    }
  }

  /** Allows {@link NestedSetVisitor} to keep track of the seen nodes and transitive sets. */
  public interface VisitedState {

    /**
     * Adds a node to the visited state, returning true if the node was not yet in the visited state
     * and false if the node was already in the visited state.
     */
    boolean add(Object node);
  }
}
