package gsonpath.generated;

import gsonpath.AutoGsonAdapter;
import gsonpath.GsonPathField;

/**
 * Created by Lachlan on 18/03/2016.
 */
@AutoGsonAdapter
public class PeopleModelGenerated {
    public PersonModel[] people;

    @AutoGsonAdapter(rootField = "person.names")
    public static class PersonModel {
        @GsonPathField("first")
        public String firstName;

        @GsonPathField("last")
        public String lastName;
    }
}
