package gsonpath.generated;

import com.google.gson.annotations.SerializedName;
import gsonpath.AutoGsonAdapter;

/**
 * Created by Lachlan on 18/03/2016.
 */
@AutoGsonAdapter
public class PeopleModelGenerated {
    public PersonModel[] people;

    @AutoGsonAdapter(rootField = "person.names")
    public static class PersonModel {
        @SerializedName("first")
        public String firstName;

        @SerializedName("last")
        public String lastName;
    }
}
