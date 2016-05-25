Change Log
==========

Version 1.4.0 *(2016-05-26)*
----------------------------

 * New: Made changes to the mandatory field feature to make it more useful. The changes are described below:
     * Removed `@Mandatory` and `@Optional`. Instead the annotation processor will attempt to find any `@Nullable` or `Nonnull` (as well as other permutations such as `NonNull`, `NotNull` and `Notnull`) annotations, and use those instead.
     * The `@AutoGsonAdapter` annotation property `fieldPolicy` has been renamed to `fieldValidationType` to be more clear.
     * The fieldValidationType enum values have the same behaviour as before, however they have been renamed to clearer as well:
        * NO_VALIDATION - No fields are validated, and the Gson parser should never raise exceptions for missing content.
        * VALIDATE_EXPLICIT_NON_NULL - Any Objects marked with `@NonNull` (or similar), or primitives should fail if the value does not exist within the JSON.
        * VALIDATE_ALL_EXCEPT_NULLABLE - All fields will be treated as `@NonNull`, and should fail when value is not found, unless the field is annotation with `@Nullable` (except for primitives).
 * New: Added a default configuration concept for the `@AutoGsonAdapter` annotation.
     * Allows developers to set default values for the annotation if they are unhappy with the default values provided within the annotation.
        * Some developers may not like the '.' character being used as a delimiter, and wish to make the change everyone in one place.
     * To use this feature, developers must:
        * Annotate a class with the new `@GsonPathDefaultConfiguration` annotation and specify specify default values
        * Point the `defaultConfiguration` property within the `@AutoGsonAdapter` annotation to this class on every usage of `@AutoGsonAdapter`
     * See the annotation javadoc for further details.

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
 