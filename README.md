# Gson Path
An annotation processor library which generates gson type adapters at compile time which also use basic JsonPath functionality.

The benefits of this library are as follows:
- Statically generated type adapters can remove the majority of reflection used by the Gson library.
- JsonPath syntax can reduce the number of POJOs required to parse a JSON file. This provides easier integration with other libraries which rely on a flat class structure (such as DBFlow)

For example, given the following JSON:

```json
{
    "person": {
        "names": {
            "first": "Lachlan",
            "last": "McKee"
        }
    }
}
```

We can deserialize the content with a single class by using Gson Path. The following class demonstrates the annotations required to create a type adapter which can correctly read the content.

```java
@AutoGsonAdapter(rootField = "person.names")
public class PersonModel {
    @GsonPathField(".first")
    String firstName;

    @GsonPathField("last")
    String lastName;
}
```
This will then generate the following Gson TypeAdapter:

```java
public final class PersonModel_GsonTypeAdapter extends TypeAdapter<PersonModel> {
    private final Gson mGson;

    public PersonModel_GsonTypeAdapter(Gson gson) {
        this.mGson = gson;
    }

    @Override
    public PersonModel read(JsonReader in) throws IOException {
        PersonModel result = new PersonModel();
        in.beginObject();
		int counter0 = 0;
        while (in.hasNext()) {
			if (counter0 == 1) {
				in.skipValue();
				continue;
			}
            switch(in.nextName()) {
                case "person":
					counter0++;
                    in.beginObject();
					int counter1 = 0;
                    while (in.hasNext()) {
						if (counter1 == 1) {
							in.skipValue();
							continue;
						}
                        switch(in.nextName()) {
                            case "names":
								counter1++;
                                in.beginObject();
								int counter2 = 0;
                                while (in.hasNext()) {
									if (counter2 == 2) {
										in.skipValue();
										continue;
									}
                                    switch(in.nextName()) {
                                        case "first":
											counter2++;
                                            String safeValue0 = getStringSafely(in);
                                            if (safeValue0 != null) {
                                                result.firstName = safeValue0;
                                            }
                                            break;
                                        case "last":
											counter2++;
                                            String safeValue1 = getStringSafely(in);
                                            if (safeValue1 != null) {
                                                result.lastName = safeValue1;
                                            }
                                            break;
                                        default:
                                            in.skipValue();
                                            break;
                                    }
                                }
                                in.endObject();
                                break;
                            default:
                                in.skipValue();
                                break;
                        }
                    }
                    in.endObject();
                    break;
                default:
                    in.skipValue();
                    break;
            }
        }
        in.endObject();
        return result;
    }

    @Override
    public void write(JsonWriter out, PersonModel value) throws IOException {
        // GsonPath does not support writing at this stage.
    }
}
```

## Download
At the moment the library is not available on Maven, this will change in the future.
For now, you can obtain it via [Jitpack](https://jitpack.io/docs/)

Gradle:
```gradle
compile 'com.github.lachlanm.gsonpath:gsonpath:1.0.5'
apt 'com.github.lachlanm.gsonpath:gsonpath-compiler:1.0.5'
```
