package adapter.auto.field_policy.never_fail;

import gsonpath.AutoGsonAdapter;
import gsonpath.FieldPolicy;
import gsonpath.Mandatory;
import gsonpath.Optional;

@AutoGsonAdapter(fieldPolicy = FieldPolicy.NEVER_FAIL)
public class TestNeverFail {
    @Mandatory // This annotation is ignored for 'NEVER_FAIL'
    public int optional1;
    @Optional // This annotation is ignored for 'NEVER_FAIL'
    public int optional2;

    public int optional3;
}