package adapter.auto.field_policy.fail_mandatory;

import gsonpath.AutoGsonAdapter;
import gsonpath.FieldPolicy;
import gsonpath.Mandatory;

@AutoGsonAdapter(fieldPolicy = FieldPolicy.FAIL_MANDATORY)
public class TestFailMandatory {
    @Mandatory
    public int mandatory1;
    @Mandatory
    public int mandatory2;

    public int optional1;
}