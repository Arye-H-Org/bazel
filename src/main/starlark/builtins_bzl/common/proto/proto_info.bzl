# Copyright 2022 The Bazel Authors. All rights reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

"""
Definition of ProtoInfo provider.
"""

_warning = """ Don't use this field. It's intended for internal use and will be changed or removed
    without warning."""

def _uniq(iterable):
    unique_elements = {element: None for element in iterable}
    return list(unique_elements.keys())

def _join(*path):
    return "/".join([p for p in path if p != ""])

def _empty_to_dot(path):
    return path if path else "."

def _from_root(root, repo, relpath):
    """Constructs an exec path from root to relpath"""
    if not root:
        # `relpath` is a directory with an input source file, the exec path is one of:
        # - when in main repo: `package/path`
        # - when in a external repository: `external/repo/package/path`
        #   - with sibling layout: `../repo/package/path`
        return _join(repo, relpath)
    else:
        # `relpath` is a directory with a generated file or an output directory:
        # - when in main repo: `{root}/package/path`
        # - when in an external repository: `{root}/external/repo/package/path`
        #   - with sibling layout: `{root}/package/path`
        return _join(root, "" if repo.startswith("../") else repo, relpath)

def _create_proto_info(*, srcs, deps, descriptor_set, proto_path = "", workspace_root = "", genfiles_dir = None):
    """Constructs ProtoInfo.

    Args:
      srcs: ([File]) List of .proto files (possibly under _virtual path)
      deps: ([ProtoInfo]) List of dependencies
      descriptor_set: (File) Descriptor set for this Proto
      proto_path: (str) Path that should be stripped from files in srcs.
      workspace_root: (str) Set to ctx.workspace_root if this is not the main repository.
      genfiles_dir: (str) Set to ctx.genfiles_dir if _virtual_imports are used.

    Returns:
      (ProtoInfo)
    """

    # Validate parameters
    src_prefix = _join(workspace_root.replace("external/", "../"), proto_path)
    for src in srcs:
        if type(src) != "File":
            fail("srcs parameter expects a list of Files")
        if src.owner.workspace_root != workspace_root:
            fail("srcs parameter expects all files to have the same workspace_root: ", workspace_root)
        if not src.short_path.startswith(src_prefix):
            fail("srcs parameter expects all files start with %s" % src_prefix)
    if type(descriptor_set) != "File":
        fail("descriptor_set parameter expected to be a File")
    if "_virtual_imports/" in proto_path and not genfiles_dir:
        fail("genfiles_dir parameter should be set when _virtual_imports are used")

    direct_proto_sources = [_ProtoSourceInfo(_source_file = src, _proto_path = proto_path) for src in srcs]
    transitive_proto_sources = depset(
        direct = direct_proto_sources,
        transitive = [dep._transitive_proto_sources for dep in deps],
        order = "preorder",
    )
    transitive_sources = depset(
        direct = srcs,
        transitive = [dep.transitive_sources for dep in deps],
        order = "preorder",
    )

    # There can be up more than 1 direct proto_paths, for example when there's
    # a generated and non-generated .proto file in srcs
    root_paths = _uniq([src.root.path for src in srcs])
    transitive_proto_path = depset(
        direct = [_empty_to_dot(_from_root(root, workspace_root, proto_path)) for root in root_paths],
        transitive = [dep.transitive_proto_path for dep in deps],
    )

    if srcs:
        check_deps_sources = depset(direct = srcs)
    else:
        check_deps_sources = depset(transitive = [dep.check_deps_sources for dep in deps])

    transitive_descriptor_sets = depset(
        direct = [descriptor_set],
        transitive = [dep.transitive_descriptor_sets for dep in deps],
    )

    # Layering checks.
    if srcs:
        exported_sources = depset(direct = direct_proto_sources)
    else:
        exported_sources = depset(transitive = [dep._exported_sources for dep in deps])

    if "_virtual_imports/" in proto_path:
        #TODO(b/281812523): remove genfiles_dir from proto_source_root (when users assuming it's there are migrated)
        proto_source_root = _empty_to_dot(_from_root(genfiles_dir, workspace_root, proto_path))
    elif workspace_root.startswith("../"):
        proto_source_root = proto_path
    else:
        proto_source_root = _empty_to_dot(_join(workspace_root, proto_path))

    return dict(
        direct_sources = srcs,
        transitive_sources = transitive_sources,
        direct_descriptor_set = descriptor_set,
        transitive_descriptor_sets = transitive_descriptor_sets,
        proto_source_root = proto_source_root,
        transitive_proto_path = transitive_proto_path,
        check_deps_sources = check_deps_sources,
        transitive_imports = transitive_sources,
        _direct_proto_sources = direct_proto_sources,
        _transitive_proto_sources = transitive_proto_sources,
        _exported_sources = exported_sources,
    )

ProtoInfo, _ = provider(
    doc = "Encapsulates information provided by a `proto_library.`",
    fields = {
        "direct_sources": "(list[File]) The `.proto` source files from the `srcs` attribute.",
        "transitive_sources": """(depset[File]) The `.proto` source files from this rule and all
                    its dependent protocol buffer rules.""",
        "direct_descriptor_set": """(File) The descriptor set of the direct sources. If no srcs,
            contains an empty file.""",
        "transitive_descriptor_sets": """(depset[File]) A set of descriptor set files of all
            dependent `proto_library` rules, and this one's. This is not the same as passing
            --include_imports to proto-compiler. Will be empty if no dependencies.""",
        "proto_source_root": """(str) The directory relative to which the `.proto` files defined in
            the `proto_library` are defined. For example, if this is `a/b` and the rule has the
            file `a/b/c/d.proto` as a source, that source file would be imported as
            `import c/d.proto`

            In principle, the `proto_source_root` directory itself should always
            be relative to the output directory (`ctx.bin_dir` or `ctx.genfiles_dir`).

            This is at the moment not true for `proto_libraries` using (additional and/or strip)
            import prefixes. `proto_source_root` is in this case prefixed with the output
            directory. For example, the value is similar to
            `bazel-out/k8-fastbuild/bin/a/_virtual_includes/b` for an input file in
            `a/_virtual_includes/b/c.proto` that should be imported as `c.proto`.

            When using the value please account for both cases in a general way.
            That is assume the value is either prefixed with the output directory or not.
            This will make it possible to fix `proto_library` in the future.
            """,
        "transitive_proto_path": """(depset(str) A set of `proto_source_root`s collected from the
            transitive closure of this rule.""",
        "check_deps_sources": """(depset[File]) The `.proto` sources from the 'srcs' attribute.
            If the library is a proxy library that has no sources, it contains the
            `check_deps_sources` from this library's direct deps.""",

        # Deprecated fields:
        "transitive_imports": """(depset[File]) Deprecated: use `transitive_sources` instead.""",

        # Internal fields:
        "_direct_proto_sources": """(list[ProtoSourceInfo]) The `ProtoSourceInfo`s from the `srcs`
            attribute.""" + _warning,
        "_transitive_proto_sources": """(depset[ProtoSourceInfo]) The `ProtoSourceInfo`s from this
            rule and all its dependent protocol buffer rules.""" + _warning,
        "_exported_sources": """(depset[ProtoSourceInfo]) A set of `ProtoSourceInfo`s that may be
            imported by another `proto_library` depending on this one.""" + _warning,
    },
    init = _create_proto_info,
)

_ProtoSourceInfo = provider(
    doc = "Represents a single `.proto` source file.",
    fields = {
        "_source_file": """(File) The `.proto` file. Possibly virtual to handle additional/stripped
          path prefix.""" + _warning,
        "_proto_path": "(str) The root of the virtual location." + _warning,
    },
)
