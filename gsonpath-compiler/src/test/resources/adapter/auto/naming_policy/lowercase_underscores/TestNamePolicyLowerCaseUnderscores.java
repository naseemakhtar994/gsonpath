package adapter.auto.naming_policy.lowercase_underscores;

import gsonpath.AutoGsonAdapter;
import gsonpath.GsonPathFieldNamingPolicy;

@AutoGsonAdapter(fieldNamingPolicy = GsonPathFieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
public class TestNamePolicyLowerCaseUnderscores {
    public int testValue;
}