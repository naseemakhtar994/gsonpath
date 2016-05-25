package adapter.auto.naming_policy.upper_camel_case;

import gsonpath.AutoGsonAdapter;
import gsonpath.GsonPathFieldNamingPolicy;

@AutoGsonAdapter(fieldNamingPolicy = GsonPathFieldNamingPolicy.UPPER_CAMEL_CASE)
public class TestNamePolicyUpperCamelCase {
    public int testValue;
}