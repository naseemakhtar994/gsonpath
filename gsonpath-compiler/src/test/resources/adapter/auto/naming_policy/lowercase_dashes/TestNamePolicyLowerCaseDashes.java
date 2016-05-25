package adapter.auto.naming_policy.lowercase_dashes;

import gsonpath.AutoGsonAdapter;
import gsonpath.GsonPathFieldNamingPolicy;

@AutoGsonAdapter(fieldNamingPolicy = GsonPathFieldNamingPolicy.LOWER_CASE_WITH_DASHES)
public class TestNamePolicyLowerCaseDashes {
    public int testValue;
}