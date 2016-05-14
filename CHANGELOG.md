Change Log
==========

Version 1.3.0 *(2016-05-14)*
----------------------------

 * New: Added writing support to the generated `TypeAdapter` classes.
    * The `@AutoGsonAdapter` annotation has a new `serializeNulls` property which specifies whether nulls are written to the document. (The same way Gson handles this)
 * New: Added a mandatory field concept which will raise exceptions for missing JSON content
    * Two annotations were added to solve this issue: `@Mandatory` and `@Optional`
        * Since these annotations can be used on primitives, it didn't make sense to use `@Nullable` and `Nonnull` at this stage.
    * The `@AutoGsonAdapter` annotation has a new `fieldPolicy` property which specifies how the generated `TypeAdapter` handles mandatory content. It is an enum with the following values:
        * NEVER_FAIL - No fields are mandatory, and the Gson parser should never raise exceptions for missing content.
        * FAIL_MANDATORY - Any field marked with `@Mandatory` should fail if the value does not exist within the JSON.
        * FAIL_ALL_EXCEPT_OPTIONAL - All fields will be treated as `@Mandatory`, and should fail when content is not found, unless the field is annotation with `@Optional`.
 * Fix: Annotation processor messages are clearer and inform when it has started and completed.
 * Fix: Better error handling for invalid scenarios, such as duplicate JSON field names.
 