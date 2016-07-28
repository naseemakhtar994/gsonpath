Path Substitution
=
Gson Path provides a mechanism to use basic json path support when using the `@SerializedName` annotation. The aim of the path substitution is to provide flexibility to developers to replace certain placeholders within the path.

Example
-
This may be useful in situations where an API returns nearly identical json, however the keys may differ. The aim of this feature is to reuse as much code as possible, and avoid creating two completely different models, or using inheritance in conjunction with abstract methods.

For example, given the following two similar json responses:

```json
{
  "staff": {
    "name": "John Smith",
    "age": 50
  },
  "address": "123 Fake St"
}
```
```json
{
  "customer": {
    "name": "Jack Torrance",
    "age": 29
  },
  "address": "Overlook Hotel"
}
```

The following base model can be created:

```java
public class ContactDetailsBase {
    @SerializedName("{PERSON_DETAILS_SUB}.")
    public String name;
    
    @SerializedName("{PERSON_DETAILS_SUB}.")
    public int age;
    
    public String address;
}
```

And the two implementing classes would be as follows:

```java
@AutoGsonAdapter(substitutions = {
        @PathSubstitution(original = "PERSON_DETAILS_SUB", replacement = "staff")
})
public class StaffContactDetails extends ContactDetailsBase {
}
```
```java
@AutoGsonAdapter(substitutions = {
        @PathSubstitution(original = "PERSON_DETAILS_SUB", replacement = "customer")
})
public class CustomerContactDetails extends ContactDetailsBase {
}
```