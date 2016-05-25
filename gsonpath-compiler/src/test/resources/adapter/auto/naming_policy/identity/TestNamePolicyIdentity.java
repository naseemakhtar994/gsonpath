package adapter.auto.naming_policy.identity;

import gsonpath.AutoGsonAdapter;
import gsonpath.GsonPathFieldNamingPolicy;

@AutoGsonAdapter(fieldNamingPolicy = GsonPathFieldNamingPolicy.IDENTITY)
public class TestNamePolicyIdentity {
    public int testValue;
}