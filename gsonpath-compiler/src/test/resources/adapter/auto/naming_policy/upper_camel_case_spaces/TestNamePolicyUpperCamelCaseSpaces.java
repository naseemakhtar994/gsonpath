package adapter.auto.naming_policy.upper_camel_case_spaces;

import gsonpath.AutoGsonAdapter;
import gsonpath.GsonPathFieldNamingPolicy;

@AutoGsonAdapter(fieldNamingPolicy = GsonPathFieldNamingPolicy.UPPER_CAMEL_CASE_WITH_SPACES)
public class TestNamePolicyUpperCamelCaseSpaces {
    public int testValue;
}