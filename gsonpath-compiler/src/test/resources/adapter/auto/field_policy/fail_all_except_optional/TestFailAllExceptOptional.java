package adapter.auto.field_policy.fail_all_except_optional;

import gsonpath.AutoGsonAdapter;
import gsonpath.FieldPolicy;
import gsonpath.Mandatory;
import gsonpath.Optional;

@AutoGsonAdapter(fieldPolicy = FieldPolicy.FAIL_ALL_EXCEPT_OPTIONAL)
public class TestFailAllExceptOptional {
    public int mandatory1;
    public int mandatory2;

    @Optional
    public int optional1;
}