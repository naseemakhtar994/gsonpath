package gsonpath.mandatory;

import gsonpath.AutoGsonAdapter;
import gsonpath.FieldPolicy;

@AutoGsonAdapter(fieldPolicy = FieldPolicy.FAIL_ALL_EXCEPT_OPTIONAL)
public class MandatorySampleModel {
    int test;
}
